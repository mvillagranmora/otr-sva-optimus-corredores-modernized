package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaTomaDepositosAutodesembolso;
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
 * WP-F characterization of CentralizaTomaDepositosAutodesembolso.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaTomaDepositosAutodesembolso.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaTomaDepositosAutodesembolsoCharacterizationTest {

    static final String MOV = "CentralizaTomaDepositosAutodesembolso_SelectMovimientos.sql";
    static final String EVE = "CentralizaTomaDepositosAutodesembolso_SelectEventos.sql";
    static final String DAT = "CentralizaTomaDepositosAutodesembolso_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaTomaDepositosAutodesembolso", CentralizaTomaDepositosAutodesembolso::new,
            List.of(
                    // legacy CentralizaTomaDepositosAutodesembolso.java:96-100
                    Spec.consulta("CentralizaTomaDepositosAutodesembolso_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaTomaDepositosAutodesembolso.java:104-107
                    Spec.consulta("CentralizaTomaDepositosAutodesembolso_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaTomaDepositosAutodesembolso.java:111-114
                    Spec.consulta("CentralizaTomaDepositosAutodesembolso_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE, "IdEmpresa", ID_EMPRESA))),
            // legacy CentralizaTomaDepositosAutodesembolso.java:118-173 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("CentroCosto", "CentroCosto", STR),
                    Spec.campo("CodigoAgente", "CodigoAgente", STR),
                    Spec.campo("CodigoBolsa", "CodigoBolsa", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("CodigoSucursal", "CodigoSucursal", STR),
                    Spec.campo("CodigoTipoOperacion", "CodigoTipoOperacion", STR),
                    Spec.campo("CodSecuencia", "CodSecuencia", CONST),
                    Spec.campo("DiasLiquidacion", "DiasLiquidacion", STR),
                    Spec.campo("DiasVencido", "DiasVencido", STR),
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
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("FamiliaTipoInstrumento", "FamiliaTipoInstrumento", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("TipoFamilia", "TipoFamilia", STR),
                    Spec.campo("TipoReajuste", "TipoReajuste", STR),
                    Spec.campo("IdCartera", "IdCartera", CONST),
                    Spec.campo("TipoDocumento", "TipoDocumento", STR),
                    Spec.campo("Concepto", "Concepto", STR),
                    Spec.campo("Glosa", "Glosa", STR),
                    Spec.campo("FechaDeVencimiento", "FechaDeVencimiento", DATE),
                    Spec.campo("FechaAnterior", "FechaAnterior", DATE),
                    Spec.campo("FechaActual", "FechaActual", DATETIME),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("DocumentoIdentidadCAVALI", "DocumentoIdentidadCAVALI", STR),
                    Spec.campo("TasaMercadoActual", "TasaMercadoActual", DECIMAL),
                    Spec.campo("PrecioMercadoActual", "PrecioMercadoActual", DECIMAL),
                    Spec.campo("TasaMercadoAnterior", "TasaMercadoAnterior", DECIMAL),
                    Spec.campo("PrecioMercadoAnterior", "PrecioMercadoAnterior", DECIMAL),
                    Spec.campo("TasaEmision", "TasaEmision", STR),
                    Spec.campo("TasaCompra", "TasaCompra", DECIMAL),
                    Spec.campo("PrecioCompra", "PrecioCompra", DECIMAL),
                    Spec.campo("TasaCambio", "TasaCambio", DECIMAL)));

    // legacy CentralizaTomaDepositosAutodesembolso.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-211: los SQL legacy del tipo existen en el classpath")
    void contF211_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java:96-100,104-107,111-114
    @Test
    @DisplayName("CONT-F-212: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF212_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-213: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF213_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java:96-100,104-107 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-214: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF214_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java:118-173
    @Test
    @DisplayName("CONT-F-215: TabDatosMovimiento: 52 TipoDato por fila, orden y formato legacy")
    void contF215_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(52 * 2 * 1);
        Casos.literal(datos, "FechaEmisionDocumento", "2025-02-17");
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java:118-173 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-216: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF216_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java:118-173 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-217: una fecha nula aborta la centralizacion del tipo")
    void contF217_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaTomaDepositosAutodesembolso.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-218: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF218_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-219: simulaOperacion N persiste una vez las tablas producidas")
    void contF219_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-220: simulaOperacion S no persiste")
    void contF220_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-221: consultas vacias producen salida vacia sin errores")
    void contF221_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


    // Legacy CentralizaTomaDepositosAutodesembolso.java:159 FechaActual = Fecha.toStringDatetime(Timestamp) -> yyyy-MM-dd HH:mm:ss (no millis).
    @Test
    @DisplayName("CONT-F-222: FechaActual se formatea yyyy-MM-dd HH:mm:ss sin milisegundos (legacy :159)")
    void contF222_fechaActualSinMilisegundos() {
        Escenario e = new Escenario();
        e.filas(DAT, Filas.datos(TIPO, 0, false));
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.tipoDato().equals("FechaActual"))
                .extracting(d -> d.valorVarChar()).containsExactly("2025-03-10 14:05:09", "2025-03-10 15:05:09");
    }
}
