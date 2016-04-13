package com.rmasc.fireroad.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.rmasc.fireroad.Entities.WebServiceParameter;

import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ADMIN on 04/03/2016.
 */
public class ObtenerUsuario extends AsyncTask <Context, Void, String> {

    Context appContext;
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    private Date parseDateTime(String lastModified) {
        Date date = null;
        if (lastModified != null && lastModified.length() > 0) {
            try {
                lastModified = lastModified.replace("/Date(","");
                lastModified = lastModified.replace(")/", "");
                date = new Date(Long.parseLong(lastModified));
            } catch (Exception e) {
                // otherwise we just leave it empty
            }
        }
        return date;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonResponse = new JSONObject(s);
            JSONObject data = jsonResponse.getJSONObject("d");
            SharedPreferences userP = appContext.getSharedPreferences("User", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = userP.edit();

            if (data.optInt("Id") != 0) {
                editor.putInt("Id", data.optInt("Id"));
                editor.putString("Nombres", data.optString("Nombres"));
                //editor.putString("Apellidos", data.optString("Apellidos"));
                editor.putString("Telefono", data.optString("Telefono"));
                editor.putString("Sexo", data.optString("Sexo"));
                editor.putString("Correo", data.optString("Correo"));
                Date fechaN = parseDateTime(data.optString("FechaNacimiento"));
                editor.putString("FechaNacimiento", formatoFecha.format(fechaN.getTime()));
                editor.putString("RH", data.optString("RH"));
                editor.putString("IdTwitter", data.optString("IdTwitter"));
                editor.putString("IdFacebook", data.optString("IdFacebook"));
                editor.putString("UserLogin", data.optString("Login"));
                editor.putString("FotoPath", data.optString("FotoPath"));
                editor.apply();

                new ObtenerVehiculo().execute(appContext);
            }

        }
        catch (Exception e)
        {
        }
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

        return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerUsuario", parameters);
    }
}
