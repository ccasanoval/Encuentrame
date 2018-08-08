package com.cesoft.encuentrame3.util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova on 25/04/2017.
public class Constantes
{
	// GPS TRACKING SERVICE
	public static final int GEOFEN_DWELL_TIME = 2*60*1000;
	public static final long DELAY_TRACK_MIN = 20*1000;//30*1000;
	//public static final long DELAY_TRACK_MAX = 5*60*1000;//7*60*1000;
	public static final long ACCURACY_MAX = 15;//m
	public static final long DISTANCE_MIN = 7;//m
	//public static final long DISTANCE_MAX = 50;//m
	public static final long DELAY_LOAD_GEOFENCE = 5*60*1000;
	public static final long SPEED_MAX = 60;// m/s => 50=180km/h 60=216Km/h
	public static final long ACCEL_MAX = 6;// m/s2
	// Cuando se actualiza el widget?
	public static final long WIDGET_DELAY_SHORT = 2*DELAY_TRACK_MIN;
	public static final long WIDGET_DELAY_LONG = DELAY_LOAD_GEOFENCE;

	//
	public static final int NADA=-1, LUGARES=0, RUTAS=1, AVISOS=2, BUSCAR=9, CONFIG=10;
	public static final String WIN_TAB = "wintab", MENSAJE = "mensaje";
	static final String DIRTY = "dirty";
	public static final String NOTIF = "notificacion";

	public static final int ID_JOB_GEOFENCE_LOADING = 69;
	public static final int ID_JOB_TRACKING = 70;
}
