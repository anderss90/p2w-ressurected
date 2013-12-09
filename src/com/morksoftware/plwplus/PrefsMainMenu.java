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
import com.morksoftware.plwplus.Utils;


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
	private boolean mExtraFeaturesUnlocked=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("PrefsMainMenu","OnCreate starting");
        PACKAGE_NAME = getApplicationContext().getPackageName();
        // Display the fragment as the main content.
        mPrefs = new PrefsHelper(this);
        
        
        //DEBUG
        /*
        if(hasBeenCreated==false){
        	mPrefs.setPremiumFromUtils();
        }
        */
    	mExtraFeaturesUnlocked=mPrefs.getPremium();
        
        
        mPrefs.registerOnSharedPrefListener(this); 
        
        pref_mode = mPrefs.getWallpaperMode();
        setContentView(R.xml.prefs_main_menu);
        fragmentManager= getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        modeFlag=false;
        
        //fragmentTransaction.replace(R.id.mainFragment,mainFragment,"mainFragment");
        fragmentTransaction.replace(R.id.modeFragment,new PrefsLabsFragment());
        /*
        if(pref_mode.equals("Space")){
			fragmentTransaction.replace(R.id.modeFragment,new PrefsSpaceFragment());
        }
        else if(pref_mode.equals("Labs"))  {
        	fragmentTransaction.replace(R.id.modeFragment,new PrefsLabsFragment());
        }
        */
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
		
		if ((key.equals(mPrefs.PREF_SPACE_BACKGROUND_SOURCE)|| key.equals(mPrefs.PREF_LABS_BACKGROUND_SOURCE) ||key.equals("pref_mode") ||key.equals("pref_space_sound")
				 ||key.equals("pref_labs_sound") ||key.equals("pref_premium_enabled")) && hasBeenCreated){
			Log.i("PrefsMainMenu", "OnSharedPrefChanged");
			try{
				fragmentTransaction = fragmentManager.beginTransaction();
		    	pref_mode = mPrefs.getWallpaperMode();
		    	
		    	fragmentTransaction.replace(R.id.modeFragment,new PrefsLabsFragment());
		    	/*
				if(pref_mode.equals("Space")){
					fragmentTransaction.replace(R.id.modeFragment,new PrefsSpaceFragment());
		        }
		        else if(pref_mode.equals("Labs"))  {
		        	fragmentTransaction.replace(R.id.modeFragment,new PrefsLabsFragment());
		        }
		        */
		    	
		    	
				fragmentTransaction.commitAllowingStateLoss();
			}
			catch (Exception e){
				modeFlag=true;
			}
		}
	}
    @Override
    public void onResumeFragments(){
    	Log.i("PrefsMainFragment", "OnPostResumed");
    	super.onResumeFragments();
    	if (modeFlag){
    		fragmentTransaction = fragmentManager.beginTransaction();
	    	pref_mode = mPrefs.getWallpaperMode();
			if(pref_mode.equals("Space")){
				fragmentTransaction.replace(R.id.modeFragment,spaceFragment);
	        }
	        else if(pref_mode.equals("Labs"))  {
	        	fragmentTransaction.replace(R.id.modeFragment,labsFragment);
	        }
			fragmentTransaction.commitAllowingStateLoss();
			modeFlag=false;
    	}
    	
    }
    @Override
    public void onResume(){
    	super.onResume();
    	mPrefs.registerOnSharedPrefListener(this);
    }
    @Override
    public void onPause(){
    	super.onPause();
    	mPrefs.unregisterOnSharedPrefListener(this);
    }
}
