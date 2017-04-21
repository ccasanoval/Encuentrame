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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.google.firebase.auth.FirebaseUser;			//TODO: todo la logica BBDD a clase Fire
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Ruta;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO
//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//TODO
@Singleton
public class CesService extends IntentService
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
	private static final String TAG = CesService.class.getSimpleName();
	private static final int GEOFEN_DWELL_TIME = 60*1000;
	private static final long DELAY_TRACK_MIN = 20*1000;//30*1000;
	private static final long DELAY_TRACK_MAX = 2*60*1000;//7*60*1000;
	private static long DELAY_TRACK = DELAY_TRACK_MIN;
	private static final long ACCURACY_MAX = 24;//m
	private static final long DISTANCE_MIN = 10;//m
	private static final long DELAY_LOAD = DELAY_TRACK_MAX;
	//private static final int RADIO_TRACKING = 10;//El radio es el nuevo periodo, config al crear NUEVA ruta...

	@Inject	Util _util;
	@Inject	Login _login;

	private CesGeofenceStore _GeofenceStoreAvisos;
	private ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();

	//TODO: probar a crear una ruta cuando no tienes conexion... no sale de NuevaRutaAct y no muestra amarilla la ruta...

	//______________________________________________________________________________________________
	@Inject public CesService()
	{
		super("EncuentrameSvc");
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		//_util = ((App)getApplication()).getGlobalComponent().util();
		App.getInstance().getGlobalComponent().inject(this);
		_login.login(new Login.AuthListener()
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
				CesService.this.stopSelf();
			}
		});
		iniGeoTracking();
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
				SystemClock.elapsedRealtime() + 1000,
				restartServicePendingIntent);

		Log.e(TAG, "-------------------Reiniciando...---------------------");
		super.onTaskRemoved(rootIntent);
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
			long tmLoad = System.currentTimeMillis() - 2*DELAY_LOAD;
			//long tmTrack = System.currentTimeMillis() - 2*DELAY_TRACK;
			//noinspection InfiniteLoopStatement
			while(true)//No hay un sistema para listen y not polling??????
			{
Log.w(TAG, String.format(Locale.ENGLISH, "CesService:loop---------------------DELAY_TRACK=%d------------------------%s", DELAY_TRACK/1000, java.text.DateFormat.getDateTimeInstance().format(new java.util.Date())));
				if( ! _login.isLogged())
				{
					//Log.w(TAG, "loop---sin usuario");
					_login.login(new Login.AuthListener()
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
				if(_tmTrack + DELAY_TRACK < System.currentTimeMillis())
				{
					saveGeoTracking();
					_tmTrack = System.currentTimeMillis();
				}
//Log.w(TAG, "LOOP before sleeping------------------------"+(DELAY_TRACK/4000));
				try{
					//if(DELAY_TRACK > DELAY_TRACK_MIN+20*1000)
						Thread.sleep(DELAY_TRACK/4);
					//else Thread.sleep(DELAY_TRACK/3);
				}catch(InterruptedException e){Log.e(TAG, "---------------- LOOP SLEEP INTERRUPTED---------------");}
			}
		}
		catch(Exception e){Log.e(TAG, "onHandleIntent:e:--------------------------------------------", e);}
		CesWakefulReceiver.completeWakefulIntent(workIntent);
	}

	//______________________________________________________________________________________________
	public void _restartDelayRuta()
	{
		DELAY_TRACK = DELAY_TRACK_MIN;
Log.e(TAG, "_startRuta:------------------------------"+DELAY_TRACK);
		saveGeoTracking();
		_tmTrack = System.currentTimeMillis();
		//if(_hiloLoop!=null)_hiloLoop.interrupt();Doesnt work
	}

	//______________________________________________________________________________________________
	Fire.ObjetoListener<Aviso> _lisAviso = new Fire.ObjetoListener<Aviso>()
	{
		@Override
		public void onData(Aviso[] aData)
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
				_GeofenceStoreAvisos = new CesGeofenceStore(aGeofences);//Se puede añadir en lugar de crear desde cero?
			}
		}
		@Override
		public void onError(String err)
		{
			Log.e(TAG, "cargarListaGeoAvisos:e:-----------------------------------------------------"+err);
		}
	};
	//______________________________________________________________________________________________
	public void cargarListaGeoAvisos()
	{
		try
		{
			Aviso.getActivos(_lisAviso);
			/*Aviso.getActivos(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot avisos)
				{
					//TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
					boolean bDirty = false;
					long n = avisos.getChildrenCount();
					if(n != CesService.this._listaGeoAvisos.size())
					{
						if(_GeofenceStoreAvisos != null)_GeofenceStoreAvisos.clear();
						_listaGeoAvisos.clear();
						bDirty = true;
					}
					ArrayList<Geofence> aGeofences = new ArrayList<>();
					ArrayList<Aviso> aAvisos = new ArrayList<>();
					int i=0;
					for(DataSnapshot l : avisos.getChildren())
					{
						Aviso a = l.getValue(Aviso.class);
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
						_GeofenceStoreAvisos = new CesGeofenceStore(CesService.this, aGeofences);//Se puede añadir en lugar de crear desde cero?
					}
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					Log.e(TAG, String.format("cargarListaGeoAvisos:e:%s",err));
				}
			});*/
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
			public void onData(Ruta[] aData)
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
	private void guardarPunto(Location loc, Ruta r, String sId)
	{
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
		if(r.getPuntosCount() > 1 && loc.getAccuracy() > ACCURACY_MAX || loc.getAccuracy() > 5+DISTANCE_MIN)
		{
			Log.w(TAG, "guardarPunto:loc.getAccuracy() > MAX  or  loc.getAccuracy() > DISTANCE_MIN ---------"+loc.getAccuracy()+" > "+ACCURACY_MAX+" / "+DISTANCE_MIN);
			return;
		}

		if( ! _sId.equals(sId))//TODO: if sId exist in bbdd, not new route, _locLastSaved = last loc in bbdd ==> Too much monkey business
		{
			_sId = sId;
			_locLastSaved = null;
			DELAY_TRACK = DELAY_TRACK_MIN;
			Log.w(TAG, "guardarPunto:Nueva ruta: ----------------------------------------------- "+sId);
		}
		else if(_locLastSaved != null)
		{
			//TODO: puntos mas cercanos dependiendo de si tienes wifi? opcion de no guardar hasta tener wifi?
			float distLastSaved = loc.distanceTo(_locLastSaved);
			if(distLastSaved < DISTANCE_MIN)//Puntos muy cercanos
			{
				Log.w(TAG, String.format(Locale.ENGLISH, "guardarPunto:Punto repetido o sin precision: %s   dist=%.1f  acc=%.1f", sId, distLastSaved, loc.getAccuracy()));
				DELAY_TRACK += 10*1000;
				if(DELAY_TRACK > DELAY_TRACK_MAX) DELAY_TRACK = DELAY_TRACK_MAX;
				return;
			}
			else if(distLastSaved > 200)
				DELAY_TRACK = DELAY_TRACK_MIN;
			else if(distLastSaved > 150)
				DELAY_TRACK -= 3*60*1000;
			else if(distLastSaved > 100)
				DELAY_TRACK -= 2*60*1000;
			else if(distLastSaved > 75)
				DELAY_TRACK -= 60*1000;
			//else if(distLastSaved > 50)
			if(DELAY_TRACK < DELAY_TRACK_MIN) DELAY_TRACK = DELAY_TRACK_MIN;
		}
		r.guardar(new GuardarListener(loc));
		_locLastSaved = loc;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	private class GuardarListener implements DatabaseReference.CompletionListener
	{
		private Location _loc = null;
		GuardarListener(Location loc){_loc = loc;}
		@Override
		public void onComplete(DatabaseError err, DatabaseReference data)
		{
			if(err == null)
			{
				//Log.w(TAG, "GuardarListener:onComplete:----------------------:" + data);
				Ruta.addPunto(data.getKey(), _loc.getLatitude(), _loc.getLongitude(),
						_loc.getAccuracy(), _loc.getAltitude(), _loc.getSpeed(), _loc.getBearing(),
						new Transaction.Handler()
					{
						@Override public Transaction.Result doTransaction(MutableData mutableData){return null;}
						@Override public void onComplete(DatabaseError err, boolean b, DataSnapshot data)
						{
							if(err != null)	Log.e(TAG, String.format("saveGeoTracking:guardar:pto:err:----------------------:%s",err));
							//else        	Log.w(TAG, "saveGeoTracking:guardar:pto:----------------------:" + data);
							Util.refreshListaRutas();//Refrescar lista rutas en main..
						}
					});
			}
			else
			{
				Log.e(TAG, String.format("saveGeoTracking:guardar:err:-------------------------:%s",err));
			}
		}
	}


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
		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
			@Override
			public void onResult(@NonNull LocationSettingsResult result)
			{
				final Status status = result.getStatus();
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
			}
		});
	}

}




//Si el nuevo punto no tiene sentido, no se guarda...
			/*
			double vel = _locLastSaved.distanceTo(loc) * 3600 / (System.currentTimeMillis() - _tmLastSaved);//Km/h
			if(vel > 200)//kilometros hora a metros segundo : 300km/h = 83m/s
			{
				System.err.println("CesService:saveGeoTracking:Punto FALSO: " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " :::  t="+(System.currentTimeMillis() - _tmLastSaved));
				return;
			}
			else
				System.err.println("CesService:saveGeoTracking:Punto FALSO: " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " :::  t="+(System.currentTimeMillis() - _tmLastSaved));

			//TODO: read this
			//http://gis.stackexchange.com/questions/19683/what-algorithm-should-i-use-to-remove-outliers-in-trace-data
			//Si el nuevo punto no tiene sentido, no se guarda...
			//if(loc.getAccuracy() > 10) {
			if(_loc0 == null)
				_loc0 = loc;
			else if(_loc1 == null)
				_loc1 = loc;
			else
			{
				double dis01 = _loc0.distanceTo(_loc1);
				double dis02 = _loc0.distanceTo(loc);
				if(dis01 > 2*dis02 && (System.currentTimeMillis() - _tmLastSaved) < DELAY_LOAD*2)
				{
					// El punto _loc1 es incorrecto, borrar
					System.err.println("CesService: Punto anterior incorrecto: "+_loc1.getLatitude()+"/"+_loc1.getLongitude());
				}
				else
				{
					_loc0 = _loc1;
					_loc1 = loc;
				}
			}*/


//TODO: por que no funciona tracking con geofence?????
	/*//______________________________________________________________________________________________
	public static void cargarGeoTracking()
	{
System.err.println("CesService:cargarGeoTracking---------------------------------0--------");
		if(_GeofenceStoreTracking != null)_GeofenceStoreTracking.clear();//TODO: si es el mismo no hay necesidad de recrearlo: comprobar...

		RutaPto.getTrackingPto(new AsyncCallback<RutaPto>()
		{
			@Override
			public void handleResponse(RutaPto ptoTrackin)
			{
//System.err.println("CesService:cargarGeoTracking-----------------------------------------:" + ptoTrackin);
				if(Util.getTrackingRoute().isEmpty())
				{
					System.err.println("CesService:cargarGeoTracking-----------------------------------------:No hay ruta activa:Eliminando..." + ptoTrackin);
					ptoTrackin.removeTrackingPto(new AsyncCallback<Long>()
					{
						@Override
						public void handleResponse(Long aLong)
						{
							System.err.println("CesService:cargarGeoTracking-----------------------------------------:Eliminado:"+aLong);
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.err.println("CesService:cargarGeoTracking-----------------------------------------:Eliminado FALLO:" + backendlessFault);
						}
					});
					return;
				}
System.err.println("CesService:cargarGeoTracking---------------------------------pto.loc--------:" + ptoTrackin.getLatitud()+","+ ptoTrackin.getLongitud());
				Geofence gf = new Geofence.Builder()
					.setRequestId(ptoTrackin.getObjectId())
					.setCircularRegion(ptoTrackin.getLatitud(), ptoTrackin.getLongitud(), RADIO_TRACKING)
					.setExpirationDuration(Geofence.NEVER_EXPIRE)
					.setLoiteringDelay(GEOFEN_DWELL_TIME)// Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
					.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
					.build();
				ArrayList<Geofence> aGeofences = new ArrayList<>();
				aGeofences.add(gf);
				_GeofenceStoreTracking = new CesGeofenceStore(CesService._this, aGeofences);
	System.err.println("CesService:cargarGeoTracking-----------------------------------------:Se añadio la geofence para tracking:"+gf);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("CesService:cargarGeoTracking-----------------------------------------:Sin puntos");
				Util.setTrackingRoute("");
				return;
			}
		});
	}*/
	//______________________________________________________________________________________________
	//Varias rutas al mismo tiempo, si tendrían los mismos puntos?
	/*private void cargarListaGeoTracking()
	{
		try
		{
			if(_GeofenceStoreTracking != null)_GeofenceStoreTracking.clear();

			//BackendlessDataQuery query = new BackendlessDataQuery();
			Backendless.Persistence.of(GeoPoint.class).find(new AsyncCallback<BackendlessCollection<GeoPoint>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<GeoPoint> gs)
				{
					int n = gs.getTotalObjects();
					if(n < 1)return;
					ArrayList<Geofence> aGeofences = new ArrayList<>();
					Iterator<GeoPoint> it = gs.getCurrentPage().iterator();
					while(it.hasNext())
					{
						GeoPoint g = it.next();
						//_listaGeoTracking.add(g);
if(g.getLatitude() == null)return;
						aGeofences.add(new Geofence.Builder().setRequestId(g.getObjectId())
							.setCircularRegion(g.getLatitude(), g.getLongitude(), RADIO_TRACKING)
							.setExpirationDuration(Geofence.NEVER_EXPIRE)
							.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
							.build());
					}
					_GeofenceStoreTracking = new CesGeofenceStore(CesService.this, aGeofences);
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("CesService:cargarListaGeoTracking:e:" + backendlessFault);//BackendlessFault{ code: '2024', message: 'Wrong entity name: name must not have a symbol '.'' }
				}
			});
		}
		catch(Exception e)
		{
			System.err.println("CesService:cargarListaGeoTracking:e:"+e);
			//_lista.clear();
		}
	}



	if(loc.getAccuracy() > ACCURACY_MAX)
		{
			if(aLoc == null){
				aLoc = new Location[3];
				iLoc = 0;
			}
			if(iLoc < aLoc.length){
				aLoc[iLoc++] = loc;
				return;
			}
			else{
				for(Location l : aLoc)
					if(loc.getAccuracy() > l.getAccuracy()) loc = l;
				aLoc = null;
			}
		}
		else
		{
			if(aLoc!=null && _locLastSaved!= null && loc.distanceTo(_locLastSaved) > 500)
			{
				//for(Location l : aLoc)if(loc.getAccuracy() > l.getAccuracy()) loc = l;
				Location l0 = aLoc[0];
				for(int i=1; i < aLoc.length && aLoc[i]!=null; i++)
					if(l0.getAccuracy() > aLoc[i].getAccuracy()) l0 = aLoc[i];
				r.guardar(new GuardarListener(l0));
			}
			aLoc = null;
		}


	*/