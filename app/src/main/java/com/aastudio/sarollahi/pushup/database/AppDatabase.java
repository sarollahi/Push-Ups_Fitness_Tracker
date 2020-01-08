package com.aastudio.sarollahi.pushup.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Created by Seyed Ahmad Sarollahi 01/06/2020.
 */
@Database(version = 1, entities = {SessionEntity.class}, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    abstract public SessionDOA sessionDOA();

}
