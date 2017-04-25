package com.cesoft.encuentrame3.models;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/04/2017.
// FIREBASE
@Singleton
public class Fire
{
	//private static final String TAG = Fire.class.getSimpleName();

	@Inject public Fire(){}

	/*private FirebaseDatabase _fbdb = null;
	//private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	/*private synchronized FirebaseDatabase getDBInstance()
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
	}*/


	//----------------------------------------------------------------------------------------------
	public static abstract class ObjetoListener<T> implements SimpleListener<T>
	{
		private DatabaseReference _ref;
			void setRef(DatabaseReference ref){_ref=ref;}
		/*private GeoFire _geoFire;
			public void setGeoFire(GeoFire geoFire){_geoFire = geoFire;}*/

		private ValueEventListener _vel = null;
			public void setListener(ValueEventListener vel){delListener();_vel = vel;}
			//ValueEventListener getListener(){return _vel;}
			private void delListener()
			{
				//Log.e(TAG, "ObjetoListener:delListener: ref="+_ref+", vel="+_vel);
				if(_ref!=null && _vel!=null)_ref.removeEventListener(_vel);
			}

		//private GeoQueryEventListener _gel = null;
			//public void setListenerGeo(GeoQueryEventListener gel){_gel = gel;}
			//public GeoQueryEventListener getListenerGeo(){return _gel;}

		//public abstract void onData(T[] aData);
		//public abstract void onError(String err);
	}
	//----------------------------------------------------------------------------------------------
	public interface SimpleListener<T>
	{
		void onData(T[] aData);
		void onError(String err);
	}

	//----------------------------------------------------------------------------------------------
	public static abstract class CompletionListener implements DatabaseReference.CompletionListener
	{
		protected abstract void onDatos(String id);
		protected abstract void onError(String err, int code);
		@Override
		public void onComplete(DatabaseError err, DatabaseReference data)
		{
			if(err == null)	onDatos(data.getKey());
			else			onError(err.getMessage(), err.getCode());
		}
	}

	public static abstract class Transaccion implements Transaction.Handler
	{
		protected abstract void onDatos(DataSnapshot data);
		protected abstract void onError(String err, int code);
		@Override public Transaction.Result doTransaction(MutableData mutableData){return null;}
		@Override public void onComplete(DatabaseError err, boolean b, DataSnapshot data)
		{
			if(err == null)	onDatos(data);
			else			onError(err.getMessage(), err.getCode());
		}
	}
}
