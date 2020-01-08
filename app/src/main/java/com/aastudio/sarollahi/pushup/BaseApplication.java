package com.aastudio.sarollahi.pushup;

import android.app.Application;
import androidx.room.Room;
import android.content.SharedPreferences;

import com.aastudio.sarollahi.pushup.database.AppDatabase;
import com.aastudio.sarollahi.pushup.jobs.JobCreatorUtil;
import com.aastudio.sarollahi.pushup.jobs.ReminderJob;
import com.evernote.android.job.JobManager;
import com.facebook.ads.AudienceNetworkAds;


import timber.log.Timber;


public class BaseApplication extends Application {

    private static BaseApplication instance = null;
    public AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        Instrumentation.getInstance().init(this);
        Timber.plant(new MyDebugTree());

        AudienceNetworkAds.initialize(getApplicationContext());
        AudienceNetworkAds.isInAdsProcess(getApplicationContext());

        if (instance == null) {
            instance = this;
        }

        JobManager.create(this).addJobCreator(new JobCreatorUtil());
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "sessions-database").build();

        SharedPreferences settingsPref = getSharedPreferences("settingsFile", MODE_PRIVATE);
        if (settingsPref.getInt("reminderSetting", 1) == 1) {
            if (JobManager.instance().getAllJobRequests().isEmpty()) {
                ReminderJob.scheduleJob(settingsPref.getInt("reminder_hour", 7), settingsPref.getInt("reminder_minute", 0));
            }
        }
    }

    public static BaseApplication getInstance() {
        return instance;
    }


    public class MyDebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("[L:%s] [M:%s] [C:%s]",
                    element.getLineNumber(),
                    element.getMethodName(),
                    super.createStackElementTag(element));
        }
    }
}
