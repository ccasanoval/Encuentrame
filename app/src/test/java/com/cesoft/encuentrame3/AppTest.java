package com.cesoft.encuentrame3;

import android.app.Application;
import android.content.Context;
import com.cesoft.encuentrame3.di.components.GlobalComponentTest;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/05/2017.
//https://github.com/ecgreb?tab=repositories
public class AppTest  extends Application
{
	private static GlobalComponentTest _globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();
		getGlobalComponent();
	}

	public static GlobalComponentTest getComponent(Context context)
	{
		return ((AppTest)context.getApplicationContext()).getGlobalComponent();
	}
	public GlobalComponentTest getGlobalComponent()
	{
		/*if(_globalComponent == null)
			_globalComponent = DaggerGlobalComponentTest.builder()
					.globalModule(new GlobalModuleTest(this))
					.build();*/
		return _globalComponent;
	}
}
