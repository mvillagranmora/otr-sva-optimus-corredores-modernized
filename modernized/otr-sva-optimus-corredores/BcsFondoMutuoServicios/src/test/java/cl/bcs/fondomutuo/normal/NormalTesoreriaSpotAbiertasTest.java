package cl.bcs.fondomutuo.normal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.CasoNormal.Perfil;

/**
 * FM-NOR-F2: with the gates G1/G2 OPEN and the two F2 collaborators scripted (they are not ported in 4A: no golden, the legacy ran them against H2 without Tesoreria / Mesa
 * tables), the code AFTER the gates behaves as the legacy source reads: results of Tesoreria and Spot are applied to the right request, and D-NOR-5 (the FIRST request is
 * the one rejected when a collaborator reports an error) is kept bug for bug.
 */
class NormalTesoreriaSpotAbiertasTest {

    private static final String SOLICITUDES = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";

    @AfterEach
    void cerrar() {
        SujetoNormal.tesoreriaF2 = null;
        SujetoNormal.spotF2 = null;
    }

    private static JsonArray filas(JsonObject r) {
        return r.getAsJsonObject("tablas").getAsJsonArray(SOLICITUDES);
    }

    @Test
    @DisplayName("FM-NOR-F2a Tesoreria answers: the generated entry id goes to the request that has that reference")
    void tesoreriaAsignaElIngreso() throws Exception {
        SujetoNormal.tesoreriaF2 = e -> {
            ProcesosExternos.SalidaTesoreria s = new ProcesosExternos.SalidaTesoreria();
            ProcesosExternos.IngresoEgresoGenerado g = new ProcesosExternos.IngresoEgresoGenerado();
            g.NumeroReferencia = 1L;
            g.IdIngresoEgreso = 777L;
            s.P_OUT_INGRESOEGRESOGENERADO.add(g);
            return s;
        };
        JsonObject r = CasoNormal.ejecutar("FMM-NOR-06_aporte-medio-no-cin-tesoreria", Perfil.ABIERTAS);
        assertEquals("COMMIT", r.get("decisionServlet").getAsString(), r.toString());
        assertEquals(1, filas(r).size());
        assertEquals(777L, filas(r).get(0).getAsJsonObject().get("IdIngresoEgreso").getAsLong());
    }

    @Test
    @DisplayName("FM-NOR-F2b D-NOR-5: Tesoreria reports an error: the FIRST request is rejected, not the one that failed")
    void dNor5Tesoreria() throws Exception {
        SujetoNormal.tesoreriaF2 = e -> {
            ProcesosExternos.SalidaTesoreria s = new ProcesosExternos.SalidaTesoreria();
            s.P_OUT_MENSAJES.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ERROR, "ERR_TES", "falla de la solicitud 2"));
            return s;
        };
        JsonObject r = CasoNormal.ejecutar("FMM-NOR-52_dos-aportes-tra-tesoreria", Perfil.ABIERTAS);
        assertTrue(r.has("salida"), "exception: " + r.get("excepcion"));
        JsonObject salida = r.getAsJsonObject("salida");
        assertEquals(1L, salida.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertTrue(salida.getAsJsonArray("P_OUT_MENSAJES").toString().contains("TES : falla de la solicitud 2"));
        assertEquals(1, salida.getAsJsonArray("P_OUT_RESULTADO").size());
        assertEquals(2L, salida.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("IdInterno").getAsLong(), "the second request survives: the first was rejected");
        assertEquals("ROLLBACK", r.get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-NOR-F2c Spot answers: the operation id goes to its request; a rejected operation rejects the request")
    void spot() throws Exception {
        SujetoNormal.spotF2 = e -> {
            ProcesosExternos.SalidaSpot s = new ProcesosExternos.SalidaSpot();
            ProcesosExternos.OperacionSpot o = new ProcesosExternos.OperacionSpot();
            o.IdInterno = "1";
            o.Estado = "OK";
            o.IdOperacionSpot = 55L;
            s.P_OUT_IDOPERACIONSPOT.add(o);
            return s;
        };
        JsonObject ok = CasoNormal.ejecutar("FMM-NOR-07_aporte-usd-genera-spot", Perfil.ABIERTAS);
        assertEquals("COMMIT", ok.get("decisionServlet").getAsString(), ok.toString());
        assertEquals(55L, filas(ok).get(0).getAsJsonObject().get("IdOperacionSpot").getAsLong());
        SujetoNormal.spotF2 = e -> {
            ProcesosExternos.SalidaSpot s = new ProcesosExternos.SalidaSpot();
            ProcesosExternos.OperacionSpot o = new ProcesosExternos.OperacionSpot();
            o.IdInterno = "1";
            o.Estado = "RECH";
            s.P_OUT_IDOPERACIONSPOT.add(o);
            return s;
        };
        JsonObject rech = CasoNormal.ejecutar("FMM-NOR-07_aporte-usd-genera-spot", Perfil.ABIERTAS);
        assertEquals(0, filas(rech).size());
        assertEquals(0, rech.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").size());
    }

    /** Copy of an input row through the legacy plain Gson. */
    static final class JsonLegacyCopia {
        static NormalContrato.InSolicitud copiar(NormalContrato.InSolicitud s) {
            var g = cl.bcs.fondomutuo.kit.JsonLegacy.plano();
            return g.fromJson(g.toJson(s), NormalContrato.InSolicitud.class);
        }
    }
}
