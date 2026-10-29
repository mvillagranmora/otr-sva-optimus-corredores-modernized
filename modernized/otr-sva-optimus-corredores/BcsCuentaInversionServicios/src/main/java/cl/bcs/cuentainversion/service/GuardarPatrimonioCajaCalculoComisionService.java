package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.CalendarioReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.FormatoMoneda;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.PatrimonioCaja;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custody-fee base and fee of the cash patrimony (legacy {@code GuardarPatrimonioCajaCalculoComision}; CTA-B-117..123). Called by
 * {@link GuardarPatrimonioCajaService} and also exposed as a service.
 *
 * <p>The affected patrimony of a portfolio is the ADQ patrimony of the previous BUSINESS day (CTA-B-117, 122) plus the net certified
 * movement of the requested account of the day (cargo subtracts, excluded concepts ignored: CTA-B-119, 120); the fee is the annual rate
 * / 365 of that amount once the option is in force (CTA-B-121), truncated to the system currency. The staging table lives in the
 * session, so the method runs inside the caller's transaction.
 */
@Service
public class GuardarPatrimonioCajaCalculoComisionService {

    private static final Logger log = LoggerFactory.getLogger(GuardarPatrimonioCajaCalculoComisionService.class);

    private static final String SQL_CARGO_ABONO = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_SelectCargoAbonoCuentaInversion");
    private static final String SQL_PATRIMONIO_ANTERIOR = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_SelectPatrimonioAfectoDiaAnterior");
    private static final String SQL_CREAR_PASO = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_CrearTablaPaso");
    private static final String SQL_DROP_PASO = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_DropTablaPaso");
    private static final String SQL_DELETE_PASO = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_DeleteTablaPaso");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_InsertTablaPaso");
    private static final String SQL_MONTO_COMISION = SqlReportes.recurso("GuardarPatrimonioCajaCalculoComision_SelectMontoComision");

    private final SqlReportes sql;

    public GuardarPatrimonioCajaCalculoComisionService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(calcular(JsonReportes.leer(params, Entrada.class)));
    }

    public Salida calcular(Entrada entrada) throws Exception {
        log.info("INICIO::GuardarPatrimonioCajaCalculoComision");
        Salida salida = new Salida();
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        String monedaSistema = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
        if (entrada.p_CuentaInversionAConsultar.isEmpty()) {
            return salida;
        }

        try {
            sql.ejecutar(SQL_CREAR_PASO);
            sql.lote(SQL_INSERT_PASO, entrada.p_CuentaInversionAConsultar);

            Map<Long, List<MovimientoCargoAbono>> movimientos = sql
                    .consultar(SQL_CARGO_ABONO, Map.of("p_FechaConsulta", entrada.p_FechaConsulta), MovimientoCargoAbono.class).stream()
                    .collect(Collectors.groupingBy(m -> m.IdCuentaInversion));

            String diaHabilAnterior = diaHabilAnterior(config, entrada);
            Map<Long, PatrimonioCaja> anteriores = sql
                    .consultar(SQL_PATRIMONIO_ANTERIOR, Map.of("p_FechaConsulta", diaHabilAnterior), PatrimonioCaja.class).stream()
                    .collect(Collectors.toMap(p -> p.IdPortafolio, p -> p));

            for (CuentaInversionConsultar cuenta : entrada.p_CuentaInversionAConsultar) {
                // only ONE movement row per account is used: the first of its group (CTA-B-120)
                List<MovimientoCargoAbono> delaCuenta = movimientos.get(cuenta.IdCuentaInversion);
                BigDecimal movimientoDelDia = delaCuenta != null ? delaCuenta.get(0).Monto : BigDecimal.ZERO;
                PatrimonioCaja base = anteriores.getOrDefault(cuenta.IdPortafolio, new PatrimonioCaja());
                base.MontoPatrimonioAfectoComision = base.MontoPatrimonioAfectoComision.add(movimientoDelDia, MathContext.DECIMAL128);
            }

            // LEGACY DEFECT kept (see README CTA-B-123): the result is built only from the previous-day rows of the requested
            // portfolios; with no previous patrimony nothing is reported even if the account moved.
            List<PatrimonioCaja> afectos = new ArrayList<>();
            anteriores.forEach((portafolio, patrimonio) -> {
                patrimonio.FechaPatrimonio = entrada.p_FechaConsulta;
                afectos.add(patrimonio);
            });

            if (!afectos.isEmpty()) {
                sql.ejecutar(SQL_DELETE_PASO);
                sql.lote(SQL_INSERT_PASO, afectos);
                Map<String, MontoComision> comisiones = sql.consultar(SQL_MONTO_COMISION, Map.of(), MontoComision.class).stream()
                        .collect(Collectors.toMap(m -> m.IdPortafolio + "_" + m.FechaPatrimonio, m -> m));
                FormatoMoneda formato = new FormatoMoneda(sql);
                for (PatrimonioCaja patrimonio : afectos) {
                    MontoComision c = comisiones.get(patrimonio.IdPortafolio + "_" + patrimonio.FechaPatrimonio);
                    BigDecimal montoAfecto = c != null ? c.MontoPatrimonioAfectoComision : BigDecimal.ZERO;
                    BigDecimal porcentaje = c != null ? c.PorcentajeCobro : BigDecimal.ZERO;
                    BigDecimal comision = c != null ? c.MontoComision : BigDecimal.ZERO;
                    patrimonio.MontoPatrimonioAfectoComision = formato.formatear(montoAfecto, FormatoMoneda.MONTO, monedaSistema).valor();
                    patrimonio.PorcentajeCobroComision = porcentaje;
                    patrimonio.MontoComision = formato
                            .formatear(comision.compareTo(BigDecimal.ZERO) > 0 ? comision : BigDecimal.ZERO, FormatoMoneda.MONTO, monedaSistema)
                            .valor();
                    patrimonio.IdEstadoComision = patrimonio.MontoComision.compareTo(BigDecimal.ZERO) == 0 ? "INA" : "VIG";
                }
            }
            salida.P_TBL_PATRIMONIO.addAll(afectos);
        } finally {
            sql.ejecutar(SQL_DROP_PASO);
        }
        log.info("FIN::GuardarPatrimonioCajaCalculoComision");
        return salida;
    }

    /** The business day before the query date (the query date itself when the calendar gives none). */
    private String diaHabilAnterior(ConfiguracionSistema config, Entrada entrada) throws Exception {
        Long idPais = config.entero(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.PaisSistema");
        LocalDate consulta = LocalDate.parse(entrada.p_FechaConsulta);
        return new CalendarioReportes(sql).anterior(consulta, 1L, idPais, true, true).fecha().toString();
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 1L;
        public String p_IdModulo = "CTA";
        public Long p_IdUsuario = 0L;
        public String p_UsaReglasNegocio = "S";
        public List<CuentaInversionConsultar> p_CuentaInversionAConsultar = new ArrayList<>();
        public List<PatrimonioCaja> p_PatrimoniosCaja = new ArrayList<>();
        public String p_MonedaConsulta = "";
        public String p_FechaConsulta = "0001-01-01";
    }

    public static class Salida {
        public List<PatrimonioCaja> P_TBL_PATRIMONIO = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    /** Account to consult; also the staging-table row (public fields). */
    public static class CuentaInversionConsultar {
        public Long IdCuentaInversion = 0L;
        public Long IdPortafolio = 0L;
        public Long IdCliente = 0L;
        public String FechaPatrimonio = "0001-01-01";
        public String IdMoneda = "CLP";
        public BigDecimal MontoPatrimonioAfectoComision = BigDecimal.ZERO;

        public CuentaInversionConsultar() {
        }

        public CuentaInversionConsultar(Long idCuentaInversion, Long idPortafolio, Long idCliente, String idMoneda, String fechaConsulta) {
            this.IdCuentaInversion = idCuentaInversion;
            this.IdPortafolio = idPortafolio;
            this.IdCliente = idCliente;
            this.IdMoneda = idMoneda;
            this.FechaPatrimonio = fechaConsulta;
        }
    }

    public static class MovimientoCargoAbono {
        public Long IdCuentaInversion = 0L;
        public java.sql.Date FechaSistema;
        public java.sql.Date FechaEfectivaMovimiento;
        public Long IdCargoAbonoMovimiento = 0L;
        public String IdTipoMovimiento = "";
        public BigDecimal Monto = BigDecimal.ZERO;
        public Long IdPortafolio = 0L;
    }

    public static class MontoComision {
        public String PortafolioAPV = "N";
        public String IdClasificacionTipoInstrumento = "";
        public Long OpcionCobro = 0L;
        public BigDecimal PorcentajeCobro = BigDecimal.ZERO;
        public Long IdPortafolio;
        public BigDecimal MontoPatrimonioAfectoComision = BigDecimal.ZERO;
        public BigDecimal MontoComision = BigDecimal.ZERO;
        public String FechaPatrimonio = "0001-01-01";
    }
}
