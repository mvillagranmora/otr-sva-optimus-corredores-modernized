package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorTelefono.EntradaValidarExistenciaBorradorTelefono;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorTelefono implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorTelefono.class);
	
	private String SQL_UPDATE_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorTelefono_UpdateTelefono.sql");
	private String SQL_UPDATE_TELEFONO_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorTelefono_UpdateTelefonoNoPredeterminados.sql");
	private String SQL_UPDATE_ENROLAMIENTO_TELEFONO_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorTelefono_UpdateEnrolamientoTelefonoPrincipal.sql");
	
	public static class EntradaModificarBorradorTelefono extends EntradaAdministrarBorrador{
		public List<Telefono> p_Telefonos;
		
		public EntradaModificarBorradorTelefono(){
			this.p_Telefonos = new ArrayList<Telefono>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorTelefono jin = gson.fromJson(in.toString(), EntradaModificarBorradorTelefono.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorTelefono entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_Telefonos, Telefono.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Telefonos, "Telefonos", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		ValidarExistenciaBorradorTelefono validarExistenciaBorradorTelefono = new ValidarExistenciaBorradorTelefono();
		EntradaValidarExistenciaBorradorTelefono entradaValidarExistenciaBorradorTelefono = new EntradaValidarExistenciaBorradorTelefono();
		entradaValidarExistenciaBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorTelefono.p_Telefonos = entrada.p_Telefonos;
		
		List<Validacion> validacionesExistenciaTelefonos = validarExistenciaBorradorTelefono.logica(con, entradaValidarExistenciaBorradorTelefono).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaTelefonos);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarTelefonos(con, entrada.p_Telefonos);
		predeterminarTelefonos(con, entrada.p_Telefonos);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_Telefonos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorTelefono", finServicio - inicioServicio);
		return out;
	}
	
	
	private void predeterminarTelefonos(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarBorradorTelefono.predeterminarTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Telefono> telefonosPredeterminados = telefonos.parallelStream().filter(telefono->"S".equals(telefono.Predeterminado)).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_TELEFONO_NO_PREDETERMINADOS, telefonosPredeterminados, Telefono.class);
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_TELEFONO_PRINCIPAL, telefonosPredeterminados, Telefono.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorTelefono.predeterminarTelefonos", fin - inicio);
	}
	
	private void actualizarTelefonos(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarBorradorTelefono.actualizarTelefonos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_TELEFONO, telefonos, Telefono.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorTelefono.actualizarTelefonos", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<Telefono> telefonos){
		logger.trace("INICIO::ModificarBorradorTelefono.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = telefonos.parallelStream().map(telefono->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = telefono.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = telefono.IdTelefono;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorTelefono.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}	
}
