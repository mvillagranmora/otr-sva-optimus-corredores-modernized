package cl.bcs.contabilidad.infrastructure.interfaz;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * FEC-PROCESO / HRA-PROCESO columns of the control files (CONT-A-28).
 *
 * <p>Legacy ({@code CreaBroadSaldo.java:203-210} and the same lines in every {@code Crea*} class): the data file's
 * {@code lastModified()} read through a {@code Calendar} in the JVM zone, then
 * <ul>
 *   <li>FEC-PROCESO = {@code YEAR + (MONTH + 1) + DAY_OF_MONTH} as an integer SUM, padded to 8
 *       (2026-10-13 gives {@code 2049});</li>
 *   <li>HRA-PROCESO = 12-hour {@code HOUR + ":" + MINUTE + ":" + SECOND}, unpadded, padded to 10
 *       (15:04:05 gives {@code 3:4:5}).</li>
 * </ul>
 * The data file has just been written, so its mtime is the injected clock's instant. CTAS and MOV_CONT stat a path
 * that does not exist, so they always print the epoch ({@link #epoca}).
 */
record MarcaProceso(String fecProceso, String hraProceso) {

    static MarcaProceso de(Instant instante, ZoneId zona) {
        ZonedDateTime t = instante.atZone(zona);
        int suma = t.getYear() + t.getMonthValue() + t.getDayOfMonth();
        String hora = (t.getHour() % 12) + ":" + t.getMinute() + ":" + t.getSecond();
        return new MarcaProceso(ArchivoPlano.col(suma, 8), ArchivoPlano.col(hora, 10));
    }

    /** {@code new File(missing).lastModified() == 0}: the epoch in the given zone. */
    static MarcaProceso epoca(ZoneId zona) {
        return de(Instant.EPOCH, zona);
    }
}
