package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 03/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class CesOnSysBoot extends BroadcastReceiver
{
 	@Override
	public void onReceive(Context context, Intent intent)
	{
//System.err.println("------------------CesOnSysBoot : onReceive : action="+intent.getAction()+":::"+ Util.isAutoArranque(context));
		Util _util = ((App)context.getApplicationContext()).getGlobalComponent().util();
		if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) && _util.isAutoArranque())
		{
			// Geotracking Svc
			Intent serviceIntent = new Intent(context, CesService.class);
			context.startService(serviceIntent);
			// Rute Widget Svc
			WidgetRutaService.startServ(context);
		}
	}
}
