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
	private static App _app = new App();
	public static App getInstance(){return _app;}

	private static GlobalComponent _globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();
		getGlobalComponent();
		//LeakCanary.install(this);
		/*Picasso.Builder builder = new Picasso.Builder(this);
		builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
		Picasso built = builder.build();
		built.setIndicatorsEnabled(true);
		built.setLoggingEnabled(true);
		Picasso.setSingletonInstance(built);*/
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
