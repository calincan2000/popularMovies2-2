package com.example.mircea.movieapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mircea.movieapp.Adapter.MovieAdapter;
import com.example.mircea.movieapp.data.MoviesContract;
import com.example.mircea.movieapp.model.Movie;
import com.example.mircea.movieapp.sync.MovieSyncUtils;
import com.example.mircea.movieapp.utils.JsonUtils;
import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderCallbacks<Cursor> {
    /*
     * References to RecyclerView and Adapter to reset the list to its
     * "pretty" state when the reset menu item is clicked.
     */


    @BindView(R.id.rv_movies)
    RecyclerView mMoviesList;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    private MovieAdapter mAdapter;
    private static final String LOG = MainActivity.class.getSimpleName();
    private Toast mToast;
    public static ArrayList<Movie> mMovieData = null;
    private static final int FORECAST_LOADER_ID = 0;
    private int mPosition = RecyclerView.NO_POSITION;
    public static String TopRated2 = "TopRated";
    public static String MostPopular2 = "MostPopular";
    public static String TopRated = "top_rated";
    public static String MostPopular = "popular";
    public String selection = TopRated2;

    //Create a String array containing the names of the desired data columns from our ContentProvider
    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] MAIN_FORECAST_PROJECTION = {
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_PRIORITY,
            MoviesContract.MovieEntry._ID,

    };
    //Create constant int values representing each column name's position above
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_MOVIE_TITLE = 0;
    public static final int INDEX_MOVIE_POSTER_PATH = 1;
    public static final int INDEX_MOVIE_OVERVIEW = 2;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 3;
    public static final int INDEX_MOVIE_RELEASE_DATE = 4;
    public static final int INDEX_MOVIE_MOVIE_ID = 5;
    public static final int INDEX_MOVIE_PRIORITY = 6;
    public static final int INDEX_MOVIE__ID= 7;

    /*
   * This ID will be used to identify the Loader responsible for loading our movie. In
   * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
   * We will still use this ID to initialize the loader and create the loader for best practice.
   * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
   * it is unique and consistent.
   */
    private static final int ID_FORECAST_LOADER = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mMoviesList.setLayoutManager(layoutManager);
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mMoviesList.setHasFixedSize(true);

                 /*
          * The ForecastAdapter is responsible for linking our weather data with the Views that
          * will end up displaying our weather data.
          * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
          * is also an instance of that type of handler.
          */
        mAdapter = new MovieAdapter(this, this);
        mMoviesList.setAdapter(mAdapter);

        showLoading();




        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);
       MovieSyncUtils.initialize(this);

    }


    //Override onCreateOptionsMenu
    //Use getMenuInflater().inflate to inflate the menu
    //Return true to display this menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void showJsonData() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        //what you want to show
        mMoviesList.setVisibility(View.VISIBLE);
    }
    /**
     * This method will make the loading indicator visible and hide the movie View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        /* Then, hide the weather data */
        mMoviesList.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }





    // Override onOptionsItemSelected
    // Within this method, get the ID from the MenuItem
    // If the ID equals R.id.action_refresh, create and set a new adapter on the RecyclerView and return true
    // For now, for all other IDs, return super.onOptionsItemSelected
    @Override
    public void onClick(String movieItem, String movieItem2) {


        Context context = this;
        Class destinationActivity = DetailActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        //Refactor onClick to pass the URI for the clicked movie_ID with the Intent
        Uri uriForMovieIdClicked = MoviesContract.MovieEntry.buildMoviesUri(Long.parseLong(movieItem));

        intent.putExtra(Intent.EXTRA_TEXT, movieItem2);
        intent.setData(uriForMovieIdClicked);
        //intent.putExtra(DetailActivity.EXTRA_POSITION,movieItem);
        startActivity(intent);
    }


    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {

        MovieSyncUtils.initialize(this);
        switch (loaderId) {
            //) If the loader requested is our forecast loader, return the appropriate CursorLoader
            case ID_FORECAST_LOADER:
                /* URI for all rows of movies data in our reviews table */
                Uri forecastQueryUri = MoviesContract.MovieEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                // String selection = MoviesContract.MovieEntry;
                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        MoviesContract.MovieEntry.getSqlSelectForRequest(selection),
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

    }

    /**
     * Called when a Loader has finished loading its data.
     * <p>
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
      //  Call mAdapter's swapCursor method and pass in the new Cursor
        mAdapter.swapCursor(data);
      // If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if(mPosition==RecyclerView.NO_POSITION)
            mPosition=0;
     //Smooth scroll the RecyclerView to mPosition
     //mMoviesList.smoothScrollToPosition(mPosition);
     // If the Cursor's size is not equal to mPosition
        if(data.getCount()!=0)
            showJsonData();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
     //Call mAdapter's swapCursor method and pass in null
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mAdapter.swapCursor(null);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();


        switch (itemId) {
            /*
             * When you click the reset menu item, we want to start all over
             * and display the pretty gradient again. There are a few similar
             * ways of doing this, with this one being the simplest of those
             * ways. (in our humble opinion)
             */
           /* case R.id.action_refresh:
                mMoviesList.setAdapter(mAdapter);
                return true;*/
            case R.id.top_rated:
                mMovieData = null;
                selection = TopRated2;
                Log.i(LOG, "xxxxxxxxxxxxb " + selection);

                getSupportLoaderManager().restartLoader(ID_FORECAST_LOADER, null, this);
                return true;

            case R.id.most_popular:
                mMovieData = null;
                selection = MostPopular2;
                getSupportLoaderManager().restartLoader(ID_FORECAST_LOADER, null, this);
                return true;

        }


        return super.onOptionsItemSelected(item);
    }


}
