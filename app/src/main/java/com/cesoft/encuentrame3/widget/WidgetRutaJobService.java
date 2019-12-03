package com.cesoft.encuentrame3.widget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.ID_JOB_WIDGET;
import static com.cesoft.encuentrame3.util.Constantes.WIDGET_DELAY_LONG;
import static com.cesoft.encuentrame3.util.Constantes.WIDGET_DELAY_SHORT;

@Singleton
public class WidgetRutaJobService extends JobService {
    private static final String TAG = WidgetRutaJobService.class.getSimpleName();

    //TODO: not static!!!
    public static void start(Context context) {
        start(context, WIDGET_DELAY_SHORT, true);
    }
    private static void start(Context context, long delay, boolean first) {
        Log.e(TAG, "************************* WIDGET RUTA Start *************************");

        ComponentName componentName = new ComponentName(context, WidgetRutaJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ID_JOB_WIDGET, componentName).setPersisted(true);

        //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(first ? 1000 : delay);//La primera vez espera solo 1s
        else
            builder.setPeriodic(delay);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler != null)
            jobScheduler.schedule(builder.build());
    }

    @Inject Login login;
    @Inject Util util;
    @Inject public WidgetRutaJobService() { }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStartJob *************************");
        App.getComponent().inject(this);

        new Thread(() -> {
            if( ! login.isLogged()) {
                Log.e(TAG, "No hay usuario logado !! STOPPING JOB");
                stopSelf();
                WidgetRutaJobService.this.jobFinished(jobParameters, false);
            }
            else {

                long delay = payLoad();

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    start(getApplicationContext(), delay, false);
                WidgetRutaJobService.this.jobFinished(jobParameters, true);
            }
        }).start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStopJob *************************");
        return false;
    }

    //----------------------------------------------------------------------------------------------


    private long payLoad()
    {
        String idRuta = util.getIdTrackingRoute();
        if(idRuta.isEmpty()) {
            borrarRuta();
            return WIDGET_DELAY_LONG;
        }
        else {
            setRuta();
            return WIDGET_DELAY_SHORT;
        }
    }

    private void borrarRuta()
    {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_ruta);
        remoteViews.setTextViewText(R.id.txtRuta, "");
        remoteViews.setViewVisibility(R.id.btnStop, View.INVISIBLE);
        ComponentName componentName = new ComponentName(this, WidgetRuta.class);
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, remoteViews);
    }

    private void setRuta()
    {
        try
        {
            String idRuta = util.getIdTrackingRoute();
            Ruta.getById(idRuta, new Fire.SimpleListener<Ruta>()
            {
                @Override
                public void onDatos(Ruta[] aData)
                {
                    String sRuta = String.format(Locale.ENGLISH, "%s (%d)", aData[0].getNombre(), aData[0].getPuntosCount());
                    setWidget(sRuta, true);
                }
                @Override
                public void onError(String err)
                {
                    Log.e(TAG, String.format("WidgetRutaService:cambiarTextoWidget:onError:e:-------------%s", err));
                    setWidget("Error", false);
                }
            });
            stopSelf();
        }
        catch(Exception e)
        {
            Log.e(TAG, "WidgetRutaService:onStartCommand:e:-----------------------------------------", e);
        }
    }

    private void setWidget(String sRuta, boolean bRuta)
    {
        Context context = getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WidgetRuta.class));
        WidgetRuta.setWidget(context, appWidgetManager, allWidgetIds, sRuta, bRuta);
    }
}
