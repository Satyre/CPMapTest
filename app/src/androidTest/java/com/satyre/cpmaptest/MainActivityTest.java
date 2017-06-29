package com.satyre.cpmaptest;

import android.os.SystemClock;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * Created by Satyre on 29/06/2017.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private MainActivity mActivity = null;

    @Before
    public void setActivity() {
        mActivity = mActivityRule.getActivity();
    }

    @Test
    public void test() {

//        Test Move Map
        onView(withId(R.id.mapView)).perform((swipeLeft()));
        onView(withId(R.id.mapView)).perform((swipeRight()));
        onView(withId(R.id.mapView)).perform((swipeUp()));
        onView(withId(R.id.mapView)).perform((swipeDown()));

//      Test autocomplete view

        onView(withId(R.id.autoCompleteView)).perform(click());
        onView(withId(R.id.autoCompleteView)).perform(closeSoftKeyboard());
        onView(withId(R.id.autoCompleteView)).perform(click());
        onView(withId(R.id.autoCompleteView)).perform(typeText("tour eif "));

        SystemClock.sleep(3000);


        // Tap on a suggestion.
        onView(withText("Batobus - Tour Eiffel - Musée d'Orsay, Paris, 75007, France"))
                .inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
                .perform(click());


    }
}