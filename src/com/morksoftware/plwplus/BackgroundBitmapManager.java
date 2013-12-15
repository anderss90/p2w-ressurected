package com.morksoftware.plwplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Debug;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

// TODO Add checks for OOM exceptions

public class BackgroundBitmapManager {
	// Debug tag
	private static final String TAG = "BackgroundBitmapManager";
	
	// The width of the raw background images
	private int RAW_ASSET_WIDTH = 2560;
	// The height of the raw background images
	private int RAW_ASSET_HEIGHT = 1440;
	// An instance of Display, to fetch information about the device display
	private Display mDisplay;
	
	// The desired width for the current window
	private int mDesiredWidth;
	// The desired height for the current window
	private int mDesiredHeight;
	
	// A context to fetch resources
	private Context mCtx;
	private int IMAGE_WIDTH_MAX = 1200;
	private int IMAGE_HEIGHT_MAX = 1000;
	
	/**
	 * Default constructor. Takes a Context as a parameter
	 * for resource loading.
	 * 
	 * @param ctx A context
	 */
	public BackgroundBitmapManager(Context ctx) {
		mCtx = ctx;
		
		mDisplay = ((WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}
		
	
	public Bitmap loadScaledBitmapFromPath(String path, boolean isPreview) {
		// Fetch adjusted metrics
		
		
		
		// Get an instance of BitmapFactory.Options to handle decoding options
		BitmapFactory.Options  bmOptions = new BitmapFactory.Options();
		
		// Prevent Android from loading a density specific bitmap
		bmOptions.inScaled = false;
		
		// Only decode the metric information of the bitmap (No pixels loaded)
		bmOptions.inJustDecodeBounds = true;
		Log.i("BitmapManager","path: "+path);
		// convert path to stream
		Uri uri = Uri.parse(path);
		Log.i("BitmapManager","Uri: "+uri.toString());
		Log.i("BitmapManager","Uri type: "+mCtx.getContentResolver().getType(uri));
		
		//create stream from Uri
		InputStream stream = null;
		try {
			stream = mCtx.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.i("BitmapManager","OpenInputstream failed");
			e1.printStackTrace();
		}
		
		// Sample the asset, without loading the pixels
		//BitmapFactory.decodeFile(path, bmOptions);
		BitmapFactory.decodeStream(stream, null, bmOptions);
		
		// Get the height of the sampled asset
		float tempBitmapHeight = (float)bmOptions.outHeight;
		
		// Get the width of the sampled asset
		float tempBitmapWidth = (float)bmOptions.outWidth;
		
		RAW_ASSET_HEIGHT = (int)tempBitmapHeight;
		RAW_ASSET_WIDTH = (int)tempBitmapWidth;
		updateWallpaperMetrics(isPreview);
		
		// Calculate the ratio between the the asset width and desired width 
		float widthRatio = tempBitmapWidth / mDesiredWidth;
		
		// Calculate the ratio between the asset height and the desired height 
		float heightRatio = tempBitmapHeight/ mDesiredHeight;
		Log.i("TAG","HR: "+Float.toString(heightRatio)+" WR: "+Float.toString(widthRatio));
		// This will be our scaling factor
		float scalingFactor = Math.min(widthRatio, heightRatio);
		
		Log.i(TAG, "scalingFactor: " + Float.toString(scalingFactor));
		Log.i(TAG, "sampledBitmapWidth: " + Float.toString(tempBitmapWidth) + ", sampledBitmapHeight: " + Float.toString(tempBitmapHeight));
		
		/* -------------     DEBUGGING REMOVE LATER ---------- */
		//if (scalingFactor>1)scalingFactor=1;
			
		try {
			stream = mCtx.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.i("BitmapManager","OpenInputstream failed");
			e1.printStackTrace();
		}
		
		
		
		
		/* ------------------------------------------------------*/
		Bitmap returnBitmap = null;	
		// If the raw asset metrics matchs the desired metrics, we can just decode the Bitmap as is.
		if((tempBitmapHeight == mDesiredHeight && tempBitmapWidth == mDesiredWidth)) {
			bmOptions.inJustDecodeBounds=false;
			bmOptions.inSampleSize=(int) scalingFactor;
			return returnBitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
		}		
		// We need to scale the bitmap
		else {				
			
			// Calculate position of the cropped Bitmap inside the raw asset.
			int left = (int)(tempBitmapWidth / 2) - (int)((scalingFactor*mDesiredWidth) / 2);
			int top = (int)(tempBitmapHeight / 2) - (int)((scalingFactor*mDesiredHeight) / 2);
			int right = left + (int)(scalingFactor*mDesiredWidth);
			int bottom = top + (int)(scalingFactor*mDesiredHeight);
			Log.i("TAG","left: "+left+" top: "+top+" right: "+right+" bottom: "+bottom);
			// A decode used to decode a region of an image
			BitmapRegionDecoder regionDecoder = null;
			
			try {
				// Open the asset as an InputStream.
				//File file = new File(path);			
				//FileInputStream inputStream = new FileInputStream(uri.toString());
				regionDecoder = BitmapRegionDecoder.newInstance(stream, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			if(regionDecoder != null) {
				
				// This time we want to decode completely (With pixels)
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize=(int) scalingFactor;
				Log.i("TAG","inTargetDensity:"+Integer.toString(bmOptions.inTargetDensity)+" InDensity: "+Integer.toString(bmOptions.inDensity));			
				Log.i(TAG, "loadScaledBitmap() - Background scaled with BitmapRegionDecoder");
				
				// Decode the asset
				returnBitmap = regionDecoder.decodeRegion(new Rect(left, top, right, bottom), bmOptions);
				Log.i(TAG, "Scaled width: " + Integer.toString(returnBitmap.getWidth()) + ", scaled height: " + Integer.toString(returnBitmap.getHeight()));
				regionDecoder.recycle();
			}			
		
			
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return returnBitmap;
		}
	}


public Bitmap loadScaledBitmapFromResId(int resId, boolean isPreview) {
	// Fetch adjusted metrics
	
	
	
	// Get an instance of BitmapFactory.Options to handle decoding options
	BitmapFactory.Options  bmOptions = new BitmapFactory.Options();
	
	// Prevent Android from loading a density specific bitmap
	bmOptions.inScaled = false;
	
	// Only decode the metric information of the bitmap (No pixels loaded)
	bmOptions.inJustDecodeBounds = true;
		
	// Sample the asset, without loading the pixels
	BitmapFactory.decodeResource(mCtx.getResources(), resId, bmOptions);
	
	// Get the height of the sampled asset
	float tempBitmapHeight = (float)bmOptions.outHeight;
	
	// Get the width of the sampled asset
	float tempBitmapWidth = (float)bmOptions.outWidth;
	
	RAW_ASSET_HEIGHT = (int)tempBitmapHeight;
	RAW_ASSET_WIDTH = (int)tempBitmapWidth;
	updateWallpaperMetrics(isPreview);
	
	// Calculate the ratio between the the asset width and desired width 
	float widthRatio = tempBitmapWidth / mDesiredWidth;
	
	// Calculate the ratio between the asset height and the desired height 
	float heightRatio = tempBitmapHeight/ mDesiredHeight;
	Log.i("TAG","HR: "+Float.toString(heightRatio)+" WR: "+Float.toString(widthRatio));
	// This will be our scaling factor
	float scalingFactor = Math.min(widthRatio, heightRatio);
	
	Log.i(TAG, "scalingFactor: " + Float.toString(scalingFactor));
	Log.i(TAG, "sampledBitmapWidth: " + Float.toString(tempBitmapWidth) + ", sampledBitmapHeight: " + Float.toString(tempBitmapHeight));
	
	/* -------------     DEBUGGING REMOVE LATER ---------- */
	//if (scalingFactor>1)scalingFactor=1;
		
	
	/* ------------------------------------------------------*/
	Bitmap returnBitmap = null;	
	// If the raw asset metrics matches the desired metrics, we can just decode the Bitmap as is.
	if((tempBitmapHeight == mDesiredHeight && tempBitmapWidth == mDesiredWidth)) {
		bmOptions.inJustDecodeBounds=false;
		bmOptions.inSampleSize=(int) scalingFactor;
		return returnBitmap = BitmapFactory.decodeResource(mCtx.getResources(), resId, bmOptions);
	}		
	// We need to scale the bitmap
	else {				
		
		// Calculate position of the cropped Bitmap inside the raw asset.
		int left = (int)(tempBitmapWidth / 2) - (int)((scalingFactor*mDesiredWidth) / 2);
		int top = (int)(tempBitmapHeight / 2) - (int)((scalingFactor*mDesiredHeight) / 2);
		int right = left + (int)(scalingFactor*mDesiredWidth);
		int bottom = top + (int)(scalingFactor*mDesiredHeight);
		Log.i("TAG","left: "+left+" top: "+top+" right: "+right+" bottom: "+bottom);
		// A decode used to decode a region of an image
		BitmapRegionDecoder regionDecoder = null;
		
		try {
			// Open the asset as an InputStream.
			//File file = new File(path);			
			//FileInputStream inputStream = new FileInputStream(file);
			InputStream is = mCtx.getResources().openRawResource(resId);
			regionDecoder = BitmapRegionDecoder.newInstance(is, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		if(regionDecoder != null) {
			
			// This time we want to decode completely (With pixels)
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize=(int) scalingFactor;
			Log.i("TAG","inTargetDensity:"+Integer.toString(bmOptions.inTargetDensity)+" InDensity: "+Integer.toString(bmOptions.inDensity));			
			Log.i(TAG, "loadScaledBitmap() - Background scaled with BitmapRegionDecoder");
			
			// Decode the asset
			returnBitmap = regionDecoder.decodeRegion(new Rect(left, top, right, bottom), bmOptions);
			Log.i(TAG, "Scaled width: " + Integer.toString(returnBitmap.getWidth()) + ", scaled height: " + Integer.toString(returnBitmap.getHeight()));
			regionDecoder.recycle();
		}			
	
		
		debugMemoryUsage();
		return returnBitmap;
	}
}

private void updateWallpaperMetrics(boolean isPreview) {	
	
	Log.i(TAG, "Updating WallpaperMetrics");
	Log.i(TAG, "Display info: Width - " + Integer.toString(mDisplay.getWidth()) + " Height - " + Integer.toString(mDisplay.getHeight()));

	if(!isPreview) {
		Log.i(TAG, "updateWallpaperMetrics() - isPreview = FALSE");
		
		// Set the desired with to two times the screen width, for parallax scrolling.
		// If the desired width is equal to or greater than the raw asset width, set desired with equal to display width.
		/*
		if((mDisplay.getWidth() * 2) >= RAW_ASSET_WIDTH)
			mDesiredWidth = mDisplay.getWidth();
		else
			mDesiredWidth = mDisplay.getWidth() * 2;
			*/
		mDesiredWidth = mDisplay.getWidth() * 2;
		
	}
	else {
		Log.i(TAG, "updateWallpaperMetrics() - isPreview = TRUE");
		mDesiredWidth = mDisplay.getWidth();
	}
	
	// Get the desired height, no scaling necessary
	mDesiredHeight = mDisplay.getHeight();	
	
	// Hard capping image sizes for memory efficiency
	/*
	if (mDesiredWidth>IMAGE_WIDTH_MAX){
		mDesiredWidth=IMAGE_WIDTH_MAX;
	}
	if (mDesiredHeight>IMAGE_HEIGHT_MAX){
		mDesiredHeight=IMAGE_HEIGHT_MAX;
	}
	*/
	
	Log.i(TAG, "mDesiredWidth: " + Integer.toString(mDesiredWidth) + ", mDesiredHeight: " + Integer.toString(mDesiredHeight));
}

private void debugMemoryUsage(){
	//int Avaliable = mCtx.getMemoryClass();
	float max = Debug.getNativeHeapSize() / 1024;
    float used = Debug.getNativeHeapAllocatedSize() / 1024;
    Log.i("MemoryUsage","Max:" + max + ", Used:" + used);
    Toast.makeText(mCtx,"Max: " + Float.toString(max) + "Used: "+ used, Toast.LENGTH_LONG).show();
}


}
