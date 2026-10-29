package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaSpotUtilidadPerdida;
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
 * WP-F characterization of CentralizaSpotUtilidadPerdida.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaSpotUtilidadPerdida.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaSpotUtilidadPerdidaCharacterizationTest {

    static final String MOV = "CentralizaSpotUtilidadPerdida_SelectMovimientos.sql";
    static final String EVE = "CentralizaSpotUtilidadPerdida_SelectEventos.sql";
    static final String DAT = "CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaSpotUtilidadPerdida", CentralizaSpotUtilidadPerdida::new,
            List.of(
                    // legacy CentralizaSpotUtilidadPerdida.java:88-98
                    Spec.consulta("CentralizaSpotUtilidadPerdida_ConsultaTablaBloqueo.sql", Spec.Rol.OTRA, Spec.params("IdBloqueoCentralizacion", "470_2025-03-10")),
                    // legacy CentralizaSpotUtilidadPerdida.java:125-133
                    Spec.consulta("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO, "MonedaSistema", MONEDA_SISTEMA)),
                    // legacy CentralizaSpotUtilidadPerdida.java:137-141
                    Spec.consulta("CentralizaSpotUtilidadPerdida_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO, "MonedaSistema", MONEDA_SISTEMA)),
                    // legacy CentralizaSpotUtilidadPerdida.java:145-149
                    Spec.consulta("CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "MonedaSistema", MONEDA_SISTEMA))),
            // legacy CentralizaSpotUtilidadPerdida.java:155-181 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("Producto", "Producto", STR),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("Boveda", "Boveda", STR),
                    Spec.campo("IdCartera", "Boveda", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("Canal", "Canal", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("TipoComprobante", "TipoComprobante", STR),
                    Spec.campo("CompraVenta", "CompraVenta", STR),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR),
                    Spec.campo("OperacionNoFacturable", "OperacionNoFacturable", STR)));

    // legacy CentralizaSpotUtilidadPerdida.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-155: los SQL legacy del tipo existen en el classpath")
    void contF155_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaSpotUtilidadPerdida.java:88-98,125-133,137-141,145-149
    @Test
    @DisplayName("CONT-F-156: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF156_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaSpotUtilidadPerdida.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-157: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF157_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaSpotUtilidadPerdida.java:125-133,137-141 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-158: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF158_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaSpotUtilidadPerdida.java:155-181
    @Test
    @DisplayName("CONT-F-159: TabDatosMovimiento: 27 TipoDato por fila, orden y formato legacy")
    void contF159_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(27 * 2 * 1);
        Casos.literal(datos, "DocumentoIdentidad", "DocumentoIdentidad-1");
    }

    // legacy CentralizaSpotUtilidadPerdida.java:155-181 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-160: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF160_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaSpotUtilidadPerdida.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-161: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF161_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-162: simulaOperacion N persiste una vez las tablas producidas")
    void contF162_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-163: simulaOperacion S no persiste")
    void contF163_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-164: consultas vacias producen salida vacia sin errores")
    void contF164_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


    // Legacy CentralizaSpotUtilidadPerdida.java:98-108: an existing lock row aborts with 1 error and UI_CENTRALIZACION_CONTABILIZADA_ANULAR
    // (valor Centralizacion = Nombre from CentralizaGenerico_SelectTipoCentralizacion.sql) and no movement SQL runs.
    @Test
    @DisplayName("CONT-F-165: bloqueo existente -> 1 error UI_CENTRALIZACION_CONTABILIZADA_ANULAR y no centraliza (legacy :98-108)")
    void contF165_bloqueoExistenteAbortaConError() {
        Escenario e = new Escenario();
        e.filas("CentralizaSpotUtilidadPerdida_ConsultaTablaBloqueo.sql",
                List.of(Map.of("IdBloqueoCentralizacion", "470_2025-03-10")));
        e.filas("CentralizaGenerico_SelectTipoCentralizacion.sql",
                List.of(Map.of("IdTipoCentralizacion", ID_TIPO, "Nombre", "SPOT UTILIDAD PERDIDA", "TipoEnvio", "A")));
        e.filas("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql",
                List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).singleElement().asString()
                .startsWith("UI_CENTRALIZACION_CONTABILIZADA_ANULAR").contains("SPOT UTILIDAD PERDIDA");
        assertThat(e.llamadasA("CentralizaGenerico_SelectTipoCentralizacion.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(Spec.params("IdTipoCentralizacion", ID_TIPO));
        assertThat(e.llamadasA("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql")).isEmpty();
        assertThat(s.tabMovimientos()).isEmpty();
        assertThat(s.resultados()).isEmpty();
    }

    // Legacy CentralizaSpotUtilidadPerdida.java:187-189: any exception inside the try is logged and swallowed;
    // tables filled so far are kept, P_OUT_RESULTADO stays empty, P_OUT_ERRORESCONTADOR stays 0.
    @Test
    @DisplayName("CONT-F-166: excepcion en datos movimiento se traga: no propaga, 0 errores, resultados vacios (legacy :187-189)")
    void contF166_excepcionSeTragaSinErrores() {
        Escenario e = new Escenario();
        e.filas("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql",
                List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        e.filas("CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql",
                p -> { throw new IllegalStateException("fallo simulado de la consulta de datos"); });
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isZero();
        assertThat(s.resultados()).isEmpty();
        assertThat(s.tabDatosMovimiento()).isEmpty();
        assertThat(s.tabMovimientos()).extracting(m -> m.idMovimiento()).containsExactly(7001L);
    }
}
