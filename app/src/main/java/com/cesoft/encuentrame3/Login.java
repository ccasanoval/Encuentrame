package com.cesoft.encuentrame3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

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

	private static Context _svcContext;
		public static void setSvcContext(Context c){_svcContext = c;}

	private static String _sCurrentUserID="";
		private static void setCurrentUserID(String v){_sCurrentUserID=v;}
		public static String getCurrentUserID(){return _sCurrentUserID;}
	private static String _sCurrentUserName="";
		private static void setCurrentUserName(String v){_sCurrentUserName=v;}
		public static String getCurrentUserName(){return _sCurrentUserName;}


	static
	{
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);/// Iniciar firebase disk persistence
	}

	//-----
	public interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo(Exception e);
	}
	//-----
	private static FirebaseAuth _Auth;
	private static FirebaseAuth.AuthStateListener _AuthListener;
	public static void init(final AuthListener listener)
	{
		_Auth = FirebaseAuth.getInstance();
		_AuthListener = new FirebaseAuth.AuthStateListener()
		{
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if(user != null)
				{
					System.err.println("Login:init:onAuthStateChanged:0: " + user.getDisplayName() + " /"+user.getEmail());
					Login.setCurrentUserID(user.getUid());
					Login.setCurrentUserName(user.getEmail());//user.getDisplayName());
					listener.onExito(user);
				}
				else
				{
					System.err.println("Login:init:onAuthStateChanged:1: "+firebaseAuth);
					System.err.println("Login:init:onAuthStateChanged:1: "+firebaseAuth.getCurrentUser());
					listener.onFallo(new Exception("onAuthStateChanged:user=null / "+firebaseAuth));
				}
			}
		};
		_Auth.addAuthStateListener(_AuthListener);
		//OnStop: if(mAuthListener != null)mAuth.removeAuthStateListener(mAuthListener);
		//Todo en la espera : showProgressDialog
	}

	public static void addUser(String email, String password, final AuthListener listener)
	{
		//TODO: Mostrar reglas de Firebase para crear usuarios...(en caso de error...)
System.err.println("Login: addUser:" + email + "/"+password);

		_Auth.createUserWithEmailAndPassword(email, password)
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
		_Auth.signInWithEmailAndPassword(email, password)
			.addOnSuccessListener(new OnSuccessListener<AuthResult>()
			{
				@Override
				public void onSuccess(AuthResult authResult)
				{
					System.err.println("Login:login2:ok:"+authResult.getUser()+"/"+_Auth.getCurrentUser());
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


	public static String getUsuario()
	{
		if(_svcContext == null)return null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		return prefs.getString(PREF_LOGIN, "");
	}
	public static String getClave()
	{
		if(_svcContext == null)return null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		return prefs.getString(PREF_PWD, "");
	}
	public static void saveLogin(String usr, String pwd)
	{
		if(usr == null || usr.isEmpty())return;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		if(!prefs.getBoolean(PREF_SAVE_LOGIN, true))return;
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PWD, pwd);
		e.apply();
	}
	public static void delLogin()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PWD, "");
		e.apply();
	}
	public static void delPasswordOnly()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_PWD, "");
		e.apply();
	}

	//-------
	public static boolean login(AuthListener listener)
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
			if( ! prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
				delLogin();
				return false;
			}
		}catch(Exception e){System.err.println("Login.login2:e:"+e);}
		String email = getUsuario();
		String password = getClave();
System.err.println("Login.login: "+email+":"+password);
		if(email == null || password == null || email.isEmpty() || password.isEmpty())return false;
		login2(email, password, listener);
		return true;
	}
	public static void login(String email, String password, final AuthListener listener)
	{
		login2(email, password, listener);
		if(_svcContext != null)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
			if(prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
				if(!email.isEmpty())
				saveLogin(email, password);
			}
			else
				delLogin();
		}
	}

	//-------
	public static boolean isLogged()
	{
		if(_Auth == null)return false;//TODO:
		return _Auth.getCurrentUser() != null;
	}

	//-------
	public static void logout()
	{
		_Auth.signOut();
		delPasswordOnly();//delLogin();
	}

	//-------
	public static void restoreUser(final String email, final AuthListener listener)//AuthListener listener
	{
		_Auth.sendPasswordResetEmail(email)
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
					System.err.println("Login:restoreUser:success: ");
					listener.onExito(_Auth.getCurrentUser());
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
