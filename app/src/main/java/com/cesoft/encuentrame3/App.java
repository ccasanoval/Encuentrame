package com.cesoft.encuentrame3;

import android.app.Application;

import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent;
import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
//import com.cesoft.encuentrame3.di.modules.UtilModule;


//import com.squareup.leakcanary.LeakCanary;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
public class App extends Application
{
	GlobalComponent _globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();

		//LeakCanary.install(this);

		/*Picasso.Builder builder = new Picasso.Builder(this);
		builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
		Picasso built = builder.build();
		built.setIndicatorsEnabled(true);
		built.setLoggingEnabled(true);
		Picasso.setSingletonInstance(built);*/

		_globalComponent = DaggerGlobalComponent.builder()
                .globalModule(new GlobalModule(this))
                .build();
	}

	 public GlobalComponent getGlobalComponent() { return _globalComponent; }
}
