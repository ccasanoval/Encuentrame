package com.cesoft.encuentrame3.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.EditText;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.adapters.IListaItemClick;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Objeto;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
@Singleton
public class Util
{
	private static final String TAG = Util.class.getSimpleName();
	public static final String TIPO = "tipo";

	//______________________________________________________________________________________________
	private Application _app;
	private SharedPreferences _sp;
	private LocationManager _lm;
	private NotificationManager _nm;
	private PowerManager _pm;
	@Inject
	public Util(Application app, SharedPreferences sp, LocationManager lm, NotificationManager nm, PowerManager pm)
	{
		_app = app;
		_sp = sp;
		_lm = lm;
		_nm = nm;
		_pm = pm;
	}
	//______________________________________________________________________________________________
	// REFRESH LISTA RUTAS
	//______________________________________________________________________________________________
	private static IListaItemClick _refresh;
		public static void setRefreshCallback(IListaItemClick refresh){_refresh = refresh;}
		public static void refreshListaRutas() { if(_refresh!=null)_refresh.onRefreshListaRutas(); }

	//______________________________________________________________________________________________
	// LOCATION
	//______________________________________________________________________________________________
	private static Location _locLast;
	public void setLocation(Location loc) {
		//Log.e(TAG, "setLocation:-------------------"+loc);
		_locLast=loc;
	}
	public Location getLocation()
	{
		Location location1=null, location2=null;
		try
		{
			if(_lm == null)return _locLast;
			boolean isGPSEnabled = _lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = _lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			try
			{
				if(isNetworkEnabled) location1 = _lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}catch(SecurityException se){Log.e(TAG, "Network Loc:e:---------------------------------",se);}
			try
			{
				if(isGPSEnabled)location2 = _lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}catch(SecurityException se){Log.e(TAG, "GPS Loc:e:-------------------------------------",se);}

			if(location1==null && location2==null)return _locLast;
			if(_locLast == null)_locLast = location1!=null?location1:location2;
			if(location1 != null && location1.getTime() > _locLast.getTime())
				_locLast = location1;
			else if(location2 != null && location2.getTime() > _locLast.getTime())
				_locLast = location2;
		}
		catch(SecurityException se)
		{
			se.printStackTrace();
		}
		return _locLast;
    }


	//______________________________________________________________________________________________
	// NOTIFICATION UTILS
	//______________________________________________________________________________________________
	private static void vibrate(Context context)
	{
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		//long pattern[]={0,200,100,300,400};//pattern for vibration (mili seg ?)
		// 0 = start vibration with repeated count, use -1 if you don't want to repeat the vibration
		//vibrator.vibrate(pattern, -1);
		if(vibrator != null)vibrator.vibrate(1000);//1seg
		//vibrator.cancel();
    }

	//______________________________________________________________________________________________
	// NOTIFICATION
	//______________________________________________________________________________________________
	private void showAviso(String sTitulo, Aviso aviso, Intent intent)
	{
		showNotificacion(sTitulo, aviso, intent);
	}
	//______________________________________________________________________________________________
	//private static int _idNotificacion = 1;
	private void showNotificacion(String titulo, Aviso aviso, Intent intent)
	{
		String sSound = _sp.getString("notifications_new_message_ringtone", "");
		Boolean bVibrate = _sp.getBoolean("notifications_new_message_vibrate", false);
		Boolean bLights = _sp.getBoolean("notifications_new_message_lights", false);
		//android.media.Ringtone ring = RingtoneManager.getRingtone(c, Uri.parse("content://media/internal/audio/media/122"));//.play();

		PowerManager.WakeLock wakeLock = _pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wakeLock.acquire(2000);

		Integer idNotificacion = _conversor(aviso.getId());
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(_app, _app.getString(R.string.app_name))
				.setSmallIcon(android.R.drawable.ic_menu_mylocation)//R.mipmap.ic_launcher)
				.setLargeIcon(android.graphics.BitmapFactory.decodeResource(_app.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(titulo)
				.setContentText(aviso.getNombre()+":"+aviso.getDescripcion())
				.setContentIntent(PendingIntent.getActivity(_app, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
				.setAutoCancel(true)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				;
		if( ! sSound.isEmpty())		notificationBuilder.setSound(Uri.parse(sSound));
		else						notificationBuilder.setSound(null);

		if(bLights) {
			notificationBuilder
					//.setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.FLAG_SHOW_LIGHTS)
					.setLights(0xff00ff00, 300, 100);//android.graphics.Color.RED
			notificationBuilder.setLights(0xff, 0, 0);
		}
		//else notificationBuilder.setLights(0, 0, 0);

		if(bVibrate)				//notificationBuilder.setVibrate(new long[]{1000L});//TODO: no funciona, llamar directamente a vibrar
			vibrate(_app);
		else
			notificationBuilder.setVibrate(null);

		_nm.notify(idNotificacion, notificationBuilder.build());
		wakeLock.release();
	}
	//______________________________________________________________________________________________
	private static int _conversor(String s)
	{
		// PARA Backendless: Integer.parseInt(a.getId().substring(0,6).replace('-','0'), 16);
		StringBuilder sb = new StringBuilder(10);
		s = s.replace("-", "");
		for(int i=0; i < 9; i++)
			sb.append(s.charAt(i) % 10);
		return Integer.valueOf(sb.toString().substring(0, 9));
	}

	private long _lastShowNotifGPS = 0;
	private long _delayShowNotifGPS = 0;
	public void showNotifGPS()
	{
		if(_delayShowNotifGPS < 60*60*1000)
			_delayShowNotifGPS += 5*60*1000;
		if(_lastShowNotifGPS + _delayShowNotifGPS > System.currentTimeMillis())return;
		_lastShowNotifGPS = System.currentTimeMillis();

		String sSound = _sp.getString("notifications_new_message_ringtone", "");
		Boolean bVibrate = _sp.getBoolean("notifications_new_message_vibrate", false);
		Boolean bLights = _sp.getBoolean("notifications_new_message_lights", false);

		PowerManager.WakeLock wakeLock = _pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wakeLock.acquire(2000);

		Intent intent = new Intent(_app, ActMain.class);
		int idNotificacion = 6969;

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(_app, _app.getString(R.string.app_name))
				.setSmallIcon(android.R.drawable.ic_menu_compass)
				.setLargeIcon(BitmapFactory.decodeResource(_app.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(_app.getString(R.string.ask_to_enable_gps))
				.setContentText("GPS")
				.setContentIntent(PendingIntent.getActivity(_app, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
				.setAutoCancel(true)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				;
		if( ! sSound.isEmpty())		notificationBuilder.setSound(Uri.parse(sSound));
		else						notificationBuilder.setSound(null);

		if(bLights) {
			notificationBuilder
					.setLights(0xff00ff00, 300, 100);//android.graphics.Color.RED
			notificationBuilder.setLights(0xff, 0, 0);
		}

		if(bVibrate)
			vibrate(_app);
		else
			notificationBuilder.setVibrate(null);

		_nm.notify(6969, notificationBuilder.build());
		wakeLock.release();
	}


	//______________________________________________________________________________________________
	/// CONFIG
	//______________________________________________________________________________________________
	public boolean isAutoArranque()
	{
		return _sp.getBoolean("is_auto_arranque", true);
	}

	//______________________________________________________________________________________________
	//private static final String PREF_TRACKING = "tracking_prefs";
	private static final String ID_TRACKING = "id_tracking_route";
	public void setTrackingRoute(String sIdRoute)
	{
		SharedPreferences.Editor editor = _sp.edit();
		editor.putString(ID_TRACKING, sIdRoute);
		editor.apply();//editor.commit(); Apply does it in background
	}
	public String getTrackingRoute()
	{
 		return _sp.getString(ID_TRACKING, "");
	}

	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	//______________________________________________________________________________________________
	public void return2Main(Activity act, boolean bDirty, String sMensaje)
	{
		Intent intent = new Intent();
		intent.putExtra(Constantes.DIRTY, bDirty);
		intent.putExtra(Constantes.MENSAJE, sMensaje);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	public void return2Main(Activity act, Filtro filtro)
	{
		Intent intent = new Intent();
		intent.putExtra(Constantes.DIRTY, true);
		intent.putExtra(Filtro.FILTRO, filtro);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	public void openMain(Activity act, boolean bDirty, String sMensaje, int pagina)
	{
		Intent intent = new Intent(act, ActMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Constantes.WIN_TAB, pagina);//Go to specific section (ActMain.AVISOS...)
		intent.putExtra(Constantes.DIRTY, bDirty);
		intent.putExtra(Constantes.MENSAJE, sMensaje);
		act.startActivity(intent);//Para cuando abres la pantalla desde una notificacion...
		act.finish();
	}

	//______________________________________________________________________________________________
	public void showAvisoGeo(String sId)
	{
		Aviso.getById(sId, new Fire.SimpleListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aData)
			{
				Intent i = new Intent(_app, ActAviso.class);//CesServiceAvisoGeo.this
				i.putExtra(Objeto.NOMBRE, aData[0]);
				showAviso(_app.getString(R.string.en_zona_aviso), aData[0], i);//CesServiceAvisoGeo.this
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("Util:showAvisoGeo:e:--------------------------------------%s",err));
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	@SuppressLint("DefaultLocale")
	public String formatDiffTimes(DateTime timeIni, DateTime timeEnd) {
		Interval interval = new Interval(timeIni, timeEnd);
		Period period = interval.toPeriod();
		StringBuilder sb = new StringBuilder();
		if(period.getMonths() > 0)
			sb.append(String.format(" %dM", period.getMonths()));
		if(period.getDays() > 0)
			sb.append(String.format(" %dd", period.getDays()));
		if(sb.length() > 0 || period.getHours() > 0)
			sb.append(String.format(" %dh", period.getHours()));
		if(sb.length() > 0 || period.getMinutes() > 0)
			sb.append(String.format(" %dm", period.getMinutes()));
		//if(sb.length() > 0 || period.getSeconds() > 0)
			sb.append(String.format(" %ds", period.getSeconds()));
		return sb.toString();
	}
	//private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()); // HH for 0-23
	//---
	private SimpleDateFormat timeFormatter = null;
	public String formatTiempo(long t)
	{
		if(timeFormatter == null)
		{
			timeFormatter = new SimpleDateFormat("HH'h' mm'm' ss's'", Locale.getDefault()); // HH for 0-23
			//timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return timeFormatter.format(new Date(t));//cal.getTime());
	}
	//---
	private SimpleDateFormat dateFormatter = null;
	public String formatFecha(Date date)
	{
		if(date == null)return "";
		if(dateFormatter == null)
		{
			dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
			//dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return dateFormatter.format(date);
	}
	//---
	private SimpleDateFormat dateTimeFormatter = null;
	public String formatFechaTiempo(Date date)
	{
		if(date == null)return "";
		if(dateTimeFormatter == null)
		{
			dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			//dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return dateTimeFormatter.format(date);
	}

	//----------------------------------------------------------------------------------------------
	public void onBuscar(final Context c, final GoogleMap map, final float zoom)
	{
		View viewBuscarCalle = View.inflate(c, R.layout.dialog_buscar_calle, null);
		final EditText direccion = viewBuscarCalle.findViewById(R.id.direccion);
		AlertDialog dlg = new AlertDialog.Builder(c).create();
		dlg.setView(viewBuscarCalle);
		dlg.setCancelable(true);
		dlg.setButton(AlertDialog.BUTTON_NEGATIVE, c.getString(R.string.cancelar), (dialog, which) -> { });
		dlg.setButton(AlertDialog.BUTTON_POSITIVE, c.getString(R.string.buscar), (dialog, which) ->
		{
			Geocoder geocoder = new Geocoder(c);
			List<Address> addresses;
			try
			{
				addresses = geocoder.getFromLocationName(direccion.getText().toString(), 1);
				if(addresses.size() > 0)
				{
					double lat = addresses.get(0).getLatitude();
					double lng = addresses.get(0).getLongitude();
					// Callback(new LatLng(lat, lng)) en lugar de:
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
				}
			}
			catch(Exception e){Log.e(TAG, "onBucar:e:-------------------------------------------", e);}
		});
		dlg.show();
	}

	//______________________________________________________________________________________________
	//TODO: static vs injected ?!
	public static void exeDelayed(long delay, Runnable runnable) {
		new Handler().postDelayed(runnable, delay);
	}

	//______________________________________________________________________________________________
	public boolean pideActivarGPS(Context context) {
		final LocationManager manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if(manager != null && ! manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.ask_to_enable_gps)
					.setCancelable(false)
					.setPositiveButton(R.string.ok, (dialog, id) ->
							context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
					.setNegativeButton(R.string.cancelar, (dialog, id) ->dialog.cancel())
				;
			final AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return false;
	}
	//______________________________________________________________________________________________
	/*public void pidePermisosGPS(Context settingsClient, Activity act, LocationRequest locationRequest) {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest)
				.setAlwaysShow(true);
		Task<LocationSettingsResponse> task =
				LocationServices.getSettingsClient(settingsClient).checkLocationSettings(builder.build());
		Log.e(TAG, "pidePermisosGPS:-------------------AAA-----------------");
		task.addOnCompleteListener(res -> {
            try {
                //LocationSettingsResponse response =
				res.getResult(ApiException.class);
				Log.e(TAG, "pidePermisosGPS:-------------------BBB-----------------");
                // All location settings are satisfied. The client can initialize location requests here.
			}
			catch(ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
							if(act != null) {
								resolvable.startResolutionForResult(act, 1000);
								Log.e(TAG, "pidePermisosGPS:resolvable.startResolutionForResult-------------------------------------");
							}
							else {
								showNotifGPS();
								Log.e(TAG, "pidePermisosGPS:showNotifGPS-------------------------------------");
							}
                        }
                        catch(IntentSender.SendIntentException | ClassCastException e) {
                            // Ignore the error.
							Log.e(TAG, "pidePermisosGPS:e:-------------------------------------",e);
                        }
						break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    	Log.e(TAG, "pidePermisosGPS: LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
		Log.e(TAG, "pidePermisosGPS:-------------------CCC-----------------");
	}*/


	//______________________________________________________________________________________________
	public String getActivityString(int detectedActivityType) {
		Resources resources = _app.getResources();
		switch(detectedActivityType) {
			case DetectedActivity.IN_VEHICLE:
				return resources.getString(R.string.in_vehicle);
			case DetectedActivity.ON_BICYCLE:
				return resources.getString(R.string.on_bicycle);
			case DetectedActivity.ON_FOOT:
				return resources.getString(R.string.on_foot);
			case DetectedActivity.RUNNING:
				return resources.getString(R.string.running);
			case DetectedActivity.STILL:
				return resources.getString(R.string.still);
			case DetectedActivity.TILTING:
				return resources.getString(R.string.tilting);
			case DetectedActivity.UNKNOWN:
				return resources.getString(R.string.unknown);
			case DetectedActivity.WALKING:
				return resources.getString(R.string.walking);
			default:
				return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
		}
	}
}
