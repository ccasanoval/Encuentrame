package com.cesoft.encuentrame3.presenters;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 03/05/2017.
// PRESENTER MAPS
public class PreMaps extends PresenterBase
{
	private final String TAG = PreMaps.class.getSimpleName();

	////////////////////////////////////////////////////
	public interface IMapsView extends IVista
	{
		void showLugar(Lugar l);
		void showAviso(Aviso a);
		void setMarkerRadius(LatLng pos);
		void setMarker(String sTitulo, String sDescripcion, LatLng pos);
		void animateCamera();
		void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts);
	}
	//private MapsView _view;
	////////////////////////////////////////////////////

	/*private Lugar _l;
	private Aviso _a;
	private Ruta _r;*/
	private int _iTipo = Constantes.NADA;

	@Inject PreMaps(Application app) { super(app);}

	@Override
	public void subscribe(IVista view)
	{
		super.subscribe(view);
		newListeners();
	}
	@Override
	public void unsubscribe()
	{
		super.unsubscribe();
		delListeners();
	}

	//______________________________________________________________________________________________
	@Override
	public void loadObjeto(Objeto o)
	{
		//_o = o;
		super.loadObjeto(o);
		//------------------------------------------------------------------------------------------
		/*try{_l = _view.getAct().getIntent().getParcelableExtra(Lugar.NOMBRE);	_o=_l;}catch(Exception e){_l=null;}
		try{_r = _view.getAct().getIntent().getParcelableExtra(Ruta.NOMBRE);	_o=_r;}catch(Exception e){_r=null;}
		try{_a = _view.getAct().getIntent().getParcelableExtra(Aviso.NOMBRE);	_o=_a;}catch(Exception e){_a=null;}*/
		try{_iTipo = _view.getAct().getIntent().getIntExtra(Util.TIPO, Constantes.NADA);}catch(Exception e){_iTipo=Constantes.NADA;}
		//------------------------------------------------------------------------------------------
	}
	public boolean isLugar(){return _iTipo == Constantes.LUGARES && !isNuevo();}
	public boolean isAviso(){return _iTipo == Constantes.AVISOS && !isNuevo();}
	public boolean isRuta(){return _iTipo == Constantes.RUTAS && !isNuevo();}

	@Override protected void eliminar(){}
	//______________________________________________________________________________________________
	@Override public void guardar()
	{
		if(isLugar())
		{
			((Lugar)_o).guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					_view.toast(R.string.ok_guardar_lugar);
					Intent data = new Intent();
					data.putExtra(Lugar.NOMBRE, _o);
					_view.getAct().setResult(Activity.RESULT_OK, data);
					_view.finish();
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:handleFault:e:----------------------------------------------"+err);
					_view.toast(R.string.error_guardar, err);
				}
			});
		}
		if(isAviso())
		{
			((Aviso)_o).guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					_view.toast(R.string.ok_guardar_aviso);
					Intent data = new Intent();
					data.putExtra(Aviso.NOMBRE, _o);
					_view.getAct().setResult(Activity.RESULT_OK, data);
					_view.finish();
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:handleFault:e:----------------------------------------------"+err);
					_view.toast(R.string.error_guardar, err);
				}
			});
		}
	}

	//______________________________________________________________________________________________
	private Fire.DatosListener<Lugar> _lisLugar;
	private Fire.DatosListener<Aviso> _lisAviso;
	private Fire.DatosListener<Ruta> _lisRuta;
	//----------------------------------------------------------------------------------------------
	private void delListeners()
	{
		if(_lisLugar!=null)_lisLugar.setListener(null);
		if(_lisAviso!=null)_lisAviso.setListener(null);
		if(_lisRuta!=null)_lisRuta.setListener(null);
	}
	private void newListeners()
	{
		delListeners();
		_lisLugar = new Fire.DatosListener<Lugar>()
		{
			@Override public void onDatos(Lugar[] aData)
			{
				for(Lugar o : aData)((IMapsView)_view).showLugar(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showLugares:e:--------------------------------------------LUGARES:GET:ERROR:%s",err));
			}
		};
		_lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override public void onDatos(Aviso[] aData)
			{
				for(Aviso o : aData)((IMapsView)_view).showAviso(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showAvisos:e:---------------------------------------------AVISOS:GET:ERROR:%s",err));
			}
		};
		_lisRuta = new Fire.DatosListener<Ruta>()
		{
			@Override public void onDatos(Ruta[] aData)
			{
				Log.e(TAG, "------------------------------------------------------------------------getLista : "+aData.length);
				for(Ruta o : aData)
					showRuta(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showRutas:e:----------------------------------------------RUTAS:GET:ERROR:%s",err));
			}
		};
	}

	//----------------------------------------------------------------------------------------------
	private void showLugares() { Lugar.getLista(_lisLugar); }
	private void showAvisos() { Aviso.getLista(_lisAviso); }
	private void showRutas() { Ruta.getLista(_lisRuta); }
	//----------------------------------------------------------------------------------------------
	private void showRuta() { showRuta((Ruta)_o); }
	private synchronized void showRuta(Ruta r)
	{
		if(r == null)
		{
			Log.e(TAG, "showRuta:e:----------------------------------------------------------------- r == NULL");
			return;
		}
		Ruta.RutaPunto.getLista(r.getId(), new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
//if(r.getPuntosCount() != aData.length)Log.e(TAG, "showRuta:--------------------------------------------------------"+r.getPuntosCount()+"---<>--- "+aData.length);
				((IMapsView)_view).showRutaHelper(r, aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "showRuta:e:-------------------------------------------------------------"+err);
			}
		});
	}

	public double getRadioAviso(){return ((Aviso)_o).getRadio();}

	public void setPosicion(double lat, double lon)
	{
		//if(_loc == null)_loc = new Location("dummyprovider");
		//_loc.setLatitude(lat);_loc.setLongitude(lon);
		if(isLugar())
		{
			if(_o.getLatitud() != lat || _o.getLongitud() != lon)
			{
				Log.e(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++ SUCIO ");
				_bSucio = true;
				_o.setLatLon(lat, lon);
			}
			((IMapsView)_view).setMarker(_o.getNombre(), _o.getDescripcion(), new LatLng(lat, lon));
		}
		else if(isAviso())
		{
			if(_o.getLatitud() != lat || _o.getLongitud() != lon)
			{
				_bSucio = true;
				_o.setLatLon(lat, lon);
			}
			LatLng pos = new LatLng(lat, lon);
			((IMapsView)_view).setMarker(_o.getNombre(), _o.getDescripcion(), pos);
			((IMapsView)_view).setMarkerRadius(pos);
		}
		else if(isRuta())
		{
			((IMapsView)_view).animateCamera();
		}
	}
	public void dibujar()
	{
		if(isLugar())
		{
			setPosicion(_o.getLatitud(), _o.getLongitud());
			((IMapsView)_view).showLugar((Lugar)_o);
		}
		else if(isAviso())
		{
			setPosicion(_o.getLatitud(), _o.getLongitud());
			((IMapsView)_view).showAviso((Aviso)_o);
		}
		else if(isRuta())
		{
			showRuta();
		}
		else
		{
			switch(_iTipo)
			{
				case Constantes.LUGARES:	showLugares();break;
				case Constantes.AVISOS:		showAvisos();break;
				case Constantes.RUTAS:		showRutas();break;
			}
		}
	}
}
