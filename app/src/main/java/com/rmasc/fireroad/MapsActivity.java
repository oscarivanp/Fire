package com.rmasc.fireroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();

        switch (intent.getIntExtra("Tipo", 1)) {
            case 1:
                CargarUltimaPosicion(intent.getDoubleExtra("Lat", 0), intent.getDoubleExtra("Lon", 0), intent.getStringExtra("Fecha"));
                break;
            case 2:
                CargarRecorrido(intent.getIntExtra("IdRecorrido", 0));
                break;
            default:
                break;
        }

    }

    private void CargarUltimaPosicion(double Latitud, double Longitud, String Fecha) {
        SharedPreferences userP = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
        LatLng ptoActual = new LatLng(Latitud, Longitud);
        mMap.addMarker(new MarkerOptions().position(ptoActual).title( userP.getString("Marca","") + " " + userP.getString("Placa","") + "\n" + Fecha));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ptoActual));
    }

    private void CargarRecorrido(int IdRecorrido) {
        SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences motoPref = getSharedPreferences("Moto", MODE_PRIVATE);

        new ObtenerPuntosRecorrido().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ListTransmision", String.valueOf(userPref.getInt("Id", 0)), String.valueOf((motoPref.getInt("Moto", 0))), String.valueOf((IdRecorrido)));
    }

    private class ObtenerPuntosRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(params[1]);
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = String.valueOf(params[2]);
            parameters.add(parametro);

            parametro.Nombre = "IdRecorrido";
            parametro.Valor = String.valueOf(params[3]);
            parameters.add(parametro);

            return WebService.ConexionWS(params[0], parameters);
        }
    }
}
