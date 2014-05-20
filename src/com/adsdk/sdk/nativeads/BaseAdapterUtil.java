package com.adsdk.sdk.nativeads;

public class BaseAdapterUtil {
	
	private int firstAdPosition;
	private int adPositionInterval;
	
	public BaseAdapterUtil (int firstAdPosition, int rowsOfOriginalContentBetweenAds) throws IllegalArgumentException {
		if(firstAdPosition < 0) {
			throw new IllegalArgumentException("First ad position cannot be negative!");
		}
		if(rowsOfOriginalContentBetweenAds < 1) {
			throw new IllegalArgumentException("Number of rows of original content between ads cannot be lower than 1.");
		}
		
		this.firstAdPosition = firstAdPosition;
		this.adPositionInterval = rowsOfOriginalContentBetweenAds + 1;
	}
	
	public int calculateShiftedCount(int original) {
		return original + countAdsWithinContent(original);
	}

	public int calculateShiftedPosition(int position) {
		return position - adsAlreadyShown(position);
	}

	private int adsAlreadyShown(int position) {
		if (position <= firstAdPosition) {
			return 0;
		} else {
			return (int) Math.floor((double) (position - firstAdPosition) / adPositionInterval) + 1;
		}
	}

	private int countAdsWithinContent(int contentRowCount) {
		if (contentRowCount <= firstAdPosition) {
			return 0;
		}

		int originalContentBetweenAds = adPositionInterval - 1;
		if ((contentRowCount - firstAdPosition) % originalContentBetweenAds == 0) {
			return (contentRowCount - firstAdPosition) / originalContentBetweenAds;
		} else {
			return (int) Math.floor((double) (contentRowCount - firstAdPosition) / originalContentBetweenAds) + 1;
		}
	}

	public boolean isAdPosition(int position) {

		if (position < firstAdPosition) {
			return false;
		}

		return ((position - firstAdPosition) % adPositionInterval == 0);
	}
}
