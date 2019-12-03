package com.cesoft.encuentrame3.svc;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Log;

import static com.cesoft.encuentrame3.util.Constantes.DELAY_LOAD_GEOFENCE;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class GeofencingService extends Service {
    private static final String TAG = GeofencingService.class.getSimpleName();
    private static final int ID_SERVICE = 6970;

    private static boolean isOnOff = true;
    public static void turnOnOff(Context context, boolean v) {
        isOnOff = v;
        if(isOnOff)
            GeofencingService.start(context);
        else
            GeofencingService.stop(context);
    }
    public static boolean isOnOff() { return isOnOff; }

    private static boolean isStarted = false;
    public static void start(Context context) {
        if(isStarted) return;
        Intent intentService = new Intent(context, GeofencingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
        isStarted = true;
    }
    public static synchronized void stop(Context context) {
        Intent intentService = new Intent(context, GeofencingService.class);
        context.stopService(intentService);
        Log.e(TAG, "stop-----------------------------------------------------------------------------------");
    }

    private boolean isRunning = true;
    private Thread thread = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = ServiceNotifications.createForGeofencing(this);
        startForeground(ID_SERVICE, notification);

        Log.e(TAG, "onStartCommand:--------------------------------  startId="+startId+"  :  flags="+flags+"  :  intent="+intent);
        if(thread != null && thread.isAlive()) {
            Log.e(TAG, "onStartCommand:--------------------------------  Killing old thread "+thread.hashCode());
            thread.interrupt();
        }

        thread = new Thread() {
            @Override
            public void run() {
                Login login = App.getComponent().login();
                CesGeofenceStore geofenceStoreAvisos = App.getComponent().geofence();
                isRunning = true;
                while(isRunning) {
                    Log.e(TAG, "onStartCommand:Thread:-------------------------------- RUN ");

                    if( ! login.isLogged()) {
                        Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
                        stopSelf();
                    }
                    else {
                        geofenceStoreAvisos.cargarListaGeoAvisos();//PAYLOAD
                    }

                    try { Thread.sleep(DELAY_LOAD_GEOFENCE); }
                    catch(InterruptedException e) {
                        //Log.e(TAG, "onStartCommand:Thread:InterruptedException--------------------------------",e);
                        isRunning = false;
                    }
                }
            }
        };
        thread.start();
        Log.e(TAG, "onStartCommand:--------------------------------  Starting new thread "+thread.hashCode());
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
        Log.e(TAG, "onDestroy:-----------------------------------------------------------------");
    }
}
