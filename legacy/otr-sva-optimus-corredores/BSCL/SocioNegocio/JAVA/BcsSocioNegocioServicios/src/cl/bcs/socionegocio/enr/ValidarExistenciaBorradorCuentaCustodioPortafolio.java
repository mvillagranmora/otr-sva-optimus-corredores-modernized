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
import cl.bcs.socionegocio.enr.pojo.CuentaCustodioPortafolio;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarExistenciaBorradorCuentaCustodioPortafolio implements JavaCall {

    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorCuentaCustodioPortafolio.class);

    private final String SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaBorradorCuentaCustodioPortafolio_SelectCuentaCustodioPortafolio.sql");

    public static class EntradaValidarExistenciaBorradorCuentaCustodioPortafolio extends BcsPojo {
        public Long p_IdEmpresa;
        public List<CuentaCustodioPortafolio> p_CuentasCustodioPortafolio;

        public EntradaValidarExistenciaBorradorCuentaCustodioPortafolio() {
            this.p_CuentasCustodioPortafolio = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorCuentaCustodioPortafolio extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorCuentaCustodioPortafolio() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorCuentaCustodioPortafolio logica(Connection con, EntradaValidarExistenciaBorradorCuentaCustodioPortafolio entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorCuentaCustodioPortafolio");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorCuentaCustodioPortafolio out = new SalidaValidarExistenciaBorradorCuentaCustodioPortafolio();
        out.validaciones = validarExistenciaCuentaCustodioPortafolio(con, entrada.p_CuentasCustodioPortafolio, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorCuentaCustodioPortafolio", fin - inicio);

        return out;
    }

    private List<Validacion> validarExistenciaCuentaCustodioPortafolio(Connection con, List<CuentaCustodioPortafolio> cuentasCustodioPortafolio, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ValidarExistenciaBorradorCuentaCustodioPortafolio.validarExistenciaCuentaCustodioPortafolio");
        long inicio = System.currentTimeMillis();

        List<Long> idCuentaCustodioPortafolio = cuentasCustodioPortafolio.parallelStream().map(cuentaCustodioPortafolio -> cuentaCustodioPortafolio.IdCuentaCustodioPortafolio).collect(Collectors.toList());

        HashMap<String, Object> parametros = new HashMap<>(1);
        parametros.put("IdCuentaCustodioPortafolio", idCuentaCustodioPortafolio);

        SQL sql = new SQL();
        List<CuentaCustodioPortafolio> cuentasCustodioPortafolioExistentes = sql.query(con, SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO, parametros, CuentaCustodioPortafolio.class);
        Map<Long, CuentaCustodioPortafolio> cuentasCustodioPortafolioPorIdCuentaCustodio = cuentasCustodioPortafolioExistentes.parallelStream().collect(Collectors.toMap(cuentaCustodioPortafolio -> cuentaCustodioPortafolio.IdCuentaCustodioPortafolio, cuentaCustodioPortafolio -> cuentaCustodioPortafolio));

        List<Validacion> validaciones = new ArrayList<>();

        for (CuentaCustodioPortafolio cuentaCustodioPortafolio : cuentasCustodioPortafolio) {
            if (cuentasCustodioPortafolioPorIdCuentaCustodio.get(cuentaCustodioPortafolio.IdCuentaCustodioPortafolio) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                HashMap<String, String> datosMensajes = new HashMap<>(1);
                datosMensajes.put("Codigo", cuentaCustodioPortafolio.Codigo);
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_CUENTA_CUSTODIO_PORTAFOLIO_NO_EXISTE", datosMensajes, con, idEmpresa);

                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarExistenciaBorradorCuentaCustodioPortafolio.validarExistenciaCuentaCustodioPortafolio", fin - inicio);

        return validaciones;
    }
}