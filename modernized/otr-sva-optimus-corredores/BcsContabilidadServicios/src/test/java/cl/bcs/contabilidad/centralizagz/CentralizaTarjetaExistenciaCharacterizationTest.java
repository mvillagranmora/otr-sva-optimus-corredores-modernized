package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaTarjetaExistencia;
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
 * WP-F characterization of CentralizaTarjetaExistencia.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaTarjetaExistencia.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaTarjetaExistenciaCharacterizationTest {

    static final String MOV = "CentralizaTarjetaExistencia_SelectMovimientos.sql";
    static final String EVE = "CentralizaTarjetaExistencia_SelectEventos.sql";
    static final String DAT = "CentralizaTarjetaExistencia_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaTarjetaExistencia", CentralizaTarjetaExistencia::new,
            List.of(
                    // legacy CentralizaTarjetaExistencia.java:62-66
                    Spec.consulta("CentralizaTarjetaExistencia_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaTarjetaExistencia.java:70-73
                    Spec.consulta("CentralizaTarjetaExistencia_SelectEventos.sql", Spec.Rol.EVE, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaTarjetaExistencia.java:77-79
                    Spec.consulta("CentralizaTarjetaExistencia_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE))),
            // legacy CentralizaTarjetaExistencia.java:83-105 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("IdMovimiento", "IdMovimiento", VALUEOF_LONG),
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("PortafolioCliente", "PortafolioCliente", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("IdCartera", "IdCartera", CONST),
                    Spec.campo("IdEmisor", "IdEmisor", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR)));

    // legacy CentralizaTarjetaExistencia.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-167: los SQL legacy del tipo existen en el classpath")
    void contF167_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaTarjetaExistencia.java:62-66,70-73,77-79
    @Test
    @DisplayName("CONT-F-168: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF168_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaTarjetaExistencia.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-169: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF169_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaTarjetaExistencia.java:62-66,70-73 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-170: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF170_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaTarjetaExistencia.java:83-105
    @Test
    @DisplayName("CONT-F-171: TabDatosMovimiento: 18 TipoDato por fila, orden y formato legacy")
    void contF171_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(18 * 2 * 1);
        Casos.literal(datos, "IdMovimiento", "7001");
    }

    // legacy CentralizaTarjetaExistencia.java:83-105 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-172: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF172_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaTarjetaExistencia.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-173: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF173_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-174: simulaOperacion N persiste una vez las tablas producidas")
    void contF174_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-175: simulaOperacion S no persiste")
    void contF175_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-176: consultas vacias producen salida vacia sin errores")
    void contF176_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
