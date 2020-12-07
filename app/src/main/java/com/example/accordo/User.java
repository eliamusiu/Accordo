package com.example.accordo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
}
