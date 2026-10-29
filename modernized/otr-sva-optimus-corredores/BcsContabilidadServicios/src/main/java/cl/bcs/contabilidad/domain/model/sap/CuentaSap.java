package cl.bcs.contabilidad.domain.model.sap;

/**
 * Row of the SAP B1 chart of accounts ({@code <company>.OACT}, EnvioContabilidad_SelectOACT.sql).
 * {@code LocManTran = "Y"} marks an analytic (partner) account; {@code ActType} E/I a cost-centre account.
 */
public record CuentaSap(String AcctCode, String LocManTran, String ActType, Short GroupMask) {

    /** Legacy {@code "Y".equals(LocManTran)}: case sensitive (GenerarJsonAsientoSap.java:233, CONT-B-27). */
    public boolean esAnalitica() {
        return "Y".equals(LocManTran);
    }

    /** Legacy ActType E (salida) or I (ventas), ignoring case (EnvioContabilidad.java:477-478). */
    public boolean usaCentroCosto() {
        return "E".equalsIgnoreCase(ActType) || "I".equalsIgnoreCase(ActType);
    }
}
