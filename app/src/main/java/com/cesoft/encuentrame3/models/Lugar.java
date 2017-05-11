package com.cesoft.encuentrame3.models;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import java.io.File;
import java.util.ArrayList;

import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Exclude;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import android.support.annotation.NonNull;

import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.util.Log;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://firebase.google.com/docs/database/android/save-data
@IgnoreExtraProperties
public class Lugar extends Objeto
{
	private static final String TAG = Lugar.class.getSimpleName();
	public static final String NOMBRE = "lugar";//TODO: transaccion, si no guarda en firebase, no guardar en geofire
	private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	private static GeoFire newGeoFire(){return new GeoFire(Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
	@Exclude private DatabaseReference _datos;

	//TODO: ADD	Altitud, velocidad, bearing ...

	///---------------------------------------------------------------------------------------------
	//Yet Another Firebase Bug:
	//Serialization of inherited properties from the base class, is missing in the current release of the
	// Firebase Database SDK for Android. It will be added back in an upcoming version.
	// ...
	//Serialization of inherited properties from the base class, is missing in the in releases 9.0 to 9.6 (iirc)
	// of the Firebase Database SDK for Android. It was added back in versions since then.
	//
	/*protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}
	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}
	private Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}
	//private String imgUrl;
	//	private String getImgUrl(){return imgUrl;}
	//	private void setImgUrl(String v){imgUrl = v;}
	///______________________________________________________________

	//______________________________________________________________________________________________
	protected double latitud, longitud;
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}
		//public void setLatitud(double v){latitud=v;}
		//public void setLongitud(double v){longitud=v;}*/

	//______________________________________________________________________________________________
	public Lugar() { super(); }	//NOTE: Firebase necesita un constructor sin argumentos
	@Override public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), latitud, longitud, DATE_FORMAT.format(fecha));
	}

	//// FIREBASE
	//______________________________________________________________________________________________
	public void eliminar(Fire.CompletadoListener listener)
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
		delImg();
	}
	public void guardar(Fire.CompletadoListener listener)
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
		saveGeo();//TODO: transaccion fire->geo mediante listener... o _datos.runTransaction???
	}

	//______________________________________________________________________________________________
	public static void getLista(@NonNull final Fire.DatosListener<Lugar> listener)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;
		vel = new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					aLugares.add(o.getValue(Lugar.class));
				}
				listener.onDatos(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Lugar:getLista:onCancelled:"+err);
			}
		};
		listener.setRef(ddbb);
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);
	}

	//----------------------------------------------------------------------------------------------
	public static void getLista(Fire.DatosListener<Lugar> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	private static void buscarPorFiltro(final Fire.DatosListener<Lugar> listener, final Filtro filtro)//ValueEventListener listener
	{
		//newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;
		vel = new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Lugar l = o.getValue(Lugar.class);
					if( ! l.pasaFiltro(filtro))continue;
					aLugares.add(l);
				}
				listener.onDatos(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				String s = String.format("Lugar:buscarPorFiltro:onCancelled:e:%s",err);
				Log.e(TAG, s);
				listener.onError(s);
			}
		};
		listener.setRef(ddbb);
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);
	}
	private static void buscarPorGeoFiltro(final Fire.SimpleListener<Lugar> listener, final Filtro filtro)
	{
		GeoFire geoFire = newGeoFire();
		//listener.setGeoFire(geoFire);

		if(filtro.getRadio() < 1)filtro.setRadio(100);
		final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);

		GeoQueryEventListener lisGeo;
		final ArrayList<Lugar> aLugares = new ArrayList<>();
		lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				nCount++;
				//newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				newFirebase().child(key).addValueEventListener(new ValueEventListener()//AJAX
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						Lugar l = data.getValue(Lugar.class);
						if(l.pasaFiltro(filtro))aLugares.add(l);
						if(nCount < 1)listener.onDatos(aLugares.toArray(new Lugar[aLugares.size()]));
					}
					@Override
					public void onCancelled(DatabaseError err)
					{
						nCount--;
						Log.e(TAG, "getLista:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onDatos(aLugares.toArray(new Lugar[aLugares.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				if(nCount==0)listener.onDatos(aLugares.toArray(new Lugar[aLugares.size()]));
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){Log.w(TAG, "getLista:onKeyExited");}
			@Override public void onKeyMoved(String key, GeoLocation location){Log.w(TAG, "getLista:onKeyMoved"+key+", "+location);}
			@Override public void onGeoQueryError(DatabaseError error){Log.e(TAG, "getLista:onGeoQueryError:"+error);}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	private Lugar(Parcel in)
	{
		super(in);
		//
		//setLatLon(in.readDouble(), in.readDouble());
		//
		//setImgUrl(in.readString());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		/*
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		//
		//dest.writeString(getImgUrl());*/
	}

	//@Override
	//public int describeContents() { return 0; }
	public static final Parcelable.Creator<Lugar> CREATOR = new Parcelable.Creator<Lugar>()
	{
		@Override public Lugar createFromParcel(Parcel in) { return new Lugar(in); }
		@Override public Lugar[] newArray(int size) { return new Lugar[size]; }
	};

	//----------------------------------------------------------------------------------------------
	// GEOFIRE
	@Exclude
	private GeoFire _datGeo;
	private void saveGeo()
	{
		if(_datos.getKey() == null)
		{
			Log.e(TAG, "saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), (key, error) ->
		{
			if(error != null)
				Log.e(TAG, "There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
			//else
			//	Log.i(TAG, "Location saved on server successfully!");
		});
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			Log.e(TAG, "delGeo:id==null-------------------------------------------------------------");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.removeLocation(_datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------

	//----------------------------------------------------------------------------------------------
	// IMAGEN
	//______________________________________________________________________________________________
	//https://firebase.google.com/docs/storage/android/upload-files?hl=es
	public void uploadImg(String path)
	{
		if(_datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			_datos = newFirebase().child(getId());
		}
		StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE).child(_datos.getKey());
		storageRef.delete().addOnCompleteListener(task -> Log.e(TAG, "uploadImagen:del anterior:addOnCompleteListener:"+task.toString()));

		Uri file = Uri.fromFile(new File(path));
		UploadTask uploadTask = storageRef.putFile(file);
		uploadTask.addOnProgressListener(t ->
		{
			@SuppressWarnings("VisibleForTests")
			long n = t.getBytesTransferred();
			Log.e(TAG, "uploadImagen:onProgress:"+n);
		});

		uploadTask.addOnFailureListener(exception -> Log.e(TAG, "uploadImagen:onFailure:"+exception, exception))
		.addOnSuccessListener(taskSnapshot ->
		{
			// taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
			//if(_img != null)StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(_img);
			@SuppressWarnings("VisibleForTests") Uri img = taskSnapshot.getDownloadUrl();
			//if(img != null)Lugar.this.setImgUrl(img.toString());
			Log.e(TAG, "uploadImagen:onSuccess:-----------"+img);
		});
	}
	//______________________________________________________________________________________________
	public void downloadImg(final ImageView iv, final Activity act, final Fire.SimpleListener<String> listener)
	{
		if(_datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			_datos = newFirebase().child(getId());
		}
		StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE).child(_datos.getKey());
Log.e(TAG, "AAA: "+storageRef.getPath() +" ::: "+storageRef.getBucket() + ":::"+_datos);
//Log.e(TAG, "BBB: "+getImgUrl());

		OnSuccessListener<Uri> lisOk = uri ->
		{
			Log.e(TAG, "downloadImagen: onSuccess: uri: ------------------------------------"+uri);
			loadFromCache(uri, iv, act);
			listener.onDatos(new String[]{uri.toString()});
		};
		OnFailureListener lisKo = exception ->
		{
			Log.e(TAG, "downloadImagen:onFailure:e: ----------------------------------------"+exception);
			listener.onError(exception.toString());
		};
//String img = "https://firebasestorage.googleapis.com/v0/b/sonic-totem-131614.appspot.com/o/fO47HYtdhMYhvD61wUjOygRNMYz1%2Flugar%2F-Ki4cj5oRDm6pCtnt_0D?alt=media&token=183000c4-9f06-4119-aea3-783db9435d22";
		/*String img = getImgUrl();
		if(img != null)
			FirebaseStorage.getInstance().getReferenceFromUrl(img).getDownloadUrl()
				.addOnSuccessListener(lisOk)
				.addOnFailureListener(lisKo);
		else*/
			storageRef.getDownloadUrl()
				.addOnSuccessListener(lisOk)
				.addOnFailureListener(lisKo);

		//StorageReference storageRef = FirebaseStorage.getInstance().reference().child("folderName/file.jpg");
//storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener() {
/*
		final long ONE_MEGABYTE = 1024 * 1024;
		storageRef.getBytes(ONE_MEGABYTE)
			.addOnSuccessListener(new OnSuccessListener<byte[]>()
			{
				@Override
				public void onSuccess(byte[] bytes)
				{
					// Data for "images/island.jpg" is returns, use this as needed
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception exception)
				{
					// Handle any errors
				}
			});*/
	}
	//______________________________________________________________________________________________
	//TODO: listener para avisar que picasso no pudo cargar la imagen.......... toas y poner imagen de cruz
	private void loadFromCache(final Uri imgUri, final ImageView iv, final Activity act)
	{
		//https://futurestud.io/tutorials/glide-placeholders-fade-animations
		Glide.with(act)
			.load(imgUri)
			//.centerCrop()
		    .placeholder(android.R.drawable.gallery_thumb)
		    .crossFade()
		    .into(iv);
	}
	//______________________________________________________________________________________________
	public void delImg()
	{
		if(_datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			_datos = newFirebase().child(getId());
		}
		StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE).child(_datos.getKey());
		storageRef.delete().addOnCompleteListener(task -> Log.e(TAG, "delImg:addOnCompleteListener:"+task.toString()));
	}
	// IMAGEN
	//----------------------------------------------------------------------------------------------
}
