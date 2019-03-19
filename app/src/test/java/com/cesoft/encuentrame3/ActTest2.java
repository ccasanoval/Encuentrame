package com.cesoft.encuentrame3;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;



////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 16/05/2017.
// TODO: READ: https://medium.com/@sergiygrechukha/android-ui-and-unit-tests-coverage-report-with-jacoco-and-sonarqube-1db5576f79b0
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ActTest2
{

	/*
	@Test
	public void test1()
	{
		/*ActMain act = Robolectric.setupActivity(ActMain.class);
		act.findViewById(R.id.fabNuevo).performClick();
		Intent expectedIntent = new Intent(act, ActLugar.class);
		assertThat(shadowOf(act).getNextStartedActivity()).isEqualTo(expectedIntent);* /
		ActLugar act = Robolectric.setupActivity(ActLugar.class);
		EditText txtNombre = (EditText)act.findViewById(R.id.txtNombre);
		txtNombre.setText("test");

	}

	ActivityController<ActLugar> _controller;
	ActLugar _act;
	@Before
	public void setUp() {
		// Call the "buildActivity" method so we get an ActivityController which we can use
		// to have more control over the activity lifecycle


		Intent intent = new Intent(RuntimeEnvironment.application, ActLugar.class);
		Lugar l = new Lugar();
		l.setLatLon(40.69, -3.69);
		l.setNombre("Lugar Test");
		l.setDescripcion("descripcion...");
		l.setId("69");
		l.setFecha(new Date());
		intent.putExtra(Objeto.NOMBRE, l);

		_controller = Robolectric.buildActivity(ActLugar.class, intent);
	}
	@After
	public void tearDown() {
		// Destroy activity after every test
		_controller
				.pause()
				.stop()
				.destroy();
	}

	@Test public void test2()
	{
		_act = _controller
				//.withIntent(intent) deprecated
				.create()
				.start()
				.resume()
				.visible()
				.get();
	}

	/*
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
			void ini(PresenterBase presenter, Util util, Objeto objDefecto, int idLayout)
			{
				super.ini(presenter, util, objDefecto, idLayout);
			}
		};
		view.ini(presenter, util, new Lugar(), R.layout.act_lugar);
	}

	@Test
	public void testEmpty() throws Exception
	{
		verify(presenter).ini(any());
		org.junit.Assert.assertTrue(view.txtNombre.getText().toString().isEmpty());
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
