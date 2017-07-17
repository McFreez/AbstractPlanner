package com.abstractplanner;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SlidingDrawer;

import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.fragments.AddAreaFragment;
import com.abstractplanner.fragments.AddTaskFragment;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.fragments.TodayTasksFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public List<Area> areas = new ArrayList<>();
    public List<Day> days = new ArrayList<>();

    private DrawerLayout mDrawer;
    private Toolbar mLongToolbar;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongToolbar = (Toolbar) findViewById(R.id.toolbar_long);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        changeSupportActionBar(mLongToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {
            displaySelectedScreen(R.id.today_tasks, null);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        mLongToolbar.setTitle(title);
    }

    private void changeSupportActionBar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            }
        };
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displaySelectedScreen(int id, Map<String, Object> additionalData){
        Fragment fragment = null;

        switch (id){
            case R.id.today_tasks:
                fragment = new TodayTasksFragment();
                break;
            case R.id.calendar_grid:
                fragment = new CalendarGridFragment();
                break;
            case R.id.add_area:
                fragment = new AddAreaFragment();
                break;
            case R.id.add_task:
                fragment = new AddTaskFragment();
                if(additionalData != null){
                    if(additionalData.containsKey("taskDay") && additionalData.containsKey("taskAreaName")) {
                        Calendar date = (Calendar) additionalData.get("taskDay");
                        String areaName = (String) additionalData.get("taskAreaName");

                        ((AddTaskFragment)fragment).setPredefinedParameters(areaName, date);
                    }
                }
                break;
        }

        mNavigationView.getMenu().findItem(id).setChecked(true);

        if(fragment != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment);
            ft.commit();
        }
    }

    public void setShortAppBar(Toolbar shortToolbar){
        mLongToolbar.setVisibility(View.GONE);
        shortToolbar.setVisibility(View.VISIBLE);

        changeSupportActionBar(shortToolbar);

    }

    public void setLongAppBar(Toolbar shortToolbar){
        shortToolbar.setVisibility(View.GONE);
        mLongToolbar.setVisibility(View.VISIBLE);

        changeSupportActionBar(mLongToolbar);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displaySelectedScreen(item.getItemId(), null);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
