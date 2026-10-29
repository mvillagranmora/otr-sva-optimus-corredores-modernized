package cl.bcs.ordenes.service;

import cl.bcs.ordenes.service.CrearTransaccionesHelper.*;
import cl.bcs.ordenes.util.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import static cl.bcs.ordenes.service.CrearTransaccionesHelper.*;

@Service
@Transactional
public class CrearTransaccionesService {

    private static final Logger log = LoggerFactory.getLogger(CrearTransaccionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();

    // ── SQL resources ─────────────────────────────────────────────────────────────
    private final String SQL_SELECT_ESTADO_MODULO;
    private final String SQL_SELECT_LIBRO_BY_OPERADOR;
    private final String SQL_SELECT_LIBRO_BY_DEFAULT;
    private final String SQL_INSERT_WRAPPER;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_FOLIOS;
    private final String SQL_SELECT_CONFIG_OPERADOR;
    private final String SQL_INSERT_TRANSACCIONES;
    private final String SQL_INSERT_CONTROL_MERCADO;
    private final String SQL_SELECT_VALIDA_MONEDA_RF;
    private final String SQL_SELECT_CONFIG_ASIGNACION;
    private final String SQL_SELECT_RUTS_MARCA;
    private final String SQL_SELECT_CONFIG_SHORTNAME;
    private final String SQL_UPDATE_LIBRO_ORDEN;
    private final String SQL_CREATE_TEMP;
    private final String SQL_DROP_TEMP;
    private final String SQL_INSERT_TEMP;
    private static final String SQL_EMPRESA =
        "SELECT \"IdEmpresa\",\"IdCorredora\",\"IdPais\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private static final String SQL_ZONA_HORARIA =
        "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
        "WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoParametro\" = 'PLA.ZonaHorariaSegundos'";

    // ── Dependencies ──────────────────────────────────────────────────────────────
    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ObtenerPlazoUtil plazoUtil;
    private final PlataformaCallClient plataformaCallClient;
    private final CrearTransaccionesHistoricasService historicasService;

    public CrearTransaccionesService(
            NamedParameterJdbcTemplate jdbc,
            ConfiguracionSistemaUtil configUtil,
            RequisitosBasicosUtil requisitosUtil,
            ObtenerDiaSiguienteUtil diaSiguienteUtil,
            ObtenerPlazoUtil plazoUtil,
            PlataformaCallClient plataformaCallClient,
            CrearTransaccionesHistoricasService historicasService) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.requisitosUtil = requisitosUtil;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.plazoUtil = plazoUtil;
        this.plataformaCallClient = plataformaCallClient;
        this.historicasService = historicasService;

        this.SQL_SELECT_ESTADO_MODULO     = sql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_SELECT_LIBRO_BY_OPERADOR = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByOperador.sql");
        this.SQL_SELECT_LIBRO_BY_DEFAULT  = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectLibroOrdenesByDefault.sql");
        this.SQL_INSERT_WRAPPER           = sql("cl/bcs/ordenes/sql/CrearTransacciones_InsertTransaccionesWrapper.sql");
        this.SQL_SELECT_TRANSACCIONES     = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectTransacciones.sql");
        this.SQL_SELECT_FOLIOS            = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectFolios.sql");
        this.SQL_SELECT_CONFIG_OPERADOR   = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionOperador.sql");
        this.SQL_INSERT_TRANSACCIONES     = sql("cl/bcs/ordenes/sql/CrearTransacciones_InsertTransacciones.sql");
        this.SQL_INSERT_CONTROL_MERCADO   = sql("cl/bcs/ordenes/sql/CrearTransacciones_InsertControlMercado.sql");
        this.SQL_SELECT_VALIDA_MONEDA_RF  = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectValidaMonedaInstrumentoRF.sql");
        this.SQL_SELECT_CONFIG_ASIGNACION = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionAsignacionDirecta.sql");
        this.SQL_SELECT_RUTS_MARCA        = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectRutsMarcaNegociacion.sql");
        this.SQL_SELECT_CONFIG_SHORTNAME  = sql("cl/bcs/ordenes/sql/CrearTransacciones_SelectConfiguracionShortName.sql");
        this.SQL_UPDATE_LIBRO_ORDEN       = sql("cl/bcs/ordenes/sql/CrearTransacciones_UpdateLibroOrdenes.sql");
        this.SQL_CREATE_TEMP              = sql("cl/bcs/ordenes/sql/CrearTransacciones_CreateTablaTemporal.sql");
        this.SQL_DROP_TEMP                = sql("cl/bcs/ordenes/sql/CrearTransacciones_DropTablaTemporal.sql");
        this.SQL_INSERT_TEMP              = sql("cl/bcs/ordenes/sql/CrearTransacciones_InsertTablaTemporal.sql");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MAIN EXECUTE
    // ─────────────────────────────────────────────────────────────────────────────

    public JsonObject execute(JsonObject params) {
        log.info("CrearTransaccionesService.execute() START");
        Entrada in = GSON.fromJson(params, Entrada.class);
        if (in.p_Reproceso == null) in.p_Reproceso = "N";
        Salida salida = new Salida();

        // 1. Requisitos básicos
        RequisitosBasicosUtil.Salida req = requisitosUtil.validar(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        salida.P_OUT_ERRORESCONTADOR += req.contarErrores();
        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            req.p_out_Mensajes.forEach(m -> salida.P_OUT_MENSAJES.add(new MensajeSalida(m.IdInterno, m.tipo, m.codigo)));
            return toJson(salida);
        }

        // 2. Transacciones no vacías
        if (in.p_Transacciones == null || in.p_Transacciones.isEmpty()) {
            salida.P_OUT_ERRORESCONTADOR++;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, TIPO_ERROR, "NO_SE_INGRESARON_TRANSACCIONES"));
            return toJson(salida);
        }

        // 3. Cargar config de empresa
        String localizacion   = configUtil.getParametroString(in.p_IdEmpresa, "PLA.LocalizacionSistema");
        int plazoMinSim       = parseIntSafe(configUtil.getParametroString(in.p_IdEmpresa, "PlazoMenorSimultanea"), 0);
        int plazoMaxSim       = parseIntSafe(configUtil.getParametroString(in.p_IdEmpresa, "PlazoMayorSimultanea"), 3650);
        int baseTasaRvBcs     = parseIntSafe(configUtil.getParametroString(in.p_IdEmpresa, "BaseTasaSimultaneaRvBcs"), 30);
        int baseTasaRvBec     = parseIntSafe(configUtil.getParametroString(in.p_IdEmpresa, "BaseTasaSimultaneaRvBec"), 30);
        String fechaSistema   = configUtil.getParametroString(in.p_IdEmpresa, "PLA.FechaSistema");
        String monedaSim      = configUtil.getParametroString(in.p_IdEmpresa, "MonedaSimultanea");
        String palosReservado = configUtil.getParametroString(in.p_IdEmpresa, "PALOS_NEGOCIACION_RESERVADO");

        // 4. Validar moneda en operaciones VCRF
        boolean tieneVCRF = in.p_Transacciones.stream().anyMatch(t -> TIPO_OPERACION_VCRF.equals(t.IdTipoOperacion));
        if (tieneVCRF) {
            List<Long> idsInstrVCRF = in.p_Transacciones.stream()
                .filter(t -> TIPO_OPERACION_VCRF.equals(t.IdTipoOperacion))
                .map(t -> t.IdInstrumento).collect(Collectors.toList());
            List<Map<String,Object>> validMoneda = jdbc.queryForList(SQL_SELECT_VALIDA_MONEDA_RF,
                new MapSqlParameterSource("p_IdInstrumento", idsInstrVCRF));
            Map<Long, String> monedaValida = new HashMap<>();
            for (Map<String,Object> r : validMoneda)
                monedaValida.put(toLong(r.get("IdInstrumento")), safeStr((String)r.get("MonedaEmisionValida")));
            for (Transaccion_Entrada t : in.p_Transacciones) {
                if (TIPO_OPERACION_VCRF.equals(t.IdTipoOperacion) && NO.equals(monedaValida.get(t.IdInstrumento))) {
                    salida.P_OUT_ERRORESCONTADOR++;
                    salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, TIPO_ERROR, "UI_INSTRUMENTO_SIN_MONEDA_EMISION"));
                    return toJson(salida);
                }
            }
        }

        // 5. Asignar libro ordenes si no viene
        List<LibrosOrdenes> librosDefault = null, librosPorOperador = null;
        for (Transaccion_Entrada t : in.p_Transacciones) {
            if (t.IdLibroOrdenes == null) {
                if (librosDefault == null) {
                    librosDefault = queryList(SQL_SELECT_LIBRO_BY_DEFAULT,
                        new MapSqlParameterSource("p_IdEmpresa", in.p_IdEmpresa), LibrosOrdenes.class);
                    librosPorOperador = queryList(SQL_SELECT_LIBRO_BY_OPERADOR,
                        new MapSqlParameterSource("p_IdEmpresa", in.p_IdEmpresa), LibrosOrdenes.class);
                }
                t.IdLibroOrdenes = resolverLibroOrdenes(t.CodigoOperadorCorredor, librosDefault, librosPorOperador);
            }
        }

        // 6. Validar correlativos únicos
        Set<Long> correlativos = new HashSet<>();
        for (Transaccion_Entrada t : in.p_Transacciones) {
            if (!correlativos.add(t.IdInterno)) {
                salida.P_OUT_ERRORESCONTADOR++;
                salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, TIPO_ERROR, "CORRELATIVO_INCORRECTO"));
                return toJson(salida);
            }
        }

        // 7. Validar estado módulo
        List<Map<String,Object>> modulos = jdbc.queryForList(SQL_SELECT_ESTADO_MODULO,
            new MapSqlParameterSource("IdModulo", ID_MODULO_ORDENES).addValue("IdEmpresa", in.p_IdEmpresa));
        if (modulos.isEmpty()) {
            salida.P_OUT_ERRORESCONTADOR++;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, TIPO_ERROR, "MODULO_NO_EXISTE"));
            return toJson(salida);
        }
        if (ESTADO_MODULO_CERRADO.equalsIgnoreCase(safeStr((String)modulos.get(0).get("IdEstadoModulo")))) {
            salida.P_OUT_ERRORESCONTADOR++;
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, TIPO_ERROR, "MODULO_CERRADO"));
            return toJson(salida);
        }

        // 8. Consultar empresa
        Empresa empresa = consultarEmpresa(in.p_IdEmpresa);

        // 9. Insertar wrapper y leer info transacciones
        batchInsertWrapper(in.p_Transacciones);
        List<Map<String,Object>> infoRows = jdbc.queryForList(SQL_SELECT_TRANSACCIONES, Map.of());
        Map<Long, InformacionTransaccion> infoMap = new HashMap<>();
        for (Map<String,Object> r : infoRows) {
            InformacionTransaccion it = rowToInfoTransaccion(r);
            infoMap.put(it.IdInterno, it);
        }

        // 10. Preparar info operadores y shortnames y asignacion
        List<String> codOps = new ArrayList<>();
        for (Transaccion_Entrada t : in.p_Transacciones) {
            if (t.CodigoOperadorCorredor != null && !codOps.contains(t.CodigoOperadorCorredor))
                codOps.add(t.CodigoOperadorCorredor);
            if (t.CodigoOperadorContraparte != null && t.IndicadorOrdenDirecta != null
                    && !t.IndicadorOrdenDirecta.isEmpty() && !codOps.contains(t.CodigoOperadorContraparte))
                codOps.add(t.CodigoOperadorContraparte);
        }
        Map<String, InformacionOperador> infoOps = new HashMap<>();
        if (!codOps.isEmpty()) {
            for (Map<String,Object> r : jdbc.queryForList(SQL_SELECT_CONFIG_OPERADOR,
                    new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa).addValue("codigosOperador", codOps))) {
                InformacionOperador io = new InformacionOperador();
                io.CodigoOperador    = safeStr((String)r.get("CodigoOperador"));
                io.IdTipoInstrumento = safeStr((String)r.get("IdTipoInstrumento"));
                io.ReservaTransaccion= safeStr((String)r.get("ReservaTransaccion"));
                io.AsignacionAuto    = safeStr((String)r.get("AsignacionAuto"));
                io.IdPortafolio      = toLong(r.get("IdPortafolio"));
                io.UtilizaPortafolio = safeStr((String)r.get("UtilizaPortafolio"));
                infoOps.put(claveMapa(io.CodigoOperador, io.IdTipoInstrumento), io);
            }
        }
        Map<String, ConfiguracionShortName> shortNames = new HashMap<>();
        for (Map<String,Object> r : jdbc.queryForList(SQL_SELECT_CONFIG_SHORTNAME, Map.of())) {
            ConfiguracionShortName sn = new ConfiguracionShortName();
            sn.ShortName          = safeStr((String)r.get("ShortName"));
            sn.IdTipoInstrumento  = safeStr((String)r.get("IdTipoInstrumento"));
            sn.AsignacionAuto     = safeStr((String)r.get("AsignacionAuto"));
            sn.IdLibroOrdenes     = toLong(r.get("IdLibroOrdenes"));
            shortNames.put(claveMapa(sn.ShortName, sn.IdTipoInstrumento), sn);
        }
        Map<String, ConfiguracionAsignacion> configAsignacion = new HashMap<>();
        for (Map<String,Object> r : jdbc.queryForList(SQL_SELECT_CONFIG_ASIGNACION,
                new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa))) {
            ConfiguracionAsignacion ca = new ConfiguracionAsignacion();
            ca.IdTipoInstrumento = safeStr((String)r.get("IdTipoInstrumento"));
            ca.AsignacionAuto    = safeStr((String)r.get("AsignacionAuto"));
            configAsignacion.put(claveMapa(ca.IdTipoInstrumento), ca);
        }

        // Ruts marca negociacion
        List<String> rutsList = new ArrayList<>();
        for (Transaccion_Entrada t : in.p_Transacciones) {
            if (t.MarcaNegociacion != null) { String[] p = t.MarcaNegociacion.split(";"); if (p.length > 0 && !p[0].isEmpty()) rutsList.add(p[0].trim().toUpperCase()); }
            if (t.MarcaNegociacionContraparte != null && !t.MarcaNegociacionContraparte.equalsIgnoreCase(t.MarcaNegociacion)) {
                String[] p = t.MarcaNegociacionContraparte.split(";"); if (p.length > 0 && !p[0].isEmpty()) rutsList.add(p[0].trim().toUpperCase());
            }
        }
        rutsList = rutsList.stream().distinct().collect(Collectors.toList());
        Map<String, RutPortafolio> rutsPrelacion = new HashMap<>();
        if (!rutsList.isEmpty()) {
            for (Map<String,Object> r : jdbc.queryForList(SQL_SELECT_RUTS_MARCA, new MapSqlParameterSource("Rut", rutsList))) {
                RutPortafolio rp = new RutPortafolio();
                rp.DocumentoIdentidad = safeStr((String)r.get("DocumentoIdentidad"));
                rp.CodigoPortafolio   = safeStr((String)r.get("CodigoPortafolio"));
                rp.IdLibroOrdenes     = toLong(r.get("IdLibroOrdenes"));
                rp.NombreEstado       = safeStr((String)r.get("NombreEstado"));
                rutsPrelacion.put(claveMapa(rp.DocumentoIdentidad, rp.CodigoPortafolio), rp);
            }
        }

        // 11. Construir Transaccion_Proceso list
        Pattern patRut  = Pattern.compile("\\d+-(?:[0-9]|K|k)");
        Pattern patRut2 = Pattern.compile("\\d+-(?:[0-9]|K|k);[a-zA-Z0-9''\\-|]+");
        ArrayList<Transaccion_Proceso> transacciones = new ArrayList<>();
        ArrayList<Transaccion_Proceso> transaccionesError = new ArrayList<>();

        for (Transaccion_Entrada tranE : in.p_Transacciones) {
            InformacionTransaccion info = infoMap.get(tranE.IdInterno);
            if (info == null) { log.warn("No info found for IdInterno={}", tranE.IdInterno); continue; }
            Transaccion_Proceso tranP = buildTransaccionProceso(tranE, info, empresa, monedaSim,
                baseTasaRvBcs, baseTasaRvBec, palosReservado, in.p_IdUsuario, fechaSistema);
            transacciones.add(tranP);
        }

        limpiarTransacciones(transacciones, transaccionesError);
        if (transacciones.isEmpty()) {
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return toJson(salida);
        }

        // 12. Fecha liquidación contado
        {
            List<ObtenerDiaSiguienteUtil.FechaIn> fechas = transacciones.stream().map(t -> {
                ObtenerDiaSiguienteUtil.FechaIn f = new ObtenerDiaSiguienteUtil.FechaIn();
                f.fechaInicial = toStringDate(t.FechaSistemaCreacion);
                f.numeroDias   = t.DiasLiquidacionContado == null ? 0L : t.DiasLiquidacionContado;
                f.IdInterno    = t.IdInterno;
                f.pais         = empresa.IdPais;
                return f;
            }).collect(Collectors.toList());
            ObtenerDiaSiguienteUtil.Salida out = diaSiguienteUtil.calcular(empresa.IdPais, "S", "N", fechas);
            for (ObtenerDiaSiguienteUtil.FechaOut fo : out.p_out_fechas) {
                for (Transaccion_Proceso t : transacciones) {
                    if (t.IdInterno.equals(fo.IdInterno))
                        t.FechaLiquidacion = toDate(fo.fechaCalculada);
                }
            }
        }

        // 13. Plazo para simultáneas por ruteo
        {
            List<ObtenerPlazoUtil.FechasIn> fps = new ArrayList<>();
            for (Transaccion_Proceso t : transacciones) {
                if ((eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE) || eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM))
                        && !"0001-01-01".equals(toStringDate(t.FechaVencimiento)) && t.Plazo != null && t.Plazo == -1L) {
                    ObtenerPlazoUtil.FechasIn f = new ObtenerPlazoUtil.FechasIn();
                    f.FechaDesde = toStringDate(t.FechaLiquidacion);
                    f.FechaHasta = toStringDate(t.FechaVencimiento);
                    f.IdInterno  = t.IdInterno;
                    f.SoloDiasHabiles = "N";
                    fps.add(f);
                }
            }
            if (!fps.isEmpty()) {
                ObtenerPlazoUtil.Salida sp = plazoUtil.calcular(empresa.IdPais, fps);
                for (ObtenerPlazoUtil.FechasOut fo : sp.p_out_Fechas) {
                    for (Transaccion_Proceso t : transacciones) {
                        if (t.IdInterno.equals(fo.IdInterno)) t.Plazo = fo.Plazo;
                    }
                }
            }
        }

        // 14. Fecha vencimiento para simultáneas/IF/RE sin vencimiento
        {
            List<ObtenerDiaSiguienteUtil.FechaIn> fechas = new ArrayList<>();
            for (Transaccion_Proceso t : transacciones) {
                if ((eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM)
                        || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_IF)
                        || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE))
                        && "0001-01-01".equals(toStringDate(t.FechaVencimiento))) {
                    ObtenerDiaSiguienteUtil.FechaIn f = new ObtenerDiaSiguienteUtil.FechaIn();
                    f.fechaInicial = toStringDate(t.FechaLiquidacion);
                    f.numeroDias   = t.Plazo == null ? 0L : t.Plazo;
                    f.IdInterno    = t.IdInterno;
                    f.pais         = empresa.IdPais;
                    fechas.add(f);
                }
            }
            if (!fechas.isEmpty()) {
                ObtenerDiaSiguienteUtil.Salida out = diaSiguienteUtil.calcular(empresa.IdPais, "N", "N", fechas);
                for (ObtenerDiaSiguienteUtil.FechaOut fo : out.p_out_fechas) {
                    for (Transaccion_Proceso t : transacciones) {
                        if (t.IdInterno.equals(fo.IdInterno))
                            t.FechaVencimiento = toDate(fo.fechaCalculada);
                    }
                }
            }
        }

        // 15. Consultar nemo negociación + CalcularParidad (RV only)
        procesarRV(transacciones, fechaSistema, in.p_IdEmpresa);

        // 16. Validación folio repetido (temp table)
        List<FolioExistente> foliosExistentes = new ArrayList<>();
        {
            List<String> foliosFechas = new ArrayList<>();
            for (Transaccion_Proceso t : transacciones) {
                String ff = t.Folio + " " + toStringDate(t.FechaSistemaCreacion);
                if (!foliosFechas.contains(ff)) foliosFechas.add(ff);
                if ((eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM) || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE))
                        && t.FolioTransaccionPlazo != null && !foliosFechas.contains(t.FolioTransaccionPlazo + " " + toStringDate(t.FechaSistemaCreacion)))
                    foliosFechas.add(t.FolioTransaccionPlazo + " " + toStringDate(t.FechaSistemaCreacion));
            }
            try {
                jdbc.getJdbcTemplate().execute(SQL_CREATE_TEMP);
                if (!foliosFechas.isEmpty()) {
                    SqlParameterSource[] ps = foliosFechas.stream()
                        .map(ff -> new MapSqlParameterSource("FolioFecha", ff))
                        .toArray(SqlParameterSource[]::new);
                    jdbc.batchUpdate(SQL_INSERT_TEMP, ps);
                }
                List<Map<String,Object>> feRows = jdbc.queryForList(SQL_SELECT_FOLIOS,
                    new MapSqlParameterSource("idEmpresa", empresa.IdEmpresa));
                for (Map<String,Object> r : feRows) {
                    FolioExistente fe = new FolioExistente(toLong(r.get("IdTransaccion")), safeStr((String)r.get("FolioFecha")));
                    foliosExistentes.add(fe);
                }
            } finally {
                try { jdbc.getJdbcTemplate().execute(SQL_DROP_TEMP); } catch (Exception ex) { log.warn("DROP temp: {}", ex.getMessage()); }
            }
        }

        // 17. Validaciones por transacción
        for (Transaccion_Proceso t : transacciones) {
            procesaRequisito(t.Folio == null || t.Folio.isEmpty(), "FOLIO_TRANSACCION_VACIO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            procesaRequisito(foliosExistentes.contains(new FolioExistente(t.IdTransaccion, t.Folio + " " + toStringDate(t.FechaSistemaCreacion))), "FOLIO_TRANSACCION_NO_DISPONIBLE", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_DV))
                procesaRequisito(t.Precio == null || t.Precio.compareTo(BigDecimal.ZERO) <= 0, "PRECIO_MENOR_CERO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_IF))
                procesaRequisito(t.IdMonedaEmision == null, "MONEDA_EMISION_OBLIGATORIA", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if (eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM)) {
                procesaRequisito(foliosExistentes.contains(new FolioExistente(t.IdTransaccion, t.FolioTransaccionPlazo + " " + toStringDate(t.FechaSistemaCreacion))), "FOLIO_TRANSACCION_PLAZO_NO_DISPONIBLE", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
                procesaRequisito(t.Folio != null && t.Folio.equalsIgnoreCase(t.FolioTransaccionPlazo), "FOLIO_TRANSACCION_IGUAL_FOLIO_TRANSACCION_PLAZO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
                procesaRequisito(t.Plazo == null || t.Plazo < plazoMinSim || t.Plazo > plazoMaxSim, "PLAZO_INVALIDO_NO_SE_ENCUENTRA_DENTRO_DEL_RANGO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            }
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV))
                procesaRequisito(t.PrecioRaiz == null || t.PrecioRaiz.compareTo(BigDecimal.ZERO) == 0, "UI_INSTRUMENTO_NEGOCIADO_NO_EXISTE", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if ("MANUAL".equalsIgnoreCase(t.IdSistemaBursatil) && NO.equals(t.EsBilateral) && "CL".equalsIgnoreCase(localizacion)) {
                procesaRequisito(SI.equals(t.BolsaEsExtranjera), "TRANSACCION_BOLSA_EXTRANJERA_DEBE_SER_BILATERAL", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
                procesaRequisito(t.IdMonedaInstrumento != null && !"CLP".equalsIgnoreCase(t.IdMonedaInstrumento), "TRANSACCION_MONEDA_EXTRANJERA_DEBE_SER_BILATERAL", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            }
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) {
                procesaRequisito(t.PrecioPlazo == null || t.PrecioPlazo.compareTo(BigDecimal.ZERO) <= 0, "PRECIO_PLAZO_MENOR_CERO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
                procesaRequisito(t.Plazo == null || t.Plazo <= 0, "PLAZO_INVALIDO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            }
            procesaRequisito(t.Cantidad == null || t.Cantidad.compareTo(BigDecimal.ZERO) <= 0, "CANTIDAD_MENOR_CERO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RF) && t.CorteMinimo != null && t.CorteMinimo.compareTo(BigDecimal.ZERO) > 0)
                procesaRequisito(t.Cantidad.remainder(t.CorteMinimo).compareTo(BigDecimal.ZERO) != 0, "CORTE_MINIMO_INVALIDO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            if (SI.equalsIgnoreCase(t.EsFungible) && eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_IF))
                procesaRequisito(t.ValorRescate == null || t.ValorRescate.compareTo(BigDecimal.ZERO) <= 0, "VALOR_RESCATE_MENOR_CERO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            procesaRequisito(SI.equalsIgnoreCase(t.EsVentaCorta) && NO.equalsIgnoreCase(t.InstrumentoPermiteVentaCorta), "INSTRUMENTO_NO_PERMITE_VENTA_CORTA", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
            procesaRequisito(ESTADO_LIBRO_CERRADO.equalsIgnoreCase(t.IdEstadoLibroOrdenes), "LIBRO_ORDENES_CERRADO", TIPO_ERROR, t, salida.P_OUT_MENSAJES);
        }

        // 18. Guardar histórico para modificaciones con errores
        limpiarTransacciones(transacciones, transaccionesError);
        List<Transaccion_Proceso> mdErrors = transaccionesError.stream()
            .filter(t -> SI.equals(t.EsModificacionMarketData)).collect(Collectors.toList());
        if (!mdErrors.isEmpty()) {
            List<Long> ids18 = getSecuencia(contarIdsAdicionales(new ArrayList<>(mdErrors)));
            int p18 = procesarOrdenDirecta(new ArrayList<>(mdErrors), ids18);
            try { historicasService.execute(buildHistoricasParams(in, new ArrayList<>(mdErrors))); }
            catch (Exception ex) { log.warn("historicasService error (mdErrors): {}", ex.getMessage()); }
        }

        if (transacciones.isEmpty()) {
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return toJson(salida);
        }

        // 19. Obtener NemoSVS para IF
        procesarNemoSVS(transacciones);

        // 20. Obtener IDs adicionales (OD, SIM, RE plazo)
        List<Long> idsTransaccion = getSecuencia(contarIdsAdicionales(transacciones));

        // 21. Procesar OD
        int pos = procesarOrdenDirecta(transacciones, idsTransaccion);

        // 22. Transacciones plazo (SIM / RE)
        pos = procesarTransaccionesPlazo(transacciones, idsTransaccion, pos);

        // 23. Corrección OD de simultáneas plazo
        for (Transaccion_Proceso tranP : transacciones) {
            if (SI.equalsIgnoreCase(tranP.EsAPlazo)) {
                for (Transaccion_Proceso other : transacciones) {
                    if (tranP.IdTransaccionOd.equals(other.IdTransaccion)) {
                        tranP.IdTransaccionOd = other.IdTransaccionPlazo;
                        break;
                    }
                }
            }
        }

        // 24. Prelación
        List<Transaccion_Proceso> preladas = procesarPrelacion(in, transacciones, infoMap,
            infoOps, shortNames, rutsPrelacion, configAsignacion, palosReservado, fechaSistema);

        // 25. Construir lista final y agregar log (previo a insert)
        ArrayList<Map<String,Object>> transaccionesFinales = buildTransaccionesFinales(transacciones);
        agregarLog(in, transaccionesFinales, fechaSistema, "TRANSACCION_INGRESADA");

        // 26. Si no es simulación, insertar
        if (NO.equalsIgnoreCase(in.p_simularOperacion) && !transaccionesFinales.isEmpty()) {
            SqlParameterSource[] insPs = transaccionesFinales.stream()
                .map(m -> new MapSqlParameterSource(m)).toArray(SqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_INSERT_TRANSACCIONES, insPs);

            agregarLogPost(in, transacciones, fechaSistema);

            if (!preladas.isEmpty()) {
                SqlParameterSource[] prelPs = preladas.stream()
                    .map(t -> new MapSqlParameterSource()
                        .addValue("TipoMensaje", t.TipoMensaje)
                        .addValue("FechaSistemaCreacion", t.FechaSistemaCreacion)
                        .addValue("IdTransaccion", t.IdTransaccion))
                    .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT_CONTROL_MERCADO, prelPs);
            }

            try { historicasService.execute(buildHistoricasParams(in, transacciones)); }
            catch (Exception ex) { log.warn("historicasService error: {}", ex.getMessage()); }
        }

        // 27. Construir salida
        for (Transaccion_Proceso t : transacciones) {
            salida.P_OUT_MENSAJES.add(new MensajeSalida(t.IdInterno, TIPO_INFO, "TRANSACCION_INGRESADA",
                "TRANSACCION_INGRESADA IdTransaccion=" + t.IdTransaccion));
            ResultadoSalida rs = new ResultadoSalida();
            rs.IdInterno = t.IdInterno; rs.IdTransaccion = t.IdTransaccion;
            rs.IdTipoOperacion = t.IdTipoOperacion; rs.EsAPlazo = t.EsAPlazo;
            salida.P_OUT_RESULTADO.add(rs);
        }
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        log.info("CrearTransaccionesService.execute() END errores={}", salida.P_OUT_ERRORESCONTADOR);
        return toJson(salida);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────────

    private Transaccion_Proceso buildTransaccionProceso(Transaccion_Entrada tranE, InformacionTransaccion info,
            Empresa empresa, String monedaSim, int baseTasaRvBcs, int baseTasaRvBec,
            String palosReservado, Long idUsuario, String fechaSistema) {
        Transaccion_Proceso t = new Transaccion_Proceso();
        t.IdInterno    = tranE.IdInterno;
        t.IdTransaccion= info.IdTransaccion;
        t.Folio        = tranE.FolioTransaccion;
        t.FechaSistemaCreacion = toDate(tranE.FechaCreacion);
        t.HoraSistemaCreacion  = toTime(tranE.HoraCreacion);
        t.FechaOperacion       = fechaSistemaHoraUTC(tranE.FechaCreacion, t.HoraSistemaCreacion);
        t.FechaModificacionOperacion = toDateTime(tranE.FechaCreacion + " " + tranE.HoraCreacion);
        t.FechaIngresoOferta   = t.FechaOperacion;

        // Nemotecnico
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) && NEMOS_BOLSON.contains(info.NemotecnicoInstrumento))
            t.Nemotecnico = tranE.NemotecnicoEspecificoBolson;
        else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) && BONO_RECONOCIMIENTO.contains(info.NemotecnicoInstrumento))
            t.Nemotecnico = tranE.NemotecnicoEspecificoBolson;
        else
            t.Nemotecnico = info.NemotecnicoInstrumento;

        // Cámara
        t.IdCamara = resolverCamara(info, tranE);

        // Fecha emisión
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) && BONO_RECONOCIMIENTO.contains(info.NemotecnicoInstrumento)) {
            String nemoBase = info.NemotecnicoInstrumento;
            String codigoMes = tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length(), nemoBase.length()+1);
            String codigoAgno = tranE.NemotecnicoEspecificoBolson.substring(nemoBase.length()+2, nemoBase.length()+3);
            t.FechaEmision = toDate(CODIGOS_AGNO_BONO.getOrDefault(codigoAgno,"2000") + "-" + CODIGOS_MES_BONO.getOrDefault(codigoMes,"01") + "-01");
        } else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF))
            t.FechaEmision = toDate(tranE.FechaEmision);
        else
            t.FechaEmision = info.FechaEmisionInstrumento;

        // Fecha vencimiento
        t.FechaVencimiento = resolverFechaVencimiento(tranE, info);

        t.IdSistemaBursatil = tranE.IdSistemaBursatil;

        // Indicador OD
        if (tranE.IdCorredoraContraparte != null && tranE.IdCorredoraContraparte.equals(empresa.IdCorredora)) {
            if (OD.equalsIgnoreCase(tranE.IndicadorOrdenDirecta) || OE.equalsIgnoreCase(tranE.IndicadorOrdenDirecta))
                t.IndicadorOrdenDirecta = tranE.IndicadorOrdenDirecta;
            else {
                t.errores++;
                // message handled downstream
            }
        } else {
            t.IndicadorOrdenDirecta = "";
        }

        t.EsCuentaPropia  = tranE.EsCuentaPropia;
        t.EsFungible      = tranE.EsFungible;
        t.EsAPlazo        = NO;
        t.AccionOrden     = info.AccionOrden;
        t.EsVentaCortaOD  = safeStr(tranE.EsVentaCorta);
        t.EsVentaCorta    = eq(info.AccionOrden, ACCION_COMPRA) ? NO : safeStr(tranE.EsVentaCorta);
        t.CodigoOperadorContraparte = safeStr(tranE.CodigoOperadorContraparte);
        t.CodigoOperadorCorredor    = safeStr(tranE.CodigoOperadorCorredor);

        t.IdEmisor = eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF) ? tranE.IdEmisor : info.IdEmisorInstrumento;
        t.Cantidad   = tranE.Cantidad;
        t.Monto      = tranE.Monto;
        t.MontoPlazo = tranE.MontoPlazo;
        t.Precio     = tranE.Precio;
        t.PrecioPlazo= tranE.PrecioPlazo;
        t.Tasa       = tranE.Tasa;
        t.NemotecnicoSVS = safeStr(tranE.NemotecnicoSVS);
        t.ValorRescate = tranE.ValorRescate;
        t.IdEstado     = ESTADO_TRANSACCION_INGRESADA;
        t.IdTransaccionPlazo = 0L;
        t.IdTransaccionOd    = 0L;
        t.IdTipoInstrumento  = info.IdTipoInstrumento;
        t.IdClasificacionTipoInstrumento = info.IdClasificacionTipoInstrumento;
        t.IdEmpleado   = tranE.IdEmpleado;
        t.IdTipoOperacion = tranE.IdTipoOperacion;
        t.IdCondicionDeLiquidacion = tranE.IdCondicionDeLiquidacionContado;

        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF)) {
            t.IdTipoReajuste = info.IdTipoReajusteInstrumento; t.IdMonedaEmision = info.IdMonedaEmisionInstrumento;
        } else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RE) && eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_RRF)) {
            t.IdTipoReajuste = info.IdTipoReajusteInstrumento; t.IdMonedaEmision = info.IdMonedaEmisionInstrumento;
        } else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF)) {
            t.IdTipoReajuste = tranE.IdTipoReajuste; t.IdMonedaEmision = tranE.IdMonedaEmision;
        } else {
            t.IdTipoReajuste = ""; t.IdMonedaEmision = "";
        }

        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF)) t.IdMonedaInstrumento = tranE.IdMoneda;
        else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM)) t.IdMonedaInstrumento = monedaSim;
        else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) t.IdMonedaInstrumento = tranE.IdMoneda;
        else t.IdMonedaInstrumento = info.IdMonedaInstrumento;

        t.IdCorredoraContraparte = tranE.IdCorredoraContraparte;
        t.IdCorredoraLocal  = empresa.IdCorredora;
        t.IdEmpresa         = empresa.IdEmpresa;
        t.IdBolsa           = tranE.IdBolsa;
        t.IdBoveda          = tranE.IdBoveda;
        t.IdPaisBolsa       = info.IdPaisBolsa;
        t.BolsaEsExtranjera = safeStr(info.BolsaEsExtranjera);
        t.IdLibroOrdenes    = tranE.IdLibroOrdenes;
        t.IdInstrumento     = tranE.IdInstrumento;
        t.NemotecnicoSubyacente   = safeStr(info.NemotecnicoInstrumentoSubyacente);
        t.IdInstrumentoSubyacente = info.IdInstrumentoSubyacente;
        t.IdMonedaSubyacente      = safeStr(info.IdMonedaInstrumentoSubyacente);
        t.DiasLiquidacionContado  = info.DiasLiquidacionContado == null ? 0L : info.DiasLiquidacionContado.longValue();
        t.FechaParidad    = toDate("0001-01-01");
        t.ValorParidad    = BigDecimal.ZERO;
        t.FechaTipoCambio = toDate("0001-01-01");
        t.ValorTipoCambio = BigDecimal.ZERO;
        t.Plazo           = tranE.Plazo;
        t.MarcaNegociacion = safeStr(tranE.MarcaNegociacion);
        t.MarcaNegociacionContraparte = safeStr(tranE.MarcaNegociacionContraparte);
        t.IndicadorDCV    = tranE.IndicadorDCV;
        t.EsBilateral     = safeStr(tranE.EsBilateral);
        t.EsFueraRueda    = safeStr(tranE.EsFueraRueda);
        t.FolioTransaccionPlazo = tranE.FolioTransaccionPlazo;
        t.CorteMinimo     = info.CorteMinimo;
        t.TipoCambioCorresponsal        = tranE.TipoCambioCorresponsal == null ? BigDecimal.ZERO : tranE.TipoCambioCorresponsal;
        t.IdCorredoraCorresponsal       = tranE.IdCorredoraCorresponsal;
        t.MontoLiquidacionCorresponsal  = tranE.MontoLiquidacionCorresponsal == null ? BigDecimal.ZERO : tranE.MontoLiquidacionCorresponsal;
        t.IdMonedaLiquidacionCorresponsal = safeStr(tranE.IdMonedaLiquidacionCorresponsal);
        t.GastosCorresponsal            = tranE.GastosCorresponsal == null ? BigDecimal.ZERO : tranE.GastosCorresponsal;
        t.MejorPrecioCompra = tranE.MejorPrecioCompra;
        t.MejorPrecioVenta  = tranE.MejorPrecioVenta;
        t.InstrumentoPermiteVentaCorta = safeStr(info.InstrumentoPermiteVentaCorta);
        t.IdEstadoLibroOrdenes = safeStr(info.IdEstadoLibroOrdenes);
        t.NemotecnicoRelacionado    = safeStr(info.NemotecnicoRelacionado);
        t.IdInstrumentoRelacionado  = info.IdInstrumentoRelacionado;
        t.IdTipoOperacionRelacionada= safeStr(info.IdTipoOperacionRelacionada);

        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) { t.BaseTasa = 360L; t.TipoInteres = "C"; }
        else if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM)) {
            t.BaseTasa = (long)(eq(tranE.IdBolsa,"BCS") ? baseTasaRvBcs : baseTasaRvBec);
            t.TipoInteres = "S";
        } else { t.BaseTasa = 0L; t.TipoInteres = ""; }

        t.Intereses  = tranE.Intereses;
        t.Reajustes  = tranE.Reajustes;
        t.EsRuteo    = safeStr(tranE.EsRuteo);
        t.Reservado  = SI.equals(t.EsRuteo) ? SI : NO;
        t.IdUsuarioReserva = SI.equalsIgnoreCase(palosReservado) ? idUsuario : (SI.equals(t.EsRuteo) ? idUsuario : 0L);
        t.FlujoOrigen = safeStr(tranE.FlujoOrigen);
        t.IdMonedaRaiz = safeStr(tranE.IdMoneda);
        t.PrecioRaiz  = eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) ? BigDecimal.ZERO : tranE.Precio;
        t.MontoRaiz   = eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) ? BigDecimal.ZERO : tranE.Monto;
        t.IdInstrumentoNegociacion = 0L;
        t.EsModificacionMarketData = safeStr(tranE.EsModificacionMarketData);
        t.EstadoModificacion       = tranE.EstadoModificacion;
        t.IdTransaccionHistoricoAnulacion = tranE.IdTransaccionHistoricoAnulacion == null ? 0L : tranE.IdTransaccionHistoricoAnulacion;
        return t;
    }

    private String resolverCamara(InformacionTransaccion info, Transaccion_Entrada t) {
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && "T2".equalsIgnoreCase(t.IdCondicionDeLiquidacionContado)) return "RV";
        if ((eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) || eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF)) && "T1".equalsIgnoreCase(t.IdCondicionDeLiquidacionContado)) return "PM";
        if ((eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) || eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF)) && ("T0".equalsIgnoreCase(t.IdCondicionDeLiquidacionContado) || "T2".equalsIgnoreCase(t.IdCondicionDeLiquidacionContado))) return "PH";
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM) && "T0".equalsIgnoreCase(t.IdCondicionDeLiquidacionContado)) return "SM";
        return "";
    }

    private Date resolverFechaVencimiento(Transaccion_Entrada t, InformacionTransaccion info) {
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF) && NEMOS_BOLSON.contains(info.NemotecnicoInstrumento) && t.NemotecnicoEspecificoBolson != null && t.NemotecnicoEspecificoBolson.length() >= 10) {
            String dia = t.NemotecnicoEspecificoBolson.substring(4,6), mes = t.NemotecnicoEspecificoBolson.substring(6,8);
            String agno = t.NemotecnicoEspecificoBolson.substring(8,10);
            agno = Integer.parseInt(agno) > 49 ? "19"+agno : "20"+agno;
            return toDate(agno+"-"+mes+"-"+dia);
        }
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RF)) return info.FechaVencimientoInstrumento;
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_DV)) return info.FechaVencimientoInstrumento;
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_IF)) return toDate(t.FechaVencimiento);
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM)
                && t.FechaVencimiento != null && !"0001-01-01".equals(t.FechaVencimiento.substring(0,10)))
            return toDate(t.FechaVencimiento);
        if (eq(info.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) return toDate(t.FechaVencimiento);
        return toDate("0001-01-01");
    }

    private void procesarRV(ArrayList<Transaccion_Proceso> transacciones, String fechaSistema, Long idEmpresa) {
        boolean tieneRV = transacciones.stream().anyMatch(t -> eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV));
        if (!tieneRV) return;
        // ConsultarNemoNegociacion
        JsonObject nemoIn = new JsonObject();
        JsonArray datosArr = new JsonArray();
        for (Transaccion_Proceso t : transacciones) {
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV)) {
                JsonObject d = new JsonObject();
                d.addProperty("IdInstrumentoRaiz", t.IdInstrumento);
                d.addProperty("IdCondLiquidacion", t.IdCondicionDeLiquidacion);
                d.addProperty("IdMoneda", t.IdMonedaRaiz);
                d.addProperty("IdConsulta", toLongFolio(t.Folio));
                datosArr.add(d);
            }
        }
        nemoIn.add("p_DatosInstrumentoConsulta", datosArr);
        JsonObject nemoOut = plataformaCallClient.call("cl.bcs.plataforma.ins.ConsultarNemoNegociacion", nemoIn);
        JsonArray nemoSalida = nemoOut.has("P_InstrumentoSalida") ? nemoOut.getAsJsonArray("P_InstrumentoSalida") : new JsonArray();
        Map<Long, Long> folioToIdInstrNegociacion = new HashMap<>();
        for (JsonElement el : nemoSalida) {
            JsonObject ne = el.getAsJsonObject();
            long idConsulta = ne.has("IdConsulta") ? ne.get("IdConsulta").getAsLong() : 0L;
            long idInstr    = ne.has("IdInstrumento") ? ne.get("IdInstrumento").getAsLong() : 0L;
            if (idInstr != 0L) folioToIdInstrNegociacion.put(idConsulta, idInstr);
        }
        for (Transaccion_Proceso t : transacciones) {
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV)) {
                Long idN = folioToIdInstrNegociacion.get(toLongFolio(t.Folio));
                if (idN != null) t.IdInstrumentoNegociacion = idN;
                else { t.PrecioRaiz = BigDecimal.ZERO; t.MontoRaiz = BigDecimal.ZERO; t.IdMonedaRaiz = ""; }
            }
        }
        // CalcularParidad
        JsonObject paridadIn = new JsonObject();
        paridadIn.addProperty("p_IdEmpresa", idEmpresa);
        paridadIn.addProperty("p_IdTrabajo", -100L);
        JsonArray calcArr = new JsonArray();
        for (Transaccion_Proceso t : transacciones) {
            if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && !safeStr(t.IdMonedaRaiz).isEmpty()
                    && !safeStr(t.IdMonedaRaiz).equals(safeStr(t.IdMonedaInstrumento))) {
                JsonObject c = new JsonObject();
                c.addProperty("IdTrabajo", toLongFolio(t.Folio));
                c.addProperty("IdInterno", toLongFolio(t.Folio));
                c.addProperty("Fecha", fechaSistema);
                c.addProperty("IdMonedaOrigen", t.IdMonedaRaiz);
                c.addProperty("MontoOrigen", t.Precio);
                c.addProperty("IdMonedaDestino", t.IdMonedaInstrumento);
                c.addProperty("IdUso", "CARTERA");
                calcArr.add(c);
            }
        }
        if (calcArr.size() > 0) {
            paridadIn.add("p_CalculosEntrada", calcArr);
            JsonObject paridadOut = plataformaCallClient.call("cl.bcs.plataforma.mer.CalcularParidad", paridadIn);
            JsonArray calcSalida = paridadOut.has("p_out_CalculosSalida") ? paridadOut.getAsJsonArray("p_out_CalculosSalida") : new JsonArray();
            Map<Long, BigDecimal> folioToPrecioRaiz = new HashMap<>();
            for (JsonElement el : calcSalida) {
                JsonObject cs = el.getAsJsonObject();
                long id = cs.has("IdInterno") ? cs.get("IdInterno").getAsLong() : 0L;
                BigDecimal monto = cs.has("MontoDestino") ? cs.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;
                folioToPrecioRaiz.put(id, monto);
            }
            for (Transaccion_Proceso t : transacciones) {
                if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && !safeStr(t.IdMonedaRaiz).isEmpty()) {
                    if (safeStr(t.IdMonedaRaiz).equals(safeStr(t.IdMonedaInstrumento))) {
                        t.PrecioRaiz = t.Precio; t.MontoRaiz = t.Monto;
                    } else {
                        BigDecimal pr = folioToPrecioRaiz.getOrDefault(toLongFolio(t.Folio), BigDecimal.ZERO);
                        t.PrecioRaiz = pr; t.MontoRaiz = t.Cantidad.multiply(pr);
                    }
                } else if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV)) {
                    t.PrecioRaiz = t.Precio; t.MontoRaiz = t.Monto; t.IdMonedaRaiz = t.IdMonedaInstrumento;
                }
            }
        } else {
            for (Transaccion_Proceso t : transacciones) {
                if (eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && !safeStr(t.IdMonedaRaiz).isEmpty()) {
                    t.PrecioRaiz = t.Precio; t.MontoRaiz = t.Monto;
                }
            }
        }
    }

    private void procesarNemoSVS(ArrayList<Transaccion_Proceso> transacciones) {
        List<Transaccion_Proceso> sinNemo = transacciones.stream()
            .filter(t -> eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_IF) && safeStr(t.NemotecnicoSVS).isEmpty())
            .collect(Collectors.toList());
        if (sinNemo.isEmpty()) return;
        JsonObject in = new JsonObject(); JsonArray arr = new JsonArray();
        for (Transaccion_Proceso t : sinNemo) {
            JsonObject e = new JsonObject();
            e.addProperty("FechaVencimiento", toStringDate(t.FechaVencimiento));
            e.addProperty("IdEmisor", t.IdEmisor);
            e.addProperty("IdInterno", t.IdInterno);
            e.addProperty("IdTipoReajuste", t.IdTipoReajuste);
            e.addProperty("Nemotecnico", t.Nemotecnico);
            e.addProperty("PagaderoEn", t.IdMonedaInstrumento);
            e.addProperty("Plazo", t.Plazo);
            arr.add(e);
        }
        in.add("p_Instrumentos", arr);
        JsonObject out = plataformaCallClient.call("cl.bcs.plataforma.pla.ObtenerNemoSVS", in);
        if (out.has("p_out_Instrumento")) {
            for (JsonElement el : out.getAsJsonArray("p_out_Instrumento")) {
                JsonObject inst = el.getAsJsonObject();
                long id = inst.has("IdInterno") ? inst.get("IdInterno").getAsLong() : 0L;
                String nemo = inst.has("NemoSVS") ? inst.get("NemoSVS").getAsString() : "";
                for (Transaccion_Proceso t : transacciones)
                    if (t.IdInterno.equals(id)) { t.NemotecnicoSVS = nemo; break; }
            }
        }
    }

    private long contarIdsAdicionales(ArrayList<Transaccion_Proceso> transacciones) {
        long count = 0;
        for (Transaccion_Proceso t : transacciones) {
            boolean esOD = OD.equalsIgnoreCase(t.IndicadorOrdenDirecta) || OE.equalsIgnoreCase(t.IndicadorOrdenDirecta);
            if (esOD) count += eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM) || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE) ? 3 : 1;
            else if (eq(t.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM) || eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) count += 1;
        }
        return count;
    }

    private int procesarOrdenDirecta(ArrayList<Transaccion_Proceso> transacciones, List<Long> ids) {
        int pos = 0;
        ArrayList<Transaccion_Proceso> odList = new ArrayList<>();
        for (Transaccion_Proceso tran : transacciones) {
            if (!OD.equalsIgnoreCase(tran.IndicadorOrdenDirecta) && !OE.equalsIgnoreCase(tran.IndicadorOrdenDirecta)) continue;
            Transaccion_Proceso od = copyTransaccion(tran);
            od.MarcaNegociacion = od.MarcaNegociacionContraparte;
            od.IdTransaccion = ids.get(pos++);
            od.EsVentaCorta = (ACCION_COMPRA.equalsIgnoreCase(od.AccionOrden) && SI.equalsIgnoreCase(od.EsVentaCortaOD)) ? od.EsVentaCortaOD : NO;
            od.CodigoOperadorContraparte = tran.CodigoOperadorCorredor;
            od.CodigoOperadorCorredor    = tran.CodigoOperadorContraparte;
            od.IdTransaccionOd = tran.IdTransaccion;
            tran.IdTransaccionOd = od.IdTransaccion;
            if (esTipoSim(od)) { od.NemotecnicoRelacionado = od.Nemotecnico; od.IdInstrumentoRelacionado = od.IdInstrumento; od.Nemotecnico = tran.NemotecnicoRelacionado; od.IdInstrumento = tran.IdInstrumentoRelacionado; }
            od.IdTipoOperacionRelacionada = od.IdTipoOperacion; od.IdTipoOperacion = tran.IdTipoOperacionRelacionada;
            odList.add(od);
        }
        transacciones.addAll(odList);
        return pos;
    }

    private int procesarTransaccionesPlazo(ArrayList<Transaccion_Proceso> transacciones, List<Long> ids, int pos) {
        ArrayList<Transaccion_Proceso> plazoList = new ArrayList<>();
        for (Transaccion_Proceso tran : transacciones) {
            if (!(eq(tran.IdClasificacionTipoInstrumento, CLASIFICACION_RV) && eq(tran.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM))
                    && !eq(tran.IdClasificacionTipoInstrumento, CLASIFICACION_RE)) continue;
            if (pos >= ids.size()) break;
            Transaccion_Proceso p = copyTransaccion(tran);
            p.IdTransaccion = ids.get(pos++);
            p.IdTransaccionPlazo = tran.IdTransaccion;
            tran.IdTransaccionPlazo = p.IdTransaccion;
            p.Folio = tran.FolioTransaccionPlazo;
            p.FechaLiquidacion = tran.FechaVencimiento;
            p.EsAPlazo = SI;
            p.Monto = tran.MontoPlazo;
            p.Precio = tran.PrecioPlazo;
            p.IdCondicionDeLiquidacion = "TP";
            if (esTipoSim(p)) { p.NemotecnicoRelacionado = p.Nemotecnico; p.IdInstrumentoRelacionado = p.IdInstrumento; p.Nemotecnico = tran.NemotecnicoRelacionado; p.IdInstrumento = tran.IdInstrumentoRelacionado; }
            p.IdTipoOperacionRelacionada = p.IdTipoOperacion; p.IdTipoOperacion = tran.IdTipoOperacionRelacionada;
            plazoList.add(p);
        }
        transacciones.addAll(plazoList);
        return pos;
    }

    private List<Transaccion_Proceso> procesarPrelacion(Entrada in, ArrayList<Transaccion_Proceso> transacciones,
            Map<Long, InformacionTransaccion> infoMap, Map<String, InformacionOperador> infoOps,
            Map<String, ConfiguracionShortName> shortNames, Map<String, RutPortafolio> rutsPrelacion,
            Map<String, ConfiguracionAsignacion> configAsignacion, String parametroPrelacion, String fechaSistema) {
        String opRuteoAgente  = configUtil.getParametroString(in.p_IdEmpresa, "OperadorRuteoAgente");
        String opRuteoRetail  = configUtil.getParametroString(in.p_IdEmpresa, "OperadorRuteoRetail");
        Pattern patRut  = Pattern.compile("\\d+-(?:[0-9]|K|k)");
        Pattern patRut2 = Pattern.compile("\\d+-(?:[0-9]|K|k);[a-zA-Z0-9''\\-|]+");
        List<Transaccion_Proceso> controlMercado = new ArrayList<>();
        List<LibrosOrdenesCambiar> librosACambiar = new ArrayList<>();
        for (Transaccion_Proceso tran : transacciones) {
            if (SI.equalsIgnoreCase(tran.EsAPlazo)) continue;
            InformacionOperador ope = infoOps.get(claveMapa(tran.CodigoOperadorCorredor, tran.IdTipoInstrumento));
            ConfiguracionShortName sn = shortNames.get(claveMapa(tran.MarcaNegociacion, tran.IdTipoInstrumento));
            InformacionTransaccion info = infoMap.get(tran.IdInterno);
            ConfiguracionAsignacion ca = configAsignacion.get(claveMapa(tran.IdTipoInstrumento));
            if (tran.CodigoOperadorCorredor.equalsIgnoreCase(opRuteoAgente) || tran.CodigoOperadorCorredor.equalsIgnoreCase(opRuteoRetail))
                tran.EsRuteo = SI;
            boolean procesado = false;
            if (SI.equalsIgnoreCase(parametroPrelacion)) {
                if (!procesado) procesado = prelacionOperador(tran, ope, parametroPrelacion, controlMercado);
                if (!procesado) procesado = prelacionClientePortafolio(in, tran, patRut, patRut2, rutsPrelacion, ca, parametroPrelacion, fechaSistema, controlMercado, librosACambiar);
                if (!procesado) procesado = prelacionShortname(tran, info, sn, parametroPrelacion, controlMercado, librosACambiar);
                if (!procesado) procesado = prelacionMarcaNeg(tran, parametroPrelacion, controlMercado);
            } else {
                if (!procesado) procesado = prelacionShortname(tran, info, sn, parametroPrelacion, controlMercado, librosACambiar);
                if (!procesado) procesado = prelacionClientePortafolio(in, tran, patRut, patRut2, rutsPrelacion, ca, parametroPrelacion, fechaSistema, controlMercado, librosACambiar);
                if (!procesado) procesado = prelacionOperador(tran, ope, parametroPrelacion, controlMercado);
            }
            if (!procesado && NO.equalsIgnoreCase(tran.EsRuteo)) tran.IdUsuarioReserva = 0L;
        }
        if (!librosACambiar.isEmpty()) {
            SqlParameterSource[] ps = librosACambiar.stream()
                .map(l -> new MapSqlParameterSource().addValue("IdLibroOrdenes",l.IdLibroOrdenes).addValue("IdTransaccion",l.IdTransaccion))
                .toArray(SqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_UPDATE_LIBRO_ORDEN, ps);
        }
        return controlMercado;
    }

    private boolean prelacionOperador(Transaccion_Proceso tran, InformacionOperador ope, String param, List<Transaccion_Proceso> cm) {
        if (ope == null) return false;
        if (SI.equalsIgnoreCase(param) && tran.CodigoOperadorCorredor.equalsIgnoreCase(ope.CodigoOperador) && tran.IdTipoInstrumento.equalsIgnoreCase(ope.IdTipoInstrumento)) {
            if ((SI.equalsIgnoreCase(ope.UtilizaPortafolio) && ope.IdPortafolio > 0) || SI.equalsIgnoreCase(ope.ReservaTransaccion)) { tran.TipoMensaje="TRANSACCION_OPERADOR"; cm.add(tran); return true; }
        } else if (NO.equalsIgnoreCase(param) && tran.CodigoOperadorCorredor.equalsIgnoreCase(ope.CodigoOperador) && tran.IdTipoInstrumento.equalsIgnoreCase(ope.IdTipoInstrumento) && NO.equalsIgnoreCase(tran.EsAsignacionDirecta)) {
            if ((SI.equalsIgnoreCase(ope.UtilizaPortafolio) && ope.IdPortafolio > 0 && SI.equalsIgnoreCase(ope.AsignacionAuto)) || SI.equalsIgnoreCase(ope.ReservaTransaccion)) { tran.TipoMensaje="TRANSACCION_OPERADOR"; cm.add(tran); return true; }
        }
        return false;
    }

    private boolean prelacionShortname(Transaccion_Proceso tran, InformacionTransaccion info, ConfiguracionShortName conf,
            String param, List<Transaccion_Proceso> cm, List<LibrosOrdenesCambiar> libros) {
        if (conf == null || info == null) return false;
        if (!conf.ShortName.equalsIgnoreCase(tran.MarcaNegociacion)) return false;
        boolean tipoOk = tran.IdTipoInstrumento.equalsIgnoreCase(conf.IdTipoInstrumento)
            || (eq(info.IdTipoInstrumento, TIPO_INSTRUMENTO_SIM) && info.IdTipoInstrumentoSubyacente != null && info.IdTipoInstrumentoSubyacente.equalsIgnoreCase(conf.IdTipoInstrumento));
        if (!tipoOk || !NO.equalsIgnoreCase(tran.EsRuteo)) return false;
        if (NO.equalsIgnoreCase(param) && !SI.equalsIgnoreCase(conf.AsignacionAuto)) return false;
        tran.TipoMensaje = "SHORTNAME";
        if (NO.equalsIgnoreCase(param)) tran.EsAsignacionDirecta = SI;
        if (conf.IdLibroOrdenes != null && !tran.IdLibroOrdenes.equals(conf.IdLibroOrdenes)) {
            tran.IdLibroOrdenes = conf.IdLibroOrdenes;
            LibrosOrdenesCambiar l = new LibrosOrdenesCambiar(); l.IdLibroOrdenes = conf.IdLibroOrdenes; l.IdTransaccion = tran.IdTransaccion; libros.add(l);
        }
        cm.add(tran); return true;
    }

    private boolean prelacionClientePortafolio(Entrada in, Transaccion_Proceso tran, Pattern pat1, Pattern pat2,
            Map<String,RutPortafolio> ruts, ConfiguracionAsignacion ca, String param, String fechaSistema,
            List<Transaccion_Proceso> cm, List<LibrosOrdenesCambiar> libros) {
        if (pat1.matcher(tran.MarcaNegociacion).matches()) tran.MarcaNegociacion += ";0";
        String[] parts = tran.MarcaNegociacion.split(";");
        if (parts.length <= 1) return false;
        RutPortafolio rp = ruts.get(claveMapa(parts[0], parts[1]));
        if (pat2.matcher(tran.MarcaNegociacion).matches() && rp == null) {
            agregarLogExterno(in, tran, "ASIGNACION-RUTCUENTA", "Error: No existe Cliente con Documento Identidad " + parts[0] + " portafolio:" + parts[1], fechaSistema);
            return false;
        }
        if (rp == null) return false;
        if ("BLOQUEADO".equalsIgnoreCase(rp.NombreEstado)) {
            agregarLogExterno(in, tran, "ASIGNACION-RUTCUENTA", "Error: Cliente Bloqueado Documento Identidad " + parts[0], fechaSistema);
            return false;
        }
        if (!parts[0].equalsIgnoreCase(rp.DocumentoIdentidad) || !parts[1].equalsIgnoreCase(rp.CodigoPortafolio)) return false;
        tran.MarcaNegociacionCorrecta = SI;
        if (rp.IdLibroOrdenes != null && !tran.IdLibroOrdenes.equals(rp.IdLibroOrdenes)) {
            tran.IdLibroOrdenes = rp.IdLibroOrdenes;
            LibrosOrdenesCambiar l = new LibrosOrdenesCambiar(); l.IdLibroOrdenes = rp.IdLibroOrdenes; l.IdTransaccion = tran.IdTransaccion; libros.add(l);
        }
        if (SI.equalsIgnoreCase(tran.MarcaNegociacionCorrecta)) {
            if (NO.equalsIgnoreCase(param) && NO.equalsIgnoreCase(tran.EsRuteo)) {
                if (ca != null && tran.IdTipoInstrumento.equalsIgnoreCase(ca.IdTipoInstrumento) && SI.equalsIgnoreCase(ca.AsignacionAuto)) { tran.EsAsignacionDirecta = SI; tran.TipoMensaje = "ASIGNACION_DIRECTA"; cm.add(tran); return true; }
            } else if (SI.equalsIgnoreCase(param) && NO.equalsIgnoreCase(tran.EsRuteo)) { tran.TipoMensaje = "ASIGNACION_DIRECTA"; cm.add(tran); return true; }
        }
        return false;
    }

    private boolean prelacionMarcaNeg(Transaccion_Proceso tran, String param, List<Transaccion_Proceso> cm) {
        if (SI.equalsIgnoreCase(param) && NO.equalsIgnoreCase(tran.EsRuteo) && tran.MarcaNegociacion != null && !tran.MarcaNegociacion.isEmpty()) {
            tran.TipoMensaje = "TRANSACCION_MARCA_NEGOCIACION"; cm.add(tran); return true;
        }
        return false;
    }

    private void agregarLogExterno(Entrada in, Transaccion_Proceso tran, String tipo, String msg, String fechaSistema) {
        try {
            JsonObject logIn = new JsonObject(); logIn.addProperty("p_IdTrabajo", 1L);
            JsonArray arr = new JsonArray(); JsonObject l = new JsonObject();
            l.addProperty("IdCabeceraLog", tran.IdTransaccion); l.addProperty("IdTipoLog","INGRESO"); l.addProperty("IdModulo","ORD");
            l.addProperty("IdUsuario", in.p_IdUsuario); l.addProperty("IdEmpresa", in.p_IdEmpresa); l.addProperty("FechaSistemaLog", fechaSistema);
            l.addProperty("IdTipoRegistro", tipo); l.addProperty("Nemo", msg);
            arr.add(l); logIn.add("p_Log", arr);
            plataformaCallClient.call("cl.bcs.plataforma.pla.AgregarLog", logIn);
        } catch (Exception e) { log.warn("AgregarLog failed: {}", e.getMessage()); }
    }

    private void agregarLog(Entrada in, ArrayList<Map<String,Object>> finales, String fechaSistema, String tipoLog) {
        try {
            JsonObject logIn = new JsonObject(); logIn.addProperty("p_IdTrabajo", 1L); JsonArray arr = new JsonArray();
            for (Map<String,Object> tf : finales) {
                JsonObject l = new JsonObject();
                l.addProperty("IdCabeceraLog", toLong(tf.get("IdTransaccion"))); l.addProperty("IdTipoLog","INGRESO"); l.addProperty("IdModulo","ORD");
                l.addProperty("IdUsuario", in.p_IdUsuario); l.addProperty("IdEmpresa", in.p_IdEmpresa); l.addProperty("FechaSistemaLog", fechaSistema);
                l.addProperty("IdTipoRegistro","ORDEN"); l.addProperty("IdTipoOperacion", safeStr((String)tf.get("IdTipoOperacion")));
                l.addProperty("Nemotecnico", safeStr((String)tf.get("Nemotecnico")));
                arr.add(l);
            }
            logIn.add("p_Log", arr);
            plataformaCallClient.call("cl.bcs.plataforma.pla.AgregarLog", logIn);
        } catch (Exception e) { log.warn("AgregarLog (pre) failed: {}", e.getMessage()); }
    }

    private void agregarLogPost(Entrada in, ArrayList<Transaccion_Proceso> transacciones, String fechaSistema) {
        try {
            JsonObject logIn = new JsonObject(); JsonArray arr = new JsonArray();
            for (Transaccion_Proceso t : transacciones) {
                JsonObject l = new JsonObject();
                l.addProperty("IdCabeceraLog", t.IdTransaccion); l.addProperty("IdTipoLog","INGRESO"); l.addProperty("IdModulo","ORD");
                l.addProperty("IdUsuario", in.p_IdUsuario); l.addProperty("IdEmpresa", in.p_IdEmpresa); l.addProperty("FechaSistemaLog", fechaSistema);
                l.addProperty("IdTipoRegistro","TRANSACCION"); l.addProperty("IdRegistro", t.IdTransaccion+""); l.addProperty("EstadoRegistro", safeStr(t.IdEstado));
                l.addProperty("IdTipoOperacion", safeStr(t.IdTipoOperacion)); l.addProperty("Folio", toLongFolio(t.Folio));
                l.addProperty("Nemotecnico", safeStr(t.Nemotecnico)); l.addProperty("NombreProceso","Ingresar Transaccion");
                arr.add(l);
            }
            logIn.add("p_Log", arr);
            plataformaCallClient.call("cl.bcs.plataforma.pla.AgregarLog", logIn);
        } catch (Exception e) { log.warn("AgregarLog (post) failed: {}", e.getMessage()); }
    }

    private ArrayList<Map<String,Object>> buildTransaccionesFinales(ArrayList<Transaccion_Proceso> transacciones) {
        ArrayList<Map<String,Object>> result = new ArrayList<>();
        for (Transaccion_Proceso t : transacciones) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("IdTransaccion", t.IdTransaccion);
            m.put("Folio", toLongFolio(t.Folio));
            m.put("FechaSistemaCreacion", t.FechaSistemaCreacion);
            m.put("HoraSistemaCreacion", t.HoraSistemaCreacion);
            m.put("FechaOperacion", t.FechaOperacion);
            m.put("FechaLiquidacion", t.FechaLiquidacion);
            m.put("FechaEmision", t.FechaEmision);
            m.put("FechaVencimiento", t.FechaVencimiento);
            m.put("FechaIngresoOferta", t.FechaIngresoOferta);
            m.put("IdSistemaBursatil", t.IdSistemaBursatil);
            m.put("EsCuentaPropia", t.EsCuentaPropia);
            m.put("EsFungible", t.EsFungible);
            m.put("EsAPlazo", t.EsAPlazo);
            m.put("EsVentaCorta", t.EsVentaCorta);
            m.put("CodigoOperadorContraparte", t.CodigoOperadorContraparte);
            m.put("CodigoOperadorCorredor", t.CodigoOperadorCorredor);
            m.put("IdEmisor", t.IdEmisor);
            m.put("Cantidad", t.Cantidad);
            m.put("Monto", t.Monto);
            m.put("Precio", t.Precio);
            m.put("Tasa", t.Tasa);
            m.put("Nemotecnico", t.Nemotecnico);
            m.put("NemotecnicoSVS", t.NemotecnicoSVS);
            m.put("ValorRescate", t.ValorRescate);
            m.put("IdEstado", t.IdEstado);
            m.put("IdTransaccionPlazo", t.IdTransaccionPlazo);
            m.put("IdTransaccionOd", t.IdTransaccionOd);
            m.put("IdTipoInstrumento", t.IdTipoInstrumento);
            m.put("IdClasificacionTipoInstrumento", t.IdClasificacionTipoInstrumento);
            m.put("IdTipoReajuste", t.IdTipoReajuste);
            m.put("IdEmpleado", t.IdEmpleado);
            m.put("IdTipoOperacion", t.IdTipoOperacion);
            m.put("IdCondicionDeLiquidacion", t.IdCondicionDeLiquidacion);
            // IdMoneda: for RV = IdMonedaRaiz, else = IdMonedaInstrumento
            m.put("IdMoneda", eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) ? t.IdMonedaRaiz : t.IdMonedaInstrumento);
            m.put("IdCorredorContraparte", t.IdCorredoraContraparte);
            m.put("IdEmpresa", t.IdEmpresa);
            m.put("IdBolsa", t.IdBolsa);
            m.put("IdLibroOrdenes", t.IdLibroOrdenes);
            m.put("IdInstrumento", t.IdInstrumento);
            m.put("NemotecnicoSubyacente", t.NemotecnicoSubyacente);
            m.put("IdInstrumentoSubyacente", t.IdInstrumentoSubyacente);
            m.put("IdMonedaSubyacente", t.IdMonedaSubyacente);
            m.put("FechaParidad", t.FechaParidad);
            m.put("ValorParidad", t.ValorParidad);
            m.put("FechaTipoCambio", t.FechaTipoCambio);
            m.put("ValorTipoCambio", t.ValorTipoCambio);
            m.put("Plazo", t.Plazo);
            m.put("FechaModificacionOperacion", t.FechaModificacionOperacion);
            m.put("MarcaNegociacion", t.MarcaNegociacion);
            m.put("IndicadorDCV", t.IndicadorDCV);
            m.put("EsBilateral", t.EsBilateral);
            m.put("EsFueraRueda", t.EsFueraRueda);
            m.put("IdCorredorLocal", t.IdCorredoraLocal);
            m.put("IdProgramaProcesaAutomatico", t.IdProgramaProcesaAutomatico);
            m.put("TipoCambioCorresponsal", t.TipoCambioCorresponsal);
            m.put("IdCorredorCorresponsal", t.IdCorredoraCorresponsal);
            m.put("MontoLiquidacionCorresponsal", t.MontoLiquidacionCorresponsal);
            m.put("IdMonedaLiquidacionCorresponsal", t.IdMonedaLiquidacionCorresponsal);
            m.put("MejorPrecioCompra", t.MejorPrecioCompra);
            m.put("MejorPrecioVenta", t.MejorPrecioVenta);
            m.put("BaseTasa", t.BaseTasa);
            m.put("TipoInteres", t.TipoInteres);
            m.put("IndicadorOrdenDirecta", t.IndicadorOrdenDirecta);
            m.put("IdMonedaEmision", t.IdMonedaEmision);
            m.put("Intereses", t.Intereses);
            m.put("Reajustes", t.Reajustes);
            m.put("GastosCorresponsal", t.GastosCorresponsal);
            m.put("IdBoveda", t.IdBoveda);
            m.put("IdCamara", t.IdCamara);
            m.put("Reservado", t.Reservado);
            m.put("IdUsuarioReserva", t.IdUsuarioReserva);
            m.put("FlujoOrigen", t.FlujoOrigen);
            m.put("PrecioRaiz", t.PrecioRaiz);
            // IdMonedaRaiz: for RV = IdMonedaInstrumento (swapped), else = IdMonedaRaiz
            m.put("IdMonedaRaiz", eq(t.IdClasificacionTipoInstrumento, CLASIFICACION_RV) ? t.IdMonedaInstrumento : t.IdMonedaRaiz);
            m.put("MontoRaiz", t.MontoRaiz);
            m.put("IdInstrumentoNegociacion", t.IdInstrumentoNegociacion);
            m.put("MotivoReserva", t.MotivoReserva);
            result.add(m);
        }
        return result;
    }

    private JsonObject buildHistoricasParams(Entrada in, ArrayList<Transaccion_Proceso> transacciones) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", in.p_IdEmpresa);
        p.addProperty("p_IdUsuario", in.p_IdUsuario);
        p.addProperty("p_IdModulo", in.p_IdModulo);
        p.addProperty("p_UsaReglasNegocio", safeStr(in.p_UsaReglasNegocio));
        p.addProperty("p_Reproceso", safeStr(in.p_Reproceso));
        JsonArray arr = new JsonArray();
        for (Transaccion_Proceso t : transacciones) {
            JsonObject ht = new JsonObject();
            ht.addProperty("TipoRegistro", "ING");
            ht.addProperty("IdEstadoRegistro", safeStr(t.EstadoModificacion));
            ht.addProperty("EsModificacionMarketData", safeStr(t.EsModificacionMarketData));
            ht.addProperty("IdTransaccionHistoricoAnulacion", t.IdTransaccionHistoricoAnulacion);
            ht.addProperty("IdTransaccion", t.IdTransaccion);
            ht.addProperty("Folio", toLongFolio(t.Folio));
            ht.addProperty("IdTipoOperacion", safeStr(t.IdTipoOperacion));
            ht.addProperty("IdEstado", safeStr(t.IdEstado));
            ht.addProperty("Nemotecnico", safeStr(t.Nemotecnico));
            arr.add(ht);
        }
        p.add("p_Transacciones", arr);
        return p;
    }

    // ── Infrastructure helpers ────────────────────────────────────────────────────

    private void batchInsertWrapper(List<Transaccion_Entrada> list) {
        SqlParameterSource[] ps = list.stream().map(t -> new MapSqlParameterSource()
            .addValue("IdInterno", t.IdInterno)
            .addValue("IdInstrumento", t.IdInstrumento)
            .addValue("IdInstrumentoSubyacente", t.IdInstrumentoSubyacente)
            .addValue("IdLibroOrdenes", t.IdLibroOrdenes)
            .addValue("IdBolsa", t.IdBolsa)
            .addValue("IdTipoOperacion", t.IdTipoOperacion)
            .addValue("IdCorredoraCorresponsal", t.IdCorredoraCorresponsal)
            .addValue("IdTipoReajuste", t.IdTipoReajuste)
            .addValue("IdCondicionDeLiquidacionContado", t.IdCondicionDeLiquidacionContado)
            .addValue("CodigoOperadorCorredor", t.CodigoOperadorCorredor)
            .addValue("FlujoOrigen", t.FlujoOrigen))
            .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(SQL_INSERT_WRAPPER, ps);
    }

    private InformacionTransaccion rowToInfoTransaccion(Map<String,Object> r) {
        InformacionTransaccion it = new InformacionTransaccion();
        it.IdInterno = toLong(r.get("IdInterno")); it.IdTransaccion = toLong(r.get("IdTransaccion"));
        it.IdClasificacionTipoInstrumento = safeStr((String)r.get("IdClasificacionTipoInstrumento"));
        it.IdTipoInstrumento = safeStr((String)r.get("IdTipoInstrumento")); it.AccionOrden = safeStr((String)r.get("AccionOrden"));
        it.NemotecnicoInstrumento = safeStr((String)r.get("NemotecnicoInstrumento")); it.IdEmisorInstrumento = safeStr((String)r.get("IdEmisorInstrumento"));
        it.IdTipoReajusteInstrumento = safeStr((String)r.get("IdTipoReajusteInstrumento")); it.IdMonedaEmisionInstrumento = safeStr((String)r.get("IdMonedaEmisionInstrumento"));
        it.IdMonedaInstrumento = safeStr((String)r.get("IdMonedaInstrumento")); it.IdPaisBolsa = toLong(r.get("IdPaisBolsa"));
        it.NemotecnicoInstrumentoSubyacente = safeStr((String)r.get("NemotecnicoInstrumentoSubyacente")); it.IdInstrumentoSubyacente = toLong(r.get("IdInstrumentoSubyacente"));
        it.IdMonedaInstrumentoSubyacente = safeStr((String)r.get("IdMonedaInstrumentoSubyacente")); it.IdEmisorInstrumentoSubyacente = safeStr((String)r.get("IdEmisorInstrumentoSubyacente"));
        Object cm = r.get("CorteMinimo"); it.CorteMinimo = cm == null ? BigDecimal.ZERO : new BigDecimal(cm.toString());
        it.InstrumentoPermiteVentaCorta = safeStr((String)r.get("InstrumentoPermiteVentaCorta")); it.IdEstadoLibroOrdenes = safeStr((String)r.get("IdEstadoLibroOrdenes"));
        Object dlc = r.get("DiasLiquidacionContado"); it.DiasLiquidacionContado = dlc == null ? 0 : ((Number)dlc).intValue();
        it.NemotecnicoRelacionado = safeStr((String)r.get("NemotecnicoRelacionado")); it.IdInstrumentoRelacionado = toLong(r.get("IdInstrumentoRelacionado"));
        it.IdTipoOperacionRelacionada = safeStr((String)r.get("IdTipoOperacionRelacionada")); it.BolsaEsExtranjera = safeStr((String)r.get("BolsaEsExtranjera"));
        it.IdTipoInstrumentoSubyacente = safeStr((String)r.get("IdTipoInstrumentoSubyacente"));
        Object fe = r.get("FechaEmisionInstrumento"); it.FechaEmisionInstrumento = fe instanceof Date ? (Date)fe : (fe != null ? Date.valueOf(fe.toString().substring(0,10)) : Date.valueOf("0001-01-01"));
        Object fv = r.get("FechaVencimientoInstrumento"); it.FechaVencimientoInstrumento = fv instanceof Date ? (Date)fv : (fv != null ? Date.valueOf(fv.toString().substring(0,10)) : Date.valueOf("0001-01-01"));
        return it;
    }

    private Empresa consultarEmpresa(Long idEmpresa) {
        List<Map<String,Object>> rows = jdbc.queryForList(SQL_EMPRESA, new MapSqlParameterSource("IdEmpresa", idEmpresa));
        if (rows.isEmpty()) throw new IllegalStateException("Empresa no existe: " + idEmpresa);
        Map<String,Object> r = rows.get(0);
        Empresa e = new Empresa(); e.IdEmpresa = toLong(r.get("IdEmpresa")); e.IdCorredora = toLong(r.get("IdCorredora")); e.IdPais = toLong(r.get("IdPais"));
        return e;
    }

    private List<Long> getSecuencia(long count) {
        List<Long> ids = new ArrayList<>();
        if (count <= 0) return ids;
        for (long i = 0; i < count; i++) {
            Long nv = jdbc.getJdbcTemplate().queryForObject(
                "SELECT \"BSCL.Orden.BD.Secuencias::Transaccion\".NEXTVAL FROM DUMMY", Long.class);
            ids.add(nv == null ? 0L : nv);
        }
        return ids;
    }

    private Long resolverLibroOrdenes(String codigoOp, List<LibrosOrdenes> def, List<LibrosOrdenes> porOp) {
        if (codigoOp != null && !codigoOp.isEmpty() && porOp != null) {
            for (LibrosOrdenes l : porOp) if (codigoOp.equalsIgnoreCase(l.CodigoOperador) && l.IdLibroOrdenes != null) return l.IdLibroOrdenes;
        }
        return def != null && !def.isEmpty() ? def.get(0).IdLibroOrdenes : 1L;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> queryList(String sql, MapSqlParameterSource params, Class<T> clazz) {
        return (List<T>) jdbc.queryForList(sql, params);
    }

    private void limpiarTransacciones(ArrayList<Transaccion_Proceso> transacciones, ArrayList<Transaccion_Proceso> errores) {
        for (Transaccion_Proceso t : transacciones) if (t.errores > 0) errores.add(t);
        transacciones.removeAll(errores);
    }

    private void procesaRequisito(boolean esError, String codMsg, String tipo, Transaccion_Proceso t, List<MensajeSalida> msgs) {
        if (esError) { msgs.add(new MensajeSalida(t.IdInterno, tipo, codMsg)); if (TIPO_ERROR.equals(tipo)) t.errores++; else if (TIPO_AUTH.equals(tipo)) t.autorizaciones++; else if (TIPO_WARN.equals(tipo)) t.advertencias++; }
        else t.ok++;
    }

    private Transaccion_Proceso copyTransaccion(Transaccion_Proceso src) {
        Transaccion_Proceso d = new Transaccion_Proceso();
        d.AccionOrden=src.AccionOrden; d.BaseTasa=src.BaseTasa; d.CorteMinimo=src.CorteMinimo; d.Cantidad=src.Cantidad;
        d.CodigoOperadorCorredor=src.CodigoOperadorCorredor; d.CodigoOperadorContraparte=src.CodigoOperadorContraparte;
        d.DiasLiquidacionContado=src.DiasLiquidacionContado; d.EsFueraRueda=src.EsFueraRueda; d.EsBilateral=src.EsBilateral;
        d.EsVentaCorta=src.EsVentaCorta; d.EsVentaCortaOD=src.EsVentaCortaOD; d.EsAPlazo=src.EsAPlazo; d.EsFungible=src.EsFungible; d.EsCuentaPropia=src.EsCuentaPropia;
        d.FechaLiquidacion=src.FechaLiquidacion; d.FechaTipoCambio=src.FechaTipoCambio; d.FolioTransaccionPlazo=src.FolioTransaccionPlazo;
        d.FechaIngresoOferta=src.FechaIngresoOferta; d.FechaVencimiento=src.FechaVencimiento; d.FechaEmision=src.FechaEmision;
        d.FechaSistemaCreacion=src.FechaSistemaCreacion; d.Folio=src.Folio; d.FechaOperacion=src.FechaOperacion;
        d.FechaModificacionOperacion=src.FechaModificacionOperacion; d.FechaParidad=src.FechaParidad; d.GastosCorresponsal=src.GastosCorresponsal;
        d.HoraSistemaCreacion=src.HoraSistemaCreacion; d.IdTipoOperacionRelacionada=src.IdTipoOperacionRelacionada;
        d.IdInstrumentoRelacionado=src.IdInstrumentoRelacionado; d.IdEstadoLibroOrdenes=src.IdEstadoLibroOrdenes;
        d.InstrumentoPermiteVentaCorta=src.InstrumentoPermiteVentaCorta; d.IdMonedaLiquidacionCorresponsal=src.IdMonedaLiquidacionCorresponsal;
        d.IdCorredoraCorresponsal=src.IdCorredoraCorresponsal; d.IndicadorDCV=src.IndicadorDCV;
        d.IdProgramaProcesaAutomatico=src.IdProgramaProcesaAutomatico; d.IdBolsa=src.IdBolsa; d.IdBoveda=src.IdBoveda;
        d.IdCorredoraLocal=src.IdCorredoraLocal; d.IdCorredoraContraparte=src.IdCorredoraContraparte;
        d.IdMonedaInstrumento=src.IdMonedaInstrumento; d.IdMonedaEmision=src.IdMonedaEmision; d.IdEmpleado=src.IdEmpleado;
        d.IdTipoOperacion=src.IdTipoOperacion; d.IdCondicionDeLiquidacion=src.IdCondicionDeLiquidacion;
        d.IdTipoReajuste=src.IdTipoReajuste; d.IdClasificacionTipoInstrumento=src.IdClasificacionTipoInstrumento;
        d.IdTipoInstrumento=src.IdTipoInstrumento; d.IdTransaccionOd=src.IdTransaccionOd; d.IdTransaccionPlazo=src.IdTransaccionPlazo;
        d.IdEstado=src.IdEstado; d.IdInterno=src.IdInterno; d.IdTransaccion=src.IdTransaccion; d.Intereses=src.Intereses;
        d.IdPaisBolsa=src.IdPaisBolsa; d.IdLibroOrdenes=src.IdLibroOrdenes; d.IdEmpresa=src.IdEmpresa;
        d.IdMonedaSubyacente=src.IdMonedaSubyacente; d.IdInstrumentoSubyacente=src.IdInstrumentoSubyacente;
        d.IdInstrumento=src.IdInstrumento; d.IndicadorOrdenDirecta=src.IndicadorOrdenDirecta; d.IdEmisor=src.IdEmisor; d.IdSistemaBursatil=src.IdSistemaBursatil;
        d.MarcaNegociacion=src.MarcaNegociacion; d.MarcaNegociacionContraparte=src.MarcaNegociacionContraparte;
        d.MejorPrecioVenta=src.MejorPrecioVenta; d.MontoLiquidacionCorresponsal=src.MontoLiquidacionCorresponsal;
        d.MejorPrecioCompra=src.MejorPrecioCompra; d.MontoPlazo=src.MontoPlazo; d.Monto=src.Monto;
        d.NemotecnicoSVS=src.NemotecnicoSVS; d.Nemotecnico=src.Nemotecnico; d.NemotecnicoSubyacente=src.NemotecnicoSubyacente; d.NemotecnicoRelacionado=src.NemotecnicoRelacionado;
        d.ProcesaAutomatico=src.ProcesaAutomatico; d.Plazo=src.Plazo; d.PrecioPlazo=src.PrecioPlazo; d.Precio=src.Precio; d.Reajustes=src.Reajustes;
        d.Tasa=src.Tasa; d.TipoInteres=src.TipoInteres; d.TipoCambioCorresponsal=src.TipoCambioCorresponsal;
        d.ValorTipoCambio=src.ValorTipoCambio; d.ValorParidad=src.ValorParidad; d.ValorRescate=src.ValorRescate;
        d.IdCamara=src.IdCamara; d.EsRuteo=src.EsRuteo; d.IdUsuarioReserva=src.IdUsuarioReserva; d.Reservado=src.Reservado;
        d.BolsaEsExtranjera=src.BolsaEsExtranjera; d.FlujoOrigen=src.FlujoOrigen;
        d.PrecioRaiz=src.PrecioRaiz; d.MontoRaiz=src.MontoRaiz; d.IdMonedaRaiz=src.IdMonedaRaiz; d.IdInstrumentoNegociacion=src.IdInstrumentoNegociacion;
        d.TipoMensaje=src.TipoMensaje; d.EsAsignacionDirecta=src.EsAsignacionDirecta; d.MarcaNegociacionCorrecta=src.MarcaNegociacionCorrecta;
        d.EsModificacionMarketData=src.EsModificacionMarketData; d.EstadoModificacion=src.EstadoModificacion;
        d.IdTransaccionHistoricoAnulacion=src.IdTransaccionHistoricoAnulacion; d.MotivoReserva=src.MotivoReserva;
        return d;
    }

    private boolean esTipoSim(Transaccion_Proceso t) {
        return TIPO_OPERACION_COMSIM.equalsIgnoreCase(t.IdTipoOperacion) || TIPO_OPERACION_VENSIM.equalsIgnoreCase(t.IdTipoOperacion)
            || TIPO_OPERACION_COMRRV.equalsIgnoreCase(t.IdTipoOperacion) || TIPO_OPERACION_VENRRV.equalsIgnoreCase(t.IdTipoOperacion)
            || TIPO_OPERACION_COMRRF.equalsIgnoreCase(t.IdTipoOperacion) || TIPO_OPERACION_VENRRF.equalsIgnoreCase(t.IdTipoOperacion);
    }

    private JsonObject toJson(Salida s) {
        JsonObject o = new JsonObject();
        o.addProperty("P_OUT_ERRORESCONTADOR", s.P_OUT_ERRORESCONTADOR);
        JsonArray msgs = new JsonArray();
        for (MensajeSalida m : s.P_OUT_MENSAJES) { JsonObject mo = new JsonObject(); mo.addProperty("IdInterno",m.IdInterno); mo.addProperty("tipo",m.tipo); mo.addProperty("codigo",m.codigo); mo.addProperty("descripcion",m.descripcion); msgs.add(mo); }
        o.add("P_OUT_MENSAJES", msgs);
        JsonArray res = new JsonArray();
        for (ResultadoSalida r : s.P_OUT_RESULTADO) { JsonObject ro = new JsonObject(); ro.addProperty("IdInterno",r.IdInterno); ro.addProperty("IdTransaccion",r.IdTransaccion); ro.addProperty("IdTipoOperacion",r.IdTipoOperacion); ro.addProperty("EsAPlazo",r.EsAPlazo); res.add(ro); }
        o.add("P_OUT_RESULTADO", res);
        return o;
    }

    private static long toLongFolio(String folio) {
        if (folio == null || folio.isBlank()) return 0L;
        try { return Long.parseLong(folio.trim()); } catch (Exception e) { return 0L; }
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String sql(String path) {
        try (var is = CrearTransaccionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
