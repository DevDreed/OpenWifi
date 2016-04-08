package com.dustinmreed.openwifi.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class OpenWiFiSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static OpenWiFiSyncAdapter sOpenWiFiSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("OpenWiFiSyncService", "onCreate - OpenWiFiSyncService");
        synchronized (sSyncAdapterLock) {
            if (sOpenWiFiSyncAdapter == null) {
                sOpenWiFiSyncAdapter = new OpenWiFiSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sOpenWiFiSyncAdapter.getSyncAdapterBinder();
    }
}