package com.rmasc.fireroad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;

import com.rmasc.fireroad.Adapters.ListViewAdapter;

public class SettingsActivity extends Activity {

    private ListView listViewPuntosInteres;
    private Switch switchCopiloto;
    private LinearLayout layoutCopiloto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AssignViews();
        LoadControls();
    }

    private void AssignViews()
    {
        listViewPuntosInteres = (ListView) findViewById(R.id.listViewPuntosInteres);
        switchCopiloto = (Switch) findViewById(R.id.switchCopiloto);
        layoutCopiloto = (LinearLayout) findViewById(R.id.layoutCopiloto);
    }

    private void LoadControls()
    {
        final SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean coop = sharedPreferences.getBoolean("Copiloto", false);
        switchCopiloto.setChecked(coop);
        switchCopiloto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    layoutCopiloto.setVisibility(View.VISIBLE);
                else
                    layoutCopiloto.setVisibility(View.GONE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Copiloto", isChecked);
                editor.apply();
            }
        });
        if (coop)
            layoutCopiloto.setVisibility(View.VISIBLE);
        else
            layoutCopiloto.setVisibility(View.GONE);

        listViewPuntosInteres.setAdapter(new ListViewAdapter(getBaseContext(), getResources().getStringArray(R.array.puntosInteres)));
    }
}
