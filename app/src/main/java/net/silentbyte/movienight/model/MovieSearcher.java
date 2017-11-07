package net.silentbyte.movienight.model;

import net.silentbyte.movienight.tmdb.MovieApi;
import net.silentbyte.movienight.tmdb.MovieBasic;
import net.silentbyte.movienight.tmdb.MovieDetailed;
import net.silentbyte.movienight.tmdb.ReleaseDates;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * This class contains a set of methods to search and retrieve movies from the TMDb API server.
 */
@Singleton
public class MovieSearcher
{
    private MovieApi movieApi;

    @Inject
    public MovieSearcher(MovieApi movieApi)
    {
        this.movieApi = movieApi;
    }

    /**
     * Returns a Single that can be subscribed to in order to get a list of Movies with limited detail.
     * While this won't populate the Movie objects with a full set of data, it only requires one HTTP request
     * to the TMDb API server which helps to avoid hitting the request rate limit.
     */
    public Single<List<Movie>> searchBasic(String title)
    {
        return movieApi.searchForMovie(title)
            .map((movieSearchResponse) ->
            {
                List<MovieBasic> results = movieSearchResponse.getResults();
                List<Movie> movies = new ArrayList<>();

                for (int i = 0; i < 8 && i < results.size(); i++)
                {
                    MovieBasic result = results.get(i);
                    Movie movie = new Movie();

                    movie.setId(result.getId());
                    movie.setTitle(result.getTitle());
                    movie.setOverview(result.getOverview());
                    movie.setPosterPath(result.getPosterPath());
                    movie.setReleaseDate(result.getReleaseDate());
                    movie.setLanguage(result.getOriginalLanguage());
                    movie.setCommunityRating(result.getVoteAverage());
                    movie.setVoteCount(result.getVoteCount());

                    movies.add(movie);
                }

                return movies;
            });
    }

    /**
     * Returns a Single that can be subscribed to in order to get a list of Movies with full detail.
     * This method will perform numerous HTTP requests to the TMDb API server and should be used
     * sparingly to avoid hitting the request rate limit (40 requests every 10 seconds). Because of
     * the request rate limit, this method has been limited to retrieving 8 movies max.
     */
    public Single<List<Movie>> searchDetailed(String title)
    {
        return movieApi.searchForMovie(title)
            .flatMapObservable(movieSearchResponse ->
            {
                List<Integer> movieIds = new ArrayList<Integer>();
                List<MovieBasic> movies = movieSearchResponse.getResults();

                for (int i = 0; i < 8 && i < movies.size(); i++)
                {
                    movieIds.add(movies.get(i).getId());
                }

                return Observable.fromIterable(movieIds);
            })
            .flatMapSingle(this::getMovieDetailed)
            .toList();
    }

    /**
     * Returns an Observable that can be subscribed to in order to get a fully detailed Movie object.
     */
    public Single<Movie> getMovieDetailed(int movieId)
    {
        return movieApi.getMovieDetail(movieId)
            .map(movieDetailed ->
            {
                Movie movie = new Movie();

                movie.setId(movieDetailed.getId());
                movie.setTitle(movieDetailed.getTitle());
                movie.setOverview(movieDetailed.getOverview());
                movie.setPosterPath(movieDetailed.getPosterPath());
                movie.setReleaseDate(movieDetailed.getReleaseDate());
                movie.setLanguage(movieDetailed.getOriginalLanguage());
                movie.setRuntime(movieDetailed.getRuntime());
                movie.setCommunityRating(movieDetailed.getVoteAverage());
                movie.setVoteCount(movieDetailed.getVoteCount());

                // Attempt to extract the US certification and set it on the builder.
                for (ReleaseDates.Country country : movieDetailed.getReleaseDates().getResults())
                {
                    if (country.getIso().equals("US"))
                    {
                        for (ReleaseDates.ReleaseDate releaseDate : country.getReleaseDates())
                        {
                            if (!releaseDate.getCertification().isEmpty())
                            {
                                movie.setCertification(releaseDate.getCertification());
                                break;
                            }
                        }
                    }
                }

                // Attempt to extract the genre and set it on the builder.
                // StringJoiner would be nice here, but it's only available on API level 24+
                StringBuilder sb = new StringBuilder();

                for (MovieDetailed.IdName genre : movieDetailed.getGenres())
                {
                    if (!genre.getName().isEmpty())
                        sb.append(genre.getName()).append(", ");
                }

                if (sb.length() > 0)
                {
                    sb.setLength(sb.length() - 2);
                    movie.setGenre(sb.toString());
                }

                return movie;
            });
    }
}
