package net.silentbyte.movienight.data;

import android.arch.persistence.room.EmptyResultSetException;

import net.silentbyte.movienight.data.source.local.MovieDatabase;
import net.silentbyte.movienight.data.source.remote.MovieSearcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;

/**
 * Provides local access to a database of saved movies and remote access to TMDb to search for movies.
 * Maintains a cache of results to prevent unnecessary queries and to provide instant results upon config change.
 */
public class MovieRepository {

    private final MovieDatabase movieDatabase; // Local database
    private final MovieSearcher movieSearcher; // Remote TMDb
    private List<Movie> movieCache; // Local in-memory cache

    @Inject
    public MovieRepository(MovieDatabase movieDatabase, MovieSearcher movieSearcher) {
        this.movieDatabase = movieDatabase;
        this.movieSearcher = movieSearcher;
    }

    /**
     * Returns a Single that can be subscribed to in order to get a list of movies from the local database.
     */
    public Single<List<Movie>> getSavedMovies() {
        if (movieCache != null) {
            // Cache is available. No need to retrieve movies from database.
            return Single.just(new ArrayList<>(movieCache));
        }

        // Retrieve movies from database and set result on cache.
        return movieDatabase.movieDao().getMovies()
                .flatMap(savedMovies -> {
                    refreshCache(savedMovies);
                    return Single.just(savedMovies);
                });
    }

    /**
     * Returns a Single that can be subscribed to in order to search for movies matching the specified
     * title. Emits a list of movies with limited detail. This is to be used specifically for search
     * suggestions. This will be called multiple times as the user types, providing a new search result
     * each time, so there is no need to use a cache here. Also, the Android search view already takes
     * care of caching the most recent suggestions.
     */
    public Single<List<Movie>> searchBasic(String title) {
        return movieSearcher.searchBasic(title);
    }

    /**
     * Returns a Single that can be subscribed to in order to search for movies matching the specified
     * title. Emits a list of movies with full detail. This method should be called only when a fully
     * detailed list of movies is needed, as it will hit the TMDb servers with multiple HTTP requests,
     * possibly exceeding the request rate limit.
     */
    public Single<List<Movie>> searchDetailed(String title) {
        if (movieCache != null) {
            // Cache is available. No need to query TMDb.
            return Single.just(new ArrayList<>(movieCache));
        }

        // This will first search TMDb for movies matching the specified title.
        // For each result, it will check to see if that movie is in the local database.
        // If so, replace the movie in the result list with the one from the local database.
        return movieSearcher.searchDetailed(title)
                .flatMap(movies -> {
                    return movieDatabase.movieDao().getMovies()
                            .map(savedMovies -> {
                                for (int i = 0; i < movies.size(); i++) {
                                    int index = savedMovies.indexOf(movies.get(i));

                                    if (index != -1) {
                                        movies.set(i, savedMovies.get(index));
                                    }
                                }

                                refreshCache(movies);
                                return movies;
                            });
                });
    }

    /**
     * Returns a Single that can be subscribed to in order to retrieve a movie with the specified id.
     * This will first attempt to retrieve it from the local database. If it doesn't exist, it will
     * retrieve it from TMDb.
     */
    public Single<Movie> getMovieById(int movieId) {
        if (movieCache != null) {
            return Single.just(movieCache.get(0));
        }

        return movieDatabase.movieDao().getMovieById(movieId)
                .onErrorResumeNext(error -> {
                    if (error instanceof EmptyResultSetException) {
                        return Single.just(new Movie());
                    }

                    throw new Exception(error);
                })
                .flatMap(savedMovie -> {
                    if (savedMovie.getId() == 0) {
                        return movieSearcher.getMovieDetailed(movieId)
                                .flatMap(movie -> {
                                    addToCache(movie);
                                    return Single.just(movie);
                                });
                    }
                    else {
                        addToCache(savedMovie);
                        return Single.just(savedMovie);
                    }
                });
    }

    /**
     * Returns a Single that can be subscribed to in order to insert the specified movie into the
     * local database. Emits true if a new movie was inserted, or false if the movie was replaced.
     */
    public Single<Boolean> insertMovie(Movie movie) {
        return movieDatabase.movieDao().getMovieById(movie.getId())
                .onErrorResumeNext(error -> {
                    // TODO: Probably not the most elegant solution to signal that there was no match in the DB. Is there a better way?
                    if (error instanceof EmptyResultSetException) {
                        return Single.just(new Movie());
                    }

                    throw new Exception(error);
                })
                .flatMap(savedMovie -> {
                    return Single.create(emitter -> {
                        movie.setUpdateTime(System.currentTimeMillis() / 1000);
                        movieDatabase.movieDao().insertMovie(movie);
                        addToCache(movie);

                        if (savedMovie.getId() == 0) {
                            emitter.onSuccess(true);
                        }
                        else {
                            emitter.onSuccess(false);
                        }
                    });
                });
    }

    /**
     * Returns a Single that can be subscribed to in order to delete the specified movie from the
     * local database and cache. Emits the updated cache.
     */
    public Single<List<Movie>> deleteMovie(Movie movie) {
        return Single.create(emitter -> {
            movieDatabase.movieDao().deleteMovie(movie.getId());
            movieCache.remove(movie);
            emitter.onSuccess(movieCache);
        });
    }

    /**
     * Returns a Single that can be subscribed to in order to restore a movie that has been deleted
     * from the local database and cache. Emits the updated cache.
     */
    public Single<List<Movie>> restoreMovie(Movie movie) {
        return Single.create(emitter -> {
            movieDatabase.movieDao().insertMovie(movie);
            addToCache(movie);
            emitter.onSuccess(movieCache);
        });
    }

    /**
     * Invalidates the cache (sets it to null) so that subsequent requests are freshly loaded from
     * the local database and/or TMDb.
     */
    public void invalidateCache() {
        movieCache = null;
    }

    /**
     * Invalidates the cache (sets it to null) if it contains any of the specified movie ids.
     * Returns true if cache has been invalidated (i.e. cache is null), false otherwise.
     */
    public boolean invalidateCache(List<Integer> movieIds) {
        if (movieCache == null) {
            return true;
        }

        for (Movie movie : movieCache) {
            if (movieIds.contains(movie.getId())) {
                movieCache = null;
                return true;
            }
        }

        return false;
    }

    /**
     * Clears the cache and adds the movies from the specified list.
     */
    private void refreshCache(List<Movie> movies) {
        if (movieCache == null) {
            movieCache = new ArrayList<>();
        }

        movieCache.clear();
        movieCache.addAll(movies);
    }

    /**
     * Adds the specified movie to the cache.
     */
    private void addToCache(Movie movie) {
        if (movieCache == null) {
            movieCache = new ArrayList<>();
        }

        movieCache.add(0, movie);
    }
}
