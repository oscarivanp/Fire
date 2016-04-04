package com.rmasc.fireroad;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.Entities.ImagenData;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

public class PerfilActivity extends AppCompatActivity {

    View.OnClickListener buttonClickListener;
    EditText editTextNombre, editTextCorreo, editTextTelefono, editTextApellido, editTexPassword, editTexPasswordConfirmacion;
    Spinner spinnerSexo, spinnerRh;
    ImageButton imageButtonUser;
    public static Button btnFecha, btnRegistrar;

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageButtonUser.setImageDrawable(new RoundImages(ReSizeImage(BitmapFactory.decodeFile(picturePath))));
            imageButtonUser.setScaleType(ImageView.ScaleType.FIT_XY);
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
                        i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                        break;
                    case R.id.btnRegistrar:
                        new EditarUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/EditUser", editTextNombre.getText().toString() + " " + editTextApellido.getText().toString(), editTextTelefono.getText().toString(),
                                spinnerSexo.getSelectedItem().toString(), editTextCorreo.getText().toString(), btnFecha.getText().toString(), spinnerRh.getSelectedItem().toString(),
                                "0", editTextNombre.getText().toString(), editTexPassword.getText().toString());
                        break;
                    case R.id.btnFecha:
                        CalendarPicker calendarioFecha = new CalendarPicker();
                        calendarioFecha.show(getFragmentManager(), "datepicker");
                        break;
                    default:
                        break;
                }
            }
        };

        AssignViews();
        CargarControles();
    }

    private void AssignViews() {
        editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        editTextApellido = (EditText) findViewById(R.id.editTextApellido);
        editTexPassword = (EditText) findViewById(R.id.editTexPassword);
        editTexPasswordConfirmacion = (EditText) findViewById(R.id.editTexPasswordConfirmacion);
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
    }

    private void CargarControles() {
        SharedPreferences userPreferences = getBaseContext().getSharedPreferences("User", MODE_PRIVATE);
        editTextNombre.setText(userPreferences.getString("Nombres", ""));
        editTextApellido.setText(userPreferences.getString("Apellidos", ""));
        editTextCorreo.setText(userPreferences.getString("Correo", ""));
        editTextTelefono.setText(userPreferences.getString("Telefono", ""));
        ArrayAdapter adapterRh = ArrayAdapter.createFromResource(getBaseContext(), R.array.RhItems, R.layout.activity_perfil);
        spinnerRh.setSelection(adapterRh.getPosition(userPreferences.getString("RH", "A+")));
        adapterRh = ArrayAdapter.createFromResource(getBaseContext(), R.array.GeneroItems, R.layout.activity_perfil);
        spinnerSexo.setSelection(adapterRh.getPosition(userPreferences.getString("Sexo", "Masculino")));
        btnFecha.setText(userPreferences.getString("FechaNacimiento", "Ingresa tu cumpleaños"));
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
                    editor.putString("Apellidos", editTextApellido.getText().toString());
                    editor.putString("Telefono", editTextTelefono.getText().toString());
                    editor.putString("Sexo", spinnerSexo.getSelectedItem().toString());
                    editor.putString("Correo", editTextCorreo.getText().toString());
                    editor.putString("FechaNacimiento", btnFecha.getText().toString());
                    editor.putString("RH", spinnerRh.getSelectedItem().toString());
                    editor.putString("IdTwitter", "0");
                    editor.putString("UserLogin", editTextNombre.getText().toString());
                    editor.commit();
                    startActivity(goToMain);
                    finish();
                } else {
                    editor.putInt("Id", 0);
                    editor.commit();
                    ShowMessage("Error al registrar, intente más tarde.");
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
            parametro.Valor = params[9];
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
