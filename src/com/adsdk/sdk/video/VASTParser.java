package com.adsdk.sdk.video;

import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.res.Resources.NotFoundException;


public class VASTParser {

	public static VAST createVastFromString(String string) {
		VAST vast = null;
		Serializer serial = new Persister();
 
        try {
            vast = serial.read(VAST.class, string);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

		return vast;
	}
	
	public static VAST createVastFromInputStream(InputStream stream) {
		VAST vast = null;
		Serializer serial = new Persister();
 
        try {
            vast = serial.read(VAST.class, stream);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

		return vast;
	}

	public static VideoData fillVideoDataFromVast(VideoData video) {
		VAST vast = null;
		
		//TODO: fill data
		
		
		return video;
		
		
	}

}
