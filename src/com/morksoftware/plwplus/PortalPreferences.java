package com.morksoftware.plwplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class PortalPreferences {
	
	private SharedPreferences mPrefs;
	
	public static final String PREF_FIRST_RUN = "pref_first_run";
	public static final String PREF_WALLPAPER_MODE = "pref_wallpaper_mode";
	public static final String PREF_WALLPAPER_BACKGROUND = "pref_wallpaper_background";
	
	public static final int WALLPAPER_MODE_SPACE = 0;
	public static final int WALLPAPER_MODE_LABS = 1;	
	
	public static final int WALLPAPER_BACKGROUND_DEFAULT = 0;
		
	public PortalPreferences(Context ctx) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);		
	}
	
	public void setWallpaperMode(int mode) {
		Editor edit = mPrefs.edit();
		
		edit.putInt(PREF_WALLPAPER_MODE, mode);
		edit.putInt(PREF_WALLPAPER_BACKGROUND, WALLPAPER_BACKGROUND_DEFAULT);
		
		edit.commit();
	}
	
	public int getWallpaperMode() {
		return mPrefs.getInt(PREF_WALLPAPER_MODE, WALLPAPER_MODE_SPACE);
	}
	
	public void setWallpaperBackground(int resId) {
		Editor edit = mPrefs.edit();
		
		edit.putInt(PREF_WALLPAPER_BACKGROUND, resId);
		
		edit.commit();
	}
	
	public int getWallpaperBackground() {		
		return mPrefs.getInt(PREF_WALLPAPER_BACKGROUND, WALLPAPER_BACKGROUND_DEFAULT);
	}
		
	public boolean getFirstRun() {
		return mPrefs.getBoolean(PREF_FIRST_RUN, true);
	}
	
	/*
	 * Loads default preference values
	 */
	public void setDefaultPreferences() {
		Editor edit = mPrefs.edit();
		
		edit.putBoolean(PREF_FIRST_RUN, false);		
		edit.commit();
	}
	
	/*
	 * Register and unregister OnChangeListener
	 */
	public void registerOnSharedPrefListener(OnSharedPreferenceChangeListener listener) {
		mPrefs.registerOnSharedPreferenceChangeListener(listener);
	}
	public void unregisterOnSharedPrefListener(OnSharedPreferenceChangeListener listener) {
		mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
