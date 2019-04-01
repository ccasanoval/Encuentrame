package com.cesoft.encuentrame3;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/05/2017.
//
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActLugarTest2
{
	//////////////////////////////// INSTRUMENTATION
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
	public void testActLugar1() {
		ActLugar act = ruleActLugar.getActivity();
		View txtNombre = act.findViewById(R.id.txtNombre);
		assertThat(txtNombre, notNullValue());
		assertThat(txtNombre, instanceOf(EditText.class));
		EditText textView = (EditText)txtNombre;
		assertThat(textView.getText().toString(), is("nombre lugar 69"));

		EditText txtDes = act.findViewById(R.id.txtDescripcion);
		assertThat(txtDes.getText().toString(), is("desc lugar 69"));

		TextView lblPos = act.findViewById(R.id.lblPosicion);
		assertThat(lblPos.getText().toString(), is("40.69000/-3.69000"));

		act.runOnUiThread(act::onBackPressed);
//		act.runOnUiThread(() -> { act.onBackPressed(); });
		///assertTrue(act.isFinishing());
	}


}
