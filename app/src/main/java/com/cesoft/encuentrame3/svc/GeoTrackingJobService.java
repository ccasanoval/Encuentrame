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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;
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

@Singleton
public class GeoTrackingJobService
        extends JobService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GeoTrackingJobService.class.getSimpleName();


    @Inject Util _util;
    @Inject Login _login;
    @Inject Preferencias _pref;
    @Inject
    public GeoTrackingJobService() {
        EventBus.getDefault().register(this);
    }

    private Context _appContext;
    private FusedLocationProviderClient _fusedLocationClient;
    private PowerManager.WakeLock _wakeLock = null;
    private JobParameters _jobParameters;
    private GoogleApiClient _GoogleApiClient;

    private static int _lastActividad = DetectedActivity.STILL;//Actividad actual: Coche, bici, parado...
    private static String _sId = "";                //Ruta actual
    private static Location _locLastSaved = null;   //Para calculo de distancia minima
    private static Location _locLast = null;        //Para calculo de punto erroneo
    private static double _velLast = 0;             //Para calculo de punto erroneo

    private static long _delay = DELAY_TRACK_MIN;
    private boolean _saveAll = true;

    public static void start(Context context, long delay) {
        start(context, delay,true);
    }
    private static void start(Context context, long delay, boolean first) {
        Log.e(TAG, "************************* TRACKING Start *************************");

        ComponentName componentName = new ComponentName(context, GeoTrackingJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ID_JOB_TRACKING, componentName).setPersisted(true);

        //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(first ? 1000 : delay);//La primera vez espera solo 1s
        else
            builder.setPeriodic(delay);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null)
            jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* GEO onStartJob *************************");
        runPayload(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* GEO onStopJob *************************");
        return false;
    }

    //---

    private void runPayload(JobParameters jobParameters) {
        _jobParameters = jobParameters;
        WidgetRutaService.startSvc(getApplicationContext());//Init widget serv if it isn't
        iniEnviron();
        if (!_login.isLogged()) {
            Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
            finish();
        } else if (_util.getTrackingRoute().isEmpty()) {
            Log.e(TAG, "getTrackingRoute().isEmpty() !! STOPPING JOB " + _sId);
            notTracking();
        } else {
            iniWakeLock();
            iniTracking();
            new Thread(this::saveGeoTracking).start();
        }
    }

    private void iniWakeLock() {
        //if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)//TODO: When would wakelock be needed? DELETE
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if(powerManager != null) {
            _wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CESoft::GeoTrackingJobService");
            _wakeLock.acquire(5*60*1000);//_delay);//x milliseconds max
            Log.e(TAG, "iniWakeLock--------------------------------------------");
        }
    }

    private void endWakeLock() {
        if (_wakeLock != null)
            _wakeLock.release();
        _wakeLock = null;
        Log.e(TAG, "endWakeLock--------------------------------------------");
    }

    private void iniEnviron() {
        _appContext = getApplicationContext();
        App.getComponent(_appContext).inject(this);
        _saveAll = _pref.isSaveAllPoints();
        _delay = _pref.getTrackingDelay();
        Log.e(TAG, "iniEnviron-------------------------------------"+_saveAll+" : "+_delay);
    }

    private void reschedule() {
        //endWakeLock();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            start(getApplicationContext(), _delay, false);
        jobFinished(_jobParameters, true);
    }

    private void finish() {
        stopSelf();
        endWakeLock();
        jobFinished(_jobParameters, false);
    }

    private void notTracking() {
        stopTracking();
        finish();
    }


    //---


    private void stopTracking() {
        Log.e(TAG, "stopTracking:-----------------------------------------------------------");
        if (_GoogleApiClient != null && _GoogleApiClient.isConnected())
            _GoogleApiClient.disconnect();
        if(_fusedLocationClient != null)
            _fusedLocationClient.removeLocationUpdates(_locationCallback);
        _fusedLocationClient = null;
        _locLastSaved = null;
        _locLast = null;
        _velLast = 0;
    }

    private void iniTracking() {
        Log.e(TAG, "iniGeoTracking:------------------------1-----------------------------------");
        if (checkPlayServices()) buildGoogleApiClient();
        if (_GoogleApiClient != null) _GoogleApiClient.connect();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(_delay-100);//TODO: ajustar segun velocidad cambio pos...
        locationRequest.setFastestInterval(_delay-100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "pideGPS:---------------------------------------------------------------");
            _util.showNotifGPS();
        }
        else {
            _fusedLocationClient.requestLocationUpdates(locationRequest, _locationCallback, Looper.myLooper());
        }
        Log.e(TAG, "iniGeoTracking:------------------------2-----------------------------------"+_fusedLocationClient);
    }

    private synchronized void buildGoogleApiClient()
    {
        _GoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private boolean checkPlayServices()
    {
        try {
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(_appContext);
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
        final String sId = _util.getTrackingRoute();
        Log.e(TAG, "saveGeoTracking ************************************** "+_sId+" / "+sId);
        if(sId.isEmpty())
        {
            Log.e(TAG, "saveGeoTracking ******************sId.isEmpty() IS EMPTY***************");
            finish();
            return;
        }

        Ruta.getById(sId, new Fire.SimpleListener<Ruta>() {
            @Override
            public void onDatos(Ruta[] aData) {
                if(aData[0] == null) {
                    Log.e(TAG, "saveGeoTracking:Ruta.getById: RUTA == NULL --------------------"+sId);
                    _util.setTrackingRoute("");
                    finish();
                }
                else {
                    final Location loc = _util.getLocation();
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

    private synchronized boolean guardarPunto(Location loc, Ruta r)
    {
        Log.e(TAG, "guardarPunto:    A    ********************* \nLOC:"+loc+"\n : RUT:"+r.id+"\n :  _ID:"+_sId);
        if(loc == null)
        {
            Log.e(TAG, "guardarPunto:loc==NULL------------------------------------------------------");
            //if(_saveAll)loc = new Location("FAKE");//?
            //else
                return false;
        }
        if( ! loc.hasAccuracy())//!_saveAll&&
        {
            Log.e(TAG, "guardarPunto:loc.hasAccuracy()==FALSE---------------------------------------");
            return false;
        }
        if(!_saveAll&& r.getPuntosCount() > 0 && (loc.getAccuracy() > ACCURACY_MAX || loc.getAccuracy() > 2*DISTANCE_MIN)) {
            Log.e(TAG, "guardarPunto:loc.getAccuracy()("+loc.getAccuracy()+")   > MAX_ACCURACY("+ ACCURACY_MAX+")  or  > DISTANCE_MIN*2("+2*DISTANCE_MIN+")    :::: n pts "+r.getPuntosCount());
            return false;
        }

        if( ! _sId.equals(r.id))
        {
            Log.e(TAG, "guardarPunto: NUEVA RUTA: --------------------------------- "+_sId+" ------- "+r.id);
            _sId = r.id;
            _locLastSaved = null;
            _locLast = loc;
        }
        else if(!_saveAll&& _locLastSaved != null)
        {
            //---
            //Determinar probabilidad de punto erroneo (velocidad actual, velocidad pasada, distancia al anterior punto, tiempo transcurrido)
            if(_locLast != null)
            {
                try
                {
                    float distLast = loc.distanceTo(_locLast);
                    long t0 = _locLast.getTime();//if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1).getElapsedRealtimeNanos()/1000000000;
                    long t1 = loc.getTime();

                    double time = (t1-t0)/1000;//s
                    double speed = distLast / time;	//60 m/s = 216 Km/h
                    double accel = (speed - _velLast)/time;//Aceleración coche muy potente (desde parado!) < 8 m/s2

                    Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:-------*************----TIME(s)= %.0f  VEL(m/s)= %.2f  LAST VEL=%.2f  A(m/s2)= %.2f", time, speed, _velLast, accel));
                    if(speed > SPEED_MAX || accel > ACCEL_MAX)//imaginamos que es un punto erróneo, salvo que vayas en un cohete
                    {
                        Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto erróneo:   VEL=%.2f m/s  LAST VEL=%.2f  T=%.0f  a=%.2f ***************************** !!!", speed, _velLast, time, accel));
                        return false;
                    }
                    _velLast = speed;
                }
                catch(Exception e){Log.e(TAG, "guardarPunto:e:-----------------------------------------",e);}
            }
            //---

            float distLastSaved = loc.distanceTo(_locLastSaved);
            Log.e(TAG, "guardarPunto:-------********---last="+_locLastSaved
                    +"\n---newLoc="+loc
                    +"\n----distLastSaved="+distLastSaved
                    +"\n-----acc="+loc.getAccuracy());
            //Puntos muy cercanos
            if(distLastSaved < DISTANCE_MIN) return false;
            _locLast = loc;
            //TODO: Retardar bucle?? ... actividad == Still -------------------------
        }
        r.guardar(new GuardarListener(loc));
        _locLastSaved = loc;
        return true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class GuardarListener extends Fire.CompletadoListener
    {
        private Location _loc;
        GuardarListener(Location loc){_loc = loc;}

        @Override
        protected void onDatos(String id) {
            Log.e(TAG, "GuardarListener:onComplete:----------------------:" + id);
            Ruta.addPunto(id, _loc.getLatitude(), _loc.getLongitude(), _loc.getAccuracy(),
                    _loc.getAltitude(), _loc.getSpeed(), _loc.getBearing(), _lastActividad,
                    new Fire.SimpleListener<Long>() {
                        @Override
                        public void onDatos(Long[] puntos) {
                            Log.e(TAG, String.format(Locale.ENGLISH, "GuardarListener:addPunto: n ptos: %d", puntos[0]));
                            Util.refreshListaRutas();//Refrescar lista rutas en main..
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
        if(_GoogleApiClient != null)
            _GoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, String.format("**********************onConnectionFailed:e: %s", connectionResult.getErrorCode()));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //https://github.com/mcharmas/Android-ReactiveLocation
    private LocationCallback _locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if(locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                _util.setLocation(location);
                /*Log.e(TAG, "**************************----------------LocationCallback:::"
                        +"-- accu:"+location.getAccuracy()
                        +"-- prov:"+location.getProvider()
                        +"-- time:"+(new java.util.Date(location.getTime())));*/
            }
        }
    };


    @Subscribe(threadMode = ThreadMode.POSTING)//BACKGROUND)
    public void onActividadEvent(ActividadIntentService.ActividadEvent event)
    {
        DetectedActivity act = event.getActividad();
        _lastActividad = act.getType();
        //Log.e(TAG, "onActividadEvent:(last="+_lastActividad+")-------"+_util.getActivityString(_lastActividad)+" : "+act.getConfidence());
    }

}
