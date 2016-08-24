package com.example.iris.popularmovies.network;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.iris.popularmovies.AsyncTaskCompleteListener;
import com.example.iris.popularmovies.BuildConfig;
import com.example.iris.popularmovies.data.MovieVideo;

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

/**
 * Created by Iris on 22/08/2016.
 */
public class FetchMovieVideoTask extends AsyncTask<String, Void, ArrayList<MovieVideo>> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;
    private AsyncTaskCompleteListener<ArrayList<MovieVideo>> listener;

    public FetchMovieVideoTask(Context context, AsyncTaskCompleteListener<ArrayList<MovieVideo>> listener) {
        this.mContext = context;
        this.listener = listener;
    }

    /**
     * Parse the JSON string return by API
     * @param movieJsonStr
     * @return
     * @throws JSONException
     */
    private ArrayList<MovieVideo> getVideoDataFromJson(String videoJsonStr)
            throws JSONException {

        final String MDB_RESULTS = "results";
        final String MDB_ID = "id";
        final String MDB_KEY = "key";
        final String MDB_NAME = "name";
        final String MDB_SITE = "site";
        final String MDB_SIZE = "size";
        final String MDB_TYPE = "type";

        try {
            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(MDB_RESULTS);

            ArrayList<MovieVideo> videoList = new ArrayList<>();

            for(int i = 0; i < videoArray.length(); i++) {
                // values to be collected

                String videoId, key, name, site, type;

                JSONObject currentMovie = videoArray.getJSONObject(i);

                videoId = currentMovie.getString(MDB_ID);
                key = currentMovie.getString(MDB_KEY);
                name = currentMovie.getString(MDB_NAME);
                site = currentMovie.getString(MDB_SITE);
                type = currentMovie.getString(MDB_TYPE);

                videoList.add(new MovieVideo(
                        videoId,
                        key,
                        name,
                        site,
                        type
                ));
            }

            return videoList;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected ArrayList<MovieVideo> doInBackground(String... params) {

        HttpURLConnection urlConnection;
        BufferedReader reader;

        String videoJsonStr;
        String movieId = params[0];

        try {
            final String MOVIES_BASE_URL = "https://api.themoviedb.org/3";
            final String MOVIE_PATH = "movie";
            final String VIDEO_PATH = "videos";
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

            videoJsonStr = buffer.toString();
            return getVideoDataFromJson(videoJsonStr);


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
    protected void onPostExecute(ArrayList<MovieVideo> data) {
        listener.onTaskComplete(data);
    }
}
