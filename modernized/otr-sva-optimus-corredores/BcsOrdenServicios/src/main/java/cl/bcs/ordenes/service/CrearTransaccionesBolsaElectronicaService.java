package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrates cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.
 * Processes BEC (Bolsa Electrónica de Chile) IF/RF/RV transactions into internal order format.
 * Delegates final persistence to CrearTransaccionesService.
 */
@Service
public class CrearTransaccionesBolsaElectronicaService {

    private static final Logger log = LoggerFactory.getLogger(CrearTransaccionesBolsaElectronicaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final CrearTransaccionesService crearTransaccionesService;

    private final String SQL_INSERT_WRAPPER;
    private final String SQL_SELECT_CORREDORAS;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_LIBRO_DEFECTO;

    private static final String SQL_EMPRESA =
        "SELECT EMP.\"IdEmpresa\", CORR.\"IdCorredora\", EMP.\"IdPais\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Empresa\" EMP " +
        "LEFT JOIN \"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio\" SOC ON EMP.\"IdSocioDeNegocio\" = SOC.\"IdSocioDeNegocio\" " +
        "LEFT JOIN \"BSCL.Plataforma.BD::INS.Corredora\" CORR ON CORR.\"IdPersona\" = SOC.\"IdPersona\" " +
        "WHERE EMP.\"IdEmpresa\" = :idEmpresa";

    public CrearTransaccionesBolsaElectronicaService(
            NamedParameterJdbcTemplate jdbc,
            ConfiguracionSistemaUtil configUtil,
            ObtenerDiaSiguienteUtil diaSiguienteUtil,
            CrearTransaccionesService crearTransaccionesService) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.crearTransaccionesService = crearTransaccionesService;
        this.SQL_INSERT_WRAPPER    = loadSql("CrearTransaccionesBolsaElectronica_InsertTransaccionesWrapper.sql");
        this.SQL_SELECT_CORREDORAS = loadSql("CrearTransaccionesBolsaElectronica_SelectCorredoras.sql");
        this.SQL_SELECT_TRANSACCIONES = loadSql("CrearTransaccionesBolsaElectronica_SelectTransacciones.sql");
        this.SQL_LIBRO_DEFECTO     = loadSql("Globales_ObtenerLibroPorDefecto.sql");
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada entrada = GSON.fromJson(params.toString(), Entrada.class);
        if (entrada.transaccionesIF == null) entrada.transaccionesIF = new ArrayList<>();
        if (entrada.transaccionesRV == null) entrada.transaccionesRV = new ArrayList<>();
        if (entrada.transaccionesRF == null) entrada.transaccionesRF = new ArrayList<>();

        Salida salida = new Salida();
        try {
            logica(entrada, salida);
        } catch (UnsupportedOperationException e) {
            log.warn("CrearTransaccionesBolsaElectronica: sub-service stub active ({}); partial result", e.getMessage());
            MensajeSalida m = new MensajeSalida();
            m.Codigo = "SERVICIO_PENDIENTE"; m.Descripcion = e.getMessage();
            salida.P_OUT_MENSAJES.add(m);
        } catch (Exception e) {
            log.error("CrearTransaccionesBolsaElectronica failed", e);
            MensajeSalida m = new MensajeSalida();
            m.Codigo = "ERROR_INTERNO"; m.Descripcion = e.getMessage();
            salida.P_OUT_MENSAJES.add(m);
        }
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    private void logica(Entrada entrada, Salida salida) {
        String fechaCarga = configUtil.getParametroString(entrada.p_IdEmpresa, "PLATAFORMA.PLA.FechaSistema");

        // Empresa info (need IdCorredora)
        Long idCorredoraEmpresa = queryIdCorredora(entrada.p_IdEmpresa);

        // Load corredoras indexed by CodigoCorredora
        Map<String, Corredora> corredoras = loadCorredoras();

        // Load libro por defecto
        Long idLibroOrdenes = queryLibroDefecto(entrada.p_IdEmpresa);
        if (idLibroOrdenes == null) {
            MensajeSalida m = new MensajeSalida();
            m.Codigo = "LIBRO_DEFECTO_NO_CONFIGURADO";
            m.Descripcion = "No se encontró libro de órdenes por defecto";
            salida.P_OUT_MENSAJES.add(m);
            return;
        }

        // Map BEC transactions to internal format
        List<TxProceso> transacciones = new ArrayList<>();
        procesarIF(entrada.transaccionesIF, corredoras, idCorredoraEmpresa, fechaCarga, idLibroOrdenes, transacciones, salida);
        procesarRF(entrada.transaccionesRF, corredoras, idCorredoraEmpresa, fechaCarga, idLibroOrdenes, transacciones, salida);
        procesarRV(entrada.transaccionesRV, corredoras, idCorredoraEmpresa, fechaCarga, idLibroOrdenes, transacciones, salida);

        if (transacciones.isEmpty()) return;

        // Batch insert to processing temp table
        batchInsertWrapper(transacciones);

        // Query instrument info per IdInterno
        Map<Long, InfoTransaccion> infoMap = queryInfoTransacciones();

        // Filter out transactions with missing instrument info, compute Cantidad for PDBC/PRBC
        List<TxProceso> ready = new ArrayList<>();
        for (TxProceso tx : transacciones) {
            InfoTransaccion info = infoMap.get(tx.IdInterno);
            if (info == null) {
                MensajeSalida m = new MensajeSalida();
                m.IdInterno = tx.IdInterno; m.Codigo = "INSTRUMENTO_NO_ENCONTRADO";
                m.Descripcion = "No se pudo obtener instrumento para IdInterno=" + tx.IdInterno;
                salida.P_OUT_MENSAJES.add(m);
                continue;
            }
            tx.IdTipoOperacion = info.IdTipoOperacion;
            tx.IdInstrumento = info.IdInstrumento;
            tx.GastosCorresponsal = BigDecimal.ZERO;
            tx.Intereses = BigDecimal.ZERO;
            tx.Reajustes = BigDecimal.ZERO;
            if ("PDBC".equalsIgnoreCase(tx.Nemotecnico) || "PRBC".equalsIgnoreCase(tx.Nemotecnico)) {
                if (info.CorteMinimo != null && info.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
                    tx.Cantidad = tx.ValorRescate.divide(info.CorteMinimo, BigDecimal.ROUND_HALF_UP);
                }
            }
            ready.add(tx);
        }

        if (ready.isEmpty()) return;

        // Calculate settlement dates via ObtenerDiaSiguienteUtil
        calcularFechasLiquidacion(ready, infoMap, entrada.p_IdEmpresa);

        // Delegate to CrearTransaccionesService
        JsonObject crearParams = buildCrearTransaccionesParams(entrada, ready);
        JsonObject crearResult = crearTransaccionesService.execute(crearParams);

        // Merge results
        if (crearResult.has("P_OUT_RESULTADO")) {
            crearResult.getAsJsonArray("P_OUT_RESULTADO").forEach(el -> {
                ResultadoSalida r = GSON.fromJson(el, ResultadoSalida.class);
                salida.P_OUT_RESULTADO.add(r);
            });
        }
        if (crearResult.has("P_OUT_MENSAJES")) {
            crearResult.getAsJsonArray("P_OUT_MENSAJES").forEach(el -> {
                MensajeSalida m = GSON.fromJson(el, MensajeSalida.class);
                salida.P_OUT_MENSAJES.add(m);
            });
        }
    }

    // ── Transaction type processors ────────────────────────────────────────────

    private void procesarIF(List<TransaccionIF> list, Map<String, Corredora> corredoras,
                            Long idCorredoraEmpresa, String fechaCarga, Long idLibro,
                            List<TxProceso> out, Salida salida) {
        for (TransaccionIF tx : list) {
            Corredora corrCompra = corredoras.get(str(tx.COD_CORR_COM));
            Corredora corrVenta  = corredoras.get(str(tx.COD_CORR_VEN));
            TxProceso txe = new TxProceso();

            if (corrCompra == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_COM + " no cargado"); continue; }
            if (corrVenta  == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_VEN + " no cargado"); continue; }

            if (corrCompra.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_COM);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_VEN);
                txe.IdCorredoraContraparte    = corrVenta.IdCorredora;
                txe.AccionOrden = "COMPRA";
            } else if (corrVenta.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_VEN);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_COM);
                txe.IdCorredoraContraparte    = corrCompra.IdCorredora;
                txe.AccionOrden = "VENTA";
            } else {
                addError(salida, tx.IdInterno, "Transaccion otros corredores Folio:" + tx.FOLIO + " C:" + tx.COD_CORR_COM + " V:" + tx.COD_CORR_VEN);
                continue;
            }

            String nemo = tx.NEMO != null ? tx.NEMO.trim() : "";
            txe.Cantidad = ("PRBC".equalsIgnoreCase(nemo) || "PDBC".equalsIgnoreCase(nemo)) ? BigDecimal.ZERO : BigDecimal.ONE;
            txe.EsBilateral = "N"; txe.EsCuentaPropia = "N"; txe.EsFueraRueda = "N";
            txe.EsFungible = ("PDBC".equalsIgnoreCase(nemo) || "PRBC".equalsIgnoreCase(nemo)) ? "N" : "S";
            txe.IndicadorOrdenDirecta = ""; txe.EsVentaCorta = "N";
            txe.FechaCreacion = (fechaCarga == null || fechaCarga.isEmpty() || "0001-01-01".equalsIgnoreCase(fechaCarga)) ? tx.FECHA : fechaCarga;
            txe.FechaEmision = tx.FEC_EMI; txe.FechaVencimiento = tx.FEC_VCTO;
            txe.FolioTransaccion = str(tx.FOLIO); txe.FolioTransaccionPlazo = "";
            txe.HoraCreacion = safeHora(tx.HORA);
            txe.IdBolsa = "BEC";
            txe.IdCondicionDeLiquidacionContado = mapCondicion(tx.COND_COD_002);
            txe.IdCorredoraCorresponsal = 0L; txe.IdEmpleado = 0L;
            txe.IdEmisor = tx.EMISOR != null ? String.valueOf(tx.EMISOR) : "";
            txe.IdInstrumentoSubyacente = 0L; txe.IdInterno = tx.IdInterno;
            txe.IdLibroOrdenes = idLibro; txe.IdMonedaLiquidacionCorresponsal = "";
            txe.IdSistemaBursatil = "BEC";
            txe.IdTipoReajuste = "NR".equalsIgnoreCase(tx.TIPO_REAJ) ? "CLP" : (tx.TIPO_REAJ != null ? tx.TIPO_REAJ : "");
            txe.IndicadorDCV = "S"; txe.MarcaNegociacion = "";
            txe.MejorPrecioCompra = BigDecimal.ZERO; txe.MejorPrecioVenta = BigDecimal.ZERO;
            txe.Monto = tx.MONTO != null ? tx.MONTO : BigDecimal.ZERO;
            txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO; txe.MontoPlazo = BigDecimal.ZERO;
            txe.Nemotecnico = nemo; txe.IdMoneda = "CLP";
            txe.Plazo = tx.PLAZO != null ? tx.PLAZO.longValue() : 0L;
            txe.Precio = BigDecimal.ZERO; txe.PrecioPlazo = BigDecimal.ZERO;
            txe.Tasa = tx.TASA != null ? tx.TASA : BigDecimal.ZERO;
            txe.TipoCambioCorresponsal = BigDecimal.ZERO;
            txe.ValorRescate = tx.VAL_RESC != null ? tx.VAL_RESC : BigDecimal.ZERO;
            txe.FlujoOrigen = "INGRESOORDEN";
            out.add(txe);
        }
    }

    private void procesarRF(List<TransaccionRF> list, Map<String, Corredora> corredoras,
                            Long idCorredoraEmpresa, String fechaCarga, Long idLibro,
                            List<TxProceso> out, Salida salida) {
        for (TransaccionRF tx : list) {
            Corredora corrCompra = corredoras.get(str(tx.COD_CORR_COM));
            Corredora corrVenta  = corredoras.get(str(tx.COD_CORR_VEN));
            TxProceso txe = new TxProceso();

            if (corrCompra == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_COM + " no cargado"); continue; }
            if (corrVenta  == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_VEN + " no cargado"); continue; }

            if (corrCompra.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_COM);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_VEN);
                txe.IdCorredoraContraparte    = corrVenta.IdCorredora;
                txe.AccionOrden = "COMPRA";
            } else if (corrVenta.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_VEN);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_COM);
                txe.IdCorredoraContraparte    = corrCompra.IdCorredora;
                txe.AccionOrden = "VENTA";
            } else {
                addError(salida, tx.IdInterno, "Transaccion otros corredores Folio:" + tx.FOLIO + " C:" + tx.COD_CORR_COM + " V:" + tx.COD_CORR_VEN);
                continue;
            }

            txe.Cantidad = tx.CANTIDAD != null ? tx.CANTIDAD : BigDecimal.ZERO;
            txe.EsBilateral = "N"; txe.EsCuentaPropia = "N"; txe.EsFueraRueda = "N";
            txe.EsFungible = "N"; txe.IndicadorOrdenDirecta = ""; txe.EsVentaCorta = "N";
            txe.FechaCreacion = (fechaCarga == null || fechaCarga.isEmpty() || "0001-01-01".equalsIgnoreCase(fechaCarga)) ? tx.FECHA : fechaCarga;
            txe.FechaEmision = "0001-01-01"; txe.FechaVencimiento = "0001-01-01";
            txe.FolioTransaccion = str(tx.FOLIO); txe.FolioTransaccionPlazo = "";
            txe.HoraCreacion = safeHora(tx.HORA);
            txe.IdBolsa = "BEC";
            txe.IdCondicionDeLiquidacionContado = mapCondicion(tx.COND_COD_002);
            txe.IdCorredoraCorresponsal = 0L; txe.IdEmpleado = 0L; txe.IdEmisor = "";
            txe.IdInstrumentoSubyacente = 0L; txe.IdInterno = tx.IdInterno;
            txe.IdLibroOrdenes = idLibro; txe.IdMonedaLiquidacionCorresponsal = "";
            txe.IdSistemaBursatil = "BEC"; txe.IdTipoReajuste = "";
            txe.IndicadorDCV = "S"; txe.MarcaNegociacion = "";
            txe.MejorPrecioCompra = BigDecimal.ZERO; txe.MejorPrecioVenta = BigDecimal.ZERO;
            txe.Monto = tx.MONTO != null ? tx.MONTO : BigDecimal.ZERO;
            txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO; txe.MontoPlazo = BigDecimal.ZERO;
            txe.Nemotecnico = tx.NEMO != null ? tx.NEMO : "";
            txe.NemotecnicoEspecificoBolson = txe.Nemotecnico;
            txe.Plazo = 0L; txe.Precio = tx.PRECIO != null ? tx.PRECIO : BigDecimal.ZERO;
            txe.PrecioPlazo = BigDecimal.ZERO;
            txe.Tasa = tx.TIR != null ? tx.TIR : BigDecimal.ZERO;
            txe.TipoCambioCorresponsal = BigDecimal.ZERO; txe.ValorRescate = BigDecimal.ZERO;
            txe.FlujoOrigen = "INGRESOORDEN";
            out.add(txe);
        }
    }

    private void procesarRV(List<TransaccionRV> list, Map<String, Corredora> corredoras,
                            Long idCorredoraEmpresa, String fechaCarga, Long idLibro,
                            List<TxProceso> out, Salida salida) {
        for (TransaccionRV tx : list) {
            Corredora corrCompra = corredoras.get(str(tx.COD_CORR_COM));
            Corredora corrVenta  = corredoras.get(str(tx.COD_CORR_VEN));
            TxProceso txe = new TxProceso();

            if (corrCompra == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_COM + " no cargado"); continue; }
            if (corrVenta  == null) { addError(salida, tx.IdInterno, "Corredor " + tx.COD_CORR_VEN + " no cargado"); continue; }

            if (corrCompra.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_COM);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_VEN);
                txe.IdCorredoraContraparte    = corrVenta.IdCorredora;
                txe.AccionOrden = "COMPRA";
            } else if (corrVenta.IdCorredora.equals(idCorredoraEmpresa)) {
                txe.CodigoOperadorCorredor    = str(tx.COD_OPE_VEN);
                txe.CodigoOperadorContraparte = str(tx.COD_OPE_COM);
                txe.IdCorredoraContraparte    = corrCompra.IdCorredora;
                txe.AccionOrden = "VENTA";
            } else {
                addError(salida, tx.IdInterno, "Transaccion otros corredores Folio:" + tx.FOLIO + " C:" + tx.COD_CORR_COM + " V:" + tx.COD_CORR_VEN);
                continue;
            }

            txe.Cantidad = tx.CANTIDAD != null ? tx.CANTIDAD : BigDecimal.ZERO;
            txe.EsBilateral = "N"; txe.EsCuentaPropia = "N"; txe.EsFueraRueda = "N";
            txe.EsFungible = "N"; txe.IndicadorOrdenDirecta = "";
            txe.EsVentaCorta = "V".equalsIgnoreCase(tx.COND_COD_004) ? "S" : "N";
            txe.FechaCreacion = (fechaCarga == null || fechaCarga.isEmpty() || "0001-01-01".equalsIgnoreCase(fechaCarga)) ? tx.FECHA : fechaCarga;
            txe.FechaEmision = "0001-01-01"; txe.FechaVencimiento = "0001-01-01";
            txe.FolioTransaccion = str(tx.FOLIO); txe.FolioTransaccionPlazo = "";
            txe.HoraCreacion = safeHora(tx.HORA);
            txe.IdBolsa = "BEC";
            txe.IdCondicionDeLiquidacionContado = mapCondicion(tx.COND_COD_002);
            txe.IdCorredoraCorresponsal = 0L; txe.IdEmpleado = 0L; txe.IdEmisor = "";
            txe.IdInstrumentoSubyacente = 0L; txe.IdInterno = tx.IdInterno;
            txe.IdLibroOrdenes = idLibro; txe.IdMonedaLiquidacionCorresponsal = "";
            txe.IdSistemaBursatil = "BEC"; txe.IdTipoReajuste = "";
            txe.IndicadorDCV = "D".equalsIgnoreCase(tx.IND_DCV) ? "S" : "N";
            txe.MarcaNegociacion = "";
            txe.MejorPrecioCompra = BigDecimal.ZERO; txe.MejorPrecioVenta = BigDecimal.ZERO;
            txe.Monto = tx.MONTO != null ? tx.MONTO : BigDecimal.ZERO;
            txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO; txe.MontoPlazo = BigDecimal.ZERO;
            txe.Nemotecnico = tx.NEMO != null ? tx.NEMO.trim() : "";
            txe.NemotecnicoEspecificoBolson = tx.NEMO;
            txe.Plazo = 0L; txe.Precio = tx.PRECIO != null ? tx.PRECIO : BigDecimal.ZERO;
            txe.PrecioPlazo = BigDecimal.ZERO; txe.Tasa = BigDecimal.ZERO;
            txe.TipoCambioCorresponsal = BigDecimal.ZERO; txe.ValorRescate = BigDecimal.ZERO;
            txe.FlujoOrigen = "INGRESOORDEN";
            out.add(txe);
        }
    }

    // ── JDBC helpers ──────────────────────────────────────────────────────────

    private Long queryIdCorredora(Long idEmpresa) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_EMPRESA,
                new MapSqlParameterSource("idEmpresa", idEmpresa));
            if (rows.isEmpty()) return 0L;
            Object v = rows.get(0).get("IdCorredora");
            return v instanceof Number ? ((Number) v).longValue() : 0L;
        } catch (Exception e) {
            log.warn("queryIdCorredora failed for empresa={}: {}", idEmpresa, e.getMessage());
            return 0L;
        }
    }

    private Map<String, Corredora> loadCorredoras() {
        Map<String, Corredora> map = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_SELECT_CORREDORAS,
                new MapSqlParameterSource("idBolsa", "BCS"));
            for (Map<String, Object> r : rows) {
                Corredora c = new Corredora();
                c.IdCorredora = toLong(r.get("IdCorredora"));
                c.CodigoCorredora = str(r.get("CodigoCorredora"));
                c.CodigoCorredoraFix = str(r.get("CodigoCorredoraFix"));
                c.IdBolsa = str(r.get("IdBolsa"));
                map.put(c.CodigoCorredora, c);
            }
        } catch (Exception e) {
            log.warn("loadCorredoras failed: {}", e.getMessage());
        }
        return map;
    }

    private Long queryLibroDefecto(Long idEmpresa) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_LIBRO_DEFECTO,
                new MapSqlParameterSource("idEmpresa", idEmpresa));
            if (rows.isEmpty()) return null;
            return toLong(rows.get(0).get("IdLibroOrdenes"));
        } catch (Exception e) {
            log.warn("queryLibroDefecto failed: {}", e.getMessage());
            return null;
        }
    }

    private void batchInsertWrapper(List<TxProceso> txs) {
        MapSqlParameterSource[] batch = txs.stream().map(tx -> new MapSqlParameterSource()
            .addValue("IdInterno", tx.IdInterno)
            .addValue("Nemotecnico", tx.Nemotecnico)
            .addValue("AccionOrden", tx.AccionOrden)
            .addValue("IdCondicionDeLiquidacionContado", tx.IdCondicionDeLiquidacionContado)
            .addValue("IdTipoReajuste", tx.IdTipoReajuste)
        ).toArray(MapSqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_INSERT_WRAPPER, batch);
    }

    private Map<Long, InfoTransaccion> queryInfoTransacciones() {
        Map<Long, InfoTransaccion> map = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_SELECT_TRANSACCIONES,
                new MapSqlParameterSource());
            for (Map<String, Object> r : rows) {
                InfoTransaccion i = new InfoTransaccion();
                i.IdInterno = toLong(r.get("IdInterno"));
                i.Nemotecnico = str(r.get("Nemotecnico"));
                i.IdInstrumento = toLong(r.get("IdInstrumento"));
                i.IdTipoInstrumento = str(r.get("IdTipoInstrumento"));
                i.IdCondicionDeLiquidacionContado = str(r.get("IdCondicionDeLiquidacionContado"));
                i.DiasLiquidacionContado = toLong(r.get("DiasLiquidacionContado"));
                i.IdTipoOperacion = str(r.get("IdTipoOperacion"));
                Object cm = r.get("CorteMinimo");
                i.CorteMinimo = cm instanceof BigDecimal ? (BigDecimal) cm :
                                cm instanceof Number ? BigDecimal.valueOf(((Number) cm).doubleValue()) : null;
                map.put(i.IdInterno, i);
            }
        } catch (Exception e) {
            log.warn("queryInfoTransacciones failed: {}", e.getMessage());
        }
        return map;
    }

    private void calcularFechasLiquidacion(List<TxProceso> txs, Map<Long, InfoTransaccion> infoMap, Long idEmpresa) {
        List<ObtenerDiaSiguienteUtil.FechaIn> fechasIn = new ArrayList<>();
        for (TxProceso tx : txs) {
            InfoTransaccion info = infoMap.get(tx.IdInterno);
            if (info == null) continue;
            ObtenerDiaSiguienteUtil.FechaIn f = new ObtenerDiaSiguienteUtil.FechaIn();
            f.fechaInicial = tx.FechaCreacion;
            f.numeroDias = info.DiasLiquidacionContado != null ? info.DiasLiquidacionContado : 0L;
            f.pais = idEmpresa;
            f.IdInterno = tx.IdInterno;
            fechasIn.add(f);
        }
        if (fechasIn.isEmpty()) return;
        ObtenerDiaSiguienteUtil.Salida sal = diaSiguienteUtil.calcular(idEmpresa, "S", "N", fechasIn);
        if (sal == null) return;
        for (ObtenerDiaSiguienteUtil.FechaOut fo : sal.p_out_fechas) {
            for (TxProceso tx : txs) {
                InfoTransaccion info = infoMap.get(tx.IdInterno);
                if (info == null) continue;
                if (tx.FechaCreacion.equalsIgnoreCase(fo.fechaInicial)
                        && Objects.equals(info.DiasLiquidacionContado, fo.numeroDiasInicial)) {
                    tx.FechaLiquidacionContado = fo.fechaCalculada;
                }
            }
        }
    }

    private JsonObject buildCrearTransaccionesParams(Entrada e, List<TxProceso> txs) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", e.p_IdEmpresa);
        p.addProperty("p_IdModulo", "ORDENES");
        p.addProperty("p_IdUsuario", e.p_IdUsuario);
        p.addProperty("p_simularOperacion", "N");
        p.addProperty("p_UsaReglasNegocio", "S");
        p.add("p_Transacciones", GSON.fromJson(GSON.toJson(txs), com.google.gson.JsonArray.class));
        return p;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String mapCondicion(String cond) {
        if (cond == null) return "T2";
        return switch (cond.toUpperCase()) {
            case "H" -> "T0";
            case "M" -> "T1";
            case "T" -> "TP";
            case "3" -> "T3";
            default  -> "T2";
        };
    }

    private static String safeHora(String hora) {
        if (hora == null || hora.length() < 19) return hora != null ? hora : "";
        return hora.substring(11, 19);
    }

    private static String str(Object o) { return o != null ? o.toString() : ""; }

    private static Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static void addError(Salida salida, Long idInterno, String msg) {
        MensajeSalida m = new MensajeSalida();
        m.IdInterno = idInterno; m.Codigo = "ERROR"; m.Descripcion = msg;
        salida.P_OUT_MENSAJES.add(m);
    }

    private String loadSql(String filename) {
        String path = "cl/bcs/ordenes/sql/" + filename;
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public List<TransaccionIF> transaccionesIF = new ArrayList<>();
        public List<TransaccionRV> transaccionesRV = new ArrayList<>();
        public List<TransaccionRF> transaccionesRF = new ArrayList<>();
    }

    public static class TransaccionIF {
        public Long     IdInterno;
        public Object   COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN;
        public String   NEMO, FECHA, HORA, COND_COD_002, TIPO_REAJ, FEC_EMI, FEC_VCTO;
        public Object   FOLIO, EMISOR;
        public BigDecimal MONTO, TASA, VAL_RESC;
        public BigDecimal PLAZO;
    }

    public static class TransaccionRF {
        public Long     IdInterno;
        public Object   COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN;
        public String   NEMO, FECHA, HORA, COND_COD_002;
        public Object   FOLIO;
        public BigDecimal MONTO, PRECIO, TIR, CANTIDAD;
    }

    public static class TransaccionRV {
        public Long     IdInterno;
        public Object   COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN;
        public String   NEMO, FECHA, HORA, COND_COD_002, COND_COD_004, IND_DCV;
        public Object   FOLIO;
        public BigDecimal MONTO, PRECIO, CANTIDAD;
    }

    public static class Corredora {
        public Long   IdCorredora;
        public String CodigoCorredora, CodigoCorredoraFix, IdBolsa;
    }

    public static class InfoTransaccion {
        public Long       IdInterno;
        public String     Nemotecnico, IdTipoInstrumento, IdCondicionDeLiquidacionContado, IdTipoOperacion;
        public Long       IdInstrumento, DiasLiquidacionContado;
        public BigDecimal CorteMinimo;
        public String     IdTipoReajuste;
    }

    public static class TxProceso {
        public Long       IdInterno, IdLibroOrdenes, IdInstrumento, IdInstrumentoSubyacente;
        public Long       IdCorredoraContraparte, IdCorredoraCorresponsal, IdEmpleado;
        public Long       Plazo;
        public String     AccionOrden, CodigoOperadorCorredor, CodigoOperadorContraparte;
        public String     EsBilateral, EsCuentaPropia, EsFueraRueda, EsFungible;
        public String     IndicadorOrdenDirecta, EsVentaCorta;
        public String     FechaCreacion, FechaEmision, FechaLiquidacionContado, FechaVencimiento;
        public String     FolioTransaccion, FolioTransaccionPlazo, HoraCreacion;
        public String     IdBolsa, IdCondicionDeLiquidacionContado, IdEmisor;
        public String     IdMoneda, IdMonedaLiquidacionCorresponsal, IdSistemaBursatil;
        public String     IdTipoOperacion, IdTipoReajuste, IndicadorDCV, MarcaNegociacion;
        public String     Nemotecnico, NemotecnicoEspecificoBolson, FlujoOrigen;
        public BigDecimal Cantidad, Monto, MontoPlazo, MontoLiquidacionCorresponsal;
        public BigDecimal MejorPrecioCompra, MejorPrecioVenta;
        public BigDecimal Precio, PrecioPlazo, Tasa, TipoCambioCorresponsal, ValorRescate;
        public BigDecimal GastosCorresponsal, Intereses, Reajustes;
    }

    public static class ResultadoSalida {
        public Long   IdInterno, IdTransaccion;
        public String IdTipoOperacion, EsAPlazo;
    }

    public static class MensajeSalida {
        public Long   IdInterno;
        public String Codigo, Descripcion, TipoMensaje;
    }

    public static class Salida {
        public List<ResultadoSalida> P_OUT_RESULTADO  = new ArrayList<>();
        public List<MensajeSalida>   P_OUT_MENSAJES   = new ArrayList<>();
    }
}
