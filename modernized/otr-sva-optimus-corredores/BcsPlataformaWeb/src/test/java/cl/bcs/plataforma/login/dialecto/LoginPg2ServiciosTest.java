package cl.bcs.plataforma.login.dialecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.login.bootstrap.BootstrapServicio;
import cl.bcs.plataforma.login.clave.GestionClaveServicio;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.ArregloGrabado;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Respuesta;
import cl.bcs.plataforma.login.dialecto.JdbcGrabador.Sentencia;
import cl.bcs.plataforma.login.sesion.SesionDatos;

/**
 * LOGIN-PG round 2, part B (T-33): the REAL login services ({@code BootstrapServicio}, {@code GestionClaveServicio}) over the Postgres implementation and the
 * recording fake. It proves the part the unit contract cannot: that the legacy OUT names the services read ({@code P_OUT_ESADMIN}, {@code P_OUT_VALOR},
 * {@code P_OUT_MONEDAFORMATO}, {@code P_OUT_FECHAHORASISTEMALOCAL}, {@code P_OUT_FECHAHORASISTEMA}, {@code P_OUT_MAIL}, {@code P_OUT_ERRORESCONTADOR}) arrive
 * with the right values, and that the JSON the services build for the event procedure becomes the four arrays. Services that are package-private or private
 * are reached by reflection (the production classes are not edited).
 */
class LoginPg2ServiciosTest {

    private static ProcedimientoAlmacenado pg() {
        return ApiLoginPg3.procedimientosPostgres();
    }

    private static SentenciasSql sentenciasPg() {
        return ApiLoginPg3.sentenciasPostgres();
    }

    private static LoginPg2Recursos.Rutina r(String pgNombre) {
        return LoginPg2ProcedimientosTest.RUT.rutina(pgNombre);
    }

    private static Object invocar(Object o, String nombre, Class<?>[] tipos, Object... args) throws Exception {
        Method m = o.getClass().getDeclaredMethod(nombre, tipos);
        m.setAccessible(true);
        try {
            return m.invoke(o, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            throw e;
        }
    }

    // ------------------------------------------------------------------------------------------------ GestionClaveServicio

    @Test
    @DisplayName("LOGIN-PG2 service: SolicitudRecuperacionClave over Postgres: counter 0 and the mail come back under P_OUT_ERRORESCONTADOR / P_OUT_MAIL, the answer is the 200 of the legacy with the masked address; the token is 5 characters and the user name upper case")
    void solicitud_recuperacion_ok() throws Exception {
        LoginPg2Recursos.Rutina rut = r("sp_pla_acc_recuperar_clave_usuario");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(rut,
                LoginPg2ProcedimientosTest.mapa("p_out_mail", "jperez@example.invalid", "p_out_advertencias_contador", 0, "p_out_errores_contador", 0))));
        GestionClaveServicio.Respuesta resp = new GestionClaveServicio(pg(), sentenciasPg()).solicitudRecuperacionClave(g.conexion(), "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\"}");
        assertEquals(200, resp.estado());
        JsonObject cuerpo = LoginPg2ProcedimientosTest.json(resp.cuerpo());
        assertEquals("0", cuerpo.get("statusCode").getAsString());
        assertEquals("OK", cuerpo.get("statusMessage").getAsString());
        assertEquals(GestionClaveServicio.obfuscateEmail("jperez@example.invalid"), cuerpo.get("recoveryTo").getAsString());
        Sentencia s = g.unica();
        assertEquals("{call sp_pla_acc_recuperar_clave_usuario(?,?,?,?,?,?,?,?)}", LoginPg2ProcedimientosTest.norm(s.sql));
        assertTrue(JdbcGrabador.iguales(1L, s.valor(1)));
        assertEquals("JPEREZ", s.valor(2));
        assertThat((String) s.valor(3)).hasSize(5);
    }

    @Test
    @DisplayName("LOGIN-PG2 service: SolicitudRecuperacionClave over Postgres with errors: the counter 2 read from P_OUT_ERRORESCONTADOR becomes statusCode 2 and ERROR_RECUPERAR_CLAVE")
    void solicitud_recuperacion_con_errores() throws Exception {
        LoginPg2Recursos.Rutina rut = r("sp_pla_acc_recuperar_clave_usuario");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(rut,
                LoginPg2ProcedimientosTest.mapa("p_out_mail", null, "p_out_advertencias_contador", 0, "p_out_errores_contador", 2,
                        "p_out_errores_detalle", List.of(JdbcGrabador.fila("codigo", "E1"))))));
        GestionClaveServicio.Respuesta resp = new GestionClaveServicio(pg(), sentenciasPg()).solicitudRecuperacionClave(g.conexion(), "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\"}");
        assertEquals(200, resp.estado());
        assertEquals(LoginPg2ProcedimientosTest.json("{\"statusCode\":2,\"statusMessage\":\"ERROR_RECUPERAR_CLAVE\"}"), LoginPg2ProcedimientosTest.json(resp.cuerpo()));
    }

    @Test
    @DisplayName("LOGIN-PG2 service: ConfirmarCambioClave over Postgres: counter 0 is the 200 OK and nothing is rolled back; counter 1 is statusCode 1 ERROR_CONFIRMAR_CLAVE and the service rolls the request back")
    void confirmar_cambio_clave() throws Exception {
        LoginPg2Recursos.Rutina rut = r("sp_pla_acc_confirmar_cambio_clave_usuario");
        String body = "{\"p_IdEmpresa\":\"1\",\"p_Username\":\"jperez\",\"p_Password\":\"Nueva-Clave-1\",\"p_Token\":\"A1B2C\"}";
        JdbcGrabador ok = new JdbcGrabador().responder(Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(rut,
                LoginPg2ProcedimientosTest.mapa("p_status_code", 0, "p_out_advertencias_contador", 0, "p_out_errores_contador", 0))));
        GestionClaveServicio.Respuesta a = new GestionClaveServicio(pg(), sentenciasPg()).confirmarCambioClave(ok.conexion(), body);
        assertEquals(LoginPg2ProcedimientosTest.json("{\"statusCode\":\"0\",\"statusMessage\":\"OK\"}"), LoginPg2ProcedimientosTest.json(a.cuerpo()));
        assertThat(ok.otrasLlamadas).doesNotContain("rollback");
        assertEquals("JPEREZ", ok.unica().valor(2));
        assertEquals("Nueva-Clave-1", ok.unica().valor(3));
        assertEquals("A1B2C", ok.unica().valor(4));
        JdbcGrabador mal = new JdbcGrabador().responder(Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(rut,
                LoginPg2ProcedimientosTest.mapa("p_status_code", 1, "p_out_advertencias_contador", 0, "p_out_errores_contador", 1))));
        GestionClaveServicio.Respuesta b = new GestionClaveServicio(pg(), sentenciasPg()).confirmarCambioClave(mal.conexion(), body);
        assertEquals(LoginPg2ProcedimientosTest.json("{\"statusCode\":1,\"statusMessage\":\"ERROR_CONFIRMAR_CLAVE\"}"), LoginPg2ProcedimientosTest.json(b.cuerpo()));
        assertThat(mal.otrasLlamadas).contains("rollback");
    }

    @Test
    @DisplayName("LOGIN-PG2 service: fechaSistema reads P_OUT_FECHAHORASISTEMA and keeps the date part")
    void fecha_sistema() throws Exception {
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(Map.of(2, "2026-11-03 09:30:00")));
        Object fecha = invocar(new GestionClaveServicio(pg(), sentenciasPg()), "fechaSistema", new Class<?>[] { Connection.class, long.class }, g.conexion(), 1L);
        assertEquals("2026-11-03", fecha);
        assertEquals("{call sp_pla_obtener_fecha_hora_sistema(?,?)}", LoginPg2ProcedimientosTest.norm(g.unica().sql));
    }

    @Test
    @DisplayName("LOGIN-PG2 service: the event the recovery notifies (SOLICITUD_CAMBIO_CLAVE) reaches Postgres as four arrays: one event, four values (name with spaces quoted), no attachment, one address; company, user and module as numbers and text; origin ''")
    void notificacion_de_la_recuperacion() throws Exception {
        GestionClaveServicio.DatosRecuperacion d = new GestionClaveServicio.DatosRecuperacion();
        d.IdPersona = 777L;
        d.IdDireccionElectronicaPrincipal = 5551L;
        d.NombreCompleto = "Juan Perez Soto";
        d.corredora = "BCS Corredores";
        d.IdEmpresa = 1L;
        d.IdUsuario = 7L;
        d.TipoEntidad = "CLI";
        JdbcGrabador g = new JdbcGrabador();
        invocar(new GestionClaveServicio(pg(), sentenciasPg()), "notificar", new Class<?>[] { Connection.class, GestionClaveServicio.DatosRecuperacion.class, String.class, String.class, String.class },
                g.conexion(), d, "2026-11-03", "Xk3!q", "/logo/bcs.png");
        Sentencia s = g.unica();
        assertEquals("{call sp_pla_evt_agregar_evento(?,?,?,?,?,?,?,?,?)}", LoginPg2ProcedimientosTest.norm(s.sql));
        assertTrue(JdbcGrabador.iguales(1L, s.valor(1)));
        assertTrue(JdbcGrabador.iguales(7L, s.valor(2)));
        assertEquals("PLA", s.valor(3));
        assertEquals(new ArregloGrabado("ty_pla_evt_agregar_evento_eventos", List.of("(SOLICITUD_CAMBIO_CLAVE,0,2026-11-03,0,777,0)")), s.valor(4));
        assertEquals(new ArregloGrabado("ty_pla_evt_agregar_evento_valores", List.of("(0,Nombre,\"Juan Perez Soto\")", "(0,Corredora,\"BCS Corredores\")", "(0,ClaveProvisoria,Xk3!q)",
                "(0,LogoCorredora,/logo/bcs.png)")), s.valor(5));
        assertEquals(new ArregloGrabado("ty_pla_evt_agregar_evento_archivos_adjuntos", List.of()), s.valor(6));
        assertEquals(new ArregloGrabado("ty_pla_evt_agregar_evento_id_direcciones", List.of("(0,5551,TO)")), s.valor(7));
        assertEquals("", s.valor(8));
    }

    // ------------------------------------------------------------------------------------------------ BootstrapServicio

    private static BootstrapServicio bootstrap() {
        return new BootstrapServicio(new SesionDatos(), pg(), sentenciasPg());
    }

    @Test
    @DisplayName("LOGIN-PG2 service: fechaHoraLocal reads P_OUT_FECHAHORASISTEMALOCAL (the fourth of the six dates) and sends only the company")
    void fecha_hora_local() throws Exception {
        Caso c = Caso.FECHAS;
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.salidas(LoginPg2ProcedimientosTest.porIndice(r("sp_pla_obtener_fechas"), c.salidas)));
        Object fecha = invocar(bootstrap(), "fechaHoraLocal", new Class<?>[] { Connection.class, long.class }, g.conexion(), 1L);
        assertEquals("2026-11-02 09:00:00", fecha);
        assertEquals("{call sp_pla_obtener_fechas(?,?,?,?,?,?,?)}", LoginPg2ProcedimientosTest.norm(g.unica().sql));
    }

    private static final class Caso {
        static final Caso FECHAS = new Caso();
        final Map<String, Object> salidas = LoginPg2ProcedimientosTest.mapa("p_out_fecha_sistema_utc", "2026-11-03", "p_out_fecha_sistema_local", "2026-11-02",
                "p_out_fecha_hora_sistema_utc", "2026-11-03 12:00:00", "p_out_fecha_hora_sistema_local", "2026-11-02 09:00:00", "p_out_fecha_hora_utc", "2026-11-03 12:00:01",
                "p_out_fecha_hora_local", "2026-11-02 09:00:01");
    }

    @Test
    @DisplayName("LOGIN-PG2 service: formatos() builds the seven format matrices from the rows of fn_pla_consultar_formatos under the HANA labels (a different decimals and rounding per matrix: a swapped label shows)")
    void formatos() throws Exception {
        Map<String, Object> fila = JdbcGrabador.fila("id_uso_moneda_formato", "OPE", "id_moneda", "CLP", "id_tipo_instrumento", "RF", "id_tipo_operacion", "CPR",
                "precio_decimales", 1, "precio_truca_redondea", "P", "tasa_decimales", 2, "tasa_truca_redondea", "Q", "cantidad_decimales", 0, "cantidad_truca_redondea", "C",
                "monto_decimales", 3, "monto_truca_redondea", "M", "tipo_cambio_decimales", 4, "tipo_cambio_truca_redondea", "X", "paridad_decimales", 5,
                "paridad_truca_redondea", "Y", "puntos_decimales", 6, "puntos_truca_redondea", "Z");
        JdbcGrabador g = new JdbcGrabador().responder(Respuesta.filas(List.of(fila)));
        JsonObject out = new JsonObject();
        invocar(bootstrap(), "formatos", new Class<?>[] { Connection.class, JsonObject.class }, g.conexion(), out);
        String[][] esperado = { { "bcs_formato_cantidad", "CantidadDecimales", "0", "CantidadTrucaRedondea", "C" }, { "bcs_formato_precio", "PrecioDecimales", "1", "PrecioTrucaRedondea", "P" },
                { "bcs_formato_tasa", "TasaDecimales", "2", "TasaTrucaRedondea", "Q" }, { "bcs_formato_monto", "MontoDecimales", "3", "MontoTrucaRedondea", "M" },
                { "bcs_formato_tipoCambio", "TipoCambioDecimales", "4", "TipoCambioTrucaRedondea", "X" }, { "bcs_formato_paridad", "ParidadDecimales", "5", "ParidadTrucaRedondea", "Y" },
                { "bcs_formato_puntos", "PuntosDecimales", "6", "PuntosTrucaRedondea", "Z" } };
        for (String[] e : esperado) {
            JsonObject nodo = out.getAsJsonObject(e[0]).getAsJsonObject("OPE").getAsJsonObject("CLP").getAsJsonObject("RF").getAsJsonObject("CPR");
            assertEquals(e[2], nodo.get(e[1]).getAsString(), e[0]);
            assertEquals(e[4], nodo.get(e[3]).getAsString(), e[0]);
        }
        assertEquals("select * from fn_pla_consultar_formatos()", LoginPg2ProcedimientosTest.norm(g.unica().sql).toLowerCase());
    }

    @Test
    @DisplayName("LOGIN-PG2 service: accesos() over Postgres calls admin (P_OUT_ESADMIN), then the default of conflicts (P_OUT_VALOR), then SQL_ACCESOS in Postgres text; the conflict rule uses the default read from P_OUT_VALOR")
    void accesos() throws Exception {
        ApiLoginPg3.textoPg("BootstrapServicio.SQL_ACCESOS");
        JdbcGrabador g = new JdbcGrabador().guion(s -> {
            if (s.sql.contains("sp_pla_acc_consultar_usuario_admin")) return Respuesta.salidas(Map.of(3, "N"));
            if (s.sql.contains("sp_pla_obtener_configuracion")) return Respuesta.salidas(Map.of(3, "S"));
            return Respuesta.filas(List.of(acceso("btnA", "S", "S"), acceso("btnA", "S", "N"), acceso("btnB", "N", "S")));
        });
        Object out = invocar(bootstrap(), "accesos", new Class<?>[] { Connection.class, long.class, long.class }, g.conexion(), 1L, 10L);
        assertEquals(LoginPg2ProcedimientosTest.json("{\"ADMIN\":false,\"btnA\":{\"IdElementoHtml\":\"btnA\",\"Existe\":\"S\",\"EsVisible\":\"S\"},"
                + "\"btnB\":{\"IdElementoHtml\":\"btnB\",\"Existe\":\"S\",\"EsVisible\":\"N\"}}"), out);
        assertEquals(3, g.sentencias.size());
        assertEquals("{call sp_pla_acc_consultar_usuario_admin(?,?,?)}", LoginPg2ProcedimientosTest.norm(g.sentencias.get(0).sql));
        assertEquals("{call sp_pla_obtener_configuracion(?,?,?)}", LoginPg2ProcedimientosTest.norm(g.sentencias.get(1).sql));
        assertEquals("PLA.ConflictoAccesoDefecto", g.sentencias.get(1).valor(2));
        assertThat(g.sentencias.get(2).sql).contains("v_pla_acc_consultar_usuario_acceso").doesNotContain("BSCL").doesNotContain("::");
    }

    private static Map<String, Object> acceso(String id, String visible, String existe) {
        return JdbcGrabador.fila("IdElementoHtml", id, "EsVisibleMenu", visible, "Existe", existe, "IdUsuario", 10, "IdAcceso", 1, "NombreAcceso", "x", "TipoAcceso", "MENU", "Padre", null,
                "Orden", 1, "IdServicio", 100);
    }
}
