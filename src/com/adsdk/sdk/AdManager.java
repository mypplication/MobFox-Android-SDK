package com.adsdk.sdk;

import static com.adsdk.sdk.Const.AD_EXTRA;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;

import com.adsdk.sdk.video.ResourceManager;
import com.adsdk.sdk.video.RichMediaActivity;
import com.adsdk.sdk.video.RichMediaAd;
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
	private AdRequest mRequest = null;
	private AdListener mListener;
	private boolean mEnabled = true;
	private RichMediaAd mResponse;
	private String requestURL;

	private String mUserAgent;

	public static AdManager getAdManager(RichMediaAd ad) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d(
					"Cannot find AdManager with running ad:"
							+ ad.getTimestamp());
		}
		return adManager;
	}

	public static void closeRunningAd(RichMediaAd ad, boolean result) {
		AdManager adManager = sRunningAds.remove(ad.getTimestamp());
		if (adManager == null) {
			Log.d(
					"Cannot find AdManager with running ad:"
							+ ad.getTimestamp());
			return;
		}
		Log.d(
				"Notify closing event to AdManager with running ad:"
						+ ad.getTimestamp());
		adManager.notifyAdClose(ad, result);
	}

	public void release() {
		TrackerService.release();
		ResourceManager.cancel();

	}

	public AdManager(Context ctx, final String requestURL, final String publisherId,
			final boolean includeLocation)
					throws IllegalArgumentException {
		Util.prepareAndroidAdId(ctx);
		AdManager.setmContext(ctx);
		this.requestURL = requestURL;
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
		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-"
					+ Const.PROTOCOL_VERSION + ")");
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
						RequestRichMediaAd requestAd = new RequestRichMediaAd();
						AdRequest request = getRequest();
						mResponse = requestAd.sendRequest(request);
						if(mResponse.getVideo()!=null && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO){
							Log.d("Not capable of video");
							notifyNoAdFound();
						}
						else if (mResponse.getType() == Const.VIDEO_TO_INTERSTITIAL || mResponse.getType() == Const.INTERSTITIAL_TO_VIDEO || mResponse.getType() == Const.VIDEO || mResponse.getType() == Const.INTERSTITIAL ) {
							Log.d("response OK received");
							if (mListener != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										mListener.adLoadSucceeded(mResponse);
									}
								});
							}
						} else if (mResponse.getType() == Const.NO_AD){
							Log.d("response NO AD received");
							if (mListener != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										notifyNoAdFound();
									}
								});
							}
						}
						else {
							Log.w("response BANNER received");
							if (mListener != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										notifyNoAdFound();
									}
								});
							}
						}
					} catch (Throwable t) {
						mResponse = new RichMediaAd();
						mResponse.setType(Const.AD_FAILED);
						if (mListener != null) {
							Log.d("Ad Load failed. Reason:" + t);
							t.printStackTrace();

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									notifyNoAdFound();

								}
							});
						}
					}
					Log.d("finishing ad request thread");
					mRequestThread = null;
				}
			});
			mRequestThread
			.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread,
						Throwable ex) {
					mResponse = new RichMediaAd();
					mResponse.setType(Const.AD_FAILED);
					Log.e(
							"Handling exception in ad request thread",
							ex);
					mRequestThread = null;
				}
			});
			mRequestThread.start();
		} else {
			Log.w("Request thread already running");
		}
	}

	public void setRequestURL(String requestURL){
		this.requestURL = requestURL;
	}

	public void requestAd(final InputStream xml) {
		if (!mEnabled) {
			Log.w("Cannot request rich adds on low memory devices");
			return;
		}
		if (mRequestThread == null) {
			Log.d("Requesting Ad (v" + Const.VERSION + "-"
					+ Const.PROTOCOL_VERSION + ")");
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
						RequestRichMediaAd requestAd = new RequestRichMediaAd(xml);
						AdRequest request = getRequest();
						mResponse = requestAd.sendRequest(request);
						if (mResponse.getType() != Const.NO_AD) {
							Log.d("response OK received");
							if (mListener != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										mListener.adLoadSucceeded(mResponse);
									}
								});
							}
						} else {
							Log.d("response NO AD received");
							if (mListener != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										notifyNoAdFound();
									}
								});
							}
						}
					} catch (Throwable t) {
						mResponse = new RichMediaAd();
						mResponse.setType(Const.AD_FAILED);
						if (mListener != null) {
							Log.d("Ad Load failed. Reason:" + t);
							t.printStackTrace();

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									notifyNoAdFound();

								}
							});
						}
					}
					Log.d("finishing ad request thread");
					mRequestThread = null;
				}
			});
			mRequestThread
			.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread thread,
						Throwable ex) {
					mResponse = new RichMediaAd();
					mResponse.setType(Const.AD_FAILED);
					Log.e(
							"Handling exception in ad request thread",
							ex);
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

		if ((mResponse == null)
				|| (mResponse.getType() == Const.NO_AD)
				|| (mResponse.getType() == Const.AD_FAILED)) {
			notifyAdShown(mResponse, false);
			return;
		}
		RichMediaAd ad = mResponse;
		boolean result = false;
		try {
			if (Util.isNetworkAvailable(getContext())) {
				ad.setTimestamp(System.currentTimeMillis());
				Log.v("Showing Ad:" + ad);
				Intent intent = new Intent(activity,
						RichMediaActivity.class);
				intent.putExtra(AD_EXTRA, ad);
				activity.startActivityForResult(intent, 0);
				int enterAnim = Util.getEnterAnimation(ad.getAnimation());
				int exitAnim = Util.getExitAnimation(ad.getAnimation());
				RichMediaActivity.setActivityAnimation(activity,
						enterAnim, exitAnim);
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

	private void initialize() throws IllegalArgumentException {
		mUserAgent = Util.getDefaultUserAgentString(getContext());
		Log.LOGGING_ENABLED = Log.isLoggingEnabled(getmContext());
		Log.d("Ad SDK Version:" + Const.VERSION);
		this.mUniqueId1 = Util.getTelephonyDeviceId(getContext());
		this.mUniqueId2 = Util.getDeviceId(getContext());
		
		this.androidID = Util.getDeviceId(getContext());
		this.androidIMEI = Util.getTelephonyDeviceId(getContext());
		this.androidAdId = Util.getAndroidAdId();
		
		if ((mPublisherId == null) || (mPublisherId.length() == 0)) {
			Log.e("Publisher Id cannot be null or empty");
			throw new IllegalArgumentException(
					"User Id cannot be null or empty");
		}
		if ((mUniqueId2 == null) || (mUniqueId2.length() == 0)) {
			Log.e("Cannot get system device Id");
			throw new IllegalArgumentException(
					"System Device Id cannot be null or empty");
		}
		Log.d("AdManager Publisher Id:" + mPublisherId
				+ " Device Id:" + mUniqueId1 + " DeviceId2:" + mUniqueId2);
		mEnabled = (Util.getMemoryClass(getContext()) > 16);
		Util.initializeAnimations(getContext());

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

	private void notifyAdShown(final RichMediaAd ad, final boolean ok) {
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

	private void notifyAdClose(final RichMediaAd ad, final boolean ok) {
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

	private AdRequest getRequest() {
		if(androidAdId == "") {
			androidAdId = Util.getAndroidAdId();
		}
		if (mRequest == null) {
			mRequest = new AdRequest();
			mRequest.setDeviceId(mUniqueId1);
			mRequest.setDeviceId2(mUniqueId2);
			mRequest.setAndroidID(androidID); 
			mRequest.setAndroidIMEI(androidIMEI);
			mRequest.setAndroidAdId(androidAdId);
			mRequest.setPublisherId(mPublisherId);
			mRequest.setUserAgent(mUserAgent);
			mRequest.setUserAgent2(Util.buildUserAgent());
		}
		Location location = null;
		if (this.mIncludeLocation) {
			location = Util.getLocation(getContext());
		}
		if (location != null) {
			Log.d("location is longitude: " + location.getLongitude()
					+ ", latitude: " + location.getLatitude());
			mRequest.setLatitude(location.getLatitude());
			mRequest.setLongitude(location.getLongitude());
		} else {
			mRequest.setLatitude(0.0);
			mRequest.setLongitude(0.0);
		}
		mRequest.setConnectionType(Util.getConnectionType(getContext()));
		mRequest.setIpAddress(Util.getLocalIpAddress());
		mRequest.setTimestamp(System.currentTimeMillis());

		mRequest.setType(AdRequest.VAD);
		mRequest.setRequestURL(this.requestURL);
		Log.d("Getting new request:" + mRequest.toString());
		return mRequest;
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

}
