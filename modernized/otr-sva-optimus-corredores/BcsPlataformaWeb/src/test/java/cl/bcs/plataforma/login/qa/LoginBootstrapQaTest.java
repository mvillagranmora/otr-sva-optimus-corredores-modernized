package cl.bcs.plataforma.login.qa;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.Test;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.login.sesion.SesionDatos;

/**
 * QA only, READ ONLY: the bootstrap of the login page ({@code GET /PLA/bootstrap/login}) built from HANA QA, written to target/qa/nuevo-login.json to be
 * compared with the public login page of the legacy. Only SELECTs run (no procedure, no session); the connection is flagged read-only. Skipped without
 * the QA_HANA_* variables.
 */
class LoginBootstrapQaTest {

    @Test
    void bootstrap_de_login_en_solo_lectura() throws Exception {
        String url = System.getenv("QA_HANA_URL");
        String usuario = System.getenv("QA_HANA_USER");
        String clave = System.getenv("QA_HANA_PASSWORD");
        assumeTrue(url != null && usuario != null && clave != null, "HANA QA variables not set");
        try (Connection con = DriverManager.getConnection(url, usuario, clave)) {
            con.setReadOnly(true);
            con.setAutoCommit(false);
            JsonObject datos = new BootstrapServicio(new SesionDatos(), (c, ruta, in) -> {
                throw new IllegalStateException("no procedure is called by the login bootstrap: " + ruta);
            }).login(con);
            con.rollback();
            Path dir = Paths.get(System.getProperty("qa.salida", "target/qa"));
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("nuevo-login.json"), new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(datos), StandardCharsets.UTF_8);
        }
    }
}
