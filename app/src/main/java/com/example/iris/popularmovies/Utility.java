package com.example.iris.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Date;

/**
 * Created by Iris on 17/08/2016.
 */
public class Utility {
    public static String getPreferedSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_key_sort_order),
                context.getString(R.string.pref_sort_order_default));
    }

    public static String formatDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateStr = formatter.parse(dateString);
            formatter = new SimpleDateFormat("dd MMM yyyy");
            return formatter.format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateString;
    }
}
