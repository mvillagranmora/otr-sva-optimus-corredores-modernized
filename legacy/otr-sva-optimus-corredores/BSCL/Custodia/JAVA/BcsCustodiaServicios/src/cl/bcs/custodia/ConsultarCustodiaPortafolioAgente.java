package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarCustodiaPortafolioAgente implements JavaCall  {
	static final Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioAgente.class.getName());
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCustodiaPortafolioAgente in = gson.fromJson(jin.toString(), EntradaConsultarCustodiaPortafolioAgente.class);
		SalidaConsultarCustodiaPortafolioAgente out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class EntradaConsultarCustodiaPortafolioAgente extends ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio{
		public Long 	p_IdEmpleadoConsulta;
		
		public EntradaConsultarCustodiaPortafolioAgente(){
			
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCustodiaPortafolioAgente extends ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio{
		
		public SalidaConsultarCustodiaPortafolioAgente(){

		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private List<ConsultarCustodiaPortafolio.Tabla_Salida> filtroPortafolio(Connection conn, EntradaConsultarCustodiaPortafolioAgente entrada, List<ConsultarCustodiaPortafolio.Tabla_Salida> registros) throws Exception {

		List<Long> listIdPortafolio = new ArrayList<Long>();
		List<ConsultarCustodiaPortafolio.Tabla_Salida> consultaFiltrada =  new ArrayList<>();
		logger.debug("INICIO FILTRA PORTAFOLIO EJECUTIVO");
		try {
			if( entrada.p_IdPortafolio == null ) {
				//Obtenemos los portafolios del ejecutivo
				ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo(); 
				
				ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
				
				entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
				entradaConsulta.p_IdModulo = entrada.p_IdModulo;
				entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
				boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
				if (restringeCartera) {
					listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
					//Se filtra de la data los portafolios de la lista
					for (ConsultarCustodiaPortafolio.Tabla_Salida reg : registros) {
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
	public SalidaConsultarCustodiaPortafolioAgente logica(Connection conn, EntradaConsultarCustodiaPortafolioAgente entrada) throws Exception{
		SalidaConsultarCustodiaPortafolioAgente salida = new SalidaConsultarCustodiaPortafolioAgente();
		logger.info("INICIO CONSULTA CUSTODIA PORTAFOLIO AGENTE");
		
		try {
			ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio salidaServicio = new ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio();
			ConsultarCustodiaPortafolio consulta = new ConsultarCustodiaPortafolio();
			salidaServicio = consulta.logica(conn, entrada);
			salida.P_OUT_ERRORESCONTADOR = salidaServicio.P_OUT_ERRORESCONTADOR;
			if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
				salida.P_OUT_TBL = filtroPortafolio(conn, entrada, salidaServicio.P_OUT_TBL);
			}
			
			
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
				
		logger.info("FIN CONSULTA CUSTODIA PORTAFOLIO AGENTE");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = Utils.getGsonBuilder();			
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"MSP","p_IdEmpleadoConsulta":7889,"p_IdPortafolio":0,"p_IdCliente":0,"p_Producto":"0","p_Mesa":"MEME","p_FechaDesde":"2021-04-13T00:00:00","p_FechaHasta":"2021-04-13T23:59:59","p_IdTipoOperacion":"0"}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_IdEmpleadoConsulta\":7305,\"p_IdPortafolio\":0,\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-04-13T23:59:59\",\"p_IdTipoOperacion\":\"COMPRA\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"MSP\",\"p_IdEmpleadoConsulta\":7889,\"p_IdPortafolio\":0,\"p_IdCliente\":0,\"p_Producto\":\"0\",\"p_Mesa\":\"MEME\",\"p_FechaDesde\":\"2021-02-01T00:00:00\",\"p_FechaHasta\":\"2021-09-30T23:59:59\",\"p_IdTipoOperacion\":\"0\"}";
			// id empleado consulta que no restringe cartera 7889 / id empleado consulta que restringe cartera7301
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"CUS","p_IdCliente":null,"p_IdPortafolio":null,"p_IdClasificacionTipoInstrumento":"","p_IdTipoInstrumento":"","p_IdInstrumento":0,"p_IdBoveda":"","p_AgrupaPorBoveda":"S","p_AgrupaPorRelacion":"S","p_FechaConsulta":"2021-04-13","p_TipoFechaConsulta":"ADQ","p_SaldosNegativos":"S","p_SaldosNegativosPresuntos":"N"}
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_IdCliente\":null,\"p_IdPortafolio\":null,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_SolicitaDividendo\":\"S\",\"p_AgrupaPorRelacion\":\"S\",\"p_SaldosNegativos\":\"S\",\"p_SaldosNegativosPresuntos\":\"N\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2021-04-13\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_IdCliente\":524890,\"p_IdPortafolio\":512339,\"p_IdClasificacionTipoInstrumento\":\"\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_AgrupaPorRelacion\":\"S\",\"p_FechaConsulta\":\"0001-01-01\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"N\"}";
			EntradaConsultarCustodiaPortafolioAgente Objin = gson.fromJson(json, EntradaConsultarCustodiaPortafolioAgente.class);
			ConsultarCustodiaPortafolioAgente demo = new ConsultarCustodiaPortafolioAgente();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				SalidaConsultarCustodiaPortafolioAgente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA:" + ObjOut.P_OUT_TBL.size());
				
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
}
