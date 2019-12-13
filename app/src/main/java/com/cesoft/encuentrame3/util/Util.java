package com.cesoft.encuentrame3.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.adapters.IListaItemClick;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.svc.ServiceNotifications;
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
	private final Application app;
	private final Preferencias pref;
	private final LocationManager lm;
	private final PowerManager pm;
	private final ServiceNotifications sn;
	@Inject
	public Util(Application app, Preferencias pref, LocationManager lm, PowerManager pm, ServiceNotifications sn)
	{
		this.app = app;
		this.pref = pref;
		this.lm = lm;
		this.pm = pm;
		this.sn = sn;
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
//	private void vibrate()
//	{
//        Vibrator vibrator = (Vibrator)app.getSystemService(Context.VIBRATOR_SERVICE);
//		//long pattern[]={0,200,100,300,400};//pattern for vibration (mili seg ?)
//		// 0 = start vibration with repeated count, use -1 if you don't want to repeat the vibration
//		if(vibrator == null) return;
//			//vibrator.vibrate(1000);//1seg
//		if (Build.VERSION.SDK_INT >= 26) {
//			vibrator.vibrate(android.os.VibrationEffect.createOneShot(150,10));
//		} else {
//			vibrator.vibrate(150);
//		}
//    }

	//______________________________________________________________________________________________
	public void setTrackingRoute(String idRoute, String nameRoute) {
		pref.setTrackingRoute(idRoute, nameRoute);
	}
	public String getIdTrackingRoute() {
 		return pref.getIdTrackingRoute();
	}
	public String getNameTrackingRoute() {
		return pref.getNameTrackingRoute();
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
Log.e(TAG, "return2Main-------------------------------------------- ");
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
				String titulo = app.getString(R.string.en_zona_aviso);
				Aviso aviso = aData[0];
				sn.createForAviso(titulo, aviso);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showAvisoGeo:e:--------------------------------------%s",err));
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
	//---
	private SimpleDateFormat dateFormatter = null;
	public String formatFecha(Date date)
	{
		if(date == null)return "";
		if(dateFormatter == null)
		{
			dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
			if(pm != null && pm.isIgnoringBatteryOptimizations(app.getPackageName())) {
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
		return false;
	}
	public boolean pideBateriaDeNuevoSiEsNecesario(Context context) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& pm != null && !pm.isIgnoringBatteryOptimizations(app.getPackageName())) {
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
		return false;
	}

	public boolean compruebaPermisosGPS(Activity act, int requestCode) {
		boolean isFineLocation = ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if(isFineLocation) {
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
				boolean isBackgroundLocation = ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
				if( ! isBackgroundLocation) {
					ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, requestCode);
					return false;
				}
			}
		}
		else {
			ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
			return false;
		}
		return true;
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

	//______________________________________________________________________________________________
	/*public boolean checkPlayServices()
	{
//ServiceNotifications.create(app, "Google Play Services", "----------------------------------------------");
		try {
			GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
			int result = googleAPI.isGooglePlayServicesAvailable(app);
			if (result != ConnectionResult.SUCCESS) {
				//TODO: Notificacion?
				ServiceNotifications.create(app, "Google Play Services", "Para usar esta aplicacion debe instalar Google Play Services");
				Log.e(TAG, String.format("checkPlayServices: No tiene Google Services? result = %s !!!!!!!!!!!!!!!", result));
				return false;
			}
			return true;
		}
		catch(Exception e) {
			Log.e(TAG, "checkPlayServices:e:---------------------------------------------------", e);
			return false;
		}
	}*/
}
