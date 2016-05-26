package com.cesoft.encuentrame;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

//http://www.vogella.com/tutorials/AndroidWidgets/article.html
//http://developer.android.com/intl/es/guide/topics/appwidgets/index.html
//http://developer.android.com/intl/es/guide/practices/ui_guidelines/widget_design.html
/*
row height	=>	look at widget_layout	=>	widget_info
col width
1 	40dp
2 	110dp
3 	180dp
4 	250dp
*/
////////////////////////////////////////////////////////////////////////////////////////////////////
public class WidgetRuta extends AppWidgetProvider
{
	//______________________________________________________________________________________________
	@Override
	public void onEnabled(Context context)
	{
	}

	//______________________________________________________________________________________________
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		// Update the widgets via the service and user click
		ComponentName thisWidget = new ComponentName(context, WidgetRuta.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		Intent intent = new Intent(context.getApplicationContext(), WidgetRutaService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(intent);

		// Open the app
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);
		Intent intent2 = new Intent(context.getApplicationContext(), ActMain.class);
		PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);
		remoteViews.setOnClickPendingIntent(R.id.lblNomApp, actionPendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}

	//______________________________________________________________________________________________
	/*@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, android.os.Bundle newOptions)
	{
		//getAppWidgetOptions()
	}*/

}
