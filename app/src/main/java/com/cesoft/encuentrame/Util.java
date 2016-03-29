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
import android.support.v4.app.NotificationCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.cesoft.encuentrame.models.Aviso;

import java.util.HashMap;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
public class Util
{
	public static final int NADA=-1, LUGARES=0, RUTAS=1, AVISOS=2;
	public static final String TIPO = "tipo";

	//______________________________________________________________________________________________
	// REFRESH LISTA RUTAS
	//______________________________________________________________________________________________
	private static CesIntLista _refresh;
		public static void setRefreshCallback(CesIntLista refresh){_refresh = refresh;}
		public static void refreshListaRutas()
		{
			System.err.println("----------------------Util.refreshListaRutas  = "+_refresh);
			if(_refresh!=null)_refresh.onRefreshListaRutas();
		}

	//______________________________________________________________________________________________
	// INIT
	//______________________________________________________________________________________________
	private static Application _app;
		public static void setApplication(Application app){_app = app;}
		public static Application getApplication(){return _app;}
	private static Context _svcContext;
		public static void setSvcContext(Context c){_svcContext = c;}
	public static void initBackendless(Context c)
	{
System.err.println("---------------Util.initBackendless c = "+c);
		Backendless.initApp(c, BackendSettings.APP, BackendSettings.KEY, BackendSettings.VER);
	}

	//______________________________________________________________________________________________
	// LOCATION
	//______________________________________________________________________________________________
	protected  static Location _locLast;
	public static void setLocation(Location loc)
	{
		_locLast=loc;
System.err.println("Util.setLocation="+_locLast.getLatitude()+", "+_locLast.getLongitude()+", "+_locLast.getTime());
	}
	public static Location getLocation()
	{
		Location location1=null, location2=null;
		try
		{
			LocationManager locationManager = (LocationManager)_svcContext.getSystemService(Context.LOCATION_SERVICE);
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
System.err.println("Util.getLocation="+_locLast.getLatitude()+", "+_locLast.getLongitude()+", "+_locLast.getTime());
		return _locLast;
    }


	//______________________________________________________________________________________________
	// NOTIFICATION UTILS
	//______________________________________________________________________________________________
	public static void playNotificacion()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_svcContext);
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
	private static void showLights(Context c, int color)
	{
		NotificationManager notif = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		notif.cancel(1); // clear previous notification
		final Notification notification = new Notification();
		notification.ledARGB = color;
		notification.ledOnMS = 1000;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notif.notify(1, notification);
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
	//private static int _idNotificacion = 1;
	private static void showNotificacion(Context c, String titulo, Aviso a, Intent intent)
	{
		PowerManager pm = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wakeLock.acquire();

		Integer idNotificacion = Integer.parseInt(a.getObjectId().substring(0,6).replace('-','0'), 16);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(android.R.drawable.ic_menu_mylocation)//R.mipmap.ic_launcher)
				.setLargeIcon(android.graphics.BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(titulo)
				.setContentText(a.getNombre()+":"+a.getDescripcion())
				.setDefaults(Notification.DEFAULT_ALL)
				.setContentIntent(PendingIntent.getActivity(c, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
				.setAutoCancel(true);
		NotificationManager notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(idNotificacion, notificationBuilder.build());
		wakeLock.release();

		//http://stackoverflow.com/questions/18094791/android-notification-pendingintent-open-activity-per-notification-click
		/*
		NotificationCompat.Builder b = new NotificationCompat.Builder(c);
       b.setNumber(g_push.Counter)
        .setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.list_avatar))
        .setSmallIcon(R.drawable.ic_stat_example)
        .setAutoCancel(true)
        .setContentTitle(pushCount > 1 ? c.getString(R.string.stat_messages_title) + pushCount : title)
        .setContentText(pushCount > 1 ? push.ProfileID : mess)
        .setWhen(g_push.Timestamp)
        .setContentIntent(PendingIntent.getActivity(c, 0, it, PendingIntent.FLAG_UPDATE_CURRENT))
        .setDeleteIntent(PendingIntent.getBroadcast(c, 0, new Intent(ACTION_CLEAR_NOTIFICATION), PendingIntent.FLAG_CANCEL_CURRENT))
        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
        .setSound(Uri.parse(prefs.getString(
                SharedPreferencesID.PREFERENCE_ID_PUSH_SOUND_URI,
                "android.resource://ru.mail.mailapp/raw/new_message_bells")));
		*/
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
		SharedPreferences sp = _svcContext.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(ID_TRACKING, sIdRoute);
		editor.apply();//editor.commit(); Aply does it in background
	}
	public static String getTrackingRoute()
	{
		SharedPreferences sp = _svcContext.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
 		return sp.getString(ID_TRACKING, "");
	}

	/*public static void showSnackbar()
	{
		_app.getBaseContext().runOnUiThread(new Runnable(){public void run(){
							Snackbar.make(_coordinatorLayout, getString(R.string.ok_eliminar), Snackbar.LENGTH_LONG).show();
        						//Toast.makeText(activity, "Hello, world!", Toast.LENGTH_SHORT).show();
						}});
	}*/


	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	//______________________________________________________________________________________________
	public static void return2Main(Activity act, boolean bDirty, String sMensaje)
	{
		Intent intent = new Intent();
		intent.putExtra(ActMain.DIRTY, bDirty);
		intent.putExtra(ActMain.MENSAJE, sMensaje);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	public static void openMain(Activity act, boolean bDirty, String sMensaje, int pagina)
	{
		Intent intent = new Intent(act, ActMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ActMain.PAGINA, pagina);//Go to specific section (ActMain.AVISOS...)
		intent.putExtra(ActMain.DIRTY, bDirty);
		intent.putExtra(ActMain.MENSAJE, sMensaje);
		act.startActivity(intent);//Para cuando abres la pantalla desde una notificacion...
		act.finish();
	}


	//______________________________________________________________________________________________
	// LOGIN
	public static String getUsuario()
	{
		//TODO: Check preferences...
		return "quake1978";
	}
	public static String getClave()
	{
		//TODO: Check preferences...
		return "colt1911";
	}
	public static boolean login(AsyncCallback<BackendlessUser> res)
	{
		String usr = getUsuario();
		String pwd = getClave();
		if( ! usr.isEmpty() && ! pwd.isEmpty())
		{
System.err.println("Util.login: "+usr);
			login(usr, pwd, res);
			return true;
		}
System.err.println("Util.login: no hay usr y pwd en settings..."+usr+" / "+pwd);
		return false;
	}
	public static void login(String usr, String pwd, AsyncCallback<BackendlessUser> res)
	{
		BackendlessUser bu = Backendless.UserService.CurrentUser();
		if(bu != null)
		{
			res.handleResponse(bu);
System.err.println("Util.login: Usuario ya logado: "+bu);
		}
		else
		{
			Backendless.UserService.login(usr, pwd, res);
System.err.println("Util.login: logando...");
		}
	}
	public static boolean isLogged()
	{
		BackendlessUser usr = Backendless.UserService.CurrentUser();
		return (usr != null);
	}
}
