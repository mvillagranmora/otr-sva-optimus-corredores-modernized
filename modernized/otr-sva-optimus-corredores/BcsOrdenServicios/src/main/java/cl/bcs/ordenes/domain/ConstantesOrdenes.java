package cl.bcs.ordenes.domain;

/** B-ORD-CONST-*: string constants from legacy ConstantesOrdenes.java. */
public final class ConstantesOrdenes {
    private ConstantesOrdenes() {}

    public static final String CLASIFICACION_IF  = "IF";
    public static final String CLASIFICACION_RF  = "RF";
    public static final String CLASIFICACION_RV  = "RV";
    public static final String CLASIFICACION_RE  = "RE";
    public static final String TIPO_INSTRUMENTO_SIM    = "SIM";
    public static final String TIPO_OPERACION_VENSIM   = "VENSIM";
    public static final String TIPO_OPERACION_COMSIM   = "COMSIM";
    public static final String ESTADO_ORDEN_INGRESADA            = "ING";
    public static final String ESTADO_ORDEN_PENDIENTE_AUTORIZAR  = "PDA";
    public static final String ESTADO_ORDEN_AUTORIZADA           = "AUT";
    public static final String ESTADO_ORDEN_RECHAZADA            = "RZD";
    public static final String ESTADO_ORDEN_ANULADA              = "ANU";
    public static final String ESTADO_CONFIRMACION_ORDEN_EN_PLAZO = "CEP";
    public static final String ESTADO_CONFIRMACION_ORDEN_NO       = "NCF";
    public static final String TIPO_CANAL_INGRESO_AGENTE = "AGE";
    public static final String TIPO_CANAL_INGRESO_TRADER = "TRA";
    public static final String TIPO_CANAL_INGRESO_CLI    = "CLI";
    public static final String TIPO_CANAL_INGRESO_OWP    = "OWP";
    public static final String TIPO_CANAL_INGRESO_WS     = "WBS";
    public static final String TIPO_VIGENCIA_SOLO_HOY   = "SH";
    public static final String TIPO_VIGENCIA_SEMANAL    = "SE";
    public static final String TIPO_VIGENCIA_FECHA_FIJA = "FF";
    public static final String TIPO_VIGENCIA_PERMANENTE = "OP";
    public static final String ESTADO_MODULO_CERRADO    = "C";
    public static final String ID_MODULO_ORDENES        = "ORD";
    public static final String ACCION_COMPRA = "COMPRA";
    public static final String ACCION_VENTA  = "VENTA";
    /** Código ISO de dólar estadounidense. */
    public static final String Dolar = "USD";
    public static final String SI = "S";
    public static final String NO = "N";
    public static final String TIPO_CANAL_DISTRIBUCION_MANUAL = "MAN";
    public static final String GLOSA_CARGA_MASIVA   = "CARGA_MASIVA";
    public static final String GLOSA_ORDEN_POSTRERA = "ORDEN_POSTRERA";
}
