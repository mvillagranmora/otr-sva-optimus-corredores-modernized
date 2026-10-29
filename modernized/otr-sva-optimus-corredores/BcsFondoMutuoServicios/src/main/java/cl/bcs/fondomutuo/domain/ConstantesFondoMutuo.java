package cl.bcs.fondomutuo.domain;

/**
 * Domain constants for the FondoMutuo (Mutual Fund) module.
 * Migrated from: cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo
 */
public final class ConstantesFondoMutuo {

    private ConstantesFondoMutuo() {}

    public static final String SI = "S";
    public static final String NO = "N";

    public static final String TIPO_SOLICITUD_APORTE = "APO";
    public static final String TIPO_SOLICITUD_RESCATE = "RES";

    public static final String DOLAR = "USD";
    public static final String PESOS = "CLP";

    public static final String CALCULO_POR_CUOTA = "C";
    public static final String CALCULO_POR_MONTO = "M";
    public static final String CALCULO_POR_TOTAL = "T";
    public static final String CALCULO_POR_PORCENTAJE = "P";

    public static final String ESTADO_ACTIVO = "ACT";
    public static final String ESTADO_PENDIENTE = "PEN";
    public static final String ESTADO_RECHAZADO = "RZD";
    public static final String ESTADO_CERTIFICADO = "CRT";
    public static final String ESTADO_INGRESADO = "ING";
    public static final String ESTADO_LIQUIDADO = "LIQ";
    public static final String ESTADO_ACEPTADO = "ACP";
    public static final String ESTADO_APROBADO = "APD";
    public static final String ESTADO_ADVERTENCIA = "ADV";
    public static final String ESTADO_ERROR = "ERR";
    public static final String ESTADO_ANULADO = "ANU";
    public static final String ESTADO_RECHAZADO_TESORERIA = "RECH";
    public static final String ESTADO_VIGENTE = "VIG";
    public static final String ESTADO_PROCESADO = "PRO";
    public static final String ESTADO_RES = "RES";
    public static final String ESTADO_CONFIRMADO = "CNF";

    public static final String ABIERTO = "A";
    public static final String CERRADO = "C";

    public static final String REINVERSION = "RESREINV";
    public static final String TRANSFERENCIA = "RESTRANSF";
    public static final String RESCATE_TRASPASO = "RESTRASP";
    public static final String APORTE_TRASPASO = "APOTRASP";
    public static final String TRASPASO_PORTAFOLIO = "RESTRAPOR";
    public static final String CAMBIO_SERIE = "RESCAM";
    public static final String INGRESO_TITULO = "INGTIT";
    public static final String RETIRO_TITULO = "RETTIT";
    public static final String APORTE_FONDO_MUTUO = "APONORM";
    public static final String APORTE_DIVIDENDO_CFI = "APODIVCFI";
    public static final String RESCATE_FONDO_MUTUO = "RESNORM";
    public static final String APORTE_RESCATE_PROGRAMADO = "APORESPRO";
    public static final String APORTE_OVERNIGHT = "APOOVER";
    public static final String APORTE_REINVERSION = "APOREINV";
    public static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    public static final String TIPO_OPERACION_RESCATE_FONDO_MUTUO = "RESCFM";
    public static final String TIPO_OPERACION_ANULACION_APORTE_FONDO_MUTUO = "ANUAPO";
    public static final String TIPO_OPERACION_ANULACION_RESCATE_FONDO_MUTUO = "ANURES";
    public static final String INGRESO_GARANTIA = "INGGTA";
    public static final String RETIRO_GARANTIA = "RETGTA";
    public static final String APORTE_FUSION = "APOFUSION";
    public static final String RESCATE_FUSION = "RESFUSION";

    public static final String PERFIL_COMERCIAL = "PERFIL_COMERCIAL";
    public static final String PERFIL_BACKOFFICE = "PERFIL_BACKOFFICE";

    public static final String RETIRO = "RETIRO";
    public static final String INGRESO = "INGRESO";

    public static final String TIPO_OPERACION = "INFM";
    public static final String TIPO_PRODUCTO = "INFM";

    public static final String CONCEPTO_APORTE_TRANSFERENCIA = "APOTRN";
    public static final String CONCEPTO_RESCATE_TRANSFERENCIA = "RESTRN";
    public static final String CONCEPTO_APORTE_TRASPASO = "APOTRS";
    public static final String CONCEPTO_RESCATE_TRASPASO = "RESTRS";
    public static final String CONCEPTO_RESCATE_CAMBIO_SERIE = "RESCAM";
    public static final String CONCEPTO_APORTE_CAMBIO_SERIE = "APORFM";
    public static final String CONCEPTO_RETIRO_TITULO = "RETIFM";
    public static final String CONCEPTO_INGRESO_TITULO = "INTIFM";
    public static final String CONCEPTO_APORTE_NORMAL = "APORFM";
    public static final String CONCEPTO_RESCATE_NORMAL = "RESCFM";
    public static final String CONCEPTO_APORTE_REINVERSION = "APOREI";
    public static final String CONCEPTO_APORTE_DIVIDENDO = "APODIV";
    public static final String CONCEPTO_APORTE_OVERNIGHT = "APOOVE";
    public static final String CONCEPTO_APORTE_RESCATE_PROGRAMADO = "APOPRO";
    public static final String CONCEPTO_APORTE_NORMAL_DOLAR = "APODOL";
    public static final String CONCEPTO_RESCATE_NORMAL_DOLAR = "RESDOL";
    public static final String CONCEPTO_TRASPASO_PORTAFOLIO = "RESPOR";
    public static final String CONCEPTO_APORTE_FUSION = "APOFUS";
    public static final String CONCEPTO_RESCATE_FUSION = "RESFUS";
    public static final String CONCEPTO_TRASPASO_INGRESO = "TRAING";
    public static final String CONCEPTO_ANULACION_APORTE = "ANUAPO";
    public static final String CONCEPTO_ANULACION_RESCATE = "ANURES";

    public static final String TRANSITO = "TRANSITO";
    public static final String CARTERA = "CARTERA";

    public static final String TIPO_COMPROBANTE_CUSTODIA = "FFMM";

    public static final String BCS = "BCS";
    public static final String UF = "UF";
    public static final String COR = "COR";
    public static final String OK = "OK";

    public static final String ORIGEN_MANUAL = "M";

    public static final String TIPO_REGISTRO_PARTICIPE = "PA";
    public static final String TIPO_REGISTRO_TRASPASO = "TR";
    public static final String TIPO_REGISTRO_APORTE_RESCATE = "AR";

    public static final String CUS = "CUS";

    public static final String MODULO_FONDO_MUTUO = "FMM";
    public static final String TIPO_COMPROBANTE_LIQUIDACION_TESORERIA = "LFM";
    public static final String TIPO_COMPROBANTE_TESORERIA = "FMM";
    public static final String TIPO_COMPROBANTE_CUENTA_INVERSION = "FFM";

    public static final String COMISION_SALIDA = "SALIDA";
    public static final String COMISION_ENTRADA = "ENTRADA";

    public static final String USA_CUENTA_INVERSION = "GEN_OCCINV";

    public static final String FECHA_DEFECTO = "0001-01-01";

    public static final String TRIBUTACION_APV = "ModeloRetiroAPV";
    public static final String TRIBUTACION_ART108 = "MayorMenorValor";
    public static final String TRIBUTACION_ART107 = "Beneficio107LIR";
    public static final String TRIBUTACION_57BIS = "Beneficio57BIS";
    public static final String TRIBUTACION_DEFECTO = "MayorMenorValor";

    public static final String CUENTA_INVERSION = "CIN";

    public static final String USO_FLUJO_CAJA_INGRESO = "INGRESO_APORTE_FMM";
    public static final String USO_FLUJO_CAJA_CERTIFICACION = "CERTIFICACION_APORTE_FMM";

    public static final String INGRESO_TESORERIA = "MOVING";
    public static final String EGRESO_TESORERIA = "MOVEGR";
    public static final String TIPO_PERSONA_CLIENTE = "CLI";
    public static final String TIPO_PERSONA_CONTRAPARTE = "CNT";

    public static final String T0 = "T0";
    public static final String TIPO_INSTRUMENTO = "FMM";

    public static final String CANAL_AGENTE = "AGE";
    public static final String CANAL_WS = "WS";
    public static final String CANAL_WEB = "WEB";
    public static final String CANAL_OPERADOR = "OPE";
    public static final String CANAL_EJECUTIVO = "EJE";
    public static final String CANAL_OPTIMUS = "OPT";

    public static final String ORIGEN_RECAUDACION = "REC";
    public static final String ORIGEN_PAC = "PAC";
    public static final String ORIGEN_BONIFICACION = "BONIF";
    public static final String ORIGEN_CARGA_MASIVA_FM = "CAR";

    public static final String FM = "FM";
    public static final String FFMM = "FFMM";

    public static final String CSC = "CSC";
    public static final String SAAPV = "SAAPV";
    public static final String OWP = "OWP";
    public static final String APV = "APV";
    public static final String APV_B = "APV-B";
    public static final String APV_AN = "APV-AN";

    public static final String MERCADO_RF = "RF";
    public static final String TIPO_FECHA_DIGITACION = "DIGITA";
    public static final String TIPO_FECHA_CURSE = "CURSE";
    public static final String TIPO_FECHA_RETENCION = "LIQUID";
    public static final String TIPO_FECHA_VALOR_CUOTA = "VCUOTA";
    public static final String TIPO_FECHA_CERTIFICACION = "CERTIF";

    public static final Long ID_PRODUCTO_AGRUPADOR_NO_APV = 9999L;

    public static final String REGIMEN_18Q = "18Q";
    public static final String TIPO_MOV_TRANSFERENCIA = "MOVTRN";

    public static final Integer MAX_REGISTROS = 2000;

    public static final String RESCATE_COMISION = "RESCOM";
    public static final String RESCATE_COMISION_NN = "RESCNN";
    public static final String TIPO_COMISION_TRIMESTRAL = "PER";
}
