package com.dev.nihitb06.newlightningnote.apptour;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;

import com.dev.nihitb06.newlightningnote.R;

class AppTourPagerTransformer implements ViewPager.PageTransformer {

    private View backgroundView;
    private int[] colors;
    private ViewCache[] cache;

    public AppTourPagerTransformer(Context context, View backgroundView) {
        this.backgroundView = backgroundView;

        colors = new int[3];
        colors[0] = ContextCompat.getColor(context, R.color.color_app_tour_one);
        colors[1] = ContextCompat.getColor(context, R.color.color_app_tour_two);
        colors[2] = ContextCompat.getColor(context, R.color.color_app_tour_three);

        cache = new ViewCache[3];
        cache[0] = cache[1] = cache[2] = null;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        transformPage(page, (int) page.getTag(), position);
    }

    private void transformPage(View page, int index, float position) {
        if(cache[index] == null) {
            cache[index] = new ViewCache(page);
        }

        float absolutePosition = Math.abs(position);
        float oneMinusAbsolutePosition = 1f - absolutePosition;
        float pageWidthTimesPosition = page.getWidth() * position;

        if(position < -1f) {
            page.setAlpha(0);
        } else if(position < 0) {
            backgroundView.setBackgroundColor(blendColors(colors[index], colors[index+1], oneMinusAbsolutePosition));

            View tvPageTitle = cache[index].findViewById(R.id.tvPageTitle);
            tvPageTitle.setTranslationY(-pageWidthTimesPosition/2f);
            tvPageTitle.setAlpha(oneMinusAbsolutePosition);

            View tvPageSubtitle = cache[index].findViewById(R.id.tvPageSubtitle);
            tvPageSubtitle.setTranslationY(-pageWidthTimesPosition/2f);
            tvPageSubtitle.setAlpha(oneMinusAbsolutePosition);

            cache[index].findViewById(R.id.pageIllustrations).setAlpha(oneMinusAbsolutePosition);
        } else if(position == 0) {
            backgroundView.setBackgroundColor(colors[index]);
        } else if(position <= 1) {
            backgroundView.setBackgroundColor(blendColors(colors[index-1], colors[index], absolutePosition));

            if(index == 1) {
                cache[index].findViewById(R.id.ivHelperTwo).setTranslationX(pageWidthTimesPosition);
            } else if(index == 2) {
                View background = cache[index].findViewById(R.id.ivBackground);
                background.setScaleX(oneMinusAbsolutePosition);
                background.setScaleY(oneMinusAbsolutePosition);
            }
        } else {
            page.setAlpha(0);
        }
    }

    private int blendColors(int previousColor, int nextColor, float ratio) {
        float inverseRatio = 1f - ratio;

        float red = Color.red(previousColor)*ratio  + Color.red(nextColor)*inverseRatio;
        float green = Color.green(previousColor)*ratio  + Color.green(nextColor)*inverseRatio;
        float blue = Color.blue(previousColor)*ratio  + Color.blue(nextColor)*inverseRatio;

        return Color.rgb((int) red,  (int) green, (int) blue);
    }

    private static class ViewCache {
        private SparseArray<View> cache;
        private View page;

        ViewCache(View page) {
            this.page = page;
            cache = new SparseArray<>();
        }

        View findViewById(int viewId) {
            if(cache.get(viewId, null) == null) {
                cache.append(viewId, page.findViewById(viewId));
            }

            return cache.get(viewId, null);
        }
    }
}
