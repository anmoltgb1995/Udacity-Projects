package com.example.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Anmol on 4/14/2017.
 */

public class MovieContract {

    static final String AUTHORITY = "com.example.android.popularmovies";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // This is the path for the "favorites" directory
    static final String PATH_FAVORITES = "favorites";

    public static final class MovieEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_TITLE = "title";
        //The movie's id given by MovieDB
        public static final String COLUMN_MOVIE_ID = "moviedb_id";
        public static final String COLUMN_MOVIE_POSTER = "poster";
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_MOVIE_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }

}
