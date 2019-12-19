package com.cesoft.encuentrame3.views

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cesoft.encuentrame3.App.Companion.component
import com.cesoft.encuentrame3.R
import com.cesoft.encuentrame3.adapters.*
import com.cesoft.encuentrame3.models.*
import com.cesoft.encuentrame3.models.Fire.DatosListener
import com.cesoft.encuentrame3.presenters.PreFrgMain
import com.cesoft.encuentrame3.util.Constantes
import com.cesoft.encuentrame3.util.Log
import com.cesoft.encuentrame3.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova
class FrgMain
    : Fragment(), IListaItemClick, PreFrgMain.IVista
{
    private val util: Util = component.util()
    private val presenter = PreFrgMain()

    private var filtro: Filtro? = null
    fun getFiltro(): Filtro? {
        return filtro
    }

    //private var layoutManager: RecyclerView.LayoutManager? = null
    private var rootView: View? = null
    private var listView : RecyclerView? = null
    private var scrollState: Parcelable? = null //Recuerda posicion del scroll de la lista

    private var main: MainIterface? = null
    private var sectionNumber = Constantes.LUGARES
    fun getSectionNumber(): Int {
        return sectionNumber
    }

    //----------------------------------------------------------------------------------------------
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        scrollState = listView?.layoutManager?.onSaveInstanceState()
        state.putParcelable(LIST_SCROLL_STATE, scrollState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        component.inject(this)
        val args = arguments
        if (args != null) sectionNumber = args.getInt(ARG_SECTION_NUMBER)
        filtro = Filtro(sectionNumber)
        rootView = inflater.inflate(R.layout.act_main_frag, container, false)
        listView = rootView?.findViewById(R.id.listView)
        listView?.layoutManager = LinearLayoutManager(context)

        // Decorators
        val verticalSpacing = 20
        val itemDecorator = SpaceItemDecorator(verticalSpacing)
        listView?.addItemDecoration(itemDecorator)
        val shadowItemDecorator = ShadowItemDecorator(context, R.drawable.drop_shadow)
        listView?.addItemDecoration(shadowItemDecorator)

        if (sectionNumber < 0) {
            main?.gotoLogin()
            return null
        }
        val fab: FloatingActionButton = rootView!!.findViewById(R.id.fabNuevo)
        fab.setOnClickListener {
            when (sectionNumber) {
                Constantes.LUGARES -> main?.onLugar(false)
                Constantes.RUTAS -> main?.onRuta(false)
                Constantes.AVISOS -> main?.onAviso(false)
                else -> {}
            }
        }
        val textView = TextView(rootView!!.context)
        when (sectionNumber) {
            Constantes.LUGARES -> textView.text = getString(R.string.lugares)
            Constantes.RUTAS -> textView.text = getString(R.string.rutas)
            Constantes.AVISOS -> textView.text = getString(R.string.avisos)
            else -> {}
        }
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val view = rootView?.parent as ViewGroup?
        view?.removeView(rootView)
        rootView = null
        listView = null
    }


    //______________________________________________________________________________________________
    override fun onStart() {
        super.onStart()
        newListeners()
        when (sectionNumber) {
            Constantes.LUGARES -> refreshLugares()
            Constantes.RUTAS -> refreshRutas()
            Constantes.AVISOS -> refreshAvisos()
            else -> {}
        }
        /// Actualizar lista de rutas
        if (sectionNumber == Constantes.RUTAS) {
            util.setRefreshCallback(this)
            messageReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    refreshRutas()
                }
            }
        }
    }

    //______________________________________________________________________________________________
    override fun onStop() {
        super.onStop()
        messageReceiver = null
        if (sectionNumber == Constantes.RUTAS)
            util.setRefreshCallback(null)
        delListeners()
    }

    //______________________________________________________________________________________________
    override fun onResume() {
        super.onResume()
        if (sectionNumber == Constantes.RUTAS) {
            /*messageReceiver?.let {
                LocalBroadcastManager
                        .getInstance(instance)
                        .registerReceiver(it, IntentFilter(RUTA_REFRESH))
            }*/
            refreshRutas()
        }
    }

    override fun onPause() {
        super.onPause()
        scrollState = listView?.layoutManager?.onSaveInstanceState()
        /*if(sectionNumber == Constantes.RUTAS) {
            messageReceiver?.let {
                LocalBroadcastManager.getInstance(instance).unregisterReceiver(it)
            }
        }*/
    }


    //----------------------------------------------------------------------------------------------
    internal interface MainIterface {
        fun gotoLogin()
        fun onLugar(isVoiceCommand: Boolean)
        fun onAviso(isVoiceCommand: Boolean)
        fun onRuta(isVoiceCommand: Boolean)
        fun goLugar(obj: Objeto?)
        fun goAviso(obj: Objeto?)
        fun goRuta(obj: Objeto?)
        fun goLugarMap(obj: Objeto?)
        fun goAvisoMap(obj: Objeto?)
        fun goRutaMap(obj: Objeto?)
        fun buscar(f: FrgMain?)
        val currentItem: Int
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        main = if (context is MainIterface) {
            context
        } else {
            throw RuntimeException("$context must implement MainIterface")
        }
    }

    override fun onDetach() {
        super.onDetach()
        main = null
    }


    //// implements IListaItemClick
//______________________________________________________________________________________________
    override fun onItemEdit(tipo: Int, obj: Objeto?) {
        when (tipo) {
            Constantes.LUGARES -> main?.goLugar(obj)
            Constantes.AVISOS -> main?.goAviso(obj)
            Constantes.RUTAS -> main?.goRuta(obj)
            else -> {}
        }
    }

    override fun onItemMap(tipo: Int, obj: Objeto?) {
        when (tipo) {
            Constantes.LUGARES -> main?.goLugarMap(obj)
            Constantes.AVISOS -> main?.goAvisoMap(obj)
            Constantes.RUTAS -> main?.goRutaMap(obj)
            else -> {}
        }
    }

    // 4 IListaItemClick
    override fun onRefreshListaRutas() {
        //LocalBroadcastManager.getInstance(instance).sendBroadcast(Intent(RUTA_REFRESH))
    }


    //______________________________________________________________________________________________
    private var lisLugar: DatosListener<Lugar>? = null
    private var lisAviso: DatosListener<Aviso>? = null
    private var lisRuta: DatosListener<Ruta>? = null
    //----------------------------------------------------------------------------------------------
    private fun delListeners() {
        if (lisLugar != null) lisLugar!!.setListener(null)
        if (lisAviso != null) lisAviso!!.setListener(null)
        if (lisRuta != null) lisRuta!!.setListener(null)
    }

    private fun showMsgListaVacia() {
        try {
            Toast.makeText(context, R.string.lista_vacia, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "showToast:e:%s", e)
        }
    }

    private fun setLugaresListener() {
        lisLugar = object : DatosListener<Lugar>() {
            override fun onDatos(aLugares: Array<Lugar>) {
                val n = aLugares.size.toLong()
                if (n < 1) {
                    try {
                        if (main?.currentItem == Constantes.LUGARES) showMsgListaVacia()
                    } catch (e: Exception) {
                        Log.e(TAG, String.format("_acLugar:%s", e), e)
                    }
                }
                scrollState = listView?.layoutManager?.onSaveInstanceState()
                listView?.layoutManager?.onRestoreInstanceState(scrollState)
                listView?.adapter = LugarArrayAdapter(aLugares, this@FrgMain)
                listView?.contentDescription = getString(R.string.lugares) //Para Espresso
            }

            override fun onError(err: String) //FirebaseError err)
            {
                Log.e(TAG, "LUGARES2:GET:e:----------------------------------------------------$err")
            }
        }
    }

    private fun setRutasListener() {
        lisRuta = object : DatosListener<Ruta>() {
            override fun onDatos(aRutas: Array<Ruta>) {
                val n = aRutas.size.toLong()
                if (n < 1) {
                    try {
                        if (main?.currentItem == Constantes.RUTAS) showMsgListaVacia()
                    }
                    catch (e: Exception) {
                        Log.e(TAG, "_acRuta:e:-------------------------------------------------", e)
                    }
                }
                scrollState = listView?.layoutManager?.onSaveInstanceState()
                listView?.layoutManager?.onRestoreInstanceState(scrollState)
                val r = RutaArrayAdapter(aRutas, this@FrgMain)
                listView?.adapter = r
                listView?.contentDescription = getString(R.string.rutas) //Para Espresso
                r.notifyDataSetChanged()
            }
            override fun onError(err: String) {
                Log.e(TAG, "RUTAS2:GET:e:------------------------------------------------------$err")
            }
        }
    }

    private fun setAvisoListener() {
        lisAviso = object : DatosListener<Aviso>() {
            override fun onDatos(aAvisos: Array<Aviso>) {
                val n = aAvisos.size.toLong()
                if (n < 1 && main?.currentItem == Constantes.AVISOS) {
                    showMsgListaVacia()
                }
                scrollState = listView?.layoutManager?.onSaveInstanceState()
                listView?.layoutManager?.onRestoreInstanceState(scrollState)
                listView?.adapter = AvisoArrayAdapter(aAvisos, this@FrgMain)
                listView?.contentDescription = getString(R.string.avisos) //Para Espresso
            }

            override fun onError(err: String) {
                Log.e(TAG, "AVISOS2:GET:e:-----------------------------------------------------$err")
            }
        }
    }

    private fun newListeners() {
        delListeners()
        //--- LUGARES
        setLugaresListener()
        //--- RUTAS
        setRutasListener()
        //--- AVISO
        setAvisoListener()
    }

    //______________________________________________________________________________________________
    private fun refreshLugares() {
        if (filtro == null) {
            Log.e(TAG, "refreshLugares:-------------------------------------------------------- FILTRO = NULL   $sectionNumber")
            filtro = Filtro(sectionNumber)
        }
        if (filtro!!.isOn) {
            checkFechas()
            Lugar.getLista(lisLugar, filtro)
        } else Lugar.getLista(lisLugar!!)
    }

    //______________________________________________________________________________________________
    private fun refreshRutas() {
        if (filtro!!.isOn) {
            checkFechas()
            Ruta.getLista(lisRuta, filtro, util!!.idTrackingRoute)
        } else Ruta.getLista(lisRuta)
    }

    //______________________________________________________________________________________________
    private fun refreshAvisos() {
        if (filtro!!.isOn) {
            checkFechas()
            Aviso.getLista(lisAviso, filtro)
        } else Aviso.getLista(lisAviso)
    }

    //______________________________________________________________________________________________
    private fun checkFechas() {
        val ini = filtro!!.fechaIni
        val fin = filtro!!.fechaFin
        if (ini != null && fin != null && ini.time > fin.time) {
            filtro!!.fechaIni = fin
            filtro!!.fechaFin = ini
        }
    }


    //----------------------------------------------------------------------------------------------
    // Recoge el resultado de startActivityForResult : buscar
    private fun processDataResult(data: Intent?) {
        if (data != null) {
            try {
                val sMensaje = data.getStringExtra(MENSAJE)
                if (sMensaje != null && !sMensaje.isEmpty()) Toast.makeText(context, sMensaje, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "processDataResult:e:-----------------------------------------------", e)
            }
            if (!data.getBooleanExtra(DIRTY, true)) return
            val filtro0: Filtro? = data.getParcelableExtra(Filtro.FILTRO)
            if(filtro?.tipo != Constantes.NADA)
                filtro = filtro0
            if(filtro?.isOn == false)
                Toast.makeText(context, getString(R.string.sin_filtro), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_OK) return
        processDataResult(data)
        var requestCode2 = requestCode
        if(requestCode == Constantes.BUSCAR && filtro != null)
            requestCode2 = filtro!!.tipo
        when(requestCode2) {
            Constantes.LUGARES -> refreshLugares()
            Constantes.RUTAS -> refreshRutas()
            Constantes.AVISOS -> refreshAvisos()
            else -> {}
        }
    }

    //----------------------------------------------------------------------------------------------
    companion object {
        private val TAG = FrgMain::class.java.simpleName
        private const val ARG_SECTION_NUMBER = "ARG_SECTION_NUMBER"
        private const val LIST_SCROLL_STATE = "LIST_SCROLL_STATE"
        //private const val RUTA_REFRESH = "RUTA_REFRESH"
        private const val MENSAJE = "MENSAJE"
        private const val DIRTY = "DIRTY"
        private var messageReceiver: BroadcastReceiver? = null

        @JvmStatic
        fun newInstance(sectionNumber: Int): FrgMain? {
            val fragment = FrgMain()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}