package com.cesoft.encuentrame3.util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova on 25/04/2017.
public class Constantes
{
	private Constantes(){}

	// GPS TRACKING SERVICE
	public static final int GEOFEN_DWELL_TIME = 2*60*1000;
	public static final long DELAY_TRACK_MIN = 30*1000;
	public static final long ACCURACY_MAX = 30;//m
	public static final long DISTANCE_MIN = 5;//m
	//public static final long DISTANCE_MAX = 50;//m
	public static final long DELAY_LOAD_GEOFENCE = 5*60*1000;
	public static final long SPEED_MAX = 80;// m/s => 50=180km/h 60=216Km/h 80=288km/h
	public static final long ACCEL_MAX = 7;// m/s2
	// Cuando se actualiza el widget?
	public static final long WIDGET_DELAY_SHORT = DELAY_TRACK_MIN;
	public static final long WIDGET_DELAY_LONG = DELAY_LOAD_GEOFENCE;
	// Delay de servicio deteccion actividad
	public static final long DELAY_ACTIVITY_DETECTION = DELAY_TRACK_MIN;

	//
	public static final int NADA=-1, LUGARES=0, RUTAS=1, AVISOS=2, BUSCAR=9, CONFIG=10;
	public static final String WIN_TAB = "wintab", MENSAJE = "mensaje";
	static final String DIRTY = "dirty";
	public static final String NOTIF = "notificacion";

	public static final int ID_JOB_GEOFENCE_LOADING = 69;
	public static final int ID_JOB_TRACKING = 70;
	public static final int ID_JOB_WIDGET = 71;
}
