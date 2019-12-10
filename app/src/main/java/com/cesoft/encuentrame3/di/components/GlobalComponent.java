package com.cesoft.encuentrame3.di.components;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.ActBuscar;
import com.cesoft.encuentrame3.ActLugar;
import com.cesoft.encuentrame3.ActMaps;
import com.cesoft.encuentrame3.ActRuta;
import com.cesoft.encuentrame3.ActWidgetNuevaRuta;
import com.cesoft.encuentrame3.ActWidgetNuevoLugar;
import com.cesoft.encuentrame3.FrgLogin;
import com.cesoft.encuentrame3.FrgMain;
import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.adapters.RutaArrayAdapter;
import com.cesoft.encuentrame3.adapters.RutaViewHolder;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
import com.cesoft.encuentrame3.svc.GeofenceStore;
import com.cesoft.encuentrame3.svc.ServiceNotifications;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.util.Voice;
import com.cesoft.encuentrame3.widget.WidgetRutaJobService;

import javax.inject.Singleton;
import dagger.Component;

////////////////////////////////////////////////////////////////////////////////////////////////////
@Singleton
@Component(modules = {GlobalModule.class})
public interface GlobalComponent
{
	void inject(FrgLogin v);
	void inject(ActAviso v);
	void inject(ActLugar v);
	void inject(ActRuta v);
	void inject(ActMaps v);
	void inject(ActBuscar v);
	void inject(ActWidgetNuevaRuta v);
	void inject(ActWidgetNuevoLugar v);
	void inject(RutaArrayAdapter v);
	void inject(FrgMain v);
	void inject(WidgetRutaJobService v);
	void inject(RutaViewHolder v);

	Util util();
	Voice voice();
	Login login();
	Preferencias pref();
	GeofenceStore geofence();
	ServiceNotifications serviceNotifications();
}
