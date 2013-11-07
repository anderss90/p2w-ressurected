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
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

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
		updateWallpaperMetrics(isPreview);
		
		// Calculate the ratio between the the asset width and desired width 
		float widthRatio = RAW_ASSET_WIDTH / mDesiredWidth;
		
		// Calculate the ratio between the asset height and the desired height 
		float heightRatio = RAW_ASSET_HEIGHT / mDesiredHeight;
		
		// This will be our scaling factor
		int scalingFactor = (int) Math.min(widthRatio, heightRatio);
		
		Log.i(TAG, "scalingFactor: " + Integer.toString(scalingFactor));
		
		// Get an instance of BitmapFactory.Options to handle decoding options
		BitmapFactory.Options  bmOptions = new BitmapFactory.Options();
		
		// Prevent Android from loading a density specific bitmap
		bmOptions.inScaled = false;
		
		// Only decode the metric information of the bitmap (No pixels loaded)
		bmOptions.inJustDecodeBounds = true;
		
		// If the scaling factor is greater then 1, a scaled bitmap will be loaded.
		bmOptions.inSampleSize = scalingFactor;
					
		// Sample the asset, without loading the pixels
		BitmapFactory.decodeFile(path, bmOptions);
		
		// Get the height of the sampled asset
		int tempBitmapHeight = bmOptions.outHeight;
		
		// Get the width of the sampled asset
		int tempBitmapWidth = bmOptions.outWidth;
		
		Log.i(TAG, "sampledBitmapWidth: " + Integer.toString(tempBitmapWidth) + ", sampledBitmapHeight: " + Integer.toString(tempBitmapHeight));
		
		
		Bitmap returnBitmap = null;		

		// If the raw asset metrics matchs the desired metrics, we can just decode the Bitmap as is.
		if(tempBitmapHeight == mDesiredHeight && tempBitmapWidth == mDesiredWidth) {
			return returnBitmap = BitmapFactory.decodeFile(path);
		}		
		// We need to scale the bitmap
		else {				
			
			// Calculate position of the cropped Bitmap inside the raw asset.
			int left = (tempBitmapWidth / 2) - (mDesiredWidth / 2);
			int top = (tempBitmapHeight / 2) - (mDesiredHeight / 2);
			int right = left + mDesiredWidth;
			int bottom = top + mDesiredHeight;
				
			// A decode used to decode a region of an image
			BitmapRegionDecoder regionDecoder = null;
			
			try {
				// Open the asset as an InputStream.
				File file = new File(path);			
				FileInputStream inputStream = new FileInputStream(file);
				
				regionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			if(regionDecoder != null) {
				
				// This time we want to decode completely (With pixels)
				bmOptions.inJustDecodeBounds = false;
							
				Log.i(TAG, "loadScaledBitmap() - Background scaled with BitmapRegionDecoder");
				
				// Decode the asset
				returnBitmap = regionDecoder.decodeRegion(new Rect(left, top, right, bottom), bmOptions);
			}			
		
			Log.i(TAG, "Scaled width: " + Integer.toString(returnBitmap.getWidth()) + ", scaled height: " + Integer.toString(returnBitmap.getHeight()));

			return returnBitmap;
		}
	}
	
	/**
	 * A highly memory efficient Bitmap loader. Returns a 
	 * scaled Bitmap.
	 * 
	 * @param resId The id of the resource you want to load
	 * @param isPreview Is the wallpaper running in preview mode?
	 * 
	 * @return A scaled Bitmap
	 */
	public Bitmap loadScaledBitmapFromResId(int resId, boolean isPreview) {
		
		// Fetch adjusted metrics
		updateWallpaperMetrics(isPreview);
		
		// Calculate the ratio between the the asset width and desired width 
		float widthRatio = RAW_ASSET_WIDTH / mDesiredWidth;
		
		// Calculate the ratio between the asset height and the desired height 
		float heightRatio = RAW_ASSET_HEIGHT / mDesiredHeight;
		
		// This will be our scaling factor
		int scalingFactor = (int) Math.min(widthRatio, heightRatio);
		
		Log.i(TAG, "scalingFactor: " + Integer.toString(scalingFactor));
		
		// Get an instance of BitmapFactory.Options to handle decoding options
		BitmapFactory.Options  bmOptions = new BitmapFactory.Options();
		
		// Prevent Android from loading a density specific bitmap
		bmOptions.inScaled = false;
		
		// Only decode the metric information of the bitmap (No pixels loaded)
		bmOptions.inJustDecodeBounds = true;
		
		// If the scaling factor is greater then 1, a scaled bitmap will be loaded.
		bmOptions.inSampleSize = scalingFactor;
					
		// Sample the asset, without loading the pixels
		BitmapFactory.decodeResource(mCtx.getResources(), resId, bmOptions);
		
		// Get the height of the sampled asset
		int tempBitmapHeight = bmOptions.outHeight;
		
		// Get the width of the sampled asset
		int tempBitmapWidth = bmOptions.outWidth;
		
		Log.i(TAG, "sampledBitmapWidth: " + Integer.toString(tempBitmapWidth) + ", sampledBitmapHeight: " + Integer.toString(tempBitmapHeight));
		
		
		Bitmap returnBitmap = null;		

		// If the raw asset metrics matchs the desired metrics, we can just decode the Bitmap as is.
		if(tempBitmapHeight == mDesiredHeight && tempBitmapWidth == mDesiredWidth) {
			return returnBitmap = BitmapFactory.decodeResource(mCtx.getResources(), resId);
		}		
		// We need to scale the bitmap
		else {				
			
			// Calculate position of the cropped Bitmap inside the raw asset.
			int left = (tempBitmapWidth / 2) - (mDesiredWidth / 2);
			int top = (tempBitmapHeight / 2) - (mDesiredHeight / 2);
			int right = left + mDesiredWidth;
			int bottom = top + mDesiredHeight;
		
			// Open the asset as an InputStream.
			InputStream is = mCtx.getResources().openRawResource(resId);
			
			// A decode used to decode a region of an image
			BitmapRegionDecoder regionDecoder = null;
			
			try {
				regionDecoder = BitmapRegionDecoder.newInstance(is, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			if(regionDecoder != null) {
				
				// This time we want to decode completely (With pixels)
				bmOptions.inJustDecodeBounds = false;
							
				Log.i(TAG, "loadScaledBitmap() - Background scaled with BitmapRegionDecoder");
				
				// Decode the asset
				returnBitmap = regionDecoder.decodeRegion(new Rect(left, top, right, bottom), bmOptions);
			}			
		
			Log.i(TAG, "Scaled width: " + Integer.toString(returnBitmap.getWidth()) + ", scaled height: " + Integer.toString(returnBitmap.getHeight()));

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
			if((mDisplay.getWidth() * 2) >= RAW_ASSET_WIDTH)
				mDesiredWidth = mDisplay.getWidth();
			else
				mDesiredWidth = mDisplay.getWidth() * 2;
		}
		else {
			Log.i(TAG, "updateWallpaperMetrics() - isPreview = TRUE");
			mDesiredWidth = mDisplay.getWidth();
		}
		
		// Get the desired height, no scaling necessary
		mDesiredHeight = mDisplay.getHeight();		
		
		Log.i(TAG, "mDesiredWidth: " + Integer.toString(mDesiredWidth) + ", mDesiredHeight: " + Integer.toString(mDesiredHeight));
	}
}
