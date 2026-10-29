package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaVencimientoPactos;
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
 * WP-F characterization of CentralizaVencimientoPactos.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaVencimientoPactos.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaVencimientoPactosCharacterizationTest {

    static final String MOV = "CentralizaVencimientoPactos_SelectMovimientos.sql";
    static final String EVE = "CentralizaVencimientoPactos_SelectEventosSubyacente.sql";
    static final String DAT = "CentralizaVencimientoPactos_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaVencimientoPactos", CentralizaVencimientoPactos::new,
            List.of(
                    // legacy CentralizaVencimientoPactos.java:86-90
                    Spec.consulta("CentralizaVencimientoPactos_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaVencimientoPactos.java:69-83,103-111
                    Spec.consulta("CentralizaVencimientoPactos_SelectEventosSubyacente.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO, "p_FechaAnterior", FECHA_ANTERIOR)),
                    // legacy CentralizaVencimientoPactos.java:113-114
                    Spec.consulta("CentralizaVencimientoPactos_SelectEventosOperacion.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaVencimientoPactos.java:118-120
                    Spec.consulta("CentralizaVencimientoPactos_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE))),
            // legacy CentralizaVencimientoPactos.java:124-148 (one add(...) per line, in this order)
            List.of(
                    Spec.campo("IdMovimiento", "IdMovimiento", VALUEOF_LONG),
                    Spec.campo("DocumentoIdentidad", "DocumentoIdentidad", STR),
                    Spec.campo("NombreCliente", "NombreCliente", STR),
                    Spec.campo("PortafolioCliente", "Portafolio", STR),
                    Spec.campo("Moneda", "Moneda", STR),
                    Spec.campo("Producto", "Producto", STR),
                    Spec.campo("ClaseInstrumento", "ClaseInstrumento", STR),
                    Spec.campo("TipoInstrumento", "TipoInstrumento", STR),
                    Spec.campo("Nemo", "Nemo", STR),
                    Spec.campo("Boveda", "Boveda", STR),
                    Spec.campo("IdCartera", "IdCartera", STR),
                    Spec.campo("Emisor", "Emisor", STR),
                    Spec.campo("IdTipoReajuste", "TipoReajuste", STR),
                    Spec.campo("TipoEmisor", "TipoEmisor", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("CompraVenta", "CompraVenta", STR),
                    Spec.campo("Mesa", "Mesa", STR)));

    // legacy CentralizaVencimientoPactos.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-223: los SQL legacy del tipo existen en el classpath")
    void contF223_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaVencimientoPactos.java:86-90,69-83,103-111,113-114,118-120
    @Test
    @DisplayName("CONT-F-224: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF224_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaVencimientoPactos.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-225: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF225_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaVencimientoPactos.java:86-90,69-83,103-111,113-114 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-226: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF226_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaVencimientoPactos.java:124-148
    @Test
    @DisplayName("CONT-F-227: TabDatosMovimiento: 25 TipoDato por fila, orden y formato legacy")
    void contF227_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(25 * 2 * 1);
        Casos.literal(datos, "IdMovimiento", "7001");
    }

    // legacy CentralizaVencimientoPactos.java:124-148 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-228: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF228_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaVencimientoPactos.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-229: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF229_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-230: simulaOperacion N persiste una vez las tablas producidas")
    void contF230_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-231: simulaOperacion S no persiste")
    void contF231_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-232: consultas vacias producen salida vacia sin errores")
    void contF232_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


    // Legacy CentralizaVencimientoPactos.java:94-112: CONTABILIDAD|DetalleEventoCentralizacionVP equalsIgnoreCase "S"
    // switches the subyacente SQL to *_SelectEventosSubyacenteDetalle.sql (same params, incl. p_FechaAnterior).
    @Test
    @DisplayName("CONT-F-233: DetalleEventoCentralizacionVP=s usa SelectEventosSubyacenteDetalle (legacy :94-112)")
    void contF233_detalleEventoUsaConsultaDetalle() {
        Escenario e = new Escenario();
        e.config.put("CONTABILIDAD|DetalleEventoCentralizacionVP", "s");
        e.ejecutar(TIPO, "S");
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosSubyacenteDetalle.sql")).singleElement()
                .extracting(Escenario.SqlCall::params)
                .isEqualTo(Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE,
                        "p_IdTipoCentralizacion", ID_TIPO, "p_FechaAnterior", FECHA_ANTERIOR));
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosSubyacente.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosOperacion.sql")).hasSize(1);
    }
}
