package com.adsdk.sdk.nativeads;

public interface NativeAdListener {
	
	public void adLoaded(NativeAd ad);

	public void adFailedToLoad();

	public void impression();

	public void adClicked();

}
