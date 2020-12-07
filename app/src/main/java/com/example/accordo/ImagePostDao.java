package com.example.accordo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImagePostDao {
    @Query("SELECT * FROM Images")
    List<TextImagePost> getAllImages();

    @Insert
    void insertImage(TextImagePost imagePost);

    @Delete
    void deleteImage(TextImagePost imagePost);
}
