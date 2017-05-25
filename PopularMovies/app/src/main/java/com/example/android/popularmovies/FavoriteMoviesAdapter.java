package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;

/**
 * Created by Anmol on 4/16/2017.
 */

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.FavoriteViewHolder> {
    private static final String TAG = FavoriteMoviesAdapter.class.getSimpleName();
    private final Context mContext;
    private String[] movies;
    private final FavoriteClickHandler mClickHandler;
    private Cursor mCursor = null;

    FavoriteMoviesAdapter(Context context, FavoriteClickHandler mClickHandler) {
        this.mContext = context;
        this.mClickHandler = mClickHandler;
    }

    void swapCursor(Cursor mCursor) {
        if(this.mCursor != null) this.mCursor.close();
        this.mCursor = mCursor;
        notifyDataSetChanged();
    }

    interface FavoriteClickHandler{
        void onFavoriteItemClick(String title);
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FavoriteViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.favorite_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FavoriteViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.movieTitleTextView
                .setText(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)));
        holder.itemView.setTag(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
        //holder.mItemView.setTag(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public String[] getMovies() {
        return movies;
    }

    public void setMovies(String[] movies) {
        this.movies = movies;
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView movieTitleTextView;
        FavoriteViewHolder(View itemView) {
            super(itemView);
            movieTitleTextView = (TextView) itemView.findViewById(R.id.tv_fav_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String title = ((TextView) v.findViewById(R.id.tv_fav_title)).getText().toString();
            Log.d(TAG, "Clicked " + title);
            mClickHandler.onFavoriteItemClick(title);
        }
    }
}
