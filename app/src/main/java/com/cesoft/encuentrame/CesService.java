package com.cesoft.encuentrame;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;

import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Ruta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: No funciona AVISO!!!!!!!!!!!!!!!!!!!!!!
//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo?
//TODO: Si el primer punto de ruta es erroneo y esta lejos, los demas no se grabaran por filtro velocidad!!!
//Backendless GEOFencing service:
//https://backendless.com/backend-as-a-service/quick-start-guide-for-backendless-geofencing/

public class CesService extends IntentService
{
	private static final int GEOFEN_DWELL_TIME = 2*60*1000;//TODO:customize in settings...
	private static final long DELAY_LOAD = 5*60*1000;//TODO: ajustar
	private static final int MIN_DISTANCIA = 5;
	private static final int MAX_VELOCIDAD = 300;
	//private static final int RADIO_TRACKING = 10;//TODO: El radio es el nuevo periodo, config al crear NUEVA ruta...

	private static CesGeofenceStore _GeofenceStoreAvisos;
	private static CesService _this;
	private static ArrayList<Aviso> _listaGeoAvisos = new ArrayList<>();

	/*FirebaseAuth.AuthResultHandler loginListener = new Firebase.AuthResultHandler()
	{
		@Override
		public void onAuthenticated(AuthData usr)
		{
			System.err.println("CesService:onCreate:onAuthenticated:"+usr);
		}
		@Override
		public void onAuthenticationError(FirebaseError err)
		{
			System.err.println("CesService:onCreate:onAuthenticated:e:"+err);
		}
	};*/

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
		Util.init(this);
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
					Login.login();
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
			//TODO: si so n los mismos no hay necesidad de recrearlos: comprobar...
			//_listaGeoAvisos.clear();
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
	private static long _tmLastSaved = System.currentTimeMillis();
	//
	private static Location _locLast = null;
	private static String _sId = "";
	public static void saveGeoTracking()
	{
		final String sId = Util.getTrackingRoute(CesService._this.getBaseContext());//TODO: guardar ruta en nube para que no se olvide al reiniciar?
		if(sId.isEmpty())return;
System.err.println("CesService:saveGeoTracking:idRuta:"+sId);
		Ruta.getById(sId, new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot rutas)
			{
				System.err.println("CesService:saveGeoTracking:Ruta.getById:"+rutas.getChildrenCount());
				if(rutas.getChildrenCount() < 1)return;

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

				final Location loc = Util.getLocation(CesService._this.getBaseContext());
				if(loc == null)
				{
					System.err.println("CesService:saveGeoTracking:findById:Util.getLocation() == NULL -------------------");
					return;
				}
				System.err.println("CesService:saveGeoTracking:findById:Util.getLocation()----------------------:" + loc.getLatitude() + "," + loc.getLongitude());
				if( ! _sId.equals(sId))//TODO: si se reinicia la toma por nueva y el ultimo punto podria repetirse
				{
					System.err.println("CesService:saveGeoTracking:Nueva ruta: " + _sId + " != " + sId);
					_sId = sId;
					_locLastSaved = null;
				}
				else if(_locLastSaved != null)
				{
					if(loc.distanceTo(_locLastSaved) < MIN_DISTANCIA)//Puntos muy cercanos (metros)
					{
						System.err.println("CesService:saveGeoTracking:Punto repetido o cercano: " + sId + " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLastSaved.getLatitude() + "," + _locLastSaved.getLongitude());
						return;
					}

					//Si el nuevo punto no tiene sentido, no se guarda...
					double vel = _locLastSaved.distanceTo(loc) * 3600 / (System.currentTimeMillis() - _tmLastSaved);//Km/h
//System.err.println("CesService:saveGeoTracking:Punto FALSO???: " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLast.getLatitude() + "," + _locLast.getLongitude()+" t="+(System.currentTimeMillis() - _tmLastSaved));
					if(vel > MAX_VELOCIDAD)//kilometros hora a metros segundo : 300km/h = 83m/s
					{
						System.err.println("CesService:saveGeoTracking:Punto FALSO:--------------------------- " + vel+ " dist=" + _locLastSaved.distanceTo(loc) + " ::: " + _locLast.getLatitude() + "," + _locLast.getLongitude()+" t="+(System.currentTimeMillis() - _tmLastSaved));
						return;
					}
				}
//String s = "null";
//if(_locLastSaved != null)s = _locLastSaved.getLatitude() + "," + _locLastSaved.getLongitude();
//System.err.println("CesService:saveGeoTracking:Punto dif: " + sId + " ::: " + s + " ///// " + loc.getLatitude() + "," + loc.getLongitude());
//System.err.println("CesService:saveGeoTracking:findById:----------------------:" + r + " :::: " + s);

//TODO: añadir geofence por si quisiera funcionar...?
System.err.println("CesService:saveGeoTracking:guardar0000:----------------------:" + r);
				r.guardar(new DatabaseReference.CompletionListener()
				{
					@Override
					public void onComplete(DatabaseError err, DatabaseReference data)
					{
						if(err == null)
						{
							System.err.println("CesService:saveGeoTracking:guardar:----------------------:" + data);
							//r.addPunto(loc.getLatitude(), loc.getLongitude());Ruta.getById(firebase.getKey())
							//Ruta.addPunto(, loc.getLatitude(), loc.getLongitude(), new Firebase.CompletionListener()
							Ruta.addPunto(data.getKey(), loc.getLatitude(), loc.getLongitude(), new Transaction.Handler()
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
						_tmLastSaved = System.currentTimeMillis();
					}
				});
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				System.err.println("CesService:saveGeoTracking:findById:f:----------------------:" + err);
			}
		});
	}
}

