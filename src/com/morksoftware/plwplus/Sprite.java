package com.morksoftware.plwplus;

import android.content.Context;
import android.graphics.Canvas;

public abstract class Sprite implements OnTapListener {

	public abstract void doDraw(Canvas c);
    public abstract void initResources(Context ctx, int screenWidth, int screenHeight);
	public abstract void releaseResources();
	public abstract void onSingleTap(int x, int y);
	public abstract void onDoubleTap(int x, int y);
	public abstract void doWallpaperScroll(float xOffset, float xOffsetStep, int xPixelOffset, int screenWidth);
	public abstract void onSharedPreferenceChanged();
}
