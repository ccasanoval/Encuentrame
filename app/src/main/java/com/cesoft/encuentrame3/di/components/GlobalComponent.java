package com.cesoft.encuentrame3.di.components;

import android.app.Application;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.ActWidgetNuevoLugar;
import com.cesoft.encuentrame3.CesService;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.Util;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
//import com.cesoft.encuentrame3.di.modules.UtilModule;

import javax.inject.Singleton;
import dagger.Component;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
@Component(modules = {GlobalModule.class})
public interface GlobalComponent
{
	void inject(CesService v);
	void inject(ActMain.PlaceholderFragment v);
	void inject(ActWidgetNuevaRuta v);
	void inject(ActWidgetNuevoLugar v);

	//SharedPreferences sharedPreferences();
	//LocationManager locationManager();
	Util util();
	Login login();
}
