package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;

public class FavoritesActivity extends AppCompatActivity implements FavoriteMoviesAdapter.FavoriteClickHandler {

    public static final String PARENT_KEY_EXTRA = "parent";
    public static final String MOVIE_TITLE_KEY_EXTRA = "title";

    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private RecyclerView mFavoritesList;
    private FavoriteMoviesAdapter mFavAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mFavoritesList = (RecyclerView) findViewById(R.id.rv_favorites);

        mFavAdapter = new FavoriteMoviesAdapter(getBaseContext(), this);
        mFavoritesList.setAdapter(mFavAdapter);
        mFavoritesList.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String movieId = (String) viewHolder.itemView.getTag();

                Uri contentUri = Uri.withAppendedPath(MovieContract.MovieEntry.CONTENT_URI, movieId);
                int numItemsDeleted = getContentResolver().delete(contentUri, null, null);

                if (numItemsDeleted > 0) {
                    fetchTitlesFromContentProvider();
                }
            }
        }).attachToRecyclerView(mFavoritesList);

        fetchTitlesFromContentProvider();
    }

    private void fetchTitlesFromContentProvider() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected void onPreExecute()
            {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Cursor doInBackground(Void... params) {
                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                progressBar.setVisibility(View.INVISIBLE);

                //Check if any favorited movies present
                if (cursor != null && cursor.getCount()>0) {
                    mFavAdapter.swapCursor(cursor);
                    Toast.makeText(getBaseContext(), "Swipe left or right to delete a movie", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "No favorite movies present", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    public void onFavoriteItemClick(String title) {
        Intent intent = new Intent(this, MovieDetail_Activity.class);
        intent.putExtra(PARENT_KEY_EXTRA, FavoritesActivity.class.getSimpleName());
        //Put title of movie at clicked position
        intent.putExtra(MOVIE_TITLE_KEY_EXTRA, title);

        startActivity(intent);
    }
}
