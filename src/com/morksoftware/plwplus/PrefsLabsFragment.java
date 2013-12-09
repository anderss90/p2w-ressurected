package com.morksoftware.plwplus;

import com.morksoftware.plwplus.PrefsIncludedBackgrounds;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
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
public class PrefsLabsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener, OnPreferenceClickListener {
	
	private static int SELECT_FROM_INCLUDED = 2;
	private PrefsHelper mPrefs;
	private Preference BackgroundButton;
	private Preference LabsBackgroundSource;
	private Preference SpaceBackgroundSource;
	private Preference mModeButton=null;
	private Preference mEnableSoundButton;
	private Preference mPremiumButton;
	private static final int SELECT_PICTURE = 1;
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
    	}
    	
    	
    	
    	// Handling EnableSound
    	
    	if (mEnableSoundButton!=null){
	    	if (mExtraFeaturesUnlocked==false){
	    		//mEnableSoundButton.setEnabled(false);
	    		mEnableSoundButton.setSummary("This feature requires premium");
	    		
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
    	mEnableSoundButton = (Preference) findPreference("pref_labs_sound");
        if (LabsBackgroundSource!=null) LabsBackgroundSource.setOnPreferenceChangeListener(this);
        if (SpaceBackgroundSource!=null) SpaceBackgroundSource.setOnPreferenceChangeListener(this);
        if (mModeButton!=null) mModeButton.setOnPreferenceChangeListener(this);
        if (BackgroundButton!=null) BackgroundButton.setOnPreferenceClickListener(this);
        if (mPremiumButton!=null) mPremiumButton.setOnPreferenceClickListener(this);
        if (mPremiumButton!=null) mEnableSoundButton.setOnPreferenceClickListener(this);
        
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
		        Intent pictureActionIntent = new Intent(
	                    Intent.ACTION_PICK,
	                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	            pictureActionIntent.setType("image/*");
	            pictureActionIntent.setAction(Intent.ACTION_GET_CONTENT);
	            startActivityForResult(pictureActionIntent,SELECT_PICTURE);
			}
			 else if(mExtraFeaturesUnlocked==false) {
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
	        if (resultCode==-1) {
	            if (requestCode == SELECT_PICTURE) {
	                Uri selectedImageUri = data.getData();
	                //selectedImagePath = getPath(selectedImageUri);
	                selectedImagePath = selectedImageUri.toString();
	                Log.i("PrefsFragment","uri: "+selectedImagePath);
	                if (selectedImagePath!=null){
		                mPrefs.setWallpaperBackgroundPath(selectedImagePath);
		                mPrefs.setBackgroundSource(mPrefs.PREF_BACKGROUND_SOURCE_GALLERY);
		                PreviousBackgroundSource=mPrefs.PREF_BACKGROUND_SOURCE_GALLERY;
	                }
	                else {
	                	Toast.makeText(getActivity(), "Error resolving image path. Only images on stored locally are supported", Toast.LENGTH_SHORT).show();
	                	return;
	                }
	            }
	            else if (requestCode==SELECT_FROM_INCLUDED){
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
	        //else Toast.makeText(getActivity(), "Background saved", Toast.LENGTH_SHORT).show();
	        
	        
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
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference==BackgroundButton){
			Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
			startActivityForResult(backgroundIntent,SELECT_FROM_INCLUDED);
		}
		else if(preference==mEnableSoundButton){
			if (mExtraFeaturesUnlocked==false){
				getPremiumPopUp();
				mPrefs.disablePremiumFeatures();
			}
			
		}
		else if (preference==mPremiumButton){
			if (mExtraFeaturesUnlocked){
				mPrefs.setPremium(false);
				mExtraFeaturesUnlocked=false;
				Toast.makeText(getActivity(), "Premium Disabled", Toast.LENGTH_SHORT).show();
			}
			else {
			mPrefs.setPremium(true);
			mExtraFeaturesUnlocked=true;
			Toast.makeText(getActivity(), "Premium Enabled", Toast.LENGTH_SHORT).show();
			}
		
		}
		return true;
	}
}