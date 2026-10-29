package cl.bcs.plataforma.adapter.in;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

/**
 * File BLOB upload/download endpoint.
 * Source: FileManagerServlet — @WebServlet("/FileBlobManager")
 *        FileBlobManagerFactory, AnotacionClienteConcrete, AnotacionPortafolioConcrete,
 *        AnotacionRecaudacionAPV
 */
@RestController
public class FileBlobController {

    private static final Logger log = LoggerFactory.getLogger(FileBlobController.class);

    private final DataSource dataSource;

    public FileBlobController(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /**
     * POST /FileBlobManager — uploads a file blob.
     * Source: FileManagerServlet.doPost → manager.update()
     *
     * @param entity  one of: ANOTACION_PORTAFOLIO | ANOTACION_CLIENTE | ANOTACION_RECAUDACION_APV
     * @param id      record identifier
     * @param name    file name
     * @param file    multipart file
     */
    @PostMapping("/FileBlobManager")
    public ResponseEntity<String> upload(
            @RequestParam("entity") String entity,
            @RequestParam("id")     Long    id,
            @RequestParam("name")   String  name,
            @RequestParam("file")   MultipartFile file) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(id,     "id");
        Objects.requireNonNull(name,   "name");
        Objects.requireNonNull(file,   "file");

        String updateSql = resolveUpdateSql(entity);
        if (updateSql == null) {
            return ResponseEntity.badRequest().body("NOK");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBinaryStream(1, file.getInputStream());
            ps.setString(2, name);
            ps.setLong(3, id);
            ps.execute();
            conn.commit();
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error uploading file blob for entity={} id={}", entity, id, e);
            return ResponseEntity.status(500).body("NOK");
        }
    }

    /**
     * GET /FileBlobManager — downloads a file blob.
     * Source: FileManagerServlet.doGet → manager.download()
     */
    @GetMapping("/FileBlobManager")
    public void download(
            @RequestParam("entity") String entity,
            @RequestParam("id")     Long   id,
            HttpServletResponse response) throws Exception {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(id,     "id");

        String selectSql = resolveSelectSql(entity);
        if (selectSql == null) {
            response.getWriter().write("NOK");
            return;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fileName = rs.getString("NombreArchivo");
                    try (InputStream is = rs.getBlob("Archivo").getBinaryStream()) {
                        IOUtils.copy(is, response.getOutputStream());
                    }
                    response.setHeader("Content-disposition",
                            "inline; filename=" + (fileName != null ? fileName : "file"));
                    response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.getOutputStream().flush();
                }
            }
        } catch (Exception e) {
            log.error("Error downloading file blob for entity={} id={}", entity, id, e);
            response.getWriter().write("NOK");
        }
    }

    // ─── Pure-logic helpers (public static for characterization tests) ───────

    /**
     * Resolves the UPDATE SQL for a given entity type.
     * Source: FileBlobManagerFactory.getManager + Concrete.update SQL
     */
    public static String resolveUpdateSql(String entity) {
        Objects.requireNonNull(entity, "entity");
        return switch (entity) {
            case "ANOTACION_PORTAFOLIO" ->
                "UPDATE \"BSCL.Portafolio.BD::POR.Anotacion\" " +
                "SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = ?";
            case "ANOTACION_CLIENTE" ->
                "UPDATE \"BSCL.SocioNegocio.BD::CLI.Anotacion\" " +
                "SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = ?";
            case "ANOTACION_RECAUDACION_APV" ->
                "UPDATE \"BSCL.APV.BD::REC.Anotacion\" " +
                "SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = ?";
            default -> null;
        };
    }

    /**
     * Resolves the SELECT SQL for a given entity type.
     * Source: Concrete.download SQL
     */
    public static String resolveSelectSql(String entity) {
        Objects.requireNonNull(entity, "entity");
        return switch (entity) {
            case "ANOTACION_PORTAFOLIO" ->
                "SELECT \"NombreArchivo\", \"Archivo\" FROM \"BSCL.Portafolio.BD::POR.Anotacion\" " +
                "WHERE \"IdAnotacion\" = ?";
            case "ANOTACION_CLIENTE" ->
                "SELECT \"NombreArchivo\", \"Archivo\" FROM \"BSCL.SocioNegocio.BD::CLI.Anotacion\" " +
                "WHERE \"IdAnotacion\" = ?";
            case "ANOTACION_RECAUDACION_APV" ->
                "SELECT \"NombreArchivo\", \"Archivo\" FROM \"BSCL.APV.BD::REC.Anotacion\" " +
                "WHERE \"IdAnotacion\" = ?";
            default -> null;
        };
    }
}
