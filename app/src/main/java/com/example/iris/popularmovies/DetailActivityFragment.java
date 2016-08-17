package com.example.iris.popularmovies;

import android.content.Intent;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iris.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private String LOG_TAG = DetailActivity.class.getSimpleName();
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if(intent != null || intent.hasExtra("DATA")) {
            Bundle movieBundle = intent.getParcelableExtra("DATA");
            Movie movie = movieBundle.getParcelable("MOVIE");

            ImageView detailPoster = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
            TextView detailTitle = (TextView) rootView.findViewById(R.id.detail_movie_title);
            TextView detailOverview = (TextView) rootView.findViewById(R.id.detail_movie_overview);
            TextView detailReleaseDate = (TextView) rootView.findViewById(R.id.detail_movie_release_date);
            TextView detailVotedAverage = (TextView) rootView.findViewById(R.id.detail_movie_voted_average);

            getActivity().setTitle(movie.getTitle()); // set title for the detail view

            Picasso.with(getContext()).load(movie.getPosterPath()).into(detailPoster);
            detailTitle.setText(movie.getOriginalTitle());
            detailOverview.setText(movie.getOverview());

            detailReleaseDate.setText(Utility.formatDate(movie.getReleaseDate()));
            detailVotedAverage.setText(String.valueOf(movie.getVoteAverage()));

        }

        return rootView;
    }
}
