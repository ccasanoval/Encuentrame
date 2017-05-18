package com.cesoft.encuentrame3;

import android.app.Application;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;

import com.cesoft.encuentrame3.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static junit.framework.Assert.assertTrue;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
//
@RunWith(JUnit4.class)
public class UtilTest
{
	private Util _util;

	//----------------------------------------------------------------------------------------------
	@Before
	public void ini()
	{
		_util = new Util(null, null, null, null, null);//Application app, SharedPreferences sp, LocationManager lm, NotificationManager nm, PowerManager pm);
	}

	//----------------------------------------------------------------------------------------------
	@Test
	public void testFormatTiempo()
	{
		String s = _util.formatTiempo(30*60*1000);
		System.out.println("testFormatTiempo------------------------------------ "+s);
		assertTrue(s.equals("00h 30m 00s"));
	}

	//----------------------------------------------------------------------------------------------
	@Test
	public void testFormatFechaTiempo() throws Exception
	{
		String sFecha = "16/08/1980 13:45:59";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d = sdf.parse(sFecha);

		String s = _util.formatFechaTiempo(d);
		System.out.println("testFormatFechaTiempo------------------------------------ "+s);
		assertTrue(s.equals(sFecha));

		d.setTime(0);
		s = _util.formatFechaTiempo(d);
		System.out.println("testFormatFechaTiempo------------------------------------ "+s);
		assertTrue(s.equals("01/01/1970 00:00:00"));
	}

	//----------------------------------------------------------------------------------------------
	@Test
	public void testFormatFecha() throws Exception
	{
		String sFecha = "16/08/1980";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d = sdf.parse(sFecha);

		String s = _util.formatFecha(d);
		System.out.println("testFormatFecha------------------------------------ "+s);
		assertTrue(s.equals(sFecha));

		d.setTime(0);
		s = _util.formatFecha(d);
		System.out.println("testFormatFecha------------------------------------ "+s);
		assertTrue(s.equals("01/01/1970"));
	}
}


/*
import org.joda.time.DateTime;
		import org.junit.Before;
		import org.junit.Rule;
		import org.junit.Test;
		import org.junit.runner.RunWith;
		import org.mockito.Mockito;

		import javax.inject.Inject;
		import javax.inject.Singleton;

		import dagger.Component;

		import static android.support.test.espresso.Espresso.onView;
		import static android.support.test.espresso.assertion.ViewAssertions.matches;
		import static android.support.test.espresso.matcher.ViewMatchers.withId;
		import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
	@Inject
	Clock clock;

	@Singleton
	@Component(modules = MockClockModule.class)
	public interface TestComponent extends DemoComponent {
		void inject(MainActivityTest mainActivityTest);
	}

	@Rule
	public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(
			MainActivity.class,
			true,     // initialTouchMode
			false);   // launchActivity. False so we can customize the intent per test method

	@Before
	public void setUp() {
		Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
		DemoApplication app
				= (DemoApplication) instrumentation.getTargetContext().getApplicationContext();
		TestComponent component = (TestComponent) app.component();
		component.inject(this);
	}

	@Test
	public void today() {
		Mockito.when(clock.getNow()).thenReturn(new DateTime(2008, 9, 23, 0, 0, 0));

		activityRule.launchActivity(new Intent());

		onView(withId(R.id.date))
				.check(matches(withText("2008-09-23")));
	}

	@Test
	public void intent() {
		DateTime dateTime = new DateTime(2014, 10, 15, 0, 0, 0);
		Intent intent = new Intent();
		intent.putExtra(MainActivity.KEY_MILLIS, dateTime.getMillis());

		activityRule.launchActivity(intent);

		onView(withId(R.id.date))
				.check(matches(withText("2014-10-15")));
	}
}*/