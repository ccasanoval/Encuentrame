package com.cesoft.encuentrame.models;

import android.os.Parcel;

import com.cesoft.encuentrame.Util;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//https://develop.backendless.com/#Encuentrame/v1/main/data/Aviso
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//ExcludeProperty(propertyName = "latitud")
public class Aviso extends Objeto
{
	public static final String NOMBRE = "aviso";

	private Firebase _datos;

	//______________________________________________________________________________________________
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean v){activo=v;}

	private double latitud, longitud;
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}

	private double radio;//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence ¿COMO MEJORAR GOOGLE GEOFENCE? Probar backendless geofences?????
		public double getRadio(){return radio;}
		public void setRadio(double v){if(v >= 0 && v < 10000)radio=v;}

	//TODO: Desactivar por hoy, tambien desactivar todos los avisos... incluso: modo avion para app completa
	/*protected Date fechaActivo;
		public void desactivarPorHoy()
		{
			fechaActivo = Calendar.getInstance().getTime();
			//Backendless.Persistence.save(this, ac);
		}
		public void reactivarPorHoy()
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -2);
			fechaActivo = cal.getTime();
			//Backendless.Persistence.save(this, ac);
		}*/

	//______________________________________________________________________________________________
	public Aviso(){}
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Aviso{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', radio='%f', activo='%b'}",
				getId(), nombre, descripcion, latitud, longitud, radio, activo);
	}

	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(this == o)return true;
		if(!(o instanceof Aviso))return false;
		Aviso a = (Aviso)o;
System.err.println("------------------AVISO-EQUALS-"+o+" : "+this);
		return getId().equals(a.getId())
			&& getLatitud() == a.getLatitud() && getLongitud() == a.getLongitud() && getRadio() == a.getRadio()
			&& getNombre().equals(a.getNombre()) && getDescripcion().equals(a.getDescripcion());
	}

	//// PARCELABLE
	//
	protected Aviso(Parcel in)
	{
		//setObjectId(in.readString());nombre = in.readString();descripcion = in.readString();
		super(in);
		//
		setActivo(in.readByte() > 0);
		setId(in.readString());
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
		setRadio(in.readDouble());
System.err.println("----------------Aviso:from parcel 2:" + this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeByte(isActivo()?(byte)1:0);
		dest.writeString(getId());
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		dest.writeDouble(getRadio());
System.err.println("----------------Aviso:writeToParcel:"+this);
	}
	@Override
	public int describeContents(){return 0;}
	public static final Creator<Aviso> CREATOR = new Creator<Aviso>()
	{
		@Override
		public Aviso createFromParcel(Parcel in){return new Aviso(in);}
		@Override
		public Aviso[] newArray(int size){return new Aviso[size];}
	};


	//// FIREBASE
	//
	public void eliminar(Firebase.CompletionListener listener)
	{
		if(_datos != null)
		{
			_datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			Firebase ref = new Firebase(FIREBASE);
			_datos = ref.child(NOMBRE).child(getId());
			_datos.setValue(null, listener);
		}
	}
	public void guardar(Firebase.CompletionListener listener)
	{
		if(_datos != null)
		{
			_datos.setValue(this, listener);
		}
		else
		{
			Firebase ref = new Firebase(FIREBASE);
			if(getId() != null)
			{
				_datos = ref.child(NOMBRE).child(getId());
			}
			else
			{
				_datos = ref.child(NOMBRE).push();
				setId(_datos.getKey());
			}
			_datos.setValue(this, listener);
		}
	}

	public static void getById(String sId, ValueEventListener listener)
	{
		//TODO: Cuando cierras app pero das a notificacion: exception: You need to set the Android context using Firebase.setAndroidContext() before using Firebase.
		Firebase.setAndroidContext(Util.getApplication().getBaseContext());
		Firebase ref1 = new Firebase(FIREBASE).child(NOMBRE).child(sId);
		ref1.addListenerForSingleValueEvent(listener);
/*
		Firebase ref = new Firebase(FIREBASE);
		Query queryRef = ref.orderByKey().equalTo(sId);//.limitToFirst(1);
		queryRef.addListenerForSingleValueEvent(listener);
    	//queryRef.addChildEventListener(listener);*/
	}
	public static void getActivos(ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		Query queryRef = ref.orderByChild("activo").equalTo(true);
		queryRef.addListenerForSingleValueEvent(listener);
    	//queryRef.addChildEventListener(listener);
	}
	public static void getLista(ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		//ref.addValueEventListener(listener);//TODO:Cual mejor?
		ref.addListenerForSingleValueEvent(listener);
	}
	public static void getListaByPos(GeoQueryEventListener listener, Filtro filtro)
	{
System.err.println("-----------------------------------------Aviso:getListaByPos:");
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		GeoFire geoFire = new GeoFire(ref);
		GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio());
		geoQuery.addGeoQueryEventListener(listener);
	}
	public static void getLista(ValueEventListener listener, Filtro filtro)
	{
//TODO-----------------------------------------------------------------------------------------------------
getLista(listener);
if(1==1)return;
System.err.println("Aviso:getLista:filtro: "+filtro);


		//--FILTRO
		StringBuilder sb = new StringBuilder();
		if( ! filtro.getNombre().isEmpty())
		{
			sb.append(" nombre LIKE '%");
			sb.append(filtro.getNombre());
			sb.append("%' ");
		}
		if(filtro.getRadio() > 0 && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, lugar.latitude, lugar.longitude ) < km(%f) ",
					filtro.getPunto().latitude, filtro.getPunto().longitude, filtro.getRadio()/1000.0));
		}
		if(filtro.getFechaIni() != null)
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
		if(filtro.getActivo() != Util.NADA)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" activo = ");
			sb.append(filtro.getActivo()==Filtro.ACTIVO?"true":"false");
		}
System.err.println("Aviso:getLista:SQL: "+sb.toString());
		//if(sb.length() > 0)			query.setWhereClause(sb.toString());
		//--FILTRO
		//Backendless.Persistence.of(Aviso.class).find(query, res);
	}

}
