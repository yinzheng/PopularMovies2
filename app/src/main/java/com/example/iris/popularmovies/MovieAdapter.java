package com.example.iris.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.iris.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Iris on 12/08/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private Cursor mCursor;
    final private Context mContext;
    final private MovieAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    public MovieAdapter(Context context, MovieAdapterOnClickHandler dh, View emptyView) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            // inflate the layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item_movie, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerViewSelection");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        mCursor.moveToPosition(position);

        // Read data from cursor
        String poster = mCursor.getString(MoviesFragment.COL_MOVIE_POSTER_PATH);
        Picasso.with(mContext).load(poster).into(viewHolder.posterView);

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE: View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }


    /**
     * Cache of the children views for a forecast list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.grid_item_movie_poster) ImageView posterView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int movieID = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            mClickHandler.onClick(mCursor.getInt(movieID), this);
        }
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(int id, ViewHolder vh);
    }
}
