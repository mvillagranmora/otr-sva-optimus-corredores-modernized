package cl.bcs.tesoreria.javacall.distribucion;

/** The legacy {@code ConstantesTesoreria} members used by ProcesarDistribucion (TES-B-277..321). */
final class Constantes {

    static final String SI = "S";
    static final String NO = "N";

    static final String TIPO_SOLICITUD_APORTE = "APO";
    static final String DOLAR = "USD";
    static final String CALCULO_POR_MONTO = "M";
    static final String ORIGEN_MANUAL = "M";
    static final String CUENTA_INVERSION = "CIN";
    static final String CANAL_AGENTE = "AGE";
    static final String UF = "UF";

    static final String ESTADO_ACTIVO = "ACT";
    static final String ESTADO_RECHAZADO = "RZD";
    static final String ESTADO_PROCESADO = "PRO";

    static final String APORTE_FONDO_MUTUO = "APONORM";
    static final String RESCATE_FONDO_MUTUO = "RESNORM";
    static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    static final String TIPO_PRODUCTO = "INFM";
    static final String TIPO_COMPROBANTE_TESORERIA = "FMM";
    static final String TIPO_COMPROBANTE_CUENTA_INVERSION = "FFM";

    static final String INGRESO_TESORERIA = "MOVING";
    static final String EGRESO_TESORERIA = "MOVEGR";
    static final String CARGO_CUENTA_INVERSION = "MOVCAR";
    static final String ABONO_CUENTA_INVERSION = "MOVABN";

    static final String TIPO_PERSONA_CLIENTE = "CLI";
    static final String TIPO_PERSONA_CORREDOR = "CNT";
    static final String TIPO_PERSONA_CONTRAPARTE = "CNT";

    static final String MODULO_TESORERIA = "TES";
    static final String MODULO_CTA_INVERSION = "CTA";

    static final String T0 = "T0";
    static final String BCS = "BCS";
    static final String MODCAJ = "MODCAJ";

    /** Concepts of the movements per kind of contract (CSC fund contract / SAAPV pension saving contract). */
    static final String ABNCSC = "ABNCSC";
    static final String ACSCIR = "ACSCIR";
    static final String ACSCER = "ACSCER";
    static final String ACSCID = "ACSCID";
    static final String ABNAPV = "ABNAPV";
    static final String AAPVIR = "AAPVIR";
    static final String AAPVER = "AAPVER";
    static final String AAPVID = "AAPVID";

    private Constantes() {
    }
}
