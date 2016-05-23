package com.rmasc.fireroad.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.rmasc.fireroad.DataBase.TransmisionesHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class SubirArchivoRecorrido {

    String URLServer;
    File fileToUpload;
    SharedPreferences user;
    SharedPreferences moto;
    Context appContext;

    public SubirArchivoRecorrido(Context context) {
        user = context.getSharedPreferences("User", Context.MODE_PRIVATE);
        moto = context.getSharedPreferences("Moto", Context.MODE_PRIVATE);
        this.appContext = context;
        this.URLServer = "http://gladiatortrackr.com/FireRoadService/MobileService.asmx/UploadFileRecorrido";
    }

    public boolean CrearArchivo() {
        TransmisionesHelper dataBase = new TransmisionesHelper(appContext);
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Recorrido");
            if (!root.exists()) {
                root.mkdirs();
            }
            fileToUpload = new File(root, "Recorrido.txt");
            FileWriter writer = new FileWriter(fileToUpload);
            writer.append(dataBase.SelectTransmision(dataBase.getReadableDatabase(), "VehiculoId = " + moto.getInt("Id", 0) + " AND ReporteId = " + moto.getInt("IdRecorrido", 0), null));
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String UploadFile() {
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(URLServer);
            httppost.addHeader("IdUser", String.valueOf(user.getInt("Id", 0)));
            httppost.addHeader("IdVehiculo", String.valueOf(moto.getInt("Id", 0)));

            FileEntity fileEntity = new FileEntity(fileToUpload, "text/plain");
            fileEntity.setChunked(true);
            httppost.setEntity(fileEntity);

            HttpResponse response = httpclient.execute(httppost);
            InputStream inputStream = response.getEntity().getContent();
            if (inputStream != null) {
                return convertInputStreamToString(inputStream);
            }

            return "Did not work!";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String resultado = "";
        while ((line = bufferedReader.readLine()) != null) {
            resultado += line;
        }
        inputStream.close();
        return resultado;
    }

}
