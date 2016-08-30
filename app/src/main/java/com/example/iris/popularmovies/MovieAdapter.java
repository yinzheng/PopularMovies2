package com.example.iris.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.iris.popularmovies.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Iris on 12/08/2016.
 */
public class MovieAdapter extends CursorAdapter {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // inflate the layout
        View view = LayoutInflater.from(context)
                .inflate(R.layout.grid_item_movie, parent, false);
        // well set up the ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        // store the holder with the view.
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read data from cursor
        String poster = cursor.getString(MoviesFragment.COL_MOVIE_POSTER_PATH);
        Picasso.with(context).load(poster).into(viewHolder.posterView);

    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        @BindView(R.id.grid_item_movie_poster) ImageView posterView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
