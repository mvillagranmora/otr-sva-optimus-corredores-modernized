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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class ExportarRolesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ExportarRolesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_ROLES;

    public ExportarRolesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_ROLES = loadSql("cl/bcs/plataforma/acc/sql/ConsultaRolesPerfil_Select.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ExportarRoles"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ExportarRoles");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;

            List<RolPerfilAcceso> lista = jdbc.query(SQL_ROLES,
                new MapSqlParameterSource("IdEmpresa", idEmpresa),
                BeanPropertyRowMapper.newInstance(RolPerfilAcceso.class));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(0xef); bos.write(0xbb); bos.write(0xbf);
            try (OutputStreamWriter w = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
                w.write("Nombre Rol;Estado Rol;Nombre Perfil;Modulo Menu;Menu;Pestaña;Sub Pestaña;Boton\n");
                RolPerfilAcceso prev = new RolPerfilAcceso();
                for (RolPerfilAcceso a : lista) {
                    boolean mismoRol    = eq(a.NombreRol,    prev.NombreRol);
                    boolean mismoPerfil = eq(a.NombrePerfil, prev.NombrePerfil);
                    boolean mismoMod    = eq(a.Modulo,       prev.Modulo);
                    boolean mismoAcc    = eq(a.NombreAcceso,  prev.NombreAcceso);
                    boolean mismo1      = eq(a.PrimerNivel,  prev.PrimerNivel);
                    boolean mismo2      = eq(a.SegundoNivel, prev.SegundoNivel);
                    prev = a;

                    String cRol    = mismoRol    ? "" : s(a.NombreRol);
                    String cEst    = mismoRol    ? "" : s(a.EstadoRol);
                    String cPerfil = mismoPerfil ? "" : s(a.NombrePerfil);
                    String cMod    = mismoMod    ? "" : s(a.Modulo);
                    String cAcc    = mismoAcc    ? "" : s(a.NombreAcceso);

                    if (isEmpty(a.PrimerNivel)) {
                        w.write(csv(cRol, cEst, cPerfil, cMod, cAcc, "", "", ""));
                    } else if (isEmpty(a.SegundoNivel)) {
                        w.write(csv(cRol, cEst, cPerfil, cMod, cAcc, "", "", s(a.PrimerNivel)));
                    } else if (isEmpty(a.TercerNivel)) {
                        w.write(csv(cRol, cEst, cPerfil, cMod, cAcc, mismo1 ? "" : s(a.PrimerNivel), "", s(a.SegundoNivel)));
                    } else {
                        w.write(csv(cRol, cEst, cPerfil, cMod, cAcc, mismo1 ? "" : s(a.PrimerNivel), mismo2 ? "" : s(a.SegundoNivel), s(a.TercerNivel)));
                    }
                }
            }

            out.P_RutaNombreArchivo = "ACC/ReporteRolesPerfil.csv";
            out.P_CsvData = Base64.getEncoder().encodeToString(bos.toByteArray());
            out.P_Out_errores = 0L;
        } catch (Exception e) {
            log.error("Error en ExportarRoles: {}", e.getMessage(), e);
            out.P_Out_errores = 1L;
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private boolean eq(String a, String b) { return a != null && a.equalsIgnoreCase(b); }
    private boolean isEmpty(String s) { return s == null || s.isBlank(); }
    private String s(String v) { return v == null ? "" : v; }

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

    public static class RolPerfilAcceso {
        public Long IdEmpresa; public String NombreRol; public Long IdRol;
        public String EstadoRol; public String NombrePerfil; public Long IdPerfil;
        public String NombreAcceso; public String TipoAcceso; public String Modulo;
        public String PrimerNivel; public String SegundoNivel; public String TercerNivel;
    }

    public static class Salida {
        public Long P_Out_errores = 1L;
        public String P_RutaNombreArchivo = "";
        public String P_CsvData = "";
    }
}
