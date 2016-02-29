package com.rmasc.fireroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.FacebookSdk;

public class LoginActivity extends AppCompatActivity {

    private Button btnGo, btnRegistrar;
    private ImageButton imgBtnFace, imgBtnTwitt;

    private EditText editTextContrasena;

    private View.OnClickListener buttonClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLogin;
                switch (v.getId())
                {
                    case R.id.btnGo:
                        if (IniciarLogin()) {
                            goToLogin = new Intent(getBaseContext(), MainActivity.class);
                            startActivity(goToLogin);
                            finish();
                        }
                        else
                        ShowMessage("Correo y/o contraseña invalidas.");
                        break;
                    case R.id.btnRegistrar:
                        goToLogin = new Intent(getBaseContext(), RegisterActivity.class);
                        startActivity(goToLogin);
                        break;

                    default:
                        break;
                }
            }
        };

        AssignControls();
    }

    private void AssignControls()
    {
        btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(buttonClickListener);

        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(buttonClickListener);

        editTextContrasena = (EditText) findViewById(R.id.editTextContrasena);
    }


    private void ShowMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean IniciarLogin()
    {
        //ServicioWeb ir a login
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Id", 1);// Poner Id retornado del servicio web
        editor.commit();
        return true;
    }
}
