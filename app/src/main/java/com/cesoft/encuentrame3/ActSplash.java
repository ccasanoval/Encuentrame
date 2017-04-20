package com.cesoft.encuentrame3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by booster-bikes on 09/02/2017.
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
	}
}
