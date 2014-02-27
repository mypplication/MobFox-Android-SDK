package com.adsdk.sdk.video;

import java.util.ArrayList;
import java.util.List;


public class VAST {
	String version;
	List<Ad> ads = new ArrayList<Ad>();
	String error;

	public static class Ad {
		String id;
		int sequence;
		InLine inLine;
		Wrapper wrapper;
		
		public static class AdSystem {
			String version;
			String name;
		}
		
		public static class Wrapper {
			AdSystem adSystem;
			List<Impression> impressions = new ArrayList<Impression>();
			String VASTAdTagUri;
			String error;
			List<Creative> creatives = new ArrayList<Creative>();
		}
		
		public static class Impression {
			String id;
			String url;
		}

		public static class InLine {
			AdSystem adSystem;
			String adTitle;
			String description;
			String advertiser;
			String error;
			List<Impression> impressions = new ArrayList<Impression>();
			List<Creative> creatives = new ArrayList<Creative>();

		}

		public static class Creative {
			String id;
			int sequence;
			String adId;
			String apiFramework;
			Linear linear;
			CompanionAds companionAds;
			NonLinearAds nonLinearAds;

			public static class Linear {
				int skipoffset;
				int duration; // required, format: HH:MM:SS or HH:MM:SS.mmm
				List<MediaFile> mediaFiles = new ArrayList<MediaFile>(); // required (1-*)
				List<Tracking> trackingEvents = new ArrayList<Tracking>(); // optional (1-*)
				VideoClicks videoClicks;

				public static class MediaFile {
					String id;
					String delivery;
					String type;
					String bitrate;
					int width;
					int height;
					boolean scalable;
					boolean maintainAspectRatio;
					String codec;
					String apiFramework;
					String url;
				}

				public static class VideoClicks {
					String clickThrough;
					List<String> clickTracking = new ArrayList<String>();
					List<String> customClicks = new ArrayList<String>();
				}

			}

			public static class CompanionAds {
				String required;
				List<Companion> companions = new ArrayList<Creative.CompanionAds.Companion>();

				public static class Companion {
					String id;
					int width;
					int height;
					int assetWidth;
					int assetHeight;
					int expandedWidth;
					int expandedHeight;
					String apiFramework;
					String adSlotID;
					StaticResource staticResource;
					String iframeResource;
					String htmlResource;
					String altText;
					String companionClickThrough;
					String companionClickTracking;
					List<Tracking> trackingEvents;
				}
			}

			public static class NonLinearAds {
				List<NonLinear> nonLinears = new ArrayList<NonLinear>();
				List<Tracking> trackingEvents = new ArrayList<Creative.Tracking>();

				public static class NonLinear {
					String id;
					int width;
					int height;
					int expandedWidth;
					int expandedHeight;
					boolean scalable;
					boolean maintainAspectRatio;
					String minSuggestedDuration;
					String apiFramework;
					StaticResource staticResource;
					String iframeResource;
					String htmlResource;
					String nonLinearClickThrough;
					String nonLinearClickTracking;
				}
			}

			public static class StaticResource {
				String type;
				String url;
			}

			public static class Tracking {
				String event;
				String progress;
				String url;
			}
		}
		
	}
}
