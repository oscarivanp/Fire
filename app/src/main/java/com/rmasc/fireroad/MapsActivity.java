package com.rmasc.fireroad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rmasc.fireroad.DataBase.TransmisionesHelper;
import com.rmasc.fireroad.Entities.DeviceData;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PolylineOptions polyOptionsUpdate = new PolylineOptions().color(Color.RED).geodesic(true).width(5);
    private TransmisionesHelper transmisionesHelper;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getIntExtra("Tipo", 1)) {
                    case 1:
                        ShowMessage("Nuevo reporte bluetooth");
                        CargarUltimaPosicionBle(intent.getFloatExtra("Lat", 0), intent.getFloatExtra("Lon", 0), intent.getStringExtra("Fecha"));
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent = getIntent();

        switch (intent.getIntExtra("Tipo", 1)) {
            case 1:
                mMap.addPolyline(polyOptionsUpdate);
                registerReceiver(broadcastReceiver, new IntentFilter("UPDATE_MAP"));
                CargarUltimaPosicionBle(intent.getFloatExtra("Lat", 0), intent.getFloatExtra("Lon", 0), intent.getStringExtra("Fecha"));
                break;
            case 2:
                CargarRecorrido(intent.getIntExtra("IdRecorrido", 0), intent.getIntExtra("IdVehiculo", 0));
                break;
            case 3:
                SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
                SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
                new ObtenerUltimaTransmision().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerPosicionReciente", String.valueOf(user.getInt("Id", 0)), String.valueOf((moto.getInt("Id", 0))));
                break;
            default:
                break;
        }

    }

    private void CargarUltimaPosicionBle(double Latitud, double Longitud, String Fecha) {
        SharedPreferences userP = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
        LatLng ptoActual = new LatLng(Latitud, Longitud);
        polyOptionsUpdate.add(ptoActual);
        mMap.addMarker(new MarkerOptions().position(ptoActual).title(userP.getString("Marca", "") + " " + userP.getString("Placa", "") + "\n" + Fecha));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ptoActual));
    }

    private void CargarRecorrido(int IdRecorrido, int IdVehiculo) {
        SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);
        if (transmisionesHelper == null) {
            transmisionesHelper = new TransmisionesHelper(this);
            ArrayList<DeviceData> deviceDataArrayList = transmisionesHelper.ArrayTransmision(transmisionesHelper.getReadableDatabase(), "VehiculoId = " + IdVehiculo + " AND ReporteId = " + IdRecorrido, null);
            if (deviceDataArrayList.size() > 0) {
                PintarRecorrido(deviceDataArrayList);
                return;
            }
        }
        new ObtenerPuntosRecorrido().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ListTransmision", String.valueOf(userPref.getInt("Id", 0)), String.valueOf(IdVehiculo), String.valueOf((IdRecorrido)));
    }


    private void PintarRecorrido(ArrayList<DeviceData> Puntos) {
        ArrayList<LatLng> puntosLinea = new ArrayList<>();
        for (int i = 0; i < Puntos.size(); i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud))
                    .title("Velocidad: " + Puntos.get(i).Velocidad + "Km/h \r\n Fecha:" + Puntos.get(i).Fecha));
            puntosLinea.add(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud));
        }
        mMap.addPolyline(new PolylineOptions().addAll(puntosLinea).color(Color.RED).width(5).geodesic(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosLinea.get(0), 10));
    }

    private class ObtenerUltimaTransmision extends AsyncTask<String, Void, String> {
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

            return WebService.ConexionWS(params[0], parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject dataRe = new JSONObject(s);
                JSONObject puntosMapa = dataRe.optJSONObject("d");
                ArrayList<DeviceData> Puntos = new ArrayList<DeviceData>();
                DeviceData punto = new DeviceData();

                punto.Id = puntosMapa.optInt("Id");
                punto.Latitud = Float.valueOf(puntosMapa.optString("Latitud"));
                punto.Longitud = Float.valueOf(puntosMapa.optString("Longitud"));
                punto.Fecha = puntosMapa.optString("FechaTransmision");
                punto.Bateria = puntosMapa.optInt("Bateria");
                punto.Velocidad = puntosMapa.optInt("Velocidad");
                Puntos.add(punto);

                PintarRecorrido(Puntos);

            } catch (Exception e) {

            }
        }
    }

    private class ObtenerPuntosRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject dataRe = new JSONObject(s);
                JSONArray puntosMapa = dataRe.optJSONArray("d");
                ArrayList<DeviceData> Puntos = new ArrayList<DeviceData>();

                for (int i = 0; i < puntosMapa.length(); i++) {
                    JSONObject puntoTemp = puntosMapa.optJSONObject(i);
                    DeviceData punto = new DeviceData();

                    punto.Id = puntoTemp.optInt("Id");
                    punto.Latitud = Float.valueOf(puntoTemp.optString("Latitud"));
                    punto.Longitud = Float.valueOf(puntoTemp.optString("Longitud"));
                    punto.Fecha = puntoTemp.optString("FechaTransmision");
                    punto.Bateria = puntoTemp.optInt("Bateria");
                    punto.Velocidad = puntoTemp.optInt("Velocidad");
                    Puntos.add(punto);
                }
                PintarRecorrido(Puntos);

            } catch (Exception e) {

            }
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

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdRecorrido";
            parametro.Valor = String.valueOf(params[3]);
            parameters.add(parametro);

            return WebService.ConexionWS(params[0], parameters);
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
}
