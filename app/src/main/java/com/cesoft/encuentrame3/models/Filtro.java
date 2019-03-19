package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cesoft.encuentrame3.util.Constantes;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 05/04/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Filtro implements Parcelable
{
	public static final String FILTRO = "filtro";
	private static final int TODOS = -1;
	public static final int INACTIVO = 0;
	public static final int ACTIVO = 1;

	private boolean onOff = false;
		public boolean isOn(){return onOff;}
		public void turnOn(){
			onOff = true;}
		public void turnOff(){
			onOff = false;}
		public boolean isValid()
		{
			return !(activo == TODOS && nombre.isEmpty() && fechaIni == null && fechaFin == null && punto.latitude == 0 && punto.longitude == 0);
		}

	private int		tipo = Constantes.NADA;
	private int 	activo = Filtro.TODOS;
	private String	nombre = "";
	private Date	fechaIni;
	private Date	fechaFin;
	private LatLng	punto = new LatLng(0,0);
	private int		radio = Constantes.NADA;

	public int getTipo(){return tipo;}
	public int getActivo(){return activo;}
	public String getNombre(){return nombre;}
	public Date getFechaIni(){return fechaIni;}
	public Date getFechaFin(){return fechaFin;}
	public LatLng getPunto(){return punto;}
	public int getRadio(){return radio;}

	private void setTipo(int v)
	{
		switch(v)
		{
		case Constantes.LUGARES:
		case Constantes.RUTAS:
		case Constantes.AVISOS:
			tipo = v;
			break;
		default:
			tipo = Constantes.NADA;
			break;
		}
	}
	public void setActivo(int v)
	{
		switch(v)
		{
		case Filtro.ACTIVO:
		case Filtro.INACTIVO:
			activo = v;
			break;
		default:
			activo = Filtro.TODOS;
			break;
		}
	}
	public void setNombre(String v)//.replace("'", "\\'");//Wanna stop sql injection : they say there's no need
	{
		nombre = v!=null ? v : "";
	}
	public void setFechaIni(Date v) { fechaIni =v; }
	public void setFechaFin(Date v) { fechaFin =v; }
	public void setPunto(LatLng v) { punto = v!=null ? v : new LatLng(0,0); }
	public void setRadio(int v) { radio = v>0 ? v : Constantes.NADA; }

	//______________________________________________________________________________________________
	public Filtro(int tipo)
	{
		turnOff();
		setTipo(tipo);
	}

	//______________________________________________________________________________________________
	@NonNull
	@Override
	public String toString()
	{
		DateFormat df = java.text.DateFormat.getDateTimeInstance();
		return String.format(Locale.ENGLISH, "{%b, %d, %d, '%s', %.5f/%.5f %d, %s - %s}",
				onOff, tipo, activo, nombre, punto.latitude, punto.longitude, radio,
				fechaIni ==null?"null":df.format(fechaIni), fechaFin ==null?"null":df.format(fechaFin));
	}

	//______________________________________________________________________________________________
	// 4 PARCELABLE
	private Filtro(Parcel in)
	{
		onOff = in.readInt() > 0;
		tipo = in.readInt();
		activo = in.readInt();
		nombre = in.readString();
		punto = new LatLng(in.readDouble(), in.readDouble());
		radio = in.readInt();
		long fi = in.readLong();
		if(fi > 0)	fechaIni = new Date(fi);
		else		fechaIni = null;
		long ff = in.readLong();
		if(ff > 0)	fechaFin = new Date(ff);
		else		fechaFin = null;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(onOff ?1:0);
		dest.writeInt(tipo);
		dest.writeInt(activo);
		dest.writeString(nombre);
		dest.writeDouble(punto.latitude);
		dest.writeDouble(punto.longitude);
		dest.writeInt(radio);
		dest.writeLong(fechaIni == null ? 0 : fechaIni.getTime());
		dest.writeLong(fechaFin == null ? 0 : fechaFin.getTime());
	}
	@Override public int describeContents() { return 0; }
	public static final Creator<Filtro> CREATOR = new Creator<Filtro>()
	{
		@Override public Filtro createFromParcel(Parcel in) { return new Filtro(in); }
		@Override public Filtro[] newArray(int size) { return new Filtro[size]; }
	};
}
