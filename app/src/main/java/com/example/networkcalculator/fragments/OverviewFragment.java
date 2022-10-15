package com.example.networkcalculator.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.example.networkcalculator.DateTimeUtils;
import com.example.networkcalculator.R;
import com.example.networkcalculator.Utils;
import com.example.networkcalculator.managers.NetworkUsageManager;
import com.example.networkcalculator.managers.PreferenceManager;
import com.example.networkcalculator.views.UsageBarView;


public class OverviewFragment extends Fragment {

    Context context;

    UsageBarView dailyUsageBarView;
    UsageBarView periodUsageBarView;

    PreferenceManager preferenceManager;
    NetworkUsageManager networkUsageManager;

    public OverviewFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_overview, container, false);
        context = getContext();
        dailyUsageBarView = root.findViewById(R.id.daily_usage_bar);
        periodUsageBarView = root.findViewById(R.id.period_usage_bar);

        preferenceManager = new PreferenceManager(context);
        networkUsageManager = new NetworkUsageManager(context);

        calculateOverview();

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void calculateOverview() {
        int daysTillEndOfPeriod = DateTimeUtils.getDaysTillPeriodEnd(preferenceManager);
        Long periodUsage = networkUsageManager.getAllBytesMobile(DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getDayEndMillis());
        Long periodLimit = preferenceManager.getPeriodLimit();

        periodUsageBarView.setData(periodUsage, periodLimit);

        Long dailyUsage = networkUsageManager.getAllBytesMobile(DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis());
        Long dailyLimit = preferenceManager.getDailyLimitCustom() ? preferenceManager.getDailyLimit() : (periodLimit - (periodUsage-dailyUsage)) / daysTillEndOfPeriod;

        dailyUsageBarView.setData(dailyUsage, dailyLimit);
    }
}