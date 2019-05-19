/*
 * Copyright (c) 2015 The CyanogenMod Project
 * Copyright (c) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.thermalconfig;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Activity;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;;

import com.xiaomi.thermalconfig.util.FileUtils;

import java.util.List;

public class AutoThermalConfig {
    private static final String TAG = "AutoThermalConfig";
    private static final boolean DEBUG = true;

    private static final Handler handler = new Handler();
    private ActivityRunnable activityRunnable;

    private String foregroundApp;

    private Context mContext;

    private static final String THERMAL_MESSAGE_PATH = "/sys/class/thermal/thermal_message/sconfig";

    // Supported Thermal Modes
        private static final String MODE_DEFAULT = "0";
    private static final String MODE_DIALER = "8";
    private static final String MODE_GAME = "9";
    private static final String MODE_PERFORMANCE = "10";
    private static final String MODE_BROWSER = "11";
    private static final String MODE_CAMERA = "12";
    private static final String MODE_PUBG = "13";
    private static final String MODE_VIDEO = "14";

    public AutoThermalConfig(Context context) {
        mContext = context;
        checkActivity(context);
    }

    private void SetThermalMode(String packagename) {
        switch (packagename) {
            case "com.android.dialer":
            case "com.google.android.dialer":
                SendThermalMessage(MODE_DIALER, packagename);
                break;
            case "com.antutu.ABenchMark":
            case "com.antutu.benchmark.full":
            case "com.futuremark.dmandroid.application":
            case "com.primatelabs.geekbench":
                SendThermalMessage(MODE_PERFORMANCE, packagename);
                break;
            case "org.codeaurora.snapcam":
            case "com.android.camera":
            case "com.google.android.GoogleCamera":
            case "com.google.android.GoogleCameraEng":
            case "com.android.gallery3d":
            case "org.codeaurora.gallery":
            case "com.google.android.apps.photos":
                SendThermalMessage(MODE_CAMERA, packagename);
                break;
            case "org.lineageos.jelly":
            case "com.android.chrome":
            case "com.UCMobile.intl":
                SendThermalMessage(MODE_BROWSER, packagename);
                break;
            case "com.tencent.ig":
                SendThermalMessage(MODE_PUBG, packagename);
                break;
            case "com.google.android.youtube":
            case "com.netflix.mediaclient":
            case "com.google.android.videos":
            case "com.amazon.avod.thirdpartyclient":
            case "com.google.android.apps.youtube.kids":
                SendThermalMessage(MODE_VIDEO, packagename);
                break;
            default:
                if (isGame(packagename) == 1) {
                    SendThermalMessage(MODE_GAME, packagename);
                }else{
                    SendThermalMessage(MODE_DEFAULT, packagename);
                }
        }
    }

    private int isGame(String packageName) {
        int isGame = 0;
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if (ai != null) {
                isGame = (ai.category == ApplicationInfo.CATEGORY_GAME ||
                    (ai.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME) ? 1 : 0;
            }
        } catch (PackageManager.NameNotFoundException ex) {
            // do nothing
        }
        return isGame;
    }

    private void SendThermalMessage(String mMode, String packagename) {
        if (!(GetThermalMessage().equals(mMode))) {
            Log.d(TAG, "Set thermal config for foreground Change: " + packagename);
            if (FileUtils.writeLine(THERMAL_MESSAGE_PATH, mMode))
                Log.d(TAG, "Change Thermal Mode to " + mMode + " successful");
            else
                Log.d(TAG, "Change Thermal Mode to " + mMode + " fail!");
        }
    }

    private String GetThermalMessage() {
        return FileUtils.readOneLine(THERMAL_MESSAGE_PATH);
    }

    protected void checkActivity(Context context) {
        activityRunnable = new ActivityRunnable(context);
        handler.postDelayed(activityRunnable, 500);
    }

    protected void removeCallback() {
        handler.removeCallbacks(activityRunnable);
        if (DEBUG) Log.d(TAG, "Removed Callbacks");
        SendThermalMessage(MODE_DEFAULT, "ScreenOff");
    }

    private class ActivityRunnable implements Runnable {
        private Context context;
        private ActivityRunnable(Context context) {
            this.context = context;
        }
        @Override
        public void run() {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List < ActivityManager.RunningTaskInfo > runningTasks = manager.getRunningTasks(1);
            if (runningTasks != null && runningTasks.size() > 0) {
                ComponentName topActivity = runningTasks.get(0).topActivity;
                foregroundApp = topActivity.getPackageName();
                handler.postDelayed(this, 500);
                SetThermalMode(foregroundApp);
            }
        }
    }
}
