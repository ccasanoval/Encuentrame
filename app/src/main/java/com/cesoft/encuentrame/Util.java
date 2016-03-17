package com.cesoft.encuentrame;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;

import com.cesoft.encuentrame.models.Aviso;

import java.util.HashMap;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
public class Util
{
	//TODO: Comprobar con servicio, que solo llamo a esto en main...
	private static Application _app;//TODO: cambiar los context de abajo por esto...
		public static void setApplication(Application app){_app = app;}

	//______________________________________________________________________________________________
	// LOCATION
	//______________________________________________________________________________________________
	protected  static Location _locLast;
	public static void setLocation(Location loc){_locLast=loc;}
	public static Location getLocation()
	{
		Location location1=null, location2=null;
		try
		{
			LocationManager locationManager = (LocationManager)_app.getSystemService(Context.LOCATION_SERVICE);
			if(locationManager == null)return _locLast;
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if(isNetworkEnabled)
				location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(isGPSEnabled)
				location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(location1==null && location2==null)return _locLast;
			if(_locLast == null)_locLast = location1!=null?location1:location2;
			if(location1 != null && location1.getTime() > _locLast.getTime())
				_locLast = location1;
			else if(location2 != null && location2.getTime() > _locLast.getTime())
				_locLast = location2;
		}
		catch(SecurityException se)
		{
			se.printStackTrace();
		}
		return _locLast;
    }


	//______________________________________________________________________________________________
	// NOTIFICATION UTILS
	//______________________________________________________________________________________________
	public static void playNotificacion()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_app);
		if(prefs.getBoolean("notifications_new_message", true))//true o false ha de coincidir con lo que tengas en pref_notificacion.xml
		{
System.err.println("-----------------------------Ding Dong!!!!!!!!!");
			String sound = prefs.getString("notifications_new_message_ringtone", "");
			if( ! sound.isEmpty())
				playSound(_app, Uri.parse(sound));

			if(prefs.getBoolean("notifications_new_message_vibrate", true))
				vibrate(_app);
		}
	}

	//______________________________________________________________________________________________
	private static void playSound(Context c, Uri sound)
	{
		if(sound == null)
			sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(c, sound);
		if(r != null)r.play();
	}
	//______________________________________________________________________________________________
	private static void vibrate(Context c)
	{
        Vibrator vibrator = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
		//long pattern[]={0,200,100,300,400};//pattern for vibration (mili seg ?)
		// 0 = start vibration with repeated count, use -1 if you don't want to repeat the vibration
		//vibrator.vibrate(pattern, -1);
		vibrator.vibrate(1000);//1seg
		//vibrator.cancel();
    }

	//______________________________________________________________________________________________
	// NOTIFICATION
	//______________________________________________________________________________________________
	public static void showAviso(Context c, String sTitulo, Aviso a, Intent intent)
	{
		/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		if(prefs.getBoolean("notifications_new_message_type", true))
			showNotificacionDlg(c, a, intent);
		else*/
			showNotificacion(c, sTitulo, a, intent);
	}
	//______________________________________________________________________________________________
	private static void showNotificacion(Context c, String titulo, Aviso a, Intent intent)
	{
		PowerManager pm = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wakeLock.acquire();
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(android.R.drawable.ic_menu_mylocation)//R.mipmap.ic_launcher)
				.setContentTitle(titulo)
				.setContentText(a.getDescripcion())
				.setDefaults(Notification.DEFAULT_ALL)
				.setContentIntent(PendingIntent.getActivity(c, 0, intent, 0))
				.setAutoCancel(true);
		NotificationManager notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		Integer iId = Integer.parseInt(a.getObjectId().substring(0,6).replace('-','0'), 16);
		notificationManager.notify(iId, notificationBuilder.build());
		wakeLock.release();
	}
	//______________________________________________________________________________________________
	/*private static void showNotificacionDlg(Context c, Aviso a, Intent intent)
	{
		Intent i = new Intent(c, ActAvisoDlg.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra("aviso", a);
		i.putExtra("intent", intent);
		c.startActivity(i);
		//Util.playNotificacion(c);Aqui no funcionaria
	}*/


	//______________________________________________________________________________________________
	/// TEXT TO SPEECH
	//______________________________________________________________________________________________
	private static TextToSpeech tts = null;
	public static void hablar(final Context c, String texto)
	{
		if(tts == null)
		tts = new TextToSpeech(c.getApplicationContext(), new TextToSpeech.OnInitListener()
		{
   			@Override
   			public void onInit(int status)
			{
				if(status != TextToSpeech.ERROR)
					tts.setLanguage(c.getResources().getConfiguration().locale);//new Locale("es", "ES");Locale.forLanguageTag("ES")
			}
		});
		//tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);//DEPRECATED
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    		ttsGreater21(c, tts, texto);
		else
    		ttsUnder20(tts, texto);

	}
	//______________________________________________________________________________________________
	@SuppressWarnings("deprecation")
	private static void ttsUnder20(TextToSpeech tts, String texto)
	{
		//System.err.println("------ttsUnder20");
    	HashMap<String, String> map = new HashMap<>();
    	map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
    	tts.speak(texto, TextToSpeech.QUEUE_FLUSH, map);
	}
	//______________________________________________________________________________________________
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static void ttsGreater21(Context c, TextToSpeech tts, String texto)
	{
		//System.err.println("------ttsGreater21");
    	String utteranceId=c.hashCode() + "";
    	tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
	}

	//______________________________________________________________________________________________
	/// CONFIG
	//______________________________________________________________________________________________
	public static boolean isAutoArranque(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getBoolean("is_auto_arranque", true);
	}

	//______________________________________________________________________________________________
	private static final String PREF_TRACKING = "tracking_prefs";
	private static final String ID_TRACKING = "id_tracking_route";
	public static void setTrackingRoute(String sIdRoute)
	{
		SharedPreferences sp = _app.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(ID_TRACKING, sIdRoute);
		editor.commit();
	}
	public static String getTrackingRoute()
	{
		SharedPreferences sp = _app.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
 		return sp.getString(ID_TRACKING, "");
	}

	/*public static void showSnackbar()
	{
		_app.getBaseContext().runOnUiThread(new Runnable(){public void run(){
							Snackbar.make(_coordinatorLayout, getString(R.string.ok_eliminar), Snackbar.LENGTH_LONG).show();
        						//Toast.makeText(activity, "Hello, world!", Toast.LENGTH_SHORT).show();
						}});
	}*/

}
