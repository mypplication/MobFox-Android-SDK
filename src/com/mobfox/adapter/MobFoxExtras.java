package com.mobfox.adapter;

// Copyright 2011 Google Inc. All Rights Reserved.

import com.google.ads.mediation.NetworkExtras;

public final class MobFoxExtras implements NetworkExtras {
	private boolean animation;
	private boolean location;

	public MobFoxExtras() {
		animation = false;
		location = false;
	}

	public boolean getAnimation() {
		return animation;
	}

	public void setAnimation(boolean animation) {
		this.animation = animation;
	}

	public boolean getLocation() {
		return location;
	}

	public void setLocation(boolean location) {
		this.location = location;
	}

}
