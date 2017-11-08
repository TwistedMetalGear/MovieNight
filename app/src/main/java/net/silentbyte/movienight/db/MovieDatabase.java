package net.silentbyte.movienight.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import net.silentbyte.movienight.model.Movie;

@Database(entities = {Movie.class}, version = 1)
public abstract class MovieDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "movie.db";

    public abstract MovieDao movieDao();
}
