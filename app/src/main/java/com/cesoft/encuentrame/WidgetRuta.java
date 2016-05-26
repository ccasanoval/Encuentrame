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
	private static final String btnAddClick = "btnAddClick";
	private static final String btnStopClick = "btnStopClick";

	//______________________________________________________________________________________________
	@Override
	public void onReceive(Context context, Intent intent)
	{
		System.err.println("----------------onReceive:"+intent.getAction());
		if(btnAddClick.equals(intent.getAction()))
		{
			Toast.makeText(context, "btnAddClick", Toast.LENGTH_SHORT).show();
		}
		else if(btnStopClick.equals(intent.getAction()))
		{
			Toast.makeText(context, "btnStopClick", Toast.LENGTH_SHORT).show();
		}
	}
	//______________________________________________________________________________________________
	protected PendingIntent getPendingSelfIntent(Context context, String action)
	{
		Intent intent = new Intent(context, getClass());
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	//______________________________________________________________________________________________
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
    	ComponentName thisWidget = new ComponentName(context, WidgetRuta.class);
    	int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for(int widgetId : allWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
			remoteViews.setOnClickPendingIntent(R.id.btnAdd, getPendingSelfIntent(context, btnAddClick));
			remoteViews.setOnClickPendingIntent(R.id.btnStop, getPendingSelfIntent(context, btnStopClick));
			remoteViews.setTextViewText(R.id.txtNombre, "gps cords");
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

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
