package com.cesoft.encuentrame3.models;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://github.com/firebase/geofire-java
public class ObjetoOld extends Objeto
{
	static final String GEO = "GEO";
	public static final String NOMBRE = "objeto";

	public static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);

	@Exclude public Long fechaLong;
	public Date fecha = new Date();//Old field, dont remove for old objects stored on firebase...

	//______________________________________________________________________________________________
	ObjetoOld() { fecha = new Date(); }
	//______________________________________________________________________________________________
	@NonNull
	@Override public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Objeto{id='%s', nombre='%s', descripcion='%s', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(fecha));
	}

	// PARCELABLE
	//______________________________________________________________________________________________
	ObjetoOld(Parcel in)
	{
		try {
			setId(in.readString());
			setNombre(in.readString());
			setDescripcion(in.readString());
			setLatLon(in.readDouble(), in.readDouble());
			setFecha(in.readLong());
		}
		catch(Exception e) {
			android.util.Log.e("Objeto", "Constructor-------------------------------------",e);
		}
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(getNombre());
		dest.writeString(getDescripcion());
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		dest.writeLong(getFecha());
	}
	@Override public int describeContents() { return 0; }
	public static final Creator<ObjetoOld> CREATOR = new Creator<ObjetoOld>()
	{
		@Override public ObjetoOld createFromParcel(Parcel in) { return new ObjetoOld(in); }
		@Override public ObjetoOld[] newArray(int size) { return new ObjetoOld[size]; }
	};
}
