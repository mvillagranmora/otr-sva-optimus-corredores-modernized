package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import cl.bcs.ordenes.util.ObtenerPlazoUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Migrated from legacy cl.bcs.ordenes.ProcesarCargaMasivaInstruccion (JavaCall).
 * Processes bulk instruction loads: enriches raw rows, validates, then delegates to CrearInstruccionesService.
 */
@Service
public class ProcesarCargaMasivaInstruccionService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaInstruccionService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ObtenerPlazoUtil plazoUtil;
    private final CrearInstruccionesService crearInstruccionesService;

    private final String SQL_CLIENTE_PORTAFOLIO;
    private final String SQL_INSTRUMENTO;
    private final String SQL_INTERMEDIARIO;
    private final String SQL_EMISOR;
    private final String SQL_CONDICION_LIQUIDACION;

    public ProcesarCargaMasivaInstruccionService(
            NamedParameterJdbcTemplate jdbc,
            ConfiguracionSistemaUtil configUtil,
            ObtenerDiaSiguienteUtil diaSiguienteUtil,
            ObtenerPlazoUtil plazoUtil,
            CrearInstruccionesService crearInstruccionesService) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.plazoUtil = plazoUtil;
        this.crearInstruccionesService = crearInstruccionesService;
        this.SQL_CLIENTE_PORTAFOLIO  = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectClientePortafolio.sql");
        this.SQL_INSTRUMENTO         = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectInstrumento.sql");
        this.SQL_INTERMEDIARIO       = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectIntermediario.sql");
        this.SQL_EMISOR              = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectEmisor.sql");
        this.SQL_CONDICION_LIQUIDACION = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectCondicionLiquidacion.sql");
    }

    public JsonObject execute(JsonObject params) {
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
        Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
        String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "";
        String usaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "N";
        String canal = params.has("p_IdTipoCanalIngreso") ? params.get("p_IdTipoCanalIngreso").getAsString() : "";

        List<Map<String, Object>> instruccionesRaw = new ArrayList<>();
        if (params.has("p_Instrucciones") && params.get("p_Instrucciones").isJsonArray()) {
            for (JsonElement el : params.get("p_Instrucciones").getAsJsonArray()) {
                instruccionesRaw.add(jsonToMap(el.getAsJsonObject()));
            }
        }

        List<JsonObject> mensajes = new ArrayList<>();
        long erroresContador = 0L;
        JsonArray refIds = new JsonArray();

        if (instruccionesRaw.isEmpty()) {
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", new JsonArray());
            out.add("P_OUT_REFID", new JsonArray());
            return out;
        }

        Long idPais = configUtil.getParametroLong(idEmpresa, "PLATAFORMA.PLA.PaisSistema");
        if (idPais == 0L) idPais = configUtil.getParametroLong(idEmpresa, "PLA.PaisSistema");
        String fechaSistema = configUtil.getParametroString(idEmpresa, "PLATAFORMA.PLA.FechaSistema");
        if (fechaSistema == null || fechaSistema.isBlank())
            fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");

        // ── Step 1: batch-load reference data ────────────────────────────────
        List<String> documentosIdentidad = instruccionesRaw.stream()
            .map(r -> str(r.get("DocumentoIdentidad"))).distinct().collect(Collectors.toList());
        List<String> nemotecnicos = instruccionesRaw.stream()
            .map(r -> str(r.get("Nemotecnico"))).distinct().collect(Collectors.toList());
        List<Long> intermediariosIds = instruccionesRaw.stream()
            .map(r -> toLong(r.get("Intermediario"))).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<String> idEmisores = instruccionesRaw.stream()
            .map(r -> str(r.get("IdEmisor"))).distinct().collect(Collectors.toList());
        List<String> condicionCodigos = instruccionesRaw.stream()
            .map(r -> str(r.get("CondicionLiquidacion"))).distinct().collect(Collectors.toList());

        Map<String, Map<String, Object>> clientesPortafolioMap = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_CLIENTE_PORTAFOLIO,
                new MapSqlParameterSource("DocumentoIdentidad", documentosIdentidad));
            for (Map<String, Object> r : rows)
                clientesPortafolioMap.put(str(r.get("DocumentoIdentidad")) + "_" + str(r.get("CodigoPortafolio")), r);
        } catch (Exception e) { log.warn("ClientePortafolio lookup failed: {}", e.getMessage()); }

        Map<String, Map<String, Object>> instrumentoMap = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_INSTRUMENTO,
                new MapSqlParameterSource("Nemotecnico", nemotecnicos));
            for (Map<String, Object> r : rows)
                instrumentoMap.put(str(r.get("Nemotecnico")), r);
        } catch (Exception e) { log.warn("Instrumento lookup failed: {}", e.getMessage()); }

        Map<Long, Map<String, Object>> intermediarioMap = new HashMap<>();
        if (!intermediariosIds.isEmpty()) {
            try {
                List<Map<String, Object>> rows = jdbc.queryForList(SQL_INTERMEDIARIO,
                    new MapSqlParameterSource("Intermediario", intermediariosIds));
                for (Map<String, Object> r : rows)
                    intermediarioMap.put(toLong(r.get("Intermediario")), r);
            } catch (Exception e) { log.warn("Intermediario lookup failed: {}", e.getMessage()); }
        }

        Map<String, Map<String, Object>> emisorMap = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_EMISOR,
                new MapSqlParameterSource("IdEmisor", idEmisores));
            for (Map<String, Object> r : rows)
                emisorMap.put(str(r.get("IdEmisor")), r);
        } catch (Exception e) { log.warn("Emisor lookup failed: {}", e.getMessage()); }

        Map<String, Map<String, Object>> condicionMap = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_CONDICION_LIQUIDACION,
                new MapSqlParameterSource("Codigo", condicionCodigos));
            for (Map<String, Object> r : rows)
                condicionMap.put(str(r.get("Codigo")), r);
        } catch (Exception e) { log.warn("CondicionLiquidacion lookup failed: {}", e.getMessage()); }

        // ── Step 2: calculate settlement dates via ObtenerDiaSiguienteUtil ───
        List<ObtenerDiaSiguienteUtil.FechaIn> fechasIn = new ArrayList<>();
        for (Map<String, Object> instr : instruccionesRaw) {
            Map<String, Object> condicion = condicionMap.get(str(instr.get("CondicionLiquidacion")));
            if (condicion != null) {
                ObtenerDiaSiguienteUtil.FechaIn f = new ObtenerDiaSiguienteUtil.FechaIn();
                f.fechaInicial = str(instr.get("FechaTrade"));
                f.numeroDias  = toLong(condicion.get("PlazoEnDias"));
                f.pais        = idPais;
                fechasIn.add(f);
            }
        }
        Map<String, Date> fechasLiquidacion = new HashMap<>();
        if (!fechasIn.isEmpty()) {
            try {
                ObtenerDiaSiguienteUtil.Salida salFechas =
                    diaSiguienteUtil.calcular(idPais, "S", "S", fechasIn);
                for (ObtenerDiaSiguienteUtil.FechaOut fo : salFechas.p_out_fechas) {
                    String key = fo.fechaInicial + "_" + fo.numeroDiasInicial;
                    try { fechasLiquidacion.put(key, Date.valueOf(fo.fechaCalculada)); }
                    catch (Exception ignore) {}
                }
            } catch (Exception e) { log.warn("ObtenerDiaSiguiente failed: {}", e.getMessage()); }
        }

        // ── Step 3: enrich each instruccion into proceso object ───────────────
        List<Map<String, Object>> instruccionesProceso = new ArrayList<>();
        for (Map<String, Object> instr : instruccionesRaw) {
            Map<String, Object> proc = new LinkedHashMap<>(instr);

            String tipoInstruccion = str(instr.get("TipoInstruccion"));
            String operacion       = str(instr.get("Operacion"));
            String idTipoInstr     = obtenerIdTipoInstrumentoInstruccion(tipoInstruccion);
            String idTipoOpInstr   = obtenerIdTipoOperacionInstruccion(operacion, idTipoInstr);
            String codMotivo       = obtenerCodigoMotivoInstruccion(canal);

            proc.put("IdTipoInstrumentoInstruccion", idTipoInstr);
            proc.put("IdTipoOperacionInstruccion", idTipoOpInstr);
            proc.put("CodMotivoInstruccion", codMotivo);
            proc.put("IdMonedaLiquidacion", "CLP");
            proc.put("IdClasificacionTipoInstrumentoInstruccion", "IR");
            proc.put("IdBolsa", "BCS");
            proc.put("IdBoveda", "DCV");
            proc.put("PlazoVencimiento", 0L);
            proc.put("IdEstadoProceso", "PEN");
            proc.put("Glosa", "");

            // Precio
            BigDecimal precio = toBigDecimal(instr.get("Precio"));
            BigDecimal cantidad = toBigDecimal(instr.get("Cantidad"));
            BigDecimal montoTotal = toBigDecimal(instr.get("MontoTotal"));
            BigDecimal totalCobros = toBigDecimal(instr.get("TotalCobros"));
            if ("IRV".equals(idTipoInstr) && precio.compareTo(BigDecimal.ZERO) == 0
                    && cantidad.compareTo(BigDecimal.ZERO) != 0) {
                precio = montoTotal.divide(cantidad, MathContext.DECIMAL128);
            }
            proc.put("PrecioInstruccion", precio);
            proc.put("MontoNeto", montoTotal.subtract(totalCobros, MathContext.DECIMAL128));

            // ClientePortafolio
            String cpKey = str(instr.get("DocumentoIdentidad")) + "_" + str(instr.get("CodigoPortafolio"));
            Map<String, Object> cp = clientesPortafolioMap.get(cpKey);
            if (cp != null) {
                proc.put("IdCliente", cp.get("IdCliente"));
                proc.put("IdEstadoCliente", cp.get("IdEstadoCliente"));
                proc.put("IdPortafolio", cp.get("IdPortafolio"));
                proc.put("IdEstadoPortafolio", cp.get("IdEstadoPortafolio"));
                proc.put("NombreCompleto", cp.get("NombreCompleto"));
            }

            // Instrumento
            Map<String, Object> inst = instrumentoMap.get(str(instr.get("Nemotecnico")));
            if (inst != null) {
                proc.put("IdClasificacionTipoInstrumento", inst.get("IdClasificacionTipoInstrumento"));
                proc.put("IdTipoInstrumento", inst.get("IdTipoInstrumento"));
                proc.put("IdInstrumento", inst.get("IdInstrumento"));
                proc.put("CorteMinimo", toBigDecimal(inst.get("CorteMinimo")));
                String idTipoReajuste = "IFI".equals(idTipoInstr)
                    ? str(instr.get("IdTipoReajuste"))
                    : str(inst.get("IdMonedaEmision"));
                proc.put("IdTipoReajuste", idTipoReajuste);
            }

            // Intermediario
            Map<String, Object> inter = intermediarioMap.get(toLong(instr.get("Intermediario")));
            if (inter != null) proc.put("IdIntermediario", inter.get("IdIntermediario"));

            // Emisor
            Map<String, Object> emisor = emisorMap.get(str(instr.get("IdEmisor")));
            if (emisor != null) proc.put("EsFungible", emisor.get("EmiteFungible"));

            // FechaLiquidacion
            Map<String, Object> condicion = condicionMap.get(str(instr.get("CondicionLiquidacion")));
            if (condicion != null) {
                proc.put("IdCondicionDeLiquidacion", condicion.get("IdCondicionDeLiquidacion"));
                String liqKey = str(instr.get("FechaTrade")) + "_" + toLong(condicion.get("PlazoEnDias"));
                Date fechaLiq = fechasLiquidacion.get(liqKey);
                proc.put("FechaLiquidacion", fechaLiq != null ? fechaLiq.toString() : null);
            }

            // ValorRescate / ValorNominal / Cantidad (type-specific)
            proc.put("ValorRescate", "IFI".equals(idTipoInstr) ? toBigDecimal(instr.get("ValorRescate")) : BigDecimal.ZERO);
            proc.put("ValorNominal", "IRF".equals(idTipoInstr) ? cantidad : BigDecimal.ZERO);

            // Cantidad corte for IFI
            if ("IFI".equals(idTipoInstr)) {
                String esFungible = str(proc.get("EsFungible"));
                BigDecimal corteMinimo = toBigDecimal(proc.get("CorteMinimo"));
                BigDecimal valorRescate = toBigDecimal(proc.get("ValorRescate"));
                BigDecimal cantidadCalc = BigDecimal.ZERO;
                if ("S".equals(esFungible)) {
                    cantidadCalc = BigDecimal.ONE;
                } else if ("N".equals(esFungible) && corteMinimo.compareTo(BigDecimal.ZERO) != 0) {
                    cantidadCalc = valorRescate.divide(corteMinimo, MathContext.DECIMAL128);
                }
                proc.put("Cantidad", cantidadCalc);
            }

            instruccionesProceso.add(proc);
        }

        // ── Step 4: calculate plazo for RF/IF ─────────────────────────────────
        List<ObtenerPlazoUtil.FechasIn> plazosIn = new ArrayList<>();
        for (Map<String, Object> proc : instruccionesProceso) {
            String tipo = str(proc.get("TipoInstruccion"));
            String fecLiq = str(proc.get("FechaLiquidacion"));
            if (("RF".equals(tipo) || "IF".equals(tipo)) && fecLiq != null && !fecLiq.isBlank()) {
                ObtenerPlazoUtil.FechasIn pi = new ObtenerPlazoUtil.FechasIn();
                pi.IdInterno = toLong(proc.get("IdInterno"));
                pi.FechaDesde = fecLiq;
                pi.FechaHasta = str(proc.get("FechaVencimiento"));
                pi.SoloDiasHabiles = "N";
                plazosIn.add(pi);
            }
        }
        if (!plazosIn.isEmpty()) {
            try {
                ObtenerPlazoUtil.Salida salPlazo = plazoUtil.calcular(idPais, plazosIn);
                Map<Long, Long> plazosPorId = salPlazo.p_out_Fechas.stream()
                    .collect(Collectors.toMap(fo -> fo.IdInterno, fo -> fo.Plazo, (a, b) -> a));
                for (Map<String, Object> proc : instruccionesProceso) {
                    Long idInterno = toLong(proc.get("IdInterno"));
                    Long plazo = plazosPorId.get(idInterno);
                    if (plazo != null) proc.put("PlazoVencimiento", plazo);
                }
            } catch (Exception e) { log.warn("ObtenerPlazo failed: {}", e.getMessage()); }
        }

        // ── Step 5: validate ──────────────────────────────────────────────────
        for (Map<String, Object> proc : instruccionesProceso) {
            Long idInterno = toLong(proc.get("IdInterno"));

            if (proc.get("IdCliente") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE",
                    "Cliente/portafolio no encontrado: " + proc.get("DocumentoIdentidad") + "/" + proc.get("CodigoPortafolio")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("IdTipoInstrumentoInstruccion") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE",
                    "TipoInstruccion inválido: " + proc.get("TipoInstruccion")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("IdTipoOperacionInstruccion") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE",
                    "Operacion inválida: " + proc.get("Operacion")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("IdInstrumento") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE",
                    "Instrumento no encontrado: " + proc.get("Nemotecnico")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("IdIntermediario") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE",
                    "Intermediario no encontrado: " + proc.get("Intermediario")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("EsFungible") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE",
                    "Emisor no encontrado: " + proc.get("IdEmisor")));
                proc.put("IdEstadoProceso", "ERR");
            }
            if (proc.get("IdCondicionDeLiquidacion") == null) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE",
                    "CondicionLiquidacion inválida: " + proc.get("CondicionLiquidacion")));
                proc.put("IdEstadoProceso", "ERR");
            }
            // FechaVencimiento required for RF/IF
            String tipoInstr = str(proc.get("TipoInstruccion"));
            if (("RF".equals(tipoInstr) || "IF".equals(tipoInstr))) {
                String fv = str(proc.get("FechaVencimiento"));
                if (fv == null || fv.startsWith("0001")) {
                    mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE",
                        "FechaVencimiento no informada para instruccion RF/IF"));
                    proc.put("IdEstadoProceso", "ERR");
                }
            }
            // ValorRescate required for IF
            if ("IF".equals(tipoInstr) && toBigDecimal(proc.get("ValorRescate")).compareTo(BigDecimal.ZERO) == 0) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO",
                    "ValorRescate no informado para IF"));
                proc.put("IdEstadoProceso", "ERR");
            }
            // Cantidad required for RV
            if ("RV".equals(tipoInstr) && toBigDecimal(proc.get("Cantidad")).compareTo(BigDecimal.ZERO) == 0) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO",
                    "Cantidad no informada para RV"));
                proc.put("IdEstadoProceso", "ERR");
            }
            // ValorNominal required for RF
            if ("RF".equals(tipoInstr) && toBigDecimal(proc.get("ValorNominal")).compareTo(BigDecimal.ZERO) == 0) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO",
                    "ValorNominal no informado para RF"));
                proc.put("IdEstadoProceso", "ERR");
            }
            // FechaTrade must not be after fechaSistema
            String fechaTrade = str(proc.get("FechaTrade"));
            if (fechaSistema != null && !fechaSistema.isBlank() && fechaMayor(fechaSistema, fechaTrade)) {
                mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA",
                    "FechaTrade " + fechaTrade + " > FechaSistema " + fechaSistema));
                proc.put("IdEstadoProceso", "ERR");
            }
            // MultiploCorte for non-RV
            if (!"RV".equals(tipoInstr)) {
                BigDecimal corteMinimo = toBigDecimal(proc.get("CorteMinimo"));
                if (corteMinimo.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal valor = "RF".equals(tipoInstr)
                        ? toBigDecimal(proc.get("ValorNominal"))
                        : toBigDecimal(proc.get("ValorRescate"));
                    if (valor.remainder(corteMinimo).compareTo(BigDecimal.ZERO) != 0) {
                        mensajes.add(error(idInterno, "UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE",
                            "Valor no es múltiplo del corte mínimo " + corteMinimo));
                        proc.put("IdEstadoProceso", "ERR");
                    }
                }
            }
            // CorteMinimo must be > 0 for IF(N) and RF
            if (("IF".equals(tipoInstr) && "N".equals(str(proc.get("EsFungible"))) && toBigDecimal(proc.get("CorteMinimo")).compareTo(BigDecimal.ZERO) == 0)
                || ("RF".equals(tipoInstr) && toBigDecimal(proc.get("CorteMinimo")).compareTo(BigDecimal.ZERO) == 0)) {
                mensajes.add(error(idInterno, "CORTE_MINIMO_INVALIDO", "CorteMinimo inválido (0)"));
                proc.put("IdEstadoProceso", "ERR");
            }
        }

        erroresContador = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo").getAsString())).count();

        // ── Step 6: call CrearInstrucciones for valid ones ────────────────────
        List<Map<String, Object>> validadas = instruccionesProceso.stream()
            .filter(p -> "PEN".equals(str(p.get("IdEstadoProceso"))))
            .collect(Collectors.toList());

        if (!validadas.isEmpty()) {
            try {
                JsonObject crearParams = new JsonObject();
                crearParams.addProperty("p_IdEmpresa", idEmpresa);
                crearParams.addProperty("p_IdUsuario", idUsuario);
                crearParams.addProperty("p_IdModulo", idModulo);
                crearParams.addProperty("p_UsaReglasNegocio", usaReglasNegocio);
                crearParams.addProperty("p_IdTipoCanalIngreso", canal);

                JsonArray instrArr = new JsonArray();
                for (Map<String, Object> proc : validadas) {
                    JsonObject instrJson = new JsonObject();
                    for (Map.Entry<String, Object> entry : proc.entrySet()) {
                        Object v = entry.getValue();
                        if (v == null) instrJson.addProperty(entry.getKey(), (String) null);
                        else if (v instanceof Long)       instrJson.addProperty(entry.getKey(), (Long) v);
                        else if (v instanceof BigDecimal) instrJson.addProperty(entry.getKey(), (BigDecimal) v);
                        else if (v instanceof Boolean)    instrJson.addProperty(entry.getKey(), (Boolean) v);
                        else                              instrJson.addProperty(entry.getKey(), v.toString());
                    }
                    instrArr.add(instrJson);
                }
                crearParams.add("p_Instrucciones", instrArr);

                JsonObject crearResult = crearInstruccionesService.execute(crearParams);
                long crearErrores = crearResult.has("P_OUT_ERRORESCONTADOR")
                    ? crearResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                erroresContador += crearErrores;

                if (crearResult.has("P_OUT_MENSAJES") && crearResult.get("P_OUT_MENSAJES").isJsonArray())
                    for (JsonElement el : crearResult.get("P_OUT_MENSAJES").getAsJsonArray())
                        mensajes.add(el.getAsJsonObject());

                if (crearResult.has("P_OUT_REFID") && crearResult.get("P_OUT_REFID").isJsonArray())
                    refIds = crearResult.get("P_OUT_REFID").getAsJsonArray();

            } catch (Exception e) {
                log.error("CrearInstrucciones failed: {}", e.getMessage(), e);
                erroresContador++;
                mensajes.add(error(null, "ERROR_CREAR_INSTRUCCIONES", "Error al crear instrucciones: " + e.getMessage()));
            }
        }

        JsonArray mensajesArr = new JsonArray();
        mensajes.forEach(mensajesArr::add);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", erroresContador);
        out.add("P_OUT_MENSAJES", mensajesArr);
        out.add("P_OUT_REFID", refIds);
        return out;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JsonObject error(Long idInterno, String codigo, String descripcion) {
        JsonObject m = new JsonObject();
        m.addProperty("tipo", "ERROR");
        if (idInterno != null) m.addProperty("id", idInterno);
        m.addProperty("codigo", codigo);
        m.addProperty("descripcion", descripcion);
        return m;
    }

    private static String obtenerIdTipoInstrumentoInstruccion(String tipo) {
        return switch (tipo == null ? "" : tipo) {
            case "RV" -> "IRV";
            case "RF" -> "IRF";
            case "IF" -> "IFI";
            default   -> null;
        };
    }

    private static String obtenerIdTipoOperacionInstruccion(String operacion, String idTipoInstr) {
        if (idTipoInstr == null || operacion == null) return null;
        return switch (operacion) {
            case "CO" -> "COM" + idTipoInstr;
            case "VE" -> "VEN" + idTipoInstr;
            case "EN" -> "ENT" + idTipoInstr;
            case "RE" -> "REC" + idTipoInstr;
            default   -> null;
        };
    }

    private static String obtenerCodigoMotivoInstruccion(String canal) {
        return switch (canal == null ? "" : canal) {
            case "CLI" -> "CMCLI";
            case "AGE" -> "CMCUS";
            default    -> null;
        };
    }

    private static boolean fechaMayor(String fechaMenor, String fechaMayor) {
        if (fechaMenor == null || fechaMayor == null) return false;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            return fmt.parse(fechaMenor).before(fmt.parse(fechaMayor));
        } catch (Exception e) { return false; }
    }

    private static Map<String, Object> jsonToMap(JsonObject jo) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
            JsonElement v = e.getValue();
            if (v.isJsonNull()) m.put(e.getKey(), null);
            else if (v.isJsonPrimitive()) {
                var prim = v.getAsJsonPrimitive();
                if (prim.isNumber()) {
                    try { m.put(e.getKey(), prim.getAsLong()); }
                    catch (Exception ex) { m.put(e.getKey(), prim.getAsBigDecimal()); }
                } else m.put(e.getKey(), prim.getAsString());
            } else m.put(e.getKey(), v.toString());
        }
        return m;
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long) return (Long) v;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String loadSql(String path) {
        try (var is = ProcesarCargaMasivaInstruccionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
