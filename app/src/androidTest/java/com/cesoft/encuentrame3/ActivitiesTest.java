package com.cesoft.encuentrame3;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;

import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsAnything.anything;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
// TODO: Run your tests with Firebase Test Lab
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivitiesTest
{
	//----------------------------------------------------------------------------------------------
	/*@Before
	public void ini()
	{
	}*/
	//private void wait(float f){for(int i=0; i < Integer.MAX_VALUE*f; i++);}//TODO: idlingResource

	////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	@Rule
	public ActivityTestRule<ActMain> activityRule = new ActivityTestRule<>(
			ActMain.class,
			true,     // initialTouchMode
			true);   // launchActivity. False so we can customize the intent per test method

	//----------------------------------------------------------------------------------------------
	@Test
	public void testMain()	// INSTRUMENTATION
	{
		//Mockito.when(clock.getNow()).thenReturn(new DateTime(2008, 9, 23, 0, 0, 0));
		//activityRule.launchActivity(new Intent());
		ActMain activity = activityRule.getActivity();

		View container = activity.findViewById(R.id.container);
		assertThat(container, instanceOf(ViewPager.class));

		View viewById = activity.findViewById(R.id.listView);
		assertThat(viewById, notNullValue());
		assertThat(viewById, instanceOf(ListView.class));

		//Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// LUGAR
	@Rule
	public ActivityTestRule<ActLugar> ruleActLugar = new ActivityTestRule<ActLugar>(ActLugar.class)
	{
		@Override
		protected Intent getActivityIntent()
		{
			Lugar l = new Lugar();
			l.setId("id_lugar_69");
			l.setLatLon(40.69, -3.69);
			l.setNombre("nombre lugar 69");
			l.setDescripcion("desc lugar 69");
			l.setFecha(new Date());
			//
			InstrumentationRegistry.getTargetContext();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.putExtra(Objeto.NOMBRE, l);
			return intent;
		}
	};
	//----------------------------------------------------------------------------------------------
	@Test
	//@UiThreadTest
	public void testActLugar1() throws Exception		// INSTRUMENTATION
	{
		ActLugar act = ruleActLugar.getActivity();
		View txtNombre = act.findViewById(R.id.txtNombre);
		assertThat(txtNombre, notNullValue());
		assertThat(txtNombre, instanceOf(EditText.class));
		EditText textView = (EditText)txtNombre;
		assertThat(textView.getText().toString(), is("nombre lugar 69"));

		EditText txtDes = (EditText)act.findViewById(R.id.txtDescripcion);
		assertThat(txtDes.getText().toString(), is("desc lugar 69"));

		TextView lblPos = (TextView) act.findViewById(R.id.lblPosicion);
		assertThat(lblPos.getText().toString(), is("40.69000/-3.69000"));

		act.runOnUiThread(act::onBackPressed);
/*		act.runOnUiThread(() ->
		{
			act.onBackPressed();
			//assertTrue(act.isFinishing());
		});*/
		///assertTrue(act.isFinishing());
	}


	//----------------------------------------------------------------------------------------------
	@Test
	//@UiThreadTest
	public void testActLugarEditar() throws Exception	// ESPRESSO
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
		onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.lugares)))
				.atPosition(1)		// Position 0 is the title of the list...
				.onChildView(withId(R.id.btnEditar)).perform(click());	// Click on editar
		onView(withText(R.string.editar_lugar)).check(matches(isDisplayed()));
		//----
		// Open the overflow menu OR open the options menu, depending on if the device has a hardware or software overflow menu button.
		//openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());//Solo si esta oculto?
//for(int i=0; i < Integer.MAX_VALUE/120; i++);//TODO:mejorar: no da tiempo al dialog a mostrarse...
		onView(withId(R.id.menu_eliminar)).perform(click());
//for(int i=0; i < Integer.MAX_VALUE/60; i++);//TODO:mejorar: no da tiempo al dialog a mostrarse...
		onView(withText(R.string.seguro_eliminar)).check(matches(isDisplayed()));
		onView(withText(R.string.cancelar)).perform(click());
		//----
		onView(withId(R.id.fabVolver)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}

	/*@Test
	public void testActLugar5() throws Exception
	{
		ActLugar act = ruleActLugar.getActivity();

		//openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
		onView(withId(R.id.menu_eliminar)).perform(click());
		//onView(withText(act.getString(R.string.seguro_eliminar))).check(matches(isDisplayed()));
		//onView(withId(R.id.myDialogTextId)).check(matches(allOf(withText(myDialogText), isDisplayed()));

	}*/
	//----------------------------------------------------------------------------------------------
	/*@Mock ActLugar _actLugar;
	@Mock Util _util;
	@Mock Application _app;
	@Test
	public void testActLugar2() throws Exception	// MOCK
	{
		//_actLugar = new ActLugar();
		//_actLugar.onCreate(null);
		//verify(_actLugar._presenter).ini(any());
		//PreLugar presenter = new PreLugar(_app, _util);
		//verify(presenter).ini;
	}*/


	////////////////////////////////////////////////////////////////////////////////////////////////
	// RUTA
	//----------------------------------------------------------------------------------------------
	@Test
	public void testActRutaEditar() throws Exception	// ESPRESSO
	{
		onView(withId(R.id.container)).perform(swipeLeft());//Arrastrar hacia la izquierda para ver rutas
		//onView(withId(R.id.container)).perform(swipeRight());
		//onView(withText(R.string.rutas)).perform(click());
//for(int i=0; i < Integer.MAX_VALUE/6; i++);//TODO:mejorar: no da tiempo al dialog a mostrarse...
		onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.rutas)))
				.atPosition(1)		// Position 0 is the title of the list...
				.onChildView(withId(R.id.btnEditar)).perform(click());	// Click on editar

		try////TODO: idlingResource
		{
			onView(withText(R.string.editar_ruta)).check(matches(isDisplayed()));
		}
		catch(Exception ignore)
		{
			Thread.sleep(100);
			onView(withText(R.string.editar_ruta)).check(matches(isDisplayed()));
		}
		//----
		onView(withId(R.id.txtNombre)).perform(clearText(), typeText("Nuevo nombre"), closeSoftKeyboard());
		onView(withText(R.string.editar_ruta)).perform(pressBack());
		// OR
		//onView(withId(R.id.fabVolver)).perform(click());
		onView(withText(R.string.seguro_salir)).check(matches(isDisplayed()));
		//----
		onView(withText(R.string.salir)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}



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
		for(int i=0; i < Integer.MAX_VALUE; i++)
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
		onView(withText(R.string.seguro_salir)).check(matches(isDisplayed()));
		//----
		onView(withText(R.string.salir)).perform(click());
		onView(withText(R.string.title_activity_act_main)).check(matches(isDisplayed()));
	}
	//----------------------------------------------------------------------------------------------
	@Test
	public void testActAvisoEliminar() throws Exception	// ESPRESSO
	{
		onView(withId(R.id.container)).perform(swipeLeft(), swipeLeft());//Arrastrar hacia la izquierda para ver avisos

		for(int i=0; i < Integer.MAX_VALUE; i++)
		try////TODO: idlingResource
		{
			onData(anything()).inAdapterView(allOf(withId(R.id.listView), withContentDescription(R.string.avisos)))
					.atPosition(1).onChildView(withId(R.id.btnEditar)).perform(click());
			onView(withText(R.string.editar_aviso)).check(matches(isDisplayed()));
			break;
		}
		catch(Exception ignore) { Thread.sleep(100); }
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
