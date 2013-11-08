package com.morksoftware.plwplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class PainterThread extends Thread {
	// Constants for debugging
	private static final String LOG = "PainterThread";
	
	// Initialization constants
	private static final int INIT_DONE = 0;
	private static final int INIT_AWAITING = 1;
	
	// SurfaceHolder holding the canvas we paint to
	private SurfaceHolder mSurfaceHolder;
	
	// Context used to fetch resources
	private Context mCtx;
	
	// Thread states
	private boolean mRun;
	private boolean mWait;	
	
	// WallpaperService states
	private boolean mPreview;
	
	// Resources
	private BackgroundBitmapManager mBackgroundManager;
	private Bitmap mBackgroundBitmap;
	private Sprite mSprite;
	
	// Notifies thread that a new background image is needed
	private int mBackgroundInit = INIT_AWAITING;
	private int mSpriteInit = INIT_AWAITING;
	
	// Preferences
	private PrefsHelper mPrefs;
	private String mBackgroundSource;
	private String mBackgroundPath;
	private int mBackgroundID;
	private String wallpaperMode;
	
	// Screen sizes and offset
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private int mPixelOffset;
	private float mOffset;
	private float mOffsetStep;
	
	private boolean mFirstTap = false;
	private boolean mSecondTap = false;
	private long mLastTapTime;
	private int mDoubleTapTime = 200;
	
	private int mTapX = 0;
	private int mTapY = 0;

	/*
	 * Constructors
	 */	
	PainterThread(SurfaceHolder surfaceHolder, Context ctx, boolean isPreview) {
		// Bind supplied SurfaceHolder
		mSurfaceHolder = surfaceHolder;
		
		// Bind supplied Context
		mCtx = ctx;
		
		// Bind isPreview
		mPreview = isPreview;
				
		// Initialize PrefsHelper
		mPrefs = new PrefsHelper(ctx);
		
		if(mPrefs.getFirstRun()) {
			// If this is the first run, load default values
			mPrefs.setDefaultPreferences(ctx);
		}
		
		// This is our background manager
		mBackgroundManager = new BackgroundBitmapManager(ctx);
		
		// We don't want to paint right away
		mWait = true;
	}
	
	/*
	 * Main loop
	 */	
	@Override
	public void run() {		
		// Set mRun = true, to begin looping
		mRun = true;
		
		// The canvas we are going to paint to
		Canvas c = null;
		
		// Lets loop
		while(mRun) {
			try {			
				// Fetch canvas from our SurfaceHolder
				c = mSurfaceHolder.lockCanvas();
				
                synchronized(mSurfaceHolder) { 
                	// We paint inside this synchronized call
                	drawBackground(c);   
                	
                	if(mSprite != null) {
                		mSprite.doDraw(c);
                    	
                    	handleTaps();
                	}          
                	else {
                		pausePainting();
                	}
                }
				
			} finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);            
                }
            }		
			
			synchronized(this) {
				if (mWait) {
                    try {
                    	Log.i(LOG, "Paused...");
                    	// Pause thread
                        wait();
                    } catch (Exception e) {
                    	// We do nothing...
                    }
                }
			}
		}
	}
	
	/*
	 *  Resource initialization and releasing
	 */
	private void initResources() {		
		// What shall we initialize?
		wallpaperMode = mPrefs.getWallpaperMode();
		mBackgroundSource= mPrefs.getBackgroundSource();
		if (mBackgroundSource.equals(mPrefs.PREF_BACKGROUND_SOURCE_INCLUDED)){
			mBackgroundID = mPrefs.getWallpaperBackgroundID();
			mBackgroundBitmap = mBackgroundManager.loadScaledBitmapFromResId(mBackgroundID, mPreview);
		}
		else {
			mBackgroundPath = mPrefs.getWallpaperBackgroundPath();
			mBackgroundBitmap = mBackgroundManager.loadScaledBitmapFromPath(mBackgroundPath, mPreview);
		}
		
		if(mBackgroundInit == INIT_AWAITING) {
			Log.i("tag", "Loading Background...");
			
			mBackgroundInit = INIT_DONE;
			
			Log.i("TAG", Integer.toString(mBackgroundID));
			
			//mBackgroundBitmap = mBackgroundManager.loadScaledBitmapFromPath("/mnt/sdcard/DCIM/Camera/IMG_20130820_214915.jpg", mPreview);
			
			// Always init background image
		}
			
		// If INIT_ALL, init Sprite too
		if(mSpriteInit == INIT_AWAITING && !mPreview) {	
			
			mSpriteInit = INIT_DONE;
			
			Log.i("tag", "Loading Sprite...");
			if(wallpaperMode.equals(PrefsHelper.PREF_WALLPAPER_MODE_LABS)) {				
				// Init Wheatley
				mSprite = new WheatleySprite();
				mSprite.initResources(mCtx, mSurfaceWidth, mSurfaceHeight);				
			}
			else {
				// Init SpaceCore
				mSprite = new SpaceCoreSprite();
				mSprite.initResources(mCtx, mSurfaceWidth, mSurfaceHeight);
			}
		}
	}
		
	public void releaseResources() {
		// Recycle background and null it
		if(mBackgroundBitmap != null && mBackgroundInit == INIT_AWAITING) {
			Log.i("", "RELEASING BACKGROUND");
			
			mBackgroundBitmap.recycle();
			mBackgroundBitmap = null;
		}
		
		// Notify Sprite to release resources;
		if(mSprite != null && mSpriteInit == INIT_AWAITING) {
			Log.i("", "RELEASING SPRITE");
			
			mSprite.releaseResources();
			mSprite = null;
		}
	}	
	
	public void notifyPreferenceChange(String key) {
		if(key.equalsIgnoreCase(PrefsHelper.PREF_LABS_BACKGROUND) || key.equalsIgnoreCase(PrefsHelper.PREF_SPACE_BACKGROUND)) {
			Log.i(LOG, "Background pref change");
			mBackgroundInit = INIT_AWAITING;
		}
		else if(key.equalsIgnoreCase(PrefsHelper.PREF_WALLPAPER_MODE)) {		
			// Initialize new background and Sprite
			Log.i(LOG, "Sprite pref change");
			
			mBackgroundInit = INIT_AWAITING;
			mSpriteInit = INIT_AWAITING;
		}
	}
		
	
	/*
	 * Drawing functions
	 */
	private void drawBackground(Canvas c) {		
		if(mBackgroundBitmap != null) {
			c.drawBitmap(mBackgroundBitmap, (mSurfaceWidth - mBackgroundBitmap.getWidth()) * mOffset, 0, null);
		}
	}
	
	
	/*
	 * Painting states
	 */
	public void resumePainting() {		
		// Check to see if we need a new background bitmap
		if(mBackgroundInit == INIT_AWAITING) {
			
			Log.i(LOG, "HAVE TO INIT RESOURCES FROM RESUME");
						
			// Release the resources we already are holding
			releaseResources();
			
			// Initialize new ones
			initResources();
		}
		
		mWait = false;
        synchronized(this) { 
            notify();
        }
    }
	
	public void pausePainting() { 
		Log.i(LOG, "pausePainting");
		
		mWait = true;
		synchronized(this) {
			notify();
		}
	} 	
	
    public void stopPainting() {
    	Log.i(LOG, "stopPainting");
    	
    	mBackgroundInit = INIT_AWAITING;
    	mSpriteInit = INIT_AWAITING;
    	
        mWait = false;
        mRun = false;
        synchronized(this) {
            notify();
        }
    }
    
    /*
     * Function used to controll canvas width, height, offsets etc.
     */
    public void setSurfaceSize(int width, int height) {
    	
    	if(width != mSurfaceWidth || height != mSurfaceHeight) { 
        	mSurfaceWidth = width;
        	mSurfaceHeight = height;
        	
        	// Surface has changed dimensions, so we need a new background
        	mBackgroundInit = INIT_AWAITING;
    	}
    	
    	//Log.i(LOG, "Width: " + Integer.toString(width) + ", Height: " + Integer.toString(height) + ", Format: " + Integer.toString(format) + ", Desired width: " + Integer.toString(desiredWidth) + ", Desired height: " + Integer.toString(desiredHeight));
    }
    
    public void setSurfaceOffsets(float xOffset, float xOffsetStep, int xPixelOffset) {    	
    	//resumePainting();
    	
    	mOffset = xOffset;
    	mOffsetStep = xOffsetStep;
    	mPixelOffset = xPixelOffset;
    	
    	if(mSprite != null) {
    		mSprite.doWallpaperScroll(xOffset, xOffsetStep, xPixelOffset, mSurfaceWidth);
    	}
    	//Log.i(LOG, "Offset: " + Float.toString(xOffset) + ", PixelOffset: " + Integer.toString(xPixelOffset) + ", OffsetStep: " + Float.toString(xOffsetStep));
    }
    
    /*
     * Handling tap events
     */
    public void doTapEvent(int x, int y) {
    	Log.i(LOG, "Tap event occured at: " + Integer.toString(x) + ", " + Integer.toString(y));
    	
    	mTapX = x;
    	mTapY = y;
    	
    	if(!mFirstTap) {
    		mFirstTap = !mFirstTap;
    		mLastTapTime = System.currentTimeMillis();
    	}
    	else if(!mSecondTap) {
    		mSecondTap = !mSecondTap;
    	}    	
    }
            
    private void handleTaps() {    	
    	if(mFirstTap) {    		
    		if(System.currentTimeMillis() > mLastTapTime + mDoubleTapTime) {
    			handleSingleTap();  
    			mFirstTap = !mFirstTap;
    		}
    		else {
    			if(mSecondTap) {
    				handleDoubleTap();
    				
    				mFirstTap = !mFirstTap;
    				mSecondTap = !mSecondTap;
    			}
    		}
    	}
    }
    
    private void handleSingleTap() {    	
    	mSprite.doSingleTapEvent(mTapX, mTapY);    	
    }
    
    private void handleDoubleTap() {    	
    	mSprite.doDoubleTapEvent(mTapX, mTapY);
    }
}
