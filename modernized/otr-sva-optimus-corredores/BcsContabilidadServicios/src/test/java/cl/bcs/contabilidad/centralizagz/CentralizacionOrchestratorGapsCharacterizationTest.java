package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.ConfiguracionTipoCentralizacion;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.EntradaMultiSeleccion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaMultiSeleccion;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import cl.bcs.contabilidad.domain.service.CentralizacionOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.centralizagz.Fx.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WP-F: CentralizacionOrchestrator vs legacy CentralizaMultiSeleccion.java — only the behaviours the existing
 * cl.bcs.contabilidad.CentralizacionOrchestratorTest does not pin. Oracle: legacy
 * BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaMultiSeleccion.java.
 */
class CentralizacionOrchestratorGapsCharacterizationTest {

    static final String FECHA = "2025-08-06";

    PlataformaPort plataforma;
    ContabilidadRepository repository;
    BcsCentralizacionService svcA;
    BcsCentralizacionService svcB;
    CentralizacionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        plataforma = mock(PlataformaPort.class);
        repository = mock(ContabilidadRepository.class);
        svcA = mock(BcsCentralizacionService.class);
        svcB = mock(BcsCentralizacionService.class);
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(any())).thenReturn(FECHA);
        when(plataforma.obtenerMonedaSistema(any())).thenReturn("CLP");
        when(plataforma.consultarEmpresa(any())).thenReturn(EMPRESA);
        when(repository.queryCentralizacionesVigentes(any(), any(), any())).thenReturn(List.of());
        when(svcA.centralizar(any())).thenReturn(SalidaCentralizacion.empty());
        when(svcB.centralizar(any())).thenReturn(SalidaCentralizacion.empty());
        orchestrator = new CentralizacionOrchestrator(plataforma, repository, Map.of("svcA", svcA, "svcB", svcB));
    }

    static ConfiguracionTipoCentralizacion cfg(long id, String nombre, String servicio, String requiereFechaConsulta) {
        return new ConfiguracionTipoCentralizacion(id, nombre, null, null, null, servicio, "J",
                "INF-" + id, null, requiereFechaConsulta, null, null);
    }

    static EntradaMultiSeleccion entrada(String simula, String fechaConsulta, List<Long> ids) {
        return new EntradaMultiSeleccion(ID_EMPRESA, ID_USUARIO, ID_MODULO, FECHA, fechaConsulta, EJERCICIO, simula, ids);
    }

    // Legacy CentralizaMultiSeleccion.java:240 — the vigentes check only runs when SimulaOperacion = "N".
    @Test
    @DisplayName("CONT-F-245: simulacion S no consulta centralizaciones vigentes (legacy :240)")
    void contF245_simulacionNoConsultaVigentes() {
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        SalidaMultiSeleccion s = orchestrator.ejecutar(entrada("S", null, List.of(470L)));
        verify(repository, never()).queryCentralizacionesVigentes(any(), any(), any());
        verify(svcA).centralizar(any());
        assertThat(s.erroresContador()).isZero();
    }

    // Legacy CentralizaMultiSeleccion.java:240 — and only when PLA.LocalizacionSistema = "CL".
    @Test
    @DisplayName("CONT-F-246: localizacion distinta de CL no consulta vigentes aunque simula N (legacy :240)")
    void contF246_localizacionNoChileNoConsultaVigentes() {
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn("PE");
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        orchestrator.ejecutar(entrada("N", null, List.of(470L)));
        verify(repository, never()).queryCentralizacionesVigentes(any(), any(), any());
        verify(svcA).centralizar(any());
    }

    // Legacy CentralizaMultiSeleccion.java:247-253 — several types requested: each already-posted type is removed,
    // P_OUT_ERRORESCONTADOR++ and one UI_CENTRALIZACION_CONTABILIZADA_ANULAR error (Centralizacion = Nombre); the rest run.
    @Test
    @DisplayName("CONT-F-247: con varios tipos, el vigente suma 1 error UI_CENTRALIZACION_CONTABILIZADA_ANULAR y el resto se ejecuta (legacy :247-253)")
    void contF247_vigenteEnMultiplesSumaErrorYContinua() {
        when(repository.queryConfiguraciones(List.of(470L, 471L))).thenReturn(List.of(
                cfg(470L, "TIPO 470", "svcA", "N"), cfg(471L, "TIPO 471", "svcB", "N")));
        when(repository.queryCentralizacionesVigentes(any(), any(), any()))
                .thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        SalidaMultiSeleccion s = orchestrator.ejecutar(entrada("N", null, List.of(470L, 471L)));
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).filteredOn(m -> m.startsWith("UI_CENTRALIZACION_CONTABILIZADA_ANULAR"))
                .singleElement().asString().contains("TIPO 470");
        verify(svcA, never()).centralizar(any());
        verify(svcB).centralizar(any());
    }

    // Legacy CentralizaMultiSeleccion.java:255-261 — a single requested type already posted: 1 error + message, return immediately.
    @Test
    @DisplayName("CONT-F-248: un solo tipo vigente devuelve 1 error UI_CENTRALIZACION_CONTABILIZADA_ANULAR sin ejecutar (legacy :255-261)")
    void contF248_vigenteUnicoRetornaError() {
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        when(repository.queryCentralizacionesVigentes(any(), any(), any()))
                .thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        SalidaMultiSeleccion s = orchestrator.ejecutar(entrada("N", null, List.of(470L)));
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).singleElement().asString()
                .startsWith("UI_CENTRALIZACION_CONTABILIZADA_ANULAR").contains("TIPO 470");
        verify(svcA, never()).centralizar(any());
        assertThat(s.resultados()).isEmpty();
    }

    // Legacy CentralizaMultiSeleccion.java:347-348 — the delegate's P_OUT_ERRORESCONTADOR and P_OUT_MENSAJES reach the output.
    @Test
    @DisplayName("CONT-F-249: errores y mensajes devueltos por el tipo llegan a la salida (legacy :347-348)")
    void contF249_erroresYMensajesDelTipoSePropagan() {
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        when(svcA.centralizar(any())).thenReturn(new SalidaCentralizacion(1L,
                List.of("UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA:Moneda=EUR"), List.of(), List.of(), List.of(), List.of(), List.of()));
        SalidaMultiSeleccion s = orchestrator.ejecutar(entrada("S", null, List.of(470L)));
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).contains("UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA:Moneda=EUR");
    }

    // Legacy CentralizaMultiSeleccion.java:266-269, 428-453 — a type with RequiereFechaConsulta "S" and FechaConsulta after
    // FechaContable adds warning FECHA_VALORIZACION_INVALIDA; the removeIf compares Nombre against Long keys so nothing is removed.
    @Test
    @DisplayName("CONT-F-250: FechaConsulta posterior a FechaContable agrega FECHA_VALORIZACION_INVALIDA y el tipo igual se ejecuta (legacy :266-269,428-453)")
    void contF250_fechaConsultaInvalidaAdvierteYEjecuta() {
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "S")));
        SalidaMultiSeleccion s = orchestrator.ejecutar(entrada("N", "2025-08-07", List.of(470L)));
        assertThat(s.mensajes()).anyMatch(m -> m.startsWith("FECHA_VALORIZACION_INVALIDA"));
        verify(svcA).centralizar(any());
        assertThat(s.erroresContador()).isZero();
    }

    // Legacy CentralizaMultiSeleccion.java:335-343 — EntradaCentralizacion built from the request plus conf.IdInforme.
    @Test
    @DisplayName("CONT-F-251: la entrada del tipo lleva empresa, usuario, modulo, IdInforme de la configuracion, ejercicio y simula (legacy :335-343)")
    void contF251_entradaDelTipo() {
        when(repository.queryConfiguraciones(List.of(470L))).thenReturn(List.of(cfg(470L, "TIPO 470", "svcA", "N")));
        orchestrator.ejecutar(entrada("S", null, List.of(470L)));
        ArgumentCaptor<EntradaCentralizacion> cap = ArgumentCaptor.forClass(EntradaCentralizacion.class);
        verify(svcA).centralizar(cap.capture());
        assertThat(cap.getValue()).isEqualTo(new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, 470L,
                FECHA, "INF-470", EJERCICIO, "S"));
    }
}
