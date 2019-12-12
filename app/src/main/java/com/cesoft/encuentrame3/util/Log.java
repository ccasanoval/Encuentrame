package com.cesoft.encuentrame3.util;

import com.cesoft.encuentrame3.BuildConfig;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/04/2017.
//^(?!.*(No setter)).*$
public class Log
{
	private static final boolean FORCE_LOG = false;
	private static final String PREFIX = "CESoft:";
	private Log(){}
//	public static void d(String tag, String msg) {
//		if(FORCE_LOG || BuildConfig.DEBUG) android.util.Log.d(tag, PREFIX+msg);
//	}
	public static void w(String tag, String msg) {
		if(FORCE_LOG || BuildConfig.DEBUG) android.util.Log.w(tag, PREFIX+msg);
	}
	public static void e(String tag, String msg) {
		if(FORCE_LOG || BuildConfig.DEBUG) android.util.Log.e(tag, PREFIX+msg);
	}
	public static void e(String tag, String msg, Throwable t) {
		if(FORCE_LOG || BuildConfig.DEBUG) android.util.Log.e(tag, PREFIX+msg, t);
	}
}
