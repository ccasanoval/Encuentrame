package com.cesoft.encuentrame3.presenters;

import android.app.AlertDialog;
import android.app.Application;
import android.graphics.Color;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.svc.GeotrackingService;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Preferencias;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
//Created by Cesar Casanova on 03/05/2017.
// PRESENTER RUTA
@Singleton
public class PreRuta extends PresenterBase
{
	private static final String TAG = PreRuta.class.getSimpleName();
	private static final String KMH = "%.1f Km/h";
	private static final String MS = "%.1f m/s";
	private static final String METROS = "%.0f m";
	private static final String KILOMETROS = "%.1f Km";

	////////////////////////////////////////////////////
	public interface IVistaRuta extends IVista
	{
		void moveCamara(LatLng pos);
	}
	public String getId(){return o.getId();}

	//----------------------------------------------------------------------------------------------
	private Util util;
	private Preferencias pref;
	@Inject PreRuta(Application app, Util util, Preferencias pref)
	{
		super(app);
		this.util = util;
		this.pref = pref;
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public void subscribe(IVista view)
	{
		super.subscribe(view);
		newListeners();
		if(bEstadisticas)estadisticas();
	}
	@Override
	public void unsubscribe()
	{
		super.unsubscribe();
		delListeners();
		//Como dlg tienen referencia a view, debemos destruir referencia para evitar MemoryLeak!
		if(dlgEstadisticas != null) dlgEstadisticas.dismiss();
		dlgEstadisticas = null;
	}

	//----------------------------------------------------------------------------------------------
	public synchronized void guardar()
	{
		if(isErrorEnCampos())return;

		if(!bGuardar)return;
		bGuardar = false;
		view.iniEspera();

		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
Log.e(TAG, "onDatos---------------------------------------------------------------------isWorking="+isWorking);
				GeotrackingService.start(app, pref.getTrackingDelay());
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					util.return2Main(view.getAct(), true, app.getString(R.string.ok_guardar_ruta));
				}
			}
			@Override
			public void onError(String err, int code)
			{
Log.e(TAG, "onError---------------------------------------------------------------------isWorking="+isWorking);
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_guardar, err);
				}
			}
			@Override public void onTimeout()
			{
Log.e(TAG, "onTimeout---------------------------------------------------------------------isWorking="+isWorking);
				if( ! isWorking)return;
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					util.return2Main(view.getAct(), true, app.getString(R.string.on_timeout));
				}
			}
		});
	}
	public void guardar(Fire.CompletadoListener res)
	{
		o.setNombre(view.getTextNombre());
		o.setDescripcion(view.getTextDescripcion());
		((Ruta)o).guardar(res);
	}
	//----------------------------------------------------------------------------------------------
	private boolean isErrorEnCampos()
	{
		if(view != null && view.getTextNombre().isEmpty())
		{
			view.toast(R.string.sin_nombre);
			view.requestFocusNombre();
			return true;
		}
		return false;
	}

	//----------------------------------------------------------------------------------------------
	public synchronized void eliminarPto(String idRutaPunto) {
		Ruta.RutaPunto.eliminarPto(idRutaPunto);
	}

	//----------------------------------------------------------------------------------------------
	private void eliminarHelper() {
		((Ruta)o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					Log.e(TAG, "eliminar:onDatos-------------------------------------------- "+onBackPressed);
					if( ! onBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.ok_eliminar_ruta));
				}
			}
			@Override
			protected void onError(String err, int code)
			{
				isEliminar = true;
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------- "+err);
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_eliminar, err);
				}
			}
			@Override
			public void onTimeout() {
				if( ! isWorking)return;
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					if( ! onBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.on_timeout));
				}
			}
		});
	}
	public synchronized void eliminar()
	{
		view.iniEspera();

		if(o.getId() != null && o.getId().equals(util.getIdTrackingRoute()))
			util.setTrackingRoute("", "");

		onBackPressed = false;
		new Thread() {
			@Override public void run() {
				eliminarHelper();
			}
		}.start();
	}

	//----------------------------------------------------------------------------------------------
	public boolean startTrackingRecord()
	{
		if(isErrorEnCampos())return false;
		view.iniEspera();
		onBackPressed = false;
		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
Log.e(TAG, "startTrackingRecord:onDatos---------------------------------------------------------------------isWorking="+isWorking);
				util.setTrackingRoute(o.id, o.nombre);
				if(view != null) {
					view.finEspera();
					if(!onBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.ok_guardar_ruta));
				}
				GeotrackingService.start(app, pref.getTrackingDelay());
			}
			@Override
			public void onError(String err, int code)
			{
				util.setTrackingRoute("", "");
				Log.e(TAG, "startTrackingRecord:onError:e:------------------------------------- "+err);
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_guardar, err);
				}
			}
			@Override
			public void onTimeout() {
				if( ! isWorking)return;
Log.e(TAG, "startTrackingRecord:onTimeout---------------------------------------------------------------------");
				util.setTrackingRoute(o.id, o.nombre);
				GeotrackingService.start(app, pref.getTrackingDelay());
				if(view != null) {
					view.finEspera();
					if(!onBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.on_timeout));
				}
			}
		});
		return true;
	}
	//----------------------------------------------------------------------------------------------
	public void stopTrackingRecord()
	{
		util.setTrackingRoute("", "");
	}

	//----------------------------------------------------------------------------------------------
	private String getDistancia(double fDistancia) {
		Locale loc = Locale.getDefault();
		if(fDistancia < 2000)   return String.format(loc, METROS, fDistancia);
		else					return String.format(loc, KILOMETROS, fDistancia/1000);
	}
	private String getMinAltura(double fMinAlt) {
		Locale loc = Locale.getDefault();
		if(fMinAlt==Double.MAX_VALUE)	return "-";
		else							return String.format(loc, METROS, fMinAlt);
	}
	private String getMaxAltura(double fMaxAlt) {
		Locale loc = Locale.getDefault();
		if(fMaxAlt==Double.MIN_VALUE)	return "-";
		else							return String.format(loc, METROS, fMaxAlt);
	}
	private String getMinVelociodad(double fMinVel) {
		if(fMinVel==Double.MAX_VALUE)
			return "-";
		else
		{
			Locale loc = Locale.getDefault();
			if(fMinVel > 3)
			{
				fMinVel = fMinVel*3600/1000;
				return String.format(loc, KMH, fMinVel);
			}
			else
				return String.format(loc, MS, fMinVel);
		}
	}
	private String getMaxVelociodad(double fMaxVel) {
		if(fMaxVel == Double.MIN_VALUE)
			return "-";
		else
		{
			Locale loc = Locale.getDefault();
			if (fMaxVel > 3)
			{
				fMaxVel = fMaxVel * 3600 / 1000;
				return String.format(loc, KMH, fMaxVel);
			}
			else
				return String.format(loc, MS, fMaxVel);
		}
	}
	public void estadisticas()
	{
		((Ruta) o).getPuntos(new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				double fMaxAlt = Double.MIN_VALUE;
				double fMinAlt = Double.MAX_VALUE;
				double fMaxVel = Double.MIN_VALUE;
				double fMinVel = Double.MAX_VALUE;
				double fDistancia = 0;

				String sDistancia = "";
				String sAltMin = "";
				String sAltMax = "";
				String sVelMin = "";
				String sVelMax = "";
				String sTiempo = "";
				String sVelMed = "";

				try {
					for(int i = 0; i < aData.length; i++) {
						Ruta.RutaPunto pto = aData[i];
						if(fMaxAlt < pto.getAltura()) fMaxAlt = pto.getAltura();
						if(pto.getAltura() != 0.0 && fMinAlt > pto.getAltura())
							fMinAlt = pto.getAltura();
						if(fMaxVel < pto.getVelocidad()) fMaxVel = pto.getVelocidad();
						if(pto.getVelocidad() != 0.0 && fMinVel > pto.getVelocidad())
							fMinVel = pto.getVelocidad();
						if(i > 0) fDistancia += pto.distanciaReal(aData[i - 1]);
					}

					sDistancia = getDistancia(fDistancia);
					sAltMin = getMinAltura(fMinAlt);
					sAltMax = getMaxAltura(fMaxAlt);
					sVelMin = getMinVelociodad(fMinVel);
					sVelMax = getMaxVelociodad(fMaxVel);

					if(aData.length > 0) {
						long t = aData[aData.length - 1].getFecha() - aData[0].getFecha();
						sTiempo = util.formatDiffTimes(
								new DateTime(aData[0].getFecha()),						//Time Ini
								new DateTime(aData[aData.length - 1].getFecha()));		//Time End

						if(t > 1000)//No calcular velocidad media si tiempo < 1s
						{
							Locale loc = Locale.getDefault();
							double d = fDistancia * 1000 / t;
							if(d > 3) {
								d = d * 3600 / 1000;
								sVelMed = String.format(loc, KMH, d);
							}
							else {
								sVelMed = String.format(loc, MS, d);
							}
						}
						else sVelMed = "-";
					}
				}
				catch(Exception e) {
					Log.e(TAG, "estadisticas:onDatos:e:---------------------------------------",e);
				}

				estadisticasShow(String.format(app.getString(R.string.estadisticas_format),
						sDistancia, sTiempo, sVelMed, sVelMin, sVelMax, sAltMin, sAltMax));
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "estadisticas:onCancelled:------------------------------------------ "+err);
			}
		});
	}
	private AlertDialog dlgEstadisticas = null;
	private boolean bEstadisticas = false;
	private void estadisticasShow(String s)
	{
		bEstadisticas = true;
		dlgEstadisticas = new AlertDialog.Builder(view.getAct()).create();
		dlgEstadisticas.setTitle(app.getString(R.string.estadisticas));
		dlgEstadisticas.setMessage(s);
		dlgEstadisticas.setOnDismissListener(dialog ->	bEstadisticas = false);
		dlgEstadisticas.show();
	}


	//----------------------------------------------------------------------------------------------
	private Fire.DatosListener<Ruta.RutaPunto> lisRuta;
	private void delListeners()
	{
		if(lisRuta != null) lisRuta.setListener(null);
		lisRuta = null;
	}
	private void newListeners()
	{
		delListeners();
		lisRuta = new Fire.DatosListener<Ruta.RutaPunto>()
		{
			@Override public void onDatos(Ruta.RutaPunto[] aData)
			{
				showRutaHelper(aData);
			}
			@Override public void onError(String err)
			{
				Log.e(TAG, "newListeners:ListenerRuta:e:--------------------------------------- "+err);
				if(view != null)
					view.toast(R.string.err_get_ruta_pts);
			}
		};
	}
	//----------------------------------------------------------------------------------------------
	public void showRuta()
	{
		Ruta.RutaPunto.getListaRep(o.getId(), lisRuta);
	}
	//----------------------------------------------------------------------------------------------
	private void showRutaHelper(Ruta.RutaPunto[] aPts)
	{
		if(aPts.length < 1) {
			Log.e(TAG, "showRutaHelper:e:------------------------------------------------------ n pts = "+aPts.length);
			return;
		}
		if(view == null || view.getMap()==null) {
			Log.e(TAG, "showRutaHelper:e:------------------------------------------------------ MAP = NULL");
			return;
		}
		view.getMap().clear();

		String ini = app.getString(R.string.ini);
		String fin = app.getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();

		Ruta.RutaPunto gpAnt = null;
		Ruta.RutaPunto gpIni = aPts[0];
		Ruta.RutaPunto gpFin = aPts[aPts.length -1];
		for(Ruta.RutaPunto pto : aPts)
		{
			LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
			MarkerOptions mo = new MarkerOptions();
			mo.title(o.getNombre());

			String snippet;
			if(pto == gpIni)snippet = ini;
			else if(pto == gpFin)snippet = fin;
			else snippet = app.getString(R.string.info_time);

			Date date = new Date(pto.getFecha());
			snippet += util.formatFechaTiempo(date);
			snippet += String.format(Locale.ENGLISH, app.getString(R.string.info_prec), pto.getPrecision());
			if(gpAnt != null)
			{
				float d = pto.distanciaReal(gpAnt);
				String sDist;
				if(d > 3000)	sDist = String.format(Locale.ENGLISH, app.getString(R.string.info_dist2), d/1000);
				else			sDist = String.format(Locale.ENGLISH, app.getString(R.string.info_dist), d);
				snippet += sDist;
			}
			if(pto.getVelocidad() > 3)
				snippet += String.format(Locale.ENGLISH, app.getString(R.string.info_speed2), pto.getVelocidad()*3600/1000);
			else if(pto.getVelocidad() > 0)
				snippet += String.format(Locale.ENGLISH, app.getString(R.string.info_speed), pto.getVelocidad());
			if(pto.getDireccion() > 0)snippet += String.format(Locale.ENGLISH, app.getString(R.string.info_nor), pto.getDireccion());
			if(pto.getAltura() > 0)snippet += String.format(Locale.ENGLISH, app.getString(R.string.info_alt), pto.getAltura());
			snippet += String.format(app.getString(R.string.info_activity), util.getActivityString(pto.getActividad()));
			mo.snippet(snippet);

			if(pto == gpIni)
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				mo.rotation(45);
				Marker m = view.getMap().addMarker(mo.position(pos));
				m.setTag(pto.getId());
			}
			else if(pto == gpFin)
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				mo.rotation(-45);
				Marker m = view.getMap().addMarker(mo.position(pos));
				m.setTag(pto.getId());
			}
			else if(gpAnt != null && pto.distanciaReal(gpAnt) > 5)
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				Marker m = view.getMap().addMarker(mo.position(pos));
				m.setTag(pto.getId());
			}
			gpAnt = pto;
			po.add(pos);
		}
		po.width(5).color(Color.BLUE);
		view.getMap().addPolyline(po);
		((IVistaRuta) view).moveCamara(new LatLng(gpFin.getLatitud(), gpFin.getLongitud()));
	}
}
