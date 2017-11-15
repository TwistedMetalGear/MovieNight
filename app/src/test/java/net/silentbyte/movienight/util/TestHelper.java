package net.silentbyte.movienight.util;

import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;

import com.google.gson.Gson;

import net.silentbyte.movienight.data.Movie;
import net.silentbyte.movienight.data.source.remote.MovieDetailed;
import net.silentbyte.movienight.data.source.remote.MovieSearchResponse;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Various helpers to assist with testing.
 */
public class TestHelper {

    public static List<Movie> getLocalMovies() {
        List<Movie> movies = new ArrayList<>();

        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("A");
        movie.setUserRating(10);
        movies.add(movie);

        movie = new Movie();
        movie.setId(2);
        movie.setTitle("B");
        movie.setUserRating(9);
        movies.add(movie);

        movie = new Movie();
        movie.setId(3);
        movie.setTitle("C");
        movie.setUserRating(8);
        movies.add(movie);

        return movies;
    }

    public static List<Movie> getRemoteMovies() {
        List<Movie> movies = new ArrayList<>();

        // This specific movie will be contained in both the localMovies and remoteMovies lists.
        // Some functionality involves checking the local and remote sources for movies in common.
        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("A");
        movie.setUserRating(0);
        movies.add(movie);

        movie = new Movie();
        movie.setId(4);
        movie.setTitle("D");
        movie.setUserRating(0);
        movies.add(movie);

        movie = new Movie();
        movie.setId(5);
        movie.setTitle("E");
        movie.setUserRating(0);
        movies.add(movie);

        return movies;
    }

    public static List<Movie> getUnsortedMovies() {
        List<Movie> movies = new ArrayList<>();

        Movie movie = new Movie();
        movie.setId(3);
        movie.setTitle("C");
        movie.setReleaseDate("2015-06-13");
        movie.setUpdateTime(3);
        movie.setUserRating(8);
        movies.add(movie);

        movie = new Movie();
        movie.setId(2);
        movie.setTitle("B");
        movie.setReleaseDate("2017-11-14");
        movie.setUpdateTime(1);
        movie.setUserRating(8.5f);
        movies.add(movie);

        movie = new Movie();
        movie.setId(1);
        movie.setTitle("A");
        movie.setReleaseDate("2016-04-26");
        movie.setUpdateTime(2);
        movie.setUserRating(7);
        movies.add(movie);

        return movies;
    }

    public static MovieSearchResponse getMovieSearchResponse() {
        String json = MovieJson.movieSearchResponse;
        return new Gson().fromJson(json, MovieSearchResponse.class);
    }

    public static List<MovieDetailed> getMovieDetailedList() {
        List<MovieDetailed> movieDetailedList = new ArrayList<>();
        MovieDetailed movieDetailed;
        Gson gson = new Gson();

        String json = MovieJson.movieDetailed1;
        movieDetailed = gson.fromJson(json, MovieDetailed.class);
        movieDetailedList.add(movieDetailed);

        json = MovieJson.movieDetailed2;
        movieDetailed = gson.fromJson(json, MovieDetailed.class);
        movieDetailedList.add(movieDetailed);

        json = MovieJson.movieDetailed3;
        movieDetailed = gson.fromJson(json, MovieDetailed.class);
        movieDetailedList.add(movieDetailed);

        return movieDetailedList;
    }

    public static void initializeRxSchedulers() {
        Scheduler scheduler = new Scheduler() {
            @Override
            public Worker createWorker() {
                return new ExecutorScheduler.ExecutorWorker(Runnable::run);
            }
        };

        RxAndroidPlugins.setInitMainThreadSchedulerHandler(schedulerCallable -> scheduler);
        RxJavaPlugins.setInitIoSchedulerHandler(schedulerCallable -> scheduler);
    }

    // TODO: Could possibly use generics to reduce the number of observe methods below.

    /**
     * Initializes an observer that observes the specified ObservableBoolean. Any time the
     * boolean value is updated, its value will be added to the specified values list.
     */
    public static void observeBoolean(ObservableBoolean observable, List<Boolean> values) {
        observable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                values.add(((ObservableBoolean) observable).get());
            }
        });
    }

    /**
     * Initializes an observer that observes the specified ObservableFloat. Any time the
     * float value is updated, its value will be added to the specified values list.
     */
    public static void observeFloat(ObservableFloat observable, List<Float> values) {
        observable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                values.add(((ObservableFloat) observable).get());
            }
        });
    }

    /**
     * Initializes an observer that observes the specified ObservableField<String>. Any time
     * the string value is updated, its value will be added to the specified values list.
     */
    public static void observeString(ObservableField<String> observable, List<String> values) {
        observable.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                values.add(((ObservableField<String>) observable).get());
            }
        });
    }
}
