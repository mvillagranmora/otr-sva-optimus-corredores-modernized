package cl.bcs.fondomutuo.cierre;

/**
 * The subset of legacy {@code cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo} that the pure closure of step 3A uses (same names and literals; the legacy
 * class had them as instance fields, here they are constants: no state). Step 3A, packages {@code fechas}, {@code cuotas}, {@code comisiones}.
 */
public final class ConstantesCierre {

    public static final String SI = "S";
    public static final String NO = "N";

    public static final String TIPO_SOLICITUD_APORTE = "APO";
    public static final String TIPO_SOLICITUD_RESCATE = "RES";

    public static final String DOLAR = "USD";

    public static final String CALCULO_POR_MONTO = "M";

    public static final String REINVERSION = "RESREINV";

    public static final String COMISION_SALIDA = "SALIDA";
    public static final String COMISION_ENTRADA = "ENTRADA";
    public static final String COMISION_TRIMESTRAL = "PER";

    public static final String MERCADO_RF = "RF";

    public static final String TIPO_FECHA_DIGITACION = "DIGITA";
    public static final String TIPO_FECHA_CURSE = "CURSE";
    public static final String TIPO_FECHA_RETENCION = "LIQUID";
    public static final String TIPO_FECHA_VALOR_CUOTA = "VCUOTA";
    public static final String TIPO_FECHA_CERTIFICACION = "CERTIF";

    private ConstantesCierre() {
    }
}
