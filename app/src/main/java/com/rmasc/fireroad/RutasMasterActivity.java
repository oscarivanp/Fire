package com.rmasc.fireroad;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RutasAdapter;
import com.rmasc.fireroad.Adapters.ViewHolder;
import com.rmasc.fireroad.Entities.Ruta;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.R;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RutasMasterActivity extends AppCompatActivity {

    public ListView listViewRutas;
    public TextView textViewTitulo;
    public ArrayList<Ruta> MisRutas = new ArrayList<Ruta>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas_master);

        AssignViews();
        new CargarRutas().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ListarRecorridos");
    }

    private void AssignViews()
    {
        listViewRutas = (ListView) findViewById(R.id.listViewRutas);
        textViewTitulo = (TextView) findViewById(R.id.textViewTitulo);
        listViewRutas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder holder = (ViewHolder) view.getTag();
                ShowMessage("Id del item " + holder.Id);
            }
        });
    }

    private class CargarRutas extends AsyncTask<String, Void, String>
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
                    Ruta rutaTemp = new Ruta();
                    rutaTemp.Id = recoTemp.optInt("Id");
                    rutaTemp.Descripcion = recoTemp.optString("Descripcion");
                    rutaTemp.FechaInicio = recoTemp.optString("FechaInicio");
                    rutaTemp.FechaInicio = recoTemp.optString("FechaFin");
                    MisRutas.add(rutaTemp);
                }
                listViewRutas.setAdapter(new RutasAdapter( getBaseContext(), MisRutas));

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
