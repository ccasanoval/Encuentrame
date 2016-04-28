package com.cesoft.encuentrame.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

//TODO: https://www.firebase.com/docs/android/guide/user-auth.html
////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 28/04/2016.
public class Login
{
	private static final String PREF_LOGIN = "login";
	private static final String PREF_PWD = "password";
	private static final String PREF_SAVE_LOGIN = "save_login";

	private static String _idUser = null;
	private static Context _svcContext;
		public static void setSvcContext(Context c){_svcContext = c;}

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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		if(prefs.getBoolean(PREF_SAVE_LOGIN, false))return;
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PWD, pwd);
		e.apply();
	}
	public static void delLogin()
	{
		// Lo contrario a Backendless.UserService.login(usr, pwd, res,   true  );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
		if(prefs.getBoolean(PREF_SAVE_LOGIN, false))return;
		SharedPreferences.Editor e = prefs.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PWD, "");
		e.apply();
	}

	//-------
	public static void login(Firebase.AuthResultHandler listerner)
	{
		try
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
//System.err.println("Util.login2: no hay usr y pwd en settings..."+prefs.getBoolean(PREF_SAVE_LOGIN, false));
			if(prefs.getBoolean(PREF_SAVE_LOGIN, false))
			{
				delLogin();
				return;
			}
		}catch(Exception e){System.err.println("Util.login2:e:"+e);}
		String usr = getUsuario();
		String pwd = getClave();
//System.err.println("Util.login2: "+usr);
		login(usr, pwd, listerner);
//System.err.println("Util.login2: no hay usr y pwd en settings..."+usr+" / "+pwd);
	}
	//-------
	public static void login(String usr, String pwd, final Firebase.AuthResultHandler listerner)
	{
System.err.println("Util.login1: logando...");
		Firebase ref = new Firebase(Objeto.FIREBASE);//TODO: Mas limpio: objeto autenticacion....
		ref.authWithPassword(usr, pwd, new Firebase.AuthResultHandler()
		{
    		@Override
    		public void onAuthenticated(AuthData authData)
			{
				listerner.onAuthenticated(authData);
				_idUser = authData.getUid();
        		System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
    		}
    		@Override
    		public void onAuthenticationError(FirebaseError err)
			{
				listerner.onAuthenticationError(err);
        		System.err.println("Util.login1:err:"+err);

				switch(err.getCode())//https://www.firebase.com/docs/android/guide/user-auth.html
				{
				case FirebaseError.USER_DOES_NOT_EXIST:
					// handle a non existing user
					break;
				case FirebaseError.INVALID_PASSWORD:
					// handle an invalid password
					break;
				default:
					// handle other errors
					break;
				}

    		}
		});

		if(_svcContext != null)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
			if(prefs.getBoolean(PREF_SAVE_LOGIN, true))
				saveLogin(usr, pwd);
			else
				delLogin();
		}
	}
	//-------
	public static boolean isLogged()
	{
		return _idUser != null && !_idUser.isEmpty();
	}
	//-------
	public static void logout()
	{
		Firebase ref = new Firebase(Objeto.FIREBASE);//TODO: Mas limpio: objeto autenticacion....
		ref.unauth();
		_idUser = null;
	}
	//-------
	public static void addUser(String email, String pwd, final Firebase.ValueResultHandler<Map<String, Object>> listener)
	{
		Firebase ref = new Firebase(Objeto.FIREBASE);//TODO: Mas limpio: objeto autenticacion....
		ref.createUser(email, pwd, new Firebase.ValueResultHandler<Map<String, Object>>()
		{
			@Override
			public void onSuccess(Map<String, Object> result)
			{
				listener.onSuccess(result);
				System.out.println("Util.addUser: Successfully created user account with uid: " + result.get("uid"));
			}
			@Override
			public void onError(FirebaseError err)
			{
				listener.onError(err);
				System.err.println("Util.addUser:err:"+err);
			}
		});
	}
	//-------
	public static void restoreUser(String email, final Firebase.ResultHandler listener)
	{
		Firebase ref = new Firebase(Objeto.FIREBASE);//TODO: Mas limpio: objeto autenticacion....
		ref.resetPassword(email, new Firebase.ResultHandler()
		{
			@Override
			public void onSuccess()
			{
				listener.onSuccess();
				System.err.println("Util:restoreUser:password reset email sent");
			}
			@Override
			public void onError(FirebaseError err)
			{
				listener.onError(err);
				System.err.println("Util:restoreUser::err:"+err);
			}
		});
	}
}
