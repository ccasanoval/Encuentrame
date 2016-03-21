package com.cesoft.encuentrame;

import java.util.ArrayList;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 04/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Por que no saltan geofences trackin y aviso?
// http://stackoverflow.com/questions/21414160/how-to-increase-consistency-of-android-geofence-enter-exit-notifications
//    Make sure you're using a BroadcastReceiver and not a Service to receive the transitions, otherwise you will not/might not get it if your app gets killed/turned off. As discussed here: Android Geofence eventually stop getting transition intents
//    Make sure you recreate your geofences after the device is rebooted, preferably using a boot-broadcast receiver. As discussed here: Do Geofences remain active in android after a device reboot
//    One other common misconception, and which stumped me since it's different than IOS is that you'll always get a trigger immediately for a newly created geofence, if the device discovers that you're inside the geofence when creating it. I have solved this myself using a "grace period" for newly created geofences, which i explained in this thread: addProximityAlert doesn't work as expected
// http://stackoverflow.com/questions/19505614/android-geofence-eventually-stop-getting-transition-intents/19521823#19521823
public class CesGeofenceStore implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>
{
	private Context _Context;
	private GoogleApiClient _GoogleApiClient;
	private PendingIntent _PendingIntent;
	private ArrayList<Geofence> _aGeofences;

	public CesGeofenceStore(Context context, ArrayList<Geofence> geofences)
	{
		_Context = context;
		_aGeofences = new ArrayList<>(geofences);
		// Build a new GoogleApiClient, specify that we want to use LocationServices by adding the API to the client,
		// specify the connection callbacks are in this class as well as the OnConnectionFailed method.
		_GoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
System.err.println("CesGeofenceStore:------------------"+geofences.size()+":"+this);
	}

	//// 4 ResultCallback<Status>
	@Override
	public void onResult(@NonNull Status result)
	{
		if(result.isSuccess())
			System.err.println("CesGeofenceStore:onResult------------------Success!"+this);
		else if(result.hasResolution())
			System.err.println("CesGeofenceStore:onResult------------------hasResolution");
		else if(result.isCanceled())
			System.err.println("CesGeofenceStore:onResult------------------Canceled");
		else if(result.isInterrupted())
			System.err.println("CesGeofenceStore:onResult------------------Interrupted");
	}
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		System.err.println("CesGeofenceStore:e:Connection failed");
	}
	//// 4 ConnectionCallbacks
	@Override
	public void onConnected(Bundle connectionHint)
	{
		System.err.println("CesGeofenceStore:onConnected");
		// We're connected, now we need to create a GeofencingRequest with the geofences we have stored.
		if(_aGeofences.size() > 0)
		{
			GeofencingRequest GeofencingRequest = new GeofencingRequest.Builder().addGeofences(_aGeofences).build();
			_PendingIntent = createRequestPendingIntent();
			// Submitting the request to monitor geofences.
			//if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
			try
			{
				PendingResult<Status> pendingResult = LocationServices.GeofencingApi.addGeofences(_GoogleApiClient, GeofencingRequest, _PendingIntent);
				pendingResult.setResultCallback(this);// Set the result callbacks listener to this class.
			}catch(SecurityException se){}
		}
	}
	@Override
	public void onConnectionSuspended(int cause)
	{
		System.err.println("CesGeofenceStore:onConnectionSuspended:e:");
	}

	//______________________________________________________________________________________________
	public void clear()
	{
		System.err.println("CesGeofenceStore:clear:"+this);
		if(_PendingIntent != null)
			LocationServices.GeofencingApi.removeGeofences(_GoogleApiClient, _PendingIntent);
	}

	// This creates a PendingIntent that is to be fired when geofence transitions take place. In this instance, we are using an IntentService to handle the transitions.
	private PendingIntent createRequestPendingIntent()
	{
		if(_PendingIntent == null)
		{
			Intent intent = new Intent(_Context, CesServiceAvisoGeo.class);
			_PendingIntent = PendingIntent.getService(_Context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return _PendingIntent;
	}

}
