package com.rmasc.fireroad.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rmasc.fireroad.Entities.DeviceData;

import java.util.ArrayList;

/**
 * Created by ADMIN on 06/04/2016.
 */
public class TransmisionesHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "FireRoadDB";
    public static String TABLE_TRANSMISION = "Transmisiones";
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_CREATE = "CREATE TABLE "+ TABLE_TRANSMISION + " (" +
            "Latitud TEXT, " +
            "Longitud TEXT, " +
            "Fecha TEXT, " +
            "Bateria INTEGER, " +
            "Encendido INTEGER, " +
            "Velocidad INTEGER, " +
            "LocationCode TEXT, " +
            "Rumbo INTEGER, " +
            "Satelites INTEGER, " +
            "VIn INTEGER, " +
            "TiempoReporte BIT," +
            "Fix BIT," +
            "ReporteId INTEGER, " +
            "VehiculoId INTEGER )";

    public TransmisionesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void InsertTransmision (SQLiteDatabase db, DeviceData transmision)
    {
        ContentValues parametros = new ContentValues();
        parametros.put("Latitud", transmision.Latitud);
        parametros.put("Longitud", transmision.Longitud);
        parametros.put("Fix", transmision.Fix);
        parametros.put("Fecha", transmision.FormatDate() + " " + transmision.Hora);
        parametros.put("Bateria", transmision.Bateria);
        parametros.put("Encendido", transmision.Modo);
        parametros.put("Velocidad", ((int) transmision.Velocidad));
        parametros.put("LocationCode", transmision.LocationCode);
        parametros.put("Rumbo", ((int) transmision.Rumbo));
        parametros.put("Satelites", transmision.Satelites);
        parametros.put("VIn", transmision.VoltajeEntrada);
        parametros.put("TiempoReporte", transmision.TiempoReporte);
        parametros.put("ReporteId", transmision.ReporteId);
        parametros.put("VehiculoId", transmision.VehiculoId);

        db.insert(TABLE_TRANSMISION, null, parametros);
    }

    public ArrayList<DeviceData> ArrayTransmision (SQLiteDatabase db, String where, String Orderby)
    {
        ArrayList<DeviceData> listToReturn = new ArrayList<DeviceData>();
        Cursor cursor = db.query(TABLE_TRANSMISION, null, where, null, Orderby, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    DeviceData transmision = new DeviceData();
                    transmision.Latitud = cursor.getFloat(0);
                    transmision.Longitud = cursor.getFloat(1);
                    transmision.Fecha = cursor.getString(2);
                    transmision.Bateria = cursor.getInt(3);
                    transmision.Velocidad = cursor.getInt(5);
                    transmision.Fix = cursor.getInt(11) > 0;
                    listToReturn.add(transmision);
                } while (cursor.moveToNext());
            }
        }
        return listToReturn;
    }

    public String SelectTransmision(SQLiteDatabase db, String where, String Orderby)
    {
        Cursor cursor = db.query(TABLE_TRANSMISION, null, where, null, Orderby, null, null);
        if (cursor != null)
        {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                String transmision = "";
                do {
                    transmision += cursor.getString(0) + ";" + cursor.getString(1) + ";" + cursor.getString(2) + ";" + cursor.getString(3) + ";" + cursor.getString(4)
                            + ";" + cursor.getString(5) + ";0;" + cursor.getString(6) + ";" + cursor.getString(7) + ";" + cursor.getString(8) + ";" + cursor.getString(9)
                            + ";" + cursor.getString(10) + ";" + cursor.getString(11) + ",";
                } while (cursor.moveToNext());
                return transmision;
            }
        }
        return "";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }
}
