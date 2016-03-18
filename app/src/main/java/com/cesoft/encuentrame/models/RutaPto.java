package com.cesoft.encuentrame.models;


import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 19/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: tener claro si el punto es GeoFenceTracking o es un punto de ruta... un objeto para cada cosa?
//TODO: Quiza heredar de Objeto y te ahorras id, fechas, etc... si vas a utilizar esos campos
public class RutaPto
{
	/// Backendless
	protected String objectId;
	protected Date created;
	protected Date updated;
		public String getObjectId(){return objectId;}
		public void setObjectId(String objectId){this.objectId = objectId;}
		public Date getCreated(){return created;}
		public void setCreated(Date created){this.created = created;}
		public Date getUpdated(){return updated;}
		public void setUpdated(Date updated){this.updated = updated;}

	protected String idRuta;
		public String getIdRuta(){return idRuta;}
		public void setIdRuta(String idRuta){this.idRuta = idRuta;}

	/// Payload
	private double latitud, longitud;
		public void setLatLon(double lat, double lon){latitud = lat; longitud = lon;}
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}


	public String toString()
	{
		return super.toString() + ", POS:"+latitud+"/"+longitud;
	}

	/// TRACKING POINT
	public static void getTrackingPto(AsyncCallback<RutaPto> res)
	{
		Backendless.Persistence.of(RutaPto.class).findFirst(res);
	}
	public void saveTrackingPto(AsyncCallback<RutaPto> res)
	{
		getTrackingPto(new AsyncCallback<RutaPto>()
		{
			@Override public void handleResponse(RutaPto rutaPto)
			{
				//Backendless.Persistence.of(RutaPto.class).remove(rutaPto);
				Backendless.Persistence.of(RutaPto.class).remove(rutaPto, new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long aLong)
					{
						System.err.println("RutaPto:saveTrackingPto:getTrackingPto:Remove old:Eliminado:"+aLong);
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("RutaPto:saveTrackingPto:getTrackingPto:Remove old:f:"+backendlessFault);
					}
				});
			}
			@Override public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("RutaPto:saveTrackingPto:getTrackingPto:f:"+backendlessFault);
			}
		});
		Backendless.Persistence.save(this, res);
	}
	public void removeTrackingPto(AsyncCallback<Long> res)
	{
		Backendless.Persistence.of(RutaPto.class).remove(this, res);
	}
}
