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
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

@Service
public class FirmasImageManagerService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(FirmasImageManagerService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_DELETE;

    public FirmasImageManagerService(DataSource dataSource, NamedParameterJdbcTemplate jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/FirmasImageManager_SelectImagenes.sql");
        this.SQL_DELETE = loadSql("cl/bcs/plataforma/pla/sql/FirmasImageManager_DeleteFirma.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.FirmasImageManager"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::FirmasImageManager");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();
        try {
            switch (in.operacion == null ? "" : in.operacion) {
                case "CREATE" -> handleCreate(in, out);
                case "UPDATE" -> handleUpdate(in, out);
                case "DELETE" -> handleDelete(in, out);
                case "SELECT" -> handleSelect(in, out);
                default -> { out.P_OUT_ERRORESCONTADOR = 1L; out.P_OUT_MENSAJES = "OPERACION_DESCONOCIDA"; }
            }
        } catch (Exception e) {
            log.error("Error en FirmasImageManager: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES = e.getMessage();
        }
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private List<Map<String, Object>> existeFirma(Entrada in) {
        return jdbc.queryForList(SQL_SELECT, new MapSqlParameterSource()
            .addValue("IdPersona", in.idPersona)
            .addValue("IdDocumentoIdentidad", in.idDocumento));
    }

    private byte[] imagenLista(Entrada in) throws Exception {
        if (in.data == null) return null;
        String base64Data = in.data.contains(",") ? in.data.split(",")[1] : in.data;
        byte[] raw = Base64.getDecoder().decode(base64Data);
        ByteArrayInputStream bis = new ByteArrayInputStream(raw);
        BufferedImage img = ImageIO.read(bis);
        if (img == null) return raw;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, in.ext != null ? in.ext : "jpg", bos);
        return bos.toByteArray();
    }

    private void handleCreate(Entrada in, Salida out) throws Exception {
        List<Map<String, Object>> existing = existeFirma(in);
        if (!existing.isEmpty()) { out.P_OUT_ERRORESCONTADOR = 1L; out.P_OUT_MENSAJES = "UI_FIRMAEXISTE"; return; }
        byte[] img = imagenLista(in);
        if (img == null) { out.P_OUT_ERRORESCONTADOR = 0L; return; }
        String sql = "INSERT INTO \"BSCL\".\"BSCL.SocioNegocio.BD::PER.ImagenesFirmas\"(\"IdEmpresa\",\"IdImagen\",\"NombreImagen\",\"IdDocumentoIdentidad\",\"IdPersona\",\"Tipo\",\"Extension\",\"Version\",\"Binario\") VALUES(?,CAST(SYSUUID AS VARCHAR),?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, in.idEmpresa); ps.setString(2, in.name);
            ps.setLong(3, in.idDocumento); ps.setLong(4, in.idPersona);
            ps.setString(5, in.type); ps.setString(6, in.ext); ps.setLong(7, in.version);
            ps.setBinaryStream(8, new ByteArrayInputStream(img));
            ps.execute();
        }
        out.P_OUT_ERRORESCONTADOR = 0L; out.P_OUT_MENSAJES = "UI_FIRMACREADA";
    }

    private void handleUpdate(Entrada in, Salida out) throws Exception {
        List<Map<String, Object>> existing = existeFirma(in);
        if (existing.isEmpty()) { out.P_OUT_ERRORESCONTADOR = 1L; out.P_OUT_MENSAJES = "UI_FIRMANOEXISTE"; return; }
        byte[] img = imagenLista(in);
        if (img == null) { out.P_OUT_ERRORESCONTADOR = 1L; return; }
        String sql = "UPDATE \"BSCL\".\"BSCL.SocioNegocio.BD::PER.ImagenesFirmas\" SET \"NombreImagen\"=?,\"Tipo\"=?,\"Extension\"=?,\"Version\"=?,\"Binario\"=? WHERE \"IdPersona\"=? AND \"IdDocumentoIdentidad\"=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, in.name); ps.setString(2, in.type); ps.setString(3, in.ext);
            ps.setLong(4, in.version); ps.setBinaryStream(5, new ByteArrayInputStream(img));
            ps.setLong(6, in.idPersona); ps.setLong(7, in.idDocumento);
            ps.execute();
        }
        out.P_OUT_ERRORESCONTADOR = 0L; out.P_OUT_MENSAJES = "UI_FIRMAMODIFICADA";
    }

    private void handleDelete(Entrada in, Salida out) {
        List<Map<String, Object>> existing = existeFirma(in);
        if (existing.isEmpty()) { out.P_OUT_ERRORESCONTADOR = 1L; out.P_OUT_MENSAJES = "FIRMA NO EXISTE"; return; }
        jdbc.update(SQL_DELETE, new MapSqlParameterSource()
            .addValue("IdPersona", in.idPersona)
            .addValue("IdDocumentoIdentidad", in.idDocumento));
        out.P_OUT_ERRORESCONTADOR = 0L; out.P_OUT_MENSAJES = "UI_FIRMAELIMINADA";
    }

    private void handleSelect(Entrada in, Salida out) {
        List<Map<String, Object>> rows = existeFirma(in);
        if (rows.isEmpty()) { out.P_OUT_ERRORESCONTADOR = 0L; out.P_OUT_IMAGEN = ""; out.P_OUT_MENSAJES = "UI_SELECTNOOK"; return; }
        byte[] binario = (byte[]) rows.get(0).get("Binario");
        String tipo = (String) rows.get(0).get("Tipo");
        if (binario == null) { out.P_OUT_IMAGEN = ""; out.P_OUT_MENSAJES = "UI_SELECTNOOK"; return; }
        out.P_OUT_IMAGEN = "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(binario);
        out.P_OUT_ERRORESCONTADOR = 0L; out.P_OUT_MENSAJES = "UI_SELECTOK";
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Entrada {
        public Long idUsuario; public Long idEmpresa; public Long idDocumento;
        public Long idPersona; public String name; public Long version = 1L;
        public String file; public String type; public String ext; public String data;
        public String operacion;
    }

    public static class Salida {
        public String P_OUT_MENSAJES = ""; public String P_OUT_IMAGEN = "";
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }
}
