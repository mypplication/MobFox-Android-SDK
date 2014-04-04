package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;

public class CustomEventBannerFactory {
	private static CustomEventBannerFactory instance = new CustomEventBannerFactory();

	public static CustomEventBanner create(String className) throws Exception {
		return instance.internalCreate(className);
	}

	protected CustomEventBanner internalCreate(String className) throws Exception {
		className = "com.adsdk.sdk.customevents." + className + "Banner";
		Class<? extends CustomEventBanner> bannerClass = Class.forName(className).asSubclass(CustomEventBanner.class);
		Constructor<?> bannerConstructor = bannerClass.getDeclaredConstructor((Class[]) null);
		bannerConstructor.setAccessible(true);
		return (CustomEventBanner) bannerConstructor.newInstance();
	}
}
