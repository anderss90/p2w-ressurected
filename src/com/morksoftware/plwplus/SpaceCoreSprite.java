package com.morksoftware.plwplus;

import java.util.Random;

import android.content.Context;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class SpaceCoreSprite extends Sprite {
	// The bitmap we want drawn to our canvas
	private Bitmap mBitmap;
    private Bitmap mBitmap2;
	
	// Sprite values
	private int mSpriteWidth;
	private int mSpriteHeigth;
	private int mFrameCount;

    private int mSprite2Width;
    private int mSprite2Heigth;
    private int mFrameCount2;
    private boolean mTapAnimation1 = false;
    private boolean mTapAnimation2 = false;
    private boolean mTapAnimation3 = false;
    private boolean mTapAnimation4 = false;
    // Screen values
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
	
	// Source and destination rectangles
	private Rect mDestRect;
	private Rect mSrcRect;

    private Rect mDestRect2;
    private Rect mSrcRect2;
	
	// Animation controls
	private long mLastAnimationUpdateTime = 0;
	private int mAnimationUpdatePeriod = 70;
    private long mLastAngleUpdateTime = 0;
    private int mAngleUpdatePeriod = 200;
	private int mCurrentFrame = 0;
	private boolean mReverse = false;
	
	// Position controls	
	private double mAngle = Math.PI/4;	
	private float mMaxSpeed = 12;
	private float mSpeed = 6;
	private Random mRandomGen;
	private float mPositionZ = 0;
	private float mPositionX = -500;
	private float mPositionY = 500;

    private double mAngle2 = Math.PI/4;
    private float mMaxSpeed2 = 12;
    private float mSpeed2 = 12;
    private float mPosition2Z = 0;
    private float mPosition2X = -500;
    private float mPosition2Y = 500;
	
	private long mLastPositionUpdateTime = 0;
	private long mLastDistanceUpdateTime = 0;
	private int mDistanceUpdatePeriod =  20;
	private int mPositionUpdatePeriod = 20;
	private long mCurrentTime;

    private int mLeftBoundry=0;
    private int mRightBoundry=0;
    private int mTopBoundry=0;
    private int mBottomBoundry=0;
    private boolean mLeftAction = true;
    private boolean mRightAction = true;
    private boolean mTopAction = true;
    private boolean mBottomAction = true;


    private int mRectWidth = 500;
    private int mRectHeight = 500;

	private Context mCtx;
	
	private float mOffsetX = 0;
	private float mOffsetStepX = 0;
	private float mPixelOffsetX = 0;
	private boolean mFirstRun = true;
	// Sound 
	private int[] mSoundResources = {R.raw.space01, R.raw.space02, R.raw.space04, R.raw.space05, R.raw.space20, R.raw.space22, R.raw.space24, R.raw.space25, R.raw.space26, R.raw.space27, R.raw.space28};
	private boolean mIsPlayingSound = false;

    //Matrix
    Matrix mRotateMatrix = new Matrix();
    float dx = 0;
    float dy = 0;
    float mRotateAngle = ((float) 1);
    float sx = 0;
    float sy = 0;

	@Override
	public void initResources(Context ctx, int screenWidth, int screenHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inInputShareable = true;
		options.inPurgeable = true;
        mFirstRun=true;
		
		mBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.spritesheet_space_2, options);
        mBitmap2 = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.companion_cube, options);
		mCtx = ctx;
		
		mFrameCount = 28;
        mFrameCount2 = 29;
		
		mRandomGen = new Random();
		
		mSpriteWidth = mBitmap.getWidth()/mFrameCount;
        mSpriteHeigth = mBitmap.getHeight();

        mSprite2Width = mBitmap2.getWidth();
        mSprite2Heigth = mBitmap2.getHeight();

        //mSpriteHeigth=100;
        //mSpriteWidth=mSpriteHeigth;
		
		mDestRect = new Rect(0,50,mSpriteWidth,(mSpriteHeigth+50));
		mSrcRect = new Rect(0,0,mSpriteWidth,mSpriteHeigth);

        mDestRect2 = new Rect(0,50,mSprite2Width,(mSprite2Heigth+50));
        mSrcRect2 = new Rect(0,0,mSprite2Width,mSprite2Heigth);

        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;

        mBottomBoundry = mScreenHeight+mSpriteHeigth;
        mTopBoundry = -mSpriteHeigth;
        mRightBoundry = mScreenWidth+mSpriteWidth;
        mLeftBoundry = -mSpriteWidth;

        mRotateMatrix.reset();





	}

	@Override
	public void releaseResources() {
		mBitmap.recycle();
		mBitmap = null;
        mBitmap2.recycle();
        mBitmap2 = null;
	}
	
	@Override
	public void doDraw(Canvas c) {
		mCurrentTime = System.currentTimeMillis();		
		
		doSomeLogging();
		
		updateAnimation();
		updatePosition();
        speedControl();
        reduceBoundries();


        if (mTapAnimation3) {
            mRotateAngle +=1;
            mRotateMatrix.setTranslate(0,0);
            mRotateMatrix.setScale(sx,sy);
            //mRotateMatrix.reset();
            //mRotateMatrix.postTranslate(dx, dy);
            mRotateMatrix.postRotate(mRotateAngle, mSprite2Width/2, mSprite2Heigth/2);
            mRotateMatrix.postTranslate(mPosition2X,mPosition2Y);

        }
        if (mTapAnimation1 || mTapAnimation2 ||mTapAnimation3 || mTapAnimation4) {
            c.drawBitmap(mBitmap2, mRotateMatrix , null);
        }
        if(mFirstRun) {
            //Log.i("", "mLastOffset: " + Float.toString(mLastStalkerOffset));
            mFirstRun = false;
        }
        c.drawBitmap(mBitmap, mSrcRect, mDestRect, null);
	}
	
	private void updateAnimation() {		
		if(mCurrentTime > mLastAnimationUpdateTime + mAnimationUpdatePeriod) {
			mSrcRect.left = mCurrentFrame*mSpriteWidth;
			mSrcRect.right = mSrcRect.left + mSpriteWidth;
			
			
			// Update current shown frame
			if(mReverse) {
				mCurrentFrame -= 1;
			}
			else {
				mCurrentFrame += 1;
			}
			
			// Update direction on frames
			if(mCurrentFrame <= 0) {
				mReverse = false;
			}
			else if(mCurrentFrame >= (mFrameCount-1)) {
				mReverse = true;
			}



			
			// Register animation update
			mLastAnimationUpdateTime = mCurrentTime;
		}		
	}
	
	private void updatePosition() {	
		if(mCurrentTime >= mLastPositionUpdateTime + mPositionUpdatePeriod) {
			
			updateAngleAndPositionZ();
            sx = (float)(1+(mPosition2Z / 2000.0));
            sy= sx;
			
			updatePositionX();
			updatePositionY();
            updatePositionZ();




			
			mDestRect.top = (int) ((double)   mPositionY + (mSpriteHeigth*(-mPositionZ / 2000.0)));
			mDestRect.bottom = (int) ((double) (mPositionY + (mSpriteHeigth * (1.0 + (mPositionZ / 2000.0)))));

			mDestRect.left = (int) ((double)   mPositionX + (mSpriteWidth * (-mPositionZ / 2000.0)));
			mDestRect.right = (int) ((double) (mPositionX + (mSpriteWidth * (1.0 + (mPositionZ / 2000.0)))));

            mDestRect2.top = (int) ((double)   mPosition2Y + (mSprite2Heigth*(-mPosition2Z / 2000.0)));
            mDestRect2.bottom = (int) ((double) (mPosition2Y + (mSprite2Heigth * (1.0 + (mPosition2Z / 2000.0)))));

            mDestRect2.left = (int) ((double)   mPosition2X + (mSprite2Width * (-mPosition2Z / 2000.0)));
            mDestRect2.right = (int) ((double) (mPosition2X + (mSprite2Width * (1.0 + (mPosition2Z / 2000.0)))));

			mRectWidth = Math.abs(mDestRect.right-mDestRect.left);
            mRectHeight = Math.abs(mDestRect.bottom-mDestRect.top);

			mLastPositionUpdateTime = mCurrentTime;
			//mFirstRun=false;
		}
	}
	
	private void updatePositionX() {

		mPositionX += Math.ceil(Math.cos(mAngle) * mSpeed);

        if (mTapAnimation1 || mTapAnimation3) {
            mPosition2X += Math.ceil(Math.cos(mAngle2) * mSpeed2);
            dx = (float)Math.ceil(Math.cos(mAngle2) * mSpeed2);

            if (mPosition2X < -(mSprite2Width) || mPosition2X > (mScreenWidth)) {
                mTapAnimation1=false;
                mTapAnimation3=false;
            }
        }
        if (mTapAnimation2) {
            mPosition2X += Math.ceil(Math.cos(mAngle2) * mSpeed2);
            if (mPosition2X < -(mSprite2Width) || mPosition2X > (mScreenWidth)) {
                mTapAnimation2=false;
            }
        }
	}
	
	private void updatePositionY() {
		mPositionY -= Math.ceil(Math.sin(mAngle) * mSpeed);

        if (mTapAnimation1 || mTapAnimation3) {
            mPosition2Y -= Math.ceil(Math.sin(mAngle2) * mSpeed2);
            dy = (float)Math.ceil(Math.sin(mAngle2) * mSpeed2);

            if (mPosition2Y < -(mSprite2Heigth) || mPosition2Y > (mScreenHeight)) {
                mTapAnimation1=false;
                mTapAnimation3=false;
            }
        }
        if (mTapAnimation2) {
            mPosition2Y -= Math.ceil(Math.sin(mAngle2) * mSpeed2);

            if (mPosition2Y < -(mScreenHeight*1.5) || mPosition2Y > (mScreenHeight*1.5)) {
                mTapAnimation2=false;
            }
        }
	}

    private void updatePositionZ()   {
        if(mTapAnimation2) {
            mPosition2Z +=mSpeed2*2;
            if (mPosition2Z > 0) {
                mTapAnimation2=false;
                mPosition2Z = -500;
            }
        }
        if(mTapAnimation3) {
            mPosition2Z +=mSpeed2;

        }
    }
	
	private void updateAngleAndPositionZ() {

                // Log.i("SCALE", Float.toString(mPositionZ) + ", " + Float.toString(mSpeed));
                if (mPositionX < mLeftBoundry && !mLeftAction) {
                    if (Math.sin(mAngle) < 0) {
                        mAngle += (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mLeftAction=true;
                    }
                    else if(Math.sin(mAngle) > 0) {
                        mAngle -= (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mLeftAction=true;
                    }
                    //Log.i("side=", "Left: ");
                }
                if (mPositionX +mSpriteWidth > mRightBoundry && !mRightAction){
                    if (Math.sin(mAngle) < 0) {
                        mAngle -= (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mRightAction=true;
                    }
                    else if(Math.sin(mAngle) > 0) {
                        mAngle += (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mRightAction=true;
                    }
                    //Log.i("side=", "Right: ");
                }
                if (mPositionY < mTopBoundry && !mTopAction) {
                    if(Math.cos(mAngle) <0) {
                        mAngle += (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mTopAction=true;
                    }
                    else if(Math.cos(mAngle)> 0) {
                        mAngle -= (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mTopAction=true;
                    }
                    //Log.i("side=", "Top: ");
                }
                if (mPositionY+mSpriteHeigth > mBottomBoundry && !mBottomAction) {
                    if(Math.cos(mAngle) < 0) {
                        mAngle -= (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mBottomAction=true;
                    }
                    else if(Math.cos(mAngle) > 0) {
                        mAngle += (mRandomGen.nextFloat()*((Math.PI)/6)) + ((4*Math.PI)/8);
                        mPositionZ = 0 - (mRandomGen.nextFloat() * 400);
                        mLastDistanceUpdateTime = mCurrentTime;
                        mBottomAction=true;
                    }
                    //Log.i("side=", "Bottom: ");
                }
                if (mPositionY+mSpriteHeigth > mTopBoundry) {
                    mTopAction=false;
                }
                if (mPositionX+mSpriteWidth > mLeftBoundry) {
                    mLeftAction=false;
                }
                if (mPositionX < mRightBoundry) {
                    mRightAction=false;
                }
                if(mPositionY > mBottomBoundry) {
                    mBottomAction=false;
                }
                mLastDistanceUpdateTime = mCurrentTime;


	}


	@Override
	public void doSingleTapEvent(int x, int y) {
		if(mDestRect.contains(x, y) && !mIsPlayingSound) {
			mIsPlayingSound = true;
			
			Log.i("SpaceCoreSprite", "HIT!");
			MediaPlayer mp = MediaPlayer.create(mCtx, mSoundResources[mRandomGen.nextInt(11)]);
            mp.start();
            mp.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.release();
                    mIsPlayingSound = false;
                }

            });
		}
        else if (!mDestRect.contains(x, y)) {
            //tapAnimation1(x,y);
            //tapAnimation2(x,y);
            tapAnimation3(x,y);
            //tapAnimation4(x,y);
        }
    }

    private void tapAnimation1 (int x, int y) {
        if (!mTapAnimation1)     {
            mAngle2 = (mRandomGen.nextFloat()*Math.PI*2);
            mPosition2X =  x - (mSprite2Width/2) - (int) Math.ceil(Math.cos(mAngle2)*mScreenHeight);
            mPosition2Y =  y - (mSprite2Width/2) + (int) Math.ceil(Math.sin(mAngle2)*mScreenHeight);
            mTapAnimation1=true;
            }
    }

    private void tapAnimation2 (int x, int y) {
        if (!mTapAnimation2)     {
            if (x < 0.5*mScreenWidth) {
                mAngle2 = (mRandomGen.nextFloat()*Math.PI) - Math.PI/2;
            }
            else {
                mAngle2 = (mRandomGen.nextFloat()*Math.PI) + Math.PI/2;
            }
            mPosition2X =  x - (mSprite2Width/2);
            mPosition2Y =  y - (mSprite2Width/2);
            mPosition2Z = -1000;
            mTapAnimation2=true;
        }
    }

    private void tapAnimation3(int x, int y) {
        if (!mTapAnimation3)     {
            mPosition2Z = -500;
            //mAngle2 = (mRandomGen.nextFloat()*Math.PI*2);
            int a = mRandomGen.nextInt(2);
            if (a == 0) {
                mPosition2X = 0-mSprite2Width;
            }
            else {
                mPosition2X = mScreenWidth;
            }
            mPosition2Y = mRandomGen.nextFloat()*mScreenHeight;
            y -= (mSprite2Heigth/2);
            x -= (mSprite2Width/2);
            if (a == 0)   {
                mAngle2 = Math.atan((mPosition2Y-y)/(x-mPosition2X));
            }
            else {
                mAngle2 = Math.atan((mPosition2Y-y)/(x-mPosition2X)) +Math.PI;
            }
            mRotateMatrix.setTranslate(mPosition2X,mPosition2Y);
            mRotateAngle=1;
           // mPosition2X =  x - (mSprite2Width/2) - (int) Math.ceil(Math.cos(mAngle2)*mScreenHeight);
           // mPosition2Y =  y - (mSprite2Width/2) + (int) Math.ceil(Math.sin(mAngle2)*mScreenHeight);
           // mPosition2Z = -700;
            mTapAnimation3=true;
        }
    }
    private void tapAnimation4(int x, int y) {
        if (!mTapAnimation4)     {
           mPosition2X = x;
           mPosition2Y = y;
           //mRotateMatrix.setTranslate(x,y);
            mTapAnimation4=true;
        }
        else if (mTapAnimation4)   {
            mTapAnimation4 = false;
        }
    }

	@Override
	public void doDoubleTapEvent(int x, int y) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void doWallpaperScroll(float xOffset, float xOffsetStep, int xPixelOffset, int screenWidth) {
		if(!mFirstRun) {			


            if (mOffsetX > xOffset) {
                mRightBoundry += 2*(int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
            }
            else if(mOffsetX < xOffset){
                mLeftBoundry += 2*(int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
            }

            mPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
			//mNewPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));
			//mLastPositionX += (int) Math.ceil((screenWidth) * ((mOffsetX-xOffset)/xOffsetStep));				

			mOffsetX = xOffset;
			mOffsetStepX = xOffsetStep;
			mPixelOffsetX = xPixelOffset;
		}
		else {			
			mOffsetX = xOffset;
			mOffsetStepX = xOffsetStep;
			mPixelOffsetX = xPixelOffset;
		}
		
	}

    private void reduceBoundries () {
        if (mRightBoundry > mScreenWidth+mSpriteWidth && Math.cos(mAngle) < 0 && mPositionX > mScreenWidth+mSpriteWidth) {
            mRightBoundry = (int) mPositionX+ 2*mSpriteWidth;
        }
        else if (mLeftBoundry < -mSpriteWidth && Math.cos(mAngle) > 0 && mPositionX < -mSpriteWidth) {
            mLeftBoundry = (int) mPositionX - mSpriteWidth;
        }
        if (mPositionX > 0) {
            mLeftBoundry =  -mSpriteWidth;
        }
        else if (mPositionX < mScreenWidth) {
            mRightBoundry = mScreenWidth+mSpriteWidth;
        }

    }
    private void speedControl () {
        if((mPositionX + mSpriteWidth) < 0 || mPositionX > mScreenWidth ||
                (mPositionY + mSpriteHeigth) < 0 || mPositionY > mScreenHeight) {
            mSpeed = mMaxSpeed*4;
        }

        else {
            mSpeed = (1-(-mPositionZ/ 1000)) * mMaxSpeed;
        }

    }

	
	/*
	 * Debug
	 */
	private void doSomeLogging() {
		boolean doDebug = false;
		
		if(doDebug) {
			//Log.i("SpaceCore", "Position: " + Integer.toString(mDestRect.left) + ", " + Integer.toString(mDestRect.top)+ " leftBoundry: " + Integer.toString(mLeftBoundry) + " rightBoundry: " + Integer.toString(mRightBoundry) + " angle: "+ Double.toString(mAngle) + " speed: " + Float.toString(mSpeed)            );
            Log.i("SpaceCore", "mAngle 2 : "+ Double.toString(mAngle2) + "mTapAnimation3 :" + Boolean.toString(mTapAnimation3) );
		}   //Log.i("lol", "Height: "+ Integer.toString(mSpriteHeigth));
        //Log.i("scale:", "sx: " + Float.toString(sx) + "sy: "+Float.toString(sy));
	}
}
