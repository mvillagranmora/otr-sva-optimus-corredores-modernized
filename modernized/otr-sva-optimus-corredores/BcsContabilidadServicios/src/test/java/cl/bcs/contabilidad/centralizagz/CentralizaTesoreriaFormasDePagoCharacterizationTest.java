package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaTesoreriaFormasDePago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.centralizagz.Fx.*;
import static cl.bcs.contabilidad.centralizagz.Spec.Formato.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * WP-F characterization of CentralizaTesoreriaFormasDePago.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaTesoreriaFormasDePago.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaTesoreriaFormasDePagoCharacterizationTest {

    static final String MOV = "CentralizaTesoreriaFormasDePago_SelectMovimientos.sql";
    static final String EVE = "CentralizaTesoreriaFormasDePago_SelectEventos.sql";
    static final String DAT = "CentralizaTesoreriaFormasDePago_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaTesoreriaFormasDePago", CentralizaTesoreriaFormasDePago::new,
            List.of(
                    // legacy CentralizaTesoreriaFormasDePago.java:145-151
                    Spec.consulta("CentralizaTesoreriaFormasDePago_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO, "LocSistema", LOC_SISTEMA)),
                    // legacy CentralizaTesoreriaFormasDePago.java:236-242
                    Spec.consulta("CentralizaTesoreriaFormasDePago_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO, "LocSistema", LOC_SISTEMA)),
                    // legacy CentralizaTesoreriaFormasDePago.java:133,265-270
                    Spec.consulta("CentralizaTesoreriaFormasDePago_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "FechaSistema", FECHA_CONTABLE, "LocSistema", LOC_SISTEMA)),
                    // legacy CentralizaTesoreriaFormasDePago.java:281
                    Spec.consulta("CentralizaTesoreria_SelectDatosMovimiento_Documentos_Partidas.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "FechaSistema", FECHA_CONTABLE, "LocSistema", LOC_SISTEMA)),
                    // legacy CentralizaTesoreriaFormasDePago.java:282
                    Spec.consulta("CentralizaTesoreria_SelectDatosMovimiento_Solicitudes_Partidas.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "FechaSistema", FECHA_CONTABLE, "LocSistema", LOC_SISTEMA))),
            // legacy CentralizaTesoreriaFormasDePago.java:307-361 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("NombreCuentaCorriente", "NombreCuentaCorriente", STR),
                    Spec.campo("TipoDocumento", "TipoDocumento", STR),
                    Spec.campo("Folio", "Folio", VALUEOF_LONG),
                    Spec.campo("FechaDocumento", "FechaDocumento", STR),
                    Spec.campo("NumeroCuentaCorriente", "NumeroCuentaCorriente", STR),
                    Spec.campo("MedioDePago", "MedioDePago", STR),
                    Spec.campo("NumeroDocumentoPago", "NumeroDocumentoPago", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("FechaDocumentoPago", "FechaDocumentoPago", TS_O_DEFECTO),
                    Spec.campo("DocumentoIdentidadCAVALI", "DocumentoIdentidadCAVALI", STR),
                    Spec.campo("SerieDocumento", "SerieDocumento", STR),
                    Spec.campo("Concepto", "Concepto", STR),
                    Spec.campo("NombreConcepto", "NombreConcepto", STR),
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("CodigoCaja", "CodigoCaja", STR),
                    Spec.campo("EmitidoA", "EmitidoA", STR),
                    Spec.campo("EsBilateral", "EsBilateral", STR),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("FechaProceso", "FechaProceso", STR),
                    Spec.campo("Proceso", "Proceso", STR),
                    Spec.campo("TipoComprobante", "TipoComprobante", STR),
                    Spec.campo("Valuta", "Valuta", STR),
                    Spec.campo("PartidaLiquidacion", "PartidaLiquidacion", STR),
                    Spec.campo("NumeroComprobante", "NumeroComprobante", VALUEOF_LONG),
                    Spec.campo("AliasBancoCaja", "AliasBancoCaja", STR),
                    Spec.campo("Monto", "Monto", DECIMAL),
                    Spec.campo("TipoComprobanteAdj", "TipoComprobanteAdj", STR),
                    Spec.campo("FolioComprobanteAdj", "FolioComprobanteAdj", VALUEOF_LONG),
                    Spec.campo("FolioOperacion", "FolioOperacion", VALUEOF_LONG),
                    Spec.campo("CodigoProducto", "CodigoProducto", STR),
                    Spec.campo("Nemotecnico", "Nemotecnico", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Valor", "Valor", DECIMAL),
                    Spec.campo("TipoCambio", "TipoCambio", DECIMAL),
                    Spec.campo("EsContraparte", "EsContraparte", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("ComprobanteTipo", "ComprobanteTipo", STR),
                    Spec.campo("FechaMovimiento", "FechaMovimiento", DATE),
                    Spec.campo("MontoMonedaSistema", "MontoMonedaSistema", DECIMAL),
                    Spec.campo("Referencia3", "Referencia3", VALUEOF_LONG),
                    Spec.campo("EsProveedor", "EsProveedor", VALUEOF_STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR),
                    Spec.campo("CondicionLiquidacion", "CondicionLiquidacion", STR),
                    Spec.campo("FolioDTE", "FolioDTE", VALUEOF_LONG)));

    // legacy CentralizaTesoreriaFormasDePago.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-194: los SQL legacy del tipo existen en el classpath")
    void contF194_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaTesoreriaFormasDePago.java:145-151,236-242,133,265-270,281,282
    @Test
    @DisplayName("CONT-F-195: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF195_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaTesoreriaFormasDePago.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-196: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF196_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaTesoreriaFormasDePago.java:145-151,236-242 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-197: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF197_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaTesoreriaFormasDePago.java:307-361
    @Test
    @DisplayName("CONT-F-198: TabDatosMovimiento: 53 TipoDato por fila, orden y formato legacy")
    void contF198_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(53 * 2 * 3);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-08");
    }

    // legacy CentralizaTesoreriaFormasDePago.java:307-361 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-199: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF199_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaTesoreriaFormasDePago.java:307-361 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-200: una fecha nula aborta la centralizacion del tipo")
    void contF200_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaTesoreriaFormasDePago.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-201: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF201_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-202: simulaOperacion N persiste una vez las tablas producidas")
    void contF202_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-203: simulaOperacion S no persiste")
    void contF203_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-204: consultas vacias producen salida vacia sin errores")
    void contF204_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


    // Legacy CentralizaTesoreriaFormasDePago.java:136-142 reads module "CONTABILIDAD", parameter "CalculaParidadCLPCentralizacionTesoreria" (default "N" on error),
    // and :154-226, 244-260, 287-304, 371-484 convert every non-CLP amount to CLP: factor = TipoCambioMercado of "DO" for USD,
    // of the currency itself otherwise; MontoDestino = MontoOrigen * factor (DECIMAL128) then setScale(0, HALF_UP).
    @Test
    @DisplayName("CONT-F-205: CalculaParidadCLPCentralizacionTesoreria=S convierte montos no CLP a CLP con HALF_UP escala 0 (legacy :136-304,371-484)")
    void contF205_paridadConvierteMontosNoClp() {
        Escenario e = escenarioParidad();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        // 25 USD * 0.5 = 12.5 -> 13 ; 1000 CLP unchanged ; 10 EUR * 1.05 = 10.50 -> 11 (HALF_EVEN would give 10)
        assertThat(s.tabMovimientos()).extracting(m -> m.monto().toPlainString()).containsExactly("13", "1000.00", "11");
        assertThat(s.tabEventos()).extracting(ev -> ev.idEvento() + "=" + ev.montoEvento().toPlainString())
                .containsExactly("SOLICITUD_MONTO_INGRESO=13", "OTRO_EVENTO=25.00", "DOCUMENTO_MONTO_EGRESO=11",
                        "SOLICITUD_MONTO_INGRESO=1000.00");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.idMovimiento() == 8001L
                        && (d.tipoDato().equals("Monto") || d.tipoDato().equals("Valor")))
                .extracting(d -> d.tipoDato() + "=" + d.valorDecimal().toPlainString())
                .containsExactly("Monto=13", "Valor=13");
        assertThat(s.erroresContador()).isZero();
    }

    // Same rows with the flag absent: amounts untouched and no paridad lookup (legacy :154 guard).
    @Test
    @DisplayName("CONT-F-206: sin CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria=S los montos no se convierten (legacy :154)")
    void contF206_sinParidadMontosOriginales() {
        Escenario e = escenarioParidad();
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabMovimientos()).extracting(m -> m.monto().toPlainString()).containsExactly("25.00", "1000.00", "10.00");
        assertThat(e.llamadasA("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql")).isEmpty();
    }

    // Legacy :379-417 first paridad lookups for the first non-CLP movement: IdParametroFinanciero "DO", then the currency;
    // FechaMinima = Fecha - PLA.DiasFechaMinimaCalculoParidad (default 1095) days, FechaMaxima = Fecha.
    @Test
    @DisplayName("CONT-F-207: consulta de paridad DO y moneda con ventana de 1095 dias (legacy :379-417)")
    void contF207_consultaParidadParametros() {
        Escenario e = escenarioParidad();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        e.ejecutar(TIPO, "S");
        List<Escenario.SqlCall> calls = e.llamadasA("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql");
        assertThat(calls).hasSizeGreaterThanOrEqualTo(2);
        assertThat(calls.get(0).params().get("IdParametroFinanciero")).isEqualTo("DO");
        assertThat(String.valueOf(calls.get(0).params().get("FechaMinima"))).isEqualTo("2022-03-11");
        assertThat(String.valueOf(calls.get(0).params().get("FechaMaxima"))).isEqualTo("2025-03-10");
        assertThat(calls.get(1).params().get("IdParametroFinanciero")).isEqualTo("USD");
    }

    // Legacy :189-215, 230-233: a currency without TipoCambioMercado is RECHAZO -> P_OUT_ERRORESCONTADOR = 1,
    // one UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA message (Moneda = EUR), and no movements / events / datos at all.
    @Test
    @DisplayName("CONT-F-208: moneda sin tipo de cambio -> 1 error UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA y salida vacia (legacy :189-233)")
    void contF208_paridadRechazadaAbortaConError() {
        Escenario e = new Escenario();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        e.filas(MOV, List.of(Filas.movimiento(8003L, EJERCICIO, "EUR", "10.00")));
        e.filas("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql", p -> "DO".equals(p.get("IdParametroFinanciero"))
                ? List.of(paridad("DO", "0.5")) : List.of());
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).singleElement().asString()
                .startsWith("UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA").contains("EUR");
        assertThat(s.tabMovimientos()).isEmpty();
        assertThat(s.tabEventos()).isEmpty();
        assertThat(s.tabDatosMovimiento()).isEmpty();
        assertThat(s.resultados()).isEmpty();
        assertThat(e.llamadasA(EVE)).isEmpty();
    }

    // Legacy :315 FechaDocumentoPago: Fecha.toStringTimeStamp (yyyy-MM-dd HH:mm:ss.SSS), null -> "0001-01-01 00:00:00".
    @Test
    @DisplayName("CONT-F-209: FechaDocumentoPago con milisegundos y por defecto 0001-01-01 00:00:00 (legacy :315)")
    void contF209_fechaDocumentoPagoFormato() {
        Escenario e = new Escenario();
        List<Map<String, Object>> rows = Filas.datos(TIPO, 0, false);
        rows.get(1).put("FechaDocumentoPago", null);
        e.filas(DAT, rows);
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.tipoDato().equals("FechaDocumentoPago"))
                .extracting(d -> d.valorVarChar()).containsExactly("2025-03-10 14:05:09.123", "0001-01-01 00:00:00");
    }

    // Legacy :42-45 loads the paridad SQL (and the comprobantes-asociados insert).
    @Test
    @DisplayName("CONT-F-210: SQL de paridad presente en el classpath (legacy :45)")
    void contF210_recursoSqlParidad() {
        String f = "CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql";
        ClassLoader cl = getClass().getClassLoader();
        assertThat(cl.getResource("sql/" + f) != null || cl.getResource("cl/bcs/contabilidad/sql/" + f) != null).as(f).isTrue();
    }

    private static Map<String, Object> paridad(String idMoneda, String tipoCambio) {
        Map<String, Object> r = new HashMap<>();
        r.put("IdMoneda", idMoneda);
        r.put("Fecha", java.sql.Timestamp.valueOf("2025-03-10 00:00:00"));
        r.put("ParidadMercado", BigDecimal.ONE);
        r.put("TipoCambioMercado", new BigDecimal(tipoCambio));
        return r;
    }

    private static Escenario escenarioParidad() {
        Escenario e = new Escenario();
        e.filas(MOV, List.of(
                Filas.movimiento(8001L, EJERCICIO, "USD", "25.00"),
                Filas.movimiento(8002L, EJERCICIO, "CLP", "1000.00"),
                Filas.movimiento(8003L, EJERCICIO, "EUR", "10.00")));
        e.filas(EVE, List.of(
                Filas.evento(8001L, "SOLICITUD_MONTO_INGRESO", "25.00", "USD"),
                Filas.evento(8001L, "OTRO_EVENTO", "25.00", "USD"),
                Filas.evento(8003L, "DOCUMENTO_MONTO_EGRESO", "10.00", "EUR"),
                Filas.evento(8002L, "SOLICITUD_MONTO_INGRESO", "1000.00", "CLP")));
        List<Map<String, Object>> datos = Filas.datos(TIPO, 0, false);
        datos.get(0).put("IdMovimiento", 8001L);
        datos.get(0).put("Moneda", "USD");
        datos.get(0).put("Monto", new BigDecimal("25.00"));
        datos.get(0).put("Valor", new BigDecimal("25.00"));
        datos.get(1).put("Moneda", "CLP");
        e.filas(DAT, datos);
        e.filas("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql", p -> switch (String.valueOf(p.get("IdParametroFinanciero"))) {
            case "DO" -> List.of(paridad("DO", "0.5"));
            case "EUR" -> List.of(paridad("EUR", "1.05"));
            default -> List.<Map<String, Object>>of();
        });
        return e;
    }
}
