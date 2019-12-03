package com.cesoft.encuentrame3.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/04/2017.
// FIREBASE
//@Singleton
public class Fire
{
	private static FirebaseDatabase fbdb =null;
	private static synchronized FirebaseDatabase getDBInstance()
	{
		if(fbdb == null)
		{
			fbdb = FirebaseDatabase.getInstance();
			try{
				fbdb.setPersistenceEnabled(true);/// Iniciar firebase disk persistence
			}catch(Exception e){
				com.cesoft.encuentrame3.util.Log.e("Login", "getDBInstance:e:", e);}
		}
		return fbdb;
	}
	private static String getCurrentUserID()
	{
		FirebaseAuth a = FirebaseAuth.getInstance();
		if(a.getCurrentUser() == null)return "";
		return a.getCurrentUser().getUid();
	}
	public static DatabaseReference newFirebase() {
		return Fire.getDBInstance().getReference().child(getCurrentUserID());
	}
	public static StorageReference newStorage() {
		return FirebaseStorage.getInstance().getReference().child(getCurrentUserID());
	}

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
		protected boolean isWorking = true;
		protected abstract void onDatos(String id);
		protected abstract void onError(String err, int code);
		protected void onTimeout() {
			if( ! isWorking)return;
			isWorking = false;
		}
		@Override
		public void onComplete(DatabaseError err, @NonNull DatabaseReference data)
		{
Log.e("Fire", "onComplete--------------------isWorking="+isWorking+"---------err="+err+" : data="+data+" ------------------------------------------------");
			isWorking = false;
			if(err == null)	onDatos(data.getKey());
			else			onError(err.getMessage(), err.getCode());
		}
	}

}
