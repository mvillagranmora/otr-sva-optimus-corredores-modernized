package cl.bcs.fondomutuo.comisionnn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.comisionnn.ComisionNnArmado.Armado;
import cl.bcs.fondomutuo.comisionnn.ComisionNnArmado.Datos;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Entrada;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Salida;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;

/** The write phase of {@code CrearSolicitudRescateComisionNN}: build each request, check requirements, insert and audit. */
final class ComisionNnCierre {

    private ComisionNnCierre() {
    }

    static void finalizar(Entrada entrada, Salida out, Datos d, ServicioCumpleRequisitosService requisitos, ComisionNnDatos datos, LogPort log)
            throws Exception {
        ServicioCumpleRequisitosService.Entrada entradaCumple = new ServicioCumpleRequisitosService.Entrada();
        entradaCumple.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCumple.p_IdUsuario = entrada.p_IdUsuario;

        int i = 0;
        List<ResultadoSolicitud> resultadoTemp = new ArrayList<>();
        List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<>();
        for (InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate) {
            Armado a = ComisionNnArmado.armar(entrada, solicitud, i, d);
            solicitudesProcesar.add(a.sol);
            entradaCumple.p_Portafolios.add(a.portVal);
            entradaCumple.p_SolicitudesAporteRescate.add(a.solVal);
            resultadoTemp.add(a.salida);
            i++;
        }
        HashMap<Long, ResultadoSolicitud> hSolicitudSalida = Hashes.aHash(resultadoTemp, r -> r.IdInterno);

        ServicioCumpleRequisitosService.Salida salidaCumple = requisitos.logica(entradaCumple);
        for (MensajeSalida mje : salidaCumple.P_OUT_MENSAJES) {
            MensajeSalida mensaje = new MensajeSalida();
            mensaje.codigo = mje.codigo;
            mensaje.descripcion = mje.descripcion;
            mensaje.id = folioCartera(entradaCumple.p_SolicitudesAporteRescate, mje.id);
            mensaje.tipo = MensajeSalida.MENSAJE_ADVERTENCIA;
            out.P_OUT_MENSAJES.add(mensaje);
            if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                for (SolicitudAporteRescate sol : solicitudesProcesar) {
                    if (sol.IdInterno.equals(Math.abs(mje.id))) {
                        sol.EstadoProceso = Constantes3B.ESTADO_RECHAZADO;
                        break;
                    }
                }
            }
        }
        int registrosPorProcesar = 0;
        for (SolicitudAporteRescate sol : solicitudesProcesar) {
            if (Constantes3B.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                registrosPorProcesar++;
            }
        }
        if (registrosPorProcesar == 0) {
            return;
        }

        List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<>();
        if (!Constantes3B.SI.equalsIgnoreCase(entrada.p_SimularOperacion)) {
            for (SolicitudAporteRescate sol : solicitudesProcesar) {
                if (Constantes3B.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                    solicitudesInsertar.add(sol);
                    out.P_OUT_RESULTADO.add(hSolicitudSalida.get(sol.IdInterno));
                }
            }
            if (solicitudesInsertar.size() > 0) {
                datos.insertarSolicitudes(solicitudesInsertar);
                agregarLog(entrada, d, solicitudesInsertar, log);
                out.P_OUT_COMISIONES_INSERTADAS = solicitudesInsertar;
            }
        } else {
            for (SolicitudAporteRescate sol : solicitudesProcesar) {
                if (Constantes3B.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                    out.P_OUT_RESULTADO.add(hSolicitudSalida.get(sol.IdInterno));
                }
            }
        }
        out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_MENSAJES.stream().filter(m -> m.tipo.equals(MensajeSalida.MENSAJE_ADVERTENCIA)).count();
    }

    private static void agregarLog(Entrada entrada, Datos d, List<SolicitudAporteRescate> insertadas, LogPort log) {
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        for (SolicitudAporteRescate sol : insertadas) {
            ClientePortafolioSolicitudes cliente = d.hclientes.get(sol.IdPortafolio);
            LogContrato.Log l = new LogContrato.Log();
            l.IdCabeceraLog = sol.IdSolicitudAporteRescate;
            l.IdTipoLog = "INGRESO";
            l.IdModulo = Constantes3B.MODULO_FONDO_MUTUO;
            l.IdUsuario = entrada.p_IdUsuario;
            l.IdEmpresa = entrada.p_IdEmpresa;
            l.FechaSistemaLog = d.fechaSistema;
            l.IdUsuarioAtendido = entrada.p_IdUsuario;
            l.IdCliente = sol.IdCliente;
            l.EstadoCliente = cliente.IdEstadoCliente;
            l.NombreCliente = cliente.NombreCompleto;
            l.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
            l.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
            l.IdPortafolio = sol.IdPortafolio;
            l.EstadoPortafolio = cliente.IdEstadoPortafolio;
            l.IdTipoRegistro = "SOLICITUD_RESCATE_COMISION";
            l.EstadoRegistro = sol.Estado;
            l.IdTipoOperacion = sol.IdTipoMovimientoFondoMutuo;
            l.Folio = sol.IdSolicitudAporteRescate;
            l.Cantidad = sol.NumeroCuotas;
            l.PrecioTasa = sol.ValorCuota;
            l.Monto = sol.Monto;
            l.IdMonedaOrigen = sol.IdMoneda;
            l.IdMonedaLiquidacion = sol.IdMoneda;
            l.TipoCambio = sol.ParidadFechaInversion.doubleValue() + "";
            l.Nemo = sol.IdSerieFondo;
            l.IdProceso = 0L;
            l.EstadoProceso = "";
            l.NombreProceso = "";
            entradaLog.p_Log.add(l);
        }
        JsonObject json = JsonLegacy.conNulos().toJsonTree(entradaLog).getAsJsonObject();
        log.agregar(json);
    }

    /** Legacy {@code obtenerFolioCartera}: the folio of the first checked request with that id (0 when there is none; null if its folio is null). */
    private static Long folioCartera(List<InSolicitudes> solicitudes, Long id) {
        return solicitudes.stream().filter(s -> s.IdInterno.equals(id)).findFirst().orElse(new InSolicitudes()).FolioCartera;
    }
}
