package cl.bcs.fondomutuo.comisionnn;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.comisionnn.ComisionNnArmado.Armado;
import cl.bcs.fondomutuo.comisionnn.ComisionNnArmado.Datos;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Entrada;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Salida;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.ConteoErrores;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.FechaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;

/**
 * Legacy {@code cl.bcs.fondomutuo.CrearSolicitudRescateComisionNN} (634 LOC), rules FM-COM / FM-NOR-46. Creates the rescue requests that charge the
 * quarterly commission: dates and quotas come from the 3A classes (ports {@link FechaContrato.Puerto} and {@link CuotaContrato.Puerto}, bound in step 4),
 * the requirements check is {@link ServicioCumpleRequisitosService}, the audit line goes through the H port {@link LogPort} (gate G5).
 * Kept as legacy: the early exits set the counter to 1; every requirement message becomes an ADVERTENCIA with the cartera folio as id and only the
 * requests whose message was an ERROR are rejected; when nothing is left the answer has no result and a warning counter of 0; with
 * {@code p_SimularOperacion != S} the rows are inserted and the audit is written; the warnings counter is only set at the end.
 * DEV-F2: the time zone shift is local to the call (the legacy set a JVM-wide static).
 */
public class CrearSolicitudRescateComisionNnService {

    private static final String SECUENCIA_SOLICITUD = "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate";

    private final RevisarRequisitosBasicosPort basicos;
    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final DiccionarioMensajesPort diccionario;
    private final ComisionNnDatos datos;
    private final RequisitosDatos datosRequisitos;
    private final FechaContrato.Puerto fechas;
    private final CuotaContrato.Puerto cuotas;
    private final SecuenciaPort secuencias;
    private final ServicioCumpleRequisitosService requisitos;
    private final CalendarioPort calendario;
    private final LogPort log;

    public CrearSolicitudRescateComisionNnService(RevisarRequisitosBasicosPort basicos, ConfiguracionSistemaPort configuracion, EmpresaPort empresas,
            DiccionarioMensajesPort diccionario, ComisionNnDatos datos, RequisitosDatos datosRequisitos, FechaContrato.Puerto fechas,
            CuotaContrato.Puerto cuotas, SecuenciaPort secuencias, ServicioCumpleRequisitosService requisitos, CalendarioPort calendario, LogPort log) {
        this.basicos = basicos;
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.diccionario = diccionario;
        this.datos = datos;
        this.datosRequisitos = datosRequisitos;
        this.fechas = fechas;
        this.cuotas = cuotas;
        this.secuencias = secuencias;
        this.requisitos = requisitos;
        this.calendario = calendario;
        this.log = log;
    }

    private MensajeSalida error(Long id, String codigo, Long idEmpresa) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(), diccionario, idEmpresa);
    }

    public Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        RevisarRequisitosBasicosSalida salidaBasicos = basicos.revisar(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (ConteoErrores.errores(salidaBasicos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
            return out;
        }
        if (entrada.p_SolicitudesRescate.size() == 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(error(0L, "NO_SE_INGRESARON_SOLICITUDES_RESCATE", entrada.p_IdEmpresa));
            return out;
        }
        ArrayList<Long> correlativos = new ArrayList<>();
        for (InSolicitudesRescate mov : entrada.p_SolicitudesRescate) {
            if (mov.IdInterno.longValue() == 0L) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(error(0L, "ID_INTERNO_NO_CERO", entrada.p_IdEmpresa));
                return out;
            }
            if (correlativos.contains(mov.IdInterno)) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(error(0L, "CORRELATIVO_INCORRECTO", entrada.p_IdEmpresa));
                return out;
            }
            correlativos.add(mov.IdInterno);
        }
        procesar(entrada, out);
        return out;
    }

    private void procesar(Entrada entrada, Salida out) throws Exception {
        Datos d = new Datos();
        d.fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String respetaValutas = Constantes3B.NO;
        try {
            respetaValutas = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
        } catch (Exception e) {
            // legacy: warning only, the default stays N
        }
        Long idPaisEmpresa = empresas.informacion(entrada.p_IdEmpresa).IdPais;
        ComisionNnLectura.leerApoyo(entrada, d, datos, datosRequisitos);
        ComisionNnLectura.calcularFechasYCuotas(entrada, d, respetaValutas, idPaisEmpresa, fechas, cuotas);
        d.secuencias = secuencias.siguientes(SECUENCIA_SOLICITUD, Long.valueOf(entrada.p_SolicitudesRescate.size()));
        ComisionNnLectura.hashCuotas(d);
        int segundosZona = (int) configuracion.entero(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        Timestamp local = calendario.fechaHoraSistemaLocal(entrada.p_IdEmpresa);
        d.fechaHoraUTC = new Timestamp(local.getTime() - segundosZona * 1000L);   // Fecha.toUTCTimeStamp: local time minus the zone offset
        ComisionNnCierre.finalizar(entrada, out, d, requisitos, datos, log);
    }
}
