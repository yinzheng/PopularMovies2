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
    public static final String PATH_VIDEO = "video";
    public static final String PATH_REVIEW = "review";


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

        public static Uri buildMovieFavouriteUri(long id) {
            return buildMovieUri(id).buildUpon().appendPath("favourite").build();
        }

    }

    public static final class MovieListEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_list";
        public static final String SUBTABLE_NAME = "L";
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

    public static final class VideoEntry implements BaseColumns {
        public static final String TABLE_NAME = "video";
        public static final String COLUMN_VIDEO_ID = "video_id";
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIDEO;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static Uri buildMovieVideoListUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId)
                    .appendPath("videos").build();
        }

        public static Uri buildMovieVideoUri(String movieId, String id) {
            return CONTENT_URI.buildUpon().appendPath(movieId)
                    .appendPath("videos").appendPath(id).build();
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static Uri buildMovieReviewListUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId)
                    .appendPath("reviews").build();
        }

        public static Uri buildMovieReviewUri(String movieId, String id) {
            return CONTENT_URI.buildUpon().appendPath(movieId)
                    .appendPath("reviews").appendPath(id).build();
        }
    }
}
