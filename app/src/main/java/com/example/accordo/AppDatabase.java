package com.example.accordo;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.accordo.UserDao;

@Database(entities = { User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
