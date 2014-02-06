package com.adsdk.sdk.banner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.style.LineHeightSpan.WithDensity;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.BannerAd;
import com.adsdk.sdk.Const;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.data.ClickType;

@SuppressLint({ "ViewConstructor", "SetJavaScriptEnabled" })
public class BannerAdView extends RelativeLayout {

	public static final int LIVE = 0;
	public static final int TEST = 1;

	private boolean animation;

	private boolean isInternalBrowser = false;

	private BannerAd response;
	private Animation fadeInAnimation = null;
	// private Animation fadeOutAnimation = null;
	private WebSettings webSettings;

	private Context mContext = null;
	protected boolean mIsInForeground;

	private WebView webView;

	private int width;
	private int height;

	private AdListener adListener;

	private static Method mWebView_SetLayerType;
	private static Field mWebView_LAYER_TYPE_SOFTWARE;

	private final Handler updateHandler = new Handler();

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public BannerAdView(final Context context, final BannerAd response, int width, int height, final boolean animation, final AdListener adListener) {
		super(context);
		mContext = context;
		this.response = response;
		this.width = width;
		this.height = height;
		this.animation = animation;
		this.adListener = adListener;
		this.initialize(context);
	}

	private WebView createWebView(final Context context) {
		final WebView webView = new WebView(this.getContext()) {

			@Override
			public void draw(final Canvas canvas) {
				if (this.getWidth() > 0 && this.getHeight() > 0)
					super.draw(canvas);
			}
		};

		this.webSettings = webView.getSettings();
		this.webSettings.setJavaScriptEnabled(true);
		this.webSettings.setSupportMultipleWindows(true);
		webView.setBackgroundColor(Color.TRANSPARENT);
		setLayer(webView);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				if (response.getSkipOverlay() == 1) {
					doOpenUrl(url);
					Log.i("TouchListener", "false");
					return true;
				}
				Log.i("TouchListener", "default");
				openLink();
				return true;
			}

		});

		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);

		return webView;
	}

	private void doOpenUrl(final String url) {
		notifyAdClicked();
		if(this.response.getClickUrl() != null) {
			makeTrackingRequest(this.response.getClickUrl());
		}

		if (this.response.getClickType() != null && this.response.getClickType().equals(ClickType.INAPP) && (url.startsWith("http://") || url.startsWith("https://"))) {
			if (url.endsWith(".mp4")) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse(url), "video/mp4");
				this.getContext().startActivity(i);
			} else {
				final Intent intent = new Intent(this.getContext(), InAppWebView.class);
				intent.putExtra(Const.REDIRECT_URI, url);
				this.getContext().startActivity(intent);
			}
		} else {
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			this.getContext().startActivity(intent);
		}
	}

	private void makeTrackingRequest(final String clickUrl) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setURI(new URI(clickUrl));
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

	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			for (Method m : WebView.class.getMethods()) {
				if (m.getName().equals("setLayerType")) {
					mWebView_SetLayerType = m;
					break;
				}
			}

			Log.v("set layer " + mWebView_SetLayerType);
			mWebView_LAYER_TYPE_SOFTWARE = WebView.class.getField("LAYER_TYPE_SOFTWARE");
			Log.v("set1 layer " + mWebView_LAYER_TYPE_SOFTWARE);

		} catch (SecurityException e) {

			Log.v("SecurityException");
		} catch (NoSuchFieldException e) {

			Log.v("NoSuchFieldException");
		}
	}

	private static void setLayer(WebView webView) {
		if (mWebView_SetLayerType != null && mWebView_LAYER_TYPE_SOFTWARE != null) {
			try {
				Log.v("Set Layer is supported");
				mWebView_SetLayerType.invoke(webView, mWebView_LAYER_TYPE_SOFTWARE.getInt(WebView.class), null);
			} catch (InvocationTargetException ite) {
				Log.v("Set InvocationTargetException");
			} catch (IllegalArgumentException e) {
				Log.v("Set IllegalArgumentException");
			} catch (IllegalAccessException e) {
				Log.v("Set IllegalAccessException");
			}
		} else {
			Log.v("Set Layer is not supported");
		}
	}

	private void buildBannerView() {
		this.webView = this.createWebView(mContext);
		Log.d(Const.TAG, "Create view flipper");
		final float scale = mContext.getResources().getDisplayMetrics().density;
		if (width != 0 && height != 0) {
			this.setLayoutParams(new RelativeLayout.LayoutParams((int) (width * scale + 0.5f), (int) (height * scale + 0.5f)));
		} else {
			this.setLayoutParams(new RelativeLayout.LayoutParams((int) (300 * scale + 0.5f), (int) (50 * scale + 0.5f)));
		}

		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		this.addView(this.webView, params);

		Log.d(Const.TAG, "animation: " + this.animation);
		if (this.animation) {

			this.fadeInAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
					Animation.RELATIVE_TO_PARENT, 0.0f);
			this.fadeInAnimation.setDuration(1000);

			// this.fadeOutAnimation = new TranslateAnimation(
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, 0.0f,
			// Animation.RELATIVE_TO_PARENT, -1.0f);
			// this.fadeOutAnimation.setDuration(1000);
			this.webView.setAnimation(fadeInAnimation);
		}
	}

	private void initialize(final Context context) {
		initCompatibility();
		buildBannerView();
		showContent();
	}

	public boolean isInternalBrowser() {
		return this.isInternalBrowser;
	}

	private void notifyAdClicked() {
		this.updateHandler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					Log.d(Const.TAG, "notify bannerListener of ad clicked: " + BannerAdView.this.adListener.getClass().getName());
					adListener.adClicked();
				}
			}
		});
	}

	private void notifyLoadAdSucceeded() {
		this.updateHandler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					Log.d(Const.TAG, "notify bannerListener of load succeeded: " + BannerAdView.this.adListener.getClass().getName());
					adListener.adLoadSucceeded(null);
				}
			}
		});
	}

	private void openLink() {

		if (this.response != null && this.response.getClickUrl() != null)
			this.doOpenUrl(this.response.getClickUrl());

	}

	public void setAdListener(final AdListener bannerListener) {
		this.adListener = bannerListener;
	}

	public void setInternalBrowser(final boolean isInternalBrowser) {
		this.isInternalBrowser = isInternalBrowser;
	}

	private void showContent() {

		try {
			if (this.response.getType() == Const.IMAGE) {

				String text = MessageFormat.format(Const.IMAGE_BODY, this.response.getImageUrl(), this.response.getBannerWidth(), this.response.getBannerHeight());
				Log.d(Const.TAG, "set image: " + text);
				text = Uri.encode(Const.HIDE_BORDER + text);
				webView.loadData(text, "text/html", Const.ENCODING);
				this.notifyLoadAdSucceeded();
			} else if (this.response.getType() == Const.TEXT) {
				final String text = Uri.encode(Const.HIDE_BORDER + this.response.getText());
				Log.d(Const.TAG, "set text: " + text);
				webView.loadData(text, "text/html", Const.ENCODING);
				this.notifyLoadAdSucceeded();
			}
			if (animation) {
				webView.startAnimation(fadeInAnimation);
			}
		} catch (final Throwable t) {
			Log.e(Const.TAG, "Exception in show content", t);
		}
	}

}
