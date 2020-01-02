package com.cesoft.encuentrame3.presenters;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
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
	private static final String TAG = PreMaps.class.getSimpleName();

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
	////////////////////////////////////////////////////

	private int iTipo = Constantes.NADA;

	@Inject PreMaps(Application app, Util util) { super(app, util);}

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
		super.loadObjeto(o);
		//------------------------------------------------------------------------------------------
		try
		{
			iTipo = view.getAct().getIntent().getIntExtra(Util.TIPO, Constantes.NADA);
		}
		catch(Exception e) {iTipo = Constantes.NADA;}
		//------------------------------------------------------------------------------------------
	}
	public boolean isLugar(){return iTipo == Constantes.LUGARES && !isNuevo();}
	public boolean isAviso(){return iTipo == Constantes.AVISOS && !isNuevo();}
	public boolean isRuta(){return iTipo == Constantes.RUTAS && !isNuevo();}

	@Override protected void eliminar(){}
	//______________________________________________________________________________________________
	@Override public void guardar()
	{
		if(isLugar())
		{
			((Lugar) o).guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					if(view != null) {
						view.toast(R.string.ok_guardar_lugar);
						Intent data = new Intent();
						data.putExtra(Lugar.NOMBRE, o);
						view.getAct().setResult(Activity.RESULT_OK, data);
						view.finish();
					}
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:onError:e:--------------------------------------------"+err);
					view.toast(R.string.error_guardar, err);
				}
			});
		}
		if(isAviso())
		{
			((Aviso) o).guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					view.toast(R.string.ok_guardar_aviso);
					Intent data = new Intent();
					data.putExtra(Aviso.NOMBRE, o);
					view.getAct().setResult(Activity.RESULT_OK, data);
					view.finish();
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:onError:e:--------------------------------------------"+err);
					view.toast(R.string.error_guardar, err);
				}
			});
		}
	}

	//______________________________________________________________________________________________
	private Fire.DatosListener<Lugar> lisLugar;
	private Fire.DatosListener<Aviso> lisAviso;
	private Fire.DatosListener<Ruta> lisRuta;
	//----------------------------------------------------------------------------------------------
	private void delListeners()
	{
		if(lisLugar !=null) lisLugar.setListener(null);
		if(lisAviso !=null) lisAviso.setListener(null);
		if(lisRuta !=null) lisRuta.setListener(null);
	}
	private void newListeners()
	{
		delListeners();
		lisLugar = new Fire.DatosListener<Lugar>()
		{
			@Override public void onDatos(Lugar[] aData)
			{
				if(view != null)
					for(Lugar o : aData)
						((IMapsView)view).showLugar(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showLugares:e:--------------------------------------------LUGARES:GET:ERROR:%s",err));
			}
		};
		lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override public void onDatos(Aviso[] aData)
			{
				if(view != null)
					for(Aviso o : aData)
						((IMapsView)view).showAviso(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showAvisos:e:---------------------------------------------AVISOS:GET:ERROR:%s",err));
			}
		};
		lisRuta = new Fire.DatosListener<Ruta>()
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
	private void showLugares() {
		Lugar.getLista(lisLugar);
	}
	private void showAvisos() {
		Filtro filtro = new Filtro(Constantes.AVISOS);
		filtro.setActivo(Filtro.ACTIVO);
		Aviso.getLista(lisAviso, filtro);
	}
	private void showRutas() {
		Ruta.getLista(lisRuta);
	}
	//----------------------------------------------------------------------------------------------
	private void showRuta() { showRuta((Ruta) o); }
	private synchronized void showRuta(Ruta r)
	{
		if(r == null)
		{
			Log.e(TAG, "showRuta:e:------------------------------------------------------------ r == NULL");
			return;
		}
		Ruta.RutaPunto.getLista(r.getId(), new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				if(view != null)
					((IMapsView)view).showRutaHelper(r, aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "showRuta:e:--------------------------------------------------------"+err);
			}
		});
	}

	public double getRadioAviso(){return ((Aviso)o).getRadio();}

	public void setPosicion(double lat, double lon)
	{
		if(isLugar())
		{
			if(o.getLatitud() != lat || o.getLongitud() != lon)
			{
				isSucio = true;
				o.setLatLon(lat, lon);
			}
			if(view != null)
				((IMapsView)view).setMarker(o.getNombre(), o.getDescripcion(), new LatLng(lat, lon));
		}
		else if(isAviso())
		{
			if(o.getLatitud() != lat || o.getLongitud() != lon)
			{
				isSucio = true;
				o.setLatLon(lat, lon);
			}
			if(view != null) {
				LatLng pos = new LatLng(lat, lon);
				((IMapsView)view).setMarker(o.getNombre(), o.getDescripcion(), pos);
				((IMapsView)view).setMarkerRadius(pos);
			}
		}
		else if(isRuta() && view != null)
		{
			((IMapsView)view).animateCamera();
		}
	}
	public void dibujar()
	{
		if(isLugar())
		{
			setPosicion(o.getLatitud(), o.getLongitud());
			if(view != null)
				((IMapsView)view).showLugar((Lugar) o);
		}
		else if(isAviso())
		{
			setPosicion(o.getLatitud(), o.getLongitud());
			if(view != null)
				((IMapsView) view).showAviso((Aviso) o);
		}
		else if(isRuta())
		{
			showRuta();
		}
		else
		{
			switch(iTipo)
			{
				case Constantes.LUGARES:	showLugares();break;
				case Constantes.AVISOS:		showAvisos();break;
				case Constantes.RUTAS:		showRutas();break;
				default:break;
			}
		}
	}
}
