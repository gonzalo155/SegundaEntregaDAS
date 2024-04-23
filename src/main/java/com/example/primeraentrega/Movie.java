package com.example.primeraentrega;

import android.graphics.Bitmap;
import android.net.Uri;

public class Movie {
    private String title;
    //private int poster;
    private float rating;
    private boolean viewed;

    private Uri imageUri;

    private Bitmap imageBitmap;

    public Movie(String title, float rating, boolean viewed, Uri imageUri, Bitmap imageBitmap) {
        this.title = title;
        //this.poster = poster;
        this.rating = rating;
        this.viewed = viewed;
        this.imageUri = imageUri;
        this.imageBitmap=imageBitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
}

