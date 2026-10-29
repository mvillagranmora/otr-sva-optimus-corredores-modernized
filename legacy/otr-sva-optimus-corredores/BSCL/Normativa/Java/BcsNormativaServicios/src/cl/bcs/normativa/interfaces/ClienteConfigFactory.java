package cl.bcs.normativa.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ClienteConfigFactory {


		public static class ClienteConfigDefault implements ClienteConfig {
			@Override
			public void configurarParametros(EntradaGenerarInterfazP40SBIF entradap40, String idEmpresa, String idUsuario, String rutaBaseArchivos) throws Exception {
				entradap40.p_Fecha     					= "";	// Interfaz P40 genera la fecha actual
				entradap40.p_IdModulo  					= "NOR";
				entradap40.p_IdEmpresa 					= Long.parseLong(idEmpresa);
				entradap40.p_IdUsuario 					= Long.parseLong(idUsuario);
				entradap40.p_Ruta 							= rutaBaseArchivos;
				entradap40.p_Banco						= "223";
				entradap40.p_SBIF						= "104";	// Codigo del tenedor dado por la superintendencia
				entradap40.p_Mercado					= "";		// (RF/IF/ACN/FM/"": todos) -> ACN: Acciones Nacionales
				entradap40.p_TipoCartera				= "1";		// Valor fijo
				entradap40.p_UsaNemoSVS					= "S";
				entradap40.p_ExcluyeGarantia			= "N";
				entradap40.p_ExcluyeSimultanea			= "N";
				}
		}

		public static class ClienteConfigBancoEstado implements ClienteConfig {
			@Override
			public void configurarParametros(EntradaGenerarInterfazP40SBIF entradap40, String idEmpresa, String idUsuario, String rutaBaseArchivos) throws Exception {
				entradap40.p_Fecha     					= ""; 
				entradap40.p_IdModulo  					= "NOR";
				entradap40.p_IdEmpresa 					= Long.parseLong(idEmpresa);
				entradap40.p_IdUsuario 					= Long.parseLong(idUsuario);
				entradap40.p_Ruta 							= rutaBaseArchivos;
				entradap40.p_Banco						= "223";
				entradap40.p_SBIF						= "104"; 
				entradap40.p_Mercado					= ""; 
				entradap40.p_TipoCartera				= "1"; 
				entradap40.p_UsaNemoSVS					= "S";
				entradap40.p_ExcluyeGarantia			= "N";
				entradap40.p_ExcluyeSimultanea			= "N";
				}
		}

		public static class ClienteConfigScotia implements ClienteConfig {
				@Override
				public void configurarParametros(EntradaGenerarInterfazP40SBIF entradap40, String idEmpresa, String idUsuario, String rutaBaseArchivos) throws Exception {
					// Configuración específica para Scotia
					entradap40.p_Fecha 				= "";
					entradap40.p_IdModulo 			= "NOR";
					entradap40.p_IdEmpresa 			= Long.parseLong(idEmpresa);
					entradap40.p_IdUsuario 			= Long.parseLong(idUsuario);
					entradap40.p_Ruta 				= rutaBaseArchivos;
					entradap40.p_Banco 				= "105"; 
					entradap40.p_SBIF 				= "105";
					entradap40.p_Mercado 			= "";
					entradap40.p_TipoCartera 		= "1";
					entradap40.p_UsaNemoSVS 		= "S";
					entradap40.p_ExcluyeGarantia 	= "N";
					entradap40.p_ExcluyeSimultanea 	= "N";
				}
		}

					
		public static ClienteConfig getClienteConfig(String codigoCliente) {
				switch (codigoCliente) {
					case "85":
						return new ClienteConfigScotia();
					case "54":
						return new ClienteConfigBancoEstado();
					default:
						return new ClienteConfigDefault(); // Configuración por defecto
				}
		}
}
	
	

