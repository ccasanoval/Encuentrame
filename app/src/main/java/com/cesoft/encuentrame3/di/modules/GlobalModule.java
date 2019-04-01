package com.cesoft.encuentrame3.di.modules;

import android.app.Application;
import android.app.NotificationManager;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Module
public class GlobalModule
{
	private final Application app;

	public GlobalModule(Application application) { app = application; }

	@Singleton
	@Provides
	Application provideApplication() { return app; }

	@Singleton
	@Provides
	SharedPreferences providePreferenceManager() {
		return PreferenceManager.getDefaultSharedPreferences(app);
	}

	@Singleton
	@Provides
	LocationManager provideLocationManager() {
		return (LocationManager)app.getSystemService(Context.LOCATION_SERVICE);
	}

	@Singleton
	@Provides
	NotificationManager provideNotificationManager() {
		return (NotificationManager)app.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Singleton
	@Provides
	PowerManager providePowerManager() {
		return (PowerManager)app.getSystemService(Context.POWER_SERVICE);
	}

	@Singleton
	@Provides
	JobScheduler provideJobScheduler() {
		return (JobScheduler)app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
	}

	@Singleton
	@Provides
	Preferencias providePreferencias(SharedPreferences sp) {
		return new Preferencias(sp);
	}
	@Singleton
	@Provides
	Util provideUtil(Application app, Preferencias pref, LocationManager lm, NotificationManager nm, PowerManager pm) {
		return new Util(app, pref, lm, nm, pm);
	}
	@Singleton
	@Provides
	Login provideLogin(SharedPreferences sp) {
		return new Login(sp);
	}

}
