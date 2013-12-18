package com.morksoftware.plwplus;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class PainterThread extends Thread implements OnTapListener {
	// Constants for debugging
	private static final String LOG = "PainterThread";
	
	// Initialization constants
	private static final int INIT_DONE = 0;
	private static final int INIT_AWAITING = 1;
	
	// SurfaceHolder holding the canvas we paint to
	private SurfaceHolder mSurfaceHolder;
	private Canvas c;
	
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
	Matrix mScaleMatrix = new Matrix();
	float translation;
	float widthRatio;
	float heightRatio;
	float scalingFactor;
	
	
	// Taps
	private boolean mFirstTap = false;
	private boolean mSecondTap = false;
	private long mLastTapTime;
	private int mDoubleTapTime;
	
	private int mTapX = 0;
	private int mTapY = 0;
	
	// Scheduling
	private long scheduleInterval;
	private long mlastLoopTime;
	private long mCurrentTime;
	
	//Display
	private Display mDisplay;
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
		
		//DEBUGGING REMOVE LATER
		//mPrefs.setDefaultPreferences(ctx);
		
		// This is our background manager
		mBackgroundManager = new BackgroundBitmapManager(ctx);
		
		// We don't want to paint right away
		mWait = true;
		
		//scheduling
		
		mDoubleTapTime = 200;
		scheduleInterval=20;
		
		//Display
		mDisplay = ((WindowManager) mCtx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				
		
	}
	/*
	 * Main loop
	 */	
	@Override
	public void run() {		
		// Lets loop
		
		// commenting out to try scheduling instead of while loop
		// Set mRun = true, to begin looping
		mRun = true;
		// The canvas we are going to paint to
		c = null;
		while(mRun) {
			mCurrentTime = System.currentTimeMillis();
			if(mCurrentTime > mlastLoopTime + scheduleInterval){
				try {			
					// Fetch canvas from our SurfaceHolder
					c = mSurfaceHolder.lockCanvas();
					
	                synchronized(mSurfaceHolder) { 
	                	// We paint inside this synchronized call
	                	drawBackgroundWithMatrix(c);   
	                	
	                	if(mSprite != null) {
	                		mSprite.doDraw(c);
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
				mlastLoopTime=System.currentTimeMillis();
			}
			
		}
		 
	}

	/*
	 *  Resource initialization and releasing
	 */
	private void initResources() {		
		// What shall we initialize?
		mPrefs.setPremiumFromUtils();
		wallpaperMode = mPrefs.getWallpaperMode();
		mBackgroundSource= mPrefs.getBackgroundSource();
		
		if (mBackgroundSource.equals(mPrefs.PREF_BACKGROUND_SOURCE_INCLUDED)|| mPrefs.getPremium()==false){
			mBackgroundID = mPrefs.getWallpaperBackgroundID();
			mBackgroundBitmap = mBackgroundManager.loadScaledBitmapFromResId(mBackgroundID, mPreview);
		}
		else {
			mBackgroundPath = mPrefs.getWallpaperBackgroundPath();
			mBackgroundBitmap = mBackgroundManager.loadScaledBitmapFromPath(mBackgroundPath, mPreview);
			
		}
		if (mBackgroundBitmap!=null){
			if (mDisplay.getHeight()<mDisplay.getWidth()) { // Landscape mode
				widthRatio = (float)mSurfaceWidth/mBackgroundBitmap.getWidth();
				heightRatio = (float)mSurfaceHeight/mBackgroundBitmap.getHeight();
			}
			else {
				widthRatio = (float)2*mSurfaceWidth/mBackgroundBitmap.getWidth();
				heightRatio = (float)mSurfaceHeight/mBackgroundBitmap.getHeight();
			}
			
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
		if(key.equalsIgnoreCase(PrefsHelper.PREF_LABS_BACKGROUND_PATH) || key.equalsIgnoreCase(PrefsHelper.PREF_LABS_BACKGROUND_ID)
				|| key.equalsIgnoreCase(PrefsHelper.PREF_SPACE_BACKGROUND_PATH) || key.equalsIgnoreCase(PrefsHelper.PREF_SPACE_BACKGROUND_ID)) {
			Log.i(LOG, "Background pref change");
			mBackgroundInit = INIT_AWAITING;
		}
		else if(key.equalsIgnoreCase(PrefsHelper.PREF_WALLPAPER_MODE)) {		
			// Initialize new background and Sprite
			Log.i(LOG, "Sprite pref change");
			
			mBackgroundInit = INIT_AWAITING;
			mSpriteInit = INIT_AWAITING;
		}
		if(mSprite!=null)mSprite.onSharedPreferenceChanged();
	}
		
	
	/*
	 * Drawing functions
	 */
	private void drawBackground(Canvas c) {		
		if(mBackgroundBitmap != null) {
			c.drawBitmap(mBackgroundBitmap, (mSurfaceWidth - mBackgroundBitmap.getWidth()) * mOffset, 0, null);
		}
	}
	
	private void drawBackgroundWithMatrix(Canvas c) {		
		if(mBackgroundBitmap != null && mScaleMatrix!=null && c!=null) {
			translation= (float)((-mSurfaceWidth) * mOffset);
			//Log.i("Painterthread","translation: "+translation);
			mScaleMatrix.reset();
			
			/* Not necessary every iteration
			widthRatio = (float)2*mSurfaceWidth/mBackgroundBitmap.getWidth();
			heightRatio = (float)mSurfaceHeight/mBackgroundBitmap.getHeight();
			*/
			//Log.i("PainterThread","HR: "+Float.toString(heightRatio)+" WR: "+Float.toString(widthRatio)+
					//" SF: "+scalingFactor+" mOffset: "+mOffset+" trans: "+translation);
			scalingFactor = Math.max(widthRatio, heightRatio);
			mScaleMatrix.setScale(scalingFactor, scalingFactor);
			mScaleMatrix.postTranslate(translation,0);
			c.drawBitmap(mBackgroundBitmap,mScaleMatrix, null);
		}
	}
	
	
	/*
	 * Painting states
	 */
	public void resumePainting() {	
		mPrefs.setPremiumFromUtils();
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
     * Function used to control canvas width, height, offsets etc.
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
    	
    	
    	if (mDisplay.getHeight()<mDisplay.getWidth()) {
		    // TODO: add logic for landscape mode here 
			
		}
    	else{
    		mOffset = xOffset;
        	mOffsetStep = xOffsetStep;
        	mPixelOffset = xPixelOffset;
    		if(mSprite != null) {
        		mSprite.doWallpaperScroll(xOffset, xOffsetStep, xPixelOffset, mSurfaceWidth);
        	}
    		
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
            
//    private void handleTaps() {    	
//    	if(mFirstTap) {    		
//    		if(System.currentTimeMillis() > mLastTapTime + mDoubleTapTime) {
//    			handleSingleTap();  
//    			mFirstTap = !mFirstTap;
//    		}
//    		else {
//    			if(mSecondTap) {
//    				handleDoubleTap();
//    				
//    				mFirstTap = !mFirstTap;
//    				mSecondTap = !mSecondTap;
//    			}
//    		}
//    	}
//    }
    
//    private void handleSingleTap() {    	
//    	mSprite.onSingleTap(mTapX, mTapY);    	
//    }
//    
//    private void handleDoubleTap() {    	
//    	mSprite.onDoubleTap(mTapX, mTapY);
//    }
    
	@Override
	public boolean onSingleTap(float x, float y) {
		
		if(mSprite != null) {
			mSprite.onSingleTap(x, y);
		}
		
		// Not yet implemented
		return false;
	}
	@Override
	public boolean onDoubleTap(float x, float y) {
		
		if(mSprite != null) {
			mSprite.onDoubleTap(x, y);
		}
		
		// Not yet implemented
		return false;
	}
}
