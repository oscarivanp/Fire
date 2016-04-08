package com.rmasc.fireroad.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rmasc.fireroad.Entities.Ruta;
import com.rmasc.fireroad.R;

import java.util.ArrayList;

/**
 * Created by ADMIN on 08/03/2016.
 */
public class RutasAdapter extends ArrayAdapter<Ruta> {

    public RutasAdapter(Context context, ArrayList<Ruta> rutas)
    {
        super(context, R.layout.array_item ,rutas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Ruta ruta = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.array_item, parent, false);
            viewHolder.Titulo = (TextView) convertView.findViewById(R.id.txtTitulo);

        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.Titulo.setText(ruta.Descripcion+ ruta.FechaInicio + " " + ruta.FechaFin);
        viewHolder.Id = ruta.Id;
        convertView.setTag(viewHolder);
        return convertView;
    }
}
