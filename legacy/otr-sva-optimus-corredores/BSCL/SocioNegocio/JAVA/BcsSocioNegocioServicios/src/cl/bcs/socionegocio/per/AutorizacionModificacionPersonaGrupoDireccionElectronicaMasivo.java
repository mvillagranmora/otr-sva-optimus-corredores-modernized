package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo implements BcsAutorizaciones {

    private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo.class);

    private static final String ESTADO_AUTORIZADO = "AUT";
    private static final String ESTADO_RECHAZADO = "RZD";
    private static final String ESTADO_PENDIENTE = "PEN";
    private static final String ID_TIPO_ENTIDAD = "GRUPODIRECCIONELECTRONICA";
    private static final String TIPO_LOG = "MODIFICACION";
    private static final String NOMBRE_PROCESO_LOG = "LOG_NOMBRE_PROCESO_MODIFICAR_GRUPO_DIRECCION_ELECTRONICA";

    private final SQL sql = new SQL();

    private final String SQL_COUNT_SOLICITUD_PENDIENTE = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo_Count_SolicitudPendiente.sql");
    private final String SQL_SELECT_DATOS_SOLICITUD = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo_Select_DatosSolicitud.sql");
    private final String SQL_UPDATE_GRUPO_AUTORIZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo_Update_GrupoDireccionAutorizado.sql");
    private final String SQL_UPDATE_GRUPO_RECHAZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo_Update_GrupoDireccionRechazado.sql");
    private final String SQL_SELECT_GRUPO_PERSONA = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo_Select_GrupoPersona.sql");

    public static class GrupoEvento {
        public Long IdGrupoDireccion;
        public String IdConcepto;
        public Long IdEvento;
    }

    public static class Contador {
        public Long Contador;
    }

    public static class DatosSolicitud {
        public Long IdSolicitudCambio;
        public String CampoModificado;
        public String ValorAntiguo;
        public String ValorNuevo;
        public String IdEntidadOrigen;
        public String IdTipoEntidad;
        public Long IdGrupoDireccion;
    }

    public static class UpdateGrupoDireccionAutorizado {
        public Long IdGrupoDireccion;
        public String Grupo;
        public String Comentario;
        public String IdEstado;
    }

    public static class UpdateGrupoDireccionRechazado {
        public Long IdGrupoDireccion;
        public String IdEstadoAutorizacion;
    }

    public static class GrupoPersona {
        public Long IdGrupoDireccion;
        public Long IdPersona;
    }

    public static class ResultadoGrupo {
        public Long IdGrupoDireccion;
        public String IdEstadoAutorizacion;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo.autorizarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<GrupoEvento> gruposEvento = obtenerGruposEvento(entrada);
            List<Long> idsGrupo = gruposEvento.stream().map(g -> g.IdGrupoDireccion).distinct().collect(Collectors.toList());

            if (idsGrupo.isEmpty()) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "FALTA_ESPECIFICAR_GRUPO_DIRECCION_ELECTRONICA", "No se recibieron grupos para autorizar");
            }

            if (!existenSolicitudesPendientes(conn, idsGrupo)) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
            }

            List<DatosSolicitud> solicitudes = obtenerSolicitudesPendientes(conn, idsGrupo);
            if (solicitudes.isEmpty()) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
            }

            List<UpdateGrupoDireccionAutorizado> updates = construirUpdatesAutorizacion(solicitudes);
            if (!updates.isEmpty()) {
                sql.batch(conn, SQL_UPDATE_GRUPO_AUTORIZADO, updates, UpdateGrupoDireccionAutorizado.class);
            }

            actualizarEstadoSolicitudes(conn, entrada, solicitudes, ESTADO_AUTORIZADO, salida);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            agregarLog(conn, entrada, solicitudes, ESTADO_AUTORIZADO);
            agregarResultados(salida, gruposEvento, ESTADO_AUTORIZADO);

        } catch (Exception e) {
            logger.error("Error en autorizarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
            String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_AUTORIZACION", e.getMessage());
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", descripcion));
        }

        logger.info("FIN::AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo.autorizarEvento");
        return salida;
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo.rechazarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<GrupoEvento> gruposEvento = obtenerGruposEvento(entrada);
            List<Long> idsGrupo = gruposEvento.stream().map(g -> g.IdGrupoDireccion).distinct().collect(Collectors.toList());

            if (idsGrupo.isEmpty()) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "FALTA_ESPECIFICAR_GRUPO_DIRECCION_ELECTRONICA", "No se recibieron grupos para rechazar");
            }

            if (!existenSolicitudesPendientes(conn, idsGrupo)) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
            }

            List<DatosSolicitud> solicitudes = obtenerSolicitudesPendientes(conn, idsGrupo);
            if (solicitudes.isEmpty()) {
                return agregarMensajeError(conn, entrada.p_IdEmpresa, salida, "SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes");
            }

            List<UpdateGrupoDireccionRechazado> updates = idsGrupo.stream().map(id -> {
                UpdateGrupoDireccionRechazado update = new UpdateGrupoDireccionRechazado();
                update.IdGrupoDireccion = id;
                update.IdEstadoAutorizacion = ESTADO_RECHAZADO;
                return update;
            }).collect(Collectors.toList());

            if (!updates.isEmpty()) {
                sql.batch(conn, SQL_UPDATE_GRUPO_RECHAZADO, updates, UpdateGrupoDireccionRechazado.class);
            }

            actualizarEstadoSolicitudes(conn, entrada, solicitudes, ESTADO_RECHAZADO, salida);
            if (salida.P_OUT_ERRORESCONTADOR > 0) {
                return salida;
            }

            agregarLog(conn, entrada, solicitudes, ESTADO_RECHAZADO);
            agregarResultados(salida, gruposEvento, ESTADO_RECHAZADO);

        } catch (Exception e) {
            logger.error("Error en rechazarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
            String descripcion = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, "ERROR_RECHAZO", e.getMessage());
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_RECHAZO", descripcion));
        }

        logger.info("FIN::AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo.rechazarEvento");
        return salida;
    }

    private List<GrupoEvento> obtenerGruposEvento(EntradaAutorizacion entrada) {
        Map<String, GrupoEvento> mapa = new LinkedHashMap<>();

        for (Evento evento : entrada.p_Lista) {
            GrupoEvento grupo = new GrupoEvento();
            grupo.IdGrupoDireccion = Long.valueOf(evento.NumeroOperacionConcepto);
            grupo.IdConcepto = evento.IdTipoEvento;
            grupo.IdEvento = evento.IdEvento;
            mapa.put(grupo.IdGrupoDireccion + "_" + grupo.IdConcepto, grupo);
        }

        return new ArrayList<>(mapa.values());
    }

    private boolean existenSolicitudesPendientes(Connection conn, List<Long> idsGrupo) throws Exception {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("IdGrupoDireccion", idsGrupo);
        params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);
        params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);

        List<Contador> out = sql.query(conn, SQL_COUNT_SOLICITUD_PENDIENTE, params, Contador.class);
        Long contador = (out != null && !out.isEmpty() && out.get(0).Contador != null) ? out.get(0).Contador : 0L;
        return contador > 0;
    }

    private List<DatosSolicitud> obtenerSolicitudesPendientes(Connection conn, List<Long> idsGrupo) throws Exception {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("IdGrupoDireccion", idsGrupo);
        params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);
        params.put("IdEstadoAutorizacion", ESTADO_PENDIENTE);

        return sql.query(conn, SQL_SELECT_DATOS_SOLICITUD, params, DatosSolicitud.class);
    }

    private List<UpdateGrupoDireccionAutorizado> construirUpdatesAutorizacion(List<DatosSolicitud> solicitudes) {
        Map<Long, UpdateGrupoDireccionAutorizado> updates = new LinkedHashMap<Long, UpdateGrupoDireccionAutorizado>();

        for (DatosSolicitud solicitud : solicitudes) {
            UpdateGrupoDireccionAutorizado update = updates.get(solicitud.IdGrupoDireccion);
            if (update == null) {
                update = new UpdateGrupoDireccionAutorizado();
                update.IdGrupoDireccion = solicitud.IdGrupoDireccion;
                updates.put(solicitud.IdGrupoDireccion, update);
            }

            if ("Grupo".equals(solicitud.CampoModificado)) {
                update.Grupo = solicitud.ValorNuevo;
            } else if ("Comentario".equals(solicitud.CampoModificado)) {
                update.Comentario = solicitud.ValorNuevo;
            } else if ("IdEstado".equals(solicitud.CampoModificado)) {
                update.IdEstado = solicitud.ValorNuevo;
            }
        }

        return new ArrayList<UpdateGrupoDireccionAutorizado>(updates.values());
    }

    private void actualizarEstadoSolicitudes(Connection conn, EntradaAutorizacion entrada, List<DatosSolicitud> solicitudes,
            String estadoDestino, SalidaAutorizacion salida) throws Exception {

        EntradaModificarEstado in = new EntradaModificarEstado();
        in.p_IdEmpresa = entrada.p_IdEmpresa;
        in.p_IdUsuario = entrada.p_IdUsuario;
        in.p_IdModulo = entrada.p_IdModulo;
        in.p_IdEstadoAutorizacion = estadoDestino;

        Map<String, EntidadOrigen> entidadesMap = new LinkedHashMap<String, EntidadOrigen>();
        for (DatosSolicitud solicitud : solicitudes) {
            String key = solicitud.IdEntidadOrigen + "_" + solicitud.IdTipoEntidad;
            if (!entidadesMap.containsKey(key)) {
                EntidadOrigen entidad = new EntidadOrigen();
                entidad.IdEntidadOrigen = solicitud.IdEntidadOrigen;
                entidad.IdTipoEntidad = solicitud.IdTipoEntidad;
                entidadesMap.put(key, entidad);
            }
        }

        in.p_EntidadOrigen.addAll(entidadesMap.values());

        ModificarEstadoRegistroSolicitudCambioMasivo servicio = new ModificarEstadoRegistroSolicitudCambioMasivo();
        SalidaModificarEstado out = servicio.logica(conn, in);

        if (out.p_out_ErroresContador > 0) {
            salida.P_OUT_ERRORESCONTADOR = (long) out.p_out_ErroresContador;
            for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : out.p_out_ErroresDetalle) {
                String codigo = (err.Codigo != null && !err.Codigo.trim().isEmpty()) ? err.Codigo : "ERROR_ESTADO_SOLICITUD";
                String descripcion = (err.Descripcion != null && !err.Descripcion.trim().isEmpty())
                    ? err.Descripcion
                    : obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, codigo, "Error actualizando estado de solicitud");
                salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
            }
        }
    }

    private void agregarLog(Connection conn, EntradaAutorizacion entrada, List<DatosSolicitud> solicitudes, String estado) throws Exception {
        if (solicitudes == null || solicitudes.isEmpty()) {
            return;
        }

        List<Long> idsGrupo = solicitudes.stream().map(s -> s.IdGrupoDireccion).distinct().collect(Collectors.toList());
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("IdGrupoDireccion", idsGrupo);

        List<GrupoPersona> personas = sql.query(conn, SQL_SELECT_GRUPO_PERSONA, params, GrupoPersona.class);
        Map<Long, Long> personaPorGrupo = personas.stream().collect(Collectors.toMap(g -> g.IdGrupoDireccion, g -> g.IdPersona, (a, b) -> a));

        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String nombreProceso = obtenerMensajeDiccionario(conn, entrada.p_IdEmpresa, NOMBRE_PROCESO_LOG, "Confirmar Solicitud de Cambio");

        AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();

        for (Long idGrupo : idsGrupo) {
            AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
            log.IdCabeceraLog = idGrupo;
            log.IdTipoLog = TIPO_LOG;
            log.IdModulo = entrada.p_IdModulo;
            log.IdUsuario = entrada.p_IdUsuario;
            log.IdEmpresa = entrada.p_IdEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = entrada.p_IdUsuario;
            log.IdRegistro = idGrupo.toString();
            log.IdTipoRegistro = ID_TIPO_ENTIDAD;
            log.EstadoRegistro = estado;
            log.IdProceso = 0L;
            log.EstadoProceso = estado;
            log.NombreProceso = nombreProceso;
            log.IdPersona = personaPorGrupo.containsKey(idGrupo) ? personaPorGrupo.get(idGrupo) : 0L;
            entradaLog.p_Log.add(log);
        }

        for (DatosSolicitud solicitud : solicitudes) {
            AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
            detalle.IdTrabajo = 0L;
            detalle.IdCabeceraLog = solicitud.IdGrupoDireccion;
            detalle.SubRegistro = solicitud.IdTipoEntidad;
            detalle.CampoRegistro = solicitud.CampoModificado;
            detalle.ValorAntiguo = solicitud.ValorAntiguo;
            detalle.ValorNuevo = solicitud.ValorNuevo;
            entradaLog.p_LogDetalle.add(detalle);
        }

        AgregarLog agregarLog = new AgregarLog();
        agregarLog.logica(conn, entradaLog);
    }

    private void agregarResultados(SalidaAutorizacion salida, List<GrupoEvento> grupos, String estado) {
        for (GrupoEvento grupo : grupos) {
            ResultadoGrupo resultadoGrupo = new ResultadoGrupo();
            resultadoGrupo.IdGrupoDireccion = grupo.IdGrupoDireccion;
            resultadoGrupo.IdEstadoAutorizacion = estado;

            BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
            resultado.evento = resultadoGrupo;
            resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
            salida.P_OUT_RESULTADO.add(resultado);
        }
    }

    private SalidaAutorizacion agregarMensajeError(Connection conn, Long idEmpresa, SalidaAutorizacion salida,
            String codigo, String descripcionDefault) {
        salida.P_OUT_ERRORESCONTADOR = 1L;
        String descripcion = obtenerMensajeDiccionario(conn, idEmpresa, codigo, descripcionDefault);
        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
        return salida;
    }

    private String obtenerMensajeDiccionario(Connection conn, Long idEmpresa, String codigo, String fallback) {
        try {
            return Diccionario.getMensaje(conn, idEmpresa, codigo).msg();
        } catch (Exception ex) {
            return fallback;
        }
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            try {
                String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"AUT\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"IdEvento\":1,\"NumeroOperacionConcepto\":\"10023\",\"IdTipoEvento\":\"MODGDE\"}]}";
                EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

                AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo servicio =
                    new AutorizacionModificacionPersonaGrupoDireccionElectronicaMasivo();

                System.out.println("=== Probando AUTORIZAR ===");
                SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
                System.out.println("Errores autorizar: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
                System.out.println("Resultados autorizar: " + salidaAutorizar.P_OUT_RESULTADO.size());
                conn.rollback();

                System.out.println("=== Probando RECHAZAR ===");
                SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
                System.out.println("Errores rechazar: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
                System.out.println("Resultados rechazar: " + salidaRechazar.P_OUT_RESULTADO.size());
                conn.rollback();

            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
