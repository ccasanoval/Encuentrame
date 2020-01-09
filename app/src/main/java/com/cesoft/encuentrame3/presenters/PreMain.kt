package com.cesoft.encuentrame3.presenters

import android.app.Activity
import android.app.Application
import com.cesoft.encuentrame3.util.Login
import com.cesoft.encuentrame3.util.Util
import com.cesoft.encuentrame3.util.Voice
import javax.inject.Inject
import javax.inject.Singleton

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/12/2019.
// PRESENTER MAIN
//TODO: common interface for all presenters... subscribe and unsubscribe
@Singleton
class PreMain @Inject constructor(
        val app: Application,
        val util: Util,
        val login: Login,
        val voice: Voice) {

    ////////////////////////////////////////////////////
    interface IVista {
        val act: Activity?
        fun gotoLogin()
        fun finish()
        fun iniEspera()
        fun finEspera()
        fun toast(msg: Int)
        fun toast(msg: Int, err: String?)
    }

    var view: IVista? = null
    ////////////////////////////////////////////////////

    fun onCreate(view: IVista) {
        this.view = view
        if (!login.isLogged) {
            login.logout()
            view.gotoLogin()
        }
    }

    fun subscribe(view: IVista?) {
        this.view = view

    }

    fun unsubscribe() {
        view = null
    }
}