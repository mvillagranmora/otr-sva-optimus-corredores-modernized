package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cash flow balance service. Runs 20 SQL queries against a HANA persistent
 * process table (PLA.SaldoFlujoCaja_PortafolioProceso_TablaPaso) plus date-range
 * aggregation logic.
 *
 * Cross-service dependencies (SaldoCuentaInversion, SaldoPendienteDeLiquidar)
 * return empty results until BcsCuentaInversionServicios and BcsTesoreriaServicios
 * are fully migrated. Those items (SALDO_CUENTA_INVERSION, CARGOS_CUENTA_INVERSION,
 * INGRESOS_TESORERIA, EGRESOS_TESORERIA) will contribute 0 to the cash flow sum
 * during the canary period.
 */
@Service
public class SaldoFlujoCajaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(SaldoFlujoCajaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String SQL_CONFIG_TIPO_CALCULO = "SELECT \"Valor\" FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoParametro\" IN ('PLATAFORMA.PLA.TipoCalculoFlujoCaja','PLA.TipoCalculoFlujoCaja') ORDER BY \"CodigoParametro\" DESC";
    private static final String SQL_CONFIG_TIPO_CALCULO_SPOT = "SELECT \"Valor\" FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoParametro\" IN ('PLATAFORMA.PLA.TipoCalculoFlujoCajaSpot','PLA.TipoCalculoFlujoCajaSpot') ORDER BY \"CodigoParametro\" DESC";

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_CONFIGURACIONES;
    private final String SQL_SELECT_CLIENTE_PORTAFOLIO;
    private final String SQL_SELECT_CUENTA_INVERSION;
    private final String SQL_INSERT_PORTAFOLIOS_PROCESO;
    private final String SQL_DELETE_PORTAFOLIOS_PROCESO;
    private final String SQL_ORDENES;
    private final String SQL_ASIGNACIONES;
    private final String SQL_FACTURAS;
    private final String SQL_APORTES_EN_TRANSITO;
    private final String SQL_APORTES_EN_TRANSITO_PAC;
    private final String SQL_APORTES_EN_TRANSITO_REINVERSION;
    private final String SQL_APORTES_EN_TRANSITO_INSTRUCCION;
    private final String SQL_RESCATES_EN_TRANSITO;
    private final String SQL_INVERSION_EN_LIQUIDEZ;
    private final String SQL_EVENTO_CAPITAL;
    private final String SQL_SIMULTANEAS_EMP_PRES;
    private final String SQL_MESAS;
    private final String SQL_SOBREGIRO;
    private final String SQL_SOBREGIRO_TESORERIA;
    private final String SQL_ENTREGAMOS_SPOT;
    private final String SQL_RECIBIMOS_SPOT;
    private final String SQL_RECIBIMOS_SPOT_PAC;
    private final String SQL_SALDO_RETENIDO;
    private final String SQL_ABONOS_RETENIDOS;
    private final String SQL_CARGOS_RETENIDOS;
    private final String SQL_CUSTODIA_MONEY_MARKET;

    public SaldoFlujoCajaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        SQL_SELECT_CONFIGURACIONES     = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectConfiguraciones.sql");
        SQL_SELECT_CLIENTE_PORTAFOLIO  = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectClientePortafolio.sql");
        SQL_SELECT_CUENTA_INVERSION    = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectCuentaInversion.sql");
        SQL_INSERT_PORTAFOLIOS_PROCESO = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_InsertPortafoliosProcesoTablaPaso.sql");
        SQL_DELETE_PORTAFOLIOS_PROCESO = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_DeletePortafoliosProcesoTablaPaso.sql");
        SQL_ORDENES                    = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectOrdenesEnTransito.sql");
        SQL_ASIGNACIONES               = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAsignacionesEnTransito.sql");
        SQL_FACTURAS                   = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectMovimientoFacturacion.sql");
        SQL_APORTES_EN_TRANSITO        = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransito.sql");
        SQL_APORTES_EN_TRANSITO_PAC    = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoPAC.sql");
        SQL_APORTES_EN_TRANSITO_REINVERSION = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoPorReinversion.sql");
        SQL_APORTES_EN_TRANSITO_INSTRUCCION = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoInstruccionLiquidada.sql");
        SQL_RESCATES_EN_TRANSITO       = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRescateEnTransito.sql");
        SQL_INVERSION_EN_LIQUIDEZ      = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectInversionEnLiquidez.sql");
        SQL_EVENTO_CAPITAL             = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectEventoCapital.sql");
        SQL_SIMULTANEAS_EMP_PRES       = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSimultaneasEmpPre.sql");
        SQL_MESAS                      = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectMesas.sql");
        SQL_SOBREGIRO                  = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectLineaSobregiroPortafolio.sql");
        SQL_SOBREGIRO_TESORERIA        = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectLineaSobregiroTesoreriaPortafolio.sql");
        SQL_ENTREGAMOS_SPOT            = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectEntregamosSpot.sql");
        SQL_RECIBIMOS_SPOT             = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRecibimosSpot.sql");
        SQL_RECIBIMOS_SPOT_PAC         = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRecibimosSpotPAC.sql");
        SQL_SALDO_RETENIDO             = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenido.sql");
        SQL_ABONOS_RETENIDOS           = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenidoAbonosPorLiquidar.sql");
        SQL_CARGOS_RETENIDOS           = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenidoCargosPorLiquidar.sql");
        SQL_CUSTODIA_MONEY_MARKET      = loadSql("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectCustodiaMoneyMarket.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.SaldoFlujoCaja"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::SaldoFlujoCaja");
        Salida out = new Salida();
        try {
            Long idEmpresa      = params.has("P_IDEMPRESA")      ? params.get("P_IDEMPRESA").getAsLong()      : 1L;
            String fechaDesde   = params.has("P_FECHADESDE")     ? params.get("P_FECHADESDE").getAsString()   : "";
            String fechaHasta   = params.has("P_FECHAHASTA")     ? params.get("P_FECHAHASTA").getAsString()   : "";
            String uso          = params.has("P_USO")            ? (params.get("P_USO").isJsonNull() ? null : params.get("P_USO").getAsString()) : null;
            String desplegarUI  = params.has("P_DESPLEGARENUI")  ? params.get("P_DESPLEGARENUI").getAsString() : "N";
            String agruparOrigen= params.has("P_AGRUPARPORORIGEN")? params.get("P_AGRUPARPORORIGEN").getAsString() : "N";

            JsonArray portafoliosJson = params.has("P_PORTAFOLIOACONSULTAR") ? params.getAsJsonArray("P_PORTAFOLIOACONSULTAR") : new JsonArray();
            List<ObjPortafolio> portafoliosIn = new ArrayList<>();
            for (JsonElement el : portafoliosJson) {
                JsonObject po = el.getAsJsonObject();
                ObjPortafolio p = new ObjPortafolio();
                p.IdPortafolio = po.has("IdPortafolio") ? po.get("IdPortafolio").getAsLong() : 0L;
                p.IdMoneda     = po.has("IdMoneda") && !po.get("IdMoneda").isJsonNull() ? po.get("IdMoneda").getAsString() : null;
                portafoliosIn.add(p);
            }

            if (portafoliosIn.isEmpty()) {
                return toJson(out);
            }

            Date fDesde = Date.valueOf(fechaDesde);
            Date fHasta = Date.valueOf(fechaHasta);

            // Read TipoCalculoFlujoCaja config
            String tipoCalculo = "LIQUIDACION";
            try {
                List<String> rows = jdbc.queryForList(SQL_CONFIG_TIPO_CALCULO, new MapSqlParameterSource("IdEmpresa", idEmpresa), String.class);
                if (!rows.isEmpty()) tipoCalculo = rows.get(0);
            } catch (Exception ignored) {}

            String tipoCalculoSpot = "LIQUIDACION";
            try {
                List<String> rows = jdbc.queryForList(SQL_CONFIG_TIPO_CALCULO_SPOT, new MapSqlParameterSource("IdEmpresa", idEmpresa), String.class);
                if (!rows.isEmpty()) tipoCalculoSpot = rows.get(0).toUpperCase();
            } catch (Exception ignored) {}

            // Collect unique monedas
            List<String> idMonedas = portafoliosIn.stream()
                .map(p -> p.IdMoneda).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (idMonedas.isEmpty()) idMonedas.add("");

            // DELETE + INSERT process table
            jdbc.update(SQL_DELETE_PORTAFOLIOS_PROCESO, new MapSqlParameterSource());
            MapSqlParameterSource[] batchParams = portafoliosIn.stream()
                .map(p -> new MapSqlParameterSource()
                    .addValue("IdPortafolio", p.IdPortafolio)
                    .addValue("IdMoneda", p.IdMoneda))
                .toArray(MapSqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_INSERT_PORTAFOLIOS_PROCESO, batchParams);

            // Resolve portafoliosProceso
            List<PortafolioProceso> portafoliosProceso;
            if ("CUENTA_INVERSION".equals(uso)) {
                portafoliosProceso = jdbc.query(SQL_SELECT_CUENTA_INVERSION,
                    new MapSqlParameterSource("IdMoneda", idMonedas),
                    BeanPropertyRowMapper.newInstance(PortafolioProceso.class));
            } else {
                portafoliosProceso = jdbc.query(SQL_SELECT_CLIENTE_PORTAFOLIO,
                    new MapSqlParameterSource(),
                    BeanPropertyRowMapper.newInstance(PortafolioProceso.class));
            }
            portafoliosProceso.forEach(pp -> pp.Id = pp.IdPortafolio + "_" + pp.IdMoneda);

            // Build portafoliosProceso de-duplicated against the requested portafolios
            Map<String, PortafolioProceso> ppHash = new LinkedHashMap<>();
            for (PortafolioProceso pp : portafoliosProceso) {
                if (!ppHash.containsKey(pp.Id)) ppHash.put(pp.Id, pp);
            }
            List<PortafolioProceso> ppList = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (ObjPortafolio req : portafoliosIn) {
                String key = req.IdPortafolio + "_" + req.IdMoneda;
                PortafolioProceso pp = ppHash.get(key);
                if (pp != null && seen.add(pp.Id)) ppList.add(pp);
            }
            if (ppList.isEmpty()) ppList.addAll(ppHash.values());

            // Read configuraciones
            MapSqlParameterSource confParams = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("Uso", uso);
            List<ConfiguracionFlujoCaja> configuraciones = jdbc.query(SQL_SELECT_CONFIGURACIONES,
                confParams, BeanPropertyRowMapper.newInstance(ConfiguracionFlujoCaja.class));

            List<ConfiguracionFlujoCaja> confFilter = configuraciones.stream()
                .filter(c -> (uso == null || uso.equals(c.NombreUso)) &&
                             (!"S".equals(desplegarUI) || desplegarUI.equals(c.DesplegarEnUI)))
                .collect(Collectors.toList());

            // Build date-range grid: portafolio x config x date
            List<RangoFecha> saldosFecha = new ArrayList<>();
            for (PortafolioProceso pp : ppList) {
                for (ConfiguracionFlujoCaja conf : confFilter) {
                    LocalDate iter = LocalDate.parse(fechaDesde, DATE_FMT);
                    LocalDate hasta = LocalDate.parse(fechaHasta, DATE_FMT);
                    while (!iter.isAfter(hasta)) {
                        RangoFecha rf = new RangoFecha();
                        rf.Id = pp.Id;
                        rf.IdPersona = pp.IdPersona;
                        rf.IdPortafolio = pp.IdPortafolio;
                        rf.IdMoneda = pp.IdMoneda;
                        rf.IdUsoFlujoCaja = conf.IdUsoFlujoCaja;
                        rf.NombreUso = conf.NombreUso;
                        rf.ItemCalculo = conf.ItemCalculo;
                        rf.ItemOrigen = conf.ItemOrigen;
                        rf.Item = "N".equals(agruparOrigen) ? conf.ItemCalculo : conf.ItemOrigen;
                        rf.FuncionOperador = "-".equals(conf.FuncionOperador) ? -1L : 1L;
                        rf.FuncionOperadorString = conf.FuncionOperador;
                        rf.DesplegarEnUI = conf.DesplegarEnUI;
                        rf.Fecha = Date.valueOf(iter);
                        saldosFecha.add(rf);
                        iter = iter.plusDays(1);
                    }
                }
            }

            // Re-insert portafoliosProceso into process table (the SELECT queries JOIN to it)
            jdbc.update(SQL_DELETE_PORTAFOLIOS_PROCESO, new MapSqlParameterSource());
            MapSqlParameterSource[] batchPP = ppList.stream()
                .map(pp -> new MapSqlParameterSource()
                    .addValue("IdPersona", pp.IdPersona)
                    .addValue("IdPortafolio", pp.IdPortafolio)
                    .addValue("IdMoneda", pp.IdMoneda)
                    .addValue("Id", pp.Id))
                .toArray(MapSqlParameterSource[]::new);
            if (batchPP.length > 0) jdbc.batchUpdate(SQL_INSERT_PORTAFOLIOS_PROCESO, batchPP);

            // Run data queries for each unique item type
            MapSqlParameterSource dataParams = new MapSqlParameterSource()
                .addValue("FechaDesde", fechaDesde)
                .addValue("FechaHasta", fechaHasta)
                .addValue("IdEmpresa", idEmpresa)
                .addValue("TipoCalculoFlujoCaja", tipoCalculo);

            Map<String, Boolean> queryFiltro = new HashMap<>();
            List<DatosFlujoCaja> datosFlujoCaja = new ArrayList<>();

            for (ConfiguracionFlujoCaja conf : configuraciones) {
                String tipoConsulta = tipoQuery(conf.ItemCalculo);
                String sqlQuery = tipoConsulta != null ? obtenerConsulta(tipoConsulta) : null;
                if (sqlQuery != null && !queryFiltro.containsKey(tipoConsulta)) {
                    queryFiltro.put(tipoConsulta, true);
                    boolean isSpot = "ENTREGAMOS_OPERACION_SPOT".equals(tipoConsulta)
                        || "RECIBIMOS_OPERACION_SPOT".equals(tipoConsulta)
                        || "FACTURAS".equals(tipoConsulta);
                    MapSqlParameterSource qp = new MapSqlParameterSource(dataParams.getValues());
                    if (isSpot) qp.addValue("TipoCalculoFlujoCajaSpot", tipoCalculoSpot);
                    try {
                        datosFlujoCaja.addAll(jdbc.query(sqlQuery, qp,
                            BeanPropertyRowMapper.newInstance(DatosFlujoCaja.class)));
                    } catch (Exception e) {
                        log.warn("Error al consultar {}: {}", tipoConsulta, e.getMessage());
                    }
                }
            }

            jdbc.update(SQL_DELETE_PORTAFOLIOS_PROCESO, new MapSqlParameterSource());

            // Aggregate: saldosFecha x datosFlujoCaja
            Map<String, ObjFLUJOCAJADETALLADO> flujosDetallado = new LinkedHashMap<>();
            Map<String, ObjFLUJOCAJA> flujosTotalizado = new LinkedHashMap<>();

            for (RangoFecha rf : saldosFecha) {
                String fechaSaldo = rf.Fecha.toString();
                String keyDet = rf.IdPortafolio + "_" + rf.IdMoneda + "_" + fechaSaldo + "_" + rf.Item + "_" + rf.ItemCalculo + "_" + rf.IdUsoFlujoCaja;
                String keyCaja = rf.IdPortafolio + "_" + rf.IdMoneda + "_" + fechaSaldo + "_" + rf.NombreUso;

                ObjFLUJOCAJADETALLADO fd = flujosDetallado.computeIfAbsent(keyDet, k -> {
                    ObjFLUJOCAJADETALLADO d = new ObjFLUJOCAJADETALLADO();
                    d.FechaSaldo = fechaSaldo; d.IdPortafolio = rf.IdPortafolio;
                    d.IdMoneda = rf.IdMoneda; d.IdUsoFlujoCaja = rf.IdUsoFlujoCaja;
                    d.NombreUso = rf.NombreUso; d.ItemCalculo = rf.ItemCalculo;
                    d.FuncionOperador = rf.FuncionOperadorString;
                    return d;
                });
                ObjFLUJOCAJA fc = flujosTotalizado.computeIfAbsent(keyCaja, k -> {
                    ObjFLUJOCAJA f = new ObjFLUJOCAJA();
                    f.FechaSaldo = fechaSaldo; f.NombreUso = rf.NombreUso;
                    f.IdPortafolio = rf.IdPortafolio; f.IdMoneda = rf.IdMoneda;
                    return f;
                });

                BigDecimal operador = BigDecimal.valueOf(rf.FuncionOperador);
                String itemCalculo = rf.ItemCalculo;

                // Cross-service items: SaldoCuentaInversion and SaldoPendienteDeLiquidar
                // Return 0 until those services are migrated (safe canary-period behavior)
                if ("SALDO_CUENTA_INVERSION".equals(itemCalculo) || "CARGOS_CUENTA_INVERSION".equals(itemCalculo)
                    || "INGRESOS_TESORERIA".equals(itemCalculo)  || "EGRESOS_TESORERIA".equals(itemCalculo)) {
                    // Cross-service not yet wired: contribute 0 to cash flow
                    continue;
                }

                String tipoMonto = obtenerTipoMonto(itemCalculo);
                if (tipoMonto != null) {
                    for (DatosFlujoCaja dato : datosFlujoCaja) {
                        if (dato.ItemCalculo == null || !dato.ItemCalculo.equals(itemCalculo)) continue;
                        if (dato.Id == null || !dato.Id.equals(rf.Id)) continue;
                        if (dato.FechaLiquidacion == null || dato.FechaLiquidacion.compareTo(fDesde) < 0) continue;
                        boolean cargosRetenidos = "CARGOS_RETENIDOS_POR_LIQUIDAR".equalsIgnoreCase(itemCalculo);
                        if (!cargosRetenidos && dato.FechaLiquidacion.compareTo(rf.Fecha) > 0) continue;

                        BigDecimal monto = BigDecimal.ZERO;
                        if ("MontoCompra".equals(tipoMonto)) monto = nvd(dato.MontoCompra);
                        else if ("MontoVenta".equals(tipoMonto)) monto = nvd(dato.MontoVenta);
                        else monto = nvd(dato.Monto);

                        if (cargosRetenidos && dato.FechaLiquidacion.compareTo(rf.Fecha) <= 0) monto = BigDecimal.ZERO;

                        monto = operador.multiply(monto, MathContext.DECIMAL128);
                        fd.Monto = fd.Monto.add(monto, MathContext.DECIMAL128);
                        fc.Saldo = fc.Saldo.add(monto, MathContext.DECIMAL128);
                    }
                }
            }

            out.P_OUT_FLUJOCAJADETALLADO.addAll(flujosDetallado.values());
            out.P_OUT_FLUJOCAJA.addAll(flujosTotalizado.values());
            out.P_OUT_FLUJOCAJADETALLADO.sort(Comparator.comparing(d -> d.IdUsoFlujoCaja));

        } catch (Exception e) {
            log.error("Error en SaldoFlujoCaja: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            ObjERRORESDETALLE err = new ObjERRORESDETALLE();
            err.Codigo = "ERROR_INTERNO"; err.Descripcion = e.getMessage();
            out.P_OUT_ERRORESDETALLE.add(err);
            try { jdbc.update(SQL_DELETE_PORTAFOLIOS_PROCESO, new MapSqlParameterSource()); } catch (Exception ignored) {}
        }
        log.info("FIN::SaldoFlujoCaja");
        return toJson(out);
    }

    private String tipoQuery(String item) {
        if (item == null) return null;
        return switch (item) {
            case "ORDENES_EN_TRANSITO_COMPRAS", "ORDENES_EN_TRANSITO_VENTAS" -> "ORDENES";
            case "ASIGNACIONES_COMPRAS", "ASIGNACIONES_VENTAS" -> "ASIGNACIONES";
            case "MOVIMIENTOS_FACTURAR_COMPRAS", "MOVIMIENTOS_FACTURAR_VENTAS", "MOVIMIENTOS_FACTURAR_COMISION_SALIDA_FFM" -> "FACTURAS";
            case "APORTES_EN_TRANSITO" -> "APORTES_EN_TRANSITO";
            case "APORTES_EN_TRANSITO_PAC" -> "APORTES_EN_TRANSITO_PAC";
            case "APORTES_EN_TRANSITO_POR_REINVERSION" -> "APORTES_EN_TRANSITO_POR_REINVERSION";
            case "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA" -> "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA";
            case "RESCATES_EN_TRANSITO" -> "SELECT_RESCATES_EN_TRANSITO";
            case "INVERSION_EN_LIQUIDEZ" -> "INVERSION_EN_LIQUIDEZ";
            case "DIVIDENDOS_EN_DINERO", "CORTE_CUPON", "SORTEO_DE_LETRAS" -> "EVENTO_CAPITAL";
            case "SIMULTANEA_COMPRAS", "EMPRESTITOS", "PRESTAMOS" -> "SIMULTANEAS_EMP_PRES";
            case "COMPRA_PACTO", "COMPRA_TERMINO", "COMPRA_FORWARD", "VENTA_PACTO", "VENTA_TERMINO", "VENTA_FORWARD" -> "MESAS";
            case "LINEA_SOBREGIRO" -> "SOBREGIRO";
            case "LINEA_SOBREGIRO_TESORERIA" -> "SOBREGIRO_TESORERIA";
            case "ENTREGAMOS_OPERACION_SPOT" -> "ENTREGAMOS_OPERACION_SPOT";
            case "RECIBIMOS_OPERACION_SPOT_PAC" -> "RECIBIMOS_OPERACION_SPOT_PAC";
            case "RECIBIMOS_OPERACION_SPOT" -> "RECIBIMOS_OPERACION_SPOT";
            case "SALDO_RETENIDO" -> "SALDO_RETENIDO";
            case "ABONOS_RETENIDOS_POR_LIQUIDAR" -> "ABONOS_RETENIDOS_POR_LIQUIDAR";
            case "CARGOS_RETENIDOS_POR_LIQUIDAR" -> "CARGOS_RETENIDOS_POR_LIQUIDAR";
            case "CUSTODIA_FFMM_MONEY_MARKET" -> "CUSTODIA_FFMM_MONEY_MARKET";
            default -> null;
        };
    }

    private String obtenerConsulta(String tipo) {
        return switch (tipo) {
            case "ORDENES"                             -> SQL_ORDENES;
            case "ASIGNACIONES"                        -> SQL_ASIGNACIONES;
            case "FACTURAS"                            -> SQL_FACTURAS;
            case "APORTES_EN_TRANSITO"                 -> SQL_APORTES_EN_TRANSITO;
            case "APORTES_EN_TRANSITO_PAC"             -> SQL_APORTES_EN_TRANSITO_PAC;
            case "APORTES_EN_TRANSITO_POR_REINVERSION" -> SQL_APORTES_EN_TRANSITO_REINVERSION;
            case "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA" -> SQL_APORTES_EN_TRANSITO_INSTRUCCION;
            case "SELECT_RESCATES_EN_TRANSITO"         -> SQL_RESCATES_EN_TRANSITO;
            case "INVERSION_EN_LIQUIDEZ"               -> SQL_INVERSION_EN_LIQUIDEZ;
            case "EVENTO_CAPITAL"                      -> SQL_EVENTO_CAPITAL;
            case "SIMULTANEAS_EMP_PRES"                -> SQL_SIMULTANEAS_EMP_PRES;
            case "MESAS"                               -> SQL_MESAS;
            case "SOBREGIRO"                           -> SQL_SOBREGIRO;
            case "SOBREGIRO_TESORERIA"                 -> SQL_SOBREGIRO_TESORERIA;
            case "ENTREGAMOS_OPERACION_SPOT"           -> SQL_ENTREGAMOS_SPOT;
            case "RECIBIMOS_OPERACION_SPOT_PAC"        -> SQL_RECIBIMOS_SPOT_PAC;
            case "RECIBIMOS_OPERACION_SPOT"            -> SQL_RECIBIMOS_SPOT;
            case "SALDO_RETENIDO"                      -> SQL_SALDO_RETENIDO;
            case "ABONOS_RETENIDOS_POR_LIQUIDAR"       -> SQL_ABONOS_RETENIDOS;
            case "CARGOS_RETENIDOS_POR_LIQUIDAR"       -> SQL_CARGOS_RETENIDOS;
            case "CUSTODIA_FFMM_MONEY_MARKET"          -> SQL_CUSTODIA_MONEY_MARKET;
            default -> null;
        };
    }

    private String obtenerTipoMonto(String item) {
        if (item == null) return null;
        return switch (item) {
            case "ORDENES_EN_TRANSITO_COMPRAS", "ASIGNACIONES_COMPRAS", "MOVIMIENTOS_FACTURAR_COMPRAS",
                 "APORTES_EN_TRANSITO", "APORTES_EN_TRANSITO_PAC", "APORTES_EN_TRANSITO_POR_REINVERSION",
                 "SIMULTANEA_COMPRAS", "COMPRA_PACTO", "COMPRA_TERMINO", "COMPRA_FORWARD",
                 "VENTA_PACTO", "VENTA_TERMINO", "VENTA_FORWARD" -> "MontoCompra";
            case "ORDENES_EN_TRANSITO_VENTAS", "ASIGNACIONES_VENTAS", "MOVIMIENTOS_FACTURAR_VENTAS",
                 "RESCATES_EN_TRANSITO", "SIMULTANEA_VENTAS" -> "MontoVenta";
            case "DIVIDENDOS_EN_DINERO", "CORTE_CUPON", "SORTEO_DE_LETRAS", "EMPRESTITOS", "PRESTAMOS",
                 "LINEA_SOBREGIRO", "LINEA_SOBREGIRO_TESORERIA", "ENTREGAMOS_OPERACION_SPOT",
                 "RECIBIMOS_OPERACION_SPOT_PAC", "RECIBIMOS_OPERACION_SPOT", "SALDO_RETENIDO",
                 "ABONOS_RETENIDOS_POR_LIQUIDAR", "CARGOS_RETENIDOS_POR_LIQUIDAR",
                 "CUSTODIA_FFMM_MONEY_MARKET", "MOVIMIENTOS_FACTURAR_COMISION_SALIDA_FFM" -> "Monto";
            default -> null;
        };
    }

    private BigDecimal nvd(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    private JsonObject toJson(Object o) { return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject(); }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    // ---- Public data classes (used by ConsultarFlujoCajaService and ConsultarFlujoCajaClienteService) ----

    public static class ObjPortafolio { public Long IdPortafolio; public String IdMoneda; }

    public static class PortafolioProceso {
        public String Id; public Long IdPersona; public Long IdPortafolio; public String IdMoneda;
    }

    public static class ConfiguracionFlujoCaja {
        public Long IdUsoFlujoCaja; public String NombreUso;
        public String ItemCalculo; public String ItemOrigen;
        public String FuncionOperador; public String DesplegarEnUI;
    }

    public static class RangoFecha {
        public String Id; public Long IdPersona; public Long IdPortafolio; public String IdMoneda;
        public Long IdUsoFlujoCaja; public String NombreUso;
        public String Item; public String ItemCalculo; public String ItemOrigen;
        public Long FuncionOperador; public String FuncionOperadorString;
        public String DesplegarEnUI; public Date Fecha;
    }

    public static class DatosFlujoCaja {
        public String Id; public Date FechaLiquidacion; public Long IdPortafolio; public String IdMoneda;
        public BigDecimal Monto; public BigDecimal MontoCompra; public BigDecimal MontoVenta;
        public String ItemCalculo;
    }

    public static class ObjFLUJOCAJA {
        public String NombreUso; public String FechaSaldo;
        public Long IdPortafolio; public String IdMoneda;
        public BigDecimal Saldo = BigDecimal.ZERO;
    }

    public static class ObjFLUJOCAJADETALLADO {
        public String FechaSaldo; public Long IdPortafolio; public String IdMoneda;
        public Long IdUsoFlujoCaja; public String NombreUso;
        public Long IdConfiguracionFlujoCaja = 0L;
        public String ItemCalculo; public String FuncionOperador;
        public BigDecimal Monto = BigDecimal.ZERO;
    }

    public static class ObjERRORESDETALLE { public String Codigo; public String Descripcion; }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ObjFLUJOCAJA> P_OUT_FLUJOCAJA = new ArrayList<>();
        public List<ObjFLUJOCAJADETALLADO> P_OUT_FLUJOCAJADETALLADO = new ArrayList<>();
        public List<ObjERRORESDETALLE> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }
}
