package com.gregavola.photoTest;

import android.app.Application;
import com.aviary.android.feather.sdk.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.adobe.creativesdk.foundation.internal.auth.AdobeAuthIMSEnvironment;

public class MainApplication extends Application implements IAviaryClientCredentials {

    @Override
    public void onCreate() {
        super.onCreate();
        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext(), AdobeAuthIMSEnvironment.AdobeAuthIMSEnvironmentProductionUS);
    }

    /* 2) Be sure to fill in the two strings below. */
    private static final String CREATIVE_SDK_CLIENT_ID = "";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "";

    /* 3) Add the getBillingKey() method */
    @Override
    public String getBillingKey() {
        return ""; // Leave this blank
    }

    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }
}


