package cl.bcs.fondomutuo.normal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.normal.CasoNormal.Perfil;

/** FM-NOR-G (gates G1..G4 and G-CUS closed: 503 before any write, lock released) and FM-NOR-D (defects kept bug for bug, D-NOR-4 JSON vs in-process). */
class NormalCompuertasYDefectosTest {

    private static void cerrada(String id, Perfil perfil, String compuerta) throws Exception {
        JsonObject r = CasoNormal.ejecutar(id, perfil);
        assertEquals("ROLLBACK_503", r.get("decisionServlet").getAsString(), id + " " + perfil + " :: " + r);
        assertEquals(compuerta, r.get("compuerta").getAsString(), id);
        NormalEquivalenciaTest.sinEscrituras(id, r);
        // the lock was taken (autonomous commit) and released BEFORE the 503, or TomEE would answer "duplicate" on the repeat (plan 6.3)
        boolean conBloqueo = !r.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").isEmpty();
        if (conBloqueo) {
            assertTrue(r.getAsJsonObject("sesion").getAsJsonArray("commitsAutonomos").toString().contains("DELETE 1"), id + " :: lock released: " + r.get("sesion"));
        }
    }

    @Test
    @DisplayName("FM-NOR-G1 closed: a contribution paid out of the investment account needs SaldoFlujoCaja (G3)")
    void g3() throws Exception {
        cerrada("FMM-NOR-04_aporte-cin-manual-saldo-ok", Perfil.CERRADAS, "G3");
    }

    @Test
    @DisplayName("FM-NOR-G2 closed: a rescue needs the custody flow (G-CUS)")
    void gCustodia() throws Exception {
        cerrada("FMM-NOR-P01_rescate-minimo-por-cuotas", Perfil.CERRADAS, "G-CUS");
        cerrada("FMM-NOR-15_rescate-mayor-a-custodia", Perfil.CERRADAS, "G-CUS");
    }

    @Test
    @DisplayName("FM-NOR-G3 closed: OWP notification (G4), after every validation, before the first write")
    void g4() throws Exception {
        cerrada("FMM-NOR-17_canal-owp-notifica", Perfil.SOLO_G4_CERRADA, "G4");
    }

    @Test
    @DisplayName("FM-NOR-G4 closed: Spot (G2) is an F2 W2 collaborator; G1 configured closed (reversion of F2 W1) still answers 503 before any write")
    void g1g2() throws Exception {
        cerrada("FMM-NOR-06_aporte-medio-no-cin-tesoreria", Perfil.G1_CERRADA, "G1");
        cerrada("FMM-NOR-87_tesoreria-aporte-tra-ingreso-generado", Perfil.G1_CERRADA, "G1");
        cerrada("FMM-NOR-07_aporte-usd-genera-spot", Perfil.ABIERTAS, "G2");
    }

    @Test
    @DisplayName("FM-NOR-G1 lifted (F2 W1): a non-CIN contribution is served; the helper is called and the generated movement is stored (before: 503)")
    void g1Levantada() throws Exception {
        JsonObject r = CasoNormal.ejecutar("FMM-NOR-87_tesoreria-aporte-tra-ingreso-generado", Perfil.ABIERTAS);
        assertEquals("COMMIT", r.get("decisionServlet").getAsString(), r.toString());
        assertTrue(r.get("colaboradores").toString().contains("\"colaborador\":\"CrearIngresoEgreso\""), "Tesoreria reached: " + r.get("colaboradores"));
        assertEquals(1, r.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate").size());
    }

    @Test
    @DisplayName("FM-NOR-G5 closed gates do not stop a simulation that needs no gate, nor a request that fails validation first")
    void sinCompuertaNoHay503() throws Exception {
        JsonObject r = CasoNormal.ejecutar("FMM-NOR-20_bloqueo-duplicado", Perfil.CERRADAS);
        assertEquals("ROLLBACK", r.get("decisionServlet").getAsString());
        JsonObject s = CasoNormal.ejecutar("FMM-NOR-21_lista-vacia", Perfil.CERRADAS);
        assertEquals("ROLLBACK", s.get("decisionServlet").getAsString());
    }

    @Test
    @DisplayName("FM-NOR-D4 D-NOR-4: by JSON the origin PAC is NOT exempt from the minimum equity; an in-process caller with the literal is")
    void dNor4Pac() throws Exception {
        JsonObject json = CasoNormal.ejecutar("FMM-NOR-10_pac-patrimonio-minimo-d-nor-4", Perfil.ABIERTAS);
        assertEquals(1L, json.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals("UI_PATRIMONIO_MINIMO_INSUFICIENTE", json.getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("codigo").getAsString());
        JsonObject proceso = CasoNormal.ejecutar("FMM-NOR-10_pac-patrimonio-minimo-d-nor-4", Perfil.ABIERTAS, e -> e.p_IdOrigenProceso = "PAC");
        assertEquals(0L, proceso.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong(), "interned literal: reference equality makes it exempt");
        assertEquals(1, proceso.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").size());
    }

    @Test
    @DisplayName("FM-NOR-D4 D-NOR-4: the same for the SAAPV contract type")
    void dNor4Saapv() throws Exception {
        JsonObject json = CasoNormal.ejecutar("FMM-NOR-44_origen-vacio-saawv", Perfil.ABIERTAS);
        assertEquals(1L, json.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong());
        JsonObject proceso = CasoNormal.ejecutar("FMM-NOR-44_origen-vacio-saawv", Perfil.ABIERTAS, e -> e.p_SolicitudesAporteRescate.get(0).TipoContratoOperacion = "SAAPV");
        assertEquals(0L, proceso.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("FM-NOR-D5 the JavaCall class is registered in the dispatcher")
    void handlerRegistrado() {
        NormalJavaCallHandler h = new NormalJavaCallHandler(e -> new NormalContrato.Salida());
        assertEquals(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher.CREAR_SOLICITUD_NORMAL, h.fqn());
        assertTrue(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher.LISTA_BLANCA.contains(h.fqn()));
    }
}
