package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.widget.WidgetRutaJobService;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 03/02/2016
public class SystemBootReceiver extends BroadcastReceiver
{
	private static final String TAG = SystemBootReceiver.class.getSimpleName();

 	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.e(TAG, "onReceive:---------------------------ACTION="+intent.getAction());
		Preferencias pref = App.getComponent().pref();
		if(intent.getAction() != null
			&& pref.isAutoArranque())
			//&& intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		{
			GeofencingService.start(context);
			GeotrackingService.start(context);
			WidgetRutaJobService.start(context);
		}
	}
}
