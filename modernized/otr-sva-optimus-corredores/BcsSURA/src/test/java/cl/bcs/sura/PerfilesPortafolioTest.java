package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.PerfilesPortafolioService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PerfilesPortafolioTest {

    // ── B-SURA-01: APV portfolio blocks forma-cobro update ──────────────────

    @Test void validarApv_propositoAPV_setActualizarN() {
        IdsEntidadYPerfiles p = perfil(1L, "PROPOSITOAPV", "S", 0L, "");
        PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(List.of(p));
        assertThat(p.actualizarFormaCobro).isEqualTo("N");
    }

    @Test void validarApv_otroProposito_mantieneSActualizar() {
        IdsEntidadYPerfiles p = perfil(1L, "GENERAL", "S", 0L, "");
        PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(List.of(p));
        assertThat(p.actualizarFormaCobro).isEqualTo("S");
    }

    @Test void validarApv_mixto_soloAPVCambia() {
        IdsEntidadYPerfiles apv  = perfil(1L, "PROPOSITOAPV", "S", 0L, "");
        IdsEntidadYPerfiles otro = perfil(2L, "GENERAL",      "S", 0L, "");
        PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(
                new ArrayList<>(List.of(apv, otro)));
        assertThat(apv.actualizarFormaCobro).isEqualTo("N");
        assertThat(otro.actualizarFormaCobro).isEqualTo("S");
    }

    @Test void validarApv_listaVacia_devuelveVacia() {
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .validarIndicadorFormaCobroPortafolioApv(new ArrayList<>());
        assertThat(result).isEmpty();
    }

    @Test void validarApv_propositoNull_noFalla() {
        IdsEntidadYPerfiles p = perfil(1L, null, "S", 0L, "");
        assertThatCode(() ->
                PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(List.of(p)))
                .doesNotThrowAnyException();
        assertThat(p.actualizarFormaCobro).isEqualTo("S");
    }

    @Test void validarApv_multiplesAPV_todosSetN() {
        IdsEntidadYPerfiles a = perfil(1L, "PROPOSITOAPV", "S", 0L, "");
        IdsEntidadYPerfiles b = perfil(2L, "PROPOSITOAPV", "S", 0L, "");
        PerfilesPortafolioService.validarIndicadorFormaCobroPortafolioApv(
                new ArrayList<>(List.of(a, b)));
        assertThat(a.actualizarFormaCobro).isEqualTo("N");
        assertThat(b.actualizarFormaCobro).isEqualTo("N");
    }

    // ── B-SURA-02: filtrar para insertar indicadores (IdIndicador == 0) ─────

    @Test void filtrarInsertar_idIndicadorCero_incluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 0L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaIngresarIndicadores(List.of(p));
        assertThat(result).containsExactly(p);
    }

    @Test void filtrarInsertar_idIndicadorNoEsCero_excluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 99L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaIngresarIndicadores(List.of(p));
        assertThat(result).isEmpty();
    }

    @Test void filtrarInsertar_mixto_soloIdCeroIncluido() {
        IdsEntidadYPerfiles nuevo     = perfil(1L, "", "S", 0L,  "");
        IdsEntidadYPerfiles existente = perfil(2L, "", "S", 5L,  "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaIngresarIndicadores(new ArrayList<>(List.of(nuevo, existente)));
        assertThat(result).containsExactly(nuevo);
    }

    // ── B-SURA-03: filtrar para modificar indicadores (IdIndicador != 0 && "S") ─

    @Test void filtrarModificar_idNoEsCeroYActualizarS_incluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 5L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaModificarIndicadores(List.of(p));
        assertThat(result).containsExactly(p);
    }

    @Test void filtrarModificar_idNoEsCeroYActualizarN_excluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "N", 5L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaModificarIndicadores(List.of(p));
        assertThat(result).isEmpty();
    }

    @Test void filtrarModificar_idEsCero_excluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 0L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaModificarIndicadores(List.of(p));
        assertThat(result).isEmpty();
    }

    @Test void filtrarModificar_idEsCeroYActualizarN_excluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "N", 0L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarParaModificarIndicadores(List.of(p));
        assertThat(result).isEmpty();
    }

    // ── B-SURA-04: fusionar forma cobro (empty → fill from input) ───────────

    @Test void fusionar_formaCobroVacia_seRellena() {
        IdsEntidadYPerfiles db    = conPortafolio(100L, "");
        IdsEntidadYPerfiles input = conPortafolio(100L, "TRANSFERENCIA");
        PerfilesPortafolioService.fusionarFormaCobro(
                new ArrayList<>(List.of(db)), List.of(input));
        assertThat(db.idFormaCobro).isEqualTo("TRANSFERENCIA");
    }

    @Test void fusionar_formaCobroYaExiste_mantiene() {
        IdsEntidadYPerfiles db    = conPortafolio(100L, "CHEQUE");
        IdsEntidadYPerfiles input = conPortafolio(100L, "TRANSFERENCIA");
        PerfilesPortafolioService.fusionarFormaCobro(
                new ArrayList<>(List.of(db)), List.of(input));
        assertThat(db.idFormaCobro).isEqualTo("CHEQUE");
    }

    @Test void fusionar_sinMatchPortafolio_noCambia() {
        IdsEntidadYPerfiles db    = conPortafolio(100L, "");
        IdsEntidadYPerfiles input = conPortafolio(999L, "TRANSFERENCIA");
        PerfilesPortafolioService.fusionarFormaCobro(
                new ArrayList<>(List.of(db)), List.of(input));
        assertThat(db.idFormaCobro).isEqualTo("");
    }

    // ── B-SURA-05: filtrar con IdFormaCobro no vacío ────────────────────────

    @Test void filtrarConFormaCobro_conValor_incluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 0L, "TRANSFERENCIA");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarConIdFormaCobro(List.of(p));
        assertThat(result).containsExactly(p);
    }

    @Test void filtrarConFormaCobro_vacio_excluye() {
        IdsEntidadYPerfiles p = perfil(1L, "", "S", 0L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarConIdFormaCobro(List.of(p));
        assertThat(result).isEmpty();
    }

    @Test void filtrarConFormaCobro_mixto_soloConValorIncluido() {
        IdsEntidadYPerfiles con    = perfil(1L, "", "S", 0L, "DEPOSITO");
        IdsEntidadYPerfiles sinVal = perfil(2L, "", "S", 0L, "");
        List<IdsEntidadYPerfiles> result = PerfilesPortafolioService
                .filtrarConIdFormaCobro(new ArrayList<>(List.of(con, sinVal)));
        assertThat(result).containsExactly(con);
    }

    // ── B-SURA-06: aplicar forma cobro de COBROCUST indicator ───────────────

    @Test void aplicarFormaCobro_setValorEnTodos() {
        IdsEntidadYPerfiles p1 = perfil(1L, "", "S", 0L, "");
        IdsEntidadYPerfiles p2 = perfil(2L, "", "S", 0L, "");
        PerfilesPortafolioService.aplicarFormaCobro(
                new ArrayList<>(List.of(p1, p2)), "DEPOSITO");
        assertThat(p1.idFormaCobro).isEqualTo("DEPOSITO");
        assertThat(p2.idFormaCobro).isEqualTo("DEPOSITO");
    }

    @Test void aplicarFormaCobro_listaVacia_noFalla() {
        assertThatCode(() ->
                PerfilesPortafolioService.aplicarFormaCobro(new ArrayList<>(), "TRANSFERENCIA"))
                .doesNotThrowAnyException();
    }

    // ── @Disabled: HANA / SOAP sandbox required ─────────────────────────────

    @Disabled("requires HANA: BSCL.PERFILES_COMERCIALES_T (ModificarPerfilesPortafolioUtils_ConsultarPerfilesComercialesPorDocumentosIdentidad.sql)")
    @Test void hana_consultarPerfilesComerciales() { }

    @Disabled("requires HANA: BSCL.PORTAFOLIOS_T (ModificarPerfilesPortafolioUtils_ConsultarIdsPortafolioIdClientePorDocumentoIdentidad.sql)")
    @Test void hana_consultarIdsPortafolioIdCliente() { }

    @Disabled("requires SURA SOAP sandbox: CrearClienteFullWSModificarPerfilesPortafolio")
    @Test void soap_crearClienteFullWS() { }

    @Disabled("requires SURA SOAP sandbox: ModificarClienteModificarPerfilesPortafolioWS")
    @Test void soap_modificarClienteWS() { }

    @Disabled("requires HANA + SURA SOAP sandbox: complete profile modification flow")
    @Test void integracion_flujoCompletoModificarPerfiles() { }

    // ── helpers ──────────────────────────────────────────────────────────────

    private IdsEntidadYPerfiles perfil(Long id, String proposito, String actualizarFormaCobro,
                                        Long idIndicador, String formaCobro) {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = id;
        p.proposito = proposito;
        p.actualizarFormaCobro = actualizarFormaCobro;
        p.idIndicador = idIndicador;
        p.idFormaCobro = formaCobro;
        return p;
    }

    private IdsEntidadYPerfiles conPortafolio(Long idPortafolio, String formaCobro) {
        IdsEntidadYPerfiles p = new IdsEntidadYPerfiles();
        p.idPortafolio = idPortafolio;
        p.idFormaCobro = formaCobro;
        return p;
    }
}
