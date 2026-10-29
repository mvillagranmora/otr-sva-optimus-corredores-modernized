package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RegistrarUsoVentana implements JavaCall {
	
	private static final Logger logger 						= LogManager.getLogger(RegistrarUsoVentana.class);
	private final String SQL_INSERT_REGISTRAR_USO_VENTANA 	= loadResourceToString("cl/bcs/plataforma/pla/sql/RegistrarUsoVentana_InsertUsoVentana.sql");
	
	public static class EntradaRegistrarUsoVentana extends BcsPojo {
		
		public Long			p_IdEmpresa; 
		public Long			p_IdUsuario;
		public String		p_IdTipoCanalIngreso;
		public String		p_Adicional1;
		public String		p_IdVentana;
		public String		p_Ventana;
			
	};
	
	public static class SalidaRegistrarUsoVentana extends BcsPojo {
		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public SalidaRegistrarUsoVentana() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
		}
	
	};
	
	public static String obtenerModulo(String ruta, String[] modulos) {
		for (String abreviatura : modulos) {
	        if (ruta.equalsIgnoreCase(abreviatura)) {
	            return abreviatura;
	        }
	    }
	    for (String abreviatura : modulos) {
	        if (ruta.toLowerCase().contains(abreviatura.toLowerCase())) {
	            return abreviatura;
	        }
	    }
	    return "false";
    }
	
	public SalidaRegistrarUsoVentana logica(Connection conn , EntradaRegistrarUsoVentana entrada) throws Exception {
		
		logger.info("INICIO::RegistrarUsoVentana.logica");
	    Long inicio = System.currentTimeMillis();
		SalidaRegistrarUsoVentana salida = new SalidaRegistrarUsoVentana();
		
		if(entrada.p_IdVentana.equals("VentanaMensajeResultado") || entrada.p_IdVentana.equals("VentanaResultado") || entrada.p_IdVentana.equals("NO_TITLE") 
			|| entrada.p_IdVentana.equals("Win_VentanaMensajeResultado") || entrada.p_IdVentana.equals("VentanaAlerta")) 
		{
			logger.info("No guardamos la ventana del resultado del mensaje, omitido correctamente");
			return salida;
		}
		
		if(entrada.p_IdTipoCanalIngreso.equals("AGE") || entrada.p_IdTipoCanalIngreso.equals("CLI")) {
			EntradaRegistrarUsoVentana registro = new EntradaRegistrarUsoVentana();
	 		registro.p_IdEmpresa = entrada.p_IdEmpresa;
	 		registro.p_IdUsuario = entrada.p_IdUsuario;
	 		registro.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
	 		String p_adicional1Modificado = entrada.p_Adicional1.substring(0, 1).toUpperCase() + entrada.p_Adicional1.substring(1).toLowerCase();
	 		registro.p_Adicional1 = p_adicional1Modificado;
	 		registro.p_IdVentana = entrada.p_IdVentana;
	 		registro.p_Ventana = entrada.p_Ventana;
			
			List<EntradaRegistrarUsoVentana> registrosUsoVentana = new ArrayList<EntradaRegistrarUsoVentana>(1);
			registrosUsoVentana.add(registro);
			
			SQL sql = new SQL();
			int[] resultadoInsert;
			resultadoInsert = sql.batch(conn, SQL_INSERT_REGISTRAR_USO_VENTANA, registrosUsoVentana, EntradaRegistrarUsoVentana.class);
			
			if(resultadoInsert[0] == 1) {
				logger.info("Registro de uso de ventana insertado exitosamente");
			}else {
				logger.info("La consulta ha fallado");
				return salida;
			}

			Long fin = System.currentTimeMillis();
			logger.info("FIN::RegistrarUsoVentana.logica", fin - inicio);
			return salida;
		}
		
		//los datos que se obtienen desde optimus clientes son: Órdenes, Servicios, Normativo, Mercado, Mi cuenta
		//los datos que se obtienen desde optimus negocios son: Agente, Cliente, Inversiones, Tesorería, Custodia
		
		String[] modulos = {"SocioNegocio", //Socio Negocio
							"CanalAgente", //Socio negocio o plataforma revisar
							"Portafolio", //socio negocio
							"FondoMutuo",
							"CuentaInversion",
							"Custodia",
							"GTA", //Custodia
							"Garantia", //Custodia
							"Facturacion",
							"Tesoreria",
							"SPT", //Spot
							"Spot",
							"Mesa",
							"Corredor", //Informes
							"BECB", //Informes
							"APV",
							"PAC", //APV
							"Plataforma",
							"PLA", //PLATAFORMA
							"INS", //PLATAFORMA
							"ACC", //Plataforma
							"BAT", //Plataforma
							"EVT", //Plataforma
							"DOC", //Plataforma
							"TRA", //Plataforma
							"Normativa",
							"FWD", //FORWARD
							"Forward",
							"Contabilidad", //Interfaz Contable
							"REP", //Reportes
							"Rueda", //Operaciones en rueda
							"Orden", //Operaciones en rueda
							"Homologacion",
							//Modulos usados anteriormente:
							//"Agente", //"Accesos",//"Cliente",//"Inversiones",//"Mi cuenta",//"Mercado",//"Normativo",//"Órdenes",//"Servicios",//"Sebra",//"Tesorería",															 				
							};
		
		String modulo = "";
		String ruta = entrada.p_Adicional1;
		String moduloEncontrado = obtenerModulo(ruta, modulos);
		
		if(moduloEncontrado.equals("false")) {
			logger.info("No se encontro el modulo en el arreglo, omitido correctamente");
			return salida;
		}
		
		if(moduloEncontrado.equals("SocioNegocio") || moduloEncontrado.equals("CanalAgente") || moduloEncontrado.equals("Portafolio")) modulo = "Socio Negocio";
		if(moduloEncontrado.equals("Rueda") || moduloEncontrado.equals("Orden")) modulo = "Operaciones en Rueda";
		if(moduloEncontrado.equals("PLA") || moduloEncontrado.equals("INS") || moduloEncontrado.equals("ACC") || moduloEncontrado.equals("BAT") || moduloEncontrado.equals("EVT") || moduloEncontrado.equals("DOC") || moduloEncontrado.equals("TRA") ) modulo = "Plataforma";
		if(moduloEncontrado.equals("FondoMutuo")) modulo = "Fondos Mutuos";
		if(moduloEncontrado.equals("Facturacion")) modulo = "Facturación";
		if(moduloEncontrado.equals("CuentaInversion")) modulo = "Cuenta Inversión";
		if(moduloEncontrado.equals("Tesoreria")) modulo = "Tesorería";
		if(moduloEncontrado.equals("SPT")) modulo = "Spot";
		if(moduloEncontrado.equals("FWD")) modulo = "Forward";
		if(moduloEncontrado.equals("Contabilidad")) modulo = "Interfaz Contable";
		if(moduloEncontrado.equals("PAC")) modulo = "APV";
		if(moduloEncontrado.equals("REP")) modulo = "Reportes";
		if(moduloEncontrado.equals("Homologacion")) modulo = "Homologación";
		if(moduloEncontrado.equals("GTA") || moduloEncontrado.equals("Garantia")) modulo = "Custodia";
		if(moduloEncontrado.equals("Corredor") || moduloEncontrado.equals("BECB")) modulo = "Informes";
		
		
		if(!moduloEncontrado.equals("SocioNegocio") && !moduloEncontrado.equals("CanalAgente") && !moduloEncontrado.equals("Portafolio") && !moduloEncontrado.equals("Rueda")
			&& !moduloEncontrado.equals("Orden") && !moduloEncontrado.equals("PLA") && !moduloEncontrado.equals("INS") && !moduloEncontrado.equals("ACC") 
			&& !moduloEncontrado.equals("BAT") && !moduloEncontrado.equals("EVT") && !moduloEncontrado.equals("FondoMutuo") && !moduloEncontrado.equals("Facturacion") 
			&& !moduloEncontrado.equals("CuentaInversion") && !moduloEncontrado.equals("Tesoreria") && !moduloEncontrado.equals("SPT") && !moduloEncontrado.equals("FWD") 
			&& !moduloEncontrado.equals("Contabilidad") && !moduloEncontrado.equals("PAC") && !moduloEncontrado.equals("REP") && !moduloEncontrado.equals("GTA")
			&& !moduloEncontrado.equals("Corredor") && !moduloEncontrado.equals("DOC") && !moduloEncontrado.equals("TRA")) 
		{
			modulo = moduloEncontrado;
		}
		
		if(entrada.p_IdVentana.equals("ConsultaCobrosClientes")) modulo = "Facturación";
		if(entrada.p_IdVentana.equals("CuadraturaComprobantesCuentaInversion")) modulo = "Tesorería";
		if(entrada.p_IdVentana.equals("MantenedorAccesosIcliente")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorLimites")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorTipoRelaciones")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorContratosComisiones")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("AdministrarModelosTipoCliente")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("ConsultarLimites")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("AperturaCierreModulos")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorPauta")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorArchivosContratos")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("MantenedorDatosContratos")) modulo = "Plataforma";
		if(entrada.p_IdVentana.equals("InformePosicionClientesFWD")) modulo = "Forward";
		if(entrada.p_IdVentana.equals("ReporteResumenDescalceDiarioCliente")) modulo = "Mesa";
		if(entrada.p_IdVentana.equals("ReporteCarteraFWDBasilea")) modulo = "Forward";
		if(entrada.p_IdVentana.equals("ClienteContratoSAAPV")) modulo = "APV";
		
 		EntradaRegistrarUsoVentana registro = new EntradaRegistrarUsoVentana();
 		registro.p_IdEmpresa = entrada.p_IdEmpresa;
 		registro.p_IdUsuario = entrada.p_IdUsuario;
 		registro.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
 		registro.p_Adicional1 = modulo;
 		registro.p_IdVentana = entrada.p_IdVentana;
 		registro.p_Ventana = entrada.p_Ventana;
		
		List<EntradaRegistrarUsoVentana> registrosUsoVentana = new ArrayList<EntradaRegistrarUsoVentana>(1);
		registrosUsoVentana.add(registro);
		
		SQL sql = new SQL();
		int[] resultadoInsert;
		resultadoInsert = sql.batch(conn, SQL_INSERT_REGISTRAR_USO_VENTANA, registrosUsoVentana, EntradaRegistrarUsoVentana.class);
		
		if(resultadoInsert[0] == 1) {
			logger.info("Registro de uso de ventana insertado exitosamente");
		}else {
			logger.info("La consulta ha fallado");
			return salida;
		}

		Long fin = System.currentTimeMillis();
		logger.info("FIN::RegistrarUsoVentana.logica", fin - inicio);
		return salida;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();	
		EntradaRegistrarUsoVentana jin=gson.fromJson(in.toString() ,EntradaRegistrarUsoVentana.class);
		SalidaRegistrarUsoVentana out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}

	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdTipoCanalIngreso\":\"OPT\",\"p_Adicional1\":\"INS/ConsultarPreciosSistema/ConsultarPreciosSistema.html\",\"p_IdVentana\":\"ConsultarPreciosSistema\",\"p_Ventana\":\"UI_CONSULTAR_PRECIOS_SISTEMA\"}";
				EntradaRegistrarUsoVentana Objin = gson.fromJson(json, EntradaRegistrarUsoVentana.class);
								
				RegistrarUsoVentana  kk = new RegistrarUsoVentana();
				SalidaRegistrarUsoVentana Objout = kk.logica(con, Objin);
				//con.commit();
				con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	

}