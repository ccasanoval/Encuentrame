package com.cesoft.encuentrame3;

import android.app.Application;
import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent;
import com.cesoft.encuentrame3.di.components.GlobalComponent;
import com.cesoft.encuentrame3.di.modules.GlobalModule;
import com.cesoft.encuentrame3.svc.GeotrackingService;
import com.cesoft.encuentrame3.util.Log;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

//TODO:
// + Cuando borra se congela la UI ? y luego para atras y luego vuelves y te echa?
// + Traducir a kotlin, asi evito if(view!=null)
// + En lugar de menu a la derecha un DrawerMenu grande a la izquierda...con foto de usuario etc
// + Cuando se cambia de orientacion se recrea el activity, evitar!!
// + Presenter para ActMain !!
// + Opcion pausa en ruta ???

//TODO:  Adaptative Icons
//https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive
//https://developer.android.com/studio/write/image-asset-studio.html#create-adaptive

//TODO: Conectar con un smart watch en la ruta y cada punto que guarde bio-metrics...?!   --->   https://github.com/patloew

//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Opcion que diga no preguntar por activar GPS ni BATTERY (en tablet que no tiene gps...)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 15/09/2016
public class App extends Application //implements ActivityCompat.OnRequestPermissionsResultCallback
{
	private static final String TAG = App.class.getSimpleName();
	private static App instance = null;
	public static App getInstance() { return instance; }
	public static GlobalComponent getComponent() { return instance.getGlobalComponent(); }

	private GlobalComponent globalComponent;

	@Override public void onCreate()
	{
		super.onCreate();
		instance = this;

		Fabric.with(this, new Crashlytics());

		getGlobalComponent();

		iniServicesDependantOnLogin();
	}

	public GlobalComponent getGlobalComponent()
	{
		if(globalComponent == null)
			globalComponent = DaggerGlobalComponent.builder()
				.globalModule(new GlobalModule(this))
				.build();
		return globalComponent;
	}

	public void iniServicesDependantOnLogin() {
//TODO: Aqui y en ActMain despues de pedir permisos, por si al arrancar no arranca ActMain ???????
		GeotrackingService.start(this);
	}

}
