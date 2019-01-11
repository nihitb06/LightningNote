package com.dev.nihitb06.newlightningnote.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class FloatingActionButtonHideOnScrollBehaviour extends FloatingActionButton.Behavior {

    public FloatingActionButtonHideOnScrollBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        if (dyConsumed > 0) {
            animateFloatingActionButton(
                    child,
                    child.getHeight() + ((CoordinatorLayout.LayoutParams) child.getLayoutParams()).bottomMargin
            );
        } else if (dyConsumed < 0) {
            animateFloatingActionButton(child, 0);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    private void animateFloatingActionButton(FloatingActionButton floatingActionButton, float translationY) {
        floatingActionButton.animate().translationY(translationY).setInterpolator(new LinearInterpolator()).start();
    }
}
