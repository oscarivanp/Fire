package com.rmasc.fireroad.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.R;
import com.rmasc.fireroad.RegisterActivity;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONObject;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    private CallbackManager callbackManager = null;
    private AccessTokenTracker mtracker = null;
    private ProfileTracker mprofileTracker = null;
    ProgressDialog progressDialog;
    private Profile profile;

    public static final String PARCEL_KEY = "parcel_key";

    private LoginButton loginButton;
    private ImageView imageView;
    private Button btnRegistrar;

    FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            progressDialog.show(getContext(), "", "Cargando", true);
            new ValidarUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/LoginFacebook", loginResult.getAccessToken().getUserId());
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        callbackManager = CallbackManager.Factory.create();


        mtracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

                Log.v("AccessTokenTracker", "oldAccessToken=" + oldAccessToken + "||" + "CurrentAccessToken" + currentAccessToken);
            }
        };


        mprofileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

                Log.v("Session Tracker", "oldProfile=" + oldProfile + "||" + "currentProfile" + currentProfile);
                         }
        };

        mtracker.startTracking();
        mprofileTracker.startTracking();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends","email");

        // If using in a fragment
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStop() {
        super.onStop();
        mtracker.stopTracking();
        mprofileTracker.stopTracking();
    }


    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private class ValidarUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "IdFacebook";
            parametro.Valor = urls[1].toString();
            parameters.add(parametro);

            return WebService.ConexionWS(urls[0], parameters);
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                int IdUser = jsonResponse.optInt("d");

                SharedPreferences sharedPref = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                Intent goToRegister = new Intent(getContext(), RegisterActivity.class);

                if (IdUser != 0) {
                    editor.putInt("Id", IdUser);
                    editor.commit();
                    goToRegister.putExtra("TipoLogin", "facebook");
                    startActivity(goToRegister);
                } else {
                    editor.putInt("Id", 0);
                    editor.commit();
                    goToRegister.putExtra("TipoLogin", "facebook");
                    startActivity(goToRegister);
                }
                if (progressDialog != null)
                    progressDialog.dismiss();

            } catch (Exception e) {
            }
        }
    }
}
