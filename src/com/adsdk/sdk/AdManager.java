package com.adsdk.sdk;

import static com.adsdk.sdk.Const.AD_EXTRA;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Handler;

import com.adsdk.sdk.customevents.CustomEvent;
import com.adsdk.sdk.customevents.CustomEventFullscreen;
import com.adsdk.sdk.customevents.CustomEventFullscreen.CustomEventFullscreenListener;
import com.adsdk.sdk.customevents.CustomEventFullscreenFactory;
import com.adsdk.sdk.video.ResourceManager;
import com.adsdk.sdk.video.RichMediaActivity;
import com.adsdk.sdk.video.TrackerService;

public class AdManager {

	private static HashMap<Long, AdManager> sRunningAds = new HashMap<Long, AdManager>();

	private String mPublisherId;
	private String mUniqueId1;
	private String mUniqueId2;
	private String androidIMEI = "";
	private String androidID = "";
	private String androidAdId;
	private boolean mIncludeLocation;
	private static Context mContext;
	private Thread mRequestThread;
	private Handler mHandler;
	private AdRequest request = null;
	private AdListener mListener;
	private CustomEventFullscreenListener customFullscreenListener;
	private CustomEventFullscreen customEventFullscreen;
	private boolean mEnabled = true;
	private AdResponse mResponse;
	private String interstitialRequestURL;
	private String videoRequestURL;
	private boolean isInterstitialAdsEnabled = true;
	private boolean isVideoAdsEnabled;
	private boolean prioritizeVideoAds;
	private boolean alreadyRequestedVideo;
	private boolean alreadyRequestedInterstitial;
	private int videoMinimalDuration;
	private int videoMaxDuration;
	private boolean requestedHorizontalAd;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	public static AdManager getAdManager(AdResponse ad) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d("Cannot find AdManager with running ad:" + ad.getTimestamp());
		}
		return adManager;
	}

	public static void closeRunningAd(AdResponse ad, boolean result) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d("Cannot find AdManager with running ad:" + ad.getTimestamp());
			return;
		}
		Log.d("Notify closing event to AdManager with running ad:" + ad.getTimestamp());
		adManager.notifyAdClose(ad, result);
	}
	
	public static void notifyAdClick(AdResponse ad) {
		AdManager adManager = sRunningAds.get(ad.getTimestamp());
		if (adManager != null) {
			adManager.notifyAdClicked();
		}
	}

	public void release() {
		TrackerService.release();
		ResourceManager.cancel();
	}

	public AdManager(Context ctx, final String interstitialRequestURL, final String videoReqestURL, final String publisherId, final boolean includeLocation) throws IllegalArgumentException {
		Util.prepareAndroidAdId(ctx);
		AdManager.setmContext(ctx);
		this.interstitialRequestURL = interstitialRequestURL;
		this.setVideoRequestURL(videoReqestURL);
		this.mPublisherId = publisherId;
		this.mIncludeLocation = includeLocation;
		this.mRequestThread = null;
		this.mHandler = new Handler();
		initialize();
	}

	public void setListener(AdListener listener) {
		this.mListener = listener;
	}

	public void requestAd() {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		alreadyRequestedInterstitial = false;
		alreadyRequestedVideo = false;

		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;
			if(!isVideoAdsEnabled) {
				prioritizeVideoAds = false;
			}
			mRequestThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (ResourceManager.isDownloading()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
					Log.d("starting request thread");
					try {
						RequestGeneralAd requestAd = new RequestGeneralAd();
						if(isInterstitialAdsEnabled && !prioritizeVideoAds) {							
							request = getInterstitialRequest();
							alreadyRequestedInterstitial = true;
						} else if(isVideoAdsEnabled) {
							request = getVideoRequest();
							alreadyRequestedVideo = true;
						} else {
							Log.d("Both video and interstitial ads disabled");
							notifyNoAdFound();
							mRequestThread = null;
							return;
						}
		
						mResponse = requestAd.sendRequest(request);
						if(mResponse.getType() == Const.NO_AD && (mResponse.getCustomEvents() == null || mResponse.getCustomEvents().isEmpty())) {
							if(isVideoAdsEnabled && !alreadyRequestedVideo) {
								request = getVideoRequest();
								alreadyRequestedVideo = true;
								mResponse = requestAd.sendRequest(request);
							} else if(isInterstitialAdsEnabled && !alreadyRequestedInterstitial) {
								request = getInterstitialRequest();
								alreadyRequestedInterstitial = true;
								mResponse = requestAd.sendRequest(request);
							}
						}
						
						if (mResponse.getVideoData() != null && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO && (mResponse.getCustomEvents()==null || mResponse.getCustomEvents().isEmpty())) {
							Log.d("Not capable of video");
							notifyNoAdFound();
						} else if (mResponse.getType() == Const.VIDEO && mResponse.getCustomEvents().isEmpty()) {
							Log.d("response OK received");
							notifyAdLoaded(mResponse);
						} else if (mResponse.getCustomEvents().isEmpty() && (mResponse.getType() == Const.TEXT || mResponse.getType() == Const.MRAID || mResponse.getType() == Const.IMAGE)) { 
							notifyAdLoaded(mResponse);
						} else if (mResponse.getType() == Const.NO_AD && mResponse.getCustomEvents() != null && mResponse.getCustomEvents().isEmpty()) {
							Log.d("response NO AD received");
							notifyNoAdFound();
						} else if (mResponse.getCustomEvents() != null && !mResponse.getCustomEvents().isEmpty()) {
							loadCustomEventFullscreen();
							if(customEventFullscreen == null) {
								mResponse.getCustomEvents().clear();
								notifyNoAdFound();
							}
						} else {
							notifyNoAdFound();
						}
					} catch (Throwable t) {
						mResponse = new AdResponse();
						mResponse.setType(Const.AD_FAILED);
						notifyNoAdFound();
					}
					Log.d("finishing ad request thread");
					mRequestThread = null;
				}
			});
			mRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					mResponse = new AdResponse();
					mResponse.setType(Const.AD_FAILED);
					Log.e("Handling exception in ad request thread", ex);
					mRequestThread = null;
				}
			});
			mRequestThread.start();
		} else {
			Log.w("Request thread already running");
		}
	}

	public void setInterstitialRequestURL(String requestURL) {
		this.interstitialRequestURL = requestURL;
	}

	public void setVideoRequestURL(String videoRequestURL) {
		this.videoRequestURL = videoRequestURL;
	}

	public void requestAd(final InputStream xml) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		alreadyRequestedInterstitial = false;
		alreadyRequestedVideo = false;

		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-" + Const.PROTOCOL_VERSION + ")");
			mResponse = null;
			mRequestThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (ResourceManager.isDownloading()) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
					Log.d("starting request thread");
					try {
						RequestGeneralAd requestAd = new RequestGeneralAd(xml);
						if(isInterstitialAdsEnabled && !prioritizeVideoAds) {							
							request = getInterstitialRequest();
						} else if(isVideoAdsEnabled) {
							request = getVideoRequest();
						} else {
							Log.d("Both video and interstitial ads disabled");
							notifyNoAdFound();
							mRequestThread = null;
							return;
						}
						mResponse = requestAd.sendRequest(request);
						
						if(mResponse.getType() == Const.NO_AD && mResponse.getCustomEvents().isEmpty()) {
							if(isVideoAdsEnabled && !alreadyRequestedVideo) {
								request = getVideoRequest();
								mResponse = requestAd.sendRequest(request);
							} else if(isInterstitialAdsEnabled && !alreadyRequestedInterstitial) {
								request = getInterstitialRequest();
								mResponse = requestAd.sendRequest(request);
							}
						}
									
						if (mResponse.getType() != Const.NO_AD && mResponse.getCustomEvents().isEmpty()) {
							Log.d("response OK received");
							notifyAdLoaded(mResponse);
						} else if (mResponse.getCustomEvents().isEmpty()) {
							Log.d("response NO AD received");
							notifyNoAdFound();
						} else {
							loadCustomEventFullscreen();
							if(customEventFullscreen == null) {
								mResponse.getCustomEvents().clear();
								notifyNoAdFound();
							}
						}
					} catch (Throwable t) {
						mResponse = new AdResponse();
						mResponse.setType(Const.AD_FAILED);
						notifyNoAdFound();
					}
					Log.d("finishing ad request thread");
					mRequestThread = null;
				}
			});
			mRequestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					mResponse = new AdResponse();
					mResponse.setType(Const.AD_FAILED);
					Log.e("Handling exception in ad request thread", ex);
					mRequestThread = null;
				}
			});
			mRequestThread.start();
		} else {
			Log.w("Request thread already running");
		}
	}

	public boolean isAdLoaded() {
		return (mResponse != null);
	}

	public void requestAdAndShow(long timeout) {
		AdListener l = mListener;

		mListener = null;
		requestAd();
		long now = System.currentTimeMillis();
		long timeoutTime = now + timeout;
		while ((!isAdLoaded()) && (now < timeoutTime)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			now = System.currentTimeMillis();
		}
		mListener = l;
		showAd();
	}

	public void showAd() {
		Activity activity = (Activity) getContext();

		if (((mResponse == null) || (mResponse.getType() == Const.NO_AD) || (mResponse.getType() == Const.AD_FAILED)) && customEventFullscreen == null) {
			notifyAdShown(mResponse, false);
			return;
		}
		AdResponse ad = mResponse;
		boolean result = false;
		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				ad.setHorizontalOrientationRequested(requestedHorizontalAd);
				Log.v("Showing Ad:" + ad);
				if (customEventFullscreen == null) {
					Intent intent = new Intent(activity, RichMediaActivity.class);
					intent.putExtra(AD_EXTRA, ad);
					activity.startActivityForResult(intent, 0);
				} else {
					customEventFullscreen.showFullscreen();
				}
				result = true;
				sRunningAds.put(ad.getTimestamp(), this);
			} else {
				Log.d("No network available. Cannot show Ad.");
			}
		} catch (Exception e) {
			Log.e("Unknown exception when showing Ad", e);
		} finally {
			notifyAdShown(ad, result);
		}
	}

	private void loadCustomEventFullscreen() {
		customEventFullscreen = null;
		while(!mResponse.getCustomEvents().isEmpty() && customEventFullscreen == null) {
			try {
				final CustomEvent event = mResponse.getCustomEvents().get(0);
				mResponse.getCustomEvents().remove(event);
				customEventFullscreen = CustomEventFullscreenFactory.create(event.getClassName());
				mHandler.post(new Runnable() { //required by AdMob.
					@Override
					public void run() {
						customEventFullscreen.loadFullscreen(mContext, customFullscreenListener, event.getOptionalParameter(), event.getPixelUrl());
					}
				});
				
			} catch (Exception e) {
				customEventFullscreen = null;
				Log.d("Failed to create Custom Event Fullscreen.");
			}

		}
	}

	private void initialize() throws IllegalArgumentException {
		Log.LOGGING_ENABLED = Log.isLoggingEnabled(getmContext());
		Log.d("Ad SDK Version:" + Const.VERSION);
		this.mUniqueId1 = Util.getTelephonyDeviceId(getContext());
		this.mUniqueId2 = Util.getDeviceId(getContext());

		this.androidID = Util.getDeviceId(getContext());
		this.androidIMEI = Util.getTelephonyDeviceId(getContext());
		this.androidAdId = Util.getAndroidAdId();

		if ((mPublisherId == null) || (mPublisherId.length() == 0)) {
			Log.e("Publisher Id cannot be null or empty");
			throw new IllegalArgumentException("User Id cannot be null or empty");
		}
		if ((mUniqueId2 == null) || (mUniqueId2.length() == 0)) {
			Log.e("Cannot get system device Id");
			throw new IllegalArgumentException("System Device Id cannot be null or empty");
		}
		Log.d("AdManager Publisher Id:" + mPublisherId + " Device Id:" + mUniqueId1 + " DeviceId2:" + mUniqueId2);
		mEnabled = (Util.getMemoryClass(getContext()) > 16);
		customFullscreenListener = createCustomFullscreenListener();
	}

	private CustomEventFullscreenListener createCustomFullscreenListener() {
		return new CustomEventFullscreenListener() {

			@Override
			public void onFullscreenOpened() {
				//	notifyAdShown(mResponse, true); //not needed here, just doubles the notification
			}

			@Override
			public void onFullscreenLoaded(CustomEventFullscreen fullscreen) {
				customEventFullscreen = fullscreen;
				notifyAdLoaded(mResponse);
			}

			@Override
			public void onFullscreenLeftApplication() {
				notifyAdClicked();
			}

			@Override
			public void onFullscreenFailed() {
				loadCustomEventFullscreen();
				if (customEventFullscreen != null) {
					return;
				} else if (mResponse.getType() != Const.NO_AD && mResponse.getType() != Const.AD_FAILED) {
					notifyAdLoaded(mResponse);
				} else {
					notifyNoAdFound();
				}
			}

			@Override
			public void onFullscreenClosed() {
				notifyAdClose(mResponse, true);
			}
		};
	}

	private void notifyNoAdFound() {
		if (mListener != null) {
			Log.d("No ad found.");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.noAdFound();
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdLoaded(final AdResponse ad) {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.adLoadSucceeded(ad);
				}
			});
		}
	}

	private void notifyAdClicked() {
		if (mListener != null) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mListener.adClicked();
				}
			});
		}
	}

	private void notifyAdShown(final AdResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("Ad Shown. Result:" + ok);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.adShown(ad, ok);
				}
			});
		}
		this.mResponse = null;
	}

	private void notifyAdClose(final AdResponse ad, final boolean ok) {
		if (mListener != null) {
			Log.d("Ad Close. Result:" + ok);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListener.adClosed(ad, ok);
				}
			});
		}
	}

	private AdRequest getInterstitialRequest() {
		if (this.request == null) {
			this.request = new AdRequest();
			request.setDeviceId(mUniqueId1);
			request.setDeviceId2(mUniqueId2);
			request.setAndroidID(androidID);
			request.setAndroidIMEI(androidIMEI);
			request.setAndroidAdId(androidAdId);
			this.request.setPublisherId(this.mPublisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(mContext));
			this.request.setUserAgent2(Util.buildUserAgent());
		}
		Location location = null;
		request.setVideoRequest(false);
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setKeywords(keywords);

		if (this.mIncludeLocation)
			location = Util.getLocation(mContext);
		if (location != null) {
			Log.d("location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			request.setLatitude(location.getLatitude());
			request.setLongitude(location.getLongitude());
		} else {
			request.setLatitude(0.0);
			request.setLongitude(0.0);
		}
		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			requestedHorizontalAd = true;
			this.request.setAdspaceHeight(320);
			this.request.setAdspaceWidth(480);
		} else {
			requestedHorizontalAd = false;
			this.request.setAdspaceHeight(480);
			this.request.setAdspaceWidth(320);
		}
		
		
		this.request.setAdspaceStrict(false);
		
		request.setConnectionType(Util.getConnectionType(getContext()));
		request.setIpAddress(Util.getLocalIpAddress());
		request.setTimestamp(System.currentTimeMillis());
		
		this.request.setRequestURL(interstitialRequestURL);
		return this.request;
	}
	
	private AdRequest getVideoRequest() {
		if (this.request == null) {
			this.request = new AdRequest();
			request.setDeviceId(mUniqueId1);
			request.setDeviceId2(mUniqueId2);
			request.setAndroidID(androidID);
			request.setAndroidIMEI(androidIMEI);
			request.setAndroidAdId(androidAdId);
			this.request.setPublisherId(this.mPublisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(mContext));
			this.request.setUserAgent2(Util.buildUserAgent());
		}
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setKeywords(keywords);
		
		request.setVideoRequest(true);
		request.setVideoMaxDuration(videoMaxDuration);
		request.setVideoMinDuration(videoMinimalDuration);
		Location location = null;
		if (this.mIncludeLocation)
			location = Util.getLocation(mContext);
		if (location != null) {
			Log.d("location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			request.setLatitude(location.getLatitude());
			request.setLongitude(location.getLongitude());
		} else {
			request.setLatitude(0.0);
			request.setLongitude(0.0);
		}
		
		this.request.setAdspaceHeight(480);
		this.request.setAdspaceWidth(320);
		this.request.setAdspaceStrict(false);
		
		request.setConnectionType(Util.getConnectionType(getContext()));
		request.setIpAddress(Util.getLocalIpAddress());
		request.setTimestamp(System.currentTimeMillis());
		
		this.request.setRequestURL(videoRequestURL);
		return this.request;
	}

	private Context getContext() {
		return getmContext();
	}

	private static Context getmContext() {
		return mContext;
	}

	private static void setmContext(Context mContext) {
		AdManager.mContext = mContext;
	}

	public void setInterstitialAdsEnabled(boolean enableInterstitialAds) {
		this.isInterstitialAdsEnabled = enableInterstitialAds;
	}

	public void setVideoAdsEnabled(boolean enableVideoAds) {
		this.isVideoAdsEnabled = enableVideoAds;
	}

	public boolean isPrioritizeVideoAds() {
		return prioritizeVideoAds;
	}

	public void setPrioritizeVideoAds(boolean prioritizeVideoAds) {
		this.prioritizeVideoAds = prioritizeVideoAds;
	}

	public int getVideoMinimalDuration() {
		return videoMinimalDuration;
	}

	public void setVideoMinimalDuration(int videoMinimalDuration) {
		this.videoMinimalDuration = videoMinimalDuration;
	}

	public int getVideoMaxDuration() {
		return videoMaxDuration;
	}

	public void setVideoMaxDuration(int videoMaxDuration) {
		this.videoMaxDuration = videoMaxDuration;
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
