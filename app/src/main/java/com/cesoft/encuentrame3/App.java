package com.cesoft.encuentrame3;

import android.app.Application;
import android.content.Context;

import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent;
import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
import com.cesoft.encuentrame3.svc.ActividadIntentService;
import com.cesoft.encuentrame3.svc.GeoTrackingJobService;
import com.cesoft.encuentrame3.svc.LoadGeofenceJobService;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.squareup.leakcanary.LeakCanary;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
public class App extends Application //implements ActivityCompat.OnRequestPermissionsResultCallback
{
	private GlobalComponent globalComponent;
	private static App instance;
		public static App getInstance() { return instance; }

	@Override public void onCreate()
	{
		super.onCreate();
		instance = this;

		if(LeakCanary.isInAnalyzerProcess(this))
		{
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);

		Fabric.with(this, new Crashlytics());

		getGlobalComponent();

		iniServicesDependantOnLogin();
	}

	public static GlobalComponent getComponent(Context context)
	{
		if(context == null)return null;
		return ((App)context.getApplicationContext()).getGlobalComponent();
	}
	public GlobalComponent getGlobalComponent()
	{
		if(globalComponent == null)
			globalComponent = DaggerGlobalComponent.builder()
				.globalModule(new GlobalModule(this))
				.build();
		return globalComponent;
	}

	public void iniServicesDependantOnLogin() {
		long delay = getGlobalComponent().pref().getTrackingDelay();
		ActividadIntentService.start(this);
		LoadGeofenceJobService.start(this);
		GeoTrackingJobService.start(this, delay);
		//WidgetRutaService.startSvc(this);//It's already started by GeoTrackingJobService
	}

	// Implements ActivityCompat.OnRequestPermissionsResultCallback
	/*@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		try {
			for(int i=0; i < permissions.length; i++)
				Log.e(TAG, "onRequestPermissionsResult------------------- requestCode = "
						+ requestCode + " : " + permissions[i] + " = " + grantResults[i]);
		}
		catch(Exception ignore){}
	}*/
}
