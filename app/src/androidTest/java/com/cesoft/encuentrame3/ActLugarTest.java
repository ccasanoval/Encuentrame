package com.cesoft.encuentrame3;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/05/2017.
//
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActLugarTest
{
	///////////////////////////////////// ESPRESSO
	@Rule public ActivityTestRule<ActMain> activityRule = new ActivityTestRule<>(ActMain.class, true, true);
	//----------------------------------------------------------------------------------------------
	@Test
	//@UiThreadTest
	public void testActLugarEditar()    // ESPRESSO
	{
		// No hay una manera de NO utilizar un valor actual real (Park) ??
		//onView(allOf(withId(R.id.btnEditar), hasSibling(allOf(withId(R.id.txtNombre), withText("Park"))))).perform(click());//scrollTo(),
		onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.lugares)))
				.atPosition(1)		// Position 0 is the title of the list...
				.onChildView(withId(R.id.btnEditar)).perform(click());	// Click on editar
		onView(withText(R.string.editar_lugar)).check(matches(isDisplayed()));
		//----
		onView(withId(R.id.txtNombre)).perform(clearText(), typeText("Nuevo nombre\n"), closeSoftKeyboard());
		onView(withText(R.string.editar_lugar)).perform(pressBack());
		// OR
		//onView(withId(R.id.fabVolver)).perform(click());
		onView(withText(R.string.seguro_salir)).check(matches(isDisplayed()));
		//----
		onView(withText(R.string.salir)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}

	//----------------------------------------------------------------------------------------------
	@Test
	public void testActLugarEliminar() throws Exception	// ESPRESSO
	{
		// No hay una manera de NO utilizar un valor actual real (Park) ??
		//onView(allOf(withId(R.id.btnEditar), hasSibling(allOf(withId(R.id.txtNombre), withText("Park"))))).perform(click());//scrollTo(),
		for(int i=0; i < 100; i++)
		try{
			onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.lugares)))
				.atPosition(1)		// Position 0 is the title of the list...
				.onChildView(withId(R.id.btnEditar)).perform(click());	// Click on editar
			onView(withText(R.string.editar_lugar)).check(matches(isDisplayed()));
			break;
		}catch(Exception ignore){Thread.sleep(100);}
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
