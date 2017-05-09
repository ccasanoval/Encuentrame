package com.cesoft.encuentrame3;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Lugar;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;


import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
// TODO: Run your tests with Firebase Test Lab
@RunWith(AndroidJUnit4.class)
public class ActivitiesTest
{
	//----------------------------------------------------------------------------------------------
	/*@Before
	public void ini()
	{
	}*/


	////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	@Rule
	public ActivityTestRule<ActMain> activityRule = new ActivityTestRule<>(
			ActMain.class,
			true,     // initialTouchMode
			true);   // launchActivity. False so we can customize the intent per test method

	//----------------------------------------------------------------------------------------------
	@Test
	public void testMain()
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
		//onView(withId(R.id.toolbar)). check(matches(withText("2008-09-23")));
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
			l.setLatitud(40.69);
			l.setLongitud(-3.69);
			l.setNombre("nombre lugar 69");
			l.setDescripcion("desc lugar 69");
			l.setFecha(new Date());
			//
			InstrumentationRegistry.getTargetContext();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.putExtra(Lugar.NOMBRE, l);
			return intent;
		}
	};
	//----------------------------------------------------------------------------------------------
	@Test
	public void ensureIntentDataIsDisplayed() throws Exception
	{
		ActLugar act = ruleActLugar.getActivity();
		View txtNombre = act.findViewById(R.id.txtNombre);
		assertThat(txtNombre,notNullValue());
		assertThat(txtNombre, instanceOf(EditText.class));
		EditText textView = (EditText)txtNombre;
		assertThat(textView.getText().toString(), is("nombre lugar 69"));

		EditText txtDes = (EditText)act.findViewById(R.id.txtDescripcion);
		assertThat(txtDes.getText().toString(), is("desc lugar 69"));

		TextView lblPos = (TextView) act.findViewById(R.id.lblPosicion);
		assertThat(lblPos.getText().toString(), is("40.69000/-3.69000"));
	}
}
