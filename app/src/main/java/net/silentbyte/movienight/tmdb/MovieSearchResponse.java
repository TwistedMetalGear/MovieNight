package net.silentbyte.movienight.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents the set of results corresponding to a search for a particular movie.
 * This models the response of a GET /search/movie request from TMDb.
 */
public class MovieSearchResponse
{
    private int page;
    List<MovieBasic> results;
    @SerializedName("total_results") private int totalResults;
    @SerializedName("total_pages") private int totalPages;

    public int getPage()
    {
        return page;
    }

    public List<MovieBasic> getResults()
    {
        return results;
    }

    public int getTotalResults()
    {
        return totalResults;
    }

    public int getTotalPages()
    {
        return totalPages;
    }
}
