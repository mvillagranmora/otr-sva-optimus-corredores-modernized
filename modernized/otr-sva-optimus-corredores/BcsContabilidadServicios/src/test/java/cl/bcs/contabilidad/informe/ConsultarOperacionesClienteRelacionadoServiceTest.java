package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.port.out.ClienteRelacionadoRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.service.ConsultarOperacionesClienteRelacionadoService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Characterization of legacy {@code ConsultarOperacionesClienteRelacionado.call()/logica()}
 * (COCR = legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
 * ConsultarOperacionesClienteRelacionado.java): input validation, query parameters and the JSON shape
 * produced by Gson from {@code ConsultaOperacionesClientesRelacionados_OUT} (COCR:103-109).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarOperacionesClienteRelacionadoServiceTest {

    @Mock ClienteRelacionadoRepository repository;
    @Mock PlataformaPort plataforma;

    ConsultarOperacionesClienteRelacionadoService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarOperacionesClienteRelacionadoService(repository, plataforma);
    }

    private static JsonObject entrada(String fechaInicio, String fechaFin) {
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", 1L);
        in.addProperty("p_IdUsuario", 97L);
        in.addProperty("p_FechaInicio", fechaInicio);
        in.addProperty("p_FechaFin", fechaFin);
        in.addProperty("p_IdModulo", "CON");
        return in;
    }

    private void stubFelices(FuentesOperaciones fuentes) {
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        lenient().when(repository.consultarConceptosNormativos()).thenReturn(List.of());
        lenient().when(repository.consultarOperaciones(anyString(), anyString())).thenReturn(fuentes);
    }

    // ------------------------------------------------------------------ CONT-D-01

    @ParameterizedTest(name = "CONT-D-01: {0} -> {1} rechazado={2}")
    @CsvSource({
        "2025-01-17,2025-04-17,false",   // exactly 3 months, same day -> allowed
        "2025-01-17,2025-04-18,true",    // 3 months + 1 day, end day > start day -> rejected
        "2025-01-17,2025-07-17,true",    // 6 months (legacy main() sample range) -> rejected
        "2025-01-17,2025-05-17,true",    // exactly 4 months, same day -> rejected by 'totalMeses > 3'
        "2025-01-17,2025-05-16,false",   // Period P3M29D (~4 months!), end day 16 < 17 -> allowed (legacy quirk)
        "2025-01-31,2025-04-30,false",   // Period P2M30D -> allowed
        "2025-01-31,2025-05-01,false",   // Period P3M1D but end day 1 < start day 31 -> allowed (legacy quirk)
        "2025-01-15,2025-04-16,true",    // Period P3M1D, end day 16 > 15 -> rejected
        "2024-11-30,2025-02-28,false",   // across year end, P2M29D
        "2025-03-01,2025-03-01,false"    // same day
    })
    void contD01_rangoMaximoTresMeses(String fechaInicio, String fechaFin, boolean rechazado) {
        // COCR:140-149: Period.between; totalMeses > 3 || (== 3 && finDia > inicioDia) -> "FECHA_FUERA_RANGO"
        stubFelices(fuentes().build());

        JsonObject out = service.execute(entrada(fechaInicio, fechaFin));

        if (rechazado) {
            assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
            assertThat(out.get("P_OUT_MENSAJE").getAsString()).isEqualTo("FECHA_FUERA_RANGO");
            // COCR:148 returns before loading conditions or operations
            verify(repository, never()).consultarConceptosNormativos();
            verify(repository, never()).consultarOperaciones(anyString(), anyString());
        } else {
            assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
            verify(repository).consultarOperaciones(fechaInicio, fechaFin);
        }
    }

    // ------------------------------------------------------------------ CONT-D-02

    @Test
    @DisplayName("CONT-D-02: fecha inicio > fecha fin -> mensaje literal en español, tras cargar condiciones y sin consultar operaciones")
    void contD02_inicioPosteriorAFin() {
        // COCR:142-145 Period negativo pasa el chequeo de rango; COCR:154-161 carga condiciones; COCR:166-170
        stubFelices(fuentes().build());

        JsonObject out = service.execute(entrada("2025-03-10", "2025-03-01"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString())
            .isEqualTo("La fecha de inicio no puede ser posterior a la fecha de fin.");
        verify(repository, never()).consultarOperaciones(anyString(), anyString());
    }

    // ------------------------------------------------------------------ CONT-D-03

    @Test
    @DisplayName("CONT-D-03: fecha con formato inválido -> errores=1 y P_OUT_MENSAJE = mensaje de DateTimeParseException")
    void contD03_formatoFechaInvalido() {
        // COCR:135 ofPattern("yyyy-MM-dd"); COCR:140 parse; COCR:351-354 catch -> ex.getMessage()
        stubFelices(fuentes().build());

        JsonObject out = service.execute(entrada("2025/01/17", "2025-02-17"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString())
            .isEqualTo("Text '2025/01/17' could not be parsed at index 4");
    }

    @Test
    @DisplayName("CONT-D-03: parámetro PLA.MonedaSistema ausente -> se reporta antes que el rango inválido")
    void contD03_monedaSistemaAusente_seLeePrimero() {
        // COCR:138 se lee antes de validar fechas; excepción -> COCR:351-354
        when(plataforma.obtenerMonedaSistema(1L))
            .thenThrow(new RuntimeException("Parametro PLA.MonedaSistema en modulo PLATAFORMA no encontrado"));

        JsonObject out = service.execute(entrada("2025-01-01", "2025-12-31"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString())
            .isEqualTo("Parametro PLA.MonedaSistema en modulo PLATAFORMA no encontrado");
    }

    // ------------------------------------------------------------------ CONT-D-04

    @Test
    @DisplayName("CONT-D-04: falla al cargar condiciones -> su mensaje gana sobre 'inicio posterior a fin'")
    void contD04_fallaCondiciones_precedeAValidacionDeOrden() {
        // COCR:156-161 (ConsultarCondicionesConceptosNormativos.logica captura y devuelve ex.getMessage(), :115-118)
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(repository.consultarConceptosNormativos()).thenThrow(new RuntimeException("falla condiciones"));

        JsonObject out = service.execute(entrada("2025-03-10", "2025-03-01"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString()).isEqualTo("falla condiciones");
    }

    // ------------------------------------------------------------------ CONT-D-05

    @Test
    @DisplayName("CONT-D-05: error de BD al consultar operaciones -> errores=1, P_OUT_MENSAJE = mensaje de la excepción")
    void contD05_errorConsultaOperaciones() {
        // COCR:175-181 queries; COCR:351-354
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(repository.consultarConceptosNormativos()).thenReturn(List.of());
        when(repository.consultarOperaciones("2025-03-01", "2025-03-31")).thenThrow(new RuntimeException("falla-bd"));

        JsonObject out = service.execute(entrada("2025-03-01", "2025-03-31"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.get("P_OUT_MENSAJE").getAsString()).isEqualTo("falla-bd");
    }

    // ------------------------------------------------------------------ CONT-D-06

    @Test
    @DisplayName("CONT-D-06: las consultas reciben las fechas tal cual (strings yyyy-MM-dd) y la moneda sale de PLA.MonedaSistema")
    void contD06_parametrosDeConsulta() {
        // COCR:151-153 params FechaInicio/FechaFin = strings de entrada; SQL usa TO_VARCHAR(..) BETWEEN :FechaInicio AND :FechaFin
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(repository.consultarConceptosNormativos()).thenReturn(List.of(
            concepto(1, "Comisiones", cond("TipoDato", "MONTO_COMISION"), cond("TipoInstrumento", "ACN"))));
        when(repository.consultarOperaciones("2025-03-01", "2025-03-31")).thenReturn(fuentes()
            .comprobantes(comprobante(1).moneda("CLP").operado("9999").comision("150").build()).build());

        JsonObject out = service.execute(entrada("2025-03-01", "2025-03-31"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        // moneda CLP == comprobante CLP -> MONTO_COMISION applies (150), proving MonedaSistema was used
        assertThat(out.getAsJsonArray("conceptos").get(0).getAsJsonObject()
            .getAsJsonObject("totalTransaccion").get("Monto").getAsBigDecimal()).isEqualByComparingTo("150");
        verify(repository, times(1)).consultarOperaciones("2025-03-01", "2025-03-31");
    }

    // ------------------------------------------------------------------ CONT-D-07

    @Test
    @DisplayName("CONT-D-07: JSON de salida conserva nombres del legacy (conceptos/totalTransaccion/saldo/totalizado/operaciones)")
    void contD07_formaJsonExito() {
        // COCR:71-109 clases serializadas por Gson; COCR:120-127 call()
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(repository.consultarConceptosNormativos()).thenReturn(List.of(
            concepto(1, "Acciones", cond("TipoInstrumento", "ACN"))));
        when(repository.consultarOperaciones("2025-03-01", "2025-03-31")).thenReturn(fuentes()
            .comprobantes(comprobante(100).tipoDocumento("FAC").operado("1500.50").total("1600").cobro("99.5")
                .comision("80").gasto("10").impto("9.5").retencion("0").derecho("0").build())
            .spots(dolar(200).compraVenta("V").compra("0").venta("950000").build())
            .pactos(pacto(300).montoInicial("10000000").montoFinal("10025000").perdida("-25000").ganancia("0").build())
            .build());

        JsonObject out = service.execute(entrada("2025-03-01", "2025-03-31"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();

        JsonObject c = out.getAsJsonArray("conceptos").get(0).getAsJsonObject();
        assertThat(c.get("Concepto").getAsString()).isEqualTo("Acciones");
        assertThat(c.getAsJsonObject("totalTransaccion").get("Cantidad").getAsBigDecimal()).isEqualByComparingTo("1");
        assertThat(c.getAsJsonObject("totalTransaccion").get("Monto").getAsBigDecimal()).isEqualByComparingTo("1500.50");
        assertThat(c.getAsJsonObject("totalTransaccion").get("Resultado").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(c.getAsJsonObject("saldo").get("Activos").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(c.getAsJsonObject("saldo").get("Pasivos").getAsBigDecimal()).isEqualByComparingTo("0");

        JsonObject t = out.getAsJsonObject("totalizado");
        assertThat(t.get("Concepto").getAsString()).isEqualTo("Total");
        assertThat(t.getAsJsonObject("totalTransaccion").get("Monto").getAsBigDecimal()).isEqualByComparingTo("1500.50");

        JsonArray ops = out.getAsJsonArray("operaciones");
        assertThat(ops).hasSize(3);
        JsonObject comp = ops.get(0).getAsJsonObject();
        assertThat(comp.get("FolioOperacion").getAsLong()).isEqualTo(100L);
        assertThat(comp.get("FechaOperacion").getAsString()).isEqualTo("2025-03-05");
        assertThat(comp.get("Nombre").getAsString()).isEqualTo(NOMBRE_PERSONA);
        assertThat(comp.get("Portafolio").getAsString()).isEqualTo(PORTAFOLIO);
        assertThat(comp.get("DocumentoIdentidad").getAsString()).isEqualTo(RUT_PERSONA);
        assertThat(comp.get("IdTipoInstrumento").getAsString()).isEqualTo("ACN");
        assertThat(comp.get("TipoDocumento").getAsString()).isEqualTo("FAC");
        assertThat(comp.get("CompraVenta").getAsString()).isEqualTo("C");
        assertThat(comp.get("IdTipoOperacion").getAsString()).isEqualTo("COMACC");
        assertThat(comp.get("IdClasificacionTipoInstrumento").getAsString()).isEqualTo("RV");
        assertThat(comp.get("IdMoneda").getAsString()).isEqualTo("CLP");
        assertThat(comp.get("MontoOperado").getAsBigDecimal()).isEqualByComparingTo("1500.50");
        assertThat(comp.get("MontoTotal").getAsBigDecimal()).isEqualByComparingTo("1600");
        assertThat(comp.get("MontoCobro").getAsBigDecimal()).isEqualByComparingTo("99.5");
        assertThat(comp.get("TotalComision").getAsBigDecimal()).isEqualByComparingTo("80");
        assertThat(comp.get("TotalGasto").getAsBigDecimal()).isEqualByComparingTo("10");
        assertThat(comp.get("TotalImpto").getAsBigDecimal()).isEqualByComparingTo("9.5");
        assertThat(comp.get("TotalRetencion").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(comp.get("TotalDerecho").getAsBigDecimal()).isEqualByComparingTo("0");

        JsonObject spot = ops.get(1).getAsJsonObject();
        assertThat(spot.get("FolioOperacion").getAsLong()).isEqualTo(200L);
        assertThat(spot.get("MontoCompra").getAsBigDecimal()).isEqualByComparingTo("0");
        assertThat(spot.get("MontoVenta").getAsBigDecimal()).isEqualByComparingTo("950000");

        JsonObject pac = ops.get(2).getAsJsonObject();
        assertThat(pac.get("FolioOperacion").getAsLong()).isEqualTo(300L);
        assertThat(pac.get("FechaInicio").getAsString()).isEqualTo("2025-03-03");
        assertThat(pac.get("FechaTermino").getAsString()).isEqualTo("2025-03-10");
        assertThat(pac.get("MontoInicial").getAsBigDecimal()).isEqualByComparingTo("10000000");
        assertThat(pac.get("MontoFinal").getAsBigDecimal()).isEqualByComparingTo("10025000");
        assertThat(pac.get("Perdida").getAsBigDecimal()).isEqualByComparingTo("-25000");
        assertThat(pac.get("Ganancia").getAsBigDecimal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("CONT-D-07: en error 'conceptos' y 'operaciones' vienen como arreglos vacíos")
    void contD07_formaJsonError() {
        // COCR:131-133 listas inicializadas antes de validar; COCR:146-148 return out
        lenient().when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");

        JsonObject out = service.execute(entrada("2025-01-01", "2025-12-31"));

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(out.getAsJsonArray("conceptos")).isEmpty();
        assertThat(out.getAsJsonArray("operaciones")).isEmpty();
    }
}
