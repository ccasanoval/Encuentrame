package com.cesoft.encuentrame3.models;

import androidx.annotation.NonNull;

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
	public interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo(Exception e);
	}
	//----------------------------------------------------------------------------------------------
	public abstract static class DatosListener<T> implements SimpleListener<T>
	{
		private DatabaseReference ref;
			void setRef(DatabaseReference ref){this.ref=ref;}

		private ValueEventListener vel = null;
			public void setListener(ValueEventListener vel) {
				delListener(); this.vel = vel;
			}
			private void delListener() {
				if(ref!=null && vel!=null)
					ref.removeEventListener(vel);
			}
	}
	//----------------------------------------------------------------------------------------------
	public interface SimpleListener<T>
	{
		void onDatos(T[] aData);
		void onError(String err);
	}

	//----------------------------------------------------------------------------------------------
	public abstract static class CompletadoListener implements DatabaseReference.CompletionListener
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
