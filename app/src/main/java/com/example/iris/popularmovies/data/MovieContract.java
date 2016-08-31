package com.example.iris.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Iris on 22/08/2016.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.example.iris.popularmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";

        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTED_AVERAGE = "voted_average";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class MovieListEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_list";
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_LIST_TYPE = "list_type";

        public static final String MOVIE_TYPE_POPULAR = "popular";
        public static final String MOVIE_TYPE_TOP_RATED = "top_rated";
        public static final String MOVIE_TYPE_FAVOURITE = "favourite";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static Uri buildMovieListUri(String type) {
            switch (type) {
                case MOVIE_TYPE_POPULAR:
                case MOVIE_TYPE_TOP_RATED:
                case MOVIE_TYPE_FAVOURITE:
                    return CONTENT_URI.buildUpon().appendPath(type).build();
                default:
                    return CONTENT_URI;
            }
        }

    }
}
