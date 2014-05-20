package com.adsdk.sdk.nativeads;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.nativeads.NativeAd.Tracker;

@SuppressLint("ViewConstructor")
public class NativeAdView extends FrameLayout {
	private boolean impressionReported;
	private View adView;
	private NativeAdListener listener;
	private Handler handler;
	private List<Tracker> trackers;

	public NativeAdView(Context context, NativeAd ad, NativeViewBinder binder, NativeAdListener listener) {
		super(context);
		if (ad == null || binder == null) {
			return;
		}
		adView = inflate(context, binder.getAdLayoutId(), null);
		trackers = ad.getTrackers();
		handler = new Handler();
		this.listener = listener;
		fillAdView(ad, binder);
		this.addView(adView);
	}

	public void fillAdView(NativeAd ad, NativeViewBinder binder) {
		for (String key : binder.getTextAssetsBindingsKeySet()) {
			int resId = binder.getIdForTextAsset(key);
			if (resId == 0) {
				continue;
			}
			try {
				if (key.equals("rating")) { // rating is special, not displayed as normal text view.
					RatingBar bar = (RatingBar) adView.findViewById(resId);
					if (bar != null) {
						int rating = Integer.parseInt(ad.getTextAsset(key));
						bar.setIsIndicator(true);
						bar.setRating(rating);
					}
				} else {
					TextView view = (TextView) adView.findViewById(resId);
					String text = ad.getTextAsset(key);
					if (view != null && text != null) {
						view.setText(text);
					}
				}
			} catch (ClassCastException e) {
				Log.e("Cannot fill view for " + key);
			}
		}

		for (String key : binder.getImageAssetsBindingsKeySet()) {
			int resId = binder.getIdForImageAsset(key);
			if (resId == 0) {
				continue;
			}
			try {
				ImageView view = (ImageView) adView.findViewById(resId);
				Bitmap imageBitmap = ad.getImageAsset(key).bitmap;
				if (view != null && imageBitmap != null) {
					view.setImageBitmap(imageBitmap);
				}
			} catch (ClassCastException e) {
				Log.e("Cannot fill view for " + key);
			}
		}

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (!impressionReported) {
			impressionReported = true;
			notifyImpression();

			for (Tracker t : trackers) {
				if (t.type.equals("impression")) {
					trackImpression(t.url);
				}
			}
		}
		super.dispatchDraw(canvas);
	}

	private void notifyImpression() {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.impression();
				}
			});
		}
	}

	private void trackImpression(final String url) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setHeader("User-Agent", System.getProperty("http.agent"));
					request.setURI(new URI(url));
					client.execute(request);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

		};
		task.execute();
	}

}
