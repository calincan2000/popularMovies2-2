package com.example.mircea.movieapp.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.content.ContentResolver;

/**
 * Created by mircea on 28.04.2018.
 */

public class MoviesContract {


    /*  Add content provider constants to the Contract
     Clients need to know how to access the task data, and it's your job to provide
     these content URI's for the path to that data:
        1) Content authority,
        2) Base content URI,
        3) Path(s) to the tasks directory
        4) Content URI for data in the MovieEntry class
      */

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String AUTHORITY = "com.example.mircea.movieapp";
    /*
     * Use AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "reviews";


    /* MovieEntry is an inner class that defines the contents of the Movie table */
    public static final class MovieEntry implements BaseColumns {

        // Task table and column names
        /* Used internally as the name of our reviews table. */
        public static final String TABLE_NAME = "reviews";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below

        public static final String _ID = "_id";
        public static final String COLUMN_TITLE = "originalTitle";                          // Type: TEXT
        public static final String COLUMN_POSTER_PATH = "moviePosterImageThumblail";        // Type: TEXT
        public static final String COLUMN_OVERVIEW = "overview";                            // Type: TEXT
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";                    // Type: TEXT
        public static final String COLUMN_RELEASE_DATE = "releaseDate";                     // Type: TEXT
        public static final String COLUMN_MOVIE_ID = "id";                                  // Type: TEXT
        public static final String COLUMN_PRIORITY = "fav";                                 // Type: TEXT
        public static final String COLUMN_REQUEST = "request";                                 // Type: TEXT


        /* The base CONTENT_URI used to query the reviews table from the content provider */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon()
                        .appendPath(PATH_MOVIES)
                        .build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES;

        // for building URIs on insertion
        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Builds a URI that adds the MOVIE_ID to the end of the forecast content URI path.
         * This is used to query details about a single Movie entry by MOVIE_ID. This is what we
         * use for the detail view query.
         *
         * @param movie_ID MOVIE_ID
         * @return Uri to query details about a single movie entry
         */
        public static Uri buildMovieUriWithMovieID(String movie_ID) {
            return CONTENT_URI.buildUpon()
                    .appendPath(movie_ID)
                    .build();
        }

        /**
         * @return The selection part of the reviews query for request
         */
        public static String getSqlSelectForRequest(String S) {
            if (S == "FavoritesCollection")
                return MovieEntry.COLUMN_PRIORITY + " >= " + 1 ;
            else
                return MoviesContract.MovieEntry.COLUMN_REQUEST + " like '%" + S + "%'";
        }

    }
}
