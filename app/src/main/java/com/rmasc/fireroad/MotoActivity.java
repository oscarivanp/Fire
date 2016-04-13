package com.rmasc.fireroad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.Entities.ImagenData;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MotoActivity extends AppCompatActivity {

    private static View.OnClickListener buttonClickListener;
    private String APP_DIRECTORY = "myPictureApp/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";
    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;
    private boolean imagUser = false, imagMoto = false;
    private boolean imagenCargada=false;
    private static EditText editTextMarca, editTextPlaca, editTextColor, editTextModelo, editTextMacBlue;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode){
                case PHOTO_CODE:
                    if(resultCode == RESULT_OK){
                        String dir =  Environment.getExternalStorageDirectory() + File.separator
                                + MEDIA_DIRECTORY + File.separator + TEMPORAL_PICTURE_NAME;
                        decodeBitmap(dir,"Moto");
                    }
                    break;

                case SELECT_PICTURE:
                    if(resultCode == RESULT_OK){
                        Uri imageUri = data.getData();
                        try {


                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            decodeBitmap(bitmap, "Moto");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
            }

            imagMoto = false;


    }

    private void decodeBitmap(Bitmap dir, String tipo) {


         if(tipo.equals("Moto")){

            RoundImages imaghenFace = new RoundImages(dir);
            Bitmap imagenProcesada = imaghenFace.RoundImages(dir, 200, 200);
            imageButtonMoto.setImageBitmap(imagenProcesada);

        }

    }


    private static ImageButton imageButtonMoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moto);
        imageButtonMoto = (ImageButton) findViewById(R.id.imageButtonMoto);
        imageButtonMoto.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_moto)));
        editTextMarca = (EditText) findViewById(R.id.editTextMarca);
        editTextPlaca = (EditText) findViewById(R.id.editTextPlaca);
        editTextColor = (EditText) findViewById(R.id.editTextColor);
        editTextModelo = (EditText) findViewById(R.id.editTextModelo);
        editTextMacBlue = (EditText)findViewById(R.id.editTextMacBlue);

        CargarControles();
    }


    private void CargarControles() {
        SharedPreferences motoPreferences = getBaseContext().getSharedPreferences("Moto", MODE_PRIVATE);
        editTextMarca.setText(motoPreferences.getString("Marca", ""));
        editTextPlaca.setText(motoPreferences.getString("Placa", ""));
        editTextColor.setText(motoPreferences.getString("Color", ""));
        editTextModelo.setText(motoPreferences.getString("Modelo", ""));
       editTextMacBlue.setText(motoPreferences.getString("MacBluetooth",""));


        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireMoto";
            File streamImage = new File(path);
            imageButtonMoto.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        } catch (Exception e) {
            e.printStackTrace();
            imageButtonMoto.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_moto)));
        }

    }

    public void CambiarFoto(View v){


        imagUser = true;
        imagMoto = !imagUser;

        final CharSequence[] options = {"Tomar foto", "Elegir de galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MotoActivity.this);
        builder.setTitle("Elige una opcion");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int seleccion) {
                if (options[seleccion] == "Tomar foto") {
                    openCamera();
                } else if (options[seleccion] == "Elegir de galeria") {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                } else if (options[seleccion] == "Cancelar") {
                    dialog.dismiss();
                }
            }
        });
        builder.show();


    }


    private void decodeBitmap(String dir, String tipo) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);

        if(tipo.equals("Moto")){

            RoundImages imaghenFace = new RoundImages(bitmap);
            Bitmap imagenProcesada = imaghenFace.RoundImages(bitmap, 200, 200);
            imageButtonMoto.setImageBitmap(imagenProcesada);

        }

    }




    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();

        String path = Environment.getExternalStorageDirectory() + File.separator
                + MEDIA_DIRECTORY + File.separator + TEMPORAL_PICTURE_NAME;

        File newFile = new File(path);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(intent, PHOTO_CODE);
    }

    public void guardar(View view){

        new EditarMoto().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/EditarVehiculo", editTextMarca.getText().toString(), editTextPlaca.getText().toString(), editTextColor.getText().toString(), editTextModelo.getText().toString(), editTextMacBlue.getText().toString());

    }

    private class EditarMoto extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                String isOk = jsonResponse.optString("d");

                SharedPreferences motoMain = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = motoMain.edit();
                Intent goToMain = new Intent(getBaseContext(), MainActivity.class);

                if (isOk.equals("true")) {

                    editor.putString("Marca", editTextMarca.getText().toString());
                    editor.putString("Placa", editTextPlaca.getText().toString());
                    editor.putString("Color", editTextColor.getText().toString());
                    editor.putString("Modelo", editTextModelo.getText().toString());
                    editor.putString("MacBluetooth", editTextMacBlue.getText().toString());
                    editor.putString("NombreBluetooth", "FireRoad-RM");
                    editor.apply();
                    startActivity(goToMain);
                    finish();
                }
            } catch (Exception e) {

            }
        }

        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            SharedPreferences moto = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
            parametro.Nombre = "Id";
            parametro.Valor = String.valueOf(moto.getInt("Id", 0));
            parameters.add(parametro);

            SharedPreferences user = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdUser";
            parametro.Valor = String.valueOf(user.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Foto";
            try {
                String path = Environment.getExternalStorageDirectory().toString() + "/FireMoto";
                File streamImage = new File(path);
                parametro.Valor = new ImagenData(BitmapFactory.decodeStream(new FileInputStream(streamImage))).content;
                parameters.add(parametro);
            } catch (Exception e) {
                parametro.Valor = "NA";
                parameters.add(parametro);
            }

            parametro = new WebServiceParameter();
            parametro.Nombre = "Marca";
            parametro.Valor = params[1];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Placa";
            parametro.Valor = params[2];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Color";
            parametro.Valor = params[3];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Modelo";
            parametro.Valor = params[4];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "MacAdress";
            parametro.Valor = params[5];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "NombreBlue";
            parametro.Valor = "FireRoad-RM";
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



}
