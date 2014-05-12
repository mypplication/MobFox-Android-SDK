package com.adsdk.sdk.customevents;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdMobBanner extends CustomEventBanner {

	private AdView adView;
	
	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		
		String adId = optionalParameters;
		listener = customEventBannerListener;

		try {
			Class.forName("com.google.android.gms.ads.AdView");
			Class.forName("com.google.android.gms.ads.AdListener");
			Class.forName("com.google.android.gms.ads.AdRequest");
			Class.forName("com.google.android.gms.ads.AdSize");
		} catch (ClassNotFoundException e) {
			if(listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		this.trackingPixel = trackingPixel;
		
		adView = new AdView(context);
		adView.setAdUnitId(adId);
		adView.setAdSize(new AdSize(width, height));
		
		adView.setAdListener(createAdListener());
		AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
		adView.loadAd(adRequest);
		
	}

	private AdListener createAdListener() {
		return new AdListener() {
			@Override
			public void onAdFailedToLoad(int errorCode) {
				if(listener != null) {
					listener.onBannerFailed();
				}
			}
			
			@Override
			public void onAdLoaded() {
				reportImpression();
				if(listener != null) {
					listener.onBannerLoaded(adView);
				}
			}
			
			@Override
			public void onAdOpened() {
				if(listener != null) {
					listener.onBannerExpanded();
				}
			}
			
			@Override
			public void onAdClosed() {
				if(listener != null) {
					listener.onBannerClosed();
				}
			}
		};
	}


}
