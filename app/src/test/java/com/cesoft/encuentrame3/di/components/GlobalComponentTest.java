package com.cesoft.encuentrame3.di.components;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.ActBuscar;
import com.cesoft.encuentrame3.ActLogin;
import com.cesoft.encuentrame3.ActLugar;
import com.cesoft.encuentrame3.ActMaps;
import com.cesoft.encuentrame3.ActRuta;
import com.cesoft.encuentrame3.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.ActWidgetNuevoLugar;
import com.cesoft.encuentrame3.FrgMain;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.adapters.RutaArrayAdapter;
import com.cesoft.encuentrame3.di.modules.GlobalModuleTest;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.widget.WidgetRutaService;

import javax.inject.Singleton;

import dagger.Component;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
@Component(modules = {GlobalModuleTest.class})
public interface GlobalComponentTest
{
	void inject(ActLogin.PlaceholderFragment v);
	void inject(ActAviso v);
	void inject(ActLugar v);
	void inject(ActRuta v);
	void inject(ActMaps v);
	void inject(ActBuscar v);
	void inject(ActWidgetNuevaRuta v);
	void inject(ActWidgetNuevoLugar v);
	void inject(WidgetRutaService v);
	void inject(RutaArrayAdapter v);
	void inject(FrgMain v);
	void inject(CesService v);

	Util util();
	Login login();
}
