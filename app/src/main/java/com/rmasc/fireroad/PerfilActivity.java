package com.rmasc.fireroad;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.rmasc.fireroad.Adapters.RoundImages;

public class PerfilActivity extends AppCompatActivity {

    View.OnClickListener buttonClickListener;
    EditText editTextNombre, editTextEdad, editTextCorreo, editTextTelefono;
    ImageButton imageButtonUser;

    private static int RESULT_LOAD_IMAGE = 1;

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
                    case R.id.btnSave:
                        //Guarda datos
                        i = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };

        AssignViews();
    }

    private void AssignViews()
    {
        editTextNombre = (EditText) findViewById(R.id.editTextNombre);
        editTextEdad = (EditText) findViewById(R.id.editTextEdad);
        editTextCorreo = (EditText) findViewById(R.id.editTextCorreo);
        editTextTelefono = (EditText) findViewById(R.id.editTextTelefono);
        imageButtonUser = (ImageButton) findViewById(R.id.imageButtonUser);
        imageButtonUser.setOnClickListener(buttonClickListener);
        imageButtonUser.setImageDrawable(new RoundImages(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.no_user)));
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
}
