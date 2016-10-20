package com.cesoft.encuentrame3.models;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.cesoft.encuentrame3.Login;
import com.cesoft.encuentrame3.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Exclude;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

//TODO: listado AJAX como rutas....
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://firebase.google.com/docs/database/android/save-data
@IgnoreExtraProperties
public class Lugar extends Objeto
{
	private static final String TAG = "CESoft:Lugar:";
	public static final String NOMBRE = "lugar";//TODO: transaccion, si no guarda en firebase, no guardar en geofire
	private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	private static GeoFire newGeoFire(){return new GeoFire(Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
	@Exclude
	private DatabaseReference _datos;


	///______________________________________________________________
	//Yet Another Firebase Bug:
	//Serialization of inherited properties from the base class, is missing in the current release of the
	// Firebase Database SDK for Android. It will be added back in an upcoming version.
	protected String id = null;
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
	///______________________________________________________________

	//______________________________________________________________________________________________
	private double latitud, longitud;
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}

	//______________________________________________________________________________________________
	public Lugar()
	{
		this.fecha = new Date();
	}
	@Override
	public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), latitud, longitud, DATE_FORMAT.format(fecha));
	}

	//// FIREBASE
	//______________________________________________________________________________________________
	public void eliminar(DatabaseReference.CompletionListener listener)
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
	public void guardar(DatabaseReference.CompletionListener listener)
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
	public static void getLista(final Objeto.ObjetoListener<Lugar> listener)
	{
		//newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		newFirebase().addValueEventListener(new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
					aLugares.add(o.getValue(Lugar.class));
				listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Lugar:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	public static void getLista(Objeto.ObjetoListener<Lugar> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	private static void buscarPorFiltro(final Objeto.ObjetoListener<Lugar> listener, final Filtro filtro)//ValueEventListener listener
	{
		//newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		newFirebase().addValueEventListener(new ValueEventListener()//AJAX
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
				listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				String s = String.format("Lugar:buscarPorFiltro:onCancelled:e:%s",err);
				Log.e(TAG, s);
				listener.onError(s);
			}
		});
	}
	private static void buscarPorGeoFiltro(final Objeto.ObjetoListener<Lugar> listener, final Filtro filtro)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);
		final ArrayList<Lugar> aLugares = new ArrayList<>();
		GeoFire geoFire = newGeoFire();
		final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
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
						if(nCount < 1)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
					}
					@Override
					public void onCancelled(DatabaseError err)
					{
						nCount--;
						Log.e(TAG, "getLista:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				if(nCount==0)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){Log.i(TAG, "getLista:onKeyExited");}
			@Override public void onKeyMoved(String key, GeoLocation location){Log.i(TAG, "getLista:onKeyMoved"+key+", "+location);}
			@Override public void onGeoQueryError(DatabaseError error){Log.e(TAG, "getLista:onGeoQueryError:"+error);}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	private Lugar(Parcel in)
	{
		//super(in);
		setId(in.readString());
		nombre = (in.readString());
		setDescripcion(in.readString());
		setFecha(new Date(in.readLong()));
		//
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		//super.writeToParcel(dest, flags);
		dest.writeString(getId());
		dest.writeString(nombre);
		dest.writeString(getDescripcion());
		dest.writeLong(getFecha().getTime());
		//
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
	}

	//@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Parcelable.Creator<Lugar> CREATOR = new Parcelable.Creator<Lugar>()
	{
		@Override
		public Lugar createFromParcel(Parcel in)
		{
			return new Lugar(in);
		}
		@Override
		public Lugar[] newArray(int size)
		{
			return new Lugar[size];
		}
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
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
		{
			@Override
			public void onComplete(String key, DatabaseError error)
			{
        		if(error != null)
            		Log.e(TAG, "There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
        		//else
            	//	Log.i(TAG, "Location saved on server successfully!");
			}
        });
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			Log.e(TAG, "delGeo:id==null");
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
		storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>()
		{
			@Override
			public void onComplete(@NonNull Task<Void> task)
			{
				Log.e(TAG, "uploadImagen:del anterior:addOnCompleteListener:"+task.toString());
			}
		});

		Uri file = Uri.fromFile(new File(path));
		UploadTask uploadTask = storageRef.putFile(file);
		uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
		{
			@Override
			public void onProgress(UploadTask.TaskSnapshot t)
			{
				Log.e(TAG, "uploadImagen:onProgress:"+t.getBytesTransferred());
			}
		});


		uploadTask.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception exception)
			{
				Log.e(TAG, "uploadImagen:onFailure:"+exception, exception);
			}
		})
		.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
		{
			@Override
			public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
			{
				// taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
				//TODO: delete anterior _img ...
				/*if(_img != null)
				{
					StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(_img);
				}*/
				Uri img = taskSnapshot.getDownloadUrl();
				Log.e(TAG, "uploadImagen:onSuccess:"+img+"::::"+taskSnapshot.toString());
			}
		});
	}
	//______________________________________________________________________________________________
	public void downloadImg(final ImageView iv, final Activity act, final ObjetoListener<String> listener)
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

		storageRef.getDownloadUrl()
			.addOnSuccessListener(new OnSuccessListener<Uri>()
			{
				@Override
				public void onSuccess(Uri uri)
				{
					Log.e(TAG, "downloadImagen: onSuccess: uri: "+uri);
					loadFromPicasso(uri, iv, act);
					listener.onData(new String[]{uri.toString()});
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception exception)
				{
					Log.e(TAG, "downloadImagen:onFailure:e: "+exception);
					listener.onError(exception.toString());
				}
			});

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
	private void loadFromPicasso(final Uri imgUri, final ImageView iv, final Activity act)
	{
		int max0 = Util.getMaxTextureSize();		Log.e(TAG, "loadFromPicaso-----------MAX SIZE:"+max0);
		if(max0 == 0)max0=2048;
		final int max = 800;//max0;

		Picasso.Builder builder = new Picasso.Builder(act);
		builder.listener(new Picasso.Listener()
		{
			@Override
			public void onImageLoadFailed(Picasso picasso, Uri uri, Exception e)
			{
				Log.e(TAG, String.format("loadFromPicasso:e:%s",e),e);
			}
		});
		Picasso p = builder.build();//Picasso.with(act);
		//p.setIndicatorsEnabled(false);
		p.setIndicatorsEnabled(true);//Red = network.  Green = cache memory.  Blue = disk memory.
		p.load(imgUri)
			.networkPolicy(NetworkPolicy.OFFLINE)
			//.resize(max,max)
			//.centerCrop()
			//.onlyScaleDown()
			.fit()
			.into(iv, new Callback()
			{
				@Override
				public void onSuccess()
				{
					Log.e(TAG, "loadFromPicaso:onSuccess---------------------------------------");
				}
				@Override
				public void onError()
				{
					Log.e(TAG, "loadFromPicaso:onError:----------------------------------------");
					//Try again online if cache failed
					Picasso p = Picasso.with(act);
					p.setIndicatorsEnabled(true);
					//p.error(R.string.error_eliminar)//TODO:
					p.load(imgUri)
						/*.resize(max, max)
						.centerCrop()
						.onlyScaleDown()*/
						//.fit()
						.into(iv, new Callback()
						{
							@Override
							public void onSuccess()
							{
								Log.e(TAG, "loadFromPicaso:onSuccess 2---------------");
							}
							@Override
							public void onError()
							{
								Log.v(TAG, "loadFromPicaso: Could not fetch image-----------");
							}
						});
					}
				});
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
		storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>()
		{
			@Override
			public void onComplete(@NonNull Task<Void> task)
			{
				Log.e(TAG, "delImg:addOnCompleteListener:"+task.toString());
			}
		});
	}
	// IMAGEN
	//----------------------------------------------------------------------------------------------
}
