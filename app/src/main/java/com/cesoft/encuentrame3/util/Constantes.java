package com.cesoft.encuentrame3.util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova on 25/04/2017.
public class Constantes
{
	// GPS TRACKING SERVICE
	public static final int GEOFEN_DWELL_TIME = 60*1000;
	public static final long DELAY_TRACK_MIN = 20*1000;//30*1000;
	public static final long DELAY_TRACK_MAX = 4*60*1000;//7*60*1000;
	public static final long ACCURACY_MAX = 24;//m
	public static final long DISTANCE_MIN = 10;//m
	public static final long DELAY_LOAD = DELAY_TRACK_MAX;
	// Cuando se actualiza el widget?
	public static final long WIDGET_DELAY_SHORT = DELAY_TRACK_MIN;
	public static final long WIDGET_DELAY_LONG = DELAY_TRACK_MAX;

	//
	public static final int NADA=-1, LUGARES=0, RUTAS=1, AVISOS=2, BUSCAR=9, CONFIG=10;
	public static final String WIN_TAB = "wintab", MENSAJE = "mensaje", DIRTY = "dirty";
}
