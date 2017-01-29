package com.piser.voice;

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

    public static final String CREATE = "crear";
    public static final String LIST = "listar";
    public static final String HELP = "ayuda";

    public static final String ASKMONTH = "¿En que mes quiere crear el evento?";
    public static final String ASKDAY = "¿Que día?";
    public static final String ASKYEARS = "¿En que año?";
    public static final String TRYAGAIN = "No se reconoce ninguna orden, por favor, " +
            "vuelva a intentarlo.";
    public static final String INSTRUCTIONS = "Si quiere crear un evento diga," + CREATE + "."+
            "Si quiere listar todos los eventos diga," + LIST + "." +
            "Si quiere volver a escucharlo diga, "+HELP;
    public static final String WELCOME = "Bienvenido, esta aplicación se maneja por voz." +
            "En la esquina superior derecha puede encontrar un botón para comunicarse conmigo." +
            "Si quiere saber cómo funciona, pulse el botón y diga," + HELP;

    public static final String LIST_SUCCESS = "Aquí tiene la lista con todos sus eventos.";

    public static List<String> getMonths(){
        return Arrays.asList(ENERO, FEBRERO, MARZO, ABRIL, MAYO, JUNIO, JULIO, AGOSTO, SEPTIEMBRE,
                OCTUBRE, NOVIEMBRE, DICIEMBRE);
    }



}
