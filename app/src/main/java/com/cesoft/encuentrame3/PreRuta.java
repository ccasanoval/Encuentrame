package com.cesoft.encuentrame3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

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
//Created by booster-bikes on 03/05/2017.
// PRESENTER RUTA
@Singleton
class PreRuta
{
	private static final String TAG = PreRuta.class.getSimpleName();

	private ActRuta _view;
	private Ruta _r;
	private boolean _bSucio = false;
	private boolean _bNuevo = false;

	private Util _util;
	private CesService _servicio;
	@Inject PreRuta(Util util, CesService servicio)
	{
		_util = util;
		_servicio = servicio;
	}

	void ini(ActRuta view)
	{
		_view = view;
		_bSucio = false;
	}
	void subscribe(ActRuta view)
	{
		_view = view;
		newListeners();
	}
	void unsubscribe()
	{
		_view = null;
		delListeners();
	}

	String getNombre(){return _r.getNombre();}
	String getDescripcion(){return _r.getDescripcion();}
	String getId(){return _r.getId();}
	boolean isNuevo(){return _bNuevo;}
	//boolean isSucio(){return _bSucio;}
	//
	void setSucio(){_bSucio=true;}

	void loadObjeto()
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
	}

	//______________________________________________________________________________________________
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
			dialog.create().show();
		}
		else
			_view.finish();
	}


	//______________________________________________________________________________________________
	private boolean checkCampos()
	{
		if(_view.getNombre().isEmpty())
		{
			Toast.makeText(_view, _view.getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			_view.requestFocusNombre();
			return false;
		}
		return true;
	}

	private boolean _bGuardar = true;
	public synchronized void guardar()
	{
		if( ! checkCampos())return;
		if( ! _bGuardar)return;
		_bGuardar = false;
		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				_bGuardar = true;
				_view.finEspera();
				//Log.w(TAG, "guardar:---(synchronized)-----------------------------------------------------------"+data);
				_util.return2Main(_view, true, _view.getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				_bGuardar = true;
				_view.finEspera();
				Log.e(TAG, "guardar:handleFault:f:--------------------------------------------------"+err);
				Toast.makeText(_view, String.format(_view.getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
			}
		});
	}
	public void guardar(Fire.CompletadoListener res)
	{
		_r.setNombre(_view.getNombre());
		_r.setDescripcion(_view.getDescripcion());
		_r.guardar(res);
		//Solo si es nuevo?
		CesService.setMinTrackingDelay();
	}

	//______________________________________________________________________________________________
	private boolean _bEliminar = true;
	public void eliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(_view);
		dialog.setTitle(_r.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(_view.getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			dialog.setOnDismissListener(dlg -> _bEliminar = true);
		}
		dialog.setNegativeButton(_view.getString(R.string.cancelar), (dlg, which) -> _bEliminar = true);
		dialog.setPositiveButton(_view.getString(R.string.eliminar), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_view.iniEspera();
				synchronized(this)
				{
					if(_r.getId().equals(_util.getTrackingRoute()))
						_util.setTrackingRoute("");
					_r.eliminar(new Fire.CompletadoListener()
					{
						@Override
						protected void onDatos(String id)
						{
							_bEliminar = true;
							_view.finEspera();
							_util.return2Main(_view, true, _view.getString(R.string.ok_eliminar_ruta));
						}
						@Override
						protected void onError(String err, int code)
						{
							_bEliminar = true;
							_view.finEspera();
							Log.e(TAG, "eliminar:handleFault:f:" + err);
							Toast.makeText(_view, String.format(_view.getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
		dialog.create().show();
	}

	//______________________________________________________________________________________________
	void startTrackingRecord()
	{
		if( ! checkCampos())return;
		_view.iniEspera();
		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				_view.finEspera();
				_util.setTrackingRoute(_r.getId());
				//
				_servicio._restartDelayRuta();
				_util.return2Main(_view, true, _view.getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				_view.finEspera();
				_util.setTrackingRoute("");
				//
				Log.e(TAG, "startTrackingRecord:onError:e:------------------------------------------"+err);
				Toast.makeText(_view, String.format(_view.getString(R.string.error_guardar),err), Toast.LENGTH_LONG).show();
			}
		});
	}
	//______________________________________________________________________________________________
	void stopTrackingRecord()
	{
		_util.setTrackingRoute("");
		_util.return2Main(_view, true, _view.getString(R.string.ok_stop_tracking));
	}

	//----------------------------------------------------------------------------------------------
	void estadisticas()
	{
		_r.getPuntos(new Fire.SimpleListener<Ruta.RutaPunto>()
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

				estadisticasShow(String.format(_view.getString(R.string.estadisticas_format),
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
	private void estadisticasShow(String s)
	{
		//Mostrar
		AlertDialog alertDialog = new AlertDialog.Builder(_view).create();
		alertDialog.setTitle(_view.getString(R.string.estadisticas));
		alertDialog.setMessage(s);
		alertDialog.show();
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
				Toast.makeText(_view, _view.getString(R.string.err_get_ruta_pts), Toast.LENGTH_LONG).show();
			}
		};
	}
	//----------------------------------------------------------------------------------------------
	void showRuta()
	{
		Ruta.RutaPunto.getListaRep(_r.getId(), _lisRuta);
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

		String INI = _view.getString(R.string.ini);
		String FIN = _view.getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();

		Ruta.RutaPunto gpAnt = null;
		Ruta.RutaPunto gpIni = aPts[0];
		Ruta.RutaPunto gpFin = aPts[aPts.length -1];
		for(Ruta.RutaPunto pto : aPts)
		{
			LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
			MarkerOptions mo = new MarkerOptions();
			mo.title(_r.getNombre());

			String snippet;
			if(pto == gpIni)snippet = INI;
			else if(pto == gpFin)snippet = FIN;
			else snippet = _view.getString(R.string.info_time);

			Date date = pto.getFecha();
			if(date != null)snippet += _util.formatFechaTiempo(date);
			snippet += String.format(Locale.ENGLISH, _view.getString(R.string.info_prec), pto.getPrecision());
			if(gpAnt != null)
			{
				float d = pto.distanciaReal(gpAnt);
				String sDist;
				if(d > 3000)	sDist = String.format(Locale.ENGLISH, _view.getString(R.string.info_dist2), d/1000);
				else			sDist = String.format(Locale.ENGLISH, _view.getString(R.string.info_dist), d);
				snippet += sDist;
				//snippet += String.format(Locale.ENGLISH, getString(R.string.info_dist), pto.distanciaReal(gpAnt));
			}
			if(pto.getVelocidad() > 3)
				snippet += String.format(Locale.ENGLISH, _view.getString(R.string.info_speed2), pto.getVelocidad()*3600/1000);
			else if(pto.getVelocidad() > 0)
				snippet += String.format(Locale.ENGLISH, _view.getString(R.string.info_speed), pto.getVelocidad());
			if(pto.getDireccion() > 0)snippet += String.format(Locale.ENGLISH, _view.getString(R.string.info_nor), pto.getDireccion());
			if(pto.getAltura() > 0)snippet += String.format(Locale.ENGLISH, _view.getString(R.string.info_alt), pto.getAltura());
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
		_view.moveCamara(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()));
	}
}
