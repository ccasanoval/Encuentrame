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
import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.cesoft.encuentrame3.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by CESoft on 26/06/2019
public class Voice implements RecognitionListener {
    private static final String TAG = "Voice";
    public static final String NAME = "Voice";
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


    // implements RecognitionListener
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
        Log.e(TAG, "onResults=\n"+text);
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
    // implements RecognitionListener


    // Word Process
    //----------------------------------------------------------------------------------------------
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
    //private List commandList = new ArrayList();
    private void processCommand(ArrayList<String> matches) {
        int lengthDiff = 0;
        int minDistance = Integer.MAX_VALUE;
        int bestCommandId = Integer.MIN_VALUE;
        String bestCommandStr = "";

        if(commandStr == null) {
            commandStr = new String[commandId.length];
            for (int i=0; i < commandId.length; i++) {
                commandStr[i] = app.getString(commandId[i]);
                //commandList.add(commandStr[i]);
            }
        }
        for(int i=0; i < commandId.length; i++) {
            int id = commandId[i];
            String cmd = commandStr[i];
            for(String match : matches) {
                int dist = Texto.calculateDistance(cmd, match);
                Log.e(TAG, "\nA) CMD: "+cmd+" -> "+match+" = "+dist);

                Texto t = new Texto(match, cmd);
                Log.e(TAG, "b) NEARER: "+t.computeNearestWord()+" : DIST: "+t.computeShortestDistance());
                if(minDistance > t.computeShortestDistance()) {
                    minDistance = t.computeShortestDistance();
                    bestCommandId = id;
                    bestCommandStr = cmd;
                    lengthDiff = Math.abs(cmd.length() - match.length());
                }
            }
        }

        if(minDistance < 4) {//&& minDistance < lengthDiff+2
            sendCommand(bestCommandId, bestCommandStr);
        }
    }
    //----------------------------------------------------------------------------------------------
    // Word Process


    // Ask Sound Record Permissions
    //----------------------------------------------------------------------------------------------
    private boolean checkPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();
        boolean isGranted = isPermissionGranted(permissionsList);
        if( !isGranted && !permissionsList.isEmpty()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null)
                activity.requestPermissions(permissionsList.toArray(new String[]{}), REQUEST_RECORD_PERMISSION);
            return false;
        }
        return true;
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isPermissionGranted(ArrayList<String> permissionsList) {
        String permission = android.Manifest.permission.RECORD_AUDIO;
        if(app.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "isPermissionGranted----------------- NOT GRANTED "+activity);
            //if(activity != null && activity.shouldShowRequestPermissionRationale(permission))
            {
                Log.e(TAG, "isPermissionGranted----------------- ASK");
                permissionsList.add(permission);
            }
            return false;
        }
        return true;
    }
    //----------------------------------------------------------------------------------------------
    // Ask Sound Record Permissions


    // Event Bus Messages
    //----------------------------------------------------------------------------------------------
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
        private String text;
        public String getText() { return text; }
        CommandEvent(int command, String text) {
            this.command = command;
            this.text = text;
        }

    }
    //----------------------------------------------------------------------------------------------
    // Event Bus Messages


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



    private TextToSpeech textToSpeech = null;
    public void speak(String message) {
        //TODO: check options cos user can disable TTS
        textToSpeech = new TextToSpeech(app, status -> {
            android.util.Log.e(TAG, "*********************-------"+message);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ttsGreater20(message);
            } else {
                ttsUnder20(message);
            }
        });
        textToSpeech.setLanguage(Locale.getDefault());
        textToSpeech.setPitch(1f);
        textToSpeech.setSpeechRate(1f);
    }
    private void ttsUnder20(String text) {
        HashMap map = new HashMap<String, String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            map.putIfAbsent(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater20(String text) {
        String utteranceId = hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


}
