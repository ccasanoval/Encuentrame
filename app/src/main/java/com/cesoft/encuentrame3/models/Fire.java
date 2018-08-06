package com.cesoft.encuentrame3.models;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

	//----------------------------------------------------------------------------------------------
	//-----
	public interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo(Exception e);
	}
	//----------------------------------------------------------------------------------------------
	public static abstract class DatosListener<T> implements SimpleListener<T>
	{
		private DatabaseReference _ref;
			void setRef(DatabaseReference ref){_ref=ref;}

		private ValueEventListener _vel = null;
			public void setListener(ValueEventListener vel){delListener();_vel = vel;}
			private void delListener()
			{
				if(_ref!=null && _vel!=null)_ref.removeEventListener(_vel);
			}
	}
	//----------------------------------------------------------------------------------------------
	public interface SimpleListener<T>
	{
		void onDatos(T[] aData);
		void onError(String err);
	}

	//----------------------------------------------------------------------------------------------
	public static abstract class CompletadoListener implements DatabaseReference.CompletionListener
	{
		protected abstract void onDatos(String id);
		protected abstract void onError(String err, int code);
		@Override
		public void onComplete(DatabaseError err, @NonNull DatabaseReference data)
		{
			if(err == null)	onDatos(data.getKey());
			else			onError(err.getMessage(), err.getCode());
		}
	}

}
