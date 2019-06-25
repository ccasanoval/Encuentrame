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


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
//TODO: sin conexion debería poder empezar una nueva ruta!!!!!!
//TODO: mediante ordenes de voz preguntar sobre ruta actual, empezar ruta, guardar punto...
public class App extends Application //implements ActivityCompat.OnRequestPermissionsResultCallback
{
	private GlobalComponent globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();

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

}
