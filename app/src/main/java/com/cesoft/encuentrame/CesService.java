package com.cesoft.encuentrame;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.Snackbar;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Ruta;
import com.cesoft.encuentrame.models.RutaPto;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.Iterator;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo

//Backendless GEOFencing service:
//https://backendless.com/backend-as-a-service/quick-start-guide-for-backendless-geofencing/

public class CesService extends IntentService
{
	private static final int GEOFEN_DWELL_TIME = 1*60*1000;//TODO:customize in settings...
	private static final long DELAY_LOAD = 5*60*1000;//TODO: ajustar
	private static final int RADIO_TRACKING = 10;//TODO: El radio es el nuevo periodo, config al crear NUEVA ruta...

	private static CesGeofenceStore _GeofenceStoreAvisos;//, _GeofenceStoreTracking;
	private static CesService _this;

	private static ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();
	//private static ArrayList<GeoPoint> _listaGeoTracking = new ArrayList<>();


	//______________________________________________________________________________________________
	public CesService()
	{
		super("EncuentrameAvisoSvc");
		_this = this;
	}

	//______________________________________________________________________________________________
	@Override
	protected void onHandleIntent(Intent workIntent)
	{
		try
		{
			long tmLoad = System.currentTimeMillis() - 2*DELAY_LOAD;
			while(true)
			{
System.err.println("CesService:loop-------------------------------------------------------------"+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
//Util.getLocation();
				if(tmLoad + DELAY_LOAD < System.currentTimeMillis())
				{
					cargarListaGeoAvisos();
					//cargarGeoTracking();//cargarListaGeoTracking();
					tmLoad = System.currentTimeMillis();
				}
				saveGeoTracking();//TODO: periodo...
				Thread.sleep(DELAY_LOAD/3);
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
			//TODO: si so n los mismos no hay necesidad de recrearlos: comprobar...
			//_listaGeoAvisos.clear();

			if(_GeofenceStoreAvisos != null)_GeofenceStoreAvisos.clear();
			Aviso.getActivos(new AsyncCallback<BackendlessCollection<Aviso>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Aviso> avisos)
				{
					boolean bDirty = false;
					int n = avisos.getTotalObjects();
					if(n < 1)return;
					ArrayList<Geofence> aGeofences = new ArrayList<>();
					Iterator<Aviso> it = avisos.getCurrentPage().iterator();
					while(it.hasNext())
					{
						Aviso a = it.next();
System.err.println("CesService:cargarListaGeoAvisos:handleResponse:a:-------------"+a);
						if(a.getRadio() == 0 || (a.getLatitud()==0 && a.getLongitud()==0))continue;
						//if(_listaGeoAvisos.contains(a))continue;
						if( ! _listaGeoAvisos.contains(a))
						{
							bDirty = true;
							_listaGeoAvisos.add(a);
						}
						aGeofences.add(new Geofence.Builder().setRequestId(a.getObjectId())
								.setCircularRegion(a.getLatitud(), a.getLongitud(), a.getRadio())
								.setExpirationDuration(Geofence.NEVER_EXPIRE)
								.setLoiteringDelay(GEOFEN_DWELL_TIME)// Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
								.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL).build());
					}
System.err.println("CesService:cargarListaGeoAvisos:handleResponse:d:-------------"+bDirty);
					if(bDirty)
						_GeofenceStoreAvisos = new CesGeofenceStore(CesService._this, aGeofences);//Se puede añadri en lugar de crear desde cero?
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("CesService:cargarListaGeoAvisos:f:"+backendlessFault);
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
	private static Location _locLast = null;
	private static String _sId = "";
	public static void saveGeoTracking()
	{
		final String sId = Util.getTrackingRoute();
		if(sId.isEmpty())return;

		//Backendless.Persistence.of(Ruta.class).findById(sId, new AsyncCallback<Ruta>()
		Ruta.getById(sId, new AsyncCallback<BackendlessCollection<Ruta>>()
		{
			@Override
			public void handleResponse(BackendlessCollection<Ruta> ar)
			{
				System.err.println("CesService:saveGeoTracking:Ruta.getById");
				if(ar.getCurrentPage().size() < 1)
					return;
				Ruta r = ar.getCurrentPage().get(0);
				Location loc = Util.getLocation();
				System.err.println("CesService:saveGeoTracking:findById:Util.getLocation()----------------------:" + loc.getLatitude() + "," + loc.getLongitude());
				if(!_sId.equals(sId))
				{
					_sId = sId;
					System.err.println("CesService:saveGeoTracking:Nueva ruta: " + _sId + " != " + sId);
				} else if(loc.distanceTo(_locLast) < 2)//Puntos muy cercanos
				{
					System.err.println("CesService:saveGeoTracking:Punto repetido: " + sId + " dist=" + _locLast.distanceTo(loc) + " ::: " + _locLast.getLatitude() + "," + _locLast.getLongitude());
					return;
				}
				String s = "null";
				if(_locLast != null)
					s = _locLast.getLatitude() + "," + _locLast.getLongitude();
				System.err.println("CesService:saveGeoTracking:Punto dif: " + sId + " ::: " + s + " ///// " + loc.getLatitude() + "," + loc.getLongitude());
				_locLast = loc;
				//TODO: añadir geofence por si quisiera funcionar...?
				System.err.println("CesService:saveGeoTracking:findById:----------------------:" + r + " :::: " + s);
				r.addPunto(new GeoPoint(loc.getLatitude(), loc.getLongitude()));//TODO: Add date...
				r.guardar(new AsyncCallback<Ruta>()
				{
					@Override
					public void handleResponse(Ruta r)
					{
						System.err.println("CesService:saveGeoTracking:guardar:----------------------:" + r);
						Util.refreshListaRutas();//Refrescar lista rutas en main..
					}

					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("CesService:saveGeoTracking:guardar:f:----------------------:" + backendlessFault);
					}
				});
			}

			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("CesService:saveGeoTracking:findById:f:----------------------:" + backendlessFault);
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

}
