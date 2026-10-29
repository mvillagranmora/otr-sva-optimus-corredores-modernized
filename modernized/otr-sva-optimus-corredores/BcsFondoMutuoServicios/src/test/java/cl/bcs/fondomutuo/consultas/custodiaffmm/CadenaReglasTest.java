package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.custodia.CasoCpf;

/**
 * What the CHAIN adds (RULES_FONDOMUTUO_F3_C.md FM-CPFF-21 / FM-CPFF-22), stated with LITERAL values (the ones of the legacy CPF over the legacy FFMM, both real: they equal the goldens of
 * {@link CadenaEquivalenciaTest}) and WHY the goldens of the CPF front (which script the custody calculation) stay valid for what they pinned.
 */
class CadenaReglasTest {

    private static JsonObject dump(String slug) throws Exception {
        return CasoCadena.ejecutar(CasoCadena.idPorSlug(slug));
    }

    private static JsonArray filas(JsonObject d) {
        assertFalse(d.has("excepcion"), "raised " + d.get("excepcion"));
        return d.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL");
    }

    private static JsonObject fila(String slug) throws Exception {
        JsonArray f = filas(dump(slug));
        assertEquals(1, f.size(), slug + " rows");
        return f.get(0).getAsJsonObject();
    }

    private static String v(JsonObject fila, String k) {
        JsonElement e = fila.get(k);
        return e == null ? "<absent>" : e.isJsonNull() ? "<null>" : e.getAsString();
    }

    private static String campos(JsonObject fila, String... ks) {
        List<String> l = new ArrayList<>();
        for (String k : ks) l.add(v(fila, k));
        return String.join("|", l);
    }

    private static List<String> colaboradores(JsonObject d) {
        List<String> l = new ArrayList<>();
        for (JsonElement c : d.getAsJsonArray("colaboradores")) l.add(c.getAsJsonObject().get("colaborador").getAsString());
        return l;
    }

    private static String excepcion(String slug) throws Exception {
        JsonObject d = dump(slug);
        assertTrue(d.has("excepcion"), slug + " did not raise");
        assertEquals("ROLLBACK_HTTP500", d.get("decisionServlet").getAsString());
        return d.get("excepcion").getAsString();
    }

    // ---- FM-CPFF-22.1
    @Test
    @DisplayName("FM-CPFF-22.1 C23: la solicitud en transito vuelve DENTRO de la custodia (cantidad 0, bodega ''): el saldo de CPF TIENE custodia: IdBoveda '' (no 'COR'), PPPCustodia 0 (el nulo de la FFMM se vuelve 0), aporte en transito 10.0000")
    void fmCpff22_1_laSolicitudVieneDentroDeLaCustodia() throws Exception {
        JsonObject d = dump("solicitud-sin-cartera-22-1");
        JsonObject f = filas(d).get(0).getAsJsonObject();
        assertEquals("FM001A||0|10.0000|0|Regimen General", campos(f, "IdSerieFondo", "IdBoveda", "CantidadCustodia", "CantidadTransSolicitudesAporte", "PPPCustodia", "RegimenTributario"));
        // ConsultarEmpresa twice: the first one is FFMM's (inside the custody call), the second one CPF's (D-F3A-07)
        assertEquals(List.of("EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "EntregarPreciosTasas", "CalcularParidad", "ConsultarEmpresa", "CalcularParidad"), colaboradores(d));
    }

    @Test
    @DisplayName("FM-CPFF-22.1 D-F3C-06: cuando la FFMM NO devuelve la fila de la solicitud el saldo no tiene custodia y CPF le pone la bodega 'COR': solicitud aceptada despues de la fecha, cartera anulada que la excluye, fecha historica con emisor nulo; con emisor informado vuelve ''")
    void fmCpff22_1_sinFilaDeCustodiaLaBodegaEsCor() throws Exception {
        assertEquals("COR", v(fila("solicitud-aceptada-despues"), "IdBoveda"));
        assertEquals("COR", v(fila("solicitud-cartera-anulada"), "IdBoveda"));
        assertEquals("COR", v(fila("fecha-historica-con-solicitud"), "IdBoveda"));
        assertEquals("", v(fila("fecha-historica-con-emisor"), "IdBoveda"));
        assertEquals("", v(fila("solicitud-con-cartera-vigente"), "IdBoveda"));
        assertEquals("100.00000000|10.0000|9800.00000000", campos(fila("solicitud-con-cartera-vigente"), "CantidadCustodia", "CantidadTransSolicitudesAporte", "PPPCustodia"));
    }

    @Test
    @DisplayName("FM-CPFF-22.1 D-F3C-06: con p_IdInstrumento 901 CPF solo trae la solicitud de FM002B pero la FFMM devuelve TAMBIEN la de FM001A (la rama de solicitudes no aplica el instrumento): aparece un saldo de FM001A con todo en 0 que CPF no pidio")
    void dF3c06_laFfmmAgregaSaldosQueCpfNoPidio() throws Exception {
        JsonArray f = filas(dump("filtro-instrumento-solicitud-ajena"));
        assertEquals(2, f.size());
        assertEquals("FM001A|0|0", campos(f.get(0).getAsJsonObject(), "IdSerieFondo", "CantidadCustodia", "CantidadTransSolicitudesAporte"));
        assertEquals("FM002B|0|10.00", campos(f.get(1).getAsJsonObject(), "IdSerieFondo", "CantidadCustodia", "CantidadTransSolicitudesAporte"));
        assertEquals(2, filas(dump("filtro-emisor-solicitud-ajena")).size());
        assertEquals(2, filas(dump("filtro-familia-solicitud-ajena")).size());
    }

    // ---- FM-CPFF-22.2 / 22.3
    @Test
    @DisplayName("FM-CPFF-22.2 D-F3C-01 D-F3A-03: custodia 100 + garantia 10: PPPCustodia 0E+8 (CPF lo copia), CantidadGarantia 10.00000000 y MontoGarantia 0 (siempre 0 en CPF); cantidad cero: el PPP nulo de la FFMM sale 0")
    void fmCpff22_2_pppConGarantiaYNulo() throws Exception {
        assertEquals("100.00000000|10.00000000|0E+8|0", campos(fila("custodia-mas-garantia"), "CantidadCustodia", "CantidadGarantia", "PPPCustodia", "MontoGarantia"));
        assertEquals("0E-8|0", campos(fila("posicion-cantidad-cero"), "CantidadCustodia", "PPPCustodia"));
        assertEquals("0|25.00000000|0E+8", campos(fila("solo-garantia"), "CantidadCustodia", "CantidadGarantia", "PPPCustodia"));
        assertEquals("100.00000000|0E+8", campos(fila("fecha-historica-garantia"), "CantidadCustodia", "PPPCustodia"));
    }

    @Test
    @DisplayName("FM-CPFF-22.3: CPF pide p_AgrupaPorBoveda N y p_AgrupaPorRelacion N: la bodega real (DCV, COR) y la relacion (CPRO del cliente 99) NUNCA llegan a CPF: IdBoveda '' y Relacion '' siempre")
    void fmCpff22_3_bovedaYRelacionSiempreVacias() throws Exception {
        assertEquals("|", campos(fila("boveda-dcv-sale-vacia"), "IdBoveda", "Relacion"));
        assertEquals("|", campos(fila("dos-bodegas-se-suman"), "IdBoveda", "Relacion"));
        assertEquals("99|504||", campos(fila("cliente-corredora-relacion-vacia"), "IdCliente", "IdPortafolio", "IdBoveda", "Relacion"));
        assertEquals("100.00000000", v(fila("dos-bodegas-se-suman"), "CantidadCustodia"));
    }

    // ---- FM-CPFF-22.4
    @Test
    @DisplayName("FM-CPFF-22.4 D-F3C-13: dos / tres grupos de la FFMM con la MISMA clave de CPF (solo cambia Nemotecnico / IdEmisor): CPF se queda con UNO, el ultimo en el orden de iteracion del HashMap de la FFMM (la insercion no lo decide): 100 en 054 y 055, 25 con tres grupos")
    void dF3c13_laUltimaCustodiaGana() throws Exception {
        assertEquals("100.00000000", v(fila("desempate-nemotecnico"), "CantidadCustodia"));
        assertEquals("100.00000000", v(fila("desempate-nemotecnico-invertido"), "CantidadCustodia"));
        assertEquals("100.00000000", v(fila("desempate-emisor"), "CantidadCustodia"));
        assertEquals("25.00000000|39200", campos(fila("desempate-tres-grupos"), "CantidadCustodia", "INGPrecioPromedioCompra"));
    }

    @Test
    @DisplayName("FM-CPFF-22.4 D-F3C-08 D-F3C-13: un grupo de SOLO garantia que comparte la clave de CPF con una custodia se PIERDE (gana el grupo de custodia: CantidadGarantia 0 aunque haya 5 en garantia)")
    void dF3c13_laGarantiaDeUnGrupoPerdedorSePierde() throws Exception {
        JsonObject f = fila("desempate-garantia-y-custodia");
        assertEquals("100.00000000|0|Regimen General", campos(f, "CantidadCustodia", "CantidadGarantia", "RegimenTributario"));
    }

    // ---- FM-CPFF-22.5
    @Test
    @DisplayName("FM-CPFF-22.5 C02: CPF recibe '0001-01-01' o nulo y pasa a la FFMM la fecha del sistema: responde la posicion (la llamada directa con '0001-01-01' da vacio); una fecha anterior al sistema usa el historico: Producto nulo y RegimenTributario '' (D-F3C-07)")
    void fmCpff22_5_laFechaQueCpfPasa() throws Exception {
        assertEquals("100.00000000|Regimen General", campos(fila("fecha-por-defecto-cpf-la-reemplaza"), "CantidadCustodia", "RegimenTributario"));
        assertEquals("100.00000000|Regimen General", campos(fila("fecha-nula-cpf-la-reemplaza"), "CantidadCustodia", "RegimenTributario"));
        assertEquals("501|100.00000000|<null>|", campos(fila("fecha-historica"), "IdPortafolio", "CantidadCustodia", "Producto", "RegimenTributario"));
        assertEquals("40.00000000", v(fila("fecha-historica-ultima-version"), "CantidadCustodia"));
        assertEquals("FM002B", v(fila("fecha-historica-tres-meses-antes"), "IdSerieFondo"));
        assertEquals(0, filas(dump("fecha-historica-sin-respaldo")).size());
        assertEquals("FM002B", v(fila("egreso-igual-a-la-fecha"), "IdSerieFondo"));
    }

    // ---- FM-CPFF-22.6
    @Test
    @DisplayName("FM-CPFF-22.6: los errores de la FFMM suben por CPF SIN captura: 'Empresa no existe' (ConsultarEmpresa llamada una sola vez, por la FFMM), fecha mal formada, NullPointerException de la suma, 'key field is not unique' de SelectSeries; sin PLA.FechaSistema CPF falla antes de llamar a nadie")
    void fmCpff22_6_loErroresSuben() throws Exception {
        assertEquals("Exception: Empresa no existe", excepcion("empresa-inexistente-en-ffmm"));
        assertEquals(List.of("ConsultarEmpresa"), colaboradores(dump("empresa-inexistente-en-ffmm")));
        assertTrue(excepcion("fecha-texto-libre").startsWith("DateTimeParseException: Text 'hoy' could not be parsed"));
        assertTrue(excepcion("fecha-vacia").startsWith("DateTimeParseException: Text '' could not be parsed"));
        assertTrue(excepcion("valor-compra-nulo-npe").startsWith("NullPointerException:"));
        assertEquals("Exception: key field is not unique", excepcion("fan-out-dos-series-duplica"));
        assertEquals("Exception: Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado", excepcion("sin-parametro-fecha-sistema"));
        assertEquals(List.of(), colaboradores(dump("sin-parametro-fecha-sistema")));
    }

    @Test
    @DisplayName("FM-CPFF-17 FM-CPFF-22: en la cadena la FFMM es REAL: ningun caso registra una llamada al colaborador 'ConsultarCustodiaPortafolioFFMM' (en el frente CPF era un doble) y toda llamada a ConsultarEmpresa viene en parejas FFMM / CPF")
    void cadena_noHayDobleDeLaCustodia() throws Exception {
        int conEmpresa = 0;
        for (String id : CasoCadena.casos()) {
            List<String> c = colaboradores(CasoCadena.golden(id));
            assertFalse(c.contains("ConsultarCustodiaPortafolioFFMM"), id);
            long empresas = c.stream().filter("ConsultarEmpresa"::equals).count();
            if (empresas > 0) conEmpresa++;
            assertTrue(empresas == 0 || empresas == 1 || empresas == 2, id + ": " + c);
        }
        assertTrue(conEmpresa > 40);
    }

    // ---- why the goldens of the CPF front stay valid
    /** The request CPF sends to the custody port, captured in the chain (the new CPF, before the real adapter answers). */
    private static JsonObject peticion(String slug) throws Exception {
        List<JsonObject> vistas = new ArrayList<>();
        SujetoCadena.espia = vistas::add;
        try {
            dump(slug);
        } finally {
            SujetoCadena.espia = null;
        }
        assertEquals(1, vistas.size(), slug + ": CPF calls the custody port ONCE");
        return vistas.get(0);
    }

    @Test
    @DisplayName("Validez de los goldens de CPF (doble de la FFMM): lo que fijan es la PETICION a la custodia y lo que CPF HACE con cualquier respuesta del puerto; la peticion de la cadena es IDENTICA a la que esos goldens registraron (14 campos, N / N / LIQ, fecha real)")
    void goldensDeCpf_laPeticionEsLaMisma() throws Exception {
        JsonObject enCadena = peticion("custodia-sola");
        JsonObject enCpf = CasoCpf.golden(CasoCpf.idPorSlug("custodia-sola")).getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals("ConsultarCustodiaPortafolioFFMM", CasoCpf.golden(CasoCpf.idPorSlug("custodia-sola")).getAsJsonArray("colaboradores").get(0).getAsJsonObject().get("colaborador").getAsString());
        assertEquals(14, enCadena.size());
        assertEquals(FfmmEquivalenciaTest.texto(enCpf), FfmmEquivalenciaTest.texto(enCadena));
        assertEquals("", enCadena.get("p_IdBoveda").getAsString());
        assertEquals("N|N|LIQ", enCadena.get("p_AgrupaPorBoveda").getAsString() + "|" + enCadena.get("p_AgrupaPorRelacion").getAsString() + "|" + enCadena.get("p_TipoFechaConsulta").getAsString());
        assertEquals("2026-03-02", peticion("fecha-por-defecto-cpf-la-reemplaza").get("p_FechaConsulta").getAsString());
        assertEquals("2026-03-02", peticion("fecha-nula-cpf-la-reemplaza").get("p_FechaConsulta").getAsString());
        assertEquals("2026-03-01", peticion("fecha-historica").get("p_FechaConsulta").getAsString());
    }

    @Test
    @DisplayName("Validez de los goldens de CPF: TODAS las peticiones que registraron (cada caso que llega a la custodia) son N / N / LIQ, bodega '' y fecha real (nunca '0001-01-01' ni nula): la FFMM real nunca ve la fecha por defecto desde CPF (D-F3C-04 no se ejerce)")
    void goldensDeCpf_todasLasPeticionesSonNNLiq() {
        int conPeticion = 0;
        for (String id : CasoCpf.casos()) {
            for (JsonElement c : CasoCpf.golden(id).getAsJsonArray("colaboradores")) {
                JsonObject o = c.getAsJsonObject();
                if (!"ConsultarCustodiaPortafolioFFMM".equals(o.get("colaborador").getAsString())) continue;
                conPeticion++;
                JsonObject p = o.getAsJsonArray("argumentos").get(0).getAsJsonObject();
                assertEquals(14, p.size(), id);
                assertEquals("||N|N|LIQ", p.get("p_IdBoveda").getAsString() + "||" + p.get("p_AgrupaPorBoveda").getAsString() + "|" + p.get("p_AgrupaPorRelacion").getAsString() + "|" + p.get("p_TipoFechaConsulta").getAsString(), id);
                assertFalse(p.get("p_FechaConsulta").isJsonNull(), id);
                assertFalse("0001-01-01".equals(p.get("p_FechaConsulta").getAsString()), id);
            }
        }
        assertTrue(conPeticion >= 100, "CPF goldens that reach the custody port: " + conPeticion);
    }

    @Test
    @DisplayName("Validez de los goldens de CPF: lo que esos goldens GUIONAN como respuesta de la custodia y la FFMM real NO puede producir (bodega distinta de '' en 108 de 172, relacion distinta de '' en 2, un grupo duplicado en 1) no invalida ningun golden de CPF: CPF solo copia lo que el puerto le da; la cadena cubre lo que la FFMM real SI produce")
    void goldensDeCpf_respuestasQueLaFfmmRealNoProduce() {
        int conBoveda = 0, conRelacion = 0, conDuplicado = 0, conFilas = 0;
        for (String id : CasoCpf.casos()) {
            JsonObject colaboradores = CasoCpf.caso(id).getAsJsonObject("colaboradores");
            if (!colaboradores.has("ConsultarCustodiaPortafolioFFMM")) continue;
            JsonObject c = colaboradores.getAsJsonObject("ConsultarCustodiaPortafolioFFMM");
            List<JsonArray> respuestas = new ArrayList<>();   // every scripted answer: the default one and each conditional one
            if (c.has("defecto") && c.get("defecto").isJsonObject() && c.getAsJsonObject("defecto").has("p_out_tbl") && c.getAsJsonObject("defecto").get("p_out_tbl").isJsonArray()) respuestas.add(c.getAsJsonObject("defecto").getAsJsonArray("p_out_tbl"));
            if (c.has("respuestas")) {
                for (JsonElement e : c.getAsJsonArray("respuestas")) {
                    JsonObject r = e.getAsJsonObject();
                    if (r.has("respuesta") && r.get("respuesta").isJsonObject() && r.getAsJsonObject("respuesta").has("p_out_tbl") && r.getAsJsonObject("respuesta").get("p_out_tbl").isJsonArray()) respuestas.add(r.getAsJsonObject("respuesta").getAsJsonArray("p_out_tbl"));
                }
            }
            boolean boveda = false, relacion = false, duplicado = false, hayFilas = false;
            for (JsonArray filas : respuestas) {
                List<String> claves = new ArrayList<>();
                for (JsonElement f : filas) {
                    hayFilas = true;
                    JsonObject o = f.getAsJsonObject();
                    if (!(o.get("IdBoveda").isJsonNull() || o.get("IdBoveda").getAsString().isEmpty())) boveda = true;
                    if (!(o.get("Relacion").isJsonNull() || o.get("Relacion").getAsString().isEmpty())) relacion = true;
                    String k = String.join("_", v(o, "IdInstrumento"), v(o, "Nemotecnico"), v(o, "IdEmisor"), v(o, "IdCliente"), v(o, "IdPortafolio"), v(o, "IdRegimenFondoMutuo"));
                    if (claves.contains(k)) duplicado = true;
                    claves.add(k);
                    assertEquals(29, o.size(), id + ": the scripted custody row is a SalidaCustodia of 29 fields");
                }
            }
            if (!hayFilas) continue;
            conFilas++;
            if (boveda) conBoveda++;
            if (relacion) conRelacion++;
            if (duplicado) conDuplicado++;
        }
        assertEquals("108|2|1", conBoveda + "|" + conRelacion + "|" + conDuplicado, "cases with a scripted custody answer: " + conFilas);
    }
}
