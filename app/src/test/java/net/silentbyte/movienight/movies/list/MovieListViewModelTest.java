package net.silentbyte.movienight.movies.list;

import android.app.Application;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.data.MovieRepository;
import net.silentbyte.movienight.util.MovieSorter.SortMethod;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MovieListViewModelTest {

    private static final String QUERY = "test query";

    private MovieListViewModel viewModel;
    private List<Movie> localMovies;
    private List<Movie> remoteMovies;

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
        viewModel = new MovieListViewModel(application, repository);
    }

    @Before
    public void createMovieLists() {
        localMovies = TestHelper.getLocalMovies();
        remoteMovies = TestHelper.getRemoteMovies();
    }

    @Before
    public void initializeRxSchedulers() {
        TestHelper.initializeRxSchedulers();
    }

    @Test
    public void getSavedMovies_returnsPopulatedList() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(localMovies));
        when(repository.getSavedMovies()).thenReturn(single);

        viewModel.getSavedMovies();
        verify(repository, times(1)).getSavedMovies();

        // Verify that movies list has been populated with the expected movies.
        assertEquals(3, viewModel.movies.size());
        assertEquals(1, viewModel.movies.get(0).getId());
        assertEquals(2, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void getSavedMovies_returnsEmptyList() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(new ArrayList<>()));
        when(repository.getSavedMovies()).thenReturn(single);

        viewModel.getSavedMovies();
        verify(repository, times(1)).getSavedMovies();

        assertTrue(viewModel.movies.isEmpty());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void getSavedMovies_errorOccurs() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        List<Boolean> errorValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.error, errorValues);

        Single<List<Movie>> single = Single.create(emitter -> {
            throw new Exception("");
        });

        when(repository.getSavedMovies()).thenReturn(single);

        viewModel.getSavedMovies();
        verify(repository, times(1)).getSavedMovies();

        assertTrue(viewModel.movies.isEmpty());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));

        assertEquals(1, errorValues.size());
        assertTrue(errorValues.get(0));
    }

    @Test
    public void getSavedMovies_loadingInProgress() {
        viewModel.loading.set(true);
        viewModel.getSavedMovies();
        verify(repository, never()).getSavedMovies();
    }

    @Test
    public void getSavedMovies_errorShowing() {
        viewModel.error.set(true);
        viewModel.getSavedMovies();
        verify(repository, never()).getSavedMovies();
    }

    @Test
    public void search_returnsPopulatedList() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(remoteMovies));
        when(repository.searchDetailed(QUERY)).thenReturn(single);

        viewModel.search(QUERY);
        verify(repository, times(1)).searchDetailed(QUERY);

        assertEquals(3, viewModel.movies.size());
        assertEquals(1, viewModel.movies.get(0).getId());
        assertEquals(4, viewModel.movies.get(1).getId());
        assertEquals(5, viewModel.movies.get(2).getId());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void search_returnsEmptyList() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(new ArrayList<>()));
        when(repository.searchDetailed(QUERY)).thenReturn(single);

        viewModel.search(QUERY);
        verify(repository, times(1)).searchDetailed(QUERY);

        assertTrue(viewModel.movies.isEmpty());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));
    }

    @Test
    public void search_errorOccurs() {
        List<Boolean> loadingValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.loading, loadingValues);

        List<Boolean> errorValues = new ArrayList<>();
        TestHelper.observeBoolean(viewModel.error, errorValues);

        Single<List<Movie>> single = Single.create(emitter -> {
            throw new Exception("");
        });
        when(repository.searchDetailed(QUERY)).thenReturn(single);

        viewModel.search(QUERY);
        verify(repository, times(1)).searchDetailed(QUERY);

        assertTrue(viewModel.movies.isEmpty());

        assertEquals(2, loadingValues.size());
        assertTrue(loadingValues.get(0));
        assertFalse(loadingValues.get(1));

        assertEquals(1, errorValues.size());
        assertTrue(errorValues.get(0));
    }

    @Test
    public void search_loadingInProgress() {
        viewModel.loading.set(true);
        viewModel.search(QUERY);
        verify(repository, never()).searchDetailed(QUERY);
    }

    @Test
    public void search_errorShowing() {
        viewModel.error.set(true);
        viewModel.search(QUERY);
        verify(repository, never()).searchDetailed(QUERY);
    }

    @Test
    public void setSortMethod_sortsSavedMovies() {
        List<Movie> movies = TestHelper.getUnsortedMovies();
        viewModel.setSortMethod(SortMethod.TITLE);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(movies));
        when(repository.getSavedMovies()).thenReturn(single);

        viewModel.getSavedMovies();

        // Verify that movies are sorted by title.
        assertEquals(1, viewModel.movies.get(0).getId());
        assertEquals(2, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.RELEASE_DATE);
        viewModel.getSavedMovies();

        // Verify that movies are sorted by release date.
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(1, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.MODIFIED_DATE);
        viewModel.getSavedMovies();

        // Verify that movies are sorted by modified date.
        assertEquals(3, viewModel.movies.get(0).getId());
        assertEquals(1, viewModel.movies.get(1).getId());
        assertEquals(2, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.USER_RATING);
        viewModel.getSavedMovies();

        // Verify that movies are sorted by user rating.
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(3, viewModel.movies.get(1).getId());
        assertEquals(1, viewModel.movies.get(2).getId());
    }

    @Test
    public void setSortMethod_sortsSearchResults() {
        List<Movie> movies = TestHelper.getUnsortedMovies();
        viewModel.setSortMethod(SortMethod.TITLE);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(movies));
        when(repository.searchDetailed(QUERY)).thenReturn(single);

        viewModel.search(QUERY);

        // Verify that movies are sorted by title.
        assertEquals(1, viewModel.movies.get(0).getId());
        assertEquals(2, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.RELEASE_DATE);
        viewModel.search(QUERY);

        // Verify that movies are sorted by release date.
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(1, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.MODIFIED_DATE);
        viewModel.search(QUERY);

        // Verify that movies are sorted by modified date.
        assertEquals(3, viewModel.movies.get(0).getId());
        assertEquals(1, viewModel.movies.get(1).getId());
        assertEquals(2, viewModel.movies.get(2).getId());

        viewModel.setSortMethod(SortMethod.USER_RATING);
        viewModel.search(QUERY);

        // Verify that movies are sorted by user rating.
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(3, viewModel.movies.get(1).getId());
        assertEquals(1, viewModel.movies.get(2).getId());
    }

    @Test
    public void onAddMovieClick() {
        Observer observer = mock(Observer.class);
        viewModel.getAddMovieEvent().observeForever(observer);
        viewModel.onAddMovieClick();
        verify(observer, times(1)).onChanged(null);
    }

    @Test
    public void deleteMovie() {
        deleteMovie_execute();
    }

    @Test
    public void deleteMovie_errorOccurs() {
        Observer observer = mock(Observer.class);
        viewModel.getDeleteMovieErrorEvent().observeForever(observer);

        Single<List<Movie>> getMoviesSingle = Single.create(emitter -> emitter.onSuccess(localMovies));
        when(repository.getSavedMovies()).thenReturn(getMoviesSingle);

        Single<List<Movie>> deleteMovieSingle = Single.create(emitter -> {
            throw new Exception("");
        });
        when(repository.deleteMovie(localMovies.get(0))).thenReturn(deleteMovieSingle);

        viewModel.getSavedMovies();
        viewModel.deleteMovie(localMovies.get(0));

        verify(repository, times(1)).deleteMovie(localMovies.get(0));
        assertEquals(3, viewModel.movies.size());

        verify(observer, times(1)).onChanged(null);
    }

    @Test
    public void undoDeleteMovie() {
        Observer observer = mock(Observer.class);
        viewModel.getUndoDeleteEvent().observeForever(observer);

        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(localMovies));
        when(repository.restoreMovie(localMovies.get(0))).thenReturn(single);

        deleteMovie_execute();
        viewModel.undoDeleteMovie();

        verify(repository, times(1)).restoreMovie(localMovies.get(0));
        assertEquals(3, viewModel.movies.size());
        assertEquals(1, viewModel.movies.get(0).getId());
        assertEquals(2, viewModel.movies.get(1).getId());
        assertEquals(3, viewModel.movies.get(2).getId());

        verify(observer, times(1)).onChanged(null);
    }

    @Test
    public void undoDeleteMovie_errorOccurs() {
        Observer observer = mock(Observer.class);
        viewModel.getUndoDeleteErrorEvent().observeForever(observer);

        Single<List<Movie>> single = Single.create(emitter -> {
            throw new Exception("");
        });
        when(repository.restoreMovie(localMovies.get(0))).thenReturn(single);

        deleteMovie_execute();
        viewModel.undoDeleteMovie();

        verify(repository, times(1)).restoreMovie(localMovies.get(0));
        assertEquals(2, viewModel.movies.size());
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(3, viewModel.movies.get(1).getId());

        verify(observer, times(1)).onChanged(null);
    }

    @Test
    public void invalidateCache() {
        viewModel.invalidateCache();
        verify(repository, times(1)).invalidateCache();
    }

    @Test
    public void invalidateCache_withMovieIds() {
        List<Integer> movieIds = new ArrayList<>();
        viewModel.invalidateCache(movieIds);
        verify(repository, times(1)).invalidateCache(movieIds);
    }

    private void deleteMovie_execute() {
        Observer observer = mock(Observer.class);
        viewModel.getDeleteMovieEvent().observeForever(observer);

        Single<List<Movie>> getMoviesSingle = Single.create(emitter -> emitter.onSuccess(localMovies));
        when(repository.getSavedMovies()).thenReturn(getMoviesSingle);

        List<Movie> movies = new ArrayList<>();
        movies.add(localMovies.get(1));
        movies.add(localMovies.get(2));

        Single<List<Movie>> deleteMovieSingle = Single.create(emitter -> emitter.onSuccess(movies));
        when(repository.deleteMovie(localMovies.get(0))).thenReturn(deleteMovieSingle);

        viewModel.getSavedMovies();
        viewModel.deleteMovie(localMovies.get(0));

        verify(repository, times(1)).deleteMovie(localMovies.get(0));
        assertEquals(2, viewModel.movies.size());
        assertEquals(2, viewModel.movies.get(0).getId());
        assertEquals(3, viewModel.movies.get(1).getId());

        verify(observer, times(1)).onChanged(null);
    }
}