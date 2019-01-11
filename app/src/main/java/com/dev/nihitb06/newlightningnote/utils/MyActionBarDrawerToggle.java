package com.dev.nihitb06.newlightningnote.utils;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

    public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        //super.onDrawerOpened(drawerView);
        onDrawerSlide(drawerView, 0f);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, 0f);
    }

    public void onDrawerAnimate(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);
    }
}
