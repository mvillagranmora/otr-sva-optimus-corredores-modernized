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

public class ConsultarCustodiaMovimientosAgente implements JavaCall{
	static final Logger logger = LogManager.getLogger(ConsultarCustodiaMovimientosAgente.class.getName());
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarCustodiaMovimientosAgente in = gson.fromJson(jin.toString(), EntradaConsultarCustodiaMovimientosAgente.class);
		SalidaConsultarCustodiaMovimientosAgente out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class EntradaConsultarCustodiaMovimientosAgente extends ConsultarCustodiaMovimientos.EntradaConsultarCustodiaMovimientos{
		public Long 	p_IdEmpleadoConsulta;
		
		public EntradaConsultarCustodiaMovimientosAgente(){
			
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarCustodiaMovimientosAgente extends ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos{
		
		public SalidaConsultarCustodiaMovimientosAgente(){

		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private List<ConsultarCustodiaMovimientos.SalidaMovimiento> filtroPortafolio(Connection conn, EntradaConsultarCustodiaMovimientosAgente entrada, List<ConsultarCustodiaMovimientos.SalidaMovimiento> registros) throws Exception {

		List<Long> listIdPortafolio = new ArrayList<Long>();
		List<ConsultarCustodiaMovimientos.SalidaMovimiento> consultaFiltrada =  new ArrayList<>();
		logger.debug("INICIO FILTRA PORTAFOLIO EJECUTIVO");
		try {
			if( entrada.p_VariablesProceso.get(0).IdPortafolio == null ) {
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
					for (ConsultarCustodiaMovimientos.SalidaMovimiento reg : registros) {
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
	public SalidaConsultarCustodiaMovimientosAgente logica(Connection conn, EntradaConsultarCustodiaMovimientosAgente entrada) throws Exception{
		SalidaConsultarCustodiaMovimientosAgente salida = new SalidaConsultarCustodiaMovimientosAgente();
		logger.info("INICIO CONSULTA CUSTODIA MOVIMIENTO AGENTE");
		
		try {
			ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos salidaServicio = new ConsultarCustodiaMovimientos.SalidaConsultarCustodiaMovimientos();
			ConsultarCustodiaMovimientos consulta = new ConsultarCustodiaMovimientos();
			salidaServicio = consulta.logica(conn, entrada);
			salida.P_OUT_ERRORESCONTADOR = salidaServicio.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES = salidaServicio.P_OUT_MENSAJES;
			if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
				salida.P_OUT_TABLA = filtroPortafolio(conn, entrada, salidaServicio.P_OUT_TABLA);
			}
			
			
		}
		catch(Exception e) {
			logger.error(e);
			throw e;
		}
				
		logger.info("FIN CONSULTA CUSTODIA MOVIMIENTO AGENTE");
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
			// id empleado consulta que no restringe cartera 7889 / id empleado consulta que restringe cartera 7301
			//body:{"p_IdEmpresa":1,"p_IdUsuario":21,"p_IdModulo":"CUS","p_UIConsulta":"UI_MOV_CUS","p_VariablesProceso":[{"IdCliente":null,"IdPortafolio":null,"FolioCartera":0,"IdClasificacionTipoInstrumento":"","IdTipoInstrumento":"","IdInstrumento":0}],"p_FechaInicio":"2021-04-13","p_FechaTermino":"2021-04-13"}
			//String json = "{\"p_FechaInicio\":\"2021-04-13\",\"p_FechaTermino\":\"2021-04-13\",\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_VariablesProceso\":[{\"IdCliente\":null,\"IdPortafolio\":null,\"FolioCartera\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0}],\"p_IdProductoAgrupador\":null,\"p_UIConsulta\":\"UI_MOV_CUS\"}";
			String json = "{\"p_FechaInicio\":\"2021-04-13\",\"p_FechaTermino\":\"2021-04-13\",\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_VariablesProceso\":[{\"IdCliente\":524890,\"IdPortafolio\":512339,\"FolioCartera\":0,\"IdClasificacionTipoInstrumento\":\"\",\"IdTipoInstrumento\":\"\",\"IdInstrumento\":0}],\"p_IdProductoAgrupador\":null,\"p_UIConsulta\":\"UI_MOV_CUS\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleadoConsulta\":7889,\"p_IdCliente\":249212,\"p_IdPortafolio\":0,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"\",\"p_IdInstrumento\":0,\"p_IdBoveda\":\"\",\"p_AgrupaPorBoveda\":\"S\",\"p_IdProductoAgrupador\":null,\"p_SolicitaDividendo\":\"S\",\"p_AgrupaPorRelacion\":\"S\",\"p_SaldosNegativos\":\"N\",\"p_SaldosNegativosPresuntos\":\"N\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2020-08-21\"}";
			EntradaConsultarCustodiaMovimientosAgente Objin = gson.fromJson(json, EntradaConsultarCustodiaMovimientosAgente.class);
			ConsultarCustodiaMovimientosAgente demo = new ConsultarCustodiaMovimientosAgente();
			System.out.println("ENTRADA:");
			//System.out.println(Objin.toString());
			try 
			{
				
				SalidaConsultarCustodiaMovimientosAgente ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA:" + ObjOut.P_OUT_TABLA.size());
				
				//System.out.println(ObjOut.toString());
				
			}catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
}
