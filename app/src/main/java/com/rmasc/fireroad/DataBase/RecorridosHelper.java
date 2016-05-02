package com.rmasc.fireroad.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rmasc.fireroad.Entities.Ruta;

import java.util.ArrayList;

/**
 * Created by ADMIN on 28/04/2016.
 */
public class RecorridosHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "FireRoadDBRecorridos";
    public static String TABLE_RECORRIDOS = "Recorridos";
    private static int DATABASE_VERSION = 1;

    private static String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECORRIDOS + " (" +
            "Id INTEGER, " +
            "Descripcion TEXT, " +
            "FechaInicio TEXT, " +
            "FechaFin TEXT, " +
            "VelMedia INTEGER, " +
            "VelMax INTEGER, " +
            "Distancia TEXT, " +
            "VehiculoId INTEGER )";

    public RecorridosHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void InsertRuta(SQLiteDatabase db, Ruta ruta) {
        ContentValues parametros = new ContentValues();
        parametros.put("Id", ruta.Id);
        parametros.put("Descripcion", ruta.Descripcion);
        parametros.put("FechaInicio", ruta.FechaInicio);
        parametros.put("FechaFin", ruta.FechaFin);
        parametros.put("VelMedia", ruta.VelMedia);
        parametros.put("VelMax", ruta.VelMax);
        parametros.put("Distancia", ruta.Distancia);
        parametros.put("VehiculoId", ruta.IdVehiculo);
        db.insert(TABLE_RECORRIDOS, null, parametros);
    }

    public ArrayList<Ruta> SelectRecorridos(SQLiteDatabase db, String where, String Orderby) {
        ArrayList<Ruta> rutaToReturn = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RECORRIDOS, null, where, null, Orderby, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    Ruta rutaTemp = new Ruta();
                    rutaTemp.Id = cursor.getInt(0);
                    rutaTemp.Descripcion = cursor.getString(1);
                    rutaTemp.FechaInicio = cursor.getString(2);
                    rutaTemp.FechaFin = cursor.getString(3);
                    rutaTemp.VelMedia = cursor.getString(4);
                    rutaTemp.VelMax = cursor.getString(5);
                    rutaTemp.Distancia = cursor.getString(6);
                    rutaTemp.IdVehiculo = cursor.getInt(7);
                    rutaToReturn.add(rutaTemp);

                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return rutaToReturn;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }
}
