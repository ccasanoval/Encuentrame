package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;

import javax.microedition.khronos.opengles.GL10;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/03/2016.
public class Util
{
	private static final String TAG = "CESoft:Util:";
	/*public enum Tipo
	{
		NADA(-1), LUGAR(1), RUTA(2), AVISO(3), BUSCAR(9);
		private int value;
		private Tipo(int value){this.value = value;}
		public int getValue(){return value;}
	}*/
	public static final int NADA=-1, LUGARES=0, RUTAS=1, AVISOS=2, BUSCAR=9, CONFIG=10;
	static final String TIPO = "tipo";
//	private static final String PREF_LOGIN = "login";
//	private static final String PREF_PWD = "password";
//	private static final String PREF_SAVE_LOGIN = "save_login";

	//______________________________________________________________________________________________
	// REFRESH LISTA RUTAS
	//______________________________________________________________________________________________
	private static IListaItemClick _refresh;
		static void setRefreshCallback(IListaItemClick refresh){_refresh = refresh;}
		static void refreshListaRutas()
		{
			if(_refresh!=null)_refresh.onRefreshListaRutas();
		}

	//______________________________________________________________________________________________
	// LOCATION
	//______________________________________________________________________________________________
	private static Location _locLast;
	public static void setLocation(Location loc)
	{
		_locLast=loc;
//System.err.println("Util.setLocation="+_locLast.getLatitude()+", "+_locLast.getLongitude()+", "+_locLast.getTime());
	}
	static Location getLocation(Context c)
	{
		Location location1=null, location2=null;
		try
		{
			if(c == null)return null;
			LocationManager locationManager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
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
//System.err.println("Util.getLocation="+_locLast.getLatitude()+", "+_locLast.getLongitude()+", "+_locLast.getTime());
		return _locLast;
    }


	//______________________________________________________________________________________________
	// NOTIFICATION UTILS
	//______________________________________________________________________________________________
	/*public static void playNotificacion(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
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
	}*/
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
	static void showAviso(Context c, String sTitulo, Aviso a, Intent intent)
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		String sSound = prefs.getString("notifications_new_message_ringtone", "");
		Boolean bVibrate = prefs.getBoolean("notifications_new_message_vibrate", false);
		Boolean bLights = prefs.getBoolean("notifications_new_message_lights", false);
		//android.media.Ringtone ring = RingtoneManager.getRingtone(c, Uri.parse("content://media/internal/audio/media/122"));//.play();

//System.err.println("------showNotificacion:      sound:"+sSound+"      vibrate:"+bVibrate+"     lights:"+bLights);

		PowerManager pm = (PowerManager)c.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wakeLock.acquire();

		Integer idNotificacion = _conversor(a.getId());
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(c)
				.setSmallIcon(android.R.drawable.ic_menu_mylocation)//R.mipmap.ic_launcher)
				.setLargeIcon(android.graphics.BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher))
				.setContentTitle(titulo)
				.setContentText(a.getNombre()+":"+a.getDescripcion())
				.setContentIntent(PendingIntent.getActivity(c, idNotificacion, intent, PendingIntent.FLAG_ONE_SHOT))
				.setAutoCancel(true)
				//.setDefaults(Notification.DEFAULT_ALL)
				;
		if( ! sSound.isEmpty())		notificationBuilder.setSound(Uri.parse(sSound));
		else						notificationBuilder.setSound(null);

		if(bLights)					//notificationBuilder.setLights(android.graphics.Color.RED, 500, 500);			//notificationBuilder.setLights(0xff00ff00, 300, 500);
			showLights(c, android.graphics.Color.RED);//TODO: no funciona, llamar directamente a luces
		else						notificationBuilder.setLights(0, 0, 0);

		if(bVibrate)				//notificationBuilder.setVibrate(new long[]{1000L});//TODO: no funciona, llamar directamente a vibrar
			vibrate(c);
		else						notificationBuilder.setVibrate(null);

			//notificationBuilder.setSound(Uri.parse(sSound));
		//visibility 	int: One of VISIBILITY_PRIVATE (the default), VISIBILITY_PUBLIC, or VISIBILITY_SECRET.
		NotificationManager notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(idNotificacion, notificationBuilder.build());
		wakeLock.release();

		//http://stackoverflow.com/questions/18094791/android-notification-pendingintent-open-activity-per-notification-click
		/*
       setNumber(g_push.Counter)
        .setWhen(g_push.Timestamp)
        .setDeleteIntent(PendingIntent.getBroadcast(c, 0, new Intent(ACTION_CLEAR_NOTIFICATION), PendingIntent.FLAG_CANCEL_CURRENT))
        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
        .setSound(Uri.parse(prefs.getString(
                SharedPreferencesID.PREFERENCE_ID_PUSH_SOUND_URI,
                "android.resource://ru.mail.mailapp/raw/new_message_bells")));
		*/
	}
	//______________________________________________________________________________________________
	private static int _conversor(String s)
	{
		// PARA Backendless: Integer.parseInt(a.getId().substring(0,6).replace('-','0'), 16);
		StringBuilder sb = new StringBuilder(10);
		s = s.replace("-", "");
		for(int i=0; i < 9; i++)
			sb.append(s.charAt(i) % 10);
		return Integer.valueOf(sb.toString().substring(0, 9));
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
	/*private static TextToSpeech tts = null;
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
	}*/

	//______________________________________________________________________________________________
	/// CONFIG
	//______________________________________________________________________________________________
	static boolean isAutoArranque(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return prefs.getBoolean("is_auto_arranque", true);
	}

	//______________________________________________________________________________________________
	private static final String PREF_TRACKING = "tracking_prefs";
	private static final String ID_TRACKING = "id_tracking_route";
	static void setTrackingRoute(Context c, String sIdRoute)
	{
		SharedPreferences sp = c.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(ID_TRACKING, sIdRoute);
		editor.apply();//editor.commit(); Aply does it in background
	}
	static String getTrackingRoute(Context c)
	{
		SharedPreferences sp = c.getSharedPreferences(PREF_TRACKING, Activity.MODE_PRIVATE);
 		return sp.getString(ID_TRACKING, "");
	}

	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	//______________________________________________________________________________________________
	static void return2Main(Activity act, boolean bDirty, String sMensaje)
	{
		Intent intent = new Intent();
		intent.putExtra(ActMain.DIRTY, bDirty);
		intent.putExtra(ActMain.MENSAJE, sMensaje);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	static void return2Main(Activity act, Filtro filtro)
	{
		Intent intent = new Intent();
		intent.putExtra(ActMain.DIRTY, true);
		intent.putExtra(Filtro.FILTRO, filtro);
		act.setResult(Activity.RESULT_OK, intent);
		act.finish();
	}
	static void openMain(Activity act, boolean bDirty, String sMensaje, int pagina)
	{
		Intent intent = new Intent(act, ActMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ActMain.PAGINA, pagina);//Go to specific section (ActMain.AVISOS...)
		intent.putExtra(ActMain.DIRTY, bDirty);
		intent.putExtra(ActMain.MENSAJE, sMensaje);
		act.startActivity(intent);//Para cuando abres la pantalla desde una notificacion...
		act.finish();
	}

	//----------------------------------------------------------------------------------------------
	// IMAGEN
	public static int getMaxTextureSize()
	{
		/*int[] maxTextureSize = new int[1];
		GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
		return  maxTextureSize[0];*/
		int[] maxSize = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0);
		return maxSize[0];
	}
	public static Bitmap imgResize(Bitmap b, int x, int y)
	{
		Bitmap background = Bitmap.createBitmap((int)x, (int)y, Bitmap.Config.ARGB_8888);
		float originalWidth = b.getWidth(), originalHeight = b.getHeight();
		Canvas canvas = new Canvas(background);
		float scale = x/originalWidth;
		float xTranslation = 0.0f, yTranslation = (y - originalHeight * scale)/2.0f;
		Matrix transformation = new Matrix();
		transformation.postTranslate(xTranslation, yTranslation);
		transformation.preScale(scale, scale);
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(b, transformation, paint);
		return background;
	}
	public static Bitmap imgScaleCompress(Bitmap b, String path)
	{

		/*Bitmap photo = (Bitmap) "your Bitmap image";
photo = Bitmap.createScaledBitmap(photo, 100, 100, false);
ByteArrayOutputStream bytes = new ByteArrayOutputStream();
photo.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

File f = new File(Environment.getExternalStorageDirectory()
        + File.separator + "Imagename.jpg");
f.createNewFile();
FileOutputStream fo = new FileOutputStream(f);
fo.write(bytes.toByteArray());
fo.close();*/
		try
		{
			java.io.OutputStream imagefile = new java.io.FileOutputStream(path);
			//b.compress(Bitmap.CompressFormat.PNG, 95, imagefile);
			b.compress(Bitmap.CompressFormat.JPEG, 95, imagefile);
			return BitmapFactory.decodeFile(path);
		}
		catch(Exception e)
		{
			Log.e(TAG, String.format("imgScaleCompress:e:%s",e), e);
			return null;
		}
	}

}
