package com.cesoft.encuentrame3.presenters;

import android.app.AlertDialog;
import android.app.Application;
import android.graphics.Color;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

	////////////////////////////////////////////////////
	public interface IVistaRuta extends IVista
	{
		//boolean isActivo();
		void moveCamara(LatLng pos);
	}

	private Application _app;
	private Util _util;
	private CesService _servicio;
	@Inject PreRuta(Application app, Util util, CesService servicio)
	{
		super(app);
		_app = app;
		_util = util;
		_servicio = servicio;
	}

	@Override
	public void subscribe(IVista view)
	{
		super.subscribe(view);
		newListeners();
		if(_bEstadisticas)estadisticas();
	}
	@Override
	public void unsubscribe()
	{
		_view = null;
		delListeners();
		//Como dlg tienen referencia a _view, debemos destruir referencia para evitar MemoryLeak!
		if(_dlgEstadisticas != null)_dlgEstadisticas.dismiss();
	}
	/*void ini(ActRuta view)
	{
		_view = view;
		_bSucio = false;
	}
	void subscribe(ActRuta view)
	{
		_view = view;
		newListeners();
		if(_bEstadisticas)estadisticas();
		_bEliminar=false;
	}
	void unsubscribe()
	{
		_view = null;
		delListeners();
		//Como dlg tienen referencia a _view, debemos destruir referencia para evitar MemoryLeak!
		if(_dlgEstadisticas != null)_dlgEstadisticas.dismiss();
		if(_dlgEliminar != null)_dlgEliminar.dismiss();
		if(_dlgSucio != null)_dlgSucio.dismiss();
	}

	String getNombre(){return _r.getNombre();}
	String getDescripcion(){return _r.getDescripcion();}*/
	public String getId(){return _o.getId();}
	//boolean isNuevo(){return _bNuevo;}
	//boolean isSucio(){return _bSucio;}
	//
	//void setSucio(){_bSucio=true;}

	/*void loadObjeto()
	{
		//------------------------------------------------------------------------------------------
		try
		{
			_r = _view.getIntent().getParcelableExtra(Ruta.NOMBRE);
			if(_r == null)
			{
				_bNuevo = true;
				_r = new Ruta();
			}
			else
				_bNuevo = false;
			Log.e(TAG, "onCreate:RUTA:-------------------------------"+_r);
		}
		catch(Exception e)
		{
			Log.e(TAG, "onCreate:Nueva ruta o error al desempaquetar:-------------------------------"+e);
			_bNuevo = true;
			_r = new Ruta();
		}
	}*/

	//______________________________________________________________________________________________
	/*private AlertDialog _dlgSucio = null;
	void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view);
			dialog.setTitle(_r.getNombre());
			dialog.setMessage(_view.getString(R.string.seguro_salir));
			dialog.setPositiveButton(_view.getString(R.string.guardar), (dlg, which) -> guardar());
			dialog.setNegativeButton(_view.getString(R.string.salir), (dlg, which) -> _view.finish());
			dialog.setCancelable(true);
			_dlgSucio = dialog.create();
			_dlgSucio.show();
		}
		else
			_view.finish();
	}*/



	private boolean _bGuardar = true;
	public synchronized void guardar()
	{
		if( ! checkCampos())return;

		if(!_bGuardar)return;
		_bGuardar = false;
		_view.iniEspera();

		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				_bGuardar = true;
				_view.finEspera();
				//Log.w(TAG, "guardar:---(synchronized)-----------------------------------------------------------"+data);
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				_bGuardar = true;
				_view.finEspera();
				Log.e(TAG, "guardar:handleFault:f:--------------------------------------------------"+err);
				_view.toast(R.string.error_guardar, err);
			}
		});
	}
	public void guardar(Fire.CompletadoListener res)
	{
		_o.setNombre(_view.getTextNombre());
		_o.setDescripcion(_view.getTextDescripcion());
		((Ruta)_o).guardar(res);
		//Solo si es nuevo?
		CesService.setMinTrackingDelay();
	}
	//______________________________________________________________________________________________
	private boolean checkCampos()
	{
		if(_view.getTextNombre().isEmpty())
		{
			_view.toast(R.string.sin_nombre);
			_view.requestFocusNombre();
			return false;
		}
		return true;
	}

	//______________________________________________________________________________________________
	public synchronized void eliminar()
	{
		_view.iniEspera();

		if(_o.getId().equals(_util.getTrackingRoute()))
			_util.setTrackingRoute("");
		((Ruta)_o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_bEliminar = true;
				_view.finEspera();
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_eliminar_ruta));
			}
			@Override
			protected void onError(String err, int code)
			{
				_bEliminar = true;
				_view.finEspera();
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------------"+err);
				_view.toast(R.string.error_eliminar, err);
			}
		});
	}

	//______________________________________________________________________________________________
	public void startTrackingRecord()
	{
		if( ! checkCampos())return;
		_view.iniEspera();
		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				_view.finEspera();
				_util.setTrackingRoute(_o.getId());
				//
				_servicio._restartDelayRuta();
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				_view.finEspera();
				_util.setTrackingRoute("");
				//
				Log.e(TAG, "startTrackingRecord:onError:e:------------------------------------------"+err);
				_view.toast(R.string.error_guardar,err);
			}
		});
	}
	//______________________________________________________________________________________________
	public void stopTrackingRecord()
	{
		_util.setTrackingRoute("");
		_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_stop_tracking));
	}

	//----------------------------------------------------------------------------------------------
	public void estadisticas()
	{
		((Ruta)_o).getPuntos(new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				double fMaxAlt = 0, fMinAlt = 99999;
				double fMaxVel = 0, fMinVel = 99999;
				double fDistancia = 0;

				for(int i=0; i < aData.length; i++)
				{
					Ruta.RutaPunto pto = aData[i];
					if(fMaxAlt < pto.getAltura())fMaxAlt = pto.getAltura();
					if(pto.getAltura() != 0.0 && fMinAlt > pto.getAltura())fMinAlt = pto.getAltura();
					if(fMaxVel < pto.getVelocidad())fMaxVel = pto.getVelocidad();
					if(pto.getVelocidad() != 0.0 && fMinVel > pto.getVelocidad())fMinVel = pto.getVelocidad();
					if(i>0)fDistancia += pto.distanciaReal(aData[i-1]);
				}
				Locale loc = Locale.getDefault();
				String sDistancia;
				if(fDistancia < 2000)   sDistancia = String.format(loc, "%.0f m", fDistancia);
				else					sDistancia = String.format(loc, "%.1f Km", fDistancia/1000);

				String sTiempo = "", sVelMed = "";
				if(aData.length > 0)
				{
					long t = aData[aData.length-1].getFecha().getTime() - aData[0].getFecha().getTime();
					sTiempo = _util.formatTiempo(t);

					if(t > 0)
					{
						double d = fDistancia*1000/t;
						if(d > 3)
						{
							d = d*3600/1000;
							sVelMed = String.format(loc, "%.1f Km/h", d);
						}
						else
							sVelMed = String.format(loc, "%.1f m/s", d);
					}
					else sVelMed = "-";
				}

				String sAltMin;
				if(fMinAlt==99999)sAltMin = "-";
				else sAltMin = String.format(loc, "%.0f m",fMinAlt);

				String sAltMax;
				if(fMaxAlt==0)sAltMax = "-";
				else sAltMax = String.format(loc, "%.0f m",fMaxAlt);

				String sVelMin;
				if(fMinVel==99999)sVelMin = "-";
				else
				{
					if(fMinVel > 3)
					{
						fMinVel = fMinVel*3600/1000;
						sVelMin = String.format(loc, "%.1f Km/h", fMinVel);
					}
					else
						sVelMin = String.format(loc, "%.1f m/s", fMinVel);
				}

				String sVelMax;
				if(fMaxVel == 0)sVelMax = "-";
				else if(fMaxVel > 3)
				{
					fMaxVel = fMaxVel*3600/1000;
					sVelMax = String.format(loc, "%.1f Km/h", fMaxVel);
				}
				else
					sVelMax = String.format(loc, "%.1f m/s", fMaxVel);

				estadisticasShow(String.format(_app.getString(R.string.estadisticas_format),
						sDistancia, sTiempo, sVelMed, sVelMin, sVelMax, sAltMin, sAltMax));
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("estadisticas:onCancelled:-----------:%s",err));
				//Toast.makeText(this, "Error al obtener los puntos de la ruta", Toast.LENGTH_LONG).show();
			}
		});
	}
	private AlertDialog _dlgEstadisticas = null;
	private boolean _bEstadisticas = false;
	private void estadisticasShow(String s)
	{
		_bEstadisticas = true;
		_dlgEstadisticas = new AlertDialog.Builder(_view.getAct()).create();
		_dlgEstadisticas.setTitle(_app.getString(R.string.estadisticas));
		_dlgEstadisticas.setMessage(s);
		_dlgEstadisticas.setOnDismissListener(dialog ->	_bEstadisticas = false);
		_dlgEstadisticas.show();
	}


	//----------------------------------------------------------------------------------------------
	private Fire.DatosListener<Ruta.RutaPunto> _lisRuta;
	private void delListeners()
	{
		if(_lisRuta != null)_lisRuta.setListener(null);
		_lisRuta = null;
	}
	private void newListeners()
	{
		delListeners();
		_lisRuta = new Fire.DatosListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				//Log.e(TAG, "------------------------------DatosListener --------------------------------------"+aData.length);
				showRutaHelper(aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "newListeners:ListenerRuta:e:--------------------------------------------"+err);
				_view.toast(R.string.err_get_ruta_pts);
			}
		};
	}
	//----------------------------------------------------------------------------------------------
	public void showRuta()
	{
		Ruta.RutaPunto.getListaRep(_o.getId(), _lisRuta);
	}
	//----------------------------------------------------------------------------------------------
	private void showRutaHelper(Ruta.RutaPunto[] aPts)
	{
		if(aPts.length < 1)
		{
			Log.e(TAG, "showRutaHelper:e:----------------------------------------------------------- n pts = "+aPts.length);
			return;
		}
		if(_view.getMap()==null)
		{
			Log.e(TAG, "showRutaHelper:e:----------------------------------------------------------- MAP = NULL");
			return;
		}
		_view.getMap().clear();

		String INI = _app.getString(R.string.ini);
		String FIN = _app.getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();

		Ruta.RutaPunto gpAnt = null;
		Ruta.RutaPunto gpIni = aPts[0];
		Ruta.RutaPunto gpFin = aPts[aPts.length -1];
		for(Ruta.RutaPunto pto : aPts)
		{
			LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
			MarkerOptions mo = new MarkerOptions();
			mo.title(_o.getNombre());

			String snippet;
			if(pto == gpIni)snippet = INI;
			else if(pto == gpFin)snippet = FIN;
			else snippet = _app.getString(R.string.info_time);

			Date date = pto.getFecha();
			if(date != null)snippet += _util.formatFechaTiempo(date);
			snippet += String.format(Locale.ENGLISH, _app.getString(R.string.info_prec), pto.getPrecision());
			if(gpAnt != null)
			{
				float d = pto.distanciaReal(gpAnt);
				String sDist;
				if(d > 3000)	sDist = String.format(Locale.ENGLISH, _app.getString(R.string.info_dist2), d/1000);
				else			sDist = String.format(Locale.ENGLISH, _app.getString(R.string.info_dist), d);
				snippet += sDist;
				//snippet += String.format(Locale.ENGLISH, getString(R.string.info_dist), pto.distanciaReal(gpAnt));
			}
			if(pto.getVelocidad() > 3)
				snippet += String.format(Locale.ENGLISH, _app.getString(R.string.info_speed2), pto.getVelocidad()*3600/1000);
			else if(pto.getVelocidad() > 0)
				snippet += String.format(Locale.ENGLISH, _app.getString(R.string.info_speed), pto.getVelocidad());
			if(pto.getDireccion() > 0)snippet += String.format(Locale.ENGLISH, _app.getString(R.string.info_nor), pto.getDireccion());
			if(pto.getAltura() > 0)snippet += String.format(Locale.ENGLISH, _app.getString(R.string.info_alt), pto.getAltura());
			mo.snippet(snippet);

			if(pto == gpIni)//if(pto.equalTo(gpIni)) //getLat() == gpIni.getLat() && pto.getLon() == gpIni.getLon())//It's not possible to establish the z order for the marker...
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				mo.rotation(45);
				_view.getMap().addMarker(mo.position(pos));
			}
			else if(pto == gpFin)//else if(pto.equalTo(gpFin))//(pto.getLat() == gpFin.getLat() && pto.getLon() == gpFin.getLon())
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				mo.rotation(-45);
				_view.getMap().addMarker(mo.position(pos));
			}
			//if(pto.distanciaReal(gpIni) > 5 && pto.distanciaReal(gpFin) > 5)//0.000000005 || pto.distancia2(gpFin) > 0.000000005)
			else if(gpAnt != null && pto.distanciaReal(gpAnt) > 5)
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				_view.getMap().addMarker(mo.position(pos));
			}
			gpAnt = pto;
			po.add(pos);
		}
		po.width(5).color(Color.BLUE);
		_view.getMap().addPolyline(po);
		((IVistaRuta)_view).moveCamara(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()));
	}
}
