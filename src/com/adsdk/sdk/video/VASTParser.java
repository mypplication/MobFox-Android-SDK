package com.adsdk.sdk.video;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.video.VAST.Ad;
import com.adsdk.sdk.video.VAST.Ad.Creative;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear.ClickTracking;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear.MediaFile;
import com.adsdk.sdk.video.VAST.Ad.Creative.NonLinearAds;
import com.adsdk.sdk.video.VAST.Ad.Creative.NonLinearAds.NonLinear;
import com.adsdk.sdk.video.VAST.Ad.Creative.Tracking;
import com.adsdk.sdk.video.VAST.Ad.Impression;

public class VASTParser {

	private static final Pattern DURATION_PATTERN = Pattern.compile("^([0-5]?\\d):([0-5]?\\d):([0-5]?\\d)(?:\\.(\\d?\\d?\\d))?$");
	private static final Pattern PERCENT_PATTERN = Pattern.compile("^(\\d?\\d?)%$");

	public static VAST createVastFromString(String string) {
		VAST vast = null;
		Serializer serial = new Persister();

		try {
			vast = serial.read(VAST.class, string);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vast;
	}
	
	public static VAST createVastFromStream(InputStream inputStream) {
		VAST vast = null;
		Serializer serial = new Persister();

		try {
			vast = serial.read(VAST.class, inputStream);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vast;
	}
	

	public static VideoData fillVideoDataFromVast(VAST vast) {
		if(vast == null) {
			return null;
		}
		
		VideoData video = new VideoData();
		Creative creative = null;
		Ad vastAd = null;
		MediaFile mediaFile = null;
		for (Ad ad : vast.ads) {
			if (ad.inLine == null) {
				continue;
			}
			for (Creative c : ad.inLine.creatives) {
				if (c.linear != null && c.linear.mediaFiles != null && !c.linear.mediaFiles.isEmpty()) {
					vastAd = ad;
					creative = c;
					mediaFile = c.linear.mediaFiles.get(0);
					break;
				}
			}
			if (creative != null) {
				break;
			}
		}
		if (mediaFile == null) {
			return null;
		}
		video.setSequence(creative.sequence);
//		video.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		video.orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		
		if (mediaFile.delivery != null && mediaFile.delivery.contains("streaming")) {
			video.delivery = VideoData.DELIVERY_STREAMING;
		} else {
			video.delivery = VideoData.DELIVERY_PROGRESSIVE;
		}
		video.display = VideoData.DISPLAY_FULLSCREEN;
		video.type = mediaFile.type;
		if (mediaFile.bitrate != null) {
			video.bitrate = Integer.parseInt(mediaFile.bitrate);
		}
		video.width = mediaFile.width;
		video.height = mediaFile.height;
		video.videoUrl = mediaFile.url;
		if (creative.linear.duration != null) {
			video.duration = getDurationFromString(creative.linear.duration);
		}
		video.showSkipButton = true;
		if (creative.linear.skipoffset != null) {
			video.showSkipButtonAfter = getSkipoffsetFromString(creative.linear.skipoffset, video.duration);
		}

		// video.showNavigationBars =
		// video.allowTapNavigationBars =
		// video.showTopNavigationBar =
		// video.topNavigationBarBackground =
		// boolean showBottomNavigationBar;
		// String bottomNavigationBarBackground;
		// boolean showPauseButton;
		// boolean showReplayButton;
		// boolean showTimer;
		// String pauseButtonImage;
		// String playButtonImage;
		// String replayButtonImage;
		// Vector<NavIconData> icons = new Vector<NavIconData>();

		getTrackingEvents(video, creative.linear.trackingEvents);
		
		for(Impression i : vastAd.inLine.impressions) {
			video.impressionEvents.add(i.url);
		}

		if (creative.linear.videoClicks != null && creative.linear.videoClicks.clickThrough != null) {
			if(creative.linear.videoClicks.clickTracking != null) {
				video.videoClickTracking = new ArrayList<String>();
				for(ClickTracking t : creative.linear.videoClicks.clickTracking) {
					video.videoClickTracking.add(t.url);
				}
			}
			video.videoClickThrough = creative.linear.videoClicks.clickThrough;
		}

		NonLinearAds nonLinearAds = null;
		NonLinear nonLinear = null;
		for (Creative c : vastAd.inLine.creatives) {
			if (c.nonLinearAds != null && c.nonLinearAds.nonLinears != null && !c.nonLinearAds.nonLinears.isEmpty()) {
				nonLinear = c.nonLinearAds.nonLinears.get(0);
				nonLinearAds = c.nonLinearAds;
				break;
			}
		}

		if (nonLinear != null) {
			video.overlayClickThrough = nonLinear.nonLinearClickThrough;
			video.overlayClickTracking = nonLinear.nonLinearClickTracking.trim();
			video.overlayHeight = nonLinear.height;
			video.overlayWidth = nonLinear.width;
			video.showHtmlOverlayAfter = 0;
			video.showHtmlOverlay = true;
			if (nonLinear.staticResource != null) {
				video.htmlOverlayType = VideoData.OVERLAY_MARKUP;
				if (nonLinear.staticResource.type.contains("image")) {
					String text = MessageFormat.format(Const.IMAGE_BODY, nonLinear.staticResource.url.trim(), nonLinear.width, nonLinear.height);
					text = Const.HIDE_BORDER +text;
					video.htmlOverlayMarkup = text;
				} else if (nonLinear.staticResource.type.contains("x-javascript")) {
					video.htmlOverlayMarkup = "<script src=\"" + nonLinear.staticResource.url.trim() + "\"></script>";
				}
			} else if (nonLinear.iframeResource != null) {
				video.htmlOverlayType = VideoData.OVERLAY_URL;
				video.htmlOverlayUrl = nonLinear.iframeResource;
			} else if (nonLinear.htmlResource != null) {
				video.htmlOverlayType = VideoData.OVERLAY_MARKUP;
				video.htmlOverlayMarkup = nonLinear.htmlResource;
			}
			
			getTrackingEvents(video, nonLinearAds.trackingEvents);
			
		}

		return video;

	}
	
	public static void getTrackingEvents (VideoData video, List<Tracking> events) {
		for (Tracking t : events) {
			String name = t.event;
			if (name.equals("start") || name.equals("creativeView")) { //assumption is we have only one video creative
				video.startEvents.add(t.url);
			} else if (name.equals("complete")) {
				video.completeEvents.add(t.url);
			} else if (name.equals("mute")) {
				video.muteEvents.add(t.url);
			} else if (name.equals("unmute")) {
				video.unmuteEvents.add(t.url);
			} else if (name.equals("pause")) {
				video.pauseEvents.add(t.url);
			} else if (name.equals("resume")) {
				video.resumeEvents.add(t.url);
			} else if (name.equals("skip") || name.equals("close")) {
				video.skipEvents.add(t.url);
			} else if (name.equals("firstQuartile")) {
				int time = video.duration / 4;
				Vector<String> trackers = video.timeTrackingEvents.get(time);
				if (trackers == null) {
					trackers = new Vector<String>();
					video.timeTrackingEvents.put(time, trackers);
				}
				trackers.add(t.url);
			} else if (name.equals("midpoint")) {
				int time = video.duration / 2;
				Vector<String> trackers = video.timeTrackingEvents.get(time);
				if (trackers == null) {
					trackers = new Vector<String>();
					video.timeTrackingEvents.put(time, trackers);
				}
				trackers.add(t.url);
			} else if (name.equals("thirdQuartile")) {
				int time = 3 * video.duration / 4;
				Vector<String> trackers = video.timeTrackingEvents.get(time);
				if (trackers == null) {
					trackers = new Vector<String>();
					video.timeTrackingEvents.put(time, trackers);
				}
				trackers.add(t.url);
			}
		}
	}

	public static int getDurationFromString(String time) {
		int duration = 0;

		Matcher matcher = DURATION_PATTERN.matcher(time);
		if (matcher.find() && matcher.groupCount() == 4) {
			try {
				int hours = Integer.parseInt(matcher.group(1));
				int minutes = Integer.parseInt(matcher.group(2));
				int seconds = Integer.parseInt(matcher.group(3));
				duration = seconds + 60 * minutes + 3600 * hours;
			} catch (NumberFormatException e) {
				Log.e("Failed to parse duration: " + time);
			}
		} else {
			Log.e("Failed to parse duration: " + time);
		}

		return duration;
	}

	private static int getSkipoffsetFromString(String skipoffsetString, int duration) {
		int skipoffset = 0;

		if (skipoffsetString != null) {
			Matcher durationMatcher = DURATION_PATTERN.matcher(skipoffsetString);
			Matcher percentMatcher = PERCENT_PATTERN.matcher(skipoffsetString);

			if (durationMatcher.find() && durationMatcher.groupCount() == 4) {
				try {
					int hours = Integer.parseInt(durationMatcher.group(1));
					int minutes = Integer.parseInt(durationMatcher.group(2));
					int seconds = Integer.parseInt(durationMatcher.group(3));

					skipoffset = seconds + 60 * minutes + 3600 * hours;
				} catch (NumberFormatException e) {
					Log.e("Failed to parse skipoffset: " + skipoffsetString);
				}
			} else if (percentMatcher.find() && percentMatcher.groupCount() == 1) {
				try {
					int percent = Integer.parseInt(percentMatcher.group(1));
					skipoffset = (int) ((long) percent * (long) duration / 100L);
				} catch (NumberFormatException e) {
					Log.e("Failed to parse skipoffset: " + skipoffsetString);
				}
			}
		}
		return skipoffset;
	}

}
