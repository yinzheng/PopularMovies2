package com.example.iris.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iris.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Iris on 21/09/2016.
 */

public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private final Context mContext;
    private List<Object> items;
    private Movie mMovie;
    private static final int VIEW_TYPE_INFO = 0;
    private static final int VIEW_TYPE_VIDEO = 1;
    private static final int VIEW_TYPE_REVIEW = 2;

    public DetailAdapter(Context context) {
        this.mContext = context;
        this.items = new ArrayList<Object>();
    }

    public void add(Object item) {
        items.add(item);
        notifyItemChanged(items.size()-1);
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

                View v2 = inflater.inflate(R.layout.view_type_info, parent, false);
                viewHolder = new ViewHolderInfo(v2);
                break;

            case VIEW_TYPE_REVIEW:

                View v3 = inflater.inflate(R.layout.view_type_info, parent, false);
                viewHolder = new ViewHolderInfo(v3);
                break;

            default:

                View v4 = inflater.inflate(R.layout.view_type_info, parent, false);
                viewHolder = new ViewHolderInfo(v4);
                break;
        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Movie) {
            mMovie = (Movie) items.get(position);
            return VIEW_TYPE_INFO;
        } else {
            return VIEW_TYPE_INFO;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_INFO:
                ViewHolderInfo vh1 = (ViewHolderInfo) viewHolder;
                configureViewHolderInfo(vh1, position);
                break;
            default:

        }

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }


    private void configureViewHolderInfo(ViewHolderInfo vh1, int position) {
        Picasso.with(mContext).load(mMovie.getPosterPath()).into(vh1.getDetailPoster());
        vh1.getDetailTitle().setText(mMovie.getOriginalTitle());
        vh1.getDetailOverview().setText(mMovie.getOverview());
        vh1.getDetailReleaseDate().setText(Utility.formatDate(mMovie.getReleaseDate()));
        vh1.getDetailVotedAverage().setText(String.valueOf(mMovie.getVoteAverage()));
        vh1.getDetailFavourite().setText(String.valueOf(mMovie.getFavourite()));

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

    public class ViewHolderVideo extends RecyclerView.ViewHolder {
        public ViewHolderVideo (View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
