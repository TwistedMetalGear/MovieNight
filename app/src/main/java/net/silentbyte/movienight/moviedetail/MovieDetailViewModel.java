package net.silentbyte.movienight.moviedetail;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;

import net.silentbyte.movienight.moviecommon.MovieBaseViewModel;
import net.silentbyte.movienight.data.MovieRepository;
import net.silentbyte.movienight.moviecommon.SingleLiveData;
import net.silentbyte.movienight.data.Movie;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ViewModel for MovieDetailFragment. Provides access to MovieRepository for retrieving
 * and saving movies. Exposes observable data for the View to react to.
 */
public class MovieDetailViewModel extends MovieBaseViewModel {

    public final ObservableField<Movie> movie = new ObservableField<>();
    public final ObservableFloat userRating = new ObservableFloat();
    public final ObservableField<String> formattedUserRating = new ObservableField<>();
    public final ObservableField<String> userReview = new ObservableField<>();
    public final ObservableBoolean modified = new ObservableBoolean(false);
    public final ObservableBoolean loading = new ObservableBoolean(false);

    private final SingleLiveData<Boolean> saveMovieEvent = new SingleLiveData<>();
    private final SingleLiveData<Void> saveMovieErrorEvent = new SingleLiveData<>();

    private boolean movieLoaded = false;

    private Disposable disposable;

    public MovieDetailViewModel(Application application, MovieRepository repository) {
        super(application, repository);
    }

    public void getMovieById(int movieId) {
        if (loading.get() == true || error.get() == true || movieLoaded) {
            return;
        }

        loading.set(true);

        disposable = repository.getMovieById(movieId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movie -> {
                            loading.set(false);
                            movieLoaded = true;

                            this.movie.set(movie);

                            // The user rating and review will initially be set to the corresponding
                            // values from the movie. If the user modifies these values, they will
                            // be saved to and restored from the MovieDetailFragment's bundle.
                            if (modified.get() == false) {
                                setUserRating(movie.getUserRating());
                                userReview.set(movie.getUserReview());
                            }
                        },
                        error -> {
                            loading.set(false);
                            this.error.set(true);
                        });
    }

    public void onSaveMovieClick() {
        if (loading.get() == true || error.get() == true || !movieLoaded) {
            return;
        }

        loading.set(true);

        movie.get().setUserRating(userRating.get());
        movie.get().setUserReview(userReview.get());

        disposable = repository.insertMovie(movie.get())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            loading.set(false);
                            saveMovieEvent.setValue(result);
                        }
                        ,
                        error -> {
                            loading.set(false);
                            saveMovieErrorEvent.call();
                        });
    }

    public void onUserRatingChanged(float rating) {
        setUserRating(rating);
        modified.set(true);
    }

    public void onUserReviewChanged(String review) {
        // The review EditText may trigger onTextChanged before the movie is loaded.
        // onTextChanged will lead here so we need to make sure the movie is loaded
        // before continuing.
        if (movie.get() != null) {
            userReview.set(review);

            // Since this method triggers upon config change, make sure the review
            // actually changed before setting the modified flag.
            if (!userReview.get().equals(movie.get().getUserReview())) {
                modified.set(true);
            }
        }
    }

    public SingleLiveData<Boolean> getSaveMovieEvent() {
        return saveMovieEvent;
    }

    public SingleLiveData<Void> getSaveMovieErrorEvent() {
        return saveMovieErrorEvent;
    }

    /**
     * Sets both userRating and formattedUserRating.
     */
    public void setUserRating(float rating) {
        userRating.set(rating);

        if (rating == 0) {
            formattedUserRating.set("0");
        }
        else if (rating == 10) {
            formattedUserRating.set("10");
        }
        else {
            formattedUserRating.set(String.valueOf(rating));
        }
    }

    public void invalidateCache(List<Integer> movieIds) {
        if (repository.invalidateCache(movieIds)) {
            modified.set(false);
            movieLoaded = false;
        }
    }

    @Override
    public void onRetryClick() {
        movieLoaded = false;
        super.onRetryClick();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
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
            return (T) new MovieDetailViewModel(application, repository);
        }
    }
}
