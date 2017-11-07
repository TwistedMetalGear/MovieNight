package net.silentbyte.movienight.ui;

import net.silentbyte.movienight.model.Movie;

/**
 * Exposes a single method to handle the clicking of a specific movie in a list.
 */
public interface MovieClickCallback
{
    void onClick(Movie movie);
}
