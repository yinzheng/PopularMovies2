package com.example.iris.popularmovies.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.iris.popularmovies.AsyncTaskCompleteListener;
import com.example.iris.popularmovies.BuildConfig;
import com.example.iris.popularmovies.data.Movie;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.data.MovieContract.MovieEntry;
import com.example.iris.popularmovies.data.MovieDbHelper;

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
 * Created by Iris on 22/08/2016.
 */
public class FetchMovieTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context mContext;
    private AsyncTaskCompleteListener<ArrayList<Movie>> listener;

    public FetchMovieTask(Context context) {
        this.mContext = context;
    }

    /**
     * Parse the JSON string return by API
     * @param movieJsonStr
     * @return
     * @throws JSONException
     */
    private Void getMovieDataFromJson(String movieJsonStr,
                                                  String listType)
            throws JSONException {

        final String MDB_RESULTS = "results";
        final String MDB_ID = "id";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_TITLE = "title";
        final String MDB_OVERVIEW = "overview";
        final String MDB_ORIGINAL_TITLE = "original_title";
        final String MDB_ORIGINAL_LANGUAGE = "original_language";
        final String MDB_POPULARITY = "popularity";
        final String MDB_BACKDROP_PATH = "backdrop_path";
        final String MDB_VIDEO = "video";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_GENRE_IDS = "genre_ids";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_POSTER_URL = "http://image.tmdb.org/t/p/";
        final String MDB_POSTER_FORMAT = "w185";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);
            Uri builtUri = Uri.parse(MDB_POSTER_URL).buildUpon()
                    .appendPath(MDB_POSTER_FORMAT)
                    .build();

            Vector<ContentValues> cVVector = new Vector<>(movieArray.length());
            Vector<ContentValues> listVector = new Vector<>(movieArray.length());


            for(int i = 0; i < movieArray.length(); i++) {
                // values to be collected

                int movieId;
                String posterPath, overview, title, originalTitle, releaseDate;
                double voteAverage;

                JSONObject currentMovie = movieArray.getJSONObject(i);

                movieId = currentMovie.getInt(MDB_ID);
                posterPath = currentMovie.getString(MDB_POSTER_PATH);
                overview = currentMovie.getString(MDB_OVERVIEW);
                title = currentMovie.getString(MDB_TITLE);
                originalTitle = currentMovie.getString(MDB_ORIGINAL_TITLE);
                releaseDate = currentMovie.getString(MDB_RELEASE_DATE);
                voteAverage = currentMovie.getDouble(MDB_VOTE_AVERAGE);


                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_VOTED_AVERAGE, voteAverage);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, builtUri.toString() + posterPath);

                cVVector.add(movieValues);

                ContentValues listValues = new ContentValues();
                listValues.put(MovieContract.MovieListEntry.COLUMN_MOVIE_KEY, movieId);
                listValues.put(MovieContract.MovieListEntry.COLUMN_LIST_TYPE,
                        listType);
                listVector.add(listValues);
            }


            int inserted = 0;

            if(cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                Uri movieListUri = MovieContract.MovieListEntry.buildMovieListUri("");
                inserted = mContext.getContentResolver().bulkInsert(movieListUri, cvArray);
            }

            if(listVector.size() > 0) {
                ContentValues[] listArray = new ContentValues[listVector.size()];
                listVector.toArray(listArray);

                Uri movieListUri = MovieContract.MovieListEntry.buildMovieListUri(listType);

                String[] whereArgs = {listType};
                mContext.getContentResolver().delete(movieListUri,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE + " =?",
                        whereArgs); // delete previous list

                inserted = mContext.getContentResolver().bulkInsert(movieListUri, listArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted.");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection;
        BufferedReader reader;

        String movieJsonStr;
        String listType = params[0];

        try {
            final String MOVIES_BASE_URL = "https://api.themoviedb.org/3";
            final String MOVIE_PATH = "movie";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(MOVIE_PATH)
                    .appendPath(listType)
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

            movieJsonStr = buffer.toString();
            return getMovieDataFromJson(movieJsonStr, listType);


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
}
