package cl.bcs.fondomutuo.normal;

/**
 * The part of legacy {@code pojo.ConstantesFondoMutuo} used by {@code CrearSolicitudAporteRescateNormal} (same names and literals as the legacy instance
 * fields, here constants: no state). The ones the 3A/3B constant classes already have are repeated on purpose, so this class reads like the legacy.
 */
final class NormalConstantes {

    static final String SI = "S";
    static final String NO = "N";
    static final String TIPO_SOLICITUD_APORTE = "APO";
    static final String TIPO_SOLICITUD_RESCATE = "RES";
    static final String DOLAR = "USD";
    static final String PESOS = "CLP";
    static final String CALCULO_POR_TOTAL = "T";
    static final String ESTADO_PENDIENTE = "PEN";
    static final String ESTADO_RECHAZADO = "RZD";
    static final String ESTADO_INGRESADO = "ING";
    static final String ESTADO_RECHAZADO_TESORERIA = "RECH";
    static final String ABIERTO = "A";
    static final String CERRADO = "C";
    static final String RESCATE_TRASPASO = "RESTRASP";
    static final String APORTE_TRASPASO = "APOTRASP";
    static final String APORTE_FONDO_MUTUO = "APONORM";
    static final String RESCATE_FONDO_MUTUO = "RESNORM";
    static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    static final String TIPO_OPERACION_RESCATE_FONDO_MUTUO = "RESCFM";
    static final String PERFIL_COMERCIAL = "PERFIL_COMERCIAL";
    static final String TIPO_OPERACION = "INFM";
    static final String UF = "UF";
    static final String MODULO_FONDO_MUTUO = "FMM";
    static final String USA_CUENTA_INVERSION = "GEN_OCCINV";
    static final String FECHA_DEFECTO = "0001-01-01";
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
    static final String MODULO_TESORERIA = "TES";
    static final String RAZON_RETIRO_TRASPASO = "TRA";
    static final String BONIFICACION_FISCAL = "BF";
    static final String TIPO_COMISION_TRIMESTRAL = "PER";
    static final String ORIGEN_RECAUDACION = "REC";
    static final String ORIGEN_PAC = "PAC";
    static final String ORIGEN_BONIFICACION = "BONIF";
    static final String ORIGEN_CARGA_MASIVA_FM = "CAR";
    static final String CSC = "CSC";
    static final String SAAPV = "SAAPV";

    static final String SECUENCIA_SOLICITUD = "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate";
    static final String SECUENCIA_FOLIO_CONTRATO = "BSCL.FondoMutuo.BD.Secuencias::FolioContrato";

    private NormalConstantes() {
    }
}
