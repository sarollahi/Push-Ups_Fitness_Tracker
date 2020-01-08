package com.aastudio.sarollahi.pushup.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.aastudio.sarollahi.pushup.R;
import com.aastudio.sarollahi.pushup.features.Statistics.StatisticsFragment;

public class StatisticsActivity extends AppCompatActivity implements StatisticsFragment.StatisticsFragmentInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
    }
}
