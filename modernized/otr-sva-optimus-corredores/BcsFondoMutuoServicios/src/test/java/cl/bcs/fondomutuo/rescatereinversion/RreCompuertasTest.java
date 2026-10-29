package cl.bcs.fondomutuo.rescatereinversion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.rescatereinversion.CasoRre.Perfil;

/**
 * Gates of RescateReinversion (plan 6) on the FMM-RRE goldens' inputs: before (list closed, the shipped default) = 503 without effects through BOTH entries;
 * G-CUS closed = 503 with the reinversion lock released BEFORE the 503 (canary of plan 7); G5 (AgregarLog / InscribirProcesoBatch answer 503 by the router)
 * = 503, rollback of the rows, lock released; any other exception after the lock = the legacy behaviour, the lock stays (D-F2-1).
 */
class RreCompuertasTest {

    private static final String PAR = "FMM-RRE-01_real-un-par-nacional";
    private static final String APV = "FMM-RRE-46_portafolio-apv-programa-batch";
    private static final String MAN = "FMM-RRE-M01_manejador-par-simple";

    @AfterEach
    void limpiar() {
        SujetoRre.logPrueba = null;
        SujetoRre.batchPrueba = null;
        CasoRre.fallarCrear = false;
        CasoRre.fallarEliminar = false;
    }

    private static void sinEfectos(JsonObject r) {
        JsonObject t = r.getAsJsonObject("tablas");
        for (String k : t.keySet()) assertThat(t.getAsJsonArray(k)).as(k).isEmpty();
        assertThat(r.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop")).isEmpty();
    }

    private static JsonArray autonomos(JsonObject r) {
        return r.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos");
    }

    @Test
    void antes_listaCerrada_entradaDirecta_503SinEfectos() throws Exception {
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.LISTA_CERRADA);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(r.get("compuerta").getAsString()).isEqualTo("G0");
        sinEfectos(r);
        assertThat(autonomos(r)).isEmpty();
        assertThat(r.getAsJsonArray("colaboradores")).as("nothing was called").isEmpty();
        assertThat(r.getAsJsonArray("secuenciasSolicitadas")).isEmpty();
    }

    @Test
    void antes_listaCerrada_manejador_503SinEfectos() throws Exception {
        JsonObject r = CasoRre.ejecutar(MAN, Perfil.LISTA_CERRADA);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(r.get("compuerta").getAsString()).isEqualTo("G0");
        sinEfectos(r);
        assertThat(autonomos(r)).isEmpty();
        assertThat(r.getAsJsonArray("colaboradores")).isEmpty();
    }

    @Test
    void conLaListaHabilitada_seSirve_directaYManejador() throws Exception {
        for (String id : new String[] {PAR, MAN}) {
            JsonObject r = CasoRre.ejecutar(id, Perfil.ABIERTAS);
            assertThat(r.get("decisionServlet").getAsString()).as(id).isEqualTo("COMMIT");
            assertThat(r.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate")).as(id).hasSize(1);
            RreEquivalenciaTest.comparar(id, CasoRre.golden(id), r);
        }
    }

    @Test
    void gCustodiaCerrada_503_conElBloqueoSoltadoAntes() throws Exception {
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.CUSTODIA_CERRADA);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(r.get("compuerta").getAsString()).isEqualTo("G-CUS");
        sinEfectos(r);
        assertThat(autonomos(r).toString()).contains("REINVERSION INSERT 1").contains("REINVERSION DELETE 1");
    }

    @Test
    void g5_agregarLogResponde503_rollbackYBloqueoSoltado() throws Exception {
        SujetoRre.logPrueba = e -> {
            throw new ServicioNoServidoException("ROUTER", "AgregarLog 503");
        };
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        sinEfectos(r);
        assertThat(autonomos(r).toString()).contains("REINVERSION DELETE 1");
    }

    @Test
    void g5_inscribirProcesoBatchResponde503_rollbackYBloqueoSoltado() throws Exception {
        SujetoRre.batchPrueba = e -> {
            throw new ServicioNoServidoException("ROUTER", "InscribirProcesoBatch 503");
        };
        JsonObject r = CasoRre.ejecutar(APV, Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        sinEfectos(r);
        assertThat(autonomos(r).toString()).contains("REINVERSION DELETE 1");
    }

    @Test
    void dF21_otraExcepcionTrasElBloqueo_elBloqueoAutonomoQueda() throws Exception {
        SujetoRre.logPrueba = e -> {
            throw new IllegalStateException("AgregarLog timeout");
        };
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_HTTP500");
        assertThat(r.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate")).isEmpty();
        assertThat(r.getAsJsonObject("tablas").getAsJsonArray(SesionRre.LOCK_TABLE)).as("autonomous lock left (D-F2-1)").hasSize(1);
        assertThat(r.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").toString()).contains("#p_BloqueoProcesarReinversion");
    }

    @Test
    void fallaElCallDeEliminar_soloMensaje_contadorIgual_yElDropCorre() throws Exception {
        CasoRre.fallarEliminar = true;
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
        assertThat(r.get("salidaCruda").getAsString()).contains("UI_ERROR_DELETE_BLOQUEO_REINVERSION");
        assertThat(r.getAsJsonObject("tablas").getAsJsonArray(SesionRre.LOCK_TABLE)).hasSize(1);
        assertThat(r.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop")).isEmpty();
    }

    @Test
    void fallaElCallDeCrear_UI_ERROR_BLOQUEO_REINVERSION_conDrop_DEV6H1() throws Exception {
        // DEV-6-H1 (approved 2026-10-26): the legacy left the session-local table; a failed lock now drops it so the next reinversion on the connection works
        CasoRre.fallarCrear = true;
        JsonObject r = CasoRre.ejecutar(PAR, Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK");
        assertThat(r.get("salidaCruda").getAsString()).contains("UI_ERROR_BLOQUEO_REINVERSION");
        assertThat(r.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop")).isEmpty();
    }

    @Test
    void fallaElInsertDelBloqueo_conDrop_DEV6H1_yUnaExcepcionPosteriorSigueDejandolaComoElLegacy() throws Exception {
        // the INSERT of the lock fails (id longer than 70): the table is dropped
        JsonObject insert = CasoRre.ejecutar("FMM-RRE-10_bloqueo-id-mas-de-70", Perfil.ABIERTAS);
        assertThat(insert.get("salidaCruda").getAsString()).contains("UI_ERROR_BLOQUEO_REINVERSION");
        assertThat(insert.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop")).isEmpty();
        // an exception AFTER the lock was made is not covered by the approval: lock and table stay (D-F2-1)
        JsonObject tarde = CasoRre.ejecutar("FMM-RRE-21_excepcion-tras-el-bloqueo-lo-deja", Perfil.ABIERTAS);
        assertThat(tarde.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").toString()).contains("#p_BloqueoProcesarReinversion");
    }
}
