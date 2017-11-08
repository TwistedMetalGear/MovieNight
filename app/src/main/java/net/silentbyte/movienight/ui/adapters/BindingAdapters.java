package net.silentbyte.movienight.ui.adapters;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.silentbyte.movienight.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class BindingAdapters {

    @BindingAdapter("url")
    public static void loadImage(ImageView view, String url) {
        if (url != null) {
            Glide.with(view.getContext()).load(url).into(view);
        }
    }

    @BindingAdapter("visibility_boolean")
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("movies")
    public static void setMovies(RecyclerView view, List<Movie> movies) {
        // The movies parameter is a reference to the ObservableList in the MovieListViewModel.
        // Any operations (e.g. clear, add, remove) performed on this reference will emit list
        // changed events which affect any views bound to it. We don't want to emit these events
        // outside of the ViewModel, hence we will clone it into an ArrayList before passing it on.
        ((MovieListAdapter) view.getAdapter()).setMovies(new ArrayList<>(movies));
    }
}
