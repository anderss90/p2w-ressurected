package com.morksoftware.plwplus;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import android.view.WindowManager;


import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class WheatleySprite extends Sprite {
	// Constants
	private static final int WHEATLEY_START_POSITION_X = 0;
	private static final double WHEATLEY_MINIMUM_SPEED = 1.0;
	
	public static final int WHEATLEY_TAP_ACTION_Y = 1;
	public static final int WHEATLEY_TAP_ACTION_YZ = 2;
	
	// The bitmap we want drawn to our canvas
	private Bitmap mBitmap;
	
	// Sprite values
	private int mSpriteWidth;
	private int mSpriteHeigth;
	private int mFrameCount;
	
	// Source and destination rectangles
	private Rect mDestRect;
	private Rect mSrcRect;
	
	// Velocity values
	private float mBaseSpeed=(float)14;
	private float mMaxSpeed=(float)7;	
	private float mAccel=(float)0.5;
	private float mAccelZone;
	private float mAccelZoneX;
	private double mSpeedX = 1;
	private double mSpeedY = 1;
	private double mSpeedZ = 1;
	
	// Direction values. 1 is right, 0 is left (WATTAFAKK)
	private boolean mDirectionX = true;
	private boolean mDirectionY = true;
	private boolean mDirectionZ = true;	
	private boolean mRandom = false;
	
	// Position values
	private int mPositionX = 0;
	private int mPositionY = 0;
	private int mPositionZ = 0;
	private int mNewPositionX = 0;
	private int mNewPositionY = 0;
	private int mNewPositionZ = 0; 
	private int mLastPositionX;
	private int mLastPositionY;
	private int mLastPositionZ;
    private int mDownPositionY = -52;
    private int mUpPositionY = -352;
    
	//position logic
	private boolean mNeedPositionXChange = false;
	private boolean mNeedPositionYChange = false;
	private boolean mNeedPositionZChange = false;	
	
	// Offset. {0.0.25,0.5 osv.... helt opp til 1}
	private float mOffsetX = 0;
	private float mOffsetStepX = 0;
	private float mPixelOffsetX = 0;
	private float mLastStalkerOffset;

	//timings
	private long mLastPositionUpdateTime = 0;
	private int mPositionUpdatePeriod = 20;
	private Random mRandomGen;
	private long mLastActivityTime = 0;
    private int mActivityIntervalDefault = 50000;
	private int mActivityInterval = 0;
	
	// Screen values
	private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    
	// States
	private boolean mFirstRun = true;
	private boolean mDraw = false;
	
	// Animation controls
	private long mCurrentTime;
	private long mLastAnimationUpdateTime = 0;
	private int mAnimationUpdatePeriod = 50;
	private int mCurrentFrame = 0;
	private boolean mReverseAnimation = false;

    // Sounds
    private int[] mSoundResources = {R.raw.labs01, R.raw.labs02, R.raw.labs04, R.raw.labs05, R.raw.labs06, R.raw.labs07, R.raw.labs08, R.raw.labs09, 
			R.raw.labs10, R.raw.labs11, R.raw.labs12, R.raw.labs13, R.raw.labs14, R.raw.labs15, R.raw.labs16, R.raw.labs17, R.raw.labs18};
    private int mNumberOfSounds=18;
    private boolean mIsPlayingSound = false;
    private Context mCtx;
    private int mPreviousSound;
	// Settings
    private PrefsHelper mPrefs;
	private int mTapAction = WHEATLEY_TAP_ACTION_YZ;
	private String mWheatleySnapPosition;
	private boolean mEnableSound;
	private boolean mEnableRandomMovement;
	private boolean mEnableRandomMovementY;
	private int mRandomMovementInterval;
	

	@Override
	public void initResources(Context ctx, int screenWidth, int screenHeight) {

		mFirstRun = true;
        mCtx = ctx;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inInputShareable = true;
		options.inPurgeable = true;
		
		mBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.spritesheet_wheatley_1, options);
		mRandomGen=new Random();
		mFrameCount = 29;
		
		if(mTapAction == 2) {
			mPositionZ = -200;
		}
		
		// init sounds
		
		
				
		
		mSpriteWidth = mBitmap.getWidth()/mFrameCount;
		mSpriteHeigth = mBitmap.getHeight();		
		
		mPositionY = (-1)*mSpriteHeigth + mDownPositionY;
		mNewPositionY = (-1)*mSpriteHeigth;
		
		mPositionX = WHEATLEY_START_POSITION_X;
		
		mDestRect = new Rect(mPositionX, mPositionY, mSpriteWidth+mPositionX,mSpriteHeigth+mPositionY);
		mSrcRect = new Rect(0,0,mSpriteWidth,mSpriteHeigth);
		
		mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
		
		DisplayMetrics dm = new DisplayMetrics();
	    ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

		// these will return the actual dpi horizontally and vertically
		float xDpi = dm.xdpi;
		
		mBaseSpeed = (float) (4*0.028*xDpi);
		mAccel=(float) 0.5;
		Log.i("MAX OG ACCEL", Float.toString(mMaxSpeed) + ", " + Float.toString(mAccel));
		
		mAccelZone = (float) ((Math.pow(mMaxSpeed, 2)) / (2*mAccel));
		
		mDraw = true;
		setNewPositionZ(0);
		setNewPositionY(0,false);	
		
		mLastActivityTime = System.currentTimeMillis();
        mUpPositionY = -mSpriteHeigth+mDownPositionY;
        
        //get settings
        mPrefs = new PrefsHelper(ctx);
        onSharedPreferenceChanged();
        mEnableRandomMovementY=false;
        if (mScreenWidth>mScreenHeight){
        	
        }
        
	}
	@Override
	public void onSharedPreferenceChanged(){
		mWheatleySnapPosition= mPrefs.getWheatleySnapPosition();
		mMaxSpeed=(float) (mPrefs.getMovementSpeed()*mBaseSpeed*0.01);
		mAccel=(float)mMaxSpeed/10;
		mEnableSound=mPrefs.getEnableSound();
		mEnableRandomMovement=mPrefs.getEnableRandomMovement();
		mRandomMovementInterval=mPrefs.getRandomMovementInterval()*1000;
		//Log.i("Wheatlet_onsharedpref", Float.toString(mMaxSpeed) + ", " + Float.toString(mAccel));
		Log.i("Wheatley_OnPrefChanged","mRandomMovementInterval: "+mRandomMovementInterval);
	}
	
	@Override
	public void releaseResources() {
		mBitmap.recycle();
		mBitmap = null;
	}
	
	@Override
	public void doDraw(Canvas c) {
		if(mDraw) {
			
			mCurrentTime = System.currentTimeMillis();			
			
			updatePosition();
			updateAnimation();		
			if (mEnableRandomMovement){
				if (mCurrentTime > mLastActivityTime + mRandomMovementInterval){
					randomMovement();
				}
			}
			c.drawBitmap(mBitmap, mSrcRect, mDestRect, null);
			updateVisibility();
			if(mFirstRun) {
				Log.i("", "mLastOffset: " + Float.toString(mLastStalkerOffset));
				mFirstRun = false;
			}
		}		
		doSomeLogging();		
	}
	
	private void updateAnimation() {			
		if(mCurrentTime > mLastAnimationUpdateTime + mAnimationUpdatePeriod) { // OLD IF-CONDITION: (mCurrentTime > mLastAnimationUpdateTime + mAnimationUpdatePeriod)
			mSrcRect.left = mCurrentFrame*mSpriteWidth;
			mSrcRect.right = mSrcRect.left + mSpriteWidth;			
			
		
			// Update current shown frame
			if(mReverseAnimation) {
				mCurrentFrame -= 1;
			}
			else {
				mCurrentFrame += 1;
			}
			
			// Update direction on frames			
			if(mCurrentFrame >= (mFrameCount-2)) {
				mReverseAnimation = true;
			}
			if(mCurrentFrame <= 0) {
				mReverseAnimation = false;
			}
			else {			
				// Register animation update
				mLastAnimationUpdateTime = mCurrentTime;
			}
		}		
	}
	
	private void updatePosition() {
		if(mCurrentTime > mLastPositionUpdateTime + mPositionUpdatePeriod) { // OLD IF: 
			
			doStalkerMode();

            if (mPositionY == mUpPositionY || mPositionX < -mSpriteWidth || mPositionX > mScreenWidth) {
                mActivityInterval = mActivityIntervalDefault/2;
            }
            else {
                mActivityInterval = mActivityIntervalDefault;
            }
            
			if(mPositionY == mDownPositionY) {
				if(mNeedPositionZChange ) {
					updatePositionZ();
				}
				else if(mNeedPositionYChange) {
					updatePositionY();
				}
				else if(mNeedPositionXChange) {
					updatePositionX();
				}	
			}
			else {
				if(mNeedPositionYChange) {
					updatePositionY();
				}
				else if(mNeedPositionZChange) {
					updatePositionZ();
				}
				else if(mNeedPositionXChange) {
					updatePositionX();
				}	
			}
			
			mDestRect.top = (int) ((double)   mPositionY + (mSpriteWidth*(-mPositionZ / 2000.0)));
			mDestRect.bottom = (int) ((double) (mSpriteHeigth * (1.0 + (mPositionZ / 2000.0))) + mPositionY);
			
			mDestRect.left = (int) ((double)   mPositionX + (mSpriteWidth * (-mPositionZ / 2000.0)));
			mDestRect.right = (int) ((double) (mSpriteWidth * (1.0 + (mPositionZ / 2000.0))) + mPositionX);			
						
			mLastPositionUpdateTime = mCurrentTime;			
		}	
	}
	
	private void updateVisibility() {
		if(mPositionY == mUpPositionY && !mFirstRun && !mRandom) {
			mDraw = false;
			mNeedPositionXChange = false;
			mNeedPositionYChange = false;
			mNeedPositionZChange = false;
			Log.i("Updatevisibility", "mPos Y: "+mPositionY+"mRandom: "+mRandom);
		}
	}
	
	private void setNewPositionY(int newPosition,boolean override) {
		if(override || (!mNeedPositionYChange && !mNeedPositionXChange)) {
			mNewPositionY = newPosition + mDownPositionY;
			mNeedPositionYChange = true;
			mLastPositionY = mPositionY;
		}
		else {
			Log.i("setNewPositionY", "Threw away order!");
		}
	}
	
	private void setNewPositionX(int newPosition, boolean override) {
		if(override || (!mNeedPositionXChange  && !mNeedPositionZChange && mPositionY == mDownPositionY)) {
			mNewPositionX = newPosition;
			mLastPositionX = mPositionX;
			mNeedPositionXChange = true;
			
		}
		else {
			Log.i("setNewPositionX", "Threw away order!");
		}
	}
	
	private void setNewPositionZ(int newPosition) {
		if(!mNeedPositionZChange && !mNeedPositionXChange) {
			mLastPositionZ = mPositionZ;
			mNewPositionZ = newPosition;
			mNeedPositionZChange = true;
			
		}
		else {
			Log.i("setNewPositionZ", "Threw away order!");
		}
	}
	
	private void updatePositionY() {	
		mAccelZone = (float) ((Math.pow(mMaxSpeed, 2) - mSpeedY) / (2*mAccel));
		//Log.i("Wheatley", "Current pos: " + Integer.toString(mPositionY) + ", Wanted position: " + Integer.toString(mNewPositionY) + ", Current speed: " + Double.toString(mSpeedY));
		
		if(((mLastPositionY + mAccelZone) > mPositionY && mDirectionY) || ((mLastPositionY - mAccelZone) < mPositionY && !mDirectionY)) {
			if (mSpeedY<mMaxSpeed){
				mSpeedY = mSpeedY + (mAccel);	
			}
		}
		else if(((mNewPositionY - mAccelZone - mMaxSpeed) < mPositionY && mDirectionY) || ((mNewPositionY + mAccelZone + mMaxSpeed) > mPositionY && !mDirectionY)) {
			if (mSpeedY<mMaxSpeed){
				mSpeedY = mSpeedY - (mAccel);
			}
		}
		
		if(mSpeedY < WHEATLEY_MINIMUM_SPEED) { 
			mSpeedY = WHEATLEY_MINIMUM_SPEED;
		}
					
		// Traverse Sprite in Y direction
		if(mNewPositionY > mPositionY) {
			mPositionY += mSpeedY;
			mDirectionY = true;
		}
		else if(mNewPositionY < mPositionY) {
			mPositionY -= mSpeedY;
			mDirectionY = false;
		}			
		                       //kommet fram mNewPositionX+1 > mPositionX && mNewPositionX-1 < mPositionX
		if(mNewPositionY+mSpeedY > mPositionY && mNewPositionY-mSpeedY<mPositionY) {
			mNeedPositionYChange = false;
			mLastPositionY = mPositionY;
			mSpeedY = WHEATLEY_MINIMUM_SPEED;
            if (mRandom && mPositionY == mDownPositionY) {
                mRandom = false;
            }
        }
        mLastActivityTime = System.currentTimeMillis();
	}
	
	private void updatePositionX() {	
		//Log.i("WheatleySprite","mAccelZoneX: "+mAccelZoneX+ " mAccel: "+mAccel);
		mAccelZoneX = (float) ((Math.pow(mMaxSpeed, 2)) / (2*mAccel));
		if(Math.abs(mNewPositionX - mLastPositionX) < (mAccelZoneX*2+mMaxSpeed) /*&& mSpeedX < 2*WHEATLEY_MINIMUM_SPEED DEBUG*/) {
			mAccelZoneX = Math.abs((mNewPositionX+mMaxSpeed) - mLastPositionX)/2;
		}
		
		if(((mNewPositionX - mAccelZoneX - mMaxSpeed) < mPositionX && mDirectionX) || ((mNewPositionX + mAccelZoneX + mMaxSpeed) > mPositionX && !mDirectionX)) {
			mSpeedX = mSpeedX - (mAccel);
			//Log.i("WheatleySprite","Slowing Down");
		}		
		else if(((mLastPositionX + mAccelZoneX) > mPositionX && mDirectionX) || ((mLastPositionX - mAccelZoneX) < mPositionX && !mDirectionX)) {
			if(mSpeedX < mMaxSpeed) {				
				mSpeedX = mSpeedX + (mAccel);		
				//Log.i("WheatleySprite","Speeding Up");
			}
		}
		else {
			//Log.i("WheatleySprite","I'm in a deadzone. HALP");
		}
		if(mSpeedX < WHEATLEY_MINIMUM_SPEED) { 
			mSpeedX = WHEATLEY_MINIMUM_SPEED;
		}			
		
		// Traverse Sprite in X direction		
		
		
		// What to f does this do ??? Commented out weird stuff. Direction control works, but mLastPosition should not be set here. It never escapes minimum speed.
		if(mNewPositionX > mPositionX && mSpeedX == WHEATLEY_MINIMUM_SPEED) {
			if(!mDirectionX) {
				//mLastPositionX = mPositionX;
			}
			
			mDirectionX = true;
		}
		else if(mNewPositionX < mPositionX && mSpeedX == WHEATLEY_MINIMUM_SPEED) {
			if(mDirectionX) {
				//mLastPositionX = mPositionX;
			}
			
			mDirectionX = false;
		}	
		
		if(mDirectionX){
			mPositionX += mSpeedX;
		}
		else{
			mPositionX -= mSpeedX;
		}
		// kommet fram til destinasjonen
		if(mNewPositionX+mSpeedX > mPositionX && mNewPositionX-mSpeedX < mPositionX && mSpeedX <= WHEATLEY_MINIMUM_SPEED) {
			mNeedPositionXChange = false;
			mDirectionX = !mDirectionX;
			mPositionX=mNewPositionX;
			mLastPositionX = mPositionX;
			if (mRandom && !mNeedPositionZChange && !mNeedPositionYChange) {
				mRandom = false; 
			}
			//mSpeedX = 1.0;	
		}	
		mLastActivityTime = System.currentTimeMillis();
	}
	
	private void updatePositionZ() {
		
		//Log.i("Wheatley", "Current pos: " + Integer.toString(mPositionZ) + ", Wanted position: " + Integer.toString(mNewPositionZ) + ", Current speed: " + Double.toString(mSpeedZ));
		boolean lollert = true;
		
		if(lollert) {
			if(((mLastPositionZ + mAccelZone) > mPositionZ && mDirectionZ) || ((mLastPositionZ - mAccelZone) < mPositionZ && !mDirectionZ)) {
				if(mSpeedZ < mMaxSpeed) {				
					mSpeedZ = mSpeedZ + (mAccel);		
				}
			}
			else if(((mNewPositionZ - mAccelZone - mMaxSpeed) < mPositionZ && mDirectionZ) || ((mNewPositionZ + mAccelZone + mMaxSpeed) > mPositionZ && !mDirectionZ)) {
				mSpeedZ = mSpeedZ - (mAccel);
			}
		}		
		
		if(mSpeedZ < 1.0) { 
			mSpeedZ = 1.0;
		}
		
		// Traverse Sprite in Z direction
		if(mNewPositionZ > mPositionZ) {  
			mPositionZ += mSpeedZ;
			mDirectionZ = true;
		}
		else if(mNewPositionZ < mPositionZ) {
			mPositionZ -= mSpeedZ;
			mDirectionZ = false;
		}	
		
		if(mNewPositionZ+mSpeedZ > mPositionZ && mNewPositionZ-mSpeedZ<mPositionZ) {
			mNeedPositionZChange = false;
			mPositionZ=mNewPositionZ;
			mLastPositionZ = mPositionZ;
			mSpeedZ = 1.0;
			mAccelZone = (float) ((Math.pow(mMaxSpeed, 2) - mSpeedZ) / (2*mAccel));
        }
        mLastActivityTime = System.currentTimeMillis();
		
	}
	
	private void doStalkerMode() {				
		boolean doStalker = true; // Option
		if (mWheatleySnapPosition.equals("Left")){
			if(!mRandom && doStalker && (mOffsetX%mOffsetStepX == 0 || mPixelOffsetX % (mOffsetStepX * mScreenWidth) == 0) && mLastStalkerOffset != mOffsetX   && mPositionY == mDownPositionY) {
				setNewPositionX(0, true);	
				mLastStalkerOffset = mOffsetX;			
				doWheatleySnap();
			}
		}
		else if (mWheatleySnapPosition.equals("Right")){
			if(!mRandom && doStalker && (mOffsetX%mOffsetStepX == 0 || mPixelOffsetX % (mOffsetStepX * mScreenWidth) == 0) && mLastStalkerOffset != mOffsetX   && mPositionY == mDownPositionY) {
				setNewPositionX(mScreenWidth-mSpriteWidth, true);
				mLastStalkerOffset = mOffsetX;			
				doWheatleySnap();
			}
		}
		else {
			if(!mRandom && doStalker && (mOffsetX%mOffsetStepX == 0 || mPixelOffsetX % (mOffsetStepX * mScreenWidth) == 0) && mLastStalkerOffset != mOffsetX   && mPositionY == mDownPositionY) {
				//Log.i("doStalker", "Stalking");
				if(mPositionX <= 5 || (mPositionX <= mScreenWidth-5 && !mDirectionX)) {	
					setNewPositionX(0, true);
				}		
				else if(mPositionX >= mScreenWidth-5 || (mPositionX >= 5 && mDirectionX)) {
					setNewPositionX(mScreenWidth-mSpriteWidth, true);
				}
				
				mLastStalkerOffset = mOffsetX;			
				doWheatleySnap();
			}
		}
	}

	
	private void doWheatleySnap() {
		boolean doSnap = false; // Change this with settings		
		if(doSnap) {
			if((mPositionX+(mSpriteWidth/2) < (mScreenWidth/2) && mPositionX+(mSpriteWidth/2) > 0)) {
				setNewPositionX(0, false);
			}
			else if((mPositionX+(mSpriteWidth/2) > (mScreenWidth/2) && mPositionX+(mSpriteWidth/2) < mScreenWidth)) {
				setNewPositionX(mScreenWidth-mSpriteWidth, false);
			}
		}
	}

	@Override
	public void doWallpaperScroll(float xOffset, float xOffsetStep,	int xPixelOffset, int screenWidth) {
		//Log.i("WallpaperScroll", "xOffset: " + Float.toString(xOffset) + ", xOffsetStep: " + Float.toString(xOffsetStep) + ", xPixelOffset: " + Integer.toString(xPixelOffset));
		
		if(!mFirstRun) {			
			mPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
			mNewPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
			mLastPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));				

			mOffsetX = xOffset;
			mOffsetStepX = xOffsetStep;
			mPixelOffsetX = xPixelOffset;
		}
		else {			
			mOffsetX = xOffset;
			mOffsetStepX = xOffsetStep;
			mLastStalkerOffset = xOffset;
			mPixelOffsetX = xPixelOffset;
		}
	}
	
	private void randomMovement()  {
		int xy = mRandomGen.nextInt(4);
		if (mPositionY == mDownPositionY){
	        if (xy <3 || !mEnableRandomMovementY){
	            int n=1;
	            if (mRandomGen.nextInt(2)==1){
	                n = -1;
	            }
	            int x=(int)((mRandomGen.nextFloat()*(mScreenWidth-mSpriteWidth)) + (mRandomGen.nextInt(2)*n*mScreenWidth));
	
	            if ((Math.abs(x-mPositionX)) > mScreenWidth/4 && mPositionX >= 0 && mPositionX < mScreenWidth /* && x > mPixelOffsetX && x < (2*mScreenWidth+mPixelOffsetX) */) {
	                setNewPositionX(x, false );
	                mRandom = true;
	            }
	            else if ((Math.abs(x-mPositionX)) > mScreenWidth/4 && x >= 0 && x < mScreenWidth-mSpriteWidth) {
	                setNewPositionX(x, false );
	                mRandom = true;
	            }
	        }
	        else if (mPositionY == mDownPositionY && mPositionX >= 0 && mPositionX < mScreenWidth){
            	setNewPositionZ(-200);
                setNewPositionY(-mSpriteHeigth,false);
                mRandom = true;
            }
	        
		}
        
        else {
        	
        	setNewPositionZ(0);
            setNewPositionY(0,false);
            setNewPositionX(0, false);
            mRandom = true;
        }
            
  }
	
	@Override
	public void onSingleTap(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
		int i = 0; // TODO Replace with option
		
		if(i == 0) {
			doSingleTapOptionOne(x, y);
		}
		else if(i == 1) {
			doSingleTapOptionTwo(x, y);
		}
	}
	
	@Override
	public void onDoubleTap(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
		int i = 0; // TODO Replace with option
		
		if(i == 0) {
			doDoubleTapOptionOne(x, y);
		}
		else if(i == 1) {
			doDoubleTapOptionTwo(x, y);
		}	
	}	
	
	private void doDoubleTapOptionOne(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
        if(!mDraw) {
            mDraw = true;


            if(mTapAction == 2) {
                setNewPositionZ(0);
            }

            setNewPositionY(0,false);
            setNewPositionZ(0);
        }
        else {
            if(mEnableSound && mDestRect.contains(x, y) && !mIsPlayingSound && !mNeedPositionXChange && !mNeedPositionYChange && !mNeedPositionZChange ) {
                mIsPlayingSound = true;
                mNeedPositionXChange=true;
                mNeedPositionYChange=true;
                mNeedPositionZChange=true;

                Log.i("WheatleySprite", "HIT!");
                int currentSound=mRandomGen.nextInt(mNumberOfSounds-1);
                while (currentSound==mPreviousSound){
                	currentSound=mRandomGen.nextInt(mNumberOfSounds-1);
                }
                mPreviousSound=currentSound;
                MediaPlayer mp = MediaPlayer.create(mCtx, mSoundResources[currentSound]);
                mp.start();
                
                mp.setOnCompletionListener(new OnCompletionListener() {
                
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        mp.release();
                        mIsPlayingSound = false;
                        mNeedPositionXChange=false;
                        mNeedPositionYChange=false;
                        mNeedPositionZChange=false;
                        
                    }

                });
            }
            else if(x != mPositionX && !mDestRect.contains(x, y)) {
                //Log.i("move", "wheatley");
                setNewPositionX(x - (mSpriteWidth/2), mRandom);
                setNewPositionY(0,false);
                setNewPositionZ(0);
            }
            
        }
	}
	
	private void doSingleTapOptionTwo(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();

		
	}
	
	private void doSingleTapOptionOne(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();

		if(!mDraw) {
			mDraw = true;			

			if(mTapAction == 2) {
				setNewPositionZ(0);
			}
			setNewPositionZ(0);
			setNewPositionY(0,false);
		}
		
		else if(mDestRect.contains(x, y)) {			
			// Tap action = Move wheatley i y direction
			if(mTapAction == 1) {
				if(mPositionY == mDownPositionY)
					setNewPositionY(-mSpriteHeigth,false);
			}
			// Tap action = Move wheatley in z and y direction
			else if(mTapAction == 2) {
				if(mPositionY == mDownPositionY) {
					setNewPositionZ(-200);
					setNewPositionY(-mSpriteHeigth,false);
				}
			}
		}
		else if(x != mPositionX && !mDestRect.contains(x, y)) {
            //Log.i("move", "wheatley");
            setNewPositionX(x - (mSpriteWidth/2), mRandom);
            setNewPositionY(0,false);
            setNewPositionZ(0);
        }
		//else doSingleTapOptionOne(x, y);
	}
	
	private void doDoubleTapOptionTwo(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
		
	}
	
	/*
	 * DEBUG FUNCTION
	 */
	private void doSomeLogging() {
		boolean doLog = false;
		
		if(doLog) {
			
			StringBuilder builder = new StringBuilder();
			builder.append("NeedPositionChange: ")
				   .append(mNeedPositionXChange)
				   .append(", ")
				   .append(mNeedPositionYChange)				   
				   .append(", ")
				   .append(mNeedPositionZChange)
				   .append("\n")
				   .append("Current position: ")
				   .append(mPositionX)				   
				   .append(", ")
				   .append(mPositionY)				   
				   .append(", ")
				   .append(mPositionZ)
				   .append("\n")
				   .append("Current Speed: ")
				   .append(mSpeedX)				   
				   .append(", ")
				   .append(mSpeedY)				   
				   .append(", ")
				   .append(mSpeedZ)
                   .append(", IsTalking ")
                   .append(mIsPlayingSound);
			
			Log.i("WheatleySprite", builder.toString());
		}
	}
	
	@Override
	public boolean onSingleTap(float x, float y) {
		onSingleTap((int) x, (int) y);
		
		// Return false. Not yet implemented in this wallpaper
		return false;
	}
	
	@Override
	public boolean onDoubleTap(float x, float y) {
		onDoubleTap((int) x, (int) y);
		
		// Return false. Not yet implemented in this wallpaper
		return false;
	}
}