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
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.BluetoothLe.BluetoothLE;
import com.rmasc.fireroad.Entities.DeviceBluetooth;
import com.rmasc.fireroad.Entities.DeviceData;

import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imageViewBateria, imageViewGas, imageViewUser;
    ImageButton imageButtonEstado, imageButtonCandado;
    Button btnRecorrido;
    TextView txtUser, txtReporteDispositivo, txtValueProgress;
    ProgressBar tachoMeter;

    BluetoothLE bluetoothLE;
    BluetoothGattCharacteristic charSerial;
    DeviceBluetooth DispositivoAsociado;
    String tramaIncompleta = "";

    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);

    private static View.OnClickListener buttonClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (IsNewUser())
        {
            Intent goToIntro = new Intent(getBaseContext(), IntroActivity.class);
            startActivity(goToIntro);
            finish();
        }

        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId())
                {
                    case R.id.imageButtonEstado:
                        //Permite encender la moto (si está disponible).
                        break;
                    case R.id.imageButtonCandado:
                        //Modo parqueo on/off activa o desactiva notificaciones de alarma.
                        break;
                    case R.id.imageViewUser:
                        VerDispositivoMapa();
                        break;
                    case R.id.btnRecorrido:
                        //Empieza a guardar datos del recorrido
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ConnectToDevice();
                            }
                        });
                    default:
                        break;
                }
            }
        };

        if (LoadDevice())
        {
            if(ManagerBluetooth()) {
                bluetoothLE.scanLeDevice(true);
            }
        }
        else
        {
            ShowMessage("No hay dispositivo previamente guardado.");
        }

        AssignViews();
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

    private void AssignViews()
    {
        imageViewBateria = (ImageView) findViewById(R.id.imageViewBateria);
        imageViewGas = (ImageView) findViewById(R.id.imageViewGas);
        imageViewUser = (ImageView) findViewById(R.id.imageViewUser);
        imageViewUser.setOnClickListener(buttonClickListener);

        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireUser.PNG";
            File streamImage = new File(path);
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
        }

        imageButtonEstado = (ImageButton) findViewById(R.id.imageButtonEstado);
        imageButtonEstado.setOnClickListener(buttonClickListener);
        imageButtonCandado = (ImageButton) findViewById(R.id.imageButtonCandado);
        imageButtonCandado.setOnClickListener(buttonClickListener);

        btnRecorrido = (Button) findViewById(R.id.btnRecorrido);
        btnRecorrido.setOnClickListener(buttonClickListener);

        txtUser = (TextView) findViewById(R.id.txtUser);
        txtReporteDispositivo = (TextView) findViewById(R.id.txtReporteDispositivo);
        txtValueProgress = (TextView) findViewById(R.id.txtValueProgress);

        tachoMeter = (ProgressBar) findViewById(R.id.tachoMeter);

        /*
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        MenuFragment fragmentDemo = new MenuFragment();
        ft.replace(R.id.fragmentMenu, fragmentDemo);
        ft.commit();*/
    }

    private void ShowMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowSnackMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(txtUser, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        });
    }

    private void SetProgressBar(final int value)
    {
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

    public boolean ManagerBluetooth()
    {
        try
        {
            if (BluetoothAdapter.getDefaultAdapter() == null)
            {
                ShowMessage("Su dispositivo no es compatible con Bluetooth");
                return false;
            }
            else
            {
                if (bluetoothLE == null)
                {
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
                            if (characteristic.getUuid().equals(bluetoothLE.CHARACTERISTIC_SERIAL))
                            {
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
                            for(BluetoothGattService servicios : bluetoothLE.bluetoothGattServiceList) {
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
                            switch (status)
                            {
                                case 0:
                                    ShowSnackMessage("Idle Mode");
                                    break;
                                case 1:
                                    ShowSnackMessage("Connecting");
                                    break;
                                case 4:
                                    ShowSnackMessage("Connection closed");
                                    break;
                                default:
                                    ShowSnackMessage("Unknown status: " + String.valueOf(status) + " Ns: " + String.valueOf(newState));
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
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean LoadDevice()
    {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
        String Nombre = sharedPref.getString("Name", "");
        String Mac = sharedPref.getString("Mac", "");
        if (Nombre.equals(""))
        {
            return false;
        }
        else
        {
            DispositivoAsociado = new DeviceBluetooth();
            DispositivoAsociado.Name = Nombre;
            DispositivoAsociado.Mac = Mac;
            return true;
        }
    }

    private void ConnectToDevice()
    {
        for (int i = 0; i < bluetoothLE.bleDevices.size() ; i++)
        {
            if (bluetoothLE.bleDevices.get(i).getAddress().equals(DispositivoAsociado.Mac))
            {
                try
                {
                    bluetoothLE.ConnectToGattServer(bluetoothLE.bleDevices.get(i), true);

                    if(!bluetoothLE.bleGatt.discoverServices())
                        ShowMessage("Servicios no descubiertos.");
                    ShowMessage("Conectado a: " + DispositivoAsociado.Name);
                }
                catch (Exception e)
                {
                    ShowMessage("Error al conectarse a: " + DispositivoAsociado.Name);
                }
                break;
            }
        }
    }

    public void ProcesarTrama(String tramaIn)
    {
        if (tramaIn.contains("ST3"))
        {
            tramaIncompleta = tramaIn;
            return;
        }
        else if (tramaIncompleta.contains("ST3") && tramaIn.endsWith("$"))
        {
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

     private void ActualizarControles()
     {
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

    private void SetImageViews()
    {
        int battPercent = ((int) ((DispositivoAsociado.DataReceived.Bateria * 100.0) / (3.8)));
        if (battPercent < 100)
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
            imageViewBateria.setImageResource(R.drawable.bateria_0);

        if (DispositivoAsociado.DataReceived.VoltajeEntrada > 0)
            imageButtonEstado.setImageResource(R.drawable.estado_on);
        else
            imageButtonEstado.setImageResource(R.drawable.estado_off);
    }

    private boolean IsNewUser()
    {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        if (sharedPref.getInt("Id", 0) == 0)
            return true;
        else
            return false;
    }

    private void VerDispositivoMapa()
    {
        if (DispositivoAsociado != null && DispositivoAsociado.DataReceived != null) {
            Intent i = new Intent(getBaseContext(), MapsActivity.class);
            i.putExtra("Lat", DispositivoAsociado.DataReceived.Latitud);
            i.putExtra("Lon", DispositivoAsociado.DataReceived.Longitud);
            startActivity(i);
        }
        else
            ShowMessage("Sin datos para mostrar.");
    }

    public void UpdateWidget()
    {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("Speed", ((int) DispositivoAsociado.DataReceived.Velocidad));
        editor.commit();

        Intent updateWidget = new Intent(getBaseContext(), VelocityWidget.class);
        updateWidget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(),VelocityWidget.class));
        updateWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(updateWidget);
    }
}
