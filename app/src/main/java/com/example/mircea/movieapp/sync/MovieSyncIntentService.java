package com.example.mircea.movieapp.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class MovieSyncIntentService extends IntentService {
    private static final String LOG = MovieSyncIntentService.class.getSimpleName();
    public MovieSyncIntentService() {
        super("MovieSyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        MovieSyncTask.syncMovies(this);

    }
}
