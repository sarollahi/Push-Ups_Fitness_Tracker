package com.aastudio.sarollahi.pushup.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Seyed Ahmad Sarollahi on 01/06/2020.
 * Entity that contains the data for a single push up session
 */
@Entity(tableName = "sessions")
public class SessionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "number_of_pushups")
    public int numberOfPushups;

    @ColumnInfo(name = "is_goal_reached")
    public boolean isGoalReached;

    @ColumnInfo
    public String date;
}