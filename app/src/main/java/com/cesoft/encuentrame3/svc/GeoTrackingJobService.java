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
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.DELAY_TRACK_MIN;
import static com.cesoft.encuentrame3.util.Constantes.ID_JOB_TRACKING;

@Singleton
public class GeoTrackingJobService extends JobService {
    private static final String TAG = GeoTrackingJobService.class.getSimpleName();

    @Inject Util _util;
    @Inject Login _login;
    @Inject GeoTrackingJobService() { }

    public static void start(Context context) {
        Log.e(TAG, "************************* start *************************");

        ComponentName componentName = new ComponentName(context, GeoTrackingJobService.class);

        JobInfo jobInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //SDK >= 24 => max periodic = JobInfo.getMinPeriodMillis() = 15min
            jobInfo = new JobInfo.Builder(ID_JOB_TRACKING, componentName)
                    .setMinimumLatency(DELAY_TRACK_MIN)
                    .setPersisted(true)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(ID_JOB_TRACKING, componentName)
                    .setPeriodic(DELAY_TRACK_MIN)
                    .setPersisted(true)
                    .build();
        }
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler != null)
            jobScheduler.schedule(jobInfo);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStartJob *************************");
        App.getComponent(getApplicationContext()).inject(this);
        if( ! _login.isLogged()) {
            Log.e(TAG, "No conectado !!");
            stopSelf();
            return false;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            start(getApplicationContext());
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e(TAG, "************************* onStopJob *************************");
        return false;
    }
}
