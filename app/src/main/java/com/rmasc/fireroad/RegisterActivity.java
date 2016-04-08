package com.rmasc.fireroad;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.BluetoothLe.BluetoothLE;
import com.rmasc.fireroad.Entities.ImagenData;
import com.rmasc.fireroad.Entities.JsonParser;
import com.rmasc.fireroad.Entities.WebServiceParameter;
import com.rmasc.fireroad.Services.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {


    private boolean imagUser = false, imagMoto = false;
    private static boolean imagenCargada = false;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ProgressBar progressBar;
    private static EditText editTextNombre, editTextApellido, editTextCorreo, editTextTelefono, editTexPassword, editTexPasswordConfirmacion, editTextMarca, editTextPlaca, editTextColor, editTextModelo, editTextMacBlue;
    private static TextView textViewResumen;
    private static ImageButton imageButtonUser, imageButtonMoto;
    private static Button btnScan, btnPoliticas, btnFinish, btnFecha;
    private static ListView listVDevices;
    private static View.OnClickListener buttonClickListener;
    private static AdapterView.OnItemClickListener itemClickListener;
    private static Spinner spinnerSexo, spinnerRh;

    private static int IdUser = 0;
    SharedPreferences sharedPref;
    String tipoLogin = "";

    private static String userNameTwitter;
    private static String urlTwitterProfile;

    ImageView imageView;

    private static int RESULT_LOAD_IMAGE = 1;
    private String[] objetos = new String[6];
    private String idUserFacebook = "0";
    private String tokenNumero;
    private String urlFcebook;
    private String urlFacebookProfile;
    JSONObject jsonObjectTexts, jsonObjectPicture;
    private String APP_DIRECTORY = "myPictureApp/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String TEMPORAL_PICTURE_NAME = "temporal.jpg";
    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;

    BluetoothLE bluetoothLE;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (imagUser) {


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

                imagUser = false;
            } else {
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

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        StrictMode.ThreadPolicy stream = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(stream);

        if (getIntent().getExtras() != null) {

            tipoLogin = getIntent().getExtras().getString("TipoLogin");

        }

        if (tipoLogin.equals("twitter")) {
            userNameTwitter = getIntent().getExtras().getString("UserName");
            urlTwitterProfile = "https://twitter.com/" + userNameTwitter + "/profile_image?size=original";
        }

        if (tipoLogin.equals("facebook")) {

            AccessToken token = AccessToken.getCurrentAccessToken();
            idUserFacebook = token.getUserId();
            if (token.isExpired()) {
                AccessToken.refreshCurrentAccessTokenAsync();
                token = AccessToken.getCurrentAccessToken();

            }

            tokenNumero = token.getToken();
            urlFcebook = "https://graph.facebook.com/" + idUserFacebook + "?fields=id,name,first_name,birthday,last_name,middle_name,email,picture&access_token=" + tokenNumero;
            urlFacebookProfile = "http://graph.facebook.com/" + idUserFacebook + "/picture?redirect=0&type=large";
            new ValidarUser().execute(urlFcebook, urlFacebookProfile);


        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                SetPageIndicator(position + 1);
                if ((position + 1) == 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ScanBluetooth();
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SaveDevice(view);
            }
        };

        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                switch (v.getId()) {
                    case R.id.imageButtonUser:
                        imagUser = true;
                        imagMoto = !imagUser;

                        final CharSequence[] options = {"Tomar foto", "Elegir de galeria", "Cancelar"};
                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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
                    case R.id.imageButtonMoto:
                        imagMoto = true;
                        imagUser = !imagMoto;

                        final CharSequence[] optionsMoto = {"Tomar foto", "Elegir de galeria", "Cancelar"};
                        final AlertDialog.Builder builderMoto = new AlertDialog.Builder(RegisterActivity.this);
                        builderMoto.setTitle("Elige una opcion");
                        builderMoto.setItems(optionsMoto, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int seleccion) {
                                if (optionsMoto[seleccion] == "Tomar foto") {
                                    openCamera();
                                } else if (optionsMoto[seleccion] == "Elegir de galeria") {
                                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                                } else if (optionsMoto[seleccion] == "Cancelar") {
                                    dialog.dismiss();
                                }
                            }
                        });
                        builderMoto.show();


                        break;
                    case R.id.btnFinish:
                        imageButtonUser.buildDrawingCache();
                        Bitmap imagen = imageButtonUser.getDrawingCache();
                        if (ComparePassword()) {
                            if (SaveImage("FireUser", imagen)) {
                                imageButtonMoto.buildDrawingCache();
                                imagen = imageButtonMoto.getDrawingCache();
                                if (SaveImage("FireMoto", imagen)) {
                                    if (IdUser == 0) {
                                        new CrearUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/CreateUser", editTextNombre.getText().toString() + " " + editTextApellido.getText().toString(), editTextTelefono.getText().toString(),
                                                spinnerSexo.getSelectedItem().toString(), editTextCorreo.getText().toString(), btnFecha.getText().toString(), spinnerRh.getSelectedItem().toString(),
                                                idUserFacebook, editTextNombre.getText().toString(), editTexPassword.getText().toString());
                                    } else {
                                        new EditarUsuario().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/EditUser", editTextNombre.getText().toString() + " " + editTextApellido.getText().toString(), editTextTelefono.getText().toString(),
                                                spinnerSexo.getSelectedItem().toString(), editTextCorreo.getText().toString(), btnFecha.getText().toString(), spinnerRh.getSelectedItem().toString(),
                                                "0", editTextNombre.getText().toString(), editTexPassword.getText().toString());
                                    }
                                } else
                                    ShowMessage("Error al guardar la imagen de la moto.");
                            } else
                                ShowMessage("Error al guardar la imagen de usuario.");
                        }
                        break;
                    case R.id.btnPoliticas:
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

        AssignControls();



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


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;


            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.register_user, container, false);
                    AssignStaticControls(getArguments().getInt(ARG_SECTION_NUMBER), rootView, getContext());
                    break;


                case 2:
                    rootView = inflater.inflate(R.layout.register_moto, container, false);
                    AssignStaticControls(getArguments().getInt(ARG_SECTION_NUMBER), rootView, getContext());

                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.register_review, container, false);
                    AssignStaticControls(getArguments().getInt(ARG_SECTION_NUMBER), rootView, getContext());
                    break;
                default:
                    break;
            }

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    public void SetPageIndicator(int position) {
        progressBar.setProgress(position);
    }

    public void AssignControls() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    public static void AssignStaticControls(int position, View view, Context context) {
        switch (position) {
            case 1:
                editTextNombre = (EditText) view.findViewById(R.id.editTextNombre);
                editTextApellido = (EditText) view.findViewById(R.id.editTextApellido);
                editTextCorreo = (EditText) view.findViewById(R.id.editTextCorreo);
                editTextTelefono = (EditText) view.findViewById(R.id.editTextTelefono);
                imageButtonUser = (ImageButton) view.findViewById(R.id.imageButtonUser);
                editTexPassword = (EditText) view.findViewById(R.id.editTexPassword);
                editTexPasswordConfirmacion = (EditText) view.findViewById(R.id.editTexPasswordConfirmacion);

                if (!imagenCargada) {

                    imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_user)));
                }
                imageButtonUser.setOnClickListener(buttonClickListener);

                editTextNombre.setText(userNameTwitter);

                try {
                    InputStream prueba = new URL(urlTwitterProfile).openStream();
                    Bitmap foto = BitmapFactory.decodeStream(prueba);
                    RoundImages imaghenFace = new RoundImages(foto);
                    Bitmap imagenProcesada = imaghenFace.RoundImages(foto, 200, 200);
                    imageButtonUser.setImageBitmap(imagenProcesada);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                spinnerSexo = (Spinner) view.findViewById(R.id.spinnerSexo);
                spinnerRh = (Spinner) view.findViewById(R.id.spinnerRh);

                btnFecha = (Button) view.findViewById(R.id.btnFecha);
                btnFecha.setOnClickListener(buttonClickListener);
                break;
            case 2:
                editTextMarca = (EditText) view.findViewById(R.id.editTextMarca);
                editTextPlaca = (EditText) view.findViewById(R.id.editTextPlaca);
                editTextColor = (EditText) view.findViewById(R.id.editTextColor);
                editTextModelo = (EditText) view.findViewById(R.id.editTextModelo);
                editTextMacBlue = (EditText) view.findViewById(R.id.editTextMacBlue);
                imageButtonMoto = (ImageButton) view.findViewById(R.id.imageButtonMoto);
                imageButtonMoto.setOnClickListener(buttonClickListener);
                imageButtonMoto.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_moto)));
                break;
            case 3:
                textViewResumen = (TextView) view.findViewById(R.id.textViewResumen);
                btnPoliticas = (Button) view.findViewById(R.id.btnPoliticas);
                btnPoliticas.setOnClickListener(buttonClickListener);
                btnFinish = (Button) view.findViewById(R.id.btnFinish);
                btnFinish.setOnClickListener(buttonClickListener);
                if (IdUser == 0) {
                    btnFinish.setText("Registrar");
                } else {
                    btnFinish.setText("Login");
                }
                break;
            default:
                break;
        }
    }

    private boolean ScanBluetooth() {
        try {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                ShowMessage("Su dispositivo no es compatible con Bluetooth");
                return false;
            } else {
                if (bluetoothLE == null) {
                    bluetoothLE = new BluetoothLE(getBaseContext());

                    if (!bluetoothLE.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }

                } else {
                    if (!bluetoothLE.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }
                }
                listVDevices.setAdapter(bluetoothLE.mAdapter);
                return true;
            }
        } catch (Exception e) {
            return false;
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

    private void SaveDevice(View v) {
        TextView txtDeviceSelected = (TextView) v;

        String nameDevice = txtDeviceSelected.getText().toString();

        for (int i = 0; i < bluetoothLE.bleDevices.size(); i++) {
            if (nameDevice.equals(bluetoothLE.bleDevices.get(i).getName())) {
                SharedPreferences sharedPref = getBaseContext().getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Name", bluetoothLE.bleDevices.get(i).getName());
                editor.putString("Mac", bluetoothLE.bleDevices.get(i).getAddress());
                editor.commit();
                ShowMessage("Dispositivo " + bluetoothLE.bleDevices.get(i).getName() + " guardado.");
                break;
            }
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

    private boolean SaveImage(String fileName, Bitmap imagen) {
        boolean isOk = true;
        boolean isNew = true;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            if (file.exists()) {
                isNew = false;
                DeleteRecursive(file);
            } else
                file.createNewFile();

            OutputStream fOut = new FileOutputStream(file);

            imagen.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

            if (!isNew)
                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            isOk = false;
        }
        return isOk;
    }

    public static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private class ValidarUser extends AsyncTask<String, String, String[]> {
        @Override
        protected String[] doInBackground(String... url) {

            try {
                jsonObjectTexts = JsonParser.readJsonFromUrl(url[0]);

                if (!jsonObjectTexts.isNull("first_name")) {
                    objetos[0] = jsonObjectTexts.getString("first_name");
                }

                if (!jsonObjectTexts.isNull("last_name")) {
                    objetos[1] = jsonObjectTexts.getString("last_name");
                }
                if (!jsonObjectTexts.isNull("email")) {
                    objetos[2] = jsonObjectTexts.getString("email");
                }
                if (!jsonObjectTexts.isNull("birthday")) {
                    objetos[3] = jsonObjectTexts.getString("birthday");
                }

                jsonObjectPicture = JsonParser.readJsonFromUrl(url[1]);
                objetos[4] = jsonObjectPicture.getJSONObject("data").getString("url");
            } catch (IOException | JSONException e) {

                e.printStackTrace();
            }

            return objetos;
        }

        @Override
        protected void onPostExecute(String[] stringFromDoInBackground) {

            editTextNombre = (EditText) findViewById(R.id.editTextNombre);
            editTextApellido = (EditText) findViewById(R.id.editTextApellido);
            editTextCorreo = (EditText) findViewById(R.id.editTextCorreo);
            imageButtonUser = (ImageButton) findViewById(R.id.imageButtonUser);
            btnFecha = (Button) findViewById(R.id.btnFecha);
            Date date = new Date();
            String testDate = stringFromDoInBackground[3];
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date dateFace = null;

            if (testDate != null && testDate != "") {
                try {
                    dateFace = format.parse(testDate);
                    btnFecha.setText(dateFace.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            editTextNombre.setText(stringFromDoInBackground[0]);
            editTextApellido.setText(stringFromDoInBackground[1]);
            editTextCorreo.setText(stringFromDoInBackground[2]);

            if (stringFromDoInBackground[4] != null) {
                imagenCargada = true;
                try {
                    InputStream prueba = new URL(stringFromDoInBackground[4]).openStream();
                    Bitmap foto = BitmapFactory.decodeStream(prueba);
                    RoundImages imaghenFace = new RoundImages(foto);
                    Bitmap imagenProcesada = imaghenFace.RoundImages(foto, 200, 200);
                    imageButtonUser.setImageBitmap(imagenProcesada);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                imagenCargada = false;
            }
        }
    }

    private class CrearUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<WebServiceParameter> parameters = new ArrayList<WebServiceParameter>();
            WebServiceParameter parametro = new WebServiceParameter();

            parametro.Nombre = "Nombres";
            parametro.Valor = params[1].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter(); // Si no se reinicia genera error.
            parametro.Nombre = "Telefono";
            parametro.Valor = params[2].toString();
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
            parametro.Valor = params[4].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "FechaNacimiento";
            parametro.Valor = params[5].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "RH";
            parametro.Valor = params[6].toString();
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
            parametro.Valor = params[7].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "IdTwitter";
            parametro.Valor = "0";
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "UserLogin";
            parametro.Valor = params[8].toString();
            parameters.add(parametro);

            parametro = new WebServiceParameter();
            parametro.Nombre = "Password";
            parametro.Valor = params[9].toString();
            parameters.add(parametro);

            return WebService.ConexionWS(params[0], parameters);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                int IdUser = jsonResponse.optInt("d");

                SharedPreferences user = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();

                if (IdUser != 0) {
                    editor.putInt("Id", IdUser);
                    editor.putString("Nombres", editTextNombre.getText().toString());
                    editor.putString("Apellidos", editTextApellido.getText().toString());
                    editor.putString("Telefono", editTextTelefono.getText().toString());
                    editor.putString("Sexo", spinnerSexo.getSelectedItem().toString());
                    editor.putString("Correo", editTextCorreo.getText().toString());
                    editor.putString("FechaNacimiento", btnFecha.getText().toString());
                    editor.putString("RH", spinnerRh.getSelectedItem().toString());
                    editor.putString("IdFacebook", idUserFacebook);
                    editor.putString("IdTwitter", "0");
                    editor.putString("UserLogin", editTextNombre.getText().toString());
                    editor.commit();
                    new CrearVehiculo().execute("http://gladiatortrackr.com/FireRoadService/MobileService.asmx/CrearVehiculo", editTextMarca.getText().toString(), editTextPlaca.getText().toString(), editTextColor.getText().toString(), editTextModelo.getText().toString(), editTextMacBlue.getText().toString());
                } else {
                    editor.putInt("Id", 0);
                    editor.commit();
                    ShowMessage("Error al registrar, intente más tarde.");
                }

            } catch (Exception e) {
            }
        }
    }

    private class EditarUsuario extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String params) {
            try {
                JSONObject jsonResponse = new JSONObject(params);
                String isOk = jsonResponse.optString("d");

                SharedPreferences user = getBaseContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                Intent goToMain = new Intent(getBaseContext(), MainActivity.class);

                if (isOk.equals("True")) {
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
                    ShowMessage("Error al editar, intente más tarde.");
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

    private boolean ComparePassword() {
        if (editTexPassword.getText().toString().equals(editTexPasswordConfirmacion.getText().toString()))
            return true;
        else {
            ShowMessage("Las contraseñas no coinciden.");
            return false;
        }
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

    private void decodeBitmap(String dir, String tipo) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);

        if (tipo.equals("User")) {
            RoundImages imaghenFace = new RoundImages(bitmap);
            Bitmap imagenProcesada = imaghenFace.RoundImages(bitmap, 200, 200);
            imageButtonUser.setImageBitmap(imagenProcesada);

        }
        if(tipo.equals("Moto")){

            RoundImages imaghenFace = new RoundImages(bitmap);
            Bitmap imagenProcesada = imaghenFace.RoundImages(bitmap, 200, 200);
            imageButtonMoto.setImageBitmap(imagenProcesada);

        }

    }
    private void decodeBitmap(Bitmap dir, String tipo) {


        if (tipo.equals("User")) {
            RoundImages imaghenFace = new RoundImages(dir);
            Bitmap imagenProcesada = imaghenFace.RoundImages(dir, 200, 200);
            imageButtonUser.setImageBitmap(imagenProcesada);

        }
        if(tipo.equals("Moto")){

            RoundImages imaghenFace = new RoundImages(dir);
            Bitmap imagenProcesada = imaghenFace.RoundImages(dir, 200, 200);
            imageButtonMoto.setImageBitmap(imagenProcesada);

        }

    }


    private class CrearVehiculo extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonResponse = new JSONObject(s);
                int isOk = jsonResponse.optInt("d");

                SharedPreferences motoMain = getBaseContext().getSharedPreferences("Moto", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = motoMain.edit();
                Intent goToMain = new Intent(getBaseContext(), MainActivity.class);

                if (isOk != 0) {
                    editor.putInt("Id", isOk);
                    editor.putString("Marca", editTextMarca.getText().toString());
                    editor.putString("Placa", editTextPlaca.getText().toString());
                    editor.putString("Color", editTextColor.getText().toString());
                    editor.putString("Modelo", editTextModelo.getText().toString());
                    editor.putString("MacBluetooth", editTextMacBlue.getText().toString());
                    editor.putString("NombreBluetooth", "FireRoad-RM");
                    editor.commit();
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

}
