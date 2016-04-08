package com.rmasc.fireroad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.ExpandableListAdapter;
import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.Adapters.ViewHolder;
import com.rmasc.fireroad.Entities.Ruta;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RutasMasterActivity extends AppCompatActivity {

    TextView RecorridoTotales;
    TextView DuracionTotales;
    TextView kilometrosTotales;
    int totalRecorrido=0;
    int totalHoras=0;
    int totalminutos=0;
    int totalsegundos=0;
    int totalKilometros=0;
    ImageView imageViewUser;
    ExpandableListView expandableListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> expandableListDetail;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    ArrayList<String> listFechas;
    public TextView textViewTitulo;
    public ArrayList<Ruta> MisRutas = new ArrayList<Ruta>();
    Intent goToPageDetalles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas_master);
        kilometrosTotales= (TextView) findViewById(R.id.txtKilometros);
        DuracionTotales= (TextView) findViewById(R.id.txtDuraconRecorrido);
        RecorridoTotales= (TextView) findViewById(R.id.txtRecorridos);
        listFechas= new ArrayList<String>();
        new CargarRutas().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/ListarRecorridos");
        expandableListView = (ExpandableListView) findViewById(R.id.lvExp);
        // preparing list data
        imageViewUser=(ImageView) findViewById(R.id.image_viewRecorrido);
        imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
       // AssignViews();


    }


    private class CargarRutas extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            SharedPreferences userPref = getSharedPreferences("User", MODE_PRIVATE);
            parametro.Nombre = "IdVehiculo";
            parametro.Valor = String.valueOf(getIntent().getIntExtra("IdMoto", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
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
                    rutaTemp.FechaFin = recoTemp.optString("FechaFin");
                    rutaTemp.Distancia=recoTemp.optString("Distancia");
                    MisRutas.add(rutaTemp);

                    final Calendar c = Calendar.getInstance();

                    c.setTime(parseDateTime(recoTemp.optString("FechaInicio")));

                  if(!listFechas.contains(obtenerMesNombre(c.get(Calendar.MONTH))+" / "+String.valueOf(c.get(Calendar.YEAR)))) {

                      listFechas.add(obtenerMesNombre(c.get(Calendar.MONTH))+" / "+String.valueOf(c.get(Calendar.YEAR)));

                }

            }
           //     listViewRutas.setAdapter(new RutasAdapter( getBaseContext(), MisRutas));

                expandableListDetail = prepareListData(listFechas,MisRutas);
                expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
                expandableListAdapter = new ExpandableListAdapter(getBaseContext(), expandableListTitle, expandableListDetail);
                expandableListView.setAdapter(expandableListAdapter);
                expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                    @Override
                    public void onGroupExpand(int groupPosition) {
                        Toast.makeText(getApplicationContext(),
                                expandableListTitle.get(groupPosition) + " List Expanded.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                    @Override
                    public void onGroupCollapse(int groupPosition) {
                        Toast.makeText(getApplicationContext(),
                                expandableListTitle.get(groupPosition) + " List Collapsed.",
                                Toast.LENGTH_SHORT).show();

                    }
                });

                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView parent, View v,
                                                int groupPosition, int childPosition, long id) {
                        ViewHolder holder = (ViewHolder) v.getTag();
                        ShowMessage("Id del item " + holder.Id);
                        goToPageDetalles = new Intent(getBaseContext(), DetallesActivity.class);
                        goToPageDetalles.putExtra("IdRecorrido", holder.Id);
                        goToPageDetalles.putExtra("IdVehiculo", getIntent().getIntExtra("IdMoto", 0));
                        startActivity(goToPageDetalles);
                        return false;
                    }
                });

            }
            catch (Exception e)
            {
            }
            DuracionTotales.setText(String.valueOf(totalHoras)+":"+String.valueOf(totalminutos)+":"+String.valueOf(totalsegundos));
            kilometrosTotales.setText(totalKilometros + " kms");
            RecorridoTotales.setText(String.valueOf(totalRecorrido));

        }
    }

    private String obtenerMesNombre(int mes)
    {
        String mesNombre;

        switch(mes){
            case 0:
            {
                mesNombre="Enero";
                break;
            }
            case 1:
            {
                mesNombre="Febrero";
                break;
            }
            case 2:
            {
                mesNombre="Marzo";
                break;
            }
            case 3:
            {
                mesNombre="Abril";
                break;
            }
            case 4:
            {
                mesNombre="Mayo";
                break;
            }
            case 5:
            {
                mesNombre="Junio";
                break;
            }
            case 6:
            {
                mesNombre="Julio";
                break;
            }
            case 7:
            {
                mesNombre="Agosto";
                break;
            }
            case 8:
            {
                mesNombre="Septiembre";
                break;
            }
            case 9:
            {
                mesNombre="Octubre";
                break;
            }
            case 10:
            {
                mesNombre="Noviembre";
                break;
            }
            case 11:
            {
                mesNombre="Diciembre";
                break;
            }
            default:
            {
                mesNombre="Error";
                break;
            }
        }

        return mesNombre;


    }
    private void ShowMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Date parseDateTime(String lastModified) {
        Date date = null;
        if (lastModified != null && lastModified.length() > 0) {
            try {
                lastModified = lastModified.replace("/Date(","");
                lastModified = lastModified.replace(")/", "");
                date = new Date(Long.parseLong(lastModified));
            } catch (Exception e) {
                // otherwise we just leave it empty
            }
        }
        return date;
    }


    private  HashMap<String, List<String>> prepareListData(ArrayList<String> Fechas, ArrayList<Ruta> Recorridos) {

        expandableListDetail =  new HashMap<String, List<String>>();

        for(int x=0; x<Fechas.size();x++) {
             List<String> lista  = new ArrayList<String>();

            for (int y = 0; y < Recorridos.size(); y++) {
                final Calendar c = Calendar.getInstance();

                Date FechaInicio=parseDateTime(Recorridos.get(y).FechaInicio);
                Date FechaFin=parseDateTime(Recorridos.get(y).FechaFin);
                long HorasInicio= FechaInicio.getHours();
                long MinutosInicio= FechaInicio.getMinutes();
                long segundosInicio= FechaInicio.getSeconds();

                long HorasFin= FechaFin.getHours();
                long MinutosFin= FechaFin.getMinutes();
                long segundosFin= FechaFin.getSeconds();

                long difHoras=Math.abs(HorasFin-HorasInicio);
                long difMinutos=Math.abs(MinutosFin-MinutosInicio);
                long difSegundos=Math.abs(segundosFin-segundosInicio);

                c.setTime(parseDateTime(Recorridos.get(y).FechaInicio));

               if ((obtenerMesNombre(c.get(Calendar.MONTH))+" / "+String.valueOf(c.get(Calendar.YEAR))).equals(Fechas.get(x))) {
                    lista.add(String.valueOf(c.get((Calendar.DAY_OF_MONTH)))+"/"+obtenerMesNombre(c.get((Calendar.MONTH)))+"-"+Recorridos.get(y).Id+"-"+Recorridos.get(y).Distancia+"-"+difHoras+":"+difMinutos+":"+difSegundos);
                }

                totalHoras+=difHoras;
               if( totalminutos+difMinutos>59){

                   totalHoras=totalHoras+1;
                   totalminutos=(int)(long)(59-(difMinutos-totalminutos));

               }
                else
               {
                   totalminutos+=difMinutos;

               }

                if( totalsegundos+difSegundos>59){

                    totalminutos=totalminutos+1;
                    totalsegundos=(int)(long)(60-(totalsegundos-difSegundos));
                }
                else {

                    totalsegundos+=difSegundos;

                }

                totalKilometros+=Integer.parseInt(Recorridos.get(y).Distancia);
            }
            totalRecorrido=Recorridos.size();

            expandableListDetail.put(Fechas.get(x), lista);
        }

        return expandableListDetail;
    }

}
