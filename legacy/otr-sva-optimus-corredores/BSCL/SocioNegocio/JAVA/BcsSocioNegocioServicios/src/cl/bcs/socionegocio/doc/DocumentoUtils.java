package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.doc.pojo.TipoDocumento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.socionegocio.doc.ConfirmarAutorizacionFirmaDocumento.FirmaAnexo;
import cl.bcs.socionegocio.doc.pojo.SolicitudFirmaDocumento;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentoUtils {
    static final Logger logger = LogManager.getLogger(DocumentoUtils.class);

    private static final FileLoader loader;

    private static final String SQL_SELECT_SOLICITUD_FIRMA_DOCUMENTO_PENDIENTES;
    private static final String SQL_SELECT_FIRMAS_ANEXOS;
    private static final String SQL_SELECT_TIPOS_INDICADORES_DOCUMENTOS;

    static {
        loader = new FileLoader();
        SQL_SELECT_SOLICITUD_FIRMA_DOCUMENTO_PENDIENTES = loader.loadResourceToString("cl/bcs/socionegocio/doc/sql/DocumentoUtils_SelectSolicitudFirmaDocumentoPendientes.sql");
        SQL_SELECT_FIRMAS_ANEXOS = loader.loadResourceToString("cl/bcs/socionegocio/doc/sql/DocumentoUtils_SelectFirmasAnexos.sql");
        SQL_SELECT_TIPOS_INDICADORES_DOCUMENTOS = loader.loadResourceToString("cl/bcs/socionegocio/doc/sql/DocumentoUtils_SelectTiposIndicadoresDocumentos.sql");
    }

    public static Map<Long, SolicitudFirmaDocumento> obtenerSolicitudFirmaPendientesPorIdPersonaFirmante(Connection conn, Long idAnexo, List<Long> idsPersonasFirmantes) throws SQLException {
        logger.debug("INICIO::DocumentoUtils.obtenerSolicitudFirmaDocumentoPorIdPersonaFirmante");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("IdAnexo", idAnexo);
        params.put("IdsPersonasFirmantes", idsPersonasFirmantes);

        SQL sql = new SQL();

        Map<Long, SolicitudFirmaDocumento> solicitudesPorIdPersonaFirmante = sql
                .query(conn, SQL_SELECT_SOLICITUD_FIRMA_DOCUMENTO_PENDIENTES, params, SolicitudFirmaDocumento.class)
                .stream()
                .collect(Collectors.toMap(s -> s.IdPersonaFirmante, s -> s));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::DocumentoUtils.obtenerSolicitudFirmaDocumentoPorIdPersonaFirmante", fin - inicio);

        return solicitudesPorIdPersonaFirmante;
    }

    public static Map<Long, Long> obtenerCantidadFirmasDocumentoAnexoPorIdAnexo(Connection conn, List<Long> idsAnexos) throws SQLException {
        logger.debug("INICIO::DocumentoUtils.obtenerCantidadFirmasDocumentoAnexoPorIdAnexo");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdsAnexos", idsAnexos);

        SQL sql = new SQL();
        List<FirmaAnexo> anexos = sql.query(conn, SQL_SELECT_FIRMAS_ANEXOS, params, FirmaAnexo.class);
        Map<Long, Long> firmasActivasPorIdAnexo = anexos.stream().collect(Collectors.groupingBy(fa -> fa.IdAnexo, Collectors.counting()));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::DocumentoUtils.obtenerCantidadFirmasDocumentoAnexo", fin - inicio);

        return firmasActivasPorIdAnexo;
    }

    public static Map<String, List<TipoDocumentoIndicador>> obtenerTiposIndicadoresPorIdTipoDocumento(Connection conn, long idEmpresa, List<String> idsTiposDocumentos) throws SQLException {
        logger.debug("INICIO::DocumentoUtils.obtenerTiposIndicadoresPorIdTipoDocumento");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("IdEmpresa", idEmpresa);
        params.put("IdsTiposDocumentos", idsTiposDocumentos);

        SQL sql = new SQL();
        List<TipoDocumentoIndicador> tiposIndicadoresDocumentos = sql.query(conn, SQL_SELECT_TIPOS_INDICADORES_DOCUMENTOS, params, TipoDocumentoIndicador.class);
        Map<String, List<TipoDocumentoIndicador>> tiposIndicadoresPorIdTipoDocumento = tiposIndicadoresDocumentos
                .stream()
                .collect(Collectors.groupingBy(tdi -> tdi.IdTipoDocumento, Collectors.toList()));

        long fin = System.currentTimeMillis();
        logger.debug("FIN::DocumentoUtils.obtenerTiposIndicadoresPorIdTipoDocumento", fin - inicio);

        return tiposIndicadoresPorIdTipoDocumento;
    }

    public static List<TipoDocumentoIndicador> obtenerTipoDocumentoIndicadores(Long idEmpresa, List<TipoDocumento> tipoDocumentos) {
        logger.trace("INICIO::DocumentoUtils.obtenerTipoDocumentoIndicadores");
        long inicio = System.currentTimeMillis();

        List<TipoDocumentoIndicador> tipoDocumentosIndicadores = tipoDocumentos
                .stream()
                .flatMap(td -> td.Requisitos.stream().map(req -> Pair.of(td.IdTipoDocumento, req.IdTipoIndicador)))
                .map(par -> {
                    TipoDocumentoIndicador tipoDocumentoIndicador = new TipoDocumentoIndicador();
                    tipoDocumentoIndicador.IdTipoIndicador = par.getValue();
                    tipoDocumentoIndicador.IdTipoDocumento = par.getKey();
                    tipoDocumentoIndicador.IdEmpresa = idEmpresa;

                    return tipoDocumentoIndicador;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("INICIO::DocumentoUtils.obtenerTipoDocumentoIndicadores", fin - inicio);

        return tipoDocumentosIndicadores;
    }

    public static void asignarIdsTipoDocumentoIndicadores(Connection conn, List<TipoDocumentoIndicador> tipoDocumentoIndicadores) throws SQLException {
        logger.debug("INICIO::DocumentoUtils.asignarIdsTipoDocumentoIndicadores");
        long inicio = System.currentTimeMillis();

        Iterator<Long> idsRequisitosIterator = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::TipoDocumentoIndicador", (long) tipoDocumentoIndicadores.size()).iterator();
        tipoDocumentoIndicadores.forEach(i -> i.IdTipoDocumentoIndicador = idsRequisitosIterator.next());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::DocumentoUtils.asignarIdsTipoDocumentoIndicadores", fin - inicio);
    }

    public static void agregarLogFirmaDocumento(Connection conn, Long idEmpresa, Long idUsuario, List<Long> idsPersonasFirmantes, ResumenDocumentoAnexo resumenAnexo) throws Exception {
        logger.debug("INICIO::DocumentoUtils.agregarLogFirmaDocumento");
        long inicio = System.currentTimeMillis();

        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdTipoLog = "MODIFICACION";
        log.IdModulo = "DOC";
        log.IdEmpresa = idEmpresa;
        log.IdUsuario = idUsuario;
        log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        log.IdUsuarioAtendido = idUsuario;
        log.IdTipoRegistro = "DOCUMENTO";
        log.IdRegistro = String.valueOf(resumenAnexo.IdDocumento);
        log.NombreProceso = "Firma de Documento";


        List<AgregarLog.InAgregarLog.DetalleLog> firmantesDetallesLog = idsPersonasFirmantes.stream().map(idPersonaFirmante -> {
            AgregarLog.InAgregarLog.DetalleLog firmanteDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
            firmanteDetalleLog.SubRegistro = "PERSONAFIRMA";
            firmanteDetalleLog.CampoRegistro = "IdPersona";
            firmanteDetalleLog.ValorAntiguo = "";
            firmanteDetalleLog.ValorNuevo = String.valueOf(idPersonaFirmante);

            return firmanteDetalleLog;
        }).collect(Collectors.toList());

        AgregarLog.InAgregarLog.DetalleLog clienteDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
        clienteDetalleLog.SubRegistro = "CLIENTE";
        clienteDetalleLog.CampoRegistro = "IdCliente";
        clienteDetalleLog.ValorAntiguo = "";
        clienteDetalleLog.ValorNuevo = String.valueOf(resumenAnexo.IdCliente);

        AgregarLog.InAgregarLog.DetalleLog documentoDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
        documentoDetalleLog.SubRegistro = "DOCUMENTO";
        documentoDetalleLog.CampoRegistro = "IdDocumento";
        documentoDetalleLog.ValorAntiguo = "";
        documentoDetalleLog.ValorNuevo = String.valueOf(resumenAnexo.IdDocumento);

        AgregarLog.InAgregarLog.DetalleLog anexoDetalleLog = new AgregarLog.InAgregarLog.DetalleLog();
        anexoDetalleLog.SubRegistro = "ANEXO";
        anexoDetalleLog.CampoRegistro = "IdAnexo";
        anexoDetalleLog.ValorAntiguo = "";
        anexoDetalleLog.ValorNuevo = String.valueOf(resumenAnexo.IdAnexo);

        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
        entradaAgregarLog.p_Log.add(log);
        entradaAgregarLog.p_LogDetalle.add(clienteDetalleLog);
        entradaAgregarLog.p_LogDetalle.add(documentoDetalleLog);
        entradaAgregarLog.p_LogDetalle.add(anexoDetalleLog);
        entradaAgregarLog.p_LogDetalle.addAll(firmantesDetallesLog);

        AgregarLog agregarLog = new AgregarLog();
        agregarLog.logica(conn, entradaAgregarLog);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::DocumentoUtils.agregarLogFirmaDocumento", fin - inicio);
    }

    public static ActivarDocumentos.SalidaActivarDocumentos activarDocumentos(Connection conn, Long idEmpresa, List<ResumenDocumentoAnexo> resumenesAnexos) throws Exception {
        logger.debug("INICIO::FirmarDocumento.activarDocumento");
        long inicio = System.currentTimeMillis();

        ActivarDocumentos.EntradaActivarDocumentos entradaActivarDocumentos = new ActivarDocumentos.EntradaActivarDocumentos();
        entradaActivarDocumentos.p_IdEmpresa = idEmpresa;
        entradaActivarDocumentos.p_Anexos = resumenesAnexos.stream().map(ra -> {
            ActivarDocumentos.AnexoFirmasProcesadas anexoProcesado = new ActivarDocumentos.AnexoFirmasProcesadas();
            anexoProcesado.IdAnexo = ra.IdAnexo;
            anexoProcesado.CantidadFirmasProcesadas = 0L;

            return anexoProcesado;
        }).collect(Collectors.toList());

        ActivarDocumentos activarDocumentos = new ActivarDocumentos();
        ActivarDocumentos.SalidaActivarDocumentos salidaActivarDocumentos = activarDocumentos.logica(conn, entradaActivarDocumentos);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::FirmarDocumento.activarDocumento", fin - inicio);

        return salidaActivarDocumentos;
    }
}
