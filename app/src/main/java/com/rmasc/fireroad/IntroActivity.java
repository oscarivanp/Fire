package com.rmasc.fireroad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rmasc.fireroad.Adapters.IntroAdapter;
import com.rmasc.fireroad.Adapters.IntroPageTransformer;

public class IntroActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
//cambios ya2erw otro

//pitchi
        AjustarInterface();
    }

    private void AjustarInterface()
    {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new IntroAdapter(getSupportFragmentManager()));
        mViewPager.setPageTransformer(false, new IntroPageTransformer());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    btnFinish = (Button) findViewById(R.id.btnFinish);
                    btnFinish.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         Intent goToLogin = new Intent(getBaseContext(), LoginActivity.class);
                                                         startActivity(goToLogin);
                                                         finish();
                                                     }
                                                 }
                    );
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
