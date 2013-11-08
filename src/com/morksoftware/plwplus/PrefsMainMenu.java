package com.morksoftware.plwplus;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.support.v4.app.FragmentActivity;
import android.support.v4.preference.PreferenceFragment;


public class PrefsMainMenu extends FragmentActivity {
	public static String PACKAGE_NAME;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsMainFragment())
                .commit();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main, menu);
				
		return super.onCreateOptionsMenu(menu);
	}
}
