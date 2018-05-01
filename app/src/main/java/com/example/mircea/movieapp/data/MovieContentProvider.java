package com.example.mircea.movieapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.util.Log;

/**
 * Created by mircea on 28.04.2018.
 */

public class MovieContentProvider extends ContentProvider {

    private static final String LOG_TAG = MovieContentProvider.class.getSimpleName();
    // Define final integer constants for the directory of reviews and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIES_ID = 101;
    //  Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES, CODE_MOVIES);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES + "/#", CODE_MOVIES_ID);
        return uriMatcher;
    }

    private MovieDbHelper movieDbHelper;

    @Override
    public boolean onCreate() {
        // [Hint] Declare the DbHelper as a global variable
        Context context = getContext();
        movieDbHelper = new MovieDbHelper(context);
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            // Only perform our implementation of bulkInsert if the URI matches the CODE_MOVIES code
            case CODE_MOVIES:
                // allows for multiple transactions
                db.beginTransaction();
                // keep track of successful inserts
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        if (value == null) {
                            throw new IllegalArgumentException("Cannot have null content valuest ");
                        }
                        long _id = -1;
                        try {
                            _id = db.insertOrThrow(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        } catch (SQLiteConstraintException e) {
                            Log.i(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            MoviesContract.MovieEntry.COLUMN_TITLE)
                                    + " but value is already in database.");
                        }
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    if (rowsInserted > 0)
                        db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

//           Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

//           If the URI does match match CODE_WEATHER, return the super implementation of bulkInsert
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            //all Movies Selected
            case CODE_MOVIES: {
                retCursor = movieDbHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            //Individual Movie based on ID Selected
            case CODE_MOVIES_ID: {
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        projection,
                        MoviesContract.MovieEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder
                );
                return retCursor;
            }
            default: {
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Necunoscut uri:" + uri);
            }


        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CODE_MOVIES: {
                return MoviesContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case CODE_MOVIES_ID: {
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Uri Necunoscut: " + uri);
            }
        }

    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

         /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                numRowsDeleted = movieDbHelper.getWritableDatabase().delete(
                        MoviesContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("uri Necunoscut" + uri);
        }
        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
