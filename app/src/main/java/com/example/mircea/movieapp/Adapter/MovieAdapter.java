package com.example.mircea.movieapp.Adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View.OnClickListener;

import com.example.mircea.movieapp.MainActivity;
import com.example.mircea.movieapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String LOG = MovieAdapter.class.getSimpleName();

    //Declare a private final Context field called mContext
    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;


    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void onClick(String movieItem, String movieItem2);
    }
    //Declare a private Cursor field called mCursor
    private Cursor mCursor;
    // Add an interface called ListItemClickListener
    // Within that interface, define a void method called onListItemClick that takes an int as a parameter
    /**
     * Creates a ForecastAdapter.
     *
     * @param context Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(Context context,  MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
        mContext = context;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */


    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.movie_grid_item,viewGroup,false);
        view.setFocusable(true);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */


    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder holder, int position) {

        mCursor.moveToPosition(position);


        Picasso.get()
                .load(mCursor.getString(MainActivity.INDEX_MOVIE_POSTER_PATH))
                .placeholder(R.drawable.user_placeholder)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.gridItemMovieView/*, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        // Try again online, if cache loading failed
                        Picasso.get()
                                .load(mCursor.getString(MainActivity.INDEX_MOVIE_POSTER_PATH))
                                .placeholder(R.drawable.user_placeholder)
                                .error(R.drawable.user_placeholder_error)
                                .into(holder.gridItemMovieView);
                    }
                }*/);

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

   // Create a new method that allows you to swap Cursors.
    /**
     * Swaps the cursor used by the MovieAdapter for its movie data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as MovieAdapter's data source
     */
    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
//     After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for a list item.
     */

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        @BindView(R.id.thumbnail)
        ImageView gridItemMovieView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String movieItem = mCursor.getString(MainActivity.INDEX_MOVIE__ID);
            String movieItem2= mCursor.getString(MainActivity.INDEX_MOVIE_MOVIE_ID);
            mClickHandler.onClick(movieItem,movieItem2);




        }


    }


}
