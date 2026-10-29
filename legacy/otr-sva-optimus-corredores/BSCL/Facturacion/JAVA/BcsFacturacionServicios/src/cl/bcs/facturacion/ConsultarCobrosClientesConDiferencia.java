
package cl.bcs.facturacion;

/**
 * Objetivo : Consulta los comprobantes que tienen difencia entre los cobros facturados versus los cobros de la platilla de cobros(cobros por portafolio)
 * Autor    : JVILLAGRAN - BCS
 * Fecha    : 08/01/2019
 */
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.MultiKeyMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.ConsultarCobrosPortafolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.ConsultarCobros_Entrada;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.ConsultarCobros_Salida;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.InformacionOperacion;
import cl.bcs.plataforma.com.ConsultarCobrosPortafolio.LineaCobroProceso;


public class ConsultarCobrosClientesConDiferencia implements JavaCall {

	// Log
	private final Log log = new Log("FAC.ConsultarCobrosClientesConDiferencia");

	// Query
	private final String SQL_SELECT_COMPROBANTES        = loadResourceToString("cl/bcs/facturacion/sql/ConsultarCobrosClientesConDiferencia_SelectComprobantes.sql");
	private final String SQL_SELECT_COMPROBANTE_COBROS  = loadResourceToString("cl/bcs/facturacion/sql/ConsultarCobrosClientesConDiferencia_SelectComprobanteCobro.sql");
	
	
	
	
	// ------------------------------------------------------------------------------	
	// CLASES DE ENTRADA / SALIDA	
	
	public static class ConsultarCobrosClientesConDiferencia_Entrada
	{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public Long 		p_IdCliente;
		public Long 		p_IdPortafolio;
		public String   	p_FechaDesde;
		public String   	p_FechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	public static class ConsultarCobrosClientesConDiferencia_Salida
	{	
		public Long 					 P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 		 P_OUT_MENSAJES;
		public List<Comprobante> 		 P_OUT_COMPROBANTES_DIFERENCIAS;  // Comprobantes con diferencias en cobros
		public List<ComprobanteCobro> 	 P_OUT_COBROS_DIFERENCIAS;        // Detalle de cobros de Comprobantes con diferencias
	
		public ConsultarCobrosClientesConDiferencia_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_COMPROBANTES_DIFERENCIAS = new ArrayList<>();
			this.P_OUT_COBROS_DIFERENCIAS = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}				
	}

	
	public static class Comprobante
	{
		public Long 			IdComprobante; 
		public String 			IdTipoComprobante;	
		public String 			IdConcepto; 
		public String 			NombreConcepto;	
		public Long 			IdPortafolio; 
		public String 			NombrePortafolio;	
		public Long 			IdCliente;
		public String 			NombreLargoCliente;
		public String 			DocumentoIdentidad;
		public String 			IdTipoOperacion; 
		public String 			NombreTipoOperacion;
		public String 			IdTipoProducto; 
		public String 			NombreTipoProducto; 
		public String 			IdMoneda;
		public String 			IdMonedaComprobante;
		public java.sql.Timestamp 	FechaComprobante;
		public java.sql.Date 	FechaLiquidacion;
		public BigDecimal 		Monto;
		public BigDecimal 		MontoCobro; 
		public String 			IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}			
	}


	
	
	public static class ComprobanteCobro
	{
		public Long 			 IdComprobante;
		public String 			 NombreCobro;
		public String 			 FormaCobro;
		public String 			 NombreFormaCobro;
		public String 			 IdCobro;
		public String 			 IdMonedaCobro;
		public BigDecimal 		 ValorCobroFacturado;
		public BigDecimal 		 MontoCobroFacturado;
		public BigDecimal 		 ValorCobroPlantilla = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}

	
	// ------------------------------------------------------------------------------	
	// LOGICA


	public ConsultarCobrosClientesConDiferencia_Salida logica(Connection con, ConsultarCobrosClientesConDiferencia_Entrada in) throws Exception {
		

		SQL _sql = new SQL();
		ConsultarCobrosClientesConDiferencia_Salida out = new ConsultarCobrosClientesConDiferencia_Salida();
		
		log.write("== INICIO CONSULTA COBROS CON DIFERENCIA ==");

		log.write("PARAMETROS DE ENTRADA : " + in.toString());
				

		//---------------------------------------------------------------------------------------------
		// CONSULTA COMPROBANTES A ANALIZAR DIFEREENCIAS COBROS 			
		

		HashMap<String,Object> param = new HashMap<>();
		
		param.put("IdEmpresa", in.p_IdEmpresa);			
		param.put("FechaDesde", in.p_FechaDesde);
		param.put("FechaHasta", in.p_FechaHasta);
		param.put("IdCliente", in.p_IdCliente);
		param.put("IdPortafolio", in.p_IdPortafolio);
		
		log.write("CONSULTA COMPROBANTES A ANALIZAR DIFEREENCIAS COBROS");
		
		List<Comprobante> lstComprobantes = new ArrayList<Comprobante>();
		lstComprobantes = _sql.query(con, SQL_SELECT_COMPROBANTES, param, Comprobante.class);
		
		if (lstComprobantes.size() < 1)
		{
			log.write("NO EXITEN COMPROBANTES CON DIFERENDIAS EN COBROS");
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXITEN_COMPROBANTES_DIFERENCIAS_COBROS",null,con,in.p_IdEmpresa));
			return out;							
		}
		
		log.write("COMPROBANTES > " + lstComprobantes);
		
		List<Long> lstIdComprobantes = new ArrayList<Long>();
		for (Comprobante comprobante : lstComprobantes)
		{
			lstIdComprobantes.add(comprobante.IdComprobante);
		}
		
		

		//---------------------------------------------------------------------------------------------
		// CONSULTA COMPROBANTES COBROS FACTURADOS			
			
		log.write("CONSULTA COMPROBANTES COBROS FACTURADOS");
		
		param.clear();
		param.put("IdComprobantes", lstIdComprobantes);
		
		List<ComprobanteCobro> lstComprobanteCobro = new ArrayList<ComprobanteCobro>();
		lstComprobanteCobro = _sql.query(con, SQL_SELECT_COMPROBANTE_COBROS, param, ComprobanteCobro.class);
		
		if (lstComprobanteCobro.size() < 1)
		{
			log.write("NO EXITEN COMPROBANTES COBROS FACTURADOS");
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXITEN_COMPROBANTES_COBROS_FACTURADOS",null,con,in.p_IdEmpresa));
			return out;							
		}
		
		log.write("COMPROBANTES COBROS > " + lstComprobanteCobro);


		
		
		//---------------------------------------------------------------------------------------------
		// CONSULTA COBROS PORTAFOLIO			
		
		List<InformacionOperacion> lstInformacionOperacion = new ArrayList<InformacionOperacion>();
		
		for (Comprobante comprobante : lstComprobantes)
		{
			InformacionOperacion entradaOperacion = new InformacionOperacion();
			
			entradaOperacion.IdInterno            = comprobante.IdComprobante;
			entradaOperacion.IdPortafolio         = comprobante.IdPortafolio;
			entradaOperacion.IdTipoProducto       = comprobante.IdTipoProducto;
			entradaOperacion.IdTipoOperacion      = comprobante.IdTipoOperacion;
			entradaOperacion.IdTipoCanalIngreso   = "AGE";
			entradaOperacion.IdBolsa              = comprobante.IdBolsa;
			
			lstInformacionOperacion.add(entradaOperacion);			
		}
				
		ConsultarCobrosPortafolio servicioConsultarCobrosPortafolio = new ConsultarCobrosPortafolio();		
		ConsultarCobros_Entrada entradaConsultarCobros = new ConsultarCobros_Entrada();
		
		entradaConsultarCobros.p_IdEmpresa = in.p_IdEmpresa;
		entradaConsultarCobros.p_IdUsuario = in.p_IdUsuario;
		entradaConsultarCobros.p_IdModulo = in.p_IdModulo;
		entradaConsultarCobros.p_Operaciones = lstInformacionOperacion;
		
		ConsultarCobros_Salida salidaConsultarCobros = servicioConsultarCobrosPortafolio.logica(con, entradaConsultarCobros);
		log.write("COBROS PORTAFOLIO : " + salidaConsultarCobros);
		

		// Crea un mapa con llaves multiples de los Cobros Portafolios
		MultiKeyMap mapCobrosPortafolio = new MultiKeyMap();
		for (LineaCobroProceso cobroPor : salidaConsultarCobros.p_out_MatrizOperacional)
		{	
			LineaCobroProceso infoCobrosPortafolio = (LineaCobroProceso) mapCobrosPortafolio.get(cobroPor.IdInterno, cobroPor.IdCobro, cobroPor.IdMoneda);
			
			if (infoCobrosPortafolio == null)
			{
				mapCobrosPortafolio.put(cobroPor.IdInterno, cobroPor.IdCobro, cobroPor.IdMoneda, cobroPor); 
			}	
		}
		
		
		//---------------------------------------------------------------------------------------------
		// DETERMINA COMPROBANTES CON DIFERENCIAS EN LOS COBROS
		
		List<Comprobante> lstComprobantesConDiferencias  = new ArrayList<Comprobante>(); 
		
		for (Comprobante objComprobante : lstComprobantes)
		{	
			String comprobanteConDiferencia = "N";
			for (ComprobanteCobro objComprobanteCobro : lstComprobanteCobro)
			{	
				if (objComprobanteCobro.IdComprobante.compareTo(objComprobante.IdComprobante) == 0)
				{
					LineaCobroProceso infoCobrosPortafolio = (LineaCobroProceso) mapCobrosPortafolio.get(objComprobanteCobro.IdComprobante, objComprobanteCobro.IdCobro, objComprobanteCobro.IdMonedaCobro);
					
					if (objComprobanteCobro.ValorCobroFacturado.compareTo(infoCobrosPortafolio.ValorCobro) != 0)
					{
						comprobanteConDiferencia = "S";
					}
					objComprobanteCobro.ValorCobroPlantilla = infoCobrosPortafolio.ValorCobro;
				}	
			}
			
			if (comprobanteConDiferencia.equalsIgnoreCase("S"))
			{
				lstComprobantesConDiferencias.add(objComprobante);
			}
			
		}
		
		if (lstComprobantesConDiferencias.size() < 1)
		{
			log.write("NO EXITEN COMPROBANTES CON DIFERENDIAS EN COBROS");
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXITEN_COMPROBANTES_DIFERENCIAS_COBROS",null,con,in.p_IdEmpresa));
			return out;							
		}
		
		
		//---------------------------------------------------------------------------------------------
		// PREPARAR SALIDAS DEL SERVICIO
		
		out.P_OUT_COMPROBANTES_DIFERENCIAS = lstComprobantesConDiferencias;
		
		
		for (Comprobante objComprobante : lstComprobantesConDiferencias)
		{
			for (ComprobanteCobro objComprobanteCobro : lstComprobanteCobro)
			{
				if (objComprobante.IdComprobante.compareTo(objComprobanteCobro.IdComprobante) == 0)
				{					
					out.P_OUT_COBROS_DIFERENCIAS.add(objComprobanteCobro);
				}
			}
		}
	
		log.write("PARAMETROS DE SALIDA : " + out);
			
		log.write("== TERMINO CONSULTA COBROS CON DIFERENCIA ==");
		return out;

		
	}
	
	

	// ------------------------------------------------------------------------------	
	// CALL
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarCobrosClientesConDiferencia_Entrada in = gson.fromJson(jin.toString(), ConsultarCobrosClientesConDiferencia_Entrada.class);
		ConsultarCobrosClientesConDiferencia_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	
	// ---------------------------------------------------------------------------	
	// METODO PARA PRUEBAS LOCALES

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBAS LOCALES ==");
 
		try (Connection con = PlataformaUtils.getQAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"FAC\",\"p_IdCliente\":null,\"p_IdPortafolio\":null,\"p_FechaDesde\":\"2018-12-12\",\"p_FechaHasta\":\"2018-12-12\"}";
			
			
			ConsultarCobrosClientesConDiferencia_Entrada Objin = gson.fromJson(json, ConsultarCobrosClientesConDiferencia_Entrada.class);			
			ConsultarCobrosClientesConDiferencia demo = new ConsultarCobrosClientesConDiferencia();		
 			System.out.println("ENTRADA > " + Objin.toString());
 			
 			try 
 			{
 				ConsultarCobrosClientesConDiferencia_Salida ObjOut = demo.logica(con, Objin); 				
 				System.out.println("SALIDA > " + ObjOut.toString());
 				//con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				con.rollback();
			}
 			
	    }
		 	
		System.out.println("== FIN PRUEBAS LOCALES ==");
	}
		

	

	
}

	