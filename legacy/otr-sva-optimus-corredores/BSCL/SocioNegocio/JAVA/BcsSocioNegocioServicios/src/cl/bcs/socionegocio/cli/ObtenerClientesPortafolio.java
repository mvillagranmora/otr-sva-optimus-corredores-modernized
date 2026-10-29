package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ObtenerClientesPortafolio {

	//SELECTS para poder aplicar filtros de perfil Agente-Supervisor-SSupervisor
	private final String SQL_SELECT_EJECUTIVO = "SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" where \"IdEmpleadoEjecutivo\" = :p_IdEmpleado and \"IdEmpresa\" = :p_IdEmpresa;";
	private final String SQL_SELECT_SUPERVISOR = "SELECT \"IdEmpleadoSupervisor\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarSupervisor\" where \"IdEmpleadoSupervisor\" = :p_IdEmpleado and \"IdEmpresa\" = :p_IdEmpresa;";
	private final String SQL_SELECT_RESTRINGE_CARTERA = "SELECT \"RestringeCarteraCliente\" FROM  \"BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado\" WHERE \"IdEmpresa\" 	= :p_IdEmpresa  AND \"IdEmpleado\" 	= :p_IdEmpleado;";
	private final String SQL_SELECT_EJECUTIVOS_REEMPLAZO = "SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"IdEmpleadoEjecutivoReemplaza\" = :p_IdEmpleado AND :p_FechaActual BETWEEN \"FechaInicioReemplazo\" AND \"FechaFinReemplazo\";";
	private final String SQL_SELECT_PORTAFOLIO_EJECUTIVO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdEmpleadoEjecutivo\" = :p_IdEmpleado;";
	private final String SQL_SELECT_PORTAFOLIO_SUPERVISOR = "SELECT \"IdPortafolio\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" eje INNER JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" port on eje.\"IdEmpleadoEjecutivo\" = port.\"IdEmpleadoEjecutivo\" AND eje.\"IdEmpleadoSupervisor\" = :p_IdEmpleado	WHERE port.\"EsEjecutivoExclusivo\" = 'N';";
	private final String SQL_SELECT_PORTAFOLIO_REEMPLAZOS = "SELECT \"IdPortafolio\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" eje INNER JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" port on eje.\"IdEmpleadoEjecutivo\" = port.\"IdEmpleadoEjecutivo\" WHERE eje.\"IdEmpleadoEjecutivo\" IN (:ListaReemplazo);";
	private final String SQL_SELECT_PORTAFOLIO_GENERAL = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas.UI::ConsultarPortafolio\" WHERE \"IdEmpresa\" = :p_IdEmpresa;";
	private final String SQL_SELECT_CLIENTES_PORTAFOLIO = "SELECT port.\"IdCliente\" FROM  \"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio\" port INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" cli ON cli.\"IdCliente\" = port.\"IdCliente\" WHERE cli.\"IdEmpresa\" =:p_IdEmpresa AND port.\"IdPortafolio\" IN (:ListaPortafolios);";
	
	//Restringe Cartera
	public static class RestringeCarteraCliente{
		public String RestringeCarteraCliente;
		@Override
		public String toString() {
			return RestringeCarteraCliente;
		}
	}
	
	//EmpleadoEjecutivo
	public static class EmpleadoEjecutivo{
		public long IdEmpleadoEjecutivo;
		@Override
		public String toString() {
			return String.format("EmpleadoEjecutivo [IdEmpleadoEjecutivo=%s]", IdEmpleadoEjecutivo);
		}
	}
		
	//EmpleadoSupervisor
	public static class EmpleadoSupervisor{
		public long IdEmpleadoSupervisor;
		@Override
		public String toString() {
			return String.format("EmpleadoSupervisor [IdEmpleadoSupervisor=%s]", IdEmpleadoSupervisor);
		}
	}
	
	//Ejecutivos reemplaza
	public static class EjecutivosReemplazo{
		public Long IdEmpleadoEjecutivo;
		
		public String toString() {
			return String.format("EjecutivosReemplazo [IdEmpleadoEjecutivo=%s]", IdEmpleadoEjecutivo);
		}
	}
		
	//Portafolio
	public static class PortafolioEmpleado{
		public Long IdPortafolio;
		
		public String toString() {
			return String.format("Portafolio [Portafolio=%s]", IdPortafolio);
		}
	}

	//Clientes
	public static class ClientePortafolio{
		public Long IdCliente;
		
		public String toString() {
			return String.format("ClientePortafolio [ClientePortafolio=%s]", IdCliente);
		}
	}
	
	//DatosEntrada
	public static class ConsultarClientesPortafolio_entrada{
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public List<Long> getClientesPortafolio(Connection conn, ConsultarClientesPortafolio_entrada entrada) throws Exception{
		List<Long> listIdCliente = new ArrayList<Long>();
		HashMap<String,Object> params = null;
		SQL _sql = new SQL();
		boolean restringeCartera = false;
		boolean esEjecutivo = false;
		boolean esSupervisor = false;
		String fechaSistema =  ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Long> listaReemplazos = new ArrayList<Long>();
		List<Long> listPortafolios = new ArrayList<Long>();
		
		params = new HashMap<>();		
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		
		//1-Verificamos si Empleado Restringe Cartera
		List<RestringeCarteraCliente> restringe = null;
		restringe =  _sql.query(conn, SQL_SELECT_RESTRINGE_CARTERA, params, RestringeCarteraCliente.class);
		restringeCartera = (restringe != null && restringe.size() > 0)?restringe.get(0).RestringeCarteraCliente.equalsIgnoreCase("S"):false; 
		
		//2-Validamos si es Ejecutivo, si es ejecutivo obtenemos portafolios
		List<EmpleadoEjecutivo> empleadoEjecutivo = _sql.query(conn, SQL_SELECT_EJECUTIVO, params, EmpleadoEjecutivo.class);
		esEjecutivo = ( empleadoEjecutivo!= null && !empleadoEjecutivo.isEmpty() && empleadoEjecutivo.size()==1);
		if(esEjecutivo) {
			//Obtenemos los portafolios del Ejecutivo
			params = new HashMap<>();		
			params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
			
			List<PortafolioEmpleado> portafolio = null;
			portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_EJECUTIVO, params, PortafolioEmpleado.class);
			for (PortafolioEmpleado port : portafolio) { 
				listPortafolios.add(port.IdPortafolio);
			}
		}
		
		
		//3-Validamos si es Supervisor, si es supervisor obtenemos portafolios
		params = new HashMap<>();		
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		List<EmpleadoSupervisor> empleadoSupervisor = _sql.query(conn, SQL_SELECT_SUPERVISOR, params, EmpleadoSupervisor.class);
		esSupervisor = (empleadoSupervisor!= null && !empleadoSupervisor.isEmpty() && empleadoSupervisor.size()==1);
		if(esSupervisor){
			//Obtenemos los portafolios del Supervisor
			params = new HashMap<>();		
			params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
			List<PortafolioEmpleado> portafolio = null;
			portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_SUPERVISOR, params, PortafolioEmpleado.class);
			for (PortafolioEmpleado port : portafolio) {
				listPortafolios.add(port.IdPortafolio);					
			}
		}
		
		//4-Obtenemos los ids de Ejecutivos a quien el Ejecutivo reemplaza
		//luego obtenemos los portafolios de cada uno de los ejecutivos
		params = new HashMap<>();		
		params.put("p_IdEmpleado", entrada.p_IdEmpleadoConsulta);
		params.put("p_FechaActual", fechaSistema);
		List<EjecutivosReemplazo> ejecutivosReemplazo = _sql.query(conn,SQL_SELECT_EJECUTIVOS_REEMPLAZO, params, EjecutivosReemplazo.class);
		if(ejecutivosReemplazo!= null && ejecutivosReemplazo.size() >0) {
			for(EjecutivosReemplazo reemplazos : ejecutivosReemplazo) {
				listaReemplazos.add(reemplazos.IdEmpleadoEjecutivo);
			}
			if(!listaReemplazos.isEmpty()) {
				//Obtenemos los portafolios de los Ejecutivos que reemplaza
				params = new HashMap<>();		
				params.put("ListaReemplazo", listaReemplazos);
				
				List<PortafolioEmpleado> portafolio = null;
				portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_REEMPLAZOS, params, PortafolioEmpleado.class);
				for (PortafolioEmpleado port : portafolio) {
					listPortafolios.add(port.IdPortafolio);
				}
			}
		}
		
		//5-- SI no es ejecutivo y no es supervisor o No RestringeCartera Obtenemos los portafolios asociados al empleado que consulta
		if((!esEjecutivo && !esSupervisor) || !restringeCartera){
			//Obtenemos los portafolios existentes
			params = new HashMap<>();		
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			
			List<PortafolioEmpleado> portafolio = null;
			portafolio =  _sql.query(conn, SQL_SELECT_PORTAFOLIO_GENERAL, params, PortafolioEmpleado.class);
			for (PortafolioEmpleado port : portafolio) {
				listPortafolios.add(port.IdPortafolio);					
			}
		}
		
		//6 - Obtenemos los clientes asociados a los portafolios consultados segun los filtros anteriores
		params = new HashMap<>();		
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("ListaPortafolios", listPortafolios);
		List<ClientePortafolio> clientesPortafolio = null;
		clientesPortafolio = _sql.query(conn, SQL_SELECT_CLIENTES_PORTAFOLIO, params, ClientePortafolio.class);
		for(ClientePortafolio cliente : clientesPortafolio) {
			if(!listIdCliente.contains(cliente.IdCliente))
				listIdCliente.add(cliente.IdCliente);
		}
		return listIdCliente;
	}
	 
}
