package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorDocumentoIdentidad.EntradaValidarExistenciaBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
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
import java.util.List;
import java.util.stream.Collectors;

public class EliminarBorradorDocumentoIdentidad implements JavaCall {

    static final Logger logger = LogManager.getLogger(EliminarBorradorDocumentoIdentidad.class);

    private final String SQL_DELETE_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorDocumentoIdentidad_DeleteDocumentoIdentidad.sql");

    public static class EntradaEliminarBorradorDocumentoIdentidad extends EntradaAdministrarBorrador {
        public List<DocumentoIdentidad> p_DocumentosIdentidades;

        public EntradaEliminarBorradorDocumentoIdentidad() {
            this.p_DocumentosIdentidades = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaEliminarBorradorDocumentoIdentidad jin = gson.fromJson(in, EntradaEliminarBorradorDocumentoIdentidad.class);
        SalidaAdministrarBorrador out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    private SalidaAdministrarBorrador logica(Connection conn, EntradaEliminarBorradorDocumentoIdentidad jin) throws Exception {
        logger.info("INICIO::EliminarBorradorDocumentoIdentidad");
        long inicio = System.currentTimeMillis();

        SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

        ValidarExistenciaBorradorDocumentoIdentidad validarExistenciaBorradorDocumentoIdentidad = new ValidarExistenciaBorradorDocumentoIdentidad();
        EntradaValidarExistenciaBorradorDocumentoIdentidad entradaValidarExistenciaBorradorDocumentoIdentidad = new ValidarExistenciaBorradorDocumentoIdentidad.EntradaValidarExistenciaBorradorDocumentoIdentidad();
        entradaValidarExistenciaBorradorDocumentoIdentidad.p_IdEmpresa = jin.p_IdEmpresa;
        entradaValidarExistenciaBorradorDocumentoIdentidad.p_DocumentosIdentidades = jin.p_DocumentosIdentidades;

        List<Validacion> validaciones = validarExistenciaBorradorDocumentoIdentidad.logica(conn, entradaValidarExistenciaBorradorDocumentoIdentidad).validaciones;
        out.agregarValidacionesSalida(validaciones);

        if (out.P_OUT_ERRORESCONTADOR > 0) {
            return out;
        }

        eliminarDocumentosIdentidades(conn, jin.p_DocumentosIdentidades);

        out.P_OUT_REFID = obtenerIdsEliminados(jin.p_DocumentosIdentidades);

        long fin = System.currentTimeMillis();
        logger.info("FIN::EliminarBorradorDocumentoIdentidad", fin - inicio);

        return out;
    }

    private void eliminarDocumentosIdentidades(Connection conn, List<DocumentoIdentidad> documentosIdentidades) throws SQLException {
        logger.debug("INICIO::EliminarBorradorDocumentoIdentidad.eliminarDocumentosIdentidades");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_DELETE_DOCUMENTO_IDENTIDAD, documentosIdentidades, DocumentoIdentidad.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::EliminarBorradorDocumentoIdentidad.eliminarDocumentosIdentidades", fin - inicio);
    }

    private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<DocumentoIdentidad> documentosIdentidades) {
        logger.trace("INICIO::EliminarBorradorDocumentoIdentidad.obtenerIdsEliminados");
        long inicio = System.currentTimeMillis();

        List<ResultadoAdministrarBorradorEntidad> resultados = documentosIdentidades
                .parallelStream()
                .map(documentoIdentidad -> {
                    ResultadoAdministrarBorradorEntidad resultado = new ResultadoAdministrarBorradorEntidad();
                    resultado.IdInterno = documentoIdentidad.IdInterno;
                    resultado.IdEntidad = documentoIdentidad.IdDocumentoIdentidad;

                    return resultado;
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.trace("FIN::EliminarBorradorDocumentoIdentidad.obtenerIdsEliminados", fin - inicio);

        return resultados;
    }

    public static void main(String[] args) {
    	System.out.println("== INICIO TEST ==");
    	
    	String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"OPT\",\"p_DocumentosIdentidades\":[{\"IdDocumentoIdentidad\":44,\"IdEnrolamiento\":3,\"IdTipoDocumentoIdentidad\":\"P\",\"DocumentoIdentidad\":\"1111111111\",\"NumeroSerie\":\"11111111111111111111\",\"FechaEmision\":\"2020-08-21\",\"FechaVencimiento\":\"0001-01-01\"}]}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaEliminarBorradorDocumentoIdentidad entrada = gson.fromJson(jsonEntrada, EntradaEliminarBorradorDocumentoIdentidad.class);

            try {
                EliminarBorradorDocumentoIdentidad eliminarBorradorDocumentoIdentidad = new EliminarBorradorDocumentoIdentidad();
                SalidaAdministrarBorrador salida = eliminarBorradorDocumentoIdentidad.logica(conn, entrada);

                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("== FIN TEST ==");
    }
}
