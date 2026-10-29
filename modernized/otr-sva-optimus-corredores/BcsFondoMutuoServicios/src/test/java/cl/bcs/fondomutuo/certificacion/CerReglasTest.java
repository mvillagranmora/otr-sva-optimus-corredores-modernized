package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;

/**
 * Rule by rule, with LITERAL values: every card of FM-CER-01..20 and 23 (RULES_FONDOMUTUO_F3_B.md 8.1) and the defects it pins (D-F3B-01/08/12/16/31, D-NOR-9/16, DESVIO-KIT-1)
 * against the new facade. The expected values are the ones the unmodified legacy produced when the goldens were recorded (they are written here by hand from the golden of the case, so a
 * change of the golden that is not a change of the rule shows up in {@link #elIndiceGeneradoCoincideConLoQueDevuelveElCodigoNuevo}). Each test names the card in its display name.
 *
 * <p>Terminal cases give the OUTPUT of the request (counter, message codes, code of every row) or the exception; cases that continue give what F-A and F-B left at the seam (the
 * {@link CertificarAporteRescateContexto}). The pure rules of F-B ({@link CertificarAporteRescateReglas}) are checked on their own at the end.
 */
class CerReglasTest {

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** counter | message codes | id:code of every row of the output | servlet decision, of the TERMINAL case (new code). */
    private static String salida(String slug) throws Exception {
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug(slug));
        assertNotNull(c.volcado.get("salida"), slug + " must end with an output, got " + c.volcado.get("excepcion"));
        JsonObject s = c.volcado.getAsJsonObject("salida");
        List<String> mensajes = new ArrayList<>();
        for (JsonElement m : s.getAsJsonArray("P_OUT_MENSAJES")) mensajes.add(m.getAsJsonObject().get("codigo").getAsString());
        List<String> filas = new ArrayList<>();
        for (JsonElement r : s.getAsJsonArray("P_OUT_RESULTADO")) filas.add(r.getAsJsonObject().get("IdSolicitudAporteRescate").getAsString() + ":" + r.getAsJsonObject().get("Codigo").getAsString());
        return s.get("P_OUT_ERRORESCONTADOR").getAsString() + "|" + String.join(",", mensajes) + "|" + String.join(",", filas) + "|" + c.volcado.get("decisionServlet").getAsString();
    }

    /** The exception of the TERMINAL case: class and, for a plain Exception, the message. */
    private static String excepcion(String slug) throws Exception {
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug(slug));
        assertNotNull(c.volcado.get("excepcion"), slug + " must throw");
        return c.volcado.get("excepcion").getAsString();
    }

    /** The context that the seam received for the `-parada` variant of the case (the facade ran F-A and F-B for real). */
    private static CertificarAporteRescateContexto ctx(String slugBase) throws Exception {
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug(slugBase + "-parada"));
        assertNotNull(c.contexto, slugBase + " must reach the seam; it ended with " + c.excepcion);
        return c.contexto;
    }

    private static SolicitudProceso sol(CertificarAporteRescateContexto ctx, long id) {
        return ctx.solicitudes.stream().filter(s -> s.IdSolicitudAporteRescate == id).findFirst().orElseThrow(() -> new AssertionError("solicitud " + id + " not read"));
    }

    private static List<Long> ids(List<SolicitudProceso> l) {
        List<Long> o = new ArrayList<>();
        for (SolicitudProceso s : l) o.add(s.IdSolicitudAporteRescate);
        return o;
    }

    private static void igual(String esperado, BigDecimal real, String donde) {
        assertNotNull(real, donde);
        assertEquals(0, new BigDecimal(esperado).compareTo(real), donde + ": expected " + esperado + " was " + real.toPlainString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-01 / 02 contract, requirements, empty list, repeated ids
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-01 defaults of the contract: p_NotificaOWP=S, switch simulated=N, canal of a solicitud = '' (the legacy pojo defaults, plain Gson)")
    void fmCer01_defaultsDelContrato() throws Exception {
        CertificarAporteRescateContexto c = ctx("contrato-minimo-con-defaults");
        assertEquals("S", c.entrada.p_NotificaOWP);
        assertEquals("N", c.entrada.p_CertificacionAporteSwitchSimulado);
        assertEquals("", c.entrada.p_Solicitudes.get(0).IdTipoCanalIngreso);
        assertNull(c.entrada.p_SimularOperacion);
        assertEquals(List.of(1001L), ids(c.solicitudes));
        CertificarAporteRescateContexto d = ctx("contrato-campos-extra-y-canal-por-solicitud");
        assertEquals("N", d.entrada.p_NotificaOWP);
        assertEquals("S", d.entrada.p_CertificacionAporteSwitchSimulado);
        assertEquals("OWP", d.entrada.p_Solicitudes.get(0).IdTipoCanalIngreso);
        assertEquals("AGE", d.entrada.p_IdTipoCanalIngreso);
    }

    @Test
    @DisplayName("FM-CER-01 / D-NOR-9 an id as text is coerced by Gson; a non numeric id is a JsonSyntaxException; null list, null element: NullPointerException")
    void fmCer01_idsComoTextoYNoNumericos() throws Exception {
        assertEquals(List.of(1001L), ids(ctx("contrato-ids-y-empresa-como-texto").solicitudes));
        assertTrue(excepcion("contrato-id-no-numerico").startsWith("JsonSyntaxException:"));
        assertTrue(excepcion("contrato-solicitudes-nulas").startsWith("NullPointerException:"));
        assertTrue(excepcion("contrato-elemento-nulo-en-la-lista").startsWith("NullPointerException:"));
        assertEquals("1|NO_SE_INGRESARON_SOLICITUDES||ROLLBACK", salida("contrato-sin-clave-solicitudes"));
    }

    @Test
    @DisplayName("FM-CER-02 requirements: user 99 does not exist, user 9 is not active, module XXX does not exist; null user and module give two messages; counter 1, rollback")
    void fmCer02_requisitosBasicos() throws Exception {
        assertEquals("1|USUARIO_NO_EXISTE||ROLLBACK", salida("usuario-inexistente"));
        assertEquals("1|USUARIO_NO_VIGENTE||ROLLBACK", salida("usuario-inactivo"));
        assertEquals("1|MODULO_NO_EXISTE||ROLLBACK", salida("modulo-inexistente"));
        assertEquals("1|USUARIO_VACIO,MODULO_VACIO||ROLLBACK", salida("contrato-usuario-y-modulo-nulos"));
    }

    @Test
    @DisplayName("FM-CER-02 empty list: NO_SE_INGRESARON_SOLICITUDES (id 0); repeated ids, also not consecutive, also two nulls, also with another channel: SOLICITUD_DUPLICADA; D-REQ-11")
    void fmCer02_listaVaciaYRepetidos() throws Exception {
        assertEquals("1|NO_SE_INGRESARON_SOLICITUDES||ROLLBACK", salida("lista-vacia"));
        assertEquals("1|SOLICITUD_DUPLICADA||ROLLBACK", salida("ids-repetidos"));
        assertEquals("1|SOLICITUD_DUPLICADA||ROLLBACK", salida("ids-repetidos-no-consecutivos"));
        assertEquals("1|SOLICITUD_DUPLICADA||ROLLBACK", salida("dos-ids-nulos-cuentan-como-duplicado"));
        assertEquals("1|SOLICITUD_DUPLICADA||ROLLBACK", salida("contrato-mismo-id-con-distinto-canal-es-duplicado"));
    }

    @Test
    @DisplayName("FM-CER-02 order: the requirements win over the empty list and the duplicates; PLA.PaisSistema is read BEFORE the requirements; the duplicates and the empty list win over the other parameters")
    void fmCer02_ordenDeLasComprobaciones() throws Exception {
        assertEquals("1|USUARIO_NO_EXISTE||ROLLBACK", salida("requisitos-ganan-a-lista-vacia"));
        assertEquals("1|USUARIO_NO_EXISTE||ROLLBACK", salida("requisitos-ganan-a-duplicados"));
        assertEquals("Exception: Parametro PLA.PaisSistema en modulo PLATAFORMA no encontrado", excepcion("pais-sistema-faltante-gana-a-requisitos"));
        assertEquals("NumberFormatException: For input string: \"abc\"", excepcion("pais-sistema-no-numerico"));
        assertEquals("1|SOLICITUD_DUPLICADA||ROLLBACK", salida("duplicados-ganan-a-parametros-faltantes"));
        assertEquals("1|NO_SE_INGRESARON_SOLICITUDES||ROLLBACK", salida("lista-vacia-gana-a-parametros-faltantes"));
    }

    @Test
    @DisplayName("FM-CER-02 / DESVIO-KIT-1 a company with NO configuration (or null): the legacy raises NullPointerException, the kit ParametroNoEncontradoException; either one, only in these slugs")
    void fmCer02_empresaSinConfiguracion() throws Exception {
        for (String slug : new String[] { "empresa-sin-configuracion", "empresa-inexistente", "contrato-empresa-nula" }) {
            assertTrue(CasoCer.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(slug));
            assertTrue(CasoCer.golden(CasoCer.idPorSlug(slug)).get("excepcion").getAsString().startsWith("NullPointerException:"), slug + " golden");
            String e = excepcion(slug);
            assertTrue(e.startsWith("NullPointerException:") || (e.startsWith("Exception: Parametro ") && e.endsWith(" no encontrado")), slug + " -> " + e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-03 parameters
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "fecha-sistema|PLA.FechaSistema|PLATAFORMA", "tipo-comprobante-no-apv|TipoComprobanteFacturaNoApv|FONDOMUTUO", "tipo-comprobante-apv|TipoComprobanteFacturaApv|FONDOMUTUO",
            "calcular-comision|CALCULAR_COMISION_CERTIFICACION|FONDOMUTUO", "validar-certificacion|VALIDAR_CERTIFICACION|FONDOMUTUO", "rescate-genera-spot|RESCATE_GENERA_VENTA_SPOT_DOLAR|FONDOMUTUO",
            "comprobante-automatico|RESCATE_COMISION_COMPROBANTE_AUTOMATICO|FONDOMUTUO", "notificar-owp|PLA.NotificarEstadoOrdenOWP|PLATAFORMA" })
    @DisplayName("FM-CER-03 a mandatory parameter that is missing raises Exception('Parametro X en modulo Y no encontrado') BEFORE any read of the solicitudes")
    void fmCer03_parametroObligatorioFaltante(String fila) throws Exception {
        String[] p = fila.split("\\|");
        assertEquals("Exception: Parametro " + p[1] + " en modulo " + p[2] + " no encontrado", excepcion("parametro-obligatorio-faltante-" + p[0]));
        assertEquals("[]", CasoCer.ejecutar(CasoCer.idPorSlug("parametro-obligatorio-faltante-" + p[0])).volcado.get("colaboradores").toString(), "nothing is read after the missing parameter");
    }

    @Test
    @DisplayName("FM-CER-03 the first missing parameter in READ order wins (TipoComprobanteFacturaApv before VALIDAR_CERTIFICACION)")
    void fmCer03_ganaElPrimeroQueSeLee() throws Exception {
        assertEquals("Exception: Parametro TipoComprobanteFacturaApv en modulo FONDOMUTUO no encontrado", excepcion("parametros-faltantes-gana-el-primero-que-se-lee"));
    }

    @Test
    @DisplayName("FM-CER-03 optional parameters: APV.PorcentajeRetencion absent / not numeric -> 0 and the run goes on; TGR and SII documents absent -> no person; D-F3B-09")
    void fmCer03_opcionales() throws Exception {
        assertEquals(0L, ctx("parametro-opcional-retencion-ausente").porcentajeRetencion);
        assertEquals(15L, ctx("parametro-opcional-retencion-presente").porcentajeRetencion);
        assertEquals(0L, ctx("parametro-opcional-retencion-no-numerico").porcentajeRetencion);
        CertificarAporteRescateContexto c = ctx("parametros-opcionales-tgr-y-sii-ausentes");
        assertNull(c.personaTGR);
        assertNull(c.personaSII);
        CertificarAporteRescateContexto d = ctx("contrato-minimo-con-defaults");
        assertEquals(3001L, d.personaTGR.IdPersona);
        assertEquals(3002L, d.personaSII.IdPersona);
        assertEquals("N", d.validarCertificacion);
        assertEquals("FFM", d.tipoComprobanteFacturaNoApv);
        assertEquals("FOE", d.tipoComprobanteFacturaApv);
        assertEquals("N", d.calcularComisionCertificacion);
        assertEquals("N", d.rescateGeneraVentaSpotDolar);
        assertEquals("N", d.comisionComprobanteFacturacionAutomatico);
        assertEquals("N", d.notificaOWP);
        assertEquals(56L, d.paisSistema);
        assertEquals("2026-03-02", d.fechaSistema);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-04 reads, FM-CER-05 / 08 quota value
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-04 nothing found: UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS (counter 1, no log); a solicitud of another company is not read; an unknown id is ignored without a message")
    void fmCer04_lecturas() throws Exception {
        assertEquals("1|UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS||ROLLBACK", salida("ninguna-solicitud-existe"));
        assertEquals("1|UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS||ROLLBACK", salida("solicitud-de-otra-empresa-no-se-lee"));
        assertEquals("1|UI_NO_EXISTEN_SOLICITUDES_CUMPLAN_FILTROS||ROLLBACK", salida("contrato-id-nulo-en-solicitud"));
        assertEquals(List.of(1001L), ids(ctx("una-existe-y-otra-no-se-ignora-sin-mensaje").solicitudes));
    }

    @Test
    @DisplayName("FM-CER-04 the solicitudes come ordered by TipoSolicitud only (APO before RES; the rest in the order of the table), not in the order of the request")
    void fmCer04_ordenDeLectura() throws Exception {
        assertEquals(List.of(1001L, 1003L, 1002L), ids(ctx("orden-de-lectura-por-tipo-solicitud").solicitudes));
    }

    @Test
    @DisplayName("FM-CER-04 / FM-CER-35 the traspaso maps of APV and not APV are merged with the NOT APV one winning for the same id; the related solicitud is also looked up")
    void fmCer04_traspasos() throws Exception {
        CertificarAporteRescateContexto a = ctx("traspaso-apv-y-no-apv-mismo-id-gana-el-no-apv");
        assertEquals(202L, a.traspasos.get(2001L).IdPersona, "the not APV traspaso (entity recaudadora person 202) wins over the APV one (person 101)");
        assertEquals(101L, ctx("traspaso-solo-apv").traspasos.get(2001L).IdPersona);
        CertificarAporteRescateContexto r = ctx("traspaso-de-la-solicitud-relacionada");
        assertEquals(202L, r.traspasos.get(2001L).IdPersona, "a traspaso found through the related id 2001 of solicitud 2002");
        assertNull(r.traspasos.get(2002L));
    }

    @Test
    @DisplayName("FM-CER-04 / D-F3B-12 / D-NOR-9 two periodic commission rows of the series DUPLICATE the solicitud, and the list that is keyed by id throws Exception('key field is not unique'); the other kind of commission does not")
    void fmCer04_filasDuplicadasPorElJoin() throws Exception {
        assertEquals("Exception: key field is not unique", excepcion("fila-duplicada-por-join-de-comision"));
        assertEquals("Exception: key field is not unique", excepcion("comision-nn-dos-hijas-duplica-la-fila"));
        assertEquals(List.of(1001L), ids(ctx("comision-no-periodica-no-duplica").solicitudes));
        assertEquals("Exception: key field is not unique", excepcion("estado-duplicado-en-pla-estado"));
        assertEquals("Exception: key field is not unique", excepcion("indicadores-duplicados-en-la-vista-lanzan"));
    }

    @Test
    @DisplayName("FM-CER-05 the price of the quota is asked once with the data of every solicitud; a repeated IdRegistro raises IllegalStateException (D-NOR-9); a row without IdInstrumento is dropped")
    void fmCer05_precios() throws Exception {
        assertTrue(excepcion("valor-cuota-precio-repetido-lanza").startsWith("IllegalStateException:"));
        assertEquals("1||1001:UI_NO_EXISTE_VALOR_CUOTA|ROLLBACK", salida("valor-cuota-precio-sin-instrumento-se-descarta"));
        JsonObject g = CasoCer.golden(CasoCer.idPorSlug("valor-cuota-cargado-correcto"));
        JsonObject entrada = g.getAsJsonArray("colaboradores").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals(56, entrada.get("p_IdPais").getAsInt());
        assertEquals(2, entrada.getAsJsonArray("p_DatosInstrumentos").size());
        JsonObject d = entrada.getAsJsonArray("p_DatosInstrumentos").get(0).getAsJsonObject();
        assertEquals("FM", d.get("IdClasificacionTipoInstrumento").getAsString());
        assertEquals("CARTERA", d.get("IdUso").getAsString());
        assertEquals("0", d.get("IdTipoCriterioTasa").getAsString());
        assertEquals("0", d.get("TipoCartera").getAsString());
        assertEquals(0, d.get("PlazoResidual").getAsInt());
        assertEquals("2026-03-02 00:00:00.000", d.get("FechaConsulta").getAsString());
    }

    @Test
    @DisplayName("FM-CER-08 no price, null source date, other day, or a price of another record: UI_NO_EXISTE_VALOR_CUOTA; the same day with another time of the day is fine")
    void fmCer08_valorCuota() throws Exception {
        for (String slug : new String[] { "valor-cuota-sin-precio", "valor-cuota-fecha-fuente-nula", "valor-cuota-fecha-fuente-otro-dia", "valor-cuota-precio-de-otro-registro-no-sirve" }) {
            assertEquals("1||1001:UI_NO_EXISTE_VALOR_CUOTA|ROLLBACK", salida(slug), slug);
        }
        assertEquals(List.of(1001L), ids(ctx("valor-cuota-misma-fecha-con-hora").solicitudes));
        CertificarAporteRescateContexto c = ctx("valor-cuota-uno-sin-precio-y-otro-correcto");
        assertEquals("RZD", sol(c, 1001).EstadoProceso);
        assertEquals("UI_NO_EXISTE_VALOR_CUOTA", sol(c, 1001).Codigo);
        assertEquals("PEN", sol(c, 1002).EstadoProceso);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-06..13 the rules of validation
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-06 state: anything but ING and ACP (CRT, PEN, ANU, RZD, empty, lower case 'ing') is UI_ESTADO_INCORRECTO_CERTIFICACION")
    void fmCer06_estado() throws Exception {
        for (String slug : new String[] { "estado-certificado-rechaza", "estado-pen-rechaza", "estado-anu-rechaza", "estado-rzd-rechaza", "estado-vacio-rechaza", "estado-minuscula-rechaza" }) {
            assertEquals("1||1001:UI_ESTADO_INCORRECTO_CERTIFICACION|ROLLBACK", salida(slug), slug);
        }
        assertEquals("PEN", sol(ctx("estado-aceptado-pasa"), 1001).EstadoProceso);
        assertEquals("PEN", sol(ctx("estado-ingresado-pasa"), 1001).EstadoProceso);
    }

    @Test
    @DisplayName("FM-CER-07 certification date: other day, earlier day, '0001-01-01', a request date with a time of the day, a null request date: UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO for ALL; D-FEC-6")
    void fmCer07_fecha() throws Exception {
        for (String slug : new String[] { "fecha-certificacion-distinta-rechaza", "fecha-certificacion-anterior-rechaza", "fecha-certificacion-sin-fecha-rechaza", "fecha-de-entrada-con-hora-no-coincide",
                "contrato-fecha-ausente", "contrato-fecha-con-otro-formato" }) {
            assertEquals("1||1001:UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO|ROLLBACK", salida(slug), slug);
        }
        assertEquals("1||1002:UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO,1001:UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO|ROLLBACK", salida("fecha-nula-rechaza-a-todas"));
    }

    @Test
    @DisplayName("FM-CER-09 omnibus: GeneraOmnibus S with IdOmnibus NULL rejects UI_ONMIBUS_NO_CREADO (the misspelling is the legacy's, D-F3B-31); 0 or a number does not; a switch of a reinversion does not")
    void fmCer09_omnibus() throws Exception {
        for (String slug : new String[] { "omnibus-no-creado-rechaza", "omnibus-aporte-nulo-rechaza", "omnibus-reinversion-sin-switch-rechaza" }) {
            assertEquals("1||1001:UI_ONMIBUS_NO_CREADO|ROLLBACK", salida(slug), slug);
        }
        for (String slug : new String[] { "omnibus-en-cero-no-rechaza", "omnibus-creado-no-rechaza", "omnibus-tipo-sin-omnibus-no-rechaza", "omnibus-switch-de-reinversion-no-rechaza" }) {
            assertEquals("PEN", sol(ctx(slug), 1001).EstadoProceso, slug);
        }
    }

    @Test
    @DisplayName("FM-CER-10 treasury voucher of origin FMM whose state is not LIQ rejects UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO; liquidated, other origin, other solicitud: pass; the FIRST voucher is the one of the message")
    void fmCer10_comprobante() throws Exception {
        assertEquals("1||1001:UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO|ROLLBACK", salida("comprobante-no-liquidado-rechaza"));
        assertEquals("1||1001:UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO|ROLLBACK", salida("comprobantes-dos-para-la-misma-solicitud-gana-el-primero"));
        for (String slug : new String[] { "comprobante-liquidado-no-rechaza", "comprobante-de-otro-origen-no-rechaza", "comprobante-de-otra-solicitud-no-rechaza" }) {
            assertEquals("PEN", sol(ctx(slug), 1001).EstadoProceso, slug);
        }
        JsonObject s = CasoCer.ejecutar(CasoCer.idPorSlug("salida-temprana-con-descripcion-por-parametros")).volcado.getAsJsonObject("salida");
        assertEquals("Documento 7777 asociado no liquidado (solicitud 1001)", s.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().get("DescripcionCodigo").getAsString());
        assertEquals("Solicitud asociada 2001 no certificada ni liquidada", s.getAsJsonArray("P_OUT_RESULTADO").get(1).getAsJsonObject().get("DescripcionCodigo").getAsString());
    }

    @Test
    @DisplayName("FM-CER-11 previous liquidation: VALIDAR_CERTIFICACION=S, aporte, not LIQ, folio 0 rejects; a folio, LIQ, switch, a rescate, parameter N or 's' (lower case) pass")
    void fmCer11_liquidacionPrevia() throws Exception {
        assertEquals("1||1001:UI_VALIDACION_LIQUIDACION_FONDO_MUTUO|ROLLBACK", salida("liquidacion-previa-falta-rechaza"));
        for (String slug : new String[] { "liquidacion-previa-con-folio-pasa", "liquidacion-previa-estado-LIQ-pasa", "liquidacion-previa-switch-pasa", "liquidacion-previa-solo-aportes",
                "liquidacion-previa-parametro-N-no-valida", "liquidacion-previa-parametro-minuscula-no-valida" }) {
            assertEquals("PEN", sol(ctx(slug), 1001).EstadoProceso, slug);
        }
        assertEquals("S", ctx("liquidacion-previa-con-folio-pasa").validarCertificacion);
    }

    @Test
    @DisplayName("FM-CER-12 associated solicitud: regime that does not certify without the associated one, related <> 0, id greater than the related and the related is not (CRT and LIQ) -> UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA")
    void fmCer12_asociada() throws Exception {
        for (String slug : new String[] { "asociada-no-certificada-rechaza", "asociada-certificada-sin-liquidar-rechaza", "asociada-liquidada-sin-certificar-rechaza", "asociada-inexistente-en-la-tabla-rechaza" }) {
            assertEquals("1||2002:UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA|ROLLBACK", salida(slug), slug);
        }
        for (String slug : new String[] { "asociada-certificada-y-liquidada-pasa", "asociada-regimen-que-certifica-sin-asociado-pasa", "asociada-regimen-sin-configuracion-pasa", "asociada-regimen-inexistente-pasa",
                "asociada-switch-no-valida", "asociada-solicitud-sin-relacionado-pasa" }) {
            assertEquals("PEN", sol(ctx(slug), 2002).EstadoProceso, slug);
        }
        assertEquals("PEN", sol(ctx("asociada-con-id-menor-que-la-propia-pasa"), 2001).EstadoProceso);
    }

    @Test
    @DisplayName("FM-CER-13 TGR and SII: BF without the TGR person -> UI_NO_EXISTE_TGR_CONFIGURADA; APV-B without the SII person -> UI_NO_EXISTE_SII_CONFIGURADA; APV-AN is NOT checked (D-F3B-08); the regime also comes from the portfolio code")
    void fmCer13_tgrYSii() throws Exception {
        assertEquals("1||1001:UI_NO_EXISTE_TGR_CONFIGURADA|ROLLBACK", salida("tgr-no-configurada-rechaza-BF"));
        assertEquals("1||1001:UI_NO_EXISTE_SII_CONFIGURADA|ROLLBACK", salida("sii-no-configurada-rechaza-APV-B"));
        assertEquals("1||1001:UI_NO_EXISTE_TGR_CONFIGURADA|ROLLBACK", salida("tgr-y-sii-faltan-BF-gana-tgr"));
        assertEquals("1||1001:UI_NO_EXISTE_SII_CONFIGURADA|ROLLBACK", salida("regimen-apv-por-codigo-de-portafolio"));
        for (String slug : new String[] { "tgr-configurada-pasa-BF", "sii-no-configurada-no-valida-APV-AN", "sii-configurada-pasa-APV-B", "tgr-falta-pero-la-solicitud-es-APV-B-pasa", "proposito-no-apv-ignora-el-regimen" }) {
            assertEquals("PEN", sol(ctx(slug), 1001).EstadoProceso, slug);
        }
        assertNull(ctx("sii-no-configurada-no-valida-APV-AN").personaSII, "D-F3B-08: APV-AN goes on without the SII person (the Tesoreria phase would fail later)");
        assertEquals("APV-AN", sol(ctx("sii-no-configurada-no-valida-APV-AN"), 1001).IdRegimenTributarioAPV);
        assertEquals("", sol(ctx("proposito-no-apv-ignora-el-regimen"), 1001).IdRegimenTributarioAPV);
    }

    @Test
    @DisplayName("FM-CER-06..13 the first rule that fails wins, in the order 06 state, 07 date, 08 quota value, 09 omnibus, 10 voucher, 11 liquidation, 12 associated, 13 TGR")
    void fmCer06a13_ordenDeLasReglas() throws Exception {
        assertEquals("1||1001:UI_ESTADO_INCORRECTO_CERTIFICACION|ROLLBACK", salida("orden-estado-gana-a-fecha"));
        assertEquals("1||1001:UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO|ROLLBACK", salida("orden-fecha-gana-a-valor-cuota"));
        assertEquals("1||1001:UI_NO_EXISTE_VALOR_CUOTA|ROLLBACK", salida("orden-valor-cuota-gana-a-omnibus"));
        assertEquals("1||1001:UI_ONMIBUS_NO_CREADO|ROLLBACK", salida("orden-omnibus-gana-a-comprobante"));
        assertEquals("1||1001:UI_DOCUMENTO_ASOCIADO_NO_LIQUIDADO|ROLLBACK", salida("orden-comprobante-gana-a-liquidacion"));
        assertEquals("1||2002:UI_VALIDACION_LIQUIDACION_FONDO_MUTUO|ROLLBACK", salida("orden-liquidacion-gana-a-asociada"));
        assertEquals("1||2002:UI_SOLICITUD_ASOCIADA_NO_CERTIFICADA|ROLLBACK", salida("orden-asociada-gana-a-tgr"));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-14 count and early exit
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-14 no solicitud passes: counter 1, one row per solicitud with its code, the certification log is written (and rolled back by the host); the log has the 22 columns of the legacy")
    void fmCer14_salidaTemprana() throws Exception {
        assertEquals("1||1004:UI_ONMIBUS_NO_CREADO,1001:UI_ESTADO_INCORRECTO_CERTIFICACION,1002:UI_FECHA_INVALIDA_CERTIFICACION_FONDO_MUTUO,1003:UI_NO_EXISTE_VALOR_CUOTA|ROLLBACK",
                salida("cada-solicitud-con-su-codigo-en-la-salida-temprana"));
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug("salida-temprana-simulacion-N-escribe-el-log-y-revierte"));
        String log = "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate";
        assertEquals(2, c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(log).size(), "the log is in the session when the class returns");
        assertEquals(0, c.volcado.getAsJsonObject("tablas").getAsJsonArray(log).size(), "the host rolls it back (counter 1)");
        JsonObject fila = c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray(log).get(0).getAsJsonObject();
        assertEquals("UI_ESTADO_INCORRECTO_CERTIFICACION", fila.get("Codigo").getAsString());
        assertEquals("RZD", fila.get("IdEstado").getAsString());
        assertEquals("RES", fila.get("TipoSolicitud").getAsString());
        assertEquals("RESNORM", fila.get("IdTipoMovimientoFondoMutuo").getAsString());
        assertEquals("2026-03-02", fila.get("FechaSistema").getAsString());
        assertEquals("N", fila.get("Simulacion").getAsString());
        assertEquals("[{\"secuencia\":\"BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate\",\"cantidad\":2}]", c.volcado.get("secuenciasSolicitadas").toString());
    }

    @Test
    @DisplayName("FM-CER-14 / D-F3B-01 with p_SimularOperacion S the early exit is the same (counter 1, log with Simulacion S); the log of a null amount / quota is written with 0")
    void fmCer14_simulacionYNulos() throws Exception {
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug("contrato-simulacion-S-sale-temprano"));
        assertEquals("S", c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate").get(0).getAsJsonObject().get("Simulacion").getAsString());
        assertEquals("1||1001:UI_ESTADO_INCORRECTO_CERTIFICACION|ROLLBACK", salida("salida-temprana-con-cifras-nulas-en-el-log"));
        JsonObject fila = CasoCer.ejecutar(CasoCer.idPorSlug("salida-temprana-con-cifras-nulas-en-el-log")).volcado.getAsJsonObject("tablasTrasLaLlamada")
                .getAsJsonArray("BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate").get(0).getAsJsonObject();
        assertEquals(0, fila.get("ValorCuota").getAsBigDecimal().compareTo(BigDecimal.ZERO));
        assertEquals(0, fila.get("NumeroCuotas").getAsBigDecimal().compareTo(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal("1234.56789012").compareTo(fila.get("Monto").getAsBigDecimal()));
    }

    @Test
    @DisplayName("FM-CER-14 the sequence of the log is consumed by every early exit of the SAME session (the ids go on: 3 and 4 after 1 and 2)")
    void fmCer14_dosSalidasEnLaMismaSesion() throws Exception {
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug("salida-temprana-dos-veces-en-la-misma-sesion"));
        assertEquals("[{\"secuencia\":\"BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate\",\"cantidad\":1},{\"secuencia\":\"BSCL.FondoMutuo.BD.Secuencias::LogCertificacionAporteRescate\",\"cantidad\":1}]",
                c.volcado.get("secuenciasSolicitadas").toString());
        assertEquals(2L, c.volcado.getAsJsonObject("tablasTrasLaLlamada").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate").get(0).getAsJsonObject().get("IdLogCertificacionAporteRescate").getAsLong());
    }

    @Test
    @DisplayName("FM-CER-14 / FM-CER-67 a state of the solicitud that is not in PLA.Estado makes the output throw NullPointerException (RZD missing)")
    void fmCer14_estadoSinFila() throws Exception {
        assertTrue(excepcion("salida-temprana-estado-sin-fila-en-pla-estado-lanza").startsWith("NullPointerException:"));
    }

    @Test
    @DisplayName("FM-CER-14 / FM-CER-69 one valid and one rejected: the facade goes on (the rejected one is only in the output); a foreign TipoSolicitud passes every rule (D-F3B-16)")
    void fmCer14_loteMixtoYTipoAjeno() throws Exception {
        CertificarAporteRescateContexto c = ctx("lote-mixto-una-valida-y-una-rechazada");
        assertEquals("PEN", sol(c, 1001).EstadoProceso);
        assertEquals("RZD", sol(c, 1002).EstadoProceso);
        assertEquals("UI_ESTADO_INCORRECTO_CERTIFICACION", sol(c, 1002).Codigo);
        assertEquals(List.of(1001L), ids(c.rescates));
        CertificarAporteRescateContexto a = ctx("tipo-de-solicitud-ajeno-pasa-las-reglas");
        assertEquals("PEN", sol(a, 1001).EstadoProceso);
        assertTrue(a.aportes.isEmpty() && a.rescates.isEmpty(), "a foreign type is in neither list (D-F3B-16)");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-15 / 16 / 17 indicators, recalculation of quotas and amounts
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-15 indicators of portfolio: one per portfolio, only of the pending solicitudes, a repeated portfolio in the view raises 'key field is not unique' (D-NOR-9)")
    void fmCer15_indicadores() throws Exception {
        CertificarAporteRescateContexto c = ctx("indicadores-sobregiro-S-saca-el-portafolio-del-flujo");
        assertEquals("S", c.indicadoresPortafolio.get(500L).OperaLineaSobregiro);
        assertEquals("N", c.indicadoresPortafolio.get(501L).OperaLineaSobregiro);
        assertNull(c.indicadoresPortafolio.get(502L), "no row = N when the cash flow is asked");
        assertEquals(2, c.indicadoresPortafolio.size());
        assertNull(ctx("indicadores-de-portafolio-ajeno-no-influyen").indicadoresPortafolio.get(777L), "the portfolio 777 has no pending solicitud");
        assertNull(ctx("indicadores-solo-de-las-solicitudes-vigentes").indicadoresPortafolio.get(501L), "the only solicitud of portfolio 501 was rejected (CRT)");
    }

    @Test
    @DisplayName("FM-CER-16 / 17 amounts and quotas of a rescate by AMOUNT: 1.000.000 CLP at 1500.1234 -> 666.6118 quotas; an aporte of 2.500.000 -> 1666.5296; the UF cost comes from the calculation")
    void fmCer16_porMonto() throws Exception {
        CertificarAporteRescateContexto c = ctx("calculo-por-monto-rescate-y-aporte");
        SolicitudProceso r = sol(c, 3001);
        igual("666.6118", r.NumeroCuotas, "NumeroCuotas");
        igual("1500.1234", r.ValorCuota, "ValorCuota");
        igual("1000000", r.Monto, "Monto");
        igual("1000000", r.MontoPago, "MontoPago");
        igual("1000000", r.MontoPagoLocal, "MontoPagoLocal");
        igual("1000000", r.CostoMonedaLocal, "CostoMonedaLocal");
        igual("1000000", r.MontoMonedaSistema, "MontoMonedaSistema");
        assertEquals("CLP", r.IdMonedaSistema);
        SolicitudProceso a = sol(c, 3002);
        igual("1666.5296", a.NumeroCuotas, "NumeroCuotas aporte");
        igual("2500000", a.Monto, "Monto aporte");
        igual("64.1017", a.CostoUF, "CostoUF aporte (amount in UF of the calculation)");
        igual("1", r.ParidadFechaCertificacion, "ParidadFechaCertificacion (same currency)");
    }

    @Test
    @DisplayName("FM-CER-16 by QUOTAS: 250.5 quotas at 1500.1234 -> amount 375781 (rounded to the 0 decimals of CLP); by TOTAL: the quotas of the request are kept")
    void fmCer16_porCuotasYTotal() throws Exception {
        SolicitudProceso c = sol(ctx("calculo-por-cuotas"), 3001);
        igual("250.5", c.NumeroCuotas, "NumeroCuotas");
        igual("375781", c.Monto, "Monto");
        igual("375781", c.CostoMonedaLocal, "CostoMonedaLocal");
        SolicitudProceso t = sol(ctx("calculo-por-total"), 3001);
        igual("100.25", t.NumeroCuotas, "NumeroCuotas total");
    }

    @Test
    @DisplayName("FM-CER-16 the previous amounts, quotas and value of the solicitud are REPLACED by the calculation (999.9999 quotas and 1 of amount become 199.9835 quotas and 300000)")
    void fmCer16_reemplazaLosValoresPrevios() throws Exception {
        SolicitudProceso s = sol(ctx("calculo-con-cuotas-y-monto-previos-distintos"), 3001);
        igual("199.9835", s.NumeroCuotas, "NumeroCuotas");
        igual("300000", s.Monto, "Monto");
        igual("1500.1234", s.ValorCuota, "ValorCuota");
        igual("300000", s.MontoPago, "MontoPago");
        igual("39000.5", s.ValorUFFechaInversion, "ValorUFFechaInversion: the base value 6 is replaced by the factor of the calculation");
        igual("7.6922", s.CostoUF, "CostoUF: the base value 3 is replaced by the amount in UF of the calculation (300000 / 39000.5 of the scripted CalcularParidad)");
    }

    @Test
    @DisplayName("FM-CER-16 a fund in USD with an investment in CLP (deferred): amount 5.000.000 CLP / 910 = 5494.51 USD, 542.753 quotas; a CLP fund with a USD investment of 2000: 1.800.000 CLP, 1199.9013 quotas")
    void fmCer16_diferidos() throws Exception {
        CertificarAporteRescateContexto c = ctx("calculo-fondo-usd-inversion-clp-diferido");
        SolicitudProceso a = sol(c, 3001);
        igual("5494.51", a.Monto, "Monto USD");
        igual("542.753", a.NumeroCuotas, "NumeroCuotas");
        igual("10.1234", a.ValorCuota, "ValorCuota USD");
        igual("5000000", a.CostoMonedaLocal, "CostoMonedaLocal CLP");
        igual("910", a.ParidadFechaCertificacion, "ParidadFechaCertificacion = dollar of the contributions (the fund is USD)");
        SolicitudProceso r = sol(c, 3002);
        igual("4419.89", r.Monto, "Monto USD rescate");
        igual("905", r.ParidadFechaCertificacion, "dollar of the redemptions");
        SolicitudProceso d = sol(ctx("calculo-fondo-clp-inversion-usd"), 3001);
        igual("1800000", d.Monto, "Monto CLP");
        igual("1199.9013", d.NumeroCuotas, "NumeroCuotas");
        igual("2000", d.CostoMonedaLocal, "CostoMonedaLocal USD");
    }

    @Test
    @DisplayName("FM-CER-16 a price of 0 by amount divides by zero (ArithmeticException, D-CUO-1); by quotas it rejects ERROR_CALCULO_MONTO_CUOTA and the rest of the lot goes on; a series without row: all rejected, or NullPointerException if another is valid")
    void fmCer16_errores() throws Exception {
        assertTrue(excepcion("calculo-precio-cero-por-monto-lanza").startsWith("ArithmeticException:"));
        CertificarAporteRescateContexto c = ctx("calculo-precio-cero-por-cuotas-rechaza");
        assertEquals("RZD", sol(c, 3001).EstadoProceso);
        assertEquals("ERROR_CALCULO_MONTO_CUOTA", sol(c, 3001).Codigo);
        assertEquals(List.of(3002L), ids(c.rescates));
        CertificarAporteRescateContexto s = ctx("calculo-todas-las-series-sin-registro-rechaza");
        assertEquals("ERROR_CALCULO_MONTO_CUOTA", sol(s, 3001).Codigo);
        assertEquals("ERROR_CALCULO_MONTO_CUOTA", sol(s, 3002).Codigo);
        assertTrue(s.aportes.isEmpty() && s.rescates.isEmpty());
        assertTrue(excepcion("calculo-una-serie-sin-registro-y-otra-valida-lanza").startsWith("NullPointerException:"));
        assertTrue(excepcion("calculo-por-cuotas-con-numero-de-cuotas-nulo-lanza").startsWith("NullPointerException:"));
        CertificarAporteRescateContexto n = ctx("calculo-sin-resultado-para-la-solicitud-rechaza-a-todas");
        assertEquals("ERROR_CALCULO_MONTO_CUOTA", sol(n, 3001).Codigo);
        assertEquals("ERROR_CALCULO_MONTO_CUOTA", sol(n, 3002).Codigo);
        assertEquals("Exception: formato no encontrado moneda JPY uso GEN tipo instrumento  tipo operacion ", excepcion("formato-moneda-sin-fila-de-formato"));
    }

    @Test
    @DisplayName("FM-CER-17 a solicitud with a related RESCNN commission keeps the amounts of the database (no replacement); with a RESCNN of ANOTHER solicitud it is recalculated")
    void fmCer17_comisionNnRelacionada() throws Exception {
        SolicitudProceso k = sol(ctx("comision-nn-relacionada-conserva-los-montos-de-la-base"), 3001);
        assertEquals("S", k.TieneComisionNNRelacionada);
        igual("123456.789", k.Monto, "Monto kept");
        igual("55.5555", k.NumeroCuotas, "NumeroCuotas kept");
        igual("2222.2222", k.ValorCuota, "ValorCuota kept");
        igual("123400.4", k.CostoMonedaLocal, "CostoMonedaLocal kept");
        igual("100000.5", k.MontoPago, "MontoPago kept");
        igual("3.25", k.CostoUF, "CostoUF kept");
        SolicitudProceso o = sol(ctx("comision-nn-de-otra-solicitud-no-cuenta"), 3001);
        assertEquals("N", o.TieneComisionNNRelacionada);
        igual("666.6118", o.NumeroCuotas, "NumeroCuotas recalculated");
    }

    @Test
    @DisplayName("FM-CER-17 APOREINV keeps CostoUF (12.5) and ValorUFFechaInversion (39000.25); a normal aporte takes them from the calculation; an aporte with a Spot keeps the system amount (777000.5)")
    void fmCer17_costoUfYSpot() throws Exception {
        SolicitudProceso r = sol(ctx("aporte-reinversion-conserva-costo-uf-y-valor-uf"), 3001);
        igual("12.5", r.CostoUF, "CostoUF");
        igual("39000.25", r.ValorUFFechaInversion, "ValorUFFechaInversion");
        SolicitudProceso n = sol(ctx("aporte-normal-toma-costo-uf-del-calculo"), 3001);
        igual("25.6407", n.CostoUF, "CostoUF of the calculation");
        igual("39000.5", n.ValorUFFechaInversion, "ValorUFFechaInversion of the calculation");
        CertificarAporteRescateContexto s = ctx("aporte-con-spot-conserva-monto-moneda-sistema");
        igual("777000.5", sol(s, 3001).MontoMonedaSistema, "aporte with Spot keeps the amount");
        igual("1000000", sol(s, 3002).MontoMonedaSistema, "aporte without Spot is recalculated");
        igual("1000000", sol(ctx("rescate-con-spot-no-conserva-monto-moneda-sistema"), 3001).MontoMonedaSistema, "a rescate is always recalculated");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-18 / 19 / 20 date, partition and order, GEN_OCSNEG
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-18 effective date: system 2026-03-02 and request 2026-02-27 -> 2026-03-02; request 2026-03-05 -> 2026-03-05; equal -> the same")
    void fmCer18_fechaEfectiva() throws Exception {
        assertEquals("2026-03-02", ctx("fecha-efectiva-sistema-si-la-entrada-es-anterior").fechaCertificacion.toString());
        assertEquals("2026-03-05", ctx("fecha-efectiva-entrada-si-es-posterior").fechaCertificacion.toString());
        assertEquals("2026-03-02", ctx("fecha-efectiva-igual-a-la-de-sistema").fechaCertificacion.toString());
        assertEquals("2026-03-10", ctx("fecha-efectiva-lote-con-fecha-de-sistema-cambiada").fechaCertificacion.toString());
        assertEquals("2026-03-02", ctx("fecha-de-entrada-igual-a-la-de-la-solicitud-en-otro-dia").fechaCertificacion.toString());
    }

    @Test
    @DisplayName("FM-CER-19 partition: aportes and rescates apart, each ascending by id, only the pending ones; a foreign type in neither; the rejected ones out")
    void fmCer19_particion() throws Exception {
        CertificarAporteRescateContexto c = ctx("particion-aportes-y-rescates-desordenados");
        assertEquals(List.of(3001L, 3002L), ids(c.aportes));
        assertEquals(List.of(3003L, 3004L, 3005L), ids(c.rescates));
        CertificarAporteRescateContexto d = ctx("particion-ids-en-orden-descendente-de-entrada");
        assertEquals(List.of(3004L, 3006L, 3007L), ids(d.aportes));
        assertEquals(List.of(3005L, 3008L, 3009L), ids(d.rescates));
        CertificarAporteRescateContexto e = ctx("particion-tipo-ajeno-no-entra-en-ninguna-lista");
        assertEquals(List.of(3002L), ids(e.aportes));
        assertEquals(List.of(3003L), ids(e.rescates));
        assertEquals(List.of(10L, 2147483648L, 2147483649L, 2147483650L), ids(ctx("particion-ids-de-muchos-digitos-y-comparador").rescates));
        CertificarAporteRescateContexto f = ctx("particion-una-solicitud-es-rechazada-por-calculo-y-no-entra");
        assertEquals(List.of(3001L), ids(f.aportes), "3003 was rejected by the calculation (price 0 by quotas)");
        CertificarAporteRescateContexto g = ctx("particion-doce-solicitudes-mixtas-y-rechazadas");
        assertEquals(List.of(3001L, 3003L, 3009L), ids(g.aportes), "3002 (date) and 3005 (omnibus) were rejected by F-A");
        assertEquals(List.of(3004L, 3007L, 3008L, 3010L, 3012L), ids(g.rescates), "3011 (state) was rejected; 3006 has a foreign type");
    }

    @Test
    @DisplayName("FM-CER-19 a lot of thirty solicitudes with crossed ids: three of every ten are aportes, the ids come out ascending, the ones in state CRT are out")
    void fmCer19_treintaSolicitudes() throws Exception {
        CertificarAporteRescateContexto c = ctx("lote-de-treinta-solicitudes-con-ids-cruzados");
        List<Long> aportes = new ArrayList<>();
        List<Long> rescates = new ArrayList<>();
        for (long i = 0; i < 30; i++) {
            if (i % 11 == 5) continue;
            (i % 3 == 0 ? aportes : rescates).add(4000 + i);
        }
        assertEquals(aportes, ids(c.aportes));
        assertEquals(rescates, ids(c.rescates));
        assertEquals(30, c.solicitudes.size());
    }

    @Test
    @DisplayName("FM-CER-20 OperaSaldoNegativo (GEN_OCSNEG): S -> S, N -> N, no row -> N, 'n' -> n, another value -> as is; the FIRST indicator of the portfolio wins; PAC aportes too")
    void fmCer20_operaSaldoNegativo() throws Exception {
        CertificarAporteRescateContexto c = ctx("opera-saldo-negativo-S-N-ausente-minuscula-y-otro");
        assertEquals("S", sol(c, 3001).OperaSaldoNegativo);
        assertEquals("N", sol(c, 3002).OperaSaldoNegativo);
        assertEquals("N", sol(c, 3003).OperaSaldoNegativo, "no indicator row: the default of the pojo");
        assertEquals("n", sol(c, 3004).OperaSaldoNegativo);
        assertEquals("X", sol(c, 3005).OperaSaldoNegativo);
        CertificarAporteRescateContexto p = ctx("opera-saldo-negativo-gana-el-primer-indicador-del-portafolio");
        assertEquals("S", sol(p, 3001).OperaSaldoNegativo);
        assertEquals("N", sol(p, 3002).OperaSaldoNegativo);
        CertificarAporteRescateContexto pac = ctx("opera-saldo-negativo-aporte-pac");
        assertEquals("S", sol(pac, 3001).OperaSaldoNegativo);
        assertEquals("N", sol(pac, 3002).OperaSaldoNegativo);
        assertEquals("N", sol(ctx("opera-saldo-negativo-se-consulta-aunque-no-haya-aportes"), 3001).OperaSaldoNegativo, "a rescate keeps the default N of the pojo");
    }

    @Test
    @DisplayName("FM-CER-21 / D-NOR-16 the pairs portfolio/currency that go to the cash flow are built with two INDEPENDENT lists: (500,USD) and (501,CLP) are left out and their aportes are rejected by the balance")
    void fmCer21_paresDelFlujo() throws Exception {
        CertificarAporteRescateContexto c = ctx("pares-portafolio-moneda-omitidos-por-listas-independientes");
        List<String[]> pares = CerCostura.paresFlujoCaja(c, false);
        assertEquals(2, pares.size());
        assertEquals("500", pares.get(0)[0]);
        assertEquals("CLP", pares.get(0)[1]);
        assertEquals("501", pares.get(1)[0]);
        assertEquals("USD", pares.get(1)[1]);
        assertEquals(java.util.Set.of(3003L, 3004L), CerCostura.rechazadosPorSaldo(c, CasoCer.golden(CasoCer.idPorSlug("pares-portafolio-moneda-omitidos-por-listas-independientes")).getAsJsonArray("colaboradores")));
        assertEquals(1, CerCostura.paresFlujoCaja(ctx("pac-y-no-pac-del-mismo-portafolio-dos-llamadas-al-flujo"), true).size());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-18 / 19 / 23 the pure rules of F-B on their own
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-CER-18 pure: fechaCertificacionEfectiva('2026-03-02', '2026-02-27') = 2026-03-02; ('2026-03-02', '2026-03-05') = 2026-03-05; a 19 character text is a date and time")
    void fmCer18_pura() {
        assertEquals("2026-03-02", CertificarAporteRescateReglas.fechaCertificacionEfectiva("2026-03-02", "2026-02-27").toString());
        assertEquals("2026-03-05", CertificarAporteRescateReglas.fechaCertificacionEfectiva("2026-03-02", "2026-03-05").toString());
        assertEquals("2026-03-02", CertificarAporteRescateReglas.fechaCertificacionEfectiva("2026-03-02", "2026-03-02").toString());
        assertEquals("2026-03-05", CertificarAporteRescateReglas.fechaCertificacionEfectiva("2026-03-02", "2026-03-05 00:00:00").toString());
    }

    @Test
    @DisplayName("FM-CER-19 pure: porTipoOrdenadas keeps PEN and ADV of the type, ascending by id (3 solicitudes -> [1, 5, 9]); RZD, APD and another type are out")
    void fmCer19_pura() {
        List<SolicitudProceso> l = new ArrayList<>();
        l.add(solicitud(9L, "RES", "PEN"));
        l.add(solicitud(3L, "RES", "RZD"));
        l.add(solicitud(5L, "RES", "ADV"));
        l.add(solicitud(1L, "RES", "PEN"));
        l.add(solicitud(2L, "APO", "PEN"));
        l.add(solicitud(7L, "RES", "APD"));
        l.add(solicitud(4L, "XXX", "PEN"));
        assertEquals(List.of(1L, 5L, 9L), ids(CertificarAporteRescateReglas.porTipoOrdenadas(l, "RES")));
        assertEquals(List.of(2L), ids(CertificarAporteRescateReglas.porTipoOrdenadas(l, "APO")));
        assertEquals(List.of(), ids(CertificarAporteRescateReglas.porTipoOrdenadas(new ArrayList<>(), "RES")));
    }

    private static SolicitudProceso solicitud(Long id, String tipo, String estado) {
        SolicitudProceso s = new SolicitudProceso();
        s.IdSolicitudAporteRescate = id;
        s.TipoSolicitud = tipo;
        s.EstadoProceso = estado;
        return s;
    }

    @Test
    @DisplayName("FM-CER-23 pure: the 11 amounts take the decimals of the currency of the fund (USD 2, EUR 3 truncated), of the investment (CLP 0) and of the system (CLP 0); null becomes 0")
    void fmCer23_formato() throws Exception {
        List<Map<String, Object>> filas = new ArrayList<>();
        filas.add(fila("CLP", 0, "R"));
        filas.add(fila("USD", 2, "R"));
        filas.add(fila("EUR", 3, "T"));
        FormatoMoneda f = new FormatoMoneda(new CalendarioPort() {
            @Override
            public List<Map<String, Object>> feriados(List<Long> idPaises) {
                return List.of();
            }

            @Override
            public List<Map<String, Object>> monedaFormato() {
                return filas;
            }

            @Override
            public java.sql.Timestamp fechaHoraSistemaLocal(Long idEmpresa) {
                return null;
            }
        });
        SolicitudProceso s = new SolicitudProceso();
        s.IdMonedaFondo = "USD";
        s.IdMoneda = "CLP";
        s.IdMonedaSistema = "CLP";
        s.Monto = new BigDecimal("1234.5678");
        s.MontoComision = new BigDecimal("12.3456");
        s.MontoPago = new BigDecimal("0.005");
        s.MontoLiquido = null;
        s.MontoRetencion = new BigDecimal("1.994");
        s.CostoMonedaLocal = new BigDecimal("1234567.5");
        s.MontoComisionLocal = new BigDecimal("98.765");
        s.MontoPagoLocal = new BigDecimal("0.4999");
        s.MontoLiquidoLocal = new BigDecimal("99.5");
        s.MontoRetencionLocal = new BigDecimal("2.5");
        s.MontoMonedaSistema = new BigDecimal("777000.5");
        CertificarAporteRescateReglas.formatearMontos(f, List.of(s));
        igual("1234.57", s.Monto, "Monto USD");
        igual("12.35", s.MontoComision, "MontoComision USD");
        igual("0.01", s.MontoPago, "MontoPago USD (half up)");
        igual("0", s.MontoLiquido, "null -> 0");
        igual("1.99", s.MontoRetencion, "MontoRetencion USD");
        igual("1234568", s.CostoMonedaLocal, "CostoMonedaLocal CLP (half up)");
        igual("99", s.MontoComisionLocal, "MontoComisionLocal CLP");
        igual("0", s.MontoPagoLocal, "MontoPagoLocal CLP");
        igual("100", s.MontoLiquidoLocal, "MontoLiquidoLocal CLP");
        igual("3", s.MontoRetencionLocal, "MontoRetencionLocal CLP");
        igual("777001", s.MontoMonedaSistema, "MontoMonedaSistema CLP");
        SolicitudProceso t = new SolicitudProceso();
        t.IdMonedaFondo = "EUR";
        t.IdMoneda = "EUR";
        t.IdMonedaSistema = "EUR";
        t.Monto = new BigDecimal("12.34567");
        t.MontoComision = new BigDecimal("-0.0009");
        CertificarAporteRescateReglas.formatearMontos(f, List.of(t));
        igual("12.345", t.Monto, "EUR truncates (T) to 3 decimals");
        igual("-0.001", t.MontoComision, "EUR truncation is a FLOOR: a small negative goes down to -0.001");
        SolicitudProceso u = new SolicitudProceso();
        u.IdMonedaFondo = "JPY";
        u.IdMoneda = "CLP";
        u.IdMonedaSistema = "CLP";
        assertEquals("formato no encontrado moneda JPY uso GEN tipo instrumento  tipo operacion ", org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> CertificarAporteRescateReglas.formatearMontos(f, List.of(u))).getMessage());
    }

    private static Map<String, Object> fila(String moneda, int decimales, String trunca) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("IdMoneda", moneda);
        m.put("IdUsoMonedaFormato", "GEN");
        m.put("IdTipoInstrumento", "");
        m.put("IdTipoOperacion", "");
        m.put("MontoDecimales", decimales);
        m.put("MontoTrucaRedondea", trunca);
        return m;
    }

    @Test
    @DisplayName("FM-CER-23 on the amounts that the UPDATE writes: commissions 1234.5678 CLP -> 1235 and 98.765 -> 99; an aporte of 0.5 -> 1 (half up); the USD fund 12.3456 -> 12.35; EUR 12.34567 -> 12.345 (truncated)")
    void fmCer23_enLasFilasEscritas() throws Exception {
        JsonObject g = CasoCer.golden(CasoCer.idPorSlug("formato-comisiones-clp-sin-decimales"));
        com.google.gson.JsonArray t = g.getAsJsonObject("tablas").getAsJsonArray("BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate");
        assertEquals(1235, t.get(0).getAsJsonObject().get("MontoComision").getAsInt());
        assertEquals(99, t.get(0).getAsJsonObject().get("MontoComisionLocal").getAsInt());
        assertEquals(1, t.get(1).getAsJsonObject().get("MontoComision").getAsInt());
        assertEquals(2, t.get(1).getAsJsonObject().get("MontoComisionLocal").getAsInt());
        CasoCer.Corrida c = CasoCer.ejecutar(CasoCer.idPorSlug("formato-fondo-usd-dos-decimales-y-moneda-de-inversion-clp-parada"));
        SolicitudProceso f = c.formateadas.get(3001L);
        igual("12.35", f.MontoComision, "USD fund");
        igual("11112", f.MontoComisionLocal, "CLP investment");
        CasoCer.Corrida e = CasoCer.ejecutar(CasoCer.idPorSlug("calculo-fondo-eur-inversion-clp-por-paridad-y-formato-truncado-parada"));
        igual("12.345", e.formateadas.get(3001L).MontoComision, "EUR truncated");
        igual("0", CasoCer.ejecutar(CasoCer.idPorSlug("comision-nn-con-monto-nulo-en-la-vista-parada")).formateadas.get(3001L).Monto, "a null amount of the view");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the generated index against the new code
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("the index generated from the goldens (CerTablasLiterales) agrees with the new code: every TERMINAL case and the output rows of every `completo` case up to what F-A / F-B decide")
    void elIndiceGeneradoCoincideConLoQueDevuelveElCodigoNuevo() throws Exception {
        int n = 0;
        for (String linea : CerTablasLiterales.TERMINALES.strip().split("\n")) {
            String[] p = linea.strip().split("\\|", -1);
            if (p.length < 5) continue;
            String slug = p[0];
            if (p[2].equals("OUT")) {
                assertEquals(p[3] + "|" + p[4] + "|" + p[5].replace(":", ":") + "|" + p[6], salida(slug), slug);
            } else {
                String e = excepcion(slug);
                assertTrue(e.startsWith(p[3] + ":") || CasoCer.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(slug), slug + " expected " + p[3] + " got " + e);
            }
            n++;
        }
        assertTrue(n >= 80, "terminal cases in the index: " + n);
        int m = 0;
        for (String linea : CerTablasLiterales.CONTINUAN.strip().split("\n")) {
            String[] p = linea.strip().split("\\|", -1);
            if (p.length < 3) continue;
            CertificarAporteRescateContexto c = ctx(p[0]);
            for (String fila : p[2].split(",")) {
                if (fila.isBlank()) continue;
                String[] f = fila.split(":", 3);
                long id = Long.parseLong(f[0]);
                if (id == 0) continue;
                SolicitudProceso s = sol(c, id);
                if (f[1].equals("RZD") && !f[2].equals("UI_SALDO_INSUFICIENTE_FLUJO_CAJA")) {
                    assertEquals("RZD", s.EstadoProceso, p[0] + " " + id);
                    assertEquals(f[2], s.Codigo, p[0] + " " + id);
                } else {
                    assertFalse("RZD".equals(s.EstadoProceso), p[0] + " " + id + " is pending after F-A / F-B (the legacy then " + f[1] + ":" + f[2] + ")");
                }
            }
            m++;
        }
        assertTrue(m >= 90, "completo cases in the index: " + m);
    }
}
