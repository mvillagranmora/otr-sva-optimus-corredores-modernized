package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AgregarLogService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarLogService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_LOG;
    private final String SQL_INSERT_DETALLE_LOG;
    private final String SQL_SELECT_PERSONA;
    private final String SQL_SELECT_CLIENTE;
    private final String SQL_SELECT_PORTAFOLIO;
    private static final String SQL_CALL_SP =
        "CALL \"BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java\"(?)";

    public AgregarLogService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_LOG = loadSql("cl/bcs/plataforma/pla/sql/AgregarLog_InsertLog.sql");
        this.SQL_INSERT_DETALLE_LOG = loadSql("cl/bcs/plataforma/pla/sql/AgregarLog_InsertDetalleLog.sql");
        this.SQL_SELECT_PERSONA = loadSql("cl/bcs/plataforma/pla/sql/AgregarLog_SelectPersona.sql");
        this.SQL_SELECT_CLIENTE = loadSql("cl/bcs/plataforma/pla/sql/AgregarLog_SelectCliente.sql");
        this.SQL_SELECT_PORTAFOLIO = loadSql("cl/bcs/plataforma/pla/sql/AgregarLog_SelectPortafolio.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AgregarLog"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AgregarLog");
        InAgregarLog in = GSON.fromJson(params, InAgregarLog.class);
        Salida out = new Salida();

        if (in.p_Log == null || in.p_Log.isEmpty()) {
            return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
        }

        try {
            long idTrabajo = (long) (Math.random() * 10000);

            // Enrich log entries with persona/cliente/portafolio info
            List<Long> idsPersona = new ArrayList<>();
            List<Long> idsCliente = new ArrayList<>();
            List<Long> idsPortafolio = new ArrayList<>();
            for (LogEntry l : in.p_Log) {
                if (l.IdPersona != null && l.IdPersona != 0) idsPersona.add(l.IdPersona);
                if (l.IdCliente != null && l.IdCliente != 0) idsCliente.add(l.IdCliente);
                if (l.IdPortafolio != null && l.IdPortafolio != 0) idsPortafolio.add(l.IdPortafolio);
            }

            Map<Long, Persona> hPersona = queryPersonas(idsPersona);
            Map<Long, Cliente> hCliente = queryClientes(idsCliente);
            Map<Long, Portafolio> hPortafolio = queryPortafolios(idsPortafolio);

            List<LogEntry> cabeceras = prepararCabeceras(in, idTrabajo, hPersona, hCliente, hPortafolio);
            Map<Long, LogEntry> hCabecera = new HashMap<>();
            for (LogEntry c : cabeceras) hCabecera.put(c.IdCabeceraLog, c);

            List<DetalleLog> detalle = prepararDetalle(in, hCabecera);

            // Batch insert into temp tables
            if (!cabeceras.isEmpty()) {
                SqlParameterSource[] batch = cabeceras.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT_LOG, batch);
            }
            if (!detalle.isEmpty()) {
                SqlParameterSource[] batch = detalle.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT_DETALLE_LOG, batch);
            }

            // Call stored procedure
            jdbc.getJdbcTemplate().update(SQL_CALL_SP, idTrabajo);

        } catch (Exception e) {
            log.error("Error en AgregarLog: {}", e.getMessage(), e);
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private Map<Long, Persona> queryPersonas(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        List<Persona> list = jdbc.query(SQL_SELECT_PERSONA,
                new MapSqlParameterSource("IdPersona", ids),
                BeanPropertyRowMapper.newInstance(Persona.class));
        Map<Long, Persona> map = new HashMap<>();
        for (Persona p : list) map.put(p.IdPersona, p);
        return map;
    }

    private Map<Long, Cliente> queryClientes(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        List<Cliente> list = jdbc.query(SQL_SELECT_CLIENTE,
                new MapSqlParameterSource("IdCliente", ids),
                BeanPropertyRowMapper.newInstance(Cliente.class));
        Map<Long, Cliente> map = new HashMap<>();
        for (Cliente c : list) map.put(c.IdCliente, c);
        return map;
    }

    private Map<Long, Portafolio> queryPortafolios(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        List<Portafolio> list = jdbc.query(SQL_SELECT_PORTAFOLIO,
                new MapSqlParameterSource("IdPortafolio", ids),
                BeanPropertyRowMapper.newInstance(Portafolio.class));
        Map<Long, Portafolio> map = new HashMap<>();
        for (Portafolio p : list) map.put(p.IdPortafolio, p);
        return map;
    }

    private List<LogEntry> prepararCabeceras(InAgregarLog in, long idTrabajo,
            Map<Long, Persona> hPersona, Map<Long, Cliente> hCliente, Map<Long, Portafolio> hPortafolio) {
        List<LogEntry> result = new ArrayList<>();
        for (LogEntry l : in.p_Log) {
            if (l.IdCabeceraLog == null || l.IdCabeceraLog == 0) continue;

            boolean ya = result.stream().anyMatch(c -> c.IdCabeceraLog.equals(l.IdCabeceraLog)
                    && c.IdTipoLog.equalsIgnoreCase(l.IdTipoLog)
                    && c.IdTipoRegistro.equalsIgnoreCase(l.IdTipoRegistro));
            if (ya) continue;

            LogEntry obj = new LogEntry();
            obj.IdTrabajo = idTrabajo;
            obj.IdCabeceraLog = l.IdCabeceraLog;
            obj.IdTipoLog = l.IdTipoLog != null ? l.IdTipoLog : "";
            obj.IdTipoRegistro = l.IdTipoRegistro != null ? l.IdTipoRegistro : "";
            obj.IdModulo = l.IdModulo != null ? l.IdModulo : (in.p_IdModulo != null ? in.p_IdModulo : "");
            obj.IdUsuario = l.IdUsuario != null ? l.IdUsuario : (in.p_IdUsuario != null ? in.p_IdUsuario : 0L);
            obj.IdEmpresa = l.IdEmpresa != null ? l.IdEmpresa : (in.p_IdEmpresa != null ? in.p_IdEmpresa : 0L);
            obj.FechaSistemaLog = l.FechaSistemaLog != null ? l.FechaSistemaLog : "";
            obj.IdUsuarioAtendido = l.IdUsuarioAtendido != null ? l.IdUsuarioAtendido : obj.IdUsuario;
            obj.IdRegistro = l.IdRegistro;
            obj.EstadoRegistro = (in.p_IdEstadoAutorizacion != null && !in.p_IdEstadoAutorizacion.trim().isEmpty())
                    ? in.p_IdEstadoAutorizacion : "";
            if (l.EstadoRegistro != null && !l.EstadoRegistro.isBlank()) obj.EstadoRegistro = l.EstadoRegistro;
            obj.IdTipoOperacion = l.IdTipoOperacion;
            obj.Folio = l.Folio;
            obj.Cantidad = l.Cantidad;
            obj.PrecioTasa = l.PrecioTasa;
            obj.Monto = l.Monto;
            obj.IdMonedaOrigen = l.IdMonedaOrigen;
            obj.IdMonedaLiquidacion = l.IdMonedaLiquidacion;
            obj.TipoCambio = l.TipoCambio;
            obj.Nemo = l.Nemo;
            obj.IdProceso = l.IdProceso;
            obj.EstadoProceso = l.EstadoProceso != null && !l.EstadoProceso.isBlank() ? l.EstadoProceso : obj.EstadoRegistro;
            obj.NombreProceso = l.NombreProceso;

            // Enrich from persona/cliente/portafolio
            if (l.IdPersona != null && l.IdPersona != 0) {
                obj.IdPersona = l.IdPersona;
                Persona p = hPersona.get(l.IdPersona);
                if (p != null) {
                    obj.NombreCliente = nvl(p.NombreCompleto);
                    obj.IdDocumentoIdentidad = nvl(p.DocumentoIdentidad);
                    obj.IdTipoDocumentoIdentidad = nvl(p.TipoDocumentoIdentidad);
                }
            } else if (l.IdCliente != null && l.IdCliente != 0) {
                obj.IdCliente = l.IdCliente;
                Cliente c = hCliente.get(l.IdCliente);
                if (c != null) {
                    obj.NombreCliente = nvl(c.NombreCompleto);
                    obj.IdDocumentoIdentidad = nvl(c.DocumentoIdentidad);
                    obj.IdTipoDocumentoIdentidad = nvl(c.TipoDocumentoIdentidad);
                    obj.EstadoCliente = nvl(c.EstadoCliente);
                }
            } else if (l.IdPortafolio != null && l.IdPortafolio != 0) {
                obj.IdPortafolio = l.IdPortafolio;
                Portafolio p = hPortafolio.get(l.IdPortafolio);
                if (p != null) {
                    obj.NombreCliente = nvl(p.NombreCompleto);
                    obj.IdDocumentoIdentidad = nvl(p.DocumentoIdentidad);
                    obj.IdTipoDocumentoIdentidad = nvl(p.TipoDocumentoIdentidad);
                    obj.EstadoPortafolio = nvl(p.EstadoPortafolio);
                }
            }

            result.add(obj);
        }
        return result;
    }

    private List<DetalleLog> prepararDetalle(InAgregarLog in, Map<Long, LogEntry> hCabecera) {
        List<DetalleLog> result = new ArrayList<>();
        if (in.p_LogDetalle == null) return result;
        for (DetalleLog dl : in.p_LogDetalle) {
            if (dl.IdCabeceraLog == null || dl.IdCabeceraLog == 0) continue;
            LogEntry cab = hCabecera.get(dl.IdCabeceraLog);
            if (cab == null) continue;
            DetalleLog obj = new DetalleLog();
            obj.IdTrabajo = cab.IdTrabajo;
            obj.IdCabeceraLog = cab.IdCabeceraLog;
            obj.SubRegistro = dl.SubRegistro;
            obj.CampoRegistro = dl.CampoRegistro;
            obj.ValorAntiguo = dl.ValorAntiguo;
            obj.ValorNuevo = dl.ValorNuevo != null && dl.ValorNuevo.length() > 5000
                    ? dl.ValorNuevo.substring(0, 5000) : dl.ValorNuevo;
            result.add(obj);
        }
        return result;
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    // Input/Output classes
    public static class InAgregarLog {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_IdEstadoAutorizacion;
        public Long p_IdTrabajo = 0L;
        public List<LogEntry> p_Log = new ArrayList<>();
        public List<DetalleLog> p_LogDetalle = new ArrayList<>();
    }

    public static class LogEntry {
        public Long IdTrabajo = 0L;
        public Long IdCabeceraLog;
        public String IdTipoLog = "";
        public String IdModulo = "";
        public Long IdUsuario = 0L;
        public Long IdEmpresa = 0L;
        public String FechaSistemaLog = "";
        public Long IdUsuarioAtendido = 0L;
        public Long IdCliente = 0L;
        public String EstadoCliente = "";
        public String NombreCliente = "";
        public String IdTipoDocumentoIdentidad = "";
        public String IdDocumentoIdentidad = "";
        public Long IdPortafolio = 0L;
        public String EstadoPortafolio = "";
        public String IdTipoRegistro = "";
        public String IdRegistro = "";
        public String EstadoRegistro = "";
        public String IdTipoOperacion = "";
        public Long Folio = 0L;
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public BigDecimal PrecioTasa = BigDecimal.ZERO;
        public BigDecimal Monto = BigDecimal.ZERO;
        public String IdMonedaOrigen = "";
        public String IdMonedaLiquidacion = "";
        public String TipoCambio = "";
        public String Nemo = "";
        public Long IdProceso = 0L;
        public String EstadoProceso = "";
        public String NombreProceso = "";
        public Long IdPersona = 0L;
    }

    public static class DetalleLog {
        public Long IdTrabajo;
        public Long IdCabeceraLog;
        public String SubRegistro;
        public String CampoRegistro;
        public String ValorAntiguo;
        public String ValorNuevo;
    }

    public static class Persona {
        public Long IdPersona = 0L;
        public String NombreCompleto = "";
        public String DocumentoIdentidad = "";
        public String TipoDocumentoIdentidad = "";
    }

    public static class Cliente extends Persona {
        public Long IdCliente = 0L;
        public String EstadoCliente = "";
    }

    public static class Portafolio extends Persona {
        public Long IdPortafolio = 0L;
        public String CodigoPortafolio = "";
        public String EstadoPortafolio = "";
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private static String loadSql(String path) {
        try (InputStream is = AgregarLogService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
