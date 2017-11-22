package net.silentbyte.movienight.data;

import android.arch.persistence.room.EmptyResultSetException;

import net.silentbyte.movienight.data.source.local.MovieDao;
import net.silentbyte.movienight.data.source.local.MovieDatabase;
import net.silentbyte.movienight.data.source.remote.MovieSearcher;
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
import io.reactivex.observers.TestObserver;

import static org.junit.Assert.*;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MovieRepositoryTest {

    private static final String QUERY = "test query";

    private MovieRepository movieRepository;
    private List<Movie> localMovies;
    private List<Movie> remoteMovies;

    @Mock
    MovieDatabase movieDatabase;
    @Mock
    MovieDao movieDao;
    @Mock
    MovieSearcher movieSearcher;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void createMovieLists() {
        localMovies = TestHelper.getLocalMovies();
        remoteMovies = TestHelper.getRemoteMovies();
    }

    @Test
    public void getSavedMovies_returnsPopulatedList() {
        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(localMovies));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovies()).thenReturn(single);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First retrieval should be from database.
        getSavedMovies_returnsPopulatedList_execute();

        // Verify that the MovieDao was used to retrieve movies.
        verify(movieDao, times(1)).getMovies();

        // Second retrieval should be from cache.
        getSavedMovies_returnsPopulatedList_execute();

        // Verify that the MovieDao hasn't been used a second time.
        verify(movieDao, times(1)).getMovies();
    }

    @Test
    public void getSavedMovies_returnsEmptyList() {
        Single<List<Movie>> single = Single.create(emitter -> emitter.onSuccess(new ArrayList<>()));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovies()).thenReturn(single);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First retrieval should be from database.
        getSavedMovies_returnsEmptyList_execute();

        // Verify that the MovieDao was used to retrieve movies.
        verify(movieDao, times(1)).getMovies();

        // Second retrieval should be from cache.
        getSavedMovies_returnsEmptyList_execute();

        // Verify that the MovieDao hasn't been used a second time.
        verify(movieDao, times(1)).getMovies();
    }

    @Test
    public void searchBasic() {
        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        movieRepository.searchBasic(QUERY);

        // Not much to verify here. Just verify that search is called on the MovieSearcher.
        verify(movieSearcher, times(1)).searchBasic(QUERY);
    }

    @Test
    public void searchDetailed_returnsPopulatedList() {
        Single<List<Movie>> singleLocal = Single.create(emitter -> emitter.onSuccess(localMovies));
        Single<List<Movie>> singleRemote = Single.create(emitter -> emitter.onSuccess(remoteMovies));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovies()).thenReturn(singleLocal);
        when(movieSearcher.searchDetailed(QUERY)).thenReturn(singleRemote);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First search should use MovieSearcher.
        searchDetailed_returnsPopulatedList_execute();

        // Verify that the MovieSearcher was used to search.
        verify(movieSearcher, times(1)).searchDetailed(QUERY);

        // Second search should be from cache.
        searchDetailed_returnsPopulatedList_execute();

        // Verify that the MovieSearcher hasn't been used a second time.
        verify(movieSearcher, times(1)).searchDetailed(QUERY);
    }

    @Test
    public void searchDetailed_returnsEmptyList() {
        Single<List<Movie>> singleLocal = Single.create(emitter -> emitter.onSuccess(new ArrayList<>()));
        Single<List<Movie>> singleRemote = Single.create(emitter -> emitter.onSuccess(new ArrayList<>()));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovies()).thenReturn(singleLocal);
        when(movieSearcher.searchDetailed(QUERY)).thenReturn(singleRemote);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First search should use MovieSearcher.
        searchDetailed_returnsEmptyList_execute();

        // Verify that the MovieSearcher was used to search.
        verify(movieSearcher, times(1)).searchDetailed(QUERY);

        // Second search should be from cache.
        searchDetailed_returnsEmptyList_execute();

        // Verify that the MovieSearcher hasn't been used a second time.
        verify(movieSearcher, times(1)).searchDetailed(QUERY);
    }

    @Test
    public void getMovieById_returnsLocalMovie() {
        Single<Movie> singleLocal = Single.create(emitter -> emitter.onSuccess(localMovies.get(0)));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First retrieval should be from database.
        getMovieById_returnsLocalMovie_execute();

        // Verify that the MovieDao was used to retrieve movie.
        verify(movieDao, times(1)).getMovieById(1);

        // Verify that MovieSearcher wasn't used.
        verify(movieSearcher, never()).getMovieDetailed(1);

        // Second retrieval should be from cache.
        getMovieById_returnsLocalMovie_execute();

        // Verify that the MovieDao hasn't been used a second time.
        verify(movieDao, times(1)).getMovieById(1);

        // Verify that MovieSearcher wasn't used.
        verify(movieSearcher, never()).getMovieDetailed(1);
    }

    @Test
    public void getMovieById_returnsRemoteMovie() {
        Single<Movie> singleLocal = Single.create(emitter -> {
            throw new EmptyResultSetException("");
        });
        Single<Movie> singleRemote = Single.create(emitter -> emitter.onSuccess(remoteMovies.get(1)));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(4)).thenReturn(singleLocal);
        when(movieSearcher.getMovieDetailed(4)).thenReturn(singleRemote);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        // First retrieval should be from MovieSearcher.
        getMovieById_returnsRemoteMovie_execute();

        // Verify that both MovieDao and MovieSearcher were used.
        verify(movieDao, times(1)).getMovieById(4);
        verify(movieSearcher, times(1)).getMovieDetailed(4);

        // Second retrieval should be from cache.
        getMovieById_returnsRemoteMovie_execute();

        // Verify that neither MovieDao nor MovieSearcher were used a second time.
        verify(movieDao, times(1)).getMovieById(4);
        verify(movieSearcher, times(1)).getMovieDetailed(4);
    }

    @Test
    public void insertMovie_createsNewMovie() {
        Single<Movie> singleLocal = Single.create(emitter -> {
            throw new EmptyResultSetException("");
        });

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        insertMovie_common_execute(true);

        // Verify that a get of the inserted movie comes from the cache.
        movieRepository.getMovieById(1).test();
        verify(movieDao, times(1)).getMovieById(1);
    }

    @Test
    public void insertMovie_replacesExistingMovie() {
        Single<Movie> singleLocal = Single.create(emitter -> emitter.onSuccess(localMovies.get(0)));

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        insertMovie_common_execute(false);

        // Verify that a get of the inserted movie comes from the cache.
        movieRepository.getMovieById(1).test();
        verify(movieDao, times(1)).getMovieById(1);
    }

    @Test
    public void deleteMovie() {
        // First, insert a movie, then retrieve it and verify that it comes from the cache.
        // Next, delete the movie and verify that the returned cache is empty.
        Single<Movie> singleLocal = Single.create(emitter -> {
            throw new EmptyResultSetException("");
        });

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        insertMovie_common_execute(true);

        TestObserver<Movie> observer = movieRepository.getMovieById(1).test();
        List<Object> emittedObjects = observer.getEvents().get(0);
        Movie movie = (Movie) emittedObjects.get(0);

        TestObserver<List<Movie>> deleteMovieObserver = movieRepository.deleteMovie(movie).test();
        deleteMovieObserver.assertNoErrors();

        verify(movieDao, times(1)).deleteMovie(movie.getId());

        emittedObjects = deleteMovieObserver.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        // Verify that the returned cache is empty since the movie has been deleted.
        List<Movie> movies = (List<Movie>) emittedObjects.get(0);
        assertTrue(movies.isEmpty());
    }

    @Test
    public void restoreMovie() {
        when(movieDatabase.movieDao()).thenReturn(movieDao);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        TestObserver<List<Movie>> observer = movieRepository.restoreMovie(localMovies.get(0)).test();
        observer.assertNoErrors();

        verify(movieDao, times(1)).insertMovie(localMovies.get(0));

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> movies = (List<Movie>) emittedObjects.get(0);
        assertEquals(1, movies.size());
        assertEquals(1, movies.get(0).getId());
    }

    @Test
    public void invalidateCache_returnsTrue() {
        Single<Movie> singleLocal = Single.create(emitter -> {
            throw new EmptyResultSetException("");
        });

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        insertMovie_common_execute(true);

        List<Integer> movieIds = new ArrayList<>();
        movieIds.add(1);

        boolean invalidated = movieRepository.invalidateCache(movieIds);
        assertTrue(invalidated);

        // Verify that a subsequent retrieve of the movie doesn't come from the cache.
        movieRepository.getMovieById(1).test();
        verify(movieDao, times(2)).getMovieById(1);
    }

    @Test
    public void invalidateCache_returnsFalse() {
        Single<Movie> singleLocal = Single.create(emitter -> {
            throw new EmptyResultSetException("");
        });

        when(movieDatabase.movieDao()).thenReturn(movieDao);
        when(movieDao.getMovieById(1)).thenReturn(singleLocal);

        movieRepository = new MovieRepository(movieDatabase, movieSearcher);

        insertMovie_common_execute(true);

        List<Integer> movieIds = new ArrayList<>();
        movieIds.add(2);

        boolean invalidated = movieRepository.invalidateCache(movieIds);
        assertFalse(invalidated);

        // Verify that a subsequent retrieve of the movie comes from the cache.
        movieRepository.getMovieById(1).test();
        verify(movieDao, times(1)).getMovieById(1);
    }

    /**
     * Helper for getSavedMovies_returnsPopulatedList. Performs execution and initial verification.
     */
    private void getSavedMovies_returnsPopulatedList_execute() {
        TestObserver<List<Movie>> observer = movieRepository.getSavedMovies().test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> emittedMovies = (List<Movie>) emittedObjects.get(0);
        assertEquals(1, emittedMovies.get(0).getId());
        assertEquals(2, emittedMovies.get(1).getId());
        assertEquals(3, emittedMovies.get(2).getId());
    }

    /**
     * Helper for getSavedMovies_returnsEmptyList. Performs execution and initial verification.
     */
    private void getSavedMovies_returnsEmptyList_execute() {
        TestObserver<List<Movie>> observer = movieRepository.getSavedMovies().test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> emittedMovies = (List<Movie>) emittedObjects.get(0);
        assertTrue(emittedMovies.isEmpty());
    }

    /**
     * Helper for searchDetailed_returnsPopulatedList. Performs execution and initial verification.
     */
    private void searchDetailed_returnsPopulatedList_execute() {
        TestObserver<List<Movie>> observer = movieRepository.searchDetailed(QUERY).test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> emittedMovies = (List<Movie>) emittedObjects.get(0);
        assertEquals(3, emittedMovies.size());

        // First movie should have been replaced by the local copy which has a user rating of 10.
        assertEquals(1, emittedMovies.get(0).getId());
        assertEquals(10, emittedMovies.get(0).getUserRating(), 0);

        // Other movies have no matching local copies and have a user rating of 0.
        assertEquals(4, emittedMovies.get(1).getId());
        assertEquals(0, emittedMovies.get(1).getUserRating(), 0);
        assertEquals(5, emittedMovies.get(2).getId());
        assertEquals(0, emittedMovies.get(2).getUserRating(), 0);
    }

    /**
     * Helper for searchDetailed_returnsEmptyList. Performs execution and initial verification.
     */
    private void searchDetailed_returnsEmptyList_execute() {
        TestObserver<List<Movie>> observer = movieRepository.searchDetailed(QUERY).test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        List<Movie> emittedMovies = (List<Movie>) emittedObjects.get(0);
        assertTrue(emittedMovies.isEmpty());
    }

    /**
     * Helper for getMovieById_returnsLocalMovie. Performs execution and initial verification.
     */
    private void getMovieById_returnsLocalMovie_execute() {
        TestObserver<Movie> observer = movieRepository.getMovieById(1).test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        Movie movie = (Movie) emittedObjects.get(0);
        assertEquals(1, movie.getId());

        // Verify that the movie has a user rating of 10 to prove that the movie is the local copy.
        assertEquals(10, movie.getUserRating(), 0);
    }

    /**
     * Helper for getMovieById_returnsRemoteMovie. Performs execution and initial verification.
     */
    private void getMovieById_returnsRemoteMovie_execute() {
        TestObserver<Movie> observer = movieRepository.getMovieById(4).test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        Movie movie = (Movie) emittedObjects.get(0);
        assertEquals(4, movie.getId());
    }

    /**
     * Helper for various test methods that rely on a precondition of a movie being inserted.
     * Performs execution and initial verification.
     */
    private void insertMovie_common_execute(boolean expected) {
        assertTrue(localMovies.get(0).getUpdateTime() == 0);

        TestObserver<Boolean> observer = movieRepository.insertMovie(localMovies.get(0)).test();
        observer.assertNoErrors();

        List<Object> emittedObjects = observer.getEvents().get(0);
        assertEquals(1, emittedObjects.size());

        boolean newMovieCreated = (Boolean) emittedObjects.get(0);

        assertEquals(expected, newMovieCreated);
        assertTrue(localMovies.get(0).getUpdateTime() > 0);
    }
}