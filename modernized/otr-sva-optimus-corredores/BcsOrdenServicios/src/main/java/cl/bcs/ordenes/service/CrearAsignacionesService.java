package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Migrates cl.bcs.ordenes.CrearAsignaciones (2434-line legacy JavaCall).
 *
 * Assigns orders to portfolios/accounts. Supports CL and PE localization,
 * all instrument types (RV/RF/IF/DV/RE) and plazo operations.
 * Cross-service calls (CalcularParidad, CalcularCobros, ValidarLimites,
 * CrearEventoAutorizacion) go to BcsPlataformaServicios via PlataformaCallClient.
 */
@Service
public class CrearAsignacionesService {

    private static final Logger log = LoggerFactory.getLogger(CrearAsignacionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    // ── Instrument classifications ───────────────────────────────────────────
    private static final String CL_RV = "RV", CL_RF = "RF", CL_IF = "IF",
                                 CL_RE = "RE", CL_DV = "DV";
    // ── Assignment states ────────────────────────────────────────────────────
    private static final String EST_NO_ENVIADA = "NO_ENVIADA";
    private static final String EST_PEND_AUT   = "PENDIENTE_AUTORIZACION";
    // ── Tipo operacion ───────────────────────────────────────────────────────
    private static final String OP_COMSIM = "COMSIM", OP_VENSIM = "VENSIM";
    private static final String OP_COMRRF = "COMRRF", OP_VENRRF = "VENRRF";
    private static final String OP_COMRRV = "COMRRV", OP_VENRRV = "VENRRV";
    // ── Message types ────────────────────────────────────────────────────────
    private static final String M_ERROR = "ERROR", M_INFO = "INFORMACION", M_WARN = "ADVERTENCIA";

    // ── SQL ──────────────────────────────────────────────────────────────────
    private final String SQL_SELECT_USUARIO_SISTEMA;
    private final String SQL_SELECT_ESTADO_MODULO;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_TRANSACCIONES_PLAZO;
    private final String SQL_SELECT_ASIGNACIONES_OD;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_COBRO_ORDENES;
    private final String SQL_SELECT_COBROS_RANGO;
    private final String SQL_INSERT_ASOCIACIONES;
    private final String SQL_SELECT_ASOCIACIONES;
    private static final String SQL_DELETE_ASOCIACIONES =
        "DELETE FROM \"BSCL.Orden.BD::ORD.CrearAsignaciones_Proceso\" WHERE 1=1";
    private final String SQL_INSERT_ASIGNACIONES;
    private final String SQL_INSERT_COBROS;

    private final NamedParameterJdbcTemplate jdbc;
    private final PlataformaCallClient plataforma;
    private final RequisitosBasicosUtil requisitos;
    private final ConfiguracionSistemaUtil config;

    public CrearAsignacionesService(NamedParameterJdbcTemplate jdbc,
                                    PlataformaCallClient plataforma,
                                    RequisitosBasicosUtil requisitos,
                                    ConfiguracionSistemaUtil config) {
        this.jdbc       = jdbc;
        this.plataforma = plataforma;
        this.requisitos = requisitos;
        this.config     = config;
        this.SQL_SELECT_USUARIO_SISTEMA  = load("cl/bcs/ordenes/sql/Globales_SelectUsuarioSistema.sql");
        this.SQL_SELECT_ESTADO_MODULO    = load("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_SELECT_TRANSACCIONES    = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectTransacciones.sql");
        this.SQL_SELECT_TRANSACCIONES_PLAZO = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectTransaccionesPlazo.sql");
        this.SQL_SELECT_ASIGNACIONES_OD  = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectAsignacionesOd.sql");
        this.SQL_SELECT_ORDENES          = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectOrdenes.sql");
        this.SQL_SELECT_COBRO_ORDENES    = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobros.sql");
        this.SQL_SELECT_COBROS_RANGO     = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectCobrosRango.sql");
        this.SQL_INSERT_ASOCIACIONES     = load("cl/bcs/ordenes/sql/CrearAsignaciones_InsertAsociaciones.sql");
        this.SQL_SELECT_ASOCIACIONES     = load("cl/bcs/ordenes/sql/CrearAsignaciones_SelectAsociaciones.sql");
        this.SQL_INSERT_ASIGNACIONES     = load("cl/bcs/ordenes/sql/CrearAsignaciones_InsertAsignaciones.sql");
        this.SQL_INSERT_COBROS           = load("cl/bcs/ordenes/sql/CrearAsignaciones_InsertCobrosAsignacion.sql");
    }

    private static String load(String path) {
        try (var is = CrearAsignacionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load SQL: " + path, e);
        }
    }

    // ─── POJOs ───────────────────────────────────────────────────────────────

    public static class AsignacionEntrada {
        public Long IdInterno;
        public Long IdOrden;
        public Long IdTransaccion;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public BigDecimal ValorRescate = BigDecimal.ZERO;
        public Long FolioCartera = 0L;
        public String CodigoIFRS = "";
        public String IdClasificacionTipoInstrumento = "";
        public String FlujoOrigen = "INGRESOORDEN";
        public Timestamp FechaTrade;
        public String DocumentoIdentidad = "";
        public String NombreCompleto = "";
        public BigDecimal Monto = BigDecimal.ZERO;
        public String GlosaRefactura = "";
        public String FechaOperacionOriginal = "";
    }

    public static class Mensaje {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
        Mensaje(Long id, String tipo, String codigo, String desc) {
            this.id = id; this.tipo = tipo; this.codigo = codigo; this.descripcion = desc;
        }
    }

    private static class AsigProceso {
        Long IdAsignacion;
        Long IdInterno;
        Long IdOrden;
        Long IdTransaccion;
        BigDecimal Cantidad = BigDecimal.ZERO;
        BigDecimal ValorRescate = BigDecimal.ZERO;
        Long FolioCartera = 0L;
        String CodigoIFRS = "";
        String FlujoOrigen = "INGRESOORDEN";
        Timestamp FechaAsignacion;
        String EsAPlazo = "N";
        String IdEstadoAsignacion = EST_NO_ENVIADA;
        Long IdAsignacionGrupo;
        Long IdAsignacionRef;
        BigDecimal Monto = BigDecimal.ZERO;
        BigDecimal MontoAsignadoLiquidacion = BigDecimal.ZERO;
        BigDecimal MontoAsignadoLocal = BigDecimal.ZERO;
        BigDecimal MontoAsignadoInteresContado = BigDecimal.ZERO;
        BigDecimal MontoAsignadoInteresPlazo = BigDecimal.ZERO;
        BigDecimal MontoAsignadoPlazo = BigDecimal.ZERO;
        BigDecimal PrecioPlazo = BigDecimal.ZERO;
        BigDecimal Tasa = BigDecimal.ZERO;
        BigDecimal TasaPromedio = BigDecimal.ZERO;
        BigDecimal TipoCambio = BigDecimal.ZERO;
        BigDecimal Paridad = BigDecimal.ZERO;
        String FechaTipoCambio = "";
        String FechaParidad = "";
        String GlosaRefactura = "";
        String FechaOperacionOriginal = "";
        String DocumentoIdentidad = "";
        String NombreCompleto = "";
        int errores = 0;
        boolean odMismoPortafolio = false;
        List<CobroAsigProceso> cobros = new ArrayList<>();
    }

    private static class TranProceso {
        Long IdTransaccion;
        String IdTipoOperacion;
        String IdInstrumento;
        String IdLibroOrdenes;
        BigDecimal CantidadDisponible = BigDecimal.ZERO;
        BigDecimal MontoDisponible = BigDecimal.ZERO;
        BigDecimal ValorRescateDisponible = BigDecimal.ZERO;
        String IdCondicionDeLiquidacion = "";
        String IdBolsa = "";
        String FechaOperacion = "";
        BigDecimal Precio = BigDecimal.ZERO;
        BigDecimal Tasa = BigDecimal.ZERO;
        String EsVentaCorta = "N";
        String IdEstado = "";
        String EsFungible = "N";
        BigDecimal CorteMinimo = BigDecimal.ZERO;
        BigDecimal Cantidad = BigDecimal.ZERO;
        BigDecimal Monto = BigDecimal.ZERO;
        BigDecimal ValorRescate = BigDecimal.ZERO;
        String IdInstrumentoSubyacente = "";
        String IdClasificacionTipoInstrumento = "";
        Long Tamano = 1L;
        Long IdTransaccionPlazo;
        String EsAPlazo = "N";
        String IdMoneda = "";
        String AccionOrden = "";
        String IdTipoInstrumento = "";
        Long IdCorredoraCorresponsal;
        String Bloqueado = "N";
        String Reservado = "N";
        Long IdUsuarioReserva;
        Long IdTransaccionOd;
        BigDecimal Intereses = BigDecimal.ZERO;
        String FechaVencimiento = "";
        String Folio = "";
        String NemoSVS = "";
        String Nemotecnico = "";
        // Running totals
        BigDecimal CantidadTotalAsignada = BigDecimal.ZERO;
        BigDecimal MontoTotalAsignado = BigDecimal.ZERO;
        BigDecimal ValorRescateTotalAsignado = BigDecimal.ZERO;
    }

    private static class OrdenProceso {
        Long IdOrden;
        String IdTipoOperacion;
        String IdTipoProducto;
        String IdTipoPrecio;
        String EsVentaCorta = "N";
        BigDecimal PrecioOrden = BigDecimal.ZERO;
        Long Tamano = 1L;
        Long IdPortafolio;
        BigDecimal CantidadDisponible = BigDecimal.ZERO;
        BigDecimal MontoDisponible = BigDecimal.ZERO;
        BigDecimal ValorRescateDisponible = BigDecimal.ZERO;
        String IdTipoCanalIngreso = "";
        String IdEstadoOrden = "";
        String IdMonedaOrden = "";
        String IdMonedaLiquidacionOrden = "";
        BigDecimal Cantidad = BigDecimal.ZERO;
        Long IdGrupoCobro;
        String IdInstrumento = "";
        String IdCondicionDeLiquidacion = "";
        String IdBolsa = "";
        String FechaOperacion = "";
        String IdClasificacionTipoInstrumento = "";
        BigDecimal TipoCambio = BigDecimal.ZERO;
        String IdLibroOrdenes = "";
        BigDecimal ValorRescate = BigDecimal.ZERO;
        String AccionOrden = "";
        String IdInstrumentoSubyacente = "";
        String IdTipoInstrumento = "";
        Long PlazoSimultanea = 0L;
        String EsFungible = "N";
        Long BaseTasa = 360L;
        Long PlazoReporte = 0L;
        BigDecimal MontoInteresContado = BigDecimal.ZERO;
        BigDecimal MontoInteresPlazo = BigDecimal.ZERO;
        BigDecimal PorcentajeCobros = BigDecimal.ZERO;
        BigDecimal TasaReporte = BigDecimal.ZERO;
        BigDecimal TasaPromedioReporte = BigDecimal.ZERO;
        String TipoInteres = "";
        Long IdCorredorCorresponsal;
        String EsCuentaPropia = "N";
        String PermiteAsignarOEODMismoRut = "N";
        Long IdCliente;
        // Running totals
        BigDecimal CantidadTotalAsignada = BigDecimal.ZERO;
        BigDecimal MontoTotalAsignado = BigDecimal.ZERO;
        BigDecimal ValorRescateTotalAsignado = BigDecimal.ZERO;
        List<CobroOrdenProceso> cobrosOrden = new ArrayList<>();
    }

    private static class CobroOrdenProceso {
        Long IdOrden;
        Long IdCobroOrden;
        String IdCobro;
        BigDecimal ValorCobroPerfil = BigDecimal.ZERO;
        BigDecimal ValorCobroAplicado = BigDecimal.ZERO;
        BigDecimal MontoMinimo = BigDecimal.ZERO;
        String IdMoneda = "";
        String FormaCobro = "";
        String CodigoCobroClasificacion = "";
        String CodigoCobroClasificacionBase = "";
        Long IdGrupoCobro;
        BigDecimal ValorCobroFinal = BigDecimal.ZERO;
        String PermiteCambiarValor = "S";
        // Peru fields
        String _CheckCobroMonedaOperacion = "N";
        String _IdMonedaCobroOtraMoneda;
        BigDecimal _MontoFijo_MontoCobro = BigDecimal.ZERO;
        String _MontoFijo_CheckSiNo = "N";
        BigDecimal _PorcentualFijo_ValorCobro = BigDecimal.ZERO;
        String _PorcentualFijo_CheckSobreMonto = "N";
        String _Otros_CheckOperacionContado = "S";
        String _Otros_CheckOperacionPlazo = "N";
        String _Otros_CheckAllIn = "N";
        String _Otros_CheckInteresCompuesto = "N";
        String _Otros_CheckInteresSimple = "N";
        String _PorcentualRango_IdMonedaRangoMonto;
        String _PorcentualRango_IdTipoRangoCobro;
        List<Map<String,Object>> RangoCobros = new ArrayList<>();
    }

    private static class CobroAsigProceso {
        Long IdInternoCobro;
        Long IdAsignacion;
        Long IdCobroAsignacion;
        Timestamp FechaIngresoCobro;
        String IdCobro;
        BigDecimal ValorCobroPerfil = BigDecimal.ZERO;
        BigDecimal ValorCobroAplicado = BigDecimal.ZERO;
        BigDecimal MontoMinimo = BigDecimal.ZERO;
        String IdMoneda = "";
        String FormaCobro = "";
        String CodigoCobroClasificacion = "";
        BigDecimal MontoAsignacionMonedaCobro = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculado = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLocal = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
        String PermiteCambiarValor = "S";
        BigDecimal ValorCobroFinal = BigDecimal.ZERO;
        String _CheckCobroMonedaOperacion = "N";
        String _IdMonedaCobroOtraMoneda;
        String _Otros_CheckOperacionContado = "S";
        String _Otros_CheckOperacionPlazo = "N";
        List<Map<String,Object>> _RangoCobros = new ArrayList<>();
    }

    private static class AsocOD {
        Long IdAsignacion;
        Long IdOrden;
        Long IdPortafolio;
        Long IdTransaccion;
        Long IdTransaccionOd;
        String IdClasificacionTipoInstrumento = "";
    }

    // ─── Entry point ─────────────────────────────────────────────────────────

    @Transactional
    public JsonObject execute(JsonObject params) {
        long t0 = System.currentTimeMillis();

        // parse input
        Long idEmpresa = params.has("p_IdEmpresa")  ? params.get("p_IdEmpresa").getAsLong()   : 0L;
        Long idUsuario = params.has("p_IdUsuario")  ? params.get("p_IdUsuario").getAsLong()   : 0L;
        String idModulo = params.has("p_IdModulo")  ? params.get("p_IdModulo").getAsString()  : "";
        String simular  = params.has("p_simularOperacion") ? params.get("p_simularOperacion").getAsString() : "N";
        String usaRN    = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";
        String origenAsig = params.has("p_OrigenAsig") ? params.get("p_OrigenAsig").getAsString() : "";
        String aplicarComision = params.has("p_AplicarComision") ? params.get("p_AplicarComision").getAsString() : "S";
        String esMasivaPP = params.has("p_EsAsignacionMasivaPrecioPromedio") ? params.get("p_EsAsignacionMasivaPrecioPromedio").getAsString() : "N";
        String usaLogDetallado = params.has("p_UsaLogDetallado") ? params.get("p_UsaLogDetallado").getAsString() : "N";

        JsonArray asignacionesArr = params.has("p_Asignaciones") ? params.get("p_Asignaciones").getAsJsonArray() : new JsonArray();
        List<AsignacionEntrada> entradas = new ArrayList<>();
        for (JsonElement el : asignacionesArr) {
            entradas.add(GSON.fromJson(el, AsignacionEntrada.class));
        }

        List<Mensaje> mensajes = new ArrayList<>();
        List<JsonObject> resultados = new ArrayList<>();
        List<JsonObject> cobros = new ArrayList<>();

        // 1. Requisitos básicos
        {
            RequisitosBasicosUtil.Salida reqSalida = requisitos.validar(idEmpresa, idUsuario, idModulo);
            if (reqSalida.contarErrores() > 0) {
                reqSalida.p_out_Mensajes.forEach(ms ->
                    mensajes.add(new Mensaje(ms.IdInterno, ms.tipo, ms.codigo, ms.descripcion)));
                return buildOut(mensajes, resultados, cobros);
            }
        }

        // 2. Input validations
        if (entradas.isEmpty()) {
            mensajes.add(new Mensaje(0L, M_ERROR, "NO_SE_INGRESARON_ASIGNACIONES", "No se ingresaron asignaciones"));
            return buildOut(mensajes, resultados, cobros);
        }

        Set<Long> correlativos = new HashSet<>();
        for (AsignacionEntrada a : entradas) {
            if (a.IdInterno != null && !correlativos.add(a.IdInterno)) {
                mensajes.add(new Mensaje(0L, M_ERROR, "CORRELATIVO_INCORRECTO", "Correlativo duplicado: " + a.IdInterno));
                return buildOut(mensajes, resultados, cobros);
            }
        }

        // Módulo ORDENES state
        List<Map<String,Object>> modulos = jdbc.queryForList(SQL_SELECT_ESTADO_MODULO,
            Map.of("IdModulo", "ORDENES", "IdEmpresa", idEmpresa));
        if (modulos.isEmpty()) {
            mensajes.add(new Mensaje(0L, M_ERROR, "MODULO_NO_EXISTE", "Módulo ORDENES no existe para empresa " + idEmpresa));
            return buildOut(mensajes, resultados, cobros);
        }
        String estadoModulo = str(modulos.get(0), "IdEstadoModulo");
        if ("CERRADO".equalsIgnoreCase(estadoModulo)) {
            mensajes.add(new Mensaje(0L, M_ERROR, "MODULO_CERRADO", "Módulo ORDENES cerrado"));
            return buildOut(mensajes, resultados, cobros);
        }

        // 3. Configuration
        String localizacion = config.getParam(idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
        String fechaSistema = config.getParam(idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String idMonedaSistema = config.getParam(idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

        Long idUsuarioSistema = 0L;
        List<Map<String,Object>> usrSis = jdbc.queryForList(SQL_SELECT_USUARIO_SISTEMA, Map.of("IdEmpresa", idEmpresa));
        if (!usrSis.isEmpty()) idUsuarioSistema = longOf(usrSis.get(0), "IdUsuario");

        // 4. INSERT asociaciones temp + SELECT back
        try {
            if (!entradas.isEmpty()) {
                List<MapSqlParameterSource> asocParams = entradas.stream()
                    .map(a -> new MapSqlParameterSource()
                        .addValue("IdOrden", a.IdOrden)
                        .addValue("IdTransaccion", a.IdTransaccion)
                        .addValue("Cantidad", a.Cantidad))
                    .collect(Collectors.toList());
                jdbc.batchUpdate(SQL_INSERT_ASOCIACIONES, asocParams.toArray(new MapSqlParameterSource[0]));
            }

            List<Map<String,Object>> asociaciones = jdbc.queryForList(SQL_SELECT_ASOCIACIONES, Map.of());

            // 5. Get sequences for asignaciones
            List<Long> seqAsig = getSecuencia("BSCL.Orden.BD.Secuencias::Asignacion", (long) entradas.size());

            // 6. Build AsigProceso list
            Timestamp fechaAsignacion = new Timestamp(Instant.now().toEpochMilli());
            List<AsigProceso> asignaciones = new ArrayList<>();
            Map<Long, AsigProceso> asigHash = new HashMap<>();

            for (int i = 0; i < entradas.size(); i++) {
                AsignacionEntrada e = entradas.get(i);
                AsigProceso ap = new AsigProceso();
                ap.IdAsignacion = seqAsig.get(i);
                ap.IdInterno = e.IdInterno;
                ap.IdOrden = e.IdOrden;
                ap.IdTransaccion = e.IdTransaccion;
                ap.Cantidad = e.Cantidad != null ? e.Cantidad : BigDecimal.ZERO;
                ap.ValorRescate = e.ValorRescate != null ? e.ValorRescate : BigDecimal.ZERO;
                ap.FolioCartera = e.FolioCartera != null ? e.FolioCartera : 0L;
                ap.CodigoIFRS = e.CodigoIFRS != null ? e.CodigoIFRS : "";
                ap.FlujoOrigen = e.FlujoOrigen != null ? e.FlujoOrigen : "INGRESOORDEN";
                ap.GlosaRefactura = e.GlosaRefactura != null ? e.GlosaRefactura : "";
                ap.FechaOperacionOriginal = e.FechaOperacionOriginal != null ? e.FechaOperacionOriginal : "";
                ap.DocumentoIdentidad = e.DocumentoIdentidad != null ? e.DocumentoIdentidad : "";
                ap.NombreCompleto = e.NombreCompleto != null ? e.NombreCompleto : "";
                ap.IdEstadoAsignacion = EST_NO_ENVIADA;
                ap.EsAPlazo = "N";
                ap.FechaAsignacion = "INGRESOINSTRUCCION".equals(e.FlujoOrigen) && e.FechaTrade != null
                    ? new Timestamp(e.FechaTrade.getTime()) : fechaAsignacion;
                if (!asociaciones.isEmpty()) ap.IdAsignacionGrupo = e.IdOrden;
                asignaciones.add(ap);
                asigHash.put(ap.IdAsignacion, ap);
            }

            // 7. Validate basic fields
            List<AsigProceso> errList = new ArrayList<>();
            for (AsigProceso ap : asignaciones) {
                if (ap.IdOrden == null) {
                    mensajes.add(new Mensaje(ap.IdInterno, M_ERROR, "NO_SE_ENVIO_ORDEN_DE_ASIGNACION", "IdOrden requerido"));
                    ap.errores++;
                }
                if (ap.IdTransaccion == null) {
                    mensajes.add(new Mensaje(ap.IdInterno, M_ERROR, "NO_SE_ENVIO_TRANSACCION_DE_ASIGNACION", "IdTransaccion requerido"));
                    ap.errores++;
                }
            }
            removeErrors(asignaciones, errList);
            if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);

            // 8. SELECT transacciones
            Map<Long, TranProceso> transacciones = new HashMap<>();
            Map<Long, TranProceso> transaccionesOd = new HashMap<>();
            {
                List<Map<String,Object>> rows = jdbc.queryForList(SQL_SELECT_TRANSACCIONES, Map.of());
                List<Long> idsPlazo = new ArrayList<>(), idsOd = new ArrayList<>();
                for (Map<String,Object> r : rows) {
                    TranProceso tp = mapTran(r);
                    transacciones.put(tp.IdTransaccion, tp);
                    if (tp.IdTransaccionPlazo != null && tp.IdTransaccionPlazo > 0) idsPlazo.add(tp.IdTransaccionPlazo);
                    if (tp.IdTransaccionOd != null && tp.IdTransaccionOd > 0) idsOd.add(tp.IdTransaccionOd);
                }
                if (!idsPlazo.isEmpty()) {
                    List<Map<String,Object>> plazoRows = jdbc.queryForList(SQL_SELECT_TRANSACCIONES_PLAZO,
                        new MapSqlParameterSource("idsTransaccion", idsPlazo));
                    for (Map<String,Object> r : plazoRows) { TranProceso tp = mapTran(r); transacciones.put(tp.IdTransaccion, tp); }
                }
                if (!idsOd.isEmpty()) {
                    List<Map<String,Object>> odRows = jdbc.queryForList(SQL_SELECT_TRANSACCIONES_PLAZO,
                        new MapSqlParameterSource("idsTransaccion", idsOd));
                    for (Map<String,Object> r : odRows) {
                        TranProceso tp = mapTran(r);
                        transaccionesOd.put(tp.IdTransaccionOd != null ? tp.IdTransaccionOd : tp.IdTransaccion, tp);
                        transacciones.put(tp.IdTransaccion, tp);
                    }
                }
            }

            // OD asociaciones extra asignaciones
            List<Long> seqAsoc = getSecuencia("BSCL.Orden.BD.Secuencias::Asignacion", (long) asociaciones.size());
            for (int i = 0; i < asociaciones.size(); i++) {
                Map<String,Object> asoc = asociaciones.get(i);
                Long idOrdenN = longOf(asoc, "IdOrdenN");
                Long idOrdenM = longOf(asoc, "IdOrdenM");
                BigDecimal cantAsoc = bdOf(asoc, "Cantidad");
                TranProceso tranOd = transaccionesOd.get(idOrdenN);
                if (tranOd == null) continue;
                AsigProceso asigOd = new AsigProceso();
                asigOd.IdAsignacion = seqAsoc.get(i);
                asigOd.IdInterno = idOrdenM;
                asigOd.IdOrden = idOrdenM;
                asigOd.IdTransaccion = tranOd.IdTransaccion;
                asigOd.Cantidad = cantAsoc;
                asigOd.ValorRescate = BigDecimal.ZERO;
                asigOd.EsAPlazo = "N";
                asigOd.FechaAsignacion = fechaAsignacion;
                asigOd.IdEstadoAsignacion = EST_NO_ENVIADA;
                asigOd.IdAsignacionGrupo = idOrdenN;
                asignaciones.add(asigOd);
                asigHash.put(asigOd.IdAsignacion, asigOd);
            }

            // 9. SELECT ordenes
            Map<Long, OrdenProceso> ordenes = new HashMap<>();
            List<Map<String,Object>> ordRows = jdbc.queryForList(SQL_SELECT_ORDENES, Map.of());
            for (Map<String,Object> r : ordRows) { OrdenProceso op = mapOrden(r); ordenes.put(op.IdOrden, op); }

            // 10. Build plazo asignaciones
            List<Long> seqPlazo = null;
            List<AsigProceso> asigPlazo = new ArrayList<>();
            for (AsigProceso ap : asignaciones) {
                TranProceso tran = transacciones.get(ap.IdTransaccion);
                if (tran == null || tran.IdTransaccionPlazo == null || tran.IdTransaccionPlazo <= 0) continue;
                OrdenProceso ord = ordenes.get(ap.IdOrden);
                if (seqPlazo == null) seqPlazo = getSecuencia("BSCL.Orden.BD.Secuencias::Asignacion", (long) asignaciones.size());
                AsigProceso plazo = copyAsig(ap);
                plazo.EsAPlazo = "S";
                plazo.IdTransaccion = tran.IdTransaccionPlazo;
                plazo.IdAsignacion = seqPlazo.get(asignaciones.indexOf(ap));
                plazo.IdAsignacionRef = ap.IdAsignacion;
                if (ord != null) plazo.Tasa = ord.TasaReporte;
                ap.IdAsignacionRef = plazo.IdAsignacion;
                asigPlazo.add(plazo);
                asigHash.put(plazo.IdAsignacion, plazo);
            }
            asignaciones.addAll(asigPlazo);

            // OD same-portafolio tracking
            List<AsocOD> asocODList = new ArrayList<>();
            {
                List<Long> txs = new ArrayList<>();
                for (AsigProceso ap : asignaciones) {
                    TranProceso tran = transacciones.get(ap.IdTransaccion);
                    OrdenProceso ord = ordenes.get(ap.IdOrden);
                    if (tran == null || tran.IdTransaccionOd == null || (ord != null && "S".equalsIgnoreCase(ord.PermiteAsignarOEODMismoRut))) continue;
                    AsocOD aod = new AsocOD();
                    aod.IdAsignacion = ap.IdAsignacion; aod.IdOrden = ap.IdOrden;
                    aod.IdPortafolio = ord != null ? ord.IdPortafolio : null;
                    aod.IdTransaccion = ap.IdTransaccion; aod.IdTransaccionOd = tran.IdTransaccionOd;
                    aod.IdClasificacionTipoInstrumento = tran.IdClasificacionTipoInstrumento;
                    txs.add(tran.IdTransaccionOd);
                    asocODList.add(aod);
                }
                if (!txs.isEmpty()) {
                    List<Map<String,Object>> odRows2 = jdbc.queryForList(SQL_SELECT_ASIGNACIONES_OD, Map.of());
                    for (Map<String,Object> r : odRows2) {
                        AsocOD aod2 = new AsocOD();
                        aod2.IdTransaccion = longOf(r,"IdTransaccion"); aod2.IdTransaccionOd = longOf(r,"IdTransaccionOd");
                        aod2.IdPortafolio = longOf(r,"IdPortafolio"); aod2.IdClasificacionTipoInstrumento = str(r,"IdClasificacionTipoInstrumento");
                        asocODList.add(aod2);
                    }
                    Map<Long, List<AsocOD>> odByTx = asocODList.stream().collect(Collectors.groupingBy(a -> a.IdTransaccion));
                    for (AsocOD aod : asocODList) {
                        if (aod.IdTransaccionOd == null) continue;
                        List<AsocOD> same = odByTx.getOrDefault(aod.IdTransaccionOd, List.of());
                        for (AsocOD aod2 : same) {
                            if (aod == aod2) continue;
                            if (!"DV".equalsIgnoreCase(aod.IdClasificacionTipoInstrumento)
                                && aod.IdTransaccionOd.equals(aod2.IdTransaccion)
                                && aod.IdPortafolio != null && aod.IdPortafolio.equals(aod2.IdPortafolio)) {
                                AsigProceso real = asigHash.get(aod.IdAsignacion);
                                if (real != null) real.odMismoPortafolio = true;
                            }
                        }
                    }
                }
            }

            // 11. Business validations
            {
                String asigCronologica = config.getParam(idEmpresa, "ORDENES", "AsignacionCronologicaObligatoria");
                String permitePalosReservado = config.getParam(idEmpresa, "FACTURACION", "PALOS_NEGOCIACION_RESERVADO");
                for (AsigProceso ap : asignaciones) {
                    TranProceso tran = transacciones.get(ap.IdTransaccion);
                    OrdenProceso ord = ordenes.get(ap.IdOrden);
                    if (ord == null) { addErr(ap, mensajes, "ORDEN_NO_EXISTE", "Orden no existe"); continue; }
                    if (tran == null) { addErr(ap, mensajes, "TRANSACCION_NO_EXISTE", "Transacción no existe"); continue; }

                    if ("N".equalsIgnoreCase(ap.EsAPlazo)) {
                        boolean reservadoCheck = "S".equalsIgnoreCase(permitePalosReservado)
                            ? "S".equalsIgnoreCase(tran.Reservado) && !(tran.IdUsuarioReserva.equals(idUsuario) || idUsuario.equals(idUsuarioSistema))
                            : "S".equalsIgnoreCase(tran.Reservado) && !tran.IdUsuarioReserva.equals(idUsuario);
                        if (reservadoCheck) addErr(ap, mensajes, "TRANSACCION_RESERVADA", "Transacción reservada");
                        if ("S".equalsIgnoreCase(tran.Bloqueado)) addErr(ap, mensajes, "TRANSACCION_BLOQUEADA", "Transacción bloqueada");
                        if (!ord.IdTipoOperacion.equalsIgnoreCase(tran.IdTipoOperacion)) addErr(ap, mensajes, "TIPO_OPERACION_NO_COINCIDE", "Tipo operación no coincide");
                        if (!ord.IdInstrumento.equalsIgnoreCase(tran.IdInstrumento)) addErr(ap, mensajes, "INSTRUMENTO_NO_COINCIDE", "Instrumento no coincide");
                        if (!ord.IdCondicionDeLiquidacion.equalsIgnoreCase(tran.IdCondicionDeLiquidacion)) addErr(ap, mensajes, "COND_LIQ_NO_COINCIDE", "Condición liquidación no coincide");
                        if (!tran.IdMoneda.equalsIgnoreCase(ord.IdMonedaOrden)) addErr(ap, mensajes, "MONEDA_NO_COINCIDE", "Moneda no coincide");
                        if (!(ord.IdBolsa.equalsIgnoreCase(tran.IdBolsa) || "CUALQUIERA".equalsIgnoreCase(ord.IdBolsa))) addErr(ap, mensajes, "BOLSA_NO_COINCIDE", "Bolsa no coincide");
                        if (!ord.EsVentaCorta.equalsIgnoreCase(tran.EsVentaCorta)) addErr(ap, mensajes, "MARCA_VENTA_CORTA_NO_COINCIDE", "Marca venta corta no coincide");
                        if (ap.odMismoPortafolio) addErr(ap, mensajes, "ASIGNACION_OD_MISMO_PORTAFOLIO", "OD mismo portafolio");
                    }

                    if (CL_RV.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                        if (ap.Cantidad.compareTo(BigDecimal.ZERO) <= 0) addErr(ap, mensajes, "CANTIDAD_MENOR_IGUAL_CERO", "Cantidad <= 0");
                        if ("LIMITE".equalsIgnoreCase(ord.IdTipoPrecio) && "COMPRA".equalsIgnoreCase(ord.AccionOrden) && tran.Precio.compareTo(ord.PrecioOrden) > 0)
                            addErr(ap, mensajes, "PRECIO_NO_VALIDO", "Precio > límite compra");
                        if ("LIMITE".equalsIgnoreCase(ord.IdTipoPrecio) && "VENTA".equalsIgnoreCase(ord.AccionOrden) && tran.Precio.compareTo(ord.PrecioOrden) < 0)
                            addErr(ap, mensajes, "PRECIO_NO_VALIDO", "Precio < límite venta");
                    } else if (CL_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                        if (ap.ValorRescate.compareTo(BigDecimal.ZERO) <= 0) addErr(ap, mensajes, "VAL_RESC_MENOR_IGUAL_CERO", "ValorRescate <= 0");
                        if (tran.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
                            BigDecimal mod = ap.ValorRescate.remainder(tran.CorteMinimo);
                            if (mod.compareTo(BigDecimal.ZERO) != 0) addErr(ap, mensajes, "VALOR_RESCATE_NO_ES_MULTIPLO_CORTES", "ValorRescate no múltiplo corte");
                        }
                    } else {
                        if (ap.Cantidad.compareTo(BigDecimal.ZERO) <= 0) addErr(ap, mensajes, "CANTIDAD_MENOR_IGUAL_CERO", "Cantidad <= 0");
                        if (CL_RF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento) && tran.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
                            BigDecimal mod = ap.Cantidad.remainder(tran.CorteMinimo);
                            if (mod.compareTo(BigDecimal.ZERO) != 0) addErr(ap, mensajes, "CANTIDAD_NO_ES_MULTIPLO_CORTES", "Cantidad no múltiplo corte");
                        }
                    }

                    if (!CL_RE.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) || !"COMPRA".equalsIgnoreCase(ord.AccionOrden)) {
                        if (!"OFERTADA".equalsIgnoreCase(ord.IdEstadoOrden)) addErr(ap, mensajes, "ESTADO_ORDEN_INCORRECTO_ASIGNACION", "Estado orden incorrecto");
                    }
                    if (!"INGRESADA".equalsIgnoreCase(tran.IdEstado) && !"EN_PROCESO".equalsIgnoreCase(tran.IdEstado))
                        addErr(ap, mensajes, "ESTADO_TRANSACCION_INCORRECTO_ASIGNACION", "Estado transacción incorrecto");
                    if (!tran.IdLibroOrdenes.equals(ord.IdLibroOrdenes))
                        addErr(ap, mensajes, "CLIENTE_NO_PERTENECE_LIBRO_ORDENES", "Cliente no pertenece a libro órdenes");
                }
                removeErrors(asignaciones, errList);
                if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);
            }

            // 12. Calculate amounts
            for (AsigProceso ap : asignaciones) {
                TranProceso tran = transacciones.get(ap.IdTransaccion);
                if (tran == null) continue;
                if (CL_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    ap.Cantidad = "S".equalsIgnoreCase(tran.EsFungible) ? BigDecimal.ONE
                        : ap.ValorRescate.divide(tran.CorteMinimo.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : tran.CorteMinimo, 0, RoundingMode.FLOOR);
                }
                if (CL_RV.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    ap.Monto = tran.Precio.multiply(ap.Cantidad);
                } else if (CL_DV.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    ap.Monto = tran.Precio.multiply(ap.Cantidad).multiply(new BigDecimal(tran.Tamano));
                } else if (CL_RE.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    if ("RRF".equalsIgnoreCase(tran.IdTipoInstrumento)) ap.Monto = tran.Precio.divide(new BigDecimal(100), 20, RoundingMode.HALF_UP).multiply(ap.Cantidad);
                    else ap.Monto = tran.Precio.multiply(ap.Cantidad);
                } else if (CL_RF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    if (tran.Cantidad.compareTo(BigDecimal.ZERO) != 0)
                        ap.Monto = tran.Monto.multiply(ap.Cantidad).divide(tran.Cantidad, 20, RoundingMode.HALF_UP);
                } else if (CL_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                    if (tran.ValorRescate.compareTo(BigDecimal.ZERO) != 0)
                        ap.Monto = tran.Monto.multiply(ap.ValorRescate).divide(tran.ValorRescate, 20, RoundingMode.HALF_UP);
                }
                if (!CL_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) ap.ValorRescate = BigDecimal.ZERO;
            }

            // 13. Plazo calculations (SIM/RRF)
            for (AsigProceso ap : asignaciones) {
                ap.PrecioPlazo = BigDecimal.ZERO; ap.MontoAsignadoPlazo = BigDecimal.ZERO; ap.Tasa = BigDecimal.ZERO;
                if (ap.IdAsignacionRef == null) continue;
                AsigProceso apPlazo = asigHash.get(ap.IdAsignacionRef);
                if (apPlazo == null) continue;
                TranProceso tranPlazo = transacciones.get(apPlazo.IdTransaccion);
                OrdenProceso ord = ordenes.get(ap.IdOrden);
                if (ord == null || tranPlazo == null) continue;
                if (ord.Cantidad.compareTo(BigDecimal.ZERO) != 0) {
                    ap.MontoAsignadoInteresPlazo = ap.Cantidad.multiply(ord.MontoInteresPlazo).divide(ord.Cantidad, 20, RoundingMode.HALF_UP);
                    ap.MontoAsignadoInteresContado = ap.Cantidad.multiply(ord.MontoInteresContado).divide(ord.Cantidad, 20, RoundingMode.HALF_UP);
                }
                if (OP_VENSIM.equalsIgnoreCase(tranPlazo.IdTipoOperacion)) {
                    ap.PrecioPlazo = tranPlazo.Precio.add(tranPlazo.Precio.multiply(tranPlazo.Tasa)
                        .divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP)
                        .divide(new BigDecimal(100), 20, RoundingMode.HALF_UP));
                    ap.MontoAsignadoPlazo = ap.Cantidad.multiply(ap.PrecioPlazo);
                } else if (OP_COMSIM.equalsIgnoreCase(tranPlazo.IdTipoOperacion)) {
                    ap.MontoAsignadoPlazo = ap.Monto.multiply(tranPlazo.Tasa)
                        .divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP)
                        .divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                } else if ("COMPUESTO".equalsIgnoreCase(ord.TipoInteres)) {
                    BigDecimal tasa = ord.TasaPromedioReporte.divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                    BigDecimal periodos = new BigDecimal(ord.PlazoReporte).divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP);
                    double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(), periodos.doubleValue());
                    if (OP_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion) || OP_COMRRV.equalsIgnoreCase(ord.IdTipoOperacion)) {
                        ap.PrecioPlazo = ord.PrecioOrden.multiply(new BigDecimal(factor));
                        ap.MontoAsignadoPlazo = ap.Cantidad.multiply(ap.PrecioPlazo);
                    } else if (OP_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion) || OP_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion)) {
                        BigDecimal montoMasInteres = ap.Monto.add(ap.MontoAsignadoInteresContado);
                        BigDecimal montoFactor = montoMasInteres.multiply(new BigDecimal(factor));
                        ap.PrecioPlazo = ap.Cantidad.compareTo(BigDecimal.ZERO) != 0
                            ? (montoFactor.subtract(ap.MontoAsignadoInteresPlazo)).divide(ap.Cantidad, 20, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                        ap.MontoAsignadoPlazo = ap.PrecioPlazo.multiply(ap.Cantidad);
                    }
                    ap.Tasa = ord.TasaReporte; ap.TasaPromedio = ord.TasaPromedioReporte;
                }
            }

            // 14. Totals
            for (AsigProceso ap : asignaciones) {
                TranProceso tran = transacciones.get(ap.IdTransaccion);
                OrdenProceso ord = ordenes.get(ap.IdOrden);
                if (tran == null) continue;
                tran.CantidadTotalAsignada = tran.CantidadTotalAsignada.add(ap.Cantidad);
                tran.MontoTotalAsignado = tran.MontoTotalAsignado.add(ap.Monto);
                tran.ValorRescateTotalAsignado = tran.ValorRescateTotalAsignado.add(ap.ValorRescate);
                if (ord != null && "N".equalsIgnoreCase(ap.EsAPlazo)) {
                    ord.CantidadTotalAsignada = ord.CantidadTotalAsignada.add(ap.Cantidad);
                    ord.MontoTotalAsignado = ord.MontoTotalAsignado.add(ap.Monto);
                    ord.ValorRescateTotalAsignado = ord.ValorRescateTotalAsignado.add(ap.ValorRescate);
                }
            }

            // 15. Sobreasignacion validation
            if (!OP_COMRRF.equalsIgnoreCase(origenAsig) && !"REASIGNACION".equalsIgnoreCase(origenAsig)) {
                Set<Long> ordSobra = new HashSet<>(), tranSobra = new HashSet<>();
                for (OrdenProceso ord : ordenes.values()) {
                    if (CL_DV.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) || CL_RF.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento)) {
                        if (ord.CantidadTotalAsignada.compareTo(ord.CantidadDisponible) > 0) ordSobra.add(ord.IdOrden);
                    } else if (CL_IF.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento)) {
                        if (ord.ValorRescateTotalAsignado.compareTo(ord.ValorRescateDisponible) > 0) ordSobra.add(ord.IdOrden);
                    } else if (CL_RV.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento) && OP_COMSIM.equalsIgnoreCase(ord.IdTipoOperacion)) {
                        if (ord.MontoTotalAsignado.compareTo(ord.MontoDisponible) > 0) ordSobra.add(ord.IdOrden);
                    } else if (CL_RV.equalsIgnoreCase(ord.IdClasificacionTipoInstrumento)) {
                        if (ord.CantidadTotalAsignada.compareTo(ord.CantidadDisponible) > 0) ordSobra.add(ord.IdOrden);
                    }
                }
                for (TranProceso tran : transacciones.values()) {
                    boolean byAmount = CL_RV.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento) && (OP_COMSIM.equalsIgnoreCase(tran.IdTipoOperacion) || OP_COMRRF.equalsIgnoreCase(tran.IdTipoOperacion) || OP_COMRRV.equalsIgnoreCase(tran.IdTipoOperacion));
                    if (CL_IF.equalsIgnoreCase(tran.IdClasificacionTipoInstrumento)) {
                        if (tran.ValorRescateTotalAsignado.compareTo(tran.ValorRescateDisponible) > 0) tranSobra.add(tran.IdTransaccion);
                    } else {
                        if (tran.CantidadTotalAsignada.compareTo(tran.CantidadDisponible) > 0) tranSobra.add(tran.IdTransaccion);
                    }
                }
                for (AsigProceso ap : asignaciones) {
                    if ("S".equalsIgnoreCase(ap.EsAPlazo)) continue;
                    TranProceso tran = transacciones.get(ap.IdTransaccion);
                    OrdenProceso ord = ordenes.get(ap.IdOrden);
                    if (tran != null && tranSobra.contains(tran.IdTransaccion)) { addErr(ap, mensajes, "TRANSACCION_SOBREASIGNADA", "Transacción sobreasignada"); }
                    if (ord != null && ordSobra.contains(ord.IdOrden)) { addErr(ap, mensajes, "ORDEN_SOBREASIGNADA", "Orden sobreasignada"); }
                }
                removeErrors(asignaciones, errList);
                if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);
            }

            // 16. Read cobros
            long idInternoCobro = 0L;
            Map<Long, CobroAsigProceso> cobrosHash = new HashMap<>();
            {
                List<Long> idsGrupoCobro = ordenes.values().stream().map(o -> o.IdGrupoCobro).filter(Objects::nonNull).distinct().collect(Collectors.toList());
                if (!idsGrupoCobro.isEmpty()) {
                    MapSqlParameterSource ps = new MapSqlParameterSource("idsGrupoCobro", idsGrupoCobro);
                    List<Map<String,Object>> cobrosOrd = jdbc.queryForList(SQL_SELECT_COBRO_ORDENES, ps);
                    List<Map<String,Object>> cobrosRango = jdbc.queryForList(SQL_SELECT_COBROS_RANGO, ps);
                    for (Map<String,Object> cr : cobrosOrd) {
                        CobroOrdenProceso cop = mapCobro(cr);
                        for (Map<String,Object> rr : cobrosRango) {
                            if (longOf(rr,"IdCobroOrden").equals(cop.IdCobroOrden) && longOf(rr,"IdOrden").equals(cop.IdOrden))
                                cop.RangoCobros.add(rr);
                        }
                        OrdenProceso ord = ordenes.get(cop.IdOrden);
                        if (ord != null) ord.cobrosOrden.add(cop);
                    }
                    for (AsigProceso ap : asignaciones) {
                        OrdenProceso ord = ordenes.get(ap.IdOrden);
                        if (ord == null) continue;
                        for (CobroOrdenProceso cop : ord.cobrosOrden) {
                            if ("CL".equalsIgnoreCase(localizacion) && "S".equalsIgnoreCase(ap.EsAPlazo)) continue;
                            if ("N".equalsIgnoreCase(ap.EsAPlazo)) {
                                CobroAsigProceso cob = copyCobro(cop);
                                cob.IdInternoCobro = idInternoCobro++;
                                cob.IdAsignacion = ap.IdAsignacion;
                                cob.ValorCobroAplicado = cop.ValorCobroFinal;
                                cob.FechaIngresoCobro = ap.FechaAsignacion;
                                cob._RangoCobros = new ArrayList<>(cop.RangoCobros);
                                ap.cobros.add(cob);
                                cobrosHash.put(cob.IdInternoCobro, cob);
                            }
                        }
                    }
                }
            }

            // 17. Sequences for cobros
            if (!cobrosHash.isEmpty()) {
                List<Long> seqCobro = getSecuencia("BSCL.Orden.BD.Secuencias::CobroAsignacion", (long) cobrosHash.size());
                int ci = 0;
                for (AsigProceso ap : asignaciones) {
                    for (CobroAsigProceso cob : ap.cobros) { cob.IdCobroAsignacion = seqCobro.get(ci++); }
                }
            }

            // 18. CalcularParidad — monto→moneda cobro (tarea 1001)
            callCalcularParidad(idEmpresa, fechaSistema, asignaciones, ordenes, cobrosHash, asigHash, mensajes, 1001L, "cobro");
            removeErrors(asignaciones, errList);
            if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);

            // 19. CalcularParidad — monto→liquidacion/local (tarea 2001)
            callCalcularParidadLiq(idEmpresa, fechaSistema, asignaciones, ordenes, asigHash, mensajes, idMonedaSistema);
            removeErrors(asignaciones, errList);
            if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);

            // 20. CalcularCobros
            if ("CL".equalsIgnoreCase(localizacion)) {
                callCalcularCobrosCL(idEmpresa, asignaciones, ordenes, transacciones, cobrosHash);
            } else if ("PE".equalsIgnoreCase(localizacion)) {
                callCalcularCobrosPE(idEmpresa, asignaciones, ordenes, cobrosHash);
            }

            // 21. CalcularParidad — cobros→liquidacion/local (tarea 3001)
            callCalcularParidadCobros(idEmpresa, fechaSistema, asignaciones, ordenes, cobrosHash, asigHash, mensajes, idMonedaSistema);
            removeErrors(asignaciones, errList);
            if (asignaciones.isEmpty()) return buildOut(mensajes, resultados, cobros);

            // 22. ValidarLimiteCuentaPropia
            Set<Long> pendAut = validarLimites(idEmpresa, idUsuario, idModulo, usaRN, asignaciones, ordenes, transacciones, mensajes);
            for (AsigProceso ap : asignaciones) {
                if (pendAut.contains(ap.IdInterno)) ap.IdEstadoAsignacion = EST_PEND_AUT;
            }

            // 23. INSERT asignaciones + cobros
            if ("N".equalsIgnoreCase(simular)) {
                List<MapSqlParameterSource> asigBatch = new ArrayList<>();
                List<MapSqlParameterSource> cobroBatch = new ArrayList<>();
                for (AsigProceso ap : asignaciones) {
                    OrdenProceso ord = ordenes.get(ap.IdOrden);
                    TranProceso tran = transacciones.get(ap.IdTransaccion);
                    MapSqlParameterSource ps = new MapSqlParameterSource()
                        .addValue("IdAsignacion", ap.IdAsignacion)
                        .addValue("FechaAsignacion", ap.FechaAsignacion)
                        .addValue("CantidadAsignada", ap.Cantidad)
                        .addValue("MontoAsignado", ap.Monto)
                        .addValue("MontoAsignadoLocal", ap.MontoAsignadoLocal)
                        .addValue("MontoAsignadoLiquidacion", ap.MontoAsignadoLiquidacion)
                        .addValue("TipoCambio", ap.TipoCambio)
                        .addValue("ValorRescateAsignado", ap.ValorRescate)
                        .addValue("IdEstadoAsignacion", ap.IdEstadoAsignacion)
                        .addValue("IdTransaccion", ap.IdTransaccion)
                        .addValue("IdOrden", ap.IdOrden)
                        .addValue("EsAPlazo", ap.EsAPlazo)
                        .addValue("IdLibroOrdenes", ord != null ? ord.IdLibroOrdenes : null)
                        .addValue("IdAsignacionRef", ap.IdAsignacionRef)
                        .addValue("FolioCartera", ap.FolioCartera)
                        .addValue("Paridad", ap.Paridad)
                        .addValue("FechaTipoCambio", ap.FechaTipoCambio)
                        .addValue("FechaParidad", ap.FechaParidad)
                        .addValue("CodigoIFRS", ap.CodigoIFRS)
                        .addValue("MontoAsignadoInteres", BigDecimal.ZERO)
                        .addValue("IdAsignacionGrupo", ap.IdAsignacionGrupo)
                        .addValue("IdAsignacionMasiva", null)
                        .addValue("IdTipoAsignacion", null)
                        .addValue("AplicaComisionAsignacionMasiva", "N");
                    asigBatch.add(ps);

                    for (CobroAsigProceso cob : ap.cobros) {
                        if ("N".equalsIgnoreCase(ap.EsAPlazo)) {
                            cobroBatch.add(new MapSqlParameterSource()
                                .addValue("IdCobroAsignacion", cob.IdCobroAsignacion)
                                .addValue("IdAsignacion", cob.IdAsignacion)
                                .addValue("IdCobro", cob.IdCobro)
                                .addValue("ValorCobro", cob.ValorCobroPerfil)
                                .addValue("MontoCobroCalculado", cob.MontoCobroCalculado)
                                .addValue("MontoCobroCalculadoLocal", cob.MontoCobroCalculadoLocal)
                                .addValue("MontoCobroCalculadoLiquidacion", cob.MontoCobroCalculadoLiquidacion)
                                .addValue("ValorCobroAplicado", cob.ValorCobroAplicado)
                                .addValue("IdMoneda", cob.IdMoneda)
                                .addValue("FormaCobro", cob.FormaCobro)
                                .addValue("CodigoCobroClasificacion", cob.CodigoCobroClasificacion));
                        }
                    }
                }
                if (!asigBatch.isEmpty())
                    jdbc.batchUpdate(SQL_INSERT_ASIGNACIONES, asigBatch.toArray(new MapSqlParameterSource[0]));
                if (!cobroBatch.isEmpty())
                    jdbc.batchUpdate(SQL_INSERT_COBROS, cobroBatch.toArray(new MapSqlParameterSource[0]));

                // 24. CrearEventoAutorizacionMasivo for PENDIENTE_AUTORIZACION
                List<AsigProceso> pendAutList = asignaciones.stream()
                    .filter(ap -> EST_PEND_AUT.equals(ap.IdEstadoAsignacion)).collect(Collectors.toList());
                if (!pendAutList.isEmpty()) {
                    try {
                        JsonArray eventos = new JsonArray();
                        for (AsigProceso ap : pendAutList) {
                            OrdenProceso ord = ordenes.get(ap.IdOrden);
                            TranProceso tran = transacciones.get(ap.IdTransaccion);
                            JsonObject ev = new JsonObject();
                            ev.addProperty("IdTipoEvento", "INGRESAR_ASIGNACION");
                            ev.addProperty("IdConcepto", "INGRESAR_ASIGNACION");
                            ev.addProperty("NumeroOperacionConcepto", String.valueOf(ap.IdAsignacion));
                            ev.addProperty("IdCliente", ord != null ? ord.IdCliente : 0L);
                            ev.addProperty("IdUsuarioSolicita", idUsuario);
                            ev.addProperty("IdInstrumento", ord != null ? ord.IdInstrumento : "");
                            ev.addProperty("Monto", ap.Monto);
                            eventos.add(ev);
                        }
                        JsonObject evParams = new JsonObject();
                        evParams.addProperty("p_IdEmpresa", idEmpresa);
                        evParams.addProperty("p_IdModulo", idModulo);
                        evParams.addProperty("p_IdUsuario", idUsuario);
                        evParams.add("p_TABLAEVENTOS", eventos);
                        plataforma.call("cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo", evParams);
                    } catch (Exception ex) {
                        log.warn("CrearEventoAutorizacionMasivo failed (non-fatal): {}", ex.getMessage());
                    }
                }
            }

            // 25. Build output
            for (AsigProceso ap : asignaciones) {
                JsonObject res = new JsonObject();
                res.addProperty("IdAsignacion", ap.IdAsignacion);
                res.addProperty("IdInterno", ap.IdInterno);
                res.addProperty("EsAPlazo", ap.EsAPlazo);
                resultados.add(res);
                mensajes.add(new Mensaje(ap.IdInterno, M_INFO, "ASIGNACION_CREADA", "Asignación " + ap.IdAsignacion + " creada"));
                for (CobroAsigProceso cob : ap.cobros) {
                    JsonObject c = new JsonObject();
                    c.addProperty("IdInterno", ap.IdInterno);
                    c.addProperty("IdAsignacion", cob.IdAsignacion);
                    c.addProperty("IdCobroAsignacion", cob.IdCobroAsignacion);
                    c.addProperty("CodigoCobroClasificacion", cob.CodigoCobroClasificacion);
                    c.addProperty("IdCobro", cob.IdCobro);
                    c.addProperty("IdMonedaCobro", cob.IdMoneda);
                    c.addProperty("ValorCobroPerfil", cob.ValorCobroPerfil);
                    c.addProperty("FormaCobro", cob.FormaCobro);
                    c.addProperty("MontoMinimo", cob.MontoMinimo);
                    c.addProperty("ValorCobroAplicado", cob.ValorCobroAplicado);
                    c.addProperty("MontoCobroCalculado", cob.MontoCobroCalculado);
                    c.addProperty("MontoCobroCalculadoLocal", cob.MontoCobroCalculadoLocal);
                    c.addProperty("MontoCobroCalculadoLiquidacion", cob.MontoCobroCalculadoLiquidacion);
                    cobros.add(c);
                }
            }

            // 26. Notificar orden asignada (non-blocking)
            try {
                JsonArray ordNotif = new JsonArray();
                asignaciones.stream().map(ap -> ap.IdOrden).distinct().forEach(idOrd -> {
                    JsonObject on = new JsonObject(); on.addProperty("IdOrden", idOrd); ordNotif.add(on);
                });
                JsonObject nParams = new JsonObject();
                nParams.addProperty("p_IdEmpresa", idEmpresa); nParams.addProperty("p_IdModulo", idModulo);
                nParams.addProperty("p_IdUsuario", idUsuario); nParams.addProperty("p_IdEstadoAsignacion", "ASIGNADA");
                nParams.add("p_Ordenes", ordNotif);
                plataforma.call("cl.bcs.ordenes.NotificacionOrdenAsignada", nParams);
            } catch (Exception ex) {
                log.warn("NotificacionOrdenAsignada failed (non-fatal): {}", ex.getMessage());
            }

            log.info("CrearAsignaciones completado en {}ms, {} asignaciones", System.currentTimeMillis() - t0, asignaciones.size());
        } finally {
            // 27. Delete temporal data
            try { jdbc.update(SQL_DELETE_ASOCIACIONES, Map.of()); }
            catch (Exception ex) { log.warn("DELETE asociaciones failed: {}", ex.getMessage()); }
        }

        return buildOut(mensajes, resultados, cobros);
    }

    // ─── Cross-service helpers ────────────────────────────────────────────────

    private void callCalcularParidad(Long idEmpresa, String fecha, List<AsigProceso> asigs,
            Map<Long,OrdenProceso> ordenes, Map<Long,CobroAsigProceso> cobrosHash,
            Map<Long,AsigProceso> asigHash, List<Mensaje> mensajes, Long idTrabajo, String tipo) {
        JsonArray calculos = new JsonArray();
        for (AsigProceso ap : asigs) {
            for (CobroAsigProceso cob : ap.cobros) {
                OrdenProceso ord = ordenes.get(ap.IdOrden);
                if (ord == null) continue;
                JsonObject c = new JsonObject();
                c.addProperty("IdInterno", cob.IdInternoCobro);
                c.addProperty("Fecha", fecha);
                c.addProperty("IdMonedaOrigen", ord.IdMonedaOrden);
                c.addProperty("IdMonedaDestino", cob.IdMoneda);
                c.addProperty("MontoOrigen", ap.Monto);
                c.addProperty("IdTrabajo", idTrabajo);
                calculos.add(c);
            }
        }
        if (calculos.size() == 0) return;
        JsonObject p = new JsonObject(); p.addProperty("p_IdEmpresa", idEmpresa); p.addProperty("p_IdTrabajo", idTrabajo); p.add("p_CalculosEntrada", calculos);
        try {
            JsonObject resp = plataforma.call("cl.bcs.plataforma.mer.CalcularParidad", p);
            JsonArray salida = resp.has("p_out_CalculosSalida") ? resp.get("p_out_CalculosSalida").getAsJsonArray() : new JsonArray();
            for (JsonElement el : salida) {
                JsonObject cs = el.getAsJsonObject();
                long idInterno = cs.get("IdInterno").getAsLong();
                CobroAsigProceso cob = cobrosHash.get(idInterno);
                if (cob == null) continue;
                if (!"OK".equals(cs.has("Resultado") ? cs.get("Resultado").getAsString() : "OK")) {
                    AsigProceso ap = asigHash.get(cob.IdAsignacion);
                    if (ap != null) { addErr(ap, mensajes, cs.has("CodigoMensaje") ? cs.get("CodigoMensaje").getAsString() : "ERROR_PARIDAD", "Error paridad cobro"); }
                } else {
                    cob.MontoAsignacionMonedaCobro = cs.has("MontoDestino") ? cs.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;
                }
            }
        } catch (Exception ex) {
            log.warn("CalcularParidad({}) failed: {}", tipo, ex.getMessage());
        }
    }

    private void callCalcularParidadLiq(Long idEmpresa, String fecha, List<AsigProceso> asigs,
            Map<Long,OrdenProceso> ordenes, Map<Long,AsigProceso> asigHash, List<Mensaje> mensajes, String monedaSistema) {
        JsonArray calculos = new JsonArray();
        for (AsigProceso ap : asigs) {
            OrdenProceso ord = ordenes.get(ap.IdOrden);
            if (ord == null) continue;
            JsonObject c = new JsonObject();
            c.addProperty("IdInterno", ap.IdAsignacion);
            c.addProperty("Fecha", fecha);
            c.addProperty("IdMonedaOrigen", ord.IdMonedaOrden);
            c.addProperty("IdMonedaDestino", ord.IdMonedaLiquidacionOrden);
            c.addProperty("MontoOrigen", ap.Monto);
            c.addProperty("IdTrabajo", 2001L);
            calculos.add(c);
        }
        if (calculos.size() == 0) return;
        JsonObject p = new JsonObject(); p.addProperty("p_IdEmpresa", idEmpresa); p.addProperty("p_IdTrabajo", 2001L); p.add("p_CalculosEntrada", calculos);
        try {
            JsonObject resp = plataforma.call("cl.bcs.plataforma.mer.CalcularParidad", p);
            JsonArray salida = resp.has("p_out_CalculosSalida") ? resp.get("p_out_CalculosSalida").getAsJsonArray() : new JsonArray();
            for (JsonElement el : salida) {
                JsonObject cs = el.getAsJsonObject();
                long idInterno = cs.get("IdInterno").getAsLong();
                AsigProceso ap = asigHash.get(idInterno);
                if (ap == null) continue;
                if (!"OK".equals(cs.has("Resultado") ? cs.get("Resultado").getAsString() : "OK")) {
                    addErr(ap, mensajes, cs.has("CodigoMensaje") ? cs.get("CodigoMensaje").getAsString() : "ERROR_PARIDAD_LIQ", "Error paridad liquidacion");
                } else {
                    ap.MontoAsignadoLiquidacion = cs.has("MontoDestino") ? cs.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;
                    ap.MontoAsignadoLocal = cs.has("MontoLocal") ? cs.get("MontoLocal").getAsBigDecimal() : BigDecimal.ZERO;
                    ap.TipoCambio = cs.has("FactorCambioLocal") ? cs.get("FactorCambioLocal").getAsBigDecimal() : BigDecimal.ZERO;
                    ap.Paridad = cs.has("FactorOrigenCambio") ? cs.get("FactorOrigenCambio").getAsBigDecimal() : BigDecimal.ZERO;
                    ap.FechaTipoCambio = cs.has("FechaCambioLocal") ? cs.get("FechaCambioLocal").getAsString() : "";
                    ap.FechaParidad = cs.has("FechaOrigenCambio") ? cs.get("FechaOrigenCambio").getAsString() : "";
                }
            }
        } catch (Exception ex) {
            log.warn("CalcularParidad(liq) failed: {}", ex.getMessage());
        }
    }

    private void callCalcularParidadCobros(Long idEmpresa, String fecha, List<AsigProceso> asigs,
            Map<Long,OrdenProceso> ordenes, Map<Long,CobroAsigProceso> cobrosHash,
            Map<Long,AsigProceso> asigHash, List<Mensaje> mensajes, String monedaSistema) {
        JsonArray calculos = new JsonArray();
        for (AsigProceso ap : asigs) {
            for (CobroAsigProceso cob : ap.cobros) {
                OrdenProceso ord = ordenes.get(ap.IdOrden);
                if (ord == null) continue;
                JsonObject c = new JsonObject();
                c.addProperty("IdInterno", cob.IdInternoCobro);
                c.addProperty("Fecha", fecha);
                c.addProperty("IdMonedaOrigen", cob.IdMoneda);
                c.addProperty("IdMonedaDestino", ord.IdMonedaLiquidacionOrden);
                c.addProperty("MontoOrigen", cob.MontoCobroCalculado);
                c.addProperty("IdTrabajo", 3001L);
                calculos.add(c);
            }
        }
        if (calculos.size() == 0) return;
        JsonObject p = new JsonObject(); p.addProperty("p_IdEmpresa", idEmpresa); p.addProperty("p_IdTrabajo", 3001L); p.add("p_CalculosEntrada", calculos);
        try {
            JsonObject resp = plataforma.call("cl.bcs.plataforma.mer.CalcularParidad", p);
            JsonArray salida = resp.has("p_out_CalculosSalida") ? resp.get("p_out_CalculosSalida").getAsJsonArray() : new JsonArray();
            for (JsonElement el : salida) {
                JsonObject cs = el.getAsJsonObject();
                long idInterno = cs.get("IdInterno").getAsLong();
                CobroAsigProceso cob = cobrosHash.get(idInterno);
                if (cob == null) continue;
                AsigProceso ap = asigHash.get(cob.IdAsignacion);
                if (!"OK".equals(cs.has("Resultado") ? cs.get("Resultado").getAsString() : "OK")) {
                    if (ap != null) addErr(ap, mensajes, "ERROR_PARIDAD_COBRO_LIQ", "Error paridad cobro liquidacion");
                } else {
                    cob.MontoCobroCalculadoLiquidacion = cs.has("MontoDestino") ? cs.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;
                    cob.MontoCobroCalculadoLocal = cs.has("MontoLocal") ? cs.get("MontoLocal").getAsBigDecimal() : BigDecimal.ZERO;
                }
            }
        } catch (Exception ex) {
            log.warn("CalcularParidad(cobros) failed: {}", ex.getMessage());
        }
    }

    private void callCalcularCobrosCL(Long idEmpresa, List<AsigProceso> asigs,
            Map<Long,OrdenProceso> ordenes, Map<Long,TranProceso> transacciones, Map<Long,CobroAsigProceso> cobrosHash) {
        for (AsigProceso ap : asigs) {
            if (ap.cobros.isEmpty()) continue;
            OrdenProceso ord = ordenes.get(ap.IdOrden);
            TranProceso tran = transacciones.get(ap.IdTransaccion);
            JsonArray cobrosArr = new JsonArray();
            for (CobroAsigProceso cob : ap.cobros) {
                JsonObject c = new JsonObject();
                c.addProperty("IdInternoCobro", cob.IdInternoCobro);
                c.addProperty("IdCobro", cob.IdCobro);
                c.addProperty("ValorCobroPerfil", cob.ValorCobroPerfil);
                c.addProperty("MontoMinimo", cob.MontoMinimo);
                c.addProperty("CodigoCobroClasificacion", cob.CodigoCobroClasificacion);
                c.addProperty("PermiteCambiarValor", cob.PermiteCambiarValor);
                c.addProperty("ValorCobroAplicado", cob.ValorCobroAplicado);
                c.addProperty("IdMonedaCobro", cob.IdMoneda);
                c.addProperty("MontoOperacionMonedaCobro", cob.MontoAsignacionMonedaCobro);
                cobrosArr.add(c);
            }
            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa", idEmpresa);
            p.addProperty("DiasPlazo", ord != null ? ord.PlazoSimultanea : 0L);
            p.addProperty("Tasa", tran != null ? tran.Tasa : BigDecimal.ZERO);
            p.add("cobros", cobrosArr);
            try {
                JsonObject resp = plataforma.call("cl.bcs.plataforma.com.CalcularCobros", p);
                JsonArray salida = resp.has("p_out_Cobros") ? resp.get("p_out_Cobros").getAsJsonArray() : new JsonArray();
                for (JsonElement el : salida) {
                    JsonObject cs = el.getAsJsonObject();
                    long ic = cs.get("IdInternoCobro").getAsLong();
                    CobroAsigProceso cob = cobrosHash.get(ic);
                    if (cob == null) continue;
                    cob.ValorCobroAplicado = cs.has("ValorCobroFinal") ? cs.get("ValorCobroFinal").getAsBigDecimal() : cob.ValorCobroAplicado;
                    cob.MontoCobroCalculado = cs.has("MontoCobroCalculado") ? cs.get("MontoCobroCalculado").getAsBigDecimal() : BigDecimal.ZERO;
                }
            } catch (Exception ex) {
                log.warn("CalcularCobros(CL) failed for asig {}: {}", ap.IdAsignacion, ex.getMessage());
            }
        }
    }

    private void callCalcularCobrosPE(Long idEmpresa, List<AsigProceso> asigs,
            Map<Long,OrdenProceso> ordenes, Map<Long,CobroAsigProceso> cobrosHash) {
        JsonArray ops = new JsonArray(); JsonArray cobrosAll = new JsonArray();
        for (AsigProceso ap : asigs) {
            if ("S".equalsIgnoreCase(ap.EsAPlazo)) continue;
            OrdenProceso ord = ordenes.get(ap.IdOrden);
            if (ord == null) continue;
            JsonObject info = new JsonObject();
            info.addProperty("IdInterno", ap.IdAsignacion);
            info.addProperty("MontoOperacion", ap.Monto);
            info.addProperty("MontoLiquidacionOperacion", ap.MontoAsignadoLiquidacion);
            info.addProperty("MontoOperacionPlazo", ap.MontoAsignadoPlazo);
            info.addProperty("Tasa", ap.TasaPromedio);
            info.addProperty("PlazoEnDias", ord.PlazoReporte);
            info.addProperty("AccionOrden", ord.AccionOrden);
            ops.add(info);
            for (CobroAsigProceso cob : ap.cobros) {
                JsonObject c = new JsonObject(); c.addProperty("IdInterno", ap.IdAsignacion); c.addProperty("IdInternoCobro", cob.IdInternoCobro);
                c.addProperty("CodigoDefinicionCobro", cob.IdCobro); c.addProperty("IdTipoCobro", cob.FormaCobro);
                c.addProperty("IdClaseCobro", cob.CodigoCobroClasificacion); cobrosAll.add(c);
            }
        }
        if (ops.size() == 0) return;
        JsonObject p = new JsonObject(); p.addProperty("p_IdEmpresa", idEmpresa); p.add("p_Operaciones", ops); p.add("p_Cobros", cobrosAll);
        try {
            JsonObject resp = plataforma.call("cl.bcs.plataforma.com.pe.CalcularCobrosPe", p);
            JsonArray salida = resp.has("p_out_Cobros") ? resp.get("p_out_Cobros").getAsJsonArray() : new JsonArray();
            for (JsonElement el : salida) {
                JsonObject cs = el.getAsJsonObject();
                long ic = cs.get("IdInternoCobro").getAsLong();
                CobroAsigProceso cob = cobrosHash.get(ic);
                if (cob == null) continue;
                cob.ValorCobroAplicado = cs.has("ValorCobroFinal") ? cs.get("ValorCobroFinal").getAsBigDecimal() : cob.ValorCobroAplicado;
                cob.MontoCobroCalculado = cs.has("MontoCobroCalculado") ? cs.get("MontoCobroCalculado").getAsBigDecimal() : BigDecimal.ZERO;
                if (cob.IdMoneda == null && cs.has("IdMonedaCobro")) cob.IdMoneda = cs.get("IdMonedaCobro").getAsString();
            }
        } catch (Exception ex) {
            log.warn("CalcularCobrosPe failed: {}", ex.getMessage());
        }
    }

    private Set<Long> validarLimites(Long idEmpresa, Long idUsuario, String idModulo, String usaRN,
            List<AsigProceso> asigs, Map<Long,OrdenProceso> ordenes, Map<Long,TranProceso> transacciones, List<Mensaje> mensajes) {
        Set<Long> pendAut = new HashSet<>();
        JsonArray ops = new JsonArray();
        for (AsigProceso ap : asigs) {
            OrdenProceso ord = ordenes.get(ap.IdOrden);
            TranProceso tran = transacciones.get(ap.IdTransaccion);
            if (ord == null || tran == null) continue;
            JsonObject o = new JsonObject();
            o.addProperty("IdOperacion", ap.IdAsignacion); o.addProperty("IdInterno", ap.IdInterno);
            o.addProperty("IdInternoGrupo", ap.IdAsignacionGrupo); o.addProperty("IdCliente", ord.IdCliente);
            o.addProperty("IdPortafolio", ord.IdPortafolio); o.addProperty("IdInstrumento", ord.IdInstrumento);
            o.addProperty("IdTipoOperacion", ord.IdTipoOperacion); o.addProperty("MontoCompraMonedaSistema", ap.MontoAsignadoLocal);
            o.addProperty("Cantidad", ap.Cantidad); o.addProperty("ValorRescate", ap.ValorRescate);
            o.addProperty("IdClasificacionTipoInstrumento", ord.IdClasificacionTipoInstrumento);
            ops.add(o);
        }
        if (ops.size() == 0) return pendAut;
        JsonObject p = new JsonObject(); p.addProperty("p_IdEmpresa", idEmpresa); p.addProperty("p_IdUsuario", idUsuario);
        p.addProperty("p_UsaReglasNegocio", usaRN); p.addProperty("p_IdTipoProceso", "ASIGNAR"); p.add("p_Operaciones", ops);
        try {
            JsonObject resp = plataforma.call("cl.bcs.plataforma.lim.ValidarLimiteCuentaPropiaIFRF", p);
            JsonArray validaciones = resp.has("P_OUT_VALIDACIONES") ? resp.get("P_OUT_VALIDACIONES").getAsJsonArray() : new JsonArray();
            for (JsonElement el : validaciones) {
                JsonObject v = el.getAsJsonObject();
                String tipo = v.has("TipoComportamiento") ? v.get("TipoComportamiento").getAsString() : "";
                long idInternoOp = v.has("IdInternoOperacion") ? v.get("IdInternoOperacion").getAsLong() : -1L;
                if ("AUTORIZACION".equalsIgnoreCase(tipo)) pendAut.add(idInternoOp);
                else if ("RECHAZAR".equalsIgnoreCase(tipo)) {
                    Optional<AsigProceso> apOpt = asigs.stream().filter(a -> idInternoOp == a.IdInterno).findFirst();
                    apOpt.ifPresent(ap -> addErr(ap, mensajes, "LIMITE_CUENTA_PROPIA_SUPERADO", "Límite cuenta propia superado"));
                }
            }
        } catch (Exception ex) {
            log.warn("ValidarLimiteCuentaPropiaIFRF failed (non-fatal): {}", ex.getMessage());
        }
        return pendAut;
    }

    // ─── Sequence helper ─────────────────────────────────────────────────────

    private List<Long> getSecuencia(String nombre, Long cantidad) {
        if (cantidad == null || cantidad <= 0) return new ArrayList<>();
        String sql = "SELECT \"" + nombre + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
        List<Map<String,Object>> rows = jdbc.queryForList(sql, Map.of("cantidad", cantidad));
        return rows.stream().map(r -> longOf(r, "Id")).collect(Collectors.toList());
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private TranProceso mapTran(Map<String,Object> r) {
        TranProceso tp = new TranProceso();
        tp.IdTransaccion = longOf(r,"IdTransaccion"); tp.IdTipoOperacion = str(r,"IdTipoOperacion");
        tp.IdInstrumento = str(r,"IdInstrumento"); tp.IdLibroOrdenes = str(r,"IdLibroOrdenes");
        tp.CantidadDisponible = bdOf(r,"CantidadDisponible"); tp.MontoDisponible = bdOf(r,"MontoDisponible");
        tp.ValorRescateDisponible = bdOf(r,"ValorRescateDisponible"); tp.IdCondicionDeLiquidacion = str(r,"IdCondicionDeLiquidacion");
        tp.IdBolsa = str(r,"IdBolsa"); tp.Precio = bdOf(r,"Precio"); tp.Tasa = bdOf(r,"Tasa");
        tp.EsVentaCorta = str(r,"EsVentaCorta"); tp.IdEstado = str(r,"IdEstado"); tp.EsFungible = str(r,"EsFungible");
        tp.CorteMinimo = bdOf(r,"CorteMinimo"); tp.Cantidad = bdOf(r,"Cantidad"); tp.Monto = bdOf(r,"Monto");
        tp.ValorRescate = bdOf(r,"ValorRescate"); tp.IdInstrumentoSubyacente = str(r,"IdInstrumentoSubyacente");
        tp.IdClasificacionTipoInstrumento = str(r,"IdClasificacionTipoInstrumento");
        tp.Tamano = r.get("Tamano") != null ? ((Number)r.get("Tamano")).longValue() : 1L;
        tp.IdTransaccionPlazo = r.get("IdTransaccionPlazo") != null ? ((Number)r.get("IdTransaccionPlazo")).longValue() : null;
        tp.EsAPlazo = str(r,"EsAPlazo"); tp.IdMoneda = str(r,"IdMoneda"); tp.AccionOrden = str(r,"AccionOrden");
        tp.IdTipoInstrumento = str(r,"IdTipoInstrumento");
        tp.IdCorredoraCorresponsal = r.get("IdCorredoraCorresponsal") != null ? ((Number)r.get("IdCorredoraCorresponsal")).longValue() : null;
        tp.Bloqueado = str(r,"Bloqueado"); tp.Reservado = str(r,"Reservado");
        tp.IdUsuarioReserva = r.get("IdUsuarioReserva") != null ? ((Number)r.get("IdUsuarioReserva")).longValue() : 0L;
        tp.IdTransaccionOd = r.get("IdTransaccionOd") != null ? ((Number)r.get("IdTransaccionOd")).longValue() : null;
        tp.Intereses = bdOf(r,"Intereses"); tp.FechaVencimiento = str(r,"FechaVencimiento");
        tp.Folio = str(r,"Folio"); tp.NemoSVS = str(r,"NemoSVS"); tp.Nemotecnico = str(r,"Nemotecnico");
        return tp;
    }

    private OrdenProceso mapOrden(Map<String,Object> r) {
        OrdenProceso op = new OrdenProceso();
        op.IdOrden = longOf(r,"IdOrden"); op.IdTipoOperacion = str(r,"IdTipoOperacion");
        op.IdTipoPrecio = str(r,"IdTipoPrecio"); op.EsVentaCorta = str(r,"EsVentaCorta");
        op.PrecioOrden = bdOf(r,"PrecioOrden"); op.Tamano = r.get("Tamano") != null ? ((Number)r.get("Tamano")).longValue() : 1L;
        op.IdPortafolio = r.get("IdPortafolio") != null ? ((Number)r.get("IdPortafolio")).longValue() : null;
        op.CantidadDisponible = bdOf(r,"CantidadDisponible"); op.MontoDisponible = bdOf(r,"MontoDisponible");
        op.ValorRescateDisponible = bdOf(r,"ValorRescateDisponible"); op.IdTipoCanalIngreso = str(r,"IdTipoCanalIngreso");
        op.IdEstadoOrden = str(r,"IdEstadoOrden"); op.IdMonedaOrden = str(r,"IdMonedaOrden");
        op.IdMonedaLiquidacionOrden = str(r,"IdMonedaLiquidacionOrden"); op.Cantidad = bdOf(r,"Cantidad");
        op.IdGrupoCobro = r.get("IdGrupoCobro") != null ? ((Number)r.get("IdGrupoCobro")).longValue() : null;
        op.IdInstrumento = str(r,"IdInstrumento"); op.IdCondicionDeLiquidacion = str(r,"IdCondicionDeLiquidacion");
        op.IdBolsa = str(r,"IdBolsa"); op.IdClasificacionTipoInstrumento = str(r,"IdClasificacionTipoInstrumento");
        op.TipoCambio = bdOf(r,"TipoCambio"); op.IdLibroOrdenes = str(r,"IdLibroOrdenes");
        op.ValorRescate = bdOf(r,"ValorRescate"); op.AccionOrden = str(r,"AccionOrden");
        op.IdInstrumentoSubyacente = str(r,"IdInstrumentoSubyacente"); op.IdTipoInstrumento = str(r,"IdTipoInstrumento");
        op.PlazoSimultanea = r.get("PlazoSimultanea") != null ? ((Number)r.get("PlazoSimultanea")).longValue() : 0L;
        op.EsFungible = str(r,"EsFungible"); op.BaseTasa = r.get("BaseTasa") != null ? ((Number)r.get("BaseTasa")).longValue() : 360L;
        op.PlazoReporte = r.get("PlazoReporte") != null ? ((Number)r.get("PlazoReporte")).longValue() : 0L;
        op.MontoInteresContado = bdOf(r,"MontoInteresContado"); op.MontoInteresPlazo = bdOf(r,"MontoInteresPlazo");
        op.PorcentajeCobros = bdOf(r,"PorcentajeCobros"); op.TasaReporte = bdOf(r,"TasaReporte");
        op.TasaPromedioReporte = bdOf(r,"TasaPromedioReporte"); op.TipoInteres = str(r,"TipoInteres");
        op.EsCuentaPropia = str(r,"EsCuentaPropia");
        op.PermiteAsignarOEODMismoRut = str(r,"PermiteAsignarOEODMismoRut");
        op.IdCliente = r.get("IdCliente") != null ? ((Number)r.get("IdCliente")).longValue() : null;
        return op;
    }

    private CobroOrdenProceso mapCobro(Map<String,Object> r) {
        CobroOrdenProceso cop = new CobroOrdenProceso();
        cop.IdOrden = longOf(r,"IdOrden"); cop.IdCobroOrden = longOf(r,"IdCobroOrden");
        cop.IdCobro = str(r,"IdCobro"); cop.ValorCobroPerfil = bdOf(r,"ValorCobroPerfil");
        cop.ValorCobroAplicado = bdOf(r,"ValorCobroAplicado"); cop.MontoMinimo = bdOf(r,"MontoMinimo");
        cop.IdMoneda = str(r,"IdMoneda"); cop.FormaCobro = str(r,"FormaCobro");
        cop.CodigoCobroClasificacion = str(r,"CodigoCobroClasificacion");
        cop.CodigoCobroClasificacionBase = str(r,"CodigoCobroClasificacionBase");
        cop.IdGrupoCobro = r.get("IdGrupoCobro") != null ? ((Number)r.get("IdGrupoCobro")).longValue() : null;
        cop.ValorCobroFinal = bdOf(r,"ValorCobroFinal"); cop.PermiteCambiarValor = str(r,"PermiteCambiarValor");
        cop._CheckCobroMonedaOperacion = str(r,"_CheckCobroMonedaOperacion");
        cop._Otros_CheckOperacionContado = str(r,"_Otros_CheckOperacionContado");
        cop._Otros_CheckOperacionPlazo = str(r,"_Otros_CheckOperacionPlazo");
        return cop;
    }

    private CobroAsigProceso copyCobro(CobroOrdenProceso src) {
        CobroAsigProceso c = new CobroAsigProceso();
        c.IdCobro = src.IdCobro; c.ValorCobroPerfil = src.ValorCobroPerfil;
        c.ValorCobroAplicado = src.ValorCobroAplicado; c.MontoMinimo = src.MontoMinimo;
        c.IdMoneda = src.IdMoneda; c.FormaCobro = src.FormaCobro;
        c.CodigoCobroClasificacion = src.CodigoCobroClasificacion;
        c.PermiteCambiarValor = src.PermiteCambiarValor;
        c._CheckCobroMonedaOperacion = src._CheckCobroMonedaOperacion;
        c._Otros_CheckOperacionContado = src._Otros_CheckOperacionContado;
        c._Otros_CheckOperacionPlazo = src._Otros_CheckOperacionPlazo;
        c.ValorCobroFinal = src.ValorCobroFinal;
        return c;
    }

    private AsigProceso copyAsig(AsigProceso src) {
        AsigProceso ap = new AsigProceso();
        ap.IdInterno = src.IdInterno; ap.IdOrden = src.IdOrden; ap.IdTransaccion = src.IdTransaccion;
        ap.Cantidad = src.Cantidad; ap.ValorRescate = src.ValorRescate; ap.FolioCartera = src.FolioCartera;
        ap.CodigoIFRS = src.CodigoIFRS; ap.FlujoOrigen = src.FlujoOrigen; ap.FechaAsignacion = src.FechaAsignacion;
        ap.IdEstadoAsignacion = src.IdEstadoAsignacion; ap.IdAsignacionGrupo = src.IdAsignacionGrupo;
        ap.GlosaRefactura = src.GlosaRefactura; ap.FechaOperacionOriginal = src.FechaOperacionOriginal;
        ap.DocumentoIdentidad = src.DocumentoIdentidad; ap.NombreCompleto = src.NombreCompleto;
        return ap;
    }

    // ─── Utility methods ─────────────────────────────────────────────────────

    private static void addErr(AsigProceso ap, List<Mensaje> msgs, String code, String desc) {
        msgs.add(new Mensaje(ap.IdInterno, M_ERROR, code, desc));
        ap.errores++;
    }

    private static void removeErrors(List<AsigProceso> asigs, List<AsigProceso> errList) {
        Iterator<AsigProceso> it = asigs.iterator();
        while (it.hasNext()) { if (it.next().errores > 0) it.remove(); }
    }

    private static String str(Map<String,Object> r, String key) {
        Object v = r.get(key); return v != null ? v.toString() : "";
    }
    private static Long longOf(Map<String,Object> r, String key) {
        Object v = r.get(key); return v instanceof Number ? ((Number)v).longValue() : null;
    }
    private static BigDecimal bdOf(Map<String,Object> r, String key) {
        Object v = r.get(key);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal)v;
        if (v instanceof Number) return new BigDecimal(v.toString());
        return BigDecimal.ZERO;
    }

    // ─── Output builder ──────────────────────────────────────────────────────

    private static JsonObject buildOut(List<Mensaje> msgs, List<JsonObject> resultados, List<JsonObject> cobros) {
        JsonObject out = new JsonObject();
        long errores = msgs.stream().filter(m -> M_ERROR.equals(m.tipo)).count();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        JsonArray mArr = new JsonArray();
        for (Mensaje m : msgs) {
            JsonObject mo = new JsonObject();
            mo.addProperty("id", m.id); mo.addProperty("tipo", m.tipo);
            mo.addProperty("codigo", m.codigo); mo.addProperty("descripcion", m.descripcion);
            mArr.add(mo);
        }
        out.add("P_OUT_MENSAJES", mArr);
        JsonArray rArr = new JsonArray(); for (JsonObject r : resultados) rArr.add(r);
        out.add("p_out_Resultado", rArr);
        JsonArray cArr = new JsonArray(); for (JsonObject c : cobros) cArr.add(c);
        out.add("p_out_Cobros", cArr);
        return out;
    }

}
