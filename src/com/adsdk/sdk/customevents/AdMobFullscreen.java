package com.adsdk.sdk.customevents;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdMobFullscreen extends CustomEventFullscreen {

	private InterstitialAd interstitial;
	private AdListener adListener;

	@Override
	public void loadFullscreen(Context context, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;
		
		try {
			Class.forName("com.google.android.gms.ads.AdListener");
			Class.forName("com.google.android.gms.ads.AdRequest");
			Class.forName("com.google.android.gms.ads.InterstitialAd");
		} catch (ClassNotFoundException e) {
			if(listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		
		adListener = createAdListener();
		interstitial = new InterstitialAd(context);
		interstitial.setAdUnitId(adId);
		interstitial.setAdListener(adListener);
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
					listener.onFullscreenLoaded(AdMobFullscreen.this);
				}
			}
			
			@Override
			public void onAdOpened() {
				if(listener != null) {
					reportImpression();
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
