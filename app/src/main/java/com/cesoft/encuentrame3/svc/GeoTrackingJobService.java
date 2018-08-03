package com.cesoft.encuentrame3.svc;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Log;
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
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = GeoTrackingJobService.class.getSimpleName();

    @Inject Util _util;
    @Inject Login _login;
    @Inject public GeoTrackingJobService() {
        EventBus.getDefault().register(this);
    }


    private Context appContext;
    private int _lastActividad = DetectedActivity.STILL;
    private LocationRequest _LocationRequest;
    private GoogleApiClient _GoogleApiClient;
    private FusedLocationProviderClient _fusedLocationClient;
    private Location _locLastSaved = null;
    private String _sId = "";
    private Location _locLast = null;
    private double _velLast = 0;


    public static void start(Context context) {
        Log.e(TAG, "************************* start *************************");

        ComponentName componentName = new ComponentName(context, GeoTrackingJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ID_JOB_TRACKING, componentName).setPersisted(true);

        //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(DELAY_TRACK_MIN);
        else
            builder.setPeriodic(DELAY_TRACK_MIN);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler != null)
            jobScheduler.schedule(builder.build());
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStartJob *************************");
        WidgetRutaService.startSvc(getApplicationContext());//Ini widget
        appContext = getApplicationContext();
        App.getComponent(appContext).inject(this);
        new Thread(() -> {
            if( ! _login.isLogged()) {
                Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
                stopSelf();
                GeoTrackingJobService.this.jobFinished(jobParameters, false);
            }
            else if(_util.getTrackingRoute().isEmpty()) {
                Log.e(TAG, "getTrackingRoute().isEmpty() !! STOPPING JOB " + _sId);
                stopTracking();
                stopSelf();
                GeoTrackingJobService.this.jobFinished(jobParameters, false);
            }
            else {
                //PAYLOAD
                saveGeoTracking();

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    start(getApplicationContext());
                GeoTrackingJobService.this.jobFinished(jobParameters, true);
            }
        }).start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStopJob *************************");
        return false;
    }

    //---

    public void iniGeoTracking()
    {
        if(checkPlayServices())buildGoogleApiClient();
        if(_GoogleApiClient != null)_GoogleApiClient.connect();
        _LocationRequest = new LocationRequest();
        _LocationRequest.setInterval(DELAY_TRACK_MIN);//TODO: ajustar por usuario...
        _LocationRequest.setFastestInterval(DELAY_TRACK_MIN);
        _LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        pideGPS();
    }
    protected void pideGPS() {
        _util.pideGPS(this, null, _LocationRequest);
    }
    protected synchronized void buildGoogleApiClient()
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

    private void startTracking() {
        if(_GoogleApiClient != null && _GoogleApiClient.isConnected()) {
            try {
                if(_LocationRequest == null) {
                    iniGeoTracking();
                    throw new SecurityException("_LocationRequest = NULL");
                }
                _fusedLocationClient.requestLocationUpdates(_LocationRequest, _locationCallback, Looper.myLooper());
            }
            catch(SecurityException e) {
                Log.e(TAG, "startTracking:e:---------------------------------------------------", e);
            }
        }
    }
    private void stopTracking()
    {
        if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
            _fusedLocationClient.removeLocationUpdates(_locationCallback);
        _locLastSaved = null;
        _locLast = null;
        _velLast = 0;
    }


    ///------------------------ CALLBACKS ----------------------------------------------------------
    LocationCallback _locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                _util.setLocation(location);
                Log.w(TAG, "----------------LocationCallback:::"+location.getAccuracy()+"--"+location.getProvider()+"--"+(new java.util.Date(location.getTime())));
            }
        }
    };

    public void saveGeoTracking()
    {
        final String sId = _util.getTrackingRoute();
        Log.e(TAG, "saveGeoTracking ************************************** "+_sId+" / "+sId);
        if(sId.isEmpty())
        {
            stopTracking();
            return;
        }
        startTracking();

        Ruta.getById(sId, new Fire.SimpleListener<Ruta>() {
            @Override
            public void onDatos(Ruta[] aData) {
                if(aData[0] == null) {
                    Log.e(TAG, "saveGeoTracking:Ruta.getById: RUTA == NULL -------------------------------------------"+sId);
                    _util.setTrackingRoute("");
                    stopTracking();
                }
                else {
                    final Location loc = _util.getLocation();
                    guardarPunto(loc, aData[0], sId);
                }
            }
            @Override
            public void onError(String err) {
                Log.e(TAG, "saveGeoTracking:findById:e:---------------------------------------------:" + err);
            }
        });
    }

    private synchronized void guardarPunto(Location loc, Ruta r, String sId)
    {
        Log.w(TAG, "guardarPunto:    A    *** ");
        if(loc == null) {
            Log.w(TAG, "guardarPunto:loc==NULL------------------------------------------------------");
            return;
        }
        if( ! loc.hasAccuracy()) {
            Log.w(TAG, "guardarPunto:loc.hasAccuracy()==FALSE---------------------------------------");
            return;
        }
        if(r.getPuntosCount() > 0 && (loc.getAccuracy() > ACCURACY_MAX || loc.getAccuracy() > 15+ DISTANCE_MIN)) {
            Log.w(TAG, "guardarPunto:loc.getAccuracy() ("+loc.getAccuracy()+")   > MAX_ACCURACY ("+ ACCURACY_MAX+")  or  > DISTANCE_MIN+15 ("+ DISTANCE_MIN+")    :::: n pts "+r.getPuntosCount());
            return;
        }

        if( ! _sId.equals(sId))//TODO: if sId exist in bbdd, not new route, _locLastSaved = last loc in bbdd ==> Too much monkey business
        {
            Log.w(TAG, "guardarPunto: NUEVA RUTA: -----------------"+(_sId.equals(sId))+"-------------------------- "+_sId+" ------- "+sId);//TODO : se llama dos veces, _sID=='' ?????!!!!!
            _sId = sId;
            _locLastSaved = null;
            _locLast = loc;
//            DELAY_TRACK = DELAY_TRACK_MIN;
        }
        else if(_locLastSaved != null)
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

                    Log.w(TAG, String.format(Locale.ENGLISH, "guardarPunto:-------*************----TIME(s)= %.0f  VEL(m/s)= %.2f  LAST VEL=%.2f  A(m/s2)= %.2f", time, speed, _velLast, accel));
                    //if(speed > 40 && _velLastSaved < 20 && time < 2*60)//|| speed > (_velLastSaved+1)*50)//50m/s = 180Km/h
                    if(speed > SPEED_MAX || accel > ACCEL_MAX)//imaginamos que es un punto erróneo, salvo que vayas en un cohete
                    {
                        Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto erróneo:   VEL=%.2f m/s  LAST VEL=%.2f  T=%.0f  a=%.2f *****************************", speed, _velLast, time, accel));
//                        DELAY_TRACK = DELAY_TRACK_MIN;
                        return;
                    }
                    _velLast = speed;
                }
                catch(Exception e){Log.e(TAG, "guardarPunto:e:-----------------------------------------",e);}
            }
            _locLast = loc;
            //---

            float distLastSaved = loc.distanceTo(_locLastSaved);
            Log.w(TAG, "guardarPunto:----------------************************---------------------distLastSaved="+distLastSaved+"  acc="+loc.getAccuracy());
            if(distLastSaved < DISTANCE_MIN)//Puntos muy cercanos
            {
//                Log.w(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto repetido o sin precision: %s   dist=%.1f  acc=%.1f", sId, distLastSaved, loc.getAccuracy()));
//                DELAY_TRACK += 2*1000;
//                if(DELAY_TRACK > DELAY_TRACK_MAX) DELAY_TRACK = DELAY_TRACK_MAX;
                return;
            }
//            else if(distLastSaved > 10*DISTANCE_MIN)
//                DELAY_TRACK = DELAY_TRACK_MIN;
//            else if(distLastSaved > 5*DISTANCE_MIN)//TODO: Mejorar... con actividad actual del dispositivo -------------------------
//                DELAY_TRACK -= DELAY_TRACK_MIN;
//            if(DELAY_TRACK < DELAY_TRACK_MIN) DELAY_TRACK = DELAY_TRACK_MIN;
        }
        r.guardar(new GuardarListener(loc));
        _locLastSaved = loc;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private class GuardarListener extends Fire.CompletadoListener
    {
        private Location _loc;
        GuardarListener(Location loc){_loc = loc;}

        @Override
        protected void onDatos(String id) {
            Log.w(TAG, "GuardarListener:onComplete:----------------------:" + id);
            Ruta.addPunto(id, _loc.getLatitude(), _loc.getLongitude(), _loc.getAccuracy(),
                    _loc.getAltitude(), _loc.getSpeed(), _loc.getBearing(), _lastActividad,
                    new Fire.SimpleListener<Long>() {
                        @Override
                        public void onDatos(Long[] puntos) {
                            Log.w(TAG, String.format(Locale.ENGLISH, "GuardarListener:addPunto: n ptos: %d", puntos[0]));
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
    //implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
    }
    @Override
    public void onConnectionSuspended(int i) {
        if(_GoogleApiClient != null)
            _GoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, String.format("onConnectionFailed:e: %s", connectionResult.getErrorCode()));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////




    @Subscribe(threadMode = ThreadMode.POSTING)//BACKGROUND)
    public void onActividadEvent(ActividadIntentService.ActividadEvent event)
    {
        DetectedActivity act = event.getActividad();
        _lastActividad = act.getType();
        Log.e(TAG, "onActividadEvent:(last="+_lastActividad+")-------"+_util.getActivityString(_lastActividad)+" : "+act.getConfidence());
    }

}
