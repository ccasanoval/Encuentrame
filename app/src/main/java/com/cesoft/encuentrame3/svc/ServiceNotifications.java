package com.cesoft.encuentrame3.svc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.cesoft.encuentrame3.ActAviso;
import com.cesoft.encuentrame3.ActMain;
import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.util.Constantes;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
@Singleton
public class ServiceNotifications {
    private static final String TAG = ServiceNotifications.class.getSimpleName();
    private static final String CHANNEL_ID_SERVICE = "mainservices";
    public static final String ACTION_STOP = "stop";

    private static final int RC_GEOTRACKING = 100;
    private static final int RC_GEOTRACKING_STOP = 101;
    private static final int RC_GEOFENCING = 200;
    private static final int RC_GEOFENCING_STOP = 201;

    private NotificationManager notificationManager;
    private Context appContext;
    @Inject
    public ServiceNotifications(Context appContext, NotificationManager notificationManager) {
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    Notification createForGeotracking(String subtitle) {

        Intent intentMain = new Intent(appContext, ActMain.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentMain.putExtra(Constantes.WIN_TAB, Constantes.RUTAS);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(appContext, RC_GEOTRACKING, intentMain, 0);

        Intent intentStop = new Intent(appContext, ActMain.class);
        intentStop.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentStop.putExtra(Constantes.WIN_TAB, Constantes.RUTAS);
        intentStop.putExtra(Constantes.MENSAJE, "Stoping tracking service...");
        intentStop.putExtra(ACTION_STOP, true);
        PendingIntent stopPendingIntent = PendingIntent.getActivity(appContext, RC_GEOTRACKING_STOP, intentStop, 0);

        Bitmap iconLarge = BitmapFactory.decodeResource(appContext.getResources(), R.mipmap.ic_launcher);
        return create(
                appContext,
                iconLarge,
                android.R.drawable.ic_menu_directions,//ic_menu_compass,
                "Tracking service",
                subtitle,
                mainPendingIntent,
                stopPendingIntent);
    }

    Notification createForGeofencing() {
        Context context = App.getInstance().getApplicationContext();

        Intent intentMain = new Intent(context.getApplicationContext(), ActMain.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentMain.putExtra(Constantes.WIN_TAB, Constantes.AVISOS);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context.getApplicationContext(), RC_GEOFENCING, intentMain, 0);

        Intent intentStop = new Intent(context.getApplicationContext(), ActMain.class);
        intentStop.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentStop.putExtra(Constantes.WIN_TAB, Constantes.AVISOS);
        intentStop.putExtra(Constantes.MENSAJE, "Stoping geofencing service...");
        intentStop.putExtra(ACTION_STOP, true);
        PendingIntent stopPendingIntent = PendingIntent.getActivity(context, RC_GEOFENCING_STOP, intentStop, 0);

        Bitmap iconLarge = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return create(
                context,
                iconLarge,
                android.R.drawable.ic_menu_mylocation,
                "Geofencing service",
                "",
                mainPendingIntent,
                stopPendingIntent);
    }

    private final AtomicInteger c = new AtomicInteger(0);
    private int getID() {
        return c.incrementAndGet();
    }
    public void createForAviso(String titulo, Aviso aviso) {
        Context context = App.getInstance().getApplicationContext();
        Intent intent = new Intent(context, ActAviso.class);
        intent.putExtra(Objeto.NOMBRE, aviso);

        int id = getID();
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(),
                id,
                intent,
                0);
        Bitmap iconLarge = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        String subtitle = aviso.getNombre();
        if( ! aviso.getDescripcion().isEmpty())
            subtitle += ":"+aviso.getDescripcion();
        Notification notification = create(
                context,
                iconLarge,
                android.R.drawable.ic_menu_compass,
                titulo,
                subtitle,
                mainPendingIntent,
                null);
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, notification);
    }

    private static Notification create(final Context context, final Bitmap iconBig, final int iconSmall,
        final CharSequence title, final CharSequence subtitle,
        final PendingIntent contentIntent, final PendingIntent stopPendingIntent) {

        boolean isService = (stopPendingIntent != null);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID_SERVICE)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setTicker(subtitle)
                .setSmallIcon(iconSmall)
                .setLargeIcon(iconBig)
                //.setShowWhen(false)
                //.setOngoing(true)
                .setVibrate(new long[0])
                ;
        if(isService) {
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.stop), stopPendingIntent);
            builder.setAutoCancel(false);
        }
        else {
            builder.setAutoCancel(true);
        }

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context.getApplicationContext());
            builder.setChannelId(CHANNEL_ID_SERVICE);

            notification = builder.build();
            if(isService) {
                notification.flags = notification.flags
                        | Notification.FLAG_FOREGROUND_SERVICE
                        | Notification.FLAG_ONLY_ALERT_ONCE;
            }
        }
        else {
            notification = builder.build();
        }

        return notification;
    }

    private static boolean isChannelCreated = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(Context context) {
        if( ! isChannelCreated) {
            String name = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE, name, importance);
            channel.setDescription(name);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                isChannelCreated = true;
            }
        }
    }
}
