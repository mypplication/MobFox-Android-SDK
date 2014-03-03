package com.adsdk.sdk.customevents;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdMobCustomFullscreen extends CustomEventFullscreen {

	private InterstitialAd interstitial;

	@Override
	public void loadFullscreen(Context context, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = ""; //TODO: get from optionalParameters

		interstitial = new InterstitialAd(context);
		interstitial.setAdUnitId(adId);
		interstitial.setAdListener(createAdListener());
		AdRequest adRequest = new AdRequest.Builder().build();

		interstitial.loadAd(adRequest);
	}

	private AdListener createAdListener() {
		return new AdListener() {
			@Override
			public void onAdClosed() {
				if(listener != null) {
					listener.onFullscreenClosed();
				}
			}
			
			@Override
			public void onAdFailedToLoad(int errorCode) {
				if(listener != null) {
					listener.onFullscreenFailed();
				}
			}
			
			@Override
			public void onAdLeftApplication() {
				if(listener != null) {
					listener.onFullscreenLeftApplication();
				}
			}
			
			@Override
			public void onAdLoaded() {
				if(listener != null) {
					listener.onFullscreenLoaded(AdMobCustomFullscreen.this);
				}
			}
			
			@Override
			public void onAdOpened() {
				if(listener != null) {
					listener.onFullscreenOpened();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}

}
