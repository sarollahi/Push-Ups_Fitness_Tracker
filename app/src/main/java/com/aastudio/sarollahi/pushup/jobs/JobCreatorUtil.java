package com.aastudio.sarollahi.pushup.jobs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aastudio.sarollahi.pushup.Constants;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Scott Quach on 11/23/2017.
 *
 * Based of the job scheduling API provided by Evernote
 */

public class JobCreatorUtil implements JobCreator{

    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case Constants.REMINDER_JOB:
                return new ReminderJob();
            default:
                return null;
        }
    }
}
