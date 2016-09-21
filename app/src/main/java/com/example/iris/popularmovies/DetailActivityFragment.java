package com.example.iris.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iris.popularmovies.data.Movie;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.data.MovieReview;
import com.example.iris.popularmovies.data.MovieVideo;
import com.example.iris.popularmovies.network.FetchMovieReviewTask;
import com.example.iris.popularmovies.network.FetchMovieVideoTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int MOVIE_LOADER_ITEM = 0;
    private static final int MOVIE_LOADER_VIDEO = 1;
    private static final int MOVIE_LOADER_REVIEW = 2;

    private String LOG_TAG = DetailActivity.class.getSimpleName();
    private final String MOVIE_ITEM = "MOVIE";
    private final String VIDEO_LOADED = "VIDEO_LOADED";
    private final String REVIEW_LOADED = "REVIEW_LOADED";
    private Movie mMovie;
    private String mMovieStr;
    private Uri mMovieUri;
    private boolean mVideoLoaded = false;
    private boolean mReviewLoaded = false;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_VOTED_AVERAGE,
            "CASE WHEN " + MovieContract.MovieListEntry.SUBTABLE_NAME + "." +
                    MovieContract.MovieListEntry.COLUMN_LIST_TYPE + "='" +
                    MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE + "' THEN 1 ELSE 0 END AS " +
                    MovieContract.MovieListEntry.COLUMN_LIST_TYPE
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
    static final int COL_MOVIE_FAVOURITE = 8;

    @BindView(R.id.detail_movie_poster) ImageView detailPoster;
    @BindView(R.id.detail_movie_title) TextView detailTitle;
    @BindView(R.id.detail_movie_overview) TextView detailOverview;
    @BindView(R.id.detail_movie_release_date) TextView detailReleaseDate;
    @BindView(R.id.detail_movie_voted_average) TextView detailVotedAverage;
    @BindView(R.id.detail_movie_favourite) Button detailFavourite;

    public DetailActivityFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER_ITEM, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_ITEM)) {
            Intent intent = getActivity().getIntent();
            if(intent != null) {
                mMovieStr = intent.getDataString();
                mMovieUri = Uri.parse(mMovieStr);
            }
        } else {
            mMovie = savedInstanceState.getParcelable(MOVIE_ITEM);
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey(VIDEO_LOADED)) {
            mVideoLoaded = false;
        } else {
            mVideoLoaded = savedInstanceState.getBoolean(VIDEO_LOADED);
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey(REVIEW_LOADED)) {
            mReviewLoaded = false;
        } else {
            mReviewLoaded = savedInstanceState.getBoolean(REVIEW_LOADED);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MOVIE_ITEM, mMovie);
        outState.putBoolean(VIDEO_LOADED, mVideoLoaded);
        outState.putBoolean(REVIEW_LOADED, mReviewLoaded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

//        if(!mVideoLoaded) {
//            new FetchMovieVideoTask(getContext(), new FetchVideosTaskCompleteListener())
//                    .execute(String.valueOf(mMovie.getID()));
//        }
//
//        if(!mReviewLoaded) {
//            new FetchMovieReviewTask(getContext(), new FetchReviewsTaskCompleteListener())
//                    .execute(String.valueOf(mMovie.getID()));
//        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if ( mMovieUri != null ) {

            return new CursorLoader(getActivity(),
                    mMovieUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);

        }

        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data != null && data.moveToFirst()) {
            int movieId = data.getInt(COL_MOVIE_ID);
            String movieOriginalTitle = data.getString(COL_MOVIE_ORIGINAL_TITLE);
            String movieTitle = data.getString(COL_MOVIE_TITLE);
            String moviePosterPath = data.getString(COL_MOVIE_POSTER_PATH);
            String movieReleaseDate = data.getString(COL_MOVIE_RELEASE_DATE);
            String movieOverview = data.getString(COL_MOVIE_OVERVIEW);
            double movieVotedAverage = data.getDouble(COL_MOVIE_VOTED_AVERAGE);
            int movieFavourite = data.getInt(COL_MOVIE_FAVOURITE);
            
            mMovie = new Movie(movieId, movieTitle, movieOriginalTitle, movieReleaseDate,
                    movieOverview, movieVotedAverage, moviePosterPath, movieFavourite);

            getActivity().setTitle(mMovie.getTitle()); // set title for the detail view

            Picasso.with(getContext()).load(mMovie.getPosterPath()).into(detailPoster);
            detailTitle.setText(mMovie.getOriginalTitle());
            detailOverview.setText(mMovie.getOverview());
            detailReleaseDate.setText(Utility.formatDate(mMovie.getReleaseDate()));
            detailVotedAverage.setText(String.valueOf(mMovie.getVoteAverage()));
            detailFavourite.setText(String.valueOf(mMovie.getFavourite()));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FetchVideosTaskCompleteListener
            implements AsyncTaskCompleteListener<ArrayList<MovieVideo>>
    {

        @Override
        public void onTaskComplete(ArrayList<MovieVideo> data)
        {
            if(data != null) {
                mVideoLoaded = true;
                mMovie.setVideos(data);
            }
        }
    }

    public class FetchReviewsTaskCompleteListener
            implements AsyncTaskCompleteListener<ArrayList<MovieReview>>
    {

        @Override
        public void onTaskComplete(ArrayList<MovieReview> data)
        {
            if(data != null) {
                mReviewLoaded = true;
                mMovie.setReviews(data);
            }
        }
    }
}
