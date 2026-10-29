package cl.bcs.contabilidad.informe;

import cl.bcs.contabilidad.domain.model.basilea.CuentaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.CuentaPropia;
import cl.bcs.contabilidad.domain.model.basilea.DatoBusquedaBasilea;
import cl.bcs.contabilidad.domain.model.basilea.RegistroInterfazBasilea;
import cl.bcs.contabilidad.domain.port.out.InterfazBasileaRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.service.ProcesarInterfazBasileaService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.assertj.core.groups.Tuple;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static cl.bcs.contabilidad.informe.ClienteRelacionadoFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Characterization of legacy {@code ProcesarInterfazBasilea.logica()} (PIB:278-345,
 * legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/
 * ProcesarInterfazBasilea.java): query order and parameters, per-row account resolution, warning/error
 * messages and the "insert only if no errors" rule.
 *
 * <p>Messages keep the legacy {@code MensajeSalida} JSON shape {id, tipo, codigo, descripcion}; Basilea builds them
 * with the 4-arg constructor (PIB:312, 318, 324), so codigo == tipo and descripcion is the literal text.
 */
@ExtendWith(MockitoExtension.class)
class ProcesarInterfazBasileaServiceTest {

    static final String FECHA_SISTEMA = "2025-09-14";
    static final long ID_CLIENTE_CORREDORA = 900L;

    @Mock InterfazBasileaRepository repository;
    @Mock PlataformaPort plataforma;

    ProcesarInterfazBasileaService service;

    // per-source rows; tests fill them before execute()
    List<DatoBusquedaBasilea> acciones = new ArrayList<>(), rfIf = new ArrayList<>(), ventasCortas = new ArrayList<>(),
        pactos = new ArrayList<>(), forwards = new ArrayList<>();
    List<CuentaBasilea> cuentas = new ArrayList<>();
    List<CuentaPropia> cuentaPropia = new ArrayList<>(List.of(new CuentaPropia(ID_CLIENTE_CORREDORA, RUT_CORREDORA)));

    @BeforeEach
    void setUp() {
        service = new ProcesarInterfazBasileaService(repository, plataforma);
        lenient().when(plataforma.obtenerFechaSistema(1L)).thenReturn(FECHA_SISTEMA);
        lenient().when(repository.consultarCuentasBasileaActivas()).thenAnswer(i -> cuentas);
        lenient().when(repository.consultarCuentaPropia(1L)).thenAnswer(i -> cuentaPropia);
        lenient().when(repository.consultarAcciones(ID_CLIENTE_CORREDORA)).thenAnswer(i -> acciones);
        lenient().when(repository.consultarRfIf(ID_CLIENTE_CORREDORA, FECHA_SISTEMA)).thenAnswer(i -> rfIf);
        lenient().when(repository.consultarVentasCortas(ID_CLIENTE_CORREDORA, FECHA_SISTEMA)).thenAnswer(i -> ventasCortas);
        lenient().when(repository.consultarPactosVigentes(FECHA_SISTEMA)).thenAnswer(i -> pactos);
        lenient().when(repository.consultarForward(FECHA_SISTEMA)).thenAnswer(i -> forwards);
    }

    private static JsonObject entrada() {
        JsonObject in = new JsonObject();
        in.addProperty("p_IdEmpresa", 1L);
        in.addProperty("p_IdUsuario", 97L);
        in.addProperty("p_IdModulo", "CON");
        return in;
    }

    @SuppressWarnings("unchecked")
    private List<RegistroInterfazBasilea> insertados() {
        ArgumentCaptor<List<RegistroInterfazBasilea>> cap = ArgumentCaptor.forClass(List.class);
        verify(repository).insertarInterfazBasilea(cap.capture());
        return cap.getValue();
    }

    private static List<JsonObject> mensajes(JsonObject out) {
        List<JsonObject> l = new ArrayList<>();
        JsonArray a = out.getAsJsonArray("P_OUT_MENSAJES");
        for (JsonElement e : a) l.add(e.getAsJsonObject());
        return l;
    }

    private static Tuple msg(JsonObject m) {
        return tuple(m.get("id").getAsLong(), m.get("tipo").getAsString(), m.get("codigo").getAsString(),
            m.get("descripcion").getAsString());
    }

    /** Acciones row exactly as ProcesarInterfazBasilea_SelectAcciones.sql shapes it ('' for the filter columns). */
    private static DatoB accion(String esVentaCorta) {
        return dato().grupo("ACN").producto("ACCIONES").clasificacion("RV").tipoInstrumento("").idEmisor("")
            .nemotecnico("").relacionado("").tipoOperacion("").esVentaCorta(esVentaCorta).nemoBasilea("NEMOFICT")
            .monto("2500000");
    }

    private static CuentaB cuentaAccion(String cta, String esVentaCorta) {
        return cuenta(cta).grupo("ACN").producto("ACCIONES").clasificacion("RV").tipoInstrumento("")
            .esVentaCorta(esVentaCorta);
    }

    // ------------------------------------------------------------------ CONT-D-75

    @Test
    @DisplayName("CONT-D-75: fuentes consultadas en orden acciones, RF-IF, ventas cortas, pactos, forward con sus parámetros")
    void contD75_ordenYParametrosDeConsultas() {
        // PIB:284 FechaSistema; PIB:287 cuentas; PIB:290-292 cuenta propia (get(0)); PIB:296-303
        service.execute(entrada());

        InOrder o = inOrder(repository);
        o.verify(repository).consultarAcciones(ID_CLIENTE_CORREDORA);
        o.verify(repository).consultarRfIf(ID_CLIENTE_CORREDORA, FECHA_SISTEMA);
        o.verify(repository).consultarVentasCortas(ID_CLIENTE_CORREDORA, FECHA_SISTEMA);
        o.verify(repository).consultarPactosVigentes(FECHA_SISTEMA);
        o.verify(repository).consultarForward(FECHA_SISTEMA);
        verify(repository).consultarCuentasBasileaActivas();
        verify(repository).consultarCuentaPropia(1L);
        verify(plataforma).obtenerFechaSistema(1L);
    }

    @Test
    @DisplayName("CONT-D-75: sólo se usa la primera fila de cuenta propia (IdClienteCtaPropia y su RUT)")
    void contD75_primeraCuentaPropia() {
        // PIB:291-292 DatosCtaPropia.get(0)
        cuentaPropia.add(new CuentaPropia(901L, "99999999-9"));
        rfIf.add(dato().build());
        cuentas.add(cuenta("110101000001").build());

        service.execute(entrada());

        verify(repository, never()).consultarAcciones(901L);
        assertThat(insertados()).singleElement().extracting(RegistroInterfazBasilea::rutCliente).isEqualTo("76123456");
    }

    // ------------------------------------------------------------------ CONT-D-76

    @Test
    @DisplayName("CONT-D-76: cada fila con cuenta única genera un registro, en el orden de las fuentes, con FechaEnvio=FechaSistema")
    void contD76_registrosInsertados() {
        // PIB:307-336, PIB:340-342
        acciones.add(accion("N").build());
        rfIf.add(dato().folio("1001").build());
        ventasCortas.add(accion("S").monto("300").build());
        pactos.add(dato().grupo("PACTEADA").folio("2002").tipoReajuste("UF").build());
        forwards.add(dato().grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").folio("3003").build());
        cuentas.addAll(List.of(
            cuentaAccion("CTA-ACC", "N").build(),
            cuentaAccion("CTA-VC", "S").build(),
            cuenta("CTA-RF").build(),
            cuenta("CTA-PC").grupo("PACTEADA").build(),
            cuenta("CTA-FW").grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").build()));

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(mensajes(out)).isEmpty();
        List<RegistroInterfazBasilea> regs = insertados();
        assertThat(regs).extracting(RegistroInterfazBasilea::cuentaContable, RegistroInterfazBasilea::tipoProducto,
                RegistroInterfazBasilea::relacionContable)
            .containsExactly(
                tuple("CTA-ACC", "AC", "ACT"),
                tuple("CTA-RF", "RF", "ACT"),
                tuple("CTA-VC", "AC", "PAC"),
                tuple("CTA-PC", "PC", "PAS"),
                tuple("CTA-FW", "FW", "ACT"));
        assertThat(regs).allSatisfy(r -> {
            assertThat(r.fechaEnvio()).isEqualTo(FECHA_SISTEMA);
            assertThat(r.rutCliente()).isEqualTo("76123456");
            assertThat(r.digitoVerificador()).isEqualTo("K");
        });
        assertThat(regs.get(3).codigoMoneda()).isEqualTo(1L); // PACTEADA + UF
    }

    // ------------------------------------------------------------------ CONT-D-77

    @Test
    @DisplayName("CONT-D-77: fila sin cuenta -> ADVERTENCIA 'No se encontró cuenta para los criterios G_P_TI_CTI', no se inserta, no es error")
    void contD77_sinCuenta_advertenciaYSeOmite() {
        // PIB:316-321
        acciones.add(accion("N").build());                       // IdTipoInstrumento '' -> "ACN_ACCIONES__RV"
        rfIf.add(dato().build());
        cuentas.add(cuenta("CTA-RF").build());

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(mensajes(out)).extracting(ProcesarInterfazBasileaServiceTest::msg).containsExactly(
            tuple(0L, "ADVERTENCIA", "ADVERTENCIA", "No se encontró cuenta para los criterios ACN_ACCIONES__RV"));
        assertThat(insertados()).extracting(RegistroInterfazBasilea::cuentaContable).containsExactly("CTA-RF");
    }

    // ------------------------------------------------------------------ CONT-D-78

    @Test
    @DisplayName("CONT-D-78: cuenta configurada vacía -> ADVERTENCIA 'No existe cuenta configurada...' y el registro SÍ se inserta con cuenta ''")
    void contD78_cuentaVacia_advertenciaYSeInserta() {
        // PIB:322-327 (sin continue) -> PIB:329-335
        forwards.add(dato().grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").build());
        cuentas.add(cuenta("").grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").build());

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(mensajes(out)).extracting(ProcesarInterfazBasileaServiceTest::msg).containsExactly(
            tuple(0L, "ADVERTENCIA", "ADVERTENCIA", "No existe cuenta configurada para grupo NOCIONAL, producto FORWARD"));
        assertThat(insertados()).singleElement().satisfies(r -> {
            assertThat(r.cuentaContable()).isEmpty();
            assertThat(r.tipoProducto()).isEqualTo("FW");
        });
    }

    // ------------------------------------------------------------------ CONT-D-79

    @Test
    @DisplayName("CONT-D-79: más de una cuenta -> 1 ERROR, se corta el recorrido y NO se inserta nada")
    void contD79_multiplesCuentas_errorCortaYNoInserta() {
        // PIB:309-315 (+1 error, putIfAbsent, break); PIB:340 sólo inserta si errores == 0
        acciones.add(accion("N").build());                                     // before the error: warning kept
        rfIf.add(dato().build());                                              // two matching accounts -> ERROR
        rfIf.add(dato().grupo("LIBRE").producto("RF-IF").build());             // never evaluated (break)
        pactos.add(dato().grupo("PACTEADA").tipoInstrumento("NOEXISTE").build()); // never evaluated -> no warning
        cuentas.addAll(List.of(cuenta("CTA-1").build(), cuenta("CTA-2").build()));

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        assertThat(mensajes(out)).extracting(ProcesarInterfazBasileaServiceTest::msg).containsExactlyInAnyOrder(
            tuple(0L, "ERROR", "ERROR",
                "Error al Procesar RF-IF, grupo: LIBRE, se obtuvo más de una cuenta basilea en el filtrado"),
            tuple(0L, "ADVERTENCIA", "ADVERTENCIA", "No se encontró cuenta para los criterios ACN_ACCIONES__RV"));
        verify(repository, never()).insertarInterfazBasilea(anyList());
    }

    // ------------------------------------------------------------------ CONT-D-80

    @Test
    @DisplayName("CONT-D-80: advertencias se deduplican por su clave (G*P*TI*CTI para 'no encontrada', G-P para 'vacía')")
    void contD80_deduplicacionDeMensajes() {
        // PIB:305 HashMap + putIfAbsent con claves distintas por tipo (PIB:311, 317, 323)
        rfIf.add(dato().tipoInstrumento("PDBC").build());
        rfIf.add(dato().tipoInstrumento("PDBC").folio("otro").build());   // same key -> deduplicated
        rfIf.add(dato().tipoInstrumento("PRBC").build());                 // different TI -> second warning
        forwards.add(dato().grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").build());
        forwards.add(dato().grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").folio("x").build());
        cuentas.add(cuenta("").grupo("NOCIONAL").producto("FORWARD").clasificacion("").tipoInstrumento("FWD").build());

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(mensajes(out)).extracting(m -> m.get("descripcion").getAsString()).containsExactlyInAnyOrder(
            "No se encontró cuenta para los criterios LIBRE_RF-IF_PDBC_RF",
            "No se encontró cuenta para los criterios LIBRE_RF-IF_PRBC_RF",
            "No existe cuenta configurada para grupo NOCIONAL, producto FORWARD");
        assertThat(insertados()).hasSize(2); // both forward rows (empty account) are inserted
    }

    // ------------------------------------------------------------------ CONT-D-81

    @Test
    @DisplayName("CONT-D-81: sin cuenta propia configurada -> error (legacy: IndexOutOfBounds) y no se inserta nada")
    void contD81_sinCuentaPropia_error() {
        // PIB:291 DatosCtaPropia.get(0) sobre lista vacía lanza; modern contract: errores >= 1, sin insert
        cuentaPropia.clear();

        JsonObject out = service.execute(entrada());

        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isGreaterThanOrEqualTo(1L);
        verify(repository, never()).insertarInterfazBasilea(any());
        verify(repository, never()).consultarAcciones(any());
    }
}
