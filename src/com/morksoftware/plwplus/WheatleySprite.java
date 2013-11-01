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
	private float mMaxSpeed = 7;	
	private float mAccel = (float) 0.5;
	private float mAccelZone;
	
	private double mSpeedX = 1;
	private double mSpeedY = 1;
	private double mSpeedZ = 1;
	
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
	
	private boolean mNeedPositionXChange = false;
	private boolean mNeedPositionYChange = false;
	private boolean mNeedPositionZChange = false;	
	
	private float mOffsetX = 0;
	private float mOffsetStepX = 0;
	private float mPixelOffsetX = 0;
	private float mLastStalkerOffset;

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
	private int mAnimationUpdatePeriod = 70;
	private int mCurrentFrame = 0;
	private boolean mReverseAnimation = false;

    // Sounds
    private int[] mSoundResources = {R.raw.labs01, R.raw.labs02, R.raw.labs03};
    private boolean mIsPlayingSound = false;
    private Context mCtx;

	// Settings
	private int mTapAction = WHEATLEY_TAP_ACTION_Y;
	

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
		
		mSpriteWidth = mBitmap.getWidth()/mFrameCount;
		mSpriteHeigth = mBitmap.getHeight();		
		
		mPositionY = (-1)*mSpriteHeigth + mDownPositionY;
		mNewPositionY = (-1)*mSpriteHeigth;
		
		mPositionX = WHEATLEY_START_POSITION_X;
		
		mDestRect = new Rect(mPositionX, mPositionY, mSpriteWidth+mPositionX,mSpriteHeigth+mPositionY);
		mSrcRect = new Rect(0,0,mSpriteWidth,mSpriteHeigth);
		
		mScreenWidth = screenWidth;
        mScreenHeight = screenWidth;
		
		DisplayMetrics dm = new DisplayMetrics();
	    ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);

		// these will return the actual dpi horizontally and vertically
		float xDpi = dm.xdpi;
		
		mMaxSpeed = (float) (0.028*xDpi);
		mAccel = (float) mMaxSpeed/14;
		
		Log.i("MAX OG ACCEL", Float.toString(mMaxSpeed) + ", " + Float.toString(mAccel));
		
		mAccelZone = (float) ((Math.pow(mMaxSpeed, 2) - mSpeedY) / (2*mAccel));
		
		mDraw = true;
		setNewPositionZ(0);
		setNewPositionY(0);	
		
		mLastActivityTime = System.currentTimeMillis();
        mUpPositionY = -mSpriteHeigth+mDownPositionY;
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
		if(mCurrentTime > mLastAnimationUpdateTime + mAnimationUpdatePeriod) {
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
		if(mCurrentTime > mLastPositionUpdateTime + mPositionUpdatePeriod) {
			
			doStalkerMode();

            if (mPositionY == mUpPositionY || mPositionX < -mSpriteWidth || mPositionX > mScreenWidth) {
                mActivityInterval = mActivityIntervalDefault/2;
            }
            else {
                mActivityInterval = mActivityIntervalDefault;
            }
			if(mCurrentTime > mLastActivityTime + mActivityInterval) {
				randomMovement();
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
		}
	}
	
	private void setNewPositionY(int newPosition) {
		if(!mNeedPositionYChange && !mNeedPositionXChange) {
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
			mNeedPositionXChange = true;
			
			if(mSpeedX == WHEATLEY_MINIMUM_SPEED) {
				//mLastPositionX = mPositionX;
			}
		}
		else {
			Log.i("setNewPositionX", "Threw away order!");
		}
	}
	
	private void setNewPositionZ(int newPosition) {
		if(!mNeedPositionZChange && !mNeedPositionXChange && !mNeedPositionYChange) {
			mNewPositionZ = newPosition;
			mNeedPositionZChange = true;
			mLastPositionZ = mPositionZ;
		}
		else {
			Log.i("setNewPositionZ", "Threw away order!");
		}
	}
	
	private void updatePositionY() {			
		//Log.i("Wheatley", "Current pos: " + Integer.toString(mPositionY) + ", Wanted position: " + Integer.toString(mNewPositionY) + ", Current speed: " + Double.toString(mSpeedY));
		
		if(((mLastPositionY + mAccelZone) > mPositionY && mDirectionY) || ((mLastPositionY - mAccelZone) < mPositionY && !mDirectionY)) {
			
			mSpeedY = mSpeedY + (mAccel);			
			//Log.i("Wheatley", "�verste l�kke: " + Double.toString(mSpeedY) + ", Count: " + Integer.toString(i));
		}
		else if(((mNewPositionY - mAccelZone - mMaxSpeed) < mPositionY && mDirectionY) || ((mNewPositionY + mAccelZone + mMaxSpeed) > mPositionY && !mDirectionY)) {
			mSpeedY = mSpeedY - (mAccel);
			//Log.i("Wheatley", "Nederste l�kke: " + Double.toString(mSpeedY) + ", Count: " + Integer.toString(j));
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
		                       //kommet fram
		if(mNewPositionY == mPositionY) {
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
		if(Math.abs(mNewPositionX - mLastPositionX) < mAccelZone*2 && mSpeedX < 2*WHEATLEY_MINIMUM_SPEED) {;
			mAccelZone = Math.abs(mNewPositionX - mPositionX)/2;
		}
		
		if(((mNewPositionX - mAccelZone - mMaxSpeed) < mPositionX && mDirectionX) || ((mNewPositionX + mAccelZone + mMaxSpeed) > mPositionX && !mDirectionX)) {
			mSpeedX = mSpeedX - (mAccel);
		}		
		else if(((mLastPositionX + mAccelZone) > mPositionX && mDirectionX) || ((mLastPositionX - mAccelZone) < mPositionX && !mDirectionX)) {
			if(mSpeedX < mMaxSpeed) {				
				mSpeedX = mSpeedX + (mAccel);		
			}
		}
		
		if(mSpeedX < WHEATLEY_MINIMUM_SPEED) { 
			mSpeedX = WHEATLEY_MINIMUM_SPEED;
		}			
		
		// Traverse Sprite in X direction		
		if(mNewPositionX > mPositionX && mSpeedX == WHEATLEY_MINIMUM_SPEED) {
			if(!mDirectionX) {
				mLastPositionX = mPositionX;
			}
			
			mDirectionX = true;
		}
		else if(mNewPositionX < mPositionX && mSpeedX == WHEATLEY_MINIMUM_SPEED) {
			if(mDirectionX) {
				mLastPositionX = mPositionX;
			}
			
			mDirectionX = false;
		}	
		
		if(mDirectionX)
			mPositionX += mSpeedX;
		else
			mPositionX -= mSpeedX;
			       // kommet fram til destinasjonen
		if(mNewPositionX+1 > mPositionX && mNewPositionX-1 < mPositionX && mSpeedX <= WHEATLEY_MINIMUM_SPEED) {
			mNeedPositionXChange = false;
			mDirectionX = !mDirectionX;
			mLastPositionX = mPositionX;
			if (mRandom) {
				mRandom = false; 
			}
			mSpeedX = 1.0;
			mAccelZone = (float) ((Math.pow(mMaxSpeed, 2) - mSpeedY) / (2*mAccel));		
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
				//Log.i("Wheatley", "�verste l�kke: " + Double.toString(mSpeedZ));
			}
			else if(((mNewPositionZ - mAccelZone - mMaxSpeed) < mPositionZ && mDirectionZ) || ((mNewPositionZ + mAccelZone + mMaxSpeed) > mPositionZ && !mDirectionZ)) {
				mSpeedZ = mSpeedZ - (mAccel);
				//Log.i("Wheatley", "Nederste l�kke: " + Double.toString(mSpeedZ));
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
		
		if(mNewPositionZ == mPositionZ) {
			mNeedPositionZChange = false;
			mLastPositionZ = mPositionZ;
			mSpeedZ = 1.0;
			mAccelZone = (float) ((Math.pow(mMaxSpeed, 2) - mSpeedZ) / (2*mAccel));
            if (mRandom) {
                mRandom = false;
            }
        }
        mLastActivityTime = System.currentTimeMillis();
		
	}
	
	private void doStalkerMode() {				
		boolean doStalker = true; // Option
		
		if(!mRandom && doStalker && (mOffsetX%mOffsetStepX == 0 || mPixelOffsetX % (mOffsetStepX * mScreenWidth) == 0) && mLastStalkerOffset != mOffsetX   && mPositionY == mDownPositionY) {
			Log.i("doStalker", "Stalking");
			if(mPositionX <= 0+5 || (mPositionX <= mScreenWidth-5 && !mDirectionX)) {	
				setNewPositionX(0, true);
			}		
			else if(mPositionX >= mScreenWidth-5 || (mPositionX >= 0+5 && mDirectionX)) {
				setNewPositionX(mScreenWidth-mSpriteWidth, true);
			}
			
			mLastStalkerOffset = mOffsetX;			
			doWheatleySnap();
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
		Log.i("WallpaperScroll", "xOffset: " + Float.toString(xOffset) + ", xOffsetStep: " + Float.toString(xOffsetStep) + ", xPixelOffset: " + Integer.toString(xPixelOffset));
		
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
	
	private void randomMovement ()  {
		int xy = mRandomGen.nextInt(4);
        if (xy <3 && mPositionY == mDownPositionY){
            int n=1;
            if (mRandomGen.nextInt(2)==1){
                n = -n;
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
        else {
            if (mPositionY == mDownPositionY && mPositionX >= 0 && mPositionX < mScreenWidth){
                setNewPositionY(-mSpriteHeigth);
                mRandom = true;
            }
            else if (mPositionY == mUpPositionY) {
                setNewPositionY(0);
                mRandom = true;
            }
        }
	}
	
	@Override
	public void doSingleTapEvent(int x, int y) {
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
	public void doDoubleTapEvent(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
		int i = 0; // TODO Replace with option
		
		if(i == 0) {
			doDoubleTapOptionOne(x, y);
		}
		else if(i == 1) {
			doDoubleTapOptionTwo(x, y);
		}	
	}	
	
	private void doSingleTapOptionOne(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();
        if(!mDraw) {
            mDraw = true;


            if(mTapAction == 2) {
                setNewPositionZ(0);
            }

            setNewPositionY(0);
        }
        else {
            if(mDestRect.contains(x, y) && !mIsPlayingSound && !mNeedPositionXChange && !mNeedPositionYChange && !mNeedPositionZChange ) {
                mIsPlayingSound = true;
                mNeedPositionXChange=true;
                mNeedPositionYChange=true;
                mNeedPositionZChange=true;

                Log.i("WheatleySprite", "HIT!");
                MediaPlayer mp = MediaPlayer.create(mCtx, mSoundResources[mRandomGen.nextInt(3)]);
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
                setNewPositionX(x - (mSpriteWidth/2), false);
            }
        }
	}
	
	private void doSingleTapOptionTwo(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();

		
	}
	
	private void doDoubleTapOptionOne(int x, int y) {
        mLastActivityTime = System.currentTimeMillis();

		if(!mDraw) {
			mDraw = true;			

			if(mTapAction == 2) {
				setNewPositionZ(0);
			}
			
			setNewPositionY(0);
		}
		
		if(mDestRect.contains(x, y)) {			
			// Tap action = Move wheatley i y direction
			if(mTapAction == 1) {
				if(mPositionY == mDownPositionY)
					setNewPositionY(-mSpriteHeigth);
			}
			// Tap action = Move wheatley in z and y direction
			else if(mTapAction == 2) {
				if(mPositionY == mDownPositionY) {
					setNewPositionZ(-200);
					setNewPositionY(-mSpriteHeigth);
				}
			}
		}
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
}