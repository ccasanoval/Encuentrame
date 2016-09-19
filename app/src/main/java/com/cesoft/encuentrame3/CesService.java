package com.cesoft.encuentrame3;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Ruta;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: CesService:Login:f: -------------------------------------------------- Entity with the specified ID cannot be found: Id - A801DF39-639B-910E-FF23-C9996E781E00

//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo
//TODO: Si el primer punto de ruta es erroneo y esta lejos, los demas no se grabaran por filtro velocidad!!!
public class CesService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
	private static final int GEOFEN_DWELL_TIME = 60*1000;//TODO:customize in settings...
	private static final long DELAY_LOAD = 5*60*1000;//TODO: ajustar
	//private static final int RADIO_TRACKING = 10;//TODO: El radio es el nuevo periodo, config al crear NUEVA ruta...

	private static CesGeofenceStore _GeofenceStoreAvisos;
	private static CesService _this;
	private static ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();


	//______________________________________________________________________________________________
	public CesService()
	{
		super("EncuentrameSvc");
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		_this = this;
		Login.setSvcContext(getApplicationContext());
		Login.login(new Login.AuthListener()
		{
			@Override
			public void onExito(FirebaseUser usr)
			{
				System.err.println("CesService:onCreate:login:exito:-------------------------------------------------- ");
			}
			@Override
			public void onFallo(Exception e)
			{
				System.err.println("CesService:onCreate:login:fallo:-------------------------------------------------- "+e);
				CesService.this.stopSelf();
			}
		});
		iniGeoTracking();
System.err.println("CesService:onCreate:-------------------------------------------------- ");
	}

	//______________________________________________________________________________________________
	@Override
	protected void onHandleIntent(Intent workIntent)
	{
		try
		{
			long tmLoad = System.currentTimeMillis() - 2*DELAY_LOAD;
			//noinspection InfiniteLoopStatement
			while(true)//No hay un sistema para listen y not polling??????
			{
System.err.println("CesService:loop-------------------------------------------------------------"+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
				if( ! Login.isLogged())
				{
					System.err.println("CesService:loop---sin usuario");
					Login.login(new Login.AuthListener()
					{
						@Override
						public void onExito(FirebaseUser usr)
						{
							System.err.println("CesService:loop---------Login OK:"+usr);
						}
						@Override
						public void onFallo(Exception e)
						{
							System.err.println("CesService:loop---------Login kk:"+e);
						}
					});
					Thread.sleep(DELAY_LOAD / 3);
					continue;
				}
				if(tmLoad + DELAY_LOAD < System.currentTimeMillis())
				{
					cargarListaGeoAvisos();
					tmLoad = System.currentTimeMillis();
				}
				saveGeoTracking();
				Thread.sleep(DELAY_LOAD / 3);
			}
		}
		catch(InterruptedException e){System.err.println("CesService:onHandleIntent:e:"+e);}
	}

	//______________________________________________________________________________________________
	public static void cargarListaGeoAvisos()
	{
System.err.println("CesService:cargarListaGeoAvisos-----------------------------------0------");
		try
		{
			Aviso.getActivos(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot avisos)
				{
					//TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
					boolean bDirty = false;
					long n = avisos.getChildrenCount();
System.err.println("CesService:cargarListaGeoAvisos-----********************************************************************------------1------"+n);
					if(n != _listaGeoAvisos.size())
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
System.err.println("CesService:cargarListaGeoAvisos-----------------------------------2------");
						if( ! bDirty)
						{
							if(_listaGeoAvisos.size() < i)
								bDirty = true;
							else if( ! _listaGeoAvisos.contains(a))//else if(_listaGeoAvisos.get(i))
								bDirty = true;
							i++;
						}
if(bDirty)System.err.println("Aviso="+a.getId()+" : "+a.getNombre()+":"+a.getDescripcion() + "\t Geof=" + gf.getRequestId());
					}
					if(bDirty)
					{
System.err.println("CesService:cargarListaGeoAvisos:handleResponse:-------------DIRTY");
						_listaGeoAvisos = aAvisos;
						_GeofenceStoreAvisos = new CesGeofenceStore(_this, aGeofences);//Se puede añadir en lugar de crear desde cero?
					}
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					System.err.println("CesService:cargarListaGeoAvisos:f:"+err);
				}
			});
		}
		catch(Exception e)
		{
			System.err.println("CesService:cargarListaGeoAvisos:e:"+e);
			//_lista.clear();
		}
	}

	//______________________________________________________________________________________________
	//TODO: No guardar punto si es igual que el último...
	//TODO: No guardar si (distancia con ultimo punto)/(tiempo ultimo punto) > 300km/h
	private static Location _locLastSaved = null;
	//private static long _tmLastSaved = System.currentTimeMillis();
	//
	//private static Location _loc0 = null, _loc1 = null;
	private static String _sId = "";
	public static void saveGeoTracking()
	{
		final String sId = Util.getTrackingRoute(CesService._this);//TODO: guardar ruta en nube para que no se olvide al reiniciar?
		if(sId.isEmpty())
		{
			CesService._this.stopTracking();
			return;
		}
		CesService._this.startTracking();

		Ruta.getById(sId, new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot rutas)
			{
				try{
				rutas.getValue(Ruta.class);
				System.err.println("CesService:saveGeoTracking:Ruta.getById: OOOOOOOOOK");
				}catch(Exception e){System.err.println("CesService:saveGeoTracking:Ruta.getById:"+rutas);}

				Ruta r = null;
				for(DataSnapshot ruta : rutas.getChildren())
				{
					r = ruta.getValue(Ruta.class);//om.firebase.client.FirebaseException: Failed to bounce to type
					if(r != null)break;
				}
				if(r == null)
				{
					System.err.println("CesService:saveGeoTracking:Ruta.getById:NULL---------------");
					return;
				}

				CesService.handleResponse(r, sId);
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				System.err.println("CesService:saveGeoTracking:findById:f:----------------------:" + err);
			}
		});
	}

	public static void handleResponse(Ruta r, String sId)
	{
		System.err.println("CesService:handleResponse:"+r);
		if(r == null)return;
		final Location loc = Util.getLocation(CesService._this);
		guardarPunto(loc, r, sId);
	}

	private static void guardarPunto(final Location loc, final Ruta r, final String sId)
	{
		if(loc == null)
		{
			System.err.println("CesService:guardarPunto:findById:Util.getLocation()-----------------:NULL");
			return;
		}
System.err.println("CesService:guardarPunto:findById:Util.getLocation()----------------------:" + loc.getLatitude() + "/" + loc.getLongitude()+":"+(new java.util.Date(loc.getTime())));
if(loc.hasAccuracy())System.err.println("CesService:guardarPunto:findById:Util.getAccuracy()-------------------:" + loc.getAccuracy());
if(loc.hasSpeed())System.err.println("CesService:guardarPunto:findById:Util.getSpeed()-------------------:" + loc.getSpeed());
if(loc.hasSpeed())System.err.println("CesService:guardarPunto:findById:Util.getAltitude()-------------------:" + loc.getAltitude());
	if(loc.hasSpeed())System.err.println("CesService:guardarPunto:findById:Util.getBearing()-------------------:" + loc.getBearing());


		if(!loc.hasAccuracy() || loc.getAccuracy() > 100)return;//TODO:Test

		if( ! _sId.equals(sId))
		{
			_sId = sId;
			_locLastSaved = null;
			System.err.println("CesService:guardarPunto:Nueva ruta: " + _sId + " != " + sId);
		}
		else if(_locLastSaved != null)
		{
			if(loc.distanceTo(_locLastSaved) < 2)//Puntos muy cercanos
			{
				System.err.println("CesService:saveGeoTracking:Punto repetido: " + sId + " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLastSaved.getLatitude() + "," + _locLastSaved.getLongitude());
				return;
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
			{
				_loc0 = loc;
			}
			else if(_loc1 == null)
			{
				_loc1 = loc;
			}
			else
			{
				double dis01 = _loc0.distanceTo(_loc1);
				double dis02 = _loc0.distanceTo(loc);
				if(dis01 > 2*dis02 && (System.currentTimeMillis() - _tmLastSaved) < DELAY_LOAD*2)
				{
					//TODO: El punto _loc1 es incorrecto, borrar
					System.err.println("CesService: Punto anterior incorrecto: "+_loc1.getLatitude()+"/"+_loc1.getLongitude());
				}
				else
				{
					_loc0 = _loc1;
					_loc1 = loc;
				}
			}*/
		}
		//TODO: añadir geofence por si quisiera funcionar...?
		r.guardar(new DatabaseReference.CompletionListener()
		{
			@Override
			public void onComplete(DatabaseError err, DatabaseReference data)
			{
				if(err == null)
				{
					System.err.println("CesService:saveGeoTracking:guardar:----------------------:" + data);
					Ruta.addPunto(data.getKey(), loc.getLatitude(), loc.getLongitude(),
							loc.getAccuracy(), loc.getAltitude(), loc.getSpeed(), loc.getBearing(),
							new Transaction.Handler()
						{
							@Override public Transaction.Result doTransaction(MutableData mutableData){return null;}
							@Override
							public void onComplete(DatabaseError err, boolean b, DataSnapshot data)
							{
								if(err != null)
									System.err.println("CesService:saveGeoTracking:guardar:pto:err:----------------------:" + err);
								else
									System.err.println("CesService:saveGeoTracking:guardar:pto:----------------------:" + data);

								Util.refreshListaRutas();//Refrescar lista rutas en main..
							}
						});
				}
				else
				{
					System.err.println("CesService:saveGeoTracking:guardar:err:----------------------:" + err);
				}
				_locLastSaved = loc;
				//_tmLastSaved = System.currentTimeMillis();
			}
		});
	}

	//TODO: por que no funciona tracking con geofence?????
	/*//______________________________________________________________________________________________
	public static void cargarGeoTracking()
	{
System.err.println("CesService:cargarGeoTracking---------------------------------0--------");
		if(_GeofenceStoreTracking != null)_GeofenceStoreTracking.clear();//TODO: si es el mismo no hay necesidad de recrearlo: comprobar...
		//TODO: Investigar posibilidades de Backendless.Geo

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
				//TODO:Si hubiese ruta activa habria que crear el punto...
				//TODO: hay que añadir fecha a los puntos...
				Util.setTrackingRoute("");//TODO:refrescar lista de rutas para quitar ruta activa...
				return;
			}
		});
	}*/
	//______________________________________________________________________________________________
	//TODO: Varias rutas al mismo tiempo, si tendrían los mismos puntos?
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
	}*/





	//https://developer.android.com/training/location/change-location-settings.html
	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;
	public void iniGeoTracking()
	{
		if(checkPlayServices())buildGoogleApiClient();
		if(_GoogleApiClient != null)_GoogleApiClient.connect();

	    _LocationRequest = new LocationRequest();
	    _LocationRequest.setInterval(60*1000);//TODO: ajustar por usuario...
	    _LocationRequest.setFastestInterval(50*1000);
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
			System.err.println("CesService:checkPlayServices:e:" + result);
	        return false;
	    }
	    return true;
	}


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
				System.err.println("CesService:startTracking:e:"+e);
			}
		}
	}
	private void stopTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
			LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this);
	}


	@Override
	public void onConnected(@Nullable Bundle bundle)
	{
		System.err.println("CesService:onConnected");
	}
	@Override
	public void onConnectionSuspended(int i)
	{
		System.err.println("CesService:onConnectionSuspended:"+i);
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}
	@Override
	public void onLocationChanged(Location location)
	{
		System.err.println("CesService:onLocationChanged:"+location.getLatitude()+"/"+location.getLongitude()+":"+(new java.util.Date(location.getTime())));
		Util.setLocation(location);
	}
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
	{
		System.err.println("CesService:onConnectionFailed:e:" + connectionResult.getErrorCode());
	}

	private void pideGPS()
	{
		//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(_LocationRequest)
				.setAlwaysShow(true)//so it ask for GPS activation like google maps
				;
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
					System.err.println("LocationSettingsStatusCodes.SUCCESS");
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
					try{status.startResolutionForResult(null, 1000);}catch(android.content.IntentSender.SendIntentException ignored){}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					System.err.println("LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
					break;
				}
			}
		});
	}

}

