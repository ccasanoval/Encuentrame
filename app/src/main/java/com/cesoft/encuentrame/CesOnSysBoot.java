package com.cesoft.encuentrame;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame.Util;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 03/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class CesOnSysBoot extends BroadcastReceiver
{
 	@Override
	public void onReceive(Context context, Intent intent)
	{
System.err.println("------------------CesOnSysBoot : onReceive : action="+intent.getAction()+":::"+ Util.isAutoArranque(context));
		if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) && Util.isAutoArranque(context))
		{
			Util.setApplication((Application)context.getApplicationContext());
			Intent serviceIntent = new Intent(context, CesService.class);
			context.startService(serviceIntent);
		}
	}
}
