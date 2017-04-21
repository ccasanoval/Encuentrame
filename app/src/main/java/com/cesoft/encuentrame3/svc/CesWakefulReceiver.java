package com.cesoft.encuentrame3.svc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by booster-bikes on 21/04/2017.
//
public class CesWakefulReceiver extends WakefulBroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Start the service, keeping the device awake while the service is launching.
		Intent service = new Intent(context, CesService.class);
		startWakefulService(context, service);
	}
}
