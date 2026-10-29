package cl.bcs.fondomutuo.tesoreria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida;

/**
 * FM-TES unit tests of the F2 W1 helper (the equivalence with the legacy is in the goldens FMM-NOR-87..96 of NormalEquivalenciaTest): the three in-line SQL
 * are the legacy text (C-W0A-5, SHA-256 of the legacy literals computed from ProcesarIngresoEgresoTesoreria.java:40,42,45), defects D-TES-1 / D-TES-2 at
 * the unit level, the routing of Tesoreria's collaborators (C4) and DEV-F5 (a swallowed HTTP failure is rethrown).
 */
class ProcesarIngresoEgresoTesoreriaTest {

    private static String sha(String s) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("FM-TES-02/04/05 the three in-line SQL are the legacy literals, character for character (read only)")
    void sqlEnLineaIgualesAlLegacy() throws Exception {
        assertThat(sha(TesoreriaJdbc.SQL_SELECT_EMPLEADO)).isEqualTo("ed079353a5a89d441e7f585fb6ce7ed8b087fccfcc2803aa1e57e97bbb10d162");
        assertThat(sha(TesoreriaJdbc.SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO)).isEqualTo("791c7d845a4066a28fbdecee8658e19e216255367f8bd6717ed816815f3cc115");
        assertThat(sha(TesoreriaJdbc.SQL_SELECT_PREFERENCIA_PAGO_CORREDOR)).isEqualTo("3fb8ea3db99a707d1b82c2bb7950bd590dd91c213e67cdf38c3810f071692a89");
        for (String s : List.of(TesoreriaJdbc.SQL_SELECT_EMPLEADO, TesoreriaJdbc.SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, TesoreriaJdbc.SQL_SELECT_PREFERENCIA_PAGO_CORREDOR,
                TesoreriaJdbc.SQL_SELECT_MODALIDAD_PAGO)) {
            assertThat(s.strip()).startsWithIgnoringCase("SELECT");
        }
    }

    @Test
    @DisplayName("D-TES-1 / D-TES-2: TitularPago always N, FechaLiquidacion = issue date, concept APORFM for every non-Spot contribution (also APOREINV)")
    void defectosTesoreria() {
        var sol = TesoreriaH2.aporteTra();
        sol.IdTipoMovimientoFondoMutuo = "APOREINV";
        sol.FechaLiquidacion = java.sql.Date.valueOf("2025-04-03");
        var ie = TesoreriaArmado.ingreso(sol, 0L);
        assertThat(ie.TitularPago).isEqualTo("N");
        assertThat(ie.FechaLiquidacion).isEqualTo("2025-04-01");
        assertThat(ie.IdConcepto).isEqualTo("APORFM");
        assertThat(ie.IdTipoMovimiento).isEqualTo("MOVING");
        sol.IdPersona = null;
        assertThatThrownBy(() -> TesoreriaArmado.ingreso(sol, 0L)).as("FM-TES-13").isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("FM-TES-12: p_SimularOperacion null becomes N, p_EsIngresoAutomatico passes as given (null overrides Tesoreria's default S)")
    void camposDeLaLlamada() throws Exception {
        List<cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Entrada> vistas = new ArrayList<>();
        TesoreriaDatos datos = new TesoreriaDatos() {
            public List<Empleado> empleados(Long u) { return List.of(); }
            public List<ModalidadPago> modalidadesPago() { return List.of(); }
            public List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio(List<Long> ids) { return List.of(); }
            public List<PreferenciaPagoCorredor> preferenciasPagoCorredor(Long e) { return List.of(); }
        };
        var helper = new ProcesarIngresoEgresoTesoreriaService(datos, e -> {
            vistas.add(e);
            return new CrearIngresoEgreso_Salida();
        });
        ProcesosExternos.EntradaTesoreria e = new ProcesosExternos.EntradaTesoreria();
        e.p_Solicitudes.add(TesoreriaH2.aporteTra());
        helper.procesar(e);
        assertThat(vistas.get(0).p_SimularOperacion).isEqualTo("N");
        assertThat(vistas.get(0).p_EsIngresoAutomatico).isNull();
        assertThat(vistas.get(0).p_FormaDePago.get(0).IdModalidadDePago).isEqualTo("MODCAJ");
        assertThat(vistas.get(0).p_IngresoEgresoGenerar.get(0).IdEmpleadoDigitador).isZero();
    }

    @Test
    @DisplayName("C4: Plataforma by the router (G3 before SaldoFlujoCaja), an unknown collaborator fails closed (503); DEV-F5 rethrows a swallowed HTTP failure")
    void ruteoDeColaboradores() throws Exception {
        TesoreriaH2.Router router = new TesoreriaH2.Router();
        Compuertas cerradas = new Compuertas("NORMAL", true, false, false, false, false);
        ColaboradoresTesoreria c = new ColaboradoresTesoreria(router, cerradas, null, entrada -> new JsonObject());
        c.invocar("RevisarRequisitosBasicos", new JsonObject());
        c.invocar("AgregarLog", new JsonObject());
        assertThat(c.invocar("ActualizarSaldosCuentaBanco", new JsonObject())).isNotNull();
        assertThat(router.llamadas).containsExactly("cl.bcs.plataforma.pla.RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.AgregarLog");
        assertThatThrownBy(() -> c.invocar("SaldoFlujoCaja", new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
        assertThatThrownBy(() -> c.invocar("CrearCargoAbono", new JsonObject())).isInstanceOf(ServicioNoServidoException.class);
        // Tesoreria swallows the failure; vigilar rethrows it after Tesoreria returns
        router.noServido = "cl.bcs.plataforma.pla.AgregarLog";
        assertThatThrownBy(() -> ColaboradoresTesoreria.vigilar(() -> {
            try {
                c.invocar("AgregarLog", new JsonObject());
            } catch (RuntimeException tragada) {
                // as CrearIngresoEgresoService.logica does
            }
            return "resultado";
        })).isInstanceOf(ServicioNoServidoException.class);
        assertThat(ColaboradoresTesoreria.vigilar(() -> "ok")).as("nothing is left for the next call").isEqualTo("ok");
    }
}
