package com.example.iris.popularmovies;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.iris.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Iris on 12/08/2016.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter (Activity context, List<Movie> movies) {
        super(context, 0, movies);
        Log.v(LOG_TAG, movies.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the layout
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolder(convertView);
            // store the holder with the view.
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Movie movie = getItem(position);
        Picasso.with(getContext()).load(movie.getPosterPath()).into(viewHolder.posterView);

        Log.v(LOG_TAG, String.valueOf(position));
        Log.v(LOG_TAG, movie.toString());

        return convertView;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_item_movie_poster);
        }
    }
}
