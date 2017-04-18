package com.cesoft.encuentrame3.di.components;

import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.ActWidgetNuevoLugar;
import com.cesoft.encuentrame3.FrgMain;
import com.cesoft.encuentrame3.RutaArrayAdapter;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.di.modules.GlobalModule;

import javax.inject.Singleton;
import dagger.Component;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
@Component(modules = {GlobalModule.class})
public interface GlobalComponent
{
	void inject(CesService v);
	void inject(ActWidgetNuevaRuta v);
	void inject(ActWidgetNuevoLugar v);
	void inject(RutaArrayAdapter v);
	void inject(FrgMain v);

	//SharedPreferences sharedPreferences();
	//LocationManager locationManager();
	Util util();
	Login login();
}
