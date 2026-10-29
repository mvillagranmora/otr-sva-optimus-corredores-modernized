package cl.bcs.apv.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_b.support.Caso;
import cl.bcs.apv.familia_b.support.Equivalencia;
import cl.bcs.apv.familia_b.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.APV.GrabarContratoTraspasoSAAPV} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code GrabarContratosTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class GrabarContratoTraspasoSAAPVTest {

    static final List<String> CASOS = List.of(
            "APV-B-214_gc-sin-contratos-no-hace-nada",
            "APV-B-215_gc-folio-existente-advierte-y-continua",
            "APV-B-216_gc-egreso-falla-en-el-insert-del-contrato-por-Canal",
            "APV-B-217_gc-ingreso-con-todo-existente-falla-en-el-insert",
            "APV-B-218_gc-ingreso-persona-existente-por-documento-no-la-crea",
            "APV-B-219_gc-persona-nueva-crea-persona-telefono-y-correo",
            "APV-B-220_gc-persona-nueva-con-telefono-fijo-y-movil",
            "APV-B-221_gc-persona-nueva-con-cargo-actualiza-profesion",
            "APV-B-222_gc-persona-ya-existe-segun-el-servicio",
            "APV-B-223_gc-telefono-con-error-distinto",
            "APV-B-224_gc-empleador-nuevo-crea-persona-e-indicadores-rrhh",
            "APV-B-225_gc-indicadores-rrhh-con-errores",
            "APV-B-226_gc-empleador-ya-existe-segun-el-servicio",
            "APV-B-227_gc-cliente-nuevo-con-datos-completos-crea-cliente",
            "APV-B-228_gc-cliente-con-datos-incompletos-no-se-crea",
            "APV-B-229_gc-cliente-con-error-cliente-existe",
            "APV-B-230_gc-sin-relacion-persona-empleador-la-crea",
            "APV-B-231_gc-relacion-con-errores",
            "APV-B-232_gc-movimiento-nulo-lanza-NullPointerException",
            "APV-B-233_gc-dos-filas-folio-existente-no-consume-id",
            "APV-B-234_gc-tipo-cliente-defecto-sin-configurar-excepcion");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GrabarContratoTraspasoSAAPV");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // GrabarContratoTraspasoSAAPV.java:440,444: no contracts: only the sequence of contract ids is requested (for 0 rows); empty message list and counter 0
    @Test
    void apvB214_gcSinContratosNoHaceNada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // GrabarContratoTraspasoSAAPV.java:520-528: the folio already exists (ValidaContratoSAAPV): ADVERTENCIA FOLIO_CONTRATO_EXISTE with id = the folio number and the row is skipped
    @Test
    void apvB215_gcFolioExistenteAdvierteYContinua() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // GrabarContratoTraspasoSAAPV.java:1250,SQL ContratoSAAPV_Insert.sql: the INSERT binds :Canal but ContratoSAAPV has no such field: every row that reaches the insert throws SQLException 'Map Object failed Canal' (EGRESO skips the person/client/relation steps)
    @Test
    void apvB216_gcEgresoFallaEnElInsertDelContratoPorCanal() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // GrabarContratoTraspasoSAAPV.java:529-1250: INGRESO with person, client and employer already known and the relation present: no collaborator is called, same SQLException at the contract insert
    @Test
    void apvB217_gcIngresoConTodoExistenteFallaEnElInsert() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // GrabarContratoTraspasoSAAPV.java:462-486,531: the person is looked up by document through the real ValidaContratoSAAPV: found, so CrearPersona is not called
    @Test
    void apvB218_gcIngresoPersonaExistentePorDocumentoNoLaCrea() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // GrabarContratoTraspasoSAAPV.java:531-776: a new person: CrearPersona (Enrolamiento PERSONA, laboral phone first), CrearTelefono OTRO, CrearDireccionElectronica; then the client (CrearCliente) before the failing contract insert; dates taken from the system clock are masked
    @Test
    void apvB219_gcPersonaNuevaCreaPersonaTelefonoYCorreo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // GrabarContratoTraspasoSAAPV.java:677-745: without a laboral phone: CrearTelefono for the fijo and then for the movil; the relation stored procedure is called with the new person
    @Test
    void apvB220_gcPersonaNuevaConTelefonoFijoYMovil() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // GrabarContratoTraspasoSAAPV.java:615-638: IdTipoCargoPersona != 0 runs the PER.Persona profession UPDATE (its effect is rolled back with the failing insert)
    @Test
    void apvB221_gcPersonaNuevaConCargoActualizaProfesion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // GrabarContratoTraspasoSAAPV.java:783-802: CrearPersona answers PERSONA_EXISTE: an ADVERTENCIA message (lost by the exception) and the flow goes on, the person id stays 0
    @Test
    void apvB222_gcPersonaYaExisteSegunElServicio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // GrabarContratoTraspasoSAAPV.java:659-673: CrearTelefono answers an error that is not UI_ERROR_AL_CREAR_TELEFONO: an ERROR message (lost) and the flow goes on
    @Test
    void apvB223_gcTelefonoConErrorDistinto() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // GrabarContratoTraspasoSAAPV.java:806-962: a new employer: CrearPersona (J, RazonSocial) and the stored procedure IND.ModificarIndicador with the RRHH indicators (name/mail/phone of the encargado, expiry 0001-01-01)
    @Test
    void apvB224_gcEmpleadorNuevoCreaPersonaEIndicadoresRrhh() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // GrabarContratoTraspasoSAAPV.java:938-958: the indicator procedure answers errors: ERROR messages (lost) and the flow goes on
    @Test
    void apvB225_gcIndicadoresRrhhConErrores() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // GrabarContratoTraspasoSAAPV.java:963-984: CrearPersona answers PERSONA_EXISTE for the employer: no indicators
    @Test
    void apvB226_gcEmpleadorYaExisteSegunElServicio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // GrabarContratoTraspasoSAAPV.java:986-1099: complete minimum data and no client: CrearCliente (Enrolamiento CLIENTE) is called
    @Test
    void apvB227_gcClienteNuevoConDatosCompletosCreaCliente() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // GrabarContratoTraspasoSAAPV.java:1101-1116,1440-1482: incomplete client data (name, email): CrearCliente is NOT called and warnings are produced
    @Test
    void apvB228_gcClienteConDatosIncompletosNoSeCrea() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // GrabarContratoTraspasoSAAPV.java:1074-1098: CrearCliente answers CLIENTE_EXISTE: an ADVERTENCIA message and the flow goes on
    @Test
    void apvB229_gcClienteConErrorClienteExiste() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // GrabarContratoTraspasoSAAPV.java:1119-1177: no relation person-employer: the stored procedure REL.CrearRelacionPersona is called (type EMPDOR, ACT, FechaFin 2099-12-31 from the configuration)
    @Test
    void apvB230_gcSinRelacionPersonaEmpleadorLaCrea() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // GrabarContratoTraspasoSAAPV.java:1153-1176: the relation procedure answers errors: ERROR messages (lost) and the flow goes on
    @Test
    void apvB231_gcRelacionConErrores() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }

    // GrabarContratoTraspasoSAAPV.java:529: TipoMovimiento null: NullPointerException
    @Test
    void apvB232_gcMovimientoNuloLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(18));
    }

    // GrabarContratoTraspasoSAAPV.java:440,447,520-528,1434: the skipped (existing folio) row does not advance contadorIds, so the next row reuses the same id; the next row then fails at the insert
    @Test
    void apvB233_gcDosFilasFolioExistenteNoConsumeId() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(19));
    }

    // GrabarContratoTraspasoSAAPV.java:450: PLATAFORMA/APV.ContratoCLI_IdTipoClienteDefecto is read inside the loop, after the id sequence: missing parameter, Exception
    @Test
    void apvB234_gcTipoClienteDefectoSinConfigurarExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(20));
    }
}
