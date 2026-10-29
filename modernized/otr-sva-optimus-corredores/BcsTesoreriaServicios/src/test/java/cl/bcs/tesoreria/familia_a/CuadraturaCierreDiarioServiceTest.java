package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CuadraturaCierreDiarioService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CuadraturaCierreDiario} (entry point call(conn, json)) as {@link CuadraturaCierreDiarioService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-780 .. TES-A-795} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CuadraturaCierreDiarioServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-780_cua-certificacion-sin-operaciones-queda-SIN-OPERACIONES",
            "TES-A-781_cua-certificacion-con-todas-las-formas-de-pago-certificadas-queda-CUADRADO",
            "TES-A-782_cua-certificacion-con-formas-de-pago-ingresadas-queda-DESCUADRADO-y-detalla-NO-CERTIFICADO",
            "TES-A-783_cua-sin-comprobante-sin-movimientos-queda-SIN-OPERACIONES",
            "TES-A-784_cua-sin-comprobante-con-movimientos-emitidos-queda-CUADRADO",
            "TES-A-785_cua-sin-comprobante-con-movimientos-ingresados-queda-DESCUADRADO-y-detalla-SIN-COMPROBANTE",
            "TES-A-786_cua-ambos-procesos-en-la-misma-llamada",
            "TES-A-787_cua-proceso-desconocido-solo-registra-el-log-y-no-modifica-el-resumen",
            "TES-A-788_cua-proceso-sin-fila-en-el-resumen-borra-e-inserta-nada",
            "TES-A-789_cua-solo-considera-la-fecha-de-liquidacion-informada",
            "TES-A-790_cua-sin-fecha-de-sistema-el-log-falla-pero-el-proceso-termina-bien",
            "TES-A-791_cua-consulta-devuelve-el-detalle-del-primer-resumen",
            "TES-A-792_cua-consulta-sin-resumen-no-devuelve-detalle",
            "TES-A-793_cua-consulta-de-otra-empresa-no-devuelve-detalle",
            "TES-A-794_cua-tipo-de-llamado-desconocido-no-hace-nada",
            "TES-A-795_cua-tipo-de-llamado-ausente-lanza-NPE");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        CuadraturaCierreDiarioService service = new CuadraturaCierreDiarioService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CuadraturaCierreDiario");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-780 cua certificacion sin operaciones queda SIN OPERACIONES")
    void tesA780_cuaCertificacionSinOperacionesQuedaSINOPERACIONES() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-781 cua certificacion con todas las formas de pago certificadas queda CUADRADO")
    void tesA781_cuaCertificacionConTodasLasFormasDePagoCertificadasQuedaCUADRADO() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-782 cua certificacion con formas de pago ingresadas queda DESCUADRADO y detalla NO CERTIFICADO")
    void tesA782_cuaCertificacionConFormasDePagoIngresadasQuedaDESCUADRADOYDetallaNOCERTIFICADO() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-783 cua sin comprobante sin movimientos queda SIN OPERACIONES")
    void tesA783_cuaSinComprobanteSinMovimientosQuedaSINOPERACIONES() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-784 cua sin comprobante con movimientos emitidos queda CUADRADO")
    void tesA784_cuaSinComprobanteConMovimientosEmitidosQuedaCUADRADO() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-785 cua sin comprobante con movimientos ingresados queda DESCUADRADO y detalla SIN COMPROBANTE")
    void tesA785_cuaSinComprobanteConMovimientosIngresadosQuedaDESCUADRADOYDetallaSINCOMPROBANTE() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-786 cua ambos procesos en la misma llamada")
    void tesA786_cuaAmbosProcesosEnLaMismaLlamada() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-787 cua proceso desconocido solo registra el log y no modifica el resumen")
    void tesA787_cuaProcesoDesconocidoSoloRegistraElLogYNoModificaElResumen() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-788 cua proceso sin fila en el resumen borra e inserta nada")
    void tesA788_cuaProcesoSinFilaEnElResumenBorraEInsertaNada() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-789 cua solo considera la fecha de liquidacion informada")
    void tesA789_cuaSoloConsideraLaFechaDeLiquidacionInformada() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-790 cua sin fecha de sistema el log falla pero el proceso termina bien")
    void tesA790_cuaSinFechaDeSistemaElLogFallaPeroElProcesoTerminaBien() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-791 cua consulta devuelve el detalle del primer resumen")
    void tesA791_cuaConsultaDevuelveElDetalleDelPrimerResumen() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-792 cua consulta sin resumen no devuelve detalle")
    void tesA792_cuaConsultaSinResumenNoDevuelveDetalle() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-793 cua consulta de otra empresa no devuelve detalle")
    void tesA793_cuaConsultaDeOtraEmpresaNoDevuelveDetalle() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-794 cua tipo de llamado desconocido no hace nada")
    void tesA794_cuaTipoDeLlamadoDesconocidoNoHaceNada() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-795 cua tipo de llamado ausente lanza NPE")
    void tesA795_cuaTipoDeLlamadoAusenteLanzaNPE() throws Exception {
        verificar(CASOS.get(15));
    }
}
