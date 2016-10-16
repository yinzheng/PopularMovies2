package com.example.iris.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
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

    static final int MOVIE_LIST_ALL = 100;
    static final int MOVIE_LIST_POPULAR = 101;
    static final int MOVIE_LIST_TOP_RATED = 102;
    static final int MOVIE_LIST_FAVOURITE = 103;
    static final int MOVIE = 200;
    static final int MOVIE_FAVOURITE = 201;
    static final int MOVIE_VIDEOS = 202;
    static final int MOVIE_REVIEWS = 203;

    private static final SQLiteQueryBuilder sMovieListQueryBuilder;
    private static final SQLiteQueryBuilder sMovieQueryBuilder;

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

    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();

        sMovieQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME +
                        " LEFT JOIN ( SELECT * FROM " +
                        MovieContract.MovieListEntry.TABLE_NAME +
                        " WHERE " + MovieContract.MovieListEntry.COLUMN_LIST_TYPE +
                        " = '" + MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE + "' ) as " +
                        MovieContract.MovieListEntry.SUBTABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                        " = " + MovieContract.MovieListEntry.SUBTABLE_NAME +
                        "." + MovieContract.MovieListEntry.COLUMN_MOVIE_KEY
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE_LIST_ALL);
        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_POPULAR, MOVIE_LIST_POPULAR);
        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_TOP_RATED, MOVIE_LIST_TOP_RATED);
        matcher.addURI(authority, MovieContract.PATH_MOVIE +
                "/" + MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE, MOVIE_LIST_FAVOURITE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/#", MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/#/favourite", MOVIE_FAVOURITE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/#/videos", MOVIE_VIDEOS);
        matcher.addURI(authority, MovieContract.PATH_MOVIE+"/#/reviews", MOVIE_REVIEWS);

        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieHelper = new MovieDbHelper(getContext());
        return true;
    }

    private Cursor getMovieListByType(String[] projection, String selection,
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

    private Cursor getMovieById(String[] projection, String selection,
                                String[] selectionArgs, String sortOrder) {
        return sMovieQueryBuilder.query(mMovieHelper.getReadableDatabase(),
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
            case MOVIE: {
                long id = ContentUris.parseId(uri);
                retCursor = getMovieById(
                        projection,
                        MovieContract.MovieEntry.TABLE_NAME +
                                "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                                " = '" + id + "'",
                        selectionArgs,
                        sortOrder
                );
                break;
            }
            case MOVIE_LIST_POPULAR:
            case MOVIE_LIST_TOP_RATED:
            case MOVIE_LIST_FAVOURITE: {
                retCursor = getMovieListByType(
                        projection,
                        MovieContract.MovieListEntry.COLUMN_LIST_TYPE + " =? ",
                        selectionArgs,
                        sortOrder);
                break;
            }
            case MOVIE_VIDEOS: {
                String[] segments = uri.getPath().split("/");
                String idStr = segments[segments.length-2];
                int id = Integer.parseInt(idStr);

                retCursor = mMovieHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        MovieContract.VideoEntry.COLUMN_MOVIE_KEY +
                                " = '" + id + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
            }
            case MOVIE_REVIEWS: {
                String[] segments = uri.getPath().split("/");
                String idStr = segments[segments.length-2];
                int id = Integer.parseInt(idStr);

                retCursor = mMovieHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_KEY +
                                " = '" + id + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

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
            case MOVIE_LIST_POPULAR:
            case MOVIE_LIST_TOP_RATED:
            case MOVIE_LIST_FAVOURITE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
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

        Uri returnUri = uri;

        switch (match) {
            case MOVIE_LIST_ALL: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME,
                        null, contentValues);
                if ( _id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            case MOVIE_VIDEOS: {
                int movieId = contentValues.getAsInteger(MovieContract.VideoEntry.COLUMN_MOVIE_KEY);
                long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME,
                        null, contentValues);

                if ( _id > 0)
                    returnUri = MovieContract.VideoEntry
                            .buildMovieVideoUri(String.valueOf(movieId), String.valueOf(_id));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;

            }

            case MOVIE_REVIEWS: {
                int movieId = contentValues.getAsInteger(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY);
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME,
                        null, contentValues);

                if ( _id > 0)
                    returnUri = MovieContract.ReviewEntry
                            .buildMovieReviewUri(String.valueOf(movieId), String.valueOf(_id));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

                break;

            }
            case MOVIE_FAVOURITE: {
                db.insert(MovieContract.MovieListEntry.TABLE_NAME,
                        null, contentValues);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(returnUri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_LIST_POPULAR:
            case MOVIE_LIST_TOP_RATED:
                getContext().getContentResolver().notifyChange(uri, null);
                return db.delete(MovieContract.MovieListEntry.TABLE_NAME,
                        where,
                        whereArgs
                );
            case MOVIE_FAVOURITE: {
                getContext().getContentResolver().notifyChange(uri, null);
                getContext().getContentResolver().notifyChange(
                        MovieContract.MovieListEntry.buildMovieListUri(
                                MovieContract.MovieListEntry.MOVIE_TYPE_FAVOURITE), null);
                return db.delete(MovieContract.MovieListEntry.TABLE_NAME, where, whereArgs);
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }



    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mMovieHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int returnCount = 0;

        switch (match) {
            case MOVIE_LIST_ALL:
                db.beginTransaction();
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
                break;

            case MOVIE_LIST_POPULAR:
            case MOVIE_LIST_TOP_RATED:
                db.beginTransaction();
                try {
                    for (ContentValues value: values) {
                        long _id = db.insert(MovieContract.MovieListEntry.TABLE_NAME,
                                null, value);
                        if ( _id != -1 ) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;

            case MOVIE_VIDEOS:
                db.beginTransaction();
                try {
                    for (ContentValues value: values) {
                        long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME,
                                null, value);
                        if ( _id != -1 ) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;

            case MOVIE_REVIEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value: values) {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME,
                                null, value);
                        if ( _id != -1 ) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;

            default:
                return super.bulkInsert(uri, values);
        }

        if(returnCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnCount;
    }

    @Override
    public void shutdown() {
        mMovieHelper.close();
        super.shutdown();
    }
}
