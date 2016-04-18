package com.rmasc.fireroad;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Calendar;

public class PerfilActivity extends AppCompatActivity {

    View.OnClickListener buttonClickListener;
    EditText editTextNombre, editTextCorreo, editTextTelefono, editTexPassword, editTexPasswordConfirmacion, editTexPasswordAntigua;
    Spinner spinnerSexo, spinnerRh;
    ImageButton imageButtonUser;
    TextView textViewContra, textViewTitulo, pass1, pass2;
    public static Button btnFecha, btnRegistrar, btnCambiarContraseña;
    private String APP_DIRECTORY = "myPictureApp/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";
    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;


    public  static String contraseña = "";

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case PHOTO_CODE:
                if (resultCode == RESULT_OK) {
                    String dir = Environment.getExternalStorageDirectory() + File.separator
                            + MEDIA_DIRECTORY + File.separator + TEMPORAL_PICTURE_NAME;
                    decodeBitmap(dir, "User");
                }
                break;

            case SELECT_PICTURE:
                if(resultCode == RESULT_OK){

                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        decodeBitmap(bitmap, "User");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
      }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                switch (v.getId()) {
                    case R.id.imageButtonUser:

                        final CharSequence[] options = {"Tomar foto", "Elegir de galeria", "Cancelar"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
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

                        break;
                    case R.id.btnRegistrar:
                        if (editTexPasswordAntigua.getVisibility() == View.VISIBLE)
                        {
                            if (VerificarPass(editTexPassword.getText().toString(), editTexPasswordConfirmacion.getText().toString()) && VerificarPass(editTexPasswordAntigua.getText().toString(), contraseña) )
                            {
                                contraseña = editTexPassword.getText().toString();
                                new EditarUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/EditUser", editTextNombre.getText().toString(), editTextTelefono.getText().toString(),
                                        spinnerSexo.getSelectedItem().toString(), editTextCorreo.getText().toString(), btnFecha.getText().toString(), spinnerRh.getSelectedItem().toString(),
                                        "0", editTextNombre.getText().toString());
                            }
                            ShowMessage("Las contraseñas no coinciden.");
                        }
                        else
                        {
                            new EditarUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/EditUser", editTextNombre.getText().toString(), editTextTelefono.getText().toString(),
                                    spinnerSexo.getSelectedItem().toString(), editTextCorreo.getText().toString(), btnFecha.getText().toString(), spinnerRh.getSelectedItem().toString(),
                                    "0", editTextNombre.getText().toString());
                        }

                        break;
                    case R.id.btnFecha:
                        CalendarPicker calendarioFecha = new CalendarPicker();
                        calendarioFecha.show(getFragmentManager(), "datepicker");
                        break;
                    case R.id.btnCambiarContraseña:
                        textViewContra.setVisibility(View.VISIBLE);
                        pass1.setVisibility(View.VISIBLE);
                        pass2.setVisibility(View.VISIBLE);
                        editTexPassword.setVisibility(View.VISIBLE);
                        editTexPasswordConfirmacion.setVisibility(View.VISIBLE);
                        editTexPasswordAntigua.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        };

        AssignViews();
        CargarControles();
    }

    private boolean VerificarPass(String pass1, String pass2)
    {
        if (pass1.equals(pass2))
            return true;
        else
            return false;
    }



    private void decodeBitmap(String dir, String tipo) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);

        if (tipo.equals("User")) {
            RoundImages imaghenFace = new RoundImages(bitmap);
            Bitmap imagenProcesada = imaghenFace.RoundImages(bitmap, 200, 200);
            imageButtonUser.setImageBitmap(imagenProcesada);

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


    private void decodeBitmap(Bitmap dir, String tipo) {


        if (tipo.equals("User")) {
            RoundImages imaghenFace = new RoundImages(dir);
            Bitmap imagenProcesada = imaghenFace.RoundImages(dir, 200, 200);
            imageButtonUser.setImageBitmap(imagenProcesada);

        }
    }


    private void AssignViews() {
        editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        editTexPassword = (EditText) findViewById(R.id.editTexPassword);
        editTexPassword.setVisibility(View.GONE);
        editTexPasswordConfirmacion = (EditText) findViewById(R.id.editTexPasswordConfirmacion);
        editTexPasswordConfirmacion.setVisibility(View.GONE);
        editTexPasswordAntigua = (EditText) findViewById(R.id.editTexPasswordAntigua);
        pass1 = (TextView) findViewById(R.id.pass1);
        pass1.setVisibility(View.GONE);
        pass2 = (TextView) findViewById(R.id.pass2);
        pass2.setVisibility(View.GONE);
        textViewContra = (TextView) findViewById(R.id.textViewContra);
        textViewTitulo = (TextView) findViewById(R.id.textViewTitulo);
        textViewTitulo.setText("Editar Perfil");
        spinnerRh = (Spinner) findViewById(R.id.spinnerRh);
        spinnerSexo = (Spinner) findViewById(R.id.spinnerSexo);
        editTextCorreo = (EditText) findViewById(R.id.editTextCorreo);
        editTextTelefono = (EditText) findViewById(R.id.editTextTelefono);
        imageButtonUser = (ImageButton) findViewById(R.id.imageButtonUser);
        imageButtonUser.setOnClickListener(buttonClickListener);
        btnFecha = (Button) findViewById(R.id.btnFecha);
        btnFecha.setOnClickListener(buttonClickListener);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        btnRegistrar.setText("Guardar");
        btnRegistrar.setOnClickListener(buttonClickListener);
        btnCambiarContraseña = (Button) findViewById(R.id.btnCambiarContraseña);
        btnCambiarContraseña.setVisibility(View.VISIBLE);
        btnCambiarContraseña.setOnClickListener(buttonClickListener);
    }

    private void CargarControles() {
        SharedPreferences userPreferences = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
        editTextNombre.setText(userPreferences.getString("Nombres", ""));
        editTextCorreo.setText(userPreferences.getString("Correo", ""));
        editTextTelefono.setText(userPreferences.getString("Telefono", ""));
        ArrayAdapter adapterRh = ArrayAdapter.createFromResource(getBaseContext(), R.array.RhItems, R.layout.activity_perfil);
        spinnerRh.setSelection(adapterRh.getPosition(userPreferences.getString("RH", "A+")));
        adapterRh = ArrayAdapter.createFromResource(getBaseContext(), R.array.GeneroItems, R.layout.activity_perfil);
        spinnerSexo.setSelection(adapterRh.getPosition(userPreferences.getString("Sexo", "Masculino")));
        btnFecha.setText(userPreferences.getString("FechaNacimiento", "Ingresa tu cumpleaños"));
        contraseña = userPreferences.getString("Contraseña", "");
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/FireUser";
            File streamImage = new File(path);
            imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeStream(new FileInputStream(streamImage))));
        } catch (Exception e) {
            e.printStackTrace();
            imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
        }

    }

    private Bitmap ReSizeImage(Bitmap imagen) {
        if (imagen.getHeight() <= 200 && imagen.getWidth() <= 200)
            return imagen;
        else if (imagen.getHeight() <= 500 && imagen.getWidth() <= 500)
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth() * (0.5)), (int) (imagen.getHeight() * (0.5)), true);
        else if (imagen.getHeight() <= 750 && imagen.getWidth() <= 750)
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth() * (0.2)), (int) (imagen.getHeight() * (0.2)), true);
        else
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth() * (0.1)), (int) (imagen.getHeight() * (0.1)), true);
    }

    public static class CalendarPicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public int anio = Calendar.getInstance().get(Calendar.YEAR);
        public int mes = Calendar.getInstance().get(Calendar.MONTH);
        public int dia = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog dialogDatePicker = new DatePickerDialog(getActivity(), this, anio, mes, dia);
            dialogDatePicker.getDatePicker().setCalendarViewShown(true);
            return dialogDatePicker;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            btnFecha.setText(dayOfMonth + "/" + monthOfYear + "/" + year);
        }
    }

    private class EditarUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String params) {
            try {
                JSONObject jsonResponse = new JSONObject(params);
                String IdUser = jsonResponse.optString("d");

                SharedPreferences user = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                Intent goToMain = new Intent(getBaseContext(), MainActivity.class);

                if (IdUser.equals("true")) {
                    editor.putString("Nombres", editTextNombre.getText().toString());
                    editor.putString("Telefono", editTextTelefono.getText().toString());
                    editor.putString("Sexo", spinnerSexo.getSelectedItem().toString());
                    editor.putString("Correo", editTextCorreo.getText().toString());
                    editor.putString("FechaNacimiento", btnFecha.getText().toString());
                    editor.putString("RH", spinnerRh.getSelectedItem().toString());
                    editor.putString("Contraseña", editTexPassword.getText().toString());
                    editor.putString("IdTwitter", "0");
                    editor.putString("UserLogin", editTextNombre.getText().toString());
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

            SharedPreferences user = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
            parametro.Nombre = "Id";
            parametro.Valor = String.valueOf(user.getInt("Id", 0));
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Nombres";
            parametro.Valor = params[1];
            parameters.add(parametro);

            parametro = new WebServiceParameter(); // Si no se reinicia genera error.
            parametro.Nombre = "Telefono";
            parametro.Valor = params[2];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Sexo";
            if (params[3].equals("Masculino"))
                parametro.Valor = String.valueOf(1);
            else
                parametro.Valor = String.valueOf(2);
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Correo";
            parametro.Valor = params[4];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "FechaNacimiento";
            parametro.Valor = params[5];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "RH";
            parametro.Valor = params[6];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "FotoPath";
            try {
                String path = Environment.getExternalStorageDirectory().toString() + "/FireUser";
                File streamImage = new File(path);
                parametro.Valor = new ImagenData(BitmapFactory.decodeStream(new FileInputStream(streamImage))).content;
            } catch (Exception e) {
                parametro.Valor = "No imagen";
            }
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdFacebook";
            parametro.Valor = user.getString("IdFacebook", "0");
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdTwitter";
            parametro.Valor = params[7];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "UserLogin";
            parametro.Valor = params[8];
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Password";
            parametro.Valor = contraseña;
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
