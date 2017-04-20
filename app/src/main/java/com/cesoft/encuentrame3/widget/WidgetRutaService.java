package com.cesoft.encuentrame3.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Ruta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

//TODO: si no hay ruta, parar sin fecha de activacion: ACTIVAR solo cuando se cree una nueva ruta...
// listener new punto ruta... o cambiar delay segun situacion...
////////////////////////////////////////////////////////////////////////////////////////////////////
// 
////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
public class WidgetRutaService extends Service
{
	private static final String TAG = WidgetRutaService.class.getSimpleName();
	private static Handler _h = null;
	private static Runnable _r = null;
	private static final int _DELAY_SHORT = 30*1000;
	private static final int _DELAY_LONG = 3*60*1000;

	private Util _util;

	public static Intent startServ(Context c)
	{
		Intent serviceIntent = new Intent(c, WidgetRutaService.class);
		c.startService(serviceIntent);
		return serviceIntent;
	}
	public static void stopServ(Activity act, Intent serviceIntent)
	{
		if(act != null && serviceIntent != null)
		act.stopService(serviceIntent);
	}

	//______________________________________________________________________________________________
	@Override
	//public void onStart(Intent intent, int startId)
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		_util = App.getInstance().getGlobalComponent().util();
Log.e(TAG, "onStartCommand--------------------------------------------------------------------"+_util);

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
		return super.onStartCommand(intent, flags, startId);
	}

	//______________________________________________________________________________________________
	@Override public IBinder onBind(Intent intent){return null;}

	//______________________________________________________________________________________________
	public void refresh()//TODO: cuando se crea ruta => pero necesito   https://developer.android.com/reference/android/app/Service.html#LocalServiceSample
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
			_h.postDelayed(_r, _DELAY_LONG);
		}
		else
		{
			setRuta();
			_h.postDelayed(_r, _DELAY_SHORT);
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
		try//TODO: activar desactivar botones de widget
		{
			String idRuta = _util.getTrackingRoute();
			Ruta.getById(idRuta, new Fire.SimpleListener<Ruta>()//Todo: ObjetoListener para actualizar widget? si no, como actualiza?
			{
				@Override
				public void onData(Ruta[] aData)
				{
					String sRuta = String.format(Locale.ENGLISH, "%s (%d)", aData[0].getNombre(), aData[0].getPuntosCount());
					RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_ruta);
					remoteViews.setTextViewText(R.id.txtRuta, sRuta);
					remoteViews.setViewVisibility(R.id.btnStop, View.VISIBLE);
					ComponentName componentName = new ComponentName(WidgetRutaService.this, WidgetRuta.class);
					AppWidgetManager.getInstance(WidgetRutaService.this).updateAppWidget(componentName, remoteViews);
Log.e(TAG, "setRuta:--------*****---------------"+sRuta);
				}
				@Override
				public void onError(String err)
				{
					Log.e(TAG, String.format("WidgetRutaService:cambiarTextoWidget:onError:e:-------------%s", err));
				}
			});
			stopSelf();
		}
		catch(Exception e)
		{
			Log.e(TAG, "WidgetRutaService:onStartCommand:e:-----------------------------------------", e);
		}
	}
}
