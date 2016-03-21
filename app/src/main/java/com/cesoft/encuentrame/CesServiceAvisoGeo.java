package com.cesoft.encuentrame;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.backendless.Backendless;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Ruta;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import com.cesoft.encuentrame.models.Aviso;


////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 27/01/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Diferenciar entre los avisos geofence y los tracking geofence...
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
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if( ! geofencingEvent.hasError())
		{
			int transition = geofencingEvent.getGeofenceTransition();
			List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();
			switch(transition)
			{
			case Geofence.GEOFENCE_TRANSITION_ENTER:
System.err.println("CesServiceAvisoGeo:onHandleIntent:-----------------------------GEOFENCE_TRANSITION_ENTER");
				for(Geofence geof : geofences)
					showAviso(geof.getRequestId(), getString(R.string.en_zona_aviso));
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
				System.err.println("CesServiceAvisoGeo:onHandleIntent:-----------------------------GEOFENCE_TRANSITION_DWELL");
				for(Geofence geof : geofences)
				{
					showAviso(geof.getRequestId(), getString(R.string.en_zona_aviso));
					System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
System.err.println("CesServiceAvisoGeo:onHandleIntent:-----------------------------GEOFENCE_TRANSITION_EXIT");
				for(Geofence geof : geofences)
					addTrackingPoint(geof);
				break;
			default:
				System.err.println("CesServiceAvisoGeo:onHandleIntent:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	protected void showAviso(String sId, final String sTitle)
	{
//System.err.println("CesServiceAvisoGeo:showAviso-----------------------------" + sId + " : " + sTitle);
		Aviso.getById(sId, new AsyncCallback<Aviso>()
		{
			@Override
			public void handleResponse(Aviso a)
			{
				Intent i = new Intent(CesServiceAvisoGeo.this, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, a);
				Util.showAviso(CesServiceAvisoGeo.this, sTitle, a, i);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("CesServiceAvisoGeo:showAviso:e:"+backendlessFault);
			}
		});
	}

	//______________________________________________________________________________________________
	protected void addTrackingPoint(Geofence geof)
	{
		//GeoPoint.getById(sId);
System.err.println("CesServiceAvisoGeo:addTrackingPoint-----------*****************************************------------"+geof);
		//TODO: Hallar posicion, borrar este geofence y crear otro con centro en posicion...

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
			//TODO: comprobar maximo numero de puntos... si max cerrar ruta
			Location loc = Util.getLocation();
System.err.println("CesServiceAvisoGeo:addTrackingPoint:------------loc----------:" + loc.getLatitude()+", "+loc.getLongitude());
			Ruta r = Backendless.Persistence.of(Ruta.class).findById(sId);
			r.addPunto(new GeoPoint(loc.getLatitude(), loc.getLongitude()));//TODO: Add date...
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
	}
}
