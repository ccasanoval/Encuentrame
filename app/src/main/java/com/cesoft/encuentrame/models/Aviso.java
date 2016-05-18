package com.cesoft.encuentrame.models;

import android.content.Context;
import android.os.Parcel;

import com.cesoft.encuentrame.Util;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.Locale;

//https://develop.backendless.com/#Encuentrame/v1/main/data/Aviso
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Aviso extends Objeto
{
	public static final String NOMBRE = "aviso";

	protected Firebase _datos;
	protected static Firebase newFirebase(){return new Firebase(FIREBASE).child(NOMBRE);}
	protected static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}

	//______________________________________________________________________________________________
	private final static String ACTIVO = "activo";
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean v){activo=v;}

	private double latitud, longitud;
		public double getLatitud(){return latitud;}//TODO: con GeoFire, quiza esto podrias sobra...pero complicaria igual que quitar id de objeto
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}

	private double radio;//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence ¿COMO MEJORAR GOOGLE GEOFENCE? Probar backendless geofences?????
		public double getRadio(){return radio;}
		public void setRadio(double v){if(v >= 0 && v < 10000)radio=v;}

	//TODO: Desactivar por hoy, tambien desactivar todos los avisos... incluso: modo avion para app completa
	/*protected Date fechaActivo;
		public void desactivarPorHoy()
		{
			fechaActivo = Calendar.getInstance().getTime();
			//Backendless.Persistence.save(this, ac);
		}
		public void reactivarPorHoy()
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -2);
			fechaActivo = cal.getTime();
			//Backendless.Persistence.save(this, ac);
		}*/

	//______________________________________________________________________________________________
	public Aviso(){}
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Aviso{id='%s', nombre='%s', descripcion='%s', fecha='%s', latitud='%f', longitud='%f', radio='%f', activo='%b'}",
				getId(), nombre, descripcion, DATE_FORMAT.format(fecha), latitud, longitud, radio, activo);
	}

	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(this == o)return true;
		if(!(o instanceof Aviso))return false;
		Aviso a = (Aviso)o;
System.err.println("------------------AVISO-EQUALS-"+o+" : "+this);
		return getId().equals(a.getId())
			&& getLatitud() == a.getLatitud() && getLongitud() == a.getLongitud() && getRadio() == a.getRadio()
			&& getNombre().equals(a.getNombre()) && getDescripcion().equals(a.getDescripcion());
	}

	//// PARCELABLE
	//
	protected Aviso(Parcel in)
	{
		//setObjectId(in.readString());nombre = in.readString();descripcion = in.readString();
		super(in);
		//
		setActivo(in.readByte() > 0);
		setId(in.readString());
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
		setRadio(in.readDouble());
System.err.println("----------------Aviso:from parcel 2:" + this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeByte(isActivo()?(byte)1:0);
		dest.writeString(getId());
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		dest.writeDouble(getRadio());
System.err.println("----------------Aviso:writeToParcel:"+this);
	}
	@Override
	public int describeContents(){return 0;}
	public static final Creator<Aviso> CREATOR = new Creator<Aviso>()
	{
		@Override
		public Aviso createFromParcel(Parcel in){return new Aviso(in);}
		@Override
		public Aviso[] newArray(int size){return new Aviso[size];}
	};


	//// FIREBASE
	//
	public void eliminar(Firebase.CompletionListener listener)
	{
		if(_datos != null)
		{
			_datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			_datos = newFirebase().child(getId());
			_datos.setValue(null, listener);
		}
		delGeo();
	}
	public void guardar(Firebase.CompletionListener listener)
	{
		if(_datos != null)
		{
			_datos.setValue(this, listener);
		}
		else
		{
			if(getId() != null)
			{
				_datos = newFirebase().child(getId());
			}
			else
			{
				_datos = newFirebase().push();
				setId(_datos.getKey());
			}
			_datos.setValue(this, listener);
		}
		saveGeo();
	}

	//______________________________________________________________________________________________
	public static void getById(String sId, Context c, ValueEventListener listener)
	{

		if(Util.getApplication() != null)
			Firebase.setAndroidContext(Util.getApplication().getBaseContext());
		//else if(Util.getSvcContext() != null)//Cuando cierras app pero das a notificacion: exception: You need to set the Android context using Firebase.setAndroidContext() before using Firebase.
		//	Firebase.setAndroidContext(Util.getSvcContext());
		//else return;
		Firebase.setAndroidContext(c);
		newFirebase().child(sId).addListenerForSingleValueEvent(listener);
	}
	public static void getActivos(ValueEventListener listener)
	{
		Query queryRef = newFirebase().orderByChild(ACTIVO).equalTo(true);
			//Query queryRef = ref.equalTo(true, ACTIVO);//NO PIRULA
		queryRef.addListenerForSingleValueEvent(listener);
    	//queryRef.addChildEventListener(listener);//TODO:Cual mejor?
	}
	public static void getLista(final ObjetoListener<Aviso> listener)
	{
		//newFirebase().addValueEventListener(listener);//TODO:Cual mejor?
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
					aAvisos.add(o.getValue(Aviso.class));
				listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("Aviso:getLista:onCancelled:"+err);
				listener.onError("Aviso:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean pasaFiltro(Filtro filtro)
	{
		if( ! super.pasaFiltro(filtro))return false;
System.err.println("----------"+filtro.getActivo()+" : "+isActivo());
		if(filtro.getActivo()==Filtro.ACTIVO && !isActivo()  ||  filtro.getActivo()==Filtro.INACTIVO && isActivo())return false;
System.err.println("----------pasaFiltro FINAL");
		return true;
	}
	public static void getLista(ObjetoListener<Aviso> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	//----
	public static void buscarPorFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
	{
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Aviso a = o.getValue(Aviso.class);
					if( ! a.pasaFiltro(filtro))continue;
					aAvisos.add(o.getValue(Aviso.class));
				}
				listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("Aviso:buscarPorFiltro:onCancelled:"+err);
				listener.onError(err.toString());
			}
		});
	}
	//----
	public static void buscarPorGeoFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
	{
System.err.println("Aviso:buscarPorGeoFiltro:--------------------------:"+filtro);
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final ArrayList<Aviso> aAvisos = new ArrayList<>();

		final GeoQuery geoQuery = newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				System.err.println("Aviso:buscarPorGeoFiltro:onKeyEntered:"+key+", "+location);
				nCount++;
				newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						Aviso a = data.getValue(Aviso.class);
						if(a.pasaFiltro(filtro))aAvisos.add(a);
						if(nCount < 1)listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
					@Override
					public void onCancelled(FirebaseError err)
					{
						nCount--;
						System.err.println("Aviso:buscarPorGeoFiltro:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
System.err.println("Aviso:buscarPorGeoFiltro:onGeoQueryReady:"+nCount);
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){}
			@Override public void onKeyMoved(String key, GeoLocation location){}
			@Override
			public void onGeoQueryError(FirebaseError err)
			{
				System.err.println("Aviso:buscarPorGeoFiltro:onGeoQueryError:"+err);
			}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}



	//----------------------------------------------------------------------------------------------
	// GEOFIRE
	private GeoFire _datGeo;
	private void saveGeo()
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Aviso:saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
		{
    		@Override
    		public void onComplete(String key, FirebaseError error)
			{
        		if(error != null)
            		System.err.println("Aviso:saveGeo:There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
        		else
            		System.out.println("Aviso:saveGeo:Location saved on server successfully!");
			}
        });
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Aviso:delGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.removeLocation(_datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------
}
