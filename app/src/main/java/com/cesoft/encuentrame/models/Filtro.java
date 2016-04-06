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
	public static final int NULO = -1;
	public static final int INACTIVO = 0;
	public static final int ACTIVO = 1;

	private int		_tipo = Util.NADA;
	private int 	_activo = Filtro.NULO;
	private String	_nombre;
	private Date	_fechaIni, _fechaFin;
	private LatLng	_punto;
	private int		_radio = Util.NADA;

	public int getTipo(){return _tipo;}
	public int getActivo(){return _activo;}
	public String getNombre(){return _nombre;}
	public Date getFechaIni(){return _fechaIni;}
	public Date getFechaFin(){return _fechaFin;}
	public LatLng getPunto(){return _punto;}
	public int getRadio(){return _radio;}

	public void setTipo(int v)
	{
		switch(v)
		{
		case Util.LUGARES:
		case Util.RUTAS:
		case Util.AVISOS:
			_tipo = v;
			break;
		default:
			_tipo = Util.NADA;
			break;
		}
	}
	public void setActivo(int v)
	{
		switch(v)
		{
		case Filtro.ACTIVO:
		case Filtro.INACTIVO:
			_activo = v;
			break;
		default:
			_activo = Filtro.NULO;
			break;
		}
	}
	public void setNombre(String v)
	{
		_nombre = v!=null ? v : "";//.replace("'", "\\'");//Wanna stop sql injection : they say there's no need
	}
	public void setFechaIni(Date v)
	{
		_fechaIni=v;
	}
	public void setFechaFin(Date v)
	{
		_fechaFin=v;
	}
	public void setPunto(LatLng v)
	{
		_punto = v!=null ? v : new LatLng(0,0);
	}
	public void setRadio(int v)
	{
		_radio = v>0 ? v : Util.NADA;
	}

	//______________________________________________________________________________________________
	//public Filtro(){}
	public Filtro(int tipo, int activo, String nombre, Date fechaIni, Date fechaFin, LatLng punto, int radio)
	{
		setTipo(tipo);
		setActivo(activo);
		setNombre(nombre);
		setFechaIni(fechaIni);
		setFechaFin(fechaFin);
		setPunto(punto);
		setRadio(radio);
	}
	//______________________________________________________________________________________________
	@Override
	public String toString()
	{
		DateFormat df = java.text.DateFormat.getDateTimeInstance();
		return String.format("%d, %d, %s, %.5f/%.5f %d, %s - %s", _tipo, _activo, _nombre, _punto.latitude, _punto.longitude, _radio, _fechaIni==null?"null":df.format(_fechaIni), _fechaFin==null?"null":df.format(_fechaFin));
	}

	//______________________________________________________________________________________________
	// 4 PARCELABLE
	protected Filtro(Parcel in)
	{
		_tipo = in.readInt();
		_activo = in.readInt();
		_nombre = in.readString();
		_punto = new LatLng(in.readDouble(), in.readDouble());
		_radio = in.readInt();
		long fi = in.readLong();
		if(fi > 0)	_fechaIni = new Date(fi);
		else		_fechaIni = null;
		long ff = in.readLong();
		if(ff > 0)	_fechaFin = new Date(ff);
		else		_fechaFin = null;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(_tipo);
		dest.writeInt(_activo);
		dest.writeString(_nombre);
		dest.writeDouble(_punto.latitude);
		dest.writeDouble(_punto.longitude);
		dest.writeInt(_radio);
		dest.writeLong(_fechaIni == null ? 0 : _fechaIni.getTime());
		dest.writeLong(_fechaFin == null ? 0 : _fechaFin.getTime());
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
