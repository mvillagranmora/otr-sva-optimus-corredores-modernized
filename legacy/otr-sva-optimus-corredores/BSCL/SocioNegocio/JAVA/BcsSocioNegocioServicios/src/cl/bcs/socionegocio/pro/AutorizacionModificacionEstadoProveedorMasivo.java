package cl.bcs.socionegocio.pro;


import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class AutorizacionModificacionEstadoProveedorMasivo implements BcsAutorizaciones {
    private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoProveedorMasivo.class);
    private final SQL sql = new SQL();
    private final String SQL_UPDATE_PROVEEDOR = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/pro/sql/AutorizacionModificacionEstadoProveedorMasivo_Update_ProveedorAutorizado.sql");
    private final String SQL_RECHAZA_SOLICITUD = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/pro/sql/AutorizacionModificacionEstadoProveedorMasivo_Update_EstadoSolicitud.sql");

    public static class ProveedorCambio {
        public Long IdProveedor;
        public String IdEstado;
        public String IdEstadoAutorizacion; // Para el batch de solicitud
        public String IdTipoBloqueo;
        public String IdTipoDesbloqueo;
        public String FechaBloqueo;
        public String FechaDesbloqueo;
        public String ComentarioBloqueo;
        public String ComentarioDesbloqueo;
        public String FechaModificacion;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoProveedorMasivo.autorizarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<ProveedorCambio> cambios = new ArrayList<>();
            for (Evento evento : entrada.p_Lista) {
                ProveedorCambio cambio = new ProveedorCambio();
                cambio.IdProveedor = Long.valueOf(evento.NumeroOperacionConcepto);
                cambio.IdEstado = "AUT";
                cambio.IdEstadoAutorizacion = "AUT"; // O "ACT" según tu lógica de negocio
                cambio.FechaModificacion = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
                cambios.add(cambio);
            }
            // Actualizar proveedor (autorizado)
            new SQL().batch(conn, SQL_UPDATE_PROVEEDOR, cambios, ProveedorCambio.class);
            // Actualizar estado de solicitud de cambio a autorizado
            new SQL().batch(conn, SQL_RECHAZA_SOLICITUD, cambios, ProveedorCambio.class);
            for (ProveedorCambio cambio : cambios) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cambio;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en autorizarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }
        logger.info("FIN::AutorizacionModificacionEstadoProveedorMasivo.autorizarEvento");
        return salida;
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoProveedorMasivo.rechazarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<ProveedorCambio> cambios = new ArrayList<>();
            for (Evento evento : entrada.p_Lista) {
                ProveedorCambio cambio = new ProveedorCambio();
                cambio.IdProveedor = Long.valueOf(evento.NumeroOperacionConcepto);
                cambio.IdEstado = "RZD";
                cambio.IdEstadoAutorizacion = "RZD";
                cambio.FechaModificacion = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
                cambios.add(cambio);
            }
            // Actualizar estado de solicitud de cambio a rechazado
            new SQL().batch(conn, SQL_RECHAZA_SOLICITUD, cambios, ProveedorCambio.class);
            for (ProveedorCambio cambio : cambios) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cambio;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en rechazarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }
        logger.info("FIN::AutorizacionModificacionEstadoProveedorMasivo.rechazarEvento");
        return salida;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            try {
                String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76816,\"p_IdModulo\":\"AUT\",\"p_UsaReglasNegocio\":\"N\",\"p_Lista\":[{\"NumeroOperacionConcepto\":\"12345\"}]}";
//                EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
                EntradaAutorizacion entrada = new EntradaAutorizacion();
                entrada.p_IdEmpresa = 1L; // tu empresa
                entrada.p_IdUsuario = 76816L; // tu usuario
                entrada.p_IdModulo = "SOC";
                entrada.p_UsaReglasNegocio = "S";

                // Evento con el identificador del proveedor
                Evento evento = new Evento();
                evento.NumeroOperacionConcepto = "43"; // IdProveedor como string
                entrada.p_Lista.add(evento);
                
                AutorizacionModificacionEstadoProveedorMasivo servicio = new AutorizacionModificacionEstadoProveedorMasivo();
                // Probar AUTORIZAR
                System.out.println("\n=== Probando AUTORIZAR ===");
                SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
                System.out.println("Salida autorizar: " + salidaAutorizar);
                System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
                conn.rollback();
                // Probar RECHAZAR
                System.out.println("\n=== Probando RECHAZAR ===");
                SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
                System.out.println("Salida rechazar: " + salidaRechazar);
                System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
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
