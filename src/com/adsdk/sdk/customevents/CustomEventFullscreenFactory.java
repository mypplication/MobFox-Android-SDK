package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;

public class CustomEventFullscreenFactory {
	private static CustomEventFullscreenFactory instance = new CustomEventFullscreenFactory();

    public static CustomEventFullscreen create(String className) throws Exception {
        return instance.internalCreate(className);
    }

    protected CustomEventFullscreen internalCreate(String className) throws Exception {
    	className = "com.adsdk.sdk.customevents." + className + "Fullscreen";
        Class<? extends CustomEventFullscreen> fullscreenClass = Class.forName(className)
                .asSubclass(CustomEventFullscreen.class);
        Constructor<?> fullscreenConstructor = fullscreenClass.getDeclaredConstructor((Class[]) null);
        fullscreenConstructor.setAccessible(true);
        return (CustomEventFullscreen) fullscreenConstructor.newInstance();
    }
}
