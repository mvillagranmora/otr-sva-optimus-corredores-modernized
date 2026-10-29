package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.ConsultaMovimiento;
import cl.bcs.cuentainversion.domain.MovimientoCuentaInversion;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Web service "ConsultarMovimientos": the movements of the investment accounts of a client's portfolios, shaped for the
 * external consumer (README CTA-A-226..240; legacy webservices.ConsultarMovimientos). Finds the accounts, their balances
 * (SaldoCuentaInversionService), their movements (ConsultarMovimientoCuentaInversionService) and maps each movement to the
 * service row, shifting the certification time by the system time zone.
 */
@Service
public class ConsultarMovimientosWSService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientosWSService.class);

    private static final String SQL_CUENTA_INVERSION = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/ConsultarMovimientosWS_SelectCuentaInversion.sql");
    private static final String SIN_FECHA = "0001-01-01";

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdCliente;
        public String p_FechaInicioConsulta;
        public String p_FechaTerminoConsulta;
        public String p_IdMoneda;
        public String p_IdEstado;
        public String p_Origen;
        public String p_Instrumento;
        public String p_TipoOperacion;
        public Long p_IdMatrizAgrupador;
        public String p_EsADC;
        public List<Long> p_IdPortafolios;
        /** ADQ, LIQ or empty. */
        public String p_TipoFechaConsulta;
    }

    public static class Salida {
        public List<Movimiento> P_OUT_RESULTADO = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    /** Row of the service output (legacy field names are the contract). */
    public record Movimiento(String FechaEfectiva, String FechaMovimiento, String Concepto, String EsTransferencia, String Moneda,
                             BigDecimal Cargo, BigDecimal Abono, String TipoComprobante, String SecuenciaComprobante,
                             String FechaComprobante, String TipoProducto, String TipoOperacion, String FechaOperacion,
                             String FechaVencimiento, Long SecuenciaOperacion, BigDecimal MontoOperacion, Long SecuenciaMovimiento,
                             Long SecuenciaMovimientoTransferencia, String NombreClienteRelacionado, String CodigoPortafolioRelacionado,
                             String IngresadoPor, String FechaDeCertificacion, String CertificadoPor, String Descripcion, String Estado,
                             String MotivoAnulacion, String Autorizacion, String EstadoAutorizacion, String FechaLiquidacion,
                             String FolioComprobante, BigDecimal Cantidad, BigDecimal Saldo, Long IdInstrumento, String NombreCliente,
                             Long IdCliente, Long IdPortafolio, String IdConcepto, String IdEstado, String NombreTipoMovimiento,
                             BigDecimal SaldoSimulado, String CodigoPortafolio, Long IdComprobante, Long IdRespuestaDTE,
                             String RespuestaPdfDTE, String DocumentoIdentidad, String Origen, Long IdMatrizAgrupador,
                             String NemotecnicoOriginal, String EsADC) {
    }

    private record CuentaInversion(Long idCuentaInversion, Long idPortafolio, String documentoIdentidad, String codigoPortafolio, String idMoneda) {
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SaldoCuentaInversionService saldoService;
    private final ConsultarMovimientoCuentaInversionService movimientoService;
    private final ConfiguracionSistemaPort configuracion;

    public ConsultarMovimientosWSService(NamedParameterJdbcTemplate jdbc, SaldoCuentaInversionService saldoService,
                                         ConsultarMovimientoCuentaInversionService movimientoService, ConfiguracionSistemaPort configuracion) {
        this.jdbc = jdbc;
        this.saldoService = saldoService;
        this.movimientoService = movimientoService;
        this.configuracion = configuracion;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        return gson.toJsonTree(consultar(gson.fromJson(params, Entrada.class))).getAsJsonObject();
    }

    @Transactional
    public Salida consultar(Entrada entrada) throws Exception {
        log.info("INICIO::ConsultarMovimientosWS");
        List<CuentaInversion> cuentas = cuentasInversion(entrada.p_IdPortafolios, entrada.p_IdMoneda);
        SaldoCuentaInversionService.Salida saldos = saldos(entrada, cuentas);
        List<MovimientoCuentaInversion> movimientos = movimientos(entrada, cuentas, saldos);

        int segundosZonaHoraria = (int) Long.parseLong(
                CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria"));

        Map<Long, CuentaInversion> porId = cuentas.stream().collect(Collectors.toMap(CuentaInversion::idCuentaInversion, Function.identity()));
        Salida salida = new Salida();
        for (MovimientoCuentaInversion m : movimientos) {
            salida.P_OUT_RESULTADO.add(fila(m, porId.get(m.IdCuentaInversion), segundosZonaHoraria));
        }
        log.info("FIN::ConsultarMovimientosWS");
        return salida;
    }

    private List<CuentaInversion> cuentasInversion(List<Long> portafolios, String idMoneda) {
        // an empty IN list is sent as IN (NULL), as the legacy SQL helper did
        List<Long> ids = portafolios == null || portafolios.isEmpty() ? Collections.singletonList(null) : portafolios;
        return jdbc.query(SQL_CUENTA_INVERSION, new MapSqlParameterSource("IdPortafolio", ids).addValue("IdMoneda", idMoneda),
                (rs, i) -> new CuentaInversion(rs.getObject("IdCuentaInversion", Long.class), rs.getObject("IdPortafolio", Long.class),
                        rs.getString("DocumentoIdentidad"), rs.getString("CodigoPortafolio"), rs.getString("IdMoneda")));
    }

    private SaldoCuentaInversionService.Salida saldos(Entrada entrada, List<CuentaInversion> cuentas) {
        SaldoCuentaInversionService.Entrada e = new SaldoCuentaInversionService.Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_FechaDesde = entrada.p_FechaInicioConsulta;
        e.p_FechaHasta = entrada.p_FechaTerminoConsulta;
        e.p_SoloSaldoNegativo = "N";
        e.p_CuentaInversionAConsultar = cuentas.stream().map(c -> {
            SaldoCuentaInversionService.CuentaInversionAConsultar q = new SaldoCuentaInversionService.CuentaInversionAConsultar();
            q.IdPortafolio = c.idPortafolio();
            q.IdMoneda = c.idMoneda();
            return q;
        }).collect(Collectors.toList());
        return saldoService.saldo(e);
    }

    private List<MovimientoCuentaInversion> movimientos(Entrada entrada, List<CuentaInversion> cuentas, SaldoCuentaInversionService.Salida saldos) {
        Map<Long, SaldoCuentaInversionService.SaldoInicialFinal> saldoPorCuenta = saldos.P_OUT_SALDOINICIALFINAL.stream()
                .collect(Collectors.toMap(s -> s.IdCuentaInversion, Function.identity()));
        ConsultarMovimientoCuentaInversionService.Entrada e = new ConsultarMovimientoCuentaInversionService.Entrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_Consulta = cuentas.stream().map(c -> {
            SaldoCuentaInversionService.SaldoInicialFinal saldo = saldoPorCuenta.get(c.idCuentaInversion());
            ConsultaMovimiento q = new ConsultaMovimiento();
            q.IdPortafolio = c.idPortafolio();
            q.IdMoneda = c.idMoneda();
            q.IdEstado = entrada.p_IdEstado;
            // LEGACY DEFECT kept (see README CTA-A-236): the initial balance of every consultation is the FINAL effective balance of the account
            q.SaldoInicial = saldo != null ? saldo.SaldoEfectivoFinal : BigDecimal.ZERO;
            q.FechaDesde = entrada.p_FechaInicioConsulta;
            q.FechaHasta = entrada.p_FechaTerminoConsulta;
            q.IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
            q.EsADC = entrada.p_EsADC;
            // LEGACY DEFECT kept (see README CTA-A-231): the condition for "LIQ" can never be true (empty AND "LIQ"), so it always queries by movement date
            q.SaldoPorFechaEfectiva = entrada.p_TipoFechaConsulta != null && entrada.p_TipoFechaConsulta.trim().isEmpty()
                    && entrada.p_TipoFechaConsulta.equalsIgnoreCase("LIQ") ? "S" : "N";
            q.IncluyeSaldo = "N";
            return q;
        }).collect(Collectors.toList());
        return movimientoService.consultar(e).P_OUT_MOVIMIENTOS;
    }

    private Movimiento fila(MovimientoCuentaInversion m, CuentaInversion cuenta, int segundosZonaHoraria) {
        // LEGACY DEFECT kept (see README CTA-A-239): a null certification date throws NullPointerException here
        m.FechaCertificacion = Fecha.addSeconds(m.FechaCertificacion, segundosZonaHoraria);
        String hora;
        try {
            hora = Fecha.toStringTime(Fecha.toTime(Fecha.toFormat(m.FechaCertificacion, "HH:mm:ss")));
        } catch (RuntimeException e) {
            hora = "00:00:00";
        }
        String codigoPortafolio = cuenta != null ? cuenta.codigoPortafolio() : "";
        return new Movimiento(
                m.FechaEfectivaMovimiento != null ? Fecha.stringToSqlDate(m.FechaEfectivaMovimiento) : SIN_FECHA,
                m.FechaSistema != null ? Fecha.toStringDatetime(Fecha.toTimeStamp(m.FechaSistema + " " + hora)) : SIN_FECHA,
                m.NombreConcepto, m.EsCargoAbonoPorTraspaso, m.IdMoneda, m.Cargo, m.Abono, m.NombreTipoComprobante, m.FolioComprobante,
                fechaHora(m.FechaComprobante), m.NombreTipoProducto, m.NombreTipoOperacion, fechaHora(m.FechaOperacion),
                m.FechaVencimientoOperacion != null ? Fecha.stringToSqlDate(m.FechaVencimientoOperacion) : SIN_FECHA,
                m.IdOperacion, m.MontoOperacion, m.IdCargoAbonoMovimiento, m.IdCargoAbonoRelacionadoTraspaso,
                m.NombreClienteRelacionado, m.CodigoPortafolioRelacionado, m.NombreUsuario, fechaHora(m.FechaCertificacion),
                m.NombreUsuarioCert, m.Descripcion, m.NombreEstado, "", "", "",
                m.FechaLiquidacionOperacion != null ? Fecha.stringToSqlDate(m.FechaLiquidacionOperacion) : SIN_FECHA,
                m.FolioComprobante, m.Cantidad, m.SaldoPorFechaEfectiva, m.IdInstrumento, m.NombreCliente, m.IdCliente, m.IdPortafolio,
                m.IdConcepto, m.IdEstado, m.NombreTipoMovimiento, m.SaldoSimulado,
                codigoPortafolio, m.IdComprobante, 0L, "",
                cuenta != null ? cuenta.documentoIdentidad() : "", cuenta != null ? origen(codigoPortafolio) : "",
                m.IdMatrizAgrupador, m.NemotecnicoOriginal, m.EsADC);
    }

    private static String fechaHora(Timestamp t) {
        return t != null ? Fecha.toFormat(t, "yyyy-MM-dd HH:mm:ss") : SIN_FECHA;
    }

    /** APV portfolios have codes APV-* or DC-*. */
    private static String origen(String codigoPortafolio) {
        String prefijo = codigoPortafolio.split("-")[0];
        return prefijo.equals("APV") || prefijo.equals("DC") ? "APV" : "NOAPV";
    }
}
