package com.cesoft.encuentrame.models;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 10/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Lugares
{
	public Lugares(){}

	//Backendless
	private String objectId;
	private Date created;
	private Date updated;
	public String getObjectId(){return objectId;}
	public void setObjectId(String objectId){this.objectId = objectId;}
	public Date getCreated(){return created;}
	public void setCreated(Date created){this.created = created;}
	public Date getUpdated(){return updated;}
	public void setUpdated(Date updated){this.updated = updated;}


}

//TODO: https://www.thoughtworks.com/insights/blog/signing-open-source-android-apps-without-disclosing-passwords

/*
BackendlessDataQuery query = new BackendlessDataQuery();
QueryOptions queryOptions = new QueryOptions();
queryOptions.addRelated( "RELATED-PROPERTY-NAME" );
queryOptions.addRelated( "RELATED-PROPERTY-NAME.RELATION-OFRELATION");
query.setQueryOptions( queryOptions );
BackendlessCollection<T> collection = Backendless.Data.of( T.class).find( query );


Order firstOrder = Backendless.Data.of( Order.class ).findFirst();
ArrayList<String> relationProps = new ArrayList<String>();
relationProps.add( "items" );
relationProps.add( "items.manufacturer" );
Backendless.Data.of( Order.class ).loadRelations( firstOrder,relationProps );


BackendlessDataQuery query = new BackendlessDataQuery();
QueryOptions queryOptions = new QueryOptions();
queryOptions.relationsDepth = 2;
query.setQueryOptions( queryOptions );
BackendlessCollection<Foo> collection = Backendless.Data.of( Foo.class ).find( query );

public GeoPoint location;

public GeoPoint getLocation(){return location;}
public void setLocation( GeoPoint location ){this.location = location;}



GeoPoint point = new GeoPoint();
point.setLatitude( 40.7148 );
point.setLongitude( -74.0059 );
point.addCategory( "taxi" );
point.addMetadata( "service_area", "NYC" );



public void Backedless.Geo.addCategory( String categoryName,AsyncCallback<GeoCategory> responder )
*/