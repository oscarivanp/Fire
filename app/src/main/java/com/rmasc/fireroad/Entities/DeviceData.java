package com.rmasc.fireroad.Entities;

/**
 * Created by ADMIN on 11/02/2016.
 */
public class DeviceData {

    public int Id;
    public int Modelo;
    public int VersionSoftware;
    public String Fecha;
    public String Hora;
    public String LocationCode;
    public float Latitud;
    public float Longitud;
    public float Velocidad;
    public float Rumbo;
    public int Satelites;
    public boolean Fix;
    public int DistanciaRecorrida;
    public float VoltajeEntrada;
    public String Entradas;
    public int Modo;
    public int NumeroReporte;
    public String Horometro;
    public float Bateria;
    public boolean TiempoReporte;

    public int ReporteId;
    public int VehiculoId;

    public int TamañoMsj;
    public String Mensaje;
    public String ChkSum;

    public DeviceData() {
        Id = Modelo = VersionSoftware = Satelites = DistanciaRecorrida = Modo = NumeroReporte = TamañoMsj = 0;
        Fecha = Hora = LocationCode = Horometro = Entradas = Mensaje = ChkSum = "";
        Latitud = Longitud = Velocidad = VoltajeEntrada = Bateria = Rumbo = 0;
        Fix = TiempoReporte = false;
    }

    public String FormatDate()
    {
        String año = Fecha.substring(0, 4);
        String mes = Fecha.substring(4, 6);
        String dia = Fecha.substring(6, 8);
        return dia + "/" + mes + "/" + año;
    }

    public DeviceData(String DataIn) {
        String[] DatosIn = DataIn.split(";");

        if (DatosIn.length > 20) {

            if (DatosIn[0].contains("ST")) {
                Id = Integer.parseInt(DatosIn[1]);
                Modelo = Integer.parseInt(DatosIn[2]);
                VersionSoftware = Integer.parseInt(DatosIn[3]);
                Fecha = DatosIn[4];
                Hora = DatosIn[5];
                LocationCode = DatosIn[6];
                Latitud = Float.parseFloat(DatosIn[7]);
                Longitud = Float.parseFloat(DatosIn[8]);
                Velocidad = Float.parseFloat(DatosIn[9]);
                Rumbo = Float.parseFloat(DatosIn[10]);
                Satelites = Integer.parseInt(DatosIn[11]);
                Fix = Boolean.parseBoolean(DatosIn[12]);
                DistanciaRecorrida = Integer.parseInt(DatosIn[13]);
                VoltajeEntrada = Float.parseFloat(DatosIn[14]);
                Entradas = DatosIn[15];

                switch (Modelo) {
                    case 3:
                        Modo = Integer.parseInt(DatosIn[16]);
                        NumeroReporte = Integer.parseInt(DatosIn[17]);
                        Horometro = DatosIn[18];
                        Bateria = Float.parseFloat(DatosIn[19]);
                        TiempoReporte = Boolean.parseBoolean(DatosIn[20]);
                        break;
                    case 8:
                        TamañoMsj = Integer.parseInt(DatosIn[16]);
                        Mensaje = DatosIn[17];
                        ChkSum = DatosIn[18];
                        Horometro = DatosIn[19];
                        Bateria = Float.parseFloat(DatosIn[20]);
                        TiempoReporte = Boolean.parseBoolean(DatosIn[21]);
                        break;
                    default:
                        break;
                }

            }
        }
    }

}
