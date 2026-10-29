package cl.bcs.cartolas.service;

import cl.bcs.cartolas.domain.Nomina;
import cl.bcs.cartolas.domain.Parametro;
import cl.bcs.cartolas.domain.TipoProceso;
import cl.bcs.cartolas.port.in.NominaUseCase;
import cl.bcs.cartolas.port.out.NominaPort;
import cl.bcs.cartolas.port.out.ParametroPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de nóminas — orquesta las consultas sobre Nóminas y sus Parámetros.
 */
@Service
public class NominaService implements NominaUseCase {

    private final NominaPort    nominaPort;
    private final ParametroPort parametroPort;

    public NominaService(final NominaPort nominaPort, final ParametroPort parametroPort) {
        this.nominaPort    = Objects.requireNonNull(nominaPort,    "nominaPort es requerido");
        this.parametroPort = Objects.requireNonNull(parametroPort, "parametroPort es requerido");
    }

    @Override
    public Optional<Nomina> getNomina(final long idNomina) {
        return nominaPort.findById(idNomina);
    }

    @Override
    public List<Nomina> getNominasProgramadas() {
        return nominaPort.findByTipoProceso(TipoProceso.PROGRAMADO);
    }

    @Override
    public List<Parametro> getParametros(final long idNomina) {
        return parametroPort.findByIdNomina(idNomina);
    }

    // -------------------------------------------------------------------------
    // Pure-logic helpers (public static para facilitar tests)
    // -------------------------------------------------------------------------

    /**
     * Determina el período de cartolas (mes anterior al actual).
     * Pure-logic: sin dependencias externas.
     */
    public static String getPeriodoCartolas(final java.time.LocalDate hoy) {
        Objects.requireNonNull(hoy, "hoy es requerido");
        final java.time.LocalDate primerDelMes = hoy.withDayOfMonth(1);
        final java.time.LocalDate ultimoDiaMesAnterior = primerDelMes.minusDays(1);
        return ultimoDiaMesAnterior.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * Evalúa si se cumplen las condiciones de ejecución programada.
     * Pure-logic: recibe los parámetros como primitivos.
     */
    public static boolean condicionesEjecucionCumplidas(final java.util.Map<String, String> parametros,
                                                         final java.time.LocalDateTime ahora) {
        Objects.requireNonNull(parametros, "parametros es requerido");
        Objects.requireNonNull(ahora,      "ahora es requerido");

        final String habilitadoStr = parametros.get("Habilitado");
        if (!Boolean.parseBoolean(habilitadoStr)) {
            return false;
        }

        final int dd = Integer.parseInt(parametros.get("DiaEjecucion"));
        if (ahora.getDayOfMonth() != dd) {
            return false;
        }

        final String hhmm = parametros.get("HoraEjecucion");
        final String[] partes = hhmm.split(":");
        final int hh = Integer.parseInt(partes[0]);
        final int mm = Integer.parseInt(partes[1]);

        if (ahora.getHour() != hh) {
            return false;
        }
        final int minuto = ahora.getMinute();
        return minuto > mm && minuto < mm + 1;
    }
}
