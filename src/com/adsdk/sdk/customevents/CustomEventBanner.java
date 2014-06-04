package com.adsdk.sdk.customevents;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

public abstract class CustomEventBanner {
	
	protected String trackingPixel;
	protected CustomEventBannerListener listener;
	
    public abstract void loadBanner(Context context,
            CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height);
    

    
    protected void reportImpression() {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setHeader("User-Agent", System.getProperty("http.agent"));
					String url;
					if(trackingPixel.startsWith("http://") || trackingPixel.startsWith("https://")) {
						url = trackingPixel;
					} else {
						url = "http://" + trackingPixel;
					}
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
    
    
    public interface CustomEventBannerListener {
       
        void onBannerLoaded(View bannerView);
        
        void onBannerFailed();

        void onBannerExpanded();

        void onBannerClosed();

    }
}
