package com.cesoft.encuentrame3;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent;
import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
import com.cesoft.encuentrame3.svc.ActividadIntentService;
import com.cesoft.encuentrame3.svc.GeoTrackingJobService;
import com.cesoft.encuentrame3.svc.LoadGeofenceJobService;
import com.cesoft.encuentrame3.util.Log;
import com.squareup.leakcanary.LeakCanary;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
public class App extends Application implements ActivityCompat.OnRequestPermissionsResultCallback
{
	private static String TAG = App.class.getSimpleName();
	private static GlobalComponent _globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();

		if(LeakCanary.isInAnalyzerProcess(this))
		{
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);

		getGlobalComponent();

		iniServicesDependantOnLogin();

		Log.e(TAG, "onCreate:-------------------------------------------");
	}

	public static GlobalComponent getComponent(Context context)
	{
		if(context == null)return null;
		return ((App)context.getApplicationContext()).getGlobalComponent();
	}
	public GlobalComponent getGlobalComponent()
	{
		if(_globalComponent == null)
			_globalComponent = DaggerGlobalComponent.builder()
				.globalModule(new GlobalModule(this))
				.build();
		return _globalComponent;
	}

	public void iniServicesDependantOnLogin() {
		ActividadIntentService.start(this);
		LoadGeofenceJobService.start(this);
		GeoTrackingJobService.start(this);
		//WidgetRutaService.startSvc(this);//It's already started by GeoTrackingJobService
	}

	// 4 implements ActivityCompat.OnRequestPermissionsResultCallback
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		try {
			for(int i=0; i < permissions.length; i++)
				Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
						+ requestCode + " : " + permissions[i] + " = " + grantResults[i]);
		}catch(Exception ignore){}
	}
}
