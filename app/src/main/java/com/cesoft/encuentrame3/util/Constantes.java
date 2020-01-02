package com.cesoft.encuentrame3.util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova on 25/04/2017.
public class Constantes
{
	private Constantes(){}

	// GPS TRACKING SERVICE
	public static final long MIN_TRACK_DELAY = 30*1000L;
	public static final long ACCURACY_MAX = 38L;//m
	public static final long DISTANCE_MIN = 5L;//m
	//public static final long DISTANCE_MAX = 50;//m
	public static final long GEOFENCE_LOAD_DELAY =		30*60*1000L;//5min
	public static final long GEOFENCE_EXPIRE_DELAY =	60*60*1000L;//1h
	public static final int GEOFENCE_RESPONSE_DELAY =	   60*1000; //1min
	//public static final int GEOFENCE_DWELL_DELAY =	   60*1000; //1min

	public static final long SPEED_MAX = 80L;// m/s => 50=180km/h 60=216Km/h 80=288km/h
	public static final long ACCEL_MAX = 7L;// m/s2
	// Cuando se actualiza el widget?
	public static final long WIDGET_DELAY_SHORT =	  60*1000L;//1min
	public static final long WIDGET_DELAY_LONG =	5*60*1000L;//5min
	// Delay de servicio deteccion actividad
	public static final long DELAY_ACTIVITY_DETECTION = MIN_TRACK_DELAY;

	//
	public static final int NADA=-1;
	public static final int LUGARES=0;
	public static final int RUTAS=1;
	public static final int AVISOS=2;
	public static final int BUSCAR=9;
	public static final String SETTINGS_PAGE = "settings_page";
	public static final String WIN_TAB = "wintab";
	public static final String MENSAJE = "mensaje";
	static final String DIRTY = "dirty";
	public static final String NOTIF = "notificacion";

	public static final int ID_JOB_WIDGET = 71;
}
