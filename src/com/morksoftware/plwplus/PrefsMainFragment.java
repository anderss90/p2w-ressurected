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

public class PrefsMainFragment extends PreferenceFragment{
	
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
        PreviousBackgroundSource = mPrefs.getBackgroundSource();
    	
        // Load the preferences from an XML resource
        pref_mode = mPrefs.getWallpaperMode();
        Log.i("PrefsMainFragment","OnCreate");
        addPreferencesFromResource(R.layout.prefs_mode_picker);

    }
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
}
