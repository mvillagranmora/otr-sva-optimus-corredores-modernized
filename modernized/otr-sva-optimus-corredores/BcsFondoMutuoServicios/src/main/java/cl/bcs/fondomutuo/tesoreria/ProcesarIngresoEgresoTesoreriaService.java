package cl.bcs.fondomutuo.tesoreria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.Empleado;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.ModalidadPago;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.PreferenciaPagoCorredor;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.PreferenciaPagoPortafolio;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.ResultadoProceso_Salida;

/**
 * Legacy {@code cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria} (370 LOC, {@code logica} CCN 40; FM-TES-01..13, FM-NOR-90/91), ported bug for bug for the
 * F2 W1. It builds one Tesoreria movement + payment form per request ({@link TesoreriaArmado}) and calls Tesoreria's TYPED entry point
 * {@code CrearIngresoEgresoService.logica(CrearIngresoEgreso_Entrada)} (C-W0A-6) IN PROCESS: the bean is {@code @Transactional} REQUIRED and joins the Spring
 * transaction of the request (C1), so whatever Tesoreria writes is committed or rolled back with the FondoMutuo request (the legacy passed its
 * {@code Connection}). The connection is not a parameter (DEV-4A-6, as Normal). No state; no logging of the input (DEV-F7).
 *
 * <p>Kept from the legacy: no check of an empty list (FM-TES-01: the callers filter); the four reads always run in the same order (the corredor preference
 * even when no request uses it, FM-TES-05); {@code Collectors.toMap} over the payment modalities (duplicate medium = {@code IllegalStateException}) and
 * {@code List2Hash} over the two preferences (duplicate key = {@code Exception("key field is not unique")}) (D-TES-2, D-NOR-9); {@code p_SimularOperacion}
 * null becomes "N" and {@code p_EsIngresoAutomatico} is passed as given (null overrides Tesoreria's default "S"); {@code p_IdOrigenProceso},
 * {@code p_EsIngresoUI}, {@code p_FechaMovimiento} keep Tesoreria's defaults (FM-TES-12); the output is Tesoreria's, field by field. An exception propagates.
 * DEV-F5: a failure of one of Tesoreria's HTTP collaborators, which Tesoreria swallows, is rethrown ({@link ColaboradoresTesoreria#vigilar}).
 */
public class ProcesarIngresoEgresoTesoreriaService implements ProcesosExternos.TesoreriaPort {

    /** Tesoreria's typed entry point ({@code CrearIngresoEgresoService::logica} in production). */
    @FunctionalInterface
    public interface CrearIngresoEgresoPuerto {
        CrearIngresoEgreso_Salida logica(CrearIngresoEgreso_Entrada entrada) throws Exception;
    }

    /** Legacy {@code SalidaProcesarIngresoEgresoTesoreria} (Tesoreria's own output types). */
    public static class Salida {
        public List<cl.bcs.tesoreria.javacall.comun.MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<IngresoEgresoGenerado_Salida> P_OUT_INGRESOEGRESOGENERADO = new ArrayList<>();
        public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO = new ArrayList<>();
    }

    private final TesoreriaDatos datos;
    private final CrearIngresoEgresoPuerto crearIngresoEgreso;

    public ProcesarIngresoEgresoTesoreriaService(TesoreriaDatos datos, CrearIngresoEgresoPuerto crearIngresoEgreso) {
        this.datos = datos;
        this.crearIngresoEgreso = crearIngresoEgreso;
    }

    /** Legacy {@code logica(con, entrada)} ({@code :135-325}). */
    public Salida logica(ProcesosExternos.EntradaTesoreria entrada) throws Exception {
        Long idEmpleadoDigitador = 0L;
        Salida out = new Salida();
        CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();

        List<Empleado> empleadoDigitador = datos.empleados(entrada.p_IdUsuario);
        if (empleadoDigitador.size() > 0) {
            idEmpleadoDigitador = empleadoDigitador.get(0).IdEmpleado;
        }
        List<ModalidadPago> listaModalidadPago = datos.modalidadesPago();
        // DEV-4A-5: the legacy parallelStream is sequential here (same map; only the text of a duplicate-key exception may differ)
        Map<String, ModalidadPago> hashModalidadPago = listaModalidadPago.stream().collect(Collectors.toMap(modalidad -> modalidad.IdMedioDePago, modalidad -> modalidad));

        List<Long> lstIdPreferenciaPagoPortafolio = new ArrayList<>();
        Map<Long, PreferenciaPagoPortafolio> hashPreferenciaPagoPortafolio = new HashMap<>();
        Map<String, PreferenciaPagoCorredor> hashPreferenciaPagoCorredor = new HashMap<>();
        for (SolicitudAporteRescate sol : entrada.p_Solicitudes) {
            if (sol.IdPreferenciaPagoPortafolio != null) lstIdPreferenciaPagoPortafolio.add(sol.IdPreferenciaPagoPortafolio);
        }
        if (lstIdPreferenciaPagoPortafolio.size() > 0) {
            List<PreferenciaPagoPortafolio> lst = datos.preferenciasPagoPortafolio(lstIdPreferenciaPagoPortafolio);
            hashPreferenciaPagoPortafolio = ListaHash.aHash(lst, p -> p.IdPreferenciaPagoPortafolio);
        }
        List<PreferenciaPagoCorredor> lstPreferenciaPagoCorredor = datos.preferenciasPagoCorredor(entrada.p_IdEmpresa);
        if (lstPreferenciaPagoCorredor.size() > 0) {
            hashPreferenciaPagoCorredor = ListaHash.aHash(lstPreferenciaPagoCorredor, p -> p.IdMoneda);
        }

        for (SolicitudAporteRescate sol : entrada.p_Solicitudes) {
            // legacy: get(sol.IdPreferenciaPagoPortafolio != null ? sol.IdPreferenciaPagoPortafolio : 0): Long : int promotes to long, boxed to Long
            PreferenciaPagoPortafolio infoPreferenciaPagoPortafolio = hashPreferenciaPagoPortafolio.get(sol.IdPreferenciaPagoPortafolio != null ? sol.IdPreferenciaPagoPortafolio : 0);
            var ie = TesoreriaArmado.ingreso(sol, idEmpleadoDigitador);
            var fp = TesoreriaArmado.formaDePago(sol, hashModalidadPago, hashPreferenciaPagoCorredor, infoPreferenciaPagoPortafolio);
            entradaCrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie);
            entradaCrearIngresoEgreso.p_FormaDePago.add(fp);
        }
        entradaCrearIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCrearIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
        entradaCrearIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
        entradaCrearIngresoEgreso.p_SimularOperacion = entrada.p_SimularOperacion == null ? TesoreriaArmado.NO : entrada.p_SimularOperacion;
        entradaCrearIngresoEgreso.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        entradaCrearIngresoEgreso.p_EsIngresoAutomatico = entrada.p_EsIngresoAutomatico;
        CrearIngresoEgreso_Salida salida = ColaboradoresTesoreria.vigilar(() -> crearIngresoEgreso.logica(entradaCrearIngresoEgreso));
        out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
        out.P_OUT_INGRESOEGRESOGENERADO = salida.P_OUT_INGRESOEGRESOGENERADO;
        out.P_OUT_RESULTADOPROCESO = salida.P_OUT_RESULTADOPROCESO;
        out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
        return out;
    }

    /**
     * The view Normal reads ({@code ProcesosExternos.SalidaTesoreria}): the same messages, results and generated movements, field by field, in the same
     * order; a null list stays null (the legacy Normal iterates them directly and would fail the same way).
     */
    @Override
    public ProcesosExternos.SalidaTesoreria procesar(ProcesosExternos.EntradaTesoreria entrada) throws Exception {
        Salida s = logica(entrada);
        ProcesosExternos.SalidaTesoreria r = new ProcesosExternos.SalidaTesoreria();
        r.P_OUT_MENSAJES = s.P_OUT_MENSAJES == null ? null : new ArrayList<>();
        if (s.P_OUT_MENSAJES != null) {
            for (var m : s.P_OUT_MENSAJES) r.P_OUT_MENSAJES.add(m == null ? null : new MensajeSalida(m.id(), m.tipo(), m.codigo(), m.descripcion()));
        }
        r.P_OUT_RESULTADOPROCESO = s.P_OUT_RESULTADOPROCESO == null ? null : new ArrayList<>();
        if (s.P_OUT_RESULTADOPROCESO != null) {
            for (ResultadoProceso_Salida p : s.P_OUT_RESULTADOPROCESO) {
                ProcesosExternos.ResultadoProceso x = new ProcesosExternos.ResultadoProceso();
                x.Id = p.Id;
                x.Campo = p.Campo;
                x.Resultado = p.Resultado;
                r.P_OUT_RESULTADOPROCESO.add(x);
            }
        }
        r.P_OUT_INGRESOEGRESOGENERADO = s.P_OUT_INGRESOEGRESOGENERADO == null ? null : new ArrayList<>();
        if (s.P_OUT_INGRESOEGRESOGENERADO != null) {
            for (IngresoEgresoGenerado_Salida g : s.P_OUT_INGRESOEGRESOGENERADO) {
                ProcesosExternos.IngresoEgresoGenerado x = new ProcesosExternos.IngresoEgresoGenerado();
                x.NumeroReferencia = g.NumeroReferencia;
                x.IdIngresoEgreso = g.IdIngresoEgreso;
                r.P_OUT_INGRESOEGRESOGENERADO.add(x);
            }
        }
        return r;
    }
}
