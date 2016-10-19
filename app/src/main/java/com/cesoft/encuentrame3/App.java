package com.cesoft.encuentrame3;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by CESoft on 15/09/2016
 */
public class App extends Application
{
	@Override public void onCreate()
	{
		super.onCreate();

		//LeakCanary.install(this);

		Picasso.Builder builder = new Picasso.Builder(this);
		builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
		Picasso built = builder.build();
		built.setIndicatorsEnabled(true);
		built.setLoggingEnabled(true);
		Picasso.setSingletonInstance(built);
	}
}
