package cl.bcs.tesoreria.domain.model;

public record DepositanteResult(
        String idDepositante,
        String estadoDeposito,
        String codigoRespuesta,
        boolean existe
) {
    public static DepositanteResult valido() {
        return new DepositanteResult(null, "ACTIVO", "CP0000", true);
    }

    public static DepositanteResult noExiste() {
        return new DepositanteResult(null, null, "CP0010", false);
    }
}
