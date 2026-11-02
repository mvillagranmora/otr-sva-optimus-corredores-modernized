package cl.bcs.sura;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import cl.bcs.sura.service.PerfilesPortafolioUtilsService;
import cl.bcs.sura.support.SuraTestSupport.FakePort;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static cl.bcs.sura.support.SuraTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Characterizes legacy ModificarPerfilesPortafolioUtils (legacy/.../ModificarPerfilesPortafolioUtils.java)
 * method by method, against the intended port-based PerfilesPortafolioUtilsService.
 * Red today: GAP-SEAM (constructor takes NamedParameterJdbcTemplate, not SuraDataPort).
 * The pure static helpers of PerfilesPortafolioService are already covered by PerfilesPortafolioTest (B-SURA-01..06).
 */
class PerfilesPortafolioUtilsPortTest {

    static final String DOC = "11111111-1";
    static final String DOC2 = "22222222-2";

    static PerfilesPortafolioUtilsService utils(FakePort f) {
        return create(PerfilesPortafolioUtilsService.class, f.port);
    }

    static JsonObject socio(String doc, String... cobrocust) {
        return json(cl.bcs.sura.support.SuraTestSupport.socio("CLIENTE", doc, cobrocust));
    }

    @Test
    void bSura07_consultarIds_delegaDocumentosTalCual_yDevuelveLoQueDevuelveLaBD() {
        FakePort f = new FakePort().ids(idRow(101, 1, DOC, 0, "PROPOSITOAPV"));
        List<IdsEntidadYPerfiles> r = utils(f).consultarIdsPortafolioIdClientePorDocumentoIdentidad(List.of(DOC, DOC2, DOC));
        assertThat(f.calls(IDS).get(0).strs(0)).containsExactly(DOC, DOC2, DOC);   // no de-duplication
        assertThat(r).hasSize(1);
        assertThat(r.get(0).proposito).isEqualTo("PROPOSITOAPV");
        // fields not selected by the legacy query keep their pojo defaults
        assertThat(r.get(0).idFormaCobro).isEmpty();
        assertThat(r.get(0).actualizarFormaCobro).isEqualTo("S");
    }

    @Test
    void bSura08_modificarPerfiles_enviaTodosLosPerfilesSinFiltrar_ni_siquiera_los_vacios() {
        FakePort f = new FakePort();
        IdsEntidadYPerfiles vacio = idRow(1, 1, DOC, 0, "");
        IdsEntidadYPerfiles lleno = perfilRow(2, 1, DOC, 5, "F", "PC", "PCO", "PP", "");
        utils(f).modificarPerfilesPortafolio(List.of(vacio, lleno));
        assertThat(f.calls(MODIF_PERF).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.perfilComercial(), s -> s.perfilCobros(), s -> s.perfilProductos())
                .containsExactly(tuple(1L, "", "", ""), tuple(2L, "PC", "PCO", "PP"));
    }

    @Test
    void bSura12_ingresarIndicadores_pasaElValorYSoloIdIndicadorCeroSeInserta() {
        FakePort f = new FakePort();
        utils(f).ingresarIndicadoresCobroCustodia(List.of(idRow(1, 1, DOC, 0, ""), idRow(2, 1, DOC, 9, ""), idRow(3, 1, DOC, 0, "")), "OPCION 1");
        assertThat(f.calls(INGRESAR).get(0).str(1)).isEqualTo("OPCION 1");
        assertThat(efectivosInsert(f.calls(INGRESAR).get(0))).containsExactly(1L, 3L);
    }

    @Test
    void bSura03_modificarIndicadores_soloNoCeroYActualizarS() {
        FakePort f = new FakePort();
        IdsEntidadYPerfiles n = idRow(1, 1, DOC, 9, "");
        n.actualizarFormaCobro = "N";
        IdsEntidadYPerfiles minus = idRow(2, 1, DOC, 8, "");
        minus.actualizarFormaCobro = "s";           // lower-case is NOT "S"
        utils(f).modificarIndicadoresCobroCustodia(List.of(idRow(3, 1, DOC, 7, ""), n, minus, idRow(4, 1, DOC, 0, "")));
        assertThat(efectivosUpdate(f.calls(MODIF_IND).get(0))).containsExactly(7L);
    }

    @Test
    void bSura01_validarApv_soloExactoPROPOSITOAPV_mayusculas() {
        List<IdsEntidadYPerfiles> l = new ArrayList<>(List.of(idRow(1, 1, DOC, 1, "PROPOSITOAPV"), idRow(2, 1, DOC, 1, "propositoapv"), idRow(3, 1, DOC, 1, "")));
        utils(new FakePort()).validarIndicadorFormaCobroPortafolioApv(l);
        assertThat(l).extracting(p -> p.actualizarFormaCobro).containsExactly("N", "S", "S");
    }

    @Test
    void bSura18_obtenerEntidadesYPerfilesCompletas_unDocumentoPorIdYMergeDeFormaCobro() {
        IdsEntidadYPerfiles a = idRow(101, 1, DOC, 0, "");
        a.idFormaCobro = "EN-MEMORIA-A";
        IdsEntidadYPerfiles b = idRow(102, 1, DOC, 5, "");
        b.idFormaCobro = "EN-MEMORIA-B";
        FakePort f = new FakePort().perfiles(perfilRow(101, 1, DOC, 0, "", "PC", "PCO", "PP", ""),
                perfilRow(102, 1, DOC, 5, "DE-BD", "PC", "PCO", "PP", ""));
        List<IdsEntidadYPerfiles> r = utils(f).obtenerEntidadesYPerfilesCompletas(new ArrayList<>(List.of(a, b)));
        assertThat(f.calls(PERFILES).get(0).strs(0)).containsExactly(DOC, DOC);
        assertThat(r).extracting(p -> p.idFormaCobro).containsExactly("EN-MEMORIA-A", "DE-BD");
    }

    @Test
    void bSura18_obtenerEntidadesYPerfilesCompletas_filaSinContraparteEnMemoria_NoSuchElement() {
        FakePort f = new FakePort().perfiles(perfilRow(555, 1, DOC, 0, "X", "PC", "PCO", "PP", ""));
        assertThatThrownBy(() -> utils(f).obtenerEntidadesYPerfilesCompletas(new ArrayList<>(List.of(idRow(101, 1, DOC, 0, "")))))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void bSura11_obtenerEntidadesParaInsertar_semillaPorSocioEnOrden_yDescartaFormaCobroVacia() {
        List<IdsEntidadYPerfiles> ids = List.of(idRow(101, 1, DOC, 0, ""), idRow(201, 2, DOC2, 0, ""), idRow(301, 3, "OTRO-DOC", 0, ""));
        FakePort f = new FakePort().filtrados(
                perfilRow(101, 1, DOC, 0, "OPCION 1", "PC", "PCO", "PP", ""),
                perfilRow(201, 2, DOC2, 0, "", "PC", "PCO", "PP", ""));         // DB could not resolve -> dropped
        List<IdsEntidadYPerfiles> r = utils(f).obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(
                List.of(socio(DOC2), socio(DOC, "COBROCUST", "OPCION 1")), new ArrayList<>(ids));

        // seed = socios in input order; portfolio 301 (no socio) is not seeded
        assertThat(f.calls(FILTRADOS).get(0).snaps(0)).extracting(s -> s.idPortafolio(), s -> s.formaCobro())
                .containsExactly(tuple(201L, ""), tuple(101L, "OPCION 1"));
        assertThat(r).extracting(p -> p.idPortafolio).containsExactly(101L);
    }

    @Test
    void bSura11_cobrocustSinPortafolios_noSembrarNada() {
        FakePort f = new FakePort();
        List<IdsEntidadYPerfiles> r = utils(f).obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(
                List.of(socio("SIN-PORTAFOLIO", "COBROCUST", "OPCION 1")), new ArrayList<>());
        assertThat(r).isEmpty();
        assertThat(f.calls(FILTRADOS).isEmpty() || f.calls(FILTRADOS).get(0).snaps(0).isEmpty()).isTrue();
    }
}
