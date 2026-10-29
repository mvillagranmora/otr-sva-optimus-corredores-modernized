package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaMayorMenorValorRV;
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
 * WP-F characterization of CentralizaMayorMenorValorRV.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaMayorMenorValorRV.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaMayorMenorValorRVCharacterizationTest {

    static final String MOV = "CentralizaMayorMenorValorRV_SelectMovimientos.sql";
    static final String EVE = "CentralizaMayorMenorValorRV_SelectEventos.sql";
    static final String DAT = "CentralizaMayorMenorValorRV_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaMayorMenorValorRV", CentralizaMayorMenorValorRV::new,
            List.of(
                    // legacy CentralizaMayorMenorValorRV.java:64-69
                    Spec.consulta("CentralizaMayorMenorValorRV_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO, "p_IdInforme", ID_INFORME)),
                    // legacy CentralizaMayorMenorValorRV.java:73-76
                    Spec.consulta("CentralizaMayorMenorValorRV_SelectEventos.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_IdTipoCentralizacion", ID_TIPO, "p_IdInforme", ID_INFORME)),
                    // legacy CentralizaMayorMenorValorRV.java:80-83
                    Spec.consulta("CentralizaMayorMenorValorRV_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_DocumentoIdentidad", RUT_CORREDORA, "p_IdInforme", ID_INFORME))),
            // legacy CentralizaMayorMenorValorRV.java:87-106 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("EsCuentaPropia", "EsCuentaPropia", VALUEOF_STR),
                    Spec.campo("EsRelacionado", "EsRelacionado", VALUEOF_STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("FechaVencimientoOperacion", "FechaVencimientoOperacion", TOSTRING_DATE),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("FolioOperacion", "FolioOperacion", VALUEOF_LONG),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("Nemotecnico", "Nemo", STR),
                    Spec.campo("Boveda", "Boveda", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("IdTipoReajuste", "TipoReajuste", STR),
                    Spec.campo("FechaVencimientoInstrumento", "FechaVencimientoInstrumento", TOSTRING_DATE),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", TOSTRING_STR)));

    // legacy CentralizaMayorMenorValorRV.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-044: los SQL legacy del tipo existen en el classpath")
    void contF044_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaMayorMenorValorRV.java:64-69,73-76,80-83
    @Test
    @DisplayName("CONT-F-045: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF045_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaMayorMenorValorRV.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-046: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF046_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaMayorMenorValorRV.java:64-69,73-76 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-047: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF047_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaMayorMenorValorRV.java:87-106
    @Test
    @DisplayName("CONT-F-048: TabDatosMovimiento: 19 TipoDato por fila, orden y formato legacy")
    void contF048_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(20 * 2 * 1);
        Casos.literal(datos, "FechaOperacion", "2025-02-06");
    }

    // legacy CentralizaMayorMenorValorRV.java:87-106 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-049: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF049_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaMayorMenorValorRV.java:87-106 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-050: una fecha nula aborta la centralizacion del tipo")
    void contF050_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaMayorMenorValorRV.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-051: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF051_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-052: simulaOperacion N persiste una vez las tablas producidas")
    void contF052_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-053: simulaOperacion S no persiste")
    void contF053_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-054: consultas vacias producen salida vacia sin errores")
    void contF054_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
