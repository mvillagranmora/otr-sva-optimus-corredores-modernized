package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
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

public class ValidarExistenciaBorradorRelacionPersona implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorRelacionPersona.class);

    private final String SQL_SELECT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorRelacionPersona_SelectRelacionPersona.sql");

    public static class EntradaValidarExistenciaBorradorRelacionPersona extends BcsPojo {
        public Long p_IdEmpresa;
        public List<RelacionPersona> p_RelacionesPersonas;

        public EntradaValidarExistenciaBorradorRelacionPersona() {
            this.p_RelacionesPersonas = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorTarjetaBancaria extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorTarjetaBancaria() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorTarjetaBancaria logica(Connection con, EntradaValidarExistenciaBorradorRelacionPersona entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorTarjetaBancaria");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorTarjetaBancaria out = new SalidaValidarExistenciaBorradorTarjetaBancaria();
        out.validaciones = validarExistenciaRelacionesPersona(con, entrada.p_RelacionesPersonas, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorTarjetaBancaria", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaRelacionesPersona(Connection con, List<RelacionPersona> relacionesPersona, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorRelacionPersona.validarExistenciaRelacionesPersona");
        long inicio = System.currentTimeMillis();

        List<Long> idRelacionPersona = relacionesPersona.parallelStream().map(relacionPersona -> relacionPersona.IdRelacionPersona).collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdRelacionPersona", idRelacionPersona);

        SQL sql = new SQL();
        List<RelacionPersona> relacionesPersonaExistentes = sql.query(con, SQL_SELECT_RELACION_PERSONA, parametros, RelacionPersona.class);
        Map<Long, RelacionPersona> relacionesPersonaPorIdRelacion = relacionesPersonaExistentes.parallelStream().collect(Collectors.toMap(relacionPersona -> relacionPersona.IdRelacionPersona, relacionPersona -> relacionPersona));

        List<Validacion> validaciones = new ArrayList<>();

        for (RelacionPersona relacionPersona : relacionesPersona) {
            if (relacionesPersonaPorIdRelacion.get(relacionPersona.IdRelacionPersona) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("Relacion", relacionPersona.IdTipoRelacion);
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_RELACION_PERSONA_NO_EXISTE", datosMensajes, con, idEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorRelacionPersona.validarExistenciaRelacionesPersona", fin - inicio);

        return validaciones;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":1,\"p_UsaReglasNegocio\":1,\"p_CodigoFormulario\":1,\"p_IdTipoCanal\":1,\"p_RelacionesPersonas\":[{\"IdRelacionPersona\":-1, \"IdTipoRelacion\": \"123\"},{\"IdRelacionPersona\":-2, \"IdTipoRelacion\": \"321\"}]}";

        try (Connection con = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaValidarExistenciaBorradorRelacionPersona entrada = gson.fromJson(jsonEntrada, EntradaValidarExistenciaBorradorRelacionPersona.class);

            ValidarExistenciaBorradorRelacionPersona validador = new ValidarExistenciaBorradorRelacionPersona();
            try {
                SalidaValidarExistenciaBorradorTarjetaBancaria salida = validador.logica(con, entrada);
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
