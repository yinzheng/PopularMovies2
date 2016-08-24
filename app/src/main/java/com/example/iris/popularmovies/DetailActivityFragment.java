package com.example.iris.popularmovies;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iris.popularmovies.data.Movie;
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
public class DetailActivityFragment extends Fragment {
    private String LOG_TAG = DetailActivity.class.getSimpleName();
    private final String MOVIE_ITEM = "MOVIE";
    private final String VIDEO_LOADED = "VIDEO_LOADED";
    private final String REVIEW_LOADED = "REVIEW_LOADED";
    private Movie mMovie;
    private boolean mVideoLoaded = false;
    private boolean mReviewLoaded = false;

    @BindView(R.id.detail_movie_poster) ImageView detailPoster;
    @BindView(R.id.detail_movie_title) TextView detailTitle;
    @BindView(R.id.detail_movie_overview) TextView detailOverview;
    @BindView(R.id.detail_movie_release_date) TextView detailReleaseDate;
    @BindView(R.id.detail_movie_voted_average) TextView detailVotedAverage;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_ITEM)) {
            Intent intent = getActivity().getIntent();
            if(intent != null || intent.hasExtra("DATA")) {
                Bundle movieBundle = intent.getParcelableExtra("DATA");
                mMovie = movieBundle.getParcelable("MOVIE");
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

        getActivity().setTitle(mMovie.getTitle()); // set title for the detail view

        Picasso.with(getContext()).load(mMovie.getPosterPath()).into(detailPoster);
        detailTitle.setText(mMovie.getOriginalTitle());
        detailOverview.setText(mMovie.getOverview());
        detailReleaseDate.setText(Utility.formatDate(mMovie.getReleaseDate()));
        detailVotedAverage.setText(String.valueOf(mMovie.getVoteAverage()));

        if(!mVideoLoaded) {
            new FetchMovieVideoTask(getContext(), new FetchVideosTaskCompleteListener())
                    .execute(String.valueOf(mMovie.getID()));
        }

        if(!mReviewLoaded) {
            new FetchMovieReviewTask(getContext(), new FetchReviewsTaskCompleteListener())
                    .execute(String.valueOf(mMovie.getID()));
        }

        return rootView;
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
