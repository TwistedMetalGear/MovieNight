package net.silentbyte.movienight.dagger;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import net.silentbyte.movienight.data.source.local.MovieDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    MovieDatabase provideMovieDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                MovieDatabase.class, MovieDatabase.DATABASE_NAME).build();
    }
}
