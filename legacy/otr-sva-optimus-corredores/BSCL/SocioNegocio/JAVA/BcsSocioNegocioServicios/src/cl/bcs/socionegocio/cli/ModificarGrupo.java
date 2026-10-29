package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarGrupo implements JavaCall  {
	
	
	private final Logger logger = LogManager.getLogger(ModificarGrupo.class);

	private final String SQL_INSERT_GRUPO			= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarGrupo_Insert.sql");
	private final String SQL_UPDATE_GRUPO   		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarGrupo_Update.sql");
	private final String SQL_DELETE_GRUPO_CLIENTES	= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarGrupoClientes_Delete.sql");	
	private final String SQL_INSERT_GRUPO_CLIENTES	= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarGrupoClientes_Insert.sql");
	private final String SQL_SELECT_EJECUTIVOS		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarGrupoClientes_SelectEjecutivos.sql");
	private static final String TRANSACCION_INSERT	= "C";
	private static final String TRANSACCION_UPDATE 	= "U";
	
    public static class EntradaParametrosGrupo{
     	public Long p_IdGrupo;
		public Long p_IdEmpresa;
    	public String p_IdEstado;
    	public String p_Nombre;
    	public String p_Codigo;
    	public String p_IdTransaccion;
    	public List<ModificarGrupoClientesData> p_tablaClientesPortafolio;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
    
    public static class ModificarGrupoClientesData {
    	public String p_IdEmpresa;
    	public Long p_IdGrupo;
    	public Long p_IdPortafolio;
    	
		public ModificarGrupoClientesData() {
		}
    	
		public ModificarGrupoClientesData(String p_IdEmpresa, Long p_IdGrupo, Long p_IdPortafolio) {
			this.p_IdEmpresa = p_IdEmpresa;
			this.p_IdGrupo = p_IdGrupo;
			this.p_IdPortafolio = p_IdPortafolio;
		}
    }
    
    public static class EmpleadoEjecutivo{
    	public Long IdEmpleadoEjecutivo;
    }
    
    public static class SalidaProcesamiento{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
	
		public SalidaProcesamiento(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaParametrosGrupo in = gson.fromJson(jin.toString() ,EntradaParametrosGrupo.class);
		
		SalidaProcesamiento out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private SalidaProcesamiento logica(Connection con, EntradaParametrosGrupo entrada) throws Exception {
		logger.info("INICIO::ModificarGrupo");
		SalidaProcesamiento salida = new  SalidaProcesamiento();

		try {		
			List<EntradaParametrosGrupo> listCrearGrupo = new ArrayList<EntradaParametrosGrupo>();					
			listCrearGrupo.add(entrada);
			
			salida = validarEntrada(con, entrada);
			
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				return salida;
			}
			
			if (entrada.p_IdTransaccion.equals(TRANSACCION_INSERT)) {
				salida = crearGrupo(con, entrada);
				
			}
			
			if (entrada.p_IdTransaccion.equals(TRANSACCION_UPDATE)) {
				salida = modificarGrupo(con, entrada);
			}
		}		 		
		catch (Exception e) {
			logger.error("Error::ModificarGrupo::",e);
			throw e;

		}
		
		logger.info("FIN::ModificarGrupo");
		return salida;
				
	}
	
	private SalidaProcesamiento validarEntrada(Connection con, EntradaParametrosGrupo entrada) throws Exception {
		logger.debug("INICIO::ModificarGrupo.validarEntrada");
		SalidaProcesamiento salida = new  SalidaProcesamiento();
		if (esDatoInValido(entrada.p_IdEmpresa)) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR","Ingrese el Id de la empresa"));
			
		}
		if (esDatoInValido(entrada.p_IdEstado)) {
			String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_ESTADO_GRUPO_VACIO").msg();					
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_ESTADO_GRUPO_VACIO", msg));
			
		}
		if (esDatoInValido(entrada.p_Nombre)) {
			String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_NOMBRE_GRUPO_VACIO").msg();						
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_NOMBRE_GRUPO_VACIO", msg));
			
		}
		if (esDatoInValido(entrada.p_Codigo)) {
			String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"CODIGO_EN_BLANCO").msg();							
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "CODIGO_EN_BLANCO", msg));							
			
		}
		
		if(existenPortafoliosRepetidos(entrada.p_tablaClientesPortafolio)) {
			String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_PORTAFOLIO_EN_GRUPO").msg();							
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "UI_PORTAFOLIO_EN_GRUPO", msg));
		}
		
		if(existenPortafoliosConDistintoEjecutivo(con, entrada.p_tablaClientesPortafolio)) {
			String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_PORTAFOLIO_CON_DISTINTO_EJECUTIVO").msg();							
			salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR, "UI_PORTAFOLIO_CON_DISTINTO_EJECUTIVO", msg));
		}
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		logger.debug("FIN::ModificarGrupo.validarEntrada");
		return salida;
	}
	
	private Boolean esDatoInValido(String dato) {
		logger.trace("INICIO::ModificarGrupo.esDatoInValido");
		if(dato == null || dato.isEmpty()) {
			return true;
		}
		logger.trace("FIN::ModificarGrupo.esDatoInValido");
		return false;
	}
	
	private Boolean esDatoInValido(Long dato) {
		logger.trace("INICIO::ModificarGrupo.esDatoInValido");
		if(dato == null || dato.equals(0L)) {
			return true;
		}
		logger.trace("FIN::ModificarGrupo.esDatoInValido");
		return false;
	}
	
	private Boolean existenPortafoliosRepetidos(List<ModificarGrupoClientesData> portafolios) throws Exception {
		logger.debug("INICIO::ModificarGrupo.existenPortafoliosRepetidos");
		HashMap<Long, Long> portafoliosExistentes = new HashMap<Long, Long>(portafolios.size() + 1);
		
		for(ModificarGrupoClientesData portafolio : portafolios) {
			if(portafoliosExistentes.get(portafolio.p_IdPortafolio) != null) {
				return true;
			}
			portafoliosExistentes.put(portafolio.p_IdPortafolio, portafolio.p_IdPortafolio);
		}
		
		logger.debug("FIN::ModificarGrupo.existenPortafoliosRepetidos");
		return false;
	}
	
	private Boolean existenPortafoliosConDistintoEjecutivo(Connection con, List<ModificarGrupoClientesData> portafolios) throws Exception {
		logger.debug("INICIO::ModificarGrupo.existenPortafoliosConDistintoEjecutivo");
		
		List<Long> idPortafolios = portafolios
				.parallelStream()
				.map(portafolio->portafolio.p_IdPortafolio)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdPortafolio", idPortafolios);
		SQL sql = new SQL();
		Long empleadosEjecutivosDiferentes = sql.countQuery(con, SQL_SELECT_EJECUTIVOS, parametros);
		
		if(empleadosEjecutivosDiferentes > 1) {
			return true;
		}
		
		logger.debug("INICIO::ModificarGrupo.existenPortafoliosConDistintoEjecutivo");
		return false;
	}
	
	private SalidaProcesamiento crearGrupo(Connection con, EntradaParametrosGrupo entrada) throws Exception {
		logger.debug("INICIO::ModificarGrupo.crearGrupo");
		SalidaProcesamiento salida = new  SalidaProcesamiento();
		try {
			SQL sql = new SQL();
			long id = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::GrupoClientes", (long) 1).get(0);	// Get the next id from sequence
			entrada.p_IdGrupo = id;
			
			List<EntradaParametrosGrupo> listCrearGrupo = new ArrayList<EntradaParametrosGrupo>();					
			listCrearGrupo.add(entrada);
			
			sql.batch(con, SQL_INSERT_GRUPO, listCrearGrupo, EntradaParametrosGrupo.class);	// Insert the register in the table BSCL.SocioNegocio.BD::CLI.Grupo
			
			entrada.p_tablaClientesPortafolio.parallelStream().forEach(portafolio -> {
				portafolio.p_IdGrupo = id;
			});
			
			sql.batch(con, SQL_INSERT_GRUPO_CLIENTES, entrada.p_tablaClientesPortafolio, ModificarGrupoClientesData.class);	// Insert the registers in the table BSCL.SocioNegocio.BD::CLI.GrupoClientes
			
		} catch (SQLException e) {
			logger.error("ERROR:::ModificarGrupo.crearGrupo", e);
			if (e.getErrorCode() == 301) {
				String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_GRUPO_DE_CLIENTES_DUPLICADO").msg();									
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_GRUPO_DE_CLIENTES_DUPLICADO", msg));
			} 
			else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", e.getErrorCode() + " - " + e.getMessage()));
			}
		}
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		logger.debug("FIN::ModificarGrupo.crearGrupo");
		return salida;
	}
	
	private SalidaProcesamiento modificarGrupo(Connection con, EntradaParametrosGrupo entrada) throws Exception {
		logger.debug("INICIO::ModificarGrupo.modificarGrupo");
		SalidaProcesamiento salida = new  SalidaProcesamiento();
		try {
			
			SQL sql = new SQL();
			List<EntradaParametrosGrupo> listModificarGrupo = new ArrayList<EntradaParametrosGrupo>();						
			listModificarGrupo.add(entrada);
			sql.batch(con, SQL_UPDATE_GRUPO, listModificarGrupo, EntradaParametrosGrupo.class);	// Update the register in the table BSCL.SocioNegocio.BD::CLI.Grupo	
			sql.batch(con, SQL_DELETE_GRUPO_CLIENTES, listModificarGrupo, EntradaParametrosGrupo.class); // Delete all the registers where the IdGrupo = :IdGrupo in the table BSCL.SocioNegocio.BD::CLI.GrupoClientes
			sql.batch(con, SQL_INSERT_GRUPO_CLIENTES, entrada.p_tablaClientesPortafolio, ModificarGrupoClientesData.class); // Insert the registers in the table BSCL.SocioNegocio.BD::CLI.GrupoClientes
			
		} catch (SQLException e) {
			logger.error("ERROR:::ModificarGrupo.modificarGrupo", e);
			
			if (e.getErrorCode() == 301) {
				String msg= Diccionario.getMensaje(con, entrada.p_IdEmpresa,"UI_GRUPO_DE_CLIENTES_DUPLICADO").msg();								
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_GRUPO_DE_CLIENTES_DUPLICADO", msg));
			} else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", e.getErrorCode() + " - " + e.getMessage()));
			}
		}
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		logger.debug("FIN::ModificarGrupo.modificarGrupo");
		return salida;
	}
	
	
	
	/******************************************************************************
	 * 							MAIN - Only for testing use
	 ******************************************************************************/
	public static void main(String[] args){
		
		try (Connection conn = PlataformaUtils.getDESAConnection())
		{
			System.out.println("== INICIO PRUEBA LOCAL ==");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String jsonUniverso = "{\"p_IdGrupo\":6,\"p_IdEmpresa\":1,\"p_IdEstado\":ACT,\"p_Nombre\":\"JJGRUPO CLIENTES PRUEBA\",\"p_Codigo\":\"GC11\",\"p_IdTransaccion\":\"C\"}";
			String jsonUniverso = "{\"p_IdGrupo\":\"57\",\"p_IdEmpresa\":1,\"p_IdEstado\":\"ACT\",\"p_Nombre\":\"GRUPO 32\",\"p_Codigo\":\"SADASDASDASDASDA12\",\"p_IdTransaccion\":\"U\",\"p_tablaClientesPortafolio\":[{\"p_IdEmpresa\":1,\"p_IdGrupo\":\"57\",\"p_IdPortafolio\":512342}]}";
			EntradaParametrosGrupo entrada = gson.fromJson(jsonUniverso, EntradaParametrosGrupo.class);
			ModificarGrupo demo = new ModificarGrupo();				
			SalidaProcesamiento salida = demo.logica(conn, entrada);
			
			System.out.println("Salida --> " + salida.toString());
			conn.commit();
			//con.rollback();
			
		}catch(Exception e){ 
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
