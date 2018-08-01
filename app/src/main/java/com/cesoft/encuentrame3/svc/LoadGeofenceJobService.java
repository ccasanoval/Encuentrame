package com.cesoft.encuentrame3.svc;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.DELAY_LOAD;
import static com.cesoft.encuentrame3.util.Constantes.GEOFEN_DWELL_TIME;
import static com.cesoft.encuentrame3.util.Constantes.ID_JOB_GEOFENCE_LOADING;

@Singleton
public class LoadGeofenceJobService extends JobService {
    private static final String TAG = LoadGeofenceJobService.class.getSimpleName();

    @Inject LoadGeofenceJobService() { }

    public static void start(Context context) {
        Log.e(TAG, "************************* start *************************");

        ComponentName componentName = new ComponentName(context, LoadGeofenceJobService.class);

        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
            jobInfo = new JobInfo.Builder(ID_JOB_GEOFENCE_LOADING, componentName)
                    .setMinimumLatency(DELAY_LOAD)
                    .setPersisted(true)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(ID_JOB_GEOFENCE_LOADING, componentName)
                    .setPeriodic(DELAY_LOAD)
                    .setPersisted(true)
                    .build();
        }
        /*JobInfo jobInfo = new JobInfo.Builder(ID_JOB_GEOFENCE_LOADING, serviceComponent)
                .setPeriodic(30000)//.setPeriodic(DELAY_LOAD)
                .setBackoffCriteria(DELAY_LOAD, JobInfo.BACKOFF_POLICY_LINEAR)
                .setPersisted(true) //Esto haria innecesario el <receiver android:name=".svc.CesOnSysBoot"><action android:name="android.intent.action.BOOT_COMPLETED"/>
                .build();*/
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler != null)
            jobScheduler.schedule(jobInfo);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStartJob *************************");
        cargarListaGeoAvisos();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            start(getApplicationContext());
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStopJob *************************");
        if(_GeofenceStoreAvisos != null)
           _GeofenceStoreAvisos.clear();
        _GeofenceStoreAvisos = null;
        return false;
    }

    //----------------------------------------------------------------------------------------------

    public void cargarListaGeoAvisos()
    {
        Log.e(TAG, "************************* cargarListaGeoAvisos *************************");
        createListAviso();
        try { Aviso.getActivos(_lisAviso); }
        catch(Exception e) { Log.e(TAG, "cargarListaGeoAvisos:e:------------------------------", e); }
    }
    private boolean _isIni = false;
    private Fire.DatosListener<Aviso> _lisAviso;
    private CesGeofenceStore _GeofenceStoreAvisos;
    private ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();
    private void createListAviso()
    {
        if(_isIni)return;
        _isIni = true;
        final Context context = getApplicationContext();
        _lisAviso = new Fire.DatosListener<Aviso>()
        {
            @Override
            public void onDatos(Aviso[] aData)
            {
                //TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
                boolean bDirty = false;
                long n = aData.length;
                if(n != LoadGeofenceJobService.this._listaGeoAvisos.size())
                {
                    if(_GeofenceStoreAvisos != null)_GeofenceStoreAvisos.clear();
                    _listaGeoAvisos.clear();
                    bDirty = true;
                }
                ArrayList<Geofence> aGeofences = new ArrayList<>();
                ArrayList<Aviso> aAvisos = new ArrayList<>();
                for(int i=0; i < aData.length; i++)
                {
                    Aviso a = aData[i];
                    aAvisos.add(a);
                    Geofence gf = new Geofence.Builder().setRequestId(a.getId())
                            .setCircularRegion(a.getLatitud(), a.getLongitud(), (float)a.getRadio())
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setLoiteringDelay(GEOFEN_DWELL_TIME)// Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT).build();
                    aGeofences.add(gf);
                    if( ! bDirty)
                    {
                        if(_listaGeoAvisos.size() < i)
                            bDirty = true;
                        else if( ! _listaGeoAvisos.contains(a))//else if(_listaGeoAvisos.get(i))
                            bDirty = true;
                        i++;
                    }
                }
                if(bDirty)
                {
                    _listaGeoAvisos = aAvisos;
                    _GeofenceStoreAvisos = new CesGeofenceStore(aGeofences, context);//Se puede añadir en lugar de crear desde cero?
                }
            }
            @Override
            public void onError(String err)
            {
                Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------"+err);
            }
        };
    }
}
