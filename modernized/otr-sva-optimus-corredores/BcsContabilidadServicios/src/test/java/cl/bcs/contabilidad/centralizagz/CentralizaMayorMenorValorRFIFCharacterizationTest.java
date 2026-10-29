package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaMayorMenorValorRFIF;
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
 * WP-F characterization of CentralizaMayorMenorValorRFIF.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaMayorMenorValorRFIF.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaMayorMenorValorRFIFCharacterizationTest {

    static final String MOV = "CentralizaMayorMenorValorRFIF_SelectMovimientos.sql";
    static final String EVE = "CentralizaMayorMenorValorRFIF_SelectEventos.sql";
    static final String DAT = "CentralizaMayorMenorValorRFIF_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaMayorMenorValorRFIF", CentralizaMayorMenorValorRFIF::new,
            List.of(
                    // legacy CentralizaMayorMenorValorRFIF.java:62-67
                    Spec.consulta("CentralizaMayorMenorValorRFIF_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO, "p_IdInforme", ID_INFORME)),
                    // legacy CentralizaMayorMenorValorRFIF.java:71-74
                    Spec.consulta("CentralizaMayorMenorValorRFIF_SelectEventos.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_IdTipoCentralizacion", ID_TIPO, "p_IdInforme", ID_INFORME)),
                    // legacy CentralizaMayorMenorValorRFIF.java:78-81
                    Spec.consulta("CentralizaMayorMenorValorRFIF_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_DocumentoIdentidad", RUT_CORREDORA, "p_IdInforme", ID_INFORME))),
            // legacy CentralizaMayorMenorValorRFIF.java:85-104 (one add(...) per line, in this order)
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

    // legacy CentralizaMayorMenorValorRFIF.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-033: los SQL legacy del tipo existen en el classpath")
    void contF033_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaMayorMenorValorRFIF.java:62-67,71-74,78-81
    @Test
    @DisplayName("CONT-F-034: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF034_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaMayorMenorValorRFIF.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-035: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF035_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaMayorMenorValorRFIF.java:62-67,71-74 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-036: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF036_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaMayorMenorValorRFIF.java:85-104
    @Test
    @DisplayName("CONT-F-037: TabDatosMovimiento: 19 TipoDato por fila, orden y formato legacy")
    void contF037_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(20 * 2 * 1);
        Casos.literal(datos, "FechaOperacion", "2025-02-06");
    }

    // legacy CentralizaMayorMenorValorRFIF.java:85-104 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-038: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF038_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaMayorMenorValorRFIF.java:85-104 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-039: una fecha nula aborta la centralizacion del tipo")
    void contF039_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaMayorMenorValorRFIF.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-040: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF040_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-041: simulaOperacion N persiste una vez las tablas producidas")
    void contF041_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-042: simulaOperacion S no persiste")
    void contF042_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-043: consultas vacias producen salida vacia sin errores")
    void contF043_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
