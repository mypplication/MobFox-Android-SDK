package com.adsdk.sdk.nativeads;

import java.util.List;
import java.util.Random;

import android.net.Uri;
import android.text.TextUtils;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;

public class NativeAdRequest {
	private static final String REQUEST_TYPE = "native";
	private static final String RESPONSE_TYPE = "json";
	private static final String IMAGE_TYPES = "icon,main";
	private static final String TEXT_TYPES = "headline,description,cta,advertiser,rating";
	private static final String REQUEST_TYPE_ANDROID = "android_app";
	private  String request_url;
	private List<String> adTypes;
	private String publisherId;
	private String userAgent;
	private String userAgent2;
	private String androidIMEI = "";
	private String androidID = "";
	private String androidAdId = "";
	private String protocolVersion;

	private double longitude = 0.0;
	private double latitude = 0.0;

	private Gender gender;
	private int userAge;
	private List<String> keywords;
	
	@Override
	public String toString() {
		return this.toUri().toString();
	}

	public Uri toUri() {
		final Uri.Builder b = Uri.parse(request_url).buildUpon();
		Random r = new Random();
		int random = r.nextInt(50000);
		
		b.appendQueryParameter("rt", REQUEST_TYPE_ANDROID);
		b.appendQueryParameter("r_type", REQUEST_TYPE);
		b.appendQueryParameter("r_resp", RESPONSE_TYPE);
		b.appendQueryParameter("n_img", IMAGE_TYPES);
		b.appendQueryParameter("n_txt", TEXT_TYPES);
		
		if (adTypes != null && !adTypes.isEmpty()) {
			String parameter = TextUtils.join(", ", adTypes);
			b.appendQueryParameter("n_type", parameter);
		}
	
		b.appendQueryParameter("s", this.getPublisherId());
		b.appendQueryParameter("u", this.getUserAgent());
		b.appendQueryParameter("u2", this.getUserAgent2());
		b.appendQueryParameter("r_random", Integer.toString(random));
		b.appendQueryParameter("o_androidid", androidID);
		b.appendQueryParameter("o_androidimei", androidIMEI);
		b.appendQueryParameter("o_andadvid", androidAdId);
		b.appendQueryParameter("v", this.getProtocolVersion());

		if (userAge != 0) {
			b.appendQueryParameter("demo.age", Integer.toString(userAge));
		}

		if (gender != null) {
			b.appendQueryParameter("demo.gender", gender.getServerParam());
		}

		if (keywords != null && !keywords.isEmpty()) {
			String parameter = TextUtils.join(", ", keywords);
			b.appendQueryParameter("demo.keywords", parameter);
		}

		b.appendQueryParameter("u_wv", this.getUserAgent());
		b.appendQueryParameter("u_br", this.getUserAgent());
		if (longitude != 0 && latitude != 0) {
			b.appendQueryParameter("longitude", Double.toString(longitude));
			b.appendQueryParameter("latitude", Double.toString(latitude));
		}

		return b.build();
	}

	public List<String> getAdTypes() {
		return adTypes;
	}

	public void setAdTypes(List<String> adTypes) {
		this.adTypes = adTypes;
	}

	public String getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}

	public String getUserAgent() {
		if (this.userAgent == null)
			return "";
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getAndroidIMEI() {
		return androidIMEI;
	}

	public void setAndroidIMEI(String androidIMEI) {
		this.androidIMEI = androidIMEI;
	}

	public String getAndroidID() {
		return androidID;
	}

	public void setAndroidID(String androidID) {
		this.androidID = androidID;
	}

	public String getAndroidAdId() {
		return androidAdId;
	}

	public void setAndroidAdId(String androidAdId) {
		this.androidAdId = androidAdId;
	}

	public String getProtocolVersion() {
		if (this.protocolVersion == null)
			return Const.VERSION;
		else
			return this.protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getUserAge() {
		return userAge;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public String getUserAgent2() {
		if (this.userAgent2 == null)
			return "";
		return this.userAgent2;
	}
	
	public void setUserAgent2(final String userAgent) {
		this.userAgent2 = userAgent;
	}

	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
	public void setRequestUrl (String url) {
		this.request_url = url;
	}
	
}
