package com.rmasc.fireroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private Button btnGo, btnRegistrar;

    public EditText editTextContrasena, editTextCorreo;

    private View.OnClickListener buttonClickListener;

    Intent goToMain;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        goToMain = new Intent(getBaseContext(), MainActivity.class);
        sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);

        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLogin;
                switch (v.getId())
                {
                    case R.id.btnGo:
                        IniciarLogin();
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
        editTextCorreo= (EditText) findViewById(R.id.editTextCorreo);
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

    private void IniciarLogin()
    {
        new LoginWebService().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/Login", editTextCorreo.getText().toString(), editTextContrasena.getText().toString());
    }

    private class LoginWebService extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "Correo";
            parametro.Valor = urls[1].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter(); // Si no se reinicia genera error.
            parametro.Nombre = "Password";
            parametro.Valor = urls[2].toString();
            parameters.add(parametro);

            return WebService.ConexionWS(urls[0], parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonResponse = new JSONObject(s);
                int IdUser = jsonResponse.optInt("d");
                if (IdUser == 0)
                {
                    ShowMessage("Correo y/o contraseña incorrectos.");
                }
                else
                {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("Id", IdUser);
                    editor.commit();

                    startActivity(goToMain);
                    finish();
                }
            }
            catch (Exception e)
            {
                ShowMessage(e.getMessage());
            }
        }
    }
}
