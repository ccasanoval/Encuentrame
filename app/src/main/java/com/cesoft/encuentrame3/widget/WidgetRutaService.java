package com.cesoft.encuentrame3.widget;

import android.app.ActivityManager;
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
	public static void bindSvc(ServiceConnection sc, Context c)
	{
		Intent serviceIntent = new Intent(c, WidgetRutaService.class);
		c.bindService(serviceIntent, sc, Context.BIND_AUTO_CREATE);
	}
	public static void unbindSvc(ServiceConnection sc, Context c)
	{
		c.unbindService(sc);
	}
	//----
	public static void startSvc(Context context)
	{
		//Log.e(TAG, "____________________startSvc__A_____________________________");
		if(isServiceRunning(context, WidgetRutaService.class))return;
		Intent serviceIntent = new Intent(context, WidgetRutaService.class);
		context.startService(serviceIntent);
		//Log.e(TAG, "____________________startSvc__B_____________________________");
	}
	private static boolean isServiceRunning(Context c, Class<?> serviceClass)
	{
		ActivityManager manager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
		if(manager != null)
		for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if(serviceClass.getName().equals(service.service.getClassName()))
				return true;
		}
		return false;
	}

	//______________________________________________________________________________________________
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		_util = App.getComponent(getApplicationContext()).util();
		if(_h == null) {
			_h = new Handler();
			_r = this::payLoad;
			payLoad();
		}
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;//START_REDELIVER_INTENT
	}
	@Override public void onDestroy() { Log.w(TAG, "onDestroy:--------------------------------------"); }


	//______________________________________________________________________________________________
	//@Override public IBinder onBind(Intent intent){return null;}
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	private final IBinder _Binder = new LocalBinder();
	public class LocalBinder extends android.os.Binder {
		public WidgetRutaService getService() { return WidgetRutaService.this; }
	}
	@Override public IBinder onBind(Intent intent) { return _Binder; }
	//______________________________________________________________________________________________
	public void refresh()
	{
		try {
			_h.removeCallbacks(_r);
			payLoad();
		}
		catch(Exception e) {
			Log.e(TAG, "refresh:e:------------------------------------------------------------------",e);
		}
	}
	//______________________________________________________________________________________________
	private void payLoad()
	{
		String idRuta = _util.getTrackingRoute();
		if(idRuta.isEmpty()) {
			borrarRuta();
			_h.postDelayed(_r, Constantes.WIDGET_DELAY_LONG);
		}
		else {
			setRuta();
			_h.postDelayed(_r, Constantes.WIDGET_DELAY_SHORT);
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
					setWidget(sRuta, true);
				}
				@Override
				public void onError(String err)
				{
					Log.e(TAG, String.format("WidgetRutaService:cambiarTextoWidget:onError:e:-------------%s", err));
					setWidget("Error", false);
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
	private void setWidget(String sRuta, boolean bRuta)
	{
		//Log.e(TAG, "setWidget:--------------------------------- sTarea = "+sTarea);
		Context context = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplication(), WidgetRuta.class));
		WidgetRuta.setWidget(context, appWidgetManager, allWidgetIds, sRuta, bRuta);
	}

	//----------------------------------------------------------------------------------------------
	// Restaura el servicio cuando se le mata el proceso
	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		Log.e(TAG, "-------------------------------onTaskRemoved-------------------------------");
		Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		if(alarmService != null)
		alarmService.set(
				AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 500,
				restartServicePendingIntent);

		Log.e(TAG, "-------------------------------Reiniciando...-------------------------------");
		super.onTaskRemoved(rootIntent);
	}
}
