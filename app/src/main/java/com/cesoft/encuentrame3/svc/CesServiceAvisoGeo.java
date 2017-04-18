package com.cesoft.encuentrame3.svc;

import java.util.List;

import android.app.Application;
import android.app.IntentService;
import android.content.Intent;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Si no hay avisos en bbdd quitar servicio, solo cuando se añada uno, activarlo=> activar solo cuando guarde...?
public class CesServiceAvisoGeo extends IntentService
{
	//______________________________________________________________________________________________
	public CesServiceAvisoGeo()
	{
		super("EncuentrameGeofenceSvc");
	}

	//______________________________________________________________________________________________
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Util _util = ((App)getApplication()).getGlobalComponent().util();

		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if( ! geofencingEvent.hasError())
		{
			int transition = geofencingEvent.getGeofenceTransition();
			List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
			switch(transition)
			{
			case Geofence.GEOFENCE_TRANSITION_ENTER:
//System.err.println("CesServiceAvisoGeo:onHandleIntent:-------------------------------------GEOFENCE_TRANSITION_ENTER");
				for(Geofence geof : geofences)
				{
					_util.showAvisoGeo(geof.getRequestId());//, getBaseContext()
					//System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
//System.err.println("CesServiceAvisoGeo:onHandleIntent:--------------------------------------GEOFENCE_TRANSITION_DWELL");
				/*for(Geofence geof : geofences)
				{
					//showAviso(geof.getRequestId(), getString(R.string.en_zona_aviso));
					//System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}*/
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
//System.err.println("CesServiceAvisoGeo:onHandleIntent:---------------------------------------GEOFENCE_TRANSITION_EXIT");
				/*for(Geofence geof : geofences)
				{
					System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
				}*/
				//for(Geofence geof : geofences)addTrackingPoint(geof);
				break;
			default:
				//System.err.println("CesServiceAvisoGeo:onHandleIntent:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}

	/*//______________________________________________________________________________________________
	protected void showAviso(String sId, final String sTitle)//, final Context c)
	{
		Aviso.getById(sId, new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot aviso)
			{
				Aviso a = aviso.getValue(Aviso.class);
				Intent i = new Intent(_app, ActAviso.class);//CesServiceAvisoGeo.this
				i.putExtra(Aviso.NOMBRE, a);
				_util.showAviso(sTitle, a, i);//CesServiceAvisoGeo.this
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				android.util.Log.e("CESoft", String.format("CesServiceAvisoGeo:showAviso:e:%s",err));
			}
		});
	}*/

	//______________________________________________________________________________________________
	/*protected void addTrackingPoint(Geofence geof)
	{
		//GeoPoint.getById(sId);
System.err.println("CesServiceAvisoGeo:addTrackingPoint-----------*****************************************------------"+geof);

		GeoPoint geoPoint = Backendless.Persistence.of(GeoPoint.class).findFirst();
		if(geoPoint == null)
		{
			System.err.println("CesServiceAvisoGeo:addTrackingPoint: No hay geofence de tracking en BBDD...");
			CesService.cargarGeoTracking();
			return;
		}
		String sId = Util.getTrackingRoute();
		Backendless.Persistence.of(GeoPoint.class).remove(geoPoint);
		if(sId.isEmpty())
		{
			System.err.println("CesServiceAvisoGeo:addTrackingPoint: No hay ruta activa...");
			CesService.cargarGeoTracking();
		}
		else
		{
			Location loc = Util.getLocation();
System.err.println("CesServiceAvisoGeo:addTrackingPoint:------------loc----------:" + loc.getLatitude()+", "+loc.getLongitude());
			Ruta r = Backendless.Persistence.of(Ruta.class).findById(sId);
			r.addPunto(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
			r.guardar(new AsyncCallback<Ruta>()
				{
					@Override public void handleResponse(Ruta ruta)
					{
System.err.println("CesServiceAvisoGeo:addTrackingPoint:----------------------:" + ruta);
					}
					@Override public void handleFault(BackendlessFault backendlessFault){}
				});
			/// Crear geofence con pos actual
			GeoPoint gp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
			Backendless.Persistence.of(GeoPoint.class).save(gp);
			CesService.cargarGeoTracking();
		}
	}*/
}
