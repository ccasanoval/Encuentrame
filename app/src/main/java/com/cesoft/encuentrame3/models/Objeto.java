package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

//https://encuentrame-671b9.firebaseio.com/
/* FIREBASE Security & Rules
{
    "rules": {
        ".read": true,
        ".write": true,
        "aviso": {
          ".indexOn": ["activo", "nombre"]
        },
        "lugar": {
          ".indexOn": ["nombre"]
        },
        "ruta_punto": {
          ".indexOn": ["idRuta"]
        },

        "GEO": {
          "lugar": {
              ".indexOn": ["g"]
            }
        }
    }
}*/

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://github.com/firebase/geofire-java
//https://console.firebase.google.com/project/encuentrame-671b9/database/data
public class Objeto implements Parcelable
{
	public static final String GEO = "GEO";
	public static final String NOMBRE = "objeto";

	protected static GeoFire newGeoFire(){return new GeoFire(FirebaseDatabase.getInstance().getReference().child(GEO));}

	public static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
	public static final java.text.DateFormat DATE_FORMAT2 = java.text.DateFormat.getDateTimeInstance();

	//General
	protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}

	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}

	protected Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}


	//______________________________________________________________________________________________
	public Objeto()
	{
		fecha = new Date();
	}
	//______________________________________________________________________________________________
	@Override
	public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Objeto{id='%s', nombre='%s', descripcion='%s', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(fecha));
	}

	//______________________________________________________________________________________________
	public boolean pasaFiltro(Filtro filtro)
	{
		if(!filtro.getNombre().isEmpty() && !getNombre().contains(filtro.getNombre()))return false;
		if(filtro.getFechaIni() != null && getFecha().getTime() < filtro.getFechaIni().getTime())return false;
		if(filtro.getFechaFin() != null && getFecha().getTime() > filtro.getFechaFin().getTime())return false;
System.err.println("pasaFiltro----------"+filtro.getFechaFin()+" / "+getFecha().getTime());
System.err.println("pasaFiltro----------OK");
		return true;
	}

	// FIREBASE
	//______________________________________________________________________________________________
	//TODO
	//protected static Firebase newFirebase(){return new Firebase(FIREBASE).child(NOMBRE);}
	//protected static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}
	public interface ObjetoListener<T>
	{
		void onData(T[] aData);
		void onError(String err);
	}

	// PARCELABLE
	//______________________________________________________________________________________________
	protected Objeto(Parcel in)
	{
		setId(in.readString());
		setNombre(in.readString());
		setDescripcion(in.readString());
		setFecha(new Date(in.readLong()));
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(getNombre());
		dest.writeString(getDescripcion());
		dest.writeLong(getFecha().getTime());
	}
	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Parcelable.Creator<Objeto> CREATOR = new Parcelable.Creator<Objeto>()
	{
		@Override
		public Objeto createFromParcel(Parcel in)
		{
			return new Objeto(in);
		}
		@Override
		public Objeto[] newArray(int size)
		{
			return new Objeto[size];
		}
	};

}
