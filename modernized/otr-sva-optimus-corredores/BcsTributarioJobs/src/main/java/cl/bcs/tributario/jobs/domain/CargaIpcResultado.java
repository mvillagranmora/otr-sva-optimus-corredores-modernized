package cl.bcs.tributario.jobs.domain;

/**
 * Result of the HANA stored procedure
 * {@code BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc}.
 *
 * <p>Maps the two OUT parameters returned by the SP:</p>
 * <ul>
 *   <li>{@code advertenciasContador} — OUT param 6 (HANA types.INTEGER)</li>
 *   <li>{@code erroresContador}      — OUT param 7 (HANA types.INTEGER)</li>
 * </ul>
 */
public record CargaIpcResultado(int advertenciasContador, int erroresContador) {

    /**
     * Returns {@code true} when no errors were reported by the SP.
     * B-TRI-02: erroresContador == 0 → job completed successfully.
     */
    public boolean exitoso() {
        return erroresContador == 0;
    }
}
