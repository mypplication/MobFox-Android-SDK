package com.adsdk.sdk.nativeads;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NativeViewBinder {
	private Map<String, Integer> textAssetsBidings;
	private Map<String, Integer> imageAssetsBidings;
	private int adLayoutId;
	
	public NativeViewBinder(int layoutId) {
		adLayoutId = layoutId;
		textAssetsBidings = new HashMap<String, Integer>();
		imageAssetsBidings = new HashMap<String, Integer>();
	}
	
	public void bindTextAsset(String tag, int id) {
		textAssetsBidings.put(tag, id);
	}
	
	public void bindImageAsset(String tag, int id) {
		imageAssetsBidings.put(tag, id);
	}
	
	public int getAdLayoutId() {
		return adLayoutId;
	}
	
	public int getIdForTextAsset(String tag) {
		Integer result = textAssetsBidings.get(tag);
		if (result != null) {
			return result;
		} else {
			return 0;
		}
	}
	
	public int getIdForImageAsset(String tag) {
		Integer result = imageAssetsBidings.get(tag);
		if (result != null) {
			return result;
		} else {
			return 0;
		}
	}
	
	public Set<String> getTextAssetsBindingsKeySet() {
		return textAssetsBidings.keySet();
	}
	
	public Set<String> getImageAssetsBindingsKeySet() {
		return imageAssetsBidings.keySet();
	}

}
