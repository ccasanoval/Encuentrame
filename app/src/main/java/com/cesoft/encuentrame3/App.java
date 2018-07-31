package com.cesoft.encuentrame3;

import android.app.Application;
import android.content.Context;

import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent;
import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
import com.cesoft.encuentrame3.svc.ActividadIntentService;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.widget.WidgetRutaService;
import com.squareup.leakcanary.LeakCanary;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
public class App extends Application
{
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

		WidgetRutaService.startSvc(this);
		CesService.start(this);
		ActividadIntentService.start(this);
	}

	public static GlobalComponent getComponent(Context context)
	{
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
}
