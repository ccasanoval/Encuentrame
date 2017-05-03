package com.cesoft.encuentrame3.di.modules;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Module
public class GlobalModule
{
	private final Application _app;

	public GlobalModule(Application application) { _app = application; }

	@Singleton
	@Provides
	Application provideApplication() { return _app; }

	@Singleton
	@Provides
	SharedPreferences providePreferenceManager()
	{
		return PreferenceManager.getDefaultSharedPreferences(_app);
	}

	@Singleton
	@Provides
	LocationManager provideLocationManager()
	{
		return (LocationManager)_app.getSystemService(Context.LOCATION_SERVICE);
	}

	@Singleton
	@Provides
	NotificationManager provideNotificationManager()
	{
		return (NotificationManager)_app.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Singleton
	@Provides
	PowerManager providePowerManager()
	{
		return (PowerManager)_app.getSystemService(Context.POWER_SERVICE);
	}

	/*@Singleton
	@Provides
	ConnectivityManager provideConnectivityManager() {
	return (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
	}*/

	@Singleton
	@Provides
	Util provideUtil(Application app, SharedPreferences sp, LocationManager lm, NotificationManager nm, PowerManager pm)
	{
		return new Util(app, sp, lm, nm, pm);
	}
	@Singleton
	@Provides
	Login provideLogin(SharedPreferences sp)
	{
		return new Login(sp);
	}

}
