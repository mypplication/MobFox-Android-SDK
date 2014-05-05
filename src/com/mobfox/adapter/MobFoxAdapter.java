package com.mobfox.adapter;

// Copyright 2011 Google Inc. All Rights Reserved.

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.banner.AdView;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.MediationBannerAdapter;
import com.google.ads.mediation.MediationBannerListener;
import com.google.ads.mediation.MediationInterstitialAdapter;
import com.google.ads.mediation.MediationInterstitialListener;

import android.app.Activity;
import android.view.View;

public final class MobFoxAdapter implements MediationBannerAdapter<MobFoxExtras, MobFoxServerParameters>, MediationInterstitialAdapter<MobFoxExtras, MobFoxServerParameters> {

	private static final String REQUEST_URL = "http://my.mobfox.com/request.php";
	private AdView adView;
	private AdManager mAdManager;

	/*
	 * Callback listeners. This class handles both in-activity (banner) and
	 * interstitial ads, so it listens for both
	 */
	private MediationBannerListener bannerListener;
	private MediationInterstitialListener interstitialListener;

	/*
	 * ------------------------------------------------------------------------
	 * MediationAdapter Implementation
	 * ------------------------------------------------------------------------
	 */

	/*
	 * These methods let the mediation layer know what data types are used for
	 * server-side parameters and publisher "extras"
	 */
	@Override
	public Class<MobFoxExtras> getAdditionalParametersType() {
		return MobFoxExtras.class;
	}

	@Override
	public Class<MobFoxServerParameters> getServerParametersType() {
		return MobFoxServerParameters.class;
	}

	/*
	 * Ad Requests
	 */
	@Override
	public void requestBannerAd(MediationBannerListener listener, Activity activity, MobFoxServerParameters serverParameters, AdSize adSize, MediationAdRequest mediationAdRequest,
			MobFoxExtras extras) {
		bannerListener = listener;

		if (extras != null)
			adView = new AdView(activity, REQUEST_URL, serverParameters.pubIdNumber, extras.getLocation(), extras.getAnimation());
		else
			adView = new AdView(activity, REQUEST_URL, serverParameters.pubIdNumber, true, true);

		adView.setAdspaceHeight(adSize.getHeight());
		adView.setAdspaceWidth(adSize.getWidth());
		adView.setAdListener(new AdListener() {

			@Override
			public void noAdFound() {
				if (bannerListener != null)
					bannerListener.onFailedToReceiveAd(MobFoxAdapter.this, ErrorCode.NO_FILL);
			}

			@Override
			public void adShown(Ad arg0, boolean arg1) {
				if (bannerListener != null)
					bannerListener.onPresentScreen(MobFoxAdapter.this);
			}

			@Override
			public void adLoadSucceeded(Ad arg0) {
				if (bannerListener != null)
					bannerListener.onReceivedAd(MobFoxAdapter.this);
			}

			@Override
			public void adClosed(Ad arg0, boolean arg1) {
				if (bannerListener != null)
					bannerListener.onDismissScreen(MobFoxAdapter.this);
			}

			@Override
			public void adClicked() {
				if (bannerListener != null)
					bannerListener.onClick(MobFoxAdapter.this);
			}
		});

		adView.loadNextAd();
		adView.pause();
	}

	@Override
	public void requestInterstitialAd(MediationInterstitialListener listener, Activity activity, MobFoxServerParameters serverParameters, MediationAdRequest mediationAdRequest,
			MobFoxExtras extras) {
		interstitialListener = listener;

		if (extras != null)
			mAdManager = new AdManager(activity, REQUEST_URL, serverParameters.pubIdNumber, extras.getLocation());
		else
			mAdManager = new AdManager(activity, REQUEST_URL, serverParameters.pubIdNumber, true);
		
		mAdManager.setVideoAdsEnabled(true);
		
		mAdManager.setListener(new AdListener() {

			@Override
			public void noAdFound() {
				if (interstitialListener != null)
					interstitialListener.onFailedToReceiveAd(MobFoxAdapter.this, ErrorCode.NO_FILL);
			}

			@Override
			public void adShown(Ad arg0, boolean arg1) {
				if (interstitialListener != null)
					interstitialListener.onPresentScreen(MobFoxAdapter.this);
			}

			@Override
			public void adLoadSucceeded(Ad arg0) {
				if (interstitialListener != null)
					interstitialListener.onReceivedAd(MobFoxAdapter.this);

			}

			@Override
			public void adClosed(Ad arg0, boolean arg1) {
				if (interstitialListener != null)
					interstitialListener.onDismissScreen(MobFoxAdapter.this);
			}

			@Override
			public void adClicked() {
			}
		});
		mAdManager.requestAd();
	}

	@Override
	public void showInterstitial() {
		if (mAdManager != null)
			mAdManager.showAd();
	}

	@Override
	public void destroy() {
		if (adView != null)
			adView.release();
		if (mAdManager != null)
			mAdManager.release();
		bannerListener = null;
		interstitialListener = null;
	}

	@Override
	public View getBannerView() {
		return adView;
	}
}
