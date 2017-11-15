package net.silentbyte.movienight.movies;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.databinding.DataBindingUtil;
import android.provider.BaseColumns;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.silentbyte.movienight.R;
import net.silentbyte.movienight.databinding.ListItemSuggestionBinding;
import net.silentbyte.movienight.data.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * CursorAdapter which provides movie suggestions as the user types in the search box.
 */
public class SuggestionsAdapter extends CursorAdapter {

    private List<Movie> movies = new ArrayList<>();
    private MovieClickCallback movieClickCallback;

    public SuggestionsAdapter(Context context, Cursor cursor, int flags, MovieClickCallback clickCallback) {
        super(context, cursor, flags);
        movieClickCallback = clickCallback;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ListItemSuggestionBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_suggestion, parent, false);
        binding.setClickCallback(movieClickCallback);
        return binding.getRoot();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int position = cursor.getPosition();
        ListItemSuggestionBinding binding = DataBindingUtil.getBinding(view);

        // When typing very fast (or deleting very fast), position can sometimes exceed
        // the size of the movies list. Protect against that here.
        if (position < movies.size()) {
            binding.setMovie(movies.get(position));
        }

        binding.executePendingBindings();
    }

    public void setMovies(List<Movie> movies) {
        this.movies.clear();
        this.movies.addAll(movies);

        String[] columns = {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2};
        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            String[] row = {Integer.toString(i), movie.getTitle(), movie.getPosterPath()};
            cursor.addRow(row);
        }

        swapCursor(cursor);
    }
}
