package com.cesoft.encuentrame3.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 03/02/2016
public class CesOnSysBoot extends BroadcastReceiver
{
	private static final String TAG = CesOnSysBoot.class.getSimpleName();

 	@Override
	public void onReceive(Context context, Intent intent)
	{
		Preferencias pref = ((App)context.getApplicationContext()).getGlobalComponent().pref();
		if(intent.getAction() != null
			&& pref.isAutoArranque()
			&& intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
		{
			Log.e(TAG, "------------------CesOnSysBoot : onReceive : action="+intent.getAction());
		}
	}
}
