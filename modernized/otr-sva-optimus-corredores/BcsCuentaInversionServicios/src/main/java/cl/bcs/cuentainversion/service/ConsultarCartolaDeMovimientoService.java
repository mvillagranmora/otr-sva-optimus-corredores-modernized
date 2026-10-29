package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.CartolaDeMovimiento;
import cl.bcs.cuentainversion.domain.ConsultaMovimiento;
import cl.bcs.cuentainversion.domain.MovimientoCuentaInversion;
import cl.bcs.cuentainversion.domain.colaborador.RevisarRequisitosBasicos;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Movement statement (cartola) of an investment account portfolio: the accounts matching the filters, their initial balance
 * (SaldoCuentaInversionService) and their movements (ConsultarMovimientoCuentaInversionService), with the invoice instrument
 * names appended to FAC movements (README CTA-A-189..207; legacy ConsultarCartolaDeMovimiento).
 */
@Service
public class ConsultarCartolaDeMovimientoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCartolaDeMovimientoService.class);

    private static final String SQL_PORTAFOLIO_MONEDA = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarCartolaDeMovimiento_SelectPortafolioMoneda.sql");
    private static final String SQL_NEMOS = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarCartolaDeMovimiento_SelectNemosMovimiento.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdCuentaInversion;
        public Long p_IdCliente;
        public Long p_IdPortafolio;
        public String p_IdMoneda;
        public String p_IdEstado;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public String p_Validaciones = "S";
        public String p_SaldoPorFechaEfectiva = "S";
        public Long p_IdProductoAgrupador;
        public Long p_IdMatrizAgrupador;
        public String p_EsADC;
        public String p_IncluyeSaldo = "N";
        public String p_IncluyeAnulados = "S";
    }

    public static class Salida {
        public List<CartolaDeMovimiento> P_OUT_CARTOLADEMOVIMIENTO = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<Object> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    private record Nemo(Long idComprobante, String nemotecnico) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SaldoCuentaInversionService saldoService;
    private final ConsultarMovimientoCuentaInversionService movimientoService;
    private final Colaboradores colaboradores;

    public ConsultarCartolaDeMovimientoService(NamedParameterJdbcTemplate jdbc, SaldoCuentaInversionService saldoService,
                                               ConsultarMovimientoCuentaInversionService movimientoService, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.saldoService = saldoService;
        this.movimientoService = movimientoService;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        return gson.toJsonTree(consultar(gson.fromJson(params, Entrada.class))).getAsJsonObject();
    }

    @Transactional
    public Salida consultar(Entrada entrada) throws Exception {
        log.info("INICIO::ConsultarCartolaDeMovimiento");
        Salida salida = new Salida();

        RevisarRequisitosBasicos.Salida requisitos = colaboradores.invocar(RevisarRequisitosBasicos.CLASE,
                RevisarRequisitosBasicos.entrada(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo), RevisarRequisitosBasicos.Salida.class);
        if (requisitos.p_out_Mensajes.stream().anyMatch(m -> MensajeSalida.ERROR.equals(m.tipo))) {
            // LEGACY DEFECT kept (see README CTA-A-189): the error counter stays null when the basic requirements fail
            salida.P_OUT_MENSAJES.addAll(requisitos.p_out_Mensajes);
            return salida;
        }

        MapSqlParameterSource filtros = new MapSqlParameterSource()
                .addValue("p_IdCuentaInversion", entrada.p_IdCuentaInversion)
                .addValue("p_IdCliente", entrada.p_IdCliente)
                .addValue("p_IdPortafolio", entrada.p_IdPortafolio)
                .addValue("p_IdMoneda", entrada.p_IdMoneda)
                .addValue("p_IdProductoAgrupador", entrada.p_IdProductoAgrupador);
        List<SaldoCuentaInversionService.CuentaInversionAConsultar> cuentas = jdbc.query(SQL_PORTAFOLIO_MONEDA, filtros, (rs, i) -> {
            SaldoCuentaInversionService.CuentaInversionAConsultar c = new SaldoCuentaInversionService.CuentaInversionAConsultar();
            c.IdPortafolio = rs.getLong("IdPortafolio");
            c.IdMoneda = rs.getString("IdMoneda");
            return c;
        });
        if (cuentas.isEmpty()) {
            salida.P_OUT_ERRORESCONTADOR = 0L;
            return salida;
        }

        SaldoCuentaInversionService.Entrada entradaSaldo = new SaldoCuentaInversionService.Entrada();
        entradaSaldo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaSaldo.p_FechaDesde = entrada.p_FechaDesde;
        entradaSaldo.p_FechaHasta = entrada.p_FechaHasta;
        entradaSaldo.p_SoloSaldoNegativo = "N";
        entradaSaldo.p_CuentaInversionAConsultar.addAll(cuentas);
        SaldoCuentaInversionService.Salida saldos = saldoService.saldo(entradaSaldo);

        // the opening balance is the largest one among the accounts (an empty list fails with NoSuchElementException as in the legacy)
        BigDecimal saldoInicial = "S".equals(entrada.p_SaldoPorFechaEfectiva)
                ? saldos.P_OUT_SALDOINICIALFINAL.stream().map(s -> s.SaldoEfectivoInicial).max(Comparator.naturalOrder()).orElseThrow()
                : saldos.P_OUT_SALDOINICIALFINAL.stream().map(s -> s.SaldoMovimientoInicial).max(Comparator.naturalOrder()).orElseThrow();

        // LEGACY DEFECT kept (see README CTA-A-191): the movements query uses the request portfolio/currency (null -> no rows)
        ConsultaMovimiento consulta = new ConsultaMovimiento();
        consulta.IdPortafolio = entrada.p_IdPortafolio;
        consulta.IdMoneda = entrada.p_IdMoneda;
        consulta.IdEstado = entrada.p_IdEstado;
        consulta.SaldoInicial = saldoInicial;
        consulta.FechaDesde = entrada.p_FechaDesde;
        consulta.FechaHasta = entrada.p_FechaHasta;
        consulta.IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
        consulta.EsADC = entrada.p_EsADC;
        consulta.SaldoPorFechaEfectiva = entrada.p_SaldoPorFechaEfectiva;
        consulta.IncluyeSaldo = entrada.p_IncluyeSaldo;
        ConsultarMovimientoCuentaInversionService.Entrada entradaMovimientos = new ConsultarMovimientoCuentaInversionService.Entrada();
        entradaMovimientos.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaMovimientos.p_IdUsuario = entrada.p_IdUsuario;
        entradaMovimientos.p_IdModulo = entrada.p_IdModulo;
        entradaMovimientos.p_Consulta = List.of(consulta);
        List<MovimientoCuentaInversion> movimientos = movimientoService.consultar(entradaMovimientos).P_OUT_MOVIMIENTOS;

        boolean incluyeAnulados = "S".equalsIgnoreCase(entrada.p_IncluyeAnulados);
        List<Long> idsComprobante = new ArrayList<>();
        for (MovimientoCuentaInversion m : movimientos) {
            boolean entra = incluyeAnulados
                    ? estadoValido(entrada.p_IdEstado, m.IdEstado) && !"ABNAPV".equalsIgnoreCase(m.IdConcepto)
                    : !"ANU".equals(m.IdEstado);
            if (entra) {
                idsComprobante.add(m.IdComprobante);
            }
        }
        Map<Long, String> nemosPorComprobante = nemosPorComprobante(idsComprobante);

        for (MovimientoCuentaInversion m : movimientos) {
            if (estadoValido(entrada.p_IdEstado, m.IdEstado) && (incluyeAnulados || !m.IdEstado.equals("ANU"))) {
                salida.P_OUT_CARTOLADEMOVIMIENTO.add(fila(m, nemosPorComprobante));
            }
        }
        salida.P_OUT_ERRORESCONTADOR = 0L;
        log.info("FIN::ConsultarCartolaDeMovimiento");
        return salida;
    }

    /** Distinct instrument names of each invoice, joined by ", " in query order. */
    private Map<Long, String> nemosPorComprobante(List<Long> idsComprobante) {
        List<Long> ids = idsComprobante.isEmpty() ? Collections.singletonList(null) : idsComprobante;
        List<Nemo> nemos = jdbc.query(SQL_NEMOS, new MapSqlParameterSource("idsComprobante", ids),
                (rs, i) -> new Nemo(rs.getObject("IdComprobante", Long.class), rs.getString("NemotecnicoInstrumento")));
        Map<Long, String> porComprobante = new HashMap<>();
        for (Long id : idsComprobante) {
            String acumulado = "";
            for (Nemo n : nemos) {
                String nemo = Objects.toString(n.nemotecnico(), "");
                if (id != null && n.idComprobante().compareTo(id) == 0 && !acumulado.contains(nemo)) {
                    acumulado = acumulado.isEmpty() ? nemo : acumulado + ", " + nemo;
                }
            }
            porComprobante.put(id, acumulado);
        }
        return porComprobante;
    }

    private static CartolaDeMovimiento fila(MovimientoCuentaInversion m, Map<Long, String> nemos) throws Exception {
        CartolaDeMovimiento c = Utils.copyPojo(m, new CartolaDeMovimiento());
        // a movement that comes from a bulk load shows the load description as concept name
        c.NombreConcepto = m.IdCarga == null ? m.NombreConcepto : (m.IdCarga > 0 ? m.Descripcion : m.NombreConcepto);
        c.NemotecnicoOriginal = "FAC".equalsIgnoreCase(m.IdModulo)
                ? Objects.toString(nemos.get(m.IdComprobante), "")
                : Objects.toString(m.NemotecnicoOriginal, "");
        return c;
    }

    private static boolean estadoValido(String filtro, String estado) {
        return filtro == null || filtro.isEmpty() || filtro.equals(estado);
    }
}
