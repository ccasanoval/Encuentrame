package com.cesoft.encuentrame3.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Ruta;

import java.util.Locale;


//TODO: si no hay ruta, parar sin fecha de activacion: ACTIVAR solo cuando se cree una nueva ruta...
// listener new punto ruta... o cambiar delay segun situacion...
////////////////////////////////////////////////////////////////////////////////////////////////////
// 
////////////////////////////////////////////////////////////////////////////////////////////////////
//@Singleton
//TODO: hacer en thread... para no ralentizar MainThread
public class WidgetRutaService extends Service
{
	private static final String TAG = WidgetRutaService.class.getSimpleName();
	private static Handler _h = null;
	private static Runnable _r = null;

	private Util _util;

	//----
	public static Intent bindSvc(ServiceConnection sc, Context c)
	{
		Intent serviceIntent = new Intent(c, WidgetRutaService.class);
		c.bindService(serviceIntent, sc, Context.BIND_AUTO_CREATE);
		return serviceIntent;
	}
	public static void unbindSvc(ServiceConnection sc, Context c)//, Intent serviceIntent)
	{
		c.unbindService(sc);
		//c.stopService(serviceIntent);
	}
	//----
	public static Intent startSvc(Context c)
	{
		Intent serviceIntent = new Intent(c, WidgetRutaService.class);
		c.startService(serviceIntent);
		return serviceIntent;
	}
	/*public static void stopSvc(Activity act, Intent serviceIntent)
	{
		if(act != null && serviceIntent != null)
		act.stopService(serviceIntent);
	}*/

	//______________________________________________________________________________________________
	@Override
	//public void onStart(Intent intent, int startId)
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		Log.e(TAG, "--------------onStartCommand----------------------------------------------------");
		_util = App.getComponent(getApplicationContext()).util();

		if(_h == null)//TODO: mejorar la forma de actualizar... cerrar servicio si no hay ruta? y actualizar mas rapido cuando se añade o para la ruta desde propio widget...
		{
			_h = new Handler();
			_r = new Runnable()
			{
				@Override
				public void run()
				{
					payLoad();
				}
			};
			//_h.postDelayed(_r, _DELAY_SHORT);
			payLoad();
		}
		//return
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;//START_REDELIVER_INTENT
	}
	@Override public void onDestroy() { Log.e(TAG, "onDestroy:--------------------------------------"); }


	//______________________________________________________________________________________________
	//@Override public IBinder onBind(Intent intent){return null;}
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	private final IBinder _Binder = new LocalBinder();
	public class LocalBinder extends android.os.Binder
	{
		public WidgetRutaService getService() { return WidgetRutaService.this; }
	}
	@Override public IBinder onBind(Intent intent) { return _Binder; }
	//______________________________________________________________________________________________
	public void refresh()
	{
		try
		{
			_h.removeCallbacks(_r);
			payLoad();
		}
		catch(Exception e)
		{
			Log.e(TAG, "refresh:e:------------------------------------------------------------------",e);
		}
	}
	//______________________________________________________________________________________________
	private void payLoad()
	{
		String idRuta = _util.getTrackingRoute();
		if(idRuta.isEmpty())
		{
			borrarRuta();
			_h.postDelayed(_r, Constantes.WIDGET_DELAY_LONG);
		}
		else
		{
			setRuta();
			_h.postDelayed(_r, Constantes.WIDGET_DELAY_SHORT);//TODO: cambiante, acorde con CesService.DELAY_TRACK
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
	private void setRuta()
	{
		try
		{
			String idRuta = _util.getTrackingRoute();
			Ruta.getById(idRuta, new Fire.SimpleListener<Ruta>()
			{
				@Override
				public void onDatos(Ruta[] aData)
				{
					String sRuta = String.format(Locale.ENGLISH, "%s (%d)", aData[0].getNombre(), aData[0].getPuntosCount());
					/*RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_ruta);
					remoteViews.setTextViewText(R.id.txtRuta, sRuta);
					remoteViews.setViewVisibility(R.id.btnStop, View.VISIBLE);
					ComponentName componentName = new ComponentName(WidgetRutaService.this, WidgetRuta.class);
					AppWidgetManager.getInstance(WidgetRutaService.this).updateAppWidget(componentName, remoteViews);*/
					setWidget(sRuta);
Log.e(TAG, "setRuta:--------*****---------------"+sRuta);
				}
				@Override
				public void onError(String err)
				{
					Log.e(TAG, String.format("WidgetRutaService:cambiarTextoWidget:onError:e:-------------%s", err));
					setWidget("Error");
				}
			});
			stopSelf();
		}
		catch(Exception e)
		{
			Log.e(TAG, "WidgetRutaService:onStartCommand:e:-----------------------------------------", e);
		}
	}

	//----------------------------------------------------------------------------------------------
	private void setWidget(String sRuta)
	{
		//Log.e(TAG, "setWidget:--------------------------------- sTarea = "+sTarea);
		Context context = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WidgetRuta.class));
		/*for(int widgetId : allWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_ruta);

			// Actualiza tarea actual
			remoteViews.setTextViewText(R.id.txtRuta, sRuta);

			//  onClick  ->  Actualiza tarea actual
			Intent clickIntent = new Intent(context, WidgetRuta.class);
			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.txtRuta, pendingIntent);

			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}*/
		WidgetRuta.setWidget(context, appWidgetManager, allWidgetIds, sRuta);
	}

	//----------------------------------------------------------------------------------------------
	// Restaura el servicio cuando se le mata el proceso
	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		Log.e(TAG, "-------------------onTaskRemoved---------------------");
		Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmService.set(
				AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 500,
				restartServicePendingIntent);

		Log.e(TAG, "-------------------Reiniciando...---------------------");
		super.onTaskRemoved(rootIntent);
	}
}
