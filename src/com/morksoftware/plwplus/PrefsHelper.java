package com.morksoftware.plwplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import com.morksoftware.plwplus.Utils;

public class PrefsHelper {
	
	private SharedPreferences mPrefs;
	private Context mCtx;
	public static final String PREF_FIRST_RUN = "pref_first_run";
	public static final String PREF_WALLPAPER_MODE = "pref_mode";
	public static final String PREF_WALLPAPER_MODE_SPACE = "Space";
	public static final String PREF_WALLPAPER_MODE_LABS = "Labs";
	public static final String PREF_WALLPAPER_MODE_DEFAULT = "Labs";
	
	public static final String PREF_SPACE_BACKGROUND_ID = "pref_space_background_id";
	public static final String PREF_SPACE_BACKGROUND_PATH = "pref_space_background_path";
	public static final String PREF_LABS_BACKGROUND_ID = "pref_labs_background_id";
	public static final String PREF_LABS_BACKGROUND_PATH = "pref_labs_background_path";
	public static final int PREF_SPACE_BACKGROUND_DEFAULT_ID = R.drawable.raw_space_background_1;
	public static final int PREF_LABS_BACKGROUND_DEFAULT_ID = R.drawable.labs_raw_background_3_fix;
	public static final String WALLPAPER_BACKGROUND_DEFAULT = "Labs";
	
	public static final String PREF_LABS_BACKGROUND_SOURCE = "pref_labs_background_source";
	public static final String PREF_SPACE_BACKGROUND_SOURCE = "pref_space_background_source";
	public static final String PREF_BACKGROUND_SOURCE_INCLUDED = "included";
	public static final String PREF_BACKGROUND_SOURCE_GALLERY = "gallery";
	public static final String PREF_BACKGROUND_SOURCE_DEFAULT = "included";
	
	
	//for background lists
	private static final int pref_wallpaper_list_length = 10;
	private static final String KEY_LIST_LENGTH = "pref_background_list_length";
	private static final String KEY_VALUE_PREFIX = "pref_background_list_";
	public PrefsHelper(Context ctx) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		mCtx = ctx;
	}
	
	public Boolean getPremium() {
		return mPrefs.getBoolean("pref_premium_enabled", true);
	}
	
	public void setPremium(boolean input) {
		Editor edit = mPrefs.edit();
		
		edit.putBoolean("pref_premium_enabled", input);
		edit.commit();
		Log.i("PrefsHelper","pref_premium_enabled set to: "+input);
	}
	public void setPremiumFromUtils(){
		setPremium(Utils.keyIsInstalled(mCtx));
	}
	public void disablePremiumFeatures(){
		if (getPremium()==false){
			setBackgroundSource(PREF_BACKGROUND_SOURCE_DEFAULT);
			setWallpaperMode(PREF_WALLPAPER_MODE_DEFAULT);
			setSoundEnabled(false);
		}
	}
	
	public String getWheatleySnapPosition(){
		return mPrefs.getString("pref_labs_position","Both");
	}
	public int getMovementSpeed(){
		if (getWallpaperMode().equals("Space")){
			return Integer.parseInt(mPrefs.getString("pref_space_speed","50"));
		}
		else{
			return Integer.parseInt(mPrefs.getString("pref_labs_speed","50"));
		}
	}
	
	public boolean getEnableRandomMovement(){
		return mPrefs.getBoolean("pref_labs_random_movement_checkbox", true);
	}
	
	public int getRandomMovementInterval(){
			return Integer.parseInt(mPrefs.getString("pref_labs_random_movement_interval","10"));
	} 
	
	public boolean getEnableSound(){
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getBoolean("pref_space_sound",true);
		}
		else{
			return mPrefs.getBoolean("pref_labs_sound",true);
		}
	}
	
	public String getBackgroundSource(){
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getString(PREF_SPACE_BACKGROUND_SOURCE, PREF_BACKGROUND_SOURCE_DEFAULT);
		}
		else{
			return mPrefs.getString(PREF_LABS_BACKGROUND_SOURCE, PREF_BACKGROUND_SOURCE_DEFAULT);
		}
	}
	public void setBackgroundSource(String source){
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putString(PREF_SPACE_BACKGROUND_SOURCE, source);
		}
		else{
			edit.putString(PREF_LABS_BACKGROUND_SOURCE, source);
		}
		edit.commit();
		Log.i("PrefsHelper","BG_source set to: "+source);
	}
	
	public String getWallpaperMode() {
		return mPrefs.getString(PREF_WALLPAPER_MODE, PREF_WALLPAPER_MODE_LABS);
	}
	
	public void setWallpaperMode(String mode) {
		Editor edit = mPrefs.edit();
		
		edit.putString(PREF_WALLPAPER_MODE, mode);
		//edit.putString(PREF_SPACE_BACKGROUND, WALLPAPER_BACKGROUND_DEFAULT);
		
		edit.commit();
		Log.i("PrefsHelper","pref_mode set to: "+mode);
	}
	
	
	public void setWallpaperBackgroundID(int resId) {
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putInt(PREF_SPACE_BACKGROUND_ID, resId);
		}
		else if (getWallpaperMode().equals("Labs")){
			edit.putInt(PREF_LABS_BACKGROUND_ID, resId);
		}
		edit.commit();
		Log.i("PrefsHelper","BG_ID set");
	}
	
	public int getWallpaperBackgroundID() {
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getInt(PREF_SPACE_BACKGROUND_ID,PREF_SPACE_BACKGROUND_DEFAULT_ID);
		}
		else {
			return mPrefs.getInt(PREF_LABS_BACKGROUND_ID,PREF_LABS_BACKGROUND_DEFAULT_ID);
		}
	}
	
	public void setWallpaperBackgroundPath(String resPath) {
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putString(PREF_SPACE_BACKGROUND_PATH, resPath);
		}
		else if (getWallpaperMode().equals("Labs")){
			edit.putString(PREF_LABS_BACKGROUND_PATH, resPath);
		}
		edit.commit();
		Log.i("PrefsHelper","BG_Path set");
	}
	
	public String getWallpaperBackgroundPath() {
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getString(PREF_SPACE_BACKGROUND_PATH,"null");
		}
		else {
			return mPrefs.getString(PREF_LABS_BACKGROUND_PATH,"null");
		}
	}
	
	
	
	public boolean getFirstRun() {
		return mPrefs.getBoolean(PREF_FIRST_RUN, true);
	}
	public boolean getTapActionsEnabled(){
		return mPrefs.getBoolean("pref_space_tap_enable", true);
	}
	
	public boolean getSoundEnabled(){
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getBoolean("pref_space_sound",true);
		}
		else {
			return mPrefs.getBoolean("pref_labs_sound",true);
		}
		
	}
	
	public void setSoundEnabled(boolean bool){
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putBoolean("pref_space_sound", bool);	
		}
		else {
			edit.putBoolean("pref_labs_sound", bool);	
		}
		edit.commit();
	}
	
	/*
	 * Loads default preference values
	 */
	public void setDefaultPreferences(Context ctx) {
		// clearing shared prefs
		
		
		Editor edit = mPrefs.edit();
		//edit.clear();
		edit.putBoolean(PREF_FIRST_RUN, false);	
		edit.commit();
		
		//setting default labs settings
		setWallpaperMode(PREF_WALLPAPER_MODE_LABS);
		setBackgroundSource(PREF_BACKGROUND_SOURCE_DEFAULT);
		setWallpaperBackgroundID(PREF_LABS_BACKGROUND_DEFAULT_ID);
		//setting default space settings
		setWallpaperMode(PREF_WALLPAPER_MODE_SPACE);
		setBackgroundSource(PREF_BACKGROUND_SOURCE_DEFAULT);
		setWallpaperBackgroundID(PREF_SPACE_BACKGROUND_DEFAULT_ID);
		//setting default mode
		setWallpaperMode(PREF_WALLPAPER_MODE_DEFAULT);
		setPremium(false);
		
		PreferenceManager.setDefaultValues(ctx, R.layout.prefs_mode_picker, false);
		Log.i("Prefshelper","setDefaultPreferences");
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
