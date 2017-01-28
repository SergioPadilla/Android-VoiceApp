package com.piser.voice;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Arrays;
/**
 * Created by jopime on 28/1/17.
 */

public class Models {
    public static final String ENERO = "enero";
    public static final String FEBRERO = "febrero";
    public static final String MARZO = "marzo";
    public static final String ABRIL = "abril";
    public static final String MAYO = "mayo";
    public static final String JUNIO = "junio";
    public static final String JULIO = "julio";
    public static final String AGOSTO = "agosto";
    public static final String SEPTIEMBRE = "septiembre";
    public static final String OCTUBRE = "octubre";
    public static final String NOVIEMBRE = "noviembre";
    public static final String DICIEMBRE = "diciembre";

    public static final String CREAR = "crear";
    public static final String LISTAR = "listar";
    public static final String AYUDA = "ayuda";
    public static final String ASKMONTH = "¿En que mes quiere crear el evento?";
    public static final String ASKDAY = "¿Que día?";
    public static final String ASKYEARS = "¿En que año?";
    public static final String TRYAGAIN = "No le he entendido, por favor repita la orden.";
    public static final String INSTRUCCIONES = "Diga Crear, para crear un evento.\n" +
            " Diga listar, para listar todos los eventos creados. \n" +
            "Si necesita ayuda, diga ayuda.";

    public static List<String> getMonths(){
        return Arrays.asList(ENERO,FEBRERO,MARZO,ABRIL,MAYO,JUNIO,JULIO,AGOSTO,SEPTIEMBRE,OCTUBRE,NOVIEMBRE,DICIEMBRE);
    }



}
