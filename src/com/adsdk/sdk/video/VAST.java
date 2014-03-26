package com.adsdk.sdk.video;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "VAST")
public class VAST {
	
	@Attribute(name = "version")
	public String version;
	
	@ElementList(inline=true)
	public List<Ad> ads;
	
	@Element(required=false, name="Error")
	public String error;

	@Root(name = "Ad")
	public static class Ad {
		@Attribute(required = false, name = "id")
		String id;
		
		@Attribute(required = false, name = "sequence")
		int sequence;
		
		@Element(required = false, name = "InLine")
		InLine inLine;
		
		@Element(required = false, name = "Wrapper")
		Wrapper wrapper;
		
		@Root
		public static class AdSystem {
			@Attribute(required = false, name = "version")
			String version;
			
			@Text
			String name;
		}
		
		@Root(name = "Wrapper")
		public static class Wrapper {
			@Element(name = "AdSystem")
			AdSystem adSystem;
			
			@ElementList(inline=true)
			List<Impression> impressions;
			
			@Element(name = "VASTAdTagURI")
			String VASTAdTagUri;
			
			@Element(required = false, name = "Error")
			String error;
			
			@ElementList(required = false, name = "Creatives")
			List<Creative> creatives;
		}
		
		@Root(name = "Impression")
		public static class Impression {
			@Attribute(required = false, name = "id")
			String id;
			
			@Text
			String url;
		}

		@Root (name = "InLine")
		public static class InLine {
			@Element (name = "AdSystem")
			AdSystem adSystem;
			
			@Element (name = "AdTitle")
			String adTitle;
			
			@Element (required = false, name = "Description")
			String description;
			
			@Element (required = false, name = "Advertiser")
			String advertiser;
			
			@Element (required = false, name = "Error")
			String error;
			
			@ElementList(inline = true)
			List<Impression> impressions;
			
			@ElementList(name = "Creatives")
			List<Creative> creatives;

		}

		@Root (name = "Creative")
		public static class Creative {
			@Attribute(required = false, name = "id")
			String id;
			
			@Attribute(required = false, name = "sequence")
			int sequence;
			
			@Attribute(required = false, name = "AdID")
			String adId;
			
			@Attribute(required = false, name = "apiFramework")
			String apiFramework;
			
			@Element(required = false, name = "Linear")
			Linear linear;
			
			@Element(required = false, name = "CompanionAds")
			CompanionAds companionAds;
			
			@Element(required = false, name = "NonLinearAds")
			NonLinearAds nonLinearAds;

			@Root (name = "Linear")
			public static class Linear {
				@Attribute(required = false, name = "skipoffset")
				String skipoffset;
				
				@Element(name = "Duration")
				String duration;
				
				@ElementList(name = "MediaFiles")
				List<MediaFile> mediaFiles;
				
				@ElementList(required = false, name = "TrackingEvents")
				List<Tracking> trackingEvents; 
				
				@Element(required = false, name = "VideoClicks")
				VideoClicks videoClicks;

				@Root(name = "MediaFile")
				public static class MediaFile {
					@Attribute(required = false, name = "id")
					String id;
					
					@Attribute(name = "delivery")
					String delivery;
					@Attribute(name = "type")
					String type;
					@Attribute(required = false, name = "bitrate")
					String bitrate;
					@Attribute(name = "width")
					int width;
					@Attribute(name = "height")
					int height;
					@Attribute(required = false, name = "scalable")
					boolean scalable;
					@Attribute(required = false, name = "maintainAspectRatio")
					boolean maintainAspectRatio;
					@Attribute(required = false, name = "codec")
					String codec;
					@Attribute(required = false, name = "apiFramework")
					String apiFramework;
					@Text
					String url;
				}

				@Root(name = "VideoClicks")
				public static class VideoClicks {
					@Element(required = false, name = "ClickThrough")
					String clickThrough;
					@ElementList(required = false, inline = true)
					List<ClickTracking> clickTracking;
					
					@ElementList(required = false, inline = true)
					List<CustomClick> customClicks;
				}
				
				@Root(name = "ClickTracking")
				public static class ClickTracking{
					@Text
					String url;
				}
				
				@Root(name = "CustomClick")
				public static class CustomClick{
					@Text
					String url;
				}
			}
			
			@Root(name = "CompanionAds")
			public static class CompanionAds {
				@Attribute(required = false, name = "required")
				String required;
				
				@ElementList(required = false, inline = true)
				List<Companion> companions;

				@Root(name = "Companion")
				public static class Companion {
					@Attribute(required = false, name = "id")
					String id;
					@Attribute(name = "width")
					int width;
					@Attribute(name = "height")
					int height;
					@Attribute(required = false, name = "assetWidth")
					int assetWidth;
					@Attribute(required = false, name = "assetHeight")
					int assetHeight;
					@Attribute(required = false, name = "expandedWidth")
					int expandedWidth;
					@Attribute(required = false, name = "expandedHeight")
					int expandedHeight;
					@Attribute(required = false, name = "apiFramework")
					String apiFramework;
					@Attribute(required = false, name = "adSlotID")
					String adSlotID;
					@Element(required = false, name = "StaticResource")
					StaticResource staticResource;
					@Element(required = false, name = "IFrameResource")
					String iframeResource;
					@Element(required = false, name = "HTMLResource")
					String htmlResource;
					@Element(required = false, name = "AltText")
					String altText;
					@Element(required = false, name = "CompanionClickThrough")
					String companionClickThrough;
					@Element(required = false, name = "CompanionClickTracking")
					String companionClickTracking;
					@ElementList(required = false, name = "TrackingEvents")
					List<Tracking> trackingEvents;
				}
			}

			@Root(name = "NonLinearAds")
			public static class NonLinearAds {
				@ElementList(required = false, inline = true)
				List<NonLinear> nonLinears;
				
				@ElementList(required = false, name = "TrackingEvents")
				List<Tracking> trackingEvents;

				@Root (name = "NonLinear")
				public static class NonLinear {
					@Attribute(required = false, name = "id")
					String id;
					@Attribute(name = "width")
					int width;
					@Attribute(name = "height")
					int height;
					@Attribute(required = false, name = "expandedWidth")
					int expandedWidth;
					@Attribute(required = false, name = "expandedHeight")
					int expandedHeight;
					@Attribute(required = false, name = "scalable")
					boolean scalable;
					@Attribute(required = false, name = "maintainAspectRatio")
					boolean maintainAspectRatio;
					@Attribute(required = false, name = "minSuggestedDuration")
					String minSuggestedDuration;
					@Attribute(required = false, name = "apiFramework")
					String apiFramework;
					@Element(required = false, name = "StaticResource")
					StaticResource staticResource;
					@Element(required = false, name = "IFrameResource")
					String iframeResource;
					@Element(required = false, name = "HTMLResource")
					String htmlResource;
					@Element(required = false, name = "NonLinearClickThrough")
					String nonLinearClickThrough;
					@Element(required = false, name = "NonLinearClickTracking")
					String nonLinearClickTracking;
				}
			}

			@Root (name = "StaticResource")
			public static class StaticResource {
				@Attribute(required =false, name = "creativeType")
				String type;
				
				@Text
				String url;
			}

			public static class Tracking {
				@Attribute(name = "event")
				String event;
				@Text
				String url;
			}
		}
		
	}
}
