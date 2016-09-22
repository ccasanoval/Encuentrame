package com.cesoft.encuentrame3;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

//TODO: Autenticar con twitter, google, etc
////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 28/04/2016.
public class Login
{
	private static final String PREF_LOGIN = "login";
	private static final String PREF_PWD = "password";
	private static final String PREF_SAVE_LOGIN = "save_login";

	private static FirebaseAuth getAuth(){return FirebaseAuth.getInstance();}
	public static String getCurrentUserID()
	{
		FirebaseAuth a = FirebaseAuth.getInstance();
		if(a == null || a.getCurrentUser() == null)return "";
		return a.getCurrentUser().getUid();
	}
	static String getCurrentUserName()//TODO: throw exception instead of return "" ?
	{
		FirebaseAuth a = FirebaseAuth.getInstance();
		if(a == null || a.getCurrentUser() == null)return "";
		return a.getCurrentUser().getEmail();
		//return a.getCurrentUser().getDisplayName();
	}

	// TODO: Sync data: https://firebase.google.com/docs/database/android/offline-capabilities?hl=es
	private static FirebaseDatabase _fbdb=null;
	public synchronized static FirebaseDatabase getDBInstance()
	{
		if(_fbdb == null)
		{
			_fbdb = FirebaseDatabase.getInstance();
			try{
			_fbdb.setPersistenceEnabled(true);/// Iniciar firebase disk persistence
			}catch(Exception e){Log.e("CESoft:Login", String.format("getDBInstance:e:%s",e), e);}
		}
		return _fbdb;
	}

	//-----
	interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo(Exception e);
	}
	//-----

	static void addUser(String email, String password, final AuthListener listener)
	{
		//TODO: Mostrar reglas de Firebase para crear usuarios...(en caso de error...)
//System.err.println("Login: addUser:" + email + "/"+password);
		getAuth().createUserWithEmailAndPassword(email, password)
			.addOnSuccessListener(new OnSuccessListener<AuthResult>()
			{
				@Override
				public void onSuccess(AuthResult authResult)
				{
					System.err.println("Login:addUser:success: "+authResult.getUser());
					listener.onExito(authResult.getUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					System.err.println("Login:addUser:failure: "+e);
					listener.onFallo(e);
				}
			})
			.addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					System.err.println("Login: createUserWithEmail:onComplete:" + task.isSuccessful());
                }
            });
	}

	private static void login2(String email, String password, final AuthListener listener)
	{
		getAuth().signInWithEmailAndPassword(email, password)
			.addOnSuccessListener(new OnSuccessListener<AuthResult>()
			{
				@Override
				public void onSuccess(AuthResult authResult)
				{
					System.err.println("Login:login2:ok:"+authResult.getUser());
					listener.onExito(authResult.getUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					System.err.println("Login:login2:e:"+e);
					listener.onFallo(e);
				}
			})
			.addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					System.err.println("Login:login2:task:"+task);
				}
			});
	}


	private static String getUsuario(Context c)
	{
		if(c == null)return null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getString(PREF_LOGIN, "");
	}
	private static String getClave(Context c)
	{
		if(c == null)return null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getString(PREF_PWD, "");
	}
	private static void saveLogin(Context c, String usr, String pwd)
	{
		if(usr == null || usr.isEmpty())return;
		if(c == null)return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if(!prefs.getBoolean(PREF_SAVE_LOGIN, true))return;
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PWD, pwd);
		e.apply();
	}
	private static void delLogin(Context c)
	{
		if(c == null)return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PWD, "");
		e.apply();
	}
	private static void delPasswordOnly(Context c)//TODO: Log si c = null
	{
		if(c == null)return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_PWD, "");
		e.apply();
	}

	//-------
	public static boolean login(Context c, AuthListener listener)
	{
		if(c == null)return false;
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
			if( ! prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
				delLogin(c);
				return false;
			}
		}catch(Exception e){System.err.println("Login.login2:e:"+e);}
		String email = getUsuario(c);
		String password = getClave(c);
		if(email == null || password == null || email.isEmpty() || password.isEmpty())return false;
		login2(email, password, listener);
		return true;
	}
	public static void login(Context c, String email, String password, final AuthListener listener)
	{
		login2(email, password, listener);
		if(c != null)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
			if(prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
				if(!email.isEmpty())
				saveLogin(c, email, password);
			}
			else
				delLogin(c);
		}
	}

	//-------
	static boolean isLogged()
	{
		return getAuth().getCurrentUser() != null;
	}

	//-------
	static void logout(Context c)
	{
		getAuth().signOut();
		delPasswordOnly(c);//delLogin();
	}

	//-------
	static void restoreUser(final String email, final AuthListener listener)//AuthListener listener
	{
		getAuth().sendPasswordResetEmail(email)
			.addOnCompleteListener(new OnCompleteListener<Void>()
			{
				@Override
				public void onComplete(@NonNull Task<Void> task)
				{
					System.err.println("Login:restoreUser:complete: "+task);
				}
			})
			.addOnSuccessListener(new OnSuccessListener<Void>()
			{
				@Override
				public void onSuccess(Void aVoid)
				{
					System.err.println("Login:restoreUser:success: "+getAuth().getCurrentUser());
					listener.onExito(getAuth().getCurrentUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					System.err.println("Login:restoreUser:failure: "+e);
					listener.onFallo(e);
				}
			});
	}
}
