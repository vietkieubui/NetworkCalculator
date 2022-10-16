package com.example.networkcalculator.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;


import androidx.annotation.RequiresApi;


public class NetworkUsageManager {

    Context context;
    NetworkStatsManager networkStatsManager;

    public static final int BYTES_RX = 0;
    public static final int BYTES_TX = 1;
    public static final int BYTES_ALL = 2;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public NetworkUsageManager(Context context) {
        this.context = context;
        this.networkStatsManager = (NetworkStatsManager) this.context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private long getBytes(NetworkStats.Bucket bucket, int byteType) {
        switch (byteType) {
            case BYTES_RX:
                return bucket.getRxBytes();

            case BYTES_TX:
                return bucket.getTxBytes();

            case BYTES_ALL:
                return bucket.getRxBytes() + bucket.getTxBytes();
        }

        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getAllBytesMobile(long startTime, long endTime, int byteType) {
        try {
            NetworkStats.Bucket bucket;
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime);
            return getBytes(bucket, byteType);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getAllBytesMobile(long startTime, long endTime) {
        return getAllBytesMobile(startTime, endTime, BYTES_ALL);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getPackageBytesMobile(int uid, long startTime, long endTime, int byteType) {
        try {
            NetworkStats networkStats;
            networkStats = networkStatsManager.queryDetailsForUid(ConnectivityManager.TYPE_MOBILE, getSubscriberId(), startTime, endTime, uid);

            long bytes = 0;
            NetworkStats.Bucket bucket = new NetworkStats.Bucket();
            while (networkStats.hasNextBucket()) {
                networkStats.getNextBucket(bucket);
                bytes += getBytes(bucket, byteType);
            }
            networkStats.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public long getPackageBytesMobile(int uid, long startTime, long endTime) {
        return getPackageBytesMobile(uid, startTime, endTime, BYTES_ALL);
    }

    @SuppressLint("MissingPermission")
    private String getSubscriberId() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (PermissionManager.hasPermissionToReadPhoneStats(context)) {
            return tm.getSubscriberId();
        }
        return "";
    }
}
