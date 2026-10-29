package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaRescateFondoMutuoCuentaPropia;
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
 * WP-F characterization of CentralizaRescateFondoMutuoCuentaPropia.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaRescateFondoMutuoCuentaPropia.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaRescateFondoMutuoCuentaPropiaCharacterizationTest {

    static final String MOV = "CentralizaRescateFondoMutuoCuentaPropia_SelectMovimientos.sql";
    static final String EVE = "CentralizaRescateFondoMutuoCuentaPropia_SelectEventos.sql";
    static final String DAT = "CentralizaRescateFondoMutuoCuentaPropia_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaRescateFondoMutuoCuentaPropia", CentralizaRescateFondoMutuoCuentaPropia::new,
            List.of(
                    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:76-81
                    Spec.consulta("CentralizaRescateFondoMutuoCuentaPropia_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "IdEmpresa", ID_EMPRESA, "DocumentoIdentidadCorredora", RUT_CORREDORA)),
                    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:85-90
                    Spec.consulta("CentralizaRescateFondoMutuoCuentaPropia_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "IdMonedaSistema", MONEDA_SISTEMA, "IdEmpresa", ID_EMPRESA, "DocumentoIdentidadCorredora", RUT_CORREDORA)),
                    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:94-97
                    Spec.consulta("CentralizaRescateFondoMutuoCuentaPropia_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "DocumentoIdentidadCorredora", RUT_CORREDORA))),
            // legacy CentralizaRescateFondoMutuoCuentaPropia.java:101-135 (one add(...) per line, in this order)
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
                    Spec.campo("IdCartera", "IdCartera", VALUEOF_STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("CostingCode", "CentroCostoAgente", STR),
                    Spec.campo("CostingCode2", "Producto", STR),
                    Spec.campo("CostingCode3", "Relacion", STR),
                    Spec.campo("CostingCode4", "Bolsa", STR),
                    Spec.campo("CostingCode5", null, CONST),
                    Spec.campo("MontoMonedaSistema", "MontoMonedaSistema", DECIMAL),
                    Spec.campo("TipoOperacionFFMM", "TipoOperacionFFMM", STR),
                    Spec.campo("IdFondo", "IdFondo", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR)));

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-090: los SQL legacy del tipo existen en el classpath")
    void contF090_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:76-81,85-90,94-97
    @Test
    @DisplayName("CONT-F-091: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF091_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-092: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF092_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:76-81,85-90 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-093: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF093_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:101-135
    @Test
    @DisplayName("CONT-F-094: TabDatosMovimiento: 29 TipoDato por fila, orden y formato legacy")
    void contF094_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(29 * 2 * 1);
        Casos.literal(datos, "FechaOperacion", "2025-02-19");
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:101-135 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-095: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF095_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java:101-135 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-096: una fecha nula aborta la centralizacion del tipo")
    void contF096_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaRescateFondoMutuoCuentaPropia.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-097: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF097_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-098: simulaOperacion N persiste una vez las tablas producidas")
    void contF098_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-099: simulaOperacion S no persiste")
    void contF099_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-100: consultas vacias producen salida vacia sin errores")
    void contF100_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
