package cl.bcs.plataforma.service.pla;

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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GenerarInformeParametrosSistemaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeParametrosSistemaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_PARAMETROS;

    public GenerarInformeParametrosSistemaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_PARAMETROS = loadSql("cl/bcs/plataforma/pla/sql/GenerarInformeParametrosSistema_SelectParametros.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.GenerarInformeParametrosSistema"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::GenerarInformeParametrosSistema");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;

            List<Parametro> parametros = jdbc.query(SQL_PARAMETROS,
                new MapSqlParameterSource("IdEmpresa", idEmpresa),
                BeanPropertyRowMapper.newInstance(Parametro.class));

            ByteArrayOutputStream csvBos = new ByteArrayOutputStream();
            try (OutputStreamWriter w = new OutputStreamWriter(csvBos, StandardCharsets.ISO_8859_1)) {
                w.write("Modulo;Codigo;Descripcion;Valor;Uso;Valor Defecto;Valores Posibles;Ambito;Creador;F.Creacion;Modificador;F.Modificacion;Observaciones\n");
                for (Parametro p : parametros) {
                    String ambito = "N".equals(p.ModificablePorUsuario) ? "CORREDOR" : "BCS";
                    String desc   = "N".equals(p.ModificablePorUsuario) ? s(p.Descripcion) : "******";
                    w.write(csv(s(p.Modulo), s(p.CodigoParametro), desc, s(p.Valor), s(p.Uso),
                        s(p.ValorDefecto), s(p.ValoresPosibles), ambito, s(p.CreadorParametro),
                        fmtTs(p.FechaCreacion), s(p.ModificadorParametro), fmtTs(p.FechaModificacion), s(p.Observaciones)));
                }
            }

            ByteArrayOutputStream zipBos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipBos)) {
                zos.putNextEntry(new ZipEntry("PARAMETROS.csv"));
                zos.write(csvBos.toByteArray());
                zos.closeEntry();
            }

            out.P_OUT_NOMBRE_ARCHIVO = "ARCHIVO_PARAMETROS_MAESTROS.ZIP";
            out.P_OUT_ZIP_DATA = Base64.getEncoder().encodeToString(zipBos.toByteArray());
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en GenerarInformeParametrosSistema: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private String s(String v) { return v == null ? "" : v; }

    private synchronized String fmtTs(Timestamp ts) {
        if (ts == null) return "0001-01-01 00:00:00";
        return TS_FMT.format(ts);
    }

    private String csv(String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(';');
            String v = cols[i] == null ? "" : cols[i].replace("\"", "\"\"");
            sb.append(v);
        }
        sb.append('\n');
        return sb.toString();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Parametro {
        public String Modulo; public Long IdEmpresa; public String CodigoParametro;
        public String Valor; public String Descripcion; public String Uso;
        public String ValorDefecto; public String ValoresPosibles;
        public String ModificablePorUsuario; public String ModificablePostProduccion;
        public String CreadorParametro; public Timestamp FechaCreacion;
        public String ModificadorParametro; public Timestamp FechaModificacion;
        public String Observaciones;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 1L;
        public String P_OUT_NOMBRE_ARCHIVO = "";
        public String P_OUT_ZIP_DATA = "";
    }
}
