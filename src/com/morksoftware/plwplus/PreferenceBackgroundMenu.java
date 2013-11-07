package com.morksoftware.plwplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class PreferenceBackgroundMenu extends Activity implements OnClickListener, OnSharedPreferenceChangeListener{

    private int[] mPics;
    private int mSelectedId = 0;
    
    private ImageView mImgViewLarge;
    private Gallery mPreviewGallery;
    
    private PortalPreferences mPrefs;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set layout
        setContentView(R.layout.preference_background_menu);
        
        //
        mPrefs = new PortalPreferences(this);
        
        if(mPrefs.getWallpaperMode() == PortalPreferences.WALLPAPER_MODE_LABS) {
        	mPics = new int[] {R.drawable.raw_background_1, R.drawable.raw_background_2, R.drawable.raw_background_3};
        }
        else {
        	mPics = new int[] {R.drawable.raw_space_background_1, R.drawable.raw_space_background_2};
        }
        
        // Find some views by id
        Gallery mPreviewGallery = (Gallery)findViewById(R.id.Gallery01);        
        mImgViewLarge = (ImageView)findViewById(R.id.ImageView01);
        
        // Give the gallery an adapter
        mPreviewGallery.setAdapter(new ImageAdapter(this));
                        
        // Attach OnItemSelectedListener to gallery
        mPreviewGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				// Populate imageView
				mImgViewLarge.setImageResource(mPics[arg2]);	
				mSelectedId = mPics[arg2];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {		
			}
		});   
        
        mImgViewLarge.setOnClickListener(this);
    }
    
    
    public class ImageAdapter extends BaseAdapter {

    	private Context ctx;
    	//int imageBackground;
    	
    	public ImageAdapter(Context c) {
			ctx = c;
		}

		@Override
    	public int getCount() {
    		
    		return mPics.length;
    	}		

    	@Override
    	public Object getItem(int arg0) {
    		
    		return arg0;
    	}

    	@Override
    	public long getItemId(int arg0) {
    		
    		return arg0;
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView iv = new ImageView(ctx);
    		
    		iv.setImageResource(mPics[position]);
    		
    		iv.setScaleType(ImageView.ScaleType.FIT_XY);
    		
    		iv.setLayoutParams(new Gallery.LayoutParams(192,108));
    		
    		iv.setBackgroundResource(R.drawable.gallery_selector);
    		
    		return iv;
    	}
    }


	@Override
	public void onClick(View v) {
		if(v == mImgViewLarge) {			
			mPrefs.setWallpaperBackground(mSelectedId);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Register a onSharedPrefListener to mPrefs
		mPrefs.unregisterOnSharedPrefListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
				
		// Unregister the listener
		mPrefs.registerOnSharedPrefListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {}
}
