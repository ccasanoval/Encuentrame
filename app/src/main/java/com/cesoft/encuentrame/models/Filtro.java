package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.Util;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 05/04/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Filtro implements Parcelable
{
	public static final String FILTRO = "filtro";
	private int		_tipo = Util.NADA;
	private String	_nombre;
	private Date	_fechaIni, _fechaFin;
	private LatLng	_punto;
	private int		_radio;

	public int getTipo(){return _tipo;}
	public String getNombre(){return _nombre;}
	public Date getFechaIni(){return _fechaIni;}
	public Date getFechaFin(){return _fechaFin;}
	public LatLng getPunto(){return _punto;}
	public int getRadio(){return _radio;}

	//______________________________________________________________________________________________
	//public Filtro(){}
	public Filtro(int tipo, String nombre, Date fechaIni, Date fechaFin, LatLng punto, int radio)
	{
		switch(tipo)
		{
		case Util.LUGARES:
		case Util.RUTAS:
		case Util.AVISOS:
			_tipo = tipo;
			break;
		}
		_nombre = nombre;
		_fechaIni = fechaIni;
		_fechaFin = fechaFin;
		_punto = punto != null?punto:new LatLng(0,0);
		_radio = radio;
	}

	@Override
	public String toString()
	{
		DateFormat df = java.text.DateFormat.getDateTimeInstance();
		return String.format("%d, %s, %.5f/%.5f %d, %s - %s", _tipo, _nombre, _punto.latitude, _punto.longitude, _radio, df.format(_fechaIni), df.format(_fechaFin));
	}

	//______________________________________________________________________________________________
	protected Filtro(Parcel in)
	{
		//super(in);
		//
		_tipo = in.readInt();
		_nombre = in.readString();
		_fechaIni = new Date(in.readLong());
		_fechaFin = new Date(in.readLong());
		_punto = new LatLng(in.readDouble(), in.readDouble());
		_radio = in.readInt();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		//super.writeToParcel(dest, flags);
		//
		dest.writeInt(_tipo);
		dest.writeString(_nombre);
		dest.writeLong(_fechaIni.getTime());
		dest.writeLong(_fechaFin.getTime());
		dest.writeDouble(_punto.latitude);
		dest.writeDouble(_punto.longitude);
		dest.writeInt(_radio);
	}
	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Creator<Filtro> CREATOR = new Creator<Filtro>()
	{
		@Override
		public Filtro createFromParcel(Parcel in)
		{
			return new Filtro(in);
		}
		@Override
		public Filtro[] newArray(int size)
		{
			return new Filtro[size];
		}
	};
}
