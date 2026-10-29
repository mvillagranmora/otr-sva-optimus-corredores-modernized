package cl.bcs.custodia.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularPrecioPromedio implements JavaCall {

	static final Logger logger = LogManager.getLogger(CalcularPrecioPromedio.class.getName());
	
	private final String SQL_CalcularPrecioPromedioSP = loadResourceToString("cl/bcs/custodia/sql/CalcularPrecioPromedio_SEL_CustodiaValida.sql");
	private final String SQL_InsertarClientePortafolioInstrumento = loadResourceToString("cl/bcs/custodia/sql/CalcularPrecioPromedio_INS_ClientePortafolioInstrumento.sql");
	
	public static class ObjEntradaParametros {
		public Long					IdEmpresa; 
		public Long					IdUsuario; 
		public java.sql.Date		FechaParidad; 
		public String				IdMonedaSistema; 
		public String				IdMonedaAdicional; 
		public String				EsDemo; 
		public Long					IdServicioAgrupador; 
		public String				ServicioAgrupador; 
		public String				GrabaLog;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjEntradaEntrada {		
		public Long		IdCliente; 
		public Long		IdPortafolio; 
		public Long		IdInstrumento; 
				
		@Override
        public boolean equals(Object o) {
			ObjEntradaEntrada e = (ObjEntradaEntrada) o;
          boolean b = this.IdCliente.equals(e.IdCliente) && this.IdPortafolio.equals(e.IdPortafolio) && this.IdInstrumento.equals(e.IdInstrumento);
          return b;
        }

        @Override
        public int hashCode() {
            return new Long(this.IdCliente + this.IdPortafolio + this.IdInstrumento).hashCode();  
        }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjSalidaConfiguracionPrecioPromedio {
		public Long					IdConfiguracionPrecioPromedio; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public Long					IdInstrumento; 
		public String				IdOrigenInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjSalidaSalida {
		public String			IdInterno;	//Combinacion entre IdCliente-IdPortafolio-IdInstrumento
		public Long				IdCustodia;
		public String			TablaAfectada;
		public Long				IdCliente; 
		public Long				IdPortafolio;
		public Long				IdInstrumento;
		public String			IdMonedaInstrumento;
		public String			IdBoveda;
		public BigDecimal		Cantidad;
		public BigDecimal		PrecioPromedio;
		public BigDecimal		MontoPromedioMonedaSistema;
		public BigDecimal		PrecioPromedioMonedaInstrumento;
		public BigDecimal		MontoPromedioMonedaInstrumento;
		public BigDecimal		PrecioPromedioMonedaAdicional;
		public BigDecimal		MontoPromedioMonedaAdicional;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CalcularPrecioPromedioEntrada {
		public Long						IdEmpresa; 
		public java.sql.Date			FechaParidad;
		public List<ObjEntradaEntrada>	p_Entrada;
				
		public CalcularPrecioPromedioEntrada (){
			p_Entrada		= new ArrayList<ObjEntradaEntrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CalcularPrecioPromedioSalida {
		public List<ObjSalidaConfiguracionPrecioPromedio>	p_ConfiguracionPrecioPromedio;
		public List<ObjSalidaSalida>						p_Salida;
		public Long											P_OUT_ERRORESCONTADOR;
		
		public CalcularPrecioPromedioSalida (){
			p_ConfiguracionPrecioPromedio 	= new ArrayList<ObjSalidaConfiguracionPrecioPromedio>();
			p_Salida						= new ArrayList<ObjSalidaSalida>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class EntradaTablaPaso {
		public Long Id;
		public String TipoEntrada;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public CalcularPrecioPromedioSalida logica(Connection con, CalcularPrecioPromedioEntrada in) throws Exception {	
		logger.info("Inicio CalcularPrecioPromedio");
		CalcularPrecioPromedioSalida out = new CalcularPrecioPromedioSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql=new SQL();
		
		try {
			String MonedaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");		
			String MonedaAdicional	= ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
			
			//01 - PREPARAR FILTROS CONSULTA 
			List<Long> IdClientes = new ArrayList<Long>();
			List<Long> IdPortafolios = new ArrayList<Long>();
			List<Long> IdInstrumentos = new ArrayList<Long>();
			List<EntradaTablaPaso> entradaClientes = new ArrayList<EntradaTablaPaso>();
			List<EntradaTablaPaso> entradaPortafolios = new ArrayList<EntradaTablaPaso>();
			List<EntradaTablaPaso> entradaInstrumentos = new ArrayList<EntradaTablaPaso>();
			EntradaTablaPaso objetoEntrada;
			for (ObjEntradaEntrada RP: in.p_Entrada){
				if (RP.IdCliente != null && !RP.IdCliente.equals(0L) && !IdClientes.contains(RP.IdCliente)) {
					IdClientes.add( RP.IdCliente );
					objetoEntrada =  new EntradaTablaPaso();
					objetoEntrada.Id = RP.IdCliente;
					objetoEntrada.TipoEntrada = "CLIENTE";
					entradaClientes.add(objetoEntrada);
				}
				if (RP.IdPortafolio != null && !RP.IdPortafolio.equals(0L) && !IdPortafolios.contains(RP.IdPortafolio)) {
					IdPortafolios.add( RP.IdPortafolio );
					objetoEntrada =  new EntradaTablaPaso();
					objetoEntrada.Id = RP.IdPortafolio;
					objetoEntrada.TipoEntrada = "PORTAFOLIO";
					entradaPortafolios.add(objetoEntrada);
				}
				if (RP.IdInstrumento != null && !RP.IdInstrumento.equals(0L) && !IdInstrumentos.contains(RP.IdInstrumento)) {
					IdInstrumentos.add( RP.IdInstrumento );
					objetoEntrada =  new EntradaTablaPaso();
					objetoEntrada.Id = RP.IdInstrumento;
					objetoEntrada.TipoEntrada = "INSTRUMENTO";
					entradaInstrumentos.add(objetoEntrada);
				}
			};
			
			List<EntradaTablaPaso> parametrosEntrada = new ArrayList<EntradaTablaPaso>();
			parametrosEntrada.addAll(entradaClientes);
			parametrosEntrada.addAll(entradaPortafolios);
			parametrosEntrada.addAll(entradaInstrumentos);
			sql.batch(con, SQL_InsertarClientePortafolioInstrumento, parametrosEntrada, EntradaTablaPaso.class);
			/*
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdCliente", IdClientes);
			params.put("IdPortafolio", IdPortafolios);
			params.put("IdInstrumento", IdInstrumentos);
			params.put("FiltroClienteValido", 0);
			params.put("FiltroPortafolioValido", 0);
			params.put("FiltroInstrumentoValido", 0);
						
			if (IdClientes.size() == 0) {
				IdClientes.add(-1L); 
				params.put("FiltroClienteValido", 1);
			}
			if (IdPortafolios.size() == 0) {
				IdPortafolios.add(-1L);
				params.put("FiltroPortafolioValido", 1);
			}
			if (IdInstrumentos.size() == 0) {
				IdInstrumentos.add(-1L);
				params.put("FiltroInstrumentoValido", 1);
			}*/

			List<ObjSalidaSalida> resultPrecioPromedio = sql.query(con, SQL_CalcularPrecioPromedioSP, new HashMap<>(), ObjSalidaSalida.class);
			//List<ObjSalidaSalida> resultPrecioPromedio = sql.query(con, SQL_CalcularPrecioPromedioSP, params, ObjSalidaSalida.class);

			CalcularParidad.CalcularParidadEntrada CalcParidad = new CalcularParidad.CalcularParidadEntrada();
			Long IdTrabajo = new Long((long) (Math.random()*1000000));
			Long i = 1L;
			for (ObjSalidaSalida SS: resultPrecioPromedio){				
				SS.IdInterno = String.valueOf(i);
				SS.PrecioPromedioMonedaInstrumento	= SS.PrecioPromedio;
				SS.MontoPromedioMonedaInstrumento	= SS.Cantidad.multiply(SS.PrecioPromedio); 
				
				CalcularParidad.CalculoEntrada montoParidad = new CalcularParidad.CalculoEntrada();
				montoParidad.IdTrabajo 					= IdTrabajo;
				montoParidad.IdInterno 					= i;
				montoParidad.Fecha						= in.FechaParidad.toString();
				montoParidad.IdMonedaOrigen 			= SS.IdMonedaInstrumento;
				montoParidad.MontoOrigen				= SS.MontoPromedioMonedaInstrumento;  	//En moneda de sistema
				montoParidad.IdMonedaDestino 			= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
				montoParidad.IdUso						= SS.TablaAfectada; 						
				CalcParidad.p_CalculosEntrada.add(montoParidad);
				i++;
			};
			
			
			if (CalcParidad.p_CalculosEntrada != null && CalcParidad.p_CalculosEntrada.size() > 0){
				CalcParidad.p_IdEmpresa 		= in.IdEmpresa; 
				CalcParidad.p_IdTrabajo 		= IdTrabajo;
				
				CalcularParidad CP = new CalcularParidad();
				CalcularParidad.CalcularParidadSalida ResultParidad = CP.logica(con, CalcParidad);
				HashMap<Long,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");
				
				for (ObjSalidaSalida SS: resultPrecioPromedio){	
					CalcularParidad.CalculoSalida resultadoMonInstrumento = (CalcularParidad.CalculoSalida) RCP.get(new Long(SS.IdInterno));
					//solo si salio bien el calculo de paridad, agregar a la salida.
					if(resultadoMonInstrumento.MontoLocal!=null || resultadoMonInstrumento.MontoDestino!=null) {

						SS.PrecioPromedio 					= resultadoMonInstrumento.MontoLocal == null || SS.Cantidad.longValue() == 0 ?new BigDecimal(0):resultadoMonInstrumento.MontoLocal.divide(SS.Cantidad,20,RoundingMode.HALF_UP);					
						SS.MontoPromedioMonedaSistema		= resultadoMonInstrumento.MontoLocal== null?new BigDecimal(0):resultadoMonInstrumento.MontoLocal;
											
						SS.PrecioPromedioMonedaAdicional	= resultadoMonInstrumento.MontoDestino == null || SS.Cantidad.longValue() == 0 ?new BigDecimal(0):resultadoMonInstrumento.MontoDestino.divide(SS.Cantidad,20,RoundingMode.HALF_UP);					
						SS.MontoPromedioMonedaAdicional		= resultadoMonInstrumento.MontoDestino== null?new BigDecimal(0): resultadoMonInstrumento.MontoDestino;				
						
						out.p_Salida.add(SS);
					}else {
						logger.debug("ERROR AL CALCULAR PARIDAD: " + resultadoMonInstrumento);
					}
				};
			};
			//System.out.println(resultPrecioPromedio);
		}
		catch (Exception e) {
			logger.error(e);
			
			throw e;
		}
		logger.info("Fin CalcularPrecioPromedio");
		return out;
	};
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CalcularPrecioPromedioEntrada jin=gson.fromJson(in.toString() ,CalcularPrecioPromedioEntrada.class);
		CalcularPrecioPromedioSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getMBPEConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"IdEmpresa\":1,\"FechaParidad\":\"2019-03-21 00:00:00.000\",\"p_Entrada\":[{\"IdCliente\":213142,\"IdPortafolio\":48634,\"IdInstrumento\":181252},{\"IdCliente\":213142,\"IdPortafolio\":48634,\"IdInstrumento\":182030},{\"IdCliente\":226967,\"IdPortafolio\":55546,\"IdInstrumento\":182063}]}";
				CalcularPrecioPromedioEntrada entrada = gson.fromJson(json, CalcularPrecioPromedioEntrada.class);
				CalcularPrecioPromedio calcularPrecioPromedio = new CalcularPrecioPromedio();
				CalcularPrecioPromedioSalida salida = calcularPrecioPromedio.logica(con, entrada);
				System.out.println(salida.toString());
				con.rollback();
			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
};