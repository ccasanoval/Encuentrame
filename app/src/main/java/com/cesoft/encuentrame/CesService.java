package com.cesoft.encuentrame;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.Snackbar;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
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
//TODO: Si el primer punto de ruta es erroneo y esta lejos, los demas no se grabaran por filtro velocidad!!!
//Backendless GEOFencing service:
//https://backendless.com/backend-as-a-service/quick-start-guide-for-backendless-geofencing/

public class CesService extends IntentService
{
	private static final int GEOFEN_DWELL_TIME = 2*60*1000;//TODO:customize in settings...
	private static final long DELAY_LOAD = 5*60*1000;//TODO: ajustar
	private static final int RADIO_TRACKING = 10;//TODO: El radio es el nuevo periodo, config al crear NUEVA ruta...

	private static CesGeofenceStore _GeofenceStoreAvisos;//, _GeofenceStoreTracking;
	private static CesService _this;

	private static ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();
	//private static ArrayList<GeoPoint> _listaGeoTracking = new ArrayList<>();

	AsyncCallback<BackendlessUser> resLogin = new AsyncCallback<BackendlessUser>()
	{
		@Override
		public void handleResponse(BackendlessUser backendlessUser)
		{
			System.err.println("ENTER--------(desde CesService)---------" + backendlessUser);
			//TODO: hacer listener para avisar a todos que ya tenemos usuario... (a login para que pase a main...)
		}
		@Override
		public void handleFault(BackendlessFault backendlessFault)
		{
			System.out.println("CesService:Login:f: " + backendlessFault.getMessage());
		}
	};

	//______________________________________________________________________________________________
	public CesService()
	{
		super("EncuentrameAvisoSvc");
	}
	@Override
	public void onCreate()
	{
		super.onCreate();
		_this = this;
		Util.initBackendless(this);
		Util.setSvcContext(this);
		boolean b = Util.login(resLogin);
	}

	//______________________________________________________________________________________________
	@Override
	protected void onHandleIntent(Intent workIntent)
	{
		try
		{
			long tmLoad = System.currentTimeMillis() - 2*DELAY_LOAD;
			while(true)//No hay un sistema para listen y not polling??????
			{
System.err.println("CesService:loop-------------------------------------------------------------"+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
				if( ! Util.isLogged())
				{
					System.err.println("CesService:loop---sin usuario");
					Util.login(resLogin);
					//try{Backendless.UserService.login("quake1978", "colt1911");}catch(Exception e){System.err.println("e:" + e);}
					Thread.sleep(DELAY_LOAD / 3);
					continue;
				}
//Util.getLocation();
				if(tmLoad + DELAY_LOAD < System.currentTimeMillis())
				{
					cargarListaGeoAvisos();
					//cargarGeoTracking();//cargarListaGeoTracking();
					tmLoad = System.currentTimeMillis();
				}
				saveGeoTracking();//TODO: periodo...
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
			//TODO: si so n los mismos no hay necesidad de recrearlos: comprobar...
			//_listaGeoAvisos.clear();

			Aviso.getActivos(new AsyncCallback<BackendlessCollection<Aviso>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Aviso> avisos)
				{
					//TODO: cuando cambia radio debería cambiar tambien, pero esto no le dejara...
					boolean bDirty = false;
					int n = avisos.getTotalObjects();
					if(n != _listaGeoAvisos.size())
					{
						if(_GeofenceStoreAvisos != null)_GeofenceStoreAvisos.clear();
						_listaGeoAvisos.clear();
						bDirty = true;
					}
					ArrayList<Geofence> aGeofences = new ArrayList<>();
					ArrayList<Aviso> aAvisos = new ArrayList<>();
					Iterator<Aviso> it = avisos.getCurrentPage().iterator();
					int i=0;
					while(it.hasNext())
					{
						Aviso a = it.next();
						aAvisos.add(a);
						Geofence gf = new Geofence.Builder().setRequestId(a.getObjectId())
								.setCircularRegion(a.getLatitud(), a.getLongitud(), a.getRadio())
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
if(bDirty)
{
	System.err.println("Aviso="+a.getObjectId()+" : "+a.getNombre()+":"+a.getDescripcion() + "\t Geof=" + gf.getRequestId());
}
					}
					if(bDirty)
					{
System.err.println("CesService:cargarListaGeoAvisos:handleResponse:-------------DIRTY");
						_listaGeoAvisos = aAvisos;
						_GeofenceStoreAvisos = new CesGeofenceStore(_this, aGeofences);//Se puede añadri en lugar de crear desde cero?
					}
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
	private static Location _locLastSaved = null;
	private static long _tmLastSaved = System.currentTimeMillis();
	//
	private static Location _locLast = null;
	private static String _sId = "";
	public static void saveGeoTracking()
	{
		final String sId = Util.getTrackingRoute();//TODO: guardar ruta en nube para que no se olvide al reiniciar?
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
				final Location loc = Util.getLocation();
				System.err.println("CesService:saveGeoTracking:findById:Util.getLocation()----------------------:" + loc.getLatitude() + "," + loc.getLongitude());
				if( ! _sId.equals(sId))
				{
					_sId = sId;
					_locLastSaved = null;
					System.err.println("CesService:saveGeoTracking:Nueva ruta: " + _sId + " != " + sId);
				}
				else if(_locLastSaved != null)
				{
					if(loc.distanceTo(_locLastSaved) < 2)//Puntos muy cercanos
					{
						System.err.println("CesService:saveGeoTracking:Punto repetido: " + sId + " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLastSaved.getLatitude() + "," + _locLastSaved.getLongitude());
						return;
					}

					//Si el nuevo punto no tiene sentido, no se guarda...
					double vel = _locLastSaved.distanceTo(loc) * 3600 / (System.currentTimeMillis() - _tmLastSaved);//Km/h
//System.err.println("CesService:saveGeoTracking:Punto FALSO???: " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLast.getLatitude() + "," + _locLast.getLongitude()+" t="+(System.currentTimeMillis() - _tmLastSaved));
					if(vel > 300)//kilometros hora a metros segundo : 300km/h = 83m/s
					{
						System.err.println("CesService:saveGeoTracking:Punto FALSO: " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLast.getLatitude() + "," + _locLast.getLongitude()+" t="+(System.currentTimeMillis() - _tmLastSaved));
						return;
					}
				}
String s = "null";
if(_locLastSaved != null)s = _locLastSaved.getLatitude() + "," + _locLastSaved.getLongitude();
System.err.println("CesService:saveGeoTracking:Punto dif: " + sId + " ::: " + s + " ///// " + loc.getLatitude() + "," + loc.getLongitude());
System.err.println("CesService:saveGeoTracking:findById:----------------------:" + r + " :::: " + s);

				//TODO: añadir geofence por si quisiera funcionar...?
				r.addPunto(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
System.err.println("CesService:saveGeoTracking:guardar0000:----------------------:" + r);
				r.guardar(new AsyncCallback<Ruta>()
				{
					@Override
					public void handleResponse(Ruta r)
					{
						System.err.println("CesService:saveGeoTracking:guardar:----------------------:" + r);
						Util.refreshListaRutas();//Refrescar lista rutas en main..

						_locLastSaved = loc;
						_tmLastSaved = System.currentTimeMillis();
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

