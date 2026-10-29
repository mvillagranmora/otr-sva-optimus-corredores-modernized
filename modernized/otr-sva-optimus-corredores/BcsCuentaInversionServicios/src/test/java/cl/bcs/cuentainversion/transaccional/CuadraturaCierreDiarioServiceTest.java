package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.CuadraturaCierreDiarioService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.CuadraturaCierreDiario} (modes "Proceso" with the process CUAMOVCERT, and "Consulta").
 * Proceso recomputes the row of CTA.CuadraturaDiariaOperaciones from the movements of ConsultarCargosAbonos of the given date (delete by id + insert).
 * The primary key of that table is an ASSUMPTION (README).
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-146..158}. Legacy: {@code CuadraturaCierreDiario}; modern: {@link CuadraturaCierreDiarioService}.
 */
@ExtendWith(ZonaUtc.class)
class CuadraturaCierreDiarioServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-A-146_proceso-cuadrado-todos-certificados",
            "CTA-A-147_proceso-descuadrado-por-movimientos-pendientes",
            "CTA-A-148_proceso-sin-operaciones-en-la-fecha",
            "CTA-A-149_proceso-sin-fila-existente-no-inserta-nada",
            "CTA-A-150_proceso-desconocido-no-hace-nada-pero-informa-exito",
            "CTA-A-151_proceso-mezclado-con-fila-desconocida-existente-viola-clave",
            "CTA-A-152_proceso-filtra-por-empresa-de-la-fila",
            "CTA-A-153_consulta-devuelve-detalle-json",
            "CTA-A-154_consulta-sin-fila-no-devuelve-detalle",
            "CTA-A-155_consulta-varios-procesos-devuelve-el-primero",
            "CTA-A-156_tipo-de-llamado-desconocido-no-hace-nada",
            "CTA-A-157_tipo-de-llamado-nulo-lanza-NullPointerException",
            "CTA-A-158_proceso-sin-procesos-informa-exito");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        CuadraturaCierreDiarioService service = new CuadraturaCierreDiarioService(caso.jdbc(), new ConfiguracionDelCaso(caso), new DiccionarioDeCaso(), colab);
        Verifica.Ejecucion r = ejecutar(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colab.traza());
    }

    /** Runs a service call that may throw a checked exception; it reaches Verifica unchanged (its type is pinned by the goldens). */
    @FunctionalInterface
    private interface Llamada {
        com.google.gson.JsonElement ejecutar() throws Exception;
    }

    private static Verifica.Ejecucion ejecutar(Llamada llamada) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return llamada.ejecutar();
            } catch (Exception e) {
                return CuadraturaCierreDiarioServiceTest.<RuntimeException, com.google.gson.JsonElement>lanzar(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R lanzar(Throwable t) throws T {
        throw (T) t;
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CuadraturaCierreDiario");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CTA-A-146: CuadraturaCierreDiario.java:178-216: every movement certified -> UI_CUADRADO, total = movements, 0 problems; row replaced
    @Test
    void ctaA146_procesoCuadradoTodosCertificados() {
        verificar(CASOS.get(0));
    }

    // CTA-A-147: CuadraturaCierreDiario.java:184-192,249-258: PEN movements -> UI_DESCUADRADO, detail JSON with Descripcion UI_NO_CERTIFICADO
    @Test
    void ctaA147_procesoDescuadradoPorMovimientosPendientes() {
        verificar(CASOS.get(1));
    }

    // CTA-A-148: CuadraturaCierreDiario.java:186-188: no movements on the date -> UI_SIN_OPERACIONES
    @Test
    void ctaA148_procesoSinOperacionesEnLaFecha() {
        verificar(CASOS.get(2));
    }

    // CTA-A-149: CuadraturaCierreDiario.java:168,216-219: no existing row -> delete matches nothing, insert of an empty list, still success message
    @Test
    void ctaA149_procesoSinFilaExistenteNoInsertaNada() {
        verificar(CASOS.get(3));
    }

    // CTA-A-150: CuadraturaCierreDiario.java:177-214: unknown process id -> default branch, nothing deleted/inserted, success message
    @Test
    void ctaA150_procesoDesconocidoNoHaceNadaPeroInformaExito() {
        verificar(CASOS.get(4));
    }

    // CTA-A-151: CuadraturaCierreDiario.java:216-218 (LEGACY DEFECT): the existing row of a process that is not deleted is re-inserted -> primary key violation
    @Test
    void ctaA151_procesoMezcladoConFilaDesconocidaExistenteViolaClave() {
        verificar(CASOS.get(5));
    }

    // CTA-A-152: CuadraturaCierreDiario_Delete...sql (LEGACY DEFECT): the delete ignores IdEmpresa, the select does not: the row of another company is deleted and nothing re-inserted
    @Test
    void ctaA152_procesoFiltraPorEmpresaDeLaFila() {
        verificar(CASOS.get(6));
    }

    // CTA-A-153: CuadraturaCierreDiario.java:224-238: Consulta returns DetalleCuadraturaJSON of the first row
    @Test
    void ctaA153_consultaDevuelveDetalleJson() {
        verificar(CASOS.get(7));
    }

    // CTA-A-154: CuadraturaCierreDiario.java:236-238: Consulta without row -> no detail
    @Test
    void ctaA154_consultaSinFilaNoDevuelveDetalle() {
        verificar(CASOS.get(8));
    }

    // CTA-A-155: CuadraturaCierreDiario.java:237: Consulta with several ids returns the FIRST row of the select (no ORDER BY)
    @Test
    void ctaA155_consultaVariosProcesosDevuelveElPrimero() {
        verificar(CASOS.get(9));
    }

    // CTA-A-156: CuadraturaCierreDiario.java:158,224: an unknown p_TipoLlamado does nothing
    @Test
    void ctaA156_tipoDeLlamadoDesconocidoNoHaceNada() {
        verificar(CASOS.get(10));
    }

    // CTA-A-157: CuadraturaCierreDiario.java:158 (LEGACY DEFECT): null p_TipoLlamado -> NullPointerException
    @Test
    void ctaA157_tipoDeLlamadoNuloLanzaNullPointerException() {
        verificar(CASOS.get(11));
    }

    // CTA-A-158: CuadraturaCierreDiario.java:158-221: Proceso without processes -> success message only
    @Test
    void ctaA158_procesoSinProcesosInformaExito() {
        verificar(CASOS.get(12));
    }

}
