# Movie Night
Movie Night is a simple app that lets you rate and review movies.

It was built to showcase some of the latest and greatest Android technologies including:

* Android Architecture Components
  * ViewModels
  * LiveData
  
* Android Data Binding Library

* MVVM Architecture

* RxJava

* Retrofit

* Room Persistence Library

* Dagger 2 Dependency Injection

Movie Night is a single activity app that maintains a fragment back stack. There are two fragments, a movie list fragment, and a movie detail fragment. The movie list fragment is responsible for displaying the user's saved movies or movie search results. The movie detail fragment displays details for a particular movie and allows you to rate and review the movie. Changes in rating or review will be propagated down the back stack so that all fragments are displaying the latest data at all times.

Movie Night utilizes [TheMovieDB](https://www.themoviedb.org) API to search for movies and retrieve movie details. The TMDb API is free to use and requires an API key. Before running the app, please [sign up](https://www.themoviedb.org/account/signup) with TMDb, go to Account Settings, and click the API link on the sidebar. You will then have an option to request an API key. Once you have an API key, open the net.silentbyte.movienight.data.source.remote.MovieApi class and paste your key as the value of the API_KEY field.

# Screenshots

![Screenshot 1](https://raw.githubusercontent.com/TwistedMetalGear/MovieNight/master/screenshots/1.png)

![Screenshot 2](https://raw.githubusercontent.com/TwistedMetalGear/MovieNight/master/screenshots/2.png)
