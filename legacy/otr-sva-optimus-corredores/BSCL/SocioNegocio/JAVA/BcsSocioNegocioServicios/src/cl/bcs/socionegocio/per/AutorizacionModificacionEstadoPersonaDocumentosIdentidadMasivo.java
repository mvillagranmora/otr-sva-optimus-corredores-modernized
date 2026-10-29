package cl.bcs.socionegocio.per;

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

public class AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo implements BcsAutorizaciones {

    private static final Logger logger = LogManager.getLogger(AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo.class);

    private final String SQL_UPDATE_DOCUMENTO_AUTORIZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo_Update_DocumentoAutorizado.sql");
    private final String SQL_UPDATE_ESTADO_SOLICITUD = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo_Update_EstadoSolicitud.sql");
    private final String SQL_UPDATE_DOCUMENTO_RECHAZADO = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo_Update_DocumentoRechazado.sql");

    public static class DocumentoAutorizacion {
        public Long IdDocumentoIdentidad;
        public String IdEstadoAutorizacion;
        public String IdConcepto;
    }

    /**
     * AUTORIZAR: Migracion de ConfirmarModificarEstadoPersonaDocumentosIdentidadMasivo.
     * Actualiza IdEstado de DocumentoIdentidad usando SolicitudCambio y deja la solicitud en AUT.
     */
    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo.autorizarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<DocumentoAutorizacion> documentos = entrada.p_Lista.stream()
                .map(evento -> {
                    DocumentoAutorizacion d = new DocumentoAutorizacion();
                    d.IdDocumentoIdentidad = Long.valueOf(evento.NumeroOperacionConcepto);
                    d.IdEstadoAutorizacion = "AUT";
                    d.IdConcepto = evento.IdTipoEvento;
                    return d;
                })
                .distinct()
                .collect(Collectors.toList());

            new SQL().batch(conn, SQL_UPDATE_DOCUMENTO_AUTORIZADO, documentos, DocumentoAutorizacion.class);
            new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, documentos, DocumentoAutorizacion.class);

            for (DocumentoAutorizacion documento : documentos) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = documento;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en autorizarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        logger.info("FIN::AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo.autorizarEvento");
        return salida;
    }

    /**
     * RECHAZAR: Migracion de RechazaAutorizacionPersonaDocumentosIdentidadMasivo.
     * Actualiza SolicitudCambio a RZD y marca documento rechazado.
     */
    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo.rechazarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        try {
            List<DocumentoAutorizacion> documentos = entrada.p_Lista.stream()
                .map(evento -> {
                    DocumentoAutorizacion d = new DocumentoAutorizacion();
                    d.IdDocumentoIdentidad = Long.valueOf(evento.NumeroOperacionConcepto);
                    d.IdEstadoAutorizacion = "RZD";
                    d.IdConcepto = evento.IdTipoEvento;
                    return d;
                })
                .distinct()
                .collect(Collectors.toList());

            new SQL().batch(conn, SQL_UPDATE_ESTADO_SOLICITUD, documentos, DocumentoAutorizacion.class);
            new SQL().batch(conn, SQL_UPDATE_DOCUMENTO_RECHAZADO, documentos, DocumentoAutorizacion.class);

            for (DocumentoAutorizacion documento : documentos) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = documento;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en rechazarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        logger.info("FIN::AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo.rechazarEvento");
        return salida;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            try {
                String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"123456\",\"IdTipoEvento\":\"CAEDOC\"}]}";
                EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);

                AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo servicio = new AutorizacionModificacionEstadoPersonaDocumentosIdentidadMasivo();

                System.out.println("\n=== Probando AUTORIZAR ===");
                SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
                System.out.println("Salida autorizar: " + salidaAutorizar);
                System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
                conn.rollback();

                System.out.println("\n=== Probando RECHAZAR ===");
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
