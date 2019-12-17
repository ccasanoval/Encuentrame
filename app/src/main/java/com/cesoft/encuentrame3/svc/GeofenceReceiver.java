package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/03/2016.
//
public class GeofenceReceiver extends BroadcastReceiver
{
	//private static final String TAG = GeofenceReceiver.class.getSimpleName();
	public static final int REQUEST_CODE = 5555;

	// On Android 8.0 (API level 26) and higher, if an app is running in the background while
	// monitoring a geofence, then the device responds to geofencing events every couple of minutes
	@Override
	public void onReceive(Context context, Intent intent)
	{
		GeofenceJobIntentService.enqueueWork(context, intent);
	}
}