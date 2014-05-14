package com.adsdk.sdk.nativeads;

import java.util.ArrayList;
import java.util.List;

public class NativeAd {
	public static class ImageAsset {
		String type;
		String url;
		int width;
		int height;
	}

	public static class TextAsset {
		String type;
		String text;
	}

	public static class Tracker {
		String type;
		String url;
	}

	private String clickUrl;
	private List<ImageAsset> imageAssets = new ArrayList<NativeAd.ImageAsset>(); //TODO: hash maps?
	private List<TextAsset> textAssets = new ArrayList<NativeAd.TextAsset>();
	private List<Tracker> trackers = new ArrayList<NativeAd.Tracker>();

	public String getClickUrl() {
		return clickUrl;
	}

	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public List<ImageAsset> getImageAssets() {
		return imageAssets;
	}

	public void setImageAssets(List<ImageAsset> imageAssets) {
		this.imageAssets = imageAssets;
	}

	public List<TextAsset> getTextAssets() {
		return textAssets;
	}

	public void setTextAssets(List<TextAsset> textAssets) {
		this.textAssets = textAssets;
	}

	public List<Tracker> getTrackers() {
		return trackers;
	}

	public void setTrackers(List<Tracker> trackers) {
		this.trackers = trackers;
	}

}
