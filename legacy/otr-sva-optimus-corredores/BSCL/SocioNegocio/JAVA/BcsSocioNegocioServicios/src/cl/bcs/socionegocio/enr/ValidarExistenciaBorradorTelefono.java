package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.Telefono;
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

public class ValidarExistenciaBorradorTelefono implements JavaCall {
    static final Logger logger = LogManager.getLogger(ValidarExistenciaBorradorTelefono.class);

    private final String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ValidarExistenciaTelefono_SelectTelefono.sql");

    public static class EntradaValidarExistenciaBorradorTelefono extends BcsPojo {
        public Long p_IdEmpresa;
        public List<Telefono> p_Telefonos;

        public EntradaValidarExistenciaBorradorTelefono() {
            this.p_Telefonos = new ArrayList<>();
        }
    }

    public static class SalidaValidarExistenciaBorradorTelefono extends BcsPojo {
        public List<Validacion> validaciones;

        public SalidaValidarExistenciaBorradorTelefono() {
            this.validaciones = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public SalidaValidarExistenciaBorradorTelefono logica(Connection con, EntradaValidarExistenciaBorradorTelefono entrada) throws Exception {
        logger.info("INICIO::ValidarExistenciaBorradorTelefono");
        long inicio = System.currentTimeMillis();

        SalidaValidarExistenciaBorradorTelefono out = new SalidaValidarExistenciaBorradorTelefono();
        out.validaciones = validarExistenciaTelefonos(con, entrada.p_Telefonos, entrada.p_IdEmpresa);

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarExistenciaBorradorTelefono", fin - inicio);

        return out;
    }

	private List<Validacion> validarExistenciaTelefonos(Connection con, List<Telefono> telefonos, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ValidarExistenciaBorradorTelefono.validarExistenciaTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idTelefonos = telefonos.parallelStream().map(telefono->{
			return telefono.IdTelefono;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdTelefono", idTelefonos);
		
		SQL sql = new SQL();
		List<Telefono> telefonosExistentes = sql.query(con, SQL_SELECT_TELEFONO, parametros, Telefono.class);
		Map<Long, Telefono> telefonosPorId = telefonosExistentes.parallelStream().collect(Collectors.toMap(telefono->telefono.IdTelefono, telefono->telefono));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
				
		for(Telefono telefono : telefonos) {
			if(telefonosPorId.get(telefono.IdTelefono) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("Telefono", telefono.Numero);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_TELEFONO_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarExistenciaBorradorTelefono.validarExistenciaTelefonos", fin - inicio);
		return validaciones;
	}
}
