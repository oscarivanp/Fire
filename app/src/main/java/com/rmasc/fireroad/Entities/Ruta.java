package com.rmasc.fireroad.Entities;

import java.text.SimpleDateFormat;

/**
 * Created by ADMIN on 07/03/2016.
 */
public class Ruta {
    public int Id;
    public String Descripcion;
    public SimpleDateFormat formatoFecha;
    public String FechaInicio;
    public String FechaFin;
    public String Distancia;
    public String VelMedia;
    public String VelMax;
    public int IdVehiculo;

    public  Ruta()
    {
        Id = 0;
        IdVehiculo = 0;
        Descripcion = "";
        formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FechaInicio = "";
        FechaFin = "";
        Distancia="";
        VelMax = "";
        VelMedia = "";



    }
}
