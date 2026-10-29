package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaVentaTerminoSpread;
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
 * WP-F characterization of CentralizaVentaTerminoSpread.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaVentaTerminoSpread.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaVentaTerminoSpreadCharacterizationTest {

    static final String MOV = "CentralizaVentaTerminoSpread_SelectMovimientos.sql";
    static final String EVE = "CentralizaVentaTerminoSpread_SelectEventos.sql";
    static final String DAT = "CentralizaVentaTerminoSpread_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaVentaTerminoSpread", CentralizaVentaTerminoSpread::new,
            List.of(
                    // legacy CentralizaVentaTerminoSpread.java:77-81
                    Spec.consulta("CentralizaVentaTerminoSpread_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaVentaTerminoSpread.java:85-89
                    Spec.consulta("CentralizaVentaTerminoSpread_SelectEventos.sql", Spec.Rol.EVE, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "DocumentoIdentidad", RUT_CORREDORA)),
                    // legacy CentralizaVentaTerminoSpread.java:93-97
                    Spec.consulta("CentralizaVentaTerminoSpread_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "DocumentoIdentidad", RUT_CORREDORA, "LocSistema", LOC_SISTEMA))),
            // legacy CentralizaVentaTerminoSpread.java:101-131 (one add(...) per line, in this order)
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
                    Spec.campo("IdCartera", "IdCartera", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("Relacion", "Relacion", VALUEOF_STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("Mesa", "Mesa", STR),
                    Spec.campo("FolioOperacion", "FolioOperacion", VALUEOF_LONG),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR)));

    // legacy CentralizaVentaTerminoSpread.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-234: los SQL legacy del tipo existen en el classpath")
    void contF234_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaVentaTerminoSpread.java:77-81,85-89,93-97
    @Test
    @DisplayName("CONT-F-235: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF235_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaVentaTerminoSpread.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-236: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF236_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaVentaTerminoSpread.java:77-81,85-89 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-237: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF237_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaVentaTerminoSpread.java:101-131
    @Test
    @DisplayName("CONT-F-238: TabDatosMovimiento: 29 TipoDato por fila, orden y formato legacy")
    void contF238_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(29 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-23");
    }

    // legacy CentralizaVentaTerminoSpread.java:101-131 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-239: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF239_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaVentaTerminoSpread.java:101-131 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-240: una fecha nula aborta la centralizacion del tipo")
    void contF240_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaVentaTerminoSpread.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-241: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF241_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-242: simulaOperacion N persiste una vez las tablas producidas")
    void contF242_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-243: simulaOperacion S no persiste")
    void contF243_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-244: consultas vacias producen salida vacia sin errores")
    void contF244_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
