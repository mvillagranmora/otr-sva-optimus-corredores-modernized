package cl.bcs.fondomutuo.aportereinversion;

import static cl.bcs.fondomutuo.aportereinversion.CorridaCsr.correr;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.ProcesosExternos;

/**
 * The collaborators that are not ported sit behind typed ports and gates (plan 6): G1 Tesoreria ({@code fondomutuo.tesoreria.habilitada}), G2 Spot, G3 SaldoFlujoCaja, G-CUS custody,
 * G5 (a write collaborator answers 503 through the router). A closed gate answers {@code ServicioNoServidoException} (503) AFTER all the validations and BEFORE the first write
 * (Tesoreria writes through CrearIngresoEgreso, so G1 and G2 are both evaluated before Tesoreria is called), and the port behind it is never reached. The gates are NOT in the legacy
 * (approved difference: a 503 that the router repeats) and are evaluated on the real pass; the reads (G3, G-CUS) also in simulation. The order of the gates is the order of the legacy
 * calls: G3 (balance), G-CUS (custody), then G1 / G2 before the first write. Every case that does not need a closed gate must equal its golden with that gate shut.
 * The cases and the golden come from the same matrix as the equivalence tests; the gate assumptions are in API_ASSUMED_CSR.md section 4 (Q-T4A-3).
 */
class CsrCompuertaTest {

    @AfterEach
    void limpiar() {
        SujetoCsr.tesoreriaPrueba = null;
        SujetoCsr.spotPrueba = null;
        SujetoCsr.logPrueba = null;
    }

    private static void sinEfectos(JsonObject r) {
        for (String t : r.getAsJsonObject("tablas").keySet()) {
            var filas = r.getAsJsonObject("tablas").getAsJsonArray(t);
            if (t.endsWith("SolicitudAporteRescate")) {
                for (var f : filas) assertThat(f.getAsJsonObject().get("IdTipoMovimientoFondoMutuo").getAsString()).as("no APOREINV row").isNotEqualTo("APOREINV");
            } else if (t.endsWith("InterfazAgf")) {
                assertThat(filas).isEmpty();
            } else if (t.endsWith("ReinversionSerieFondoDestino")) {
                for (var f : filas) assertThat(f.getAsJsonObject().get("FechaFinalReinversion").getAsString()).as("FechaFinalReinversion untouched").isEqualTo("2026-03-02");
            }
        }
        List<String> orden = new java.util.ArrayList<>();
        r.getAsJsonArray("colaboradores").forEach(c -> orden.add(c.getAsJsonObject().get("colaborador").getAsString()));
        assertThat(orden).doesNotContain("ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot", "AgregarLog");
    }

    private static JsonObject ejecutar(String slug, Perfil perfil) throws Exception {
        return CasoCsr.ejecutarReal(CorridaCsr.idDe(slug), perfil);
    }

    private static void es503(JsonObject r, String compuerta) {
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(r.get("compuerta").getAsString()).isEqualTo(compuerta);
        assertThat(r.has("salida")).as("no output: the 503 is the answer").isFalse();
    }

    @Test
    @DisplayName("G3 closed (SaldoFlujoCaja, FM-REI-58, C73): a CIN aporte is a 503 G3 on the real pass AND in simulation (D-F2-13: the balance is read there too); nothing written")
    void g3_aporteCin_503EnRealYSimulacion() throws Exception {
        for (String slug : List.of("real-un-aporte-nacional-cin", "simulacion-un-aporte", "saldo-relacionado-distinto-de-cero-no-valida")) {
            JsonObject r = ejecutar(slug, Perfil.G3_CERRADA);
            es503(r, "G3");
            sinEfectos(r);
        }
    }

    @Test
    @DisplayName("G3 closed: a lot without CIN pairs to ask (all TRA) never meets the gate: it equals its golden")
    void g3_loteSinCin_igualAlGolden() throws Exception {
        for (String slug : List.of("tesoreria-medio-tra", "saldo-no-se-llama-si-todos-son-no-cin")) {
            CsrCostura.comparar(slug, CasoCsr.golden(CorridaCsr.idDe(slug)), ejecutar(slug, Perfil.G3_CERRADA));
        }
    }

    @Test
    @DisplayName("G1 closed (Tesoreria, FM-REI-61, C81): a real pass with a non CIN medium is a 503 G1 BEFORE Tesoreria is called (it writes) and before the INSERT; in simulation it passes")
    void g1_medioNoCin_503AntesDeLaPrimeraEscritura() throws Exception {
        for (String slug : List.of("tesoreria-medio-tra", "tesoreria-lote-mixto-cin-y-tra", "spot-y-tesoreria-juntos", "tesoreria-rech-numeroreferencia-rechaza")) {
            JsonObject r = ejecutar(slug, Perfil.G1_CERRADA);
            es503(r, "G1");
            sinEfectos(r);
        }
        CsrCostura.comparar("sim", CasoCsr.golden(CorridaCsr.idDe("tesoreria-simulacion-no-llama")), ejecutar("tesoreria-simulacion-no-llama", Perfil.G1_CERRADA));
        CsrCostura.comparar("cin", CasoCsr.golden(CorridaCsr.idDe("real-un-aporte-nacional-cin")), ejecutar("real-un-aporte-nacional-cin", Perfil.G1_CERRADA));
    }

    @Test
    @DisplayName("G2 closed (Spot, FM-REI-63, C81): a real pass with a request that GENERATES Spot is a 503 G2 BEFORE Tesoreria and the INSERT (even when Tesoreria would reject it later); without Spot, or in simulation, it passes")
    void g2_generaSpot_503AntesDeLaPrimeraEscritura() throws Exception {
        for (String slug : List.of("spot-clp-a-usd", "spot-usd-a-clp", "spot-y-tesoreria-juntos", "spot-con-tesoreria-rech-no-llega-a-spot", "spot-dos-aportes-ok")) {
            JsonObject r = ejecutar(slug, Perfil.G2_CERRADA);
            es503(r, "G2");
            sinEfectos(r);
        }
        for (String slug : List.of("spot-parametro-n-no-genera", "spot-parametro-s-sin-solicitudes-spot", "spot-simulacion-no-llama", "tesoreria-medio-tra")) {
            CsrCostura.comparar(slug, CasoCsr.golden(CorridaCsr.idDe(slug)), ejecutar(slug, Perfil.G2_CERRADA));
        }
    }

    @Test
    @DisplayName("G-CUS closed (custody, FM-REI-59): a request with PatrimonioMinimo > 0 needs the custody flow: 503 G-CUS (also in simulation, it is a read); without PatrimonioMinimo it passes")
    void gCustodia_patrimonioMinimo_503() throws Exception {
        for (String slug : List.of("patrimonio-minimo-con-custodia-suficiente", "patrimonio-minimo-sin-custodia-rechaza", "custodia-una-cartera-por-portafolio")) {
            JsonObject r = ejecutar(slug, Perfil.CUSTODIA_CERRADA);
            es503(r, "G-CUS");
            sinEfectos(r);
        }
        CsrCostura.comparar("cero", CasoCsr.golden(CorridaCsr.idDe("patrimonio-minimo-cero-no-consulta-custodia")), ejecutar("patrimonio-minimo-cero-no-consulta-custodia", Perfil.CUSTODIA_CERRADA));
    }

    @Test
    @DisplayName("Order of the gates = order of the legacy calls: with all gates shut, a CIN aporte with PatrimonioMinimo or with Spot meets G3 first; a TRA aporte with PatrimonioMinimo meets G-CUS before G1; a TRA + Spot meets G1 before G2; the shipped defaults (all shut) serve nothing that needs a collaborator")
    void ordenDeLasCompuertas() throws Exception {
        es503(ejecutar("patrimonio-minimo-con-custodia-suficiente", Perfil.CERRADAS), "G3");
        es503(ejecutar("spot-clp-a-usd", Perfil.CERRADAS), "G3");
        es503(ejecutar("spot-y-tesoreria-juntos", Perfil.CERRADAS), "G1");
        es503(ejecutar("patrimonio-minimo-medio-tra", Perfil.CERRADAS), "G-CUS");
        es503(ejecutar("tesoreria-medio-tra", Perfil.CERRADAS), "G1");
        es503(ejecutar("real-un-aporte-nacional-cin", Perfil.CERRADAS), "G3");
        CsrCostura.comparar("lista-vacia", CasoCsr.golden(CorridaCsr.idDe("lista-vacia")), ejecutar("lista-vacia", Perfil.CERRADAS));
        CsrCostura.comparar("requisitos", CasoCsr.golden(CorridaCsr.idDe("requisitos-basicos-usuario-inexistente")), ejecutar("requisitos-basicos-usuario-inexistente", Perfil.CERRADAS));
        CsrCostura.comparar("regimen", CasoCsr.golden(CorridaCsr.idDe("regimen-no-permite-aporte")), ejecutar("regimen-no-permite-aporte", Perfil.CERRADAS));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "real-un-aporte-nacional-cin", "tesoreria-medio-tra", "spot-clp-a-usd", "patrimonio-minimo-con-custodia-suficiente" })
    @DisplayName("Every case with every gate OPEN equals its golden (the gates are inert when open)")
    void compuertasAbiertas_igualAlGolden(String slug) throws Exception {
        CsrCostura.comparar(slug, CasoCsr.golden(CorridaCsr.idDe(slug)), ejecutar(slug, Perfil.ABIERTAS));
    }

    @Test
    @DisplayName("G5: an answer 503 of AgregarLog (the router) after the INSERT: the whole request is a 503 ROLLBACK, nothing stays (the legacy would have kept the log-less rows by exception: DEV-F5)")
    void g5_agregarLog503() throws Exception {
        SujetoCsr.logPrueba = e -> {
            throw new ServicioNoServidoException("ROUTER", "AgregarLog 503");
        };
        JsonObject r = ejecutar("real-un-aporte-nacional-cin", Perfil.ABIERTAS);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(r.get("compuerta").getAsString()).isEqualTo("ROUTER");
        sinRastroDeLaSolicitud(r);
    }

    private static void sinRastroDeLaSolicitud(JsonObject r) {
        for (var f : r.getAsJsonObject("tablas").getAsJsonArray(CorridaCsr.SOLICITUDES)) {
            assertThat(f.getAsJsonObject().get("IdTipoMovimientoFondoMutuo").getAsString()).isNotEqualTo("APOREINV");
        }
        for (var f : r.getAsJsonObject("tablas").getAsJsonArray(CorridaCsr.REINVERSION)) {
            assertThat(f.getAsJsonObject().get("FechaFinalReinversion").getAsString()).isEqualTo("2026-03-02");
        }
    }

    @Test
    @DisplayName("Typed ports (FM-REI-61): the port of Tesoreria receives a typed EntradaTesoreria (the pending TRA request, flags of the input) and a typed RECH / NumeroReferencia answer rejects the request")
    void puertoTipadoDeTesoreria() throws Exception {
        List<ProcesosExternos.EntradaTesoreria> recibidas = new java.util.ArrayList<>();
        SujetoCsr.tesoreriaPrueba = e -> {
            recibidas.add(e);
            ProcesosExternos.SalidaTesoreria s = new ProcesosExternos.SalidaTesoreria();
            ProcesosExternos.ResultadoProceso rp = new ProcesosExternos.ResultadoProceso();
            rp.Id = e.p_Solicitudes.get(0).IdInterno;
            rp.Campo = "NumeroReferencia";
            rp.Resultado = "RECH";
            s.P_OUT_RESULTADOPROCESO.add(rp);
            return s;
        };
        JsonObject r = ejecutar("tesoreria-medio-tra", Perfil.ABIERTAS);
        assertThat(recibidas).hasSize(1);
        assertThat(recibidas.get(0).p_SimularOperacion).isEqualTo("N");
        assertThat(recibidas.get(0).p_EsIngresoAutomatico).isEqualTo("N");
        assertThat(recibidas.get(0).p_Solicitudes).hasSize(1);
        assertThat(recibidas.get(0).p_Solicitudes.get(0).IdMedioDePago).isEqualTo("TRA");
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
        assertThat(r.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO")).isEmpty();
    }

    @Test
    @DisplayName("Typed ports (FM-REI-63): the Spot entry carries p_DatosOrigenInversion (the INPUT aportes, field assumed in ProcesosExternos.EntradaSpot: API_ASSUMED_CSR.md 3); a typed answer without P_OUT_ERRORESCONTADOR (null) is read")
    void puertoTipadoDeSpot() throws Exception {
        List<Object> origen = new java.util.ArrayList<>();
        SujetoCsr.spotPrueba = e -> {
            try {
                Object lista = ProcesosExternos.EntradaSpot.class.getField("p_DatosOrigenInversion").get(e);
                origen.addAll((java.util.List<?>) lista);
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError("CSR: ProcesosExternos.EntradaSpot must gain the public field 'List<?> p_DatosOrigenInversion' (API_ASSUMED_CSR.md 3)", ex);
            }
            ProcesosExternos.SalidaSpot s = new ProcesosExternos.SalidaSpot();
            ProcesosExternos.OperacionSpot op = new ProcesosExternos.OperacionSpot();
            op.IdInterno = String.valueOf(e.p_DatosOperacion.get(0).IdInterno);
            op.Estado = "PEN";
            op.IdOperacionSpot = 4321L;
            s.P_OUT_IDOPERACIONSPOT.add(op);
            return s;
        };
        JsonObject r = ejecutar("spot-clp-a-usd", Perfil.ABIERTAS);
        assertThat(origen).hasSize(1);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
        for (var f : r.getAsJsonObject("tablas").getAsJsonArray(CorridaCsr.SOLICITUDES)) {
            if ("APOREINV".equals(f.getAsJsonObject().get("IdTipoMovimientoFondoMutuo").getAsString())) {
                assertThat(f.getAsJsonObject().get("IdOperacionSpot").getAsLong()).isEqualTo(4321L);
            }
        }
    }
}
