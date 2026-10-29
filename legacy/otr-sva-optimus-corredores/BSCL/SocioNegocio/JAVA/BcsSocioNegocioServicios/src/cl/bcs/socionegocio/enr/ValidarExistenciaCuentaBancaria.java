package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.CuentaBancaria;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidarExistenciaCuentaBancaria implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaCuentaBancaria.class);

    private final String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaCuentaBancaria_SelectCuentaBancaria.sql");

    public static class EntradaValidarExistenciaBorradorCuentaBancaria extends BcsPojo {
        public Long p_IdEmpresa;
        public List<CuentaBancaria> p_CuentasBancarias;

        public EntradaValidarExistenciaBorradorCuentaBancaria() {
            this.p_CuentasBancarias = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorCuentaBancaria extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorCuentaBancaria() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorCuentaBancaria logica(Connection con, EntradaValidarExistenciaBorradorCuentaBancaria entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorCuentaBancaria");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorCuentaBancaria out = new SalidaValidarExistenciaBorradorCuentaBancaria();
        out.validaciones = validarExistenciaCuentasBancarias(con, entrada.p_CuentasBancarias, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorCuentaBancaria", fin - inicio);

        return out;
    }

	private List<Validacion> validarExistenciaCuentasBancarias(Connection con, List<CuentaBancaria> cuentasBancarias, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ValidarExistenciaCuentaBancaria.validarExistenciaCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> idCuentaBancaria = cuentasBancarias.parallelStream().map(cuentaBancaria->{
			return cuentaBancaria.IdCuentaBancaria;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdCuentaBancaria", idCuentaBancaria);
		
		SQL sql = new SQL();
		List<CuentaBancaria> cuentasBancariasExistentes = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, parametros, CuentaBancaria.class);
		Map<Long, CuentaBancaria> cuentasBancariasPorIdCuenta = cuentasBancariasExistentes.parallelStream().collect(Collectors.toMap(cuentaBancaria->cuentaBancaria.IdCuentaBancaria, cuentaBancaria->cuentaBancaria));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(CuentaBancaria cuentaBancaria : cuentasBancarias) {
			if(cuentasBancariasPorIdCuenta.get(cuentaBancaria.IdCuentaBancaria) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("NumeroCuentaBancaria", cuentaBancaria.NumeroCuentaBancaria);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_CUENTA_BANCARIA_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarExistenciaCuentaBancaria.validarExistenciaCuentasBancarias", fin - inicio);
		return validaciones;
	}
}
