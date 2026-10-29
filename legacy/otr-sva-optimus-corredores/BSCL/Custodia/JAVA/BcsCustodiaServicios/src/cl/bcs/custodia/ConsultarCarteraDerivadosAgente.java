package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarCarteraDerivadosAgente implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarCarteraDerivadosAgente.class.getName());
		
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCarteraDerivadosAgente in = gson.fromJson(jin.toString(), EntradaConsultarCarteraDerivadosAgente.class);
		SalidaConsultarCarteraDerivadosAgente out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class EntradaConsultarCarteraDerivadosAgente extends ConsultarCarteraDerivados.Entrada_ConsultarCustodiaDerivados{
		public Long 	p_IdEmpleadoConsulta;
		
		public EntradaConsultarCarteraDerivadosAgente(){
			
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCarteraDerivadosAgente extends ConsultarCarteraDerivados.Salida_ConsultarCustodiaDerivados{
		
		public SalidaConsultarCarteraDerivadosAgente(){
			
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private List<ConsultarCarteraDerivados.CustodiaDerivados> filtroPortafolio(Connection conn, EntradaConsultarCarteraDerivadosAgente entrada, List<ConsultarCarteraDerivados.CustodiaDerivados> registros) throws Exception {

		List<Long> listIdPortafolio = new ArrayList<Long>();
		List<ConsultarCarteraDerivados.CustodiaDerivados> consultaFiltrada =  new ArrayList<>();
		
		logger.debug("INICIO FILTRA PORTAFOLIO EJECUTIVO");
		try {
			if( entrada.p_IdPortafolio == null ) {
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa.longValue();
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario.longValue();
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					//Se filtra de la data los portafolios de la lista
					for (ConsultarCarteraDerivados.CustodiaDerivados reg : registros) {
						if(listIdPortafolio.contains(reg.IdPortafolio)) {
							consultaFiltrada.add(reg);
						}
					}
					
				}
				else {
					consultaFiltrada = registros;
				}
			}
			else {
				//el filtro de un portafolio especifico ya esta implementado en el servicio principal, por eso no verifico eso nuevamente aca
				consultaFiltrada = registros;
			}

			logger.debug("FIN FILTRA PORTAFOLIO EJECUTIVO");
			return consultaFiltrada;
		}
		catch (Exception e) {
			logger.debug("ERROR AL FILTRAR PORTAFOLIO EJECUTIVO - " + e.getMessage());
			throw e;
		}
		
	}
	
	private List<ConsultarCarteraDerivados.CustodiaDerivadosFuturo> filtroFuturoPortafolio(Connection conn, EntradaConsultarCarteraDerivadosAgente entrada, List<ConsultarCarteraDerivados.CustodiaDerivadosFuturo> registros) throws Exception {

		List<Long> listIdPortafolio = new ArrayList<Long>();
		List<ConsultarCarteraDerivados.CustodiaDerivadosFuturo> consultaFiltrada =  new ArrayList<>();
		
		logger.debug("INICIO FILTRA PORTAFOLIO EJECUTIVO");
		try {
			if( entrada.p_IdPortafolio == null ) {
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa.longValue();
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario.longValue();
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					//Se filtra de la data los portafolios de la lista
					for (ConsultarCarteraDerivados.CustodiaDerivadosFuturo reg : registros) {
						if(listIdPortafolio.contains(reg.IdPortafolio)) {
							consultaFiltrada.add(reg);
						}
					}
					
				}
				else {
					consultaFiltrada = registros;
				}
			}
			else {
				//el filtro de un portafolio especifico ya esta implementado en el servicio principal, por eso no verifico eso nuevamente aca
				consultaFiltrada = registros;
			}

			logger.debug("FIN FILTRA PORTAFOLIO EJECUTIVO");
			return consultaFiltrada;
		}
		catch (Exception e) {
			logger.debug("ERROR AL FILTRAR PORTAFOLIO EJECUTIVO - " + e.getMessage());
			throw e;
		}
		
	}
	
	
	@SuppressWarnings("deprecation")
	public SalidaConsultarCarteraDerivadosAgente logica(Connection conn, EntradaConsultarCarteraDerivadosAgente entrada) throws Exception{
		SalidaConsultarCarteraDerivadosAgente salida = new SalidaConsultarCarteraDerivadosAgente();
		logger.info("INICIO CONSULTA CUSTODIA MOVIMIENTO AGENTE");
		
		ConsultarCarteraDerivados.Salida_ConsultarCustodiaDerivados salidaServicio = new ConsultarCarteraDerivados.Salida_ConsultarCustodiaDerivados();
		ConsultarCarteraDerivados consulta = new ConsultarCarteraDerivados();
		salidaServicio = consulta.logica(conn, entrada);
		
		if(entrada.p_TipoDerivado.equals("OP")) {	
			salida.P_OUT_CARTERA = filtroPortafolio(conn, entrada, salidaServicio.P_OUT_CARTERA);
		}		
		else if(entrada.p_TipoDerivado.equals("FU")) {
			salida.P_OUT_CARTERA_FUTURO = filtroFuturoPortafolio(conn, entrada, salidaServicio.P_OUT_CARTERA_FUTURO);
		}
				
		logger.info("FIN CONSULTA CUSTODIA MOVIMIENTO AGENTE");
		return salida;
	}
	
	public static void main(String[] args) { 
		System.out.println("=== BEGIN TEST ===");
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();				
			
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 21,\r\n" + 
					"  \"p_IdModulo\": \"CUS\",\r\n" + 					
					"  \"p_FechaConsulta\": \"2020-04-01\",\r\n" + 
					"  \"p_IdClasificacionTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdTipoInstrumento\": \"\",\r\n" + 
					"  \"p_IdInstrumento\": 0,\r\n" + 					
					"  \"p_IdCliente\": 123569,\r\n" +
					"  \"p_TipoDerivado\": \"FU\",\r\n" +
					"  \"p_IdEmpleadoConsulta\": 7889\r\n" +
					//"  \"p_IdPortafolio\": 27499\r\n" +  7889 OP
					"}";
			
			
			EntradaConsultarCarteraDerivadosAgente entrada = gson.fromJson(json, EntradaConsultarCarteraDerivadosAgente.class);
			ConsultarCarteraDerivadosAgente demo = new ConsultarCarteraDerivadosAgente();
			long init = System.currentTimeMillis();
			SalidaConsultarCarteraDerivadosAgente salida = demo.logica(con, entrada);	
			System.out.println("cartera size: " + salida.P_OUT_CARTERA.size());	
			System.out.println("cartera futuro size: " + salida.P_OUT_CARTERA_FUTURO.size());	
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));			
			System.out.println(salida);			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("=== END TEST ===");
	}
	
}
