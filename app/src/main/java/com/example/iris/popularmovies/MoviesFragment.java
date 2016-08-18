package com.example.iris.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.iris.popularmovies.data.Movie;

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
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {

    protected ArrayList<Movie> mMovieList;
    protected MovieAdapter mMovieAdapter;
    private final String MOVIE_LIST = "MOVIES";
    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST)) {
            mMovieList = new ArrayList<>();
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                if (key.equals(SettingsActivity.PREF_SORT_ORDER)) {
                    String sortOrder = Utility.getPreferedSortOrder(getContext());
                    new FetchMovieTask(getContext()).execute(sortOrder);
                }

            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST, mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mMovieAdapter = new MovieAdapter( getActivity(), mMovieList);

        GridView movieGridView = (GridView) rootView.findViewById(R.id.grid_movies);
        movieGridView.setAdapter(mMovieAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("MOVIE", mMovieAdapter.getItem(i));
                detailIntent.putExtra("DATA", bundle);
                startActivity(detailIntent);
            }
        });

        String sortOrder = Utility.getPreferedSortOrder(getContext());
        new FetchMovieTask(getContext()).execute(sortOrder);

        return rootView;
    }



    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        private final Context mContext;

        public FetchMovieTask(Context context) { mContext = context; }

        /**
         * Parse the JSON string return by API
         * @param movieJsonStr
         * @return
         * @throws JSONException
         */
        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr,
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

                ArrayList<Movie> movieList = new ArrayList<>();

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

                    Uri builtUri = Uri.parse(MDB_POSTER_URL).buildUpon()
                            .appendPath(MDB_POSTER_FORMAT)
                            .build();

                    movieList.add(new Movie(
                            movieId,
                            title,
                            originalTitle,
                            releaseDate,
                            overview,
                            voteAverage,
                            builtUri.toString() + posterPath
                    ));
                }

                return movieList;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

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

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if(movies != null) {
                mMovieList = movies;
                mMovieAdapter.clear();
                mMovieAdapter.addAll(movies);
            }
        }
    }


}
