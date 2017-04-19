package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Aviso;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


//http://stackoverflow.com/questions/21414160/how-to-increase-consistency-of-android-geofence-enter-exit-notifications
//http://stackoverflow.com/questions/19505614/android-geofence-eventually-stop-getting-transition-intents/19521823#19521823
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/03/2016.
//TODO: En lugar de CesServiceAvisoGeo se utiliza este BroadcastReceiver porque dicen es mas fiable : eliminar CesServiceAvisoGeo
public class CesGeofenceReceiver extends BroadcastReceiver
{
	private static final String TAG = CesGeofenceReceiver.class.getSimpleName();
	private Util _util;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		_util = ((App)context.getApplicationContext()).getGlobalComponent().util();
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if( ! geofencingEvent.hasError())
		{
			int transition = geofencingEvent.getGeofenceTransition();
			List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
			switch(transition)
			{
			case Geofence.GEOFENCE_TRANSITION_ENTER:
Log.w(TAG, "CesGeofenceReceiver:onReceive:-------------------------------------GEOFENCE_TRANSITION_ENTER");
				for(Geofence geof : geofences)
				{
					showAviso(context, geof.getRequestId(), context.getString(R.string.en_zona_aviso));
Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
Log.w(TAG, "CesGeofenceReceiver:onReceive:--------------------------------------GEOFENCE_TRANSITION_DWELL");
				for(Geofence geof : geofences)
				{
					showAviso(context, geof.getRequestId(), context.getString(R.string.en_zona_aviso));
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
Log.w(TAG, "CesGeofenceReceiver:onReceive:---------------------------------------GEOFENCE_TRANSITION_EXIT");
				for(Geofence geof : geofences)
				{
					Log.w(TAG, "CesGeofenceReceiver:onReceive:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
				}
				//for(Geofence geof : geofences)addTrackingPoint(geof);
				break;
			default:
Log.w(TAG, "CesGeofenceReceiver:onReceive:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	protected void showAviso(final Context context, String sId, final String sTitle)
	{
		Aviso.getById(sId, new Fire.SimpleListener<Aviso>()
		{
			@Override
			public void onData(Aviso[] aData)
			{
				Intent i = new Intent(context, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, aData[0]);
				i.putExtra("notificacion", true);
				_util.showAviso(sTitle, aData[0], i);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "showAviso:e:------------------------------------------------------------" + err);
			}
		});
				/*new ValueEventListener()
		{
			@Override
			public void onDataChange(com.google.firebase.database.DataSnapshot data)
			{
				Aviso a = data.getValue(Aviso.class);
				Intent i = new Intent(context, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, a);
				i.putExtra("notificacion", true);
				_util.showAviso(sTitle, a, i);
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "CesGeofenceReceiver:showAviso:e:" + err);
			}
		});*/
	}

	/*
	public static final String CATEGORY_LOCATION_SERVICES = "CATEGORY_LOCATION_SERVICES";
	public static final String ACTION_GEOFENCE_ERROR = "ACTION_GEOFENCE_ERROR";
	public static final String EXTRA_GEOFENCE_STATUS = "EXTRA_GEOFENCE_STATUS";
	public static final String ACTION_GEOFENCE_TRANSITION = "ACTION_GEOFENCE_TRANSITION";
	public static final String EXTRA_GEOFENCE_ID = "EXTRA_GEOFENCE_ID";
	public static final String EXTRA_GEOFENCE_TRANSITION_TYPE = "EXTRA_GEOFENCE_TRANSITION_TYPE";

	Context context;
	Intent broadcastIntent = new Intent();

	@Override
	public void onReceive(Context context, Intent intent)
	{
System.err.println("---------------------CesGeofenceReceiver:onReceive");
		this.context = context;
		broadcastIntent.addCategory(CATEGORY_LOCATION_SERVICES);//GeofenceUtils.

		//if(LocationClient.hasError(intent))		handleError(intent);		else
			handleEnterExit(intent);
	}

	private void handleError(Intent intent)
	{
System.err.println("---------------------CesGeofenceReceiver:handleError");
		// Get the error code
		//int errorCode = LocationClient.getErrorCode(intent);

		// Get the error message
		//String errorMessage = LocationServiceErrorMessages.getErrorString(context, errorCode);
		// Log the error
		//Log.e(GeofenceUtils.APPTAG,		context.getString(R.string.geofence_transition_error_detail,		errorMessage));

		// Set the action and error message for the broadcast intent
		broadcastIntent.setAction(ACTION_GEOFENCE_ERROR).putExtra(EXTRA_GEOFENCE_STATUS, 69);

		// Broadcast the error *locally* to other components in this app
		LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
	}


	private void handleEnterExit(Intent intent)
	{
System.err.println("---------------------CesGeofenceReceiver:handleEnterExit");
		// Get the type of transition (entry or exit)
		int transition = LocationClient.getGeofenceTransition(intent);

		// Test that a valid transition was reported
		if((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT))
		{
			// Post a notification
			List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
			String[] geofenceIds = new String[geofences.size()];
			String ids = TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
			String transitionType = GeofenceUtils.getTransitionString(transition);

			for (int index = 0; index < geofences.size(); index++)
			{
				Geofence geofence = geofences.get(index);
				//...do something with the geofence entry or exit. I'm saving them to a local sqlite db
			}
			// Create an Intent to broadcast to the app
			broadcastIntent
				.setAction(ACTION_GEOFENCE_TRANSITION)//GeofenceUtils
				.addCategory(CATEGORY_LOCATION_SERVICES)
				.putExtra(EXTRA_GEOFENCE_ID, geofenceIds)
				.putExtra(EXTRA_GEOFENCE_TRANSITION_TYPE, transitionType);

			LocalBroadcastManager.getInstance(Util.getApplication().getBaseContext()).sendBroadcast(broadcastIntent);

			// Log the transition type and a message
			//Log.w(GeofenceUtils.APPTAG, transitionType + ": " + ids);
			//Log.w(GeofenceUtils.APPTAG,
			//context.getString(R.string.geofence_transition_notification_text));
			// In debug mode, log the result
			//Log.w(GeofenceUtils.APPTAG, "transition");

				// An invalid transition was reported
		}
		else
		{
			// Always log as an error
			//Log.e(GeofenceUtils.APPTAG,
			//context.getString(R.string.geofence_transition_invalid_type,transition));
		}
	}


//	* Posts a notification in the notification bar when a transition is
//	* detected. If the user clicks the notification, control goes to the main
//	* Activity.
//	*
//	* @param transitionType
//	*            The type of transition that occurred.
//
	private void sendNotification(String transitionType, String locationName)
	{
		// Create an explicit content Intent that starts the main Activity
		Intent notificationIntent = new Intent(context, MainActivity.class);
		// Construct a task stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the main Activity to the task stack as the parent
		stackBuilder.addParentStack(MainActivity.class);
		// Push the content Intent onto the stack
		stackBuilder.addNextIntent(notificationIntent);
		// Get a PendingIntent containing the entire back stack
		PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		// Get a notification builder that's compatible with platform versions >= 4
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		// Set the notification contents
		builder.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(transitionType + ": " + locationName)
				.setContentText("blablabla")//context.getString(R.string.geofence_transition_notification_text))
				.setContentIntent(notificationPendingIntent);
		// Get an instance of the Notification manager
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Issue the notification
		mNotificationManager.notify(0, builder.build());
	}*/
}