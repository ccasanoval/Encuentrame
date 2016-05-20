package com.cesoft.encuentrame;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

//TODO: Autenticar con twitter, google, etc. https://firebase.google.com/support/guides/firebase-android#update_your_imports_numbered
////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 28/04/2016.
public class Login
{
	private static final String PREF_LOGIN = "login";
	private static final String PREF_PWD = "password";
	private static final String PREF_SAVE_LOGIN = "save_login";

	private static Context _svcContext;
		public static void setSvcContext(Context c){_svcContext = c;}

	//-----
	public interface AuthListener
	{
		public void onExito(AuthResult authResult);
		public void onFallo(@NonNull Exception e);
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
		//}catch(Exception e){System.err.println("Util:getClave:e:"+e);}
		//return "";
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
	private static ArrayList<AuthListener> _observers = new ArrayList<>();
	public static void addOnLoginListener(AuthListener observer)
	{
		_observers.add(observer);
	}
	public static boolean login(){return login(null);}
	public static boolean login(AuthListener listerner)
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
//System.err.println("Util.login2: no hay usr y pwd en settings..."+prefs.getBoolean(PREF_SAVE_LOGIN, false));
			if( ! prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
System.err.println("Login.login2:PREF_SAVE_LOGIN = false");
				delLogin();
				return false;
			}
		}catch(Exception e){System.err.println("Util.login2:e:"+e);}
		String usr = getUsuario();
		String pwd = getClave();
System.err.println("Login.login2: "+usr+":"+pwd);
		if(usr == null || pwd == null || usr.isEmpty() || pwd.isEmpty())return false;
		login(usr, pwd, listerner);
		return true;
//System.err.println("Util.login2: no hay usr y pwd en settings..."+usr+" / "+pwd);
	}
	//-------
	public static void login(String usr, String pwd, final AuthListener listerner)
	{
System.err.println("Login.login1: logando...");

		FirebaseAuth auth = FirebaseAuth.getInstance();
		auth.signInWithEmailAndPassword(usr, pwd)
		/*.addOnCompleteListener(new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				task.isComplete();
				task.isSuccessful();
			}
		})*/
		.addOnSuccessListener(new OnSuccessListener<AuthResult>()
		{
			@Override
			public void onSuccess(AuthResult authResult)
			{
				if(listerner!=null)listerner.onExito(authResult);
        		System.out.println("User ID: " + authResult.getUser() + ", Provider: " + authResult.getClass());
				for(AuthListener obs : _observers)
					obs.onExito(authResult);
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				if(listerner!=null)listerner.onFallo(e);
        		System.err.println("Util.login1:err:"+e);
				for(AuthListener obs : _observers)
					obs.onFallo(e);
			}
		});


		if(_svcContext != null)
		{
System.err.println("--------------------login:");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
			if(prefs.getBoolean(PREF_SAVE_LOGIN, true))
			{
System.err.println("--------------------login:saveLogin("+usr+", "+pwd+")");
				if(!usr.isEmpty())
				saveLogin(usr, pwd);
			}
			else
				delLogin();
		}
	}
	//-------
	//TODO: ok?
	public static boolean isLogged()
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		return auth.getCurrentUser() != null;
	}
	public static void isLogged(FirebaseAuth.AuthStateListener listener)
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		auth.addAuthStateListener(listener);
		//TODO: auth.removeAuthStateListener(
		/*new FirebaseAuth.AuthStateListener()
		{
			@Override
			public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth)
			{
				final FirebaseUser user = firebaseAuth.getCurrentUser();
				//return user != null)
				//Log.i("AuthStateChanged", "User is signed in with uid: " + user.getUid());
			}
		});*/
		//TODO: mantener variable user y devolverla, aperte de listener...
	}
	//-------
	public static void logout()
	{
		FirebaseAuth.getInstance().signOut();
		delPasswordOnly();//delLogin();
	}
	//-------
	public static void addUser(String email, String pwd, final AuthListener listener)
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		auth.createUserWithEmailAndPassword(email, pwd)
		.addOnSuccessListener(new OnSuccessListener<AuthResult>()
		{
			@Override
			public void onSuccess(AuthResult authResult)
			{
				listener.onExito(authResult);
				System.out.println("Util.addUser: Successfully created user account with uid: " + authResult.getUser());
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				listener.onFallo(e);
				System.err.println("Util.addUser:e:"+e);
			}
		});
		/*.addOnCompleteListener(new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				task.addOnSuccessListener(listener.onExito((AuthResult)task.getResult(AuthResult.class)));
			}
		};*/
	}
	//-------
	public static void restoreUser(String email, final AuthListener listener)
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		auth.sendPasswordResetEmail(email)
		.addOnSuccessListener(new OnSuccessListener<Void>()
		{
			@Override
			public void onSuccess(Void aVoid)
			{
				listener.onExito(null);
				System.err.println("Util:restoreUser:password reset email sent");
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				listener.onFallo(e);
				System.err.println("Util:restoreUser::err:"+e);
			}
		});
	}
}
