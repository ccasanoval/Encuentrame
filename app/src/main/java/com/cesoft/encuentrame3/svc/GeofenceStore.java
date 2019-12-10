package com.cesoft.encuentrame3.svc;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import javax.inject.Inject;
import javax.inject.Singleton;


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
@Singleton
public class GeofenceStore
{
	private static final String TAG = GeofenceStore.class.getSimpleName();

	private PendingIntent pendingIntent;
	private GeofencingClient geofencingClient;
	private Context context;
	private ArrayList<Geofence> geofenceList = new ArrayList<>();

	//----------------------------------------------------------------------------------------------
	@Inject
	GeofenceStore(Context context)
	{
		try
		{
			this.context = context;
			geofencingClient = LocationServices.getGeofencingClient(context);
		}
		catch(Exception e)
		{
			Log.e(TAG, "GeofenceStore:e:------------------------------------------------------", e);
		}
	}

	private void update(ArrayList<Geofence> geofences)
	{
		if( ! geofenceList.isEmpty()) {
			geofencingClient.removeGeofences(pendingIntent);
		}
		createRequestPendingIntent();

		if(geofences.isEmpty()) {
			geofenceList = new ArrayList<>();
			return;
		}
		geofenceList = new ArrayList<>(geofences);

		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
		builder.addGeofences(geofenceList);
		GeofencingRequest geofencingRequest = builder.build();

		geofencingClient.addGeofences(geofencingRequest, pendingIntent)
				.addOnSuccessListener(aVoid -> Log.e(TAG, "update:---------A-------------------ADDED"))
				.addOnFailureListener(e -> Log.e(TAG, "update:-------------A---------------ERROR ADDING : ",e));
	}

	private void clear()
	{
		if(pendingIntent != null)
			geofencingClient.removeGeofences(pendingIntent);
	}

	// This creates a PendingIntent that is to be fired when geofence transitions take place.
	private void createRequestPendingIntent()
	{
		try
		{
			if(pendingIntent == null) {
				Intent intent = new Intent(context, GeofenceReceiver.class);
				pendingIntent = PendingIntent.getBroadcast(
						context,
						GeofenceReceiver.REQUEST_CODE,
						intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, "createRequestPendingIntent:e:-----------------------------------------",e);
		}
	}


	//----------------------------------------- AVISOS EN FIREBASE -----------------------------------------

	public void cargarListaGeoAvisos()
	{
		Log.e(TAG, "*************************---- cargarListaGeoAvisos ----*************************");
		createListAviso();
		try { Aviso.getActivos(lisAviso); }
		catch(Exception e) { Log.e(TAG, "cargarListaGeoAvisos:e:------------------------------", e); }
	}

	private boolean isInit = false;
	private Fire.DatosListener<Aviso> lisAviso;
	private ArrayList<Aviso> listaGeoAvisos = new ArrayList<>();
	private void createListAviso()
	{
		if(isInit)return;
		isInit = true;
		lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aData)
			{
				//TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
				processDatos(aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------"+err);
			}
		};
	}
    private void processDatos(Aviso[] aData) {
        boolean bDirty = false;
        long n = aData.length;

        if(n != listaGeoAvisos.size())
        {
            if( ! geofenceList.isEmpty()) clear();
            listaGeoAvisos.clear();
            bDirty = true;
        }

        ArrayList<Geofence> geofenceList2 = new ArrayList<>();
        ArrayList<Aviso> aAvisos = new ArrayList<>();
        for(int i=0; i < aData.length; i++)
        {
            Aviso a = aData[i];
            if( ! a.isActivo()) continue;

            aAvisos.add(a);

            Geofence gf = new Geofence.Builder()
                    .setRequestId(a.getId())
                    .setCircularRegion(a.getLatitud(), a.getLongitud(), (float)a.getRadio())
					.setExpirationDuration(Constantes.GEOFENCE_EXPIRE_DELAY)//Geofence.NEVER_EXPIRE
					.setNotificationResponsiveness(Constantes.GEOFENCE_RESPONSE_DELAY)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)// | Geofence.GEOFENCE_TRANSITION_DWELL)
					//.setLoiteringDelay(GEOFENCE_DWELL_DELAY)// Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
                    .build();
            geofenceList2.add(gf);
            if( ! bDirty && (listaGeoAvisos.size() < i || ! listaGeoAvisos.contains(a)))
            {
                bDirty = true;
            }
        }
        if(bDirty)
        {
            listaGeoAvisos = aAvisos;
            update(geofenceList2);//Se puede añadir en lugar de crear desde cero?
        }
        if(geofenceList.isEmpty())
            GeofencingService.stop(context);
        else if(GeofencingService.isOnOff())
            GeofencingService.start(context);
    }
}