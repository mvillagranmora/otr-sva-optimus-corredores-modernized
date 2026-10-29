package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.service.CentralizaSimultaneaAnticiposVencimientos;
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
 * WP-F characterization of CentralizaSimultaneaAnticiposVencimientos.
 * Oracle: legacy/otr-sva-optimus-corredores/BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/CentralizaSimultaneaAnticiposVencimientos.java (and the SQL files it loads).
 * Every expectation is transcribed from the legacy source; the modern class is only the subject.
 */
@SuppressWarnings("unused")
class CentralizaSimultaneaAnticiposVencimientosCharacterizationTest {

    static final String MOV = "CentralizaSimultaneaAnticiposVencimientos_SelectMovimientos.sql";
    static final String EVE = "CentralizaSimultaneaAnticiposVencimientos_SelectEventos.sql";
    static final String DAT = "CentralizaSimultaneaAnticiposVencimientos_SelectDatosMovimiento.sql";

    static final Spec.Tipo TIPO = new Spec.Tipo("CentralizaSimultaneaAnticiposVencimientos", CentralizaSimultaneaAnticiposVencimientos::new,
            List.of(
                    // legacy CentralizaSimultaneaAnticiposVencimientos.java:70-74
                    Spec.consulta("CentralizaSimultaneaAnticiposVencimientos_SelectMovimientos.sql", Spec.Rol.MOV, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaSimultaneaAnticiposVencimientos.java:78-81
                    Spec.consulta("CentralizaSimultaneaAnticiposVencimientos_SelectEventos.sql", Spec.Rol.EVE, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE, "p_IdTipoCentralizacion", ID_TIPO)),
                    // legacy CentralizaSimultaneaAnticiposVencimientos.java:85-88
                    Spec.consulta("CentralizaSimultaneaAnticiposVencimientos_SelectDatosMovimiento.sql", Spec.Rol.DAT, Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE))),
            // legacy CentralizaSimultaneaAnticiposVencimientos.java:92-125 (one add(...) per line, in this order)
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
                    Spec.campo("IdEmisor", "Emisor", STR),
                    Spec.campo("IdTipoReajuste", "TipoReajuste", STR),
                    Spec.campo("CodigoIFRS", "CodigoIFRS", STR),
                    Spec.campo("TipoCartera", "TipoCartera", STR),
                    Spec.campo("ClaseEmisor", "ClaseEmisor", STR),
                    Spec.campo("Agente", "Agente", STR),
                    Spec.campo("NombreAgente", "NombreAgente", STR),
                    Spec.campo("SucursalAgente", "SucursalAgente", STR),
                    Spec.campo("NombreSucursalAgente", "NombreSucursalAgente", STR),
                    Spec.campo("CentroCostoAgente", "CentroCostoAgente", STR),
                    Spec.campo("Relacion", "Relacion", STR),
                    Spec.campo("Bolsa", "Bolsa", STR),
                    Spec.campo("FechaLiquidacion", "FechaLiquidacion", DATE),
                    Spec.campo("TipoPersona", "TipoPersona", STR),
                    Spec.campo("FechaOperacion", "FechaOperacion", DATE),
                    Spec.campo("Concepto", "IdConcepto", STR),
                    Spec.campo("CostingCode", "CentroCostoAgente", STR),
                    Spec.campo("CostingCode2", "Producto", STR),
                    Spec.campo("CostingCode3", "Relacion", STR),
                    Spec.campo("CostingCode4", "Bolsa", STR),
                    Spec.campo("CostingCode5", null, CONST),
                    Spec.campo("TipoOperacion", "TipoOperacion", STR),
                    Spec.campo("ClienteBancario", "ClienteBancario", STR)));

    // legacy CentralizaSimultaneaAnticiposVencimientos.java SQL_* fields + BcsCentralizacion.java:86-90 (missing resource -> IllegalStateException)
    @Test
    @DisplayName("CONT-F-123: los SQL legacy del tipo existen en el classpath")
    void contF123_recursosSqlLegacyEnClasspath() {
        Casos.recursosSql(TIPO);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java:70-74,78-81,85-88
    @Test
    @DisplayName("CONT-F-124: cada SQL legacy corre una vez con sus parametros nombrados exactos y en orden")
    void contF124_consultasConParametrosLegacy() {
        Casos.consultas(TIPO);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java runs only its own SQL_* constants; ContabilidadJdbcRepository.java:72-114 runs AnticipoPactos SQL
    @Test
    @DisplayName("CONT-F-125: no ejecuta los SQL de CentralizaAnticipoPactos")
    void contF125_noEjecutaSqlDeAnticipoPactos() {
        Casos.noUsaSqlDeAnticipoPactos(TIPO);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java:70-74,78-81 (sql.query into TabMovimientos / TabEventos)
    @Test
    @DisplayName("CONT-F-126: P_TABMOVIMIENTOS y P_TABEVENTOS son las filas de las consultas propias")
    void contF126_movimientosYEventosDeConsultasPropias() {
        Casos.movimientosYEventos(TIPO);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java:92-125
    @Test
    @DisplayName("CONT-F-127: TabDatosMovimiento: 34 TipoDato por fila, orden y formato legacy")
    void contF127_datosMovimientoOrdenYFormato() {
        List<TabDatoMovimientoRef> datos = Casos.datos(TIPO, false);
        assertThat(datos).hasSize(34 * 2 * 1);
        Casos.literal(datos, "FechaLiquidacion", "2025-02-24");
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java:92-125 + pojo/TabDatosMovimiento.java:19-25
    @Test
    @DisplayName("CONT-F-128: TabDatosMovimiento con columnas nulas: String -> vacio, String.valueOf -> literal null, decimal -> 0")
    void contF128_datosMovimientoNulos() {
        Casos.datos(TIPO, true);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java:92-125 Fecha.stringToSqlDate(null) / null.toString() throw (BcsUtils Fecha.java:72-76)
    @Test
    @DisplayName("CONT-F-129: una fecha nula aborta la centralizacion del tipo")
    void contF129_fechaNulaAborta() {
        Casos.nuloEnFechaLanza(TIPO);
    }

    // legacy CentralizaSimultaneaAnticiposVencimientos.java llenarResultado -> BcsCentralizacion.java:100-111
    @Test
    @DisplayName("CONT-F-130: P_OUT_RESULTADO copia movimientos con Ejercicio String.valueOf")
    void contF130_resultadosDesdeMovimientos() {
        Casos.resultados(TIPO);
    }

    // CentralizaMultiSeleccion.java:343,354-357,411-417
    @Test
    @DisplayName("CONT-F-131: simulaOperacion N persiste una vez las tablas producidas")
    void contF131_persisteEnEjecucionReal() {
        Casos.persisteEnEjecucionReal(TIPO);
    }

    // CentralizaMultiSeleccion.java:411
    @Test
    @DisplayName("CONT-F-132: simulaOperacion S no persiste")
    void contF132_noPersisteEnSimulacion() {
        Casos.noPersisteEnSimulacion(TIPO);
    }

    // BcsCentralizacion.java:25-39; legacy test CentralizaInteresReajustePactosTest.java:86-103
    @Test
    @DisplayName("CONT-F-133: consultas vacias producen salida vacia sin errores")
    void contF133_salidaVaciaSinErrores() {
        Casos.vacio(TIPO);
    }


}
