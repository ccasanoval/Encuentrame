package com.cesoft.encuentrame3;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

//http://www.vogella.com/tutorials/AndroidWidgets/article.html
//http://developer.android.com/intl/es/guide/topics/appwidgets/index.html
//http://developer.android.com/intl/es/guide/practices/ui_guidelines/widget_design.html
/*
row height	=>	look at widget_layout	=>	widget_info
col width,	1 	40dp, 2 	110dp, 3 	180dp, 4 	250dp...
*/
////////////////////////////////////////////////////////////////////////////////////////////////////
public class WidgetLugar extends AppWidgetProvider
{
	//______________________________________________________________________________________________
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// Open the app
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_lugar);
		Intent intent = new Intent(context.getApplicationContext(), ActLogin.class);
		PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnApp, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Nuevo punto
		intent = new Intent(context.getApplicationContext(), ActWidgetNuevoLugar.class);
		actionPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.btnAdd, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}
}
