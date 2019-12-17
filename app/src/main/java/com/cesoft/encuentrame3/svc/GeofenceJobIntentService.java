package com.cesoft.encuentrame3.svc;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/12/2019.
//
public class GeofenceJobIntentService extends JobIntentService {

    private static final int JOB_ID = 573;
    private static final String TAG = "GeofenceIntentServ";

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceJobIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if( ! geofencingEvent.hasError())
        {
            Util util = ((App)getApplicationContext()).getGlobalComponent().util();
            int transition = geofencingEvent.getGeofenceTransition();
            List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
            switch(transition)
            {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.e(TAG, "GeofenceReceiver:onReceive:-------------------------------------GEOFENCE_TRANSITION_ENTER");
                    for(Geofence geof : geofences)
                    {
                        util.showAvisoGeo(geof.getRequestId());
                        Log.e(TAG, "GeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
                    }
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    Log.e(TAG, "GeofenceReceiver:onReceive:--------------------------------------GEOFENCE_TRANSITION_DWELL");
                    for(Geofence geof : geofences)
                    {
                        util.showAvisoGeo(geof.getRequestId());
                        Log.e(TAG, "GeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
                    }
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Log.e(TAG, "GeofenceReceiver:onReceive:---------------------------------------GEOFENCE_TRANSITION_EXIT");
                    for(Geofence geof : geofences)
                    {
                        Log.e(TAG, "GeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
                    }
                    break;
                default:
                    Log.e(TAG, "GeofenceReceiver:onReceive:e: Unknown Geofence Transition -----------------------------");
                    break;
            }
        }
    }
}
