package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaLiquidacionFacturas;
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
 * WP-F characterization of CentralizaLiquidacionFacturas.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaLiquidacionFacturas.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaLiquidacionFacturasCharacterizationTest {

    static final String MOV = "CentralizaLiquidacionFacturas_SelectMovimientos.sql";
    static final String EVE = "CentralizaLiquidacionFacturas_SelectEventos.sql";
    static final String DAT = "CentralizaLiquidacionFacturas_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaLiquidacionFacturas", CentralizaLiquidacionFacturas::new,
            List.of(
                    // legacy CentralizaLiquidacionFacturas.java:79-81
                    Spec.consulta("CentralizaLiquidacionFacturas_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaLiquidacionFacturas.java:73,86-89
                    Spec.consulta("CentralizaLiquidacionFacturas_SelectEventos.sql", Spec.Rol.EVE, Spec.params("FechaContable", FECHA_CONTABLE, "IdTipoCentralizacion", ID_TIPO, "Sistema", LOC_SISTEMA)),
                    // legacy CentralizaLiquidacionFacturas.java:93-94
                    Spec.consulta("CentralizaLiquidacionFacturas_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("FechaContable", FECHA_CONTABLE))),
            // legacy CentralizaLiquidacionFacturas.java:98-129 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("TipoDocumento", "TipoDocumento", STR),
                    Spec.campo("Folio", "Folio", VALUEOF_LONG),
                    Spec.campo("FechaDocumento", "FechaDocumento", STR),
                    Spec.campo("CompraVenta", "CompraVenta", STR),
                    Spec.campo("Operacion", "Operacion", STR),
                    Spec.campo("Producto", "Producto", STR),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("MonedaComprobante", "MonedaComprobante", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("Portafolio", "Portafolio", STR),
                    Spec.campo("Concepto", "Concepto", STR),
                    Spec.campo("NombreConcepto", "NombreConcepto", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("EstadoSUNAT", "EstadoSUNAT", STR),
                    Spec.campo("DocumentoIdentidadCAVALI", "DocumentoIdentidadCAVALI", STR),
                    Spec.campo("EsExento", "EsExento", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("MonedaLiquidacion", "MonedaLiquidacion", STR),
                    Spec.campo("EsContraparte", "EsContraparte", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("Nemotecnico", "Nemotecnico", STR),
                    Spec.campo("CondicionLiquidacion", "CondicionLiquidacion", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR)));

    // legacy CentralizaLiquidacionFacturas.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-023: los SQL legacy del tipo existen en el classpath")
    void contF023_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaLiquidacionFacturas.java:79-81,73,86-89,93-94
    @Test
    @DisplayName("CONT-F-024: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF024_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaLiquidacionFacturas.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-025: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF025_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaLiquidacionFacturas.java:79-81,73,86-89 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-026: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF026_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaLiquidacionFacturas.java:98-129
    @Test
    @DisplayName("CONT-F-027: TabDatosMovimiento: 32 TipoDato por fila, orden y formato legacy")
    void contF027_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(32 * 2 * 1);
        Casos.literal(datos, "TipoDocumento", "TipoDocumento-1");
    }

    // legacy CentralizaLiquidacionFacturas.java:98-129 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-028: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF028_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaLiquidacionFacturas.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-029: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF029_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-030: simulaOperacion N persiste una vez las tablas producidas")
    void contF030_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-031: simulaOperacion S no persiste")
    void contF031_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-032: consultas vacias producen salida vacia sin errores")
    void contF032_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
