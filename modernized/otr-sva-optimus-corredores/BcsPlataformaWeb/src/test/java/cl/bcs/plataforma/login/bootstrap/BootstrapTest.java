package cl.bcs.plataforma.login.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.adapter.in.BootstrapController;
import cl.bcs.plataforma.login.PrefijoPlaFilter;
import cl.bcs.plataforma.login.UnidadDeTrabajo;
import cl.bcs.plataforma.login.h2.LoginDb;
import cl.bcs.plataforma.login.h2.ProcedimientosGuionados;
import cl.bcs.plataforma.login.sesion.FiltroSesion;
import cl.bcs.plataforma.login.sesion.SesionDatos;

/**
 * Bootstrap as JSON on H2 (T-20, T-21, T-21b) through the controller and the filters the application registers. The contract: the keys of the answer
 * are the names of the constants the legacy {@code BootstrapServlet} wrote into the page ({@code BCS_*}, {@code bcs_*}), with the shapes of the legacy
 * values; the expectations below were derived line by line from {@code BootstrapServlet.java:57-626} and {@code Sesion.getSesionData}.
 */
class BootstrapTest {

    private static final String TOKEN = "TOKEN-DE-PRUEBA";

    private LoginDb db;
    private ProcedimientosGuionados sp;
    private MockMvc mvc;

    @BeforeAll
    static void utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        org.h2.util.DateTimeUtils.resetCalendar();
    }

    @BeforeEach
    void montar() throws Exception {
        db = LoginDb.nueva();
        sp = new ProcedimientosGuionados();
        sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", (c, in) -> ProcedimientosGuionados.salida("P_OUT_FECHAHORASISTEMALOCAL", "2026-03-02 06:30:00"));
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", (c, in) -> ProcedimientosGuionados.salida("P_OUT_ESADMIN", "N"));
        sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion", (c, in) -> ProcedimientosGuionados.salida("P_OUT_VALOR", "N"));
        sp.responder("BSCL.Plataforma.BD.Servicios::ConsultarFormatos", (c, in) -> {
            JsonObject o = new JsonObject();
            JsonArray filas = new JsonArray();
            filas.add(formato("USD1", "CLP", "ACC", "COMCN", 2, 4));
            filas.add(formato("USD1", "CLP", "ACC", "VENCN", 3, 5));
            JsonObject sinUso = formato("X", "CLP", "ACC", "COMCN", 9, 9);
            sinUso.remove("IdUsoMonedaFormato");
            filas.add(sinUso);
            o.add("P_OUT_MONEDAFORMATO", filas);
            return o;
        });
        UnidadDeTrabajo unidad = new UnidadDeTrabajo(db.dataSource());
        BootstrapController controlador = new BootstrapController(unidad, new BootstrapServicio(new SesionDatos(), sp));
        mvc = MockMvcBuilders.standaloneSetup(controlador).addFilters(new PrefijoPlaFilter(), new FiltroSesion(db.dataSource())).build();

        db.sql("INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Ambiente', 'DES')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.SegundosZonaHoraria', '-10800')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MuestraSSOGoogle', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.LocalizacionSistema', 'CL')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Version', '3.2')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.ZonaHoraria', 'America/Santiago')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MonedaSistema', 'CLP')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.BolsaDefectoSistema', 'BCS')",
                "INSERT INTO \"BSCL.APV.BD::PLA.ConfiguracionAPV\" VALUES (1, 'APV.Habilitado', 'S')",
                "INSERT INTO \"BSCL.APV.BD::PLA.ConfiguracionAPV\" VALUES (1, 'APV.Secreto', 'no-sale')",
                "INSERT INTO \"BSCL.Plataforma.BD::PRO.Configuracion\" VALUES (1, 'PRODUCTO.Cualquiera', 'si-sale')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('HOLA', 'CL', ' Hola ', ' H ')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('CHAO', 'ES', 'Bye', NULL)",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" VALUES (1, 'Corredora Ficticia S.A.', 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" VALUES (2, 'Inactiva', 'BLO')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" VALUES ('LOGO_CORREDOR_LOGIN', X'0102FF')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" VALUES ('OTRA', X'00')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (1, 'FERIADO', DATE '2026-03-04', NULL, 'FERIADO FICTICIO', 2026, 44, 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (2, 'INHABIL', DATE '0001-01-01', 5, 'SABADO', 2026, 44, 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (3, 'FERIADO', DATE '2026-05-01', NULL, 'INACTIVO', 2026, 44, 'BLO')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento\" VALUES ('ACC', 'COMCN')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.TipoOperacion\" VALUES ('COMCN', 'COMPRA', 'RV', 'N', 'N', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario\" VALUES (1, 1, 'tema', 'oscuro')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla\" VALUES (1, 'filas', '50')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz\" VALUES (1, 'ORD', 'COMBO', 'cmbMoneda', 'ACT', 'a=1,' || CHAR(9) || 'b=2' || CHAR(10))",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Empresa\" VALUES (1, 'Corredora Ficticia S.A.', 44, 100)",
                "INSERT INTO \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" VALUES (100, NULL, NULL, 777, 1)",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.Corredora\" VALUES (50, 777)",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.ValorDefectoSistema\" VALUES ('V1', 'Valor uno', 'x', 1, 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.ValorDefectoSistema\" VALUES ('V2', 'Valor dos', 'y', 1, 'BLO')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" VALUES (1, 1, 'mnuOrdenes', 'S', 'S', 'S', 0, 1, 10, 'Ordenes', 'MENU', 700)",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" VALUES (1, 1, 'mnuOculto', 'N', 'N', 'N', 0, 2, 11, 'Oculto', 'MENU', 701)",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" VALUES (1, 1, 'btnX', 'S', 'S', 'S', 10, 1, 12, 'X', 'BOTON', 702)",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" VALUES (1, 1, 'btnX', 'S', 'N', 'S', 10, 2, 13, 'X2', 'BOTON', 703)",
                // the user, an employee, and the session of the token
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\","
                        + "\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioCorredores\",\"CodigoMenu\") VALUES (1,1,'JPEREZ','X',DATE '0001-01-01','ACT',0,"
                        + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'S','menu_corredor')",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") VALUES ('" + TOKEN
                        + "',1,'S',TIMESTAMP '2026-03-02 08:00:00',TIMESTAMP '2026-03-02 08:00:00','COR')");
    }

    @AfterEach
    void cerrar() throws Exception {
        db.close();
    }

    private static JsonObject formato(String uso, String moneda, String tipoInstrumento, String tipoOperacion, int decimales, int trunca) {
        JsonObject f = new JsonObject();
        f.addProperty("IdUsoMonedaFormato", uso);
        f.addProperty("IdMoneda", moneda);
        f.addProperty("IdTipoInstrumento", tipoInstrumento);
        f.addProperty("IdTipoOperacion", tipoOperacion);
        for (String campo : List.of("Cantidad", "Precio", "Tasa", "Monto", "TipoCambio", "Paridad", "Puntos")) {
            f.addProperty(campo + "Decimales", String.valueOf(decimales));
            f.addProperty(campo + "TrucaRedondea", trunca == 4 ? "T" : "R");
        }
        return f;
    }

    private JsonObject home(String token) throws Exception {
        var r = mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", token)).andReturn().getResponse();
        return JsonParser.parseString(r.getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
    }

    // ------------------------------------------------------------------------------------------------ T-20 login

    @Test
    @DisplayName("T-20 T-21b GET /PLA/bootstrap/login needs no token and carries the constants of the legacy login page plus empresas and logo")
    void bootstrap_login() throws Exception {
        String cuerpo = mvc.perform(get("/PLA/bootstrap/login")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonObject j = JsonParser.parseString(cuerpo).getAsJsonObject();
        assertThat(j.getAsJsonObject("_contrato").toString()).isEqualTo("{\"nombre\":\"optimus-bootstrap\",\"version\":1}");
        assertThat(j.keySet()).containsExactlyInAnyOrder("_contrato", "CONF_IMAGEN_FONDO", "CONF_LOCALE", "BCS_DICCIONARIO", "BCS_SESION", "BCS_OFFSET", "BCS_VERSION", "BCS_ACCESOS",
                "BCS_PREFERENCIAS", "BCS_PREFERENCIAS_GRILLA", "BCS_AMBIENTE", "BCS_TIMESTAMP_LOCAL", "BCS_MUESTRA_SSO_GOOGLE", "bcs_feriados",
                "BCS_OPERACION_INSTRUMENTO", "BCS_ESTADO_MODULO", "BCS_CONFIGURACION", "BCS_OFFLINE", "BCS_USER_MENU", "empresas", "logo");
        assertThat(j.get("CONF_LOCALE").getAsString()).isEqualTo("CL");
        // dictionary of language CL only, trimmed, with the abbreviation under Codigo_ABR (null when absent)
        assertThat(j.getAsJsonObject("BCS_DICCIONARIO").get("HOLA").getAsString()).isEqualTo("Hola");
        assertThat(j.getAsJsonObject("BCS_DICCIONARIO").get("HOLA_ABR").getAsString()).isEqualTo("H");
        assertThat(j.getAsJsonObject("BCS_DICCIONARIO").has("CHAO")).isFalse();
        assertThat(j.getAsJsonObject("BCS_DICCIONARIO").get("CONTRASENA_INVALIDA_ABR").isJsonNull()).isTrue();
        assertThat(j.get("BCS_SESION").toString()).isEqualTo("{}");
        assertThat(j.get("BCS_OFFSET").getAsInt()).isEqualTo(-10800);
        assertThat(j.get("BCS_VERSION").getAsInt()).isZero();
        assertThat(j.get("BCS_AMBIENTE").getAsString()).isEqualTo("DES");
        assertThat(j.get("BCS_TIMESTAMP_LOCAL").getAsString()).isEmpty();
        assertThat(j.get("BCS_MUESTRA_SSO_GOOGLE").getAsString()).isEqualTo("S");
        // the legacy page wrote '" + value + "': with no configured value the constant is the TEXT null (not a JSON null)
        db.sql("DELETE FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.MuestraSSOGoogle'");
        JsonObject sinValor = JsonParser.parseString(mvc.perform(get("/PLA/bootstrap/login")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)).getAsJsonObject();
        assertThat(sinValor.get("BCS_MUESTRA_SSO_GOOGLE").isJsonPrimitive()).isTrue();
        assertThat(sinValor.get("BCS_MUESTRA_SSO_GOOGLE").getAsString()).isEqualTo("null");
        assertThat(j.get("BCS_USER_MENU").getAsString()).isEqualTo("BCS_FULLMENU");
        assertThat(j.get("BCS_OFFLINE").getAsBoolean()).isTrue();
        // only active companies; only the login logo, as base64
        assertThat(j.getAsJsonArray("empresas").toString()).isEqualTo("[{\"IdEmpresa\":1,\"Nombre\":\"Corredora Ficticia S.A.\"}]");
        assertThat(j.getAsJsonArray("logo").toString()).isEqualTo("[{\"NombreImagen\":\"LOGO_CORREDOR_LOGIN\",\"Binario\":\"AQL/\"}]");
        // nothing of the session is touched by the login page
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("0");
    }

    // ------------------------------------------------------------------------------------------------ T-21 home

    @Test
    @DisplayName("T-21 T-21b POST /PLA/bootstrap/home answers the session, accesses, preferences, holidays, formats, filters and public configuration under the BCS_* names")
    void bootstrap_home() throws Exception {
        JsonObject j = home(TOKEN);
        assertThat(j.keySet()).contains("_contrato", "CONF_LOCALE", "BCS_DICCIONARIO", "BCS_SESION", "BCS_OFFSET", "BCS_VERSION", "BCS_ACCESOS", "BCS_PREFERENCIAS",
                "BCS_PREFERENCIAS_GRILLA", "BCS_AMBIENTE", "BCS_TIMESTAMP_LOCAL", "bcs_feriados", "BCS_OPERACION_INSTRUMENTO", "BCS_ESTADO_MODULO", "BCS_CONFIGURACION",
                "BCS_OFFLINE", "bcs_formato_cantidad", "bcs_formato_precio", "bcs_formato_tasa", "bcs_formato_monto", "bcs_formato_tipoCambio", "bcs_formato_paridad",
                "bcs_formato_puntos", "bcs_filtrocombobase", "BCS_USER_MENU");
        assertThat(j.get("BCS_VERSION").getAsDouble()).isEqualTo(3.2);
        assertThat(j.get("BCS_TIMESTAMP_LOCAL").getAsString()).isEqualTo("2026-03-02 06:30:00");
        assertThat(j.get("BCS_USER_MENU").getAsString()).isEqualTo("MENU_CORREDOR");   // the code, upper case (T-21c)

        // BCS_SESION: the legacy SesionData, in its field order
        JsonObject s = j.getAsJsonObject("BCS_SESION");
        assertThat(String.join(",", s.keySet())).isEqualTo("IdEmpresa,IdTokenSesion,IdUsuario,NombreEmpresa,NombreUsuario,CodigoMenuUsuario,IdSocioDeNegocio,"
                + "IdPersonaCorredora,IdMonedaSistema,IdPaisSistema,FechaSistema,BolsaDefectoSistema,ValoresDefectoSistema,IdEmpleado,IdCliente,P_OUT_ERRORESCONTADOR,"
                + "P_OUT_MENSAJES,IdCorredoraSistema,IdEntidadSSO,SSORedirectURL");
        assertThat(s.get("IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(s.get("IdTokenSesion").isJsonNull()).isTrue();
        assertThat(s.get("IdUsuario").getAsLong()).isEqualTo(1L);
        assertThat(s.get("NombreEmpresa").getAsString()).isEqualTo("Corredora Ficticia S.A.");
        assertThat(s.get("NombreUsuario").getAsString()).isEqualTo("JPEREZ");
        assertThat(s.get("CodigoMenuUsuario").getAsString()).isEqualTo("menu_corredor");
        assertThat(s.get("IdSocioDeNegocio").getAsLong()).isEqualTo(100L);
        assertThat(s.get("IdPersonaCorredora").getAsLong()).isEqualTo(777L);
        assertThat(s.get("IdMonedaSistema").getAsString()).isEqualTo("CLP");
        assertThat(s.get("IdPaisSistema").getAsLong()).isEqualTo(44L);
        assertThat(s.get("FechaSistema").getAsString()).isEqualTo("2026-03-02");
        assertThat(s.get("BolsaDefectoSistema").getAsString()).isEqualTo("BCS");
        assertThat(s.getAsJsonArray("ValoresDefectoSistema").size()).isEqualTo(1);   // only the active default values
        assertThat(s.get("IdEmpleado").getAsLong()).isEqualTo(100L);
        assertThat(s.get("IdCliente").isJsonNull()).isTrue();
        assertThat(s.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(s.get("IdCorredoraSistema").getAsLong()).isEqualTo(50L);

        assertThat(j.getAsJsonObject("BCS_PREFERENCIAS").toString()).isEqualTo("{\"tema\":\"oscuro\"}");
        assertThat(j.getAsJsonObject("BCS_PREFERENCIAS_GRILLA").toString()).isEqualTo("{\"filas\":\"50\"}");
        // holidays: country -> date -> detail, only the active ones
        assertThat(j.getAsJsonObject("bcs_feriados").toString()).isEqualTo("{\"44\":{\"2026-03-04\":{\"Tipo\":\"FERIADO\",\"Dia\":null,\"DescripcionDia\":\"FERIADO FICTICIO\",\"AgnoCarga\":2026},"
                        + "\"0001-01-01\":{\"Tipo\":\"INHABIL\",\"Dia\":5,\"DescripcionDia\":\"SABADO\",\"AgnoCarga\":2026}}}");
        assertThat(j.getAsJsonArray("BCS_OPERACION_INSTRUMENTO").toString())
                .isEqualTo("[{\"AccionOrden\":\"COMPRA\",\"IdTipoInstrumento\":\"ACC\",\"IdTipoOperacion\":\"COMCN\",\"EsVentaCorta\":\"N\",\"IdTipoProducto\":\"RV\",\"EsPutEsCall\":\"N\"}]");
        // the filter text has its tabs and line breaks removed
        assertThat(j.getAsJsonObject("bcs_filtrocombobase").toString()).isEqualTo("{\"ORD\":{\"COMBO\":{\"cmbMoneda\":\"a=1,b=2\"}}}");
        // public configuration: allowed parameters of every module and everything of PRODUCTO
        assertThat(j.getAsJsonObject("BCS_CONFIGURACION").getAsJsonObject("APV").toString()).isEqualTo("{\"APV.Habilitado\":\"S\"}");
        assertThat(j.getAsJsonObject("BCS_CONFIGURACION").getAsJsonObject("PRODUCTO").toString()).isEqualTo("{\"PRODUCTO.Cualquiera\":\"si-sale\"}");
        assertThat(j.getAsJsonObject("BCS_CONFIGURACION").getAsJsonObject("PLATAFORMA").size()).isZero();   // no allowed parameter of PLATAFORMA
    }

    @Test
    @DisplayName("T-21b the format matrices are uso -> moneda -> tipo de instrumento -> tipo de operacion -> {decimals, truncate/round}; rows without uso are skipped")
    void matrices_de_formato() throws Exception {
        JsonObject j = home(TOKEN);
        assertThat(j.getAsJsonObject("bcs_formato_cantidad").toString())
                .isEqualTo("{\"USD1\":{\"CLP\":{\"ACC\":{\"COMCN\":{\"CantidadDecimales\":2,\"CantidadTrucaRedondea\":\"T\"},\"VENCN\":{\"CantidadDecimales\":3,\"CantidadTrucaRedondea\":\"R\"}}}}}");
        assertThat(j.getAsJsonObject("bcs_formato_tipoCambio").getAsJsonObject("USD1").getAsJsonObject("CLP").getAsJsonObject("ACC").getAsJsonObject("VENCN").toString())
                .isEqualTo("{\"TipoCambioDecimales\":3,\"TipoCambioTrucaRedondea\":\"R\"}");
        assertThat(j.getAsJsonObject("bcs_formato_puntos").has("X")).isFalse();
    }

    @Test
    @DisplayName("T-21 BCS_ACCESOS: the administrator sees everything as existing; others get the default of PLA.ConflictoAccesoDefecto when the same element has conflicting rows")
    void accesos() throws Exception {
        JsonObject a = home(TOKEN).getAsJsonObject("BCS_ACCESOS");
        assertThat(a.get("ADMIN").getAsBoolean()).isFalse();
        assertThat(a.getAsJsonObject("mnuOrdenes").toString()).isEqualTo("{\"IdElementoHtml\":\"mnuOrdenes\",\"Existe\":\"S\",\"EsVisible\":\"S\"}");
        assertThat(a.has("mnuOculto")).isFalse();                      // MostrarAcceso = N
        assertThat(a.getAsJsonObject("btnX").get("Existe").getAsString()).isEqualTo("N");   // conflict S / N: the default of PLA.ConflictoAccesoDefecto
        sp.responder("BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin", (c, in) -> ProcedimientosGuionados.salida("P_OUT_ESADMIN", "S"));
        JsonObject admin = home(TOKEN).getAsJsonObject("BCS_ACCESOS");
        assertThat(admin.get("ADMIN").getAsBoolean()).isTrue();
        assertThat(admin.getAsJsonObject("btnX").get("Existe").getAsString()).isEqualTo("S");
    }

    @Test
    @DisplayName("T-21 a good home commits one more use of the token and the connected flag of the user")
    void home_confirma_el_uso() throws Exception {
        home(TOKEN);
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("1");
        assertThat(db.valor("SELECT \"Conectado\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1")).isEqualTo("S");
        home(TOKEN);
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("2");
    }

    // ------------------------------------------------------------------------------------------------ refusals

    @Test
    @DisplayName("T-21 T-11 no token header: 400 (session filter); unknown token: 401 (session filter); closed token: 401")
    void sin_token_y_token_desconocido() throws Exception {
        mvc.perform(post("/PLA/bootstrap/home")).andExpect(status().isBadRequest());
        mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", "otro")).andExpect(status().isUnauthorized());
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"TokenValido\" = 'N'");
        mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("T-21 a blocked or expired user, or a company badly configured, is 401 and the body carries only the legacy CODES; nothing is written")
    void sesion_no_utilizable() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IdEstado\" = 'BLO' WHERE \"IdUsuario\" = 1");
        var r = mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isUnauthorized()).andReturn().getResponse();
        assertThat(r.getContentAsString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_MENSAJES\":[{\"Codigo\":\"USUARIO_BLOQUEADO\"}]}");
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"IdEstado\" = 'ACT', \"FechaVigencia\" = DATE '2026-03-01' WHERE \"IdUsuario\" = 1");
        assertThat(mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isUnauthorized()).andReturn().getResponse()
                .getContentAsString()).contains("USUARIO_BLOQUEADO");
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"FechaVigencia\" = DATE '0001-01-01' WHERE \"IdUsuario\" = 1", "DELETE FROM \"BSCL.Plataforma.BD::PLA.Empresa\"");
        assertThat(mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isUnauthorized()).andReturn().getResponse()
                .getContentAsString()).contains("EMPRESA_MAL_CONFIGURADA");
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("0");
    }

    @Test
    @DisplayName("T-21 faithful to the legacy: a user that is neither employee nor client row is a NullPointerException: 500 and nothing is written")
    void usuario_sin_empleado_ni_cliente_es_500() throws Exception {
        db.sql("UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"TipoEntidad\" = 'PER' WHERE \"IdUsuario\" = 1");
        mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isInternalServerError());
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("0");
    }

    @Test
    @DisplayName("T-21 a failure after the session was used (the clock procedure is down) rolls the use back: 500, token use stays 0")
    void falla_tardia_deshace_el_uso() throws Exception {
        sp.responder("BSCL.Plataforma.BD.Utilitarios::ObtenerFechas", (c, in) -> {
            throw new IllegalStateException("procedure down");
        });
        mvc.perform(post("/PLA/bootstrap/home").header("X-SESSION-TOKEN", TOKEN)).andExpect(status().isInternalServerError());
        assertThat(db.valor("SELECT \"UsoToken\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = '" + TOKEN + "'")).isEqualTo("0");
        assertThat(db.valor("SELECT \"Conectado\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = 1")).isEqualTo("N");
    }
}
