package com.example.networkcalculator.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.example.networkcalculator.DateTimeUtils;
import com.example.networkcalculator.R;
import com.example.networkcalculator.Utils;
import com.example.networkcalculator.activities.AppDetailActivity;
import com.example.networkcalculator.managers.NetworkUsageManager;
import com.example.networkcalculator.managers.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AppsFragment extends Fragment {

    Context context;

    TabLayout tabLayout;
    RecyclerView recyclerView;
    View loadingView;
    View emptyView;

    PreferenceManager preferenceManager;
    NetworkUsageManager networkUsageManager;

    public AppsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_apps, container, false);
        context = getContext();
        tabLayout = root.findViewById(R.id.tabs);
        recyclerView = root.findViewById(R.id.recycler_view);

        loadingView = root.findViewById(R.id.loading);
        emptyView = root.findViewById(R.id.empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        preferenceManager = new PreferenceManager(context);
        networkUsageManager = new NetworkUsageManager(context);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                initData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        initData();

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        executor.execute(() -> {
            List<Package> packageList = getPackages();
            handler.post(() -> {
                recyclerView.setAdapter(new PackageAdapter(packageList));
                loadingView.setVisibility(View.GONE);
                if(packageList.size() <= 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private List<Package> getPackages() {
        PackageManager packageManager = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);

        List<Package> packageList = new ArrayList<>(packageInfoList.size());
        for (PackageInfo packageInfo : packageInfoList) {
            if (packageManager.checkPermission(Manifest.permission.INTERNET, packageInfo.packageName) == PackageManager.PERMISSION_DENIED) {
                continue;
            }

            long dataUsage;

            if(tabLayout.getSelectedTabPosition() == 0) {
                dataUsage = networkUsageManager.getPackageBytesMobile(packageInfo.applicationInfo.uid, DateTimeUtils.getDayStartMillis(), DateTimeUtils.getDayEndMillis());
            } else {
                dataUsage = networkUsageManager.getPackageBytesMobile(packageInfo.applicationInfo.uid, DateTimeUtils.getPeriodStartMillis(preferenceManager.getPeriodStart()), DateTimeUtils.getPeriodEndMillis(preferenceManager.getPeriodStart()));
            }

            Log.d("TAG APP USAGE:",String.valueOf(dataUsage));
            if(dataUsage < 0) {
                continue;
            }

            Package packageItem = new Package();
            packageItem.setPackageName(packageInfo.packageName);

            packageItem.setDataUsage(dataUsage);
            packageList.add(packageItem);
            ApplicationInfo ai = null;
            try {
                ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null) {
                continue;
            }
            CharSequence appName = packageManager.getApplicationLabel(ai);
            if (appName != null) {
                packageItem.setName(appName.toString());
            }
        }

        Collections.sort(packageList, (p1, p2) -> (int) ((p2.getDataUsage() - p1.getDataUsage()) / 10));

        return packageList;
    }

    public static class Package {
        private String name;
        private String packageName;
        private Long dataUsage;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public Long getDataUsage() {
            return dataUsage;
        }

        public void setDataUsage(Long dataUsage) {
            this.dataUsage = dataUsage;
        }


    }

    public static class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {
        List<Package> mPackageList;

        public PackageAdapter(List<Package> packageList) {
            mPackageList = packageList;
        }

        @NonNull
        @Override
        public PackageAdapter.PackageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new PackageAdapter.PackageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PackageAdapter.PackageViewHolder holder, int position) {
            Package packageItem = mPackageList.get(position);
            holder.name.setText(packageItem.getName());
            holder.dataUsage.setText(Utils.convertFromBytes(packageItem.getDataUsage()));
            try {
                holder.icon.setImageDrawable(holder.context.getPackageManager().getApplicationIcon(packageItem.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.itemView.setOnClickListener(view -> {
                Intent i = new Intent(holder.context, AppDetailActivity.class);
                i.putExtra("PACKAGE", packageItem.getPackageName());
                holder.context.startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return mPackageList.size();
        }

        public class PackageViewHolder extends RecyclerView.ViewHolder {
            Context context;
            TextView name;
            TextView dataUsage;
            ImageView icon;

            public PackageViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                name = itemView.findViewById(R.id.text_app_name);
                dataUsage = itemView.findViewById(R.id.text_data_usage);
                icon = itemView.findViewById(R.id.app_icon);
            }
        }
    }
}

