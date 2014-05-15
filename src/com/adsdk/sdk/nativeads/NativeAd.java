package com.adsdk.sdk.nativeads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

public class NativeAd {
	public static class ImageAsset {
		String url;
		Bitmap bitmap;
		int width;
		int height;
	}

	public static class Tracker {
		String type;
		String url;
	}

	private String clickUrl;
	private Map<String, ImageAsset> imageAssets = new HashMap<String, NativeAd.ImageAsset>();
	private Map<String, String> textAssets = new HashMap<String, String>();
	private List<Tracker> trackers = new ArrayList<NativeAd.Tracker>();

	public String getClickUrl() {
		return clickUrl;
	}

	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public void addTextAsset(String type, String asset) {
		textAssets.put(type, asset);
	}
	
	public void addImageAsset (String type, ImageAsset asset) {
		imageAssets.put(type, asset);
	}
	
	public String getTextAsset (String type) {
		return textAssets.get(type);
	}

	public ImageAsset getImageAsset (String type) {
		return imageAssets.get(type);
	}
	
	public List<Tracker> getTrackers() {
		return trackers;
	}

	public void setTrackers(List<Tracker> trackers) {
		this.trackers = trackers;
	}

}
