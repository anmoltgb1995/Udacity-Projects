package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.utilities.MovieDBJsonUtils;
import com.example.android.popularmovies.utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements PopularMoviesAdapter.MovieItemClickHandler, LoaderManager.LoaderCallbacks<MovieDetail[]> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ORDER_BY_POPULAR = "POPULAR";
    private static final String ORDER_BY_TOP_RATED = "TOP_RATED";

    private static final int FETCH_MOVIES_LOADER_ID = 22;

    private RecyclerView mMoviePostersRecyclerView;
    private PopularMoviesAdapter mAdapter;
    private TextView mErrorMessageTextView;
    private ProgressBar progressBar;
    final static private int GRID_LAYOUT_COLUMN_SPAN=2;
    final static private int GRID_COLUMN_SPAN_LAND = 3;

    private MovieDetail[] movieResults;

    private String mSelectedSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviePostersRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_items);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mMoviePostersRecyclerView.setHasFixedSize(true);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mMoviePostersRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), GRID_LAYOUT_COLUMN_SPAN));
        } else {
            mMoviePostersRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), GRID_COLUMN_SPAN_LAND));
        }

        mAdapter = new PopularMoviesAdapter(this, getApplicationContext());
        mMoviePostersRecyclerView.setAdapter(mAdapter);

        //By default, display recently popular movies
        mSelectedSortOrder = ORDER_BY_POPULAR;
        getSupportLoaderManager().initLoader(FETCH_MOVIES_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                loadMovieData();
                break;
            case R.id.action_sort:
                if(item.getTitle().equals(getString(R.string.sort_top_rated))){
                    mSelectedSortOrder = ORDER_BY_TOP_RATED;
                    loadMovieData();
                    item.setTitle(R.string.sort_popular);
                } else if(item.getTitle().equals(getString(R.string.sort_popular))){
                    mSelectedSortOrder = ORDER_BY_POPULAR;
                    loadMovieData();
                    item.setTitle(R.string.sort_top_rated);
                }
                break;
            case R.id.action_show_favorites:
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }


    /**
     * This method is called after this activity has been paused or restarted.
     * This restarts the loader to re-query the underlying data for any changes.
     */
    @Override
    protected void onStart() {
        super.onStart();

        //This will not re-instantiate loader by calling onCreateLoader callback
        getSupportLoaderManager().initLoader(FETCH_MOVIES_LOADER_ID, null, this);
    }

    private void loadMovieData() {
        showMovieDataView();
        mAdapter.setMovieItems(null);

        getSupportLoaderManager().restartLoader(FETCH_MOVIES_LOADER_ID, null, this);
    }


    @Override
    public Loader<MovieDetail[]> onCreateLoader(int id, final Bundle args) {
        Log.d(TAG, "Begin onCreateLoader");
        return new AsyncTaskLoader<MovieDetail[]>(this) {

            private MovieDetail[] mMovieItems;

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "Begin onStartLoading");
                progressBar.setVisibility(View.VISIBLE);
                if (mMovieItems != null) deliverResult(mMovieItems);
                else forceLoad();
            }

            @Override
            public MovieDetail[] loadInBackground() {
                Log.d(TAG, "Begin loadInBackground");

                if( !isOnline() ){
                    Log.v(TAG, "Device not connected to network");
                    return null;
                }

                movieResults = null;
                String JsonResponse = null;

                try {
                    switch(mSelectedSortOrder){
                        case ORDER_BY_POPULAR:
                            JsonResponse = NetworkUtils.getPopularMoviesResponse();
                            break;
                        case ORDER_BY_TOP_RATED:
                            JsonResponse = NetworkUtils.getTopRatedMoviesResponse();
                    }

                    Log.d("Json Response", JsonResponse);
                    movieResults = MovieDBJsonUtils.getMovieDetailsFromJson(JsonResponse);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return movieResults;
            }

            @Override
            public void deliverResult(MovieDetail[] data) {
                mMovieItems = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MovieDetail[]> loader, MovieDetail[] data) {
        Log.d(TAG, "Begin onLoadFinished");
        progressBar.setVisibility(View.INVISIBLE);

        if (data != null) {
            showMovieDataView();
            mAdapter.setMovieItems(data);

        } else {
            showErrorMessage();
        }

    }

    @Override
    public void onLoaderReset(Loader<MovieDetail[]> loader) {
        Log.d(TAG, "Begin onLoaderReset..");
    }


    @Override

    public void onListItemClick(int positionItemClicked) {
        MovieDetail movieItemClicked = mAdapter.getMovieItems()[positionItemClicked];
        Intent intent = new Intent(this, MovieDetail_Activity.class);

        intent.putExtra("title",movieItemClicked.getTitle());
        intent.putExtra("release_date",movieItemClicked.getReleaseDate());
        intent.putExtra("overview",movieItemClicked.getOverview());
        intent.putExtra("poster_path",movieItemClicked.getPosterPath());
        intent.putExtra("rating",movieItemClicked.getRating());
        intent.putExtra("id", movieItemClicked.getId());

        startActivity(intent);
    }


    private void showErrorMessage() {
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mMoviePostersRecyclerView.setVisibility(View.INVISIBLE);
    }


    private void showMovieDataView() {
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mMoviePostersRecyclerView.setVisibility(View.VISIBLE);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
