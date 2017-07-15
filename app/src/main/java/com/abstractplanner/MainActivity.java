package com.abstractplanner;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.abstractplanner.dto.Attribute;
import com.abstractplanner.fragments.AddAttributeFragment;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.fragments.TodayTasksFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public List<Attribute> attributes = new ArrayList<>();

    private DrawerLayout mDrawer;
    private Toolbar mLongToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongToolbar = (Toolbar) findViewById(R.id.toolbar_long);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        changeSupportActionBar(mLongToolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(savedInstanceState == null) {
            displaySelectedScreen(R.id.today_tasks);
        }
    }

    private void changeSupportActionBar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

    public void displaySelectedScreen(int id){
        Fragment fragment = null;

        switch (id){
            case R.id.today_tasks:
                fragment = new TodayTasksFragment();
                break;
            case R.id.calendar_grid:
                fragment = new CalendarGridFragment();
                break;
            case R.id.add_attribute:
                fragment = new AddAttributeFragment();
                break;
        }

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
        displaySelectedScreen(item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
