package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.PrevisualizarLiberacionMasiva.
 * Validates empresa/usuario/modulo, then queries reserved transactions with optional filters.
 */
@Service
public class PrevisualizarLiberacionMasivaService {

    private static final Logger log = LoggerFactory.getLogger(PrevisualizarLiberacionMasivaService.class);
    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_LIBERACION;

    public PrevisualizarLiberacionMasivaService(NamedParameterJdbcTemplate jdbc,
                                                RequisitosBasicosUtil requisitosUtil) {
        this.jdbc           = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_LIBERACION = loadSql("cl/bcs/ordenes/sql/PrevisualizarLiberacionMasiva_ConsultarTransaccionesReservadas.sql");
    }

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long   p_IdLibroOrdenes;
        public String p_IdClasificacionTipoInstrumento;
        public String p_IdTipoInstrumento;
        public Long   p_IdInstrumento;
        public String p_AccionOrden;
        public String p_IdBolsa;
        public String p_IdMoneda;
        public String p_IdCondicionDeLiquidacion;
    }

    public static class TransaccionSalida {
        public Long       IdTransaccion;
        public String     FolioTransaccion;
        public String     FechaSistemaCreacion;
        public String     HoraSistemaCreacion;
        public String     Nemotecnico;
        public Long       IdInstrumento;
        public String     IdTipoInstrumento;
        public String     IdClasificacionTipoInstrumento;
        public String     AccionOrden;
        public String     IdBolsa;
        public String     IdMoneda;
        public String     IdCondicionDeLiquidacion;
        public String     NombreCondicionDeLiquidacion;
        public Long       IdLibroOrdenes;
        public String     NombreLibroOrdenes;
        public String     Reservado;
        public BigDecimal Cantidad;
        public BigDecimal Monto;
    }

    public static class MensajeSalida {
        public Long   IdInterno;
        public String tipo;
        public String codigo;
        public String descripcion;
        public MensajeSalida(Long id, String tipo, String codigo) {
            this.IdInterno = id; this.tipo = tipo; this.codigo = codigo; this.descripcion = codigo;
        }
    }

    public static class Salida {
        public Long                    P_OUT_ERRORESCONTADOR  = 0L;
        public List<MensajeSalida>     p_out_Mensajes         = new ArrayList<>();
        public List<TransaccionSalida> p_out_Transacciones    = new ArrayList<>();
    }

    public JsonObject execute(JsonObject params) {
        log.debug("PrevisualizarLiberacionMasivaService.execute()");
        Entrada e = GSON.fromJson(params.toString(), Entrada.class);
        Salida salida = logica(e);
        return GSON.fromJson(GSON.toJson(salida), JsonObject.class);
    }

    private Salida logica(Entrada in) {
        Salida out = new Salida();

        // 1. RevisarRequisitosBasicos
        RequisitosBasicosUtil.Salida basicos = requisitosUtil.validar(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        if (basicos.contarErrores() > 0) {
            basicos.p_out_Mensajes.forEach(m -> out.p_out_Mensajes.add(
                    new MensajeSalida(m.IdInterno, m.tipo, m.codigo)));
            out.P_OUT_ERRORESCONTADOR = basicos.contarErrores();
            return out;
        }

        // 2. Validate dates
        if (in.p_FechaDesde == null) {
            out.p_out_Mensajes.add(new MensajeSalida(0L, "ERROR", "FECHA_DESDE_VACIA"));
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }
        if (in.p_FechaHasta == null) {
            out.p_out_Mensajes.add(new MensajeSalida(0L, "ERROR", "FECHA_HASTA_VACIA"));
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        // 3. Query
        List<Map<String, Object>> rows = jdbc.queryForList(SQL_LIBERACION,
                new MapSqlParameterSource()
                        .addValue("FechaDesde", in.p_FechaDesde)
                        .addValue("FechaHasta", in.p_FechaHasta));

        // 4. Filter and map
        for (Map<String, Object> row : rows) {
            String accion     = str(row, "AccionOrden");
            String bolsa      = str(row, "IdBolsa");
            Long   instrumento = toLong(row.get("IdInstrumento"));
            Long   libro       = toLong(row.get("IdLibroOrdenes"));
            String clasif      = str(row, "IdClasificacionTipoInstrumento");
            String tipoInstr   = str(row, "IdTipoInstrumento");
            String moneda      = str(row, "IdMoneda");
            String condicion   = str(row, "IdCondicionDeLiquidacion");

            if (in.p_AccionOrden != null && !in.p_AccionOrden.equalsIgnoreCase(accion)) continue;
            if (in.p_IdBolsa != null && !in.p_IdBolsa.equalsIgnoreCase(bolsa)) continue;
            if (in.p_IdInstrumento != null && !in.p_IdInstrumento.equals(instrumento)) continue;
            if (in.p_IdLibroOrdenes != null && !in.p_IdLibroOrdenes.equals(libro)) continue;
            if (in.p_IdClasificacionTipoInstrumento != null && !in.p_IdClasificacionTipoInstrumento.equalsIgnoreCase(clasif)) continue;
            if (in.p_IdTipoInstrumento != null && !in.p_IdTipoInstrumento.equalsIgnoreCase(tipoInstr)) continue;
            if (in.p_IdMoneda != null && !in.p_IdMoneda.equalsIgnoreCase(moneda)) continue;
            if (in.p_IdCondicionDeLiquidacion != null && !in.p_IdCondicionDeLiquidacion.equalsIgnoreCase(condicion)) continue;

            TransaccionSalida t = new TransaccionSalida();
            t.IdTransaccion                  = toLong(row.get("IdTransaccion"));
            t.FolioTransaccion               = str(row, "FolioTransaccion");
            t.FechaSistemaCreacion           = str(row, "FechaSistemaCreacion");
            t.HoraSistemaCreacion            = str(row, "HoraSistemaCreacion");
            t.Nemotecnico                    = str(row, "Nemotecnico");
            t.IdInstrumento                  = instrumento;
            t.IdTipoInstrumento              = tipoInstr;
            t.IdClasificacionTipoInstrumento = clasif;
            t.AccionOrden                    = accion;
            t.IdBolsa                        = bolsa;
            t.IdMoneda                       = moneda;
            t.IdCondicionDeLiquidacion       = condicion;
            t.NombreCondicionDeLiquidacion   = str(row, "NombreCondicionDeLiquidacion");
            t.IdLibroOrdenes                 = libro;
            t.NombreLibroOrdenes             = str(row, "NombreLibroOrdenes");
            t.Reservado                      = str(row, "Reservado");
            t.Cantidad                       = toBD(row.get("Cantidad"));
            t.Monto                          = toBD(row.get("Monto"));
            out.p_out_Transacciones.add(t);
        }
        return out;
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
        Object v = row.get(key); return v == null ? "" : v.toString();
    }

    private static String loadSql(String path) {
        try (var is = PrevisualizarLiberacionMasivaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
