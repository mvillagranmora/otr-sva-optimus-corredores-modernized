package cl.bcs.plataforma.doc;

import cl.bcs.plataforma.doc.pojo.TipoDocumento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.doc.DocumentoUtils;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModificarTipoDocumentoMasivo implements JavaCall {
    static final Logger logger = LogManager.getLogger(ModificarTipoDocumentoMasivo.class);

    private final String SQL_INSERT_TIPO_DOCUMENTO_INDICADOR = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarTipoDocumentoMasivo_InsertTipoDocumentoIndicador.sql");
    private final String SQL_SELECT_TIPO_DOCUMENTOS = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarTipoDocumentoMasivo_SelectTipoDocumentos.sql");
    private final String SQL_UPDATE_TIPO_DOCUMENTO = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarTipoDocumentoMasivo_UpdateTipoDocumento.sql");
    private final String SQL_DELETE_TIPO_DOCUMENTO_INDICADOR = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarTipoDocumentoMasivo_DeleteTipoDocumentoIndicador.sql");

    public static class EntradaModificarTipoDocumentoMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public List<TipoDocumento> p_TipoDocumentos;

        public EntradaModificarTipoDocumentoMasivo() {
            p_TipoDocumentos = new ArrayList<>();
        }
    }

    public static class SalidaModificarTipoDocumentoMasivo extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

        public SalidaModificarTipoDocumentoMasivo() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }

        public void agregarValidaciones(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                agregarValidacion(validacion);
            }
        }

        private void agregarValidacion(Validacion validacion) {
            if (validacion.EsError) {
                P_OUT_ERRORESCONTADOR += 1L;
            }
            P_OUT_MENSAJES.add(validacion.MensajeError);
        }
    }

    public static class ResultadoValidacionRequisitos extends BcsPojo {
        public List<Validacion> validaciones;
        public Set<String> tipoDocumentosRequisitosCambiaron;

        public ResultadoValidacionRequisitos() {
            validaciones = new ArrayList<>();
            tipoDocumentosRequisitosCambiaron = new HashSet<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaModificarTipoDocumentoMasivo entrada = gson.fromJson(in, EntradaModificarTipoDocumentoMasivo.class);
        SalidaModificarTipoDocumentoMasivo salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }

    private SalidaModificarTipoDocumentoMasivo logica(Connection conn, EntradaModificarTipoDocumentoMasivo entrada) throws Exception {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo");
        long inicio = System.currentTimeMillis();

        SalidaModificarTipoDocumentoMasivo salida = new SalidaModificarTipoDocumentoMasivo();

        entrada.p_TipoDocumentos.forEach(td -> td.IdEmpresa = entrada.p_IdEmpresa);
        List<String> idsTipoDocumentosEntrantes = obtenerIdsTipoDocumentos(entrada.p_TipoDocumentos);
        List<TipoDocumento> tipoDocumentosIngresados = obtenerTipoDocumentos(conn, entrada.p_IdEmpresa, idsTipoDocumentosEntrantes);

        List<Validacion> tipoDocumentosValidaciones = validarTipoDocumentos(conn, entrada.p_IdEmpresa, entrada.p_TipoDocumentos, tipoDocumentosIngresados);
        if (tipoDocumentosValidaciones.size() > 0) {
            salida.agregarValidaciones(tipoDocumentosValidaciones);

            return salida;
        }

        actualizarTipoDocumentos(conn, entrada.p_TipoDocumentos);

        Map<String, List<TipoDocumentoIndicador>> requisitosIngresadosPorIdTipoDocumento = DocumentoUtils.obtenerTiposIndicadoresPorIdTipoDocumento(conn, entrada.p_IdEmpresa, idsTipoDocumentosEntrantes);
        Map<String, List<Long>> idsTipoIndicadoresIngresadosPorIdTipoDocumento = obtenerIdsTipoIndicadoresPorIdTipoDocumento(requisitosIngresadosPorIdTipoDocumento);
        Map<String, List<Long>> idsTipoIndicadoresEntrantesPorIdTipoDocumento = obtenerIdsTipoIndicadoresPorIdTipoDocumento(entrada.p_TipoDocumentos);

        ResultadoValidacionRequisitos resultadoValidacionRequisitos = validarTipoDocumentosRequisitos(conn, entrada.p_IdEmpresa, idsTipoIndicadoresEntrantesPorIdTipoDocumento, idsTipoIndicadoresIngresadosPorIdTipoDocumento);
        if (resultadoValidacionRequisitos.validaciones.size() > 0) {
            salida.agregarValidaciones(resultadoValidacionRequisitos.validaciones);

            return salida;
        }

        List<TipoDocumento> tipoDocumentoRequierenActualizarIndicadores = entrada.p_TipoDocumentos
                .stream()
                .filter(td -> resultadoValidacionRequisitos.tipoDocumentosRequisitosCambiaron.contains(td.IdTipoDocumento))
                .collect(Collectors.toList());
        eliminarTipoDocumentosIndicadores(conn, tipoDocumentoRequierenActualizarIndicadores);

        List<TipoDocumentoIndicador> tipoDocumentoIndicadoresActualizar = DocumentoUtils.obtenerTipoDocumentoIndicadores(entrada.p_IdEmpresa, tipoDocumentoRequierenActualizarIndicadores);
        DocumentoUtils.asignarIdsTipoDocumentoIndicadores(conn, tipoDocumentoIndicadoresActualizar);
        insertarTipoDocumentoIndicadores(conn, tipoDocumentoIndicadoresActualizar);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo", fin - inicio);

        return salida;
    }

    private void actualizarTipoDocumentos(Connection conn, List<TipoDocumento> tipoDocumentos) throws SQLException {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.actualizarTipoDocumentos");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_TIPO_DOCUMENTO, tipoDocumentos, TipoDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.actualizarTipoDocumentos", fin - inicio);
    }

    private void insertarTipoDocumentoIndicadores(Connection conn, List<TipoDocumentoIndicador> tipoDocumentoIndicadoresActualizar) throws SQLException {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.eliminarTipoDocumentosIndicadores");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_TIPO_DOCUMENTO_INDICADOR, tipoDocumentoIndicadoresActualizar, TipoDocumentoIndicador.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.eliminarTipoDocumentosIndicadores", fin - inicio);
    }

    private void eliminarTipoDocumentosIndicadores(Connection conn, List<TipoDocumento> tipoDocumentoRequierenActualizarIndicadores) throws SQLException {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.eliminarTipoDocumentosIndicadores");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_DELETE_TIPO_DOCUMENTO_INDICADOR, tipoDocumentoRequierenActualizarIndicadores, TipoDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.eliminarTipoDocumentosIndicadores", fin - inicio);
    }

    private Map<String, List<Long>> obtenerIdsTipoIndicadoresPorIdTipoDocumento(List<TipoDocumento> tipoDocumentos) {
        logger.trace("INICIO::ModificarTipoDocumentoMasivo.obtenerIdsRequisitosPorIdTipoDocumento");
        long inicio = System.currentTimeMillis();

        Map<String, List<Long>> idsRequisitosPorIdTipoDocumento = tipoDocumentos
                .stream()
                .flatMap(td -> td.Requisitos.stream().map(req -> Pair.of(td.IdTipoDocumento, req.IdTipoIndicador)))
                .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarTipoDocumentoMasivo.obtenerIdsRequisitosPorIdTipoDocumento", fin - inicio);

        return idsRequisitosPorIdTipoDocumento;
    }

    private Map<String, List<Long>> obtenerIdsTipoIndicadoresPorIdTipoDocumento(Map<String, List<TipoDocumentoIndicador>> requisitosPorIdTipoDocumento) {
        logger.trace("INICIO::ModificarTipoDocumentoMasivo.obtenerIdsRequisitosPorIdTipoDocumento");
        long inicio = System.currentTimeMillis();

        Map<String, List<Long>> idsTipoIndicadoresPorIdTipoDocumento = requisitosPorIdTipoDocumento
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(req -> req.IdTipoDocumento, Collectors.mapping(req -> req.IdTipoIndicador, Collectors.toList())));

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarTipoDocumentoMasivo.obtenerIdsRequisitosPorIdTipoDocumento", fin - inicio);

        return idsTipoIndicadoresPorIdTipoDocumento;
    }

    private List<Validacion> validarTipoDocumentos(Connection conn, Long idEmpresa, List<TipoDocumento> tipoDocumentosEntrantes, List<TipoDocumento> tipoDocumentosIngresados) throws Exception {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.validarTipoDocumentos");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        Set<String> idsTipoDocumentosEntrantesSet = new HashSet<>();
        Set<String> idsTipoDocumentosIngresadosSet = new HashSet<>(obtenerIdsTipoDocumentos(tipoDocumentosIngresados));

        HashMap<String, String> params = new HashMap<>(2);

        for (TipoDocumento tipoDocumentoEntrante : tipoDocumentosEntrantes) {
            String idTipoDocumentoEntrante = tipoDocumentoEntrante.IdTipoDocumento;

            boolean duplicado = !idsTipoDocumentosEntrantesSet.add(idTipoDocumentoEntrante);
            if (duplicado) {
                params.put("IdTipoDocumento", idTipoDocumentoEntrante);

                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_TIPO_DOCUMENTO_DUPLICADO", params, conn, idEmpresa);

                validaciones.add(validacion);
                params.clear();
            }

            boolean noExiste = !idsTipoDocumentosIngresadosSet.contains(idTipoDocumentoEntrante);
            if (noExiste) {
                params.put("IdTipoDocumento", idTipoDocumentoEntrante);

                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_TIPO_DOCUMENTO", params, conn, idEmpresa);

                validaciones.add(validacion);
                params.clear();
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.validarTipoDocumentos", fin - inicio);

        return validaciones;
    }

    private ResultadoValidacionRequisitos validarTipoDocumentosRequisitos(Connection conn, Long idEmpresa, Map<String, List<Long>> idsTipoIndicadoresEntrantesPorIdTipoDocumento, Map<String, List<Long>> idsTipoIndicadoresIngresadosPorIdTipoDocumento) throws Exception {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.validarTipoDocumentosRequisitos");
        long inicio = System.currentTimeMillis();

        ResultadoValidacionRequisitos resultadoValidacionRequisitos = new ResultadoValidacionRequisitos();

        List<Validacion> validaciones = new ArrayList<>();
        Set<String> idsTipoDocumentosRequisitosCambiaron = new HashSet<>();

        Set<Long> idsTipoIndicadoresEntrantesSet = new HashSet<>();
        Set<Long> idsTipoIndicadoresIngresadosSet = new HashSet<>();

        HashMap<String, String> params = new HashMap<>(4);

        for (String idTipoDocumentoEntrante : idsTipoIndicadoresEntrantesPorIdTipoDocumento.keySet()) {
            List<Long> idsTipoIndicadoresEntrantes = idsTipoIndicadoresEntrantesPorIdTipoDocumento.getOrDefault(idTipoDocumentoEntrante, Collections.emptyList());

            idsTipoIndicadoresEntrantesSet.clear();
            idsTipoIndicadoresIngresadosSet.clear();

            idsTipoIndicadoresIngresadosSet.addAll(idsTipoIndicadoresIngresadosPorIdTipoDocumento.getOrDefault(idTipoDocumentoEntrante, Collections.emptyList()));
            boolean todosRequisitosEntrantesEstanIngresados = true;

            for (Long idTipoIndicadorEntrante : idsTipoIndicadoresEntrantes) {
                boolean duplicado = !idsTipoIndicadoresEntrantesSet.add(idTipoIndicadorEntrante);
                if (duplicado) {
                    params.put("IdTipoDocumento", idTipoDocumentoEntrante);
                    params.put("IdTipoIndicador", String.valueOf(idTipoIndicadorEntrante));

                    Validacion validacion = new Validacion();
                    validacion.EsError = true;
                    validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_REQUISITO_DUPLICADO_PARA_TIPO_DOCUMENTO", params, conn, idEmpresa);

                    validaciones.add(validacion);
                    params.clear();
                }

                todosRequisitosEntrantesEstanIngresados = todosRequisitosEntrantesEstanIngresados && idsTipoIndicadoresIngresadosSet.contains(idTipoIndicadorEntrante);
            }

            boolean requiereActualizarRequisitos = idsTipoIndicadoresIngresadosSet.size() != idsTipoIndicadoresEntrantesSet.size() || !todosRequisitosEntrantesEstanIngresados;
            if (requiereActualizarRequisitos) {
                idsTipoDocumentosRequisitosCambiaron.add(idTipoDocumentoEntrante);
            }
        }

        resultadoValidacionRequisitos.validaciones.addAll(validaciones);
        resultadoValidacionRequisitos.tipoDocumentosRequisitosCambiaron.addAll(idsTipoDocumentosRequisitosCambiaron);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.validarTipoDocumentosRequisitos", fin - inicio);

        return resultadoValidacionRequisitos;
    }

    private List<String> obtenerIdsTipoDocumentos(List<TipoDocumento> tipoDocumentos) {
        logger.trace("INICIO::ModificarTipoDocumentoMasivo.obtenerIdsTipoDocumentos");
        long inicio = System.currentTimeMillis();

        List<String> idsTipoDocumentos = tipoDocumentos.stream().map(td -> td.IdTipoDocumento).collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ModificarTipoDocumentoMasivo.obtenerIdsTipoDocumentos", fin - inicio);

        return idsTipoDocumentos;
    }

    private List<TipoDocumento> obtenerTipoDocumentos(Connection conn, Long idEmpresa, List<String> idsTipoDocumentos) throws SQLException {
        logger.debug("INICIO::ModificarTipoDocumentoMasivo.obtenerTipoDocumentos");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("IdEmpresa", idEmpresa);
        params.put("IdsTipoDocumentos", idsTipoDocumentos);

        SQL sql = new SQL();
        List<TipoDocumento> tipoDocumentos = sql.query(conn, SQL_SELECT_TIPO_DOCUMENTOS, params, TipoDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ModificarTipoDocumentoMasivo.obtenerTipoDocumentos", fin - inicio);

        return tipoDocumentos;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_TipoDocumentos\":[{\"IdTipoDocumento\":\"PRUEBA01JAVA1563\",\"NombreDocumento\":\"PRUEBA 01 JAVA 1563 V01\",\"NumeroFirmantesMinimo\":1,\"NumeroFirmantesMaximo\":10,\"Glosa\":\"CONTRATOS\",\"ValidaFechaVigencia\":\"N\",\"IdTipoPersona\":\"A\",\"IdClase\":1,\"TipoDocumentoNormativa\":null,\"FechaFirma\":\"N\",\"CodigoArchivo\":\"PRUEBA-10\",\"ValidaClienteFull\":\"N\",\"Requisitos\":[{\"IdTipoIndicador\":14},{\"IdTipoIndicador\":15},{\"IdTipoIndicador\":16}]},{\"IdTipoDocumento\":\"PRUEBA02JAVA1563\",\"NombreDocumento\":\"PRUEBA 02 JAVA 1563 V01\",\"NumeroFirmantesMinimo\":2,\"NumeroFirmantesMaximo\":20,\"Clasificacion\":\"CONTRATOS\",\"ValidaFechaVigencia\":\"S\",\"IdTipoPersona\":\"A\",\"IdClase\":1,\"TipoDocumentoNormativa\":null,\"FechaFirma\":\"S\",\"CodigoArchivo\":\"PRUEBA-10\",\"ValidaClienteFull\":\"S\",\"Requisitos\":[{\"IdTipoIndicador\":17},{\"IdTipoIndicador\":46}]}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaModificarTipoDocumentoMasivo entradaModificarTipoDocumentoMasivo = gson.fromJson(jsonRequest, EntradaModificarTipoDocumentoMasivo.class);
            System.out.println("Entrada:");
            System.out.println(entradaModificarTipoDocumentoMasivo);

            ModificarTipoDocumentoMasivo modificarTipoDocumentoMasivo = new ModificarTipoDocumentoMasivo();
            try {
                SalidaModificarTipoDocumentoMasivo salidaModificarTipoDocumentoMasivo = modificarTipoDocumentoMasivo.logica(conn, entradaModificarTipoDocumentoMasivo);
                System.out.println("Salida:");
                System.out.println(salidaModificarTipoDocumentoMasivo);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
