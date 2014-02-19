package com.adsdk.sdk.customevents;

public class CustomEvent {
	 private String className;
	 private String optionalParameter;
	 private String pixelUrl;
	 
	 public CustomEvent(String className, String optionalParam, String pixelUrl) {
		 this.className = className;
		 this.optionalParameter = optionalParam;
		 this.pixelUrl = pixelUrl;
	 } 
	 
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getOptionalParameter() {
		return optionalParameter;
	}
	public void setOptionalParameter(String optionalParameter) {
		this.optionalParameter = optionalParameter;
	}
	public String getPixelUrl() {
		return pixelUrl;
	}
	public void setPixelUrl(String pixelUrl) {
		this.pixelUrl = pixelUrl;
	}
}
