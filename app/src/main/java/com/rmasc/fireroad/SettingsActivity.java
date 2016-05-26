package com.rmasc.fireroad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.ListViewAdapter;

public class SettingsActivity extends Activity {

    private ListView listViewPuntosInteres;
    private Switch switchCopiloto;
    private SeekBar seekBarVelocidad;
    private LinearLayout layoutCopiloto;
    private TextView textViewVelocidad;

    private int MAX_VELOCITY = 230;
    private static int MY_DATA_CHECK_CODE = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        AssignViews();
        LoadControls();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL) {
                ShowMessage("Su dispositivo no es compatible");
                switchCopiloto.setChecked(false);
            }
        }
    }

    private void ShowMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AssignViews() {
        listViewPuntosInteres = (ListView) findViewById(R.id.listViewPuntosInteres);
        switchCopiloto = (Switch) findViewById(R.id.switchCopiloto);
        layoutCopiloto = (LinearLayout) findViewById(R.id.layoutCopiloto);
        seekBarVelocidad = (SeekBar) findViewById(R.id.seekBarVelocidad);
        textViewVelocidad = (TextView) findViewById(R.id.textViewVelocidad);
    }

    private void LoadControls() {
        final SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean coop = sharedPreferences.getBoolean("Copiloto", false);
        seekBarVelocidad.setProgress(sharedPreferences.getInt("Velocidad", 0));
        textViewVelocidad.setText("Control de velocidad: " + (sharedPreferences.getInt("Velocidad", 0) * MAX_VELOCITY / 100) + " Km/h");
        seekBarVelocidad.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("Velocidad", progress);
                    editor.apply();
                    textViewVelocidad.setText("Control de velocidad: " + (progress * MAX_VELOCITY / 100) + " Km/h");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        switchCopiloto.setChecked(coop);
        switchCopiloto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutCopiloto.setVisibility(View.VISIBLE);
                    CheckForTTS();
                }
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

    private void CheckForTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

}
