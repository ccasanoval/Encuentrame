package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.Units;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.cesoft.encuentrame.Util;

import weborb.service.ExcludeProperties;

//https://develop.backendless.com/#Encuentrame/v1/main/data/Aviso
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 10/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@ExcludeProperties(propertyNames = { "latitud, longitud" })
public class Lugar extends Objeto implements Parcelable
{
	public transient static final String NOMBRE = "lugar";//TRANSIENT so not to include in backendless

	public Lugar(){}

	private GeoPoint lugar = new GeoPoint(0,0);
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;lugar.addCategory(NOMBRE);}
		public Double getLatitud(){return lugar.getLatitude();}
		public Double getLongitud(){return lugar.getLongitude();}
		public void setLatLon(Double lat, Double lon){lugar.setLatitude(lat);lugar.setLongitude(lon);}
		//public void setLatitud(Double lat){lugar.setLatitude(lat);}
		//public void setLongitud(Double lon){lugar.setLongitude(lon);}

	public String toString()
	{
		return super.toString() + ", POS:"+(lugar==null?"":lugar.getLatitude()+"/"+lugar.getLongitude());
	}

	//// Backendless
	//______________________________________________________________________________________________
	public void eliminar(AsyncCallback<Long> res)
	{
		Backendless.Persistence.of(Lugar.class).remove(this, res);
	}
	public void guardar(AsyncCallback<Lugar> res)
	{
		Backendless.Persistence.save(this, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
		//Backendless.Persistence.of(Lugar.class).find(res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res, Filtro filtro)
	{
System.err.println("Lugar:getLista:filtro: "+filtro);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		//--FILTRO
		StringBuilder sb = new StringBuilder();//" created = created "
		if( ! filtro.getNombre().isEmpty())
		{
			sb.append(" nombre LIKE '%");
			sb.append(filtro.getNombre());
			sb.append("%' ");
		}
		if(filtro.getRadio() > Util.NADA && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, lugar.latitude, lugar.longitude ) < km(%f) ",
					filtro.getPunto().latitude, filtro.getPunto().longitude, filtro.getRadio()/1000.0));
		}
		if(filtro.getFechaIni() != null)//DateFormat df = java.text.DateFormat.getDateTimeInstance();
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" created >= ");
			sb.append(filtro.getFechaIni().getTime());
		}
		if(filtro.getFechaFin() != null)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" created <= ");
			sb.append(filtro.getFechaFin().getTime());
		}
System.err.println("Lugar:getLista:SQL: "+sb.toString());
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		//--FILTRO
		Backendless.Persistence.of(Lugar.class).find(query, res);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Lugar(Parcel in)
	{
		super(in);
		//
		lugar = new GeoPoint();
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
//System.err.println("Lugar:read Parcel:++++++++++++++++B: "+this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeString(lugar.getObjectId());
		dest.writeDouble(lugar.getLatitude());
		dest.writeDouble(lugar.getLongitude());
	}

	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Creator<Lugar> CREATOR = new Creator<Lugar>()
	{
		@Override
		public Lugar createFromParcel(Parcel in)
		{
			return new Lugar(in);
		}
		@Override
		public Lugar[] newArray(int size)
		{
			return new Lugar[size];
		}
	};

}

/*

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




public void searchByDateInCategoryAsync() throws ParseException
{
	// date
	SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy 'at' HH:mm"
);
final Date updated = dateFormat.parse( "17.01.2015 at 12:00" );
// create
GeoPoint geoPoint = new GeoPoint( 21.306944, -157.858333 );
geoPoint.addCategory( "Coffee" );
geoPoint.addMetadata( "Name", "Starbucks" );
geoPoint.addMetadata( "Parking", true );
geoPoint.addMetadata( "updated", new Date().getTime() );
Backendless.Geo.savePoint( geoPoint, new AsyncCallback<GeoPoint>()
	{
		@Override
		public void handleResponse( GeoPoint geoPoint )
		{
			System.out.println( String.format( "searchByDateInCategory -> point: %s", geoPoint ) );
			// search
			BackendlessGeoQuery query = new BackendlessGeoQuery( Arrays.asList("Coffee" ) );
			query.setWhereClause( String.format( "updated > %d", updated.getTime() ) );
			query.setIncludeMeta( true );

			Backendless.Geo.getPoints( query, new AsyncCallback<BackendlessCollection<GeoPoint>>()
			{
				@Override
				public void handleResponse( BackendlessCollection<GeoPoint> points )
				{
					System.out.println( String.format( "searchByDateInCategory GETPOINTS: %s", points.getCurrentPage() ) );
				}
				@Override
				public void handleFault( BackendlessFault fault )
				{
					System.err.println( String.format( "searchByDateInCategory FAULT =%s", fault ) );
				}
			} );
		}
		@Override
		public void handleFault( BackendlessFault fault )
		{
			System.err.println( String.format( "searchByDateInCategory SAVEPOINT: %s", fault ) );
		}
	} );
}


con metadata:

BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
geoQuery.addCategory( "Restaurants" );
geoQuery.setIncludeMeta( true );
BackendlessCollection<GeoPoint> geoPoints = Backendless.Geo.getPoints(geoQuery );


BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
geoQuery.addCategory( "Restaurants" );
geoQuery.setLatitude( 41.38 );
geoQuery.setLongitude( 2.15 );
geoQuery.setRadius( 10000d );
geoQuery.setUnits( Units.METERS );
BackendlessCollection<GeoPoint> geoPoints = Backendless.Geo.getPoints(geoQuery );


BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
geoQuery.addCategory( "Restaurants" );
geoQuery.setLatitude( 41.38 );
geoQuery.setLongitude( 2.15 );
geoQuery.setRadius( 10000d );
geoQuery.setUnits( Units.METERS );
HashMap<String, String> metaSearch = new HashMap<String, String>();
metaSearch.put( "Cuisine", "French" );
metaSearch.put( "Athmosphere", "Romantic" );
geoQuery.setMetadata( metaSearch );
BackendlessCollection<GeoPoint> geoPoints = Backendless.Geo.getPoints(geoQuery );


public void searchByDateInRadiusAsync() throws ParseException
{
	// date
	SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy 'at' HH:mm");
	final Date updated = dateFormat.parse( "17.01.2015 at 12:00" );
	// create
	GeoPoint geoPoint = new GeoPoint( 21.306944, -157.858333 );
	geoPoint.setCategories( Arrays.asList( "City", "Coffee" ) );
	geoPoint.addMetadata( "Name", "Starbucks" );
	geoPoint.addMetadata( "City", "Honolulu" );
	geoPoint.addMetadata( "Parking", true );
	geoPoint.addMetadata( "updated", new Date().getTime() );
	Backendless.Geo.savePoint( geoPoint, new AsyncCallback<GeoPoint>()
	{
		@Override
		public void handleResponse( GeoPoint geoPoint )
		{
			System.out.println( String.format( "searchByDateInRadius -> point: %s",geoPoint ) );
			// search
			BackendlessGeoQuery query = new BackendlessGeoQuery( 21.30, -157.85, 50, Units.KILOMETERS );
			query.addCategory( "City" );
			query.setWhereClause( String.format( "updated > %d", updated.getTime() ) );
			query.setIncludeMeta( true );
			Backendless.Geo.getPoints( query, new AsyncCallback<BackendlessCollection<GeoPoint>>()
			{
				@Override
				public void handleResponse( BackendlessCollection<GeoPoint> points )
				{
					System.out.println( String.format( "searchByDateInRadius GETPOINTS:	%s", points.getCurrentPage() ) );
				}
				@Override
				public void handleFault( BackendlessFault fault )
				{
					System.err.println( String.format( "searchByDateInRadius FAULT = %s", fault ) );
				}
			} );
		}
		@Override
		public void handleFault( BackendlessFault fault )
		{
			System.err.println( String.format( "searchByDateInRadius SAVEPOINT: %s", fault ) );
		}
	} );
}





public void searchByDateInRectangularAreaAsync() throws ParseException
{
	// date
	SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy 'at' HH:mm");
	Date opened = dateFormat.parse( "17.01.2015 at 07:00" );
	Date closed = dateFormat.parse( "17.01.2015 at 23:00" );
	final Date now = dateFormat.parse( "17.01.2015 at 15:20" );
	// create

	GeoPoint geoPoint = new GeoPoint( 21.306944, -157.858333 );
	geoPoint.addCategory( "Coffee" );
	geoPoint.addMetadata( "Name", "Starbucks" );
	geoPoint.addMetadata( "opened", opened.getTime() );
	geoPoint.addMetadata( "closed", closed.getTime() );
	Backendless.Geo.savePoint( geoPoint, new AsyncCallback<GeoPoint>()
	{
		@Override
		public void handleResponse( GeoPoint geoPoint )
		{
			System.out.println( String.format( "searchByDateInRectangularArea ->point: %s", geoPoint ) );
			// search
			GeoPoint nortWestCorner = new GeoPoint( 21.306944 + 0.5, -157.858333 -0.5 );
			GeoPoint southEastCorner = new GeoPoint( 21.306944 - 0.5, -157.858333 +0.5 );
			BackendlessGeoQuery query = new BackendlessGeoQuery( nortWestCorner,southEastCorner );
			query.addCategory( "Coffee" );
			query.setWhereClause( String.format( "opened < %d AND closed > %d",now.getTime(), now.getTime() ) );
			query.setIncludeMeta( true );
			Backendless.Geo.getPoints( query, new AsyncCallback<BackendlessCollection<GeoPoint>>()
			{
				@Override
				public void handleResponse( BackendlessCollection<GeoPoint> points )
				{
					System.out.println( String.format( "searchByDateInRectangularArea GETPOINTS: %s", points.getCurrentPage() ) );
				}
				@Override
				public void handleFault( BackendlessFault fault )
				{
					System.err.println( String.format( "searchByDateInRectangularArea FAULT = %s", fault ) );
				}
			} );
		}
		@Override
		public void handleFault( BackendlessFault fault )
		{
			System.err.println( String.format( "searchByDateInRectangularArea SAVEPOINT: %s", fault ) );
		}
	} );
}



int mapWidth = 500;
double westLongitude = 23.123;
double eastLongitude = -80.238;
BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
geoQuery.addCategory( "geoservice_sample" );
geoQuery.initClustering( westLongitude, eastLongitude, mapWidth );
BackendlessCollection<GeoPoint> points = Backendless.Geo.getPoints( geoQuery);
Iterator<GeoPoint> iterator=points.getCurrentPage().iterator();
while( iterator.hasNext() )
{
	GeoPoint geoPointOrCluster =iterator.next();
	if( geoPointOrCluster instanceof GeoCluster )
	{
		GeoCluster geoCluster = (GeoCluster) geoPointOrCluster;
		System.out.println( "Number of points in the cluster: " + geoCluster.getTotalPoints() );
	}
	else
	{
		System.out.println( "Loaded geo point" );
	}
	System.out.println( "latitude - " + geoPointOrCluster.getLatitude() + ", longitude - " + geoPointOrCluster.getLongitude() );
}

*/