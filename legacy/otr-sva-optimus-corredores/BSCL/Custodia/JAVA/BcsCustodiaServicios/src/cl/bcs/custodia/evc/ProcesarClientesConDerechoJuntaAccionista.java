

package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaEntrada;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaSalida;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoEsAgrupadoPor;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoFiltrosBusqueda;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoUso;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarClientesConDerechoJuntaAccionista implements JavaCall{

	private final Log log = new Log("CUS.ProcesarClientesConDerechoJuntaAccionista");
	
	private final String SQL_SELECT_EVENTO_JUNTA_ACCIONISTA 	 = loadResourceToString("cl/bcs/custodia/sql/ProcesarClientesConDerechoJuntaAccionista_SelectEventoJuntaAccionista.sql");
	private final String SQL_SELECT_INDICADORES_JUNTA_ACCIONISTA = loadResourceToString("cl/bcs/custodia/sql/ProcesarClientesConDerechoJuntaAccionista_SelectIndicadoresJuntaAccionista.sql");
	private final String SQL_INSERT_CLIENTES_CON_DERECHO	     = loadResourceToString("cl/bcs/custodia/sql/ProcesarClientesConDerechoJuntaAccionista_InsertClientesConDerechoJuntaAccionista.sql");		
	private final String SQL_UPDATE_EVENTO_JUNTA_ACCIONISTA     = "UPDATE \"BSCL.Custodia.BD::CUS.EventoJuntaAccionista\" SET \"IdEstadoCustodia\" = 'PRO' WHERE \"IdEventoJuntaAccionista\" = :IdEventoJuntaAccionista";
	
	
	// -----------------------------------------------------------
	// CLASES DE ENTRADA 
	// -----------------------------------------------------------
	public static class ProcesarClientesConDerechoJuntaAccionista_Entrada {
		public Long					 	p_IdEmpresa;
		public Long					 	p_IdUsuario;
		public String				 	p_IdModulo;
		public Long					 	p_IdPais;  
		public List<JuntaAccionistaProceso> p_JuntaAccionistaProcesar; 
				
		public ProcesarClientesConDerechoJuntaAccionista_Entrada (){
			this.p_JuntaAccionistaProcesar 	= new ArrayList<JuntaAccionistaProceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	public static class JuntaAccionistaProceso{
		////public Long IdInterno;  // FALTA .. borrar ya no se usa ....
		public Long IdEventoJuntaAccionista;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	// -----------------------------------------------------------
	// CLASES DE SALIDA 
	// -----------------------------------------------------------
	
	public static class ProcesarClientesConDerechoJuntaAccionista_Salida {
		
		public List<MensajeSalida>	 	P_OUT_MENSAJES;
		public Long					 	P_OUT_ERRORESCONTADOR;		
		public List<JuntaAccionistaProceso> P_OUT_RESULTADO;
		
		public ProcesarClientesConDerechoJuntaAccionista_Salida() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_RESULTADO 		= new ArrayList<JuntaAccionistaProceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	// -----------------------------------------------------------
	// CLASES COMPLEMENTARIAS 
	// -----------------------------------------------------------
	
	public static class EventoJuntaAccionista {
		public Long 		IdEventoJuntaAccionista;
		public Long			IdEmpresa;
		public String 		IdEmisor;
		public String  		FechaRealizacion;
		public String 		IdEstadoJunta;
		public String 		IdEstadoCustodia;
		public String 		Instruccion;
				
		//Para validaciones
		public long 		errores 		= 0;
		public long 		advertencias 	= 0;
		public long 		autorizaciones 	= 0;
		public long 		ok 				= 0;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class ClienteConDerechoJuntaAccionista {
		public Long			IdEventoJuntaAccionista; 
		public Long			IdCliente; 
		public Long			IdInstrumento; 
		public  BigDecimal  CantidadDerecho; 
		public  BigDecimal  CantidadDerechoCalculado; 
		public String       FechaCalculo;
		public String 		Origen; 
		public String 		IndicadorMandatoRepresentacion;
		public String 		Instruccion;
		public String 		IdTipoInstruccion; 
		public String 		FechaIngreso;
		public Long			IdUsuario; 
		public String 		IdEstado;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Indicador
	{
		public Long   		IdCliente;
		public String 		CodigoTipoIndicador;
		public String 		ValorIndicador;
		
		public String toString()
		{
			return Utils.printJson(this, false); 			
		}
	};
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS
	//-------------------------------------------------------------------------------------------	

	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, EventoJuntaAccionista eja, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdEstadoCustodia", eja.IdEstadoCustodia)
					.add("FechaRealizacion", eja.FechaRealizacion)
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(eja.IdEventoJuntaAccionista, MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				eja.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(eja.IdEventoJuntaAccionista, MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				eja.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(eja.IdEventoJuntaAccionista, MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				eja.advertencias++;
			}
			else 
			{
				eja.ok++;
			}
		}
		else 
		{
			eja.ok++;
		}		
	}
	
	
	//Quita del proceso las juntas de accionistas que tuvieron problemas en la validación
	public void limpiarJuntaAccionistas(List<EventoJuntaAccionista> eventoJuntaAccionistaProceso, List<EventoJuntaAccionista> eventoJuntaAccionistaConErrores) throws Exception
	{
		for (EventoJuntaAccionista eja : eventoJuntaAccionistaProceso) 
		{
			if (eja.errores > 0) {
				eventoJuntaAccionistaConErrores.add(eja);
			}
		}
		
		for (EventoJuntaAccionista ejaErr : eventoJuntaAccionistaConErrores)
		{
			//Remueve la forma de pago con error
			if (eventoJuntaAccionistaProceso.contains(ejaErr))
				eventoJuntaAccionistaProceso.remove(ejaErr);
		}
	}
	
	
	// -----------------------------------------------------------
	// LOGICA 
	// -----------------------------------------------------------
	
	public ProcesarClientesConDerechoJuntaAccionista_Salida logica(Connection conn, ProcesarClientesConDerechoJuntaAccionista_Entrada in) throws Exception {
		
		log.write("INICIO::CUS.ProcesarClientesConDerechoJuntaAccionista");
		log.write("PARAMETROS DE ENTRADA > " + in.toString());
		
		ProcesarClientesConDerechoJuntaAccionista_Salida out = new ProcesarClientesConDerechoJuntaAccionista_Salida();
		List<EventoJuntaAccionista> eventoJuntaAccionista_ConErrores = new ArrayList<EventoJuntaAccionista>();
		SQL sql = new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		
		if (in.p_JuntaAccionistaProcesar.size() == 0) 
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_JUNTA_ACCIONISTA",null,conn, in.p_IdEmpresa));				
			return out;
		}
		
		
		// -----------------------------------------------------------------------------------
		// VALIDACIONES BASICAS
		{
			RevisarRequisitosBasicos requisitosBasicos = new RevisarRequisitosBasicos();					
			RevisarRequisitosBasicosSalida salidaRequisitosBasicos = requisitosBasicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			out.P_OUT_MENSAJES.addAll(salidaRequisitosBasicos.p_out_Mensajes);
		}
		

		// -----------------------------------------------------------------------------------
		// OBTIENE MAS DATOS DE JUNTA DE ACCIONISTAS A PROCESAR
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idsEventoJuntaAccionista = new ArrayList<Long>();
		
		for (JuntaAccionistaProceso jap: in.p_JuntaAccionistaProcesar){
			idsEventoJuntaAccionista.add(jap.IdEventoJuntaAccionista);
		}
		
		params.put("IdEventoJuntaAccionista", idsEventoJuntaAccionista);
		params.put("IdEmpresa", in.p_IdEmpresa);
		List<EventoJuntaAccionista> eventoJuntaAccionista_Proceso = sql.query(conn, SQL_SELECT_EVENTO_JUNTA_ACCIONISTA, params, EventoJuntaAccionista.class);
		
		
		if (eventoJuntaAccionista_Proceso.size() == 0)			
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"EVENTO_NO_EXISTE",null,conn,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;	
		}
		
		
		
		// -----------------------------------------------------------------------------------
		// VALIDACIONES DE NEGOCIO
		{	
			for (EventoJuntaAccionista objEveJunAcc : eventoJuntaAccionista_Proceso)	
			{
				ProcesaRequisito(conn, in.p_IdEmpresa, objEveJunAcc.IdEstadoCustodia.equalsIgnoreCase("PRO"), "ESTADO_RESPALDO_CUSTODIA_PROCESADO", MensajeSalida.MENSAJE_ERROR, objEveJunAcc , out.P_OUT_MENSAJES);				
				ProcesaRequisito(conn, in.p_IdEmpresa, objEveJunAcc.IdEstadoJunta.equalsIgnoreCase("ANU") , "JUNTA_ANULADA", MensajeSalida.MENSAJE_ERROR, objEveJunAcc , out.P_OUT_MENSAJES);
				ProcesaRequisito(conn, in.p_IdEmpresa, objEveJunAcc.IdEstadoJunta.equalsIgnoreCase("CER") , "JUNTA_CERRADA", MensajeSalida.MENSAJE_ERROR, objEveJunAcc , out.P_OUT_MENSAJES);				
				ProcesaRequisito(conn, in.p_IdEmpresa, fechaSistema.compareTo(objEveJunAcc.FechaRealizacion) == 1 , "JUNTA_REALIZADA", MensajeSalida.MENSAJE_ERROR, objEveJunAcc , out.P_OUT_MENSAJES);
				
			}
						
			limpiarJuntaAccionistas(eventoJuntaAccionista_Proceso, eventoJuntaAccionista_ConErrores);
			
			if (eventoJuntaAccionista_Proceso.size() == 0) 
			{
				log.write("JUNTA ACCIONISTAS INVALIDADAS > " + eventoJuntaAccionista_ConErrores.toString());
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;	
			}
		}
		
		HashMap<Long, Object> hashEventoJuntaAccionista = Utils.List2Hash(eventoJuntaAccionista_Proceso, "IdEmisor");
		

		// -----------------------------------------------------------------------------------
		// LLAMADA FLUJO CUSTODIA

		//Definicion del uso
		List<FlujoUso> flujoUso = new ArrayList<>();	
		FlujoUso objUso = new FlujoUso();
		
		objUso.UsoTipoOperacion		= "FLCNAC";
		objUso.IdCliente			= null;
		objUso.IdPortafolio			= null;
		objUso.BusquedaEspecifica	= "FLUJO_JUNTA_ACCIONISTA";
		objUso.IdTipoInstrumentoEspecifico = "";
		flujoUso.add(objUso);
		
		
		// Definicion de filtros busqueda
		List<FlujoFiltrosBusqueda> flujoFiltrosBusqueda = new ArrayList<>();
		FlujoFiltrosBusqueda objFiltro;
		
		for (EventoJuntaAccionista objEveJunAcc : eventoJuntaAccionista_Proceso)
		{
			objFiltro = new FlujoFiltrosBusqueda();
			
			objFiltro.IdClasificacionTipoInstrumento = null;
			objFiltro.IdTipoInstrumento = null;
			objFiltro.IdInstrumento = null;
			objFiltro.IdEmisor = objEveJunAcc.IdEmisor;
			objFiltro.FechaVencimiento = null;
			objFiltro.IdTipoReajuste = null;
			objFiltro.EsFungible = null;
			objFiltro.IdBoveda = null;
			flujoFiltrosBusqueda.add(objFiltro);
		}
		
	

		// Definicion de agrupacion
		List<FlujoEsAgrupadoPor> flujoEsAgrupadoPor = new ArrayList<>();
		FlujoEsAgrupadoPor objAgrupa = new FlujoEsAgrupadoPor();
		
		objAgrupa.ClasificacionTipoInstrumento = "S";
		objAgrupa.TipoInstrumento = "S";
		objAgrupa.Instrumento = "S";		
		objAgrupa.Emisor = "S";
		objAgrupa.Nemotecnico = "N";
		objAgrupa.NemoSVS = "N";
		objAgrupa.FechaEmisionInstrumento = "N";
		objAgrupa.FechaVencimientoInstrumento = "N";
		objAgrupa.FechaLiquidacionInstrumento = "N";
		objAgrupa.TipoReajuste = "N";
		objAgrupa.EsFungible = "N";
		objAgrupa.CodigoIFRS = "N";
		objAgrupa.Boveda = "N";
		objAgrupa.Bolsa = "N";
		objAgrupa.TipoOperacion = "N";
		objAgrupa.Mesa = "N";
		objAgrupa.FolioRegistro = "N";
		objAgrupa.ItemOrigen = "N";
		flujoEsAgrupadoPor.add(objAgrupa);		
		
		
		FlujoCustodiaEntrada entradaFlujoCustodia = new FlujoCustodiaEntrada();  
		entradaFlujoCustodia.p_IdEmpresa 	= in.p_IdEmpresa;
		entradaFlujoCustodia.p_IdUsuario 	= in.p_IdUsuario;
		entradaFlujoCustodia.p_IdModulo 	= in.p_IdModulo;
		entradaFlujoCustodia.p_FechaSaldo 	= fechaSistema;
		entradaFlujoCustodia.p_IdPais 		= in.p_IdPais;
		entradaFlujoCustodia.p_DiasProyeccion   = 0;
		entradaFlujoCustodia.p_DiaHabil 		= "S";
		entradaFlujoCustodia.p_ValorizarFlujo 	= "N"; /////"S"; //// jvillagran .. REVISAR si debe valoriza 
		entradaFlujoCustodia.p_Uso.addAll(flujoUso);
		entradaFlujoCustodia.p_FiltrosBusqueda.addAll(flujoFiltrosBusqueda);
		entradaFlujoCustodia.p_EsAgrupadoPor.addAll(flujoEsAgrupadoPor);

		ConsultarFlujoCustodia servicioFlujoCustodia = new ConsultarFlujoCustodia();
		FlujoCustodiaSalida salidaFlujoCustodia = servicioFlujoCustodia.logica(conn, entradaFlujoCustodia);
		

		if (salidaFlujoCustodia.p_out_FlujoTotalizado.size() == 0)
		{	
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_CLIENTES_CON_DERECHO_JUNTA_ACCIONISTA",null,conn,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
		
 		
		// ----------------------------------------------------------------------------------------------
		// AGRUPA FLUJO DE CUSTODIA RESULTANTE (por cliente, instrumento, emisor)
		
		HashMap<String, FlujoCustodia> hashFlujoCustodiaAgrupado = new HashMap<String, FlujoCustodia>();
		
		for (FlujoCustodia resulFlujo : salidaFlujoCustodia.p_out_FlujoTotalizado)
		{
			String KeyFlujo = resulFlujo.IdCliente +"-"+ resulFlujo.IdInstrumento +"-"+ resulFlujo.IdEmisor;
			FlujoCustodia objFluCus = (FlujoCustodia) hashFlujoCustodiaAgrupado.get(KeyFlujo);
			
			if (objFluCus == null)
			{	// si no existe lo agregamos
				hashFlujoCustodiaAgrupado.put(KeyFlujo, resulFlujo);
			}	
			else
			{ // si ya existe lo actualizamos
				objFluCus.Cantidad 	= objFluCus.Cantidad.add(resulFlujo.Cantidad);
			}
		}
		
		
		
		
		// ----------------------------------------------------------------------------------------------
		// OBTIENE INDICADORES JUNTA ACCIONISTA (Indicador Cliente: FACULTA ASISTENCIA JUNTA ACCIONISTA)
		List<Long> idsCliente = new ArrayList<Long>();
		
		for(Map.Entry<String, FlujoCustodia> obj : hashFlujoCustodiaAgrupado.entrySet())
		{
			Long idCliente = obj.getValue().IdCliente;
			if (!idsCliente.contains(idCliente))
			{	//si no lo contiene lo agrega
				idsCliente.add(idCliente);	
			}			
		}
				
		params.clear();
		params.put("IdsCliente", idsCliente);
		params.put("IdEmpresa", in.p_IdEmpresa);
		
		List<Indicador> lstIndicadorFacultaAsistenciaJunAcc = sql.query(conn, SQL_SELECT_INDICADORES_JUNTA_ACCIONISTA, params, Indicador.class);
		HashMap<Long, Object> hashIndicadorFacultaAsistenciaJunAcc = Utils.List2Hash(lstIndicadorFacultaAsistenciaJunAcc, "IdCliente");
		
		
		
		
		// ----------------------------------------------------------------------------------------------
		// GUARDA CLIENTES CON DERECHO

		List<ClienteConDerechoJuntaAccionista> lstClienteConDerecho = new ArrayList<>();
		ClienteConDerechoJuntaAccionista objClienteConDerecho;
		
		for(Map.Entry<String, FlujoCustodia> obj : hashFlujoCustodiaAgrupado.entrySet())
		{
			FlujoCustodia objFluCus = obj.getValue();			
			EventoJuntaAccionista infoEveJunAcc =  (EventoJuntaAccionista) hashEventoJuntaAccionista.get(objFluCus.IdEmisor);
			Indicador infoIndicadorFacultaAsistenciaJunAcc = (Indicador) hashIndicadorFacultaAsistenciaJunAcc.get(objFluCus.IdCliente);
 
			objClienteConDerecho =  new ClienteConDerechoJuntaAccionista();
			objClienteConDerecho.IdEventoJuntaAccionista = infoEveJunAcc.IdEventoJuntaAccionista; 
			objClienteConDerecho.IdCliente = objFluCus.IdCliente;
			objClienteConDerecho.IdInstrumento = objFluCus.IdInstrumento;
			objClienteConDerecho.CantidadDerecho = objFluCus.Cantidad;
			objClienteConDerecho.CantidadDerechoCalculado = objFluCus.Cantidad; 
			objClienteConDerecho.FechaCalculo = fechaSistema;
			objClienteConDerecho.Origen = "SISTEMA";
			objClienteConDerecho.IndicadorMandatoRepresentacion = infoIndicadorFacultaAsistenciaJunAcc == null ? "S" : infoIndicadorFacultaAsistenciaJunAcc.ValorIndicador;
			objClienteConDerecho.IdTipoInstruccion = infoIndicadorFacultaAsistenciaJunAcc == null ? "COR" : (infoIndicadorFacultaAsistenciaJunAcc.ValorIndicador.equalsIgnoreCase("S") ? "COR" : "SIE");
			objClienteConDerecho.Instruccion = infoIndicadorFacultaAsistenciaJunAcc == null ? infoEveJunAcc.Instruccion : (infoIndicadorFacultaAsistenciaJunAcc.ValorIndicador.equalsIgnoreCase("S") ? infoEveJunAcc.Instruccion : "");
			objClienteConDerecho.FechaIngreso = fechaSistema;
			objClienteConDerecho.IdUsuario = in.p_IdUsuario; 
			objClienteConDerecho.IdEstado  = "ACT";
			
			lstClienteConDerecho.add(objClienteConDerecho);
		}
		
		//Inserta en BD clientes con derecho junta accionistas
		if (lstClienteConDerecho.size() > 0)
		{
			sql.batch(conn, SQL_INSERT_CLIENTES_CON_DERECHO, lstClienteConDerecho, ClienteConDerechoJuntaAccionista.class); 
		}
		

		// -----------------------------------------------------------------------------------
		// ACTUALIZAR ESTADO DE ACCIONISTAS PROCESADOS
			
		List<JuntaAccionistaProceso> resultadoJuntaAccionista = new ArrayList<JuntaAccionistaProceso>();
		JuntaAccionistaProceso objResultadoJuntaAccionista;
		
		if (eventoJuntaAccionista_Proceso.size() > 0)
		{				
			
			for(EventoJuntaAccionista obj : eventoJuntaAccionista_Proceso)
			{
				objResultadoJuntaAccionista = new JuntaAccionistaProceso();
				objResultadoJuntaAccionista.IdEventoJuntaAccionista = obj.IdEventoJuntaAccionista; 
				resultadoJuntaAccionista.add(objResultadoJuntaAccionista);
			}
			
			sql.batch(conn, SQL_UPDATE_EVENTO_JUNTA_ACCIONISTA, resultadoJuntaAccionista, JuntaAccionistaProceso.class);
			
		}

		
		// -----------------------------------------------------------------------------------
		// PREPARA SALIDA EL SERVICIO
		
		out.P_OUT_RESULTADO = resultadoJuntaAccionista;		
		log.write("PARAMETROS DE SALIDA > " + out.toString());
		log.write("FIN::CUS.ProcesarClientesConDerechoJuntaAccionista");
		return out;
		
	}
	
	
	
	// -----------------------------------------------------------
	// CALL 
	// -----------------------------------------------------------
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarClientesConDerechoJuntaAccionista_Entrada jin = gson.fromJson(in.toString(), ProcesarClientesConDerechoJuntaAccionista_Entrada.class);
		ProcesarClientesConDerechoJuntaAccionista_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

	
	// -----------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	// -----------------------------------------------------------
	public static void main(String[] args)  throws Exception,SQLException {

		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getQAConnection();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_JuntaAccionistaProcesar\":[{\"IdEventoJuntaAccionista\":8}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_IdPais\":44,\"p_JuntaAccionistaProcesar\":[{\"IdEventoJuntaAccionista\":8},{\"IdEventoJuntaAccionista\":5}]}";
			
			ProcesarClientesConDerechoJuntaAccionista_Entrada objIn = gson.fromJson(json, ProcesarClientesConDerechoJuntaAccionista_Entrada.class);
			ProcesarClientesConDerechoJuntaAccionista demo = new ProcesarClientesConDerechoJuntaAccionista();			
			ProcesarClientesConDerechoJuntaAccionista_Salida ObjOut = demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			//conn.rollback();
			conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}

