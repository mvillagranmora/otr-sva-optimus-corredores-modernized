package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaRFIFVencimientos;
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
 * WP-F characterization of CentralizaRFIFVencimientos.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaRFIFVencimientos.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaRFIFVencimientosCharacterizationTest {

    static final String MOV = "CentralizaRFIFVencimientos_SelectMovimientos.sql";
    static final String EVE = "CentralizaRFIFVencimientos_SelectEventos.sql";
    static final String DAT = "CentralizaRFIFVencimientos_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaRFIFVencimientos", CentralizaRFIFVencimientos::new,
            List.of(
                    // legacy CentralizaRFIFVencimientos.java:106-111
                    Spec.consulta("CentralizaRFIFVencimientos_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaRFIFVencimientos.java:116-119
                    Spec.consulta("CentralizaRFIFVencimientos_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaRFIFVencimientos.java:123-127
                    Spec.consulta("CentralizaRFIFVencimientos_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE, "DocumentoIdentidad", RUT_CORREDORA, "LocSistema", LOC_SISTEMA))),
            // legacy CentralizaRFIFVencimientos.java:132-191 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("CentroCosto", "CentroCosto", STR),
                    Spec.campo("CodigoAgente", "CodigoAgente", STR),
                    Spec.campo("CodigoBolsa", "CodigoBolsa", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("CodigoSucursal", "CodigoSucursal", STR),
                    Spec.campo("CodigoTipoOperacion", "CodigoTipoOperacion", STR),
                    Spec.campo("CodSecuencia", "CodSecuencia", CONST),
                    Spec.campo("DocumentoIdentidadCAVALI", "DocumentoIdentidadCAVALI", STR),
                    Spec.campo("DiasLiquidacion", null, CONST),
                    Spec.campo("DiasVencido", "DiasVencido", CONST),
                    Spec.campo("DiasVencimiento", "DiasVencimiento", CONST),
                    Spec.campo("EmisorInstrumento", "EmisorInstrumento", STR),
                    Spec.campo("EstadoMovimiento", "EstadoMovimiento", STR),
                    Spec.campo("FamiliaInstrumento", "FamiliaInstrumento", STR),
                    Spec.campo("FechaEmisionDocumento", "FechaEmisionDocumento", DATE),
                    Spec.campo("FechaLiquidacionDocumento", "FechaLiquidacionDocumento", DATE),
                    Spec.campo("IndicadorLiquidacion", "IndicadorLiquidacion", STR),
                    Spec.campo("IndicadorSimultanea", "IndicadorSimultanea", STR),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("ProductoMovimiento", "ProductoMovimiento", STR),
                    Spec.campo("RelacionadoEmpresa", "RelacionadoEmpresa", STR),
                    Spec.campo("RelacionCorredora", "RelacionCorredora", STR),
                    Spec.campo("FamiliaTipoInstrumento", "FamiliaTipoInstrumento", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("TipoFamilia", "TipoFamilia", STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("IdCartera", "IdCartera", CONST),
                    Spec.campo("TipoDocumento", "TipoDocumento", STR),
                    Spec.campo("VentaCorta", "VentaCorta", STR),
                    Spec.campo("TipoCmbteCont", "TipoCmbteCont", STR),
                    Spec.campo("FolioCmbteCont", "FolioCmbteCont", STR),
                    Spec.campo("Concepto", "Concepto", STR),
                    Spec.campo("Glosa", "Glosa", STR),
                    Spec.campo("FechaDeVencimiento", "FechaDeVencimiento", DATE),
                    Spec.campo("FechaAnterior", "FechaAnterior", DATE),
                    Spec.campo("TasaMercadoActual", "TasaMercadoActual", CONST),
                    Spec.campo("PrecioMercadoActual", "PrecioMercadoActual", CONST),
                    Spec.campo("TasaMercadoAnterior", "TasaMercadoAnterior", CONST),
                    Spec.campo("PrecioMercadoAnterior", "PrecioMercadoAnterior", CONST),
                    Spec.campo("TasaCompra", "TasaCompra", DECIMAL),
                    Spec.campo("PrecioCompra", "PrecioCompra", DECIMAL),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("TasaCambio", "TasaCambio", DECIMAL),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("OpCentros", "OpCentros", STR),
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("Mesa", "Mesa", STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR)));

    // legacy CentralizaRFIFVencimientos.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-112: los SQL legacy del tipo existen en el classpath")
    void contF112_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaRFIFVencimientos.java:106-111,116-119,123-127
    @Test
    @DisplayName("CONT-F-113: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF113_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaRFIFVencimientos.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-114: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF114_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaRFIFVencimientos.java:106-111,116-119 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-115: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF115_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaRFIFVencimientos.java:132-191
    @Test
    @DisplayName("CONT-F-116: TabDatosMovimiento: 57 TipoDato por fila, orden y formato legacy")
    void contF116_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(57 * 2 * 1);
        Casos.literal(datos, "FechaEmisionDocumento", "2025-02-19");
    }

    // legacy CentralizaRFIFVencimientos.java:132-191 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-117: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF117_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaRFIFVencimientos.java:132-191 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-118: una fecha nula aborta la centralizacion del tipo")
    void contF118_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaRFIFVencimientos.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-119: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF119_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-120: simulaOperacion N persiste una vez las tablas producidas")
    void contF120_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-121: simulaOperacion S no persiste")
    void contF121_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-122: consultas vacias producen salida vacia sin errores")
    void contF122_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
