package com.cesoft.encuentrame3.svc;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Login;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova
public class GeofencingService extends Service {
    private static final String TAG = GeofencingService.class.getSimpleName();
    private static final int ID_SERVICE = 6970;

    private static boolean isOn = true;
    public static void turnOn() {
        GeofencingService.start();
    }
    public static void turnOff() {
        GeofencingService.stop();
    }
    public static boolean isOn() { return isOn; }

    private static boolean isStarted = false;
    public static void start() {
        if(isStarted) return;
        Context appContext = App.getInstance();
        Intent intent = new Intent(appContext, GeofencingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent);
        } else {
            appContext.startService(intent);
        }
        isStarted = true;
    }
    public static synchronized void stop() {
        Context appContext = App.getInstance();
        Intent intentService = new Intent(appContext, GeofencingService.class);
        appContext.stopService(intentService);
    }

    private Thread thread = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceNotifications sn = App.getComponent().serviceNotifications();
        Notification notification = sn.createForGeofencing();
        startForeground(ID_SERVICE, notification);

        Log.e(TAG, "onStartCommand:--------------------------------  startId="+startId+"  :  flags="+flags+"  :  intent="+intent);
        if(thread != null && thread.isAlive()) {
            Log.e(TAG, "onStartCommand:--------------------------------  Killing old thread "+thread.hashCode());
            thread.interrupt();
        }
        isOn = true;

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    Login login = App.getComponent().login();
                    GeofenceStore geofenceStoreAvisos = App.getComponent().geofence();
                    while(true) {
                        Log.e(TAG, "onStartCommand:Thread:-------------------------------- RUN ");

                        if( ! login.isLogged()) {
                            Log.e(TAG, "No hay usuario logado !! STOPPING SERVICE");
                            Thread.currentThread().interrupt();
                            stopSelf();
                        }
                        else {
                            geofenceStoreAvisos.cargarListaGeoAvisos();//PAYLOAD
                        }

                        Thread.sleep(Constantes.GEOFENCE_LOAD_DELAY);
                    }
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
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
        isOn = false;
        GeofenceStore geofenceStoreAvisos = App.getComponent().geofence();
        geofenceStoreAvisos.clear();
        Log.e(TAG, "onDestroy:-----------------------------------------------------------------");
    }
}
