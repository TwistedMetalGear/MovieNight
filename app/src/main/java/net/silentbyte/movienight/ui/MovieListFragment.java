package net.silentbyte.movienight.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.silentbyte.movienight.dagger.AppComponent;
import net.silentbyte.movienight.dagger.AppModule;
import net.silentbyte.movienight.dagger.DaggerAppComponent;
import net.silentbyte.movienight.ui.adapters.MovieListAdapter;
import net.silentbyte.movienight.MovieSorter.SortMethod;
import net.silentbyte.movienight.R;
import net.silentbyte.movienight.databinding.FragmentMovieListBinding;
import net.silentbyte.movienight.model.Movie;
import net.silentbyte.movienight.viewmodel.MovieListViewModel;

import javax.inject.Inject;

/**
 * This fragment is responsible for displaying a list of movies. Upon launching the app, the user's
 * saved movies are loaded and displayed. Upon searching for a movie, a new instance of this fragment
 * is launched, the search is executed, and the results are displayed. The query argument determines
 * the action that this fragment will take upon launch. When the query argument is null, the user's
 * saved movies are loaded. When the query argument is non-null, the value of the argument is used
 * to execute a search.
 */
public class MovieListFragment extends MovieBaseFragment
{
    private static final String ARG_QUERY = "query";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_SORT_METHOD = "sort_method";

    private FragmentMovieListBinding binding;

    private boolean firstLaunch = true;
    private SortMethod sortMethod = SortMethod.NONE;

    @Inject
    MovieListViewModel.Factory factory;

    public static MovieListFragment newInstance(String query)
    {
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        MovieListFragment fragment = new MovieListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AppComponent component = DaggerAppComponent.builder()
            .appModule(new AppModule(getActivity().getApplication()))
            .build();

        component.inject(this);

        viewModel = ViewModelProviders.of(this, factory).get(MovieListViewModel.class);

        if (savedInstanceState != null)
        {
            firstLaunch = savedInstanceState.getBoolean(KEY_FIRST_LAUNCH);
            sortMethod = (SortMethod)savedInstanceState.getSerializable(KEY_SORT_METHOD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie_list, container, false);
        binding.setViewModel((MovieListViewModel)viewModel);

        binding.movieRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.movieRecycler.setAdapter(new MovieListAdapter(movieClickCallback));

        if (getArguments().getString(ARG_QUERY) == null)
        {
            // Allow swipe to delete when displaying saved movies.
            MovieTouchCallback callback = new MovieTouchCallback();
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.movieRecycler);
        }
        else
        {
            // Hide add movie button when showing search results.
            binding.addMovieButton.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (firstLaunch)
        {
            // Default to sorting by last modified date when displaying saved movies. Note that we
            // don't set the default sort method when displaying search results. This is because the
            // ViewModel defaults to a sort method of NONE which is what we want for search results.
            // Making the assumption that TMDb is sorting search results by relevance.
            if (getArguments().getString(ARG_QUERY) == null)
                sortMethod = SortMethod.MODIFIED_DATE;
        }

        binding.getViewModel().setSortMethod(sortMethod);

        subscribeToViewModel();

        firstLaunch = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        searchView.setOnSearchClickListener(v -> binding.addMovieButton.setVisibility(View.GONE));

        searchView.setOnCloseListener(() ->
        {
            // Add movie button should only be visible if this fragment is showing saved movies.
            if (getArguments().getString(ARG_QUERY) == null)
                binding.addMovieButton.setVisibility(View.VISIBLE);

            return false;
        });

        MenuItem sortItem = menu.findItem(R.id.sort_item);
        sortItem.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.sort_item:
                showSortPopup();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // If one or more movies have been updated, invalidate the cache as necessary.
        // This ensures that the latest movie data is loaded and displayed.
        if (!invalidated && !updatedMovieIds.isEmpty())
        {
            if (getArguments().getString(ARG_QUERY) == null)
            {
                // Always invalidate when we are displaying saved movies because either a new movie was
                // added or an existing movie was updated, both of which should be reflected in the list.
                binding.getViewModel().invalidateCache();
            }
            else
            {
                // In this case we are displaying search results. We only need to invalidate if the search
                // results contain any of the movies that were updated.
                binding.getViewModel().invalidateCache(updatedMovieIds);
            }

            invalidated = true;

            // If this is the original fragment, clear updatedMovieIds. We have already invalidated the
            // cache as necessary and we don't need to pass back updatedMovieIds any further since there
            // are no more fragments on the back stack. It doesn't necessarily have to be cleared, but it
            // will keep growing in size if it isn't.
            if (getFragmentManager().getBackStackEntryCount() == 0)
                updatedMovieIds.clear();
        }

        loadMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_FIRST_LAUNCH, firstLaunch);
        outState.putSerializable(KEY_SORT_METHOD, sortMethod);
    }

    @Override
    protected void subscribeToViewModel()
    {
        super.subscribeToViewModel();

        // Called when the user taps the add movie button.
        binding.getViewModel().getAddMovieEvent().observe(this, aVoid ->
        {
            binding.addMovieButton.setVisibility(View.GONE);
            View searchButton = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
            searchButton.callOnClick();
        });

        // Called when the user taps the retry button after experiencing an error loading movies.
        binding.getViewModel().getRetryEvent().observe(this, aVoid -> loadMovies());

        // Called in response to the user deleting a movie from their saved movie list.
        binding.getViewModel().getDeleteMovieEvent().observe(this, aVoid ->
        {
            Snackbar snackbar = Snackbar.make(getView(), R.string.movie_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, view -> binding.getViewModel().undoDeleteMovie()).show();
        });

        // Called when there is an error deleting a movie.
        binding.getViewModel().getDeleteMovieErrorEvent().observe(this, aVoid ->
            Toast.makeText(getActivity(), R.string.delete_movie_error, Toast.LENGTH_SHORT).show());

        // Called in response to the user undoing a movie delete.
        binding.getViewModel().getUndoDeleteEvent().observe(this, aVoid ->
            Snackbar.make(getView(), R.string.movie_restored, Snackbar.LENGTH_SHORT).show());

        // Called when there is an error undoing a movie delete.
        binding.getViewModel().getUndoDeleteErrorEvent().observe(this, aVoid ->
        {
            Snackbar snackbar = Snackbar.make(getView(), R.string.undo_delete_error, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.retry, view -> binding.getViewModel().undoDeleteMovie()).show();
        });
    }

    private void loadMovies()
    {
        String query = getArguments().getString(ARG_QUERY);

        if (query == null)
            binding.getViewModel().getSavedMovies();
        else
            binding.getViewModel().search(query);
    }

    private void showSortPopup()
    {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.sort_item));
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem ->
        {
            switch (menuItem.getItemId())
            {
                case R.id.title:
                    sortMethod = SortMethod.TITLE;
                    break;
                case R.id.release_date:
                    sortMethod = SortMethod.RELEASE_DATE;
                    break;
                case R.id.modified_date:
                    sortMethod = SortMethod.MODIFIED_DATE;
                    break;
                case R.id.user_rating:
                    sortMethod = SortMethod.USER_RATING;
            }

            binding.getViewModel().setSortMethod(sortMethod);

            loadMovies();
            return true;
        });

        popup.show();
    }

    private class  MovieTouchCallback extends ItemTouchHelper.Callback
    {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
        {
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
        {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
        {
            Movie movie = ((MovieListAdapter.MovieViewHolder)viewHolder).getBinding().getMovie();
            binding.getViewModel().deleteMovie(movie);
        }
    }
}
