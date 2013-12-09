package com.morksoftware.plwplus;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class PortalWallpaperService extends WallpaperService {
	
	// Constants used for debugging
	private static final String LOG = "PortalWallpaperService";
	private String mID;
	
	@Override
	public void onCreate() {	
		super.onCreate();
		
		Random rand = new Random();
		mID = Integer.toString(rand.nextInt(9));
		
		Log.i(LOG, mID + " - onCreate");
	}

	@Override
	public Engine onCreateEngine() {
		Log.i(LOG, mID + " - onCreateEngine");
		return new PortalEngine();
	}
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOG, mID + " - onDestroy");
	}

	// PortalEngine
	private class PortalEngine extends Engine implements OnSharedPreferenceChangeListener {
		@SuppressLint("NewApi")
		public PortalEngine() {
			   if(Build.VERSION.SDK_INT >= 15 ){
			   this.setOffsetNotificationsEnabled(true);
			   }
		}
		
		// Constants used for debugging
		private static final String LOG = "PortalEngine";
		private String mID;
		
		// The thread doing all the work
		private PainterThread mThread;
		
		
		//
		private float mOffset;
		private float mOffsetStep;
		private int mPixelOffset;
		
		// Preference object
		PrefsHelper mPrefs;
				
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {	
			super.onCreate(surfaceHolder);
			
			Random rand = new Random();
			mID = Integer.toString(rand.nextInt(9));
			
			Log.i(LOG, mID + " - onCreate");
			
			/* DEBUG DONE*/		
			mPrefs = new PrefsHelper(getApplicationContext());
			mPrefs.registerOnSharedPrefListener(this);		
			mThread = new PainterThread(surfaceHolder, getApplicationContext(), isPreview());
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			
			//Log.i(LOG, "onOffsetsChanged");
						
			if(mOffset != xOffset || mOffsetStep != xOffsetStep || mPixelOffset != xPixelOffset) {
				mOffset = xOffset;
				mOffsetStep = xOffsetStep;
				mPixelOffset = xPixelOffset;
				
				mThread.setSurfaceOffsets(xOffset, xOffsetStep, xPixelOffset);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			
			Log.i(LOG, mID + " - onSurfaceChanged");
			Log.i(LOG, "Width: " + Integer.toString(width) + ", Height: " + Integer.toString(height));
			
			/* DEBUG DONE */
			
			Log.i(LOG, "Desired width: " + Integer.toString(getDesiredMinimumWidth()) + ", Desired height: " + Integer.toString(getDesiredMinimumHeight()));
			
			mThread.setSurfaceSize(width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			
			Log.i(LOG, mID + " - onSurfaceCreated");
			
			/* DEBUG DONE */
			
			try {
				mThread.start();
			}
			catch(IllegalThreadStateException e) {
				// Thread already started
			}catch(IllegalStateException e) {
				// Thread already started
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			
			Log.i(LOG, mID + " - onSurfaceDestroyed");
			
			/* DEBUG DONE */
			
			// We want to stop our thread and release any resources its holder
			mThread.stopPainting();
			mThread.releaseResources();
			
			boolean retry = true;               
            while (retry) {
            	try {
            		Log.i(LOG, mID + " - onSurfaceDestroy - Trying to kill thread");
            		mThread.join();
            		
            		if(!mThread.isAlive()) {
            			Log.i(LOG, mID + " - onSurfaceDestroy - Thread dead");
            			retry = false;
            		}
            	} 
            	catch (InterruptedException e) {
            		Log.i(LOG, mID + " - onSurfaceDestroy - Thread was interupted while trying to kill");
            	}
            }
            
            mPrefs.registerOnSharedPrefListener(this);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			
			Log.i(LOG, mID + " - onVisibilityChanged");
			
			/* DEBUG DONE */
			
        	// Pausing and resuming wallpaper based on whether or not its visible
        	if(visible) {
                mThread.resumePainting();
            } else {
                mThread.pausePainting();
            }
		}		
		
		@Override
		public void onDestroy() {
			super.onDestroy();
				
			Log.i(LOG, mID + " - onDestroy");	
			
			/* DEBUG DONE */		
		}
		
        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
        	super.onCommand(action, x, y, z, extras, resultRequested);   
        	
        	// Notify the painting thread that the user have touch the screen
        	if(action.equalsIgnoreCase("android.wallpaper.tap")) {              	
        		mThread.doTapEvent(x, y);
        	}
			return null;        	
        }       

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if(mThread!=null) mThread.notifyPreferenceChange(key);
		}
	}
}