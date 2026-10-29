package cl.bcs.contabilidad.domain.model.sap;

/**
 * Staged header: element of EnvioContabilidad {@code P_OUT_PRECOMPROBANTE} and row of
 * {@code ITZ.PreComprobanteEnvioContabilidad} (legacy EnvioContabilidad.PreComprobanteSalida, :119-128).
 */
public record PreComprobanteSalida(Long JdtNum, String ReferenceDate, String Memo, String DueDate, String TaxDate) {

    static final int LARGO_MEMO = 50;

    /** The stored copy cuts Memo at 50 characters; the JavaCall output keeps it whole (EnvioContabilidad.java:661-671). */
    public PreComprobanteSalida paraInsertar() {
        return Memo == null || Memo.length() <= LARGO_MEMO ? this
                : new PreComprobanteSalida(JdtNum, ReferenceDate, Memo.substring(0, LARGO_MEMO), DueDate, TaxDate);
    }
}
