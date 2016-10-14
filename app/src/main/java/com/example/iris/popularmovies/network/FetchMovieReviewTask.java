package com.example.iris.popularmovies.network;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.iris.popularmovies.AsyncTaskCompleteListener;
import com.example.iris.popularmovies.BuildConfig;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.data.MovieReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Iris on 24/08/2016.
 */
public class FetchMovieReviewTask extends AsyncTask<String, Void, ArrayList<MovieReview>> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;
    private AsyncTaskCompleteListener<ArrayList<MovieReview>> listener;
    private String movieId;

    public FetchMovieReviewTask(Context context, AsyncTaskCompleteListener<ArrayList<MovieReview>> listener) {
        this.mContext = context;
        this.listener = listener;
    }

    /**
     * Parse the JSON string return by API
     * @param reviewJsonStr
     * @return
     * @throws JSONException
     */
    private ArrayList<MovieReview> getReviewDataFromJson(String reviewJsonStr)
            throws JSONException {

        final String MDB_RESULTS = "results";
        final String MDB_ID = "id";
        final String MDB_AUTHOR = "author";
        final String MDB_CONTENT = "content";
        final String MDB_URL = "url";

        try {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(MDB_RESULTS);

            Vector<ContentValues> cVVector = new Vector<>(reviewArray.length());

            for(int i = 0; i < reviewArray.length(); i++) {
                // values to be collected

                String reviewId, author, content, url;

                JSONObject currentMovie = reviewArray.getJSONObject(i);

                reviewId = currentMovie.getString(MDB_ID);
                author = currentMovie.getString(MDB_AUTHOR);
                content = currentMovie.getString(MDB_CONTENT);
                url = currentMovie.getString(MDB_URL);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movieId);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, reviewId);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, author);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, content);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL, url);

                cVVector.add(reviewValues);
            }

            int inserted = 0;

            if(cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                Uri reviewUri = MovieContract.ReviewEntry.buildMovieReviewListUri(movieId);
                inserted = mContext.getContentResolver().bulkInsert(reviewUri, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieReviewTask Complete. " + inserted + " Inserted.");

            return null;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected ArrayList<MovieReview> doInBackground(String... params) {

        HttpURLConnection urlConnection;
        BufferedReader reader;

        String reviewJsonStr;
        movieId = params[0];

        try {
            final String MOVIES_BASE_URL = "https://api.themoviedb.org/3";
            final String MOVIE_PATH = "movie";
            final String VIDEO_PATH = "reviews";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(movieId)
                    .appendPath(VIDEO_PATH)
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            Log.v(LOG_TAG, builtUri.toString());

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read input stream
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null ) {
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0) {
                // stream is empty
                return null;
            }

            reviewJsonStr = buffer.toString();
            return getReviewDataFromJson(reviewJsonStr);


        }  catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieReview> data) {
        listener.onTaskComplete(data);
    }
}

