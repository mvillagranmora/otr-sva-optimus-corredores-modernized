package cl.bcs.fondomutuo.consultas.solicitudes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Legacy {@code ConsultarSolicitudesFFMMWS.logica} (FM-SFM-01..06; FM-SFM-07/08 the entry date comes from FechaIngresoLocal, read only). Bug for bug: the two parameters are read first, then the nine {@code esFiltro} calls (a null
 * field is a NullPointerException), then {@code PLA.SegundosZonaHoraria} (cast to int), then the SELECT; the global state of each row is computed over the
 * rows of its group (D-F3A-08: mixed states vanish), the amounts are added with DECIMAL128, {@code p_Estado} is checked in memory at the end (null is a
 * NullPointerException) and the order of the SQL result is preserved. The offset is local to the call (DEV: the legacy kept it in a static of the process).
 * No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarSolicitudesWsService implements ConsultarSolicitudesWsPort {

    private static final String SI = "S";
    private static final String NO = "N";
    private static final String REV = "REV";
    private static final String INGRESADO = "INGRESADO";
    private static final String EN_PROCESO = "EN PROCESO";
    private static final String TERMINADO = "TERMINADO";
    private static final String ANULADO = "ANULADO";
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ConfiguracionSistemaPort configuracion;
    private final ConsultarSolicitudesWsDatos datos;

    public ConsultarSolicitudesWsService(ConfiguracionSistemaPort configuracion, ConsultarSolicitudesWsDatos datos) {
        this.configuracion = configuracion;
        this.datos = datos;
    }

    private static class MontoProceso {
        BigDecimal cuotasComprometidas = BigDecimal.ZERO;
        BigDecimal cuotasDisponibles = BigDecimal.ZERO;
        BigDecimal pesosComprometidos = BigDecimal.ZERO;
        BigDecimal pesosDisponibles = BigDecimal.ZERO;
    }

    @Override
    public ConsultarSolicitudesWsContrato.Salida logica(ConsultarSolicitudesWsContrato.Entrada entrada) throws Exception {
        ConsultarSolicitudesWsContrato.Salida out = new ConsultarSolicitudesWsContrato.Salida();
        Map<String, Object> params = obtenerFiltros(entrada);
        List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes = obtenerSolicitudes(params, entrada);
        solicitudes = calcularEstadoGlobal(solicitudes);
        if (!entrada.p_Estado.isEmpty()) {
            String estado = entrada.p_Estado;
            solicitudes = solicitudes.stream().filter(s -> estado.equals(s.Estado)).collect(Collectors.toList());
        } else {
            solicitudes = solicitudes.stream().filter(s -> !"".equals(s.Estado)).collect(Collectors.toList());
        }
        out.P_OUT_SOLICITUDES = solicitudes;
        return out;
    }

    private Map<String, Object> obtenerFiltros(ConsultarSolicitudesWsContrato.Entrada entrada) {
        Map<String, Object> params = new HashMap<>();
        String filtraADC = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FiltroAdministracionCarteraWS");
        String filtraSoloNoAutomaticas = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS");

        params.put("IdCliente", entrada.p_IdCliente);
        params.put("TipoSolicitud", entrada.p_TipoSolicitud);
        params.put("Origen", entrada.p_Origen);
        params.put("Folio", entrada.p_Folio);
        params.put("TipoAhorro", entrada.p_TipoAhorro);
        params.put("TipoCuenta", entrada.p_TipoCuenta);
        params.put("IdSerieFondo", entrada.p_IdSerieFondo);
        params.put("FechaDesde", entrada.p_FechaDesde);
        params.put("FechaHasta", entrada.p_FechaHasta);
        params.put("FiltraTipoMovimiento", SI);
        params.put("FiltraCliente", esFiltro(entrada.p_IdCliente));
        params.put("FiltraTipoSolicitud", esFiltro(entrada.p_TipoSolicitud));
        params.put("FiltraOrigen", esFiltro(entrada.p_Origen));
        params.put("FiltraFolio", esFiltro(entrada.p_Folio));
        params.put("FiltraTipoAhorro", esFiltro(entrada.p_TipoAhorro));
        params.put("FiltraTipoCuenta", esFiltro(entrada.p_TipoCuenta));
        params.put("FiltraIdSerieFondo", esFiltro(entrada.p_IdSerieFondo));
        params.put("FiltraFechaDesde", esFiltro(entrada.p_FechaDesde));
        params.put("FiltraFechaHasta", esFiltro(entrada.p_FechaHasta));

        if (SI.equals(filtraADC)) {
            params.put("EsADC", NO);
            params.put("FiltraADC", SI);
        } else {
            params.put("EsADC", "");
            params.put("FiltraADC", NO);
        }
        if (SI.equals(filtraSoloNoAutomaticas)) {
            params.put("IngresoAutomatico", NO);
            params.put("FiltraIngresoAutomatico", SI);
        } else {
            params.put("IngresoAutomatico", "");
            params.put("FiltraIngresoAutomatico", NO);
        }
        return params;
    }

    private static String esFiltro(String campo) {
        return campo.isEmpty() ? NO : SI;
    }

    private static String esFiltro(Long campo) {
        return campo.equals(0L) ? NO : SI;
    }

    private List<ConsultarSolicitudesWsContrato.Solicitud> obtenerSolicitudes(Map<String, Object> params, ConsultarSolicitudesWsContrato.Entrada entrada) {
        int segundosZonaHoraria = (int) configuracion.entero(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes = datos.solicitudes(params);
        List<ConsultarSolicitudesWsContrato.Solicitud> local = new ArrayList<>();
        for (ConsultarSolicitudesWsContrato.Solicitud sol : solicitudes) {
            LocalDateTime fechaHora = sol.FechaIngresoLocal.toLocalDateTime();
            sol.FechaIngreso = fechaHora.format(FECHA_HORA);
            sol.FechaSolicitud = fechaHora.toLocalDate().toString();
            sol.HoraSolicitud = fechaHora.toLocalTime().toString();
            local.add(sol);
        }
        return local;
    }

    private List<ConsultarSolicitudesWsContrato.Solicitud> calcularEstadoGlobal(List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes) {
        List<ConsultarSolicitudesWsContrato.Solicitud> reinversiones = solicitudes.stream().filter(s -> REV.equals(s.TipoSolicitud)).collect(Collectors.toList());
        List<ConsultarSolicitudesWsContrato.Solicitud> aportesRescates = solicitudes.stream().filter(s -> !REV.equals(s.TipoSolicitud)).collect(Collectors.toList());
        for (ConsultarSolicitudesWsContrato.Solicitud solicitud : solicitudes) {
            MontoProceso montos;
            List<ConsultarSolicitudesWsContrato.Solicitud> asociadas;
            if (REV.equals(solicitud.TipoSolicitud)) {
                asociadas = reinversiones.stream()
                        .filter(r -> (!r.FolioRelacionado.equals(0L) && r.FolioRelacionado.equals(solicitud.FolioRelacionado)) || (r.NumSolicitud.equals(solicitud.NumSolicitud)))
                        .collect(Collectors.toList());
                montos = obtenerMontos(asociadas, true);
            } else {
                asociadas = aportesRescates.stream()
                        .filter(a -> (!a.FolioContrato.equals(0L) && a.FolioContrato.equals(solicitud.FolioContrato)) || (a.NumSolicitud.equals(solicitud.NumSolicitud)))
                        .collect(Collectors.toList());
                montos = obtenerMontos(asociadas, false);
            }
            long ingresadas = contar(INGRESADO, asociadas);
            long enProceso = contar(EN_PROCESO, asociadas);
            long anuladas = contar(ANULADO, asociadas);
            long terminadas = contar(TERMINADO, asociadas);
            solicitud.Estado = calcularEstado(ingresadas, enProceso, anuladas, terminadas, asociadas.size());
            solicitud.MontoCuotasComprometidas = montos.cuotasComprometidas;
            solicitud.MontoPesosComprometidos = montos.pesosComprometidos;
            solicitud.MontoCuotasDisponibles = montos.cuotasDisponibles;
            solicitud.MontoPesosDisponibles = montos.pesosDisponibles;
        }
        return solicitudes;
    }

    private static String calcularEstado(long ingresadas, long enProceso, long anuladas, long terminadas, long total) {
        String estado = "";
        if (anuladas == total) {
            estado = ANULADO;
        }
        if (ingresadas == total) {
            estado = INGRESADO;
        }
        if (terminadas == total) {
            estado = TERMINADO;
        }
        if (enProceso > 0) {
            estado = EN_PROCESO;
        }
        return estado;
    }

    private static long contar(String estadoDetalle, List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes) {
        return solicitudes.stream().filter(s -> estadoDetalle.equals(s.EstadoDetalle)).count();
    }

    /** {@code destino}: reinversion groups add the destination amounts, the others the origin ones. A null addend is a NullPointerException. */
    private static MontoProceso obtenerMontos(List<ConsultarSolicitudesWsContrato.Solicitud> solicitudes, boolean destino) {
        MontoProceso m = new MontoProceso();
        for (ConsultarSolicitudesWsContrato.Solicitud s : solicitudes) {
            if (!TERMINADO.equals(s.EstadoDetalle) && !ANULADO.equals(s.EstadoDetalle)) {
                m.cuotasComprometidas = m.cuotasComprometidas.add(destino ? s.MontoCuotasDestino : s.MontoCuotasOrigen, MathContext.DECIMAL128);
                m.pesosComprometidos = m.pesosComprometidos.add(destino ? s.MontoPesosDestino : s.MontoPesosOrigen, MathContext.DECIMAL128);
            } else if (TERMINADO.equals(s.EstadoDetalle)) {
                m.cuotasDisponibles = m.cuotasDisponibles.add(destino ? s.MontoCuotasDestino : s.MontoCuotasOrigen, MathContext.DECIMAL128);
                m.pesosDisponibles = m.pesosDisponibles.add(destino ? s.MontoPesosDestino : s.MontoPesosOrigen, MathContext.DECIMAL128);
            }
        }
        return m;
    }
}
