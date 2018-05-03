package com.example.mircea.movieapp.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.mircea.movieapp.MainActivity;
import com.example.mircea.movieapp.data.MoviesContract;
import com.example.mircea.movieapp.utils.JsonUtils;
import com.example.mircea.movieapp.utils.OpenMovieJsonUtils;

import java.net.URL;

/**
 * Created by mircea on 30.04.2018.
 */

public class MovieSyncTask {
    private static final String LOG = MovieSyncTask.class.getSimpleName();

    /**
     * Performs the network request for updated movies, parses the JSON from that request, and
     * inserts the new movies information into our ContentProvider. Will notify the user that new
     * movies has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncMovies(Context context) {

        try {

            Log.i(LOG, "xxxxxxxxxxxxb SYNC SYNC SYNC " );

            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * movies. It will decide whether to create a URL .
             */
            URL movieRequestUrl = JsonUtils.createUrl(JsonUtils.buildUrl(MainActivity.TopRated).toString());
            URL movieRequestUrl2 = JsonUtils.createUrl(JsonUtils.buildUrl(MainActivity.MostPopular).toString());

            /* Use the URL to retrieve the JSON */
            String jsonMovieResponse = JsonUtils.getResponseFromHttpUrl(movieRequestUrl);
            String jsonMovieResponse2 = JsonUtils.getResponseFromHttpUrl(movieRequestUrl2);

            /* Parse the JSON into a list of movies values */
            ContentValues[] moviesValues = OpenMovieJsonUtils.getMovieContentValuesFromJson(
                    context,
                    jsonMovieResponse,
                    jsonMovieResponse2
                    );



            /*
             * In cases where our JSON contained an error code, getMovieContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (moviesValues!=null && moviesValues.length!=0)
            {
               /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver movieContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
                movieContentResolver.delete(
                        MoviesContract.MovieEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new weather data into Sunshine's ContentProvider */
                movieContentResolver.bulkInsert(
                        MoviesContract.MovieEntry.CONTENT_URI,
                        moviesValues);

            }
        } catch (Exception e) {
            /*Server probably invalid*/
            e.printStackTrace();
        }

    }
}
