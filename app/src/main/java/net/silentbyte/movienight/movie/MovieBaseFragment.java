package net.silentbyte.movienight.movie;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.silentbyte.movienight.R;
import net.silentbyte.movienight.movie.detail.MovieDetailFragment;
import net.silentbyte.movienight.movie.list.MovieListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Base fragment for MovieListFragment and MovieDetailFragment. Implements common functionality.
 */
public abstract class MovieBaseFragment extends Fragment {

    protected static final String KEY_UPDATED_MOVIE_IDS = "updated_movie_ids";
    private static final String KEY_INVALIDATED = "invalidated";
    private static final String KEY_SNACKBAR_MESSAGE_ID = "snackbar_message_id";
    private static final int REQUEST_SEARCH = 0;
    private static final int REQUEST_DETAIL = 1;

    protected SearchView searchView;
    protected MovieBaseViewModel viewModel;
    protected FragmentHolder fragmentHolder;

    protected List<Integer> updatedMovieIds = new ArrayList<>();
    protected boolean invalidated = false;
    private int snackbarMessageId = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentHolder = (FragmentHolder) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            updatedMovieIds = savedInstanceState.getIntegerArrayList(KEY_UPDATED_MOVIE_IDS);
            invalidated = savedInstanceState.getBoolean(KEY_INVALIDATED);
            snackbarMessageId = savedInstanceState.getInt(KEY_SNACKBAR_MESSAGE_ID);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_item);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint(getString(R.string.movie_title));
        searchView.setSuggestionsAdapter(new SuggestionsAdapter(getActivity(), null, 0, movieClickCallback));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                collapseSearchView();
                Fragment fragment = MovieListFragment.newInstance(query);
                fragment.setTargetFragment(MovieBaseFragment.this, REQUEST_SEARCH);
                fragmentHolder.show(fragment);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                viewModel.onQueryTextChange(query);
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (snackbarMessageId != 0) {
            String message = getString(snackbarMessageId);
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            snackbarMessageId = 0;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(KEY_UPDATED_MOVIE_IDS, (ArrayList) updatedMovieIds);
        outState.putBoolean(KEY_INVALIDATED, invalidated);
        outState.putInt(KEY_SNACKBAR_MESSAGE_ID, snackbarMessageId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DETAIL) {
            snackbarMessageId = data.getIntExtra(MovieDetailFragment.KEY_SNACKBAR_MESSAGE, 0);
        }

        List<Integer> movieIds = data.getIntegerArrayListExtra(KEY_UPDATED_MOVIE_IDS);

        if (!movieIds.isEmpty()) {
            for (int movieId : movieIds) {
                addUpdatedMovieId(movieId);
            }

            invalidated = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentHolder = null;
    }

    public void setResult(Intent intent) {
        if (getTargetFragment() != null) {
            if (intent == null) {
                intent = new Intent();
            }

            intent.putExtra(KEY_UPDATED_MOVIE_IDS, (ArrayList) updatedMovieIds);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    protected void subscribeToViewModel() {
        viewModel.getQuerySuggestions().observe(this, movies -> {
            // There is a potential for this callback to fire before searchView is initialized.
            // Specifically, when the user enters a search term and submits before the 400ms
            // delay in the search throttler (in MovieBaseViewModel) expires, and then performs
            // a config change followed by hitting the back button.
            if (searchView != null) {
                SuggestionsAdapter adapter = (SuggestionsAdapter) searchView.getSuggestionsAdapter();
                adapter.setMovies(movies);
            }
        });
    }

    protected void addUpdatedMovieId(int movieId) {
        if (!updatedMovieIds.contains(movieId)) {
            updatedMovieIds.add(movieId);
        }
    }

    private void collapseSearchView() {
        // First call clears search text.
        searchView.setIconified(true);

        // Second call collapses search view and closes keyboard.
        searchView.setIconified(true);
    }

    protected final MovieClickCallback movieClickCallback = movie -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            collapseSearchView();
            MovieDetailFragment fragment = MovieDetailFragment.newInstance(movie.getId());
            fragment.setTargetFragment(this, REQUEST_DETAIL);
            fragmentHolder.show(fragment);
        }
    };
}
