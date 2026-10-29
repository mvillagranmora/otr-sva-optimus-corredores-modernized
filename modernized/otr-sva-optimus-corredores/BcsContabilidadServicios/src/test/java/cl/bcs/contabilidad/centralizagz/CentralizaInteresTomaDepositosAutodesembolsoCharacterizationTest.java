package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaInteresTomaDepositosAutodesembolso;
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
 * WP-F characterization of CentralizaInteresTomaDepositosAutodesembolso.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaInteresTomaDepositosAutodesembolso.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaInteresTomaDepositosAutodesembolsoCharacterizationTest {

    static final String MOV = "CentralizaInteresTomaDepositosAutodesembolso_SelectMovimientos.sql";
    static final String EVE = "CentralizaInteresTomaDepositosAutodesembolso_SelectEventos.sql";
    static final String DAT = "CentralizaInteresTomaDepositosAutodesembolso_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaInteresTomaDepositosAutodesembolso", CentralizaInteresTomaDepositosAutodesembolso::new,
            List.of(
                    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:71-76
                    Spec.consulta("CentralizaInteresTomaDepositosAutodesembolso_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:80-84
                    Spec.consulta("CentralizaInteresTomaDepositosAutodesembolso_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:88-91
                    Spec.consulta("CentralizaInteresTomaDepositosAutodesembolso_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA))),
            // legacy CentralizaInteresTomaDepositosAutodesembolso.java:95-127 (one add(...) per line, in this order)
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
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("Concepto", "Concepto", STR),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("CostingCode", "CentroCostoAgente", STR),
                    Spec.campo("CostingCode2", "Producto", STR),
                    Spec.campo("CostingCode3", "Relacion", STR),
                    Spec.campo("CostingCode4", "Bolsa", STR),
                    Spec.campo("CostingCode5", null, CONST)));

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-012: los SQL legacy del tipo existen en el classpath")
    void contF012_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:71-76,80-84,88-91
    @Test
    @DisplayName("CONT-F-013: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF013_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-014: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF014_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:71-76,80-84 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-015: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF015_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:95-127
    @Test
    @DisplayName("CONT-F-016: TabDatosMovimiento: 31 TipoDato por fila, orden y formato legacy")
    void contF016_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(31 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-23");
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:95-127 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-017: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF017_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java:95-127 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-018: una fecha nula aborta la centralizacion del tipo")
    void contF018_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaInteresTomaDepositosAutodesembolso.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-019: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF019_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-020: simulaOperacion N persiste una vez las tablas producidas")
    void contF020_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-021: simulaOperacion S no persiste")
    void contF021_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-022: consultas vacias producen salida vacia sin errores")
    void contF022_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
