package net.silentbyte.movienight.dagger;

import net.silentbyte.movienight.tmdb.MovieApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetModule
{
    @Provides
    @Singleton
    Converter.Factory provideGsonConverterFactory()
    {
        return GsonConverterFactory.create();
    }

    @Provides
    @Singleton
    CallAdapter.Factory provideRxJava2CallAdapterFactory()
    {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Converter.Factory converterFactory, CallAdapter.Factory callAdapterFactory)
    {
        return new Retrofit.Builder()
                .baseUrl(MovieApi.BASE_URL)
                .addConverterFactory(converterFactory)
                .addCallAdapterFactory(callAdapterFactory)
                .build();
    }

    @Provides
    @Singleton
    MovieApi provideMovieApi(Retrofit retrofit)
    {
        return retrofit.create(MovieApi.class);
    }
}
