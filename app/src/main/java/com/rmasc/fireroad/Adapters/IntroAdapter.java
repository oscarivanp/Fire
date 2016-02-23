package com.rmasc.fireroad.Adapters;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by ADMIN on 13/01/2016.
 */
public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroFragment.newInstance(Color.parseColor("#FFFFFF"), position);
            case 1:
                return IntroFragment.newInstance(Color.parseColor("#DDDDDD"), position);
            case 2:
                return IntroFragment.newInstance(Color.parseColor("#FFFFFF"), position);
            default:
                return IntroFragment.newInstance(Color.parseColor("#DDDDDD"), position);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
