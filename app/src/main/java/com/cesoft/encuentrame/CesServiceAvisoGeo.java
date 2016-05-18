package com.cesoft.encuentrame;

import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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
System.err.println("CesServiceAvisoGeo:onHandleIntent:-------------------------------------GEOFENCE_TRANSITION_ENTER");
				for(Geofence geof : geofences)
				{
					showAviso(geof.getRequestId(), getString(R.string.en_zona_aviso), getBaseContext());
					System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_ENTER:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_DWELL:
System.err.println("CesServiceAvisoGeo:onHandleIntent:--------------------------------------GEOFENCE_TRANSITION_DWELL");
				for(Geofence geof : geofences)
				{
					//showAviso(geof.getRequestId(), getString(R.string.en_zona_aviso));
					System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_DWELL:"+geof.getRequestId());
				}
				break;
			case Geofence.GEOFENCE_TRANSITION_EXIT:
System.err.println("CesServiceAvisoGeo:onHandleIntent:---------------------------------------GEOFENCE_TRANSITION_EXIT");
				for(Geofence geof : geofences)
				{
					System.err.println("CesServiceAvisoGeo:onHandleIntent:-------******************************-------GEOFENCE_TRANSITION_EXIT:"+geof.getRequestId());
				}
				//for(Geofence geof : geofences)addTrackingPoint(geof);
				break;
			default:
				System.err.println("CesServiceAvisoGeo:onHandleIntent:e: Unknown Geofence Transition -----------------------------");
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	protected void showAviso(String sId, final String sTitle, final Context c)
	{
		Aviso.getById(sId, c, new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot aviso)
			{
				Aviso a = aviso.getValue(Aviso.class);
				Intent i = new Intent(c, ActAviso.class);//CesServiceAvisoGeo.this
				i.putExtra(Aviso.NOMBRE, a);
				Util.showAviso(c, sTitle, a, i);//CesServiceAvisoGeo.this
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("CesServiceAvisoGeo:showAviso:e:"+err);
			}
		});
	}
}
