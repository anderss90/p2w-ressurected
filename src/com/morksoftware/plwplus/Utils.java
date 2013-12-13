package com.morksoftware.plwplus;

import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {

	public static final boolean keyIsInstalled(Context ctx) {
		String thisPackage = "com.morksoftware.plwplus";
		//String keyPackage = "com.morksoftware.plwkey";
		String keyPackage = "com.example.keytest";
		int match = ctx.getPackageManager().checkSignatures(thisPackage, keyPackage);
		
		return (match == PackageManager.SIGNATURE_MATCH) ? true : false;
	}
}
