package com.adsdk.sdk.video;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.adsdk.sdk.video.VAST.Ad;
import com.adsdk.sdk.video.VAST.Ad.AdSystem;
import com.adsdk.sdk.video.VAST.Ad.Creative;
import com.adsdk.sdk.video.VAST.Ad.Creative.CompanionAds;
import com.adsdk.sdk.video.VAST.Ad.Creative.CompanionAds.Companion;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear.MediaFile;
import com.adsdk.sdk.video.VAST.Ad.Creative.Linear.VideoClicks;
import com.adsdk.sdk.video.VAST.Ad.Creative.NonLinearAds;
import com.adsdk.sdk.video.VAST.Ad.Creative.NonLinearAds.NonLinear;
import com.adsdk.sdk.video.VAST.Ad.Creative.StaticResource;
import com.adsdk.sdk.video.VAST.Ad.Creative.Tracking;
import com.adsdk.sdk.video.VAST.Ad.Impression;
import com.adsdk.sdk.video.VAST.Ad.InLine;
import com.adsdk.sdk.video.VAST.Ad.Wrapper;


public class VASTParser {
	private static final String NAMESPACE = null;

	private static final Pattern PATTERN_DURATION = Pattern
			.compile("^([0-5]?\\d):([0-5]?\\d):([0-5]?\\d)(?:\\.(\\d?\\d?\\d))?$");
	private static final Pattern PATTERN_PERCENT = Pattern.compile("^(\\d?\\d?)%$");

	public static VAST createVastFromXml(String xml) throws XmlPullParserException, IOException {
		InputStream inStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

		return createVastFromXmlStream(inStream);
	}

	public static VAST createVastFromXmlStream(InputStream inStream) throws XmlPullParserException, IOException {
		VAST vast = null;

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inStream, null);
			parser.nextTag();
			vast = readVast(parser);
		} finally {
			inStream.close();
		}

		return vast;
	}

	public static VAST readVast(XmlPullParser parser) throws XmlPullParserException, IOException {
		VAST vast = new VAST();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "VAST");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			vast.version = parser.getAttributeValue(NAMESPACE, "version");

			String name = parser.getName();
			if (name.equals("Ad")) {
				vast.ads.add(readAd(parser));
			} else if (name.equals("Error")) {
				vast.error = readError(parser);
			} else {
				skip(parser);
			}
		}

		return vast;
	}

	public static Ad readAd(XmlPullParser parser) throws XmlPullParserException, IOException {
		Ad ad = new Ad();
		
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Ad");

		ad.id = parser.getAttributeValue(NAMESPACE, "id");

		String value = parser.getAttributeValue(NAMESPACE, "sequence");
		if (value != null) {
			ad.sequence = Integer.parseInt(value);
		}

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("InLine")) {
				ad.inLine = readInline(parser);
			} else if(name.equals("Wrapper")){
				ad.wrapper = readWrapper(parser);
			}else {
				skip(parser);
			}
		}

		return ad;
	}
	
	public static Wrapper readWrapper(XmlPullParser parser) throws XmlPullParserException, IOException {
		Wrapper wrapper = new Wrapper();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Wrapper");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("AdSystem")) {
				wrapper.adSystem = readAdSystem(parser);
			} else if (name.equals("VASTAdTagURI")) {
				wrapper.VASTAdTagUri = readVASTAdTagUri(parser);
			} else if (name.equals("Impression")) {
				wrapper.impressions.add(readImpression(parser));
			} else if (name.equals("Creatives")) {
				wrapper.creatives = readCreatives(parser);
			} else if (name.equals("Error")) {
				wrapper.error = readError(parser);
			} else {
				skip(parser);
			}
		}

		return wrapper;
	}

	public static InLine readInline(XmlPullParser parser) throws XmlPullParserException, IOException {
		InLine inline = new InLine();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "InLine");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("AdSystem")) {
				inline.adSystem = readAdSystem(parser);
			} else if (name.equals("AdTitle")) {
				inline.adTitle = readAdTitle(parser);
			} else if (name.equals("Description")) {
				inline.description = readDescription(parser);
			} else if (name.equals("Impression")) {
				inline.impressions.add(readImpression(parser));
			} else if (name.equals("Creatives")) {
				inline.creatives = readCreatives(parser);
			} else if (name.equals("Advertiser")) {
				inline.advertiser = readAdvertiser(parser);
			} else if (name.equals("Error")) {
				inline.error = readError(parser);
			} else {
				skip(parser);
			}
		}

		return inline;
	}

	public static List<Creative> readCreatives(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<Creative> list = new ArrayList<Creative>();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Creatives");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("Creative")) {
				list.add(readCreative(parser));
			} else {
				skip(parser);
			}
		}

		return list;
	}

	public static Creative readCreative(XmlPullParser parser) throws IOException, XmlPullParserException {
		Creative creative = new Creative();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Creative");

		creative.adId = parser.getAttributeValue(NAMESPACE, "adID");
		creative.id = parser.getAttributeValue(NAMESPACE, "id");
		creative.apiFramework = parser.getAttributeValue(NAMESPACE, "apiFramework");

		String value = parser.getAttributeValue(NAMESPACE, "sequence");
		if (value != null) {
			creative.sequence = Integer.parseInt(value);
		}

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("Linear")) {
				creative.linear = readLinear(parser);
			} else if (name.equals("NonLinearAds")) {
				creative.nonLinearAds = readNonLinearAds(parser);
			} else if (name.equals("CompanionAds")) {
				creative.companionAds = readCompanionAds(parser);
			} else {
				skip(parser);
			}
		}

		return creative;
	}

	public static CompanionAds readCompanionAds(XmlPullParser parser) throws IOException, XmlPullParserException {
		CompanionAds companionAds = new CompanionAds();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "CompanionAds");

		companionAds.required = parser.getAttributeValue(NAMESPACE, "required");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("Companion")) {
				companionAds.companions.add(readCompanion(parser));
			} else {
				skip(parser);
			}
		}

		return companionAds;
	}

	public static AdSystem readAdSystem(XmlPullParser parser) throws IOException, XmlPullParserException {
		AdSystem adSystem = new AdSystem();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "AdSystem");

		adSystem.version = parser.getAttributeValue(NAMESPACE, "version");
		adSystem.name = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "AdSystem");

		return adSystem;
	}

	public static Impression readImpression(XmlPullParser parser) throws IOException, XmlPullParserException {
		Impression impression = new Impression();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Impression");

		impression.id = parser.getAttributeValue(NAMESPACE, "id");
		impression.url = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Impression");

		return impression;
	}

	public static Companion readCompanion(XmlPullParser parser) throws IOException, XmlPullParserException {
		Companion companion = new Companion();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Companion");

		String value = parser.getAttributeValue(NAMESPACE, "height");
		if (value != null) {
			companion.height = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "width");
		if (value != null) {
			companion.width = Integer.parseInt(value);
		}

		companion.id = parser.getAttributeValue(NAMESPACE, "id");

		value = parser.getAttributeValue(NAMESPACE, "assetWidth");
		if (value != null) {
			companion.assetWidth = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "assetHeight");
		if (value != null) {
			companion.assetHeight = Integer.parseInt(value);
		}

		companion.apiFramework = parser.getAttributeValue(NAMESPACE, "apiFramework");
		companion.adSlotID = parser.getAttributeValue(NAMESPACE, "adSlotID");

		value = parser.getAttributeValue(NAMESPACE, "expandedWidth");
		if (value != null) {
			companion.expandedWidth = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "expandedHeight");
		if (value != null) {
			companion.expandedHeight = Integer.parseInt(value);
		}

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("StaticResource")) {
				companion.staticResource = readStaticResource(parser);
			} else if (name.equals("HTMLResource")) {
				companion.htmlResource = readHTMLResource(parser);
			} else if (name.equals("IFrameResource")) {
				companion.iframeResource = readIFrameResource(parser);
			} else if (name.equals("CompanionClickThrough")) {
				companion.companionClickThrough = readCompanionClickThrough(parser);
			} else if (name.equals("CompanionClickTracking")) {
				companion.companionClickTracking = readCompanionClickTracking(parser);
			} else if (name.equals("AltText")) {
				companion.altText = readAltText(parser);
			} else if (name.equals("TrackingEvents")) {
				companion.trackingEvents = readTrackingEvents(parser);
			} else {
				skip(parser);
			}
		}

		return companion;
	}

	public static List<Tracking> readTrackingEvents(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<Tracking> list = new ArrayList<Tracking>();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "TrackingEvents");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("Tracking")) {
				list.add(readTracking(parser));
			} else {
				skip(parser);
			}
		}

		return list;
	}

	public static Linear readLinear(XmlPullParser parser) throws IOException, XmlPullParserException {
		Linear linear = new Linear();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Linear");

		String skipoffsetString = parser.getAttributeValue(NAMESPACE, "skipoffset");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("Duration")) {
				linear.duration = readDuration(parser);
			} else if (name.equals("MediaFiles")) {
				linear.mediaFiles = readMediaFiles(parser);
			} else if (name.equals("VideoClicks")) {
				linear.videoClicks = readVideoClicks(parser);
			} else if (name.equals("TrackingEvents")) {
				linear.trackingEvents = readTrackingEvents(parser);
			} else {
				skip(parser);
			}
		}

		linear.skipoffset = parseSkipoffset(parser, skipoffsetString, linear.duration);

		return linear;
	}

	public static List<MediaFile> readMediaFiles(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<MediaFile> list = new ArrayList<MediaFile>();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "MediaFiles");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("MediaFile")) {
				list.add(readMediaFile(parser));
			} else {
				skip(parser);
			}
		}

		return list;
	}

	public static NonLinearAds readNonLinearAds(XmlPullParser parser) throws IOException, XmlPullParserException {
		NonLinearAds nonLinearAds = new NonLinearAds();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "NonLinearAds");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("NonLinear")) {
				nonLinearAds.nonLinears.add(readNonLinear(parser));
			} else if (name.equals("TrackingEvents")) {
				nonLinearAds.trackingEvents = readTrackingEvents(parser);
			} else {
				skip(parser);
			}
		}

		return nonLinearAds;
	}

	public static NonLinear readNonLinear(XmlPullParser parser) throws IOException, XmlPullParserException {
		NonLinear nonLinear = new NonLinear();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "NonLinear");

		String value = parser.getAttributeValue(NAMESPACE, "height");
		if (value != null) {
			nonLinear.height = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "width");
		if (value != null) {
			nonLinear.width = Integer.parseInt(value);
		}

		nonLinear.id = parser.getAttributeValue(NAMESPACE, "id");
		nonLinear.scalable = Boolean.parseBoolean(parser.getAttributeValue(NAMESPACE, "scalable"));
		nonLinear.maintainAspectRatio = Boolean
				.parseBoolean(parser.getAttributeValue(NAMESPACE, "maintainAspectRatio"));
		nonLinear.apiFramework = parser.getAttributeValue(NAMESPACE, "apiFramework");
		nonLinear.minSuggestedDuration = parser.getAttributeValue(NAMESPACE, "minSuggestedDuration");

		value = parser.getAttributeValue(NAMESPACE, "expandedWidth");
		if (value != null) {
			nonLinear.expandedWidth = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "expandedHeight");
		if (value != null) {
			nonLinear.expandedHeight = Integer.parseInt(value);
		}

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			if (name.equals("StaticResource")) {
				nonLinear.staticResource = readStaticResource(parser);
			} else if (name.equals("HTMLResource")) {
				nonLinear.htmlResource = readHTMLResource(parser);
			} else if (name.equals("IFrameResource")) {
				nonLinear.iframeResource = readIFrameResource(parser);
			} else if (name.equals("NonLinearClickThrough")) {
				nonLinear.nonLinearClickThrough = readNonLinearClickThrough(parser);
			} else if (name.equals("NonLinearClickTracking")) {
				nonLinear.nonLinearClickTracking = readNonLinearClickTracking(parser);
			} else {
				skip(parser);
			}
		}

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "NonLinear");

		return nonLinear;
	}

	public static StaticResource readStaticResource(XmlPullParser parser) throws IOException, XmlPullParserException {
		StaticResource resource = new StaticResource();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "StaticResource");

		resource.type = parser.getAttributeValue(NAMESPACE, "type");
		resource.url = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "StaticResource");

		return resource;
	}

	public static VideoClicks readVideoClicks(XmlPullParser parser) throws IOException, XmlPullParserException {
		VideoClicks videoClicks = new VideoClicks();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "VideoClicks");

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();

			if (name.equals("ClickThrough")) {
				videoClicks.clickThrough = readClickThrough(parser);
			} else if (name.equals("ClickTracking")) {
				videoClicks.clickTracking.add(readClickTracking(parser));
			} else if (name.equals("CustomClick")) {
				videoClicks.customClicks.add(readCustomClick(parser));
			} else {
				skip(parser);
			}
		}

		return videoClicks;
	}

	public static Tracking readTracking(XmlPullParser parser) throws IOException, XmlPullParserException {
		Tracking tracking = new Tracking();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Tracking");

		tracking.event = parser.getAttributeValue(NAMESPACE, "event");
		tracking.url = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Tracking");

		return tracking;
	}

	public static MediaFile readMediaFile(XmlPullParser parser) throws IOException, XmlPullParserException {
		MediaFile mediaFile = new MediaFile();

		parser.require(XmlPullParser.START_TAG, NAMESPACE, "MediaFile");

		mediaFile.delivery = parser.getAttributeValue(NAMESPACE, "delivery");
		mediaFile.type = parser.getAttributeValue(NAMESPACE, "type");
		mediaFile.codec = parser.getAttributeValue(NAMESPACE, "codec");
		mediaFile.id = parser.getAttributeValue(NAMESPACE, "id");
		mediaFile.bitrate = parser.getAttributeValue(NAMESPACE, "bitrate");
		mediaFile.scalable = Boolean.parseBoolean(parser.getAttributeValue(NAMESPACE, "scalable"));
		mediaFile.maintainAspectRatio = Boolean
				.parseBoolean(parser.getAttributeValue(NAMESPACE, "maintainAspectRatio"));
		mediaFile.apiFramework = parser.getAttributeValue(NAMESPACE, "apiFramework");

		String value = parser.getAttributeValue(NAMESPACE, "height");
		if (value != null) {
			mediaFile.height = Integer.parseInt(value);
		}

		value = parser.getAttributeValue(NAMESPACE, "width");
		if (value != null) {
			mediaFile.width = Integer.parseInt(value);
		}

		mediaFile.url = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "MediaFile");

		return mediaFile;
	}

	public static String readSkipoffset(XmlPullParser parser) {
		String skipoffsetString = parser.getAttributeValue(NAMESPACE, "skipoffset");

		return skipoffsetString;
	}

	public static int readDuration(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Duration");

		String durationString = readText(parser);
		int duration = parseDuration(parser, durationString);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Duration");

		return duration;
	}

	public static String readAltText(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "AltText");

		String altText = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "AltText");

		return altText;
	}

	public static String readHTMLResource(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "HTMLResource");

		String htmlResource = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "HTMLResource");

		return htmlResource;
	}

	public static String readIFrameResource(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "IFrameResource");

		String resource = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "IFrameResource");

		return resource;
	}

	public static String readNonLinearClickThrough(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "NonLinearClickThrough");

		String clickThrough = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "NonLinearClickThrough");

		return clickThrough;
	}

	public static String readNonLinearClickTracking(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "NonLinearClickTracking");

		String clickTracking = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "NonLinearClickTracking");

		return clickTracking;
	}

	public static String readCompanionClickThrough(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "CompanionClickThrough");

		String clickThrough = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "CompanionClickThrough");

		return clickThrough;
	}

	public static String readCompanionClickTracking(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "CompanionClickTracking");

		String clickTracking = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "CompanionClickTracking");

		return clickTracking;
	}

	public static String readClickTracking(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "ClickTracking");

		String text = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "ClickTracking");

		return text;
	}

	public static String readCustomClick(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "CustomClick");

		String text = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "CustomClick");

		return text;
	}

	public static String readClickThrough(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "ClickThrough");

		String clickThrough = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "ClickThrough");

		return clickThrough;
	}

	public static String readAdTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "AdTitle");

		String title = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "AdTitle");

		return title;
	}

	public static String readAdvertiser(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Advertiser");

		String advertiser = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Advertiser");

		return advertiser;
	}

	public static String readError(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Error");

		String error = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Error");

		return error;
	}
	
	public static String readVASTAdTagUri(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "VASTAdTagURI");

		String uri = readUrl(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "VASTAdTagURI");

		return uri;
	}

	public static String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Description");

		String description = readText(parser);

		parser.require(XmlPullParser.END_TAG, NAMESPACE, "Description");

		return description;
	}

	public static String readUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
		String urlValue = readText(parser);
		return urlValue.trim();
	}

	public static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";

		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}

		return result;
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}

		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	private static int parseDuration(XmlPullParser parser, String durationString) throws XmlPullParserException {
		int duration;

		Matcher matcher = PATTERN_DURATION.matcher(durationString);
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
				throw new XmlPullParserException("Failed to parse duration: " + durationString, parser, e);
			}
		} else {
			throw new XmlPullParserException("Failed to parse duration: " + durationString, parser, null);
		}

		return duration;
	}

	private static int parseSkipoffset(XmlPullParser parser, String skipoffsetString, int duration)
			throws XmlPullParserException {
		int skipoffset;

		if (skipoffsetString != null) {
			Matcher durationMatcher = PATTERN_DURATION.matcher(skipoffsetString);
			Matcher percentMatcher = PATTERN_PERCENT.matcher(skipoffsetString);

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
					throw new XmlPullParserException("Failed to parse skip offset: " + skipoffsetString, parser, e);
				}
			} else if (percentMatcher.find() && percentMatcher.groupCount() == 1) {
				try {
					int percent = Integer.parseInt(percentMatcher.group(1));

					skipoffset = (int) ((long) percent * (long) duration / 100L);
				} catch (NumberFormatException e) {
					throw new XmlPullParserException("Failed to parse skip offset: " + skipoffsetString, parser, e);
				}
			} else {
				throw new XmlPullParserException("Failed to parse skip offset: " + skipoffsetString, parser, null);
			}
		} else {
			skipoffset = 0;
		}

		return skipoffset;
	}
}
