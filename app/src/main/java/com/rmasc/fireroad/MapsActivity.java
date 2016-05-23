package com.rmasc.fireroad;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private ProgressDialog progressDialog;

    private Button btnIniciarRecorrido;

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSatelite = (Button) findViewById(R.id.btnMapaSatelital);
        btnHibrido = (Button) findViewById(R.id.btnMapaHibrido);
        btnStreetMap = (Button) findViewById(R.id.btnMapaStreetView);
        btnIniciarRecorrido = (Button) findViewById(R.id.btnIniciarRecorrido);

        if (LoadDevice()) {

            btnIniciarRecorrido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (!isRecorrido) {
                        new CrearRecorrido().execute();
                    } else {
                        if (transmisionesHelper == null) {
                            transmisionesHelper = new TransmisionesHelper(getBaseContext());
                        }
                        SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                        new EnviarRecorrido().execute(transmisionesHelper.SelectTransmision(transmisionesHelper.getReadableDatabase(), "VehiculoId = " + moto.getInt("Id", 0) + " AND ReporteId = " + moto.getInt("IdRecorrido", 0), null));
                    }

                }
            });
        } else {
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
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btnHibrido.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnHibrido.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                    btnStreetMap.setTextColor(getResources().getColor(R.color.windowBackground));
                    btnSatelite.setBackgroundColor(getResources().getColor(R.color.tw__transparent));
                    btnSatelite.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
                } else {
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
            case 1: //Mapa actualizando la ultima posición bluetooth
                if (intent.getBooleanExtra("IsRecorrido", true)) {
                    polyOptionsUpdate = mMap.addPolyline(new PolylineOptions().color(Color.RED).geodesic(true).width(5));
                    btnIniciarRecorrido.setText("Stop");
                    btnIniciarRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
                    isRecorrido = true;
                }
                btnIniciarRecorrido.setVisibility(View.VISIBLE);
                SharedPreferences userP = getSharedPreferences("Moto", MODE_PRIVATE);
                markerOptionsUpdate = mMap.addMarker(new MarkerOptions().visible(false).position(new LatLng(0, 0)).title(userP.getString("Placa", "")));
                registerReceiver(broadcastReceiver, new IntentFilter("UPDATE_MAP"));
                CargarUltimaPosicionBle(intent.getFloatExtra("Lat", 0), intent.getFloatExtra("Lon", 0), intent.getStringExtra("Fecha"));
                break;
            case 2: //Mapa cargando de la base de datos un recorrido previo
                CargarRecorrido(intent.getIntExtra("IdRecorrido", 0), intent.getIntExtra("IdVehiculo", 0));
                break;
            case 3: //Mapa cargando la ultima posicion registrada en la base de datos por el dispositivo
                SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
                SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
                progressDialog = ProgressDialog.show(this, "Cargando..", "", true);
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
        //markerOptionsUpdate.setTitle(userP.getString("Placa", ""));
        markerOptionsUpdate.setSnippet(Fecha);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ptoActual, 10));
    }

    private void CargarRecorrido(int IdRecorrido, int IdVehiculo) {
        SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);
        if (transmisionesHelper == null) {
            transmisionesHelper = new TransmisionesHelper(this);
            ArrayList<DeviceData> deviceDataArrayList = transmisionesHelper.ArrayTransmision(transmisionesHelper.getReadableDatabase(), "VehiculoId = " + IdVehiculo + " AND ReporteId = " + IdRecorrido, null);
            if (deviceDataArrayList.size() > 0) {
                //PintarRecorrido(deviceDataArrayList);
                PintarRecorridoColores(deviceDataArrayList, getIntent().getIntExtra("VelMax", 0));
                return;
            }
        }
        new ObtenerPuntosRecorrido().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ListTransmision", String.valueOf(userPref.getInt("Id", 0)), String.valueOf(IdVehiculo), String.valueOf((IdRecorrido)), String.valueOf(IdVehiculo));
    }

    private void PintarRecorrido(ArrayList<DeviceData> Puntos) {
        ArrayList<LatLng> puntosLinea = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        SharedPreferences userP = getSharedPreferences("Moto", MODE_PRIVATE);
        for (int i = 0; i < Puntos.size(); i++) {
            mMap.addMarker(new MarkerOptions().snippet("").position(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud))
                    .snippet("Velocidad: " + Puntos.get(i).Velocidad + "Km/h   Fecha: " + Puntos.get(i).Fecha)
                    .title(userP.getString("Placa", "")).infoWindowAnchor(0, 1).anchor(0, 1));
            puntosLinea.add(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud));
            builder.include(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud));
        }
        mMap.addPolyline(new PolylineOptions().addAll(puntosLinea).color(Color.RED).width(5).geodesic(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosLinea.get(0), 10));
        progressDialog.dismiss();
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 15, 15, 1));
    }

    private void PintarRecorridoColores(ArrayList<DeviceData> Puntos, int VelocidadMaxima) {
        int rango = VelocidadMaxima / 6;
        int ColorRuta = 0;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 1; i < Puntos.size(); i++) {

            if (i == 1) {

                mMap.addMarker(new MarkerOptions().snippet("").position(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud))
                        .snippet("Fecha: " + Puntos.get(i).Fecha)
                        .title("Inicio Recorrido").infoWindowAnchor(0, 1).anchor(0, 1));
            }
            if (i == Puntos.size() - 1) {

                mMap.addMarker(new MarkerOptions().snippet("").position(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud))
                        .snippet("Fecha: " + Puntos.get(i).Fecha)
                        .title("Fin Recorrido").infoWindowAnchor(0, 1).anchor(0, 1));
            }

            builder.include(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud));
            int VelocidadTemp = (int) Puntos.get(i).Velocidad;
            //  puntosLinea.add(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud));
            if (VelocidadTemp >= 0 && VelocidadTemp <= rango) {
                ColorRuta = getResources().getColor(R.color.colorruta1);
            }
            if (VelocidadTemp > rango && VelocidadTemp < rango * 2) {
                ColorRuta = getResources().getColor(R.color.colorruta2);
            }
            if (VelocidadTemp > rango * 2 && VelocidadTemp < rango * 3) {
                ColorRuta = getResources().getColor(R.color.colorruta3);
            }
            if (VelocidadTemp > rango * 3 && VelocidadTemp < rango * 4) {
                ColorRuta = getResources().getColor(R.color.colorruta4);
            }
            if (VelocidadTemp > rango * 4 && VelocidadTemp < rango * 5) {
                ColorRuta = getResources().getColor(R.color.colorruta5);
            }
            if (VelocidadTemp > rango * 5 && VelocidadTemp < rango * 6) {
                ColorRuta = getResources().getColor(R.color.colorruta6);
            }
            if (i + 1 < Puntos.size())
                mMap.addPolyline(new PolylineOptions().add(new LatLng(Puntos.get(i).Latitud, Puntos.get(i).Longitud), new LatLng(Puntos.get(i + 1).Latitud, Puntos.get(i + 1).Longitud)).color(ColorRuta).width(5).geodesic(true));
        }
        // mMap.addPolyline(new PolylineOptions().(puntosLinea).color(Color.RED).width(5).geodesic(true));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosLinea.get(0), 10));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 350, 350, 10));
    }


    private class ObtenerUltimaTransmision extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
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
                ArrayList<DeviceData> Puntos = new ArrayList<>();
                DeviceData punto = new DeviceData();

                punto.Id = puntosMapa.optInt("Id");
                punto.Latitud = Float.valueOf(puntosMapa.optString("Latitud"));
                punto.Longitud = Float.valueOf(puntosMapa.optString("Longitud"));
                Date fechaN = parseDateTime(puntosMapa.optString("FechaTransmision"));
                punto.Fecha = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(fechaN.getTime());
                punto.Bateria = puntosMapa.optInt("Bateria");
                punto.Velocidad = puntosMapa.optInt("Velocidad");
                Puntos.add(punto);

                PintarRecorrido(Puntos);

            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }
    }

    private Date parseDateTime(String lastModified) {
        Date date = null;
        if (lastModified != null && lastModified.length() > 0) {
            try {
                lastModified = lastModified.replace("/Date(", "");
                lastModified = lastModified.replace(")/", "");
                date = new Date(Long.parseLong(lastModified));
            } catch (Exception e) {
                // otherwise we just leave it empty
            }
        }
        return date;
    }

    private class CrearRecorrido extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
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
                Log.w("Error", e.toString());
            }
        }
    }

    private class EnviarRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
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
                    SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
                    SharedPreferences.Editor editor = moto.edit();
                    editor.putInt("IdRecorrido", 0);
                    editor.apply();
                }

            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }
    }


    private class ObtenerPuntosRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject dataRe = new JSONObject(s);
                JSONArray puntosMapa = dataRe.optJSONArray("d");
                ArrayList<DeviceData> Puntos = new ArrayList<>();

                for (int i = 0; i < puntosMapa.length(); i++) {
                    JSONObject puntoTemp = puntosMapa.optJSONObject(i);
                    DeviceData punto = new DeviceData();

                    punto.Id = puntoTemp.optInt("Id");
                    punto.Latitud = Float.valueOf(puntoTemp.optString("Latitud"));
                    punto.Longitud = Float.valueOf(puntoTemp.optString("Longitud"));
                    Date fechaN = parseDateTime(puntoTemp.optString("FechaTransmision"));
                    punto.Fecha = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(fechaN.getTime());
                    punto.Bateria = puntoTemp.optInt("Bateria");
                    punto.Velocidad = puntoTemp.optInt("Velocidad");
                    Puntos.add(punto);
                }
                PintarRecorridoColores(Puntos, getIntent().getIntExtra("VelMax", 0));

            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
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
