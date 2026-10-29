package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfirmarAutorizacionFirmaDocumento implements JavaCall, BcsAutorizaciones {
    static final Logger logger = LogManager.getLogger(ConfirmarAutorizacionFirmaDocumento.class);

    private final String SQL_SELECT_DETALLE_EVENTO = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConfirmarAutorizacionFirmaDocumento_SelectDetalleEvento.sql");
    private final String SQL_UPDATE_AUTORIZAR_FIRMAS_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConfirmarAutorizacionFirmaDocumento_UpdateAutorizarFirmasAnexo.sql");

    public static class FirmaAnexo extends BcsPojo {
        public Long IdAnexo;
        public Long IdFirmaAnexo;
        public String EsAutorizado;
    }

    public static class DetalleEvento extends BcsPojo {
        public Long IdEventoDetalle;
        public Long IdEvento;
        public String NombreCampo;
        public String Valor;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        return logica(conn, entrada, "S");
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        return logica(conn, entrada, "N");
    }

    private SalidaAutorizacion logica(Connection conn, EntradaAutorizacion entrada, String esAutorizado) {
        SalidaAutorizacion salida = new SalidaAutorizacion();

        List<Long> idsEventos = entrada.p_Lista.stream().map(e -> e.IdEvento).distinct().collect(Collectors.toList());

        try {
            List<DetalleEvento> detalles = obtenerDetallesEventos(conn, idsEventos);
            Map<Long, Long> idAnexoPorIdEvento = entrada.p_Lista.stream().collect(Collectors.toMap(e -> e.IdEvento, e -> Long.valueOf(e.NumeroOperacionConcepto)));
            Map<Long, Long> idEventoPorIdEventoDetalle = detalles.stream().collect(Collectors.toMap(ed -> ed.IdEventoDetalle, ed -> ed.IdEvento));
            Map<Long, Long> idAnexoPorIdEventoDetalle = idEventoPorIdEventoDetalle
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> idAnexoPorIdEvento.getOrDefault(entry.getValue(), 0L)));

            List<FirmaAnexo> firmas = obtenerFirmasAnexos(detalles, idAnexoPorIdEventoDetalle, esAutorizado);

            SQL sql = new SQL();
            sql.batch(conn, SQL_UPDATE_AUTORIZAR_FIRMAS_ANEXO, firmas, FirmaAnexo.class);

            if ("S".equals(esAutorizado)) {
                List<ActivarDocumentos.AnexoFirmasProcesadas> anexos = firmas
                        .stream()
                        .map(f -> f.IdAnexo)
                        .distinct()
                        .map(id -> {
                            ActivarDocumentos.AnexoFirmasProcesadas anexoFirmasProcesadas = new ActivarDocumentos.AnexoFirmasProcesadas();
                            anexoFirmasProcesadas.CantidadFirmasProcesadas = 0L;
                            anexoFirmasProcesadas.IdAnexo = id;

                            return anexoFirmasProcesadas;
                        }).collect(Collectors.toList());

                ActivarDocumentos.EntradaActivarDocumentos entradaActivarDocumentos = new ActivarDocumentos.EntradaActivarDocumentos();
                entradaActivarDocumentos.p_IdEmpresa = entrada.p_IdEmpresa;
                entradaActivarDocumentos.p_Anexos = anexos;

                ActivarDocumentos activarDocumentos = new ActivarDocumentos();
                ActivarDocumentos.SalidaActivarDocumentos salidaActivarDocumentos = activarDocumentos.logica(conn, entradaActivarDocumentos);
                if (salidaActivarDocumentos.P_OUT_ERRORESCONTADOR > 0) {
                    salida.P_OUT_ERRORESCONTADOR += salidaActivarDocumentos.P_OUT_ERRORESCONTADOR;
                    salida.P_OUT_MENSAJES.addAll(salidaActivarDocumentos.P_OUT_MENSAJES);
                }
            }
        } catch (Exception ignored) {
            salida.P_OUT_ERRORESCONTADOR += 1L;
        }

        return salida;
    }

    private List<DetalleEvento> obtenerDetallesEventos(Connection conn, List<Long> idsEventos) throws SQLException {
        logger.debug("INICIO::ConfirmarAutorizacionFirmaDocumento.obtenerDetallesEventos");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdsEventos", idsEventos);

        SQL sql = new SQL();
        List<DetalleEvento> detalles = sql.query(conn, SQL_SELECT_DETALLE_EVENTO, params, DetalleEvento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConfirmarAutorizacionFirmaDocumento.obtenerDetallesEventos", fin - inicio);

        return detalles;
    }

    private List<FirmaAnexo> obtenerFirmasAnexos(List<DetalleEvento> detalles, Map<Long, Long> idAnexoPorIdEventoDetalle, String esAutorizado) {
        logger.trace("INICIO::ConfirmarAutorizacionFirmaDocumento.obtenerFirmasAnexos");
        long inicio = System.currentTimeMillis();

        List<FirmaAnexo> firmas = detalles
                .stream()
                .filter(detalle -> "IdFirmaAnexo".equals(detalle.NombreCampo))
                .map(detalle -> {
                    FirmaAnexo firma = new FirmaAnexo();
                    firma.IdAnexo = idAnexoPorIdEventoDetalle.getOrDefault(detalle.IdEventoDetalle, 0L);
                    firma.IdFirmaAnexo = Long.valueOf(detalle.Valor);
                    firma.EsAutorizado = esAutorizado;

                    return firma;
                }).collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ConfirmarAutorizacionFirmaDocumento.obtenerFirmasAnexos", fin - inicio);

        return firmas;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            EntradaAutorizacion entradaAutorizacion = new EntradaAutorizacion();
            entradaAutorizacion.p_IdEmpresa = 1L;
            entradaAutorizacion.p_IdModulo = "SOC";
            entradaAutorizacion.p_IdUsuario = 4L;
            entradaAutorizacion.p_UsaReglasNegocio = "N";

            Evento evento = new Evento();
            evento.IdEvento = 201296L;
            evento.NumeroOperacionConcepto = "431";
            entradaAutorizacion.p_Lista = Arrays.asList(evento);
            System.out.println("ENTRADA");
            System.out.println(entradaAutorizacion);

            ConfirmarAutorizacionFirmaDocumento confirmarAutorizacionFirmaDocumento = new ConfirmarAutorizacionFirmaDocumento();
            SalidaAutorizacion salidaAutorizacion = confirmarAutorizacionFirmaDocumento.logica(conn, entradaAutorizacion, "S");
            System.out.println("SALIDA");
            System.out.println(salidaAutorizacion);

            conn.rollback();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
