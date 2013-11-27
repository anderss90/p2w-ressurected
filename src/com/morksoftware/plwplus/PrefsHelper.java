package com.morksoftware.plwplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsHelper {
	
	private SharedPreferences mPrefs;
	
	public static final String PREF_FIRST_RUN = "pref_first_run";
	public static final String PREF_WALLPAPER_MODE = "pref_mode";
	public static final String PREF_WALLPAPER_MODE_SPACE = "Space";
	public static final String PREF_WALLPAPER_MODE_LABS = "Labs";
	public static final String PREF_WALLPAPER_MODE_DEFAULT = "Labs";
	public static final String PREF_SPACE_BACKGROUND = "pref_space_background";
	public static final String PREF_SPACE_BACKGROUND_DEFAULT_PATH = "def";
	public static final int PREF_SPACE_BACKGROUND_DEFAULT_ID = R.drawable.raw_space_background_1;
	public static final String PREF_LABS_BACKGROUND = "pref_labs_background";
	public static final String PREF_LABS_BACKGROUND_DEFAULT_PATH = "def";
	public static final int PREF_LABS_BACKGROUND_DEFAULT_ID = R.drawable.labs_raw_background_3_fix;
	public static final String PREF_LABS_BACKGROUND_SOURCE = "pref_labs_background_source";
	public static final String PREF_SPACE_BACKGROUND_SOURCE = "pref_space_background_source";
	public static final String PREF_BACKGROUND_SOURCE_INCLUDED = "included";
	public static final String PREF_BACKGROUND_SOURCE_GALLERY = "gallery";
	public static final String PREF_BACKGROUND_SOURCE_DEFAULT = "included";
	public static final String WALLPAPER_BACKGROUND_DEFAULT = "Labs";
	
	//for background lists
	private static final int pref_wallpaper_list_length = 10;
	private static final String KEY_LIST_LENGTH = "pref_background_list_length";
	private static final String KEY_VALUE_PREFIX = "pref_background_list_";
	public PrefsHelper(Context ctx) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
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
		Log.i("PrefsHelper","BG_source set");
	}
	
	public String getWallpaperMode() {
		return mPrefs.getString(PREF_WALLPAPER_MODE, PREF_WALLPAPER_MODE_LABS);
	}
	
	public void setWallpaperMode(String mode) {
		Editor edit = mPrefs.edit();
		
		edit.putString(PREF_WALLPAPER_MODE, mode);
		//edit.putString(PREF_SPACE_BACKGROUND, WALLPAPER_BACKGROUND_DEFAULT);
		
		edit.commit();
		Log.i("PrefsHelper","pref_mode set");
	}
	
	public void setWallpaperBackgroundID(int resId) {
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putInt(PREF_SPACE_BACKGROUND, resId);
		}
		else if (getWallpaperMode().equals("Labs")){
			edit.putInt(PREF_LABS_BACKGROUND, resId);
		}
		edit.commit();
		Log.i("PrefsHelper","BG_ID set");
	}
	
	public void setWallpaperBackgroundPath(String resPath) {
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putString(PREF_SPACE_BACKGROUND, resPath);
		}
		else if (getWallpaperMode().equals("Labs")){
			edit.putString(PREF_LABS_BACKGROUND, resPath);
		}
		edit.commit();
		Log.i("PrefsHelper","BG_Path set");
	}
	
	public String getWallpaperBackgroundPath() {
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getString(PREF_SPACE_BACKGROUND,PREF_SPACE_BACKGROUND_DEFAULT_PATH);
		}
		else {
			return mPrefs.getString(PREF_LABS_BACKGROUND,PREF_LABS_BACKGROUND_DEFAULT_PATH);
		}
	}
	
	public int getWallpaperBackgroundID() {
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getInt(PREF_SPACE_BACKGROUND,PREF_SPACE_BACKGROUND_DEFAULT_ID);
		}
		else {
			return mPrefs.getInt(PREF_LABS_BACKGROUND,PREF_LABS_BACKGROUND_DEFAULT_ID);
		}
	}
		
	public void insertIntoWallpaperBackgroundList(int index, String item){
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
				edit.putString(KEY_VALUE_PREFIX + "space" + index, item);
			edit.commit();
		}
		else if (getWallpaperMode().equals("Labs")){
				edit.putString(KEY_VALUE_PREFIX + "labs" + index, item);
			edit.commit();
		}
	}
	public void setWallpaperBackgroundList(int[] list){
		
		Editor edit = mPrefs.edit();
		if (getWallpaperMode().equals("Space")){
			edit.putInt(KEY_LIST_LENGTH, pref_wallpaper_list_length);
			for (int i=0;i<pref_wallpaper_list_length;i++){
				edit.putInt(KEY_VALUE_PREFIX + "space" + i, list [i]);
			}
			edit.commit();
		}
		else if (getWallpaperMode().equals("Labs")){
			edit.putInt(KEY_LIST_LENGTH, pref_wallpaper_list_length);
			for (int i=0;i<pref_wallpaper_list_length;i++){
				edit.putInt(KEY_VALUE_PREFIX + "labs" + i, list [i]);
			}
			edit.commit();
		}
		
	}
	
	public String getWallpaperBackgroundListString(int index){
		if (getWallpaperMode().equals("Space")){
			return mPrefs.getString(KEY_VALUE_PREFIX+"space"+index,"default");
		}
		else {
			return mPrefs.getString(KEY_VALUE_PREFIX+"labs"+index,"default");
		}
	}
	
	public int [] getWallpaperBackgroundList(){
		int [] returnvalues = new int[pref_wallpaper_list_length];
		if (getWallpaperMode().equals("Space")){
			for (int i=0;i<pref_wallpaper_list_length;i++){
				returnvalues[i] = mPrefs.getInt(KEY_VALUE_PREFIX+"space"+i,-1);
			}
		}
		else if (getWallpaperMode().equals("Labs")){
			for (int i=0;i<pref_wallpaper_list_length;i++){
				returnvalues[i] = mPrefs.getInt(KEY_VALUE_PREFIX+"labs"+i,-1);
			}
		}
		return returnvalues;
	}
	
	public boolean getFirstRun() {
		return mPrefs.getBoolean(PREF_FIRST_RUN, true);
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
		setBackgroundSource(PREF_BACKGROUND_SOURCE_DEFAULT);
		setWallpaperMode(PREF_WALLPAPER_MODE_DEFAULT);
		setWallpaperBackgroundID(PREF_LABS_BACKGROUND_DEFAULT_ID);
		PreferenceManager.setDefaultValues(ctx, R.xml.prefs_main_menu, false);
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
