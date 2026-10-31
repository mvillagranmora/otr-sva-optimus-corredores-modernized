package cl.bcs.fondomutuo.certificacion;

/** Literals of legacy {@code ConstantesFondoMutuo} that fronts F-A and F-B use (same strings). */
final class CerConstantes {

    static final String SI = "S";
    static final String NO = "N";
    static final String TIPO_SOLICITUD_APORTE = "APO";
    static final String ESTADO_PENDIENTE = "PEN";
    static final String ESTADO_RECHAZADO = "RZD";
    static final String ESTADO_CERTIFICADO = "CRT";
    static final String ESTADO_INGRESADO = "ING";
    static final String ESTADO_LIQUIDADO = "LIQ";
    static final String ESTADO_ACEPTADO = "ACP";
    static final String ESTADO_ADVERTENCIA = "ADV";
    static final String APORTE_REINVERSION = "APOREINV";
    static final String BONIFICACION_FISCAL = "BF";
    static final String APV_B = "APV-B";
    static final String UF = "UF";
    static final String OK = "OK";

    // F-C and F-D (same literals as legacy ConstantesFondoMutuo)
    static final String TIPO_SOLICITUD_RESCATE = "RES";
    static final String ORIGEN_MANUAL = "M";
    static final String CALCULO_POR_TOTAL = "T";
    static final String RESCATE_COMISION_NN = "RESCNN";
    static final String RESCATE_FONDO_MUTUO = "RESNORM";
    static final String DOLAR = "USD";
    static final String PESOS = "CLP";
    static final String APV_AN = "APV-AN";
    static final String CUENTA_INVERSION = "CIN";
    static final String REGIMEN_18Q = "18Q";
    static final String USO_FLUJO_CAJA_CERTIFICACION = "CERTIFICACION_APORTE_FMM";
    static final String USO_FLUJO_CAJA_CERTIFICACION_PAC = "CERTIFICACION_APORTE_FMM_PAC";
    static final String TIPO_PRODUCTO_CUSTODIA = "INFM";
    static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    static final String TIPO_OPERACION_RESCATE_FONDO_MUTUO = "RESCFM";
    static final String TIPO_COMPROBANTE_CUSTODIA = "FFMM";
    static final String BCS = "BCS";
    static final String COR = "COR";

    // F-E, F-F and F-H (same literals as legacy ConstantesFondoMutuo)
    static final String CARGO_CUENTA_INVERSION = "MOVCAR";
    static final String ABONO_CUENTA_INVERSION = "MOVABN";
    static final String TIPO_COMPROBANTE_CUENTA_INVERSION = "FFM";
    static final String REINVERSION = "RESREINV";
    static final String RESCATE_COMISION = "RESCOM";
    static final String MEDIO_PAGO_TRANSFERENCIA = "TRA";
    static final String ESTADO_RECHAZADO_TESORERIA = "RECH";
    static final String NUMERO_REFERENCIA_TESORERIA = "NumeroReferencia";
    static final String SPOT_COMPRA = "COMSPT";
    static final String SPOT_VENTA = "VENSPT";
    static final String SPOT_ESTADO_RECHAZADO = "RECH";
    static final String SPOT_CALCULA_MARGEN = "S";
    static final String SPOT_MOVIMIENTO_INGRESO = "MOVING";
    static final String SPOT_MOVIMIENTO_EGRESO = "MOVEGR";
    static final String SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO = "0";
    static final String TIPO_COMISION_TRIMESTRAL = "PER";

    // F-G (invoicing of the commission) and F-I (reinversion), same literals as legacy ConstantesFondoMutuo
    static final String RESCATE_TRASPASO_PORTAFOLIO = "RESTRAPOR";
    static final String APV = "APV";
    static final String CONCEPTO_COMISION_APV = "COSAPV";
    static final String CONCEPTO_COMISION_FM = "COSAFM";
    static final String CONCEPTO_COBRO_APV = "COCAPV";
    static final String CONCEPTO_COBRO_FM = "COAFFM";
    static final String TIPO_COMPROBANTE_COMISION = "FOE";
    static final String CONDICION_LIQUIDACION_T0 = "T0";
    static final String TIPO_CONVENIO_SCV = "SCV";
    static final String FORMA_CUSTODIA_CUS = "CUS";
    static final String CANAL_AGENTE = "AGE";
    static final String SWITCH = "S";

    private CerConstantes() {
    }
}
