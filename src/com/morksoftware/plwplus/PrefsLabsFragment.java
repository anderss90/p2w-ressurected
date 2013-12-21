package com.morksoftware.plwplus;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.morksoftware.plwplus.PrefsIncludedBackgrounds;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.preference.PreferenceFragment;
import android.support.v4.preference.PreferenceManagerCompat;
import android.util.Log;
import android.widget.Toast;
import com.morksoftware.plwplus.Utils;
@SuppressLint("NewApi")
public class PrefsLabsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener, OnPreferenceClickListener {
	
	private static final int SELECT_PICTURE = 1;
	private static int SELECT_FROM_INCLUDED = 2;
	private static int GALLERY_INTENT_CALLED = 3;
	private static int GALLERY_KITKAT_INTENT_CALLED = 4;
	private PrefsHelper mPrefs;
	private Preference BackgroundButton;
	private Preference LabsBackgroundSource;
	private Preference SpaceBackgroundSource;
	private Preference mModeButton=null;
	private Preference mEnableSoundButtonLabs;
	private Preference mEnableSoundButtonSpace;
	private Preference mPremiumButton;
	
    private String selectedImagePath;
    private String PreviousBackgroundSource;
    private PreferenceScreen preference_screen;
    private String pref_mode;
    private boolean mExtraFeaturesUnlocked=false;
    private boolean mShowPremiumPopup=true;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = new PrefsHelper(getActivity());
        mPrefs.registerOnSharedPrefListener(this);  
        PreviousBackgroundSource = mPrefs.getBackgroundSource();
        
        
    	
        // Load the preferences from an XML resource
        pref_mode = mPrefs.getWallpaperMode();
        Log.i("PrefsLabsFragment","OnCreate");
        
        
      //disabling shit if appropiate. (if statement in function)
        mPrefs.disablePremiumFeatures();
        
        if(pref_mode.equals("Space")){
        	addPreferencesFromResource(R.xml.pref_space_mode);
        }
        else if(pref_mode.equals("Labs"))  {
        	addPreferencesFromResource(R.xml.pref_labs_mode);
        }
        setListeners();
        
        // setting the summary of the mode
        setSummaries();
        
        handleExtraFeatures();
        
        // FJERN DETTE. DETTE ER KUN FOR DEBUGGING
        //Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
		//startActivity(backgroundIntent);
        
        
    }
    
   private void setSummaries(){
        if (mModeButton!=null){
	        if(pref_mode.equals("Space")){
	        	mModeButton.setSummary("Space Core");
	        }
	        else if(pref_mode.equals("Labs"))  {
	        	mModeButton.setSummary("Wheatley");
	        }
        }
    }
    
    private void handleExtraFeatures(){
    	
    	
    	//Checking if Premium is enabled
    	mExtraFeaturesUnlocked=mPrefs.getPremium();
    	
    	//Premium button
    	if (mPremiumButton!=null){
    		if (mExtraFeaturesUnlocked==true){
        		mPremiumButton.setSummary("Premium is Enabled");
        	}
    		else {
    			mPremiumButton.setSummary(getString(R.string.pref_get_premium_summary));
    		}
    	}
    	
    	
    	
    	// Handling EnableSound
    	
    	if (mEnableSoundButtonLabs!=null){
	    	if (mExtraFeaturesUnlocked==false){
	    		//mEnableSoundButton.setEnabled(false);
	    		mEnableSoundButtonLabs.setSummary("This feature requires premium");
	    		mEnableSoundButtonLabs.setEnabled(false);
	    	}
	    	else{
	    		mEnableSoundButtonLabs.setEnabled(true);
	    		mEnableSoundButtonLabs.setSummary(getString(R.string.pref_labs_sound_summary));
	    	}
	    	
    	}
    	if (mEnableSoundButtonSpace!=null){
	    	if (mExtraFeaturesUnlocked==false){
	    		//mEnableSoundButton.setEnabled(false);
	    		mEnableSoundButtonSpace.setSummary("This feature requires premium");
	    		mEnableSoundButtonSpace.setSelectable(false);
	    	}
	    	else{
	    		mEnableSoundButtonSpace.setEnabled(true);
	    		mEnableSoundButtonSpace.setSummary("pref_space_sound_summary");
	    	}
    	}
    }
    
    private void getPremiumPopUp(){
    	Toast.makeText(getActivity(), "This feature requires premium", Toast.LENGTH_SHORT).show();
    }
    private void setListeners(){
    	mModeButton = (Preference) findPreference("pref_mode");
    	BackgroundButton = (Preference) findPreference("pref_labs_background_picker");
    	LabsBackgroundSource = (Preference) findPreference("pref_labs_background_source");
    	SpaceBackgroundSource = (Preference) findPreference("pref_space_background_source");
    	mPremiumButton = (Preference) findPreference("pref_get_premium");
    	mEnableSoundButtonLabs = (Preference) findPreference("pref_labs_sound");
    	mEnableSoundButtonSpace = (Preference) findPreference("pref_space_sound");
        if (LabsBackgroundSource!=null) LabsBackgroundSource.setOnPreferenceChangeListener(this);
        if (SpaceBackgroundSource!=null) SpaceBackgroundSource.setOnPreferenceChangeListener(this);
        if (mModeButton!=null) mModeButton.setOnPreferenceChangeListener(this);
        if (BackgroundButton!=null) BackgroundButton.setOnPreferenceClickListener(this);
        if (mPremiumButton!=null) mPremiumButton.setOnPreferenceClickListener(this);
        if (mEnableSoundButtonLabs!=null) mEnableSoundButtonLabs.setOnPreferenceClickListener(this);
        if (mEnableSoundButtonSpace!=null) mEnableSoundButtonSpace.setOnPreferenceClickListener(this);
        
    }
    
  
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setListeners();
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newvalue) {
		Log.i("PrefsLabsFragment", "onPrefChange starting");
		String key = preference.getKey();
		if(key.equals("pref_labs_background_source")||key.equals("pref_space_background_source")){
			if (((String)newvalue).equals("included")){
				Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
				startActivityForResult(backgroundIntent,SELECT_FROM_INCLUDED);
			}
			else if (((String)newvalue).equals("gallery") && mExtraFeaturesUnlocked){
				
				if (Build.VERSION.SDK_INT <19){
				    Intent intent = new Intent(); 
				    intent.setType("image/*");
				    intent.setAction(Intent.ACTION_GET_CONTENT);
				    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)),GALLERY_INTENT_CALLED);
				} else {
				    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				    intent.addCategory(Intent.CATEGORY_OPENABLE);
				    intent.setType("image/jpeg");
				    startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
				}
			}
			else if(newvalue.equals("gallery") && mExtraFeaturesUnlocked==false) {
				 getPremiumPopUp();
				 mPrefs.disablePremiumFeatures();
			 }
		}
		else if (key.equals("pref_mode")){
			if(((String)newvalue).equals("Space")){
				if(mExtraFeaturesUnlocked==false){
					getPremiumPopUp();
					mPrefs.disablePremiumFeatures();
				}
			}
		}
		Log.i("PrefsLabsFragment", "onPrefChange returning");
		return true;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 if (null == data) return;
	        if (resultCode==Activity.RESULT_OK) {
	        	
	        	Uri originalUri = null;
	        	if (requestCode==GALLERY_INTENT_CALLED || requestCode==GALLERY_KITKAT_INTENT_CALLED){
		            if (requestCode == GALLERY_INTENT_CALLED) {
		            	Log.i("PrefsLabsFragment","Activityresult with requestcode: "+requestCode);
		                originalUri = data.getData();
		            } 
		            else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
		            	Log.i("PrefsLabsFragment","Activityresult with requestcode: "+requestCode);
		                originalUri = data.getData();
		                final int takeFlags = data.getFlags()
		                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
		                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		                // Check for the freshest data.
		                getActivity().getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
		            }
		            selectedImagePath = originalUri.toString();
	                Log.i("PrefsFragment","uri: "+selectedImagePath);
	                InputStream stream = null;
	        		try {
	        			stream = getActivity().getContentResolver().openInputStream(originalUri);
	        			BitmapRegionDecoder regionDecoder = null;
	        			regionDecoder = BitmapRegionDecoder.newInstance(stream, false);
	        			mPrefs.setWallpaperBackgroundPath(selectedImagePath);
		                mPrefs.setBackgroundSource(mPrefs.PREF_BACKGROUND_SOURCE_GALLERY);
		                PreviousBackgroundSource=mPrefs.PREF_BACKGROUND_SOURCE_GALLERY;
	        		} catch (Exception e1) {
	        			// TODO Auto-generated catch block
	        			Log.i("BitmapManager","OpenInputstream failed");
	        			e1.printStackTrace();
	        			Toast.makeText(getActivity(), "Error resolving image path. Only images on stored locally are supported", Toast.LENGTH_SHORT).show();
	                	return;
	        		} 
	        	}
	            else if (requestCode==SELECT_FROM_INCLUDED){
	            	Log.i("PrefsLabsFragment","Activityresult with requestcode: "+requestCode);
	            	mPrefs.setBackgroundSource(mPrefs.PREF_BACKGROUND_SOURCE_INCLUDED);
		        	PreviousBackgroundSource=mPrefs.PREF_BACKGROUND_SOURCE_INCLUDED;
		        }
		        Toast.makeText(getActivity(), "Background saved", Toast.LENGTH_SHORT).show();
	        }
	        // On non-complete picker
	        else {
	        	Log.i("PrefsMainFrag","Prev_BG_Source saves the day");
	        	mPrefs.setBackgroundSource(PreviousBackgroundSource);
	        }
	    }
	 public String getPath(Uri uri) {
	        String[] projection = { MediaStore.Images.Media.DATA };
	        Cursor cursor = getActivity().getContentResolver().query(uri,  projection, null, null, null);
	        if (cursor!=null){
		        int column_index;
		        try {
		        	column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		        }catch (IllegalArgumentException e){
		            e.printStackTrace();
		            return null;
		        }catch (NullPointerException e){
		            e.printStackTrace();
		            return null;
		        }
		        cursor.moveToFirst();
		        return cursor.getString(column_index);
	        }
	        else{
	            return uri.getPath();
	 		}
	    }
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		// TODO Auto-generated method stub
		if (key.equals("pref_premium_enabled")){
			handleExtraFeatures();
		}
		
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference==BackgroundButton){
			Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
			startActivityForResult(backgroundIntent,SELECT_FROM_INCLUDED);
		}
		else if(preference==mEnableSoundButtonLabs || preference==mEnableSoundButtonSpace){
			if (mExtraFeaturesUnlocked==false){
				getPremiumPopUp();
				mPrefs.disablePremiumFeatures();
			}
			
		}
		else if (preference==mPremiumButton){
			if (mExtraFeaturesUnlocked){
				Toast.makeText(getActivity(), "Premium is already Enabled", Toast.LENGTH_SHORT).show();
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=com.morksoftware.keytest"));
				startActivity(intent);
				
				/*
				mPrefs.setPremium(false);
				mExtraFeaturesUnlocked=false;
				Toast.makeText(getActivity(), "Premium Disabled", Toast.LENGTH_SHORT).show();
				*/
			}
			else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=com.morksoftware.keytest"));
			startActivity(intent);
			//mPrefs.setPremium(true);
			//mExtraFeaturesUnlocked=true;
			//Toast.makeText(getActivity(), "Premium Enabled", Toast.LENGTH_SHORT).show();
			}
		
		}
		return true;
	}
}