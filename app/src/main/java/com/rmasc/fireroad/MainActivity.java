package com.rmasc.fireroad;

import android.animation.ObjectAnimator;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.CircularImageView;
import com.rmasc.fireroad.BluetoothLe.BluetoothLE;
import com.rmasc.fireroad.DataBase.TransmisionesHelper;
import com.rmasc.fireroad.Entities.DeviceBluetooth;
import com.rmasc.fireroad.Entities.DeviceData;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ZQqwjflLo84ULNenXXiHAGR9s";
    private static final String TWITTER_SECRET = "ZttElB9UKZfgl3My0xgkjgol5OLtVtRDuQrCpQ7052eipvxhYR";


    TwitterLoginButton twitterloginButton;

    ImageView imageViewBateria, imageViewGps;
    Button btnRecorrido;
    ImageButton btnMapa;
    TextView txtUser, txtReporteDispositivo, txtBattMoto, txtBattDispositivo, txtValueProgress;
    ProgressBar tachoMeter;
    Switch switchEncendido;

    BluetoothLE bluetoothLE;
    BluetoothGattCharacteristic charSerial;
    DeviceBluetooth DispositivoAsociado;
    String tramaIncompleta = "";

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);

    private static View.OnClickListener buttonClickListener;
    private CircularImageView circularImageView;
    private static boolean isRecorrido = false;
    private static int countTramas = 0;

    private TransmisionesHelper transmisionesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
       // circularImageView = (CircularImageView) findViewById(R.id.CircularImageViewUser);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (IsNewUser()) {
            Intent goToIntro = new Intent(getBaseContext(), IntroActivity.class);
            startActivity(goToIntro);
            finish();
        } else {

            transmisionesHelper = new TransmisionesHelper(this);

            buttonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (v.getId()) {
                        //Modo parqueo on/off activa o desactiva notificaciones de alarma.
                        //EnviarAlDispositivo(R.id.imageButtonCandado);
                        case R.id.imageViewUser:

                            break;
                        case R.id.btnMapa:
                            VerDispositivoMapa();
                            break;
                        case R.id.btnRecorrido:
                            if (!isRecorrido) {
                                new CrearRecorrido().execute();
                            } else {
                                new EnviarRecorrido().execute(transmisionesHelper.SelectTransmision(transmisionesHelper.getReadableDatabase(), "VehiculoId = " + DispositivoAsociado.DataReceived.VehiculoId + " AND ReporteId = " + DispositivoAsociado.DataReceived.ReporteId, null));
                            }
                            break;
                        default:
                            break;
                    }
                }
            };

            if (LoadDevice()) {
                if (ManagerBluetooth()) {
                    bluetoothLE.scanLeDevice(true);
                }
            } else {
                ShowMessage("No hay dispositivo previamente guardado.");
            }

            AssignViews();
        }
    }

    @Override
    protected void onDestroy() {
        if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
            bluetoothLE.bleGatt.disconnect();
            bluetoothLE.bleGatt.close();
            unregisterReceiver(bluetoothLE.bleBroadcastReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShowMessage("Resumiendo..");
    }

    private void AssignViews() {
        SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
     //   imageViewBateria = (ImageView) findViewById(R.id.imgBateria);
     //   imageViewGps = (ImageView) findViewById(R.id.imgGps);


     //   circularImageView = (CircularImageView) findViewById(R.id.CircularImageViewUser);
     //   circularImageView.setOnClickListener(buttonClickListener);

        switchEncendido = (Switch) findViewById(R.id.switchEncendido);
        switchEncendido.setEnabled(true);
        switchEncendido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (bluetoothLE != null && DispositivoAsociado != null) {
                        bluetoothLE.bleDevices = new ArrayList<BluetoothDevice>();
                        bluetoothLE.scanLeDevice(isChecked);
                        switchEncendido.setText("Buscando");
                        switchEncendido.setEnabled(false);

                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ConnectToDevice();
                            }
                        }, 5000);
                    }
                } else {
                    switchEncendido.setText("Desconectado");
                    if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
                        //bluetoothLE.bleGatt.disconnect();
                        bluetoothLE.bleGatt.close();
                        bluetoothLE.bleGatt = null;
                    }
                }
            }
        });
        try {
            if (user.getString("FotoPath", "").equals("")) {
                String path = Environment.getExternalStorageDirectory().toString() + "/FireMoto";
                InputStream prueba = new URL(path).openStream();
                Bitmap foto = BitmapFactory.decodeStream(prueba);
             //   circularImageView.setImageBitmap(foto);
            } else {
                InputStream prueba = new URL(user.getString("FotoPath", "")).openStream();
                Bitmap foto = BitmapFactory.decodeStream(prueba);
            //    circularImageView.setImageBitmap(foto);
            }
        } catch (Exception e) {
            e.printStackTrace();
           // circularImageView.setImageResource(R.drawable.no_user);
        }

        btnRecorrido = (Button) findViewById(R.id.btnRecorrido);
        btnRecorrido.setOnClickListener(buttonClickListener);
        btnMapa = (ImageButton) findViewById(R.id.btnMapa);
        btnMapa.setOnClickListener(buttonClickListener);

        txtUser = (TextView) findViewById(R.id.txtUser);
        txtUser.setText(user.getString("UserLogin", ""));
        txtReporteDispositivo = (TextView) findViewById(R.id.txtReporteDispositivo);
      //  txtBattDispositivo = (TextView) findViewById(R.id.txtbatGps);
      //  txtBattMoto = (TextView) findViewById(R.id.txtbatMoto);

        tachoMeter = (ProgressBar) findViewById(R.id.tachoMeter);
        txtValueProgress = (TextView) findViewById(R.id.txtValueProgress);
        SetProgressBar(0);
    }

    private void ShowMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowSnackMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(txtUser, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
    }

    private void SetProgressBar(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofInt(tachoMeter, "progress", tachoMeter.getProgress(), value); //Desde un valor hasta otro valor
                animation.setDuration(3000);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
                txtValueProgress.setText(value + "\n Km/h");
            }
        });
    }

    public boolean ManagerBluetooth() {
        try {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                ShowMessage("Su dispositivo no es compatible con Bluetooth");
                return false;
            } else {
                if (bluetoothLE == null) {
                    bluetoothLE = new BluetoothLE(getBaseContext());
                    bluetoothLE.bleGattCallback = new BluetoothGattCallback() {
                        @Override
                        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                            ShowMessage("RSSI " + String.valueOf(rssi));
                            super.onReadRemoteRssi(gatt, rssi, status);
                        }

                        @Override
                        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                            super.onCharacteristicChanged(gatt, characteristic);
                            if (characteristic.getUuid().equals(bluetoothLE.CHARACTERISTIC_SERIAL)) {
                                //charSerial = characteristic;
                                ProcesarTrama(characteristic.getStringValue(0));
                            }
                        }

                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicRead(gatt, characteristic, status);
                        }

                        @Override
                        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicWrite(gatt, characteristic, status);
                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            super.onServicesDiscovered(gatt, status);
                            for (BluetoothGattService servicio : gatt.getServices()) {
                                BluetoothGattCharacteristic charSerial = servicio.getCharacteristic(bluetoothLE.CHARACTERISTIC_SERIAL);
                                if (charSerial != null)
                                    bluetoothLE.bleGatt.setCharacteristicNotification(charSerial, true);
                            }
                        }

                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            switch (status) {
                                case 0:
                                    ShowSnackMessage("Idle Mode");
                                    break;
                                case 1:
                                    ShowSnackMessage("Connecting");
                                    switchEncendido.setText("Conectando");
                                    break;
                                case 4:
                                    ShowSnackMessage("Connection closed");
                                    OnConnectionChanged(false);
                                    break;
                                default:
                                    break;
                            }
                            switch (newState) {
                                case 0:
                                    OnConnectionChanged(false);
                                    break;
                                case 2:
                                    OnConnectionChanged(true);
                                    break;
                                default:
                                    break;
                            }
                            super.onConnectionStateChange(gatt, status, newState);
                        }
                    };
                }
                if (!bluetoothLE.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);

                }

                this.registerReceiver(bluetoothLE.bleBroadcastReceiver, filter);
                this.registerReceiver(bluetoothLE.bleBroadcastReceiver, filter1);
                this.registerReceiver(bluetoothLE.bleBroadcastReceiver, filter2);
            }
            return true;
        } catch (Exception e) {
            return false;
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

    private void ConnectToDevice() {
        boolean isVisible = false;
        for (int i = 0; i < bluetoothLE.bleDevices.size(); i++) {
            //if (bluetoothLE.bleDevices.get(i).getAddress().equals("74:DA:EA:AF:8A:67")) {
            if (bluetoothLE.bleDevices.get(i).getAddress().equals("74:DA:EA:B2:33:01")) {
                //if (bluetoothLE.bleDevices.get(i).getName().equals(DispositivoAsociado.Name)) {
                try {
                    bluetoothLE.ConnectToGattServer(bluetoothLE.bleDevices.get(i), true);
                    isVisible = true;
                } catch (Exception e) {
                    ShowMessage("Error al conectarse a: " + DispositivoAsociado.Name);
                }
                break;
            }
        }
        if (!isVisible) {
            ShowMessage("No se encontró el dispositivo.");
            switchEncendido.setEnabled(true);
            switchEncendido.setChecked(false);
            switchEncendido.setText("Desconectado");
        }
    }

    public void ProcesarTrama(String tramaIn) {
        if (tramaIn.contains("ST3")) {
            tramaIncompleta = tramaIn;
            return;
        } else if (tramaIncompleta.contains("ST3") && tramaIn.endsWith("$")) {
            tramaIncompleta += tramaIn;
            new TramaProcess().execute(tramaIncompleta);
            tramaIncompleta = "";
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String tramaCompleta = tramaIncompleta;
                        tramaIncompleta = "";
                        DispositivoAsociado.DataReceived = new DeviceData(tramaCompleta);
                        ActualizarControles();
                        if (isRecorrido) {
                            if (countTramas == 10) {
                                ShowMessage("10 tramas");
                                SharedPreferences user = getSharedPreferences("User", MODE_PRIVATE);
                                SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                                new EnviarTrama().execute(String.valueOf(user.getInt("Id", 0)), String.valueOf(moto.getInt("IdRecorrido", 0)),String.valueOf(moto.getInt("Id",0)),
                                        String.valueOf(DispositivoAsociado.DataReceived.Latitud), String.valueOf(DispositivoAsociado.DataReceived.Longitud),
                                        DispositivoAsociado.DataReceived.FormatDate() , String.valueOf(DispositivoAsociado.DataReceived.Bateria).substring(0, 2),
                                        String.valueOf(1), String.valueOf(((int) DispositivoAsociado.DataReceived.Velocidad)), "0");
                                countTramas = 0;
                            }
                            else
                            {
                                countTramas ++;
                            }
                        }
                    }
                    catch (Exception e){}

                }
            }); */
            return;
        }
        tramaIncompleta += tramaIn;
    }

    private class TramaProcess extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String tramaCompleta = params[0];
                DispositivoAsociado.DataReceived = new DeviceData(tramaCompleta);
                ActualizarControles();
                sendBroadcast(setIntentToMap(true));
                if (isRecorrido) {
                    //SharedPreferences user = getSharedPreferences("User", MODE_PRIVATE);
                    //SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                    GuardarTransmision(DispositivoAsociado.DataReceived);
                    if (countTramas == 10) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //new EnviarTrama().execute(String.valueOf(user.getInt("Id", 0)), String.valueOf(moto.getInt("IdRecorrido", 0)), String.valueOf(moto.getInt("Id", 0)),
                                //      String.valueOf(DispositivoAsociado.DataReceived.Latitud), String.valueOf(DispositivoAsociado.DataReceived.Longitud),
                                //     DispositivoAsociado.DataReceived.FormatDate() + " " + DispositivoAsociado.DataReceived.Hora, String.valueOf(((int) DispositivoAsociado.DataReceived.Bateria)),
                                //     String.valueOf(1), String.valueOf(((int) DispositivoAsociado.DataReceived.Velocidad)), "0");
                            }
                        });
                        countTramas = 0;
                    } else {
                        countTramas++;
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }
    }

    private void ActualizarControles() {
        SetProgressBar(((int) DispositivoAsociado.DataReceived.Velocidad));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtReporteDispositivo.setText("ùlt. vez "+DispositivoAsociado.DataReceived.FormatDate() + " " + DispositivoAsociado.DataReceived.Hora);
                SetImageViews();
                UpdateWidget();
            }
        });
    }

    private void SetImageViews() {

        int battPercent = ((int) (DispositivoAsociado.DataReceived.Bateria));
        int battExtern = (int) (DispositivoAsociado.DataReceived.VoltajeEntrada);


        if(battPercent<=13) {

            imageViewBateria.setImageResource(R.drawable.bateria_0);

        }
        if(battPercent<=26 && battPercent>13) {

            imageViewBateria.setImageResource(R.drawable.bateria_12_5);

        }
        if(battPercent<=39 && battPercent >26) {

            imageViewBateria.setImageResource(R.drawable.bateria_25);

        }
        if(battPercent<=52 && battPercent >39) {

            imageViewBateria.setImageResource(R.drawable.bateria_37_5);

        }

        if(battPercent<=65 && battPercent >52) {

            imageViewBateria.setImageResource(R.drawable.bateria_50);

        }

        if(battPercent<=79 && battPercent >65) {

            imageViewBateria.setImageResource(R.drawable.bateria_62_5);

        }

        if(battPercent<=92 && battPercent >79) {


            imageViewBateria.setImageResource(R.drawable.bateria_75);
        }

        if(battPercent<=100&& battPercent >92) {

            imageViewBateria.setImageResource(R.drawable.bateria_100);

        }

        if(battPercent<=13) {

            imageViewBateria.setImageResource(R.drawable.bateria_0);

        }
        if(battPercent<=26 && battPercent>13) {

            imageViewBateria.setImageResource(R.drawable.bateria_12_5);

        }
        if(battPercent<=39 && battPercent >26) {

            imageViewBateria.setImageResource(R.drawable.bateria_25);

        }
        if(battPercent<=52 && battPercent >39) {

            imageViewBateria.setImageResource(R.drawable.bateria_37_5);

        }

        if(battPercent<=65 && battPercent >52) {

            imageViewBateria.setImageResource(R.drawable.bateria_50);

        }

        if(battPercent<=79 && battPercent >65) {

            imageViewBateria.setImageResource(R.drawable.bateria_62_5);

        }

        if(battPercent<=92 && battPercent >79) {


            imageViewBateria.setImageResource(R.drawable.bateria_75);
        }

        if(battPercent<=100&& battPercent >92) {

            imageViewBateria.setImageResource(R.drawable.bateria_100);

        }


        if(battExtern<=13) {

            imageViewGps.setImageResource(R.drawable.bateria_0);

        }
        if(battExtern<=26 && battExtern>13) {

            imageViewGps.setImageResource(R.drawable.bateria_12_5);

        }
        if(battExtern<=39 && battExtern >26) {

            imageViewGps.setImageResource(R.drawable.bateria_25);

        }
        if(battExtern<=52 && battExtern >39) {

            imageViewGps.setImageResource(R.drawable.bateria_37_5);

        }

        if(battExtern<=65 && battExtern >52) {

            imageViewGps.setImageResource(R.drawable.bateria_50);

        }

        if(battExtern<=79 && battExtern >65) {

            imageViewGps.setImageResource(R.drawable.bateria_62_5);

        }

        if(battExtern<=92 && battExtern >79) {


            imageViewGps.setImageResource(R.drawable.bateria_75);
        }

        if(battExtern<=100&& battExtern >92) {

            imageViewGps.setImageResource(R.drawable.bateria_100);

        }





        txtBattMoto.setText(String.valueOf(DispositivoAsociado.DataReceived.VoltajeEntrada).substring(0, 3) + "v");
        txtBattDispositivo.setText(String.valueOf(DispositivoAsociado.DataReceived.Bateria) + "v");

        if (DispositivoAsociado.DataReceived.Modo == 2) {
            //circularImageView.setBorderColor(R.color.darkgreen);
        }
        else {
            //circularImageView.setBorderColor(Color.RED);
        }
    }

    private boolean IsNewUser() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        if (sharedPref.getInt("Id", 0) == 0)
            return true;
        else
            return false;
    }

    private Intent setIntentToMap(boolean isUpdate) {
        Intent i;
        if (isUpdate) {
            i = new Intent("UPDATE_MAP");
        } else {
            i = new Intent(getBaseContext(), MapsActivity.class);
        }
        i.putExtra("Lat", DispositivoAsociado.DataReceived.Latitud);
        i.putExtra("Lon", DispositivoAsociado.DataReceived.Longitud);
        i.putExtra("Fecha", DispositivoAsociado.DataReceived.Fecha);
        i.putExtra("IsRecorrido", isRecorrido);
        i.putExtra("Tipo", 1);
        return i;
    }

    private void VerDispositivoMapa() {
        if (DispositivoAsociado != null && DispositivoAsociado.DataReceived != null) {
            if (bluetoothLE != null) {
                if (bluetoothLE.DeviceStatus.equals("Connected"))
                    startActivity(setIntentToMap(false));
            } else {
                Intent i = new Intent(getBaseContext(), MapsActivity.class);
                i.putExtra("Tipo", 3);
                startActivity(i);
            }
        } else {
            Intent i = new Intent(getBaseContext(), MapsActivity.class);
            i.putExtra("Tipo", 3);
            startActivity(i);
        }
    }

    public void UpdateWidget() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Speed", ((int) DispositivoAsociado.DataReceived.Velocidad));
        editor.commit();

        Intent updateWidget = new Intent(getBaseContext(), VelocityWidget.class);
        updateWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), VelocityWidget.class));
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateWidget);
    }

    public void OnConnectionChanged(final boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    bluetoothLE.bleGatt.discoverServices();
                    switchEncendido.setChecked(true);
                    switchEncendido.setText("Conectado");
                    ShowMessage("Conectado a: " + DispositivoAsociado.Name);
                } else {
                    switchEncendido.setChecked(false);
                    switchEncendido.setText("Desconectado");
                }
                switchEncendido.setEnabled(true);
            }
        });
    }

    public void EnviarAlDispositivo(String comando) {
        if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
            if (charSerial != null) {
                charSerial.setValue(comando);
                bluetoothLE.bleGatt.writeCharacteristic(charSerial);
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
                    btnRecorrido.setText("Stop");
                    btnRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
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
                    btnRecorrido.setText("IniciarRecorrido");
                    btnRecorrido.setTextColor(getResources().getColor(R.color.colorOk));
                }

            } catch (Exception e) {

            }
        }
    }

    private class EnviarTrama extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "IdUser";
            parametro.Valor = params[0];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdRecorrido";
            parametro.Valor = params[1];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = params[2];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Latitud";
            parametro.Valor = params[3];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Longitud";
            parametro.Valor = params[4];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Fecha";
            parametro.Valor = params[5];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Bateria";
            parametro.Valor = params[6];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Encendido";
            parametro.Valor = params[7];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Velocidad";
            parametro.Valor = params[8];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Altitud";
            parametro.Valor = params[9];
            parameters.add(parametro);

            return WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/GuardarTransmision", parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                String result = jsonResponse.optString("d");
                ShowMessage(result);
                if (!result.equals("true")) {
                    //Guardar y envias más tarde (?)
                }

            } catch (Exception e) {

            }
        }
    }

    private void GuardarTransmision(DeviceData transmisionToSave) {
        SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
        transmisionToSave.ReporteId = moto.getInt("IdRecorrido", 0);
        DispositivoAsociado.DataReceived.ReporteId = transmisionToSave.ReporteId;
        transmisionesHelper.InsertTransmision(transmisionesHelper.getWritableDatabase(), transmisionToSave);
    }
}
