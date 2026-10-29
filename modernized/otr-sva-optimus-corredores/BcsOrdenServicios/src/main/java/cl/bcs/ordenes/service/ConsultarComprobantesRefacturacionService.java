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
import java.sql.Timestamp;
import java.util.*;

/**
 * Migrates cl.bcs.ordenes.ConsultarComprobantesRefacturacion.
 * Queries FAC.Comprobante + detail views and groups by IdComprobante.
 * No RevisarRequisitosBasicos in logica() despite import.
 */
@Service
public class ConsultarComprobantesRefacturacionService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarComprobantesRefacturacionService.class);
    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_COMPROBANTES;

    public ConsultarComprobantesRefacturacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc             = jdbc;
        this.SQL_COMPROBANTES = loadSql("cl/bcs/ordenes/sql/ConsultarComprobantesRefacturacion_SelectComprobantes.sql");
    }

    // ── POJOs ─────────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaConsulta;
    }

    public static class ComprobanteSalida {
        public Long   IdComprobante;
        public String IdEstadoComprobante;
        public String IdTipoComprobante;
        public Long   IdComprobanteReferencia;
        public Long   IdCliente;
        public Long   IdPortafolio;
        public String IdCondicionDeLiquidacion;
        public Date   FechaLiquidacion;
        public String NombreEstadoComprobante;
        public String DocumentoIdentidad;
        public String NombreCompleto;
        public String CodigoPortafolio;
    }

    public static class MovimientoSalida {
        public Long       IdComprobante;
        public String     IdEstadoComprobante;
        public String     IdTipoComprobante;
        public Long       IdCliente;
        public Long       IdPortafolio;
        public String     IdCondicionDeLiquidacion;
        public Date       FechaLiquidacion;
        public String     DocumentoIdentidad;
        public String     NombreCompleto;
        public String     CodigoPortafolio;
        public Timestamp  FechaTransaccion;
        public String     FolioTransaccion;
        public Long       IdTransaccion;
        public String     Instrumento;
        public String     InstrumentoSubyacente;
        public String     IdTipoInstrumento;
        public BigDecimal Cantidad;
        public String     IdMoneda;
        public String     IdClasificacionTipoInstrumento;
    }

    public static class Salida {
        public Long                    P_OUT_ERRORESCONTADOR         = 0L;
        public List<Object>            P_OUT_MENSAJES                = new ArrayList<>();
        public List<ComprobanteSalida> P_OUT_COMPROBANTESFACTURACION = new ArrayList<>();
        public List<MovimientoSalida>  P_OUT_MOVIMIENTOS             = new ArrayList<>();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarComprobantesRefacturacionService.execute()");
        Entrada e = GSON.fromJson(params.toString(), Entrada.class);
        Salida salida = logica(e);
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    private Salida logica(Entrada in) {
        Salida salida = new Salida();

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("fechaComprobante", in.p_FechaConsulta)
                .addValue("idsTipoComprobante", List.of("FAE", "POL"));

        List<Map<String, Object>> rows = jdbc.queryForList(SQL_COMPROBANTES, p);

        Set<Long> procesados = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Long id = toLong(row.get("IdComprobante"));

            if (id != null && !procesados.contains(id)) {
                procesados.add(id);
                ComprobanteSalida c = new ComprobanteSalida();
                c.IdComprobante         = id;
                c.IdEstadoComprobante   = str(row, "IdEstadoComprobante");
                c.IdTipoComprobante     = str(row, "IdTipoComprobante");
                c.IdComprobanteReferencia = toLong(row.get("IdComprobanteNotaCredito"));
                c.IdCliente             = toLong(row.get("IdCliente"));
                c.IdPortafolio          = toLong(row.get("IdPortafolio"));
                c.IdCondicionDeLiquidacion = str(row, "IdCondicionDeLiquidacion");
                if (row.get("FechaLiquidacion") instanceof Date d) c.FechaLiquidacion = d;
                c.NombreEstadoComprobante = str(row, "NombreEstadoComprobante");
                c.DocumentoIdentidad    = str(row, "DocumentoIdentidad");
                c.NombreCompleto        = str(row, "NombreCompleto");
                c.CodigoPortafolio      = str(row, "CodigoPortafolio");
                salida.P_OUT_COMPROBANTESFACTURACION.add(c);
            }

            MovimientoSalida m = new MovimientoSalida();
            m.IdComprobante           = id;
            m.IdEstadoComprobante     = str(row, "IdEstadoComprobante");
            m.IdTipoComprobante       = str(row, "IdTipoComprobante");
            m.IdCliente               = toLong(row.get("IdCliente"));
            m.IdPortafolio            = toLong(row.get("IdPortafolio"));
            m.IdCondicionDeLiquidacion = str(row, "IdCondicionDeLiquidacion");
            if (row.get("FechaLiquidacion") instanceof Date d) m.FechaLiquidacion = d;
            m.DocumentoIdentidad      = str(row, "DocumentoIdentidad");
            m.NombreCompleto          = str(row, "NombreCompleto");
            m.CodigoPortafolio        = str(row, "CodigoPortafolio");
            if (row.get("FechaTransaccion") instanceof Timestamp t) m.FechaTransaccion = t;
            m.FolioTransaccion        = str(row, "FolioTransaccion");
            m.IdTransaccion           = toLong(row.get("IdTransaccion"));
            m.Instrumento             = str(row, "Instrumento");
            m.InstrumentoSubyacente   = str(row, "InstrumentoSubyacente");
            m.IdTipoInstrumento       = str(row, "IdTipoInstrumento");
            m.Cantidad                = toBD(row.get("Cantidad"));
            m.IdMoneda                = str(row, "IdMoneda");
            m.IdClasificacionTipoInstrumento = str(row, "IdClasificacionTipoInstrumento");
            salida.P_OUT_MOVIMIENTOS.add(m);
        }
        return salida;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }

    private static String loadSql(String path) {
        try (var is = ConsultarComprobantesRefacturacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
