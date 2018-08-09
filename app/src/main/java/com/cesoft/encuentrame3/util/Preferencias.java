package com.cesoft.encuentrame3.util;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Preferencias {

    // REFER TO  pref_notification.xml
    private static final String NOTIFICATION_RINGTONE = "notifications_new_message_ringtone";
    private static final String NOTIFICATION_VIBRATE = "notifications_new_message_vibrate";
    private static final String NOTIFICATION_LIGHTS = "notifications_new_message_lights";
    // REFER TO  pref_data_sync.xml
    private static final String SAVE_ALL_POINTS = "save_all_points";
    private static final String TRACKING_DELAY = "tracking_seconds";
    // NO pref.xml
    private static final String ID_TRACKING = "id_tracking_route";
    private static final String AUTOARRANQUE = "is_auto_arranque";


    private SharedPreferences _sp;
    @Inject
    public Preferencias(SharedPreferences sp) {
        _sp = sp;
    }


    //----------------------------------------------------------------------------------------------
    public boolean isSaveAllPoints() {
        return _sp.getBoolean(SAVE_ALL_POINTS, false);
    }
    public long getTrackingDelay() {
        long delay = Constantes.DELAY_TRACK_MIN;
        String s = _sp.getString(TRACKING_DELAY, "");
        try { delay = Long.parseLong(s) * 1000; } catch (Exception ignore) {}
        if(delay < Constantes.DELAY_TRACK_MIN / 2)///TODO:RELEASE: delay min = 30s  &&&  save all -> only op2501!!!
            delay = Constantes.DELAY_TRACK_MIN / 2;
        if(delay > 60*60*1000)
            delay = 60*60*1000;
        return delay;
    }
    //----------------------------------------------------------------------------------------------
    public String getNotificationRingtone() {
        return _sp.getString(NOTIFICATION_RINGTONE, "");
    }
    public boolean isNotificationVibrate() {
        return _sp.getBoolean(NOTIFICATION_VIBRATE, false);
    }
    public boolean isNotificationLights() {
        return _sp.getBoolean(NOTIFICATION_LIGHTS, false);
    }
    //----------------------------------------------------------------------------------------------
    public boolean isAutoArranque() {
        return _sp.getBoolean(AUTOARRANQUE, true);
    }
    //----------------------------------------------------------------------------------------------
    public void setTrackingRoute(String sIdRoute) {
        SharedPreferences.Editor editor = _sp.edit();
        editor.putString(ID_TRACKING, sIdRoute);
        editor.apply();//editor.commit(); Apply does it in background
    }
    public String getTrackingRoute() {
        return _sp.getString(ID_TRACKING, "");
    }

}
