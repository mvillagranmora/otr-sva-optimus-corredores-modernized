package cl.bcs.fondomutuo.normal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.normal.CasoNormal.Perfil;

/**
 * Option A (HANA_DDL_COMMIT_HALLAZGO) and the RESIDUAL RISK it leaves, pinned. The gates G1/G2/G4 cannot be decided before the lock DDL (they depend on which
 * requests survive the validations, which read the database: see TRANSFORMATION_NOTES, Step 6), so a write made BEFORE the real pass (the simulation pass of the
 * Manejador writes the NN commission, D-NOR-6) is committed by the implicit COMMIT of {@code CREATE LOCAL TEMPORARY TABLE} on HANA, and the closed gate then
 * answers 503: TomEE would repeat the whole request. The module answers the 503 in the service (this test); the HTTP boundary turns it into the generic 500
 * ({@code EscrituraConfirmada}, {@code JavaCallControllerEscrituraConfirmadaTest}). H2 does not commit on DDL, so the emulation is switched on explicitly.
 */
class NormalDdlConfirmaResidualTest {

    private static JsonObject conFilaPendiente(boolean ddlConfirma) throws Exception {
        CasoNormal.commitEnDdl = ddlConfirma;
        CasoNormal.antesDelServicio = (Connection raw) -> {
            try (var st = raw.createStatement()) {
                st.execute("INSERT INTO DUMMY VALUES ('Y')");   // an uncommitted write of the same request, as the simulation pass leaves
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException(e);
            }
        };
        try {
            return CasoNormal.ejecutar("FMM-NOR-06_aporte-medio-no-cin-tesoreria", Perfil.G1_CERRADA);   // F2 W1: G1 is lifted; the residual risk remains with G1 configured closed
        } finally {
            CasoNormal.commitEnDdl = false;
            CasoNormal.antesDelServicio = null;
        }
    }

    @Test
    @DisplayName("H2 (no implicit commit): the 503 of G1 rolls back the earlier write")
    void h2_elRollbackDeshaceLoAnterior() throws Exception {
        JsonObject r = conFilaPendiente(false);
        assertEquals("ROLLBACK_503", r.get("decisionServlet").getAsString());
        assertEquals("G1", r.get("compuerta").getAsString());
        assertEquals(0L, r.get("filasDummyY").getAsLong());
    }

    @Test
    @DisplayName("HANA behaviour emulated: the DDL of the lock committed the earlier write, the 503 of G1 still leaves (residual risk, option A)")
    void hana_elDdlConfirmoLaEscrituraPrevia_yAunAsiHay503() throws Exception {
        JsonObject r = conFilaPendiente(true);
        assertEquals("ROLLBACK_503", r.get("decisionServlet").getAsString());
        assertEquals("G1", r.get("compuerta").getAsString());
        assertEquals(1L, r.get("filasDummyY").getAsLong(), "the write before the DDL survived the rollback: this is the residual risk documented in Step 6");
        assertEquals(0, r.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size(), "the lock table was still dropped before the 503");
    }
}
