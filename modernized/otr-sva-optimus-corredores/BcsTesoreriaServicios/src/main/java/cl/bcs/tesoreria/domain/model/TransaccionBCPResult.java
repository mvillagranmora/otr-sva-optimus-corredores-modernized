package cl.bcs.tesoreria.domain.model;

public record TransaccionBCPResult(
        String idTransaccion,
        String codigoRespuesta,
        String severity,
        boolean transaccionExitosa
) {
    public static TransaccionBCPResult exitosa() {
        return new TransaccionBCPResult(null, "CP0000", "INFORMACION", true);
    }
}
