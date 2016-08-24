package com.example.iris.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Iris on 10/08/2016.
 */
public class Movie implements Parcelable {
    private int ID;
    private String posterPath, overview, title, originalTitle, releaseDate;
    private double voteAverage;
    private ArrayList<String> genre;
    private ArrayList<MovieVideo> videos;
    private ArrayList<MovieReview> reviews;

    public Movie(int ID, String title, String originalTitle, String releaseDate, String overview, double voteAverage, String posterPath) {
        this();
        this.ID = ID;
        this.title = title;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.voteAverage = voteAverage;
    }

    /**
     * Initialise the arraylist or may cause crash
     */
    private Movie() {
        this.videos = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    private Movie(Parcel in) {
        this();
        this.ID = in.readInt();
        this.title = in.readString();
        this.originalTitle = in.readString();
        this.posterPath = in.readString();
        this.releaseDate = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readDouble();
        in.readTypedList(this.videos, MovieVideo.CREATOR);
        in.readTypedList(this.reviews, MovieReview.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
        parcel.writeString(title);
        parcel.writeString(originalTitle);
        parcel.writeString(posterPath);
        parcel.writeString(releaseDate);
        parcel.writeString(overview);
        parcel.writeDouble(voteAverage);
        parcel.writeTypedList(videos);
        parcel.writeTypedList(reviews);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public ArrayList<MovieVideo> getVideos() { return videos; }

    public void setVideos(ArrayList<MovieVideo> videos) { this.videos = videos; }

    public ArrayList<MovieReview> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<MovieReview> reviews) {
        this.reviews = reviews;
    }

    public String toString() {
        return "MOVIE: " + originalTitle;
    }


}
