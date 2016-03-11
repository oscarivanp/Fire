package com.rmasc.fireroad.Entities;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by ADMIN on 02/03/2016.
 */
public class ImagenData{
    public String content;

    public ImagenData(Bitmap image)
    {
        content = EncodeImageToString(ImageToByteArray(image));
    }

    private byte[] ImageToByteArray(Bitmap imagen)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private String EncodeImageToString(byte[] imagen)
    {
        return Base64.encodeToString(imagen, Base64.DEFAULT);
    }
}
