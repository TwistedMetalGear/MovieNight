package net.silentbyte.movienight.util;

import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.util.MovieSorter.SortMethod;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MovieSorterTest {

    private List<Movie> movies;

    @Before
    public void createMovieList() {
        movies = TestHelper.getUnsortedMovies();
    }

    @Test
    public void sortByTitle() {
        MovieSorter.sort(movies, SortMethod.TITLE);

        assertEquals(1, movies.get(0).getId());
        assertEquals(2, movies.get(1).getId());
        assertEquals(3, movies.get(2).getId());
    }

    @Test
    public void sortByReleaseDate() {
        MovieSorter.sort(movies, SortMethod.RELEASE_DATE);

        assertEquals(2, movies.get(0).getId());
        assertEquals(1, movies.get(1).getId());
        assertEquals(3, movies.get(2).getId());
    }

    @Test
    public void sortByModifiedDate() {
        MovieSorter.sort(movies, SortMethod.MODIFIED_DATE);

        assertEquals(3, movies.get(0).getId());
        assertEquals(1, movies.get(1).getId());
        assertEquals(2, movies.get(2).getId());
    }

    @Test
    public void sortByUserRating() {
        MovieSorter.sort(movies, SortMethod.USER_RATING);

        assertEquals(2, movies.get(0).getId());
        assertEquals(3, movies.get(1).getId());
        assertEquals(1, movies.get(2).getId());
    }
}