package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/03/2016.
//En lugar de CesServiceAvisoGeo se utiliza este BroadcastReceiver porque dicen es mas fiable
public class CesGeofenceReceiver extends BroadcastReceiver
{
	private static final String TAG = CesGeofenceReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Util util = ((App)context.getApplicationContext()).getGlobalComponent().util();
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if( ! geofencingEvent.hasError())
		{
			int transition = geofencingEvent.getGeofenceTransition();
			List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
			switch(transition)
			{
			case Geofence.GEOFENCE_TRANSITION_ENTER:
Log.w(TAG, "CesGeofenceReceiver:onReceive:-------------------------------------GEOFENCE_TRANSITION_ENTER");
				for(Geofence geof : geofences)
				{
					util.showAvisoGeo(geof.getRequestId());
Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
Log.w(TAG, "CesGeofenceReceiver:onReceive:--------------------------------------GEOFENCE_TRANSITION_DWELL");
				for(Geofence geof : geofences)
				{
					util.showAvisoGeo(geof.getRequestId());
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
Log.w(TAG, "CesGeofenceReceiver:onReceive:---------------------------------------GEOFENCE_TRANSITION_EXIT");
				for(Geofence geof : geofences)
				{
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
				}
				break;
			default:
Log.w(TAG, "CesGeofenceReceiver:onReceive:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}


}