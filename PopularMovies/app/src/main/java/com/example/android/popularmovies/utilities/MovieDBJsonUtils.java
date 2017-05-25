package com.example.android.popularmovies.utilities;

import android.os.Bundle;

import com.example.android.popularmovies.MovieDetail;
import com.example.android.popularmovies.VideoDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anmol on 1/20/2017.
 */

public final class MovieDBJsonUtils {

    public static MovieDetail[] getMovieDetailsFromJson(String jsonString) throws JSONException, IOException{
        //String[] moviePosterUrls;
        MovieDetail[] movieResults;

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray results = jsonObject.getJSONArray("results");

        if(results == null || results.length() == 0 ){
            return null;
        }

        //moviePosterUrls = new String[results.length()];
        movieResults = new MovieDetail[results.length()];

        for(int i=0;i<results.length();i++){
            MovieDetail movieItem = new MovieDetail();
            JSONObject movieItemJson = results.getJSONObject(i);

            movieItem.setId(movieItemJson.getString("id"));
            movieItem.setTitle(movieItemJson.getString("original_title"));
            movieItem.setReleaseDate(movieItemJson.getString("release_date"));
            movieItem.setOverview(movieItemJson.getString("overview"));
            movieItem.setPosterPath(movieItemJson.getString("poster_path"));
            movieItem.setRating(movieItemJson.getString("vote_average"));

            movieResults[i] = movieItem;

        }
        return movieResults;
    }


    public static Bundle getMovieReviewsAsBundle(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray results = jsonObject.getJSONArray("results");

        //return empty object if no reviews found
        if(results == null || results.length() == 0 ){
            return null;
        }

        Bundle movieReviews = new Bundle();
        String[] authors = new String[results.length()];
        String[] reviews = new String[results.length()];
        for (int i=0; i<results.length(); i++) {
            authors[i] = results.getJSONObject(i).getString("author");
            reviews[i] = results.getJSONObject(i).getString("content");
            //movieReviews.put(results.getJSONObject(i).getString("author"), results.getJSONObject(i).getString("content"));
        }

        movieReviews.putStringArray("authors", authors);
        movieReviews.putStringArray("reviews", reviews);
        return movieReviews;
    }


    public static ArrayList<VideoDetail> getVideosFromJson (String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray results = jsonObject.getJSONArray("results");

        //return empty object if no reviews found
        if(results == null || results.length() == 0 ){
            return new ArrayList<>();
        }

        ArrayList<VideoDetail> videoDetails = new ArrayList<>(results.length());
        for (int i=0; i<results.length(); i++) {
            VideoDetail vid = new VideoDetail();

            JSONObject videoItemJson = results.getJSONObject(i);

            vid.setKey(videoItemJson.getString("key"));
            vid.setName(videoItemJson.getString("name"));
            vid.setSite(videoItemJson.getString("site"));
            vid.setSize(videoItemJson.getString("size"));
            String type = videoItemJson.getString("type");

            //only return trailer links
            if(type.equals("Trailer")) {
                vid.setType(type);
                videoDetails.add(vid);
            }
        }

        return videoDetails;
    }

    public static HashMap getMovieReviewsFromJson(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray results = jsonObject.getJSONArray("results");

        //return empty object if no reviews found
        if(results == null || results.length() == 0 ){
            return new HashMap();
        }

        HashMap<String, String> movieReviews = new HashMap<>(results.length());
        for (int i=0; i<results.length(); i++) {
            movieReviews.put(results.getJSONObject(i).getString("author"), results.getJSONObject(i).getString("content"));
        }

        return movieReviews;
    }

}
