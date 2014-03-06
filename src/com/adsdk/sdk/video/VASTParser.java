package com.adsdk.sdk.video;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.video.VAST.Ad;
import com.adsdk.sdk.video.VAST.Ad.Creative;
import com.adsdk.sdk.video.VAST.Ad.Creative.CompanionAds.Companion;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear.MediaFile;
import com.adsdk.sdk.video.VAST.Ad.Creative.Tracking;

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

	public static VideoData fillVideoDataFromVast(VAST vast) {
		VideoData video = new VideoData();
		Creative creative = null;
		MediaFile mediaFile = null;
		for (Ad ad : vast.ads) {
			if (ad.inLine == null) {
				continue;
			}
			for (Creative c : ad.inLine.creatives) {
				if (c.linear != null && c.linear.mediaFiles != null && !c.linear.mediaFiles.isEmpty()) {
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
		video.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
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
		if (creative.linear.skipoffset != null) {
			video.showSkipButton = true;
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

		for (Tracking t : creative.linear.trackingEvents) {
			String name = t.event;
			if (name.equals("start")) {
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
			} else if (name.equals("skip")) {
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

		//
		// HashMap<Integer, Vector<String>> timeTrackingEvents = new
		// HashMap<Integer, Vector<String>>();
		// Vector<String> replayEvents = new Vector<String>(); //TODO: will be
		// used?
		//
		// boolean showHtmlOverlay = false;
		//
		// int showHtmlOverlayAfter;
		//
		// int htmlOverlayType;
		// String htmlOverlayUrl;
		// String htmlOverlayMarkup;

		return video;

	}

	public static InterstitialData fillInterstitialDataFromVast(VAST vast) {
		InterstitialData interstitial = new InterstitialData();

		Creative creative = null;
		Companion companion = null;
		for (Ad ad : vast.ads) {
			if (ad.inLine == null) {
				continue;
			}
			for (Creative c : ad.inLine.creatives) {
				if (c.companionAds != null && !c.companionAds.companions.isEmpty()) {
					creative = c;
					companion = c.companionAds.companions.get(0);
					break;
				}
			}
			if (creative != null) {
				break;
			}
		}
		if (companion == null) {
			return null;
		}

		interstitial.setSequence(creative.sequence);
		if (companion.staticResource != null) {
			interstitial.interstitialType = InterstitialData.INTERSTITIAL_MARKUP;
			if (companion.staticResource.type.contains("image")) {
				interstitial.interstitialMarkup = "<img src=\"" + companion.staticResource.url + "\">";
			} else if (companion.staticResource.type.contains("x-javascript")) {
				interstitial.interstitialMarkup = "<script src=\"" + companion.staticResource.url + "\"></script>";
			}
		} else if (companion.iframeResource != null) {
			interstitial.interstitialType = InterstitialData.INTERSTITIAL_URL;
			interstitial.interstitialUrl = companion.iframeResource;
		} else if (companion.htmlResource != null) {
			interstitial.interstitialType = InterstitialData.INTERSTITIAL_MARKUP;
			interstitial.interstitialMarkup = companion.htmlResource;
		}

		interstitial.showSkipButton = true;
		interstitial.showSkipButtonAfter = 1;

		// int autoclose;
		// int orientation;
		// String skipButtonImage;
		//
		// boolean showNavigationBars;
		// boolean allowTapNavigationBars;
		// boolean showTopNavigationBar;
		// String topNavigationBarBackground;
		// int topNavigationBarTitleType;
		// String topNavigationBarTitle;
		// boolean showBottomNavigationBar;
		// String bottomNavigationBarBackground;
		// boolean showBackButton;
		// boolean showForwardButton;
		// boolean showReloadButton;
		// boolean showExternalButton;
		// boolean showTimer;
		// String backButtonImage;
		// String forwardButtonImage;
		// String reloadButtonImage;
		// String externalButtonImage;

		return interstitial;
	}

	public static int getDurationFromString(String time) {
		int duration = 0;

		Matcher matcher = DURATION_PATTERN.matcher(time);
		if (matcher.find() && matcher.groupCount() == 4) {
			try {
				int hours = Integer.parseInt(matcher.group(1));
				int minutes = Integer.parseInt(matcher.group(2));
				int seconds = Integer.parseInt(matcher.group(3));
				int millis = 0;
				String millisString = matcher.group(4);
				if (millisString != null) {
					millis = Integer.parseInt(millisString);
				}

				duration = millis + 1000 * seconds + 60 * 1000 * minutes + 3600 * 1000 * hours;
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
					int millis = 0;
					String millisString = durationMatcher.group(4);
					if (millisString != null) {
						millis = Integer.parseInt(millisString);
					}

					skipoffset = millis + 1000 * seconds + 60 * 1000 * minutes + 3600 * 1000 * hours;
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
