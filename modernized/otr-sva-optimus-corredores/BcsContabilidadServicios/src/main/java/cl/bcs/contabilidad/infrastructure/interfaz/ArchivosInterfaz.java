package cl.bcs.contabilidad.infrastructure.interfaz;

import java.util.Objects;

/**
 * File names of one generator family: the data file and its control file.
 *
 * <p>Legacy: the jobs passed {@code nombreArchivo} and {@code nombreArchivoCtr} to every {@code Crea*.logica}, and the
 * control file names its data file in DES-ESTRUCTURA ({@code String.format("%1$-30s", nombreArchivo)}, for example
 * CreaBroadCuentas.java:175). CreaInterfazDiaria used upper-case {@code .TXT} names (CreaInterfazDiaria.java:120-186);
 * CreaInterfazDemanda used {@code B1_BROAD_CTAS.txt} and the {@code *_BPC_DM.TXT} names
 * (CreaInterfazDemanda.java:122-123, :179-180, :197-198).
 *
 * @param datos   name of the data file
 * @param control name of the control file
 */
public record ArchivosInterfaz(String datos, String control) {

    public ArchivosInterfaz {
        Objects.requireNonNull(datos, "datos");
        Objects.requireNonNull(control, "control");
    }
}
