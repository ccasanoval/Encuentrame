package com.cesoft.encuentrame3.util;

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
import android.speech.tts.TextToSpeech;

import com.cesoft.encuentrame3.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 26/06/2019
@Singleton
public class Voice implements RecognitionListener {
    private static final String TAG = "Voice";
    public static final String NAME = "Voice";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private static final int MAX_RESULTS = 5;

    private final Application app;
    private final Preferencias pref;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private boolean isListening = false;
    private boolean isListeningActive = false;

    @Inject
    public Voice(Application app, Preferencias pref) {
        this.app = app;
        this.pref = pref;
        if(pref.isVoiceEnabled()) {
            isListeningActive = true;
            startListening();
        }

        commandStr = new String[commandId.length];
        for(int i=0; i < commandId.length; i++) {
            commandStr[i] = app.getString(commandId[i]);
        }
    }

    public boolean isListening() {
        return isListeningActive;
    }
    public void turnOffListening() {
        stopListening();
        isListeningActive = false;
        sendEvent();
    }
    private void toggleFlag() {
        isListeningActive = !isListeningActive;
        sendEvent();
    }

    public void toggleListening() {
        toggleFlag();
        if(isListeningActive)
            startListening();
        else
            stopListening();
    }

    private void restartListening() {
        stopListening();
        startListening();
    }
    public void startListening() {
        if( ! isCheckPermissions) {
            Log.e(TAG, "startListening: NOT INITIALIZED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        if(isListening) {
            return;
        }

        isListening = true;

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)

        speech = SpeechRecognizer.createSpeechRecognizer(app);
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
    }

    public void stopListening() {
        isListening = false;
        if(speech != null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();
            speech = null;
            System.gc();
        }
    }


    //----------------------------------------------------------------------------------------------
    // implements RecognitionListener
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
        if( !isListeningActive) {
            stopListening();
        }
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
        switch(error) {
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Log.e(TAG, "onError: ERROR_RECOGNIZER_BUSY------------------------------------------------------------------ DID YOU CALL startListening twice ???");
                stopListening();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            case SpeechRecognizer.ERROR_NO_MATCH:
                restartListening();
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
//        Log.e(TAG, "onResults=\n"+text);
//        Log.e(TAG, "speech.startListening(recognizerIntent)---------------------------------------------------------------------"+speech+"/"+recognizerIntent);
        if(speech == null) {
            restartListening();
        }
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
    // implements RecognitionListener
    //----------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------
    // Word Process
    private final int[] commandId = new int[] {
            R.string.voice_new_point,
            R.string.voice_new_route,
            R.string.voice_new_route2,
            R.string.voice_new_alert,
            R.string.voice_start,
            R.string.voice_save,
            R.string.voice_cancel,
            R.string.voice_stop_route,
            R.string.voice_map,
            R.string.voice_stop_listening,
    };
    private final String[] commandStr;

    private void processCommand(ArrayList<String> matches) {
        int minDistance = Integer.MAX_VALUE;
        int bestCommandId = Integer.MIN_VALUE;
        String bestCommandStr = "";

        for(int i=0; i < commandId.length; i++) {
            int id = commandId[i];
            String cmd = commandStr[i];
            for(String match : matches) {

                int distance = Texto.calculateDistance(cmd, match);
                Log.e(TAG, "\nA) CMD: "+cmd+" -> "+match+" = "+distance);
                if(minDistance > distance) {
                    minDistance = distance;
                    bestCommandId = id;
                    bestCommandStr = cmd;
                }
            }
        }

        if(minDistance < 4) {
            sendCommand(bestCommandId, bestCommandStr);
        }
    }
    // Word Process
    //----------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------
    // Ask Sound Record Permissions
    private boolean isCheckPermissions = false;
    public void checkPermissions(Activity activity) {
        if(isCheckPermissions)return;
        ArrayList<String> permissionsList = new ArrayList<>();
        boolean isGranted = isPermissionGranted(permissionsList);
        if( !isGranted && !permissionsList.isEmpty()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                activity.requestPermissions(permissionsList.toArray(new String[]{}), REQUEST_RECORD_PERMISSION);
        }
        else
            isCheckPermissions = true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(ArrayList<String> permissionsList) {
        if( ! SpeechRecognizer.isRecognitionAvailable(app)) {
            isCheckPermissions = false;
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isCheckPermissions = true;
            return true;
        }

        String permission = android.Manifest.permission.RECORD_AUDIO;
        if(app.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "isPermissionGranted----------------- NOT GRANTED ");
            //if(activity != null && activity.shouldShowRequestPermissionRationale(permission))
            {
                Log.e(TAG, "isPermissionGranted----------------- ASK");
                permissionsList.add(permission);
            }
            isCheckPermissions = false;
        }
        else
            isCheckPermissions = true;
        return isCheckPermissions;
    }
    // Ask Sound Record Permissions
    //----------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------
    // Event Bus Messages
    private void sendEvent() {
        EventBus.getDefault().postSticky(new VoiceStatusEvent());
    }
    public class VoiceStatusEvent { }

    private void sendCommand(int command, String desc) {
        EventBus.getDefault().post(new CommandEvent(command, desc));
    }
    public class CommandEvent {
        private final int command;
        public int getCommand() { return command; }
        private final String text;
        public String getText() { return text; }
        CommandEvent(int command, String text) {
            this.command = command;
            this.text = text;
        }

    }
    // Event Bus Messages
    //----------------------------------------------------------------------------------------------

//    private String getErrorText(int errorCode) {
//        String message;
//        switch (errorCode) {
//            case SpeechRecognizer.ERROR_AUDIO:
//                message = "Audio recording error";
//                break;
//            case SpeechRecognizer.ERROR_CLIENT:
//                message = "Client side error";
//                break;
//            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
//                message = "Insufficient permissions";
//                break;
//            case SpeechRecognizer.ERROR_NETWORK:
//                message = "Network error";
//                break;
//            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
//                message = "Network timeout";
//                break;
//            case SpeechRecognizer.ERROR_NO_MATCH:
//                message = "No match";
//                break;
//            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
//                message = "RecognitionService busy";
//                break;
//            case SpeechRecognizer.ERROR_SERVER:
//                message = "error from server";
//                break;
//            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
//                message = "No speech input";
//                break;
//            default:
//                message = "Didn't understand, please try again.";
//                break;
//        }
//        return message;
//    }


    private TextToSpeech textToSpeech = null;
    public void speak(String message) {
        if( !pref.isSpeechEnabled()) return;
        textToSpeech = new TextToSpeech(app, status -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater20(message);
            }
            else {
                ttsUnder20(message);
            }
        });
        textToSpeech.setLanguage(Locale.getDefault());
        textToSpeech.setPitch(1f);
        textToSpeech.setSpeechRate(1f);
    }
    private void ttsUnder20(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), "MessageId");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater20(String text) {
        String utteranceId = hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

}
