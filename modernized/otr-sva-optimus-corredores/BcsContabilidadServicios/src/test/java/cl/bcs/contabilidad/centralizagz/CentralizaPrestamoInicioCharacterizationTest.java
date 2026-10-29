package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaPrestamoInicio;
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
 * WP-F characterization of CentralizaPrestamoInicio.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaPrestamoInicio.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaPrestamoInicioCharacterizationTest {

    static final String MOV = "CentralizaPrestamoInicio_SelectMovimientos.sql";
    static final String EVE = "CentralizaPrestamoInicio_SelectEventos.sql";
    static final String DAT = "CentralizaPrestamoInicio_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaPrestamoInicio", CentralizaPrestamoInicio::new,
            List.of(
                    // legacy CentralizaPrestamoInicio.java:72-83
                    Spec.consulta("CentralizaPrestamoInicio_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "IdClasificacionTipoInstrumento", "PRE")),
                    // legacy CentralizaPrestamoInicio.java:90-95
                    Spec.consulta("CentralizaPrestamoInicio_SelectEventos.sql", Spec.Rol.EVE, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "IdClasificacionTipoInstrumento", "PRE")),
                    // legacy CentralizaPrestamoInicio.java:102-108
                    Spec.consulta("CentralizaPrestamoInicio_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "LocSistema", LOC_SISTEMA, "IdClasificacionTipoInstrumento", "PRE"))),
            // legacy CentralizaPrestamoInicio.java:116-138 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("EsRelacionado", "EsRelacionado", STR),
                    Spec.campo("EsCuentaPropia", "EsCuentaPropia", STR),
                    Spec.campo("Producto", "Producto", STR),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("Boveda", "Boveda", STR),
                    Spec.campo("IdCartera", "IdCartera", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("TipoPrestamo", "TipoPrestamo", STR),
                    Spec.campo("TipoPrestatario", "TipoPrestatario", STR),
                    Spec.campo("CondicionLiquidacion", "CondicionLiquidacion", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", TOSTRING_STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", TOSTRING_STR)));

    // legacy CentralizaPrestamoInicio.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-066: los SQL legacy del tipo existen en el classpath")
    void contF066_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaPrestamoInicio.java:72-83,90-95,102-108
    @Test
    @DisplayName("CONT-F-067: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF067_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaPrestamoInicio.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-068: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF068_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaPrestamoInicio.java:72-83,90-95 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-069: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF069_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaPrestamoInicio.java:116-138
    @Test
    @DisplayName("CONT-F-070: TabDatosMovimiento: 23 TipoDato por fila, orden y formato legacy")
    void contF070_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(23 * 2 * 1);
        Casos.literal(datos, "DocumentoIdentidad", "DocumentoIdentidad-1");
    }

    // legacy CentralizaPrestamoInicio.java:116-138 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-071: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF071_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaPrestamoInicio.java:116-138 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-072: una fecha nula aborta la centralizacion del tipo")
    void contF072_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaPrestamoInicio.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-073: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF073_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-074: simulaOperacion N persiste una vez las tablas producidas")
    void contF074_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-075: simulaOperacion S no persiste")
    void contF075_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-076: consultas vacias producen salida vacia sin errores")
    void contF076_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


    // Legacy CentralizaPrestamoInicio.java:70,82-86,94-98,107-111: when FechaContable != PLA.FechaSistema the *Historicos SQL run instead.
    @Test
    @DisplayName("CONT-F-077: FechaContable distinta de PLA.FechaSistema usa las consultas historicas (legacy :70,82-111)")
    void contF077_fechaHistoricaUsaConsultasHistoricas() {
        Escenario e = new Escenario();
        e.fechaSistema = "2025-03-11";
        e.ejecutar(TIPO, "S");
        Map<String, Object> pMov = Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE,
                "IdTipoCentralizacion", ID_TIPO, "IdClasificacionTipoInstrumento", "PRE");
        Map<String, Object> pDat = Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE,
                "LocSistema", LOC_SISTEMA, "IdClasificacionTipoInstrumento", "PRE");
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectMovimientosHistoricos.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pMov);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectEventosHistoricos.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pMov);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectDatosMovimientoHistorico.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pDat);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectMovimientos.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectEventos.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectDatosMovimiento.sql")).isEmpty();
    }

    // Legacy CentralizaPrestamoInicio.java:21-27 loads the clasificacion SQL and the three historic SQL too.
    @Test
    @DisplayName("CONT-F-078: SQL de clasificacion e historicos presentes en el classpath (legacy :21-27)")
    void contF078_recursosSqlHistoricos() {
        ClassLoader cl = getClass().getClassLoader();
        for (String f : List.of("CentralizaPrestamoInicio_SelectClasificacionTipoInstrumento.sql",
                "CentralizaPrestamoInicio_SelectMovimientosHistoricos.sql",
                "CentralizaPrestamoInicio_SelectEventosHistoricos.sql",
                "CentralizaPrestamoInicio_SelectDatosMovimientoHistorico.sql")) {
            assertThat(cl.getResource("sql/" + f) != null || cl.getResource("cl/bcs/contabilidad/sql/" + f) != null)
                    .as(f).isTrue();
        }
    }
}
