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

public class PrefsLabsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {
	
	private static int SELECT_FROM_INCLUDED = 2;
	private PrefsHelper mPrefs;
	private Preference BackgroundButton;
	private Preference LabsBackgroundSource;
	private Preference SpaceBackgroundSource;
	private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private String PreviousBackgroundSource;
    private PreferenceScreen preference_screen;
    private String pref_mode;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = new PrefsHelper(getActivity());
        mPrefs.registerOnSharedPrefListener(this);  
        PreviousBackgroundSource = mPrefs.getBackgroundSource();
    	
        // Load the preferences from an XML resource
        pref_mode = mPrefs.getWallpaperMode();
        Log.i("PrefsMainFragment","OnCreate");
        addPreferencesFromResource(R.xml.pref_labs_mode);
  
        setListeners();
        
        // FJERN DETTE. DETTE ER KUN FOR DEBUGGING
        //Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
		//startActivity(backgroundIntent);
        
        
    }
    private void setListeners(){
    	BackgroundButton = (Preference) findPreference("pref_background");
    	LabsBackgroundSource = (Preference) findPreference("pref_labs_background_source");
    	SpaceBackgroundSource = (Preference) findPreference("pref_space_background_source");
        if (LabsBackgroundSource!=null) LabsBackgroundSource.setOnPreferenceChangeListener(this);
        if (SpaceBackgroundSource!=null) SpaceBackgroundSource.setOnPreferenceChangeListener(this);
    }
    
  
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setListeners();
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newvalue) {
		Log.i("PrefsMainFragment", "onPrefChange starting");
		String key = preference.getKey();
		if (((String)newvalue).equals("included")){
			Intent backgroundIntent = new Intent(getActivity(),PrefsIncludedBackgrounds.class);
			startActivityForResult(backgroundIntent,SELECT_FROM_INCLUDED);
		}
		else if (((String)newvalue).equals("gallery")){
			/*
			Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	        intent.setType("image/*");
	        intent.setAction(Intent.ACTION_GET_CONTENT);
	        startActivityForResult(Intent.createChooser(intent,
	                "Select Picture"), SELECT_PICTURE);
	        */
	        Intent pictureActionIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pictureActionIntent.setType("image/*");
            //pictureActionIntent.putExtra("return-data", true);
            pictureActionIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(pictureActionIntent,SELECT_PICTURE);
	        
	        
	        
		}
		Log.i("PrefsMainFragment", "onPrefChange returning");
		return true;
	}
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (resultCode==-1) {
	            if (requestCode == SELECT_PICTURE) {
	                Uri selectedImageUri = data.getData();
	                selectedImagePath = getPath(selectedImageUri);
	                if (selectedImagePath!=null){
		                mPrefs.setWallpaperBackgroundPath(selectedImagePath);
		                //mPrefs.setBackgroundSource("gallery");
		                PreviousBackgroundSource=mPrefs.PREF_BACKGROUND_SOURCE_GALLERY;
	                }
	                else {
	                	Toast.makeText(getActivity(), "Error resolving image path. Only images on stored locally are supported", Toast.LENGTH_SHORT).show();
	                	return;
	                }
	            }
	            else if (requestCode==SELECT_FROM_INCLUDED){
		        	//mPrefs.setBackgroundSource("included");
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
	        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
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
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
}