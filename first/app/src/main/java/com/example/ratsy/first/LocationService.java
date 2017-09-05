package com.example.ratsy.first;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ratsy on 2017/2/24.
 */

public class LocationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("locationService","create success");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
