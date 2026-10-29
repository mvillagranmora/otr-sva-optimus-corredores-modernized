package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaPrestamosEmprestitos;
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
 * WP-F characterization of CentralizaPrestamosEmprestitos.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaPrestamosEmprestitos.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaPrestamosEmprestitosCharacterizationTest {

    static final String MOV = "CentralizaPrestamosEmprestitos_SelectMovimientos.sql";
    static final String EVE = "CentralizaPrestamosEmprestitos_SelectEventos.sql";
    static final String DAT = "CentralizaPrestamosEmprestitos_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaPrestamosEmprestitos", CentralizaPrestamosEmprestitos::new,
            List.of(
                    // legacy CentralizaPrestamosEmprestitos.java:94-99
                    Spec.consulta("CentralizaPrestamosEmprestitos_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaPrestamosEmprestitos.java:78-91,103-108
                    Spec.consulta("CentralizaPrestamosEmprestitos_SelectEventos.sql", Spec.Rol.EVE, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "FechaAnterior", FECHA_ANTERIOR, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaPrestamosEmprestitos.java:112-116
                    Spec.consulta("CentralizaPrestamosEmprestitos_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "LocSistema", LOC_SISTEMA))),
            // legacy CentralizaPrestamosEmprestitos.java:121-149 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("EsCuentaPropia", "EsCuentaPropia", STR),
                    Spec.campo("EsVentaCorta", "EsVentaCorta", STR),
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
                    Spec.campo("IdConcepto", "IdConcepto", STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR)));

    // legacy CentralizaPrestamosEmprestitos.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-079: los SQL legacy del tipo existen en el classpath")
    void contF079_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaPrestamosEmprestitos.java:94-99,78-91,103-108,112-116
    @Test
    @DisplayName("CONT-F-080: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF080_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaPrestamosEmprestitos.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-081: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF081_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaPrestamosEmprestitos.java:94-99,78-91,103-108 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-082: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF082_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaPrestamosEmprestitos.java:121-149
    @Test
    @DisplayName("CONT-F-083: TabDatosMovimiento: 28 TipoDato por fila, orden y formato legacy")
    void contF083_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(28 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-25");
    }

    // legacy CentralizaPrestamosEmprestitos.java:121-149 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-084: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF084_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaPrestamosEmprestitos.java:121-149 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-085: una fecha nula aborta la centralizacion del tipo")
    void contF085_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaPrestamosEmprestitos.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-086: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF086_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-087: simulaOperacion N persiste una vez las tablas producidas")
    void contF087_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-088: simulaOperacion S no persiste")
    void contF088_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-089: consultas vacias producen salida vacia sin errores")
    void contF089_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
