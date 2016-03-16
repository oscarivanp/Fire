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
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.BluetoothLe.BluetoothLE;
import com.rmasc.fireroad.Entities.DeviceBluetooth;
import com.rmasc.fireroad.Entities.DeviceData;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.File;
import java.io.FileInputStream;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ZQqwjflLo84ULNenXXiHAGR9s";
    private static final String TWITTER_SECRET = "ZttElB9UKZfgl3My0xgkjgol5OLtVtRDuQrCpQ7052eipvxhYR";



    TwitterLoginButton twitterloginButton;

    ImageView imageViewBateria, imageViewUser;
    Button btnRecorrido, btnMapa;
    TextView txtUser, txtReporteDispositivo, txtValueProgress, txtConexion;
    ProgressBar tachoMeter, progressBattMoto, progressBattDispositivo, progressCombustible;
    Switch switchEncendido, switchEstacionado;

    BluetoothLE bluetoothLE;
    BluetoothGattCharacteristic charSerial;
    DeviceBluetooth DispositivoAsociado;
    String tramaIncompleta = "";

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);

    private static View.OnClickListener buttonClickListener;

    private static boolean isRecorrido = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (IsNewUser()) {
            Intent goToIntro = new Intent(getBaseContext(), IntroActivity.class);
            startActivity(goToIntro);
            finish();
        } else {

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
                            //Empieza a enviar datos del recorrido
                            isRecorrido = !isRecorrido;
                            if (isRecorrido) { //Inicia el envio de tramas para el recorrido
                                btnRecorrido.setText("Stop");
                                btnRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
                            } else {
                                //Detiene el envio de tramas del recorrido
                                btnRecorrido.setText("Go!");
                                btnRecorrido.setTextColor(getResources().getColor(R.color.colorOk));
                            }
                            break;
                        case R.id.txtConexion:
                            if (bluetoothLE != null && DispositivoAsociado != null)
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ConnectToDevice();
                                    }
                                });
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

    private void AssignViews() {
        imageViewBateria = (ImageView) findViewById(R.id.imageViewBateria);
        imageViewUser = (ImageView) findViewById(R.id.imageViewUser);
        imageViewUser.setOnClickListener(buttonClickListener);

        switchEncendido = (Switch) findViewById(R.id.switchEncendido);
        switchEncendido.setEnabled(false);
        switchEstacionado = (Switch) findViewById(R.id.switchEstacionado);
        switchEstacionado.setEnabled(false);


        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireUser";
            File streamImage = new File(path);
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        } catch (Exception e) {
            e.printStackTrace();
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
        }

        btnRecorrido = (Button) findViewById(R.id.btnRecorrido);
        btnRecorrido.setOnClickListener(buttonClickListener);
        btnMapa = (Button) findViewById(R.id.btnMapa);
        btnMapa.setOnClickListener(buttonClickListener);

        SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
        txtUser = (TextView) findViewById(R.id.txtUser);
        txtUser.setText(user.getString("UserLogin", ""));
        txtReporteDispositivo = (TextView) findViewById(R.id.txtReporteDispositivo);
        txtValueProgress = (TextView) findViewById(R.id.txtValueProgress);
        txtConexion = (TextView) findViewById(R.id.txtConexion);
        txtConexion.setOnClickListener(buttonClickListener);

        tachoMeter = (ProgressBar) findViewById(R.id.tachoMeter);
        progressBattDispositivo = (ProgressBar) findViewById(R.id.progressBattDispositivo);
        progressBattMoto = (ProgressBar) findViewById(R.id.progressBattMoto);
        progressCombustible = (ProgressBar) findViewById(R.id.progressCombustible);

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
                                charSerial = characteristic;
                                ProcesarTrama(charSerial.getStringValue(0));
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
                            bluetoothLE.bluetoothGattServiceList.addAll(gatt.getServices());
                            for (BluetoothGattService servicios : bluetoothLE.bluetoothGattServiceList) {
                                bluetoothLE.bleGattCharasteristicList.addAll(servicios.getCharacteristics());
                                for (final BluetoothGattCharacteristic characteristic : bluetoothLE.bleGattCharasteristicList) {
                                    if (characteristic.getUuid().equals(bluetoothLE.CHARACTERISTIC_SERIAL))
                                        bluetoothLE.bleGatt.setCharacteristicNotification(characteristic, true);

                                }
                            }
                            super.onServicesDiscovered(gatt, status);
                        }

                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            switch (status) {
                                case 0:
                                    ShowSnackMessage("Idle Mode");
                                    break;
                                case 1:
                                    ShowSnackMessage("Connecting");
                                    txtConexion.setText("Conectando");
                                    txtConexion.setTextColor(getResources().getColor(R.color.colorWaiting));
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
        if (Nombre.equals("")) {
            return false;
        } else {
            DispositivoAsociado = new DeviceBluetooth();
            DispositivoAsociado.Name = Nombre;
            DispositivoAsociado.Mac = Mac;
            return true;
        }
    }

    private void ConnectToDevice() {
        for (int i = 0; i < bluetoothLE.bleDevices.size(); i++) {
            if (bluetoothLE.bleDevices.get(i).getAddress().equals(DispositivoAsociado.Mac)) {
                try {
                    bluetoothLE.ConnectToGattServer(bluetoothLE.bleDevices.get(i), true);

                    if (!bluetoothLE.bleGatt.discoverServices())
                        ShowMessage("Servicios no descubiertos.");
                    ShowMessage("Conectado a: " + DispositivoAsociado.Name);
                } catch (Exception e) {
                    ShowMessage("Error al conectarse a: " + DispositivoAsociado.Name);
                }
                break;
            }
        }
    }

    public void ProcesarTrama(String tramaIn) {
        if (tramaIn.contains("ST3")) {
            tramaIncompleta = tramaIn;
            return;
        } else if (tramaIncompleta.contains("ST3") && tramaIn.endsWith("$")) {
            tramaIncompleta += tramaIn;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DispositivoAsociado.DataReceived = new DeviceData(tramaIncompleta);
                    ActualizarControles();
                }
            });
            return;
        }
        tramaIncompleta += tramaIn;
    }

    private void ActualizarControles() {
        SetProgressBar(((int) DispositivoAsociado.DataReceived.Velocidad));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtReporteDispositivo.setText(DispositivoAsociado.DataReceived.Fecha + " " + DispositivoAsociado.DataReceived.Hora);
                SetImageViews();
                UpdateWidget();
            }
        });
    }

    private void SetImageViews() {
        int battPercent = ((int) ((DispositivoAsociado.DataReceived.Bateria * 100.0) / (3.8)));
        int battExtern = (int) ((DispositivoAsociado.DataReceived.VoltajeEntrada * 100.0) / (14.4));
        progressBattMoto.setProgress(battExtern);
        progressBattDispositivo.setProgress(battPercent);


        /*if (battPercent < 100)
            imageViewBateria.setImageResource(R.drawable.bateria_100);
        if (battPercent < 90)
            imageViewBateria.setImageResource(R.drawable.bateria_87_5);
        if (battPercent < 78)
            imageViewBateria.setImageResource(R.drawable.bateria_75);
        if (battPercent < 63)
            imageViewBateria.setImageResource(R.drawable.bateria_62_5);
        if (battPercent < 55)
            imageViewBateria.setImageResource(R.drawable.bateria_50);
        if (battPercent < 37)
            imageViewBateria.setImageResource(R.drawable.bateria_37_5);
        if (battPercent < 12)
            imageViewBateria.setImageResource(R.drawable.bateria_12_5);
        if (battPercent < 5)
            imageViewBateria.setImageResource(R.drawable.bateria_0); */

        if (DispositivoAsociado.DataReceived.VoltajeEntrada > 0)
            switchEncendido.setChecked(true);
        else
            switchEncendido.setChecked(false);
    }

    private boolean IsNewUser() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        if (sharedPref.getInt("Id", 0) == 0)
            return true;
        else
            return false;
    }

    private void VerDispositivoMapa() {
        if (DispositivoAsociado != null && DispositivoAsociado.DataReceived != null) {
            Intent i = new Intent(getBaseContext(), MapsActivity.class);
            i.putExtra("Lat", DispositivoAsociado.DataReceived.Latitud);
            i.putExtra("Lon", DispositivoAsociado.DataReceived.Longitud);
            startActivity(i);
        } else
            ShowMessage("Sin datos para mostrar.");
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

    public void OnConnectionChanged(boolean isConnected) {
        if (isConnected) {
            txtConexion.setText("Online");
            txtConexion.setTextColor(getResources().getColor(R.color.colorOk));
        } else {
            txtConexion.setText("Offline");
            txtConexion.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    public void EnviarAlDispositivo(String comando) {
        if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
            if (charSerial != null) {
                charSerial.setValue(comando);
                bluetoothLE.bleGatt.writeCharacteristic(charSerial);
            }
        }
    }
}
