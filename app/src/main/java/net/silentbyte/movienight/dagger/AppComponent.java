package net.silentbyte.movienight.dagger;

import net.silentbyte.movienight.moviedetail.MovieDetailFragment;
import net.silentbyte.movienight.movielist.MovieListFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(MovieListFragment target);

    void inject(MovieDetailFragment target);
}
