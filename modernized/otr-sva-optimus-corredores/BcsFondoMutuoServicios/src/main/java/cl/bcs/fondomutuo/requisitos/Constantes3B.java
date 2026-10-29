package cl.bcs.fondomutuo.requisitos;

/**
 * Values of legacy {@code cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo} used by the step 3B classes (same strings, copied verbatim). Own holder so the
 * shared domain constants are not touched by two steps at once; step 4 may unify them.
 */
public final class Constantes3B {

    private Constantes3B() {
    }

    public static final String SI = "S";
    public static final String NO = "N";
    public static final String TIPO_SOLICITUD_APORTE = "APO";
    public static final String TIPO_SOLICITUD_RESCATE = "RES";
    public static final String ESTADO_ACTIVO = "ACT";
    public static final String ESTADO_PENDIENTE = "PEN";
    public static final String ESTADO_RECHAZADO = "RZD";
    public static final String ESTADO_INGRESADO = "ING";
    public static final String ABIERTO = "A";
    public static final String REINVERSION = "RESREINV";
    public static final String TRANSFERENCIA = "RESTRANSF";
    public static final String RESCATE_TRASPASO = "RESTRASP";
    public static final String TRASPASO_PORTAFOLIO = "RESTRAPOR";
    public static final String INGRESO_TITULO = "INGTIT";
    public static final String APORTE_REINVERSION = "APOREINV";
    public static final String TIPO_OPERACION_APORTE_FONDO_MUTUO = "APORFM";
    public static final String TIPO_OPERACION_RESCATE_FONDO_MUTUO = "RESCFM";
    public static final String TIPO_OPERACION = "INFM";
    public static final String CALCULO_POR_CUOTA = "C";
    public static final String UF = "UF";
    public static final String ORIGEN_MANUAL = "M";
    public static final String ORIGEN_PAC = "PAC";
    public static final String TIPO_REGISTRO_PARTICIPE = "PA";
    public static final String TIPO_REGISTRO_TRASPASO = "TR";
    public static final String TIPO_REGISTRO_APORTE_RESCATE = "AR";
    public static final String MODULO_FONDO_MUTUO = "FMM";
    public static final String MEDIO_PAGO_TRANSFERENCIA = "TRA";
    public static final String RESCATE_COMISION_NN = "RESCNN";
    public static final String TRIBUTACION_APV = "ModeloRetiroAPV";
    public static final String TRIBUTACION_ART108 = "MayorMenorValor";
    public static final String TRIBUTACION_ART107 = "Beneficio107LIR";
    public static final String TRIBUTACION_57BIS = "Beneficio57BIS";
    public static final String TRIBUTACION_DEFECTO = "MayorMenorValor";
}
