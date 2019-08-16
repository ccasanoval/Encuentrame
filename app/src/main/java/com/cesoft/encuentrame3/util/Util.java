package com.cesoft.encuentrame3.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.adapters.IListaItemClick;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Objeto;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
@Singleton
public class Util
{
	private static final String TAG = Util.class.getSimpleName();
	public static final String TIPO = "tipo";

	//______________________________________________________________________________________________
	private Application app;
	private Preferencias pref;
	private LocationManager lm;
	private NotificationManager nm;
	private PowerManager pm;
	@Inject
	public Util(Application app, Preferencias pref, LocationManager lm, NotificationManager nm, PowerManager pm)
	{
		this.app = app;
		this.pref = pref;
		this.lm = lm;
		this.nm = nm;
		this.pm = pm;
	}
	//______________________________________________________________________________________________
	// REFRESH LISTA RUTAS
	//______________________________________________________________________________________________
	private IListaItemClick refresh;
		public void setRefreshCallback(IListaItemClick refresh) { this.refresh = refresh; }
		public void refreshListaRutas() { if(refresh !=null) refresh.onRefreshListaRutas(); }

	//______________________________________________________________________________________________
	// LOCATION
	//______________________________________________________________________________________________
	private Location locLast;
	public void setLocation(Location loc) {
		locLast = loc;
	}
	public Location getLocation()
	{
		Location location1=null;
		Location location2=null;
		try
		{
			if(lm == null)return locLast;
			boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			try
			{
				if(isNetworkEnabled) location1 = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			catch(SecurityException se){Log.e(TAG, "Network Loc:e:---------------------------------",se);}
			try
			{
				if(isGPSEnabled)location2 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			catch(SecurityException se){Log.e(TAG, "GPS Loc:e:-------------------------------------",se);}

			if(location1==null && location2==null)return locLast;
			if(locLast == null) locLast = location1!=null?location1:location2;
			if(location1 != null && location1.getTime() > locLast.getTime())
				locLast = location1;
			else if(location2 != null && location2.getTime() > locLast.getTime())
				locLast = location2;
		}
		catch(SecurityException se)
		{
			Log.e(TAG, "getLocation:e:----------------------------------------------------",se);
		}
		return locLast;
    }


	//______________________________________________________________________________________________
	// NOTIFICATION UTILS
	//______________________________________________________________________________________________
	private void vibrate()
	{
        Vibrator vibrator = (Vibrator)app.getSystemService(Context.VIBRATOR_SERVICE);
		//long pattern[]={0,200,100,300,400};//pattern for vibration (mili seg ?)
		// 0 = start vibration with repeated count, use -1 if you don't want to repeat the vibration
		if(vibrator == null) return;
			//vibrator.vibrate(1000);//1seg
		if (Build.VERSION.SDK_INT >= 26) {
			vibrator.vibrate(android.os.VibrationEffect.createOneShot(150,10));
		} else {
			vibrator.vibrate(150);
		}
    }

	//______________________________________________________________________________________________
	// NOTIFICATION
	//______________________________________________________________________________________________
	private void showAviso(String sTitulo, Aviso aviso, Intent intent)
	{
		showNotificacion(sTitulo, aviso, intent);
	}
	//______________________________________________________________________________________________
	private void showNotificacion(String titulo, Aviso aviso, Intent intent)
	{
		String sSound = pref.getNotificationRingtone();
		boolean bVibrate = pref.isNotificationVibrate();
		boolean bLights = pref.isNotificationLights();

		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CESoft:Encuentrame:Util");
		wakeLock.acquire(2000);

		int idNotificacion = conversor(aviso.getId());
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(app, app.getString(R.string.app_name))
				.setSmallIcon(android.R.drawable.ic_menu_mylocation)//R.mipmap.ic_launcher)
				.setLargeIcon(android.graphics.BitmapFactory.decodeResource(app.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(titulo)
				.setContentText(aviso.getNombre()+":"+aviso.getDescripcion())
				.setContentIntent(PendingIntent.getActivity(app, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
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

		//notificationBuilder.setVibrate(new long[]{1000L});//no funciona, llamar directamente a vibrar
		if(bVibrate)
			vibrate();
		else
			notificationBuilder.setVibrate(null);

		nm.notify(idNotificacion, notificationBuilder.build());
		wakeLock.release();
	}
	//______________________________________________________________________________________________
	private int conversor(String s)
	{
		StringBuilder sb = new StringBuilder(10);
		s = s.replace("-", "");
		for(int i=0; i < 9; i++)
			sb.append(s.charAt(i) % 10);
		return Integer.valueOf(sb.toString().substring(0, 9));
	}

	private long lastShowNotifGPS = 0;
	private long delayShowNotifGPS = 0;
	public void showNotifGPS()
	{
		if(delayShowNotifGPS < 60*60*1000)
			delayShowNotifGPS += 5*60*1000;
		if(lastShowNotifGPS + delayShowNotifGPS > System.currentTimeMillis())return;
		lastShowNotifGPS = System.currentTimeMillis();

		String sSound = pref.getNotificationRingtone();
		boolean bVibrate = pref.isNotificationVibrate();
		boolean bLights = pref.isNotificationLights();

		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CESoft:Encuentrame:Util");
		wakeLock.acquire(2000);

		Intent intent = new Intent(app, ActMain.class);
		int idNotificacion = 6900;

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(app, app.getString(R.string.app_name))
				.setSmallIcon(android.R.drawable.ic_menu_compass)
				.setLargeIcon(BitmapFactory.decodeResource(app.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(app.getString(R.string.ask_to_enable_gps))
				.setContentText("GPS")
				.setContentIntent(PendingIntent.getActivity(app, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
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
			vibrate();
		else
			notificationBuilder.setVibrate(null);

		nm.notify(idNotificacion, notificationBuilder.build());
		wakeLock.release();
	}


	//______________________________________________________________________________________________
	public void setTrackingRoute(String idRoute) {
		pref.setTrackingRoute(idRoute);
	}
	public String getTrackingRoute() {
 		return pref.getTrackingRoute();
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
				Intent i = new Intent(app, ActAviso.class);//CesServiceAvisoGeo.this
				i.putExtra(Objeto.NOMBRE, aData[0]);
				showAviso(app.getString(R.string.en_zona_aviso), aData[0], i);//CesServiceAvisoGeo.this
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
		return timeFormatter.format(new Date(t));
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
	public void onBuscar(final Context context, final GoogleMap map, final float zoom)
	{
		View viewBuscarCalle = View.inflate(app, R.layout.dialog_buscar_calle, null);
		final EditText direccion = viewBuscarCalle.findViewById(R.id.direccion);
		AlertDialog dlg = new AlertDialog.Builder(context).create();
		dlg.setView(viewBuscarCalle);
		dlg.setCancelable(true);
		dlg.setButton(AlertDialog.BUTTON_NEGATIVE, app.getString(R.string.cancelar), (dialog, which) -> { });
		dlg.setButton(AlertDialog.BUTTON_POSITIVE, app.getString(R.string.buscar), (dialog, which) ->
		{
			Geocoder geocoder = new Geocoder(app);
			List<Address> addresses;
			try
			{
				addresses = geocoder.getFromLocationName(direccion.getText().toString(), 1);
				if( ! addresses.isEmpty())
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
	public void exeDelayed(long delay, Runnable runnable) {
		new Handler().postDelayed(runnable, delay);
	}



	@SuppressLint("BatteryLife")
	public boolean pideBateria(Context context) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(pm != null) {
				if(pm.isIgnoringBatteryOptimizations(app.getPackageName())) {
					Log.e(TAG, "pideBateria:isIgnoringBatteryOptimizations: TRUE");
					return false;
				}
				else {
					// Need this in manifest: <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					intent.setData(Uri.parse("package:"+app.getPackageName()));
					if(context == null) {
						intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
						app.startActivity(intent);
					}
					else {
						context.startActivity(intent);
					}
					// CHECK:
					//  turn off the screen and:
					//adb shell dumpsys battery unplug
					//adb shell dumpsys deviceidle step --> Till status =  IDLE_PENDING, SENSING, (LOCATING), IDLE_MAINTENANCE, IDLE
					Log.e(TAG, "pideBateria:isIgnoringBatteryOptimizations: FALSE");
					return true;
				}
			}
		}
		return false;
	}
	public boolean pideBateriaDeNuevoSiEsNecesario(Context context) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(pm != null && !pm.isIgnoringBatteryOptimizations(app.getPackageName())) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(R.string.ask_to_ignore_energy_saving_again)
						.setCancelable(false)
						.setPositiveButton(android.R.string.no, (dialog, id) -> pideBateria(context))
						.setNegativeButton(android.R.string.yes, (dialog, id) -> dialog.cancel())
				;
				final AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		}
		return false;
	}


	public boolean pideGPS(Activity act, int requestCode) {
		int permissionCheck = ContextCompat.checkSelfPermission(act, android.Manifest.permission.ACCESS_FINE_LOCATION);
		if(permissionCheck == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(
					act,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
					requestCode);
			return false;
		}
		else return true;
	}
	//______________________________________________________________________________________________
	public void pideActivarGPS(Activity act, int requestCode) {
		LocationRequest request = LocationRequest.create();
		LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
				.addLocationRequest(request)
				.build();
		LocationServices.getSettingsClient(act)
				.checkLocationSettings(settingsRequest)
				.addOnCompleteListener((Task<LocationSettingsResponse> task) -> {
					if( ! task.isSuccessful()) {
						Exception e = task.getException();
						if (e instanceof ResolvableApiException) {
							// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult()
							ResolvableApiException ex = (ResolvableApiException)e;
							try {
								ex.startResolutionForResult(act, requestCode);
							}
							catch(Exception ignore) { }
						}
					}
				});

		/*

		if(context != null && lm != null && ! lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.ask_to_enable_gps)
					.setCancelable(false)
					.setPositiveButton(R.string.ok, (dialog, id) ->
							app.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
					.setNegativeButton(R.string.cancelar, (dialog, id) ->dialog.cancel())
				;
			final AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return false;*/
	}

	//______________________________________________________________________________________________
	public String getActivityString(int detectedActivityType) {
		Resources resources = app.getResources();
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
