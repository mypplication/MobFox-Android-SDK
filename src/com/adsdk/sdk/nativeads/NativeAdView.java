package com.adsdk.sdk.nativeads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.adsdk.sdk.Log;

@SuppressLint("ViewConstructor")
public class NativeAdView extends FrameLayout {
	private Context context;
	private View adView;

	public NativeAdView(Context context, NativeAd ad, NativeViewBinder binder) {
		super(context);
		this.context = context;
		adView = inflate(context, binder.getAdLayoutId(), null);
		fillAdView(ad, binder);
		this.addView(adView);
		// TODO: impression tracking
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

}
