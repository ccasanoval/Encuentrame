package com.cesoft.encuentrame3.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;


import com.cesoft.encuentrame3.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 26/06/2019
public class Voice implements RecognitionListener {
    private static final String TAG = "Voice";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private static final int MAX_RESULTS = 10;

    private Application app;
    private Activity activity;
    public void setActivity(Activity activity) { this.activity = activity; }

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private boolean isListening = false;

    public Voice(Application app) {
        this.app = app;
    }

    public void toggleStatus() {
        if(isListening)
            stopListening();
        else
            startListening();
    }

    private void startListening() {
        if( ! checkPermissions())return;
        isListening = true;
        Log.e(TAG, "isRecognitionAvailable: -------------------" + SpeechRecognizer.isRecognitionAvailable(app));

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage());   Log.e("AAAA", "------------------LANG: "+ Locale.getDefault().getLanguage());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)

        speech = SpeechRecognizer.createSpeechRecognizer(app);
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
        sendEvent();
    }

    public void stopListening(){
        isListening = false;
        if(speech != null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();
            speech = null;
        }
        sendEvent();
    }


    //implements RecognitionListener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.e(TAG, "onReadyForSpeech: ");
    }
    @Override
    public void onBeginningOfSpeech() {
        Log.e(TAG, "onBeginningOfSpeech: ");
    }
    @Override
    public void onRmsChanged(float rmsdB) {
        if(!isListening)stopListening();
    }
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.e(TAG, "onBufferReceived: ");
    }
    @Override
    public void onEndOfSpeech() {
        Log.e(TAG, "onEndOfSpeech: ");
    }

    @Override
    public void onError(int error) {
        Log.e(TAG, "onError: "+getErrorText(error));

        switch(error) {
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                stopListening();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            case SpeechRecognizer.ERROR_NO_MATCH:
                stopListening();
                startListening();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.e(TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        StringBuilder text = new StringBuilder();
        if(matches != null) {
            processCommand(matches);
            for(String result : matches) {
                text.append(result);
                text.append("\n");
            }
        }
        Log.e(TAG, "onResults="+text);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.e(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.e(TAG, "onEvent : "+eventType);
    }
    //----------------------------------------------------------------------------------------------
    //implements RecognitionListener


    private int[] commandId = new int[] {
        R.string.voice_new_point,
        R.string.voice_new_route,
        R.string.voice_new_alert,
        R.string.voice_start,
        R.string.voice_save,
        R.string.voice_cancel,
        R.string.voice_name,
        R.string.voice_description,
        R.string.voice_radious,
        R.string.voice_metres,
        R.string.voice_kilometers,
    };
    private String[] commandStr = null;
    private List commandList = new ArrayList();
    private void processCommand(ArrayList<String> matches) {
        if(commandStr == null) {
            commandStr = new String[commandId.length];
            for (int i=0; i < commandId.length; i++) {
                commandStr[i] = app.getString(commandId[i]);
                commandList.add(commandStr[i]);
            }
        }
        for(int i=0; i < commandId.length; i++) {
            int id = commandId[i];
            String cmd = commandStr[i];
            for(String match : matches) {
                int dist = Texto.calculateDistance(cmd, match);
                Log.e(TAG, "DIST: "+cmd+" -> "+match+" = "+dist);

                Texto t = new Texto(match, cmd);
                Log.e(TAG, "NEARER: "+t.computeNearestWord()+" : DIST: "+t.computeShortestDistance());
            }
        }
        //sendCommand(int command, String desc)
        //TODO: dividir con espacios
        //TODO: Comprobar si cada palabra se ajusta a la frase
    }


    private boolean checkPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();
        boolean isGranted = isPermissionGranted(permissionsList);
        if( !isGranted && !permissionsList.isEmpty()) {
            Log.e(TAG, "checkPermissions-----------------5555");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null)
                activity.requestPermissions(permissionsList.toArray(new String[]{}), REQUEST_RECORD_PERMISSION);
            return false;
        }
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(ArrayList<String> permissionsList) {
        String permission = android.Manifest.permission.RECORD_AUDIO;
        Log.e(TAG, "isPermissionGranted-----------------");
        if(app.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "isPermissionGranted----------------- NOT GRANTED "+activity);
            //if(activity != null && activity.shouldShowRequestPermissionRationale(permission))
            {
                Log.e(TAG, "isPermissionGranted----------------- ASK");
                permissionsList.add(permission);
            }
            return false;
        }
        Log.e(TAG, "isPermissionGranted----------------- TRUE ????");
        return true;
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    private void sendEvent() {
        EventBus.getDefault().post(new VoiceEvent(isListening));
    }
    public class VoiceEvent {
        private boolean isListening;
        public boolean isListening() { return isListening; }
        VoiceEvent(boolean isListening) { this.isListening = isListening; }
    }

    private void sendCommand(int command, String desc) {
        EventBus.getDefault().post(new CommandEvent(command, desc));
    }
    public class CommandEvent {
        private int command;
        public int getCommand() { return command; }
        private String desc;
        public String getDesc() { return desc; }
        CommandEvent(int command, String desc) {
            this.command = command;
            this.desc = desc;
        }

    }
}
