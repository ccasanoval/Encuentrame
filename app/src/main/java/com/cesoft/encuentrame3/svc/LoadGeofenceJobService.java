package com.cesoft.encuentrame3.svc;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.DELAY_LOAD_GEOFENCE;
import static com.cesoft.encuentrame3.util.Constantes.GEOFEN_DWELL_TIME;
import static com.cesoft.encuentrame3.util.Constantes.ID_JOB_GEOFENCE_LOADING;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
@Singleton
public class LoadGeofenceJobService extends JobService {
    private static final String TAG = LoadGeofenceJobService.class.getSimpleName();

    public static void start(Context context) {
        start(context, true);
    }
    private static void start(Context context, boolean first) {
//        Log.e(TAG, "************************* FENCE Start *************************");

        ComponentName componentName = new ComponentName(context, LoadGeofenceJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ID_JOB_GEOFENCE_LOADING, componentName).setPersisted(true);

        //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(first ? 1000 : DELAY_LOAD_GEOFENCE);//La primera vez espera solo 1s
        else
            builder.setPeriodic(DELAY_LOAD_GEOFENCE);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler != null)
            jobScheduler.schedule(builder.build());
    }

    @Inject Login login;
    @Inject public LoadGeofenceJobService() { }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
//        Log.e(TAG, "************************* onStartJob *************************");
        App.getComponent(getApplicationContext()).inject(this);
        new Thread(() -> {
            if( ! login.isLogged()) {
                Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
                stopSelf();
                LoadGeofenceJobService.this.jobFinished(jobParameters, false);
            }
            else {
                cargarListaGeoAvisos();//PAYLOAD

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    start(getApplication(), false);
                LoadGeofenceJobService.this.jobFinished(jobParameters, true);
            }
        }).start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
//        Log.e(TAG, "************************* onStopJob *************************");
        if(geofenceStoreAvisos != null)
            geofenceStoreAvisos.clear();
        geofenceStoreAvisos = null;
        return false;
    }

    //----------------------------------------------------------------------------------------------

    public void cargarListaGeoAvisos()
    {
//        Log.e(TAG, "************************* cargarListaGeoAvisos *************************");
        createListAviso();
        try { Aviso.getActivos(lisAviso); }
        catch(Exception e) { Log.e(TAG, "cargarListaGeoAvisos:e:------------------------------", e); }
    }
    private boolean isInit = false;
    private Fire.DatosListener<Aviso> lisAviso;
    private CesGeofenceStore geofenceStoreAvisos;
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
                boolean bDirty = false;
                long n = aData.length;
                //Log.e(TAG, "aData.length:"+n+" VS "+listaGeoAvisos.size()+"-----------------------------------------------------");
                if(n != listaGeoAvisos.size())
                {
                    if(geofenceStoreAvisos != null) geofenceStoreAvisos.clear();
                    listaGeoAvisos.clear();
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
                    if( ! bDirty && (listaGeoAvisos.size() < i || ! listaGeoAvisos.contains(a)))
                    {
                        bDirty = true;
                    }
                }
                if(bDirty)
                {
                    listaGeoAvisos = aAvisos;
                    geofenceStoreAvisos = new CesGeofenceStore(getApplication(), aGeofences);//Se puede añadir en lugar de crear desde cero?
                }
                //Log.e(TAG, "listaGeoAvisos:"+listaGeoAvisos.size()+"------------------aGeofences:"+aGeofences.size()+"-----------------------------------");
            }
            @Override
            public void onError(String err)
            {
                Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------"+err);
            }
        };
    }
}
