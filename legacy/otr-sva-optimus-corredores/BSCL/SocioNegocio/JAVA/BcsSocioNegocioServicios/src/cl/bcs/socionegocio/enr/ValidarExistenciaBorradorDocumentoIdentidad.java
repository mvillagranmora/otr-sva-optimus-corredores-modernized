package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidarExistenciaBorradorDocumentoIdentidad implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorDocumentoIdentidad.class);

    private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorDocumentoIdentidad_SelectDocumentoIdentidad.sql");

    public static class EntradaValidarExistenciaBorradorDocumentoIdentidad extends BcsPojo {
        public Long p_IdEmpresa;
        public List<DocumentoIdentidad> p_DocumentosIdentidades;

        public EntradaValidarExistenciaBorradorDocumentoIdentidad() {
            this.p_DocumentosIdentidades = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorDocumentosIdentidades extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorDocumentosIdentidades() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorDocumentosIdentidades logica(Connection con, EntradaValidarExistenciaBorradorDocumentoIdentidad entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorDocumentoIdentidad");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorDocumentosIdentidades out = new SalidaValidarExistenciaBorradorDocumentosIdentidades();
        out.validaciones = validarExistenciaDocumentosIdentidades(con, entrada.p_DocumentosIdentidades, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorDocumentoIdentidad", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaDocumentosIdentidades(Connection con, List<DocumentoIdentidad> documentosIdentidades, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorDocumentoIdentidad.validarExistenciaDocumentosIdentidades");
        long inicio = System.currentTimeMillis();

        List<Long> idsDocumentosIdentidades = documentosIdentidades
                .parallelStream()
                .map(documentoIdentidad -> documentoIdentidad.IdDocumentoIdentidad)
                .collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdDocumentoIdentidad", idsDocumentosIdentidades);

        SQL sql = new SQL();
        List<DocumentoIdentidad> documentosIdentidadesExistentes = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametros, DocumentoIdentidad.class);
        Map<Long, DocumentoIdentidad> documentoIdentidadPorId = documentosIdentidadesExistentes
                .parallelStream()
                .collect(Collectors.toMap(documentoIdentidad -> documentoIdentidad.IdDocumentoIdentidad, documentoIdentidad -> documentoIdentidad));

        List<Validacion> validaciones = new ArrayList<>();

        for (DocumentoIdentidad documentoIdentidad : documentosIdentidades) {
            if (documentoIdentidadPorId.get(documentoIdentidad.IdDocumentoIdentidad) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;

                HashMap<String, String> datosMensajes = new HashMap<>(1);
                String hint = documentoIdentidad.DocumentoIdentidad == null ? "ID: " + documentoIdentidad.IdDocumentoIdentidad : documentoIdentidad.DocumentoIdentidad;
                datosMensajes.put("DocumentoIdentidad", hint);

                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DOCUMENTO_IDENTIDAD_NO_EXISTE", datosMensajes, con, idEmpresa);
                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorDocumentoIdentidad.validarExistenciaDocumentosIdentidades", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"OPT\",\"p_DocumentosIdentidades\":[{\"IdDocumentoIdentidad\":-1,\"IdEnrolamiento\":3,\"IdTipoDocumentoIdentidad\":\"P\",\"DocumentoIdentidad\":\"1111111111\",\"NumeroSerie\":\"11111111111111111111\",\"FechaEmision\":\"2020-08-21\",\"FechaVencimiento\":\"0001-01-01\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorDocumentoIdentidad entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorDocumentoIdentidad.class);

            ValidarExistenciaBorradorDocumentoIdentidad validador = new ValidarExistenciaBorradorDocumentoIdentidad();
            try {
                SalidaValidarExistenciaBorradorDocumentosIdentidades salida = validador.logica(con, entrada);
                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
                con.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
