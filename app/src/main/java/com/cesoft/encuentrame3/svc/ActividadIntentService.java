package com.cesoft.encuentrame3.svc;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

//TODO: Inejct
public class ActividadIntentService extends IntentService {

    protected static final String TAG = ActividadIntentService.class.getSimpleName();

    // Time between activity detections. Larger values result in fewer detections while improving
    // battery life. A value of 0 results in activity detections at the fastest rate possible
    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 10 * 1000;//TODO: Settings
    private static PendingIntent getPendingIntent(Context context) {
        // FLAG_UPDATE_CURRENT to get the same pending intent back when ini and destroy service
        Intent intent = new Intent(context, ActividadIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static void start(Context context) {
        ActivityRecognitionClient activityRecognitionClient = new ActivityRecognitionClient(context);
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getPendingIntent(context));
        //task.addOnSuccessListener(new OnSuccessListener<Void>() { @Override public void onSuccess(Void result) {
        task.addOnFailureListener(e -> Log.e(TAG, "start:e:-----------------------------------",e));
    }
    public static void stop(Context context) {
        ActivityRecognitionClient activityRecognitionClient = new ActivityRecognitionClient(context);
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(getPendingIntent(context));
        task.addOnFailureListener(e -> Log.e(TAG, "stop:e:------------------------------------",e));
    }

    public ActividadIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get a list of the probable activities associated with the current state of the device.
        // Each activity is associated with a confidence level, which is an int between 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList)result.getProbableActivities();

        // Comunicar la nueva actividad a los observers
        //EventBus.getDefault().post(new ActividadEvent(detectedActivities));
        DetectedActivity act = getMostProbableAct(detectedActivities);
        //DetectedActivity act2 = getNextMostProbableAct(detectedActivities, act);
        if(act != null && act.getConfidence() >= 30) {
            EventBus.getDefault().post(new ActividadEvent(act));
        }
        //else if(act2 != null && act2.getConfidence() >= 30){

        //Log.e(TAG, "--------------------------------------------------------------------");
        //Log.e(TAG, "---IN_VEHICLE = 0;   ON_BICYCLE = 1;   ON_FOOT = 2;     STILL   = 3;");
        //Log.e(TAG, "---UNKNOWN    = 4;   TILTING    = 5;   WALKING = 7;     RUNNING = 8;");
        /*Log.e(TAG, "-------------------activities selected: "+act);
        Log.e(TAG, "-------------------activities detected: "+detectedActivities.size());
        for(DetectedActivity da: detectedActivities) {
            Log.e(TAG, "------"+da.getType() + " " + da.getConfidence() + "%");
        }*/
    }

    public static DetectedActivity getMostProbableAct(List<DetectedActivity> list)
    {
        DetectedActivity mostProbable = null;
        int confidence = 0;
        for(DetectedActivity act : list)
        {
            if(act.getConfidence() > confidence)
            {
                confidence = act.getConfidence();
                mostProbable = act;
            }
        }
        return mostProbable;
    }

    public static class ActividadEvent {
        private DetectedActivity actividad;
        public DetectedActivity getActividad() { return actividad; }
        ActividadEvent(DetectedActivity actividad) {
            this.actividad = actividad;
        }
    }
}
