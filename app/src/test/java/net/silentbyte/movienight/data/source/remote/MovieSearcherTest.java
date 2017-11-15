package net.silentbyte.movienight.data.source.remote;

import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.data.source.remote.MovieApi;
import net.silentbyte.movienight.data.source.remote.MovieDetailed;
import net.silentbyte.movienight.data.source.remote.MovieSearchResponse;
import net.silentbyte.movienight.data.source.remote.MovieSearcher;
import net.silentbyte.movienight.util.TestHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MovieSearcherTest {

    private static final String QUERY = "test query";

    private MovieSearcher movieSearcher;

    private MovieSearchResponse movieSearchResponse;
    private List<MovieDetailed> movieDetailedList;
    private List<Movie> localMovies;
    private List<Movie> remoteMovies;

    @Mock
    MovieApi movieApi;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void createMovieSearcher() {
        movieSearcher = new MovieSearcher(movieApi);
    }

    @Before
    public void createMovieSearchResponse() {
        movieSearchResponse = TestHelper.getMovieSearchResponse();
    }

    @Before
    public void createMovieDetailedList() {
        movieDetailedList = TestHelper.getMovieDetailedList();
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
    public void searchBasic() {
        Single<MovieSearchResponse> single = Single.create(emitter -> emitter.onSuccess(movieSearchResponse));
        when(movieApi.searchForMovie(QUERY)).thenReturn(single);

        TestObserver<List<Movie>> observer = movieSearcher.searchBasic(QUERY).test();
        observer.assertNoErrors();

        verify(movieApi, times(1)).searchForMovie(QUERY);

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> movies = (List<Movie>) emittedObjects.get(0);
        assertEquals(3, movies.size());
        assertEquals(562, movies.get(0).getId());
        assertEquals(1573, movies.get(1).getId());
        assertEquals(1572, movies.get(2).getId());
    }

    @Test
    public void searchDetailed() {
        Single<MovieSearchResponse> searchSingle = Single.create(emitter -> emitter.onSuccess(movieSearchResponse));
        Single<MovieDetailed> movieDetailedSingle1 = Single.create(emitter -> emitter.onSuccess(movieDetailedList.get(0)));
        Single<MovieDetailed> movieDetailedSingle2 = Single.create(emitter -> emitter.onSuccess(movieDetailedList.get(1)));
        Single<MovieDetailed> movieDetailedSingle3 = Single.create(emitter -> emitter.onSuccess(movieDetailedList.get(2)));

        when(movieApi.searchForMovie(QUERY)).thenReturn(searchSingle);
        when(movieApi.getMovieDetail(movieDetailedList.get(0).getId())).thenReturn(movieDetailedSingle1);
        when(movieApi.getMovieDetail(movieDetailedList.get(1).getId())).thenReturn(movieDetailedSingle2);
        when(movieApi.getMovieDetail(movieDetailedList.get(2).getId())).thenReturn(movieDetailedSingle3);

        TestObserver<List<Movie>> observer = movieSearcher.searchDetailed(QUERY).test();
        observer.assertNoErrors();

        verify(movieApi, times(1)).searchForMovie(QUERY);
        verify(movieApi, times(1)).getMovieDetail(movieDetailedList.get(0).getId());
        verify(movieApi, times(1)).getMovieDetail(movieDetailedList.get(1).getId());
        verify(movieApi, times(1)).getMovieDetail(movieDetailedList.get(2).getId());

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> movies = (List<Movie>) emittedObjects.get(0);
        assertEquals(3, movies.size());
        assertEquals(562, movies.get(0).getId());
        assertEquals(1573, movies.get(1).getId());
        assertEquals(1572, movies.get(2).getId());

        // Verify the runtime on each movie to prove that we retrieved the detail.
        assertEquals(131, movies.get(0).getRuntime());
        assertEquals(124, movies.get(1).getRuntime());
        assertEquals(128, movies.get(2).getRuntime());
    }

    @Test
    public void getMovieDetailed() {
        Single<MovieDetailed> single = Single.create(emitter -> emitter.onSuccess(movieDetailedList.get(0)));
        when(movieApi.getMovieDetail(movieDetailedList.get(0).getId())).thenReturn(single);

        TestObserver<Movie> observer = movieSearcher.getMovieDetailed(movieDetailedList.get(0).getId()).test();
        observer.assertNoErrors();

        verify(movieApi, times(1)).getMovieDetail(movieDetailedList.get(0).getId());

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        Movie movie = (Movie) emittedObjects.get(0);
        assertEquals(562, movie.getId());

        // Verify the runtime to prove that we retrieved the detail.
        assertEquals(131, movie.getRuntime());
    }
}