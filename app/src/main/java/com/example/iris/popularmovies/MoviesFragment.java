package com.example.iris.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.iris.popularmovies.data.Movie;
import java.util.ArrayList;

/**
 * Movies Fragment that contains a grid view of all movies
 */
public class MoviesFragment extends Fragment {

    private final String MOVIE_LIST = "MOVIES";
    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    protected ArrayList<Movie> mMovieList;
    protected MovieAdapter mMovieAdapter;

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
                    new FetchMovieTask(getContext(), new FetchMoviesTaskCompleteListener()).execute(sortOrder);
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
        new FetchMovieTask(getContext(), new FetchMoviesTaskCompleteListener()).execute(sortOrder);

        return rootView;
    }

    public class FetchMoviesTaskCompleteListener implements AsyncTaskCompleteListener<ArrayList<Movie>>
    {

        @Override
        public void onTaskComplete(ArrayList<Movie> movies)
        {
            if(movies != null) {
                mMovieList = movies;
                mMovieAdapter.clear();
                mMovieAdapter.addAll(movies);
            }
        }
    }
}
