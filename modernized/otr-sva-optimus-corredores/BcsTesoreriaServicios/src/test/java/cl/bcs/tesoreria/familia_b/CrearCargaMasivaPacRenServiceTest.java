package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.CrearCargaMasivaPacRenService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearCargaMasivaPacRen} (entry point {@code call}) as {@code CrearCargaMasivaPacRenService#execute(JsonObject)}:
 * loads the PAC rendition file; with p_simular S it validates it (ProcesarDistribucion.ValidaPreRendicion, real legacy code in the oracle), otherwise it schedules a batch program.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-322..329} (output, CAR.CargaMasivaDetallePAC and PAC.LogMandatoPAC). AgregarPrograma is a recorded fake.
 * The modern service expects other input keys (p_RegistrosRendicion) and has no simulation branch.
 */
@ExtendWith(ZonaUtc.class)
class CrearCargaMasivaPacRenServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-322_simulacion-S-carga-el-detalle-y-valida-la-pre-rendicion",
            "TES-B-323_simulacion-S-con-fila-huerfana-lanza-NPE-por-clave-nula",
            "TES-B-324_simulacion-N-agenda-el-programa-de-rendicion",
            "TES-B-325_solo-los-registros-de-detalle-D-se-insertan",
            "TES-B-326_sin-detalles-solo-desactiva-la-rendicion-anterior",
            "TES-B-327_simular-nulo-lanza-NPE-despues-de-insertar",
            "TES-B-328_tipo-de-registro-nulo-lanza-NPE",
            "TES-B-329_agendar-programa-lanza-excepcion-y-se-propaga");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso[] col = new cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso[1];

    /** The pre-settlement validation is legacy ProcesarDistribucion.ValidaPreRendicion (TES-B-309..318), built over the same collaborator double so its calls land in the trace. */
    private static cl.bcs.tesoreria.javacall.carga.ValidadorPreRendicion preRendicion(Caso caso, cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso colaboradores) {
        return cl.bcs.tesoreria.familia_b.support.ServicioDistribucionDeCaso.de(caso, colaboradores);
    }

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> { col[0] = new cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso(caso); return new CrearCargaMasivaPacRenService(caso.jdbc(), new cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso(caso), preRendicion(caso, col[0]), col[0]).execute(caso.entrada().deepCopy()); }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearCargaMasivaPacRen");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearCargaMasivaPacRen.java:150-190: simulation S: deactivates the previous REN load, inserts the details (spaces removed) and runs ValidaPreRendicion
    @Test
    void tesB322_simulacionSCargaElDetalleYValidaLaPreRendicion() {
        verificar(CASOS.get(0));
    }

    // CrearCargaMasivaPacRen.java:177: simulation S with an orphan file row: the NullPointerException of ValidaPreRendicion propagates
    @Test
    void tesB323_simulacionSConFilaHuerfanaLanzaNPEPorClaveNula() {
        verificar(CASOS.get(1));
    }

    // CrearCargaMasivaPacRen.java:209-249: simulation N: schedules the RENDICION_PAC program (AgregarPrograma recorded fake) and answers 'Proceso agendado OK'
    @Test
    void tesB324_simulacionNAgendaElProgramaDeRendicion() {
        verificar(CASOS.get(2));
    }

    // CrearCargaMasivaPacRen.java:142-163: only TipoRegistro D rows are inserted (H/T rows are ignored)
    @Test
    void tesB325_soloLosRegistrosDeDetalleDSeInsertan() {
        verificar(CASOS.get(3));
    }

    // CrearCargaMasivaPacRen.java:138-139: no details: the previous rendition is deactivated and the program scheduled
    @Test
    void tesB326_sinDetallesSoloDesactivaLaRendicionAnterior() {
        verificar(CASOS.get(4));
    }

    // CrearCargaMasivaPacRen.java:165: null p_simular: NullPointerException after the rows were inserted (rolled back by the caller)
    @Test
    void tesB327_simularNuloLanzaNPEDespuesDeInsertar() {
        verificar(CASOS.get(5));
    }

    // CrearCargaMasivaPacRen.java:144: null TipoRegistro: NullPointerException
    @Test
    void tesB328_tipoDeRegistroNuloLanzaNPE() {
        verificar(CASOS.get(6));
    }

    // CrearCargaMasivaPacRen.java:237: the scheduler throws: logged and rethrown
    @Test
    void tesB329_agendarProgramaLanzaExcepcionYSePropaga() {
        verificar(CASOS.get(7));
    }

}
