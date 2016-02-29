package com.rmasc.fireroad;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.rmasc.fireroad.Adapters.RoundImages;
import com.rmasc.fireroad.BluetoothLe.BluetoothLE;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity implements Serializable {


    private boolean imagUser = false, imagMoto = false;

    private static boolean imagenCargada=false;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private ProgressBar progressBar;

    private static EditText editTextNombre, editTextEdad, editTextCorreo, editTextTelefono, editTextNombreMoto;

    private static TextView textViewResumen;

    private static ImageButton imageButtonUser, imageButtonMoto;

    private static Button btnScan, btnPoliticas, btnFinish;

    private static ListView listVDevices;

    private static View.OnClickListener buttonClickListener;

    private static AdapterView.OnItemClickListener itemClickListener;

    ImageView imageView;


    private static int RESULT_LOAD_IMAGE = 1;
    private String[] objetos = new String[6];
    private String idUser="10153923985124320";
    private String tokenNumero;
    private String urlFcebook;
    private String urlFacebookProfile;
    JSONObject jsonObjectTexts, jsonObjectPicture;


    BluetoothLE bluetoothLE;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data)
        {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            if (imagUser)
            {
                //imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeFile(picturePath), imageButtonUser.getWidth(), imageButtonUser.getHeight()));
                imageButtonUser.setImageDrawable(new RoundImages(ReSizeImage(BitmapFactory.decodeFile(picturePath))));
                imageButtonUser.setScaleType(ImageView.ScaleType.FIT_XY);
                imagUser = false;
            }
            else
            {
                //imageButtonMoto.setImageDrawable(new RoundImages(BitmapFactory.decodeFile(picturePath), imageButtonUser.getWidth(), imageButtonUser.getHeight()));
                imageButtonMoto.setImageDrawable(new RoundImages(ReSizeImage(BitmapFactory.decodeFile(picturePath))));
                imageButtonMoto.setScaleType(ImageView.ScaleType.FIT_XY);
                imagMoto = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        StrictMode.ThreadPolicy stream = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(stream);
        AccessToken token = AccessToken.getCurrentAccessToken();
        if(token.isExpired())
        {
           AccessToken.refreshCurrentAccessTokenAsync();
           token = AccessToken.getCurrentAccessToken();

        }

        tokenNumero= token.getToken();

        urlFcebook="https://graph.facebook.com/"+idUser+"?fields=id,name,first_name,birthday,last_name,middle_name,email,picture&access_token="+tokenNumero;
        urlFacebookProfile="http://graph.facebook.com/"+idUser+"/picture?redirect=0&type=large";

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
                switch (v.getId())
                {
                    case R.id.imageButtonUser:
                        imagUser = true;
                        imagMoto = !imagUser;
                        i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i,RESULT_LOAD_IMAGE);
                        break;
                    case R.id.imageButtonMoto:
                        imagMoto = true;
                        imagUser = !imagMoto;
                        i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                        break;
                    case R.id.btnScan:
                        if (bluetoothLE != null)
                        {
                            if (!bluetoothLE.mScanning)
                                bluetoothLE.scanLeDevice(true);
                        }
                        break;
                    case R.id.btnFinish:
                        i = new Intent(getBaseContext(), MainActivity.class);
                        Bitmap imagen = ((RoundImages)(imageButtonUser.getDrawable())).getBitmap();
                        if (SaveImage("FireUser", imagen))
                        {
                            imagen = ((RoundImages)(imageButtonMoto.getDrawable())).getBitmap();
                            if(SaveImage("FireMoto", imagen))
                            {
                                startActivity(i);
                                finish();
                            }
                            else
                                ShowMessage("Error al guardar la imagen de la moto.");
                        }
                        else
                        ShowMessage("Error al guardar la imagen de usuario.");
                        break;
                    case R.id.btnPoliticas:
                        break;
                    default:
                        break;
                }
            }
        };

        AssignControls();

        new ValidarUser().execute(urlFcebook, urlFacebookProfile);

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


            switch (getArguments().getInt(ARG_SECTION_NUMBER))
            {
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

    public void SetPageIndicator(int position)
    {
        progressBar.setProgress(position);
    }

    public void AssignControls()
    {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public static void AssignStaticControls(int position, View view, Context context)
    {
        switch (position)
        {
            case 1:
                editTextNombre = (EditText) view.findViewById(R.id.editTextNombre);
                editTextEdad = (EditText) view.findViewById(R.id.editTextEdad);
                editTextCorreo = (EditText) view.findViewById(R.id.editTextCorreo);
                editTextTelefono = (EditText) view.findViewById(R.id.editTextTelefono);
                imageButtonUser = (ImageButton) view.findViewById(R.id.imageButtonUser);

                if(!imagenCargada) {

                  imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(context.getResources(), R.drawable.no_user)));
                }
                else
                {


                }
                imageButtonUser.setOnClickListener(buttonClickListener);
                break;
            case 2:
                listVDevices = (ListView) view.findViewById(R.id.listVDevices);
                listVDevices.setOnItemClickListener(itemClickListener);
                btnScan = (Button) view.findViewById(R.id.btnScan);
                btnScan.setOnClickListener(buttonClickListener);
                editTextNombreMoto = (EditText) view.findViewById(R.id.editTextNombreMoto);
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
                break;
            default:
                break;
        }
    }

    private boolean ScanBluetooth()
    {
        try
        {
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                ShowMessage("Su dispositivo no es compatible con Bluetooth");
                return false;
            }
            else
            {
                if (bluetoothLE == null)
                {
                    bluetoothLE = new BluetoothLE(getBaseContext());

                    if (!bluetoothLE.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }

                }
                else
                {
                    if (!bluetoothLE.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1);
                    }
                }
                listVDevices.setAdapter(bluetoothLE.mAdapter);
                return  true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void ShowMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SaveDevice(View v)
    {
        TextView txtDeviceSelected = (TextView)v;

        String nameDevice = txtDeviceSelected.getText().toString();

        for (int i = 0; i < bluetoothLE.bleDevices.size() ; i++)
        {
            if (nameDevice.equals(bluetoothLE.bleDevices.get(i).getName()))
            {
                SharedPreferences sharedPref = getBaseContext().getSharedPreferences("DeviceBLE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("Name", bluetoothLE.bleDevices.get(i).getName());
                editor.putString("Mac", bluetoothLE.bleDevices.get(i).getAddress());
                editor.commit();
                ShowMessage("Dispositivo " + bluetoothLE.bleDevices.get(i).getName() +" guardado.");
                break;
            }
        }
    }

    private Bitmap ReSizeImage (Bitmap imagen)
    {
        if (imagen.getHeight() <= 200 && imagen.getWidth() <= 200)
            return imagen;
        else if (imagen.getHeight() <= 500 && imagen.getWidth() <= 500)
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth()*(0.5)), (int)(imagen.getHeight()*(0.5)), true);
        else if (imagen.getHeight() <= 750 && imagen.getWidth() <= 750)
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth()*(0.2)), (int)(imagen.getHeight()*(0.2)), true);
        else
            return Bitmap.createScaledBitmap(imagen, (int) (imagen.getWidth()*(0.1)), (int)(imagen.getHeight()*(0.1)), true);
    }

    private boolean SaveImage (String fileName, Bitmap imagen)
    {
        boolean isOk = true;
        boolean isNew = true;
        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);
            if (file.exists()) {
                isNew = false;
                DeleteRecursive(file);
            }
            else
                file.createNewFile();

            OutputStream fOut = new FileOutputStream(file);

            imagen.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

            ShowMessage(file.getPath());

            if (!isNew)
                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            isOk = false;
        }
        return isOk;
    }

    public static void DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private class ValidarUser extends AsyncTask<String,String,String[]> {




        @Override
        protected String[] doInBackground(String... url) {

          //  try {
             //   jsonObjectTexts = JSONObjectreadJsonFromUrl(url[0]);
            //    objetos[0] = jsonObjectTexts.getString("name");
               // objetos[1] = jsonObjectTexts.getString("email");
               // objetos[2] =  jsonObjectTexts.getString("birthday");
              //  jsonObjectPicture = .readJsonFromUrl(url[1]);
               // objetos[3] = jsonObjectPicture.getJSONObject("data").getString("url");
           // } catch (IOException | JSONException e) {
        //        e.printStackTrace();
        //    }

            return objetos;
        }

        @Override
        protected void onPostExecute(String[] stringFromDoInBackground) {

            editTextNombre = (EditText) findViewById(R.id.editTextNombre);
            editTextCorreo = (EditText) findViewById(R.id.editTextCorreo);
            imageButtonUser = (ImageButton) findViewById(R.id.imageButtonUser);
            editTextEdad=(EditText) findViewById(R.id.editTextEdad);
            Date date = new Date();
            String testDate = stringFromDoInBackground[2];
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date dateFace=null ;

            try {
                dateFace = format.parse(testDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int años = date.getYear() -  dateFace.getYear();

            editTextNombre.setText(stringFromDoInBackground[0]);
            editTextCorreo.setText(stringFromDoInBackground[1]);
            editTextEdad.setText(Integer.toString(años));
            if (stringFromDoInBackground[3]!=null) {
             imagenCargada = true;
                try {
                    InputStream prueba = new URL(stringFromDoInBackground[3]).openStream();
                    Bitmap foto = BitmapFactory.decodeStream(prueba);
                    imageButtonUser.setImageDrawable(new RoundImages(foto));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                imagenCargada=false;            }
        }
    }
}
