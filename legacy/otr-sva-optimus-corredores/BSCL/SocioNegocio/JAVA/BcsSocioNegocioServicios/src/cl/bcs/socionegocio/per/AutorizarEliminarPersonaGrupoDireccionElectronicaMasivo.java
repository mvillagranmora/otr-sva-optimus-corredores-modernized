package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo implements JavaCall, BcsAutorizaciones {

    static final Logger logger = LogManager.getLogger(AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo.class);

    private final String ID_TIPO_ENTIDAD = "GRUPODIRECCION-BORRAR";
    private final String ID_ESTADO_RECHAZADO = "RZD";
    private final String TIPO_LOG = "ANULACION";
    private final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_RECHAZAR_GRUPO_DIRECCION_ELECTRONICA";

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaAutorizacion entrada = gson.fromJson(in.toString(), EntradaAutorizacion.class);
        SalidaAutorizacion salida = autorizarEvento(conn, entrada);
        JsonParser parser = new JsonParser();
        return parser.parse(gson.toJson(salida)).getAsJsonObject();
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo.autorizarEvento");
        SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();

        try {
            // Preparar entrada para el servicio de confirmación
            EntradaConfirmarEjecutarCambiosPersona entradaConfirmar = new EntradaConfirmarEjecutarCambiosPersona();
            entradaConfirmar.p_IdEmpresa = entrada.p_Lista.get(0).IdEmpresa;
            entradaConfirmar.p_IdModulo = entrada.p_Lista.get(0).IdModulo;
            entradaConfirmar.p_IdUsuario = entrada.p_Lista.get(0).IdUsuario;
            entradaConfirmar.p_UsaReglasNegocio = "N";
            entradaConfirmar.p_IdEstadoAutorizacion = "AUT";

            // Agregar todas las entidades de los eventos
            for (Evento evento : entrada.p_Lista) {
            ConfirmarEjecutarCambiosPersona.InEntidad entidad = new ConfirmarEjecutarCambiosPersona.InEntidad();
            entidad.IdEntidad = Long.parseLong(evento.NumeroOperacionConcepto);
            entidad.IdPersona = evento.IdCliente;
            entradaConfirmar.p_Entidad.add(entidad);
        }
            
            // Llamar al servicio de confirmación
            ConfirmarEliminarPersonaGrupoDireccionElectronicaMasivo confirmar = new ConfirmarEliminarPersonaGrupoDireccionElectronicaMasivo();
            SalidaConfirmarEjecutarCambiosPersona salidaConfirmacion = confirmar.logica(conn, entradaConfirmar);

            // Procesar resultados por evento
            for (Evento evt : entrada.p_Lista) {
                if (salidaConfirmacion.P_OUT_ERRORESCONTADOR == 0) {
                    String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_AUTORIZADA").msg();
                    salidaAutoriza.P_OUT_ERRORESCONTADOR = 0L;
                    salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
                    ResultadoAutorizacion res = new ResultadoAutorizacion();
                    res.evento = evt.IdEvento;
                    res.resultado = EXITO_AUTORIZACION;
                    salidaAutoriza.P_OUT_RESULTADO.add(res);
                } else {
                    salidaAutoriza.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR;
                    salidaAutoriza.P_OUT_MENSAJES = salidaConfirmacion.P_OUT_MENSAJES;
                }
            }
        } catch (Exception e) {
            logger.error("Error en autorizarEvento AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo", e);
            salidaAutoriza.P_OUT_ERRORESCONTADOR = 1L;
            salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", e.getMessage()));
        }

        logger.info("TERMINO::AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo.autorizarEvento");
        return salidaAutoriza;
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo.rechazarEvento");
        SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();

        try {
            // Recopilar todas las solicitudes por cada entidad del evento
            List<SolicitudCambio> solicitudesCambios = new ArrayList<>();
            ObtenerSolicitudesDeCambiosPersonaMasivo obtenerSolicitudes = new ObtenerSolicitudesDeCambiosPersonaMasivo();

            EntradaConfirmarEjecutarCambiosPersona entradaTemp = new EntradaConfirmarEjecutarCambiosPersona();
            entradaTemp.p_IdEmpresa = entrada.p_Lista.get(0).IdEmpresa;
            entradaTemp.p_IdModulo = entrada.p_Lista.get(0).IdModulo;
            entradaTemp.p_IdUsuario = entrada.p_Lista.get(0).IdUsuario;

            for (Evento evento : entrada.p_Lista) {
                ConfirmarEjecutarCambiosPersona.InEntidad entidad = new ConfirmarEjecutarCambiosPersona.InEntidad();
                entidad.IdEntidad = Long.parseLong(evento.NumeroOperacionConcepto);
                entidad.IdPersona = evento.IdCliente;
                entradaTemp.p_Entidad.add(entidad);
            }
            
            solicitudesCambios = obtenerSolicitudes.logica(conn, entradaTemp, ID_TIPO_ENTIDAD);

            if (solicitudesCambios.isEmpty()) {
                salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
                        "SOLICITUD_NO_EXISTE", null));
                salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
                return salidaAutorizacion;
            }

            // Actualizar estado de solicitudes a RZD
            ModificarEstadoRegistroSolicitudCambioPersona modificarEstado = new ModificarEstadoRegistroSolicitudCambioPersona();
            entradaTemp.p_IdEstadoAutorizacion = ID_ESTADO_RECHAZADO;
            SalidaModificarEstadoRegistroSolicitudCambio salidaMod = modificarEstado.logica(conn, entradaTemp, solicitudesCambios);

            if (salidaMod.P_OUT_ERRORESCONTADOR > 0) {
                salidaAutorizacion.P_OUT_ERRORESCONTADOR = salidaMod.P_OUT_ERRORESCONTADOR;
                salidaAutorizacion.P_OUT_MENSAJES.addAll(salidaMod.P_OUT_MENSAJES);
                return salidaAutorizacion;
            }

            // Agregar log de rechazo
            if (!solicitudesCambios.isEmpty()) {
                AgregarLogPersona agregarLogPersona = new AgregarLogPersona();
                agregarLogPersona.agregarLog(conn, entradaTemp, solicitudesCambios, ID_TIPO_ENTIDAD, TIPO_LOG, NOMBRE_PROCESO_LOG);
            }

            // Retornar resultado por evento
            for (Evento evt : entrada.p_Lista) {
                String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_RECHAZADA").msg();
                salidaAutorizacion.P_OUT_ERRORESCONTADOR = 0L;
                salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_RECHAZADA", descripcion));
                ResultadoAutorizacion res = new ResultadoAutorizacion();
                res.evento = evt.IdEvento;
                res.resultado = EXITO_AUTORIZACION;
                salidaAutorizacion.P_OUT_RESULTADO.add(res);
            }
        } catch (Exception e) {
            logger.error("Error en rechazarEvento AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo", e);
            salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
            salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", e.getMessage()));
        }

        logger.info("TERMINO::AutorizarEliminarPersonaGrupoDireccionElectronicaMasivo.rechazarEvento");
        return salidaAutorizacion;
    }
}
