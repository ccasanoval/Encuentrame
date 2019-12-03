package com.cesoft.encuentrame3.svc;


import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import static com.cesoft.encuentrame3.util.Constantes.ACCEL_MAX;
import static com.cesoft.encuentrame3.util.Constantes.ACCURACY_MAX;
import static com.cesoft.encuentrame3.util.Constantes.DELAY_TRACK_MIN;
import static com.cesoft.encuentrame3.util.Constantes.DISTANCE_MIN;
import static com.cesoft.encuentrame3.util.Constantes.SPEED_MAX;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class GeotrackingService extends Service {
    private static final String TAG = GeotrackingService.class.getSimpleName();
    private static final int ID_SERVICE = 6969;

    private static boolean isStarted = false;
    public static synchronized void start(Context context, long delay) {
Log.e(TAG, "start-----------------------------------------------------------------------------------isStarted="+isStarted+" delay="+delay);
        if(isStarted) return;
        if(App.getComponent().util().getIdTrackingRoute().isEmpty()) {
            Log.e(TAG, "No hay ruta activa!! STOPPING SERVICE");
            return;
        }
        if( ! App.getComponent().login().isLogged()) {
            Log.e(TAG, "No hay usuario logado!! STOPPING SERVICE");
            return;
        }

        GeotrackingService.delay = delay;
        Intent intentService = new Intent(context, GeotrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "start-----------------------------------------------------------------------------------");
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
        isStarted = true;
    }
    public static synchronized void stop(Context context) {
        App.getComponent().util().setTrackingRoute("", "");
        Intent intentService = new Intent(context, GeotrackingService.class);
        context.stopService(intentService);
        Log.e(TAG, "stop-----------------------------------------------------------------------------------");
    }

    private static long delay = DELAY_TRACK_MIN;

    private int lastDetectedActivity = DetectedActivity.STILL;

    private Util util;
    private Login login;
    private Preferencias pref;

    private FusedLocationProviderClient fusedLocationClient;

    private Thread thread = null;
    private String sId = "";                //Ruta actual
    private Location locLastSaved = null;   //Para calculo de distancia minima
    private Location locLast = null;        //Para calculo de punto erroneo
    private double velLast = 0;             //Para calculo de punto erroneo

    private boolean saveAll = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = ServiceNotifications.createForGeotracking(this, util.getNameTrackingRoute());
        startForeground(ID_SERVICE, notification);

        Log.e(TAG, "onStartCommand:--------------------------------  startId="+startId+"  :  flags="+flags+"  :  intent="+intent);
        if(thread != null && thread.isAlive()) {
            Log.e(TAG, "onStartCommand:--------------------------------  Killing old thread "+thread.hashCode());
            thread.interrupt();
        }

        iniEnviron();
        iniTracking();

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Log.e(TAG, "onStartCommand:Thread:-------------------------------- RUN ");
                        keepAwake();
                        runPayload();
                        Thread.sleep(DELAY_TRACK_MIN / 2);
                    }
                }
                catch(Exception e) {
                    Log.e(TAG, "onStartCommand:Thread:InterruptedException--------------------------------");
                }
            }
        };
        thread.start();
        Log.e(TAG, "onStartCommand:--------------------------------  Starting new thread "+thread.hashCode());
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        util = App.getComponent().util();
        login = App.getComponent().login();
        pref = App.getComponent().pref();
        EventBus.getDefault().register(this);
        Log.e(TAG, "onCreate:--------------------------------****************************");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
        dontKeepAwake();
        EventBus.getDefault().unregister(this);
        Log.e(TAG, "onDestroy:--------------------------------****************************");
    }
    private PowerManager.WakeLock wakeLock;
    private void keepAwake() {
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if(powerManager != null) {
            String lockName = getPackageName()+"::"+GeotrackingService.class.getSimpleName();
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);
            wakeLock.acquire(DELAY_TRACK_MIN*5);
        }
    }
    private void dontKeepAwake() {
        wakeLock.release();
    }


    private void runPayload() {
        if (!login.isLogged()) {
            Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
            finish();
        } else if (util.getIdTrackingRoute().isEmpty()) {
            Log.e(TAG, "getTrackingRoute().isEmpty() !! STOPPING JOB " + sId);
            notTracking();
        } else {
            saveGeoTracking();
        }
    }

    private void iniEnviron() {
        saveAll = pref.isSaveAllPoints();
        delay = pref.getTrackingDelay();
        //Log.e(TAG, "iniEnviron-------------------------------------"+ saveAll +" : "+ delay);
    }

    private void finish() {
Log.e(TAG, "finish---------------------------------------------------------------------------");
        thread.interrupt();
        stopSelf();
    }

    private void notTracking() {
Log.e(TAG, "notTracking---------------------------------------------------------------------------");
        stopTracking();
        finish();
    }

    //---


    private void stopTracking() {
Log.e(TAG, "stopTracking:-----------------------------------------------------------");
//        if (googleApiClient != null && googleApiClient.isConnected())
//            googleApiClient.disconnect();
        if(fusedLocationClient != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
        fusedLocationClient = null;
        locLastSaved = null;
        locLast = null;
        velLast = 0;
    }

    private void iniTracking() {
Log.e(TAG, "iniTracking:-----------------------------------------------------------");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(delay);
        locationRequest.setFastestInterval(delay -500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }


    //----------------------------------------------------------------------------------------------
    public void saveGeoTracking()
    {
        sId = util.getIdTrackingRoute();
        if(sId.isEmpty())
        {
            finish();
            return;
        }

        Ruta.getById(sId, new Fire.SimpleListener<Ruta>() {
            @Override
            public void onDatos(Ruta[] aData) {
                if(aData[0] == null) {
                    util.setTrackingRoute("","");
                    finish();
                }
                else {
                    final Location loc = util.getLocation();
                    if(guardarPunto(loc, aData[0]))
                        Log.e(TAG, "saveGeoTracking: guardarPunto OK --------------------------");
                }
            }
            @Override
            public void onError(String err) {
                Log.e(TAG, "saveGeoTracking:findById:e:----------------------------------------:" + err);
            }
        });
    }

    private boolean isWrongPoint(Location loc, Ruta ruta) {
        if(loc == null)
        {
            Log.e(TAG, "guardarPunto:loc==NULL------------------------------------------------------");
            return true;
        }
        if( ! loc.hasAccuracy())
        {
            Log.e(TAG, "guardarPunto:loc.hasAccuracy()==FALSE---------------------------------------");
            return true;
        }
        if( ! saveAll && ruta.getPuntosCount() > 0 && (loc.getAccuracy() > ACCURACY_MAX || loc.getAccuracy() > 2*DISTANCE_MIN)) {
            Log.e(TAG, "guardarPunto:loc.getAccuracy()("+loc.getAccuracy()+")   > MAX_ACCURACY("+ ACCURACY_MAX+")  or  > DISTANCE_MIN*2("+2*DISTANCE_MIN+")    :::: n pts "+ruta.getPuntosCount());
//            return true;//TODO:TEST-----------------------
        }
        return false;
    }
    private synchronized boolean guardarPunto(Location loc, Ruta ruta)
    {
        Log.e(TAG, "guardarPunto:    A    ********************* \nLOC:"+loc+"\n : RUTA_ID:"+ruta.id+" <> OLD_RUTA_ID:"+ sId);
        if(isWrongPoint(loc, ruta))
            return false;

        if( ! sId.equals(ruta.id))
        {
            Log.e(TAG, "guardarPunto: NUEVA RUTA: --------------------------------- "+ sId +" ------- "+ruta.id);
            sId = ruta.id;
            locLastSaved = null;
            locLast = loc;
        }
        else if(!saveAll && locLastSaved != null)
        {
            //---
            //Determinar probabilidad de punto erroneo (velocidad actual, velocidad pasada, distancia al anterior punto, tiempo transcurrido)
            //TODO: isWrongPoint deberia hacerse cargo de esto no?!!!!!!!!!!!!!
            if(locLast != null)
            {
                try
                {
                    float distLast = loc.distanceTo(locLast);
                    long t0 = locLast.getTime();
                    long t1 = loc.getTime();

                    double time = (t1-t0)/1000.0;//s
                    if(time > 0) {
                        double speed = distLast / time;    //60 m/s = 216 Km/h
                        double accel = (speed - velLast) / time;//Aceleración coche muy potente (desde parado!) < 8 m/s2

                        Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:-------*************----TIME(s)= %.0f  VEL(m/s)= %.2f  LAST VEL=%.2f  A(m/s2)= %.2f", time, speed, velLast, accel));
                        if (speed > SPEED_MAX || accel > ACCEL_MAX)//imaginamos que es un punto erróneo, salvo que vayas en un cohete
                        {
                            //Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto erróneo:   VEL=%.2f m/s  LAST VEL=%.2f  T=%.0f  a=%.2f ***************************** !!!", speed, velLast, time, accel));
                            return false;
                        }
                        velLast = speed;
                    }
                }
                catch(Exception e){Log.e(TAG, "guardarPunto:e:-----------------------------------------",e);}
            }
            //---

            float distLastSaved = loc.distanceTo(locLastSaved);
            Log.e(TAG, "guardarPunto:-------********---last="+ locLastSaved
                    +"\n---newLoc="+loc
                    +"\n----distLastSaved="+distLastSaved
                    +"\n-----acc="+loc.getAccuracy());
            //Puntos muy cercanos
            if(distLastSaved < DISTANCE_MIN) return false;
            locLast = loc;
        }
        ruta.guardar(new GuardarListener(loc));
        locLastSaved = loc;
        return true;
    }


    private class GuardarListener extends Fire.CompletadoListener
    {
        private Location loc;
        GuardarListener(Location loc) { this.loc = loc; }

        @Override
        protected void onDatos(String id) {
            Log.e(TAG, "GuardarListener:onComplete:----------------------:" + id);
            //Actividad actual: Coche, bici, parado...
            int lastActividad = DetectedActivity.STILL;
            Ruta.addPunto(id, loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(),
                    loc.getAltitude(), loc.getSpeed(), loc.getBearing(), lastActividad,
                    new Fire.SimpleListener<Long>() {
                        @Override
                        public void onDatos(Long[] puntos) {
                            Log.e(TAG, String.format(Locale.ENGLISH, "GuardarListener:addPunto: n ptos: %d", puntos[0]));
                            util.refreshListaRutas();//Refrescar lista rutas en main..
                            ServiceNotifications.createForGeotracking(GeotrackingService.this, util.getNameTrackingRoute()+" "+puntos[0]+" pts");
                        }
                        @Override
                        public void onError(String err) {
                            Log.e(TAG, String.format("GuardarListener:addPunto:e:--------------------------------------:%s",err));
                        }
                    });
        }
        @Override
        protected void onError(String err, int code) {
            Log.e(TAG, String.format(Locale.ENGLISH, "saveGeoTracking:guardar:err:-------------------------:%s : %d",err, code));
        }
    }
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if( ! locationList.isEmpty()) {
                Location location = locationList.get(locationList.size() - 1);
                util.setLocation(location);
            }
        }
    };


    @Subscribe(threadMode = ThreadMode.POSTING)//BACKGROUND)
    public void onActividadEvent(ActividadIntentService.ActividadEvent event)
    {
        DetectedActivity act = event.getActividad();
Log.e(TAG, "onActividadEvent:--------------------------------  lastDetectedActivity = "+lastDetectedActivity +" and now = "+act.getType());
        lastDetectedActivity = act.getType();
    }
}
