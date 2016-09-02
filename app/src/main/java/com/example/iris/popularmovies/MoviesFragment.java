package com.example.iris.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.iris.popularmovies.data.Movie;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.network.FetchMovieTask;

import java.util.ArrayList;

/**
 * Movies Fragment that contains a grid view of all movies
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER_POPULAR = 0;
    private static final int MOVIE_LOADER_TOP_RATED = 1;
    private static final int MOVIE_LOADER_FAVOURITE = 2;

    private static final String MOVIE_POPULAR = "popular";
    private static final String MOVIE_TOP_RATED = "top_rated";
    private static final String MOVIE_FAVOURITE = "favourite";

    private final String MOVIE_LIST = "MOVIES";
    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private static final String SELECTED_KEY = "selected_position";
    private MovieAdapter mMovieAdapter;
    private GridView mMovieGridView;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTED_AVERAGE
    };

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_ORIGINAL_TITLE = 3;
    static final int COL_MOVIE_OVERVIEW = 4;
    static final int COL_MOVIE_POSTER_PATH = 5;
    static final int COL_MOVIE_RELEASE_DATE = 6;
    static final int COL_MOVIE_VOTED_AVERAGE = 7;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        String sortOrder = Utility.getPreferedSortOrder(getContext());
        switch (sortOrder) {
            case MOVIE_POPULAR:
                getLoaderManager().initLoader(MOVIE_LOADER_POPULAR, null, this);
                break;
            case MOVIE_TOP_RATED:
                getLoaderManager().initLoader(MOVIE_LOADER_TOP_RATED, null, this);
                break;
            case MOVIE_FAVOURITE:
                getLoaderManager().initLoader(MOVIE_LOADER_FAVOURITE, null, this);
                break;
            default:
                getLoaderManager().initLoader(MOVIE_LOADER_POPULAR, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                if (key.equals(SettingsActivity.PREF_SORT_ORDER)) {
                    onListChanged();
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefListener);

    }

    private void onListChanged() {
        String sortOrder = Utility.getPreferedSortOrder(getContext());
        new FetchMovieTask(getContext()).execute(sortOrder);
        switch (sortOrder) {
            case MOVIE_POPULAR:
                getLoaderManager().restartLoader(MOVIE_LOADER_POPULAR, null, this);
                break;
            case MOVIE_TOP_RATED:
                getLoaderManager().restartLoader(MOVIE_LOADER_TOP_RATED, null, this);
                break;
            case MOVIE_FAVOURITE:
                getLoaderManager().restartLoader(MOVIE_LOADER_FAVOURITE, null, this);
                break;
            default:
                getLoaderManager().restartLoader(MOVIE_LOADER_POPULAR, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mMovieGridView = (GridView) rootView.findViewById(R.id.grid_movies);
        mMovieGridView.setAdapter(mMovieAdapter);

//        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("MOVIE", mMovieAdapter.getItem(i));
//                detailIntent.putExtra("DATA", bundle);
//                startActivity(detailIntent);
//            }
//        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieListUri;

        switch (id) {
            case MOVIE_LOADER_POPULAR:
                movieListUri = MovieContract.MovieListEntry.buildMovieListUri(MOVIE_POPULAR);
                break;
            case MOVIE_LOADER_TOP_RATED:
                movieListUri = MovieContract.MovieListEntry.buildMovieListUri(MOVIE_TOP_RATED);
                break;
            case MOVIE_LOADER_FAVOURITE:
                movieListUri = MovieContract.MovieListEntry.buildMovieListUri(MOVIE_FAVOURITE);
                break;
            default:
                movieListUri = MovieContract.MovieListEntry.buildMovieListUri(MOVIE_POPULAR);

        }

        return new CursorLoader(getActivity(),
                movieListUri,
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
