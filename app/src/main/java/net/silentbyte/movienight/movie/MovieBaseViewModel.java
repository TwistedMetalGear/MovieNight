package net.silentbyte.movienight.movie;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;

import net.silentbyte.movienight.data.MovieRepository;
import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.util.SingleLiveData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Base ViewModel for MovieListViewModel and MovieDetailViewModel. Implements common functionality.
 */
public abstract class MovieBaseViewModel extends AndroidViewModel {

    public final ObservableBoolean error = new ObservableBoolean(false);

    private final SingleLiveData<List<Movie>> querySuggestions = new SingleLiveData<>();
    private final SingleLiveData<Void> retryEvent = new SingleLiveData<>();

    private final PublishSubject<String> searchThrottler = PublishSubject.create();

    protected final MovieRepository repository;

    private Disposable searchThrottlerDisposable;

    public MovieBaseViewModel(Application application, MovieRepository repository) {
        super(application);
        this.repository = repository;
        subscribeToSearchThrottler();
    }

    public void onQueryTextChange(String query) {
        query = query.trim();

        if (!query.isEmpty()) {
            searchThrottler.onNext(query);
        }
    }

    public SingleLiveData<List<Movie>> getQuerySuggestions() {
        return querySuggestions;
    }

    public void onRetryClick() {
        error.set(false);
        retryEvent.call();
    }

    public SingleLiveData<Void> getRetryEvent() {
        return retryEvent;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        searchThrottlerDisposable.dispose();
    }

    private void subscribeToSearchThrottler() {
        // No need to handle error here. In the unlikely event of an error, the worst that
        // happens is that suggestions won't appear. An empty error handler is still attached
        // so that exceptions related to retrieving suggestions (e.g. an HTTP 429 response due
        // to excessive requests to TMDb) don't crash the app.
        searchThrottlerDisposable = searchThrottler
                .debounce(400, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .flatMapSingle(repository::searchBasic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(querySuggestions::setValue, error -> {
                });
    }
}
