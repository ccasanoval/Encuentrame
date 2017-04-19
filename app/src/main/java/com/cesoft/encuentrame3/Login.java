package com.cesoft.encuentrame3;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

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
	private SharedPreferences _sp;
	@Inject
	public Login(SharedPreferences sp)
	{
		_sp = sp;
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
	public interface AuthListener
	{
		void onExito(FirebaseUser usr);
		void onFallo(Exception e);
	}
	//-----

	static void addUser(String email, String password, final AuthListener listener)
	{
		//TODO: Mostrar reglas de Firebase para crear usuarios...(en caso de error...)
		getAuth().createUserWithEmailAndPassword(email, password)
			.addOnSuccessListener(new OnSuccessListener<AuthResult>()
			{
				@Override
				public void onSuccess(AuthResult authResult)
				{
					listener.onExito(authResult.getUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					listener.onFallo(e);
				}
			})
			.addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					//System.err.println("Login: createUserWithEmail:onComplete:" + task.isSuccessful());
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
					listener.onExito(authResult.getUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					listener.onFallo(e);
				}
			})
			.addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					//System.err.println("Login:login2:task:"+task);
				}
			});
	}


	private String getUsuario()
	{
		return _sp.getString(PREF_LOGIN, "");
	}
	private String getClave()
	{
		return _sp.getString(PREF_PWD, "");
	}
	private void saveLogin(String usr, String pwd)
	{
		if(usr == null || usr.isEmpty())return;
		if(!_sp.getBoolean(PREF_SAVE_LOGIN, true))return;
		SharedPreferences.Editor e = _sp.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PWD, pwd);
		e.apply();
	}
	private void delLogin()
	{
		SharedPreferences.Editor e = _sp.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PWD, "");
		e.apply();
	}
	private void delPasswordOnly()
	{
		SharedPreferences.Editor e = _sp.edit();
		e.putString(PREF_PWD, "");
		e.apply();
	}

	//-------
	public boolean login(AuthListener listener)
	{
		try
		{
			if( ! _sp.getBoolean(PREF_SAVE_LOGIN, true))
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
	public void login(String email, String password, final AuthListener listener)
	{
		login2(email, password, listener);
		if(_sp.getBoolean(PREF_SAVE_LOGIN, true))
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
		delPasswordOnly();//delLogin();
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
					//System.err.println("Login:restoreUser:complete: "+task);
				}
			})
			.addOnSuccessListener(new OnSuccessListener<Void>()
			{
				@Override
				public void onSuccess(Void aVoid)
				{
					//System.err.println("Login:restoreUser:success: "+getAuth().getCurrentUser());
					listener.onExito(getAuth().getCurrentUser());
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					//System.err.println("Login:restoreUser:failure: "+e);
					listener.onFallo(e);
				}
			});
	}
}
