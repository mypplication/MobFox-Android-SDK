package com.adsdk.sdk.nativeads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
		// TODO: click listeners
		// TODO: impression tracking 
	}

	public void fillAdView(NativeAd ad, NativeViewBinder binder) {
		for (String key : binder.getTextAssetsBindingsKeySet()) {
			int resId = binder.getIdForTextAsset(key);
			if (resId == 0) {
				continue;				
			}
			TextView view = (TextView)adView.findViewById(resId); //TODO: exception handling
			String text = ad.getTextAsset(key);
			if(view != null && text != null) {
				view.setText(text); 
			}
		}

		for (String key : binder.getImageAssetsBindingsKeySet()) {
			int resId = binder.getIdForImageAsset(key);
			if (resId == 0) {
				continue;				
			}
			ImageView view = (ImageView)adView.findViewById(resId); //TODO: exception handling
			Bitmap imageBitmap = ad.getImageAsset(key).bitmap;
			if(view != null && imageBitmap != null) {
				view.setImageBitmap(imageBitmap);
			}
		}
		
	}

}
