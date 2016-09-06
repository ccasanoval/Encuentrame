package com.cesoft.encuentrame3.models;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 08/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Preferencias
{
	public Preferencias(){}

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

	//Guardar contraseña y no pedir...? guardar nombre y password en local settings
	//Start at boot?
	//Delay for capturing locations
	//Delay for geofencing (of for each alert?)
	//Radius for geofencing (of for each alert?)
	//Capture locations allways on?

	private boolean	_bStartAtBoot;
	private boolean	_bRecordarCuenta;
	private String	_sLogin, _sPassword;
	private long	_nTrackingDelay;
	private boolean	_bTrackingAllwaysOn;
	private float	_nGeoRadius;

	public boolean isStartAtBoot(){return _bStartAtBoot;}
	public void setStartAtBoot(boolean v){_bStartAtBoot=v;}
	public boolean isRecordarCuenta(){return _bRecordarCuenta;}
	public void setRecordarCuenta(boolean v){_bRecordarCuenta=v;}
	public String getLogin(){return _sLogin;}
	public void setLogin(String v){_sLogin=v;}
	public void setPassword(String v){_sPassword=v;}
	public long	getTrackingDelay(){return _nTrackingDelay;}
	public void setTrackingDelay(long v){_nTrackingDelay=v;}
	public boolean isTrackingAllwaysOn(){return _bTrackingAllwaysOn;}
	public void setTrackingAllwaysOn(boolean v){_bTrackingAllwaysOn=v;}
	public float getGeoRadius(){return _nGeoRadius;}
	public void setGeoRadius(float v){_nGeoRadius=v;}
}

/*
// save object synchronously
Contact savedContact = Backendless.Persistence.save( contact );
// save object asynchronously
Backendless.Persistence.save(contact, new AsyncCallback<Contact>()
{
public void handleResponse( Contact response )
{
// new Contact instance has been saved
}
public void handleFault( BackendlessFault fault )
{
// an error has occurred, the error code can be retrieved with fault.
getCode()
}
});


Long result = Backendless.Persistence.of( Contact.class ).remove(savedContact );



Backendless.Persistence.of( Contact.class).find( new AsyncCallback<BackendlessCollection<Contact>>()
{
@Override
public void handleResponse( BackendlessCollection<Contact>
foundContacts )
{
// all Contact instances have been found
}
@Override
public void handleFault( BackendlessFault fault )
{
// an error has occurred, the error code can be retrieved with
fault.getCode()
}
});


Contact firstContact = Backendless.Persistence.of( Contact.class ).findFirst();
Backendless.Persistence.of( Contact.class).findFirst( new
AsyncCallback<Contact>(){
@Override
public void handleResponse( Contact contact )
{
// first contact instance has been found
}
@Override
public void handleFault( BackendlessFault fault )
{
// an error has occurred, the error code can be retrieved with
fault.getCode()
}
});





Contact contact = new Contact();
contact.setName( "Jack Daniels" );
contact.setAge( 147 );
contact.setPhone( "777-777-777" );
contact.setTitle( "Favorites" );
// save object synchronously
Contact savedContact = Backendless.Persistence.save( contact );
Contact lastContact = Backendless.Persistence.of( Contact.class ).
findById( savedContact.getObjectId() );


Contact contact = new Contact();
contact.setName( "Jack Daniels" );
contact.setAge( 147 );
contact.setPhone( "777-777-777" );
contact.setTitle( "Favorites" );
Backendless.Persistence.save( contact, new AsyncCallback<Contact>()
{
@Override
public void handleResponse( Contact savedContact )
{
Backendless.Persistence.of( Contact.class ).findById(
savedContact.getObjectId(), new AsyncCallback<Contact>() {
@Override
public void handleResponse( Contact response )
{
// a Contact instance has been found by ObjectId
}
@Override
public void handleFault( BackendlessFault fault )
{
// an error has occurred, the error code can be retrieved
with fault.getCode()
}
} );
}

@Override
public void handleFault( BackendlessFault fault )
{
// an error has occurred, the error code can be retrieved with
fault.getCode()
}
} );




Backendless.Persistence.of( E ).find( BackendlessDataQuery query,AsyncCallback<BackendlessCollection<E>>);

String whereClause = "age = 147";
BackendlessDataQuery dataQuery = new BackendlessDataQuery();
dataQuery.setWhereClause( whereClause );
BackendlessCollection<Contact> result = Backendless.Persistence.of(
Contact.class ).find( dataQuery );


String whereClause = "age >= 21 AND age <=30";
BackendlessDataQuery dataQuery = new BackendlessDataQuery();
dataQuery.setWhereClause( whereClause );
BackendlessCollection<Contact> result = Backendless.Persistence.of(
Contact.class ).find( dataQuery );


String whereClause = "name LIKE 'Jack%'";
BackendlessDataQuery dataQuery = new BackendlessDataQuery();
dataQuery.setWhereClause( whereClause );
BackendlessCollection<Contact> result = Backendless.Persistence.of(
Contact.class ).find( dataQuery );



distance(center point latitude,center point longitude,columnname which contains geo point.latitude,columnname which contains geo point.longitude ) <operator>units-function(value)
distance( 30.26715, -97.74306, coordinates.latitude, coordinates.longitude )< mi(200)



Friend fred = new Friend();
fred.setName( "Fred" );
fred.setPhoneNumber( "210-555-1212" );
fred.setCoordinates( new GeoPoint( 29.42412, -98.49363 ) );
fred.getCoordinates().addCategory( "Home" );
fred.getCoordinates().addMetadata( "description", "Fred's home" );
Backendless.Data.of( Friend.class ).save( fred );


String whereClause = "distance( 30.26715, -97.74306, coordinates.latitude,coordinates.longitude ) < mi(200)";
BackendlessDataQuery dataQuery = new BackendlessDataQuery( whereClause );
QueryOptions queryOptions = new QueryOptions();
queryOptions.setRelationsDepth( 1 );
dataQuery.setQueryOptions( queryOptions );
BackendlessCollection<Friend> friends = Backendless.Data.of( Friend.class ).find( dataQuery );
for( Friend friend : friends.getData() )
{
System.out.println( String.format( "%s lives at %f, %f tagged as '%s'",
	friend.getName(), friend.getCoordinates().getLatitude(), friend.getCoordinates().getLongitude(), (String) friend.getCoordinates().getMetadata( "description" ) ) );
}


updated > '23-Mar-2015'
updated > '03/23/2015'
updated > 1427068800000
 */