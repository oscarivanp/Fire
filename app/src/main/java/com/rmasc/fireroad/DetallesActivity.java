package com.rmasc.fireroad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by rafaelmartinez on 10/03/16.
 */
public class DetallesActivity extends AppCompatActivity {

    ImageView imageViewUser;
    Button btnRecorrido, btnMapa;
    TextView txtKilometraje, txtDuracion, txtVelPro, txtVelMax;
    int IdRecorrido = 0;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detallehistorico);
        IdRecorrido = getIntent().getIntExtra("IdRecorrido", 0);
        txtKilometraje = (TextView) findViewById(R.id.txtKilometros);
        txtDuracion = (TextView) findViewById(R.id.txtDuracion);
        txtVelMax = (TextView) findViewById(R.id.txtvelMax);
        txtVelPro = (TextView) findViewById(R.id.txtvelMedia);
        AssignViews();

        if (IdRecorrido != 0) {
            cargarDatosHistorico();
        }
    }


    public void cargarDatosHistorico() {

        new CrearDetalleHistorico().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/DetalleRecorrido");

    }


    private void AssignViews() {
        imageViewUser = (ImageView) findViewById(R.id.imageViewUser);
        btnMapa = (Button) findViewById(R.id.btnMapa);
        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMapa = new Intent(getBaseContext(), MapsActivity.class);
                goToMapa.putExtra("Tipo", 2);
                goToMapa.putExtra("IdRecorrido", IdRecorrido);
                goToMapa.putExtra("IdVehiculo", getIntent().getIntExtra("IdVehiculo", 0));
                startActivity(goToMapa);
            }
        });

        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireMoto";
            File streamImage = new File(path);
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        } catch (Exception e) {
            e.printStackTrace();
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
        }

    }

////Cargar detalles historico

    private class CrearDetalleHistorico extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                JSONObject data = jsonResponse.getJSONObject("d");


                if (data != null) {
                    Date FechaInicio = parseDateTime(data.optString("FechaInicio"));
                    Date FechaFin = parseDateTime(data.optString("FechaFin"));
                    long HorasInicio = FechaInicio.getHours();
                    long MinutosInicio = FechaInicio.getMinutes();
                    long segundosInicio = FechaInicio.getSeconds();

                    long HorasFin = FechaFin.getHours();
                    long MinutosFin = FechaFin.getMinutes();
                    long segundosFin = FechaFin.getSeconds();

                    long difHoras = Math.abs(HorasFin - HorasInicio);
                    long difMinutos = Math.abs(MinutosFin - MinutosInicio);
                    long difSegundos = Math.abs(segundosFin - segundosInicio);


                    txtDuracion.setText(difHoras + ":" + difMinutos + ":" + difSegundos);
                    txtKilometraje.setText(data.optString("Distancia") + " kms");
                    txtVelPro.setText(data.optString("VelMedia") + " kms/h");
                    txtVelMax.setText(data.optString("VelMax") + " kms/h");

                } else {

                    ShowMessage("Error al cargar detalles, intente más tarde.");
                }
            } catch (Exception e) {

            }
        }


        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<>();
            WebServiceParameter parametro = new WebServiceParameter();

            SharedPreferences moto = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(moto.getInt("Id", 0));
            parameters.add(parametro);

            SharedPreferences user = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = String.valueOf(user.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdRecorrido";
            parametro.Valor = String.valueOf(IdRecorrido);
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

    public static long cantidadTotalMinutos(Calendar fechaInicial, Calendar fechaFinal) {
        long totalMinutos;
        totalMinutos = ((fechaFinal.getTimeInMillis() - fechaInicial.getTimeInMillis()) / 1000 / 60);
        return totalMinutos;
    }

    /*Metodo que devuelve el Numero total de horas que hay entre las dos Fechas */
    public static long cantidadTotalHoras(Calendar fechaInicial, Calendar fechaFinal) {
        long totalMinutos;
        totalMinutos = ((fechaFinal.getTimeInMillis() - fechaInicial.getTimeInMillis()) / 1000 / 60 / 60);
        return totalMinutos;
    }


}
