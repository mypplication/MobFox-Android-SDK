package com.adsdk.sdk;

import android.content.Context;


public final class Log {

	public static boolean LOGGING_ENABLED = false;

	public static boolean isLoggingEnabled(Context context){
		int debug = context.getResources().getIdentifier("adsdk_debug_enabled", "string", context.getPackageName());
		if (debug!=0){
			String sDebug = context.getResources().getString(debug);
			if(sDebug.equalsIgnoreCase("true")){
				return true;
			}
		}
		return false;

	}

	private static boolean isLoggingEnabled(){
		return LOGGING_ENABLED;
	}

	public static void d(final String msg) {
		Log.d(Const.TAG, msg);
	}

	public static void d(final String tag, final String msg) {
		if(isLoggingEnabled())
			android.util.Log.d(tag, msg, null);
	}

	public static void d(final String tag, final String msg, final Throwable tr) {
		// if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG))
		if(isLoggingEnabled())
			android.util.Log.d(tag, msg, tr);
	}

	public static void d(final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.d(Const.TAG, msg, tr);
	}

	public static void e(final String msg) {
		Log.e(Const.TAG, msg);
	}

	public static void e(final String tag, final String msg) {
		if(isLoggingEnabled())
			android.util.Log.w(tag, msg, null);
	}

	public static void e(final String tag, final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.w(tag, msg, tr);
	}

	public static void e(final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.w(Const.TAG, msg, tr);
	}

	public static void i(final String msg) {
		Log.i(Const.TAG, msg);
	}

	public static void i(final String tag, final String msg) {
		if(isLoggingEnabled())
			android.util.Log.i(tag, msg, null);
	}

	public static void i(final String tag, final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.i(tag, msg, tr);
	}

	public static void i(final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.i(Const.TAG, msg, tr);
	}

	public static void v(final String msg) {
		Log.v(Const.TAG, msg);
	}

	public static void v(final String tag, final String msg) {
		if(isLoggingEnabled())
			android.util.Log.v(tag, msg, null);
	}

	public static void v(final String tag, final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.v(tag, msg, tr);
	}

	public static void v(final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.v(Const.TAG, msg, tr);
	}

	public static void w(final String msg) {
		Log.w(Const.TAG, msg);
	}

	public static void w(final String tag, final String msg) {
		if(isLoggingEnabled())
			android.util.Log.w(tag, msg, null);
	}

	public static void w(final String tag, final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.w(tag, msg, tr);
	}

	public static void w(final String msg, final Throwable tr) {
		if(isLoggingEnabled())
			android.util.Log.w(Const.TAG, msg, tr);
	}
}