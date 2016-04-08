package com.rmasc.fireroad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.VehiculosAdapter;
import com.rmasc.fireroad.Adapters.ViewHolder;
import com.rmasc.fireroad.Entities.Vehiculo;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by rafaelmartinez on 10/03/16.
 */
public class listMotos extends AppCompatActivity {


    public ListView listViewMotos;
    public TextView textViewTituloMotos;
    public ArrayList<Vehiculo> MisMotos = new ArrayList<Vehiculo>();
    Intent goToPageMoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listmotos);
        AssignViews();
        new CargarMotos().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/obtenerVehiculos");
    }

    private void AssignViews()
    {
        listViewMotos = (ListView) findViewById(R.id.listViewMotos);
        textViewTituloMotos = (TextView) findViewById(R.id.textViewTituloMotos);
        listViewMotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder holder = (ViewHolder) view.getTag();
                ShowMessage("Id del item " + holder.Id);

                goToPageMoto = new Intent(getBaseContext(), RutasMasterActivity.class);
                goToPageMoto.putExtra("IdMoto", holder.Id);
                startActivity(goToPageMoto);

            }
        });
    }

    private class CargarMotos extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();
            SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);

            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(userPref.getInt("Id", 0));
            parameters.add(parametro);

            return WebService.ConexionWS(params[0], parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try
            {
                JSONObject dataRe = new JSONObject(s);
                JSONArray recorridos = dataRe.optJSONArray("d");

                for (int i = 0; i < recorridos.length(); i++)
                {
                    JSONObject recoTemp = recorridos.optJSONObject(i);
                    Vehiculo moto = new Vehiculo();
                    moto.Id = recoTemp.optInt("Id");
                    moto.Marca = recoTemp.optString("Marca");
                    moto.FotoPath=recoTemp.optString("FotoPath");
                    moto.Placa = recoTemp.optString("Placa");
                    MisMotos.add(moto);
                }
                listViewMotos.setAdapter(new VehiculosAdapter( getBaseContext(), MisMotos));

            }
            catch (Exception e)
            {
            }
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
