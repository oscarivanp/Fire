package com.rmasc.fireroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "ZQqwjflLo84ULNenXXiHAGR9s";
    private static final String TWITTER_SECRET = "ZttElB9UKZfgl3My0xgkjgol5OLtVtRDuQrCpQ7052eipvxhYR";
    private Button btnGo, btnRegistrar;
    private ImageButton imgBtnFace, imgBtnTwitt;
    private EditText editTextContrasena;
    private static TwitterLoginButton twitterloginButton;
    private View.OnClickListener buttonClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);
        twitterloginButton = (TwitterLoginButton) findViewById(R.id.btnTwitter);
        twitterloginButton.setCallback(new Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:

                TwitterSession session = result.data;

                // TODO: Remove toast and use the TwitterSession's userID
                // with your app's user model
                Intent goToLogin;
                Bundle bundle = new Bundle();
                bundle.putString("UserName" ,session.getUserName() );
                bundle.putString("TipoLogin", "twitter" );
                goToLogin = new Intent(getBaseContext(), RegisterActivity.class);
                goToLogin.putExtras(bundle);
                startActivity(goToLogin);

            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }
        });

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        twitterloginButton.onActivityResult(requestCode, resultCode, data);
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
