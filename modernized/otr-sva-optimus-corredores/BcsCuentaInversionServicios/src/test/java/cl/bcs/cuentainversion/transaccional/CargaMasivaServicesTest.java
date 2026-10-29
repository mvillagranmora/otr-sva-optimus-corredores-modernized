package cl.bcs.cuentainversion.transaccional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.cuentainversion.service.CrearCargaMasivaService;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.service.EliminaCargaSeleccionadaService;
import cl.bcs.cuentainversion.service.ProcesaCargaSeleccionadaService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.SecuenciasDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/**
 * Characterization of the three legacy "carga masiva" services (entry point {@code call(conn, json)}):
 * <ul>
 *   <li>{@code EliminaCargaSeleccionada} (EliminaCargaSeleccionada.java:84-103): deletes details and headers of the listed loads, always answers OK;</li>
 *   <li>{@code CrearCargaMasiva} (CrearCargaMasiva.java:187-286): registers a load file (header + records) after checking movement types; any exception
 *       is swallowed into a generic error message;</li>
 *   <li>{@code ProcesaCargaSeleccionada} (ProcesaCargaSeleccionada.java:169-320): turns the records of a load into cargo/abono movements through the
 *       REAL CrearCargoAbono (manual entry, certificadoAutomatico N) and marks details/header OK/INV/PRO.</li>
 * </ul>
 * Modern classes: {@link EliminaCargaSeleccionadaService}, {@link CrearCargaMasivaService}, {@link ProcesaCargaSeleccionadaService}.
 * Sequences (legacy {@code Utils.getSecuencia}) are scripted per case ("secuencias") because the HANA sequences are not available.
 *
 * <p>Goldens: {@code src/test/resources/transaccional/cases/CTA-A-109..124}.
 */
@ExtendWith(ZonaUtc.class)
class CargaMasivaServicesTest {

    static final List<String> CASOS = List.of(
            "CTA-A-109_elimina-cargas-seleccionadas-cabecera-y-detalle",
            "CTA-A-110_elimina-varias-cargas",
            "CTA-A-111_elimina-lista-vacia-responde-ok-sin-borrar",
            "CTA-A-112_elimina-carga-inexistente-responde-ok",
            "CTA-A-113_crea-carga-un-archivo-dos-registros",
            "CTA-A-114_tipo-de-movimiento-distinto-al-seleccionado-es-error",
            "CTA-A-115_tipo-de-carga-desconocido-es-error-generico",
            "CTA-A-116_dos-cabeceras-comparten-id-y-fallan",
            "CTA-A-117_tipo-movimiento-nulo-es-error-generico",
            "CTA-A-118_sin-registros-crea-solo-cabecera",
            "CTA-A-119_carga-de-cargos-toma-concepto-del-tipo",
            "CTA-A-120_procesa-carga-valida-crea-movimientos-pendientes",
            "CTA-A-121_registro-sin-portafolio-no-procesado-y-cabecera-queda-PRO",
            "CTA-A-122_registro-con-error-de-validacion-marca-INV",
            "CTA-A-123_registro-con-dos-errores-duplica-id-y-falla-generico",
            "CTA-A-124_carga-inexistente-no-genera-mensajes");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void eliminar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        EliminaCargaSeleccionadaService service = new EliminaCargaSeleccionadaService(caso.jdbc(), new DiccionarioDeCaso());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
    }

    private static void crear(String nombre) {
        Caso caso = Caso.cargar(nombre);
        SecuenciasDeCaso secuencias = new SecuenciasDeCaso(caso);
        CrearCargaMasivaService service = new CrearCargaMasivaService(caso.jdbc(), secuencias, new ConfiguracionDelCaso(caso));
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r);
        // the sequences the legacy asked for ("name#count", in order) are part of the golden
        assertThat(secuencias.solicitadas()).as(nombre + ": sequences requested").isEqualTo(
                caso.esperado().getAsJsonArray("secuenciasSolicitadas").asList().stream().map(e -> e.getAsString()).toList());
    }

    private static void procesar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        ConfiguracionDelCaso configuracion = new ConfiguracionDelCaso(caso);
        DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
        CrearCargoAbonoService crear = new CrearCargoAbonoService(caso.jdbc(), new ActualizarSaldoCargoAbonoService(caso.jdbc()),
                new CalendarioHabil(caso.jdbc()), new FormatoMoneda(caso.jdbc()), configuracion, diccionario, colab);
        ProcesaCargaSeleccionadaService service = new ProcesaCargaSeleccionadaService(caso.jdbc(), crear, configuracion, diccionario);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(() -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, colab.traza(), "P_OUT_MENSAJES");
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CargaMasiva (Elimina/Crea/Procesa)");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // EliminaCargaSeleccionada.java:84-91: batch delete of detail then header; message UI_PROCESO_COMPLETADO (OK, id 1); other loads untouched
    @Test
    void ctaA109_eliminaCargasSeleccionadasCabeceraYDetalle() {
        eliminar(CASOS.get(0));
    }

    // EliminaCargaSeleccionada.java:84-85: every listed load
    @Test
    void ctaA110_eliminaVariasCargas() {
        eliminar(CASOS.get(1));
    }

    // EliminaCargaSeleccionada.java:84-91: empty list -> nothing deleted, still answers OK
    @Test
    void ctaA111_eliminaListaVaciaRespondeOkSinBorrar() {
        eliminar(CASOS.get(2));
    }

    // EliminaCargaSeleccionada.java:91: unknown load id -> OK anyway
    @Test
    void ctaA112_eliminaCargaInexistenteRespondeOk() {
        eliminar(CASOS.get(3));
    }

    // CrearCargaMasiva.java:206-273: header (sequence, IdEstado ING, MontoTotal 0, IdMoneda "", concept of the load type) + details IdEstado OK
    @Test
    void ctaA113_creaCargaUnArchivoDosRegistros() {
        crear(CASOS.get(4));
    }

    // CrearCargaMasiva.java:194-202: a record with a movement type different from p_TipoCargaSeleccionada -> error 1, nothing written
    @Test
    void ctaA114_tipoDeMovimientoDistintoAlSeleccionadoEsError() {
        crear(CASOS.get(5));
    }

    // CrearCargaMasiva.java:236-239,276-283: unknown load type -> IdConcepto null -> database error swallowed: "problemas en la linea : 0"
    @Test
    void ctaA115_tipoDeCargaDesconocidoEsErrorGenerico() {
        crear(CASOS.get(6));
    }

    // CrearCargaMasiva.java:213,221: every header takes secuencias.get(0) (LEGACY DEFECT): the second header violates the primary key
    @Test
    void ctaA116_dosCabecerasComparten_IdYFallan() {
        crear(CASOS.get(7));
    }

    // CrearCargaMasiva.java:195,276-283: TipoMovimiento null -> NullPointerException swallowed into the generic error
    @Test
    void ctaA117_tipoMovimientoNuloEsErrorGenerico() {
        crear(CASOS.get(8));
    }

    // CrearCargaMasiva.java:225,273: no records -> header with CantidadRegistros 0, "se cargaron 0 registros"
    @Test
    void ctaA118_sinRegistrosCreaSoloCabecera() {
        crear(CASOS.get(9));
    }

    // CrearCargaMasiva.java:233-239: CARGA_CARGO takes IdConcepto of its TipoCargaMasiva row
    @Test
    void ctaA119_cargaDeCargosTomaConceptoDelTipo() {
        crear(CASOS.get(10));
    }

    // ProcesaCargaSeleccionada.java:183-297: valid records -> movements PEN (manual entry), messages ADVERTENCIA, details OK, header PRO
    @Test
    void ctaA120_procesaCargaValidaCreaMovimientosPendientes() {
        procesar(CASOS.get(11));
    }

    // ProcesaCargaSeleccionada.java:270-283,291: a record dropped silently by CrearCargoAbono counts as "no procesada" (error message) but the header
    // is still PRO because only ERROR items increment ContadorErroresDetalle (LEGACY DEFECT)
    @Test
    void ctaA121_registroSinPortafolioNoProcesadoYCabeceraQuedaPro() {
        procesar(CASOS.get(12));
    }

    // ProcesaCargaSeleccionada.java:250-268,298-304: validation ERROR -> detail INV, header INV
    @Test
    void ctaA122_registroConErrorDeValidacionMarcaInv() {
        procesar(CASOS.get(13));
    }

    // ProcesaCargaSeleccionada.java:270: two messages with the same id -> List2Hash exception -> generic error, nothing updated
    @Test
    void ctaA123_registroConDosErroresDuplicaIdYFallaGenerico() {
        procesar(CASOS.get(14));
    }

    // ProcesaCargaSeleccionada.java:180-240: no records -> no messages, header update touches nothing
    @Test
    void ctaA124_cargaInexistenteNoGeneraMensajes() {
        procesar(CASOS.get(15));
    }
}
