package com.example.iris.popularmovies;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.iris.popularmovies.data.Movie;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.data.MovieReview;
import com.example.iris.popularmovies.data.MovieVideo;
import com.example.iris.popularmovies.network.FetchMovieReviewTask;
import com.example.iris.popularmovies.network.FetchMovieVideoTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int MOVIE_LOADER_ITEM = 0;
    private static final int MOVIE_LOADER_VIDEO = 1;
    private static final int MOVIE_LOADER_REVIEW = 2;

    private String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private final String MOVIE_STR = "MOVIE";
    private final String VIDEO_LOADED = "VIDEO_LOADED";
    private final String REVIEW_LOADED = "REVIEW_LOADED";
    private Movie mMovie;
    private List<MovieVideo> mMovieVideoList;
    private List<MovieReview> mMovieReviewList;

    public static final String MOVIE_URI = "movie_uri";
    private Uri mMovieUri;

    private boolean mVideoLoaded = false;
    private boolean mReviewLoaded = false;

    private DetailAdapter mDetailAdapter;
    private RecyclerView mMovieDetailView;

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

    private static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry.COLUMN_VIDEO_ID,
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_KEY,
            MovieContract.VideoEntry.COLUMN_SITE
    };

    static final int COL_VIDEO_ID = 0;
    static final int COL_VIDEO_NAME = 1;
    static final int COL_VIDEO_KEY = 2;
    static final int COL_VIDEO_SITE = 3;

    private static final String[] REVIEW_COLUMNS = {
            MovieContract.ReviewEntry.COLUMN_REVIEW_ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_URL
    };

    static final int COL_REVIEW_ID = 0;
    static final int COL_REVIEW_AUTHOR = 1;
    static final int COL_REVIEW_CONTENT = 2;
    static final int COL_REVIEW_URL = 3;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        if(arguments != null) {
            mMovieUri = arguments.getParcelable(DetailActivityFragment.MOVIE_URI);
        }

        if(savedInstanceState == null) {
            mVideoLoaded = false;
            mReviewLoaded = false;
        } else {
            mVideoLoaded = savedInstanceState.getBoolean(VIDEO_LOADED);
            mReviewLoaded = savedInstanceState.getBoolean(REVIEW_LOADED);
        }

        if(!mVideoLoaded && mMovieUri != null) {
            new FetchMovieVideoTask(getContext(), new FetchVideosTaskCompleteListener(this))
                    .execute(String.valueOf(ContentUris.parseId(mMovieUri)));
        }

        if(!mReviewLoaded && mMovieUri != null) {
            new FetchMovieReviewTask(getContext(), new FetchReviewsTaskCompleteListener(this))
                    .execute(String.valueOf(ContentUris.parseId(mMovieUri)));
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MOVIE_LOADER_ITEM, null, this);
        getLoaderManager().initLoader(MOVIE_LOADER_VIDEO, null, this);
        getLoaderManager().initLoader(MOVIE_LOADER_REVIEW, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(VIDEO_LOADED, mVideoLoaded);
        outState.putBoolean(REVIEW_LOADED, mReviewLoaded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mMovieDetailView = (RecyclerView) rootView.findViewById(R.id.recyclerview_detail);
        mMovieDetailView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mDetailAdapter = new DetailAdapter(getContext(), new DetailAdapter.MovieVideoAdapterOnClickHandler() {
            @Override
            public void onClick(String key, DetailAdapter.ViewHolderVideo vh) {
                final String youtubePlayUrl = "http://www.youtube.com/watch?v=";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubePlayUrl + key)));
            }
        });

        mMovieDetailView.setAdapter(mDetailAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if ( mMovieUri == null ) { return null; }
        long movieId = ContentUris.parseId(mMovieUri);
        String[] mId = {String.valueOf(movieId)};

        switch (id) {
            case MOVIE_LOADER_ITEM:
                return new CursorLoader(getActivity(),
                        mMovieUri,
                        MOVIE_COLUMNS,
                        null,
                        mId,
                        null);

            case MOVIE_LOADER_VIDEO:
                return new CursorLoader(getActivity(),
                        mMovieUri.buildUpon().appendPath("videos").build(),
                        VIDEO_COLUMNS,
                        null,
                        mId,
                        null);

            case MOVIE_LOADER_REVIEW:
                return new CursorLoader(getActivity(),
                        mMovieUri.buildUpon().appendPath("reviews").build(),
                        REVIEW_COLUMNS,
                        null,
                        mId,
                        null);
            }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch(loader.getId()) {
            case MOVIE_LOADER_ITEM:
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

//                    getActivity().setTitle(mMovie.getTitle()); // set title for the detail view

                    mDetailAdapter.addMovie(mMovie);
                }
                break;
            case MOVIE_LOADER_VIDEO:
                mMovieVideoList = new ArrayList<>();
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    String videoId = data.getString(COL_VIDEO_ID);
                    String videoKey = data.getString(COL_VIDEO_KEY);
                    String videoName = data.getString(COL_VIDEO_NAME);
                    String videoSite = data.getString(COL_VIDEO_SITE);

                    MovieVideo mVideo = new MovieVideo(videoId, videoKey, videoName, videoSite);
                    mMovieVideoList.add(mVideo);
                }

                mDetailAdapter.addVideos(mMovieVideoList);
                break;
            case MOVIE_LOADER_REVIEW:
                mMovieReviewList = new ArrayList<>();
                data.moveToPosition(-1);
                while (data.moveToNext()) {
                    String reviewId = data.getString(COL_REVIEW_ID);
                    String reviewAuthor = data.getString(COL_REVIEW_AUTHOR);
                    String reviewContent = data.getString(COL_REVIEW_CONTENT);
                    String reviewUrl = data.getString(COL_REVIEW_URL);

                    MovieReview mReview = new MovieReview(reviewId, reviewAuthor, reviewContent, reviewUrl);
                    mMovieReviewList.add(mReview);

                }

                mDetailAdapter.addReviews(mMovieReviewList);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailAdapter.resetAll();
    }

    public class FetchVideosTaskCompleteListener
            implements AsyncTaskCompleteListener<ArrayList<MovieVideo>>
    {
        private DetailActivityFragment fragment;

        public FetchVideosTaskCompleteListener(DetailActivityFragment fragment) {
            super();
            this.fragment = fragment;
        }

        @Override
        public void onTaskComplete(ArrayList<MovieVideo> data) {
            mVideoLoaded = true;
            getLoaderManager().initLoader(MOVIE_LOADER_VIDEO, null, fragment);
        }

    }

    public class FetchReviewsTaskCompleteListener
            implements AsyncTaskCompleteListener<ArrayList<MovieReview>>
    {
        private DetailActivityFragment fragment;

        public FetchReviewsTaskCompleteListener(DetailActivityFragment fragment) {
            super();
            this.fragment = fragment;
        }

        @Override
        public void onTaskComplete(ArrayList<MovieReview> data)
        {
            mReviewLoaded = true;
            getLoaderManager().initLoader(MOVIE_LOADER_REVIEW, null, fragment);
        }
    }
}
