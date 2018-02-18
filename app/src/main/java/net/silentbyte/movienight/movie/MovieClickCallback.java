package net.silentbyte.movienight.movie;

import net.silentbyte.movienight.data.Movie;

/**
 * Exposes a single method to handle the clicking of a specific movie in a list.
 */
public interface MovieClickCallback {

    void onClick(Movie movie);
}
