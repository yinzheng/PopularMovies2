package com.example.iris.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Iris on 29/08/2016.
 */
public class MovieProvider extends ContentProvider {
    private final String LOG_TAG = MovieProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieHelper;

    static final int MOVIE_POPULAR = 100;
    static final int MOVIE_TOP_RATED = 101;
    static final int MOVIE_FAVOURITE = 102;
    static final int MOVIE = 300;


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE
                + "/" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR, MOVIE_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_MOVIE
                + "/" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED, MOVIE_TOP_RATED);
        matcher.addURI(authority, MovieContract.PATH_MOVIE
                + "/" + MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE, MOVIE_FAVOURITE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/*", MOVIE);

        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_POPULAR: {
                Log.d(LOG_TAG, uri.toString());

                retCursor = mMovieHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME + ", "
                                + MovieContract.MovieListEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.TABLE_NAME
                                + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                                + " = " + MovieContract.MovieListEntry.TABLE_NAME
                                + "." + MovieContract.MovieListEntry.COLUMN_MOVIE_KEY
                                + " AND " + MovieContract.MovieListEntry.COLUMN_LIST_TYPE
                                + " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case MOVIE_TOP_RATED: {
                Log.d(LOG_TAG, uri.toString());

                retCursor = mMovieHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME + ", "
                                + MovieContract.MovieListEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.TABLE_NAME
                                + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID
                                + " = " + MovieContract.MovieListEntry.TABLE_NAME
                                + "." + MovieContract.MovieListEntry.COLUMN_MOVIE_KEY
                                + " AND " + MovieContract.MovieListEntry.COLUMN_LIST_TYPE
                                + " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_POPULAR:
            case MOVIE_TOP_RATED:
            case MOVIE_FAVOURITE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case MOVIE_POPULAR: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,
                        null, contentValues);

                if ( _id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;

            }

            case MOVIE_TOP_RATED: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,
                        null, contentValues);

                if ( _id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;

            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int movieCount = 0;
        int returnCount = 0;

        switch (match) {
            case MOVIE_POPULAR:
                movieCount = bulkInsertMovie(values);

                db.beginTransaction();
                try {

                    for (ContentValues value: values) {

                        ContentValues movieValues = new ContentValues();
                        int movieId = value.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                        movieValues.put(MovieContract.MovieListEntry.COLUMN_MOVIE_KEY, movieId);
                        movieValues.put(MovieContract.MovieListEntry.COLUMN_LIST_TYPE,
                                MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR);

                        long _id = db.insert(MovieContract.MovieListEntry.TABLE_NAME,
                                null, movieValues);
                        if ( _id != -1 ) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MOVIE_TOP_RATED:
                movieCount = bulkInsertMovie(values);

                db.beginTransaction();
                try {

                    for (ContentValues value: values) {

                        ContentValues movieValues = new ContentValues();
                        int movieId = value.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                        movieValues.put(MovieContract.MovieListEntry.COLUMN_MOVIE_KEY, movieId);
                        movieValues.put(MovieContract.MovieListEntry.COLUMN_LIST_TYPE,
                                MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED);

                        long _id = db.insert(MovieContract.MovieListEntry.TABLE_NAME,
                                null, movieValues);
                        if ( _id != -1 ) {
                            returnCount++;
                        }
                    }

                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    public int bulkInsertMovie(ContentValues[] values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        db.beginTransaction();
        int returnCount = 0;

        try {
            for (ContentValues value: values) {
                // insert movie detail
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,
                        null, value);
                if ( _id != -1 ) {
                    returnCount++;
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return returnCount;
    }

    @Override
    public void shutdown() {
        mMovieHelper.close();
        super.shutdown();
    }
}
