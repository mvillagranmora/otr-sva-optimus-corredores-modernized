package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.consultas.ListasLecturaArranque;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioConfiguracion;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioContrato;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaCerrada;
import cl.bcs.fondomutuo.consultas.custodia.ConsultarCustodiaPortafolioCustodiaPort;
import cl.bcs.fondomutuo.consultas.custodiaffmm.ConsultarCustodiaPortafolioFfmmContrato.Entrada;
import cl.bcs.fondomutuo.consultas.custodiaffmm.ConsultarCustodiaPortafolioFfmmContrato.Salida;
import cl.bcs.fondomutuo.consultas.custodiaffmm.ConsultarCustodiaPortafolioFfmmContrato.SalidaCustodia;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * The ADAPTER of the port CPF consumes ({@link ConsultarCustodiaPortafolioCustodiaPort}) implemented by {@link ConsultarCustodiaPortafolioFfmmAdaptador}: JSON contract (the legacy {@code call}),
 * compatibility with what CPF sends and reads (FM-CPFF-21 / 22.8), how it replaces {@link ConsultarCustodiaPortafolioCustodiaCerrada} (selection rule, M1 start-up guard of
 * {@code ListasLecturaArranque}) and that it adds no entry point of its own (FFMM is not a JavaCall: no gate list, no handler). No database: the service is a stub, so these tests do not depend
 * on the goldens.
 */
class FfmmAdaptadorTest {

    /** A service stub that records what it receives and answers a fixed {@link Salida} or raises. */
    private static final class Servicio implements ConsultarCustodiaPortafolioFfmmPort {
        final AtomicReference<Entrada> recibida = new AtomicReference<>();
        Salida respuesta = new Salida();
        RuntimeException falla;

        @Override
        public Salida logica(Entrada entrada) {
            recibida.set(entrada);
            if (falla != null) throw falla;
            return respuesta;
        }
    }

    private static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    // ---- the entry
    @Test
    @DisplayName("FM-CPFF-01: el adaptador entrega al servicio la entrada con los DEFAULTS del pojo (cliente 0, portafolio 0, instrumento 0, bodega '', N, N, '0001-01-01', LIQ, familia '') y los 5 campos sin default nulos")
    void fmCpff01_defaultsDeLaEntrada() {
        Servicio s = new Servicio();
        new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}"));
        Entrada e = s.recibida.get();
        assertEquals(1L, e.p_IdEmpresa);
        assertNull(e.p_IdUsuario);
        assertNull(e.p_IdModulo);
        assertNull(e.p_IdProductoAgrupador);
        assertNull(e.p_IdEmisor);
        assertEquals(0L, e.p_IdCliente);
        assertEquals(0L, e.p_IdPortafolio);
        assertEquals(0L, e.p_IdInstrumento);
        assertEquals("", e.p_IdBoveda);
        assertEquals("N", e.p_AgrupaPorBoveda);
        assertEquals("N", e.p_AgrupaPorRelacion);
        assertEquals("0001-01-01", e.p_FechaConsulta);
        assertEquals("LIQ", e.p_TipoFechaConsulta);
        assertEquals("", e.p_IdFamiliaFondo);
    }

    @Test
    @DisplayName("FM-CPFF-01: un null EXPLICITO sobrescribe el default (Gson), un texto numerico se convierte, una clave desconocida se ignora, 'abc' y 1.5 son JsonSyntaxException")
    void fmCpff01_nulosExplicitosYConversiones() {
        Servicio s = new Servicio();
        ConsultarCustodiaPortafolioFfmmAdaptador a = new ConsultarCustodiaPortafolioFfmmAdaptador(s);
        a.consultar(json("{\"p_IdEmpresa\":\"7\",\"p_IdCliente\":null,\"p_FechaConsulta\":null,\"p_Otro\":1,\"p_IdBoveda\":null,\"p_TipoFechaConsulta\":null}"));
        Entrada e = s.recibida.get();
        assertEquals(7L, e.p_IdEmpresa);
        assertNull(e.p_IdCliente);
        assertNull(e.p_FechaConsulta);
        assertNull(e.p_IdBoveda);
        assertNull(e.p_TipoFechaConsulta);
        assertEquals(0L, e.p_IdPortafolio);
        assertThrows(com.google.gson.JsonSyntaxException.class, () -> a.consultar(json("{\"p_IdEmpresa\":1,\"p_IdCliente\":\"abc\"}")));
        assertThrows(com.google.gson.JsonSyntaxException.class, () -> a.consultar(json("{\"p_IdEmpresa\":1,\"p_IdCliente\":1.5}")));
    }

    @Test
    @DisplayName("FM-CPFF-21: la entrada de 14 campos que CPF serializa (CustodiaEntrada, nulos escritos) llega entera: los 10 campos que CPF asigna y los 4 que deja en sus defaults (bodega '', N, N, LIQ)")
    void fmCpff21_laEntradaQueCpfEnvia() {
        ConsultarCustodiaPortafolioContrato.CustodiaEntrada de = new ConsultarCustodiaPortafolioContrato.CustodiaEntrada();
        de.p_IdEmpresa = 1L;
        de.p_IdUsuario = 7L;
        de.p_IdModulo = "FMM";
        de.p_IdCliente = 100L;
        de.p_IdPortafolio = 500L;
        de.p_IdProductoAgrupador = 3L;
        de.p_IdEmisor = "AGF1";
        de.p_IdInstrumento = 900L;
        de.p_FechaConsulta = "2026-03-02";
        de.p_IdFamiliaFondo = "FAM1";
        JsonObject enviado = new GsonBuilder().serializeNulls().create().toJsonTree(de).getAsJsonObject();
        assertEquals(14, enviado.size());
        Servicio s = new Servicio();
        new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(enviado);
        Entrada e = s.recibida.get();
        assertEquals("1|7|FMM|100|500|3|AGF1|900||N|N|2026-03-02|LIQ|FAM1", String.join("|", String.valueOf(e.p_IdEmpresa), String.valueOf(e.p_IdUsuario), e.p_IdModulo, String.valueOf(e.p_IdCliente),
                String.valueOf(e.p_IdPortafolio), String.valueOf(e.p_IdProductoAgrupador), e.p_IdEmisor, String.valueOf(e.p_IdInstrumento), e.p_IdBoveda, e.p_AgrupaPorBoveda, e.p_AgrupaPorRelacion,
                e.p_FechaConsulta, e.p_TipoFechaConsulta, e.p_IdFamiliaFondo));
    }

    // ---- the answer
    private static SalidaCustodia filaConocida() {
        SalidaCustodia f = new SalidaCustodia();
        f.IdClasificacionTipoInstrumento = "FM";
        f.NombreClasificacionTipoInstrumento = "Fondos Mutuos";
        f.IdTipoInstrumento = "FFM";
        f.NombreTipoInstrumento = "Fondo Mutuo";
        f.IdInstrumento = 900L;
        f.Nemotecnico = "FM001A";
        f.IdEmisor = "AGF1";
        f.IdSocioDeNegocio = 1100L;
        f.IdCliente = 100L;
        f.IdPortafolio = 500L;
        f.DocumentoIdentidad = "11111111-1";
        f.RazonSocial = "Cliente Ficticio Uno";
        f.CodigoPortafolio = "PF500";
        f.IdBoveda = "";
        f.Relacion = "";
        f.IdRegimenFondoMutuo = "GEN";
        f.RegimenTributario = null;
        f.Producto = null;
        f.CantidadCustodia = new BigDecimal("100.00000000");
        f.CantidadGarantia = BigDecimal.ZERO;
        f.CantidadPrecioPromedio = new BigDecimal("110.00000000");
        f.MontoPrecioPromedio = new BigDecimal("0");
        f.INGPrecioPromedioCompra = new BigDecimal("9800");
        f.INGMontoPrecioPromedioCompra = new BigDecimal("990000.00000000");
        f.INGMontoPrecioPromedioMonedaInstrumento = new BigDecimal("980000.00000000");
        f.TotalPosicion = new BigDecimal("110.00000000");
        f.IdMatrizAgrupador = 8L;
        f.EsADC = "N";
        f.PPPCustodia = new BigDecimal("0E+8");
        return f;
    }

    @Test
    @DisplayName("FM-CPFF-02 FM-CPFF-03 FM-CPFF-22.8: la respuesta es {\"p_out_tbl\":[...]} con los 29 campos en orden, los nulos ESCRITOS y los BigDecimal como numero con su escala exacta (0E+8, 980000.00000000)")
    void fmCpff02_formatoExactoDeLaRespuesta() {
        Servicio s = new Servicio();
        s.respuesta.p_out_tbl.add(filaConocida());
        JsonObject out = new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}"));
        assertEquals("{\"p_out_tbl\":[{\"IdClasificacionTipoInstrumento\":\"FM\",\"NombreClasificacionTipoInstrumento\":\"Fondos Mutuos\",\"IdTipoInstrumento\":\"FFM\",\"NombreTipoInstrumento\":\"Fondo Mutuo\","
                + "\"IdInstrumento\":900,\"Nemotecnico\":\"FM001A\",\"IdEmisor\":\"AGF1\",\"IdSocioDeNegocio\":1100,\"IdCliente\":100,\"IdPortafolio\":500,\"DocumentoIdentidad\":\"11111111-1\","
                + "\"RazonSocial\":\"Cliente Ficticio Uno\",\"CodigoPortafolio\":\"PF500\",\"IdBoveda\":\"\",\"Relacion\":\"\",\"IdRegimenFondoMutuo\":\"GEN\",\"RegimenTributario\":null,\"Producto\":null,"
                + "\"CantidadCustodia\":100.00000000,\"CantidadGarantia\":0,\"CantidadPrecioPromedio\":110.00000000,\"MontoPrecioPromedio\":0,\"INGPrecioPromedioCompra\":9800,"
                + "\"INGMontoPrecioPromedioCompra\":990000.00000000,\"INGMontoPrecioPromedioMonedaInstrumento\":980000.00000000,\"TotalPosicion\":110.00000000,\"IdMatrizAgrupador\":8,\"EsADC\":\"N\","
                + "\"PPPCustodia\":0E+8}]}", out.toString());
        assertEquals("{\"p_out_tbl\":[]}", new ConsultarCustodiaPortafolioFfmmAdaptador(new Servicio()).consultar(json("{\"p_IdEmpresa\":1}")).toString());
    }

    @Test
    @DisplayName("FM-CPFF-22.8: lo que CPF lee de vuelta (CustodiaSalida con Gson) conserva el valor Y LA ESCALA de cada BigDecimal y los nulos: no hay perdida en el viaje JSON del puerto")
    void fmCpff22_8_ladoDeCpfConservaValorYEscala() {
        Servicio s = new Servicio();
        s.respuesta.p_out_tbl.add(filaConocida());
        JsonObject out = new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}"));
        ConsultarCustodiaPortafolioContrato.CustodiaSalida leida = new Gson().fromJson(out, ConsultarCustodiaPortafolioContrato.CustodiaSalida.class);
        assertEquals(1, leida.p_out_tbl.size());
        ConsultarCustodiaPortafolioContrato.CustodiaFila f = leida.p_out_tbl.get(0);
        assertEquals("100.00000000", f.CantidadCustodia.toString());
        assertEquals("0E+8", f.PPPCustodia.toString());
        assertEquals("990000.00000000", f.INGMontoPrecioPromedioCompra.toString());
        assertEquals("9800", f.INGPrecioPromedioCompra.toString());
        assertNull(f.RegimenTributario);
        assertEquals("", f.IdBoveda);
        assertEquals(900L, f.IdInstrumento);
    }

    @Test
    @DisplayName("FM-CPFF-22.8: una fila con PPPCustodia nulo se escribe como null (CPF lo vuelve 0) y la lista vacia es []")
    void fmCpff22_2_pppNuloSeEscribeNulo() {
        Servicio s = new Servicio();
        SalidaCustodia f = filaConocida();
        f.PPPCustodia = null;
        s.respuesta.p_out_tbl.add(f);
        JsonObject out = new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}"));
        assertTrue(out.getAsJsonArray("p_out_tbl").get(0).getAsJsonObject().get("PPPCustodia").isJsonNull());
        assertTrue(out.toString().contains("\"PPPCustodia\":null"));
    }

    // ---- errors and state
    @Test
    @DisplayName("FM-CPFF-05 FM-CPFF-22.6: el adaptador NO captura nada: la excepcion del servicio sube SIN CAMBIO (misma instancia, misma clase) hasta CPF (read only: no hay nada que deshacer)")
    void fmCpff22_6_lasExcepcionesSuben() {
        Servicio s = new Servicio();
        s.falla = new IllegalStateException("Empresa no existe");
        RuntimeException visto = assertThrows(IllegalStateException.class, () -> new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}")));
        assertSame(s.falla, visto);
        s.falla = new NullPointerException();
        assertSame(s.falla, assertThrows(NullPointerException.class, () -> new ConsultarCustodiaPortafolioFfmmAdaptador(s).consultar(json("{\"p_IdEmpresa\":1}"))));
    }

    @Test
    @DisplayName("FM-CPFF-03: un Gson NUEVO por llamada (el formato de fecha toma la zona de la JVM al crearlo): dos llamadas, dos instancias, y el texto de una fecha sigue la zona vigente en ese momento")
    void fmCpff03_unGsonNuevoPorLlamada() {
        assertNotSame(ConsultarCustodiaPortafolioFfmmJson.gson(), ConsultarCustodiaPortafolioFfmmJson.gson());
        TimeZone antes = TimeZone.getDefault();
        try {
            java.util.Date d = new java.util.Date(0L);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            String utc = ConsultarCustodiaPortafolioFfmmJson.gson().toJson(d);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+05:00"));
            String mas5 = ConsultarCustodiaPortafolioFfmmJson.gson().toJson(d);
            assertEquals("\"1970-01-01 00:00:00.000\"", utc);
            assertEquals("\"1970-01-01 05:00:00.000\"", mas5);
        } finally {
            TimeZone.setDefault(antes);
        }
        assertTrue(ConsultarCustodiaPortafolioFfmmJson.gson().serializeNulls());
    }

    @Test
    @DisplayName("FM-CPFF-17: el adaptador es el puerto de CPF y NO es un punto de entrada JavaCall propio: sin handler, sin lista de compuerta, sin FQN en la lista blanca del despachador")
    void fmCpff17_noEsUnaEntradaJavaCall() {
        assertTrue(ConsultarCustodiaPortafolioCustodiaPort.class.isAssignableFrom(ConsultarCustodiaPortafolioFfmmAdaptador.class));
        assertFalse(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler.class.isAssignableFrom(ConsultarCustodiaPortafolioFfmmAdaptador.class));
        assertFalse(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher.LISTA_BLANCA.contains("cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFFMM"));
        for (var c : new Class<?>[] { ConsultarCustodiaPortafolioFfmmService.class, ConsultarCustodiaPortafolioFfmmAdaptador.class, ConsultarCustodiaPortafolioFfmmFabrica.class }) {
            assertFalse(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler.class.isAssignableFrom(c), c.getName());
        }
    }

    // ---- how it replaces ConsultarCustodiaPortafolioCustodiaCerrada
    private static final class DatosVacios implements ConsultarCustodiaPortafolioFfmmDatos {
        @Override
        public List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaVigente(Parametros p) {
            return List.of();
        }

        @Override
        public List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaHistorico(Parametros p) {
            return List.of();
        }

        @Override
        public List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaVigente(Parametros p) {
            return List.of();
        }

        @Override
        public List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaHistorico(Parametros p) {
            return List.of();
        }

        @Override
        public List<ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> personasDestino(Long idPersonaCuentaPropia) {
            return List.of();
        }
    }

    private static ApplicationContextRunner contexto() {
        return new ApplicationContextRunner().withUserConfiguration(ConsultarCustodiaPortafolioFfmmConfiguracion.class)
                .withBean(ConfiguracionSistemaPort.class, () -> new ConfiguracionSistemaPort() {
                    @Override
                    public String texto(Long idEmpresa, String modulo, String nombreParametro) {
                        return "2026-03-10";
                    }

                    @Override
                    public long entero(Long idEmpresa, String modulo, String nombreParametro) {
                        return 0;
                    }

                    @Override
                    public BigDecimal decimal(Long idEmpresa, String modulo, String nombreParametro) {
                        return BigDecimal.ZERO;
                    }

                    @Override
                    public Set<String> conjunto(Long idEmpresa, String modulo, String nombreParametro, String delimitador) {
                        return Set.of();
                    }
                })
                .withBean(EmpresaPort.class, () -> id -> new cl.bcs.fondomutuo.kit.InformacionEmpresa())
                .withBean(ConsultarCustodiaPortafolioFfmmDatos.class, DatosVacios::new);
    }

    @Test
    @DisplayName("Regla de seleccion: SIN configuracion el bean de ConsultarCustodiaPortafolioCustodiaPort es el ADAPTADOR REAL (y solo ese: ConsultarCustodiaPortafolioCustodiaCerrada ya no es @Component)")
    void seleccion_elAdaptadorRealEsElBeanPorDefecto() {
        contexto().run(ctx -> {
            assertEquals(1, ctx.getBeansOfType(ConsultarCustodiaPortafolioCustodiaPort.class).size());
            assertInstanceOf(ConsultarCustodiaPortafolioFfmmAdaptador.class, ctx.getBean(ConsultarCustodiaPortafolioCustodiaPort.class));
            assertEquals("{\"p_out_tbl\":[]}", ctx.getBean(ConsultarCustodiaPortafolioCustodiaPort.class).consultar(json("{\"p_IdEmpresa\":1,\"p_FechaConsulta\":\"2026-03-10\"}")).toString());
        });
        contexto().withPropertyValues(ConsultarCustodiaPortafolioFfmmConfiguracion.PROPIEDAD_CERRADA + "=false").run(ctx -> {
            assertEquals(1, ctx.getBeansOfType(ConsultarCustodiaPortafolioCustodiaPort.class).size());
            assertInstanceOf(ConsultarCustodiaPortafolioFfmmAdaptador.class, ctx.getBean(ConsultarCustodiaPortafolioCustodiaPort.class));
        });
        assertFalse(ConsultarCustodiaPortafolioCustodiaCerrada.class.isAnnotationPresent(org.springframework.stereotype.Component.class),
                "CustodiaCerrada must not be scanned any more: it is only the kill switch of the Configuracion");
    }

    @Test
    @DisplayName("Regla de seleccion: con fondomutuo.custodia.ffmm.cerrada=true el bean es ConsultarCustodiaPortafolioCustodiaCerrada (503 G-CUS): un interruptor operativo, no un valor por defecto; sigue habiendo UN solo bean")
    void seleccion_elInterruptorDeCierreVuelveACerrada() {
        contexto().withPropertyValues(ConsultarCustodiaPortafolioFfmmConfiguracion.PROPIEDAD_CERRADA + "=true").run(ctx -> {
            assertEquals(1, ctx.getBeansOfType(ConsultarCustodiaPortafolioCustodiaPort.class).size());
            ConsultarCustodiaPortafolioCustodiaPort p = ctx.getBean(ConsultarCustodiaPortafolioCustodiaPort.class);
            assertInstanceOf(ConsultarCustodiaPortafolioCustodiaCerrada.class, p);
            ServicioNoServidoException e = assertThrows(ServicioNoServidoException.class, () -> p.consultar(json("{\"p_IdEmpresa\":1}")));
            assertEquals("G-CUS", e.compuerta());
        });
    }

    private static ObjectProvider<ConsultarCustodiaPortafolioCustodiaPort> proveedor(ConsultarCustodiaPortafolioCustodiaPort bean) {
        return new StaticListableBeanFactory(Map.of("custodia", bean)).getBeanProvider(ConsultarCustodiaPortafolioCustodiaPort.class);
    }

    @Test
    @DisplayName("Guardia de arranque M1 (ListasLecturaArranque) SIGUE: CPF listado con el adaptador real arranca; CPF listado con CustodiaCerrada detiene el arranque (503 inutil); sin CPF listado ninguno falla")
    void guardiaM1_sigueVigente() {
        Compuertas conCpf = new Compuertas(ConsultarCustodiaPortafolioConfiguracion.LISTA, false, false, false, false, false);
        Compuertas sinCpf = new Compuertas("", false, false, false, false, false);
        ConsultarCustodiaPortafolioCustodiaPort real = new ConsultarCustodiaPortafolioFfmmAdaptador(new Servicio());
        ConsultarCustodiaPortafolioCustodiaPort cerrada = new ConsultarCustodiaPortafolioCustodiaCerrada();
        new ListasLecturaArranque(conCpf, proveedor(real));
        new ListasLecturaArranque(sinCpf, proveedor(cerrada));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ListasLecturaArranque(conCpf, proveedor(cerrada)));
        assertTrue(e.getMessage().contains(ConsultarCustodiaPortafolioConfiguracion.LISTA));
    }
}
