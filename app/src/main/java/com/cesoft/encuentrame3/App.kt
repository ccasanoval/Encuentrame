package com.cesoft.encuentrame3

import android.app.Application
import com.cesoft.encuentrame3.di.components.DaggerGlobalComponent
import com.cesoft.encuentrame3.di.components.GlobalComponent
import com.cesoft.encuentrame3.di.modules.GlobalModule
import com.cesoft.encuentrame3.svc.GeotrackingService
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric


//TODO:
// + Configurable: maxima imprecision permitida en metros (ACCURACY_MAX)! Permitir puntos imprecisos mira solo velocidad, etc
// + mas comandos de voz?
// + cambia widget para radio en ventana de busqueda como en aviso!
// + cuando no hay conexion y se guarda/empieza una ruta, puede quedar una ruta sin puntos, se para la ruta? NO SIMEPRE FALLA?!
// + Menu ayuda que explique las tres funciones LUGARES, RUTAS, AVISOS
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
class App : Application()//implements ActivityCompat.OnRequestPermissionsResultCallback
{
    override fun onCreate() {
        super.onCreate()
        instance = this
        component = DaggerGlobalComponent.builder()
                .globalModule(GlobalModule(this))
                .build()
        Fabric.with(this, Crashlytics())
        iniServicesDependantOnLogin()
    }

    //TODO: Aqui y en ActMain despues de pedir permisos, por si al arrancar no arranca ActMain ???????
    fun iniServicesDependantOnLogin() {
        GeotrackingService.start(this)
    }

    companion object {
        private val TAG = App::class.java.simpleName
        @JvmStatic
        lateinit var instance: App
            private set
        @JvmStatic
        lateinit var component: GlobalComponent
            private set
    }
}
