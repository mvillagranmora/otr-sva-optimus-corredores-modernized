package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaInteresReajusteCarteraTerminoRFIF;
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
 * WP-F characterization of CentralizaInteresReajusteCarteraTerminoRFIF.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaInteresReajusteCarteraTerminoRFIF.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaInteresReajusteCarteraTerminoRFIFCharacterizationTest {

    static final String MOV = "CentralizaInteresReajusteCarteraTerminoRFIF_SelectMovimientos.sql";
    static final String EVE = "CentralizaInteresReajusteCarteraTerminoRFIF_SelectEventos.sql";
    static final String DAT = "CentralizaInteresReajusteCarteraTerminoRFIF_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaInteresReajusteCarteraTerminoRFIF", CentralizaInteresReajusteCarteraTerminoRFIF::new,
            List.of(
                    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:76-81
                    Spec.consulta("CentralizaInteresReajusteCarteraTerminoRFIF_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO, "p_DocumentoIdentidadCorredora", RUT_CORREDORA)),
                    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:85-89
                    Spec.consulta("CentralizaInteresReajusteCarteraTerminoRFIF_SelectEventos.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO, "p_DocumentoIdentidadCorredora", RUT_CORREDORA)),
                    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:93-98
                    Spec.consulta("CentralizaInteresReajusteCarteraTerminoRFIF_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_DocumentoIdentidadCorredora", RUT_CORREDORA, "LocSistema", LOC_SISTEMA))),
            // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:101-133 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("IdMovimiento", "IdMovimiento", VALUEOF_LONG),
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
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("IdConcepto", "IdConcepto", STR),
                    Spec.campo("DocumentoIdentidadCAVALI", "DocumentoIdentidadCAVALI", STR),
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("Mesa", "Mesa", STR),
                    Spec.campo("FolioOperacion", "FolioOperacion", VALUEOF_LONG)));

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-001: los SQL legacy del tipo existen en el classpath")
    void contF001_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:76-81,85-89,93-98
    @Test
    @DisplayName("CONT-F-002: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF002_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-003: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF003_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:76-81,85-89 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-004: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF004_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:101-133
    @Test
    @DisplayName("CONT-F-005: TabDatosMovimiento: 31 TipoDato por fila, orden y formato legacy")
    void contF005_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(31 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-24");
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:101-133 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-006: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF006_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java:101-133 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-007: una fecha nula aborta la centralizacion del tipo")
    void contF007_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaInteresReajusteCarteraTerminoRFIF.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-008: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF008_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-009: simulaOperacion N persiste una vez las tablas producidas")
    void contF009_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-010: simulaOperacion S no persiste")
    void contF010_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-011: consultas vacias producen salida vacia sin errores")
    void contF011_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
