package com.cesoft.encuentrame3.util;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
@Singleton
public class Preferencias {

    // REFER TO  pref_data_sync.xml
    private static final String SAVE_ALL_POINTS = "save_all_points";
    private static final String TRACKING_DELAY = "tracking_seconds";
    // NO pref.xml
    private static final String ID_TRACKING = "id_tracking_route";
    private static final String NAME_TRACKING = "name_tracking_route";
    private static final String AUTOARRANQUE = "is_auto_arranque";
    // REFER TO  pref_voice.xml
    private static final String SPEECH_ENABLED = "speech_enabled";
    private static final String VOICE_ENABLED = "voice_enabled";


    private SharedPreferences sp;
    @Inject
    public Preferencias(SharedPreferences sp) {
        this.sp = sp;
    }


    //----------------------------------------------------------------------------------------------
    public boolean isSaveAllPoints() {
        return sp.getBoolean(SAVE_ALL_POINTS, false);
    }
    public long getTrackingDelay() {
        long delay = Constantes.MIN_TRACK_DELAY;
        String s = sp.getString(TRACKING_DELAY, "30");
        if(s == null) s = "30";
        try { delay = Long.parseLong(s) * 1000; } catch (Exception ignore) {}
        if(delay < Constantes.MIN_TRACK_DELAY / 2)///TODO:RELEASE: delay min = 30s  &&&  save all -> only op2501!!!
            delay = Constantes.MIN_TRACK_DELAY / 2;
        if(delay > 60*60*1000)
            delay = 60*60*1000L;
        return delay;
    }

    //----------------------------------------------------------------------------------------------
    public boolean isAutoArranque() {
        return sp.getBoolean(AUTOARRANQUE, true);
    }
    //----------------------------------------------------------------------------------------------
    void setTrackingRoute(String sIdRoute, String nameRoute) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(ID_TRACKING, sIdRoute);
        editor.putString(NAME_TRACKING, nameRoute);
        editor.apply();//editor.commit(); Apply does it in background
    }
    String getIdTrackingRoute() {
        return sp.getString(ID_TRACKING, "");
    }
    String getNameTrackingRoute() {
        return sp.getString(NAME_TRACKING, "");
    }

    boolean isSpeechEnabled() {
        return sp.getBoolean(SPEECH_ENABLED, true);
    }
    boolean isVoiceEnabled() {
        return sp.getBoolean(VOICE_ENABLED, false);
    }
}
