package cl.bcs.fondomutuo.consultas.custodia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * The entry of ConsultarCustodiaPortafolioFondoMutuo is CLOSED BY DEFAULT (plan F3 section 1.7, same switch as the F2 lists): unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link ConsultarCustodiaPortafolioConfiguracion#LISTA} the call is a 503 (gate G0) before anything is read, and the router repeats the request in TomEE. The class is read only:
 * opening it needs NO tesoreria / spot / flujo-caja / owp / custodia flag (the custody FLAG G-CUS guards custody MOVEMENTS; this class only reads the custody calculation), no lock and
 * no sequence. These tests use a stub port that fails if it runs when it must not.
 */
class CpfCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo";
    private static final String VACIA = "{\"P_OUT_TBL\":[]}";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-CPF closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarCustodiaPortafolioPort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarCustodiaPortafolioContrato.Salida();
        };
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class,
                () -> new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas("")).call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-CPF gate: abrir otras listas (NORMAL, RESCATE_REINVERSION, CONSULTAR_SOLICITUDES, CONSULTAR_DETALLE_SOLICITUD, CONSULTAR_FECHA_SOLICITUD) no abre la custodia del portafolio")
    void otrasListasNoLaAbren() {
        ConsultarCustodiaPortafolioPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarCustodiaPortafolioJavaCallHandler h = new ConsultarCustodiaPortafolioJavaCallHandler(servicio,
                compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_SOLICITUDES,CONSULTAR_SOLICITUDES_WS,CONSULTAR_DETALLE_SOLICITUD,CONSULTAR_FECHA_SOLICITUD"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-CPF gate: la lista se llama CONSULTAR_CUSTODIA_PORTAFOLIO y el FQN es el del legacy")
    void nombreDeLaLista() {
        assertEquals("CONSULTAR_CUSTODIA_PORTAFOLIO", ConsultarCustodiaPortafolioConfiguracion.LISTA);
        assertEquals(FQN, ConsultarCustodiaPortafolioConfiguracion.FQN);
    }

    @Test
    @DisplayName("FM-CPF gate: abierta por configuracion => delega sin otra bandera y sin leer la conexion; los defaults del pojo (cliente 0, portafolio 0, instrumento 0, fecha 0001-01-01, familia '', BuscaMLT 'N') y "
            + "los nulos (producto agrupador, emisor) llegan al servicio; la salida usa el contrato del legacy (Utils.getGsonBuilder: solo P_OUT_TBL, sin contador)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarCustodiaPortafolioPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals(0L, e.p_IdCliente);
            assertEquals(0L, e.p_IdPortafolio);
            assertEquals(0L, e.p_IdInstrumento);
            assertEquals("0001-01-01", e.p_FechaConsulta);
            assertNull(e.p_IdProductoAgrupador);
            assertNull(e.p_IdEmisor);
            assertEquals("", e.p_IdFamiliaFondo);
            assertEquals("N", e.p_BuscaMLT);
            return new ConsultarCustodiaPortafolioContrato.Salida();
        };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"Otro\":1}").getAsJsonObject();
        JsonObject out = new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA)).call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals(VACIA, out.toString());
    }

    @Test
    @DisplayName("FM-CPF gate: los valores explicitos de la entrada (incluidos los nulos explicitos que pisan los defaults) llegan al servicio")
    void entradaExplicita() throws Exception {
        ConsultarCustodiaPortafolioPort servicio = e -> {
            assertEquals(101L, e.p_IdCliente);
            assertEquals(502L, e.p_IdPortafolio);
            assertEquals(901L, e.p_IdInstrumento);
            assertEquals("2026-02-27", e.p_FechaConsulta);
            assertEquals(3L, e.p_IdProductoAgrupador);
            assertEquals("AGF2", e.p_IdEmisor);
            assertNull(e.p_IdFamiliaFondo);
            assertNull(e.p_BuscaMLT);
            return new ConsultarCustodiaPortafolioContrato.Salida();
        };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdCliente\":101,\"p_IdPortafolio\":502,\"p_IdInstrumento\":901,\"p_FechaConsulta\":\"2026-02-27\","
                + "\"p_IdProductoAgrupador\":3,\"p_IdEmisor\":\"AGF2\",\"p_IdFamiliaFondo\":null,\"p_BuscaMLT\":null}").getAsJsonObject();
        assertEquals(VACIA, new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA)).call(null, in).toString());
    }

    @Test
    @DisplayName("FM-CPF gate: una entrada vacia llega al servicio con los defaults del pojo y los tres campos de cabecera nulos (no se validan requisitos basicos)")
    void entradaVaciaLlegaConDefaults() throws Exception {
        ConsultarCustodiaPortafolioPort servicio = e -> {
            assertNull(e.p_IdEmpresa);
            assertNull(e.p_IdUsuario);
            assertNull(e.p_IdModulo);
            assertEquals(0L, e.p_IdCliente);
            assertEquals("0001-01-01", e.p_FechaConsulta);
            return new ConsultarCustodiaPortafolioContrato.Salida();
        };
        assertEquals(VACIA, new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA)).call(null, new JsonObject()).toString());
    }

    @Test
    @DisplayName("FM-CPF gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarCustodiaPortafolioPort servicio = e -> new ConsultarCustodiaPortafolioContrato.Salida();
        ConsultarCustodiaPortafolioJavaCallHandler h = new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas(" normal , consultar_custodia_portafolio "));
        assertEquals(VACIA, h.call(null, JsonParser.parseString("{}").getAsJsonObject()).toString());
    }

    @Test
    @DisplayName("FM-CPF allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarCustodiaPortafolioJavaCallHandler(e -> new ConsultarCustodiaPortafolioContrato.Salida(), compuertas("")).fqn());
    }

    @Test
    @DisplayName("FM-CPF contrato D-F3A-25: una fila por defecto se escribe con los 54 campos del legacy en su orden, los nulos escritos (serializeNulls) y los BigDecimal con su toString")
    void contratoDeLaFilaDeSalida() throws Exception {
        String fila = "{\"IdClasificacionTipoInstrumento\":null,\"NombreClasificacionTipoInstrumento\":null,\"IdTipoInstrumento\":null,\"NombreTipoInstrumento\":null,\"IdInstrumento\":null,"
                + "\"Nemotecnico\":null,\"IdSocioDeNegocio\":null,\"IdCliente\":null,\"IdPortafolio\":null,\"DocumentoIdentidad\":null,\"RazonSocial\":null,\"CodigoPortafolio\":null,"
                + "\"IdBoveda\":null,\"Relacion\":null,\"CantidadCustodia\":0,\"Monto\":0,\"IdMoneda\":null,\"ValorCuota\":0,\"DecimalesValorCuota\":null,\"DecimalesCuota\":null,"
                + "\"CantidadGarantia\":0,\"CantidadTransSolicitudesAporte\":0,\"CantidadTransSolicitudesRescate\":0,\"MontoTransSolicitudesAporte\":null,\"MontoTransSolicitudesRescate\":null,"
                + "\"INGPrecioPromedioCompra\":0,\"INGMontoPrecioPromedioCompra\":0,\"INGMontoPrecioPromedioMonedaInstrumento\":0,\"PPPCustodia\":0,\"IdMonedaPrincipal\":null,"
                + "\"IdMonedaFondo\":null,\"Producto\":null,\"IdEmisor\":null,\"MLT\":null,\"IdSerieFondo\":null,\"IdEmpresa\":null,\"IdFamiliaFondo\":null,\"FondoExtranjero\":null,"
                + "\"MontoMonedaSistema\":null,\"FechaValorCuota\":null,\"IdRegimenFondoMutuo\":null,\"IdMatrizAgrupador\":null,\"EsADC\":null,\"Fondo\":null,\"NombreFondo\":null,"
                + "\"ValorCuotaMonedaSistema\":null,\"RegimenTributario\":\"\",\"CodigoSerie\":null,\"DocumentoIdentidadAdministradora\":null,\"NombreAdministradora\":null,"
                + "\"RestringeRescate\":\"N\",\"NombreSerieFondo\":null,\"MontoGarantiaMonedaSistema\":0,\"MontoGarantia\":0}";
        ConsultarCustodiaPortafolioPort servicio = e -> {
            ConsultarCustodiaPortafolioContrato.Salida s = new ConsultarCustodiaPortafolioContrato.Salida();
            s.P_OUT_TBL.add(new ConsultarCustodiaPortafolioContrato.SalidaCustodia());
            return s;
        };
        JsonObject out = new ConsultarCustodiaPortafolioJavaCallHandler(servicio, compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA)).call(null, new JsonObject());
        assertEquals("{\"P_OUT_TBL\":[" + fila + "]}", out.toString());
        // BigDecimal keeps its scale text (0E-8), as the legacy Gson
        ConsultarCustodiaPortafolioPort conEscala = e -> {
            ConsultarCustodiaPortafolioContrato.Salida s = new ConsultarCustodiaPortafolioContrato.Salida();
            ConsultarCustodiaPortafolioContrato.SalidaCustodia c = new ConsultarCustodiaPortafolioContrato.SalidaCustodia();
            c.MontoMonedaSistema = new BigDecimal("0E-8");
            s.P_OUT_TBL.add(c);
            return s;
        };
        String texto = new ConsultarCustodiaPortafolioJavaCallHandler(conEscala, compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA)).call(null, new JsonObject()).toString();
        assertTrue(texto.contains("\"MontoMonedaSistema\":0E-8"), texto);
    }
}
