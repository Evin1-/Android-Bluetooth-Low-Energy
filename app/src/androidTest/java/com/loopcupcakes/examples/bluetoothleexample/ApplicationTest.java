package com.loopcupcakes.examples.bluetoothleexample;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.SwipeRefreshLayout;
import android.test.ApplicationTestCase;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResource() {
        Espresso.registerIdlingResources(mActivityTestRule.getActivity().idlingResource);
    }

    @Test
    public void testScanLEBluetooth() {
        onView(withId(R.id.scan_le_button)).check(matches(isEnabled()));
        onView(withId(R.id.scan_le_button)).perform(click());
        onView(withId(R.id.scan_le_button)).check(matches(isEnabled()));
    }

    @Test
    public void testRefreshScanLEBluetooth() {
        onView(withId(R.id.refresh_layout)).perform(swipeDown());
        onView(withId(R.id.refresh_layout)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (((SwipeRefreshLayout) view).isRefreshing()) {
                    throw new AssertionError("SwipeRefreshLayout is refreshing!");
                }
            }
        });
    }

    @Test
    public void testOrientationChange() {
        int requestedOrientation = mActivityTestRule.getActivity().getRequestedOrientation();
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mActivityTestRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        assertTrue(mActivityTestRule.getActivity().orientationChanged);
    }

    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(mActivityTestRule.getActivity().idlingResource);
    }
}