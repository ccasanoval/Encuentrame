package com.cesoft.encuentrame3;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.cesoft.encuentrame3.presenters.PreLugar;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 11/05/2017.
//
@RunWith(AndroidJUnit4.class)
public class PresentersTest
{
	@Rule public ActivityTestRule<ActLugar> ruleLugar = new ActivityTestRule<>(ActLugar.class, true, true);

	//----------------------------------------------------------------------------------------------
	@Test
	public void testLugar()
	{
		//TODO: check that unsubscribe is called on presenter when finish or stoping the view

		ActLugar activity = ruleLugar.getActivity();
		//activity.
		activity._presenter.onSalir();

		assertTrue(activity.isFinishing());
	}
}
