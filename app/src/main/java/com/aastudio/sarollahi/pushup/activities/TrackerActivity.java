package com.aastudio.sarollahi.pushup.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aastudio.sarollahi.pushup.Constants;
import com.aastudio.sarollahi.pushup.GoalPreferenceUtil;
import com.aastudio.sarollahi.pushup.R;
import com.aastudio.sarollahi.pushup.database.DatabaseManager;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;


import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import timber.log.Timber;


public class TrackerActivity extends AppCompatActivity implements SensorEventListener {

    private TextView countDisplay;
    private Switch vibrateSwitch;
    private Switch soundSwitch;

    private int numberOfPushUps = 0;
    private int goalValue;

    private MediaPlayer player;

    private SensorManager sm;
    private Sensor proximitySensor;

    private SharedPreferences savePref, settingPref;
    private SharedPreferences.Editor saveEditor;

    TextToSpeech tts;

    private DateTime startTime;
    private DateTime endTime;
    private boolean isTrackingDuration = false;
    private int intervalAverage = 0;

    private DatabaseManager database;
    private NativeBannerAd nativeBannerAd;
    private ImageButton vibratebtn, soundbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        database = new DatabaseManager(this);

        nativeBannerAd = new NativeBannerAd(this, Constants.tracker_key);
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

        vibrateSwitch = (Switch) findViewById(R.id.vibrateSwitch);
        soundSwitch = (Switch) findViewById(R.id.soundSwitch);
        countDisplay = (TextView) findViewById(R.id.countDisplay);

        vibratebtn = findViewById(R.id.vibrateButton);
        soundbtn = findViewById(R.id.soundButton);

        vibratebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vibrateSwitch.isChecked()){
                    vibrateSwitch.setChecked(false);
                    vibratebtn.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    vibratebtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                }else {
                    vibrateSwitch.setChecked(true);
                    vibratebtn.setBackgroundTintList(getResources().getColorStateList(R.color.yellow));
                    vibratebtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });

        soundbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundSwitch.isChecked()){
                    soundSwitch.setChecked(false);
                    soundbtn.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                    soundbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                }else {
                    soundSwitch.setChecked(true);
                    soundbtn.setBackgroundTintList(getResources().getColorStateList(R.color.yellow));
                    soundbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        });

        savePref = getSharedPreferences("savedPushUpsFile1", MODE_PRIVATE);
        saveEditor = savePref.edit();
        settingPref = getSharedPreferences("settingsFile", MODE_PRIVATE);
        goalValue = GoalPreferenceUtil.getDailyGoal(this);

        player = MediaPlayer.create(TrackerActivity.this, R.raw.beep);

        //configure proximity sensor
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sm == null) {
            Toast.makeText(this, "Proximity sensor not available on device", Toast.LENGTH_LONG).show();
        }

        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sm.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        //configure TTS
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void inflateAd(NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container.
        NativeAdLayout nativeAdLayout = findViewById(R.id.native_banner_ad_container);
        LayoutInflater inflater = LayoutInflater.from(TrackerActivity.this);
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_banner_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(TrackerActivity.this, nativeBannerAd, nativeAdLayout);
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

    //retrieve highscore
    private int getHighscore() {
        int highscore = savePref.getInt("highscore", 1);
        return highscore;
    }

    //track duration between each push-up. tts reminder when slowing down
    private void trackEncouragement() {
        if (isTrackingDuration) {
//            isTrackingDuration = false;
            endTime = new DateTime();
            Duration dur = new Duration(startTime, endTime);

            long milliseconds = dur.getMillis();
            calculateAverageInterval((int) milliseconds);
            Timber.d("interval was " + milliseconds);
            if (numberOfPushUps > 3 && milliseconds >= (intervalAverage + 600)) {
                tts.speak("Your Slowing down, keep it up", TextToSpeech.QUEUE_FLUSH, null);
            }

            isTrackingDuration = true;
            startTime = new DateTime();
        } else {
            isTrackingDuration = true;
            startTime = new DateTime();
        }
    }

    private void calculateAverageInterval(int interval) {
        if (numberOfPushUps == 1) {
            intervalAverage = interval;
        } else {
            intervalAverage = ((intervalAverage + interval) / 2);
            Timber.d("Average interval is " + intervalAverage);
        }

    }

    //Canel media player
    @Override
    protected void onPause() {
        super.onPause();

        if (this.isFinishing()) {
            if (player != null) {
                player.stop();
            }
            proximitySensor = null;
            sm = null;
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        } else {
            if (player != null) {
                player.stop();

            }
            proximitySensor = null;
            sm = null;
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Proximity sensor to detect push-ups
    @Override
    public void onSensorChanged(SensorEvent event) {

        for (int i = 0; i < 1; i++) {
            if (event.values[0] <= 3.0f) {

                if (player != null && proximitySensor != null) {
                    numberOfPushUps++;
                    if (settingPref.getInt("encouragementSetting", 1) == 1) {
                        trackEncouragement();
                    }
                    //check highscore
                    if (numberOfPushUps > getHighscore()) {
                        saveEditor.putInt("highscore", numberOfPushUps);
                        saveEditor.commit();
                    }
                    //determine if goal has been reached
                    if (numberOfPushUps == goalValue) {
                        int check = settingPref.getInt("voiceSetting", 1);
                        if (check == 1) {
                            String name = settingPref.getString("name", "");

                            tts.speak("Goal Reached nice job " + name, TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            if (player != null) player.start();
                        }
                    } else {
                        if (soundSwitch.isChecked()) {
                            if (player != null) player.start();
                        }
                    }

                    if (vibrateSwitch.isChecked() && proximitySensor != null) {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
                    }
                    countDisplay.setText(String.valueOf(numberOfPushUps));
                } else {
                    player = MediaPlayer.create(TrackerActivity.this, R.raw.beep);
                }
            }
        }
    }


/*
Button Clicks
 */

    public void countUpButtonPressed(View view) {
        if (settingPref.getInt("encouragementSetting", 1) == 1) {
            trackEncouragement();
        }
        numberOfPushUps++;
        countDisplay.setText(String.valueOf(numberOfPushUps));

        //check highscore
        if (numberOfPushUps > getHighscore()) {
            saveEditor.putInt("highscore", numberOfPushUps);
            saveEditor.commit();
        }

        if (vibrateSwitch.isChecked() && proximitySensor != null) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(80);
        }

        if (player != null && proximitySensor != null && soundSwitch.isChecked()) {
            if (numberOfPushUps == goalValue) {
                int check = settingPref.getInt("voiceSetting", 1);
                if (check == 1) {
                    String name = settingPref.getString("name", "set name");
                    tts.speak("Goal Reached nice job " + name, TextToSpeech.QUEUE_FLUSH, null);

                } else {
                    player.start();
                }

            } else {
                player.start();
            }
        } else {
            player = MediaPlayer.create(TrackerActivity.this, R.raw.beep);
        }
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

    public void savedButtonClicked(View view) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String formattedDate = df.format(c.getTime());

        if (numberOfPushUps >= goalValue) {
//            savePushUpFile = numberOfSaves + "." + " " + "On " + formattedDate + " you did : " + numberOfPushUps + " Push-Ups, GOAL REACHED";
            database.insertSession(numberOfPushUps, true, formattedDate);
        } else {
//            savePushUpFile = numberOfSaves + "." + " " + "On " + formattedDate + " you did : " + numberOfPushUps + " Push-Ups";
            database.insertSession(numberOfPushUps, false, formattedDate);
        }

        if (player != null) player.stop();
        proximitySensor = null;
        player = null;
        sm = null;

        startActivity(new Intent(TrackerActivity.this, SavesActivity.class));
        finish();
    }

    public void refreshButtonPressed(View view) {
        numberOfPushUps = 0;
        countDisplay.setText(String.valueOf(numberOfPushUps));
    }

    public void decreaseButtonClicked(View view) {
        if (numberOfPushUps > 0) {
            numberOfPushUps--;
            countDisplay.setText(String.valueOf(numberOfPushUps));
        } else Toast.makeText(this, "Cannot decrease anymore", Toast.LENGTH_SHORT).show();
    }
}
