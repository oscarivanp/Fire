package com.rmasc.fireroad.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.rmasc.fireroad.Entities.WebServiceParameter;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ADMIN on 10/03/2016.
 */
public class ObtenerVehiculo extends AsyncTask <Context, Void, String> {
    Context appContext;

    public ObtenerVehiculo() {
        super();
    }

    @Override
    protected String doInBackground(Context... params) {
        ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
        WebServiceParameter parametro = new WebServiceParameter();

        SharedPreferences userPref = params[0].getSharedPreferences("User", Context.MODE_PRIVATE);
        parametro.Nombre = "IdUser";
        parametro.Valor = String.valueOf(userPref.getInt("Id", 0));
        parameters.add(parametro);
        appContext = params[0];

        return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerVehiculo", parameters);
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonResponse = new JSONObject(s);
            JSONObject data = jsonResponse.getJSONObject("d");
            SharedPreferences userP = appContext.getSharedPreferences("Moto", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userP.edit();

            if (data.optInt("Id") != 0)
            {
                editor.putInt("Id", data.optInt("Id"));
                editor.putString("Marca", data.optString("Marca"));
                editor.putString("Placa", data.optString("Placa"));
                editor.putString("Color", data.optString("Color"));
                editor.putString("Modelo", data.optString("Modelo"));
                editor.putString("MacBluetooth", data.optString("MacBluetooth"));
                editor.putString("NombreBluetooth", data.optString("NombreBluetooth"));
                editor.commit();
            }

        }
        catch (Exception e)
        {
        }
    }
}
