package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.databinding.ActivityMovieDetail2Binding;
import com.example.android.popularmovies.utilities.ImageUtils;
import com.example.android.popularmovies.utilities.MovieDBJsonUtils;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.android.popularmovies.data.MovieContract.MovieEntry.*;

public class MovieDetail_Activity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Object> {

    private static final String TAG = MovieDetail_Activity.class.getSimpleName();
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/w320/";

    private static final int REVIEWS_LOADER_ID = 33;
    private static final int VIDEO_DETAILS_LOADER_ID = 44;
    private static final int INSERT_IN_PROVIDER_LOADER_ID = 55;
    private static final int DELETE_FROM_PROVIDER_ID = 66;

    private static final int BUTTON_STATE_FAVORITE = 1;
    private static final int BUTTON_STATE_UNFAVORITE = 0;

    private String movieId, overview, release_date, posterPath, rating, title;
    private MovieReviewsAdapter mReviewsAdapter;
    private MovieVideosAdapter mVideosAdapter;

    private ActivityMovieDetail2Binding mMovieDetailBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_movie_detail_2);
        mMovieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail_2);

        setSupportActionBar(mMovieDetailBinding.toolbarMoviedetail);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            //Check if intent is delivered from FavoritesActivity or not
            //If yes, then load movie details from ContentProvider
            if (intent.hasExtra(FavoritesActivity.PARENT_KEY_EXTRA) &&
                    intent.getStringExtra(FavoritesActivity.PARENT_KEY_EXTRA).equals(FavoritesActivity.class.getSimpleName())) {

                //QUERY CONTENT PROVIDER USING TITLE AND RETRIEVE MOVIE DETAILS
                title = intent.getStringExtra("title");
                loadMovieDetailsFromProvider();

                //Don't show Unnecessary Views
                mMovieDetailBinding.tvReviewsLabel.setVisibility(View.GONE);
                mMovieDetailBinding.tvVideosLabel.setVisibility(View.GONE);
                mMovieDetailBinding.favoriteButton.setVisibility(View.GONE);
                mMovieDetailBinding.progressBar.setVisibility(View.GONE);
            }

            // ELSE RETRIEVE MOVIE DETAILS FROM INTENT AND NETWORK
            else {
                loadMovieDetailsFromIntent(intent);

                //Query if movie is already present in Favorites
                //and hence display 'Unfavorite' button
                mMovieDetailBinding.favoriteButton.setVisibility(View.INVISIBLE);

                if (isMovieInFavorites()) {
                    //Movie already in favorites, display 'UNFAVORITE' button
                    mMovieDetailBinding.favoriteButton.setTag(BUTTON_STATE_UNFAVORITE);
                    mMovieDetailBinding.favoriteButton.setText(getString(R.string.unfavorite_button_text));
                } else {
                    //Movie not in favorites, display 'MARK AS FAVORITE'
                    mMovieDetailBinding.favoriteButton.setTag(BUTTON_STATE_FAVORITE);
                    mMovieDetailBinding.favoriteButton.setText(getString(R.string.favorite_button_text));
                }

                //Download Movie Poster using posterPath
                String imageUrl = IMAGES_BASE_URL + posterPath;
                Log.v(TAG, "Poster URL " + imageUrl);

                Picasso.with(this).load(imageUrl).into(mMovieDetailBinding.ivMoviePoster, new Callback() {
                    @Override
                    public void onSuccess() {
                        mMovieDetailBinding.progressBar.setVisibility(View.GONE);
                        //Make button visible once image has been loaded
                        mMovieDetailBinding.favoriteButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                    }
                });

                setTitle(title);
                mMovieDetailBinding.tvOverview.setText(overview);
                mMovieDetailBinding.tvReleaseDate.setText(getYearFromDate(release_date));
                mMovieDetailBinding.tvRating.setText(getString(R.string.rating_format, rating));
                mMovieDetailBinding.ivMoviePoster.setContentDescription(title);

                //Initialise adapters/loaders to display/load reviews and videos
                mReviewsAdapter = new MovieReviewsAdapter();
                mMovieDetailBinding.rvMovieReviews.setLayoutManager(new LinearLayoutManager(this));
                mMovieDetailBinding.rvMovieReviews.setAdapter(mReviewsAdapter);

                getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, null, this);

                mVideosAdapter = new MovieVideosAdapter();
                mMovieDetailBinding.rvMovieVideos.setLayoutManager(new LinearLayoutManager(this));
                mMovieDetailBinding.rvMovieVideos.setAdapter(mVideosAdapter);

                getSupportLoaderManager().initLoader(VIDEO_DETAILS_LOADER_ID, null, this);
            }
        }
    }


    private void loadMovieDetailsFromIntent(Intent intent) {
        movieId = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        overview = intent.getStringExtra("overview");
        release_date = intent.getStringExtra("release_date");
        posterPath = intent.getStringExtra("poster_path");
        rating = intent.getStringExtra("rating");
    }

    /**
     * This method is called to load movie details from Content Provider in case
     * this activity is displaying was called from FavoritesActivity.
     * It loads details asynchronously and displays them post loading.
     */
    private void loadMovieDetailsFromProvider() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                //Query by title for rest of movie data
                Uri contentUri = Uri.withAppendedPath(CONTENT_URI, title);
                Cursor cursor;
                try {
                    cursor = getContentResolver().query(contentUri, null, null, null, null);
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                cursor.moveToFirst();

                //Load movie details stored in content provider
                overview = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_OVERVIEW));
                rating = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_RATING));
                release_date = cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE));

                byte[] posterAsByteStream = cursor.getBlob(cursor.getColumnIndex(COLUMN_MOVIE_POSTER));
                Bitmap moviePosterBitmap = ImageUtils.getImage(posterAsByteStream);

                cursor.close();

                setTitle(title);
                mMovieDetailBinding.tvOverview.setText(overview);
                mMovieDetailBinding.tvReleaseDate.setText(getYearFromDate(release_date));
                mMovieDetailBinding.tvRating.setText(getString(R.string.rating_format, rating));
                //Set image bitmap in UI thread
                mMovieDetailBinding.ivMoviePoster.setImageBitmap(moviePosterBitmap);
                mMovieDetailBinding.ivMoviePoster.setContentDescription(title);
            }
        }.execute();
    }


    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Begin onCreateLoader..");

        switch (id) {
            //LOADER TO FETCH REVIEWS
            case REVIEWS_LOADER_ID:
                return new AsyncTaskLoader<Object>(this) {
                    private Object mReviews;

                    @Override
                    protected void onStartLoading() {
                        if(mReviews != null) {
                            Log.d(TAG, "Delivering last review result");
                            deliverResult(mReviews);
                        }
                        else forceLoad();
                    }

                    @Override
                    public Object loadInBackground() {
                        Log.d(TAG, "Begin loadInBackground");

                        Bundle movieReviews = null;
                        String JsonResponse;

                        try {
                            JsonResponse = NetworkUtils.getMovieReviewsAsJson(movieId);
                            Log.d("Reviews Json", JsonResponse);
                            movieReviews = MovieDBJsonUtils.getMovieReviewsAsBundle(JsonResponse);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        return movieReviews;
                    }

                    @Override
                    public void deliverResult(Object data) {
                        Log.d(TAG, "Delivering review results");
                        mReviews = data;
                        super.deliverResult(data);
                    }
                };

            //LOADER TO FETCH VIDEO DETAILS
            case VIDEO_DETAILS_LOADER_ID:
                return new AsyncTaskLoader<Object>(this) {
                    private Object mVideoDetails;

                    @Override
                    protected void onStartLoading() {
                        if (mVideoDetails != null){
                            Log.d(TAG, "Delivering last videos result");
                            deliverResult(mVideoDetails);
                        }
                        else forceLoad();
                    }

                    @Override
                    public Object loadInBackground() {
                        Log.d(TAG, "Begin loadInBackground");

                        ArrayList<VideoDetail> videoDetails = new ArrayList<>();
                        String JsonResponse;

                        try {
                            JsonResponse = NetworkUtils.getMovieVideosAsJson(movieId);
                            Log.d("Videos Json", JsonResponse);
                            videoDetails = MovieDBJsonUtils.getVideosFromJson(JsonResponse);
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        return videoDetails;
                    }

                    @Override
                    public void deliverResult(Object data) {
                        Log.d(TAG, "Delivering Videos result..");
                        mVideoDetails = data;
                        super.deliverResult(data);
                    }
                };

            case INSERT_IN_PROVIDER_LOADER_ID:
                return new AsyncTaskLoader<Object>(this) {
                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();
                        forceLoad();
                    }

                    @Override
                    public Object loadInBackground() {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(COLUMN_MOVIE_ID, movieId);
                            contentValues.put(COLUMN_MOVIE_TITLE, title);
                            contentValues.put(COLUMN_MOVIE_OVERVIEW, overview);
                            contentValues.put(COLUMN_MOVIE_RATING, Float.parseFloat(rating));
                            contentValues.put(COLUMN_RELEASE_DATE, release_date);

                            //Convert poster image to byte array that can be stored in sqlite db
                            BitmapDrawable drawable = (BitmapDrawable) mMovieDetailBinding.ivMoviePoster.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            contentValues.put(COLUMN_MOVIE_POSTER, ImageUtils.getBytes(bitmap));

                            try {
                                return getContentResolver().insert(CONTENT_URI, contentValues);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to asynchronously insert movie data.");
                                e.printStackTrace();
                                return null;
                            }
                    }
                };

            case DELETE_FROM_PROVIDER_ID:
                return new AsyncTaskLoader<Object>(this) {
                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();
                        forceLoad();
                    }

                    @Override
                    public Object loadInBackground() {
                        Uri contentUri = CONTENT_URI
                                .buildUpon().appendPath(movieId).build();
                        int itemsDeleted = getContentResolver().delete(contentUri, null, null);
                        return itemsDeleted > 0 ? itemsDeleted : null;
                    }
                };

            default:
                return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Object> loader, Object data) {
        Log.d(TAG, "Begin onLoadFinished");

        switch (loader.getId()) {
            case REVIEWS_LOADER_ID:
                Bundle reviewData = (Bundle) data;
                if (reviewData != null) {
                    mReviewsAdapter.setMovieReviews(reviewData);
                }
                break;

            case VIDEO_DETAILS_LOADER_ID:
                ArrayList<VideoDetail> videoDetails = (ArrayList<VideoDetail>) data;
                if (!videoDetails.isEmpty()) {
                    mVideosAdapter.setMovieVideos(videoDetails.toArray(new VideoDetail[]{}));
                }
                break;
        }
    }


    @Override
    public void onLoaderReset(Loader<Object> loader) {

    }


    public void onClickFavorite(View favoriteButton) {
        //Find whether we need to favorite or unfavorite the movie
        int state = (int) favoriteButton.getTag();
        switch (state) {
            case BUTTON_STATE_FAVORITE:
                //Log.d(TAG, "Starting loader INSERT..");
                getSupportLoaderManager().restartLoader(INSERT_IN_PROVIDER_LOADER_ID, null, this);
                break;
            case BUTTON_STATE_UNFAVORITE:
                //Log.d(TAG, "Starting loader DELETE..");
                getSupportLoaderManager().restartLoader(DELETE_FROM_PROVIDER_ID, null, this);
                break;
        }

        toggleButtonState();
    }


    private void toggleButtonState() {
        switch ((int) mMovieDetailBinding.favoriteButton.getTag()) {
            case BUTTON_STATE_FAVORITE:
                mMovieDetailBinding.favoriteButton.setTag(BUTTON_STATE_UNFAVORITE);
                mMovieDetailBinding.favoriteButton.setText(getString(R.string.unfavorite_button_text));
                break;

            case BUTTON_STATE_UNFAVORITE:
                mMovieDetailBinding.favoriteButton.setTag(BUTTON_STATE_FAVORITE);
                mMovieDetailBinding.favoriteButton.setText(getString(R.string.favorite_button_text));
                break;
        }
    }

    private boolean isMovieInFavorites() {
        Uri contentUri = CONTENT_URI
                .buildUpon().appendPath(this.movieId).build();
        Cursor cursor = getContentResolver().query(contentUri, new String[]{COLUMN_MOVIE_ID}, null, null, null);

        boolean isMovieFav = !(cursor == null || cursor.getCount() == 0);

        if (cursor != null) {
            cursor.close();
        }
        return isMovieFav;
    }


    class MovieReviewsAdapter extends RecyclerView.Adapter<MovieReviewsAdapter.MovieReviewVH> {
        private String[] authors, reviews;

        void setMovieReviews(Bundle movieReviews) {
            authors = null;
            reviews = null;
            if (movieReviews != null) {
                authors = movieReviews.getStringArray("authors");
                reviews = movieReviews.getStringArray("reviews");
            }
            notifyDataSetChanged();
        }

        @Override
        public MovieReviewVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
            return new MovieReviewVH(itemView);
        }

        @Override
        public void onBindViewHolder(MovieReviewVH holder, int position) {
            String authorText = authors[position] + ":";
            holder.mAuthorTextView.setText(authorText);

            String reviewString = reviews[position];
            reviewString = reviewString.replaceAll("\r\n\r\n", "\n\n");
            reviewString = reviewString.replaceAll("\r\n", " ");
            holder.mContentTextView.setText(reviewString);
        }

        @Override
        public int getItemCount() {
            return authors == null ? 0 : authors.length;
        }


        class MovieReviewVH extends RecyclerView.ViewHolder {
            final TextView mAuthorTextView, mContentTextView;

            MovieReviewVH(View itemView) {
                super(itemView);
                mAuthorTextView = (TextView) itemView.findViewById(R.id.tv_review_author);
                mContentTextView = (TextView) itemView.findViewById(R.id.tv_review_content);
            }
        }
    }


    class MovieVideosAdapter extends RecyclerView.Adapter<MovieVideosAdapter.MovieVideoVH> {

        void setMovieVideos(VideoDetail[] movieVideos) {
            this.movieVideos = movieVideos;
            notifyDataSetChanged();
        }

        VideoDetail[] movieVideos;

        @Override
        public MovieVideoVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
            return new MovieVideoVH(itemView);
        }

        @Override
        public void onBindViewHolder(MovieVideoVH holder, int position) {
            VideoDetail vid = movieVideos[position];
            holder.mVideoTypeTextView.setText("(" + vid.getType() + ")");
            holder.mVideoTitleTextView.setText(vid.getName());
            holder.mVideoSizeTextView.setText(getString(R.string.video_size, vid.getSize()));
        }

        @Override
        public int getItemCount() {
            return movieVideos == null ? 0 : movieVideos.length;
        }

        class MovieVideoVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView mVideoTitleTextView, mVideoSizeTextView, mVideoTypeTextView;

            MovieVideoVH(View itemView) {
                super(itemView);
                mVideoTitleTextView = (TextView) itemView.findViewById(R.id.tv_video_title);
                mVideoSizeTextView = (TextView) itemView.findViewById(R.id.tv_video_size);
                mVideoTypeTextView = (TextView) itemView.findViewById(R.id.tv_video_type);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int clickedPosition = getAdapterPosition();
                //Toast.makeText(view.getContext(), "Position: " + clickedPosition, Toast.LENGTH_SHORT).show();
                if (movieVideos[clickedPosition].getSite().equals("YouTube")) {
                    String key = movieVideos[clickedPosition].getKey();
                    String urlString = NetworkUtils.buildYoutubeUrlWithVideoKey(key);
                    openWebPage(urlString);
                }
            }
        }
    }


    private String getYearFromDate(String date) {
        return date.split("-")[0];
    }

    public void shareVideoURL(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        int clickedPosition = mMovieDetailBinding.rvMovieVideos.getChildAdapterPosition((View) view.getParent());
        String youtubeVideoKey = ((MovieVideosAdapter) mMovieDetailBinding.rvMovieVideos.getAdapter()).movieVideos[clickedPosition].getKey();
        String youtubeUrlString = NetworkUtils.buildYoutubeUrlWithVideoKey(youtubeVideoKey);

        intent.putExtra(Intent.EXTRA_TEXT, youtubeUrlString);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
