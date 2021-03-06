package com.cesoft.encuentrame;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.cesoft.encuentrame.models.Ruta;

import java.util.Locale;

//TODO: si no hay ruta, parar sin fecha de activacion: ACTIVAR solo cuando se cree una nueva ruta...
// listener new punto ruta... o cambiar delay segun situacion...
////////////////////////////////////////////////////////////////////////////////////////////////////
// 
////////////////////////////////////////////////////////////////////////////////////////////////////
public class WidgetRutaService extends Service
{
	private static Handler _h = null;
	private static Runnable _r = null;
	private static final int _DELAY_SHORT = 30*1000;
	private static final int _DELAY_LONG = 2*60*1000;//5*60*1000;
	//private static Long _id = -1L;

	//______________________________________________________________________________________________
	@Override
	//public void onStart(Intent intent, int startId)
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
System.err.println("-----WidgetRutaService--onStartCommand---");
		if(_h == null)//TODO: mejorar la forma de actualizar... cerrar servicio si no hay ruta? y actualizar mas rapido cuando se añade o para la ruta desde propio widget...
		{
			_h = new Handler();
			_r = new Runnable()
			{
				@Override
				public void run()
				{
					payLoad(intent);
				}
			};
			_h.postDelayed(_r, _DELAY_SHORT);
		}
		payLoad(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	//______________________________________________________________________________________________
	@Override public IBinder onBind(Intent intent){return null;}

	//______________________________________________________________________________________________
	private void payLoad(Intent intent)
	{
		String idRuta = Util.getTrackingRoute(WidgetRutaService.this);
		System.err.println("-----WidgetRutaService-----"+idRuta+";");
		if( ! idRuta.isEmpty())
		{
			setRuta(intent);
			_h.postDelayed(_r, _DELAY_SHORT);
System.err.println("-----WidgetRutaService--SHORT---");
		}
		else
		{
			borrarRuta();
			_h.postDelayed(_r, _DELAY_LONG);
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
	//______________________________________________________________________________________________
	private void setRuta(final Intent intent)
	{
System.err.println("---------WidgetRutaService:cambiarTextoWidget");
		try//TODO: activar desactivar botones de widget
		{
			String idRuta = Util.getTrackingRoute(this);
			Ruta.getById(idRuta, new AsyncCallback<Ruta>()
			{
				@Override
				public void handleResponse(Ruta ruta)
				{
					if(ruta == null)return;
					String sRuta = String.format(Locale.ENGLISH, "%s (%d)", ruta.getNombre(), ruta.getPuntos().size());
					/*AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetRutaService.this.getApplicationContext());
					int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
					if(allWidgetIds == null)
						System.err.println("---------WidgetRutaService:cambiarTextoWidget:handleResponse: allWidgetsIds == null");
					else
					for(int widgetId : allWidgetIds)
					{
						RemoteViews remoteViews = new RemoteViews(WidgetRutaService.this.getApplicationContext().getPackageName(), R.layout.widget_ruta);
						remoteViews.setTextViewText(R.id.txtRuta, s);

						Intent clickIntent = new Intent(WidgetRutaService.this.getApplicationContext(), WidgetRuta.class);
						clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
						clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

						PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
						remoteViews.setOnClickPendingIntent(R.id.txtRuta, pendingIntent);
						appWidgetManager.updateAppWidget(widgetId, remoteViews);
					}*/
					RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_ruta);
					remoteViews.setTextViewText(R.id.txtRuta, sRuta);
					remoteViews.setViewVisibility(R.id.btnStop, View.VISIBLE);
					ComponentName componentName = new ComponentName(WidgetRutaService.this, WidgetRuta.class);
					AppWidgetManager.getInstance(WidgetRutaService.this).updateAppWidget(componentName, remoteViews);
				}
				@Override
				public void handleFault(BackendlessFault f)
				{
					System.err.println("WidgetRutaService:cambiarTextoWidget:handleFault:f:"+f);
				}
			});

			stopSelf();
		}
		catch(Exception e)
		{
			System.err.println("WidgetRutaService:onStartCommand:e: "+e);
		}
	}
}
