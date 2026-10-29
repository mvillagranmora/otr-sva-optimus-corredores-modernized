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

import cl.bcs.socionegocio.enr.ValidarExistenciaDireccionElectronica.EntradaValidarExistenciaBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorDireccionElectronica  implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorDireccionFisica.class);
	
	private String SQL_UPDATE_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionElectronica_UpdateDireccionElectronica.sql");
	private String SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionElectronica_UpdateDireccionElectronicaNoPredeterminados.sql");
	private String SQL_UPDATE_ENROLAMIENTO_DIRECION_ELECTRONICA_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionElectronica_UpdateEnrolamientoDireccionElectronicaPrincipal.sql");
	
	public static class EntradaModificarBorradorDireccionElectronica extends EntradaAdministrarBorrador{
		public List<DireccionElectronica> p_DireccionesElectronicas;
		
		public EntradaModificarBorradorDireccionElectronica(){
			this.p_DireccionesElectronicas = new ArrayList<DireccionElectronica>();
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorDireccionElectronica jin = gson.fromJson(in.toString(), EntradaModificarBorradorDireccionElectronica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DireccionesElectronicas, DireccionElectronica.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DireccionesElectronicas, "DireccionesElectronicas", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		ValidarExistenciaDireccionElectronica validarExistenciaDireccionElectronica = new ValidarExistenciaDireccionElectronica();
		EntradaValidarExistenciaBorradorDireccionElectronica entradaValidarExistenciaBorradorDireccionElectronica = new EntradaValidarExistenciaBorradorDireccionElectronica();
		entradaValidarExistenciaBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorDireccionElectronica.p_DireccionesElectronicas = entrada.p_DireccionesElectronicas;
		
		List<Validacion> validacionesExistenciaDireccionesElectronicas = validarExistenciaDireccionElectronica.logica(con, entradaValidarExistenciaBorradorDireccionElectronica).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaDireccionesElectronicas);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarDireccionesElectronicas(con, entrada.p_DireccionesElectronicas);
		predeterminarDireccionesElectronicas(con, entrada.p_DireccionesElectronicas);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_DireccionesElectronicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorDireccionElectronica", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarDireccionesElectronicas(Connection con, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionElectronica.actualizarDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DIRECCION_ELECTRONICA, direccionesElectronicas, DireccionElectronica.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorDireccionElectronica.actualizarDireccionesElectronicas", fin - inicio);
	}
	
	private void predeterminarDireccionesElectronicas(Connection con, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionElectronica.predeterminarDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<DireccionElectronica> direccionesElectronicasPredeterminadas = direccionesElectronicas.parallelStream().filter(direccionElectronica->"S".equals(direccionElectronica.Predeterminado)).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DIRECCION_ELECTRONICA_NO_PREDETERMINADOS, direccionesElectronicasPredeterminadas, DireccionElectronica.class);
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_DIRECION_ELECTRONICA_PRINCIPAL, direccionesElectronicasPredeterminadas, DireccionElectronica.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorDireccionElectronica.predeterminarDireccionesElectronicas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<DireccionElectronica> direccionesElectronicas){
		logger.trace("INICIO::ModificarBorradorDireccionElectronica.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = direccionesElectronicas.parallelStream().map(direccionElectronica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = direccionElectronica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = direccionElectronica.IdDireccionElectronica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorDireccionElectronica.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}

}
