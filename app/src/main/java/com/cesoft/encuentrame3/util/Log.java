package com.cesoft.encuentrame3.util;

import com.cesoft.encuentrame3.BuildConfig;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/04/2017.
//^(?!.*(No setter)).*$
public class Log
{
	private static final String DOMINIO = "CESoft:";
	public static void w(String TAG, String msg)
	{
		if(BuildConfig.DEBUG) android.util.Log.w(TAG, DOMINIO+msg);
	}
	public static void e(String TAG, String msg)
	{
		if(BuildConfig.DEBUG) android.util.Log.e(TAG, DOMINIO+msg);
	}
	public static void e(String TAG, String msg, Throwable t)
	{
		if(BuildConfig.DEBUG) android.util.Log.e(TAG, DOMINIO+msg, t);
	}
}
