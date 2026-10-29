package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.ResultadoProceso_Salida;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.CrearTraspaso} (behaviours TES-A-740 .. TES-A-752): a traspaso is an income and an outgoing movement that share the
 * absolute value of their NumeroReferencia. The incomes are created first through {@link CrearIngresoEgresoService}, the outgoing movements
 * follow (each one pointing at its income through SecuenciaRelacionada) and finally every income is related back to its outgoing movement.
 * The modern code calls {@code CrearIngresoEgresoService.logica} in process, as the legacy did.
 *
 * <p>Legacy defects reproduced on purpose: after NO_EXISTE_INGRESO_ASOCIADO the process goes on and fails later (TES-A-742); the references match on
 * absolute value but the payment forms on the exact value, so two references with the same sign both get every payment form (TES-A-748).
 */
@Service
public class CrearTraspasoService {

    private static final String SQL_UPDATE_INGRESO = loadSql("sql/CrearTraspaso_UpdateIngreso.sql");
    /** The legacy {@code call} used a plain Gson (no nulls written). */
    private static final Gson GSON = new Gson();

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

        public static class CrearTraspaso_Entrada{
            public Long                            p_IdEmpresa;
            public Long                            p_IdUsuario;
            public String                        p_IdModulo;
            public String                        p_SimularOperacion = "N";
            public String                        p_UsaReglasNegocio;        
            public String                       p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
            public String                        p_FechaMovimiento = "0001-01-01";   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv

            public List<IngresoEgreso_Entrada>    p_IngresoEgresoGenerar;
            public List<FormaDePago_Entrada>    p_FormaDePago;

        }

        public static class CrearTraspasos_Salida {

            public List<MensajeSalida>                    P_OUT_MENSAJES;
            public Long                                    P_OUT_ERRORESCONTADOR;
            public List<IngresoEgresoGenerado_Salida>    P_OUT_INGRESOEGRESOGENERADO;
            public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;

            public CrearTraspasos_Salida()
            {    
                this.P_OUT_MENSAJES              = new ArrayList<MensajeSalida>();
                this.P_OUT_INGRESOEGRESOGENERADO = new ArrayList<IngresoEgresoGenerado_Salida>();
                this.P_OUT_RESULTADOPROCESO         = new ArrayList<ResultadoProceso_Salida>();
                this.P_OUT_ERRORESCONTADOR = 0L;
            }

            public String toString() {
                return Utils.printJson(this, false);
            }
        }

        public static class UpdateIngreso {
            public Long IdIngresoEgreso;
            public Long SecuenciaRelacionada;

            public String toString() {
                return Utils.printJson(this, false);
            }
        }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;
    private final CrearIngresoEgresoService crearIngresoEgreso;

    public CrearTraspasoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, CrearIngresoEgresoService crearIngresoEgreso) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
        this.crearIngresoEgreso = crearIngresoEgreso;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        CrearTraspaso_Entrada in = GSON.fromJson(entrada, CrearTraspaso_Entrada.class);
        return GSON.toJsonTree(logica(in)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public CrearTraspasos_Salida logica(CrearTraspaso_Entrada in) throws Exception {
        CrearTraspasos_Salida out = new CrearTraspasos_Salida();
        List<IngresoEgreso_Entrada> ingresos = new ArrayList<>();
        List<FormaDePago_Entrada> formasIngresos = new ArrayList<>();
        List<IngresoEgreso_Entrada> egresos = new ArrayList<>();
        List<FormaDePago_Entrada> formasEgresos = new ArrayList<>();

        // every income needs its outgoing movement and its payment forms
        for (IngresoEgreso_Entrada ingreso : in.p_IngresoEgresoGenerar) {
            if (!"MOVING".equalsIgnoreCase(ingreso.IdTipoMovimiento)) {
                continue;
            }
            if (!existeContraparte(in, "MOVEGR", ingreso)) {
                error(in, out, ingreso.NumeroReferencia, "NO_EXISTE_EGRESO_ASOCIADO");
                continue;
            }
            ingresos.add(ingreso);
            if (in.p_FormaDePago.isEmpty()) {
                error(in, out, ingreso.NumeroReferencia, "REQUIERE_FORMA_PAGO_TRASPASO");
            } else {
                for (FormaDePago_Entrada forma : in.p_FormaDePago) {
                    if (ingreso.NumeroReferencia.longValue() == forma.NumeroReferencia.longValue()) {
                        formasIngresos.add(forma);
                    }
                }
            }
        }
        if (out.P_OUT_ERRORESCONTADOR.longValue() > 0) {
            return out;
        }

        // every outgoing movement needs its income
        for (IngresoEgreso_Entrada egreso : in.p_IngresoEgresoGenerar) {
            if (!"MOVEGR".equalsIgnoreCase(egreso.IdTipoMovimiento)) {
                continue;
            }
            if (existeContraparte(in, "MOVING", egreso)) {
                egresos.add(egreso);
                if (!in.p_FormaDePago.isEmpty()) {
                    for (FormaDePago_Entrada forma : in.p_FormaDePago) {
                        if (egreso.NumeroReferencia.longValue() == forma.NumeroReferencia.longValue()) {
                            formasEgresos.add(forma);
                        }
                    }
                }
            } else {
                // LEGACY DEFECT kept (see README TES-A-742): the process continues after this error and fails later
                error(in, out, egreso.NumeroReferencia, "NO_EXISTE_INGRESO_ASOCIADO");
            }
        }

        // incomes
        CrearIngresoEgreso_Salida salidaIngresos = crearIngresoEgreso.logica(entradaDeCreacion(in, ingresos, formasIngresos));
        acumular(out, salidaIngresos);
        if (out.P_OUT_ERRORESCONTADOR.longValue() > 0) {
            return out;
        }

        // each outgoing movement points at the income it belongs to
        for (IngresoEgreso_Entrada egreso : egresos) {
            for (IngresoEgresoGenerado_Salida salida : salidaIngresos.P_OUT_INGRESOEGRESOGENERADO) {
                if (Math.abs(salida.NumeroReferencia.longValue()) == Math.abs(egreso.NumeroReferencia.longValue())) {
                    egreso.SecuenciaRelacionada = salida.IdIngresoEgreso;
                    break;
                }
            }
        }

        // outgoing movements
        CrearIngresoEgreso_Salida salidaEgresos = crearIngresoEgreso.logica(entradaDeCreacion(in, egresos, formasEgresos));
        acumular(out, salidaEgresos);
        if (out.P_OUT_ERRORESCONTADOR.longValue() > 0) {
            return out;
        }

        // each income is related back to its outgoing movement
        List<UpdateIngreso> actualizar = new ArrayList<>();
        for (IngresoEgresoGenerado_Salida ingreso : salidaIngresos.P_OUT_INGRESOEGRESOGENERADO) {
            for (IngresoEgresoGenerado_Salida egreso : salidaEgresos.P_OUT_INGRESOEGRESOGENERADO) {
                if (Math.abs(egreso.NumeroReferencia.longValue()) == Math.abs(ingreso.NumeroReferencia.longValue())) {
                    UpdateIngreso u = new UpdateIngreso();
                    u.SecuenciaRelacionada = egreso.IdIngresoEgreso;
                    u.IdIngresoEgreso = ingreso.IdIngresoEgreso;
                    actualizar.add(u);
                }
            }
        }
        Consultas.lote(jdbc, SQL_UPDATE_INGRESO, Utils.List2ListHashGenericObject(actualizar));

        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return out;
    }

    /** True when the request holds a movement of {@code tipo} whose reference has the same absolute value as {@code movimiento}'s. */
    private static boolean existeContraparte(CrearTraspaso_Entrada in, String tipo, IngresoEgreso_Entrada movimiento) {
        for (IngresoEgreso_Entrada otro : in.p_IngresoEgresoGenerar) {
            if (tipo.equalsIgnoreCase(otro.IdTipoMovimiento) && Math.abs(otro.NumeroReferencia.longValue()) == Math.abs(movimiento.NumeroReferencia.longValue())) {
                return true;
            }
        }
        return false;
    }

    private void error(CrearTraspaso_Entrada in, CrearTraspasos_Salida out, Long referencia, String codigo) {
        out.P_OUT_ERRORESCONTADOR += 1L;
        out.P_OUT_MENSAJES.add(new MensajeSalida(referencia, MensajeSalida.MENSAJE_ERROR, codigo, diccionario.mensaje(in.p_IdEmpresa, codigo, new HashMap<>())));
    }

    private static CrearIngresoEgreso_Entrada entradaDeCreacion(CrearTraspaso_Entrada in, List<IngresoEgreso_Entrada> movimientos,
            List<FormaDePago_Entrada> formasDePago) {
        CrearIngresoEgreso_Entrada entrada = new CrearIngresoEgreso_Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_SimularOperacion = in.p_SimularOperacion;
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_EsIngresoAutomatico = in.p_EsIngresoAutomatico;
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_IngresoEgresoGenerar = movimientos;
        entrada.p_FormaDePago = formasDePago;
        return entrada;
    }

    /** The error counter is the one of the last step (it replaces, it does not add up), the lists accumulate. */
    private static void acumular(CrearTraspasos_Salida out, CrearIngresoEgreso_Salida paso) {
        out.P_OUT_ERRORESCONTADOR = paso.P_OUT_ERRORESCONTADOR == null ? 0L : paso.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES.addAll(paso.P_OUT_MENSAJES);
        out.P_OUT_INGRESOEGRESOGENERADO.addAll(paso.P_OUT_INGRESOEGRESOGENERADO);
        out.P_OUT_RESULTADOPROCESO.addAll(paso.P_OUT_RESULTADOPROCESO);
    }
}
