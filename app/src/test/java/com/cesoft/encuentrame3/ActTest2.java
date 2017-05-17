package com.cesoft.encuentrame3;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.presenters.PreLugar;
import com.cesoft.encuentrame3.presenters.PresenterBase;
import com.cesoft.encuentrame3.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 16/05/2017.
//
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ActTest2
{
	private VistaBase view;
	@Mock PreLugar presenter;
	@Mock Util util;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		view = new VistaBase()
		{
			@Override
			void ini(PresenterBase presenter, Util util, Objeto objDefecto, int id_layout)
			{
				super.ini(presenter, util, objDefecto, id_layout);
			}
		};
		view.ini(presenter, util, new Lugar(), R.layout.act_lugar);
	}

	@Test
	public void testEmpty() throws Exception
	{
		verify(presenter).ini(any());
		org.junit.Assert.assertTrue(view._txtNombre.getText().toString().isEmpty());
	}

	/*@Test
	public void testLeaveView() throws Exception
	{
		profileView.onDetachedFromWindow();
		verify(presenter).detachView();
	}

	@Test
	public void testReturnToView() throws Exception {
		reset(presenter);
		profileView.onAttachedToWindow();
		verify(presenter).attachView(profileView);
	}

	@Test
	public void testDisplay() throws Exception {
		UserProfile user = new UserProfile(USER);
		profileView.display(user);
		//asserThat(profileView.textUsername.getText().toString()).isEqualTo(USER);
	}*/
}
