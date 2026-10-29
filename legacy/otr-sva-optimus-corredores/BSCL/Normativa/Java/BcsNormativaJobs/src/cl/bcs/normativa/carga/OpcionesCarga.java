package cl.bcs.normativa.carga;

import java.io.Serializable;
import java.util.Properties;

import cl.bcs.utils.PropertiesUtil;

/**
 * Opciones de carga.
 *
 * @author fpino@email.com
 *
 */
public class OpcionesCarga implements Serializable {

    /**
     * Define los valores por defecto de las opciones de carga.
     *
     * @author fpino@email.com
     *
     */
    public static class ValorDefecto {

        /**
         * Tama&ntilde;o del lote batch por defecto.
         */
        public static final int BATCH_SIZE = 1000;

        /**
         * Separador de listas por defecto.
         */
        public static final String SEPARADOR_LISTAS = ";";

        /**
         * Simbolo decimal por defecto.
         */
        public static final String SIMBOLO_DECIMAL = ",";
        
        /**
         * Tipo de sistema para carga DJ1 por defecto.
         */
        public static final String TIPO_ARCHIVO_DJ1 = "";
    }

    private static final long serialVersionUID = 1L;

    /**
     * Tama&ntilde;o del lote de proceso.
     */
    private final int batchSize;

    /**
     * Indicador para grabar datos en el documento.
     */
    private final boolean grabar;

    /**
     * Indicador de reemplazo de datos en el documento.
     */
    private final boolean reemplazar;

    /**
     * Separador de las listas para archivos CSV.
     */
    private final String separadorListas;

    /**
     * Simbolo decimal utilizado en el archivo de carga.
     */
    private final String simboloDecimal;

    /**
     * Tipo de archivo.
     */
    private String tipoArchivo;

    /**
     * Tipo de carga.
     */
    private final TipoCarga tipoCarga;
    
    /**
     * Tipo archivo de carga para DJ1.
     */
    private String tipoArchivoDJ1;

    /**
     * Constructor.
     *
     * @param props
     *            Las {@link Properties}.
     */
    public OpcionesCarga(final Properties props) {

        this.batchSize = PropertiesUtil.getOptional(props, "batchSize", ValorDefecto.BATCH_SIZE);
        this.grabar = PropertiesUtil.getOptional(props, "grabar", false);
        this.reemplazar = PropertiesUtil.getOptional(props, "reemplazar", false);
        this.separadorListas = PropertiesUtil.getOptional(props, "separadorListas",
                ValorDefecto.SEPARADOR_LISTAS);
        this.simboloDecimal = PropertiesUtil.getOptional(props, "simboloDecimal",
                ValorDefecto.SIMBOLO_DECIMAL);

        final String tc = PropertiesUtil.getRequiredString(props, "tipoCarga");
        this.tipoCarga = TipoCarga.valueOf(tc);
        if (this.tipoCarga.equals(TipoCarga.EXTERNA)) {
            this.tipoArchivo = PropertiesUtil.getRequiredString(props, "tipoArchivo");
            this.tipoArchivoDJ1 = PropertiesUtil.getOptional(props, "tipoArchivoDJ1", ValorDefecto.TIPO_ARCHIVO_DJ1);
        }
    }

    /**
     * Obtiene el tama&ntilde;o del lote de proceso.
     *
     * @return El tama&ntilde;o del lote de proceso.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Obtiene el separador de lista para archivo CSV.
     *
     * @return El separador de lista para archivo CSV.
     */
    public String getSeparadorListas() {
        return separadorListas;
    }

    /**
     * Obtiene el simbolo decimal utilizado en el archivo de carga.
     *
     * @return El simbolo decimal utilizado en el archivo de carga.
     */
    public String getSimboloDecimal() {
        return simboloDecimal;
    }

    /**
     * Obtiene el tipo de archivo.
     *
     * @return El tipo de archivo.
     */
    public String getTipoArchivo() {
        return tipoArchivo;
    }

    /**
     * Obtiene el tipo de carga.
     *
     * @return El tipo de carga.
     */
    public TipoCarga getTipoCarga() {
        return tipoCarga;
    }
    
    /**
     * Obtiene el tipo de archivo para DJ1.
     *
     * @return El tipo de archivo FX,FI,IR.
     */
    public String getTipoArchivoDJ1() {
    	return tipoArchivoDJ1;
    }

    /**
     * Indica si se graba datos en el destino.
     *
     * @return <code>true</code> si se graba datos en el destino,
     *         <code>false</code> en caso contrario.
     */
    public boolean isGrabar() {
        return grabar;
    }

    /**
     * Indica si se reemplaza datos en el destino.
     *
     * @return <code>true</code> si se reemplaza datos en el destino,
     *         <code>false</code> en caso contrario.
     */
    public boolean isReemplazar() {
        return reemplazar;
    }

    @Override
    public String toString() {
        return "OpcionesCarga [batchSize=" + batchSize + ", grabar=" + grabar + ", reemplazar="
                + reemplazar + ", separadorListas=" + separadorListas + ", simboloDecimal="
                + simboloDecimal + ", tipoArchivo=" + tipoArchivo + ", tipoCarga=" + tipoCarga
                + ", tipoArchivoDJ1=" + tipoArchivoDJ1 + "]";
    }

}
