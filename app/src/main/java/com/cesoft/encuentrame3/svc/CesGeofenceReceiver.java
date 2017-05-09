package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Aviso;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


//http://stackoverflow.com/questions/21414160/how-to-increase-consistency-of-android-geofence-enter-exit-notifications
//http://stackoverflow.com/questions/19505614/android-geofence-eventually-stop-getting-transition-intents/19521823#19521823
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/03/2016.
//En lugar de CesServiceAvisoGeo se utiliza este BroadcastReceiver porque dicen es mas fiable
public class CesGeofenceReceiver extends BroadcastReceiver
{
	private static final String TAG = CesGeofenceReceiver.class.getSimpleName();
	private Util _util;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		_util = ((App)context.getApplicationContext()).getGlobalComponent().util();
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
					showAviso(context, geof.getRequestId(), context.getString(R.string.en_zona_aviso));
Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
Log.w(TAG, "CesGeofenceReceiver:onReceive:--------------------------------------GEOFENCE_TRANSITION_DWELL");
				for(Geofence geof : geofences)
				{
					showAviso(context, geof.getRequestId(), context.getString(R.string.en_zona_aviso));
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
Log.w(TAG, "CesGeofenceReceiver:onReceive:---------------------------------------GEOFENCE_TRANSITION_EXIT");
				for(Geofence geof : geofences)
				{
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
				}
				//for(Geofence geof : geofences)addTrackingPoint(geof);
				break;
			default:
Log.w(TAG, "CesGeofenceReceiver:onReceive:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	protected void showAviso(final Context context, String sId, final String sTitle)
	{
		Aviso.getById(sId, new Fire.SimpleListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aData)
			{
				Intent i = new Intent(context, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, aData[0]);
				i.putExtra("notificacion", true);
				_util.showAviso(sTitle, aData[0], i);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "showAviso:e:------------------------------------------------------------" + err);
			}
		});
	}

}