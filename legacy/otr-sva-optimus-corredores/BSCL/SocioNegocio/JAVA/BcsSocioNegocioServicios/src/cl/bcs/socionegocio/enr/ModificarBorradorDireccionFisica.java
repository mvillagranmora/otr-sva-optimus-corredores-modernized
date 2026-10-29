package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.enr.pojo.Ciudad;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.enr.pojo.Cliente;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class ModificarBorradorDireccionFisica implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorDireccionFisica.class);
	
	private String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionFisica_SelectDireccionFisica.sql");
	private String SQL_UPDATE_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionFisica_UpdateDireccionFisica.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionElectronica_UpdateDireccionFisicaNoPredeterminados.sql");
	private String SQL_UPDATE_ENROLAMIENTO_DIRECION_FISICA_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDireccionElectronica_UpdateEnrolamientoDireccionFisicaPrincipal.sql");
	private String SQL_UPDATE_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorPaisResidencia_UpdatePaisResidencia.sql");
	
			
	public static class EntradaModificarBorradorDireccionFisica extends EntradaAdministrarBorrador{

		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaModificarBorradorDireccionFisica(){
			this.p_DireccionesFisicas = new ArrayList<DireccionFisica>();
		}
	}
	
	public static class PersonaIdModificarPaisResidencia extends BcsPojo {
		public long IdEnrolamiento;
		public long IdPaisResidencia;
	}
			
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorDireccionFisica jin = gson.fromJson(in.toString(), EntradaModificarBorradorDireccionFisica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorDireccionFisica entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararDireccionesEntrantes(con, entrada);
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DireccionesFisicas, DireccionFisica.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DireccionesFisicas, "DireccionesFisicas", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Validacion> validaciones = validarExistenciaDireccionesFisicas(con, entrada.p_DireccionesFisicas, entrada.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarDireccionesFisicas(con, entrada.p_DireccionesFisicas);
		predeterminarDireccionesFisicas(con, entrada.p_DireccionesFisicas);
		if(entrada.p_DireccionesFisicas.get(0).Predeterminado.compareTo("S") == 0) {
			actualizarPaisResidencia(con, entrada.p_DireccionesFisicas);
		}
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_DireccionesFisicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorDireccionFisica", finServicio - inicioServicio);
		return out;
	}

	private void prepararDireccionesEntrantes(Connection con, EntradaModificarBorradorDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::ModificarBorradorDireccionFisica.prepararDireccionesEntrantes");
		long inicio = System.currentTimeMillis();

		List<Long> ciudadIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdCiudad != null).map(u -> u.IdCiudad).collect(Collectors.toList());
		Map<Long, Ciudad> ciudadPorIdCiudad = EnrolamientoUtils.obtenerCiudadPorIdCiudad(con, ciudadIds);

		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {
			direccionFisica.EsBorrador = "S";

			Ciudad ciudad = ciudadPorIdCiudad.get(direccionFisica.IdCiudad);
			if (ciudad != null) {
				direccionFisica.IdPais = ciudad.IdPais;
				direccionFisica.UsaDivisionPais = ciudad.UsaDivisionPais;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorDireccionFisica.prepararDireccionesEntrantes {}", fin - inicio);
	}
	
	private List<Validacion> validarExistenciaDireccionesFisicas(Connection con, List<DireccionFisica> direccionesFisicas, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionFisica.validarExistenciaDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> idDireccionesFisicas = direccionesFisicas.parallelStream().map(direccionFisica->{
			return direccionFisica.IdDireccionFisica;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdDireccionFisica", idDireccionesFisicas);
		
		SQL sql = new SQL();
		List<DireccionFisica> direccionesFisicasExistentes = sql.query(con, SQL_SELECT_DIRECCION_FISICA, parametros, DireccionFisica.class);
		Map<Long, DireccionFisica> direccionesFisicasPorIdDireccion = direccionesFisicasExistentes.parallelStream().collect(Collectors.toMap(direccionFisica->direccionFisica.IdDireccionFisica, direccionFisica->direccionFisica));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(DireccionFisica direccionFisica : direccionesFisicas) {
			if(direccionesFisicasPorIdDireccion.get(direccionFisica.IdDireccionFisica) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("Direccion", direccionFisica.Calle);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_DIRECCION_FISICA_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorDireccionFisica.validarExistenciaDireccionesFisicas", fin - inicio);
		return validaciones;
	}
	
	private void actualizarDireccionesFisicas(Connection con, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionFisica.insertarDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DIRECCION_FISICA, direccionesFisicas, DireccionFisica.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorDireccionFisica.insertarDireccionesFisicas", fin - inicio);
	}
	
	private void predeterminarDireccionesFisicas(Connection con, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionFisica.predeterminarDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<DireccionFisica> direccionesFisicasPredeterminadas = direccionesFisicas.parallelStream().filter(direccionFisica->"S".equals(direccionFisica.Predeterminado)).collect(Collectors.toList());

		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADOS, direccionesFisicasPredeterminadas, DireccionFisica.class);
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_DIRECION_FISICA_PRINCIPAL, direccionesFisicasPredeterminadas, DireccionFisica.class);
				
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorDireccionFisica.predeterminarDireccionesFisicas", fin - inicio);
	}
	
	private void actualizarPaisResidencia(Connection con, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.debug("INICIO::ModificarBorradorDireccionFisica.actualizarPaisResidencia");
		long inicio = System.currentTimeMillis();
		
		PersonaIdModificarPaisResidencia entradaUpdatePaisResidencia = new PersonaIdModificarPaisResidencia();
		SQL sql = new SQL();

		//List<PersonaIdModificarPaisResidencia> idPersonasEnroladas = sql.query(con, SQL_SELECT_PERSONA_ENROLAMIENTO, personaEnrolamiento, PersonaIdModificarPaisResidencia.class);
		List<PersonaIdModificarPaisResidencia> idPersonasEnroladas = new ArrayList<PersonaIdModificarPaisResidencia>();
		entradaUpdatePaisResidencia.IdEnrolamiento = direccionesFisicas.get(0).IdEnrolamiento;
		entradaUpdatePaisResidencia.IdPaisResidencia = direccionesFisicas.get(0).IdPais;
		idPersonasEnroladas.add(entradaUpdatePaisResidencia);
		sql.batch(con, SQL_UPDATE_PAIS_RESIDENCIA, idPersonasEnroladas, PersonaIdModificarPaisResidencia.class);		
			
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorDireccionFisica.actualizarPaisResidencia", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<DireccionFisica> direccionesFisicas){
		logger.trace("INICIO::ModificarBorradorDireccionFisica.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = direccionesFisicas.parallelStream().map(direccionFisica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = direccionFisica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = direccionFisica.IdDireccionFisica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorDireccionFisica.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanal\":\"OPT\",\"p_CodigoFormulario\":\"DIRECCION_FISICA_FULL_NATURAL\",\"p_DireccionesFisicas\":[{\"IdInterno\":1,\"IdEnrolamiento\":3999,\"IdPais\":44,\"IdDireccionFisica\":5280,\"Calle\":\"221\",\"Numero\":\"221\",\"Departamento\":\"\",\"Sector\":\"\",\"CodigoPostal\":\"\",\"IdCiudad\":1345,\"IdDivisionPais\":30,\"CodigoProposito\":\"\",\"Comentario\":\"\",\"CodigoCompuesto\":\"\",\"Predeterminado\":\"S\",\"CodigoDivisionPais\":\"04201\"}]}";
			EntradaModificarBorradorDireccionFisica Objin = gson.fromJson(json, EntradaModificarBorradorDireccionFisica.class);
			
			ModificarBorradorDireccionFisica demo = new ModificarBorradorDireccionFisica();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}