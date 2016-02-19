package com.cesoft.encuentrame.models;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.Date;

/*
public GeoPoint savePoint( GeoPoint geoPoint, AsyncCallback<GeoPoint>responder ) throws BackendlessException
public GeoPoint savePoint( double latitude,double longitude,Map<String, Object> metadata,AsyncCallback<GeoPoint> responder ) throws BackendlessException
public GeoPoint savePoint( double latitude,double longitude,List<String> categories,Map<String, Object> metadata,AsyncCallback<GeoPoint> responder ) throws BackendlessException


List<String> categories = new ArrayList<String>()
categories.add( "restaurants" );
categories.add( "cool_places" );
Map<String, Object> meta = new HashMap<String, Object>();
meta.put( "name", "Eatzi's" );
Backendless.Geo.savePoint( 32.81, -96.80, categories, meta, new AsyncCallback<GeoPoint>()
	{
		@Override
		public void handleResponse( GeoPoint geoPoint ){System.out.println( geoPoint.getObjectId() );}
		@Override
		public void handleFault( BackendlessFault backendlessFault ){}
	});


public void removePoint( GeoPoint geoPoint, AsyncCallback<Void> responder )


public void getPoints( BackendlessGeoQuery geoQuery,AsyncCallback<BackendlessCollection<GeoPoint>>responder )

BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
geoQuery.addCategory( "Restaurants" );
HashMap<String, String> metaSearch = new HashMap<String, String>();
metaSearch.put( "Cuisine", "French" );
metaSearch.put( "Athmosphere", "Romantic" );
geoQuery.setMetadata( metaSearch );
BackendlessCollection<GeoPoint> geoPoints = Backendless.Geo.getPoints(geoQuery );



*/

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 19/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public class RutaPto
{
	/*
	//Backendless
	protected String objectId;
	protected Date created;
	protected Date updated;
		public String getObjectId(){return objectId;}
		public void setObjectId(String objectId){this.objectId = objectId;}
		public Date getCreated(){return created;}
		public void setCreated(Date created){this.created = created;}
		public Date getUpdated(){return updated;}
		public void setUpdated(Date updated){this.updated = updated;}

	protected String objectIdRuta;
		public String getObjectIdRuta(){return objectId;}
		public void setObjectIdRuta(String objectId){this.objectId = objectId;}
	protected Ruta ruta;
		public Ruta getRuta(){return ruta;}
		public void setRuta(Ruta v){ruta = v;}

	private GeoPoint lugar;
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;}


	//// Backendless
	//______________________________________________________________________________________________
	public static void getLista(Ruta ruta, AsyncCallback<BackendlessCollection<RutaPto>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		//queryOptions.addRelated(Ruta.NOMBRE);
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(RutaPto.class).find(query, res);
		//Backendless.Persistence.of(Lugar.class).find(res);
	}

	public void eliminar(AsyncCallback<Long> ac)
	{
		//removePoint( GeoPoint geoPoint, AsyncCallback<Void> responder )
		Backendless.Persistence.of(Lugar.class).remove(this, ac);

	}

	public void guardar(AsyncCallback<Lugar> ac)
	{
		//Backendless.Persistence.of(Lugar.class).save(this, ac);
		Backendless.Persistence.save(this, ac);
	}*/
}
