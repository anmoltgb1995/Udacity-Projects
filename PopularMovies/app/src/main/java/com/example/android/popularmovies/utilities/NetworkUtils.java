package com.example.android.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Anmol on 1/19/2017.
 */

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String POPULAR_MOVIES_BASE_URL =
            "http://api.themoviedb.org/3/movie/popular";

    private static final String TOP_RATED_MOVIES_BASE_URL =
            "http://api.themoviedb.org/3/movie/top_rated";

    private static final String MOVIE_DETAIL_BASE_URL=
            "http://api.themoviedb.org/3/movie";

    public static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    public static final String YOUTUBE_QUERY_PARAM = "v";

    private static final String IMAGES_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    private final static String API_KEY_PARAM = "api_key";
    private final static String API_KEY = "f1738f13742673d507ee582721348753";

    public static String getPopularMoviesResponse() throws IOException{
        return getResponseHttpUrl(buildUrl(POPULAR_MOVIES_BASE_URL));
    }

    public static String getTopRatedMoviesResponse() throws IOException{
        return getResponseHttpUrl(buildUrl(TOP_RATED_MOVIES_BASE_URL));
    }


    private static URL buildUrl(String baseUrlString) {

        Uri moviesUri = Uri.parse(baseUrlString).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY).build();

        URL url = null;
        try {
            url = new URL(moviesUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);
        return url;
    }


    private static String getResponseHttpUrl(URL url) throws IOException{

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try{
            InputStream in = conn.getInputStream();

            Scanner sc = new Scanner(in);
            sc.useDelimiter("\\A");
            if(sc.hasNext()) return sc.next();
            else return null;
        }finally {
            conn.disconnect();
        }


    }

    public static String getMovieReviewsAsJson (String movieId) throws IOException {
        String movieReviewUriAsString = Uri.parse(MOVIE_DETAIL_BASE_URL).buildUpon()
                .appendPath(movieId).appendPath("reviews").build().toString();

        String jsonString = getResponseHttpUrl(buildUrl(movieReviewUriAsString));
        return jsonString;

    }

    public static String getMovieVideosAsJson (String movieId) throws IOException {
        String movieReviewUriAsString = Uri.parse(MOVIE_DETAIL_BASE_URL).buildUpon()
                .appendPath(movieId).appendPath("videos").build().toString();

        String jsonString = getResponseHttpUrl(buildUrl(movieReviewUriAsString));
        return jsonString;

    }

    public static String buildYoutubeUrlWithVideoKey (String videoKey) {
        return Uri.parse(NetworkUtils.YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(NetworkUtils.YOUTUBE_QUERY_PARAM, videoKey).toString();
    }

}
