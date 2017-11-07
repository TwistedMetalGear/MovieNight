package net.silentbyte.movienight;

import net.silentbyte.movienight.model.Movie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to sort a list of movies based on certain criteria.
 */
public class MovieSorter
{
    public static void sort(List<Movie> movies, SortMethod sortMethod)
    {
        switch (sortMethod)
        {
            case TITLE:
                sortByTitle(movies);
                break;
            case RELEASE_DATE:
                sortByReleaseDate(movies);
                break;
            case MODIFIED_DATE:
                sortByModifiedDate(movies);
                break;
            case USER_RATING:
                sortByUserRating(movies);
        }
    }

    private static void sortByTitle(List<Movie> movies)
    {
        Collections.sort(movies, (movie1, movie2) -> movie1.getTitle().compareTo(movie2.getTitle()));
    }

    private static void sortByReleaseDate(List<Movie> movies)
    {
        Collections.sort(movies, (movie1, movie2) ->
        {
            String movie1ReleaseDate = movie1.getReleaseDate();
            String movie2ReleaseDate = movie2.getReleaseDate();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD", Locale.US);

            long movie1Epoch = 0;
            long movie2Epoch = 0;

            try
            {
                movie1Epoch = sdf.parse(movie1ReleaseDate).getTime();
            }
            catch (ParseException e) {}

            try
            {
                movie2Epoch = sdf.parse(movie2ReleaseDate).getTime();
            }
            catch (ParseException e) {}

            if (movie1Epoch == movie2Epoch)
                return movie1.getTitle().compareTo(movie2.getTitle());
            else
                return Long.compare(movie2Epoch, movie1Epoch);
        });
    }

    private static void sortByModifiedDate(List<Movie> movies)
    {
        Collections.sort(movies, (movie1, movie2) -> Long.compare(movie2.getUpdateTime(), movie1.getUpdateTime()));
    }

    private static void sortByUserRating(List<Movie> movies)
    {
        Collections.sort(movies, (movie1, movie2) ->
        {
            // Sort by title if ratings are equal.
            if (movie1.getUserRating() == movie2.getUserRating())
                return movie1.getTitle().compareTo(movie2.getTitle());
            else
                return Float.compare(movie2.getUserRating(), movie1.getUserRating());
        });
    }

    public enum SortMethod
    {
        NONE,
        TITLE,
        RELEASE_DATE,
        MODIFIED_DATE,
        USER_RATING
    }
}
