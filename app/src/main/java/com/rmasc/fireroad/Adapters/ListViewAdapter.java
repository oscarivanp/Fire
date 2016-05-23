package com.rmasc.fireroad.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.rmasc.fireroad.R;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by ADMIN on 19/05/2016.
 */
public class ListViewAdapter extends ArrayAdapter<String> {

    private Context context;

    public ListViewAdapter(Context context, String[] content) {
        super(context, R.layout.item_punto_interes, content);
        this.context = context;
    }

    public class Holder {
        byte Id;
        String itemText;
        CheckBox checkBox;
    }

    private boolean isItemChecked(byte Id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        byte puntos = ((byte) sharedPreferences.getInt("PuntosInteres", 0));
        if ((puntos & Id) > 0)
            return true;
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final String content = getItem(position);
        final Holder holder;

        if (convertView == null) {
            holder = new Holder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.item_punto_interes, parent, false);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkboxPunto);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.Id = ((byte) (1 << position));
        holder.checkBox.setText(content);
        holder.itemText = content;
        holder.checkBox.setChecked(isItemChecked(holder.Id));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Holder holder1 = (Holder) buttonView.getTag();
                SharedPreferences sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                byte puntos = ((byte) sharedPreferences.getInt("PuntosInteres", 0));
                if (isChecked)
                    editor.putInt("PuntosInteres", puntos | holder1.Id);
                else
                    editor.putInt("PuntosInteres", puntos & (~holder1.Id));

                editor.apply();
            }
        });

        return convertView;
    }
}
