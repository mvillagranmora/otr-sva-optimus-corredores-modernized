package cl.bcs.ordenes.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Migrates cl.bcs.ordenes.ConsultarSaldoTransaccionesRefacturar.
 * Branches on p_TipoConsulta: TRALIB (free transactions) or TRANDC (nota credito linked).
 * Returns transactions with positive available quantity.
 */
@Service
public class ConsultarSaldoTransaccionesRefacturarService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSaldoTransaccionesRefacturarService.class);
    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_TRANSACCIONES;
    private final String SQL_NOTAS_CREDITO;
    private final String SQL_ASIGNACIONES;

    public ConsultarSaldoTransaccionesRefacturarService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc              = jdbc;
        this.SQL_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoTransacciones.sql");
        this.SQL_NOTAS_CREDITO = loadSql("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoNotasCredito.sql");
        this.SQL_ASIGNACIONES  = loadSql("cl/bcs/ordenes/sql/ConsultarSaldoTransacciones_SelectSaldoAsignaciones.sql");
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_TipoConsulta          = "";
        public String p_FechaTransaccionInicio = "";
        public String p_FechaTransaccionTermino = "";
    }

    public static class TransaccionSalida {
        public Long       IdTransaccion;
        public String     EsFungible;
        public BigDecimal CantidadTransaccion;
        public Long       IdAsignacion;
        public String     IdEstadoAsignacion;
        public BigDecimal CantidadAsignada;
        public String     IdTipoInstrumento;
        public BigDecimal CantidadDisponibleTransaccion = BigDecimal.ZERO;
        public BigDecimal MontoTransaccion;
        public Timestamp  FechaOperacionTransaccion;
        public Date       FechaSistemaCreacion;
        public Time       HoraSistemaCreacion;
        public String     FolioTransaccion;
        public BigDecimal PrecioTransaccion;
        public String     InstrumentoUI;
        public String     IdTipoOperacion;
        public String     NombreTipoOperacion;
        public String     IdCondicionDeLiquidacion;
        public String     NombreCondicionDeLiquidacion;
        public String     IdBolsa;
        public String     IdMonedaTransaccion;
        public Date       FechaVencimiento;
        public String     IdEmisor;
        public BigDecimal Tasa;
        public String     IdTipoReajuste;
        public String     NemotecnicoSVS;
        public String     EsFungibleUI;
        public String     IndicadorOrdenDirecta;
        public String     EsVentaCorta;
        public String     EsVentaCortaUI;
        public String     NombreTipoInstrumento;
        public String     NombreClasificacionTipoInstrumento;
        public String     NombreCorredorContraparte;
        public String     AccionOrden;
        public String     Reservado;
        public Long       IdUsuarioReserva;
        public String     CodigoUsuarioReserva;
        public String     IdGrupoReserva;
        public Long       IdComprobante;
        public String     IdMonedaInstrumento;
        public String     InstrumentoNegociacionTran;
    }

    public static class Salida {
        public Long                    P_OUT_ERRORESCONTADOR = 0L;
        public List<Object>            P_OUT_MENSAJES        = new ArrayList<>();
        public List<TransaccionSalida> P_OUT_TRANSACCIONES   = new ArrayList<>();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarSaldoTransaccionesRefacturarService.execute()");
        Entrada e = GSON.fromJson(params.toString(), Entrada.class);
        Salida salida = logica(e);
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    private Salida logica(Entrada in) {
        Salida salida = new Salida();

        List<Map<String, Object>> transacciones = new ArrayList<>();
        Map<Long, BigDecimal> hashCantidadNotaCredito       = new HashMap<>();
        Map<Long, BigDecimal> hashCantidadNoEnviadaFacturar = new HashMap<>();
        Map<Long, BigDecimal> hashCantidadEnviadaFacturar   = new HashMap<>();

        if ("TRALIB".equalsIgnoreCase(in.p_TipoConsulta)) {
            MapSqlParameterSource p = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("FechaTransaccionInicio", in.p_FechaTransaccionInicio)
                    .addValue("FechaTransaccionTermino", in.p_FechaTransaccionTermino);
            transacciones = jdbc.queryForList(SQL_TRANSACCIONES, p);
        } else {
            List<Map<String, Object>> notasCredito = jdbc.queryForList(SQL_NOTAS_CREDITO,
                    new MapSqlParameterSource());
            if (!notasCredito.isEmpty()) {
                List<Long> idsTransaccion = new ArrayList<>();
                for (Map<String, Object> nc : notasCredito) {
                    Long idTran = toLong(nc.get("IdTransaccion"));
                    if (idTran != null && !idsTransaccion.contains(idTran)) idsTransaccion.add(idTran);
                }
                transacciones = jdbc.queryForList(SQL_ASIGNACIONES,
                        new MapSqlParameterSource("idsTransaccion", idsTransaccion));
            }
            for (Map<String, Object> nc : notasCredito) {
                Long idTran = toLong(nc.get("IdTransaccion"));
                BigDecimal cant = toBigDecimal(nc.get("CantidadAsignada"));
                if (idTran != null) hashCantidadNotaCredito.merge(idTran, cant, BigDecimal::add);
            }
        }

        for (Map<String, Object> tran : transacciones) {
            Long id = toLong(tran.get("IdTransaccion"));
            if (id == null) continue;
            hashCantidadNoEnviadaFacturar.putIfAbsent(id, BigDecimal.ZERO);
            hashCantidadEnviadaFacturar.putIfAbsent(id, BigDecimal.ZERO);

            String estado = String.valueOf(tran.getOrDefault("IdEstadoAsignacion", ""));
            BigDecimal cant = toBigDecimal(tran.get("CantidadAsignada"));
            if (tran.get("IdAsignacion") != null) {
                if ("NEF".equalsIgnoreCase(estado))
                    hashCantidadNoEnviadaFacturar.merge(id, cant, BigDecimal::add);
                if ("EAF".equalsIgnoreCase(estado) || "REF".equalsIgnoreCase(estado))
                    hashCantidadEnviadaFacturar.merge(id, cant, BigDecimal::add);
            }
        }

        Set<Long> procesadas = new HashSet<>();
        for (Map<String, Object> tran : transacciones) {
            Long id = toLong(tran.get("IdTransaccion"));
            if (id == null || procesadas.contains(id)) continue;
            procesadas.add(id);

            BigDecimal cantTotal = toBigDecimal(tran.get("CantidadTransaccion"));
            BigDecimal noEnv     = hashCantidadNoEnviadaFacturar.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal env       = hashCantidadEnviadaFacturar.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal nc        = "TRALIB".equalsIgnoreCase(in.p_TipoConsulta)
                                   ? BigDecimal.ZERO
                                   : hashCantidadNotaCredito.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal disponible = cantTotal.subtract(noEnv).subtract(env).add(nc);

            if (disponible.compareTo(BigDecimal.ZERO) > 0) {
                TransaccionSalida ts = new TransaccionSalida();
                ts.IdTransaccion                  = id;
                ts.CantidadTransaccion            = cantTotal;
                ts.CantidadDisponibleTransaccion  = disponible;
                ts.IdAsignacion                   = toLong(tran.get("IdAsignacion"));
                ts.IdEstadoAsignacion             = String.valueOf(tran.getOrDefault("IdEstadoAsignacion", ""));
                ts.CantidadAsignada               = toBigDecimal(tran.get("CantidadAsignada"));
                ts.EsFungible                     = String.valueOf(tran.getOrDefault("EsFungible", ""));
                ts.EsFungibleUI                   = String.valueOf(tran.getOrDefault("EsFungibleUI", ""));
                ts.IdTipoInstrumento              = String.valueOf(tran.getOrDefault("IdTipoInstrumento", ""));
                ts.MontoTransaccion               = toBigDecimal(tran.get("MontoTransaccion"));
                ts.FolioTransaccion               = String.valueOf(tran.getOrDefault("FolioTransaccion", ""));
                ts.PrecioTransaccion              = toBigDecimal(tran.get("PrecioTransaccion"));
                ts.InstrumentoUI                  = String.valueOf(tran.getOrDefault("InstrumentoUI", ""));
                ts.IdTipoOperacion                = String.valueOf(tran.getOrDefault("IdTipoOperacion", ""));
                ts.NombreTipoOperacion            = String.valueOf(tran.getOrDefault("NombreTipoOperacion", ""));
                ts.IdCondicionDeLiquidacion       = String.valueOf(tran.getOrDefault("IdCondicionDeLiquidacion", ""));
                ts.NombreCondicionDeLiquidacion   = String.valueOf(tran.getOrDefault("NombreCondicionDeLiquidacion", ""));
                ts.IdBolsa                        = String.valueOf(tran.getOrDefault("IdBolsa", ""));
                ts.IdMonedaTransaccion            = String.valueOf(tran.getOrDefault("IdMonedaTransaccion", ""));
                ts.IdEmisor                       = String.valueOf(tran.getOrDefault("IdEmisor", ""));
                ts.Tasa                           = toBigDecimal(tran.get("Tasa"));
                ts.IdTipoReajuste                 = String.valueOf(tran.getOrDefault("IdTipoReajuste", ""));
                ts.NemotecnicoSVS                 = String.valueOf(tran.getOrDefault("NemotecnicoSVS", ""));
                ts.IndicadorOrdenDirecta          = String.valueOf(tran.getOrDefault("IndicadorOrdenDirecta", ""));
                ts.EsVentaCorta                   = String.valueOf(tran.getOrDefault("EsVentaCorta", ""));
                ts.EsVentaCortaUI                 = String.valueOf(tran.getOrDefault("EsVentaCortaUI", ""));
                ts.NombreTipoInstrumento          = String.valueOf(tran.getOrDefault("NombreTipoInstrumento", ""));
                ts.NombreClasificacionTipoInstrumento = String.valueOf(tran.getOrDefault("NombreClasificacionTipoInstrumento", ""));
                ts.NombreCorredorContraparte      = String.valueOf(tran.getOrDefault("NombreCorredorContraparte", ""));
                ts.AccionOrden                    = String.valueOf(tran.getOrDefault("AccionOrden", ""));
                ts.Reservado                      = String.valueOf(tran.getOrDefault("Reservado", ""));
                ts.IdUsuarioReserva               = toLong(tran.get("IdUsuarioReserva"));
                ts.CodigoUsuarioReserva           = String.valueOf(tran.getOrDefault("CodigoUsuarioReserva", ""));
                ts.IdGrupoReserva                 = String.valueOf(tran.getOrDefault("IdGrupoReserva", ""));
                ts.IdComprobante                  = toLong(tran.get("IdComprobante"));
                ts.IdMonedaInstrumento            = String.valueOf(tran.getOrDefault("IdMonedaInstrumento", ""));
                ts.InstrumentoNegociacionTran     = String.valueOf(tran.getOrDefault("InstrumentoNegociacionTran", ""));
                if (tran.get("FechaOperacionTransaccion") instanceof Timestamp t) ts.FechaOperacionTransaccion = t;
                if (tran.get("FechaSistemaCreacion") instanceof Date d) ts.FechaSistemaCreacion = d;
                if (tran.get("HoraSistemaCreacion") instanceof Time t) ts.HoraSistemaCreacion = t;
                if (tran.get("FechaVencimiento") instanceof Date d) ts.FechaVencimiento = d;
                salida.P_OUT_TRANSACCIONES.add(ts);
            }
        }
        return salida;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarSaldoTransaccionesRefacturarService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
