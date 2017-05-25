package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * Created by Anmol on 1/20/2017.
 */

public class PopularMoviesAdapter extends RecyclerView.Adapter<PopularMoviesAdapter.PMAdapterViewHolder> {

    private static final String TAG = PopularMoviesAdapter.class.getSimpleName();

    final private MovieItemClickHandler mClickHandler;

    private final Context context;
    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    private MovieDetail[] movieItems;

    public PopularMoviesAdapter(MovieItemClickHandler mClickHandler, Context context){
        this.mClickHandler = mClickHandler;
        this.context = context;
    }

    public MovieDetail[] getMovieItems() {
        return movieItems;
    }


    public void setMovieItems(MovieDetail[] movieItems) {
        this.movieItems = movieItems;
        notifyDataSetChanged();
    }

    public interface MovieItemClickHandler{
        void onListItemClick(int positionItemClicked);
    }



    @Override
    public PMAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View itemView = layoutInflater.inflate(R.layout.movie_item,parent,false);

        return new PMAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PMAdapterViewHolder holder, int position) {

        String imageUrl = IMAGES_BASE_URL + movieItems[position].getPosterPath();
        //Log.v(TAG, "Built Image URL " + imageUrl);
        Picasso.with(this.context).load(imageUrl).into(holder.mMoviePosterImageView);

    }

    @Override
    public int getItemCount() {
        return movieItems == null ? 0 : movieItems.length ;
    }


    public class PMAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView mMoviePosterImageView;

        public PMAdapterViewHolder(View itemView) {
            super(itemView);
            mMoviePosterImageView = (ImageView) itemView.findViewById(R.id.iv_movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mClickHandler.onListItemClick(clickedPosition);
        }
    }
}
