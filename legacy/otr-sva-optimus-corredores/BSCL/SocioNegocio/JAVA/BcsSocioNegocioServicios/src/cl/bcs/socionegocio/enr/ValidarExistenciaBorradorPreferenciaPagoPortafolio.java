package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.PreferenciaPagoPortafolio;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarExistenciaBorradorPreferenciaPagoPortafolio implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorPreferenciaPagoPortafolio.class);

    private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorPreferenciaPagoPortafolio_SelectPreferenciaPagoPortafolio.sql");

    public static class EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio extends BcsPojo {
        public Long p_IdEmpresa;
        public List<PreferenciaPagoPortafolio> p_PreferenciasPagoPortafolio;

        public EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio() {
            this.p_PreferenciasPagoPortafolio = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorPreferenciaPagoPortafolio extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorPreferenciaPagoPortafolio() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorPreferenciaPagoPortafolio logica(Connection con, EntradaValidarExistenciaBorradorPreferenciaPagoPortafolio entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorPreferenciaPagoPortafolio");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorPreferenciaPagoPortafolio out = new SalidaValidarExistenciaBorradorPreferenciaPagoPortafolio();
        out.validaciones = validarExistenciaPreferenciaPagoPortafolio(con, entrada.p_PreferenciasPagoPortafolio, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorPreferenciaPagoPortafolio", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaPreferenciaPagoPortafolio(Connection con, List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorPreferenciaPagoPortafolio.validarExistenciaPreferenciaPagoPortafolio");
        long inicio = System.currentTimeMillis();

        List<Long> idPreferenciasPagoPortafolio = preferenciasPagoPortafolio.parallelStream().map(preferenciaPagoPortafolio -> preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio).collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdPreferenciaPagoPortafolio", idPreferenciasPagoPortafolio);

        SQL sql = new SQL();
        List<PreferenciaPagoPortafolio> preferenciasPagoPortafolioExistentes = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, parametros, PreferenciaPagoPortafolio.class);
        Map<Long, PreferenciaPagoPortafolio> preferenciasPagoPortafolioPorId = preferenciasPagoPortafolioExistentes.parallelStream().collect(Collectors.toMap(preferenciaPagoPortafolio -> preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio, preferenciaPagoPortafolio -> preferenciaPagoPortafolio));

        List<Validacion> validaciones = new ArrayList<>();

        for (PreferenciaPagoPortafolio preferenciaPagoPortafolio : preferenciasPagoPortafolio) {
            if (preferenciasPagoPortafolioPorId.get(preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                HashMap<String, String> datosMensajes = new HashMap<>(1);
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_PREFERENCIA_PAGO_PORTAFOLIO_NO_EXISTE", datosMensajes, con, idEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorPreferenciaPagoPortafolio.validarExistenciaPreferenciaPagoPortafolio", fin - inicio);

        return validaciones;
    }
}