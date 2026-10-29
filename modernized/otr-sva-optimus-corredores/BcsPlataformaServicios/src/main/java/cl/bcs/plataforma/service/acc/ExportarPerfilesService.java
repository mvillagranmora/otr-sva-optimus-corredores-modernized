package cl.bcs.plataforma.service.acc;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class ExportarPerfilesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ExportarPerfilesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_PERFILES;

    public ExportarPerfilesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_PERFILES = loadSql("cl/bcs/plataforma/acc/sql/ConsultaPerfilesAcceso_Select.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ExportarPerfiles"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ExportarPerfiles");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;

            List<PerfilAcceso> lista = jdbc.query(SQL_PERFILES,
                new MapSqlParameterSource("IdEmpresa", idEmpresa),
                BeanPropertyRowMapper.newInstance(PerfilAcceso.class));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(0xef); bos.write(0xbb); bos.write(0xbf);
            try (OutputStreamWriter w = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
                w.write("Nombre Perfil;Estado;Modulo Menu;Accesos\n");
                if (!lista.isEmpty()) {
                    w.write(csv(lista.get(0).NombrePerfil, lista.get(0).EstadoPerfil, lista.get(0).NombreMenu, lista.get(0).NombreAcceso));
                    for (int i = 1; i < lista.size(); i++) {
                        PerfilAcceso cur = lista.get(i), prev = lista.get(i - 1);
                        String nombre = cur.NombrePerfil.equals(prev.NombrePerfil) ? "" : cur.NombrePerfil;
                        String estado = cur.NombrePerfil.equals(prev.NombrePerfil) ? "" : cur.EstadoPerfil;
                        String modulo = cur.NombrePerfil.equals(prev.NombrePerfil) &&
                                        cur.IdMenu != null && cur.IdMenu.equals(prev.IdMenu) ? "" : cur.NombreMenu;
                        w.write(csv(nombre, estado, modulo, cur.NombreAcceso));
                    }
                }
            }

            out.P_RutaNombreArchivo = "ACC/ReportePerfiles.csv";
            out.P_CsvData = Base64.getEncoder().encodeToString(bos.toByteArray());
            out.P_Out_errores = 0L;
        } catch (Exception e) {
            log.error("Error en ExportarPerfiles: {}", e.getMessage(), e);
            out.P_Out_errores = 1L;
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private String csv(String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(';');
            String v = cols[i] == null ? "" : cols[i].replace("\"", "\"\"");
            if (v.contains(";") || v.contains("\"") || v.contains("\n")) sb.append('"').append(v).append('"');
            else sb.append(v);
        }
        sb.append('\n');
        return sb.toString();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class PerfilAcceso {
        public Long IdPerfil; public String NombrePerfil; public String IdEstado;
        public String EstadoPerfil; public Long IdAcceso; public String NombreAcceso;
        public String TipoAcceso; public BigDecimal IdMenu; public String NombreMenu;
    }

    public static class Salida {
        public Long P_Out_errores = 1L;
        public String P_RutaNombreArchivo = "";
        public String P_CsvData = "";
    }
}
