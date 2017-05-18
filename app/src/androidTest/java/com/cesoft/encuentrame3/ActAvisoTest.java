package com.cesoft.encuentrame3;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;

import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
//
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActAvisoTest
{
	//private void wait(float f){for(int i=0; i < Integer.MAX_VALUE*f; i++);}//TODO: idlingResource
	@Rule public ActivityTestRule<ActMain> activityRule = new ActivityTestRule<>(ActMain.class, true, true);

	//----------------------------------------------------------------------------------------------
	@Before public void ini(){}
	@After public void fin(){}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// AVISOS
	/*public class AvisoIdlingRes implements IdlingResource
	{
		private ActAviso _act;
		private ResourceCallback _callback;
		public AvisoIdlingRes(ActAviso activity) { this._act = activity; }

		@Override public String getName() { return "AvisoIdlingRes"; }

		@Override
		public boolean isIdleNow()
		{
			Boolean idle = isIdle();
			if(idle) _callback.onTransitionToIdle();
			return idle;
		}
		boolean isIdle()
		{
			return _act != null && _callback != null;// && _act.isSyncFinished();
		}
		@Override
		public void registerIdleTransitionCallback(ResourceCallback resourceCallback)
		{
			this._callback = resourceCallback;
		}
	}
	private AvisoIdlingRes _avisoIdlingRes;
	@Rule public ActivityTestRule<ActAviso> _actAvisoRule = new ActivityTestRule<>(ActAviso.class);
	@Before
	public void registerIntentServiceIdlingResource()
	{
		_avisoIdlingRes = new AvisoIdlingRes(_actAvisoRule.getActivity());
		Espresso.registerIdlingResources(_avisoIdlingRes);
	}
	@After
	public void unregisterIntentServiceIdlingResource()
	{
		Espresso.unregisterIdlingResources(_avisoIdlingRes);
	}*/
	//----------------------------------------------------------------------------------------------
	@Test
	public void testActAvisoEditar() throws Exception	// ESPRESSO
	{
		onView(withId(R.id.container)).perform(swipeLeft(), swipeLeft());//Arrastrar hacia la izquierda para ver avisos
		for(int i=0; i < 100; i++)
		try////TODO: idlingResource
		{
			onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.avisos)))
				.atPosition(1).onChildView(withId(R.id.btnEditar)).perform(click());
			onView(withText(R.string.editar_aviso)).check(matches(isDisplayed()));
			break;
		}
		catch(Exception ignore)
		{
			System.err.println("-----------------------------testActAvisoEditar---------------------------");
			Thread.sleep(100);
			//wait(0.01f);
		}
		//----
		onView(withId(R.id.txtNombre)).perform(clearText(), typeText("Nuevo nombre"), closeSoftKeyboard());
		onView(withText(R.string.editar_aviso)).perform(pressBack());
		// OR
		//onView(withId(R.id.fabVolver)).perform(click());
		for(int i=0; i < Integer.MAX_VALUE; i++)
		try
		{
			onView(withText(R.string.seguro_salir)).check(matches(isDisplayed()));
			break;
		}
		catch(Exception ignore)
		{
			Thread.sleep(100);
		}
		//----
		onView(withText(R.string.salir)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}
	//----------------------------------------------------------------------------------------------
	@Test
	public void testActAvisoEliminar()	// ESPRESSO
	{
		onView(withId(R.id.container)).perform(swipeLeft(), swipeLeft());//Arrastrar hacia la izquierda para ver avisos

		for(int i=0; i < 100; i++)
		try////TODO: idlingResource
		{
			onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.avisos)))
					.atPosition(1).onChildView(withId(R.id.btnEditar)).perform(click());
			onView(withText(R.string.editar_aviso)).check(matches(isDisplayed()));
			break;
		}
		catch(Exception e) { try{Thread.sleep(100);}catch(Exception ignore){}}
		//----
		// Open the overflow menu OR open the options menu, depending on if the device has a hardware or software overflow menu button.
		//openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());//Solo si esta oculto?
		onView(withId(R.id.menu_eliminar)).perform(click());
		onView(withText(R.string.seguro_eliminar)).check(matches(isDisplayed()));
		onView(withText(R.string.cancelar)).perform(click());
		//----
		onView(withId(R.id.fabVolver)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}
}
