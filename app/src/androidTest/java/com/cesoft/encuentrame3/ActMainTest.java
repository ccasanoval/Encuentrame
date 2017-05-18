package com.cesoft.encuentrame3;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ListView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 18/05/2017.
//
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActMainTest
{
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

}
