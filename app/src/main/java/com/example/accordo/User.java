package com.example.accordo;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity (tableName = "ProfilePictures")
public class User {
    @PrimaryKey
    @NonNull
    private String uid;

    @ColumnInfo(name = "pversion")
    private String pversion;

    @ColumnInfo(name = "picture")
    private String picture;

    private String name;

    @Ignore
    private Bitmap bitmapPicture;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPversion() {
        return pversion;
    }

    public void setPversion(String pversion) {
        this.pversion = pversion;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Bitmap getBitmapPicture() {
        return bitmapPicture;
    }

    public void setBitmapPicture(Bitmap bitmapPicture) {
        this.bitmapPicture = bitmapPicture;
    }
}
