package com.adsdk.sdk.video;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class VideoData implements Serializable {

	private static final long serialVersionUID = 3402649540160829602L;
	public static final int DELIVERY_PROGRESSIVE = 0;
	public static final int DELIVERY_STREAMING = 1;

	public static final int OVERLAY_URL = 0;
	public static final int OVERLAY_MARKUP = 1;
	public static final int DISPLAY_FULLSCREEN = 0;
	public static final int DISPLAY_NORMAL = 1;

	private int sequence;
	int orientation;

	int delivery;

	int display;

	String type;
	int bitrate;
	int width;
	int height;
	int overlayWidth;
	int overlayHeight;
	String videoUrl;

	int duration;

	boolean showSkipButton;
	int showSkipButtonAfter;
	String skipButtonImage;

	boolean showNavigationBars;
	boolean allowTapNavigationBars;

	boolean showTopNavigationBar;
	String topNavigationBarBackground;

	boolean showBottomNavigationBar;
	String bottomNavigationBarBackground;
	boolean showPauseButton;
	boolean showReplayButton;
	boolean showTimer;
	String pauseButtonImage;
	String playButtonImage;
	String replayButtonImage;

	Vector<NavIconData> icons = new Vector<NavIconData>();

	HashMap<Integer, Vector<String>> timeTrackingEvents = new HashMap<Integer, Vector<String>>();
	Vector<String> startEvents = new Vector<String>();
	Vector<String> impressionEvents = new Vector<String>();
	Vector<String> completeEvents = new Vector<String>();
	Vector<String> muteEvents = new Vector<String>();
	Vector<String> unmuteEvents = new Vector<String>();
	Vector<String> pauseEvents = new Vector<String>();
	Vector<String> resumeEvents = new Vector<String>();
	Vector<String> skipEvents = new Vector<String>();
	Vector<String> replayEvents = new Vector<String>();

	boolean showHtmlOverlay = false;

	int showHtmlOverlayAfter;

	int htmlOverlayType;
	String htmlOverlayUrl;
	String htmlOverlayMarkup;
	String videoClickThrough;
	String overlayClickThrough;
	List<String>  videoClickTracking;
	String overlayClickTracking;

	@Override
	public String toString() {
		return "VideoData \n[\norientation=" + orientation + ",\ndisplay="
				+ display + ",\ndelivery=" + delivery + ",\ntype=" + type
				+ ",\nbitrate=" + bitrate + ",\nwidth=" + width + ",\nheight="
				+ height + ",\nvideoUrl=" + videoUrl + ",\nduration="
				+ duration + ",\nshowSkipButton=" + showSkipButton
				+ ",\nshowSkipButtonAfter=" + showSkipButtonAfter
				+ ",\nskipButtonImage=" + skipButtonImage
				+ ",\nshowNavigationBars=" + showNavigationBars
				+ ",\nallowTapNavigationBars=" + allowTapNavigationBars
				+ ",\nshowTopNavigationBar=" + showTopNavigationBar
				+ ",\ntopNavigationBarBackground=" + topNavigationBarBackground
				+ ",\nshowBottomNavigationBar=" + showBottomNavigationBar
				+ ",\nbottomNavigationBarBackground="
				+ bottomNavigationBarBackground + ",\nshowPauseButton="
				+ showPauseButton + ",\npauseButtonImage=" + pauseButtonImage
				+ ",\nplayButtonImage=" + playButtonImage
				+ ",\nshowReplayButton=" + showReplayButton
				+ ",\nreplayButtonImage=" + replayButtonImage + ",\nshowTimer="
				+ showTimer + ",\nicons=" + icons + ",\ntimeTrackingEvents="
				+ timeTrackingEvents + ",\nstartEvents=" + getStartEvents()
				+ ",\ncompleteEvents=" + getCompleteEvents() + ",\nmuteEvents="
				+ muteEvents + ",\nunmuteEvents=" + unmuteEvents
				+ ",\npauseEvents=" + pauseEvents + ",\nunpauseEvents="
				+ resumeEvents + ",\nskipEvents=" + skipEvents
				+ ",\nreplayEvents=" + replayEvents + ",\nshowHtmlOverlay="
				+ showHtmlOverlay + ",\nshowHtmlOverlayAfter="
				+ showHtmlOverlayAfter + ",\nhtmlOverlayType="
				+ htmlOverlayType + ",\nhtmlOverlayUrl=" + htmlOverlayUrl
				+ ",\nhtmlOverlayMarkup=" + htmlOverlayMarkup + "\n]";
	}

	public Vector<String> getCompleteEvents() {
		return completeEvents;
	}

	public void setCompleteEvents(Vector<String> completeEvents) {
		this.completeEvents = completeEvents;
	}

	public Vector<String> getStartEvents() {
		return startEvents;
	}

	public void setStartEvents(Vector<String> startEvents) {
		this.startEvents = startEvents;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
}
