package com.rmasc.fireroad.Adapters;

import android.support.v4.view.ViewPager;
import android.view.View;
import com.rmasc.fireroad.R;

/**
 * Created by ADMIN on 13/01/2016.
 */
public class IntroPageTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        int pagePosition = (int) page.getTag();
        int pageWidth = page.getWidth();
        float pageWidthTimesPosition = pageWidth * position;
        float absPosition = Math.abs(position);

        if (position <= -1.0f || position >= 1.0f) {

        } else if (position == 0.0f) {

        } else {

            View title = page.findViewById(R.id.txtTitulo);
            title.setAlpha(1.0f - absPosition);

            View description = page.findViewById(R.id.txtDescripcion);
            description.setTranslationY(-pageWidthTimesPosition / 2f);
            description.setAlpha(1.0f - absPosition);

            View computer = page.findViewById(R.id.Icono);

            if (pagePosition == 0 && computer != null) {
                computer.setAlpha(1.0f - absPosition);
                computer.setTranslationX(-pageWidthTimesPosition * 1.5f);
            }

            if (position < 0) {
                // Create your out animation here
            } else {
                // Create your in animation here
            }
        }
    }
}
