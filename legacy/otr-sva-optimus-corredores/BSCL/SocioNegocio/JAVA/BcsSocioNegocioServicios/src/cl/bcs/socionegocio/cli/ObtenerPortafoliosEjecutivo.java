package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ObtenerPortafoliosEjecutivo {
	
	static final Logger logger = LogManager.getLogger(ObtenerPortafoliosEjecutivo.class.getName());
	
	//Obtiene los portafolios que puede ver un ejecutivo
	private final String SQL_SELECT_EMPLEADO_EJECUTIVO_SUPERVISOR = "SELECT \"RestringeCarteraCliente\", CE.\"IdEmpleadoEjecutivo\", 	CS.\"IdEmpleadoSupervisor\", 	CASE WHEN CE.\"IdEmpleadoEjecutivo\" IS NOT NULL THEN 'S' ELSE 'N' END AS \"EsEjecutivo\",  	CASE WHEN CS.\"IdEmpleadoSupervisor\" IS NOT NULL THEN 'S' ELSE 'N' END AS \"EsSupervisor\" FROM \"BSCL.SocioNegocio.BD::EMP.Empleado\" EMP LEFT JOIN \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" CE ON CE.\"IdEmpleadoEjecutivo\" = EMP.\"IdEmpleado\" LEFT JOIN \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor\" CS ON CS.\"IdEmpleadoSupervisor\" = EMP.\"IdEmpleado\" WHERE EMP.\"IdEmpleado\" = :p_IdEmpleado AND EMP.\"IdEmpresa\" = :p_IdEmpresa;";
	private final String SQL_SELECT_EJECUTIVOS_REEMPLAZO = "WITH conf AS (	SELECT		\"IdEmpresa\",		\"Valor\" AS \"FechaSistema\" 	FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" 	WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"CodigoParametro\" = 'PLA.FechaSistema') SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" CROSS JOIN conf c WHERE \"IdEmpleadoEjecutivoReemplaza\" = :p_IdEmpleado AND c.\"FechaSistema\" BETWEEN \"FechaInicioReemplazo\" AND \"FechaFinReemplazo\";";
	private final String SQL_SELECT_PORTAFOLIO_EJECUTIVO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdEmpleadoEjecutivo\" = :p_IdEmpleado AND (\"IdPortafolio\" IN (:p_IdPortafolios) OR 1 = :p_filtrarIdPortafolios);";
	private final String SQL_SELECT_PORTAFOLIO_SUPERVISOR = "SELECT \"IdPortafolio\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" eje INNER JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" port on eje.\"IdEmpleadoEjecutivo\" = port.\"IdEmpleadoEjecutivo\" AND eje.\"IdEmpleadoSupervisor\" = :p_IdEmpleado	WHERE port.\"EsEjecutivoExclusivo\" = 'N' AND (port.\"IdPortafolio\" IN (:p_IdPortafolios) OR 1 = :p_filtrarIdPortafolios);";	
	private final String SQL_SELECT_PORTAFOLIO_REEMPLAZOS = "SELECT \"IdPortafolio\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" eje INNER JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" port on eje.\"IdEmpleadoEjecutivo\" = port.\"IdEmpleadoEjecutivo\" WHERE eje.\"IdEmpleadoEjecutivo\" IN (:ListaReemplazo) AND (port.\"IdPortafolio\" IN (:p_IdPortafolios) OR 1 = :p_filtrarIdPortafolios);";
	private final String SQL_SELECT_PORTAFOLIO_GENERAL_SIN_FILTRO = "SELECT pp.\"IdPortafolio\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" pp LEFT JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" c on pp.\"IdCliente\" = c.\"IdCliente\" WHERE c.\"IdEmpresa\" = :p_IdEmpresa;";
	private final String SQL_SELECT_PORTAFOLIO_GENERAL_CON_FILTRO = "SELECT p.\"IdPortafolio\" FROM \"BSCL.SocioNegocio.BD::EJE.ObtenerPortafolioEjecutivo_Proceso\" p LEFT JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" pp ON p.\"IdPortafolio\" = pp.\"IdPortafolio\" LEFT JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" c on pp.\"IdCliente\" = c.\"IdCliente\" WHERE c.\"IdEmpresa\" = :p_IdEmpresa AND pp.\"IdPortafolio\" IS NOT NULL;";
	private final String SQL_INSERT_PORTAFOLIO_PASO = "INSERT INTO \"BSCL.SocioNegocio.BD::EJE.ObtenerPortafolioEjecutivo_Proceso\"(\"IdTrabajo\",\"IdPortafolio\") VALUES (:IdTrabajo,:IdPortafolio)";
	private final String SQL_DELETE_PORTAFOLIO_PASO = "DELETE FROM \"BSCL.SocioNegocio.BD::EJE.ObtenerPortafolioEjecutivo_Proceso\" WHERE \"IdTrabajo\" = :IdTrabajo";
	
	
	
	
	//EmpleadoEjecutivoSupervisor
	public static class EmpleadoEjecutivoSupervisor{
		public String RestringeCarteraCliente;
		public Long IdEmpleadoEjecutivo;
		public Long IdEmpleadoSupervisor;
		public String EsEjecutivo;
		public String EsSupervisor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//EmpleadoEjecutivo
	public static class EmpleadoEjecutivo{
		public long IdEmpleadoEjecutivo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	//EmpleadoSupervisor
	public static class EmpleadoSupervisor{
		public long IdEmpleadoSupervisor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Ejecutivos reemplaza
	public static class EjecutivosReemplazo{
		public Long IdEmpleadoEjecutivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	//Portafolio
	public static class PortafolioEmpleado{
		public Long IdPortafolio;
		public Long IdTrabajo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//DatosEntrada
	public static class ObtenerPortafoliosEjecutivo_entrada{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		public String	p_filtrarListaPortafolios;
		public List<Long> p_ListaIdPortafolios;
		
		public ObtenerPortafoliosEjecutivo_entrada() {
			this.p_ListaIdPortafolios = new ArrayList<Long>();
			this.p_filtrarListaPortafolios = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//utilitario para saber si el servicio que lo llama debe filtrar por portafolio o no
	public boolean restringeCartera(Connection conn, ObtenerPortafoliosEjecutivo_entrada entrada) throws SQLException {
		logger.info("Buscando informacion si restringe cartera");
		boolean restringeCartera = false;
		HashMap<String,Object> params = null;
		SQL _sql = new SQL();
		params = new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		
		List<EmpleadoEjecutivoSupervisor> emp = _sql.query(conn, SQL_SELECT_EMPLEADO_EJECUTIVO_SUPERVISOR, params, EmpleadoEjecutivoSupervisor.class);
		EmpleadoEjecutivoSupervisor empleadoBackOffice = (emp.isEmpty()) ? null : emp.get(0);
		restringeCartera = (empleadoBackOffice!= null && empleadoBackOffice.RestringeCarteraCliente!=null && empleadoBackOffice.RestringeCarteraCliente.equals("S"));
		boolean esEjecutivo = (empleadoBackOffice!= null && empleadoBackOffice.EsEjecutivo!=null && empleadoBackOffice.EsEjecutivo.equals("S"));
		boolean esSupervisor = (empleadoBackOffice!=null && empleadoBackOffice.EsSupervisor!=null && empleadoBackOffice.EsSupervisor.equals("S"));
		if( (!esEjecutivo && !esSupervisor) || !restringeCartera){
			//no Restringe cartera
			return false;
		}
		else {
			//restrige cartera
			return true;
		}
	}
	
	@Trace
	public List<Long> getPortafoliosEjecutivo(Connection conn, ObtenerPortafoliosEjecutivo_entrada entrada) throws Exception{
		logger.info("<<INICIO GET PORTAFOLIOS EJECUTIVOS>>");
		HashMap<String,Object> params = null;
		SQL _sql = new SQL();
		boolean restringeCartera = false;
		boolean esEjecutivo = false;
		boolean esSupervisor = false;
		List<Long> listaReemplazos = new ArrayList<Long>();
		List<Long> listPortafolios = new ArrayList<Long>();
		HashMap<Long,Boolean> portafoliosAgregados = new HashMap<Long,Boolean>();
		
		int filtrarIdPortafolio = 0;
		//0-Eavaluar si se filtrara la consulta por IdPortafolio
		if(entrada.p_ListaIdPortafolios.isEmpty() && Optional.ofNullable(entrada.p_filtrarListaPortafolios).orElse("S").equalsIgnoreCase("N")){
			filtrarIdPortafolio = 1;
		}
		//1-Obtener si el empleado es ejecutivo o supervisor
		logger.debug("SELECT RESTRINGE CARTERA, EJECUTIVO O SUPERVISOR");
		params = new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		
		List<EmpleadoEjecutivoSupervisor> emp = _sql.query(conn, SQL_SELECT_EMPLEADO_EJECUTIVO_SUPERVISOR, params, EmpleadoEjecutivoSupervisor.class);
		
		EmpleadoEjecutivoSupervisor empleadoBackOffice = (emp.isEmpty()) ? null : emp.get(0);
		restringeCartera = (empleadoBackOffice!= null && empleadoBackOffice.RestringeCarteraCliente!=null && empleadoBackOffice.RestringeCarteraCliente.equals("S"));
		esEjecutivo = (empleadoBackOffice!= null && empleadoBackOffice.EsEjecutivo!=null && empleadoBackOffice.EsEjecutivo.equals("S"));
		esSupervisor = (empleadoBackOffice!=null && empleadoBackOffice.EsSupervisor!=null && empleadoBackOffice.EsSupervisor.equals("S"));

		//2-- SI no es ejecutivo y no es supervisor o No RestringeCartera Obtenemos todos los portafolios del sistema!
		if( (!esEjecutivo && !esSupervisor) || !restringeCartera){
			//Obtenemos TODOS losportafolios existentes
			params = new HashMap<>();		
			
			
			logger.info("OBTENER PORTAFOLIOS EJECUTIVOS");
			List<PortafolioEmpleado> portafolio = null;
			if (filtrarIdPortafolio == 1) {
				params.put("p_IdEmpresa", entrada.p_IdEmpresa);
				portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_GENERAL_SIN_FILTRO, params, PortafolioEmpleado.class);
			}
			else {
				Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
				
				params.put("p_IdEmpresa", entrada.p_IdEmpresa);
				List<PortafolioEmpleado> listIN = entrada.p_ListaIdPortafolios.parallelStream().map(p1 -> {
					PortafolioEmpleado empleado = new PortafolioEmpleado();
					empleado.IdPortafolio = p1;
					empleado.IdTrabajo = idTrabajo;
					return empleado;
				}).collect(Collectors.toList());
				
				_sql.batch(conn, SQL_INSERT_PORTAFOLIO_PASO,listIN, PortafolioEmpleado.class);
				portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_GENERAL_CON_FILTRO, params, PortafolioEmpleado.class);
				SQL.exec(conn, SQL_DELETE_PORTAFOLIO_PASO.replace(":IdTrabajo", idTrabajo+""));
			}
			listPortafolios = portafolio.parallelStream().map(p1 -> p1.IdPortafolio).collect(Collectors.toList());
			
			
			logger.info("<<TERMINO OBTENER PORTAFOLIOS EJECUTIVOS>>");
			return listPortafolios;
		}
		
		//3-Validamos si es Ejecutivo, si es ejecutivo obtenemos portafolios
		logger.debug("SELECT EMPLEADO EJECUTIVO");		
		if(esEjecutivo) {
			//Obtenemos los portafolios del Ejecutivo
			params = new HashMap<>();		
			params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
			params.put("p_IdPortafolios", entrada.p_ListaIdPortafolios);
			params.put("p_filtrarIdPortafolios", filtrarIdPortafolio);
			
			List<PortafolioEmpleado> portafolio = null;
			portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_EJECUTIVO, params, PortafolioEmpleado.class);
			for (PortafolioEmpleado port : portafolio) {
				if(portafoliosAgregados.get(port.IdPortafolio) == null){
					portafoliosAgregados.put(port.IdPortafolio,true);
					listPortafolios.add(port.IdPortafolio);
				}
			}
		}
		logger.debug("TERMINO SELCT EMPLEADO EJECUTIVO");
		//4-Validamos si es Supervisor, si es supervisor obtenemos portafolios
		logger.debug("SELECT SUPERVISOR");		
		if(esSupervisor){
			//Obtenemos los portafolios del Supervisor
			params = new HashMap<>();		
			params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
			params.put("p_IdPortafolios", entrada.p_ListaIdPortafolios);
			params.put("p_filtrarIdPortafolios", filtrarIdPortafolio);
			
			List<PortafolioEmpleado> portafolio = null;
			portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_SUPERVISOR, params, PortafolioEmpleado.class);
			for (PortafolioEmpleado port : portafolio) {
				if(portafoliosAgregados.get(port.IdPortafolio) == null){
					portafoliosAgregados.put(port.IdPortafolio,true);
					listPortafolios.add(port.IdPortafolio);
				}					
			}
		}
		logger.debug("TERMINO SELECT SUPERVISOR");
		//5-Obtenemos los ids de Ejecutivos a quien el Ejecutivo reemplaza
		//luego obtenemos los portafolios de cada uno de los ejecutivos
		logger.debug("SELECT EJECUTIVOS REEMPLAZO");
		params = new HashMap<>();		
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<EjecutivosReemplazo> ejecutivosReemplazo = _sql.query(conn,SQL_SELECT_EJECUTIVOS_REEMPLAZO, params, EjecutivosReemplazo.class);
		if(ejecutivosReemplazo!= null && ejecutivosReemplazo.size() >0) {
			for(EjecutivosReemplazo reemplazos : ejecutivosReemplazo) {
				listaReemplazos.add(reemplazos.IdEmpleadoEjecutivo);
			}
			if(!listaReemplazos.isEmpty()) {
				//Obtenemos los portafolios de los Ejecutivos que reemplaza
				params = new HashMap<>();		
				params.put("ListaReemplazo", listaReemplazos);
				params.put("p_IdPortafolios", entrada.p_ListaIdPortafolios);
				params.put("p_filtrarIdPortafolios", filtrarIdPortafolio);
				
				List<PortafolioEmpleado> portafolio = null;
				portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_REEMPLAZOS, params, PortafolioEmpleado.class);
				for (PortafolioEmpleado port : portafolio) {
					if(portafoliosAgregados.get(port.IdPortafolio) == null){
						portafoliosAgregados.put(port.IdPortafolio,true);
						listPortafolios.add(port.IdPortafolio);
					}
				}
			}
		}
		logger.debug("TERMINO SELECT EJECUTIVOS REEMPLAZO");
		logger.info("<<TERMINO OBTENER PORTAFOLIOS EJECUTIVOS>>");
		return listPortafolios;
	}
	
	public static void main(String[] args) {
		try(Connection conn = PlataformaUtils.getDESAConnection();){
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":377,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":157}";
			String jsonValidarPortafoliosEmpleado = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":377,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":7889, \"p_ListaPortafolios\":[1450068], \"p_ValidarListaPortafolios\":'S'}";
			ObtenerPortafoliosEjecutivo_entrada entrada = gson.fromJson(jsonValidarPortafoliosEmpleado, ObtenerPortafoliosEjecutivo_entrada.class);
			
			ObtenerPortafoliosEjecutivo servicio = new ObtenerPortafoliosEjecutivo();
			
			List<Long> portafolios = servicio.getPortafoliosEjecutivo(conn, entrada);
			System.out.println(portafolios);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
