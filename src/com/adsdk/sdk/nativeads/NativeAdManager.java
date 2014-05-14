package com.adsdk.sdk.nativeads;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.TelephonyManager;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;

public class NativeAdManager {

	private NativeAd nativeAd;
	private String publisherId;
	private boolean includeLocation = false;

	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private NativeAdListener listener;

	private Context context;
	private Thread requestThread;
	private NativeAdRequest request;

	private int telephonyPermission;
	private List<String> adTypes;

	public NativeAdManager(Context context, boolean includeLocation, String publisherId, NativeAdListener listener, List<String> adTypes) {
		this.context = context;
		this.includeLocation = includeLocation;
		this.publisherId = publisherId;
		this.listener = listener;
		this.adTypes = adTypes;
		telephonyPermission = context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE);
		Util.prepareAndroidAdId(context);
	}

	public void requestAd() {
		if (this.requestThread == null) {
			this.requestThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d(Const.TAG, "starting request thread");
					final RequestNativeAd requestAd;
					requestAd = new RequestNativeAd();

					try {
						nativeAd = requestAd.sendRequest(NativeAdManager.this.getRequest());
						if (nativeAd != null) {
							if (listener != null) {
								listener.adLoaded(nativeAd);
							}
						} else {
							if (listener != null) {
								listener.adFailedToLoad();
							}
						}
					} catch (final Throwable e) {
						if (listener != null) {
							listener.adFailedToLoad();
						}
					}
					NativeAdManager.this.requestThread = null;
					Log.d(Const.TAG, "finishing request thread");
				}

			});
			this.requestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(final Thread thread, final Throwable ex) {
					Log.e(Const.TAG, "Exception in request thread", ex);
					NativeAdManager.this.requestThread = null;
				}
			});
			this.requestThread.start();
		}
	}
	
	private NativeAdRequest getRequest() {
		if (this.request == null) {
			this.request = new NativeAdRequest();
			if (this.telephonyPermission == PackageManager.PERMISSION_GRANTED) {
				final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				this.request.setAndroidIMEI(tm.getDeviceId());
			}
			this.request.setAndroidID(Util.getDeviceId(context));
			this.request.setAndroidAdId(Util.getAndroidAdId());
			this.request.setPublisherId(this.publisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(context));
			Log.d(Const.TAG, "WebKit UserAgent:" + this.request.getUserAgent());
		}
		request.setAdTypes(adTypes);
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setKeywords(keywords);
		Location location = null;
		if (this.includeLocation)
			location = Util.getLocation(context);
		if (location != null) {
			Log.d(Const.TAG, "location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			this.request.setLatitude(location.getLatitude());
			this.request.setLongitude(location.getLongitude());
		} else {
			this.request.setLatitude(0.0);
			this.request.setLongitude(0.0);
		}
		return this.request;
	}

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

}
