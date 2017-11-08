package net.silentbyte.movienight.tmdb;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API providing methods to search and retrieve movies from the TMDb API server.
 */
public interface MovieApi {

    String API_KEY = "INSERT_API_KEY_HERE";
    String BASE_URL = "https://api.themoviedb.org/3/";
    String BASE_POSTER_URL = "http://image.tmdb.org/t/p/w500";

    @GET("search/movie?api_key=" + API_KEY)
    Single<MovieSearchResponse> searchForMovie(@Query("query") String title);

    @GET("movie/{movie_id}?api_key=" + API_KEY + "&append_to_response=release_dates")
    Single<MovieDetailed> getMovieDetail(@Path("movie_id") int movieId);
}
