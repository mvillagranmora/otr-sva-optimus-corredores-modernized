package cl.bcs.plataforma.jobs.service.parametros;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Service for ActualizarParametrosFinancieros (financial parameter updates).
 *
 * Pure-logic methods are public static so they can be tested without HANA.
 * Methods that require a DB connection delegate to the HANA adapter (marked @Disabled in tests).
 *
 * Business rules extracted from:
 *   legacy cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros
 *
 * B-PLJ-01: IPC date correction — Bursatil delivers IPC with one extra month;
 *            correct by subtracting 1 month and setting to day 1.
 * B-PLJ-02: Dolar observado correction — WS date is 1 day ahead;
 *            correct by shifting fechaSistemaAnterior→fechaSistema and fechaSistema→fechaSistemaSiguiente.
 * B-PLJ-03: Paridad en USD — for currencies whose IdMonedaValor = "USD",
 *            valor = rawValor / dolarObservado (8 decimal places, FLOOR).
 * B-PLJ-04: Non-trading-day fill — if fechaSistemaSiguiente is more than 1 calendar day after
 *            fechaSistema, fill each intermediate calendar day with the same value.
 */
@Service
public class ActualizarParametrosFinancierosService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarParametrosFinancierosService.class);

    // -------------------------------------------------------------------------
    // Pure-logic methods (public static — testable without HANA)
    // -------------------------------------------------------------------------

    /**
     * B-PLJ-01: Correct IPC date received from Bursatil WS.
     * Bursatil delivers the IPC date one month ahead; subtract 1 month and
     * set day to 1.
     *
     * @param fecha ISO date string as received from Bursatil (e.g. "2025-04-01")
     * @return corrected date string (e.g. "2025-03-01"), or original if unparseable
     */
    public static String corregirFechaIPC(String fecha) {
        Objects.requireNonNull(fecha, "fecha must not be null");
        try {
            LocalDate fechaDate = LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);
            return fechaDate.minusMonths(1).withDayOfMonth(1)
                    .format(DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.warn("Cannot parse IPC date '{}'; returning as-is", fecha, e);
            return fecha;
        }
    }

    /**
     * B-PLJ-02: Correct the dolar-observado date.
     * The WS delivers dolar observado 1 day ahead:
     *   - If wsDate == fechaSistemaAnterior  → corrected = fechaSistema
     *   - If wsDate == fechaSistema          → corrected = fechaSistemaSiguiente
     *   - Otherwise keep wsDate
     * Additionally, if wsDate == fechaSistemaSiguiente the value should NOT be stored.
     *
     * @return corrected date, or null if the record should be skipped
     */
    public static String corregirFechaDolarObservado(
            String wsDate,
            String fechaSistemaAnterior,
            String fechaSistema,
            String fechaSistemaSiguiente) {

        Objects.requireNonNull(wsDate, "wsDate must not be null");
        Objects.requireNonNull(fechaSistemaAnterior, "fechaSistemaAnterior must not be null");
        Objects.requireNonNull(fechaSistema, "fechaSistema must not be null");
        Objects.requireNonNull(fechaSistemaSiguiente, "fechaSistemaSiguiente must not be null");

        if (fechaSistemaSiguiente.equals(wsDate)) {
            // skip — do not store this date
            return null;
        }
        if (fechaSistemaAnterior.equals(wsDate)) {
            return fechaSistema;
        }
        if (fechaSistema.equals(wsDate)) {
            return fechaSistemaSiguiente;
        }
        return wsDate;
    }

    /**
     * B-PLJ-03: Compute the parity-in-USD value.
     * valor = rawValor / dolarObservado (8 decimal places, FLOOR).
     * Returns rawValor unchanged if dolarObservado is null or zero.
     *
     * @param rawValor       the raw value for the currency
     * @param dolarObservado the observed dollar rate on the system date
     */
    public static BigDecimal calcularParidadEnUSD(BigDecimal rawValor, BigDecimal dolarObservado) {
        Objects.requireNonNull(rawValor, "rawValor must not be null");
        if (dolarObservado == null || dolarObservado.compareTo(BigDecimal.ZERO) == 0
                || rawValor.compareTo(BigDecimal.ZERO) == 0) {
            return rawValor;
        }
        return rawValor.divide(dolarObservado, 8, RoundingMode.FLOOR);
    }

    /**
     * B-PLJ-04: Determine whether non-trading days exist between fechaSistema
     * and fechaSistemaSiguiente (i.e. gap > 1 calendar day).
     *
     * @param fechaSistema          current system date (ISO)
     * @param fechaSistemaSiguiente next trading-day date (ISO)
     * @return true if there is at least one non-trading calendar day in the gap
     */
    public static boolean hayDiasNoHabiles(String fechaSistema, String fechaSistemaSiguiente) {
        Objects.requireNonNull(fechaSistema, "fechaSistema must not be null");
        Objects.requireNonNull(fechaSistemaSiguiente, "fechaSistemaSiguiente must not be null");
        LocalDate actual = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE);
        LocalDate siguiente = LocalDate.parse(fechaSistemaSiguiente, DateTimeFormatter.ISO_DATE);
        return actual.until(siguiente).getDays() > 1;
    }

    // -------------------------------------------------------------------------
    // HANA-dependent execution — stubbed
    // -------------------------------------------------------------------------

    /**
     * Runs the full financial parameter update against HANA.
     * This method calls the Bursatil REST WS and HANA tables/SPs.
     *
     * @throws UnsupportedOperationException always — wire the HANA datasource to enable
     */
    public void ejecutar(Long idEmpresa) {
        throw new UnsupportedOperationException(
                "ActualizarParametrosFinancierosService.ejecutar requires a live HANA connection");
    }
}
