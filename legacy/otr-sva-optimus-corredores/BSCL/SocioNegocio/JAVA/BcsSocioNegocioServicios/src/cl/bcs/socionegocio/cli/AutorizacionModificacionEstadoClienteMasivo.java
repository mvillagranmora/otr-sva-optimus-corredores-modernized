package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionEstadoClienteMasivo implements BcsAutorizaciones {

    private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoClienteMasivo.class);
    private final SQL sql = new SQL();

    private final String SQL_UPDATE_CLIENTE_AUTORIZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/cli/sql/AutorizacionModificacionEstadoClienteMasivo_Update_ClienteAutorizado.sql");
    private final String SQL_UPDATE_PORTAFOLIO_AUTORIZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/cli/sql/AutorizacionModificacionEstadoClienteMasivo_Update_PortafolioAutorizado.sql");
    private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/cli/sql/AutorizacionModificacionEstadoClienteMasivo_Update_EstadoSolicitud.sql");

    public static class ClienteAutorizacion {
        public Long IdCliente;
        public String IdEstadoAutorizacion;
        public String IdConcepto;
    }

    /**
     * AUTORIZAR: Lógica migrada de ConfirmarModificarEstadoClienteMasivo.
     */
    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoClienteMasivo.autorizarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<ClienteAutorizacion> clientes = entrada.p_Lista.stream()
                .map(evento -> {
                    ClienteAutorizacion c = new ClienteAutorizacion();
                    c.IdCliente = Long.valueOf(evento.NumeroOperacionConcepto);
                    c.IdEstadoAutorizacion = "AUT";
                    c.IdConcepto = evento.IdTipoEvento;
                    return c;
                })
                .distinct()
                .collect(Collectors.toList());

            sql.batch(conn, SQL_UPDATE_CLIENTE_AUTORIZADO, clientes, ClienteAutorizacion.class);
            sql.batch(conn, SQL_UPDATE_PORTAFOLIO_AUTORIZADO, clientes, ClienteAutorizacion.class);
            sql.batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, clientes, ClienteAutorizacion.class);

            for (ClienteAutorizacion cliente : clientes) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cliente;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en autorizarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        logger.info("FIN::AutorizacionModificacionEstadoClienteMasivo.autorizarEvento");
        return salida;
    }

    /**
     * RECHAZAR: Lógica migrada de RechazaAutorizacionClienteDatosGenerales.
     */
    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoClienteMasivo.rechazarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<ClienteAutorizacion> clientes = entrada.p_Lista.stream()
                .map(evento -> {
                    ClienteAutorizacion c = new ClienteAutorizacion();
                    c.IdCliente = Long.valueOf(evento.NumeroOperacionConcepto);
                    c.IdEstadoAutorizacion = "RZD";
                    c.IdConcepto = evento.IdTipoEvento;
                    return c;
                })
                .distinct()
                .collect(Collectors.toList());

            sql.batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, clientes, ClienteAutorizacion.class);

            for (ClienteAutorizacion cliente : clientes) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cliente;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en rechazarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        logger.info("FIN::AutorizacionModificacionEstadoClienteMasivo.rechazarEvento");
        return salida;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            try {
                String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CAECLI\"}]}";
                EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

                AutorizacionModificacionEstadoClienteMasivo servicio = new AutorizacionModificacionEstadoClienteMasivo();

                System.out.println("\\n=== Probando AUTORIZAR ===");
                SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
                System.out.println("Salida autorizar: " + salidaAutorizar);
                System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
                conn.rollback();

                System.out.println("\\n=== Probando RECHAZAR ===");
                SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
                System.out.println("Salida rechazar: " + salidaRechazar);
                System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
                conn.rollback();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
