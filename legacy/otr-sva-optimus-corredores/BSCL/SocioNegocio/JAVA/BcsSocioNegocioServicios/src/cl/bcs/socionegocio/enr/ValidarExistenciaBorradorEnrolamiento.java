package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
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

public class ValidarExistenciaBorradorEnrolamiento implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorEnrolamiento.class);

    private final String SQL_SELECT_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorEnrolamiento_SelectEnrolamiento.sql");

    public static class EntradaValidarExistenciaBorradorEnrolamiento extends BcsPojo {
        public Long p_IdEmpresa;
        public List<Enrolamiento> p_Enrolamientos;

        public EntradaValidarExistenciaBorradorEnrolamiento() {
            this.p_Enrolamientos = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorEnrolamiento extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorEnrolamiento() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorEnrolamiento logica(Connection con, EntradaValidarExistenciaBorradorEnrolamiento entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorEnrolamiento");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorEnrolamiento out = new SalidaValidarExistenciaBorradorEnrolamiento();
        out.validaciones = validarExistenciaEnrolamientos(con, entrada.p_Enrolamientos, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorEnrolamiento", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaEnrolamientos(Connection con, List<Enrolamiento> enrolamientos, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorEnrolamiento.validarExistenciaEnrolamientos");
        long inicio = System.currentTimeMillis();

        List<Long> idsEnrolamientos = enrolamientos
                .parallelStream()
                .map(enrolamiento -> enrolamiento.IdEnrolamiento)
                .collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdsEnrolamientos", idsEnrolamientos);

        SQL sql = new SQL();
        List<Enrolamiento> enrolamientosExistentes = sql.query(con, SQL_SELECT_ENROLAMIENTO, parametros, Enrolamiento.class);
        Map<Long, Enrolamiento> enrolamientoPorId = enrolamientosExistentes
                .parallelStream()
                .collect(Collectors.toMap(enrolamiento -> enrolamiento.IdEnrolamiento, enrolamiento -> enrolamiento));

        List<Validacion> validaciones = new ArrayList<>();

        for (Enrolamiento enrolamiento : enrolamientos) {
            if (enrolamientoPorId.get(enrolamiento.IdEnrolamiento) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;

                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("Enrolamiento", "ID: " + enrolamiento.IdEnrolamiento);

                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_ENROLAMIENTO NO EXISTE", datosMensajes, con, idEmpresa);
                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorEnrolamiento.validarExistenciaEnrolamientos", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"OPT\",\"p_Enrolamientos\":[{\"IdEnrolamiento\":-1,\"IdTipoCliente\":\"RETAIL\",\"IdCategoriaInversionista\":\"ARRIESGADO\",\"IdSegmentoCliente\":0,\"IdEmpleadoEjecutivo\":0,\"CodigoEjecutivoExterno\":\"E20210203\",\"IdSucursalPreferencia\":1,\"EsClienteFinal\":\"N\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorEnrolamiento entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorEnrolamiento.class);

            ValidarExistenciaBorradorEnrolamiento validador = new ValidarExistenciaBorradorEnrolamiento();
            try {
                SalidaValidarExistenciaBorradorEnrolamiento salida = validador.logica(con, entrada);
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
