package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaOrdenDirecta;
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
 * WP-F characterization of CentralizaOrdenDirecta.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaOrdenDirecta.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaOrdenDirectaCharacterizationTest {

    static final String MOV = "CentralizaOrdenDirecta_SelectMovimientos.sql";
    static final String EVE = "CentralizaOrdenDirecta_SelectEventos.sql";
    static final String DAT = "CentralizaOrdenDirecta_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaOrdenDirecta", CentralizaOrdenDirecta::new,
            List.of(
                    // legacy CentralizaOrdenDirecta.java:65-69
                    Spec.consulta("CentralizaOrdenDirecta_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaOrdenDirecta.java:62,73-77
                    Spec.consulta("CentralizaOrdenDirecta_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO, "MonedaSistema", MONEDA_SISTEMA)),
                    // legacy CentralizaOrdenDirecta.java:81-83
                    Spec.consulta("CentralizaOrdenDirecta_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA))),
            // legacy CentralizaOrdenDirecta.java:88-99 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("TipoDocumento", "TipoDocumento", STR),
                    Spec.campo("FolioTransaccion", "FolioTransaccion", STR),
                    Spec.campo("CompraVenta", "CompraVenta", STR),
                    Spec.campo("CodigoBolsa", "CodigoBolsa", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("EsCuentaPropia", "EsCuentaPropia", STR),
                    Spec.campo("FamiliaInstrumento", "FamiliaInstrumento", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("Producto", "Producto", STR),
                    Spec.campo("Estado", "Estado", STR)));

    // legacy CentralizaOrdenDirecta.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-055: los SQL legacy del tipo existen en el classpath")
    void contF055_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaOrdenDirecta.java:65-69,62,73-77,81-83
    @Test
    @DisplayName("CONT-F-056: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF056_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaOrdenDirecta.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-057: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF057_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaOrdenDirecta.java:65-69,62,73-77 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-058: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF058_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaOrdenDirecta.java:88-99
    @Test
    @DisplayName("CONT-F-059: TabDatosMovimiento: 12 TipoDato por fila, orden y formato legacy")
    void contF059_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(12 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-08");
    }

    // legacy CentralizaOrdenDirecta.java:88-99 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-060: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF060_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaOrdenDirecta.java:88-99 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-061: una fecha nula aborta la centralizacion del tipo")
    void contF061_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaOrdenDirecta.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-062: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF062_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-063: simulaOperacion N persiste una vez las tablas producidas")
    void contF063_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-064: simulaOperacion S no persiste")
    void contF064_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-065: consultas vacias producen salida vacia sin errores")
    void contF065_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
