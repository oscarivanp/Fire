package com.rmasc.fireroad.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rmasc.fireroad.Entities.Vehiculo;
import com.rmasc.fireroad.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by rafaelmartinez on 10/03/16.
 */
public class VehiculosAdapter  extends ArrayAdapter<Vehiculo> {


    public VehiculosAdapter(Context context, ArrayList<Vehiculo> motos)
    {
        super(context, R.layout.array_item ,motos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Vehiculo moto = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.array_item, parent, false);
            viewHolder.Titulo = (TextView) convertView.findViewById(R.id.txtTitulo);
            viewHolder.ImagenMoto=(ImageView) convertView.findViewById(R.id.ivMoto);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.Titulo.setText(moto.Marca + " Placa: " + moto.Placa);
        InputStream Imagen = null;


        try {
            Imagen = new URL(moto.FotoPath).openStream();
            Bitmap foto = BitmapFactory.decodeStream(Imagen);
            RoundImages imaghenFace = new RoundImages(foto);
            Bitmap imagenProcesada = imaghenFace.RoundImages(foto, 200, 200);


            viewHolder.ImagenMoto.setImageBitmap(imagenProcesada);

        } catch (IOException e) {
            e.printStackTrace();
        }

        viewHolder.Id = moto.Id;

        return convertView;
    }
}
