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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.BluetoothLe.BluetoothLE;
import com.rmasc.fireroad.DataBase.RecorridosHelper;
import com.rmasc.fireroad.DataBase.TransmisionesHelper;
import com.rmasc.fireroad.Entities.DeviceBluetooth;
import com.rmasc.fireroad.Entities.DeviceData;
import com.rmasc.fireroad.Entities.Ruta;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.SubirArchivoRecorrido;
import com.rmasc.fireroad.Services.WebService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetui.internal.util.HtmlEntities;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ZQqwjflLo84ULNenXXiHAGR9s";
    private static final String TWITTER_SECRET = "ZttElB9UKZfgl3My0xgkjgol5OLtVtRDuQrCpQ7052eipvxhYR";


    TwitterLoginButton twitterloginButton;

    ImageView imageViewBateria, imageViewGps, tipoTransmisionImagen;
    Button btnRecorrido;
    ImageView btnMapa;
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
    private ImageView circularImageView;
    private static boolean isRecorrido = false;
    private static int countTramas = 0;
    private Timer refreshTim;

    private TransmisionesHelper transmisionesHelper;
    private RecorridosHelper recorridosHelper;

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

            transmisionesHelper = new TransmisionesHelper(this);
            recorridosHelper = new RecorridosHelper(this);

            buttonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (v.getId()) {
                        //Modo parqueo on/off activa o desactiva notificaciones de alarma.
                        //EnviarAlDispositivo(R.id.imageButtonCandado);
                        case R.id.btnMapa:
                            VerDispositivoMapa();
                            break;
                        case R.id.btnRecorrido:
                            if (!isRecorrido) {
                                //new CrearRecorrido().execute();
                                IniciarRecorrido();
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
                    //bluetoothLE.scanLeDevice(true);
                }
            } else {
                ShowMessage("No hay dispositivo previamente guardado.");
            }

            AssignViews();
        }
        refreshTim = new Timer();
        refreshTim.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 60000);
    }

    @Override
    protected void onDestroy() {
        if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
            bluetoothLE.bleGatt.disconnect();
            bluetoothLE.bleGatt.close();
            unregisterReceiver(bluetoothLE.bleBroadcastReceiver);
        }
        if (refreshTim != null) {
            refreshTim.cancel();
            refreshTim.purge();
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
        imageViewBateria = (ImageView) findViewById(R.id.imgBateria);
        imageViewGps = (ImageView) findViewById(R.id.imgGps);
        tipoTransmisionImagen = (ImageView) findViewById(R.id.modoTransmision);

        circularImageView = (ImageView) findViewById(R.id.CircularImageViewUser);
        circularImageView.setOnClickListener(buttonClickListener);

        switchEncendido = (Switch) findViewById(R.id.switchEncendido);
        switchEncendido.setEnabled(true);
        switchEncendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchEncendido.isChecked()) {
                    if (bluetoothLE != null && DispositivoAsociado != null) {
                        bluetoothLE.bleDevices = new ArrayList<BluetoothDevice>();
                        bluetoothLE.scanLeDevice(true);
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
//        switchEncendido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if (bluetoothLE != null && DispositivoAsociado != null) {
//                        bluetoothLE.bleDevices = new ArrayList<BluetoothDevice>();
//                        bluetoothLE.scanLeDevice(isChecked);
//                        switchEncendido.setText("Buscando");
//                        switchEncendido.setEnabled(false);
//
//                        new android.os.Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                ConnectToDevice();
//                            }
//                        }, 5000);
//                    }
//                } else {
//                    switchEncendido.setText("Desconectado");
//                    if (bluetoothLE != null && bluetoothLE.bleGatt != null) {
//                        //bluetoothLE.bleGatt.disconnect();
//                        bluetoothLE.bleGatt.close();
//                        bluetoothLE.bleGatt = null;
//                    }
//                }
//            }
//        });
        try {
            if (user.getString("FotoPath", "").equals("")) {
                String path = Environment.getExternalStorageDirectory().toString() + "/FireMoto";
                InputStream prueba = new URL(path).openStream();
                Bitmap foto = BitmapFactory.decodeStream(prueba);
                circularImageView.setBackground(new BitmapDrawable(getRoundedCornerBitmap(foto, true)));

            } else {
                InputStream prueba = new URL(user.getString("FotoPath", "")).openStream();
                Bitmap foto = BitmapFactory.decodeStream(prueba);
                circularImageView.setBackground(new BitmapDrawable(getRoundedCornerBitmap(foto, true)));

            }
        } catch (Exception e) {
            e.printStackTrace();
            circularImageView.setBackground(new BitmapDrawable(getRoundedCornerBitmap(getResources().getDrawable(R.drawable.no_user), true)));

        }

        btnRecorrido = (Button) findViewById(R.id.btnRecorrido);
        btnRecorrido.setOnClickListener(buttonClickListener);
        btnMapa = (ImageView) findViewById(R.id.btnMapa);
        btnMapa.setOnClickListener(buttonClickListener);

        txtUser = (TextView) findViewById(R.id.txtUser);
        txtUser.setText(user.getString("UserLogin", ""));
        txtReporteDispositivo = (TextView) findViewById(R.id.txtReporteDispositivo);
        txtBattDispositivo = (TextView) findViewById(R.id.txtbatGps);
        txtBattMoto = (TextView) findViewById(R.id.txtbatMoto);

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
            if (bluetoothLE.bleDevices.get(i).getAddress().equals("74:DA:EA:AF:8A:67")) {
                //if (bluetoothLE.bleDevices.get(i).getAddress().equals("74:DA:EA:B2:33:01")) {
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

    private class ActualizarDeweb extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
            SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences motoPref = getSharedPreferences("Moto", MODE_PRIVATE);
            WebServiceParameter paramTemp = new WebServiceParameter();

            paramTemp.Nombre = "IdUser";
            paramTemp.Valor = String.valueOf(userPref.getInt("Id", 0));
            parameters.add(paramTemp);

            paramTemp = new WebServiceParameter();
            paramTemp.Nombre = "IdVehiculo";
            paramTemp.Valor = String.valueOf(motoPref.getInt("Id", 0));
            parameters.add(paramTemp);

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
                DispositivoAsociado.DataReceived = punto;

                ActualizarControles(false);
            } catch (Exception e) {

            }
        }
    }

    private class TramaProcess extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String tramaCompleta = params[0];
                DispositivoAsociado.DataReceived = new DeviceData(tramaCompleta);
                ActualizarControles(true);
                final SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                if (moto.getInt("IdRecorrido", 0) == 0) {
                    isRecorrido = false;
                } else
                    isRecorrido = true;
                sendBroadcast(setIntentToMap(true));

                if (isRecorrido) {
                    GuardarTransmision(DispositivoAsociado.DataReceived);
                    if (countTramas == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DispositivoAsociado.DataReceived.ReporteId = moto.getInt("IdRecorrido", 0);
                                btnRecorrido.setText("Stop");
                                btnRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
                            }
                        });
                        countTramas = 0;
                    } else {
                        countTramas++;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DispositivoAsociado.DataReceived.ReporteId = 0;
                            btnRecorrido.setText("Iniciar Recorrido");
                            btnRecorrido.setTextColor(Color.WHITE);
                        }
                    });
                }
            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
            return null;
        }
    }

    private void ActualizarControles(final boolean isBluetooth) throws IOException {
        SetProgressBar(((int) DispositivoAsociado.DataReceived.Velocidad));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isBluetooth) {
                    txtReporteDispositivo.setText("Últ. vez " + DispositivoAsociado.DataReceived.FormatDate() + " " + DispositivoAsociado.DataReceived.Hora);
                    tipoTransmisionImagen.setImageResource(R.drawable.bluetoothlogo);
                } else {
                    txtReporteDispositivo.setText("Últ. vez " + DispositivoAsociado.DataReceived.Fecha);
                    tipoTransmisionImagen.setImageResource(R.drawable.web);
                }
                SetImageViews();
                UpdateWidget();
            }
        });
    }

    private void SetImageViews() {

        double battPercent = ((double) (DispositivoAsociado.DataReceived.Bateria));
        double battExtern = (double) (DispositivoAsociado.DataReceived.VoltajeEntrada);

        if (battPercent == 0) {

            imageViewGps.setImageResource(R.drawable.bateria_0);

        }

        if (battPercent <= 2.8 && battPercent > 3) {

            imageViewGps.setImageResource(R.drawable.bateria_12_5);

        }
        if (battPercent <= 3.4 && battPercent > 2.8) {

            imageViewGps.setImageResource(R.drawable.bateria_25);

        }
        if (battPercent <= 3.7 && battPercent > 3.4) {

            imageViewGps.setImageResource(R.drawable.bateria_37_5);

        }

        if (battPercent <= 5 && battPercent > 3.7) {

            imageViewGps.setImageResource(R.drawable.bateria_100);

        }


        if (battExtern == 0) {

            imageViewBateria.setImageResource(R.drawable.bateria_0);

        }

        if (battExtern <= 11.5 && battExtern > 1) {

            imageViewBateria.setImageResource(R.drawable.bateria_25);

        }
        if (battExtern <= 12.8 && battExtern > 11.5) {

            imageViewBateria.setImageResource(R.drawable.bateria_37_5);

        }

        if (battExtern <= 15 && battExtern > 12.8) {

            imageViewBateria.setImageResource(R.drawable.bateria_100);

        }

        txtBattMoto.setText(String.valueOf(DispositivoAsociado.DataReceived.VoltajeEntrada).substring(0, 4) + "v");
        txtBattDispositivo.setText(String.valueOf(DispositivoAsociado.DataReceived.Bateria).substring(0, 3) + "v");
        SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
        if (DispositivoAsociado.DataReceived.Modo == 2) {
            circularImageView.setImageResource((R.drawable.bordeverde));

        } else {
            circularImageView.setImageResource((R.drawable.bordes));
        }
    }

    private boolean IsNewUser() {
        SharedPreferences sharedPref = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
        if (sharedPref.getInt("Id", 0) == 0)
            return true;
        else
            return false;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap drawable, boolean square) {
        int width = 0;
        int height = 0;


        Bitmap bitmap = drawable;

        if (square) {
            if (bitmap.getWidth() < bitmap.getHeight()) {
                width = bitmap.getWidth();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getHeight();
                height = bitmap.getHeight();
            }
        } else {
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(output);

        final int color = Color.BLUE;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final float roundPx = 90;

        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);


        paint.setColor(color);


        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public static Bitmap getRoundedCornerBitmap(Drawable drawable, boolean square) {
        int width = 0;
        int height = 0;
        Paint mBorderPaint;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        if (square) {
            if (bitmap.getWidth() < bitmap.getHeight()) {
                width = bitmap.getWidth();
                height = bitmap.getWidth();
            } else {
                width = bitmap.getHeight();
                height = bitmap.getHeight();
            }
        } else {
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(output);

        final int color = Color.BLUE;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final float roundPx = 90;

        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);


        paint.setColor(color);


        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
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
                if (bluetoothLE.DeviceStatus.equals("Connected")) {
                    startActivity(setIntentToMap(false));
                } else {
                    Intent i = new Intent(getBaseContext(), MapsActivity.class);
                    i.putExtra("Tipo", 3);
                    startActivity(i);
                }
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
        editor.apply();

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
                    btnRecorrido.setText("Stop");
                    btnRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
                    isRecorrido = true;
                } else
                    isRecorrido = false;
            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }
    }

    private void IniciarRecorrido() {
        SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
        SharedPreferences.Editor editor = moto.edit();

        editor.putInt("IdRecorrido", 9999);
        editor.apply();
        DispositivoAsociado.DataReceived.ReporteId = 9999;
        btnRecorrido.setText("Stop");
        btnRecorrido.setTextColor(getResources().getColor(R.color.colorAccent));
        isRecorrido = true;
    }

    private class EnviarRecorrido extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
//            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
//            SharedPreferences user = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
//            SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
//            WebServiceParameter parametro = new WebServiceParameter();
//
//            parametro.Nombre = "IdUser";
//            parametro.Valor = String.valueOf(user.getInt("Id", 0));
//            parameters.add(parametro);
//
//            parametro = new WebServiceParameter();
//            parametro.Nombre = "IdVehiculo";
//            parametro.Valor = String.valueOf(moto.getInt("Id", 0));
//            parameters.add(parametro);
//
//            parametro = new WebServiceParameter();
//            parametro.Nombre = "IdRecorrido";
//            parametro.Valor = String.valueOf(moto.getInt("IdRecorrido", 0));
//            parameters.add(parametro);
//
//            parametro = new WebServiceParameter();
//            parametro.Nombre = "Tramas";
//            parametro.Valor = params[0];
//            parameters.add(parametro);

            SubirArchivoRecorrido fileHelper = new SubirArchivoRecorrido(getBaseContext());

            //WebService.ConexionWS("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/GuardarPuntosRecorrido", parameters);
            if (fileHelper.CrearArchivo())
                return fileHelper.UploadFile();
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Ruta recorrido = GetFromXml(s);
                if (recorrido.Id > 0) {
                    isRecorrido = false;
                    btnRecorrido.setText("IniciarRecorrido");
                    btnRecorrido.setTextColor(Color.WHITE);
                    SharedPreferences moto = getSharedPreferences("Moto", MODE_PRIVATE);
                    recorrido.IdVehiculo = moto.getInt("Id", 0);
                    SharedPreferences.Editor editor = moto.edit();
                    editor.putInt("IdRecorrido", 0);
                    editor.apply();
                    recorridosHelper.InsertRuta(recorridosHelper.getWritableDatabase(), recorrido);
                    transmisionesHelper.UpdateIdRecorrido(transmisionesHelper.getWritableDatabase(), recorrido.Id);
                }

            } catch (Exception e) {
                Log.w("Error", e.toString());
            }
        }
    }

    private Ruta GetFromXml(String stringXml) {
        Ruta rutaToReturn = new Ruta();
        rutaToReturn.Id = Integer.parseInt(stringXml.substring(stringXml.lastIndexOf("<Id>") + 4, stringXml.lastIndexOf("</Id>")));
        rutaToReturn.Descripcion = stringXml.substring(stringXml.lastIndexOf("<Descripcion>") + 13, stringXml.lastIndexOf("</Descripcion>"));
        rutaToReturn.FechaInicio = stringXml.substring(stringXml.lastIndexOf("<FechaInicio>") + 13, stringXml.lastIndexOf("</FechaInicio>"));
        rutaToReturn.FechaInicio = rutaToReturn.FechaInicio.replace("T", " ");
        rutaToReturn.FechaFin = stringXml.substring(stringXml.lastIndexOf("<FechaFin>") + 10, stringXml.lastIndexOf("</FechaFin>"));
        rutaToReturn.FechaFin = rutaToReturn.FechaFin.replace("T", " ");
        rutaToReturn.Distancia = stringXml.substring(stringXml.lastIndexOf("<Distancia>") + 11, stringXml.lastIndexOf("</Distancia>"));
        rutaToReturn.VelMedia = stringXml.substring(stringXml.lastIndexOf("<VelMedia>") + 10, stringXml.lastIndexOf("</VelMedia>"));
        rutaToReturn.VelMax = stringXml.substring(stringXml.lastIndexOf("<VelMax>") + 8, stringXml.lastIndexOf("</VelMax>"));
        return rutaToReturn;
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
        DispositivoAsociado.DataReceived.VehiculoId = moto.getInt("Id", 0);
        transmisionesHelper.InsertTransmision(transmisionesHelper.getWritableDatabase(), transmisionToSave);
    }

    private void TimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        @Override
        public void run() {
            if (bluetoothLE != null) {
                if (bluetoothLE.DeviceStatus.equals("Disconnected")) {
                    new ActualizarDeweb().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerPosicionReciente");
                    return;
                }
                return;
            }
            new ActualizarDeweb().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ObtenerPosicionReciente");
        }
    };
}
