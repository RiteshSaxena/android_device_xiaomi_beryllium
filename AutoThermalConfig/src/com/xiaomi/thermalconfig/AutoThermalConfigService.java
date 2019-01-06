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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class AutoThermalConfigService extends Service {
    static final String TAG = "AutoThermalConfig";
    private static final boolean DEBUG = true;

    private Context mContext;

    private boolean flag;

    private AutoThermalConfig mAutoThermalConfig;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "Starting service");
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenStateFilter);
        flag = false;
        mAutoThermalConfig = new AutoThermalConfig(mContext);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "Destroying service");
        super.onDestroy();
        mAutoThermalConfig.removeCallback();
        mContext.unregisterReceiver(mScreenStateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ScreenOn() {
        if (DEBUG) Log.d(TAG, "Screen on");
        mAutoThermalConfig.checkActivity(mContext);
    }

    private void ScreenOff() {
        if (DEBUG) Log.d(TAG, "Screen off");
        flag = true;
        mAutoThermalConfig.removeCallback();
    }

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (flag) ScreenOn();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                ScreenOff();
            }
        }
    };
}
