package com.cesoft.encuentrame3.models;


import android.app.Application;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;

import com.cesoft.encuentrame3.util.Log;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by booster-bikes on 18/04/2017.
// FIREBASE
@Singleton
public class Fire
{
	private static final String TAG = Fire.class.getSimpleName();

	@Inject
	public Fire()
	{
	}

	private FirebaseDatabase _fbdb = null;
	//private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	public synchronized FirebaseDatabase getDBInstance()
	{
		if(_fbdb == null)
		{
			_fbdb = FirebaseDatabase.getInstance();
			try
			{
				_fbdb.setPersistenceEnabled(true);/// Iniciar firebase disk persistence
			}
			catch(Exception e){Log.e(TAG, "getDBInstance:e:-----------------------------------------", e);}
		}
		return _fbdb;
	}
	public synchronized DatabaseReference getDB(String user, String child)
	{
		return getDBInstance().getReference().child(user).child(child);
	}


	//----------------------------------------------------------------------------------------------
	public static abstract class ObjetoListener<T> implements SimpleListener<T>
	{
		private DatabaseReference _ref;
			public void setRef(DatabaseReference ref){_ref=ref;}
		/*private GeoFire _geoFire;
			public void setGeoFire(GeoFire geoFire){_geoFire = geoFire;}*/

		private ValueEventListener _vel = null;
			public void setListener(ValueEventListener vel){_vel = vel;}
			public ValueEventListener getListener(){return _vel;}
			public void delListener(){if(_ref!=null)_ref.removeEventListener(_vel);}

		private GeoQueryEventListener _gel = null;
			public void setListenerGeo(GeoQueryEventListener gel){_gel = gel;}
			public GeoQueryEventListener getListenerGeo(){return _gel;}

		//public abstract void onData(T[] aData);
		//public abstract void onError(String err);
	}
	public interface SimpleListener<T>
	{
		void onData(T[] aData);
		void onError(String err);
	}
}
