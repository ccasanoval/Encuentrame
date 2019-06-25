package com.cesoft.encuentrame3.svc;

import java.util.ArrayList;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 04/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
// http://stackoverflow.com/questions/21414160/how-to-increase-consistency-of-android-geofence-enter-exit-notifications
//    Make sure you're using a BroadcastReceiver and not a Service to receive the transitions, otherwise you will not/might not get it if your app gets killed/turned off. As discussed here: Android Geofence eventually stop getting transition intents
//    Make sure you recreate your geofences after the device is rebooted, preferably using a boot-broadcast receiver. As discussed here: Do Geofences remain active in android after a device reboot
//    One other common misconception, and which stumped me since it's different than IOS is that you'll always get a trigger immediately for a newly created geofence, if the device discovers that you're inside the geofence when creating it. I have solved this myself using a "grace period" for newly created geofences, which i explained in this thread: addProximityAlert doesn't work as expected
// http://stackoverflow.com/questions/19505614/android-geofence-eventually-stop-getting-transition-intents/19521823#19521823

//https://www.raywenderlich.com/103540/geofences-googleapiclient
////////////////////////////////////////////////////////////////////////////////////////////////////
class CesGeofenceStore implements ConnectionCallbacks, OnConnectionFailedListener
{
	private static final String TAG = CesGeofenceStore.class.getSimpleName();

	private PendingIntent pendingIntent;
	private ArrayList<Geofence> aGeofences;
	private GeofencingClient geoFenceClient;
	private Application app;

	//----------------------------------------------------------------------------------------------
	CesGeofenceStore(Application app, ArrayList<Geofence> geofences)
	{
		try
		{
			this.app = app;
			aGeofences = new ArrayList<>(geofences);
			new GoogleApiClient
					.Builder(app)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build()
					.connect();
			geoFenceClient = LocationServices.getGeofencingClient(app);
		}
		catch(Exception e)
		{
			Log.e(TAG, "CONSTRUCTOR:e:---------geofences:"+geofences.size()+"---------------------", e);
		}
	}

	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
		Log.e(TAG, "CesGeofenceStore:e:Connection failed");
	}
	//// 4 ConnectionCallbacks
	@Override
	public void onConnected(Bundle connectionHint)
	{
		// We're connected, now we need to create a GeofencingRequest with the geofences we have stored.
		if( ! aGeofences.isEmpty())
		{
			try
			{
				GeofencingRequest geofencingRequest = new GeofencingRequest.Builder().addGeofences(aGeofences).build();
				pendingIntent = createRequestPendingIntent();
				if(pendingIntent == null)return;

				// Submitting the request to monitor geofences.
				geoFenceClient.addGeofences(geofencingRequest, pendingIntent);
			}
			catch(SecurityException ignore) { }
		}
	}
	@Override
	public void onConnectionSuspended(int cause)
	{
		Log.w(TAG, "CesGeofenceStore:onConnectionSuspended:e:");
	}

	//______________________________________________________________________________________________
	void clear()
	{
		if(pendingIntent != null)
			geoFenceClient.removeGeofences(pendingIntent);
	}

	// This creates a PendingIntent that is to be fired when geofence transitions take place. In this instance, we are using an IntentService to handle the transitions.
	private PendingIntent createRequestPendingIntent()
	{
		try
		{
			if(null != pendingIntent)return pendingIntent;
			Intent intent = new Intent("com.cesoft.encuentrame3.ACCION_RECIBE_GEOFENCE");
			// Return a PendingIntent to start the IntentService. Always create a PendingIntent sent to Location Services with FLAG_UPDATE_CURRENT,
			// so that sending the PendingIntent again updates the original. Otherwise, Location Services can't match the PendingIntent to requests made with it.
			return PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		catch(Exception e)
		{
			Log.e(TAG, "createRequestPendingIntent:e:-----------------------------------------------",e);
			return null;
		}
	}
}
