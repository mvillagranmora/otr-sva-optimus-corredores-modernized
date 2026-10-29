package cl.bcs.normativa.exportacion;

import java.util.Properties;

import cl.bcs.utils.PropertiesUtil;

public class OpcionesExportacion {
    /**
     * Define los valores por defecto de las opciones de exportacion.
     *
     * @author fpino@email.com
     *
     */
    public static class ValorDefecto {

        /**
         * Simbolo decimal por defecto.
         */
        public static final String FORMATO_FECHAS = "dd-MM-yyyy";

        /**
         * Separador de listas por defecto.
         */
        public static final String SEPARADOR_LISTAS = ";";
        /**
         * Simbolo decimal por defecto.
         */
        public static final String SIMBOLO_DECIMAL = ",";
    }

    /**
     * Formato de las fechas.
     */
    private final String formatoFechas;

    /**
     * Separador de listas.
     */
    private final String separadorListas;

    /**
     * Simbolo decimal.
     */
    private final String simboloDecimal;

    public OpcionesExportacion(Properties props) {
        this.formatoFechas = PropertiesUtil.getOptional(props, "formatoFechas",
                ValorDefecto.FORMATO_FECHAS);
        this.separadorListas = PropertiesUtil.getOptional(props, "separadorListas",
                ValorDefecto.SEPARADOR_LISTAS);
        this.simboloDecimal = PropertiesUtil.getOptional(props, "simboloDecimal",
                ValorDefecto.SIMBOLO_DECIMAL);
    }

    /**
     * Obtiene el formato de fechas.
     *
     * @return El formato de fechas.
     */
    public String getFormatoFechas() {
        return formatoFechas;
    }

    /**
     * Obtiene el separador de listas.
     *
     * @return El separador de listas.
     */
    public String getSeparadorListas() {
        return separadorListas;
    }

    /**
     * Obtiene el simbolo decimal.
     *
     * @return El simbolo decimal.
     */
    public String getSimboloDecimal() {
        return simboloDecimal;
    }
}
