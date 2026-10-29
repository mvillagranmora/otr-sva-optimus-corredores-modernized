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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AgregarTipoDocumentoMasivo implements JavaCall {
    static final Logger logger = LogManager.getLogger(AgregarTipoDocumentoMasivo.class);

    private final String SQL_INSERT_TIPO_DOCUMENTO = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarTipoDocumentoMasivo_InsertTipoDocumento.sql");
    private final String SQL_INSERT_TIPO_DOCUMENTO_INDICADOR = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarTipoDocumentoMasivo_InsertTipoDocumentoIndicador.sql");
    private final String SQL_SELECT_TIPO_DOCUMENTOS = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarTipoDocumentoMasivo_SelectTipoDocumentos.sql");

    public static class EntradaAgregarTipoDocumentoMasivo extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public List<TipoDocumento> p_TipoDocumentos;

        public EntradaAgregarTipoDocumentoMasivo() {
            p_TipoDocumentos = new ArrayList<>();
        }
    }

    public static class SalidaAgregarTipoDocumentoMasivo extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

        public SalidaAgregarTipoDocumentoMasivo() {
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

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaAgregarTipoDocumentoMasivo entrada = gson.fromJson(in, EntradaAgregarTipoDocumentoMasivo.class);
        SalidaAgregarTipoDocumentoMasivo salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }

    private SalidaAgregarTipoDocumentoMasivo logica(Connection conn, EntradaAgregarTipoDocumentoMasivo entrada) throws Exception {
        logger.info("INICIO::AgregarTipoDocumentoMasivo");
        long inicio = System.currentTimeMillis();

        SalidaAgregarTipoDocumentoMasivo salida = new SalidaAgregarTipoDocumentoMasivo();

        List<Validacion> validacionesTipoDocumentos = validarTipoDocumentos(conn, entrada);
        if (validacionesTipoDocumentos.size() > 0) {
            salida.agregarValidaciones(validacionesTipoDocumentos);

            return salida;
        }

        entrada.p_TipoDocumentos.forEach(td -> td.IdEmpresa = entrada.p_IdEmpresa);
        insertarTipoDocumentos(conn, entrada.p_TipoDocumentos);

        List<TipoDocumentoIndicador> tipoDocumentoIndicadores = DocumentoUtils.obtenerTipoDocumentoIndicadores(entrada.p_IdEmpresa, entrada.p_TipoDocumentos);
        DocumentoUtils.asignarIdsTipoDocumentoIndicadores(conn, tipoDocumentoIndicadores);
        insertarRequisitosTipoDocumentos(conn, tipoDocumentoIndicadores);

        long fin = System.currentTimeMillis();
        logger.info("FIN::AgregarTipoDocumentoMasivo", fin - inicio);

        return salida;
    }

    private void insertarRequisitosTipoDocumentos(Connection conn, List<TipoDocumentoIndicador> requisitos) throws SQLException {
        logger.debug("INICIO::AgregarTipoDocumentoMasivo.insertarRequisitosTipoDocumentos");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_TIPO_DOCUMENTO_INDICADOR, requisitos, TipoDocumentoIndicador.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AgregarTipoDocumentoMasivo.insertarRequisitosTipoDocumentos", fin - inicio);
    }

    private void insertarTipoDocumentos(Connection conn, List<TipoDocumento> tipoDocumentos) throws SQLException {
        logger.debug("INICIO::AgregarTipoDocumentoMasivo.insertarTipoDocumentos");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_TIPO_DOCUMENTO, tipoDocumentos, TipoDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AgregarTipoDocumentoMasivo.insertarTipoDocumentos", fin - inicio);
    }

    private List<Validacion> validarTipoDocumentos(Connection conn, EntradaAgregarTipoDocumentoMasivo entrada) throws Exception {
        logger.debug("INICIO::AgregarTipoDocumentoMasivo.validarTipoDocumentos");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>(4);

        if (entrada.p_TipoDocumentos.isEmpty()) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_HAY_TIPO_DOCUMENTOS_PARA_PROCESAR", params, conn, entrada.p_IdEmpresa);

            validaciones.add(validacion);
            params.clear();
        }

        List<String> idsTipoDocumentos = entrada.p_TipoDocumentos.stream().map(td -> td.IdTipoDocumento).collect(Collectors.toList());
        List<TipoDocumento> tipoDocumentosDuplicados = obtenerTipoDocumentos(conn, entrada.p_IdEmpresa, idsTipoDocumentos);
        for (TipoDocumento tipoDocumento : tipoDocumentosDuplicados) {
            params.put("IdTipoDocumento", tipoDocumento.IdTipoDocumento);

            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_TIPO_DOCUMENTO_EXISTE", params, conn, entrada.p_IdEmpresa);

            validaciones.add(validacion);
            params.clear();
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AgregarTipoDocumentoMasivo.validarTipoDocumentos", fin - inicio);

        return validaciones;
    }

    private List<TipoDocumento> obtenerTipoDocumentos(Connection conn, Long idEmpresa, List<String> idsTipoDocumentos) throws SQLException {
        logger.debug("INICIO::AgregarTipoDocumentoMasivo.obtenerTipoDocumentos");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("IdsTipoDocumentos", idsTipoDocumentos);
        params.put("IdEmpresa", idEmpresa);

        SQL sql = new SQL();
        List<TipoDocumento> tipoDocumentos = sql.query(conn, SQL_SELECT_TIPO_DOCUMENTOS, params, TipoDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::AgregarTipoDocumentoMasivo.obtenerTipoDocumentos", fin - inicio);

        return tipoDocumentos;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_TipoDocumentos\":[{\"IdTipoDocumento\":\"PRUEBA01JAVA1563\",\"NombreDocumento\":\"PRUEBA 01 JAVA 1563\",\"NumeroFirmantesMinimo\":1,\"NumeroFirmantesMaximo\":10,\"Clasificacion\":\"CONTRATOS\",\"ValidaFechaVigencia\":\"N\",\"IdTipoPersona\":\"A\",\"IdClase\":1,\"TipoDocumentoNormativa\":null,\"FechaFirma\":\"N\",\"CodigoArchivo\":\"PRUEBA-10\",\"ValidaClienteFull\":\"N\",\"Requisitos\":[{\"IdTipoIndicador\":14},{\"IdTipoIndicador\":15},{\"IdTipoIndicador\":16}]},{\"IdTipoDocumento\":\"PRUEBA02JAVA1563\",\"NombreDocumento\":\"PRUEBA 02 JAVA 1563\",\"NumeroFirmantesMinimo\":2,\"NumeroFirmantesMaximo\":20,\"Clasificacion\":\"CONTRATOS\",\"ValidaFechaVigencia\":\"S\",\"IdTipoPersona\":\"A\",\"IdClase\":1,\"TipoDocumentoNormativa\":null,\"FechaFirma\":\"S\",\"CodigoArchivo\":\"PRUEBA-10\",\"ValidaClienteFull\":\"S\",\"Requisitos\":[{\"IdTipoIndicador\":17}]}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaAgregarTipoDocumentoMasivo entradaAgregarTipoDocumentoMasivo = gson.fromJson(jsonRequest, EntradaAgregarTipoDocumentoMasivo.class);
            System.out.println("Entrada:");
            System.out.println(entradaAgregarTipoDocumentoMasivo);

            AgregarTipoDocumentoMasivo agregarTipoDocumentoMasivo = new AgregarTipoDocumentoMasivo();
            try {
                SalidaAgregarTipoDocumentoMasivo salidaAgregarTipoDocumentoMasivo = agregarTipoDocumentoMasivo.logica(conn, entradaAgregarTipoDocumentoMasivo);
                System.out.println("Salida:");
                System.out.println(salidaAgregarTipoDocumentoMasivo);

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
