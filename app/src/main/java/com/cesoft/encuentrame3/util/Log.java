package com.cesoft.encuentrame3.util;

import com.cesoft.encuentrame3.BuildConfig;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/04/2017.
//^(?!.*(No setter)).*$
public class Log
{
	private Log(){}
	public static void d(String tag, String msg) {
		if(BuildConfig.DEBUG) android.util.Log.d(tag, msg);
	}
	public static void w(String tag, String msg) {
		if(BuildConfig.DEBUG) android.util.Log.w(tag, msg);
	}
	public static void e(String tag, String msg) {
		if(BuildConfig.DEBUG) android.util.Log.e(tag, msg);
	}
	public static void e(String tag, String msg, Throwable t) {
		if(BuildConfig.DEBUG) android.util.Log.e(tag, msg, t);
	}
}
