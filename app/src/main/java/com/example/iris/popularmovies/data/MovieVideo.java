package com.example.iris.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Iris on 23/08/2016.
 */
public class MovieVideo implements Parcelable{
    private String ID, key, name, site;

    public MovieVideo(String ID, String key, String name, String site) {
        this.ID = ID;
        this.key = key;
        this.name = name;
        this.site = site;
    }

    private MovieVideo(Parcel in) {
        this.ID = in.readString();
        this.key = in.readString();
        this.name = in.readString();
        this.site = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ID);
        parcel.writeString(key);
        parcel.writeString(name);
        parcel.writeString(site);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<MovieVideo> CREATOR
            = new Parcelable.Creator<MovieVideo>() {
        public MovieVideo createFromParcel(Parcel in) {
            return new MovieVideo(in);
        }

        public MovieVideo[] newArray(int size) {
            return new MovieVideo[size];
        }
    };

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String toString() {
        return "MOVIE: " + name;
    }
}
