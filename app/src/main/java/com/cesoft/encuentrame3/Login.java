package com.cesoft.encuentrame3;

import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 28/04/2016.
@Singleton
public class Login
{
	private static final String TAG = Login.class.getSimpleName();
	private static final String PREF_LOGIN = "login";
	private static final String PREF_PWD = "password";
	private static final String PREF_SAVE_LOGIN = "save_login";

	//______________________________________________________________________________________________
	private SharedPreferences sp;
	@Inject
	public Login(SharedPreferences sp)
	{
		this.sp = sp;
	}

	private static FirebaseAuth getAuth(){return FirebaseAuth.getInstance();}
	public static String getCurrentUserID()
	{
		FirebaseAuth a = FirebaseAuth.getInstance();
		if(a == null || a.getCurrentUser() == null)return "";
		return a.getCurrentUser().getUid();
	}
	static String getCurrentUserName()
	{
		FirebaseAuth a = FirebaseAuth.getInstance();
		if(a == null || a.getCurrentUser() == null)return "";
		return a.getCurrentUser().getEmail();
	}

	private static FirebaseDatabase fbdb =null;
	public static synchronized FirebaseDatabase getDBInstance()
	{
		if(fbdb == null)
		{
			fbdb = FirebaseDatabase.getInstance();
			try{
			fbdb.setPersistenceEnabled(true);/// Iniciar firebase disk persistence
			}catch(Exception e){Log.e("Login", String.format("getDBInstance:e:%s",e), e);}
		}
		return fbdb;
	}


	//-----

	static void addUser(String email, String password, final Fire.AuthListener listener)
	{
		//TODO: Mostrar reglas de Firebase para crear usuarios...(en caso de error...)
		getAuth().createUserWithEmailAndPassword(email, password)
			.addOnSuccessListener(authResult -> listener.onExito(authResult.getUser()))
			.addOnFailureListener(listener::onFallo)
//			.addOnCompleteListener(task -> {
//				System.err.println("Login: createUserWithEmail:onComplete:" + task.isSuccessful());
//			})
			;
	}

	private static void login2(String email, String password, final Fire.AuthListener listener)
	{
		if(email == null || password == null)return;
		try {
			getAuth().signInWithEmailAndPassword(email, password)
					.addOnSuccessListener(authResult -> listener.onExito(authResult.getUser()))
					.addOnFailureListener(listener::onFallo)
//			.addOnCompleteListener(task -> {
//				//System.err.println("Login:login2:task:"+task);
//			})
			;
		}
		catch(Exception e) {
			Log.e(TAG, "login2:e:-------------------------------------------------------------",e);
		}
	}


	private String getUsuario()
	{
		return sp.getString(PREF_LOGIN, "");
	}
	private String getClave()
	{
		return sp.getString(PREF_PWD, "");
	}
	private void saveLogin(String usr, String pwd)
	{
		if(usr == null || usr.isEmpty())return;
		if(!sp.getBoolean(PREF_SAVE_LOGIN, true))return;
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PWD, pwd);
		e.apply();
	}
	private void delLogin()
	{
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PWD, "");
		e.apply();
	}
	private void delPasswordOnly()
	{
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_PWD, "");
		e.apply();
	}

	//-------
	public boolean login(Fire.AuthListener listener)
	{
		try
		{
			if( ! sp.getBoolean(PREF_SAVE_LOGIN, true))
			{
				delLogin();
				return false;
			}
		}catch(Exception e){Log.e(TAG, String.format("Login.login:e:%s",e),e);}
		String email = getUsuario();
		String password = getClave();
		if(email.isEmpty() || password.isEmpty())return false;
		login2(email, password, listener);
		return true;
	}
	public void login(String email, String password, final Fire.AuthListener listener)
	{
		login2(email, password, listener);
		if(sp.getBoolean(PREF_SAVE_LOGIN, true))
		{
			if(!email.isEmpty())
				saveLogin(email, password);
		}
		else
			delLogin();
	}

	//-------
	public boolean isLogged()
	{
		return getAuth().getCurrentUser() != null;
	}

	//-------
	void logout()
	{
		getAuth().signOut();
		delPasswordOnly();
	}

	//-------
	static void restoreUser(final String email, final Fire.AuthListener listener)
	{
		getAuth().sendPasswordResetEmail(email)
//			.addOnCompleteListener(task -> { Log.e(); })
			.addOnSuccessListener(aVoid -> listener.onExito(getAuth().getCurrentUser()))
			.addOnFailureListener(listener::onFallo);
	}
}
