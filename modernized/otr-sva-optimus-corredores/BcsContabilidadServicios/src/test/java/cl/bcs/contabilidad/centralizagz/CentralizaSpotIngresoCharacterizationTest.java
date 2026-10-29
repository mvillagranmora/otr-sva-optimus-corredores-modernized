package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaSpotIngreso;
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
 * WP-F characterization of CentralizaSpotIngreso.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaSpotIngreso.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaSpotIngresoCharacterizationTest {

    static final String MOV = "CentralizaSpotIngreso_SelectMovimientos.sql";
    static final String EVE = "CentralizaSpotIngreso_SelectEventos.sql";
    static final String DAT = "CentralizaSpotIngreso_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaSpotIngreso", CentralizaSpotIngreso::new,
            List.of(
                    // legacy CentralizaSpotIngreso.java:65-69
                    Spec.consulta("CentralizaSpotIngreso_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaSpotIngreso.java:73-76
                    Spec.consulta("CentralizaSpotIngreso_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaSpotIngreso.java:80-82
                    Spec.consulta("CentralizaSpotIngreso_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA))),
            // legacy CentralizaSpotIngreso.java:87-113 (one add(...) per line, in this order)
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
                    Spec.campo("IdCartera", "IdCartera", CONST),
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

    // legacy CentralizaSpotIngreso.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-145: los SQL legacy del tipo existen en el classpath")
    void contF145_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaSpotIngreso.java:65-69,73-76,80-82
    @Test
    @DisplayName("CONT-F-146: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF146_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaSpotIngreso.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-147: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF147_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaSpotIngreso.java:65-69,73-76 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-148: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF148_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaSpotIngreso.java:87-113
    @Test
    @DisplayName("CONT-F-149: TabDatosMovimiento: 27 TipoDato por fila, orden y formato legacy")
    void contF149_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(27 * 2 * 1);
        Casos.literal(datos, "DocumentoIdentidad", "DocumentoIdentidad-1");
    }

    // legacy CentralizaSpotIngreso.java:87-113 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-150: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF150_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaSpotIngreso.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-151: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF151_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-152: simulaOperacion N persiste una vez las tablas producidas")
    void contF152_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-153: simulaOperacion S no persiste")
    void contF153_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-154: consultas vacias producen salida vacia sin errores")
    void contF154_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
