package net.silentbyte.movienight.movies.list;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.silentbyte.movienight.movies.MovieClickCallback;
import net.silentbyte.movienight.R;
import net.silentbyte.movienight.databinding.ListItemMovieBinding;
import net.silentbyte.movienight.data.Movie;

import java.util.List;

/**
 * Adapter for the movie list RecyclerView.
 */
public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {

    private List<Movie> movies;
    private MovieClickCallback movieClickCallback;

    public MovieListAdapter(MovieClickCallback clickCallback) {
        movieClickCallback = clickCallback;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemMovieBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_movie, parent, false);
        binding.setClickCallback(movieClickCallback);
        binding.ratingStar.setOnTouchListener((view, motionEvent) -> true); // Disables touch events on the rating star.
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.binding.setMovie(movies.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return movies == null ? 0 : movies.size();
    }

    public void setMovies(List<Movie> movies) {
        if (this.movies == null) {
            this.movies = movies;
        }
        else {
            this.movies.clear();
            this.movies.addAll(movies);
        }

        notifyDataSetChanged();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        private ListItemMovieBinding binding;

        public MovieViewHolder(ListItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ListItemMovieBinding getBinding() {
            return binding;
        }
    }
}
