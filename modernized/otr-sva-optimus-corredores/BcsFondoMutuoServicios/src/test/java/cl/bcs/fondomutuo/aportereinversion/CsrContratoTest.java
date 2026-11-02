package cl.bcs.fondomutuo.aportereinversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;

/**
 * The contract of the class as the callers see it (FM-REI-41; legacy C:101-198 of CrearSolicitudAporteReinversion): Certificar ({@code crearAporteReinversion}), the Manejador and
 * ProcesarCargaSolicitudes build the legacy input classes and read the legacy output classes IN PROCESS. The nested classes of the new {@code AporteReinversionContrato} must keep
 * every public field name, type, order and default (the Gson of the legacy reads and writes them by name and in field order), the port must keep the legacy method, and the class mutates
 * its input exactly as the legacy did (the callers read the input again after the call). Reflection only: the production classes do not exist yet (red before code).
 */
class CsrContratoTest {

    private static final Map<String, String> ENTRADA = new LinkedHashMap<>();
    private static final Map<String, String> IN_APORTE = new LinkedHashMap<>();

    static {
        String[] e = { "p_IdEmpresa:Long", "p_IdUsuario:Long", "p_IdModulo:String", "p_SimularOperacion:String", "p_UsaReglasNegocio:String", "p_IdTipoCanalIngreso:String",
                "p_SolicitudesAporte:List" };
        for (String x : e) ENTRADA.put(x.split(":")[0], x.split(":")[1]);
        String[] a = { "IdInterno:Long", "IdCliente:Long", "IdPortafolio:Long", "IdSerieFondoOrigen:String", "IdEmisorOrigen:String", "CostoUF:BigDecimal", "FechaOrigenInversion:String",
                "MontoOrigen:BigDecimal", "IndicadorMLT:String", "IdSerieFondo:String", "Monto:BigDecimal", "NumeroCuotas:BigDecimal", "IdMoneda:String", "IdMedioDePago:String", "Visado:String",
                "SolicitudBatch:String", "OrigenOperacion:String", "IdRegimenTributario:String", "Glosa:String", "IdAutorizadoOrigen:Long", "FolioContrato:Long",
                "IdSolicitudAporteRescateRelacionado:Long", "FechaEmision:String", "Porcentaje:BigDecimal", "IdAgrupador:Long", "FechaCertificacionRescateOrigen:String",
                "FechaLiquidacionRescateOrigen:String", "FechaValorCuotaRescateOrigen:String", "FechaCertificaSpotCompra:String", "FechaLiquidaSpotCompra:String", "Switch:String",
                "GeneraSpot:String", "MontoPatrimonio:BigDecimal", "Canal:String", "IngresoAutomatico:String", "MontoTotalInversion:BigDecimal", "FechaValorCuota:Date", "TipoReinversion:String" };
        for (String x : a) IN_APORTE.put(x.split(":")[0], x.split(":")[1]);
    }

    private static List<Field> publicos(Class<?> c) {
        List<Field> r = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (Modifier.isPublic(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())) r.add(f);
        }
        return r;
    }

    private static void igualAlLegacy(Class<?> c, Map<String, String> esperado) {
        List<String> reales = new ArrayList<>();
        for (Field f : publicos(c)) {
            String tipo = f.getType().getSimpleName();
            reales.add(f.getName() + ":" + tipo);
        }
        List<String> legacy = new ArrayList<>();
        esperado.forEach((k, v) -> legacy.add(k + ":" + v));
        assertThat(reales).as("public fields of " + c.getSimpleName() + " (names, Java types and ORDER of the legacy)").containsExactlyElementsOf(legacy);
    }

    private static Object campo(Object o, String nombre) throws Exception {
        return o.getClass().getField(nombre).get(o);
    }

    @Test
    @DisplayName("FM-REI-41: the input classes Entrada and InSolicitudesAporte have the 7 + 38 public fields of the legacy, same names, types and order")
    void camposDeLaEntrada() {
        igualAlLegacy(ApiCsr.cargar(ApiCsr.ENTRADA), ENTRADA);
        igualAlLegacy(ApiCsr.cargar(ApiCsr.IN_APORTE), IN_APORTE);
    }

    @Test
    @DisplayName("FM-REI-41: the defaults of the legacy constructors: Entrada has an empty list of aportes; InSolicitudesAporte: Switch N, IdAgrupador 0, Porcentaje 100, GeneraSpot N, MontoPatrimonio 0, IngresoAutomatico N, MontoTotalInversion 0, the three rescue dates 0001-01-01, CostoUF 0, TipoReinversion empty")
    void defectosDeLaEntrada() throws Exception {
        Object e = ApiCsr.cargar(ApiCsr.ENTRADA).getDeclaredConstructor().newInstance();
        assertThat((List<?>) campo(e, "p_SolicitudesAporte")).isNotNull().isEmpty();
        assertThat(campo(e, "p_IdEmpresa")).isNull();
        Object a = ApiCsr.cargar(ApiCsr.IN_APORTE).getDeclaredConstructor().newInstance();
        assertThat(campo(a, "Switch")).isEqualTo("N");
        assertThat(campo(a, "IdAgrupador")).isEqualTo(0L);
        assertThat((BigDecimal) campo(a, "Porcentaje")).isEqualByComparingTo("100");
        assertThat(campo(a, "GeneraSpot")).isEqualTo("N");
        assertThat((BigDecimal) campo(a, "MontoPatrimonio")).isEqualByComparingTo("0");
        assertThat(campo(a, "IngresoAutomatico")).isEqualTo("N");
        assertThat((BigDecimal) campo(a, "MontoTotalInversion")).isEqualByComparingTo("0");
        assertThat(campo(a, "FechaCertificacionRescateOrigen")).isEqualTo("0001-01-01");
        assertThat(campo(a, "FechaLiquidacionRescateOrigen")).isEqualTo("0001-01-01");
        assertThat(campo(a, "FechaValorCuotaRescateOrigen")).isEqualTo("0001-01-01");
        assertThat((BigDecimal) campo(a, "CostoUF")).isEqualByComparingTo("0");
        assertThat(campo(a, "TipoReinversion")).isEqualTo("");
        assertThat(campo(a, "IdInterno")).isNull();
        assertThat(campo(a, "FolioContrato")).isNull();
        assertThat(campo(a, "FechaValorCuota")).isNull();
    }

    @Test
    @DisplayName("FM-REI-41: the output class Salida: P_OUT_ERRORESCONTADOR 0, empty P_OUT_MENSAJES (kit MensajeSalida) and P_OUT_RESULTADO (ResultadoSolicitud of the module), in that order; ConsultaRegimenReinversion keeps its two fields")
    void salidaYConsultaDeRegimen() throws Exception {
        Class<?> s = ApiCsr.cargar(ApiCsr.SALIDA);
        assertThat(publicos(s).stream().map(Field::getName).toList()).containsExactly("P_OUT_ERRORESCONTADOR", "P_OUT_MENSAJES", "P_OUT_RESULTADO");
        Object o = s.getDeclaredConstructor().newInstance();
        assertThat(campo(o, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat((List<?>) campo(o, "P_OUT_MENSAJES")).isEmpty();
        assertThat((List<?>) campo(o, "P_OUT_RESULTADO")).isEmpty();
        Class<?> m = (Class<?>) ((ParameterizedType) s.getField("P_OUT_MENSAJES").getGenericType()).getActualTypeArguments()[0];
        assertThat(m).isEqualTo(cl.bcs.fondomutuo.kit.MensajeSalida.class);
        Class<?> r = (Class<?>) ((ParameterizedType) s.getField("P_OUT_RESULTADO").getGenericType()).getActualTypeArguments()[0];
        assertThat(r).isEqualTo(cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud.class);
        Class<?> c = ApiCsr.cargar(ApiCsr.CONSULTA_REGIMEN);
        assertThat(publicos(c).stream().map(f -> f.getName() + ":" + f.getType().getSimpleName()).toList()).containsExactly("IdSolicitudAporteRescate:Long", "IdRegimenTributario:String");
    }

    @Test
    @DisplayName("FM-REI-41: the port is the legacy logica(entrada) with the same typed input and output and `throws Exception`; the factory builds it from the ports")
    void elPuerto() throws Exception {
        Class<?> p = ApiCsr.cargar(ApiCsr.PUERTO);
        assertThat(p.isInterface()).isTrue();
        var m = p.getMethod("logica", ApiCsr.cargar(ApiCsr.ENTRADA));
        assertThat(m.getReturnType()).isEqualTo(ApiCsr.cargar(ApiCsr.SALIDA));
        assertThat(m.getExceptionTypes()).contains(Exception.class);
        assertThat(p.getMethods()).as("one method: the legacy logica").hasSize(1);
    }

    @Test
    @DisplayName("FM-REI-41/43/47 (in process, as Certificar and the Manejador call): the class MUTATES its input: TipoReinversion of every aporte is set (SWITCH / INTERNACIONAL / NACIONAL) and p_SolicitudesAporte keeps the elements; the output is the typed Salida")
    void muta_laEntradaComoElLegacy() throws Exception {
        List<Object> capturada = new ArrayList<>();
        JsonObject r = CasoCsr.ejecutar(CorridaCsr.idDe("lote-mixto-de-tipos"), Perfil.ABIERTAS, entrada -> capturada.add(entrada));
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
        assertThat(capturada).hasSize(1);
        List<?> aportes = (List<?>) campo(capturada.get(0), "p_SolicitudesAporte");
        assertThat(aportes).hasSize(3);
        assertThat(campo(aportes.get(0), "TipoReinversion")).isEqualTo("SWITCH");
        assertThat(campo(aportes.get(1), "TipoReinversion")).isEqualTo("INTERNACIONAL");
        assertThat(campo(aportes.get(2), "TipoReinversion")).isEqualTo("NACIONAL");
    }

    @Test
    @DisplayName("FM-REI-43, D-F2-19 (in process): with more than 2.000 aportes the legacy leaves in p_SolicitudesAporte ONLY the sublist of the last lot processed (the input list is replaced per lot)")
    void muta_laListaPorLotes() throws Exception {
        List<Object> capturada = new ArrayList<>();
        JsonObject r = CasoCsr.ejecutar(CorridaCsr.idDe("lote-2001-aportes-dos-lotes"), Perfil.ABIERTAS, capturada::add);
        assertThat(r.get("decisionServlet").getAsString()).isEqualTo("COMMIT");
        assertThat((List<?>) campo(capturada.get(0), "p_SolicitudesAporte")).as("the lot 2 (one aporte)").hasSize(1);
    }

    @Test
    @DisplayName("FM-CER crearAporteReinversion (in process): the typed answer carries what Certificar reads: P_OUT_ERRORESCONTADOR, the messages with the aporte id, and per result IdInterno, IdSolicitudAporteRescate, IdTipoCanalIngreso and a non null Switch")
    void loQueCertificarLee() throws Exception {
        JsonObject r = CasoCsr.ejecutar(CorridaCsr.idDe("certificar-aporte-switch"), Perfil.ABIERTAS, e -> { });
        JsonObject res = r.getAsJsonObject("salida").getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject();
        for (String k : List.of("IdInterno", "IdSolicitudAporteRescate", "IdTipoCanalIngreso", "Switch")) assertThat(res.has(k)).as(k).isTrue();
        assertThat(res.get("Switch").getAsString()).isEqualTo("S");
        JsonObject rechazo = CasoCsr.ejecutar(CorridaCsr.idDe("certificar-lote-con-un-rechazo"), Perfil.ABIERTAS, e -> { });
        assertThat(rechazo.getAsJsonObject("salida").get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1);
        assertThat(rechazo.getAsJsonObject("salida").getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("id").getAsLong()).isEqualTo(2);
    }
}
