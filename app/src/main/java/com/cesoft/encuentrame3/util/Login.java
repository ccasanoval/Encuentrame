package com.cesoft.encuentrame3.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.cesoft.encuentrame3.App;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

import com.cesoft.encuentrame3.models.Fire;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
/// Created by Cesar_Casanova on 28/04/2016.
@Singleton
public class Login
{
	private static final String TAG = Login.class.getSimpleName();
	private static final String PREF_LOGIN = "login";
	private static final String PREF_PASS = "password";
	private static final String PREF_SAVE_LOGIN = "save_login";

	//______________________________________________________________________________________________
	private final SharedPreferences sp;
	@Inject
	public Login(SharedPreferences sp)
	{
		this.sp = sp;
	}

	private GoogleSignInClient signInClient;
	public void setGoogleSignInClient(GoogleSignInClient signInClient) {
		this.signInClient = signInClient;
	}
	private FirebaseAuth getAuth(){return FirebaseAuth.getInstance();}
//	public String getCurrentUserID()
//	{
//		FirebaseAuth a = FirebaseAuth.getInstance();
//		if(a.getCurrentUser() == null)return "";
//		return a.getCurrentUser().getUid();
//	}
	public static String getCurrentUserName()
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		if(auth.getCurrentUser() == null)return "";
		return auth.getCurrentUser().getDisplayName();
	}
	public static String getCurrentUserEmail()
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		if(auth.getCurrentUser() == null)return "";
		return auth.getCurrentUser().getEmail();
	}
	public static void getCurrentUserImage(CustomTarget<Bitmap> listener)
	{
		FirebaseAuth auth = FirebaseAuth.getInstance();
		if(auth.getCurrentUser() == null) {
			listener.onLoadFailed(null);
			return;
		}
		Uri url = auth.getCurrentUser().getPhotoUrl();
		Glide.with(App.getInstance())
				.asBitmap()
				.load(url)
				.into(listener);
	}


	//-----

	public void addUser(String email, String password, final Fire.AuthListener listener)
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

	private void login2(String email, String password, final Fire.AuthListener listener)
	{
		if(email.isEmpty() || password.isEmpty()) {
			listener.onFallo(new Exception("email or passworkd empty"));
			return;
		}
		try {
			getAuth().signInWithEmailAndPassword(email, password)
					.addOnSuccessListener(authResult -> listener.onExito(authResult.getUser()))
					.addOnFailureListener(listener::onFallo)
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
		return sp.getString(PREF_PASS, "");
	}
	private void saveLogin(String usr, String pwd)
	{
		if(usr == null || usr.isEmpty())return;
		if(!sp.getBoolean(PREF_SAVE_LOGIN, true))return;
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_LOGIN, usr);
		e.putString(PREF_PASS, pwd);
		e.apply();
	}
	private void delLogin()
	{
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_LOGIN, "");
		e.putString(PREF_PASS, "");
		e.apply();
	}
	private void delPasswordOnly()
	{
		SharedPreferences.Editor e = sp.edit();
		e.putString(PREF_PASS, "");
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
		}
		catch(Exception e)
		{
			Log.e(TAG, String.format("Login.login:e:%s",e),e);
		}
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
	public void logout(@NonNull Activity activity)
	{
		if( ! sp.getBoolean(PREF_SAVE_LOGIN, true))
			signInClient.signOut();//.addOnCompleteListener(activity, task -> { });
		getAuth().signOut();
		delPasswordOnly();
	}

	//-------
	public void restoreUser(final String email, final Fire.AuthListener listener)
	{
		getAuth().sendPasswordResetEmail(email)
			.addOnSuccessListener(aVoid -> listener.onExito(getAuth().getCurrentUser()))
			.addOnFailureListener(listener::onFallo);
	}
}
