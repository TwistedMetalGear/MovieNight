package net.silentbyte.movienight.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.silentbyte.movienight.R;
import net.silentbyte.movienight.tmdb.MovieApi;

/**
 * Entry point of the application. This will initially show a MovieListFragment containing the user's saved movies.
 * As the user clicks through movies and performs searches, the current fragment will be replaced by the appropriate
 * new fragment. Only one fragment will be displayed at a time and a back stack will be maintained.
 */
public class MainActivity extends AppCompatActivity implements FragmentHolder, FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MovieApi.API_KEY.equals("INSERT_API_KEY_HERE")) {
            setContentView(R.layout.missing_api_key);
            return;
        }

        setContentView(R.layout.activity_movie_list);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = MovieListFragment.newInstance(null);
            manager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        determineUpVisibility();
    }

    @Override
    public void show(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (fragment instanceof MovieBaseFragment) {
            ((MovieBaseFragment) fragment).setResult(null);
        }

        super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        determineUpVisibility();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void determineUpVisibility() {
        boolean enabled = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
    }
}
