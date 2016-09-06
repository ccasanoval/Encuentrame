package com.cesoft.encuentrame3;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
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

	private static Login _this;
	public Login(){_this=this;}

	//-----
	public interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo();
	}
	//-----
	private static FirebaseAuth _Auth;
	private static FirebaseAuth.AuthStateListener _AuthListener;
	public static void init(final AuthListener listener)
	{
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);/// Iniciar firebase disk persistence
		_Auth = FirebaseAuth.getInstance();
		_AuthListener = new FirebaseAuth.AuthStateListener()
		{
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    System.err.println("Login: onAuthStateChanged: signed_in: " + user.getUid());
	                listener.onExito(user);
                }
                else
                {
                    System.err.println("onAuthStateChanged: signed_out: "+firebaseAuth);
	                listener.onFallo();//firebaseAuth);
                }
            }
        };
		_Auth.addAuthStateListener(_AuthListener);
		//OnStop: if(mAuthListener != null)mAuth.removeAuthStateListener(mAuthListener);
		//Todo en la espera : showProgressDialog
	}

	public static void addUser(String email, String password, final Activity win, final AuthListener listener)
	{
		_Auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(win, new OnCompleteListener<AuthResult>()
				{
					@Override
					public void onComplete(@NonNull Task<AuthResult> task)
					{
						System.err.println("Login: createUserWithEmail:onComplete:" + task.isSuccessful());
						// If sign in fails, display a message to the user. If sign in succeeds the auth state listener
						// will be notified and logic to handle the  signed in user can be handled in the listener.
						if(task.isSuccessful())
							listener.onExito(null);
						else
							listener.onFallo();
                    }
                });
	}

	private static void login2(String email, String password, final Activity win, final AuthListener listener)
	{
		try
		{
		_Auth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(win, new OnCompleteListener<AuthResult>()
				{
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
						System.err.println("Login: signInWithEmail:onComplete:" + task.isSuccessful());
						// If sign in fails, display a message to the user. If sign in succeeds the auth state listener
	                    // will be notified and logic to handle the signed in user can be handled in the listener.
						if(task.isSuccessful())
							listener.onExito(null);
						else
							listener.onFallo();
                    }
				});
		}
		catch(Exception e)
		{
			System.err.println("Login:login2:e:"+e);
		}
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
	public static boolean login(Activity win, AuthListener listener)
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
		login2(email, password, win, listener);
		return true;
	}
	public static void login(String email, String password, final Activity win, final AuthListener listener)
	{
		login2(email, password, win, listener);
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
	public static void restoreUser(String email)//AuthListener listener
	{
		//TODO: !!!!!!!!!!!!! Añadir paso dos: espacio para codigo y nueva contraseña...
		//https://firebase.google.com/docs/reference/node/firebase.auth.Auth#sendPasswordResetEmail
		_Auth.sendPasswordResetEmail(email);
		//confirmPasswordReset(code, newPassword)
	}
}
