package com.cesoft.encuentrame;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

////////////////////////////////////////////////////////////////////////////////////////////////////
public class WidgetRuta extends AppWidgetProvider
{
	private static final String ACTION_WIDGET_RUTA_ADD = "ACTION_WIDGET_RUTA_ADD";
	private static final String ACTION_WIDGET_RUTA_STOP = "ACTION_WIDGET_RUTA_STOP";

	//______________________________________________________________________________________________
	@Override
	public void onReceive(Context context, Intent intent)//http://stackoverflow.com/questions/2471875/processing-more-than-one-button-click-at-android-widget
	{
		System.err.println("----------------onReceive:"+intent.getAction());
		if(ACTION_WIDGET_RUTA_ADD.equals(intent.getAction()))
		{
			Intent i = new Intent(context, ActWidgetNuevaRuta.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		else if(ACTION_WIDGET_RUTA_STOP.equals(intent.getAction()))
		{
			Util.setTrackingRoute("");

			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
			remoteViews.setTextViewText(R.id.txtNombre, "Bla bla");
			//appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		else
			super.onReceive(context, intent);
	}

	//______________________________________________________________________________________________
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Intent active;
		PendingIntent actionPendingIntent;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);

		// ADD RUTA
		active = new Intent(context, getClass());//WidgetRuta.class
		active.setAction(ACTION_WIDGET_RUTA_ADD);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnAdd, actionPendingIntent);

		// STOP RUTA
		active = new Intent(context, WidgetRuta.class);
		active.setAction(ACTION_WIDGET_RUTA_STOP);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnStop, actionPendingIntent);

		// OPEN APP
		active = new Intent(context.getApplicationContext(), ActLogin.class);
		actionPendingIntent = PendingIntent.getActivity(context, 0, active, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnApp, actionPendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    	/*ComponentName thisWidget = new ComponentName(context, WidgetRuta.class);
    	int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for(int widgetId : allWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
			remoteViews.setOnClickPendingIntent(R.id.btnAdd, getPendingSelfIntent(context, ACTION_WIDGET_RUTA_ADD));
			remoteViews.setOnClickPendingIntent(R.id.btnStop, getPendingSelfIntent(context, ACTION_WIDGET_RUTA_STOP));
			remoteViews.setTextViewText(R.id.txtNombre, "gps cords");
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}*/

/*
		Intent intent;
		PendingIntent actionPendingIntent;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);

		// Open the app
		intent = new Intent(context.getApplicationContext(), ActLogin.class);
		actionPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnApp, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Nueva ruta
		intent = new Intent(context.getApplicationContext(), ActWidgetNuevaRuta.class);
		actionPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnAdd2, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Stop ruta
		intent = new Intent(context.getApplicationContext(), ActWidgetNuevoLugar.class);
		actionPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnStop, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Update the widgets via the service and user click
		ComponentName thisWidget = new ComponentName(context, WidgetRuta.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		intent = new Intent(context.getApplicationContext(), WidgetRutaService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(intent);*/
	}
}
