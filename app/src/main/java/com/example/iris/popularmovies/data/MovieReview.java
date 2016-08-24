package com.example.iris.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Iris on 23/08/2016.
 */
public class MovieReview implements Parcelable {
    private String ID, author, content, url;

    public MovieReview(String ID, String author, String content, String url) {
        this.ID = ID;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    private MovieReview(Parcel in) {
        this.ID = in.readString();
        this.author = in.readString();
        this.content = in.readString();
        this.url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ID);
        parcel.writeString(author);
        parcel.writeString(content);
        parcel.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<MovieReview> CREATOR
            = new Parcelable.Creator<MovieReview>() {

        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
