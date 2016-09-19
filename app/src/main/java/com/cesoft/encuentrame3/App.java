package com.cesoft.encuentrame3;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by CESoft on 15/09/2016
 */
public class App extends Application
{
	@Override public void onCreate()
	{
		super.onCreate();
		LeakCanary.install(this);
	}
}
