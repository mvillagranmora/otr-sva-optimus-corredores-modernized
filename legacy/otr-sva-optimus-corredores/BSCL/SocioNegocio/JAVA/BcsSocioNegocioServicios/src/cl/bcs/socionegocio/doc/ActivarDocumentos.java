package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.doc.pojo.Anexo;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.Indicador;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ActivarDocumentos implements JavaCall {
    final static Logger logger = LogManager.getLogger(ActivarDocumentos.class);

    private final String SQL_INSERT_TEMPORAL_INDICADORES_DOCUMENTOS = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_InsertTemporalIndicadorDocumento.sql");
    private final String SQL_DELETE_TEMPORAL_INDICADORES_DOCUMENTOS = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_DeleteTemporalIndicadorDocumento.sql");

    private final String SQL_SELECT_INDICADORES_DOCUMENTOS_CLIENTES = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_SelectIndicadoresDocumentosClientes.sql");
    private final String SQL_INSERT_INDICADORES_DOCUMENTOS_CLIENTES = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_InsertIndicadorDocumento.sql");
    private final String SQL_UPDATE_INDICADORES_DOCUMENTOS_CLIENTES = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_UpdateIndicadorDocumento.sql");
    private final String SQL_UPDATE_ESTADO_ANEXOS = loadResourceToString("cl/bcs/socionegocio/doc/sql/ActivarDocumentos_UpdateEstadoAnexo.sql");

    public static class EntradaActivarDocumentos extends BcsPojo {
        public Long p_IdEmpresa;
        public List<AnexoFirmasProcesadas> p_Anexos;

        public EntradaActivarDocumentos() {
            p_Anexos = new ArrayList<>();
        }
    }

    public static class SalidaActivarDocumentos extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;

        public SalidaActivarDocumentos() {
            P_OUT_ERRORESCONTADOR = 0L;
            P_OUT_MENSAJES = new ArrayList<>();
        }

        public void agregarValidacion(Validacion validacion) {
            if (validacion.EsError) {
                P_OUT_ERRORESCONTADOR++;
            }
            P_OUT_MENSAJES.add(validacion.MensajeError);
        }

        public void agregarValidaciones(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                agregarValidacion(validacion);
            }
        }
    }

    public static class AnexoFirmasProcesadas extends BcsPojo {
        public Long IdAnexo;
        public Long CantidadFirmasProcesadas;

        public AnexoFirmasProcesadas() {
            CantidadFirmasProcesadas = 0L;
        }
    }

    public static class IndicadorDocumento {
        public Long IdTrabajo;
        public Long IdTipoIndicador;
        public Long IdCliente;
        public Long IdEmpresa;
        public String IdTipoDocumento;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaActivarDocumentos logica(Connection conn, EntradaActivarDocumentos entrada) throws Exception {
        logger.info("INICIO::ActivarDocumentos");
        long inicio = System.currentTimeMillis();

        long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();

        SalidaActivarDocumentos salida = new SalidaActivarDocumentos();

        Map<Long, Long> firmasProcesadasPorIdAnexo = obtenerFirmasProcesadasPorIdAnexo(entrada.p_Anexos);
        List<Long> idsAnexos = new ArrayList<>(firmasProcesadasPorIdAnexo.keySet());

        Map<Long, ResumenDocumentoAnexo> resumenDocumentoAnexoPorIdAnexo = SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo(conn, idsAnexos);
        Collection<ResumenDocumentoAnexo> resumenesAnexos = resumenDocumentoAnexoPorIdAnexo.values();

        Map<Boolean, List<Validacion>> cantidadFirmasValidacionesPorEsError = validarCantidadFirmasPorAnexo(conn, entrada.p_IdEmpresa, resumenDocumentoAnexoPorIdAnexo, firmasProcesadasPorIdAnexo);
        List<Validacion> cantidadFirmasValidacionesErrores = cantidadFirmasValidacionesPorEsError.getOrDefault(true, Collections.emptyList());
        List<Validacion> cantidadFirmasValidacionesPendientes = cantidadFirmasValidacionesPorEsError.getOrDefault(false, Collections.emptyList());
        salida.agregarValidaciones(cantidadFirmasValidacionesPendientes);
        if (cantidadFirmasValidacionesErrores.size() > 0) {
            salida.agregarValidaciones(cantidadFirmasValidacionesErrores);

            return salida;
        }

        List<ResumenDocumentoAnexo> resumenesAnexosActivables = obtenerAnexosActivables(resumenesAnexos, cantidadFirmasValidacionesPendientes);
        List<String> idsTiposDocumentos = obtenerIdsTiposDocumentos(resumenesAnexosActivables);
        Map<String, List<TipoDocumentoIndicador>> tiposIndicadoresPorIdTipoDocumento = DocumentoUtils.obtenerTiposIndicadoresPorIdTipoDocumento(conn, entrada.p_IdEmpresa, idsTiposDocumentos);

        List<Validacion> tiposIndicadoresValidaciones = validarTiposIndicadoresPorAnexo(conn, entrada.p_IdEmpresa, resumenesAnexosActivables, tiposIndicadoresPorIdTipoDocumento);
        if (tiposIndicadoresValidaciones.size() > 0) {
            salida.agregarValidaciones(tiposIndicadoresValidaciones);

            return salida;
        }

        List<IndicadorDocumento> indicadoresDocumentos = obtenerIndicadoresDocumentos(entrada.p_IdEmpresa, idTrabajo, resumenesAnexosActivables, tiposIndicadoresPorIdTipoDocumento);

        try {
            List<Anexo> anexos = obtenerAnexos(conn, entrada.p_IdEmpresa, resumenesAnexosActivables);
            activarAnexos(conn, anexos);

            insertarIndicadoresDocumentosTemporal(conn, indicadoresDocumentos);
            upsertIndicadoresDocumentosCliente(conn, entrada.p_IdEmpresa, idTrabajo);

        } catch (Exception e) {
            logger.error("ERROR::ActivarDocumentos", e);
            throw e;

        } finally {
            eliminarIndicadoresDocumentosTemporal(conn, idTrabajo);
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::ActivarDocumentos", fin - inicio);

        return salida;
    }

    private List<ResumenDocumentoAnexo> obtenerAnexosActivables(Collection<ResumenDocumentoAnexo> resumenesAnexos, List<Validacion> cantidadFirmasValidacionesPendientes) {
        logger.trace("INICIO::ActivarDocumentos.obtenerAnexosActivables");
        long inicio = System.currentTimeMillis();

        Set<Long> idsAnexosValidacionesPendientes = cantidadFirmasValidacionesPendientes.stream().map(v -> v.IdEntidad).collect(Collectors.toSet());
        List<ResumenDocumentoAnexo> resumenesAnexosActivables = resumenesAnexos.stream().filter(ra -> !idsAnexosValidacionesPendientes.contains(ra.IdAnexo)).collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ActivarDocumentos.obtenerAnexosActivables", fin - inicio);

        return resumenesAnexosActivables;
    }

    private Map<Long, Long> obtenerFirmasProcesadasPorIdAnexo(List<AnexoFirmasProcesadas> anexos) {
        logger.trace("INICIO::ActivarDocumentos.obtenerFirmasProcesadasPorIdAnexo");
        long inicio = System.currentTimeMillis();

        Map<Long, Long> firmasProcesadasPorIdAnexo = anexos.stream().collect(Collectors.toMap(a -> a.IdAnexo, a -> a.CantidadFirmasProcesadas));

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ActivarDocumentos.obtenerFirmasProcesadasPorIdAnexo", fin - inicio);

        return firmasProcesadasPorIdAnexo;
    }

    private List<Anexo> obtenerAnexos(Connection conn, long idEmpresa, Collection<ResumenDocumentoAnexo> resumenesAnexos) throws Exception {
        logger.debug("INICIO::ActivarDocumentos.obtenerAnexos");
        long inicio = System.currentTimeMillis();

        java.sql.Timestamp fechaActual = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa);

        List<Anexo> anexos = resumenesAnexos.stream().map(ra -> {
            Anexo anexo = new Anexo();
            anexo.IdAnexo = ra.IdAnexo;
            anexo.FechaActivacion = fechaActual;
            anexo.IdEstado = "ACT";

            return anexo;
        }).collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.obtenerAnexos", fin - inicio);

        return anexos;
    }

    private Map<Boolean, List<Validacion>> validarCantidadFirmasPorAnexo(Connection conn, long idEmpresa, Map<Long, ResumenDocumentoAnexo> resumenAnexoPorIdAnexo, Map<Long, Long> firmasProcesadasPorIdAnexo) throws Exception {
        logger.debug("INICIO::ActivarDocumentos.validarCantidadFirmasPorAnexo");
        long inicio = System.currentTimeMillis();

        Map<Long, Long> cantidadFirmasPorIdAnexo = DocumentoUtils.obtenerCantidadFirmasDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(resumenAnexoPorIdAnexo.keySet()));

        HashMap<String, String> params = new HashMap<>(4);
        List<Validacion> validacionesErrores = new ArrayList<>();
        List<Validacion> validacionesPendientes = new ArrayList<>();

        for (ResumenDocumentoAnexo resumenAnexo : resumenAnexoPorIdAnexo.values()) {
            long firmasMinimas = resumenAnexo.NumeroFirmasMinimas;
            long firmasMaximas = resumenAnexo.NumeroFirmasMaximas;
            long firmasInsertadas = cantidadFirmasPorIdAnexo.getOrDefault(resumenAnexo.IdAnexo, 0L);
            long firmasProcesadas = firmasProcesadasPorIdAnexo.getOrDefault(resumenAnexo.IdAnexo, 0L);
            long firmasPresentes = firmasInsertadas + firmasProcesadas;

            long firmasFaltantes = firmasMinimas - firmasPresentes;
            if (firmasFaltantes > 0) {
                params.put("IdAnexo", String.valueOf(resumenAnexo.IdAnexo));
                params.put("FirmasFaltantes", String.valueOf(firmasFaltantes));

                Validacion validacion = new Validacion();
                validacion.EsError = false;
                validacion.MensajeError = new MensajeSalida(resumenAnexo.IdAnexo, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_FALTAN_FIRMAS_EN_ANEXO_FIRMAS_FALTANTES", params, conn, idEmpresa);
                validacion.IdEntidad = resumenAnexo.IdAnexo;
                validacion.IdTipoEntidad = "ANEXO";
                validacionesPendientes.add(validacion);

                params.clear();
                continue;
            }

            long firmasExcedidas = firmasPresentes - firmasMaximas;
            if (firmasExcedidas > 0) {
                params.put("IdAnexo", String.valueOf(resumenAnexo.IdAnexo));
                params.put("FirmasExcedidas", String.valueOf(firmasExcedidas));

                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(resumenAnexo.IdAnexo, MensajeSalida.MENSAJE_ERROR, "UI_CANTIDAD_MAXIMA_FIRMAS_ANEXO_EXCEDIDAS", params, conn, idEmpresa);
                validacion.IdEntidad = resumenAnexo.IdAnexo;
                validacion.IdTipoEntidad = "ANEXO";
                validacionesErrores.add(validacion);

                params.clear();
            }
        }

        Map<Boolean, List<Validacion>> validacionesPorEsError = new HashMap<>(4);
        validacionesPorEsError.put(true, validacionesErrores);
        validacionesPorEsError.put(false, validacionesPendientes);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.validarCantidadFirmasPorAnexo", fin - inicio);

        return validacionesPorEsError;
    }

    private void upsertIndicadoresDocumentosCliente(Connection conn, long idEmpresa, long idTrabajo) throws Exception {
        logger.debug("INICIO::ActivarDocumentos.upsertIndicadoresDocumentosCliente");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdTrabajo", idTrabajo);

        SQL sql = new SQL();
        List<Indicador> indicadores = sql.query(conn, SQL_SELECT_INDICADORES_DOCUMENTOS_CLIENTES, params, Indicador.class);

        java.sql.Timestamp fechaActual = FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa);
        long cantidadIndicadoresInsertar = indicadores.stream().filter(i -> i.IdIndicador == null).count();
        Iterator<Long> idsIndicadores = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Indicador", cantidadIndicadoresInsertar).iterator();

        List<Indicador> indicadoresInsertar = new ArrayList<>();
        List<Indicador> indicadoresActualizar = new ArrayList<>();

        for (Indicador indicador : indicadores) {
            if (indicador.IdIndicador == null) {
                indicador.IdIndicador = idsIndicadores.next();
                indicador.IdTipoEntidad = "CLIENTE";
                indicador.ModeloCliente = "";
                indicador.ValorIndicador = "S";
                indicador.FechaIngreso = fechaActual;
                indicador.FechaModificacion = fechaActual;
                indicador.FechaCreacion = fechaActual;
                indicador.IdEstadoAutorizacion = "ACT";

                indicadoresInsertar.add(indicador);
            } else {
                indicador.ValorIndicador = "S";
                indicador.FechaModificacion = fechaActual;

                indicadoresActualizar.add(indicador);
            }
        }

        sql.batch(conn, SQL_INSERT_INDICADORES_DOCUMENTOS_CLIENTES, indicadoresInsertar, Indicador.class);
        sql.batch(conn, SQL_UPDATE_INDICADORES_DOCUMENTOS_CLIENTES, indicadoresActualizar, Indicador.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.upsertIndicadoresDocumentosCliente", fin - inicio);
    }

    private void activarAnexos(Connection conn, List<Anexo> anexos) throws SQLException {
        logger.debug("INICIO::ActivarDocumentos.activarAnexos");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_ESTADO_ANEXOS, anexos, Anexo.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.activarAnexos", fin - inicio);
    }

    private void eliminarIndicadoresDocumentosTemporal(Connection conn, Long idTrabajo) throws SQLException {
        logger.debug("INICIO::ActivarDocumentos.insertarIndicadoresDocumentosTemporal");
        long inicio = System.currentTimeMillis();

        IndicadorDocumento indicadorDocumentoEliminar = new IndicadorDocumento();
        indicadorDocumentoEliminar.IdTrabajo = idTrabajo;

        List<IndicadorDocumento> indicadoresDocumentosEliminar = new ArrayList<>(Collections.singletonList(indicadorDocumentoEliminar));

        SQL sql = new SQL();
        sql.batch(conn, SQL_DELETE_TEMPORAL_INDICADORES_DOCUMENTOS, indicadoresDocumentosEliminar, IndicadorDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.insertarIndicadoresDocumentosTemporal", fin - inicio);
    }

    private void insertarIndicadoresDocumentosTemporal(Connection conn, List<IndicadorDocumento> indicadoresDocumentos) throws SQLException {
        logger.debug("INICIO::ActivarDocumentos.insertarIndicadoresDocumentosTemporal");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_TEMPORAL_INDICADORES_DOCUMENTOS, indicadoresDocumentos, IndicadorDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ActivarDocumentos.insertarIndicadoresDocumentosTemporal", fin - inicio);
    }

    private List<Validacion> validarTiposIndicadoresPorAnexo(Connection conn, Long idEmpresa, Collection<ResumenDocumentoAnexo> resumenesAnexos, Map<String, List<TipoDocumentoIndicador>> tiposIndicadoresPorIdTipoDocumento) throws Exception {
        logger.debug("INICIO::ActivarDocumentos.validarTiposIndicadoresPorAnexo");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>(2);

        for (ResumenDocumentoAnexo resumenAnexo : resumenesAnexos) {
            List<TipoDocumentoIndicador> tiposIndicadores = tiposIndicadoresPorIdTipoDocumento.getOrDefault(resumenAnexo.IdTipoDocumento, Collections.emptyList());

            for (TipoDocumentoIndicador tipoIndicador : tiposIndicadores) {
                if (tipoIndicador.IdTipoIndicador == null) {
                    params.put("IdTipoDocumentoIndicador", String.valueOf(tipoIndicador.IdTipoDocumentoIndicador));

                    Validacion validacion = new Validacion();
                    validacion.EsError = true;
                    validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_TIPO_INDICADOR", params, conn, idEmpresa);
                    validaciones.add(validacion);

                    params.clear();
                }
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("INICIO::ActivarDocumentos.validarTiposIndicadoresPorAnexo", fin - inicio);

        return validaciones;
    }

    private List<IndicadorDocumento> obtenerIndicadoresDocumentos(long idEmpresa, long idTrabajo, Collection<ResumenDocumentoAnexo> resumenesAnexos, Map<String, List<TipoDocumentoIndicador>> tiposIndicadoresPorIdTipoDocumento) {
        logger.trace("INICIO::ActivarDocumentos.obtenerIndicadoresDocumentos");
        long inicio = System.currentTimeMillis();

        List<IndicadorDocumento> indicadoresDocumentos = new ArrayList<>();

        for (ResumenDocumentoAnexo resumenAnexo : resumenesAnexos) {
            List<TipoDocumentoIndicador> tiposIndicadores = tiposIndicadoresPorIdTipoDocumento.getOrDefault(resumenAnexo.IdTipoDocumento, Collections.emptyList());

            for (TipoDocumentoIndicador tipoIndicador : tiposIndicadores) {
                IndicadorDocumento indicadorDocumento = new IndicadorDocumento();
                indicadorDocumento.IdTrabajo = idTrabajo;
                indicadorDocumento.IdEmpresa = idEmpresa;
                indicadorDocumento.IdTipoIndicador = tipoIndicador.IdTipoIndicador;
                indicadorDocumento.IdTipoDocumento = resumenAnexo.IdTipoDocumento;
                indicadorDocumento.IdCliente = resumenAnexo.IdCliente;

                indicadoresDocumentos.add(indicadorDocumento);
            }
        }

        long fin = System.currentTimeMillis();
        logger.trace("INICIO::ActivarDocumentos.obtenerIndicadoresDocumentos", fin - inicio);

        return indicadoresDocumentos;
    }

    public List<String> obtenerIdsTiposDocumentos(Collection<ResumenDocumentoAnexo> resumenesAnexos) {
        logger.trace("INICIO::ActivarDocumentos.obtenerIdsTiposDocumentos");
        long inicio = System.currentTimeMillis();

        List<String> idsTiposDocumentos = resumenesAnexos.stream().map(ra -> ra.IdTipoDocumento).distinct().collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("INICIO::ActivarDocumentos.obtenerIdsTiposDocumentos", fin - inicio);

        return idsTiposDocumentos;
    }
}
