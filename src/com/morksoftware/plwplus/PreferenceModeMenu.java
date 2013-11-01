package com.morksoftware.plwplus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class PreferenceModeMenu extends Activity implements OnCheckedChangeListener, OnSharedPreferenceChangeListener {

	private PortalPreferences mPrefs;
	private RadioGroup mRadioGroupMode;
	private RadioButton mRadioBtnSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.preference_mode_menu);
		
		// Find views in the layout
		mRadioGroupMode = (RadioGroup) findViewById(R.id.radiogroup_mode);
		mRadioBtnSpace = (RadioButton) findViewById(R.id.radiobtn_mode_space);
		
		// Get the preference manager
		mPrefs = new PortalPreferences(this);
		
		// 
		if(!Utils.keyIsInstalled(this)) {
			mRadioBtnSpace.setEnabled(false);
		}
		
		// Check correct radioBtn based on stored prefs
		if(mPrefs.getWallpaperMode() == PortalPreferences.WALLPAPER_MODE_LABS) {
			mRadioGroupMode.check(R.id.radiobtn_mode_labs);
		}
		else {
			mRadioGroupMode.check(R.id.radiobtn_mode_space);
		}
		
		// Attach onCheckedChangeListener to the radioGroup
		mRadioGroupMode.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(group == mRadioGroupMode) {
			switch(checkedId) {
				case R.id.radiobtn_mode_labs:
					Log.i("TAG", "LABS");
					mPrefs.setWallpaperMode(PortalPreferences.WALLPAPER_MODE_LABS);
				break;
				
				case R.id.radiobtn_mode_space:
					Log.i("TAG", "Space");
					mPrefs.setWallpaperMode(PortalPreferences.WALLPAPER_MODE_SPACE);
				break;
			}
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
