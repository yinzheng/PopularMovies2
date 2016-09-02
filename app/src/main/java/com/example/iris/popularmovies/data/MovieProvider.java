package com.example.iris.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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

    private static final SQLiteQueryBuilder sMovieListQueryBuilder;

    static {
        sMovieListQueryBuilder = new SQLiteQueryBuilder();

        sMovieListQueryBuilder.setTables(
          MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                  MovieContract.MovieListEntry.TABLE_NAME +
                  " ON " + MovieContract.MovieEntry.TABLE_NAME +
                  "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                  " = " + MovieContract.MovieListEntry.TABLE_NAME +
                  "." + MovieContract.MovieListEntry.COLUMN_MOVIE_KEY
        );
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR, MOVIE_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED, MOVIE_TOP_RATED);
        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE, MOVIE_FAVOURITE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/#", MOVIE);

        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieHelper = new MovieDbHelper(getContext());
        return true;
    }

    private Cursor getMovieListByType(Uri uri, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {
        return sMovieListQueryBuilder.query(mMovieHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_POPULAR: {
                retCursor = getMovieListByType(uri,
                        projection,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                                " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR + "'",
                        selectionArgs,
                        sortOrder);
                break;
            }
            case MOVIE_TOP_RATED: {
                retCursor = getMovieListByType(uri,
                        projection,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                                " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED + "'",
                        selectionArgs,
                        sortOrder);
                break;
            }
            case MOVIE_FAVOURITE: {
                retCursor = getMovieListByType(uri,
                        projection,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                                " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE + "'",
                        selectionArgs,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
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

    private int bulkInsertMovie(ContentValues[] values) {
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

    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount = 0;

        switch (match) {
            case MOVIE_POPULAR:
                bulkDelete(uri); // delete previous list
                bulkInsertMovie(values); // save movie information

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

                if(returnCount != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return returnCount;
            case MOVIE_TOP_RATED:
                bulkDelete(uri); // delete previous list
                bulkInsertMovie(values); // save movie information

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

                if(returnCount != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    public int bulkDelete(Uri uri) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_POPULAR:
                return db.delete(MovieContract.MovieListEntry.TABLE_NAME,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                                " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR + "'",
                        null
                );
            case MOVIE_TOP_RATED:
                return db.delete(MovieContract.MovieListEntry.TABLE_NAME,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                                " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED + "'",
                        null
                );
            default:
                return 0;
        }
    }


    @Override
    public void shutdown() {
        mMovieHelper.close();
        super.shutdown();
    }
}
