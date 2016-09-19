package com.cesoft.encuentrame3;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 04/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Por que no saltan geofences trackin y aviso? ======> Parece que funcionan cuando radio es muy grande. Poca precision de google?????
// http://stackoverflow.com/questions/21414160/how-to-increase-consistency-of-android-geofence-enter-exit-notifications
//    Make sure you're using a BroadcastReceiver and not a Service to receive the transitions, otherwise you will not/might not get it if your app gets killed/turned off. As discussed here: Android Geofence eventually stop getting transition intents
//    Make sure you recreate your geofences after the device is rebooted, preferably using a boot-broadcast receiver. As discussed here: Do Geofences remain active in android after a device reboot
//    One other common misconception, and which stumped me since it's different than IOS is that you'll always get a trigger immediately for a newly created geofence, if the device discovers that you're inside the geofence when creating it. I have solved this myself using a "grace period" for newly created geofences, which i explained in this thread: addProximityAlert doesn't work as expected
// http://stackoverflow.com/questions/19505614/android-geofence-eventually-stop-getting-transition-intents/19521823#19521823

//https://www.raywenderlich.com/103540/geofences-googleapiclient
public class CesGeofenceStore implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>
{
	private Context _context;
	private GoogleApiClient _GoogleApiClient;
	private PendingIntent _PendingIntent;
	private ArrayList<Geofence> _aGeofences;
		public int size(){return _aGeofences==null?0:_aGeofences.size();}

	public CesGeofenceStore(Context context, ArrayList<Geofence> geofences)
	{
		_context = context;
		_aGeofences = new ArrayList<>(geofences);
		_GoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
System.err.println("CesGeofenceStore:------------------"+geofences.size()+":"+this);
	}

	//// 4 ResultCallback<Status>
	@Override
	public void onResult(@NonNull Status result)
	{
		if(result.isSuccess())
			System.err.println("CesGeofenceStore:onResult------------------Success!");
		else if(result.hasResolution())
			System.err.println("CesGeofenceStore:onResult------------------hasResolution");
		else if(result.isCanceled())
			System.err.println("CesGeofenceStore:onResult------------------Canceled");
		else if(result.isInterrupted())
			System.err.println("CesGeofenceStore:onResult------------------Interrupted");
	}
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
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
			try
			{	// Submitting the request to monitor geofences.
				PendingResult<Status> pendingResult = LocationServices.GeofencingApi.addGeofences(_GoogleApiClient, GeofencingRequest, _PendingIntent);
				pendingResult.setResultCallback(this);// Set the result callbacks listener to this class.
			}catch(SecurityException ignored){}
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
		/*if(_PendingIntent == null)
		{
			Intent intent = new Intent(_Context, CesServiceAvisoGeo.class);
			_PendingIntent = PendingIntent.getService(_Context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return _PendingIntent;*/

		if(null != _PendingIntent)return _PendingIntent;
		Intent intent = new Intent("com.cesoft.encuentrame3.ACCION_RECIBE_GEOFENCE");
		// Return a PendingIntent to start the IntentService. Always create a PendingIntent sent to Location Services with FLAG_UPDATE_CURRENT,
		// so that sending the PendingIntent again updates the original. Otherwise, Location Services can't match the PendingIntent to requests made with it.
		return PendingIntent.getBroadcast(_context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
