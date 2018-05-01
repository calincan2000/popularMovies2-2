package com.example.mircea.movieapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mircea.movieapp.Adapter.MovieAdapter;
import com.example.mircea.movieapp.Adapter.ReviewsAdapter;
import com.example.mircea.movieapp.Adapter.TrailerAdapter;
import com.example.mircea.movieapp.data.MoviesContract;
import com.example.mircea.movieapp.model.Movie;
import com.example.mircea.movieapp.model.Review;
import com.example.mircea.movieapp.model.Trailers;
import com.example.mircea.movieapp.utils.JsonUtils;
import com.example.mircea.movieapp.utils.OpenMovieJsonUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity
        implements TrailerAdapter.TrailerAdapterOnClickHandler, ReviewsAdapter.ReviewAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.image_iv)
    ImageView mImageDetail;
    @BindView(R.id.original_title)
    TextView mOriginalTitle;
    @BindView(R.id.overview)
    TextView mOverview;
    @BindView(R.id.vote_average)
    TextView mVoteAverage;
    @BindView(R.id.release_date)
    TextView mReleaseDate;
    @BindView(R.id.trailers)
    RecyclerView mTrailers;
    @BindView(R.id.reviews)
    TextView mReviews;

    private ReviewsAdapter mReviewAdapter;
    private TrailerAdapter mAdapter;
    private static final String LOG = MovieAdapter.class.getSimpleName();
    public List<Movie> movieResultsData = MainActivity.mMovieData;
    public static ArrayList<Trailers> mTrailersData = null;
    public static ArrayList<Review> mReviewsData = null;


    public String textEntered = null;
    private Context mContext;
    String idx = null;
    /*   @BindView(R.id.pb_loading_indicator)
       ProgressBar mLoadingIndicator;*/
    String searchUrl;
    // Declare a private Uri field called mUri
    /* The URI that is used to access the chosen movie details */
    private Uri mUri;


    //Create a String array containing the names of the desired data columns from our ContentProvider
    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * weather data.
     */
    public static final String[] MAIN_FORECAST_PROJECTION = {
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_PRIORITY,

    };

    //Create constant int values representing each column name's position above
    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_MOVIE_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_POSTER_PATH = 1;
    public static final int INDEX_MOVIE_OVERVIEW = 2;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 3;
    public static final int INDEX_MOVIE_RELEASE_DATE = 4;
    public static final int INDEX_MOVIE_TITLE = 5;
    public static final int INDEX_MOVIE_PRIORITY = 6;

    // Create a constant int to identify our loader used in DetailActivity
    /*
     * This ID will be used to identify the Loader responsible for loading the movie details
     * for a particular movie. In some cases, one Activity can deal with many Loaders. However, in
     * our case, there is only one. We will still use this ID to initialize the loader and create
     * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
     * whatever number you like, so long as it is unique and consistent.
     */
    private static final int ID_DETAIL_LOADER = 353;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
            textEntered = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
            // Use getData to get a reference to the URI passed with this Activity's Intent
            Log.i(LOG, "xxxxxxxxxxxxb " + textEntered);

            mUri = getIntent().getData();
            Log.i(LOG, "xxxxxxxxxxxxb234 " + getIntent().getData());

            // Throw a NullPointerException if that URI is null
            if (mUri == null)
                throw new NullPointerException("URI for DetailActivity cannot be null");


            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
            mTrailers.setLayoutManager(layoutManager);


            mTrailers.setHasFixedSize(true);
            mAdapter = new TrailerAdapter(DetailActivity.this, new ArrayList<Trailers>(), DetailActivity.this);
            mTrailers.setAdapter(mAdapter);

        /*    mReviews.setHasFixedSize(true);
            mReviewAdapter= new ReviewsAdapter(DetailActivity.this,new ArrayList<Review>(),DetailActivity.this);
            mReviews.setAdapter(mReviewAdapter);
*/

            /* This connects our Activity into the loader lifecycle. */
            getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
            getSupportLoaderManager().initLoader(0, null, TrailerLoaderListener);
            getSupportLoaderManager().initLoader(1, null, ReviewsResultLoaderListener);


        }
    }


    private LoaderManager.LoaderCallbacks<String[]> ReviewsResultLoaderListener
            = new LoaderManager.LoaderCallbacks<String[]>() {
        @Override
        public Loader<String[]> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<String[]>(DetailActivity.this) {
                String[] mReviewsData = null;

                @Override
                protected void onStartLoading() {
                    if (mReviewsData != null) {
                        deliverResult(mReviewsData);
                    } else {
                        // mLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public String[] loadInBackground() {
                    URL ReviewsRequestUrl = JsonUtils.createUrl(JsonUtils.buildUrl(textEntered + "/reviews").toString());
                    String ReviewsSearchResults = null;


                    try {

                        ReviewsSearchResults = JsonUtils
                                .getResponseFromHttpUrl(ReviewsRequestUrl);

                        String[] ReviewsResultData = OpenMovieJsonUtils.getSimpleReviewsStringsFromJson(ReviewsSearchResults);

                        return ReviewsResultData;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                /**
                 * Sends the result of the load to the registered listener.
                 *
                 * @param data The result of the load
                 */

                public void deliverResult(String[] data) {
                    mReviewsData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String[]> loader, String[] data) {
            Log.i(LOG, "xxxxxxxxxxxxb1 " + data[0].toString());

            for (String datad : data) {
                mReviews.append(datad + "\n\n\n");
            }
        }

        @Override
        public void onLoaderReset(Loader<String[]> loader) {

        }
    };


    private LoaderManager.LoaderCallbacks<ArrayList<Trailers>> TrailerLoaderListener
            = new LoaderManager.LoaderCallbacks<ArrayList<Trailers>>() {

        @Override
        public Loader<ArrayList<Trailers>> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<ArrayList<Trailers>>(DetailActivity.this) {

                @Override
                protected void onStartLoading() {
                 /*   if (mTrailersData != null) {
                        mTrailersData=null;
                    } else {
                        // mLoadingIndicator.setVisibility(View.VISIBLE);*/
                    forceLoad();

                }

                @Override
                public ArrayList<Trailers> loadInBackground() {
                    URL TrailerRequestUrl = JsonUtils.createUrl(JsonUtils.buildUrl(textEntered + "/videos").toString());
                    String trailerSearchResults = null;
                    ArrayList<Trailers> trailerResultData = new ArrayList<>();

                    try {
                        trailerSearchResults = JsonUtils
                                .getResponseFromHttpUrl(TrailerRequestUrl);
                        trailerResultData.addAll(OpenMovieJsonUtils.parseTrailersJson(trailerSearchResults));

                        return trailerResultData;

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(ArrayList<Trailers> data) {
                    mTrailersData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Trailers>> loader, ArrayList<Trailers> data) {
            mAdapter.setTrailersData(data);


        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Trailers>> loader) {

        }
    };


    @Override
    public void onClick(String TrailerItem) {

        Context context = this;


        Log.i(LOG, "xxxxxxxxxxxxb " + mTrailersData.get(Integer.parseInt(TrailerItem)).getTrailer());

        // Convert the String URL into a URI object (to pass into the Intent constructor)
        Uri resultUri = Uri.parse(mTrailersData.get(Integer.parseInt(TrailerItem)).getTrailer());

        // Create a new intent to view the Trailer URI
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, resultUri);

        // Send the intent to launch a new activity
        startActivity(websiteIntent);


    }

    // Override onCreateLoader

    /**
     * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
     *
     * @param loaderId   The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle loaderArgs) {
        switch (loaderId) {

//         If the loader requested is our detail loader, return the appropriate CursorLoader
            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        MAIN_FORECAST_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }
    //Override onLoadFinished

    /**
     * Runs on the main thread when a load is complete. If initLoader is called (we call it from
     * onCreate in DetailActivity) and the LoaderManager already has completed a previous load
     * for this Loader, onLoadFinished will be called immediately. Within onLoadFinished, we bind
     * the data to our views so the user can see the details of the weather on the date they
     * selected from the forecast.
     *
     * @param loader The cursor loader that finished.
     * @param data   The cursor that is being returned.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor data) {
        //  Check before doing anything that the Cursor has valid data
        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */


        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        /****************
         * Movie OriginalTitle *
         ****************/
        /*
         * Read the OriginalTitle from the cursor.
         */
        mOriginalTitle.setText(data.getString(INDEX_MOVIE_TITLE));


        /****************
         * Movie Overview *
         ****************/
        mOverview.setText(data.getString(INDEX_MOVIE_OVERVIEW));
        /****************
         * Movie ReleaseDate *
         ****************/
        mReleaseDate.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
        /****************
         * Movie VoteAverage *
         ****************/
        mVoteAverage.setText(data.getString(INDEX_MOVIE_VOTE_AVERAGE));
        /****************
         * Movie ImageDetail *
         ****************/
        Picasso.get()
                .load(data.getString(INDEX_MOVIE_POSTER_PATH))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.user_placeholder)
                .into(mImageDetail, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if cache loading failed
                        Picasso.get()
                                .load(data.getString(INDEX_MOVIE_POSTER_PATH))
                                .placeholder(R.drawable.user_placeholder)
                                .error(R.drawable.user_placeholder_error)
                                .into(mImageDetail);
                    }
                });
        /****************
         * Movie ID *
         ****************/
        idx = data.getString(INDEX_MOVIE_MOVIE_ID);


    }
    //Override onLoaderReset, but don't do anything in it yet

    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     * Since we don't store any of this cursor's data, there are no references we need to remove.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
