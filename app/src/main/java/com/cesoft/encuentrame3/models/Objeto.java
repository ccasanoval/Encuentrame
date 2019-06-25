package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://github.com/firebase/geofire-java
public class Objeto implements Parcelable
{
	static final String GEO = "GEO";
	public static final String NOMBRE = "objeto";

	public static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
	public static final java.text.DateFormat DATE_FORMAT2 = java.text.DateFormat.getDateTimeInstance();

	//
	//NOTE: Firebase needs public field or public getter/setter, if use @Exclude that's like private...
	//

	//General
	public String id = null;
        @Exclude public String getId(){return id;}
        @Exclude public void setId(String v){id = v;}

	public String nombre;
	public String descripcion;
        @Exclude public String getNombre(){return nombre;}
        @Exclude public void setNombre(String v){nombre=v;}
        @Exclude public String getDescripcion(){return descripcion;}
        @Exclude public void setDescripcion(String v){descripcion=v;}

	//TODO:? para que firebase no se queje de 'No setter/field for day found on class java.util.Date'...
    //https://stackoverflow.com/questions/37890025/classmapper-warnings-after-upgrading-firebase
	//^(?!.*(No setter|NativeCrypto|IOnlyOwnerSimSupport|Asset path|IInputConnectionWrapper)).*$
	/*protected long fecha;
        @Exclude public Date getFecha(){return new Date(fecha);}
        @Exclude public void setFecha(Date v){fecha=v.getTime();}*/
	public Date fecha;
		@Exclude public Date getFecha(){return fecha;}
		@Exclude public void setFecha(Date v){fecha=v;}

	public double latitud, longitud;
        @Exclude public double getLatitud(){return latitud;}
        @Exclude public double getLongitud(){return longitud;}
        @Exclude public void setLatLon(double lat, double lon){latitud=lat;longitud=lon;}

    public void bind(Objeto obj) {
        this.descripcion = obj.descripcion;
		this.fecha = obj.fecha;
		this.id = obj.id;
		this.latitud = obj.latitud;
		this.longitud = obj.longitud;
		this.nombre = obj.nombre;
	}

	//______________________________________________________________________________________________
	Objeto() { fecha = new Date(); }
	//______________________________________________________________________________________________
	@NonNull
	@Override public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Objeto{id='%s', nombre='%s', descripcion='%s', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(fecha));
	}

	//______________________________________________________________________________________________
	public boolean pasaFiltro(Filtro filtro)
	{
		String n1 = filtro.getNombre().toLowerCase();
		String n2 = getNombre().toLowerCase();
		if(!n1.isEmpty() && !n2.contains(n1))return false;
		if(filtro.getFechaIni() != null && getFecha().getTime() < filtro.getFechaIni().getTime())return false;
        return filtro.getFechaFin() == null || getFecha().getTime() <= filtro.getFechaFin().getTime();
    }

	// PARCELABLE
	//______________________________________________________________________________________________
	Objeto(Parcel in)
	{
		setId(in.readString());
		setNombre(in.readString());
		setDescripcion(in.readString());
		setLatLon(in.readDouble(), in.readDouble());
		setFecha(new Date(in.readLong()));
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(getNombre());
		dest.writeString(getDescripcion());
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		dest.writeLong(getFecha().getTime());
	}
	@Override public int describeContents() { return 0; }
	public static final Parcelable.Creator<Objeto> CREATOR = new Parcelable.Creator<Objeto>()
	{
		@Override public Objeto createFromParcel(Parcel in) { return new Objeto(in); }
		@Override public Objeto[] newArray(int size) { return new Objeto[size]; }
	};

}
