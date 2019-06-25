package com.cesoft.encuentrame3.svc;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.ACCEL_MAX;
import static com.cesoft.encuentrame3.util.Constantes.ACCURACY_MAX;
import static com.cesoft.encuentrame3.util.Constantes.DELAY_TRACK_MIN;
import static com.cesoft.encuentrame3.util.Constantes.DISTANCE_MIN;
import static com.cesoft.encuentrame3.util.Constantes.ID_JOB_TRACKING;
import static com.cesoft.encuentrame3.util.Constantes.SPEED_MAX;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
@Singleton
public class GeoTrackingJobService
        extends JobService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GeoTrackingJobService.class.getSimpleName();

    @Inject Util util;
    @Inject Login login;
    @Inject Preferencias pref;
    @Inject
    public GeoTrackingJobService() {
        EventBus.getDefault().register(this);
    }

    private Context appContext;
    private FusedLocationProviderClient fusedLocationClient;
    //private PowerManager.WakeLock _wakeLock = null;
    private JobParameters jobParameters;
    private GoogleApiClient googleApiClient;

    private int lastActividad = DetectedActivity.STILL;//Actividad actual: Coche, bici, parado...
    private String sId = "";                //Ruta actual
    private Location locLastSaved = null;   //Para calculo de distancia minima
    private Location locLast = null;        //Para calculo de punto erroneo
    private double velLast = 0;             //Para calculo de punto erroneo

    private long delay = DELAY_TRACK_MIN;
    private boolean saveAll = true;

    public static void start(Context context, long delay) {
        start(context, delay,true);
    }
    private static void start(Context context, long delay, boolean first) {
        Log.e(TAG, "************************* TRACKING Start *************************");

        ComponentName componentName = new ComponentName(context, GeoTrackingJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ID_JOB_TRACKING, componentName);
        builder.setPersisted(true);

        //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setMinimumLatency(first ? 1000 : delay);//La primera vez espera solo 1s
            builder.setOverrideDeadline(delay+10);
        }
        else {
            builder.setPeriodic(delay);
        }

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null)
            jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* GEO onStartJob *************************");
        keepAwake();
        runPayload(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* GEO onStopJob *************************");
        dontKeepAwake();
        return false;
    }

    private PowerManager.WakeLock wakeLock;
    private void keepAwake() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CESoft::GeoTrackingJobService2h");
        wakeLock.acquire(2*60*60*1000);
    }
    private void dontKeepAwake() {
        wakeLock.release();
    }

    //---

    private void runPayload(JobParameters jobParameters) {
        this.jobParameters = jobParameters;
        //WidgetRutaService.startSvc(getApplicationContext());//Init widget serv if it isn't
        iniEnviron();
        if (!login.isLogged()) {
            Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
            finish();
        } else if (util.getTrackingRoute().isEmpty()) {
            Log.e(TAG, "getTrackingRoute().isEmpty() !! STOPPING JOB " + sId);
            notTracking();
        } else {
            //iniWakeLock();
            iniTracking();
            new Thread(this::saveGeoTracking).start();
        }
    }

    /*private void iniWakeLock() {
        //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)//TODO: Is wakelock needed? DELETE?
       PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if(powerManager != null) {
            _wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CESoft::GeoTrackingJobService");
            _wakeLock.acquire(5*60*1000);//delay);//x milliseconds max
            Log.e(TAG, "iniWakeLock--------------------------------------------");
        }
    }

    private void endWakeLock() {
        if (_wakeLock != null)
            _wakeLock.release();
        _wakeLock = null;
        Log.e(TAG, "endWakeLock--------------------------------------------");
    }*/

    private void iniEnviron() {
        appContext = getApplicationContext();
        App.getComponent(appContext).inject(this);
        saveAll = pref.isSaveAllPoints();
        delay = pref.getTrackingDelay();
        Log.e(TAG, "iniEnviron-------------------------------------"+ saveAll +" : "+ delay);
    }

    private void reschedule() {
        //endWakeLock();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            start(getApplicationContext(), delay, false);
        jobFinished(jobParameters, true);
    }

    private void finish() {
        stopSelf();
        //endWakeLock();
        jobFinished(jobParameters, false);
    }

    private void notTracking() {
        stopTracking();
        finish();
    }


    //---


    private void stopTracking() {
        Log.e(TAG, "stopTracking:-----------------------------------------------------------");
        if (googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();
        if(fusedLocationClient != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
        fusedLocationClient = null;
        locLastSaved = null;
        locLast = null;
        velLast = 0;
    }

    private void iniTracking() {
        Log.e(TAG, "iniGeoTracking:------------------------1-----------------------------------");
        if (checkPlayServices()) buildGoogleApiClient();
        if (googleApiClient != null) googleApiClient.connect();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(delay);
        locationRequest.setFastestInterval(delay -500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "pideGPS:---------------------------------------------------------------");
            util.showNotifGPS();
        }
        else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        Log.e(TAG, "iniGeoTracking:------------------------2-----------------------------------"+ fusedLocationClient);
    }

    private synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private boolean checkPlayServices()
    {
        try {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(appContext);
            if (result != ConnectionResult.SUCCESS) {
                //TODO: Notificacion?
                Log.e(TAG, String.format("checkPlayServices: No tiene Google Services? result = %s !!!!!!!!!!!!!!!", result));
                return false;
            }
            return true;
        }
        catch(Exception e) {
            Log.e(TAG, "checkPlayServices:e:---------------------------------------------------", e);
            return false;
        }
    }


    //----------------------------------------------------------------------------------------------
    public void saveGeoTracking()
    {
        sId = util.getTrackingRoute();
        Log.e(TAG, "saveGeoTracking ************************************** "+ this.sId);
        if(sId.isEmpty())
        {
            Log.e(TAG, "saveGeoTracking ******************sId IS EMPTY***************");
            finish();
            return;
        }

        Ruta.getById(sId, new Fire.SimpleListener<Ruta>() {
            @Override
            public void onDatos(Ruta[] aData) {
                if(aData[0] == null) {
                    Log.e(TAG, "saveGeoTracking:Ruta.getById: RUTA == NULL --------------------"+sId);
                    util.setTrackingRoute("");
                    finish();
                }
                else {
                    final Location loc = util.getLocation();
                    if(guardarPunto(loc, aData[0]))
                        Log.e(TAG, "saveGeoTracking: guardarPunto OK --------------------------");
                    reschedule();
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
            return true;
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
            if(locLast != null)
            {
                try
                {
                    float distLast = loc.distanceTo(locLast);
                    long t0 = locLast.getTime();
                    long t1 = loc.getTime();

                    double time = (t1-t0)/1000.0;//s
                    double speed = distLast / time;	//60 m/s = 216 Km/h
                    double accel = (speed - velLast)/time;//Aceleración coche muy potente (desde parado!) < 8 m/s2

                    Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:-------*************----TIME(s)= %.0f  VEL(m/s)= %.2f  LAST VEL=%.2f  A(m/s2)= %.2f", time, speed, velLast, accel));
                    if(speed > SPEED_MAX || accel > ACCEL_MAX)//imaginamos que es un punto erróneo, salvo que vayas en un cohete
                    {
                        Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto erróneo:   VEL=%.2f m/s  LAST VEL=%.2f  T=%.0f  a=%.2f ***************************** !!!", speed, velLast, time, accel));
                        return false;
                    }
                    velLast = speed;
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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class GuardarListener extends Fire.CompletadoListener
    {
        private Location loc;
        GuardarListener(Location loc) { this.loc = loc; }

        @Override
        protected void onDatos(String id) {
            Log.e(TAG, "GuardarListener:onComplete:----------------------:" + id);
            Ruta.addPunto(id, loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(),
                    loc.getAltitude(), loc.getSpeed(), loc.getBearing(), lastActividad,
                    new Fire.SimpleListener<Long>() {
                        @Override
                        public void onDatos(Long[] puntos) {
                            Log.e(TAG, String.format(Locale.ENGLISH, "GuardarListener:addPunto: n ptos: %d", puntos[0]));
                            util.refreshListaRutas();//Refrescar lista rutas en main..
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
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected----------------------------------------------------------------");
    }
    @Override
    public void onConnectionSuspended(int i) {
        if(googleApiClient != null)
            googleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, String.format("**********************onConnectionFailed:e: %s", connectionResult.getErrorCode()));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
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
        lastActividad = act.getType();
    }

}
