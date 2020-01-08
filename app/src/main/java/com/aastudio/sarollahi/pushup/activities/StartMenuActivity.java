package com.aastudio.sarollahi.pushup.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aastudio.sarollahi.pushup.Constants;
import com.aastudio.sarollahi.pushup.GoalPreferenceUtil;
import com.aastudio.sarollahi.pushup.Instrumentation;
import com.aastudio.sarollahi.pushup.LegacyDatabaseTransferer;
import com.aastudio.sarollahi.pushup.R;
import com.aastudio.sarollahi.pushup.database.DatabaseManager;
import com.aastudio.sarollahi.pushup.database.SessionEntity;
import com.aastudio.sarollahi.pushup.features.Statistics.StatisticsDatabase;
import com.facebook.ads.*;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

public class StartMenuActivity extends AppCompatActivity implements DatabaseManager.DatabaseCallback, StatisticsDatabase.StatisticsDatabaseCallback {

    //    private Button dailyUpButton, dailyDownButton, monthlyUpButton, monthlyDownButton;
    private TextView dailyGoalView, dailyGoaltext, monthlyGoalView, monthlyGoaltext, dailyGoalProgress, monthlyGoalProgress;
    private ProgressBar monthProgress, dayProgress;
    private ImageButton dailyGoalEditButton, monthlyGoalEditButton;
    private Button startButton;

    private int dailyGoalValue, monthlyGoalValue;

    private NativeBannerAd nativeBannerAd;
    private DatabaseManager database = new DatabaseManager(this);
    private StatisticsDatabase statsDatabase = new StatisticsDatabase(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        dailyGoalView = (TextView) findViewById(R.id.dayGoalTextView);
        dailyGoaltext = (TextView) findViewById(R.id.textView19);
        monthlyGoalView = (TextView) findViewById(R.id.monthlyGoalTextView);
        monthlyGoaltext = (TextView) findViewById(R.id.textView17);
        startButton = findViewById(R.id.button5);

        dailyGoalProgress = (TextView) findViewById(R.id.textDayProgress);
        monthlyGoalProgress = (TextView) findViewById(R.id.textMonthProgress);

        dayProgress = (ProgressBar) findViewById(R.id.progressDay);
        monthProgress = (ProgressBar) findViewById(R.id.progressMonth);

        dailyGoalValue = GoalPreferenceUtil.getDailyGoal(this);
        dailyGoalView.setText(String.valueOf(dailyGoalValue));

        monthlyGoalValue = GoalPreferenceUtil.getMonthlyGoal(this);
        monthlyGoalView.setText(String.valueOf(monthlyGoalValue));

        dailyGoalEditButton = (ImageButton) findViewById(R.id.dailyGoalEditButton);
        monthlyGoalEditButton = (ImageButton) findViewById(R.id.monthlyGoalEditButton);

        dayProgress.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
        monthProgress.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);



        nativeBannerAd = new NativeBannerAd(this, Constants.start_key);
        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }
            @Override
            public void onError(Ad ad, AdError adError) {

            }
            @Override
            public void onAdLoaded(Ad ad) {
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateAd(nativeBannerAd);
            }
            @Override
            public void onAdClicked(Ad ad) {

            }
            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        // load the ad
        nativeBannerAd.loadAd();


        SharedPreferences settingPref = getSharedPreferences("settingsFile", MODE_PRIVATE);
        if (!settingPref.getBoolean(Constants.LEGACY_DATABASE_TRANSFER, false)) {
            new LegacyDatabaseTransferer(this).transferData();
            settingPref.edit().putBoolean(Constants.LEGACY_DATABASE_TRANSFER, true).apply();
            Timber.d("legacy database called");
        } else {
            Timber.d("legacy datbase not called");
        }

        requestDataUpdate();

        dailyGoalEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumPicker(dailyGoalEditButton.getId());
            }
        });

        monthlyGoalEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumPicker(monthlyGoalEditButton.getId());
            }
        });
    }


    private void inflateAd(NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container.
        NativeAdLayout nativeAdLayout = findViewById(R.id.native_banner_ad_container);
        LayoutInflater inflater = LayoutInflater.from(StartMenuActivity.this);
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_banner_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(StartMenuActivity.this, nativeBannerAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        AdIconView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(
                nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }

    @Override
    protected void onResume() {
        super.onResume();

        nativeBannerAd = new NativeBannerAd(this, Constants.start_key);
        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }
            @Override
            public void onError(Ad ad, AdError adError) {

            }
            @Override
            public void onAdLoaded(Ad ad) {
                // Race condition, load() called again before last ad was displayed
                if (nativeBannerAd == null || nativeBannerAd != ad) {
                    return;
                }
                // Inflate Native Banner Ad into Container
                inflateAd(nativeBannerAd);
            }
            @Override
            public void onAdClicked(Ad ad) {

            }
            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        // load the ad
        nativeBannerAd.loadAd();
    }

    /**
     * Displays a number picker meant to allow the user to set their daily or monthly goal. The
     * passed in goalType is the id of the edit button and determines which goal the selected number
     * should be applied to
     *
     * @param goalType
     */
    private void showNumPicker(final int goalType) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Set Goal Value")
                .setView(input)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputString = (input.getText().length() != 0 ? input.getText().toString() : "0");
                        int inputNum = 0;
                        try {
                            inputNum = Integer.valueOf(inputString);
                        } catch (NumberFormatException exception) {
                            inputNum = 0;
                        }

                        if (goalType == dailyGoalEditButton.getId()) {
                            dailyGoalValue = inputNum;

                            GoalPreferenceUtil.setDailyGoal(StartMenuActivity.this, dailyGoalValue);
                            dailyGoalView.setText(String.valueOf(dailyGoalValue));
                        } else if (goalType == monthlyGoalEditButton.getId()) {
                            monthlyGoalValue = inputNum;

                            GoalPreferenceUtil.setMonthlyGoal(StartMenuActivity.this, monthlyGoalValue);
                            monthlyGoalView.setText(String.valueOf(monthlyGoalValue));
                        }
                        requestDataUpdate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    /**
     * Requests that the databases load the nessesary data retaining to calculating user goal progress
     */
    private void requestDataUpdate() {
        database.loadSessions();
        statsDatabase.requestTotalDayPushups();
    }

    /**
     * Navigates to the tracker activity
     *
     * @param view
     */
    public void startTrackingClicked(View view) {
        Intent openMainActivity = new Intent(this, TrackerActivity.class);
        startActivity(openMainActivity);
    }

    /**
     * Increases the goal value and updates the screen as well as updates the shared preference
     *
     * @param view
     */
    public void upButtonClicked(View view) {
        dailyGoalValue++;
        GoalPreferenceUtil.setDailyGoal(this, dailyGoalValue);
        dailyGoalView.setText(String.valueOf(dailyGoalValue));
        requestDataUpdate();
    }

    /**
     * Decreases the gaol value and updates the screen as well as updates the shared preference
     *
     * @param view
     */
    public void downButtonClicked(View view) {
        if (dailyGoalValue >= 1) {
            dailyGoalValue--;
        }
        GoalPreferenceUtil.setDailyGoal(this, dailyGoalValue);
        dailyGoalView.setText(String.valueOf(dailyGoalValue));
        requestDataUpdate();
    }

    /**
     * Navigates to the settings page
     *
     * @param view
     */
    public void openSettingsPage(View view) {
        Instrumentation.getInstance().track(Instrumentation.TrackEvents.OPENED_SETTINGS, Instrumentation.TrackValues.SUCCESS);
        Intent openSettings = new Intent(StartMenuActivity.this, SettingsActivity.class);
        startActivity(openSettings);
    }

    /**
     * Navigates to the logs page
     *
     * @param view
     */
    public void logButtonClicked(View view) {
        Intent openSaves = new Intent(StartMenuActivity.this, SavesActivity.class);
        startActivity(openSaves);
    }

    /**
     * Navigates to the statistics page
     *
     * @param view
     */
    public void statiticsButtonClicked(View view) {
        startActivity(new Intent(StartMenuActivity.this, StatisticsActivity.class));
    }

    private void updateMonthlyGoalProgress(int pushupsDone) {
        monthProgress.setMax(monthlyGoalValue);
        monthProgress.setProgress(pushupsDone);
        monthlyGoalProgress.setText(String.valueOf(pushupsDone));
    }

    private void updateDailyGoalProgress(int pushupsDone) {
        dayProgress.setMax(dailyGoalValue);
        dayProgress.setProgress(pushupsDone);
        dailyGoalProgress.setText(String.valueOf(pushupsDone));
    }


    /**
     * Callback for when session data is loaded, used to compile the total pushups done by the
     * user this month
     *
     * @param data array of stored sessions
     */
    @Override
    public void onSessionDataLoaded(@Nullable SessionEntity[] data) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        DateTime currentDate = new DateTime();
        int currentMonth = currentDate.getMonthOfYear();
        int currentYear = currentDate.getYear();

        List<SessionEntity> monthsEntities = new ArrayList<>();

        for (SessionEntity entity : data) {
            String stringDate = entity.date;
            DateTime entityDate = formatter.parseDateTime(stringDate);

            if (entityDate.getYear() == currentYear && entityDate.getMonthOfYear() == currentMonth) {
                monthsEntities.add(entity);
            }
        }

        int monthlyTotal = 0;
        for (SessionEntity entity : monthsEntities) {
            monthlyTotal += entity.numberOfPushups;
        }

        Timber.d("monthlyTotal is " + monthlyTotal);
        updateMonthlyGoalProgress(monthlyTotal);
    }


    /**
     * statsDatabase callback that is used to determine how many pushups the user has done today
     *
     * @param total pushups done today
     */
    @Override
    public void totalDayPushupsLoaded(int total) {
        updateDailyGoalProgress(total);
    }

    /*
    The following are unused callbacks
     */
    @Override
    public void dayHighScoreLoaded(int highscore) {

    }

    @Override
    public void totalPushupsLoaded(int total) {

    }

    @Override
    public void highScoreLoaded(int highscore) {

    }

    @Override
    public void timesGoalReached(int num) {

    }

    @Override
    public void timesGoalFailed(int num) {

    }

    @Override
    public void graphDataLoaded(LineGraphSeries<DataPoint> series) {

    }


}
