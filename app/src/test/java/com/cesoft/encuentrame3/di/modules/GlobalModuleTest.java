package com.cesoft.encuentrame3.di.modules;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Module
public class GlobalModuleTest
{
	private final Application _app;

	public GlobalModuleTest(Application application) { _app = application; }

	@Singleton
	@Provides
	Application provideApplication() { return _app; }

	@Singleton
	@Provides
	SharedPreferences providePreferenceManager()
	{
		SharedPreferences o = Mockito.mock(SharedPreferences.class);
		return o;
	}

	@Singleton
	@Provides
	LocationManager provideLocationManager()
	{
		LocationManager o = Mockito.mock(LocationManager.class);
		return o;
	}

	@Singleton
	@Provides
	NotificationManager provideNotificationManager()
	{
		NotificationManager o = Mockito.mock(NotificationManager.class);
		return o;
	}

	@Singleton
	@Provides
	PowerManager providePowerManager()
	{
		PowerManager o = Mockito.mock(PowerManager.class);
		return o;
	}

	@Singleton
	@Provides
	Util provideUtil(Application app, Preferencias pref, LocationManager lm, NotificationManager nm, PowerManager pm)
	{
		return new Util(app, pref, lm, nm, pm);
	}
	@Singleton
	@Provides
	Login provideLogin(SharedPreferences sp)
	{
		Login o = Mockito.mock(Login.class);
		return o;
	}

}
