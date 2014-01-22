/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.adsdk.sdk.mraid;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class AdViewController {
    static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    static final int DEFAULT_REFRESH_TIME_MILLISECONDS = 60000;
    private static WeakHashMap<View,Boolean> sViewShouldHonorServerDimensions = new WeakHashMap<View, Boolean>();;

    private  Context mContext;
    private  Runnable mRefreshRunnable;

    private boolean mIsDestroyed;
    private Handler mHandler;
    private boolean mIsLoading;
    private String mUrl;

    private Map<String, Object> mLocalExtras = new HashMap<String, Object>();
    private boolean mAutoRefreshEnabled = true;
    private String mKeywords;
    private Location mLocation;
    private boolean mIsFacebookSupported = true;
    private boolean mIsTesting;

    protected static void setShouldHonorServerDimensions(View view) {
        sViewShouldHonorServerDimensions.put(view, true);
    }

    public void loadAd() {

        if (!isNetworkAvailable()) {
            Log.d("MoPub", "Can't load an ad because there is no network connectivity.");
            scheduleRefreshTimerIfEnabled();
            return;
        }

        // tested (remove me when the rest of this is tested)
    }

    void loadNonJavascript(String url) {
        if (url == null) return;

        Log.d("MoPub", "Loading url: " + url);
        if (mIsLoading) {
            return;
        }

        mUrl = url;
        mIsLoading = true;

        fetchAd(mUrl);
    }

    public void reload() {
        Log.d("MoPub", "Reload ad: " + mUrl);
        loadNonJavascript(mUrl);
    }


    void setFailUrl(String failUrl) {
    }

    void setNotLoading() {
        this.mIsLoading = false;
    }

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public boolean isFacebookSupported() {
        return mIsFacebookSupported;
    }

    public void setFacebookSupported(boolean enabled) {
        mIsFacebookSupported = enabled;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public void setTimeout(int milliseconds) {
    }

    public boolean getAutorefreshEnabled() {
        return mAutoRefreshEnabled;
    }

    public void setAutorefreshEnabled(boolean enabled) {
        mAutoRefreshEnabled = enabled;

        if (mAutoRefreshEnabled) {
            scheduleRefreshTimerIfEnabled();
        } else {
            cancelRefreshTimer();
        }
    }

    public boolean getTesting() {
        return mIsTesting;
    }

    public void setTesting(boolean enabled) {
        mIsTesting = enabled;
    }


    boolean isDestroyed() {
        return mIsDestroyed;
    }

    /*
     * Clean up the internal state of the AdViewController.
     */
    void cleanup() {
        if (mIsDestroyed) {
            return;
        }

        setAutorefreshEnabled(false);
        cancelRefreshTimer();

        // WebView subclasses are not garbage-collected in a timely fashion on Froyo and below,
        // thanks to some persistent references in WebViewCore. We manually release some resources
        // to compensate for this "leak".



        // Flag as destroyed. LoadUrlTask checks this before proceeding in its onPostExecute().
        mIsDestroyed = true;
    }


    void trackImpression() {
    }

    void registerClick() {
    }

    void fetchAd(String mUrl) {
    }

    void forceRefresh() {
        setNotLoading();
        loadAd();
    }


    void scheduleRefreshTimerIfEnabled() {
    }

    void setLocalExtras(Map<String, Object> localExtras) {
        mLocalExtras = (localExtras != null)
                ? new HashMap<String,Object>(localExtras)
                : new HashMap<String,Object>();
    }

    Map<String, Object> getLocalExtras() {
        return (mLocalExtras != null)
                ? new HashMap<String,Object>(mLocalExtras)
                : new HashMap<String,Object>();
    }

    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    private boolean isNetworkAvailable() {
        // If we don't have network state access, just assume the network is up.
        int result = mContext.checkCallingPermission(ACCESS_NETWORK_STATE);
        if (result == PackageManager.PERMISSION_DENIED) return true;

        // Otherwise, perform the connectivity check.
        ConnectivityManager cm
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    @Deprecated
    public void customEventDidLoadAd() {
        setNotLoading();
        trackImpression();
        scheduleRefreshTimerIfEnabled();
    }

    @Deprecated
    public void customEventActionWillBegin() {
        registerClick();
    }
}
