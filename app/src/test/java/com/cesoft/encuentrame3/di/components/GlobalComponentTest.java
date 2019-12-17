package com.cesoft.encuentrame3.di.components;

import com.cesoft.encuentrame3.views.ActAviso;
import com.cesoft.encuentrame3.views.ActBuscar;
import com.cesoft.encuentrame3.views.ActLugar;
import com.cesoft.encuentrame3.views.ActMaps;
import com.cesoft.encuentrame3.views.ActRuta;
import com.cesoft.encuentrame3.views.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.views.ActWidgetNuevoLugar;
import com.cesoft.encuentrame3.views.FrgMain;
import com.cesoft.encuentrame3.util.Login;
import com.cesoft.encuentrame3.adapters.RutaArrayAdapter;
import com.cesoft.encuentrame3.di.modules.GlobalModuleTest;
import com.cesoft.encuentrame3.svc.ServiceNotifications;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Singleton;

import dagger.Component;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
@Component(modules = {GlobalModuleTest.class})
public interface GlobalComponentTest
{
	void inject(ActAviso v);
	void inject(ActLugar v);
	void inject(ActRuta v);
	void inject(ActMaps v);
	void inject(ActBuscar v);
	void inject(ActWidgetNuevaRuta v);
	void inject(ActWidgetNuevoLugar v);
	void inject(RutaArrayAdapter v);
	void inject(FrgMain v);

	Util util();
	Login login();
	ServiceNotifications serviceNotifications();
}
