package com.morksoftware.plwplus;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;


public class PrefsMainMenu extends FragmentActivity implements OnSharedPreferenceChangeListener {
	public static String PACKAGE_NAME;
	private Preference BackgroundButton;
	private Preference LabsBackgroundSource;
	private Preference SpaceBackgroundSource;
	private PrefsHelper mPrefs;
	private PrefsLabsFragment labsFragment;
	private PrefsSpaceFragment spaceFragment;
	private PrefsMainFragment mainFragment;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private String pref_mode;
	private boolean hasBeenCreated=false;
	private boolean modeFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        // Display the fragment as the main content.
        mPrefs = new PrefsHelper(this);
        
        
        // WAT WAT
        mPrefs.registerOnSharedPrefListener(this); 
        ////
        pref_mode = mPrefs.getWallpaperMode();
        setContentView(R.xml.prefs_main_menu);
        mainFragment = new PrefsMainFragment();
        labsFragment = new PrefsLabsFragment();
        spaceFragment = new PrefsSpaceFragment();
        fragmentManager= getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        modeFlag=false;
        
        //fragmentTransaction.replace(R.id.mainFragment,mainFragment,"mainFragment");
        if(pref_mode.equals("Space")){
			fragmentTransaction.replace(R.id.modeFragment,spaceFragment);
        }
        else if(pref_mode.equals("Labs"))  {
        	fragmentTransaction.replace(R.id.modeFragment,labsFragment);
        }
        fragmentTransaction.commitAllowingStateLoss();
        hasBeenCreated = true;
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		//getMenuInflater().inflate(R.menu.main, menu);
				
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		// TODO Auto-generated method stub
		Log.i("PrefsMainFragment", "OnSharedPrefChanged");
		if (key.equals("pref_mode") && hasBeenCreated){
			modeFlag=true;
		}
	}
    @Override
    public void onPostResume(){
    	
    	super.onPostResume();
    	if (modeFlag){
	    	pref_mode = mPrefs.getWallpaperMode();
			if(pref_mode.equals("Space")){
				fragmentTransaction.replace(R.id.modeFragment,spaceFragment);
	        }
	        else if(pref_mode.equals("Labs"))  {
	        	fragmentTransaction.replace(R.id.modeFragment,labsFragment);
	        }
			fragmentTransaction.commitAllowingStateLoss();
    	}
    	
    }

}
