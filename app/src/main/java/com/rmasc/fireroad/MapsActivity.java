package com.rmasc.fireroad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rmasc.fireroad.DataBase.TransmisionesHelper;
import com.rmasc.fireroad.Entities.DeviceBluetooth;
import com.rmasc.fireroad.Entities.DeviceData;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Polyline polyOptionsUpdate;
    private Marker markerOptionsUpdate;
    private TransmisionesHelper transmisionesHelper;
    private BroadcastReceiver broadcastReceiver;
    private Button btnSatelite = null;
    private Button btnHibrido = null;
    private Button btnStreetMap = null;
    DeviceBluetooth DispositivoAsociado;
    private static boolean isRecorrido = false;

    private Button btnIniciarRecorrido;

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        btnSatelite = (Button)findViewById(R.id.btnMapaSatelital);
        btnHibrido = (Button)findViewById(R.id.btnMapaHibrido);
        btnStreetMap = (Button)findViewById(R.id.btnMapaStreetView);
        btnIniciarRecorrido=(Button)findViewById(R.id.btnIniciarRecorrido);

        if(LoadDevice()) {

            btnIniciarRecorrido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (!isRecorrido) {
                        new CrearRecorrido().execute();
                    } else {
                        new EnviarRecorrido().execute(transmisionesHelper.SelectTransmision(transmisionesHelper.getReadableDatabase(), "VehiculoId = " + DispositivoAsociado.DataReceived.VehiculoId + " AND ReporteId = " + DispositivoAsociado.DataReceived.ReporteId, null));
                    }

                }
            });
        }
        else {
            btnIniciarRecorrido.setText("Sin Dispositivo");
        }


        btnSatelite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btnHibrido.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnHibrido.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setTextColor(getResources().getColor(R.color.windowBackground));
                    btnSatelite.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnSatelite.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                 } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    btnHibrido.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnHibrido.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnStreetMap.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnSatelite.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnSatelite.setTextColor(getResources().getColor(R.color.windowBackground));


                }
            }
        });



        btnHibrido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if( mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btnHibrido.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnHibrido.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setTextColor(getResources().getColor(R.color.windowBackground));
                    btnSatelite.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnSatelite.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                }
                else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    btnHibrido.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnHibrido.setTextColor(getResources().getColor(R.color.windowBackground));
                    btnStreetMap.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnStreetMap.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnSatelite.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnSatelite.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                }
                }
        });


        btnStreetMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                btnHibrido.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                btnHibrido.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                btnStreetMap.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                btnStreetMap.setTextColor(getResources().getColor(R.color.windowBackground));
                btnSatelite.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                btnSatelite.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
            }
        });

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getIntExtra("Tipo", 1)) {
                    case 1:
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
                if (intent.getBooleanExtra("IsRecorrido", true)) {
                    polyOptionsUpdate = mMap.addPolyline(new PolylineOptions().color(Color.RED).geodesic(true).width(5));
                }
                markerOptionsUpdate = mMap.addMarker(new MarkerOptions().visible(false).position(new LatLng(0, 0)));
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


    private boolean LoadDevice() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
        String Nombre = sharedPref.getString("NombreBluetooth", "");
        String Mac = sharedPref.getString("MacBluetooth", "");
        if (sharedPref.getInt("Id", 0) == 0) {
            return false;
        } else {
            DispositivoAsociado = new DeviceBluetooth();
            DispositivoAsociado.Name = Nombre;
            DispositivoAsociado.Mac = Mac;
            DispositivoAsociado.DataReceived.VehiculoId = sharedPref.getInt("Id", 0);
            DispositivoAsociado.DataReceived.ReporteId = sharedPref.getInt("IdRecorrido", 0);
            return true;
        }
    }

    private void CargarUltimaPosicionBle(double Latitud, double Longitud, String Fecha) {
        SharedPreferences userP = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
        LatLng ptoActual = new LatLng(Latitud, Longitud);
        if (polyOptionsUpdate != null) {
            List<LatLng> ptosLinea = polyOptionsUpdate.getPoints();
            ptosLinea.add(ptoActual);
            polyOptionsUpdate.setPoints(ptosLinea);
        }
        markerOptionsUpdate.setPosition(ptoActual);
        markerOptionsUpdate.setVisible(true);
        markerOptionsUpdate.setTitle(userP.getString("Placa", ""));
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

    private class CrearRecorrido extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();
            SharedPreferences user = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);

            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(user.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = String.valueOf(moto.getInt("Id", 0));
            parameters.add(parametro);

            return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/IniciarRecorrido", parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                int IdRecorrido = jsonResponse.optInt("d");
                if (IdRecorrido != 0) {
                    SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                    SharedPreferences.Editor editor = moto.edit();

                    editor.putInt("IdRecorrido", IdRecorrido);
                    editor.apply();
                    DispositivoAsociado.DataReceived.ReporteId = IdRecorrido;
                    btnIniciarRecorrido.setText("Stop");
                    btnIniciarRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
                    isRecorrido = true;
                } else
                    isRecorrido = false;
            } catch (Exception e) {
            }
        }
    }

    private class EnviarRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(user.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = String.valueOf(moto.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdRecorrido";
            parametro.Valor = String.valueOf(moto.getInt("IdRecorrido", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Tramas";
            parametro.Valor = params[0];
            parameters.add(parametro);

            return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/GuardarPuntosRecorrido", parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                String result = jsonResponse.optString("d");
                ShowMessage(result);
                if (result.equals("true")) {
                    isRecorrido = false;
                    btnIniciarRecorrido.setText("IniciarRecorrido");
                    btnIniciarRecorrido.setTextColor(getResources().getColor(R.color.colorOk));
                }

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
