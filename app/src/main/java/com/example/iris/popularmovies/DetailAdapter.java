package com.example.iris.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iris.popularmovies.data.Movie;
import com.example.iris.popularmovies.data.MovieContract;
import com.example.iris.popularmovies.data.MovieReview;
import com.example.iris.popularmovies.data.MovieVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Iris on 21/09/2016.
 */

public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private final Context mContext;
    private final MovieVideoAdapterOnClickHandler mVideoClickHandler;
    private Movie mMovie;
    private List<MovieVideo> mMovieVideoList;
    private MovieVideo mMovieVideo;
    private List<MovieReview> mMovieReviewList;
    private MovieReview mMovieReview;
    private static final int VIEW_TYPE_INFO = 0;
    private static final int VIEW_TYPE_VIDEO = 1;
    private static final int VIEW_TYPE_REVIEW = 2;

    public DetailAdapter(Context context, MovieVideoAdapterOnClickHandler dh) {
        this.mContext = context;
        this.mVideoClickHandler = dh;
        mMovie = null;
        this.mMovieVideoList = new ArrayList<>();
        this.mMovieReviewList = new ArrayList<>();
    }

    public void addMovie(Movie movie) {
        mMovie = movie;
        notifyDataSetChanged();
    }

    public void addVideos(List<MovieVideo> videos) {
        mMovieVideoList = videos;
        notifyDataSetChanged();
    }

    public void addReviews(List<MovieReview> reviews) {
        mMovieReviewList = reviews;
        notifyDataSetChanged();
    }

    public void resetAll() {
        mMovie = null;
        mMovieVideoList = null;
        mMovieReviewList = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_INFO:

                View v1 = inflater.inflate(R.layout.view_type_info, parent, false);
                viewHolder = new ViewHolderInfo(v1);
                break;

            case VIEW_TYPE_VIDEO:

                View v2 = inflater.inflate(R.layout.view_type_video, parent, false);
                viewHolder = new ViewHolderVideo(v2);
                break;

            case VIEW_TYPE_REVIEW:

                View v3 = inflater.inflate(R.layout.view_type_review, parent, false);
                viewHolder = new ViewHolderReview(v3);
                break;

            default:
                throw new UnsupportedOperationException("Unknown View Type.");
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (Integer.valueOf(0).equals(position)) {
            return VIEW_TYPE_INFO;
        } else if (Integer.valueOf(position) <= mMovieVideoList.size()) {
            mMovieVideo = (MovieVideo) mMovieVideoList.get(position - 1);
            return VIEW_TYPE_VIDEO;
        }  else if (Integer.valueOf(position) > mMovieVideoList.size()){
            mMovieReview = (MovieReview) mMovieReviewList.get(position - 1 - mMovieVideoList.size());
            return VIEW_TYPE_REVIEW;
        } else {
            throw new UnsupportedOperationException("Unknown View Type.");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_INFO:
                ViewHolderInfo vh1 = (ViewHolderInfo) viewHolder;
                configureViewHolderInfo(vh1, position);
                break;
            case VIEW_TYPE_VIDEO:
                ViewHolderVideo vh2 = (ViewHolderVideo) viewHolder;
                configureViewHolderVideo(vh2, position);
                break;
            case VIEW_TYPE_REVIEW:
                ViewHolderReview vh3 = (ViewHolderReview) viewHolder;
                configureViewHolderReview(vh3, position);
                break;
            default:

        }

    }

    @Override
    public int getItemCount() {
        return 1 + this.mMovieVideoList.size() + this.mMovieReviewList.size();
    }

    private String getFavouriteText(int value) {
        return (Integer.valueOf(value).equals(0))? "FAVOURITE" : "UNFAVOURITE";
    }


    private void configureViewHolderInfo(ViewHolderInfo vh, int position) {
        if(mMovie == null) { return; }
        Picasso.with(mContext).load(mMovie.getPosterPath()).into(vh.getDetailPoster());
        vh.getDetailTitle().setText(mMovie.getOriginalTitle());
        vh.getDetailOverview().setText(mMovie.getOverview());
        vh.getDetailReleaseDate().setText(Utility.formatDate(mMovie.getReleaseDate()));
        vh.getDetailVotedAverage().setText(String.valueOf(mMovie.getVoteAverage()));
        vh.getDetailFavourite().setText(getFavouriteText(mMovie.getFavourite()));

        vh.getDetailFavourite().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Uri contentUri = MovieContract.MovieEntry.buildMovieFavouriteUri(mMovie.getID());

                if(Integer.valueOf(mMovie.getFavourite()).equals(0)) {
                    mMovie.setFavourite(1);

                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieListEntry.COLUMN_MOVIE_KEY, mMovie.getID());
                    values.put(MovieContract.MovieListEntry.COLUMN_LIST_TYPE,
                            MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE);

                    mContext.getContentResolver().insert(contentUri, values);
                    
                } else {
                    mMovie.setFavourite(0);

                    String[] movie = {String.valueOf(mMovie.getID()),
                            MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE};

                    mContext.getContentResolver().delete(contentUri,
                            MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=? AND " +
                                    MovieContract.MovieListEntry.COLUMN_LIST_TYPE + "=?",
                            movie
                    );
                }

                Button clickedButton = (Button)v;
                clickedButton.setText(getFavouriteText(mMovie.getFavourite()));
            }
        });

    }

    private void configureViewHolderVideo(ViewHolderVideo vh, int position) {
        vh.getVideoTitle().setText(String.valueOf(mMovieVideo.getName()));
    }

    private void configureViewHolderReview(ViewHolderReview vh, int position) {
        vh.getReviewAuthor().setText(String.format(mContext.getString(R.string.format_review_title),
                mMovieReview.getAuthor()));
        vh.getReviewContent().setText(String.valueOf(mMovieReview.getContent()));
    }

    public class ViewHolderInfo extends RecyclerView.ViewHolder {

        @BindView(R.id.detail_movie_poster) ImageView detailPoster;
        @BindView(R.id.detail_movie_title) TextView detailTitle;
        @BindView(R.id.detail_movie_overview) TextView detailOverview;
        @BindView(R.id.detail_movie_release_date) TextView detailReleaseDate;
        @BindView(R.id.detail_movie_voted_average) TextView detailVotedAverage;
        @BindView(R.id.detail_movie_favourite) Button detailFavourite;

        public ViewHolderInfo (View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public ImageView getDetailPoster() {
            return detailPoster;
        }

        public void setDetailPoster(ImageView detailPoster) {
            this.detailPoster = detailPoster;
        }

        public TextView getDetailTitle() {
            return detailTitle;
        }

        public void setDetailTitle(TextView detailTitle) {
            this.detailTitle = detailTitle;
        }

        public TextView getDetailOverview() {
            return detailOverview;
        }

        public void setDetailOverview(TextView detailOverview) {
            this.detailOverview = detailOverview;
        }

        public TextView getDetailReleaseDate() {
            return detailReleaseDate;
        }

        public void setDetailReleaseDate(TextView detailReleaseDate) {
            this.detailReleaseDate = detailReleaseDate;
        }

        public TextView getDetailVotedAverage() {
            return detailVotedAverage;
        }

        public void setDetailVotedAverage(TextView detailVotedAverage) {
            this.detailVotedAverage = detailVotedAverage;
        }

        public Button getDetailFavourite() {
            return detailFavourite;
        }

        public void setDetailFavourite(Button detailFavourite) {
            this.detailFavourite = detailFavourite;
        }
    }

    public class ViewHolderVideo extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.video_title) TextView videoTitle;
        public ViewHolderVideo (View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            MovieVideo video = mMovieVideoList.get(adapterPosition - 1);
            mVideoClickHandler.onClick(video.getKey(), this);
        }

        public TextView getVideoTitle() {
            return videoTitle;
        }

        public void setVideoTitle(TextView videoTitle) {
            this.videoTitle = videoTitle;
        }
    }

    public interface MovieVideoAdapterOnClickHandler {
        void onClick(String key, ViewHolderVideo vh);
    }

    public class ViewHolderReview extends RecyclerView.ViewHolder {
        @BindView(R.id.review_author) TextView reviewAuthor;
        @BindView(R.id.review_content) TextView reviewContent;
        public ViewHolderReview (View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public TextView getReviewAuthor() {
            return reviewAuthor;
        }

        public void setReviewAuthor(TextView reviewAuthor) {
            this.reviewAuthor = reviewAuthor;
        }

        public TextView getReviewContent() {
            return reviewContent;
        }

        public void setReviewContent(TextView reviewContent) {
            this.reviewContent = reviewContent;
        }
    }
}
