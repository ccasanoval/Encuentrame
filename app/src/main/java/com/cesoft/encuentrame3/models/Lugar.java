package com.cesoft.encuentrame3.models;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.widget.ImageView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.RequestOptions;
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
//https://stackoverflow.com/questions/37890025/classmapper-warnings-after-upgrading-firebase
@Keep
@IgnoreExtraProperties
public class Lugar extends Objeto
{
	private static final String TAG = Lugar.class.getSimpleName();
	public static final String NOMBRE = "lugar";
	private static DatabaseReference newFirebase() {
		return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);
	}
	private static GeoFire newGeoFire() {
		return new GeoFire(Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));
	}
	@Exclude private DatabaseReference datos;

	//______________________________________________________________________________________________
	public Lugar() { super(); }	//NOTE: Firebase necesita un constructor sin argumentos
	@NonNull
	@Override
	public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), latitud, longitud, DATE_FORMAT.format(fecha));
	}

	//// FIREBASE
	//______________________________________________________________________________________________
	public void eliminar(Fire.CompletadoListener listener)
	{
		if(datos != null)
		{
			datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			datos = newFirebase().child(getId());
			datos.setValue(null, listener);
		}
		delGeo();
		delImg();
	}
	public void guardar(Fire.CompletadoListener listener)
	{
		if(datos != null)
		{
			datos.setValue(this, listener);
		}
		else
		{
			if(getId() != null)
			{
				datos = newFirebase().child(getId());
			}
			else
			{
				datos = newFirebase().push();
				setId(datos.getKey());
			}
			datos.setValue(this, listener);
		}
		saveGeo();
	}

	//______________________________________________________________________________________________
	public static void getLista(@NonNull final Fire.DatosListener<Lugar> listener)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;
		vel = new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren()) {
					try {
						aLugares.add(o.getValue(Lugar.class));
					}
					catch(Exception e) {
						Log.e(TAG, "getLista:onDataChange:e:-----------------------------------",e);
					}
				}
				try {
					listener.onDatos(reverse(aLugares));
				}
				catch(Exception e) {
					Log.e(TAG, "getLista:e:----------------------------------------------------",e);
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
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
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;
		vel = new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Lugar l = o.getValue(Lugar.class);
					if(l != null && ! l.pasaFiltro(filtro))continue;
					aLugares.add(l);
				}
				listener.onDatos(reverse(aLugares));
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
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
				newFirebase().child(key).addValueEventListener(new ValueEventListener()//AJAX
				{
					@Override
					public void onDataChange(@NonNull DataSnapshot data)
					{
						nCount--;
						Lugar l = data.getValue(Lugar.class);
						if(l != null && l.pasaFiltro(filtro))
							aLugares.add(l);
						if(nCount < 1)
							listener.onDatos(reverse(aLugares));
					}
					@Override
					public void onCancelled(@NonNull DatabaseError err)
					{
						nCount--;
						Log.e(TAG, "getLista:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)
							listener.onDatos(reverse(aLugares));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				if(nCount==0)
					listener.onDatos(reverse(aLugares));
				geoQuery.removeGeoQueryEventListener(this);
			}
			@Override public void onKeyExited(String key){Log.w(TAG, "getLista:onKeyExited");}
			@Override public void onKeyMoved(String key, GeoLocation location){Log.w(TAG, "getLista:onKeyMoved"+key+", "+location);}
			@Override public void onGeoQueryError(DatabaseError error){Log.e(TAG, "getLista:onGeoQueryError:"+error);}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	private Lugar(Parcel in) { super(in); }
	public static final Parcelable.Creator<Lugar> CREATOR = new Parcelable.Creator<Lugar>()
	{
		@Override public Lugar createFromParcel(Parcel in) { return new Lugar(in); }
		@Override public Lugar[] newArray(int size) { return new Lugar[size]; }
	};

	//----------------------------------------------------------------------------------------------
	// GEOFIRE
	@Exclude
	private GeoFire datGeo;
	private void saveGeo()
	{
		if(datos.getKey() == null)
		{
			Log.e(TAG, "saveGeo:id==null");
			return;
		}
		if(datGeo == null) datGeo = newGeoFire();
		datGeo.setLocation(datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), (key, error) ->
		{
			if(error != null)
				Log.e(TAG, "There was an error saving the location to GeoFire: "+error+" : "+key+" : "+ datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
		});
	}
	private void delGeo()
	{
		if(datos.getKey() == null)
		{
			Log.e(TAG, "delGeo:id==null-------------------------------------------------------------");
			return;
		}
		if(datGeo == null) datGeo = newGeoFire();
		datGeo.removeLocation(datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------

	//----------------------------------------------------------------------------------------------
	// IMAGEN
	//______________________________________________________________________________________________
	//https://firebase.google.com/docs/storage/android/upload-files?hl=es
	public void uploadImg(String path)
	{
		if(datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			datos = newFirebase().child(getId());
		}
		if(datos.getKey() == null)return;
		StorageReference storageRef = FirebaseStorage.getInstance().getReference()
				.child(Login.getCurrentUserID()).child(NOMBRE).child(datos.getKey());
		storageRef.delete().addOnCompleteListener(task ->
				Log.e(TAG, "uploadImagen:del anterior:addOnCompleteListener:"+task.toString()));

		File file = new File(path);
		Uri uriToUpload = Uri.fromFile(file);
		UploadTask uploadTask = storageRef.putFile(uriToUpload);
		uploadTask.addOnProgressListener(t ->
		{
			@SuppressWarnings("VisibleForTests")
			long n = t.getBytesTransferred();
			Log.e(TAG, "uploadImagen:onProgress:"+n);
		});
		storageRef.getDownloadUrl().addOnSuccessListener(uri ->
				Log.e(TAG, "uploadImagen:onSuccess:------BBBB-----"+uri.toString()));

		uploadTask
				.addOnFailureListener(exception -> Log.e(TAG, "uploadImagen:onFailure:"+exception, exception))
				.addOnSuccessListener(taskSnapshot ->
					storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                       	Log.e(TAG, "uploadImagen:onSuccess:----AAAAA-------"+uri.toString())));
	}
	//______________________________________________________________________________________________
	public void downloadImg(final ImageView iv, final Activity act, final Fire.SimpleListener<String> listener)
	{
		if(datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			datos = newFirebase().child(getId());
		}
		if(datos.getKey() == null)return;
		StorageReference storageRef = FirebaseStorage.getInstance().getReference()
				.child(Login.getCurrentUserID()).child(NOMBRE).child(datos.getKey());

		OnSuccessListener<Uri> lisOk = uri ->
		{
			Log.e(TAG, "downloadImagen: onSuccess: uri: ---------------------------------"+uri);
			loadFromCache(uri, iv, act);
			listener.onDatos(new String[]{uri.toString()});
		};
		OnFailureListener lisKo = exception ->
		{
			Log.e(TAG, "downloadImagen:onFailure:e: -------------------------------"+exception);
			listener.onError(exception.toString());
		};
		storageRef.getDownloadUrl()
			.addOnSuccessListener(lisOk)
			.addOnFailureListener(lisKo);
	}
	//______________________________________________________________________________________________
	private void loadFromCache(final Uri imgUri, final ImageView iv, final Activity act)
	{
		Glide.with(act)
			.load(imgUri)
			.apply(new RequestOptions()
					.placeholder(android.R.drawable.gallery_thumb)
					.error(android.R.drawable.alert_dark_frame))
		    .into(iv);
	}
	//______________________________________________________________________________________________
	public void delImg()
	{
		if(datos == null)
		{
			if(getId() == null)
			{
				Log.e(TAG, "delImg: getId() == null");
				return;//Es un nuevo objeto, no puede tener imagen en store...
			}
			datos = newFirebase().child(getId());
		}
		if(datos.getKey() == null)return;
		StorageReference storageRef = FirebaseStorage.getInstance().getReference()
				.child(Login.getCurrentUserID()).child(NOMBRE).child(datos.getKey());
		storageRef.delete().addOnCompleteListener(task -> Log.e(TAG, "delImg:addOnCompleteListener:"+task.toString()));
	}
	// IMAGEN
	//----------------------------------------------------------------------------------------------


	private static Lugar[] reverse(ArrayList<Lugar> aLugares) {
		Collections.reverse(aLugares);
		return aLugares.toArray(new Lugar[0]);
	}
}
