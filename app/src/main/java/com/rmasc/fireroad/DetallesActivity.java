package com.rmasc.fireroad;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by rafaelmartinez on 10/03/16.
 */
public class DetallesActivity extends AppCompatActivity {


    ImageView imageViewBateria, imageViewUser;
    Button btnRecorrido, btnMapa;
    TextView txtKilometraje, txtDistancia, txtVelPro, txtVelMax;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detallehistorico);

        AssignViews();

        File myImageFile = new File("drawable/bateria_0.png");
        Uri myImageUri = Uri.fromFile(myImageFile);

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text("just setting up my Fabric.")
                .image(myImageUri);
        builder.show();


    }




    private void AssignViews() {
        imageViewUser = (ImageView) findViewById(R.id.imageViewUser);


        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireUser";
            File streamImage = new File(path);
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        } catch (Exception e) {
            e.printStackTrace();
            imageViewUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
        }

    }


    private class CargarDetalles extends AsyncTask<String, Void, String> {


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


            }
            catch (Exception e)
            {
            }
        }

    }

}
