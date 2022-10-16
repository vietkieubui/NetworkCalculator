package com.example.networkcalculator;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class NetworkCalculatorApplication extends Application {
    @Override
    public void onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this);
        super.onCreate();
    }
}
