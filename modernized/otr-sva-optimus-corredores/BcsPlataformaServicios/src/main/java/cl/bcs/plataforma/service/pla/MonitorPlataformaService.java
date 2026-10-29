package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MonitorPlataformaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(MonitorPlataformaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private static final String SQL_FECHA_SISTEMA =
        "SELECT TO_VARCHAR(\"FechaSistema\", 'YYYY-MM-DD') AS FechaSistema "
        + "FROM \"BSCL.Plataforma.BD::PLA.FechaSistema\" WHERE \"IdEmpresa\" = :IdEmpresa";
    private static final String SQL_MODULO_SISTEMA =
        "SELECT \"IdEstado\", \"TipoEstado\" FROM \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\" "
        + "WHERE \"IdModulo\" = 'SIS' AND \"IdEmpresa\" = :IdEmpresa";

    public MonitorPlataformaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.MonitorPlataforma"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::MonitorPlataforma");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        // Check: DB connectivity (if we got here, it works)
        out.P_DETALLE.add(new Detalle("DB", "Conectividad HANA", "OK", "Conexión a base de datos exitosa"));

        // Check: fecha sistema vs fecha máquina
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_FECHA_SISTEMA,
                    new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa != null ? in.p_IdEmpresa : 1L));
            if (!rows.isEmpty()) {
                String fechaSistema = String.valueOf(rows.get(0).get("FechaSistema"));
                String fechaMaquina = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (fechaMaquina.equals(fechaSistema)) {
                    out.P_DETALLE.add(new Detalle("Comparación Fechas", "Sistema:AppServer",
                            "OK", "Fechas iguales: " + fechaSistema));
                } else {
                    out.P_DETALLE.add(new Detalle("Comparación Fechas", "Sistema:AppServer",
                            "ERROR", "Sistema=" + fechaSistema + ", AppServer=" + fechaMaquina));
                }
            }
        } catch (Exception e) {
            out.P_DETALLE.add(new Detalle("Comparación Fechas", "Sistema:AppServer",
                    "ERROR", "Excepción: " + e.getMessage()));
        }

        // Check: estado módulo sistema
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_MODULO_SISTEMA,
                    new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa != null ? in.p_IdEmpresa : 1L));
            if (!rows.isEmpty()) {
                String estado = String.valueOf(rows.get(0).get("TipoEstado"));
                out.P_DETALLE.add(new Detalle("Estado Módulo Sistema", "Apertura:Cierre",
                        "OK", "Estado actual: " + estado));
            }
        } catch (Exception e) {
            out.P_DETALLE.add(new Detalle("Estado Módulo Sistema", "Apertura:Cierre",
                    "ERROR", "Excepción: " + e.getMessage()));
        }

        // Check: disk space
        try {
            for (File root : File.listRoots()) {
                long totalMB = root.getTotalSpace() / 1_048_576;
                long freeMB = root.getFreeSpace() / 1_048_576;
                double pctDisponible = totalMB == 0 ? 100.0
                        : Math.floor(root.getFreeSpace() * 100.0 / root.getTotalSpace() * 100) / 100;
                String detalle = "Total:" + totalMB + "MB, Libre:" + freeMB + "MB, Disponible:" + pctDisponible + "%";
                if (totalMB > 0) {
                    out.P_DETALLE.add(new Detalle("Status AppServer", "Unidad " + root.getAbsolutePath(),
                            pctDisponible < 10 ? "ERROR" : "OK", detalle));
                }
            }
        } catch (Exception e) {
            out.P_DETALLE.add(new Detalle("Status AppServer", "", "ERROR", "Excepción: " + e.getMessage()));
        }

        // Stub checks (external services not yet migrated)
        out.P_DETALLE.add(new Detalle("MD_RV", "Ping TCP port", "PENDIENTE", "Market Data RV no configurado"));
        out.P_DETALLE.add(new Detalle("MD_RF", "Ping TCP port", "PENDIENTE", "Market Data RF no configurado"));
        out.P_DETALLE.add(new Detalle("Valorización", "Valorizar instrumento", "PENDIENTE", "ValorizarWs no migrado aún"));
        out.P_DETALLE.add(new Detalle("DTE", "Login", "PENDIENTE", "DTE login no migrado aún"));

        out.P_OUT_ERRORESCONTADOR = out.P_DETALLE.stream().filter(d -> "ERROR".equals(d.codigoValidacion)).count();
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_NotificarAdministradoresTI;
    }

    public static class Detalle {
        public String clasificacion;
        public String subClasificacion;
        public String codigoValidacion;
        public String descripcionValidacion;
        public Detalle(String c, String sc, String cv, String dv) {
            clasificacion = c; subClasificacion = sc; codigoValidacion = cv; descripcionValidacion = dv;
        }
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Detalle> P_DETALLE = new ArrayList<>();
    }
}
