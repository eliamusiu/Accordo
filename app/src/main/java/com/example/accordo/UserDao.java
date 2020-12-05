package com.example.accordo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface UserDao {
    /*
    @Query("SELECT uid, pversion, picture FROM User WHERE uid=:uid")
    User getUser(String uid); */

    @Query("SELECT * FROM User")
    List<User> getAllUsers();

    @Query("UPDATE user SET pversion = :pversion, picture=:picture WHERE uid = :uid")
    void updateUser(String uid, String pversion, String picture);

    @Insert
    void insertUser(User user);
}
