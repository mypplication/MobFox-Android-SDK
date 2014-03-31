package com.adsdk.sdk.video;

import static com.adsdk.sdk.Const.MAX_NUMBER_OF_TRACKING_RETRIES;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Log;

public class TrackerService {

	private static Object sLock = new Object();

	private static boolean sThreadRunning = false;

	private static Queue<TrackEvent> sTrackEvents = new LinkedList<TrackEvent>();

	private static Queue<TrackEvent> sRetryTrackEvents = new LinkedList<TrackEvent>();

	private static Thread sThread;

	private static boolean sStopped;

	public static void requestTrack(TrackEvent[] trackEvents) {
		synchronized (sLock) {
			for (TrackEvent trackEvent : trackEvents) {
				if (!sTrackEvents.contains(trackEvent)) {
					sTrackEvents.add(trackEvent);
				}
			}
			Log.d("Added track event:" + sTrackEvents.size());
		}
		if (!sThreadRunning) {
			startTracking();
		}
	}

	public static void requestTrack(TrackEvent trackEvent) {
		synchronized (sLock) {
			if (!sTrackEvents.contains(trackEvent)) {
				sTrackEvents.add(trackEvent);
			}
			Log.d("Added track event:" + sTrackEvents.size());
		}
		if (!sThreadRunning) {
			startTracking();
		}
	}

	public static void requestRetry(TrackEvent trackEvent) {
		synchronized (sLock) {
			if (!sRetryTrackEvents.contains(trackEvent)) {
				trackEvent.retries++;
				if (trackEvent.retries <= MAX_NUMBER_OF_TRACKING_RETRIES) {
					sRetryTrackEvents.add(trackEvent);
				}
			}
			Log.d("Added retry track event:" + sRetryTrackEvents.size());
		}
	}

	private static boolean hasMoreUpdates() {
		synchronized (sLock) {
			boolean hasMore = !sTrackEvents.isEmpty();
			Log.d("More updates:" + hasMore + " size:" + sTrackEvents.size());
			return hasMore;
		}
	}

	private static TrackEvent getNextUpdate() {
		synchronized (sLock) {
			TrackEvent nextTrackEvent = sTrackEvents.poll();
			return nextTrackEvent;
		}
	}

	public static void startTracking() {

		synchronized (sLock) {
			if (!sThreadRunning) {
				sThreadRunning = true;
				sThread = new Thread(new Runnable() {

					@Override
					public void run() {
						sStopped = false;
						while (!sStopped) {
							while (hasMoreUpdates() && !sStopped) {
								TrackEvent event = getNextUpdate();
								if (event == null) {
									continue;									
								}
								Log.d("Sending tracking :" + event.url + " Time:" + event.timestamp + " Events left:" + sTrackEvents.size());
								
								Log.d("Sending conversion Request");
								
								Log.d("Perform tracking HTTP Get Url: " + event.url);
								DefaultHttpClient client = new DefaultHttpClient();
								HttpConnectionParams.setSoTimeout(client.getParams(), Const.SOCKET_TIMEOUT);
								HttpConnectionParams.setConnectionTimeout(client.getParams(), Const.CONNECTION_TIMEOUT);

								HttpGet get = new HttpGet();
								get.setHeader("User-Agent", System.getProperty("http.agent"));
								
								HttpResponse response;
								try {
									get.setURI(new URI(event.url.trim()));
									
									response = client.execute(get);
									if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
										requestRetry(event);
									} else {
										Log.d("Tracking OK");
									}
								} catch (Throwable t) {
									requestRetry(event);
								}

							}
							if ((!sStopped) && (!sRetryTrackEvents.isEmpty())) {
								try {
									Thread.sleep(30000);
								} catch (Exception e) {

								}
								synchronized (sLock) {
									sTrackEvents.addAll(sRetryTrackEvents);
									sRetryTrackEvents.clear();
								}
							} else {
								sStopped = true;
							}
						}
						sStopped = false;
						sThreadRunning = false;
						sThread = null;
					}
				});
				sThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread thread, Throwable ex) {
						sThreadRunning = false;
						sThread = null;
						startTracking();
					}
				});
				sThread.start();
			}
		}
	}

	public static void release() {
		Log.v("release");
		if (sThread != null) {
			Log.v("release stopping Tracking events thread");
			sStopped = true;
		}
	}

}
