package com.rmasc.fireroad.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.rmasc.fireroad.Entities.WebServiceParameter;

import org.json.JSONArray;
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

        return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerVehiculos", parameters);
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonResponse = new JSONObject(s);
            JSONArray data = jsonResponse.getJSONArray("d");
            SharedPreferences userP = appContext.getSharedPreferences("Moto", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userP.edit();

            if (data != null)
            {
                JSONObject motoMain = data.optJSONObject(0);
                editor.putInt("Id", motoMain.optInt("Id"));
                editor.putString("Marca", motoMain.optString("Marca"));
                editor.putString("Placa", motoMain.optString("Placa"));
                editor.putString("FotoPath", motoMain.optString("FotoPath"));
                editor.putString("Color", motoMain.optString("Color"));
                editor.putString("Modelo", motoMain.optString("Modelo"));
                editor.putString("MacBluetooth", motoMain.optString("MacBluetooth"));
                editor.putString("NombreBluetooth", motoMain.optString("NombreBluetooth"));
                editor.apply();
            }

        }
        catch (Exception e)
        {
        }
    }
}
