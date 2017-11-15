package net.silentbyte.movienight.movies.detail;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.silentbyte.movienight.R;
import net.silentbyte.movienight.dagger.AppComponent;
import net.silentbyte.movienight.dagger.AppModule;
import net.silentbyte.movienight.dagger.DaggerAppComponent;
import net.silentbyte.movienight.databinding.FragmentMovieDetailBinding;
import net.silentbyte.movienight.movies.MovieBaseFragment;

import javax.inject.Inject;

/**
 * This fragment is responsible for displaying the detail for the movie specified by the movie id
 * argument. It also provides a means to rate the movie and write a review, then save it. Upon saving
 * the movie, the movie is added to the updated movies list which is passed back through an intent to
 * the target fragment (i.e. the fragment underneath this one in the back stack). Fragments will check
 * the updated movies list in onResume to determine if they have to invalidate their cache and reload
 * the movie(s) or not.
 */
public class MovieDetailFragment extends MovieBaseFragment {

    public static final String KEY_USER_RATING = "user_rating";
    public static final String KEY_USER_REVIEW = "user_review";
    public static final String KEY_MODIFIED = "modified";
    public static final String KEY_SNACKBAR_MESSAGE = "snackbar_message";
    private static final String ARG_MOVIE_ID = "movie_id";

    private FragmentMovieDetailBinding binding;

    @Inject
    MovieDetailViewModel.Factory factory;

    public static MovieDetailFragment newInstance(int movieId) {
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppComponent component = DaggerAppComponent.builder()
                .appModule(new AppModule(getActivity().getApplication()))
                .build();

        component.inject(this);

        viewModel = ViewModelProviders.of(this, factory).get(MovieDetailViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_movie_detail, container, false);
        binding.setViewModel((MovieDetailViewModel) viewModel);

        binding.ratingBar.setOnTouchListener((view, motionEvent) -> {
            binding.getViewModel().onUserRatingChanged(binding.ratingBar.getRating());

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
                return true;
            }

            return false;
        });

        binding.ratingStar.setOnTouchListener((view, motionEvent) -> true); // Disables touch events on the rating star.

        binding.userReview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.getViewModel().onUserReviewChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        subscribeToViewModel();

        if (savedInstanceState != null) {
            float userRating = savedInstanceState.getFloat(KEY_USER_RATING);
            String userReview = savedInstanceState.getString(KEY_USER_REVIEW);
            boolean modified = savedInstanceState.getBoolean(KEY_MODIFIED);

            binding.getViewModel().setUserRating(userRating);
            binding.getViewModel().userReview.set(userReview);
            binding.getViewModel().modified.set(modified);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // If one or more movies have been updated, invalidate the cache as necessary.
        // This ensures that the latest movie data is loaded and displayed.
        if (!invalidated && !updatedMovieIds.isEmpty()) {
            binding.getViewModel().invalidateCache(updatedMovieIds);
            invalidated = true;
        }

        loadMovie();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Because of the use of target fragments, it's possible for this method to be called
        // on a fragment in the back stack which hasn't been fully initialized and thus binding
        // is null. Case in point, load two or more detail fragments and perform a config change.
        // The detail fragments on the back stack will have onAttach and onCreate called on them
        // but not onCreateView, onActivityCreated, etc... Since the binding reference will be
        // null in this case, we will retrieve the viewModel reference not through the binding,
        // but through the base class reference.
        outState.putFloat(KEY_USER_RATING, ((MovieDetailViewModel) viewModel).userRating.get());
        outState.putString(KEY_USER_REVIEW, ((MovieDetailViewModel) viewModel).userReview.get());
        outState.putBoolean(KEY_MODIFIED, ((MovieDetailViewModel) viewModel).modified.get());
    }

    @Override
    protected void subscribeToViewModel() {
        super.subscribeToViewModel();

        // Called in response to a movie being saved.
        binding.getViewModel().getSaveMovieEvent().observe(this, result -> {
            addUpdatedMovieId(getArguments().getInt(ARG_MOVIE_ID));

            Intent intent = new Intent();
            intent.putExtra(KEY_SNACKBAR_MESSAGE, result ? R.string.movie_saved : R.string.movie_updated);

            setResult(intent);
            getFragmentManager().popBackStack();
        });

        // Called when the user taps the retry button after experiencing an error loading a movie.
        binding.getViewModel().getRetryEvent().observe(this, aVoid -> loadMovie());

        // Called when there is an error saving a movie.
        binding.getViewModel().getSaveMovieErrorEvent().observe(this, aVoid ->
                Toast.makeText(getActivity(), R.string.save_movie_error, Toast.LENGTH_SHORT).show());
    }

    private void loadMovie() {
        binding.getViewModel().getMovieById(getArguments().getInt(ARG_MOVIE_ID));
    }
}
