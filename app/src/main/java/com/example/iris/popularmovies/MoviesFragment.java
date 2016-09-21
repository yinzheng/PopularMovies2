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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.network.FetchMovieTask;

import org.w3c.dom.Text;

/**
 * Movies Fragment that contains a grid view of all movies
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

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
    private RecyclerView mMovieGridView;
    private TextView mMovieEmptyView;
    private int mPosition = RecyclerView.NO_POSITION;

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
                throw new UnsupportedOperationException("Unknown sort order: " + sortOrder);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void onListChanged() {

        String sortOrder = Utility.getPreferedSortOrder(getContext());
        switch (sortOrder) {
            case MOVIE_POPULAR:
                new FetchMovieTask(getContext()).execute(sortOrder);
                getLoaderManager().restartLoader(MOVIE_LOADER_POPULAR, null, this);
                break;
            case MOVIE_TOP_RATED:
                new FetchMovieTask(getContext()).execute(sortOrder);
                getLoaderManager().restartLoader(MOVIE_LOADER_TOP_RATED, null, this);
                break;
            case MOVIE_FAVOURITE:
                getLoaderManager().restartLoader(MOVIE_LOADER_FAVOURITE, null, this);
                break;
            default:
                throw new UnsupportedOperationException("Unknown sort order: " + sortOrder);
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        // empty view
        mMovieEmptyView = (TextView) rootView.findViewById(R.id.recyclerview_movies_empty);



        mMovieGridView = (RecyclerView) rootView.findViewById(R.id.recyclerview_movies);
        mMovieGridView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mMovieGridView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(int id, MovieAdapter.ViewHolder vh) {
//                ((Callback) getActivity()).onItemSelected(MovieContract.MovieEntry.buildMovieUri(id));
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .setData(MovieContract.MovieEntry.buildMovieUri(id));
                startActivity(intent);

                mPosition = vh.getAdapterPosition();
            }
        }, mMovieEmptyView);

        mMovieGridView.setAdapter(mMovieAdapter);

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
                throw new UnsupportedOperationException("Unknown loader id: " + id);

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


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.PREF_SORT_ORDER)) {
            onListChanged();
        }
    }

    public interface Callback {
        public void onItemSelected(Uri uri);
    }
}
