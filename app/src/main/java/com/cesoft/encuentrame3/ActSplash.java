package com.cesoft.encuentrame3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cesoft.encuentrame3.util.Log;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/02/2017.
// http://stackoverflow.com/questions/42017161/android-progress-bar-on-splash-screen
public class ActSplash extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, ActLogin.class);
		startActivity(intent);
		finish();
		//Log.e("SPLASH", "------------------------------SPLASH----------------------------");
	}
}
