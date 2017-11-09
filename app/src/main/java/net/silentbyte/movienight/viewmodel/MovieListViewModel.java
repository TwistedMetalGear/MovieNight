package net.silentbyte.movienight.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;

import net.silentbyte.movienight.MovieSorter;
import net.silentbyte.movienight.MovieSorter.SortMethod;
import net.silentbyte.movienight.R;
import net.silentbyte.movienight.model.MovieRepository;
import net.silentbyte.movienight.SingleLiveData;
import net.silentbyte.movienight.model.Movie;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for MovieListFragment. Provides access to MovieRepository for retrieving
 * saved movies and searching for movies on TMDb. Exposes observable data for the View
 * to react to.
 */
public class MovieListViewModel extends MovieBaseViewModel {

    public final ObservableList<Movie> movies = new ObservableArrayList<>();
    public final ObservableBoolean loading = new ObservableBoolean(false);
    public final ObservableField<String> emptyListMessage = new ObservableField<>();

    private final SingleLiveData<Void> addMovieEvent = new SingleLiveData<>();
    private final SingleLiveData<Void> deleteMovieEvent = new SingleLiveData<>();
    private final SingleLiveData<Void> deleteMovieErrorEvent = new SingleLiveData<>();
    private final SingleLiveData<Void> undoDeleteEvent = new SingleLiveData<>();
    private final SingleLiveData<Void> undoDeleteErrorEvent = new SingleLiveData<>();

    private SortMethod sortMethod = SortMethod.NONE;
    private Movie deletedMovie;

    private final CompositeDisposable disposables = new CompositeDisposable();

    public MovieListViewModel(Application application, MovieRepository repository) {
        super(application, repository);
    }

    /**
     * Retrieves saved movies from SQLite database.
     */
    public void getSavedMovies() {
        if (loading.get() == true || error.get() == true) {
            return;
        }

        loading.set(true);

        disposables.add(repository.getSavedMovies()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                            loading.set(false);
                            refreshMovies(movies, R.string.no_saved_movies);
                        },
                        error -> {
                            loading.set(false);
                            this.error.set(true);
                        }));
    }

    /**
     * Searches TMDb for movies matching the query.
     */
    public void search(String query) {
        query = query.trim();

        if (loading.get() == true || error.get() == true || query.isEmpty()) {
            return;
        }

        loading.set(true);

        disposables.add(repository.searchDetailed(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                            loading.set(false);
                            refreshMovies(movies, R.string.no_search_results);
                        },
                        error -> {
                            loading.set(false);
                            this.error.set(true);
                        }));
    }

    public void setSortMethod(SortMethod sortMethod) {
        this.sortMethod = sortMethod;
    }

    public void onAddMovieClick() {
        addMovieEvent.call();
    }

    public SingleLiveData<Void> getAddMovieEvent() {
        return addMovieEvent;
    }

    public void deleteMovie(Movie movie) {
        disposables.add(repository.deleteMovie(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                            // Hold a reference to the deleted movie in case the user decides to undo.
                            deletedMovie = movie;

                            refreshMovies(movies, R.string.no_saved_movies);
                            deleteMovieEvent.call();
                        },
                        error -> {
                            loading.set(false);

                            // Trigger an update on the movies list in order to refresh the RecyclerView.
                            // This restores the view that was swiped away.
                            movies.set(0, movies.get(0));

                            deleteMovieErrorEvent.call();
                        }));
    }

    public void undoDeleteMovie() {
        // deletedMovie should never be null when this is called, but just in case...
        if (deletedMovie == null) {
            undoDeleteErrorEvent.call();
            return;
        }

        disposables.add(repository.restoreMovie(deletedMovie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movies -> {
                            refreshMovies(movies, R.string.no_saved_movies);
                            undoDeleteEvent.call();
                        },
                        error -> undoDeleteErrorEvent.call()));
    }

    public SingleLiveData<Void> getDeleteMovieEvent() {
        return deleteMovieEvent;
    }

    public SingleLiveData<Void> getDeleteMovieErrorEvent() {
        return deleteMovieErrorEvent;
    }

    public SingleLiveData<Void> getUndoDeleteEvent() {
        return undoDeleteEvent;
    }

    public SingleLiveData<Void> getUndoDeleteErrorEvent() {
        return undoDeleteErrorEvent;
    }

    public void invalidateCache() {
        repository.invalidateCache();
    }

    public void invalidateCache(List<Integer> movieIds) {
        repository.invalidateCache(movieIds);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }

    private void refreshMovies(List<Movie> movies, int emptyListResourceId) {
        if (movies.isEmpty()) {
            emptyListMessage.set(getApplication().getString(emptyListResourceId));
        }

        MovieSorter.sort(movies, sortMethod);
        this.movies.clear();
        this.movies.addAll(movies);
    }

    @Singleton
    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Application application;
        private final MovieRepository repository;

        @Inject
        public Factory(Application application, MovieRepository repository) {
            this.application = application;
            this.repository = repository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new MovieListViewModel(application, repository);
        }
    }
}
