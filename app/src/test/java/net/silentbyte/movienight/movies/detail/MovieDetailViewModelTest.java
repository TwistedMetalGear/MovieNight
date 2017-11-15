package net.silentbyte.movienight.movies.detail;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.data.MovieRepository;
import net.silentbyte.movienight.util.TestHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MovieDetailViewModelTest {

    private MovieDetailViewModel viewModel;
    private Movie movie;

    @Mock
    private Application application;
    @Mock
    private MovieRepository repository;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Before
    public void createViewModel() {
        viewModel = new MovieDetailViewModel(application, repository);
    }

    @Before
    public void createMovie() {
        movie = new Movie();
        movie.setId(1);
        movie.setTitle("A");
    }

    @Before
    public void initializeRxSchedulers() {
        TestHelper.initializeRxSchedulers();
    }

    @Test
    public void getMovieById_returnsMovie() {
        getMovieById_returnsMovie_execute();
    }

    @Test
    public void getMovieById_errorOccurs() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<Movie> single = Single.create(emitter -> {
            throw new Exception("");
        });
        when(repository.getMovieById(1)).thenReturn(single);

        viewModel.getMovieById(1);

        verify(repository, times(1)).getMovieById(1);
        assertNull(viewModel.movie.get());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void getMovieById_loadingInProgress() {
        viewModel.loading.set(true);
        viewModel.getMovieById(1);
        verify(repository, never()).getMovieById(1);
    }

    @Test
    public void getMovieById_errorShowing() {
        viewModel.error.set(true);
        viewModel.getMovieById(1);
        verify(repository, never()).getMovieById(1);
    }

    @Test
    public void getMovieById_movieAlreadyLoaded() {
        getMovieById_returnsMovie_execute();
        viewModel.getMovieById(1);
        verify(repository, times(1)).getMovieById(1);
    }

    @Test
    public void onSaveMovieClick_savesMovie() {
        getMovieById_returnsMovie_execute();

        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<Boolean> single = Single.create(emitter -> emitter.onSuccess(true));
        when(repository.insertMovie(movie)).thenReturn(single);

        viewModel.userRating.set(8.5f);
        viewModel.userReview.set("test review");
        viewModel.onSaveMovieClick();

        verify(repository, times(1)).insertMovie(movie);

        assertEquals(8.5, viewModel.movie.get().getUserRating(), 0);
        assertEquals("test review", viewModel.movie.get().getUserReview());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void onSaveMovieClick_errorOccurs() {
        getMovieById_returnsMovie_execute();

        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Observer observer = mock(Observer.class);
        viewModel.getSaveMovieErrorEvent().observeForever(observer);

        Single<Boolean> single = Single.create(emitter -> {
            throw new Exception("");
        });
        when(repository.insertMovie(movie)).thenReturn(single);

        viewModel.onSaveMovieClick();

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));

        verify(observer, times(1)).onChanged(null);
    }

    @Test
    public void onSaveMovieClick_movieNotLoaded() {
        viewModel.onSaveMovieClick();
        verify(repository, never()).insertMovie(any(Movie.class));
    }

    @Test
    public void onSaveMovieClick_loadingInProgress() {
        getMovieById_returnsMovie_execute();

        viewModel.loading.set(true);
        viewModel.onSaveMovieClick();
        verify(repository, never()).insertMovie(movie);
    }

    @Test
    public void onSaveMovieClick_errorShowing() {
        getMovieById_returnsMovie_execute();

        viewModel.error.set(true);
        viewModel.onSaveMovieClick();
        verify(repository, never()).insertMovie(movie);
    }

    @Test
    public void onUserRatingChanged() {
        MovieDetailViewModel viewModelSpy = spy(viewModel);

        List<Boolean> modifiedValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModelSpy.modified, modifiedValues);

        viewModelSpy.onUserRatingChanged(8.5f);

        verify(viewModelSpy, times(1)).setUserRating(8.5f);
        assertEquals(1, modifiedValues.size());
        assertTrue(modifiedValues.get(0));
    }

    @Test
    public void onUserReviewChanged_updatedReview() {
        getMovieById_returnsMovie_execute();

        List<String> userReviewValues = new ArrayList<>();
        TestHelper.observeString(viewModel.userReview, userReviewValues);

        List<Boolean> modifiedValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.modified, modifiedValues);

        viewModel.onUserReviewChanged("test review");

        assertEquals(1, userReviewValues.size());
        assertEquals("test review", userReviewValues.get(0));

        assertEquals(1, modifiedValues.size());
        assertTrue(modifiedValues.get(0));
    }

    @Test
    public void onUserReviewChanged_sameReview() {
        getMovieById_returnsMovie_execute();

        List<String> userReviewValues = new ArrayList<>();
        TestHelper.observeString(viewModel.userReview, userReviewValues);

        List<Boolean> modifiedValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.modified, modifiedValues);

        movie.setUserReview("test review");
        viewModel.onUserReviewChanged("test review");

        assertEquals(1, userReviewValues.size());
        assertEquals("test review", userReviewValues.get(0));

        assertTrue(modifiedValues.isEmpty());
    }

    @Test
    public void onUserReviewChanged_movieNotLoaded() {
        List<String> userReviewValues = new ArrayList<>();
        TestHelper.observeString(viewModel.userReview, userReviewValues);

        List<Boolean> modifiedValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.modified, modifiedValues);

        viewModel.onUserReviewChanged("test review");

        assertTrue(userReviewValues.isEmpty());
        assertTrue(modifiedValues.isEmpty());
    }

    @Test
    public void setUserRating() {
        List<Float> userRatingValues = new ArrayList<>();
        TestHelper.observeFloat(viewModel.userRating, userRatingValues);

        List<String> formattedUserRatingValues = new ArrayList<>();
        TestHelper.observeString(viewModel.formattedUserRating, formattedUserRatingValues);

        viewModel.setUserRating(8.5f);
        assertEquals(1, userRatingValues.size());
        assertEquals(8.5, userRatingValues.get(0), 0);
        assertEquals(1, formattedUserRatingValues.size());
        assertEquals("8.5", formattedUserRatingValues.get(0));

        viewModel.setUserRating(0);
        assertEquals(2, userRatingValues.size());
        assertEquals(0, userRatingValues.get(1), 0);
        assertEquals(2, formattedUserRatingValues.size());
        assertEquals("0", formattedUserRatingValues.get(1));

        viewModel.setUserRating(10);
        assertEquals(3, userRatingValues.size());
        assertEquals(10, userRatingValues.get(2), 0);
        assertEquals(3, formattedUserRatingValues.size());
        assertEquals("10", formattedUserRatingValues.get(2));
    }

    @Test
    public void invalidateCache() {
        getMovieById_returnsMovie_execute();

        List<Integer> movieIds = new ArrayList<>();
        when(repository.invalidateCache(movieIds)).thenReturn(true);

        viewModel.invalidateCache(movieIds);
        viewModel.getMovieById(1);

        verify(repository, times(1)).invalidateCache(movieIds);
        verify(repository, times(2)).getMovieById(1);
    }

    @Test
    public void onRetryClick() {
        getMovieById_returnsMovie_execute();

        viewModel.onRetryClick();
        viewModel.getMovieById(1);

        verify(repository, times(2)).getMovieById(1);
    }

    private void getMovieById_returnsMovie_execute() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<Movie> single = Single.create(emitter -> emitter.onSuccess(movie));
        when(repository.getMovieById(1)).thenReturn(single);

        viewModel.getMovieById(1);

        verify(repository, times(1)).getMovieById(1);
        assertEquals(1, viewModel.movie.get().getId());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }
}