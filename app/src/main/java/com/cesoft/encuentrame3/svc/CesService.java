package com.cesoft.encuentrame3.svc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Locale;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.google.firebase.auth.FirebaseUser;			//TODO: todo la logica BBDD a clase Fire

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Ruta;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cesoft.encuentrame3.util.Constantes.ACCURACY_MAX;
import static com.cesoft.encuentrame3.util.Constantes.ACCEL_MAX;
import static com.cesoft.encuentrame3.util.Constantes.DELAY_LOAD;
import static com.cesoft.encuentrame3.util.Constantes.DELAY_TRACK_MAX;
import static com.cesoft.encuentrame3.util.Constantes.DELAY_TRACK_MIN;
import static com.cesoft.encuentrame3.util.Constantes.DISTANCE_MAX;
import static com.cesoft.encuentrame3.util.Constantes.DISTANCE_MIN;
import static com.cesoft.encuentrame3.util.Constantes.GEOFEN_DWELL_TIME;
import static com.cesoft.encuentrame3.util.Constantes.SPEED_MAX;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO
//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
@Singleton
public class CesService extends IntentService
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
	private static final String TAG = CesService.class.getSimpleName();
	//private static final int RADIO_TRACKING = 10;//El radio es el nuevo periodo, config al crear NUEVA ruta...
	private static long DELAY_TRACK = DELAY_TRACK_MIN;
		public static void setMinTrackingDelay(){DELAY_TRACK = DELAY_TRACK_MIN;Log.e(TAG, "****** "+DELAY_TRACK);}
		public static void setMaxTrackingDelay(){DELAY_TRACK = DELAY_TRACK_MAX;Log.e(TAG, "****** "+DELAY_TRACK);}

	@Inject	Util _util;
	@Inject	Login _login;

	private CesGeofenceStore _GeofenceStoreAvisos;
	private ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();

	//----------------------------------------------------------------------------------------------
	private static CesService INSTANCE = null;
		private boolean _bRun = true;
		public static void stop()
		{
			Log.e(TAG, "*********** STOP ***********"+INSTANCE);
			if(INSTANCE != null)
			{
				INSTANCE._bRun=false;
				INSTANCE.stopSelf();
				if(INSTANCE._GeofenceStoreAvisos != null)
					INSTANCE._GeofenceStoreAvisos.clear();
				INSTANCE._GeofenceStoreAvisos = null;
				INSTANCE = null;
			}
		}
		public static void start(Context c)
		{
			if(INSTANCE == null)
				c.startService(new Intent(c, CesService.class));
		}

	//______________________________________________________________________________________________
	@Inject public CesService()
	{
		super("EncuentrameSvc");
		INSTANCE = this;
	}
	@Override
	public void onCreate()
	{
		Log.e(TAG, "******************************** on create *************************************");
		super.onCreate();

		DELAY_TRACK = DELAY_TRACK_MIN;
		createListAviso();
		//_util = ((App)getApplication()).getGlobalComponent().util();
		App.getComponent(getApplicationContext()).inject(this);
		_login.login(new Fire.AuthListener()
		{
			@Override
			public void onExito(FirebaseUser usr)
			{
				Log.w(TAG, "onCreate:login:exito:--------------------------------------------------");
			}
			@Override
			public void onFallo(Exception e)
			{
				Log.e(TAG, "onCreate:login:e:-------------------------------------------------------", e);
				CesService.stop();
			}
		});
		iniGeoTracking();
		//Ruta.RutaPunto.eliminar(null);
	}

	//______________________________________________________________________________________________
	//private Thread _hiloLoop = null;
	long _tmTrack = System.currentTimeMillis() - 2*DELAY_TRACK;
	//______________________________________________________________________________________________
	@Override
	protected void onHandleIntent(Intent workIntent)
	{
		//_hiloLoop = Thread.currentThread();
		try
		{
			long tmLoad = System.currentTimeMillis() - 2* DELAY_LOAD;
			//long tmTrack = System.currentTimeMillis() - 2*DELAY_TRACK;
			//noinspection InfiniteLoopStatement
			while(_bRun)//No hay un sistema para listen y not polling??????
			{
Log.w(TAG, String.format(Locale.ENGLISH, "CesService:loop---------------------DELAY_TRACK=%d------------------------%s", DELAY_TRACK/1000, java.text.DateFormat.getDateTimeInstance().format(new java.util.Date())));
				if( ! _login.isLogged())
				{
					_login.login(new Fire.AuthListener()
					{
						@Override
						public void onExito(FirebaseUser usr)
						{
							//Log.w(TAG, String.format("loop----------------------------------------Login OK:%s",usr));
						}
						@Override
						public void onFallo(Exception e)
						{
							Log.e(TAG, String.format("loop------------------------------------------Login kk:%s",e), e);
						}
					});
					try{Thread.sleep(DELAY_TRACK_MIN);}catch(InterruptedException ignored){}
					continue;
				}
				if(tmLoad + DELAY_LOAD < System.currentTimeMillis())
				{
					cargarListaGeoAvisos();
					tmLoad = System.currentTimeMillis();
				}
				else if(_tmTrack + DELAY_TRACK < System.currentTimeMillis()
					|| (_locLastSaved != null && _util.getLocation().distanceTo(_locLastSaved) > DISTANCE_MAX))
				{
					WidgetRutaService.startSvc(getApplicationContext());
					saveGeoTracking();
					_tmTrack = System.currentTimeMillis();
				}
//Log.w(TAG, "LOOP before sleeping------------------------"+(DELAY_TRACK/4000));
				try{Thread.sleep(DELAY_TRACK/2);}
				catch(InterruptedException e){Log.e(TAG, "---------------- LOOP SLEEP INTERRUPTED ---------------");}
			}
		}
		catch(Exception e){Log.e(TAG, "onHandleIntent:e:--------------------------------------------", e);}
		CesWakefulReceiver.completeWakefulIntent(workIntent);
		Log.e(TAG, "---------------- LOOP END ---------------");
	}

	//______________________________________________________________________________________________
	public void _restartDelayRuta()
	{
		DELAY_TRACK = DELAY_TRACK_MIN;
Log.e(TAG, "_restartDelayRuta:------------------------------"+DELAY_TRACK);
		saveGeoTracking();
		_tmTrack = System.currentTimeMillis();
		//if(_hiloLoop!=null)_hiloLoop.interrupt();Doesnt work
	}

	//______________________________________________________________________________________________
	private Fire.DatosListener<Aviso> _lisAviso;
	private Fire.DatosListener<Aviso> createListAviso()
	{
		final Context context = getApplicationContext();
		_lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aData)
			{
				//TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
				boolean bDirty = false;
				long n = aData.length;
				if(n != CesService.this._listaGeoAvisos.size())
				{
					if(_GeofenceStoreAvisos != null)_GeofenceStoreAvisos.clear();
					_listaGeoAvisos.clear();
					bDirty = true;
				}
				ArrayList<Geofence> aGeofences = new ArrayList<>();
				ArrayList<Aviso> aAvisos = new ArrayList<>();
				for(int i=0; i < aData.length; i++)
				{
					Aviso a = aData[i];
					aAvisos.add(a);
					Geofence gf = new Geofence.Builder().setRequestId(a.getId())
							.setCircularRegion(a.getLatitud(), a.getLongitud(), (float)a.getRadio())
							.setExpirationDuration(Geofence.NEVER_EXPIRE)
							.setLoiteringDelay(GEOFEN_DWELL_TIME)// Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
							.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT).build();
					aGeofences.add(gf);
					if( ! bDirty)
					{
						if(_listaGeoAvisos.size() < i)
							bDirty = true;
						else if( ! _listaGeoAvisos.contains(a))//else if(_listaGeoAvisos.get(i))
							bDirty = true;
						i++;
					}
				}
				if(bDirty)
				{
					_listaGeoAvisos = aAvisos;
					_GeofenceStoreAvisos = new CesGeofenceStore(aGeofences, context);//Se puede añadir en lugar de crear desde cero?
				}
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------"+err);
			}
		};
		return _lisAviso;
	}
	//______________________________________________________________________________________________
	public void cargarListaGeoAvisos()
	{
		try
		{
			Aviso.getActivos(_lisAviso);
		}
		catch(Exception e)
		{
			Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------", e);
			//_lista.clear();
		}
	}

	//______________________________________________________________________________________________
	private Location _locLastSaved = null;
	private String _sId = "";
	public void saveGeoTracking()
	{
		final String sId = _util.getTrackingRoute();
Log.e(TAG, "saveGeoTracking ******************************************* "+sId);
		if(sId.isEmpty())
		{
			stopTracking();
			return;
		}
		startTracking();

		Ruta.getById(sId, new Fire.SimpleListener<Ruta>()
		{
			@Override
			public void onDatos(Ruta[] aData)
			{
				if(aData[0] == null)
				{
					Log.e(TAG, "saveGeoTracking:Ruta.getById: RUTA == NULL -------------------------------------------"+sId);
					_util.setTrackingRoute("");
					stopTracking();
				}
				else
				{
					final Location loc = _util.getLocation();
					guardarPunto(loc, aData[0], sId);
				}
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "saveGeoTracking:findById:e:---------------------------------------------:" + err);
			}
		});
	}

	/*public void handleResponse(Ruta r, String sId)
	{
		if(r == null)return;
		final Location loc = _util.getLocation();
		guardarPunto(loc, r, sId);
	}*/

	//----------------------------------------------------------------------------------------------
	//TODO: puntos mas cercanos dependiendo de si tienes wifi? opcion de no guardar hasta tener wifi?
	private Location _locLast = null;
	private double _velLast = 0;
	private synchronized void guardarPunto(Location loc, Ruta r, String sId)
	{
Log.w(TAG, "guardarPunto:    A    *** ");
		if(loc == null)
		{
			Log.w(TAG, "guardarPunto:loc==NULL------------------------------------------------------");
			return;
		}
		if( ! loc.hasAccuracy())
		{
			Log.w(TAG, "guardarPunto:loc.hasAccuracy()==FALSE---------------------------------------");
			return;
		}
		if(r.getPuntosCount() > 0 && (loc.getAccuracy() > ACCURACY_MAX || loc.getAccuracy() > 15+ DISTANCE_MIN))
		{
			Log.w(TAG, "guardarPunto:loc.getAccuracy() ("+loc.getAccuracy()+")   > MAX_ACCURACY ("+ ACCURACY_MAX+")  or  > DISTANCE_MIN+15 ("+ DISTANCE_MIN+")    :::: n pts "+r.getPuntosCount());
			return;
		}

		if( ! _sId.equals(sId))//TODO: if sId exist in bbdd, not new route, _locLastSaved = last loc in bbdd ==> Too much monkey business
		{
			Log.w(TAG, "guardarPunto: NUEVA RUTA: -----------------"+(_sId.equals(sId))+"-------------------------- "+_sId+" ------- "+sId);//TODO : se llama dos veces?????!!!!!
			_sId = sId;
			_locLastSaved = null;
			_locLast = loc;
			DELAY_TRACK = DELAY_TRACK_MIN;
		}
		else if(_locLastSaved != null)
		{
			//---
			//Determinar probabilidad de punto erroneo (velocidad actual, velocidad pasada, distancia al anterior punto, tiempo transcurrido)
			if(_locLast != null)
			{
				try
				{
					float distLast = loc.distanceTo(_locLast);
					long t0 = _locLast.getTime();//if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1).getElapsedRealtimeNanos()/1000000000;
					long t1 = loc.getTime();

					double time = (t1-t0)/1000;//s
					double speed = distLast / time;	//60 m/s = 216 Km/h
					double a = (speed - _velLast)/time;//Aceleración coche muy potente (desde parado!) < 8 m/s2

Log.w(TAG, String.format(Locale.ENGLISH, "guardarPunto:-------*************----TIME(s)= %.0f  VEL(m/s)= %.2f  LAST VEL=%.2f  A(m/s2)= %.2f", time, speed, _velLast, a));
					//if(speed > 40 && _velLastSaved < 20 && time < 2*60)//|| speed > (_velLastSaved+1)*50)//50m/s = 180Km/h
					if(speed > SPEED_MAX || a > ACCEL_MAX)//imaginamos que es un punto erróneo, salvo que vayas en un cohete
					{
						Log.e(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto erróneo:   VEL=%.2f m/s  LAST VEL=%.2f  T=%.0f  a=%.2f *****************************", speed, _velLast, time, a));
						DELAY_TRACK = DELAY_TRACK_MIN;
						return;
					}
					_velLast = speed;
				}
				catch(Exception e){Log.e(TAG, "guardarPunto:e:-----------------------------------------",e);}
			}
			_locLast = loc;
			//---

			float distLastSaved = loc.distanceTo(_locLastSaved);
Log.w(TAG, "guardarPunto:----------------************************---------------------distLastSaved="+distLastSaved+"  acc="+loc.getAccuracy());
			if(distLastSaved < DISTANCE_MIN)//Puntos muy cercanos
			{
				Log.w(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto repetido o sin precision: %s   dist=%.1f  acc=%.1f", sId, distLastSaved, loc.getAccuracy()));
				DELAY_TRACK += 2*1000;
				if(DELAY_TRACK > DELAY_TRACK_MAX) DELAY_TRACK = DELAY_TRACK_MAX;
				return;
			}
			else if(distLastSaved > 10*DISTANCE_MIN)
				DELAY_TRACK = DELAY_TRACK_MIN;
			else if(distLastSaved > 5*DISTANCE_MIN)//TODO: Mejorar...
				DELAY_TRACK -= DELAY_TRACK_MIN;
			if(DELAY_TRACK < DELAY_TRACK_MIN) DELAY_TRACK = DELAY_TRACK_MIN;
		}
		r.guardar(new GuardarListener(loc));
		_locLastSaved = loc;
Log.w(TAG, "guardarPunto:    B    *** ");
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	private class GuardarListener extends Fire.CompletadoListener
	{
		private Location _loc = null;
		GuardarListener(Location loc){_loc = loc;}

		@Override
		protected void onDatos(String id)
		{
Log.w(TAG, "GuardarListener:onComplete:----------------------:" + id);
			Ruta.addPunto(id, _loc.getLatitude(), _loc.getLongitude(),
				_loc.getAccuracy(), _loc.getAltitude(), _loc.getSpeed(), _loc.getBearing(),
				new Fire.SimpleListener<Long>()
				{
					@Override
					public void onDatos(Long[] puntos)
					{
						Log.w(TAG, String.format(Locale.ENGLISH, "GuardarListener:addPunto: n ptos: %d", puntos[0]));
						Util.refreshListaRutas();//Refrescar lista rutas en main..
					}
					@Override
					public void onError(String err)
					{
						Log.e(TAG, String.format("GuardarListener:addPunto:e:--------------------------------------:%s",err));
					}
				});
		}
		@Override
		protected void onError(String err, int code)
		{
			Log.e(TAG, String.format(Locale.ENGLISH, "saveGeoTracking:guardar:err:-------------------------:%s : %d",err, code));
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////


	//https://developer.android.com/training/location/change-location-settings.html
	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;
	public void iniGeoTracking()
	{
		if(checkPlayServices())buildGoogleApiClient();
		if(_GoogleApiClient != null)_GoogleApiClient.connect();
	    _LocationRequest = new LocationRequest();
	    _LocationRequest.setInterval(DELAY_TRACK_MIN);//TODO: ajustar por usuario...
	    _LocationRequest.setFastestInterval(DELAY_TRACK_MIN);
	    _LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		pideGPS();
	}
	//______________________________________________________________________________________________
	protected synchronized void buildGoogleApiClient()
	{
		_GoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}
	private boolean checkPlayServices()
	{
    	GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    	int result = googleAPI.isGooglePlayServicesAvailable(this);
    	if(result != ConnectionResult.SUCCESS)
		{
			Log.e(TAG, String.format("checkPlayServices: No tiene Google Services? result = %s !!!!!!!!!!!!!!!",result));
	        return false;
	    }
	    return true;
	}


	//______________________________________________________________________________________________
	private void startTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
		{
			try
			{
				if(_LocationRequest == null)
				{
					iniGeoTracking();
					throw new SecurityException("_LocationRequest = NULL");
				}
				LocationServices.FusedLocationApi.requestLocationUpdates(_GoogleApiClient, _LocationRequest, this);
			}
			catch(SecurityException e)
			{
				Log.e(TAG, "startTracking:e:--------------------------------------------------------", e);
			}
		}
	}
	private void stopTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
			LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this);
		DELAY_TRACK = DELAY_TRACK_MAX;
Log.e(TAG, "stopTracking:---------------------------------------------------------------------------");
		_locLastSaved = null;
		_locLast = null;
		_velLast = 0;
	}


	///------------------------ CALLBACKS ----------------------------------------------------------
	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		Log.w(TAG, "onConnected");
	}
	@Override
	public void onConnectionSuspended(int i)
	{
		//Log.w(TAG, "onConnectionSuspended:"+i);
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}
	@Override
	public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
		Log.w(TAG, "----------------onLocationChanged:::"+location.getAccuracy()+"--"+location.getProvider()+"--"+(new java.util.Date(location.getTime())));
	}
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
		Log.w(TAG, String.format("onConnectionFailed:e:%s", connectionResult.getErrorCode()));
	}

	//----------------------------------------------------------------------------------------------
	private void pideGPS()
	{
		//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(_LocationRequest)
				.setAlwaysShow(true)//so it ask for GPS activation like google maps
				;
		if(_GoogleApiClient == null)
		{
			Log.e(TAG, "pideGPS:e:------------------------------------------------------------------ GoogleApiClient == null");
			return;
		}
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(_GoogleApiClient, builder.build());
		result.setResultCallback(result1 ->
		{
			final Status status = result1.getStatus();
			switch(status.getStatusCode())
			{
			case LocationSettingsStatusCodes.SUCCESS:
				Log.w(TAG, "LocationSettingsStatusCodes.SUCCESS");
				break;
			case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
				try{status.startResolutionForResult(null, 1000);}catch(Exception ignored){}//catch(android.content.IntentSender.SendIntentException ignored){}
				break;
			case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
				Log.w(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
				break;
			}
		});
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
