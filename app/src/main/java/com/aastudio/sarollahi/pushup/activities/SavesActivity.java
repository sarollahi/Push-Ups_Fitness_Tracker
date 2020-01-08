package com.aastudio.sarollahi.pushup.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aastudio.sarollahi.pushup.Constants;
import com.aastudio.sarollahi.pushup.Instrumentation;
import com.aastudio.sarollahi.pushup.R;
import com.aastudio.sarollahi.pushup.adapters.RecyclerSavesAdapter;
import com.aastudio.sarollahi.pushup.database.DatabaseManager;
import com.aastudio.sarollahi.pushup.database.SessionEntity;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SavesActivity extends AppCompatActivity implements DatabaseManager.DatabaseCallback{

    private TextView highscoreView;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private SessionEntity[] saveData;

    private RecyclerView recyclerView;
    private RecyclerSavesAdapter adapter;

    private DatabaseManager database;

    private NativeBannerAd nativeBannerAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saves);

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

        sharedPref = getSharedPreferences("savedPushUpsFile1", MODE_PRIVATE);
        editor = sharedPref.edit();

        highscoreView = (TextView) findViewById(R.id.highscoreView);
        setHighscoreView();

        recyclerView = (RecyclerView) findViewById(R.id.savedRecyclerView);

        database = new DatabaseManager(this);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Timber.d("An item was swiped");
                Toast.makeText(SavesActivity.this, "Session was deleted", Toast.LENGTH_SHORT).show();
                database.deleteSession(saveData[viewHolder.getAdapterPosition()]);
                loadData();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();

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
     * Update the TextView with the users high score, defaults to 0
     */
    private void setHighscoreView() {
        int highscore = sharedPref.getInt("highscore", 0);
        highscoreView.setText("Best Session " + String.valueOf(highscore));

    }

    /**
     * Calls to the database to load the sessions in order to be displayed, recycler will be updated
     * inside callback
     */
    private void loadData() {
        database.loadSessions();
    }

    /**
     * Takes in session data and updates the recycler
     * @param data
     */
    private void populateListView(SessionEntity[] data) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerSavesAdapter(this, data);
        recyclerView.setAdapter(adapter);
        recyclerView.scheduleLayoutAnimation();
    }

/*On Button
Cicks
 */

    public void homeButtonClicked(View view) {
        Intent openHome = new Intent(this, StartMenuActivity.class);
        startActivity(openHome);
        finish();
    }

    public void resetButtonClicked(View view) {
        new AlertDialog.Builder(SavesActivity.this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    public void onClick(DialogInterface dialog, int which) {
//                        sentinel = sharedPref.getInt("sentinel", 0);
                        editor.clear();
                        editor.apply();
                        database.resetSessionData(saveData);

                        adapter.resetData();
                        adapter.notifyDataSetChanged();

                        highscoreView.setText("Highscore: ");
                        SharedPreferences settingsPref = getSharedPreferences("settingsFile", MODE_PRIVATE);
                        settingsPref.edit().putBoolean(Constants.LEGACY_DATABASE_TRANSFER, false).apply();

                        Instrumentation.getInstance().track(Instrumentation.TrackEvents.RESET_SAVES, Instrumentation.TrackValues.SUCCESS);
                        Toast.makeText(SavesActivity.this, "Data has been reset", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onSessionDataLoaded(SessionEntity[] data) {
        Timber.d("Callback called");
        saveData = data;
        populateListView(data);
    }

    private void inflateAd(NativeBannerAd nativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container.
        NativeAdLayout nativeAdLayout = findViewById(R.id.native_banner_ad_container);
        LayoutInflater inflater = LayoutInflater.from(SavesActivity.this);
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_banner_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdChoices icon
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(SavesActivity.this, nativeBannerAd, nativeAdLayout);
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
}
