package com.morksoftware.plwplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PortalPreferencesActivity extends Activity implements OnSharedPreferenceChangeListener, OnClickListener {
	
	private PortalPreferences mPrefs;
	
	private TextView mTxtViewModes;
	private TextView mTxtViewBackgrounds;
	private TextView mTxtViewFeatures;
	private TextView mTxtViewAbout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.preference_main_menu);
		
		// Get an instance of PortalPreferences
		mPrefs = new PortalPreferences(this);
				
		// Attach a changeListener to mPrefs
		mPrefs.registerOnSharedPrefListener(this);	
		
		// Find some views by id
		mTxtViewModes = (TextView) findViewById(R.id.menu_item_modes);
		mTxtViewBackgrounds = (TextView) findViewById(R.id.menu_item_backgrounds);
		mTxtViewFeatures = (TextView) findViewById(R.id.menu_item_features);
		mTxtViewAbout = (TextView) findViewById(R.id.menu_item_about);
		
		// Attach clickListener to views
		mTxtViewModes.setOnClickListener(this);
		mTxtViewBackgrounds.setOnClickListener(this);
		mTxtViewFeatures.setOnClickListener(this);
		mTxtViewAbout.setOnClickListener(this);		
	}	
		
	@Override
	public void onClick(View v) {
		if(v == mTxtViewModes) {
			Intent modeIntent = new Intent(this, PreferenceModeMenu.class);
			
			startActivity(modeIntent);
		}
		else if(v == mTxtViewBackgrounds) {
			Intent backgroundIntent = new Intent(this, PreferenceBackgroundMenu.class);
			
			startActivity(backgroundIntent);
		}
		else if(v == mTxtViewFeatures) {
			// TODO load features
		}
		else if(v == mTxtViewAbout) {
			// TODO load about screen
		}
		
//		if(v == mLol) {
//			if(mPrefs.getWallpaperMode() == PortalPreferences.WALLPAPER_MODE_LABS) {
//				mPrefs.setWallpaperMode(PortalPreferences.WALLPAPER_MODE_SPACE);
//				Toast.makeText(this, "SPACE LOADED", Toast.LENGTH_SHORT).show();
//			}
//			else if(mPrefs.getWallpaperMode() == PortalPreferences.WALLPAPER_MODE_SPACE){
//				mPrefs.setWallpaperMode(PortalPreferences.WALLPAPER_MODE_LABS);
//				Toast.makeText(this, "LABS LOADED", Toast.LENGTH_SHORT).show();
//			}
//		}		
	}
	
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		
		mPrefs.unregisterOnSharedPrefListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {		
	}
}
