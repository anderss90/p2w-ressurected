package com.morksoftware.plwplus;

import java.lang.reflect.Field;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
//import android.renderscript.Allocation.MipmapControl;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class PrefsIncludedBackgrounds extends Activity implements OnClickListener, OnSharedPreferenceChangeListener{
	private Intent returnIntent;
	public Display mDisplay;
    private int[] mPics;
    private int mSelectedId = 0;
    
    private ImageView mImgViewLarge;
    private Gallery mPreviewGallery;
    private ListView mImageList;
    private Context mCtx = this;
    private PrefsHelper mPrefs;
    private int maxWidth=600;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnIntent= new Intent();
    	setResult(RESULT_CANCELED, returnIntent);
        // Set layout
        setContentView(R.layout.preference_background_menu);
        
        //
        mPrefs = new PrefsHelper(this);
        
        if(mPrefs.getWallpaperMode().equals(mPrefs.PREF_WALLPAPER_MODE_LABS)) {
        	mPics = getBackgrounds("labs");
        }
        else {
        	mPics = getBackgrounds("space");
        }
        
        //some testing with listview
        
    
        mImageList = (ListView) findViewById(R.id.ListView01);
        mImageList.setAdapter(new ImageAdapter_listview(this));
        mImageList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				
				mSelectedId = mPics[position];
			
				
				mPrefs.setWallpaperBackgroundID(mSelectedId);
				exitListView();
			}
		});   
        try {
        	mDisplay = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        } catch (NullPointerException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i("NPE", "NPE getting mDisplay");
			}
    }
    
	private int [] getBackgrounds(String tag){
			
			Field[] ID_Fields = R.drawable.class.getFields();
	        int[] wallpapers = new int [ID_Fields.length];
	        int n_wallpapers=0;
	        for(int i = 0; i < ID_Fields.length; i++){
	            try {
	                String name = ID_Fields[i].getName();
	                if (name.contains(tag) && name.contains("background")){
	                	wallpapers [n_wallpapers]=ID_Fields[i].getInt(null);
	                	Log.i("tag",name);
	                	n_wallpapers +=1;
	                }
	                
	            } catch (IllegalArgumentException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        int [] returnArray = new int[n_wallpapers];
	        if (n_wallpapers!=0){
	        	
		        for (int i=0;i<n_wallpapers;i++){
		        	returnArray[i]=wallpapers[i];
		        }
	        }
	        else {
	        	returnArray =  new int[1];
	        	returnArray[0]=R.drawable.ic_launcher;
	        }
	        return returnArray;
		}
	    
	
	
	
	public class ImageAdapter_listview extends BaseAdapter {
		
		    	private Context ctx;
		    	//int imageBackground;
		    	
		    	public ImageAdapter_listview(Context c) {
					ctx = c;
				}
		
				@Override
		    	public int getCount() {
		    		
		    		return mPics.length;
		    	}		
		
		    	@Override
		    	public Object getItem(int arg0) {
		    		
		    		return arg0;
		    	}
		
		    	@Override
		    	public long getItemId(int arg0) {
		    		
		    		return arg0;
		    	}
		
		    	@Override
		    	public View getView(int position, View convertView, ViewGroup parent) {
		    		ImageView iv = new ImageView(ctx);
		    		
		    		
		    		//iv.setScaleType(ImageView.ScaleType.);
		    		// mDisplay.getWidth(),(9/16)*0,001*mDisplay.getWidth())
		    		double ListHeight=(mDisplay.getWidth()*(0.56));
		    		Log.i("getwidth*(9/16)",Double.toString(ListHeight));
		    		iv.setLayoutParams(new ListView.LayoutParams(mDisplay.getWidth(),(int)ListHeight));
		    		Log.i("mDisplay.getwidth",Integer.toString(mDisplay.getWidth()));
		    		iv.setBackgroundResource(0);
		    		iv.setBackgroundColor(Color.BLACK);
		    		
		    		//tegner bilder selv. Innebygde laster hele full HD-bildet -_-
		    		BitmapFactory.Options  bmOptions = new BitmapFactory.Options();
		    		//bmOptions.inScaled=true;
		    		//bmOptions.inDensity=100;
		    		//bmOptions.inTargetDensity=25;
		    		
		    		bmOptions.inJustDecodeBounds = true;
		    		// Sample the asset, without loading the pixels
		    		BitmapFactory.decodeResource(mCtx.getResources(), mPics[position], bmOptions);
		    		float sourceImageWidth=bmOptions.outWidth;
		    		float screenWidth= mDisplay.getWidth();
		    		if (screenWidth>maxWidth){
		    			screenWidth=maxWidth;
		    		}
		    		int scaleFactor;
		    		scaleFactor= (int) (sourceImageWidth/screenWidth);
		    		
		    		Log.i("PrefsInclBGs","scaleFactor: "+scaleFactor);
		    		bmOptions.inSampleSize=scaleFactor;
		    		bmOptions.inJustDecodeBounds = false;
		    		Bitmap bitmap = BitmapFactory.decodeResource(mCtx.getResources(), mPics[position], bmOptions);
		    		iv.setImageBitmap(bitmap);
		    		
		    		//iv.setImageResource(mPics[position]);
		    		
		    		return iv;
		    	}
	}
	    
	public void exitChooser(View view){
		//Intent returnIntent = new Intent();
		//setResult(RESULT_CANCELED, returnIntent); 
		//mPrefs.setWallpaperBackgroundID(mSelectedId);
		setResult(RESULT_OK, returnIntent);
		finish();
		return;
	}
	
	public void exitListView(){
		//Intent returnIntent = new Intent();
		//setResult(RESULT_CANCELED, returnIntent); 
		mPrefs.setWallpaperBackgroundID(mSelectedId);
		this.setResult(RESULT_OK, returnIntent);
		finish();
	}


	@Override
	public void onClick(View v) {
		if(v == mImgViewLarge) {	
			
			//mPrefs.setWallpaperBackgroundID(mSelectedId);
			//setResult(RESULT_OK, returnIntent);
			//Log.i("tag","Click in BG from included");
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
