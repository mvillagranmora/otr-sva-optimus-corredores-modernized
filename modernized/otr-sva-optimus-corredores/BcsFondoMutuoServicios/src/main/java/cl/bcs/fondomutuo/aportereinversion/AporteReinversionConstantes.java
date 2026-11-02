package cl.bcs.fondomutuo.aportereinversion;

/**
 * The part of legacy {@code pojo.ConstantesFondoMutuo} used by {@code CrearSolicitudAporteReinversion} (same literals as the legacy instance fields, here constants: no state).
 */
final class AporteReinversionConstantes {

    static final String SI = "S";
    static final String NO = "N";
    static final String TIPO_SOLICITUD_APORTE = "APO";
    static final String DOLAR = "USD";
    static final String PESOS = "CLP";
    static final String CALCULO_POR_MONTO = "M";
    static final String ESTADO_PENDIENTE = "PEN";
    static final String ESTADO_RECHAZADO = "RZD";
    static final String ESTADO_INGRESADO = "ING";
    static final String ESTADO_RECHAZADO_TESORERIA = "RECH";
    static final String ABIERTO = "A";
    static final String APORTE_REINVERSION = "APOREINV";
    static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    static final String PERFIL_COMERCIAL = "PERFIL_COMERCIAL";
    static final String TIPO_OPERACION = "INFM";
    static final String UF = "UF";
    static final String MODULO_FONDO_MUTUO = "FMM";
    static final String USA_CUENTA_INVERSION = "GEN_OCCINV";
    static final String TRIBUTACION_APV = "ModeloRetiroAPV";
    static final String TRIBUTACION_ART108 = "MayorMenorValor";
    static final String TRIBUTACION_ART107 = "Beneficio107LIR";
    static final String TRIBUTACION_57BIS = "Beneficio57BIS";
    static final String TRIBUTACION_DEFECTO = "MayorMenorValor";
    static final String CUENTA_INVERSION = "CIN";
    static final String USO_FLUJO_CAJA_INGRESO = "INGRESO_APORTE_FMM";
    static final String NUMERO_REFERENCIA_TESORERIA = "NumeroReferencia";
    static final String MODULO_SPOT = "SPT";
    static final String SPOT_ESTADO_RECHAZADO = "RECH";
    static final String SPOT_CALCULA_MARGEN = "S";
    static final String SPOT_ERROR = "ERROR";
    static final String BONIFICACION_FISCAL = "BF";
    static final String FECHA_DEFECTO = "0001-01-01";

    static final String SECUENCIA_SOLICITUD = "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate";
    static final String SECUENCIA_FOLIO_CONTRATO = "BSCL.FondoMutuo.BD.Secuencias::FolioContrato";

    /** Legacy {@code ConstantesFondoMutuo.MAX_REGISTROS}: the size of a lot of contributions and of an IN list. */
    static final int MAX_REGISTROS = 2000;

    private AporteReinversionConstantes() {
    }
}
