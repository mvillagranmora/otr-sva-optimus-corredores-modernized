package cl.bcs.fondomutuo.liquidacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.certificacion.CrearCargoAbonoPort;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.IngresoEgresoGenerado_Salida;

/**
 * F3 W3 T6-B, the FACADE {@code LiquidacionFachada.logica} in isolation (cards FM-LIQ-02..20), with a fake of the data port and fakes of the parameters, the dictionary, the sequences and the three collaborators, all
 * recording their calls in ONE ordered list of events. They pin what the SQL-bound equivalence cases cannot show by themselves: the ORDER of the reads and calls, the exact arguments of the data port (the date rule of
 * the filter, the company 1 of the APV flag, the ids as given), the typed entry handed to Tesoreria, that the facade does NOT recount the errors (the recount is the handler's), that the {@code tipo} of the messages keeps
 * its reference (what a parsed JSON gives never equals the constant), and that every exception goes up UNCHANGED (same instance). Nothing here touches a database.
 *
 * <p>RED BEFORE CODE: without {@code LiquidacionFachada} every test fails with "T6-B not implemented".
 */
class LiqPuertoTest {

    private static java.util.TimeZone zona;

    @BeforeAll
    static void utc() {
        zona = java.util.TimeZone.getDefault();
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
    }

    @AfterAll
    static void restaurarZona() {
        java.util.TimeZone.setDefault(zona);
    }

    /** Everything the facade talks to, recording every call in `eventos` in order. */
    private static final class Mundo {
        final List<String> eventos = new ArrayList<>();
        final List<ApiT6b.LlamadaDatos> llamadasDatos = new ArrayList<>();
        final ApiT6b.RespuestasDatos respuestas = new ApiT6b.RespuestasDatos();
        final Map<String, String> parametros = new HashMap<>(Map.of("PLATAFORMA/PLA.FechaSistema", "2026-03-02", "FONDOMUTUO/LiquidacionCuentaInversionCliente", "N", "FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS", "N",
                "PLATAFORMA/APV.Habilitado", "N"));
        final Map<String, Long> siguiente = new HashMap<>(Map.of(ApiT6b.SECUENCIA_CARTA, 100L, ApiT6b.SECUENCIA_COMPROBANTE, 500L));
        final List<List<String>> actualizaciones = new ArrayList<>();
        final List<CrearIngresoEgreso_Entrada> entradasTesoreria = new ArrayList<>();
        final List<JsonObject> entradasCuenta = new ArrayList<>();
        final List<JsonObject> entradasLog = new ArrayList<>();
        final List<Map<String, String>> valoresDiccionario = new ArrayList<>();
        CrearIngresoEgreso_Salida respuestaTesoreria = new CrearIngresoEgreso_Salida();
        JsonObject respuestaCuenta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_CARGOABONOGENERADO\":[]}").getAsJsonObject();
        Throwable fallaTesoreria;
        Throwable fallaCuenta;
        Throwable fallaLog;

        Mundo() {
            respuestas.alLlamar = nombre -> eventos.add("datos:" + nombre);
            respuestas.alActualizar = lista -> {
                List<String> instantanea = new ArrayList<>();
                for (Object o : lista) instantanea.add(ApiT6b.campo(o, "IdSolicitudAporteRescate") + ":" + ApiT6b.campo(o, "FolioCartaLiquidacion"));
                actualizaciones.add(instantanea);
            };
        }

        Mundo parametro(String clave, String valor) {
            parametros.put(clave, valor);
            return this;
        }

        Mundo tesoreriaGenera(long... referencias) {
            respuestaTesoreria.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<>();
            for (long r : referencias) {
                IngresoEgresoGenerado_Salida g = new IngresoEgresoGenerado_Salida();
                g.NumeroReferencia = r;
                g.IdIngresoEgreso = 9000 + r;
                respuestaTesoreria.P_OUT_INGRESOEGRESOGENERADO.add(g);
            }
            respuestaTesoreria.P_OUT_ERRORESCONTADOR = 0L;
            return this;
        }

        Mundo cuentaGenera(int n) {
            JsonArray a = new JsonArray();
            for (int i = 0; i < n; i++) a.add(JsonParser.parseString("{\"NumeroCargoAbono\":" + (i + 1) + ",\"IdCargoAbonoMovimiento\":" + (5000 + i) + ",\"IdTipoMovimiento\":\"MOVCAR\"}"));
            respuestaCuenta.add("P_OUT_CARGOABONOGENERADO", a);
            return this;
        }

        ConfiguracionSistemaPort configuracion() {
            return new ConfiguracionSistemaPort() {
                @Override
                public String texto(Long idEmpresa, String modulo, String nombreParametro) {
                    eventos.add("cfg:" + idEmpresa + "/" + modulo + "/" + nombreParametro);
                    String v = parametros.get(modulo + "/" + nombreParametro);
                    if (v == null) throw new ParametroNoEncontradoException(nombreParametro, modulo);
                    return v;
                }

                @Override
                public long entero(Long idEmpresa, String modulo, String nombreParametro) {
                    throw new UnsupportedOperationException("the class reads only texts");
                }

                @Override
                public BigDecimal decimal(Long idEmpresa, String modulo, String nombreParametro) {
                    throw new UnsupportedOperationException("the class reads only texts");
                }

                @Override
                public Set<String> conjunto(Long idEmpresa, String modulo, String nombreParametro, String delimitador) {
                    throw new UnsupportedOperationException("the class reads only texts");
                }
            };
        }

        DiccionarioMensajesPort diccionario() {
            return (idEmpresa, codigo, valores) -> {
                eventos.add("dic:" + idEmpresa + "/" + codigo);
                valoresDiccionario.add(valores);
                return "TXT:" + codigo;
            };
        }

        SecuenciaPort secuencias() {
            return (nombre, cantidad) -> {
                eventos.add("seq:" + nombre.replaceAll(".*::", "") + "=" + cantidad);
                List<Long> out = new ArrayList<>();
                for (long k = 0; cantidad != null && k < cantidad; k++) out.add(siguiente.merge(nombre, 1L, Long::sum) - 1);
                return out;
            };
        }

        CrearIngresoEgresoPuerto tesoreria() {
            return entrada -> {
                eventos.add("tesoreria");
                entradasTesoreria.add(entrada);
                if (fallaTesoreria != null) throw sneaky(fallaTesoreria);
                return respuestaTesoreria;
            };
        }

        CrearCargoAbonoPort cuenta() {
            return entrada -> {
                eventos.add("cuenta");
                entradasCuenta.add(entrada.deepCopy());
                if (fallaCuenta != null) throw sneaky(fallaCuenta);
                return respuestaCuenta.deepCopy();
            };
        }

        LogPort log() {
            return entrada -> {
                eventos.add("log");
                entradasLog.add(entrada.deepCopy());
                if (fallaLog != null) throw sneaky(fallaLog);
                return new JsonObject();
            };
        }

        ApiT6b.PorLiquidacion puerto() {
            Object fachada = ApiT6b.fachada(ApiT6b.datosFalso(llamadasDatos, respuestas), configuracion(), diccionario(), secuencias(), tesoreria(), cuenta(), log());
            return ApiT6b.comoPuerto(fachada);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
        throw (E) t;
    }

    /** a solicitud as the SQL gives it: an APORTE of 1.000.000 of the AGF person 7001 by transfer, to be liquidated 2026-03-03; the keywords replace fields */
    private static Object sol(Object... cambios) {
        Object s = ApiT6b.nuevo(ApiT6b.SOLICITUD);
        ApiT6b.con(s, "IdSolicitudAporteRescate", 1L, "TipoSolicitud", "APO", "IdSerieFondo", "FM001A", "IdInstrumento", 900L, "IdTipoInstrumento", "FM", "IdPortafolio", 500L, "IdMoneda", "CLP",
                "IdMonedaFondo", "CLP", "CostoMonedaLocal", "1000000", "Monto", "1000000", "FechaLiquidacion", "2026-03-03", "FechaCertificacion", "2026-03-02", "IdPortafolioAGF", 8001L, "IdMedioDePago", "TRA",
                "IdPersonaAGF", 7001L, "IdMedioDePagoAbonoAGF", "TRA", "IdMedioDePagoCargoAGF", "TRA", "EstadoLiquidacion", "ING", "MontoPago", "0", "MontoComision", "0", "MontoPagoLocal", "0",
                "MontoComisionLocal", "0", "IdOmnibus", 1L, "IdPersona", 1000L, "CodigoPortafolio", "PF500", "IdTipoMovimientoFondoMutuo", "APONORM");
        return ApiT6b.con(s, cambios);
    }

    private static Object modalidad(String medio, String modalidad) {
        return ApiT6b.con(ApiT6b.nuevo(ApiT6b.MODALIDAD), "IdMedioDePago", medio, "IdModalidadDePago", modalidad);
    }

    private static Object retencion(String retiro, String porcentaje, Long regimen) {
        return ApiT6b.con(ApiT6b.nuevo(ApiT6b.RETENCION), "CodigoRetiro", retiro, "PorcentajeRetencion", porcentaje, "IdRegimenTributario", regimen);
    }

    @SuppressWarnings("unchecked")
    private static Object entrada(Long empresa, List<Long> movimientos, Object... filtros) {
        Object e = ApiT6b.nuevo(ApiT6b.ENTRADA);
        ApiT6b.con(e, "p_IdEmpresa", empresa, "p_IdUsuario", 7L, "p_IdModulo", "FMM", "p_SimularOperacion", "N", "p_UsaReglasNegocio", "S");
        for (Long m : movimientos) ((List<Object>) ApiT6b.campo(e, "p_Movimientos")).add(ApiT6b.con(ApiT6b.nuevo(ApiT6b.MOVIMIENTO), "IdMovimiento", m));
        for (Object f : filtros) ((List<Object>) ApiT6b.campo(e, "p_Filtros")).add(f);
        return e;
    }

    private static Object filtro(Object... campos) {
        Object f = ApiT6b.con(ApiT6b.nuevo(ApiT6b.FILTRO), "IdInterno", 0L, "IdEmisor", "", "TipoSolicitud", "", "IdEstado", "", "FechaCertificacion", "", "FechaLiquidacion", "", "IdSerieFondo", "");
        return ApiT6b.con(f, campos);
    }

    private static Object salida(Mundo m, Object entrada) throws Exception {
        return m.puerto().logica(entrada);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> lista(Object o, String campo) {
        return (List<Object>) ApiT6b.campo(o, campo);
    }

    private static JsonObject linea(Mundo m, int n) {
        return JsonParser.parseString(JsonLegacy.conNulos().toJson(m.entradasTesoreria.get(0).p_IngresoEgresoGenerar.get(n))).getAsJsonObject();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // order of the reads and calls
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-02/04/08/13/14/18: the ORDER of the reads and calls: the date of the system, the two parameters of FONDOMUTUO (company of the request), the modalities, APV.Habilitado (company 1), the select by movements, "
            + "the two sequences (G + S = 2), the withholding configuration, Tesoreria, one UPDATE per generated reference, the log")
    void elOrdenDeLasLecturasYLlamadas() throws Exception {
        Mundo m = new Mundo().parametro("PLATAFORMA/APV.Habilitado", "S").tesoreriaGenera(0, 1);
        m.respuestas.porMovimientos.add(sol());
        salida(m, entrada(2L, List.of(1L), filtro()));
        assertThat(m.eventos).containsExactly("cfg:2/PLATAFORMA/PLA.FechaSistema", "cfg:2/FONDOMUTUO/LiquidacionCuentaInversionCliente", "cfg:2/FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS", "datos:modalidadesPago",
                "cfg:1/PLATAFORMA/APV.Habilitado", "datos:porMovimientos", "seq:FolioCartaLiquidacionAporteRescate=2", "seq:SecuenciaComprobanteLiquidacion=2", "datos:configuracionRetencion", "tesoreria",
                "datos:actualizar", "datos:actualizar", "log");
        assertThat(m.llamadasDatos.stream().map(l -> l.metodo)).containsExactly("modalidadesPago", "porMovimientos", "configuracionRetencion", "actualizar", "actualizar");
    }

    @Test
    @DisplayName("FM-LIQ-02/08: with APV disabled the withholding configuration is NOT read and the sequences ask for G values; with zero groups the sequences are asked for 0 (the port answers an empty list)")
    void sinApvNoSeLeeLaRetencion() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.add(sol());
        salida(m, entrada(1L, List.of(1L), filtro()));
        assertThat(m.eventos).contains("seq:FolioCartaLiquidacionAporteRescate=1", "seq:SecuenciaComprobanteLiquidacion=1").doesNotContain("datos:configuracionRetencion");
        Mundo vacio = new Mundo();
        salida(vacio, entrada(1L, List.of(1L), filtro()));
        assertThat(vacio.eventos).contains("seq:FolioCartaLiquidacionAporteRescate=0", "seq:SecuenciaComprobanteLiquidacion=0");
        assertThat(vacio.eventos).as("with an empty selection Tesoreria is still called, once, with empty lists").contains("tesoreria");
        assertThat(vacio.entradasTesoreria.get(0).p_IngresoEgresoGenerar).isEmpty();
        assertThat(vacio.entradasTesoreria.get(0).p_FormaDePago).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // selection
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-04: the movements are passed to the data port AS GIVEN (order, repeats, a null id) with the company of the request; the filters are not read")
    void losMovimientosSePasanTalCual() throws Exception {
        Mundo m = new Mundo();
        salida(m, entrada(2L, java.util.Arrays.asList(7L, 8L, 8L, null), filtro(), filtro(IdEmisor())));
        ApiT6b.LlamadaDatos l = m.llamadasDatos.stream().filter(x -> x.metodo.equals("porMovimientos")).findFirst().orElseThrow();
        assertThat(l.argumentos).containsExactly(2L, java.util.Arrays.asList(7L, 8L, 8L, null));
        assertThat(m.llamadasDatos.stream().map(x -> x.metodo)).doesNotContain("porFiltro");
    }

    private static Object[] IdEmisor() {
        return new Object[] { "IdEmisor", "AGF9" };
    }

    @Test
    @DisplayName("FM-LIQ-05 (primer-filtro, fecha-vacia-0001): with no movements ONLY the first filter is read: its texts as given (empty stays empty, null stays null), an EMPTY date becomes 0001-01-01, a date stays")
    void soloElPrimerFiltro() throws Exception {
        Mundo m = new Mundo();
        salida(m, entrada(2L, List.of(), filtro("IdEmisor", "AGF2", "TipoSolicitud", "APO", "IdEstado", "CRT", "FechaCertificacion", "", "FechaLiquidacion", "2026-03-03", "IdSerieFondo", ""),
                filtro("IdEmisor", "OTRO", "FechaCertificacion", "2026-01-01", "FechaLiquidacion", "2026-01-01")));
        List<ApiT6b.LlamadaDatos> porFiltro = m.llamadasDatos.stream().filter(x -> x.metodo.equals("porFiltro")).toList();
        assertThat(porFiltro).hasSize(1);
        assertThat(porFiltro.get(0).argumentos).containsExactly(2L, "AGF2", "APO", "CRT", "0001-01-01", "2026-03-03", "");
        Mundo nulo = new Mundo();
        salida(nulo, entrada(1L, List.of(), filtro("IdEmisor", null, "TipoSolicitud", null, "IdEstado", null, "IdSerieFondo", null, "FechaCertificacion", "2026-03-02", "FechaLiquidacion", "")));
        assertThat(nulo.llamadasDatos.stream().filter(x -> x.metodo.equals("porFiltro")).findFirst().orElseThrow().argumentos).containsExactly(1L, null, null, null, "2026-03-02", "0001-01-01", null);
    }

    @Test
    @DisplayName("FM-LIQ-05/20: a null date in the first filter raises NullPointerException BEFORE the select; a null filter raises it too (findFirst().get() and the date) and the select never runs")
    void fechasNulasLanzanNPE() throws Exception {
        for (Object f : List.of(filtro("FechaCertificacion", null), filtro("FechaLiquidacion", null))) {
            Mundo m = new Mundo();
            assertThatThrownBy(() -> salida(m, entrada(1L, List.of(), f))).isInstanceOf(NullPointerException.class);
            assertThat(m.llamadasDatos.stream().map(x -> x.metodo)).doesNotContain("porFiltro");
        }
        Mundo nulo = new Mundo();
        Object e = entrada(1L, List.of());
        lista(e, "p_Filtros").add(null);
        assertThatThrownBy(() -> salida(nulo, e)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("FM-LIQ-03 (sin-filtros-sin-mov): no movements and no filters raise NoSuchElementException after the modalities and the APV flag were read, with no select, no sequence and no collaborator")
    void sinMovimientosNiFiltros() {
        Mundo m = new Mundo();
        assertThatThrownBy(() -> salida(m, entrada(1L, List.of()))).isInstanceOf(NoSuchElementException.class);
        assertThat(m.eventos).containsExactly("cfg:1/PLATAFORMA/PLA.FechaSistema", "cfg:1/FONDOMUTUO/LiquidacionCuentaInversionCliente", "cfg:1/FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS", "datos:modalidadesPago",
                "dic:1/NO_SE_INGRESARON_FILTROS", "cfg:1/PLATAFORMA/APV.Habilitado");
    }

    @Test
    @DisplayName("FM-LIQ-03 / FM-LIQ-01: no filters WITH movements adds NO_SE_INGRESARON_FILTROS (id 0, tipo = the constant ERROR by reference, text from the dictionary with a NON-null empty map) and CONTINUES; logica leaves the counter at 0 (the recount is the handler's)")
    void sinFiltrosAvisaYSigue() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.add(sol());
        Object out = salida(m, entrada(1L, List.of(1L)));
        List<Object> mensajes = lista(out, "P_OUT_MENSAJES");
        assertThat(mensajes).hasSize(1);
        MensajeSalida msg = (MensajeSalida) mensajes.get(0);
        assertThat(msg.id).isZero();
        assertThat(msg.tipo).isSameAs(MensajeSalida.MENSAJE_ERROR);
        assertThat(msg.codigo).isEqualTo("NO_SE_INGRESARON_FILTROS");
        assertThat(msg.descripcion).isEqualTo("TXT:NO_SE_INGRESARON_FILTROS");
        assertThat(m.valoresDiccionario).hasSize(1);
        assertThat(m.valoresDiccionario.get(0)).isNotNull().isEmpty();
        assertThat(ApiT6b.campo(out, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(lista(out, "P_OUT_RESULTADO")).hasSize(1);
        assertThat(m.eventos).contains("tesoreria", "log");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // omnibus, groups, lines
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-06 (omnibus-silencioso): VALIDAR = S removes the APORTES whose IdOmnibus is exactly 0, in silence; an aporte with null or 5, and a RESCATE with 0, stay; the sequences count what is left (2 groups) and no UPDATE touches the removed one")
    void elOmnibusQuitaEnSilencio() throws Exception {
        Mundo m = new Mundo().parametro("FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS", "S").tesoreriaGenera(0, 1);
        m.respuestas.porMovimientos.addAll(List.of(sol("IdSolicitudAporteRescate", 1L, "IdOmnibus", 0L), sol("IdSolicitudAporteRescate", 2L, "IdOmnibus", null),
                sol("IdSolicitudAporteRescate", 3L, "TipoSolicitud", "RES", "IdOmnibus", 0L), sol("IdSolicitudAporteRescate", 4L, "IdOmnibus", 5L)));
        Object out = salida(m, entrada(1L, List.of(1L, 2L, 3L, 4L), filtro()));
        assertThat(lista(out, "P_OUT_MENSAJES")).as("no message").isEmpty();
        assertThat(m.eventos).contains("seq:FolioCartaLiquidacionAporteRescate=2", "seq:SecuenciaComprobanteLiquidacion=2");
        assertThat(m.entradasTesoreria.get(0).p_IngresoEgresoGenerar).hasSize(2);
        List<String> ids = new ArrayList<>();
        for (Object r : lista(out, "P_OUT_RESULTADO")) ids.add(String.valueOf(ApiT6b.campo(r, "IdSolicitudAporteRescate")));
        assertThat(ids).doesNotContain("1");
        assertThat(ids).containsExactlyInAnyOrder("2", "4", "3");
    }

    @Test
    @DisplayName("FM-LIQ-07/09: the amount of the line of a group is the BigDecimal SUM of the Monto of its solicitudes (100.000 + 250.000,25 = 350.000,25) and its folio / comprobante are the values the sequences gave (100 / 500)")
    void laLineaDelGrupo() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.addAll(List.of(sol("IdSolicitudAporteRescate", 1L, "Monto", "100000"), sol("IdSolicitudAporteRescate", 2L, "Monto", "250000.25")));
        salida(m, entrada(1L, List.of(1L, 2L), filtro()));
        JsonObject l = linea(m, 0);
        assertThat(new BigDecimal(l.get("Monto").getAsString())).isEqualByComparingTo("350000.25");
        assertThat(l.get("IdComprobante").getAsLong()).isEqualTo(100L);
        assertThat(l.get("IdOperacion").getAsLong()).isEqualTo(500L);
        assertThat(l.get("IdTipoMovimiento").getAsString()).isEqualTo("MOVEGR");
        assertThat(l.get("IdConcepto").getAsString()).isEqualTo("CAAEFM");
        assertThat(l.get("IdTipoOperacion").getAsString()).isEqualTo("APORFM");
        assertThat(l.get("FechaLiquidacion").getAsString()).isEqualTo("2026-03-03");
        assertThat(l.get("FechaOperacion").getAsString()).isEqualTo("2026-03-03");
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago).hasSize(1);
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago.get(0).Monto).isEqualByComparingTo("350000.25");
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago.get(0).IdModalidadDePago).as("the table of modalities is empty: MODCAJ").isEqualTo("MODCAJ");
    }

    @Test
    @DisplayName("FM-LIQ-09: the modality of the payment form comes from the table by medium (TRA => MODDEP) and a null amount raises NullPointerException when the group is summed")
    void laModalidadYElMontoNulo() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.modalidades.add(modalidad("TRA", "MODDEP"));
        m.respuestas.porMovimientos.add(sol());
        salida(m, entrada(1L, List.of(1L), filtro()));
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago.get(0).IdModalidadDePago).isEqualTo("MODDEP");
        Mundo nulo = new Mundo();
        nulo.respuestas.porMovimientos.add(sol("Monto", null));
        assertThatThrownBy(() -> salida(nulo, entrada(1L, List.of(1L), filtro()))).isInstanceOf(NullPointerException.class);
        assertThat(nulo.eventos).as("the sequences were asked before the sum, Tesoreria was not reached").contains("seq:FolioCartaLiquidacionAporteRescate=1", "seq:SecuenciaComprobanteLiquidacion=1").doesNotContain("tesoreria");
        Mundo repetida = new Mundo();
        repetida.respuestas.modalidades.addAll(List.of(modalidad("TRA", "MODDEP"), modalidad("TRA", "MODCAJ")));
        assertThatThrownBy(() -> salida(repetida, entrada(1L, List.of(1L), filtro()))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("FM-LIQ-12/10 (carta-lista-completa) with APV: the client's line of an aporte is MOVING for Monto - MontoComision with reference G + index (1) and goes to Tesoreria and to the forms; the one of a RESCATE is MOVEGR and does not")
    void lasLineasDelCliente() throws Exception {
        Mundo m = new Mundo().parametro("PLATAFORMA/APV.Habilitado", "S").tesoreriaGenera(0, 1);
        m.respuestas.porMovimientos.add(sol("MontoComision", "10000"));
        salida(m, entrada(1L, List.of(1L), filtro()));
        assertThat(m.entradasTesoreria.get(0).p_IngresoEgresoGenerar).hasSize(2);
        JsonObject cliente = linea(m, 1);
        assertThat(cliente.get("NumeroReferencia").getAsLong()).isEqualTo(1L);
        assertThat(cliente.get("IdTipoMovimiento").getAsString()).isEqualTo("MOVING");
        assertThat(cliente.get("IdConcepto").getAsString()).isEqualTo("ABREFM");
        assertThat(cliente.get("IdTipoOperacion").getAsString()).isEqualTo("RESCFM");
        assertThat(cliente.get("IdPersona").getAsLong()).isEqualTo(1000L);
        assertThat(new BigDecimal(cliente.get("Monto").getAsString())).isEqualByComparingTo("990000");
        assertThat(cliente.get("IdComprobante").getAsLong()).isEqualTo(101L);
        assertThat(cliente.get("IdOperacion").getAsLong()).isEqualTo(501L);
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago).hasSize(2);
        assertThat(m.entradasTesoreria.get(0).p_FormaDePago.get(1).NumeroReferencia).isEqualTo(1L);
        Mundo r = new Mundo().parametro("PLATAFORMA/APV.Habilitado", "S").tesoreriaGenera(0);
        r.respuestas.porMovimientos.add(sol("TipoSolicitud", "RES", "MontoComision", "10000"));
        salida(r, entrada(1L, List.of(1L), filtro()));
        assertThat(r.entradasTesoreria.get(0).p_IngresoEgresoGenerar).hasSize(1);
        assertThat(r.entradasTesoreria.get(0).p_FormaDePago).hasSize(1);
    }

    @Test
    @DisplayName("FM-LIQ-11: the withholding of an APV-B RESNORM rescate (MontoPago 1.000.000, 15%, commission 10.000) is 840.000 in the line of the client's account; the first row RES with percentage <> 0 and a regimen null or equal wins; "
            + "other movements, other portfolios and medium CIN have none")
    void laRetencion() throws Exception {
        assertThat(montoCliente("RESNORM", "APV-B", "TRA", List.of(retencion("RES", "0.15", null)), 1L)).isEqualByComparingTo("840000");
        assertThat(montoCliente("RESTRASP", "APV-B", "TRA", List.of(retencion("RES", "0.15", null)), 1L)).isEqualByComparingTo("840000");
        assertThat(montoCliente("RESREINV", "APV-B", "TRA", List.of(retencion("RES", "0.15", null)), 1L)).isEqualByComparingTo("990000");
        assertThat(montoCliente("RESNORM", "APV-A", "TRA", List.of(retencion("RES", "0.15", null)), 1L)).isEqualByComparingTo("990000");
        assertThat(montoCliente("RESNORM", "APV-B", "CIN", List.of(retencion("RES", "0.15", null)), 1L)).isEqualByComparingTo("990000");
        assertThat(montoCliente("RESNORM", "APV-B", "TRA", List.of(retencion("RES", "0", null), retencion("APO", "0.30", null), retencion("RES", "0.10", 99L), retencion("RES", "0.12", 1L), retencion("RES", "0.20", null)), 1L))
                .as("the first applicable row is 12% (regimen 1)").isEqualByComparingTo("870000");
        assertThat(montoCliente("RESNORM", "APV-B", "TRA", List.of(), 1L)).isEqualByComparingTo("990000");
        assertThatThrownBy(() -> montoCliente("RESNORM", "APV-B", "TRA", List.of(retencion("RES", "0.15", 1L)), null)).as("a regimen row against a null matrix").isInstanceOf(NullPointerException.class);
    }

    /** the Monto of the client's line (the third line given to CrearCargoAbono: two groups first) of an APV-B rescate, seen through the cuenta de inversion */
    private static BigDecimal montoCliente(String movimiento, String portafolio, String medio, List<Object> filas, Long matriz) throws Exception {
        Mundo m = new Mundo().parametro("PLATAFORMA/APV.Habilitado", "S").parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0, 1, 2, 3).cuentaGenera(0);
        m.respuestas.retencion.addAll(filas);
        m.respuestas.porMovimientos.add(sol("IdSolicitudAporteRescate", 1L, "TipoSolicitud", "RES", "IdTipoMovimientoFondoMutuo", movimiento, "CodigoPortafolio", portafolio, "IdMedioDePago", medio, "MontoPago", "1000000",
                "MontoComision", "10000", "IdMatrizAgrupadorAPV", matriz, "IdPersonaAGF", 7001L));
        m.respuestas.porMovimientos.add(sol("IdSolicitudAporteRescate", 2L, "TipoSolicitud", "RES", "IdMedioDePago", "CIN", "IdPersonaAGF", 7002L));
        salida(m, entrada(1L, List.of(1L, 2L), filtro()));
        JsonArray lineas = m.entradasCuenta.get(0).getAsJsonArray("p_CargoAbonoAGenerar");
        assertThat(lineas.size()).isEqualTo(4);
        return new BigDecimal(lineas.get(2).getAsJsonObject().get("Monto").getAsString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Tesoreria
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-13: Tesoreria gets the header of the request, p_EsIngresoAutomatico N, and the defaults of its own class for everything else")
    void laCabeceraDeTesoreria() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.add(sol());
        Object e = entrada(2L, List.of(1L), filtro());
        ApiT6b.con(e, "p_IdUsuario", 15L, "p_IdModulo", "XYZ", "p_SimularOperacion", "S", "p_UsaReglasNegocio", "N");
        salida(m, e);
        CrearIngresoEgreso_Entrada t = m.entradasTesoreria.get(0);
        assertThat(t.p_IdEmpresa).isEqualTo(2L);
        assertThat(t.p_IdUsuario).isEqualTo(15L);
        assertThat(t.p_IdModulo).isEqualTo("XYZ");
        assertThat(t.p_SimularOperacion).isEqualTo("S");
        assertThat(t.p_UsaReglasNegocio).isEqualTo("N");
        assertThat(t.p_EsIngresoAutomatico).isEqualTo("N");
        assertThat(t.p_IdOrigenProceso).isNull();
        assertThat(t.p_EsIngresoUI).isEqualTo("N");
        assertThat(t.p_FechaMovimiento).isEqualTo("0001-01-01");
    }

    @Test
    @DisplayName("FM-LIQ-13 (mensajes-perdidos, conteo-por-referencia): a Tesoreria counter > 0 stops the run and the output is ITS counter and ITS messages, the earlier ones lost, the tipo of each message keeping the REFERENCE it had; no UPDATE, no log")
    void unErrorDeTesoreria() throws Exception {
        Mundo m = new Mundo();
        m.respuestas.porMovimientos.add(sol());
        String tipo = new String("ERROR");
        m.respuestaTesoreria.P_OUT_ERRORESCONTADOR = 2L;
        m.respuestaTesoreria.P_OUT_MENSAJES.add(new cl.bcs.tesoreria.javacall.comun.MensajeSalida(7L, tipo, "ERR_TES", "falla de tesoreria"));
        Object out = salida(m, entrada(1L, List.of(1L)));
        assertThat(ApiT6b.campo(out, "P_OUT_ERRORESCONTADOR")).isEqualTo(2L);
        List<Object> mensajes = lista(out, "P_OUT_MENSAJES");
        assertThat(mensajes).hasSize(1);
        MensajeSalida msg = (MensajeSalida) mensajes.get(0);
        assertThat(msg.id).isEqualTo(7L);
        assertThat(msg.codigo).isEqualTo("ERR_TES");
        assertThat(msg.descripcion).isEqualTo("falla de tesoreria");
        assertThat(msg.tipo).as("the reference is kept: a String that is not the constant never counts in the recount of the handler").isSameAs(tipo).isNotSameAs(MensajeSalida.MENSAJE_ERROR);
        assertThat(lista(out, "P_OUT_RESULTADO")).isEmpty();
        assertThat(m.eventos).doesNotContain("datos:actualizar", "log", "cuenta");
    }

    @Test
    @DisplayName("FM-LIQ-13: a null counter or a counter 0 with ERROR messages does not stop the run, and the messages of Tesoreria are not copied")
    void tesoreriaQueNoCorta() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestaTesoreria.P_OUT_ERRORESCONTADOR = null;
        m.respuestaTesoreria.P_OUT_MENSAJES.add(new cl.bcs.tesoreria.javacall.comun.MensajeSalida(0L, "ERROR", "NO_CUENTA", "x"));
        m.respuestas.porMovimientos.add(sol());
        Object out = salida(m, entrada(1L, List.of(1L), filtro()));
        assertThat(lista(out, "P_OUT_MENSAJES")).isEmpty();
        assertThat(lista(out, "P_OUT_RESULTADO")).hasSize(1);
        assertThat(m.eventos).contains("datos:actualizar", "log");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the update
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-14 (estado-ING): per generated reference the solicitudes get FolioCartaLiquidacion = the folio of THAT reference BEFORE the UPDATE (100 for reference 0, 101 for 1) and each is reported with Codigo UI_EXITO, the state READ (ING), NombreEstado empty")
    void laActualizacion() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(1, 0);
        m.respuestas.porMovimientos.addAll(List.of(sol("IdSolicitudAporteRescate", 1L, "IdPersonaAGF", 7001L), sol("IdSolicitudAporteRescate", 2L, "IdPersonaAGF", 7002L)));
        Object out = salida(m, entrada(1L, List.of(1L, 2L), filtro()));
        assertThat(m.actualizaciones).hasSize(2);
        assertThat(m.actualizaciones.get(0)).as("reference 1 first (the order Tesoreria answered)").hasSize(1).first().asString().endsWith(":101");
        assertThat(m.actualizaciones.get(1)).hasSize(1).first().asString().endsWith(":100");
        List<Object> resultados = lista(out, "P_OUT_RESULTADO");
        assertThat(resultados).hasSize(2);
        for (Object r : resultados) {
            assertThat(ApiT6b.campo(r, "IdInterno")).isEqualTo(0L);
            assertThat(ApiT6b.campo(r, "IdSerieFondo")).isEqualTo("FM001A");
            assertThat(ApiT6b.campo(r, "Codigo")).isEqualTo("UI_EXITO");
            assertThat(ApiT6b.campo(r, "Estado")).isEqualTo("ING");
            assertThat(ApiT6b.campo(r, "NombreEstado")).isEqualTo("");
        }
    }

    @Test
    @DisplayName("FM-LIQ-14/20: a reference Tesoreria answers that has no solicitudes (99, null) raises NullPointerException; an empty list of generated movements updates nothing")
    void referenciasDesconocidas() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(99);
        m.respuestas.porMovimientos.add(sol());
        assertThatThrownBy(() -> salida(m, entrada(1L, List.of(1L), filtro()))).isInstanceOf(NullPointerException.class);
        Mundo vacio = new Mundo().tesoreriaGenera();
        vacio.respuestas.porMovimientos.add(sol());
        Object out = salida(vacio, entrada(1L, List.of(1L), filtro()));
        assertThat(lista(out, "P_OUT_RESULTADO")).isEmpty();
        assertThat(vacio.eventos).doesNotContain("datos:actualizar");
        assertThat(vacio.eventos).contains("log");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // cuenta de inversion
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static Mundo conCin() {
        Mundo m = new Mundo().parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0).cuentaGenera(1);
        m.respuestas.porMovimientos.add(sol("IdMedioDePago", "CIN"));
        return m;
    }

    @Test
    @DisplayName("FM-LIQ-15 (cin-referencia-0, movcar-siempre): with the client's account and a CIN solicitud CrearCargoAbono gets header FMM / N / S / fecha del sistema / request user and ONE line per line of the list, MOVCAR / CAPEFM / ABPEFM with the AGF portfolio")
    void laLlamadaALaCuentaDeInversion() throws Exception {
        Mundo m = conCin();
        Object out = salida(m, entrada(1L, List.of(1L), filtro()));
        JsonObject c = m.entradasCuenta.get(0);
        assertThat(c.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(c.get("p_IdUsuario").getAsLong()).isEqualTo(7L);
        assertThat(c.get("p_IdModulo").getAsString()).isEqualTo("FMM");
        assertThat(c.get("p_simularOperacion").getAsString()).isEqualTo("N");
        assertThat(c.get("p_UsaReglasNegocio").getAsString()).isEqualTo("S");
        assertThat(c.get("p_FechaSistema").getAsString()).isEqualTo("2026-03-02");
        assertThat(c.get("p_EsIngresoManual").getAsString()).isEqualTo("N");
        JsonObject o = c.getAsJsonArray("p_CargoAbonoAGenerar").get(0).getAsJsonObject();
        assertThat(o.get("NumeroCargoAbono").getAsLong()).isEqualTo(1L);
        assertThat(o.get("IdPortafolio").getAsLong()).isEqualTo(8001L);
        assertThat(o.get("IdTipoMovimiento").getAsString()).isEqualTo("MOVCAR");
        assertThat(o.get("IdConcepto").getAsString()).isEqualTo("CAPEFM");
        assertThat(o.get("Descripcion").getAsString()).isEqualTo("RESCATE_FONDO_MUTUO");
        assertThat(o.get("IdTipoOperacion").getAsString()).isEqualTo("ABPEFM");
        assertThat(o.get("IdOperacion").getAsLong()).isEqualTo(100L);
        assertThat(o.get("FolioComprobante").getAsString()).isEqualTo("100");
        assertThat(o.get("IdComprobanteDetalle").getAsLong()).isEqualTo(100L);
        assertThat(o.get("IdComprobanteFormaDePago").getAsLong()).isEqualTo(100L);
        assertThat(o.get("IdComprobante").isJsonNull()).isTrue();
        assertThat(o.get("FechaVencimientoOperacion").isJsonNull()).isTrue();
        assertThat(o.get("CertificadoAutomatico").getAsString()).isEqualTo("S");
        assertThat(lista(out, "P_OUT_RESULTADO")).as("one from the Tesoreria pass and one for the generated movement").hasSize(2);
        assertThat(m.eventos.subList(m.eventos.indexOf("tesoreria"), m.eventos.size())).containsExactly("tesoreria", "datos:actualizar", "cuenta", "datos:actualizar", "log");
    }

    @Test
    @DisplayName("FM-LIQ-17 (cin-referencia-0) / D-F3B-26: two CIN solicitudes in group 0 read secuencia_carta.get(K) with K counting the solicitudes: the second asks index 1 of a list of 1 and raises IndexOutOfBoundsException")
    void dosCinEnElGrupoCero() throws Exception {
        Mundo m = new Mundo().parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0).cuentaGenera(1);
        m.respuestas.porMovimientos.addAll(List.of(sol("IdSolicitudAporteRescate", 1L, "IdMedioDePago", "CIN"), sol("IdSolicitudAporteRescate", 2L, "IdMedioDePago", "CIN")));
        assertThatThrownBy(() -> salida(m, entrada(1L, List.of(1L, 2L), filtro()))).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(m.entradasCuenta).as("the call WAS made (one line of 2.000.000)").hasSize(1);
        assertThat(new BigDecimal(m.entradasCuenta.get(0).getAsJsonArray("p_CargoAbonoAGenerar").get(0).getAsJsonObject().get("Monto").getAsString())).isEqualByComparingTo("2000000");
    }

    @Test
    @DisplayName("FM-LIQ-15 (producto-n-por-m): one call per CIN solicitud with ALL the lines, numbered continuously: two CIN solicitudes of two groups => numbers 1,2 then 3,4")
    void unaLlamadaPorCin() throws Exception {
        Mundo m = new Mundo().parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0, 1).cuentaGenera(1);
        m.respuestas.porMovimientos.addAll(List.of(sol("IdSolicitudAporteRescate", 1L, "IdMedioDePago", "CIN", "IdPersonaAGF", 7001L), sol("IdSolicitudAporteRescate", 2L, "IdMedioDePago", "CIN", "IdPersonaAGF", 7002L)));
        salida(m, entrada(1L, List.of(1L, 2L), filtro()));
        assertThat(m.entradasCuenta).hasSize(2);
        List<Long> numeros = new ArrayList<>();
        for (JsonObject c : m.entradasCuenta) for (JsonElement e : c.getAsJsonArray("p_CargoAbonoAGenerar")) numeros.add(e.getAsJsonObject().get("NumeroCargoAbono").getAsLong());
        assertThat(numeros).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    @DisplayName("FM-LIQ-15: no call unless the client's account is S AND some solicitud has medium CIN")
    void cuandoNoSeLlama() throws Exception {
        Mundo sinCuenta = new Mundo().tesoreriaGenera(0);
        sinCuenta.respuestas.porMovimientos.add(sol("IdMedioDePago", "CIN"));
        salida(sinCuenta, entrada(1L, List.of(1L), filtro()));
        assertThat(sinCuenta.eventos).doesNotContain("cuenta");
        Mundo sinCin = new Mundo().parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0);
        sinCin.respuestas.porMovimientos.add(sol());
        salida(sinCin, entrada(1L, List.of(1L), filtro()));
        assertThat(sinCin.eventos).doesNotContain("cuenta");
        Mundo nulo = new Mundo().parametro("FONDOMUTUO/LiquidacionCuentaInversionCliente", "S").tesoreriaGenera(0);
        nulo.respuestas.porMovimientos.add(sol("IdMedioDePago", null));
        assertThatThrownBy(() -> salida(nulo, entrada(1L, List.of(1L), filtro()))).as("a null medium: NullPointerException after the first update").isInstanceOf(NullPointerException.class);
        assertThat(nulo.eventos).contains("datos:actualizar");
    }

    @Test
    @DisplayName("FM-LIQ-16 (conteo-por-referencia) / D-REQ-11: an error of CuentaInversion (counter > 0, or an ERROR message whatever the case with counter 0) stops the run: the output is ITS counter and messages, the results already gathered stay, the tipo parsed from the JSON is NOT the constant, and the log is not reached")
    void unErrorDeCuentaInversion() throws Exception {
        Mundo m = conCin();
        m.respuestaCuenta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":3,\"P_OUT_MENSAJES\":[{\"id\":9,\"tipo\":\"ERROR\",\"codigo\":\"ERR_CIN\",\"descripcion\":\"falla\"}],\"P_OUT_CARGOABONOGENERADO\":[]}").getAsJsonObject();
        Object out = salida(m, entrada(1L, List.of(1L), filtro()));
        assertThat(ApiT6b.campo(out, "P_OUT_ERRORESCONTADOR")).isEqualTo(3L);
        MensajeSalida msg = (MensajeSalida) lista(out, "P_OUT_MENSAJES").get(0);
        assertThat(msg.codigo).isEqualTo("ERR_CIN");
        assertThat(msg.tipo).isEqualTo("ERROR").isNotSameAs(MensajeSalida.MENSAJE_ERROR);
        assertThat(lista(out, "P_OUT_RESULTADO")).hasSize(1);
        assertThat(m.eventos).doesNotContain("log");
        Mundo cero = conCin();
        cero.respuestaCuenta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"error\",\"codigo\":\"ERR_MIN\",\"descripcion\":\"d\"}],\"P_OUT_CARGOABONOGENERADO\":[]}").getAsJsonObject();
        Object out2 = salida(cero, entrada(1L, List.of(1L), filtro()));
        assertThat(ApiT6b.campo(out2, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(lista(out2, "P_OUT_MENSAJES")).hasSize(1);
        assertThat(cero.eventos).doesNotContain("log");
        Mundo aviso = conCin();
        aviso.respuestaCuenta = JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"ADVERTENCIA\",\"codigo\":\"ADV\",\"descripcion\":\"d\"}],\"P_OUT_CARGOABONOGENERADO\":[]}").getAsJsonObject();
        Object out3 = salida(aviso, entrada(1L, List.of(1L), filtro()));
        assertThat(lista(out3, "P_OUT_MENSAJES")).isEmpty();
        assertThat(aviso.eventos).contains("log");
        Mundo nuloContador = conCin();
        nuloContador.respuestaCuenta.add("P_OUT_ERRORESCONTADOR", com.google.gson.JsonNull.INSTANCE);
        assertThatThrownBy(() -> salida(nuloContador, entrada(1L, List.of(1L), filtro()))).isInstanceOf(NullPointerException.class);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the log
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-18 (log-sin-asignar) / D-F3B-19: AgregarLog gets the DEFAULT entry whatever the filters are (three of them): p_IdTrabajo 1, p_Log and p_LogDetalle empty, the header null, as the legacy JSON writes it")
    void elLogVaVacio() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.add(sol());
        salida(m, entrada(1L, List.of(1L), filtro("IdInterno", 101L), filtro("IdInterno", 102L), filtro("IdInterno", null)));
        assertThat(m.entradasLog).hasSize(1);
        assertThat(m.entradasLog.get(0)).isEqualTo(JsonParser.parseString("{\"p_IdEmpresa\":null,\"p_IdUsuario\":null,\"p_IdModulo\":null,\"p_UsaReglasNegocio\":null,\"p_IdEstadoAutorizacion\":null,"
                + "\"p_IdTrabajo\":1,\"p_Log\":[],\"p_LogDetalle\":[]}"));
        assertThat(m.eventos.get(m.eventos.size() - 1)).as("the log is the last step").isEqualTo("log");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // exceptions: the SAME instance goes up, whoever raises it
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-20: whatever the data port, a parameter, Tesoreria, CuentaInversion or the log raise (runtime, checked, Error) comes out of logica UNCHANGED (same instance), the legacy has no catch")
    void lasExcepcionesSubenIntactas() throws Exception {
        List<Throwable> todas = List.of(new IllegalStateException("estado"), new IOException("io"), new SQLException("sql"), new Exception("plana"), new AssertionError("error"));
        for (Throwable t : todas) {
            if (t instanceof RuntimeException || t instanceof Error) {   // the data port declares no checked exception (unchecked JDBC layer)
                Mundo datos = new Mundo();
                datos.respuestas.falla = t;
                assertThat(catchThrowable(() -> salida(datos, entrada(1L, List.of(1L), filtro())))).as("data port: " + t).isSameAs(t);
            }
            Mundo tesoreria = new Mundo();
            tesoreria.respuestas.porMovimientos.add(sol());
            tesoreria.fallaTesoreria = t;
            assertThat(catchThrowable(() -> salida(tesoreria, entrada(1L, List.of(1L), filtro())))).as("Tesoreria: " + t).isSameAs(t);
            Mundo cuenta = conCin();
            cuenta.fallaCuenta = t;
            assertThat(catchThrowable(() -> salida(cuenta, entrada(1L, List.of(1L), filtro())))).as("CuentaInversion: " + t).isSameAs(t);
            Mundo log = new Mundo().tesoreriaGenera(0);
            log.respuestas.porMovimientos.add(sol());
            log.fallaLog = t;
            assertThat(catchThrowable(() -> salida(log, entrada(1L, List.of(1L), filtro())))).as("log: " + t).isSameAs(t);
        }
        Mundo parametro = new Mundo();
        parametro.parametros.remove("FONDOMUTUO/VALIDAR_LIQUIDACION_OMNIBUS");
        assertThatThrownBy(() -> salida(parametro, entrada(1L, List.of(1L), filtro()))).isInstanceOf(ParametroNoEncontradoException.class).hasMessage("Parametro VALIDAR_LIQUIDACION_OMNIBUS en modulo FONDOMUTUO no encontrado");
    }

    private static Throwable catchThrowable(org.assertj.core.api.ThrowableAssert.ThrowingCallable c) {
        return org.assertj.core.api.Assertions.catchThrowable(c);
    }

    @Test
    @DisplayName("FM-LIQ-01: the facade keeps no state between calls: the same request twice gives the same entry to Tesoreria (the sequences move on: 100 then 101) and results of the second call do not include the first")
    void sinEstadoEntreLlamadas() throws Exception {
        Mundo m = new Mundo().tesoreriaGenera(0);
        m.respuestas.porMovimientos.add(sol());
        ApiT6b.PorLiquidacion puerto = m.puerto();
        Object a = puerto.logica(entrada(1L, List.of(1L), filtro()));
        Object b = puerto.logica(entrada(1L, List.of(1L), filtro()));
        assertThat(lista(a, "P_OUT_RESULTADO")).hasSize(1);
        assertThat(lista(b, "P_OUT_RESULTADO")).hasSize(1);
        assertThat(m.entradasTesoreria.get(0).p_IngresoEgresoGenerar.get(0).IdComprobante).isEqualTo(100L);
        assertThat(m.entradasTesoreria.get(1).p_IngresoEgresoGenerar.get(0).IdComprobante).isEqualTo(101L);
    }
}
