package cl.bcs.tesoreria.jobs.guarda;

import cl.bcs.tesoreria.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresEnProceso;
import cl.bcs.tesoreria.jobs.guarda.ClasificacionColaborador.Categoria;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Which collaborator names the guard protects (inventory JOBS_H1_H4_INVENTORY.md A.2, verified against what this module actually sends). */
class ClasificacionColaboradorTest {

    private static Set<String> de(Categoria c) {
        return ClasificacionColaborador.todas().entrySet().stream().filter(e -> e.getValue() == c)
                .map(java.util.Map.Entry::getKey).collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    @DisplayName("G6-C01: every name this module can send over the collaborator port is classified (no name escapes the guard by omission)")
    void todoNombreEstaClasificado() {
        Set<String> enviados = new TreeSet<>(ColaboradoresJavaCall.nombresConocidos());
        enviados.addAll(Set.of(ServiciosExternos.VALIDAR_MANDATOS_PAC, ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC,
                ServiciosExternos.CONSULTAR_ESTADO_MODULO, ServiciosExternos.OBTENER_DIA_ANTERIOR, ServiciosExternos.AGREGAR_LOG));
        enviados.addAll(ColaboradoresEnProceso.GRUPO_M.keySet());
        // the names that the services of the library send through kit/colaborador (not all are in ColaboradoresJavaCall.CLASES' keys of the jobs)
        enviados.addAll(Set.of("ActualizarSaldosCuentaBanco", "AnularNominasTransferencias"));

        assertThat(enviados).allSatisfy(n -> assertThat(ClasificacionColaborador.de(n)).as(n).isNotNull());
    }

    @Test
    @DisplayName("G6-C02: the non-repeatable TomEE writes of inventory A.2 (group TW) are guarded: orders, fund requests, events, programs, clients...")
    void escrituraNoRepetible() {
        assertThat(de(Categoria.NO_REPETIBLE)).containsExactlyInAnyOrder("AgregarPrograma", "InscribirProcesoBatch", "AgregarEvento",
                "CrearEventoAutorizacionMasivo", "ModificarEstadoEventoAutorizacionMasivo", "ValidaEjecucionProcesoRecPAC", "CrearOrdenes",
                "CrearSolicitudAporteRescateNormal", "AnulacionContratoCSC", "RevocacionContratoCSC", "ModificarPortafolio", "CrearCliente",
                "CrearPersona", "CrearTelefono", "CrearDireccionElectronica", "CrearPersonaCuentaBancaria", "CrearMovimientoComprobante");
        assertThat(de(Categoria.NO_REPETIBLE)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).isTrue());
    }

    @Test
    @DisplayName("G6-C03: AgregarLog is the only append-only write and is NOT guarded; group TW = 17 guarded + AgregarLog = 18 clases")
    void appendOnly() {
        assertThat(de(Categoria.APPEND_ONLY)).containsExactly("AgregarLog");
        assertThat(ClasificacionColaborador.guardada("AgregarLog")).isFalse();
        assertThat(de(Categoria.NO_REPETIBLE).size() + de(Categoria.APPEND_ONLY).size()).isEqualTo(18);
    }

    @Test
    @DisplayName("G6-C04: reads, Tesoreria's own in-process services and the specials are not guarded; the group M writers of other modules are (HTTP fallback)")
    void restoDeCategorias() {
        for (String n : Set.of("ConsultarEstadoModulo", "ObtenerDiaAnterior", "CalcularParidad", "ConsultarFlujoCaja", "SaldoFlujoCaja",
                "RevisarRequisitosBasicos", "CalcularMontoYCobrosOrden", "CalcularMontoNumeroCuota", "ConsultarInstrumentoPrecioBolsa",
                "ObtenerPortafoliosEjecutivo", "CrearIngresoEgreso", "LiquidarComprobante", "CrearComprobante", "ActualizarSaldosCuentaBanco",
                "AnularNominasTransferencias", "StoredProcedure", "Cloper")) {
            assertThat(ClasificacionColaborador.guardada(n)).as(n).isFalse();
        }
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).containsExactlyInAnyOrder("ValidarMandatosPAC", "CambiarEstadoContratosSAAPV",
                "CrearCargoAbono", "AnularCargoAbono");
        assertThat(ClasificacionColaborador.guardada("CrearCargoAbono")).isTrue();
    }

    @Test
    @DisplayName("G6-C05: what the jobs of this module reach over HTTP: the guarded ones and the one append-only")
    void loQueAlcanzaElModulo() {
        // BcsJobProcesarDistribucion (ValidaEjecucionProcesoRecPAC) and, through MovimientosDistribucion, CrearOrdenes and
        // CrearSolicitudAporteRescateNormal; the DAPFFMM job only AgregarLog (CargaMasivaSalida)
        assertThat(ClasificacionColaborador.guardada("CrearOrdenes")).isTrue();
        assertThat(ClasificacionColaborador.guardada("CrearSolicitudAporteRescateNormal")).isTrue();
        assertThat(ClasificacionColaborador.guardada("ValidaEjecucionProcesoRecPAC")).isTrue();
        assertThat(ClasificacionColaborador.de("AgregarLog")).isEqualTo(Categoria.APPEND_ONLY);
        assertThat(ClasificacionColaborador.de("NombreQueNoExiste")).isNull();
        assertThat(ClasificacionColaborador.guardada(null)).isFalse();
    }
}
