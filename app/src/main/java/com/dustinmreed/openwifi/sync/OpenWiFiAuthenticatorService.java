package com.dustinmreed.openwifi.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OpenWiFiAuthenticatorService extends Service {
    private OpenWiFiAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new OpenWiFiAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
