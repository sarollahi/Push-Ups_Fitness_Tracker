package com.aastudio.sarollahi.pushup.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


/**
 * Created by Seyed Ahmad Sarollahi on 01/06/2020.
 */

@Dao
public interface SessionDOA {

    /**
     * Inserts a new session as SessionEntity
     * @param entity
     */
    @Insert
    public void insertSession(SessionEntity entity);

    /**
     * Returns all stores sessions
     * @return
     */
    @Query("SELECT * FROM sessions")
    public SessionEntity[] querySessions();

    /**
     * Returns an array of the number of pushups done in each session
     * @return
     */
    @Query("SELECT number_of_pushups FROM sessions")
    public int[] querySessionsPushups();

    /**
     * Returns the session entities where the goal was reached
     * @return
     */
    @Query("SELECT * FROM sessions WHERE is_goal_reached = 1")
    public SessionEntity[] queryWhenGoalReached();

    /**
     * Returns the session entities where the goal was not reached
     * @return
     */
    @Query("SELECT * FROM sessions WHERE is_goal_reached = 0")
    public SessionEntity[] queryWhenGoalFailed();

    /**
     * Returns an array of ints that represent the number of pushups done in each session grouped
     * by dau
     * @return
     */
    @Query("SELECT sum(number_of_pushups) FROM sessions GROUP BY date")
    public int[] queryDaySessionTotals();

    /**
     * Returns an int representing the pushups done today
     * @param currentDate
     * @return
     */
    @Query("SELECT sum(number_of_pushups) FROM sessions where date = :currentDate")
    public int queryTodaySessionTotal(String currentDate);

    /**
     * Deletes all data
     * @param entities
     */
    @Delete()
    public void resetTable(SessionEntity[] entities);

    /**
     * Deletes a session from the table
     * @param entity
     */
    @Delete
    public void deleteSession(SessionEntity entity);

}
