package com.cesoft.encuentrame3.presenters

import android.app.Application
import com.cesoft.encuentrame3.models.Filtro
import com.cesoft.encuentrame3.util.Constantes.LUGARES
import com.cesoft.encuentrame3.util.Login
import com.cesoft.encuentrame3.util.Util
import com.cesoft.encuentrame3.util.Voice
import javax.inject.Inject
import javax.inject.Singleton

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/12/2019.
// PRESENTER MAIN
/*@Singleton
class PreFrgMain @Inject constructor(
        val app: Application,
        val util: Util,
        val login: Login,
        val voice: Voice) {*/
class PreFrgMain {

////////////////////////////////////////////////////
    interface IVista {

    }

    var sectionNumber: Int= LUGARES
    var view: IVista? = null
    ////////////////////////////////////////////////////

    //var sectionNumber: Int = LUGARES
    /*fun set(v: Int) {
            if(v == LUGARES || v == RUTAS || v == AVISOS)
                sectionNumber = v
        }*/

    private val filtro: Filtro? = null
    fun getFiltro(): Filtro? {
        return filtro
    }

    fun onCreate(view: IVista) {

    }


}