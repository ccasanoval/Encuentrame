package com.cesoft.encuentrame3.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// TODO : despues de matar app con TaskKiller el widget deja de responder...! QUIZA PORQUE SE MATO EL CONTEXTO ORIGINAL
public class WidgetRuta extends AppWidgetProvider
{
	private static final String ACTION_WIDGET_RUTA_ADD = "ACTION_WIDGET_RUTA_ADD";
	private static final String ACTION_WIDGET_RUTA_STOP = "ACTION_WIDGET_RUTA_STOP";

	//______________________________________________________________________________________________
	@Override
	public void onReceive(Context context, Intent intent)
	{
		WidgetRutaService.startSvc(context);

		Util _util = ((App)context.getApplicationContext()).getGlobalComponent().util();

		if(ACTION_WIDGET_RUTA_ADD.equals(intent.getAction()))
		{
			Intent i = new Intent(context, ActWidgetNuevaRuta.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
		else if(ACTION_WIDGET_RUTA_STOP.equals(intent.getAction()))
		{
			_util.setTrackingRoute("");
			//
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
			remoteViews.setTextViewText(R.id.txtRuta, "");
			remoteViews.setViewVisibility(R.id.btnStop, View.INVISIBLE);
			ComponentName componentName = new ComponentName(context, WidgetRuta.class);
			AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
		}
		else
			super.onReceive(context, intent);
	}

	//______________________________________________________________________________________________
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
//		Log.w("WidgetRuta", "onUpdate-----------------------------------------------------**************************** ");
		setWidget(context, appWidgetManager, appWidgetIds, null, false);
	}

	//----------------------------------------------------------------------------------------------
	static public void setWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, String sRuta, boolean bRuta)
	{
		Intent intent;
		PendingIntent actionPendingIntent;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
//Log.w("WidgetRuta", "setWidget-----------------------------------------------------**************************** = "+sRuta);

		// NOMBRE RUTA
		if(sRuta != null)
		remoteViews.setTextViewText(R.id.txtRuta, sRuta);
		if(bRuta)
			remoteViews.setViewVisibility(R.id.btnStop, View.VISIBLE);
		else
			remoteViews.setViewVisibility(R.id.btnStop, View.INVISIBLE);
		//
		//TODO: al pulsar en la ruta abrir la app en la ruta especificada...

		// ADD RUTA
		intent = new Intent(context, WidgetRuta.class);
		intent.setAction(ACTION_WIDGET_RUTA_ADD);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnAdd, actionPendingIntent);

		// STOP RUTA
		intent = new Intent(context, WidgetRuta.class);
		intent.setAction(ACTION_WIDGET_RUTA_STOP);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnStop, actionPendingIntent);

		// OPEN APP
		intent = new Intent(context.getApplicationContext(), ActMain.class);
		intent.putExtra(Constantes.WIN_TAB, Constantes.RUTAS);
		actionPendingIntent = PendingIntent.getActivity(
				context,
				Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK,
				intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btnApp, actionPendingIntent);

		// REFRESH WIDGET SVC
		ComponentName thisWidget = new ComponentName(context, WidgetRuta.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		intent = new Intent(context.getApplicationContext(), WidgetRutaService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(intent);

		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
}
