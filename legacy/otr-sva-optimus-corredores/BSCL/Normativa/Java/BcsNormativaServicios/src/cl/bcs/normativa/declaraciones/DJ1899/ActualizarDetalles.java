package cl.bcs.normativa.declaraciones.DJ1899;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarDetalles implements JavaCall {
	
	public static class DetalleInicial {
		public Long IdDocumento;
		public Long ID;
		public Long Numero;
		public String FuenteDatos;
		public java.sql.Date FechaOperacion;
		public String IdMoneda;
		public BigDecimal DepositoEmpleador;
		public BigDecimal DepositoEmpleadorMonedaDJ;
		public BigDecimal DepositoEmpleadorSegundaMonedaDJ;//clp
		//Aporte Empleador
		public BigDecimal AporteEmpleador;//--uf
		public BigDecimal AporteEmpleadorMonedaDJ;//--uf
		public BigDecimal TraspasoEmpleador;//--uf
		public BigDecimal TraspasoEmpleadorMonedaDJ;//--uf
		public BigDecimal RetiroEmpleadorActualizado;//--($)
		public BigDecimal RetiroEmpleadorActualizadoMonedaDJ;//--($)
		public BigDecimal MontoRetencionActualizado;//15%($)
		public BigDecimal MontoRetencionActualizadoMonedaDJ;//15%($)
	    //Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
	    public BigDecimal AhorrosModalidadIndirectaEmpleador;
	    public BigDecimal AhorrosModalidadIndirectaEmpleadorMonedaDJ;
	    public BigDecimal AhorrosModalidadIndirectaEmpleadorSegundaMonedaDJ; //clp
	    public BigDecimal AhorrosModalidadDirectaTrabajador;
	    public BigDecimal AhorrosModalidadDirectaTrabajadorMonedaDJ;
	    public BigDecimal AhorrosModalidadDirectaTrabajadorSegundaMonedaDJ; //clp
	    //DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente del ART. 42 Nª1 de la LIR en UF
	    public BigDecimal CotizacionesModalidadIndirectaEmpleador;//--uf
	    public BigDecimal CotizacionesModalidadIndirectaEmpleadorMonedaDJ;//--uf
	    public BigDecimal CotizacionesModalidadDirectaTrabajador;//--uf
	    public BigDecimal CotizacionesModalidadDirectaTrabajadorMonedaDJ;//--uf
	    //DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente del Art.42 Nª2 de la LIR en UF
	    public BigDecimal CotizacionesTrabajadorIndependiente;//uf
	    public BigDecimal CotizacionesTrabajadorIndependienteMonedaDJ;//uf
	    public BigDecimal CotizacionesTrabajadorIndependienteSegundaMonedaDJ;//clp
	    //Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
	    //del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a los aportes del empleador, actualizados en $
	    public BigDecimal MontoRetiroPorAhorrosIncisoPrimero;
	    public BigDecimal MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ;
	    //Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
	    public BigDecimal RetencionImpuestoSobreRetiros;//15%($)
	    public BigDecimal RetencionImpuestoSobreRetirosMonedaDJ;//15%($)
	    //Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
	    public BigDecimal MontoRetiroPorAhorrosIncisoSegundo;
	    public BigDecimal MontoRetiroPorAhorrosIncisoSegundoMonedaDJ;
	    
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ObjDetalleParidad {
		public Long IdInterno;
		public Long ID;
		public Long Numero;
		public String TipoParidad;
		public String Fecha;
		public String IdMonedaOrigen;
		public BigDecimal MontoOrigen;
		public String IdMonedaDestino;
	    public BigDecimal MontoDestino;
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ActualizarDetallesEntrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public Long p_Version;
	}
		
	public static class ActualizarDetallesSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();	
	private static String SQL_SELECT_OBTENER_DETALLE_INICIAL = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ObtenerDetalleInicial.sql");
	private static String SQL_UPDATE_DETALLE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1899/sql/ActualizarDetalle.sql");
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ActualizarDetallesEntrada in=gson.fromJson(jin.toString() ,ActualizarDetallesEntrada.class);
		ActualizarDetallesSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ActualizarDetallesSalida logica(Connection con, ActualizarDetallesEntrada entrada) throws Exception {
		ActualizarDetallesSalida out = new ActualizarDetallesSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();
	        HashMap<String,Object> hs = new HashMap<String,Object>();
	        hs.put("Version", entrada.p_Version);
	        List<DetalleInicial> lstDetalleInicial = new ArrayList<DetalleInicial>();
			lstDetalleInicial = sql.query(con, SQL_SELECT_OBTENER_DETALLE_INICIAL, hs, DetalleInicial.class);
			List<ObjDetalleParidad> detalleParidad = new ArrayList<ObjDetalleParidad>();
			CalcularParidad.CalcularParidadEntrada CalcParidad = new CalcularParidad.CalcularParidadEntrada();
			long idInterno = 1;
			//long idDocumento = lstDetalleInicial.get(0).IdDocumento;
			
			for (DetalleInicial detalleInicial : lstDetalleInicial) {
				if (detalleInicial.FuenteDatos.equals("INICIAL")){
					ObjDetalleParidad depositoEmpleador = new ObjDetalleParidad();
					depositoEmpleador.IdInterno				= idInterno;
					depositoEmpleador.ID					= detalleInicial.ID;
					depositoEmpleador.Numero				= detalleInicial.Numero;
					depositoEmpleador.TipoParidad			= "DepositoEmpleador";
					depositoEmpleador.Fecha					= detalleInicial.FechaOperacion.toString();
					depositoEmpleador.IdMonedaOrigen		= detalleInicial.IdMoneda;
					depositoEmpleador.MontoOrigen			= detalleInicial.DepositoEmpleador;
					depositoEmpleador.IdMonedaDestino		= "UF";
					detalleParidad.add(depositoEmpleador);
					idInterno ++;
					//
					ObjDetalleParidad depositoEmpleador2 = new ObjDetalleParidad();
					depositoEmpleador2.IdInterno			= idInterno;
					depositoEmpleador2.ID					= detalleInicial.ID;
					depositoEmpleador2.Numero				= detalleInicial.Numero;
					depositoEmpleador2.TipoParidad			= "DepositoEmpleadorCLP";
					depositoEmpleador2.Fecha				= detalleInicial.FechaOperacion.toString();
					depositoEmpleador2.IdMonedaOrigen		= detalleInicial.IdMoneda;
					depositoEmpleador2.MontoOrigen			= detalleInicial.DepositoEmpleador;
					depositoEmpleador2.IdMonedaDestino		= "CLP";
					detalleParidad.add(depositoEmpleador2);
					idInterno ++;
					//
					ObjDetalleParidad aporteEmpleador = new ObjDetalleParidad();
					aporteEmpleador.IdInterno 				= idInterno;
					aporteEmpleador.ID						= detalleInicial.ID;
					aporteEmpleador.Numero					= detalleInicial.Numero;
					aporteEmpleador.TipoParidad				= "AporteEmpleador";
					aporteEmpleador.Fecha					= detalleInicial.FechaOperacion.toString();
					aporteEmpleador.IdMonedaOrigen 			= detalleInicial.IdMoneda;
					aporteEmpleador.MontoOrigen				= detalleInicial.AporteEmpleador;
					aporteEmpleador.IdMonedaDestino 		= "UF";
					detalleParidad.add(aporteEmpleador);
					idInterno ++;
					//
					ObjDetalleParidad traspasoEmpleador = new ObjDetalleParidad();
					traspasoEmpleador.IdInterno 			= idInterno;
					traspasoEmpleador.ID					= detalleInicial.ID;
					traspasoEmpleador.Numero				= detalleInicial.Numero;
					traspasoEmpleador.TipoParidad			= "TraspasoEmpleador";
					traspasoEmpleador.Fecha					= detalleInicial.FechaOperacion.toString();
					traspasoEmpleador.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					traspasoEmpleador.MontoOrigen			= detalleInicial.TraspasoEmpleador;
					traspasoEmpleador.IdMonedaDestino 		= "UF";
					detalleParidad.add(traspasoEmpleador);
					idInterno ++;
					//
					ObjDetalleParidad retiroEmpleadorActualizado = new ObjDetalleParidad();
					retiroEmpleadorActualizado.IdInterno 			= idInterno;
					retiroEmpleadorActualizado.ID					= detalleInicial.ID;
					retiroEmpleadorActualizado.Numero				= detalleInicial.Numero;
					retiroEmpleadorActualizado.TipoParidad			= "RetiroEmpleadorActualizado";
					retiroEmpleadorActualizado.Fecha				= detalleInicial.FechaOperacion.toString();
					retiroEmpleadorActualizado.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					retiroEmpleadorActualizado.MontoOrigen			= detalleInicial.RetiroEmpleadorActualizado;
					retiroEmpleadorActualizado.IdMonedaDestino 		= "CLP";
					detalleParidad.add(retiroEmpleadorActualizado);
					idInterno ++;
					//
					ObjDetalleParidad montoRetencionActualizado = new ObjDetalleParidad();
					montoRetencionActualizado.IdInterno 			= idInterno;
					montoRetencionActualizado.ID					= detalleInicial.ID;
					montoRetencionActualizado.Numero				= detalleInicial.Numero;
					montoRetencionActualizado.TipoParidad			= "MontoRetencionActualizado";
					montoRetencionActualizado.Fecha					= detalleInicial.FechaOperacion.toString();
					montoRetencionActualizado.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					montoRetencionActualizado.MontoOrigen			= detalleInicial.MontoRetencionActualizado;
					montoRetencionActualizado.IdMonedaDestino 		= "CLP";
					detalleParidad.add(montoRetencionActualizado);
					idInterno ++;
					//Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
					//
					ObjDetalleParidad ahorrosModalidadIndirectaEmpleador = new ObjDetalleParidad();
					ahorrosModalidadIndirectaEmpleador.IdInterno 			= idInterno;
					ahorrosModalidadIndirectaEmpleador.ID					= detalleInicial.ID;
					ahorrosModalidadIndirectaEmpleador.Numero				= detalleInicial.Numero;
					ahorrosModalidadIndirectaEmpleador.TipoParidad			= "AhorrosModalidadIndirectaEmpleador";
					ahorrosModalidadIndirectaEmpleador.Fecha				= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadIndirectaEmpleador.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					ahorrosModalidadIndirectaEmpleador.MontoOrigen			= detalleInicial.AhorrosModalidadIndirectaEmpleador;
					ahorrosModalidadIndirectaEmpleador.IdMonedaDestino 		= "UF";
					detalleParidad.add(ahorrosModalidadIndirectaEmpleador);
					idInterno ++;
					//
					ObjDetalleParidad ahorrosModalidadIndirectaEmpleador2 = new ObjDetalleParidad();
					ahorrosModalidadIndirectaEmpleador2.IdInterno 			= idInterno;
					ahorrosModalidadIndirectaEmpleador2.ID					= detalleInicial.ID;
					ahorrosModalidadIndirectaEmpleador2.Numero				= detalleInicial.Numero;
					ahorrosModalidadIndirectaEmpleador2.TipoParidad			= "AhorrosModalidadIndirectaEmpleadorCLP";
					ahorrosModalidadIndirectaEmpleador2.Fecha				= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadIndirectaEmpleador2.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					ahorrosModalidadIndirectaEmpleador2.MontoOrigen			= detalleInicial.AhorrosModalidadIndirectaEmpleador;
					ahorrosModalidadIndirectaEmpleador2.IdMonedaDestino 	= "CLP";
					detalleParidad.add(ahorrosModalidadIndirectaEmpleador2);
					idInterno ++;
					//
					ObjDetalleParidad ahorrosModalidadDirectaTrabajador = new ObjDetalleParidad();
					ahorrosModalidadDirectaTrabajador.IdInterno 			= idInterno;
					ahorrosModalidadDirectaTrabajador.ID					= detalleInicial.ID;
					ahorrosModalidadDirectaTrabajador.Numero				= detalleInicial.Numero;
					ahorrosModalidadDirectaTrabajador.TipoParidad			= "AhorrosModalidadDirectaTrabajador";
					ahorrosModalidadDirectaTrabajador.Fecha					= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadDirectaTrabajador.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					ahorrosModalidadDirectaTrabajador.MontoOrigen			= detalleInicial.AhorrosModalidadDirectaTrabajador;
					ahorrosModalidadDirectaTrabajador.IdMonedaDestino 		= "UF";
					detalleParidad.add(ahorrosModalidadDirectaTrabajador);
					idInterno ++;
					//
					ObjDetalleParidad ahorrosModalidadDirectaTrabajador2 = new ObjDetalleParidad();
					ahorrosModalidadDirectaTrabajador2.IdInterno 			= idInterno;
					ahorrosModalidadDirectaTrabajador2.ID					= detalleInicial.ID;
					ahorrosModalidadDirectaTrabajador2.Numero				= detalleInicial.Numero;
					ahorrosModalidadDirectaTrabajador2.TipoParidad			= "AhorrosModalidadDirectaTrabajadorCLP";
					ahorrosModalidadDirectaTrabajador2.Fecha				= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadDirectaTrabajador2.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					ahorrosModalidadDirectaTrabajador2.MontoOrigen			= detalleInicial.AhorrosModalidadDirectaTrabajador;
					ahorrosModalidadDirectaTrabajador2.IdMonedaDestino 		= "CLP";
					detalleParidad.add(ahorrosModalidadDirectaTrabajador2);
					idInterno ++;
					//DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente del ART. 42 Nª1 de la LIR en UF
					//
					ObjDetalleParidad cotizacionesModalidadIndirectaEmpleador = new ObjDetalleParidad();
					cotizacionesModalidadIndirectaEmpleador.IdInterno 			= idInterno;
					cotizacionesModalidadIndirectaEmpleador.ID					= detalleInicial.ID;
					cotizacionesModalidadIndirectaEmpleador.Numero				= detalleInicial.Numero;
					cotizacionesModalidadIndirectaEmpleador.TipoParidad			= "CotizacionesModalidadIndirectaEmpleador";
					cotizacionesModalidadIndirectaEmpleador.Fecha				= detalleInicial.FechaOperacion.toString();
					cotizacionesModalidadIndirectaEmpleador.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					cotizacionesModalidadIndirectaEmpleador.MontoOrigen			= detalleInicial.CotizacionesModalidadIndirectaEmpleador;
					cotizacionesModalidadIndirectaEmpleador.IdMonedaDestino 	= "UF";
					detalleParidad.add(cotizacionesModalidadIndirectaEmpleador);
					idInterno ++;
					//
					ObjDetalleParidad cotizacionesModalidadDirectaTrabajador = new ObjDetalleParidad();
					cotizacionesModalidadDirectaTrabajador.IdInterno 			= idInterno;
					cotizacionesModalidadDirectaTrabajador.ID					= detalleInicial.ID;
					cotizacionesModalidadDirectaTrabajador.Numero				= detalleInicial.Numero;
					cotizacionesModalidadDirectaTrabajador.TipoParidad			= "CotizacionesModalidadDirectaTrabajador";
					cotizacionesModalidadDirectaTrabajador.Fecha				= detalleInicial.FechaOperacion.toString();
					cotizacionesModalidadDirectaTrabajador.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					cotizacionesModalidadDirectaTrabajador.MontoOrigen			= detalleInicial.CotizacionesModalidadDirectaTrabajador;
					cotizacionesModalidadDirectaTrabajador.IdMonedaDestino 		= "UF";
					detalleParidad.add(cotizacionesModalidadDirectaTrabajador);
					idInterno ++;
					//DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente del Art.42 Nª2 de la LIR en UF
					//
					ObjDetalleParidad cotizacionesTrabajadorIndependiente = new ObjDetalleParidad();
					cotizacionesTrabajadorIndependiente.IdInterno 			= idInterno;
					cotizacionesTrabajadorIndependiente.ID					= detalleInicial.ID;
					cotizacionesTrabajadorIndependiente.Numero				= detalleInicial.Numero;
					cotizacionesTrabajadorIndependiente.TipoParidad			= "CotizacionesTrabajadorIndependiente";
					cotizacionesTrabajadorIndependiente.Fecha				= detalleInicial.FechaOperacion.toString();
					cotizacionesTrabajadorIndependiente.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					cotizacionesTrabajadorIndependiente.MontoOrigen			= detalleInicial.CotizacionesTrabajadorIndependiente;
					cotizacionesTrabajadorIndependiente.IdMonedaDestino 	= "UF";
					detalleParidad.add(cotizacionesTrabajadorIndependiente);
					idInterno ++;
					//
					ObjDetalleParidad cotizacionesTrabajadorIndependiente2 = new ObjDetalleParidad();
					cotizacionesTrabajadorIndependiente2.IdInterno 			= idInterno;
					cotizacionesTrabajadorIndependiente2.ID					= detalleInicial.ID;
					cotizacionesTrabajadorIndependiente2.Numero				= detalleInicial.Numero;
					cotizacionesTrabajadorIndependiente2.TipoParidad		= "CotizacionesTrabajadorIndependienteCLP";
					cotizacionesTrabajadorIndependiente2.Fecha				= detalleInicial.FechaOperacion.toString();
					cotizacionesTrabajadorIndependiente2.IdMonedaOrigen 	= detalleInicial.IdMoneda;
					cotizacionesTrabajadorIndependiente2.MontoOrigen		= detalleInicial.CotizacionesTrabajadorIndependiente;
					cotizacionesTrabajadorIndependiente2.IdMonedaDestino 	= "CLP";
					detalleParidad.add(cotizacionesTrabajadorIndependiente2);
					idInterno ++;	
					//Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
				    //del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a los aportes del empleador, actualizados en $
					//
					ObjDetalleParidad montoRetiroPorAhorrosIncisoPrimero = new ObjDetalleParidad();
					montoRetiroPorAhorrosIncisoPrimero.IdInterno 			= idInterno;
					montoRetiroPorAhorrosIncisoPrimero.ID					= detalleInicial.ID;
					montoRetiroPorAhorrosIncisoPrimero.Numero				= detalleInicial.Numero;
					montoRetiroPorAhorrosIncisoPrimero.TipoParidad			= "MontoRetiroPorAhorrosIncisoPrimero";
					montoRetiroPorAhorrosIncisoPrimero.Fecha				= detalleInicial.FechaOperacion.toString();
					montoRetiroPorAhorrosIncisoPrimero.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					montoRetiroPorAhorrosIncisoPrimero.MontoOrigen			= detalleInicial.MontoRetiroPorAhorrosIncisoPrimero;
					montoRetiroPorAhorrosIncisoPrimero.IdMonedaDestino 		= "CLP";
					detalleParidad.add(montoRetiroPorAhorrosIncisoPrimero);
					idInterno ++;
					//Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
					//
					ObjDetalleParidad retencionImpuestoSobreRetiros = new ObjDetalleParidad();
					retencionImpuestoSobreRetiros.IdInterno 			= idInterno;
					retencionImpuestoSobreRetiros.ID					= detalleInicial.ID;
					retencionImpuestoSobreRetiros.Numero				= detalleInicial.Numero;
					retencionImpuestoSobreRetiros.TipoParidad			= "RetencionImpuestoSobreRetiros";
					retencionImpuestoSobreRetiros.Fecha					= detalleInicial.FechaOperacion.toString();
					retencionImpuestoSobreRetiros.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					retencionImpuestoSobreRetiros.MontoOrigen			= detalleInicial.RetencionImpuestoSobreRetiros;
					retencionImpuestoSobreRetiros.IdMonedaDestino 		= "CLP";
					detalleParidad.add(retencionImpuestoSobreRetiros);
					idInterno ++;
					//Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
					//
					ObjDetalleParidad montoRetiroPorAhorrosIncisoSegundo = new ObjDetalleParidad();
					montoRetiroPorAhorrosIncisoSegundo.IdInterno 			= idInterno;
					montoRetiroPorAhorrosIncisoSegundo.ID					= detalleInicial.ID;
					montoRetiroPorAhorrosIncisoSegundo.Numero				= detalleInicial.Numero;
					montoRetiroPorAhorrosIncisoSegundo.TipoParidad			= "MontoRetiroPorAhorrosIncisoSegundo";
					montoRetiroPorAhorrosIncisoSegundo.Fecha				= detalleInicial.FechaOperacion.toString();
					montoRetiroPorAhorrosIncisoSegundo.IdMonedaOrigen 		= detalleInicial.IdMoneda;
					montoRetiroPorAhorrosIncisoSegundo.MontoOrigen			= detalleInicial.MontoRetiroPorAhorrosIncisoSegundo;
					montoRetiroPorAhorrosIncisoSegundo.IdMonedaDestino 		= "CLP";
					detalleParidad.add(montoRetiroPorAhorrosIncisoSegundo);
					idInterno ++;
				}
				else
				{
					ObjDetalleParidad depositoEmpleador2 = new ObjDetalleParidad();
					depositoEmpleador2.IdInterno			= idInterno;
					depositoEmpleador2.ID					= detalleInicial.ID;
					depositoEmpleador2.Numero				= detalleInicial.Numero;
					depositoEmpleador2.TipoParidad			= "DepositoEmpleadorCLP";
					depositoEmpleador2.Fecha				= detalleInicial.FechaOperacion.toString();
					depositoEmpleador2.IdMonedaOrigen		= "UF";
					depositoEmpleador2.MontoOrigen			= detalleInicial.DepositoEmpleador;
					depositoEmpleador2.IdMonedaDestino		= "CLP";
					detalleParidad.add(depositoEmpleador2);
					idInterno ++;
					//
					ObjDetalleParidad ahorrosModalidadIndirectaEmpleador2 = new ObjDetalleParidad();
					ahorrosModalidadIndirectaEmpleador2.IdInterno 			= idInterno;
					ahorrosModalidadIndirectaEmpleador2.ID					= detalleInicial.ID;
					ahorrosModalidadIndirectaEmpleador2.Numero				= detalleInicial.Numero;
					ahorrosModalidadIndirectaEmpleador2.TipoParidad			= "AhorrosModalidadIndirectaEmpleadorCLP";
					ahorrosModalidadIndirectaEmpleador2.Fecha				= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadIndirectaEmpleador2.IdMonedaOrigen 		= "UF";
					ahorrosModalidadIndirectaEmpleador2.MontoOrigen			= detalleInicial.AhorrosModalidadIndirectaEmpleador;
					ahorrosModalidadIndirectaEmpleador2.IdMonedaDestino 	= "CLP";
					detalleParidad.add(ahorrosModalidadIndirectaEmpleador2);
					idInterno ++;
					//
					ObjDetalleParidad ahorrosModalidadDirectaTrabajador2 = new ObjDetalleParidad();
					ahorrosModalidadDirectaTrabajador2.IdInterno 			= idInterno;
					ahorrosModalidadDirectaTrabajador2.ID					= detalleInicial.ID;
					ahorrosModalidadDirectaTrabajador2.Numero				= detalleInicial.Numero;
					ahorrosModalidadDirectaTrabajador2.TipoParidad			= "AhorrosModalidadDirectaTrabajadorCLP";
					ahorrosModalidadDirectaTrabajador2.Fecha				= detalleInicial.FechaOperacion.toString();
					ahorrosModalidadDirectaTrabajador2.IdMonedaOrigen 		= "UF";
					ahorrosModalidadDirectaTrabajador2.MontoOrigen			= detalleInicial.AhorrosModalidadDirectaTrabajador;
					ahorrosModalidadDirectaTrabajador2.IdMonedaDestino 		= "CLP";
					detalleParidad.add(ahorrosModalidadDirectaTrabajador2);
					idInterno ++;
					//
					ObjDetalleParidad cotizacionesTrabajadorIndependiente2 = new ObjDetalleParidad();
					cotizacionesTrabajadorIndependiente2.IdInterno 			= idInterno;
					cotizacionesTrabajadorIndependiente2.ID					= detalleInicial.ID;
					cotizacionesTrabajadorIndependiente2.Numero				= detalleInicial.Numero;
					cotizacionesTrabajadorIndependiente2.TipoParidad		= "CotizacionesTrabajadorIndependienteCLP";
					cotizacionesTrabajadorIndependiente2.Fecha				= detalleInicial.FechaOperacion.toString();
					cotizacionesTrabajadorIndependiente2.IdMonedaOrigen 	= "UF";
					cotizacionesTrabajadorIndependiente2.MontoOrigen		= detalleInicial.CotizacionesTrabajadorIndependiente;
					cotizacionesTrabajadorIndependiente2.IdMonedaDestino 	= "CLP";
					detalleParidad.add(cotizacionesTrabajadorIndependiente2);
					idInterno ++;
				}
			}
			
			//System.out.println(detalleParidad);
			//Se agregan los parametros de entrada para el calcular paridad
			for (ObjDetalleParidad dParidad : detalleParidad) {
				CalcularParidad.CalculoEntrada calculoEntrada = new CalcularParidad.CalculoEntrada();
				calculoEntrada.IdTrabajo 			= -5999L;
				calculoEntrada.IdInterno 			= dParidad.IdInterno;
				calculoEntrada.Fecha				= dParidad.Fecha;
				calculoEntrada.IdMonedaOrigen 		= dParidad.IdMonedaOrigen;
				calculoEntrada.MontoOrigen			= dParidad.MontoOrigen;
				calculoEntrada.IdMonedaDestino 		= dParidad.IdMonedaDestino;
				calculoEntrada.IdUso				= "CARTERA"; 						
				CalcParidad.p_CalculosEntrada.add(calculoEntrada);
			}
			
			CalcularParidad.CalcularParidadSalida ResultParidad = new CalcularParidadSalida();
			if (CalcParidad.p_CalculosEntrada != null){
				CalcParidad.p_IdEmpresa 		= entrada.p_IdEmpresa; 
				CalcParidad.p_IdTrabajo 		= -5999L;
				
				CalcularParidad CP = new CalcularParidad();				
				ResultParidad = CP.logica(con, CalcParidad);
			}
	        
			HashMap<String,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");
			
	        //Resultado
	        for (ObjDetalleParidad dParidad : detalleParidad) {
				CalcularParidad.CalculoSalida resultadoParidad= (CalcularParidad.CalculoSalida) RCP.get(dParidad.IdInterno);
				dParidad.MontoDestino = resultadoParidad.MontoDestino;
	        }
	        //System.out.println(detalleParidad);
	       
	        for (DetalleInicial detalleInicial : lstDetalleInicial) {
	        	for (ObjDetalleParidad dParidad : detalleParidad) {
	        		if ( !dParidad.ID.equals( detalleInicial.ID) ) continue;
	        		if ( dParidad.TipoParidad.equals("DepositoEmpleador") ){
	        			detalleInicial.DepositoEmpleadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("DepositoEmpleadorCLP") ){
	        			detalleInicial.DepositoEmpleadorSegundaMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("AporteEmpleador") ){
	        			detalleInicial.AporteEmpleadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("TraspasoEmpleador") ){
	        			detalleInicial.TraspasoEmpleadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("RetiroEmpleadorActualizado") ){
	        			detalleInicial.RetiroEmpleadorActualizadoMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("MontoRetencionActualizado") ){
	        			detalleInicial.MontoRetencionActualizadoMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("AhorrosModalidadIndirectaEmpleador") ){
	        			detalleInicial.AhorrosModalidadIndirectaEmpleadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("AhorrosModalidadIndirectaEmpleadorCLP") ){
	        			detalleInicial.AhorrosModalidadIndirectaEmpleadorSegundaMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("AhorrosModalidadDirectaTrabajador") ){
	        			detalleInicial.AhorrosModalidadDirectaTrabajadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("AhorrosModalidadDirectaTrabajadorCLP") ){
	        			detalleInicial.AhorrosModalidadDirectaTrabajadorSegundaMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("CotizacionesModalidadIndirectaEmpleador") ){
	        			detalleInicial.CotizacionesModalidadIndirectaEmpleadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("CotizacionesModalidadDirectaTrabajador") ){
	        			detalleInicial.CotizacionesModalidadDirectaTrabajadorMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("CotizacionesTrabajadorIndependiente") ){
	        			detalleInicial.CotizacionesTrabajadorIndependienteMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("CotizacionesTrabajadorIndependienteCLP") ){
	        			detalleInicial.CotizacionesTrabajadorIndependienteSegundaMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("MontoRetiroPorAhorrosIncisoPrimero") ){
	        			detalleInicial.MontoRetiroPorAhorrosIncisoPrimeroMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("RetencionImpuestoSobreRetiros") ){
	        			detalleInicial.RetencionImpuestoSobreRetirosMonedaDJ = dParidad.MontoDestino;
	        		}
	        		
	        		if ( dParidad.TipoParidad.equals("MontoRetiroPorAhorrosIncisoSegundo") ){
	        			detalleInicial.MontoRetiroPorAhorrosIncisoSegundoMonedaDJ = dParidad.MontoDestino;
	        		}
	        	}
	        }
	    //System.out.println(lstDetalleInicial);
	    //actualizacion de registros    
	    sql.batch(con, SQL_UPDATE_DETALLE, lstDetalleInicial, DetalleInicial.class);
	    
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
		 
	}
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			//Connection con = PlataformaUtils.getDESAConnection();
  			Connection con = PlataformaUtils.getQAConnection();
  			try{
  				
  				ActualizarDetallesEntrada entrada = new ActualizarDetallesEntrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 1L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= 2019L;
  				ActualizarDetallesSalida Salida = new ActualizarDetallesSalida();
  				ActualizarDetalles demo = new ActualizarDetalles();
  				Salida = demo.logica(con,entrada);
  				System.out.println(Salida.P_OUT_MENSAJE);
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
}
