package cl.bcs.plataforma.val;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularIntereses implements JavaCall {	
	private final Log log = new Log("PLA.CalcularIntereses");
		
	public static class ObjDatosEntrada {
		public Long			IdTrabajo;		
		public String 		FechaCalculo;
		public Long			Plazo; 
		public Long			PlazoResidual;
		public String		IdMonedaConsulta;
		public BigDecimal	MontoMonedaConsulta;
		public BigDecimal	Tasa;
		public Long			Base;
		public String		TipoInteres; 	//Simple o Compuesto
		public String		IdUso;			//Para el calculo de paridad
		public BigDecimal 	Cantidad;
		public BigDecimal	PrecioCompra;
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjResultado {
		public Long			IdTrabajo;
		public String		ResultadoProceso;		
		public Long			Plazo;
		public BigDecimal	TasaPorPlazo;		
		public Long			DiasTranscurridos;
		public BigDecimal	TasaPorDiasTranscurridos;
		//------------------------------------------------------
		public String		IdMonedaSistema;
		public BigDecimal 	MontoMonedaSistema;
		public BigDecimal	InteresDiarioMonedaSistema;
		public BigDecimal	InteresAcumuladoMonedaSistema;
		public BigDecimal	ValorPresenteMonedaSistema;
		//------------------------------------------------------
		public String		IdMonedaConsulta;
		public BigDecimal 	MontoMonedaConsulta;
		public BigDecimal	InteresDiarioMonedaConsulta;
		public BigDecimal	InteresAcumuladoMonedaConsulta;
		public BigDecimal	ValorPresenteMonedaConsulta;
		//------------------------------------------------------
		public String		IdMonedaAdicional;		
		public BigDecimal 	MontoMonedaAdicional;
		public BigDecimal	InteresDiarioMonedaAdicional;
		public BigDecimal	InteresAcumuladoMonedaAdicional;
		public BigDecimal	ValorPresenteMonedaAdicional;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CalcularInteresesEntrada {
		public Long					 IdEmpresa;
		public List<ObjDatosEntrada> DatosEntrada;
		
		public CalcularInteresesEntrada(){
			DatosEntrada = new ArrayList<ObjDatosEntrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CalcularInteresesSalida {
		public List<ObjResultado> ResultadoCalculoInteres;
		
		public CalcularInteresesSalida(){
			ResultadoCalculoInteres = new ArrayList<ObjResultado>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Trace
	public CalcularInteresesSalida logica(Connection con, CalcularInteresesEntrada in) throws Exception {		
		CalcularInteresesSalida out = new CalcularInteresesSalida();

		try {
			String MonedaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");		
			String MonedaAdicional	= ConfiguracionSistema.getParametroConfiguracionString(con, in.IdEmpresa,"PLATAFORMA", "PLA.MonedaAdicional");
			BigDecimal Tasa1Dia = BigDecimal.ZERO;
			
			CalcularParidad CP = new CalcularParidad();
			CalcularParidad.CalcularParidadEntrada CalcParidad = new CalcularParidad.CalcularParidadEntrada();
			HashMap<Long,Long> idsMontoParidad = new HashMap<Long,Long>();
			HashMap<Long,Long> idsInteresDiarioParidad = new HashMap<Long,Long>();
			HashMap<Long,Long> idsInteresAcumuladoParidad = new HashMap<Long,Long>();
			HashMap<Long,Long> idsValorPresenteParidad = new HashMap<Long,Long>();
			Long i = 1L;
			for (ObjDatosEntrada Info: in.DatosEntrada){
				ObjResultado ResultInteres 	= new ObjResultado();
				Utils.copyPojo(Info,ResultInteres);
				
				if (Info.Plazo != null && Info.MontoMonedaConsulta != null){
					ResultInteres.ResultadoProceso	= "OK";
					ResultInteres.DiasTranscurridos	= Info.Plazo - Info.PlazoResidual;
					
					//************************************ CALCULO INTERES SIMPLE ******************************************************************************************
					if ( Info.TipoInteres.equalsIgnoreCase("SIMPLE") ){ 			
						if (Info.Base.equals(0L)) 
							ResultInteres.TasaPorPlazo = new BigDecimal(0);
						else
							ResultInteres.TasaPorPlazo = (Info.Tasa.divide( new BigDecimal(100) ).multiply( new BigDecimal(Info.Plazo).divide(new BigDecimal(Info.Base), 20,RoundingMode.HALF_UP) )  );
						
						
						if (Info.Base.equals(0L)) 
							ResultInteres.TasaPorDiasTranscurridos = new BigDecimal(0);
						else { 
							Tasa1Dia = (Info.Tasa.divide( new BigDecimal(100)).multiply( new BigDecimal(1).divide(new BigDecimal(Info.Base), 20,RoundingMode.HALF_UP) ) );
							ResultInteres.TasaPorDiasTranscurridos = (Info.Tasa.divide( new BigDecimal(100)).multiply( new BigDecimal(ResultInteres.DiasTranscurridos).divide(new BigDecimal(Info.Base), 20,RoundingMode.HALF_UP) ) );							
						}

						//MSH
						//ResultInteres.InteresAcumuladoMonedaConsulta = Info.MontoMonedaConsulta.multiply( BigDecimal.ONE.add(ResultInteres.TasaPorPlazo)).subtract( Info.MontoMonedaConsulta.multiply( new BigDecimal(1).add(ResultInteres.TasaPorDiasTranscurridos)) );
						//ResultInteres.InteresDiarioMonedaConsulta    = Info.MontoMonedaConsulta.multiply( BigDecimal.ONE.add(ResultInteres.TasaPorDiasTranscurridos)).subtract(Info.MontoMonedaConsulta);

						
						ResultInteres.InteresAcumuladoMonedaConsulta = (((Info.Cantidad.multiply(Info.PrecioCompra)).multiply(Info.Tasa).divide(new BigDecimal(100))).divide(new BigDecimal(Info.Base), 20,RoundingMode.HALF_UP)).multiply(new BigDecimal(ResultInteres.DiasTranscurridos));
						ResultInteres.InteresDiarioMonedaConsulta    = ((Info.Cantidad.multiply(Info.PrecioCompra)).multiply(Info.Tasa).divide(new BigDecimal(100))).divide(new BigDecimal(Info.Base), 20,RoundingMode.HALF_UP);
						
						//no son las formulas originales
						//ResultInteres.InteresAcumuladoMonedaConsulta = Info.MontoMonedaConsulta.multiply(ResultInteres.TasaPorDiasTranscurridos);
						//ResultInteres.InteresDiarioMonedaConsulta    = Info.MontoMonedaConsulta.multiply(Tasa1Dia);
						ResultInteres.MontoMonedaConsulta 				= Info.Cantidad.multiply(Info.PrecioCompra).multiply( BigDecimal.ONE.add(ResultInteres.TasaPorPlazo));
						ResultInteres.ValorPresenteMonedaConsulta 		= Info.Cantidad.multiply(Info.PrecioCompra).multiply( BigDecimal.ONE.add(ResultInteres.TasaPorDiasTranscurridos));
						
					};					
					
					//************************************ CALCULO INTERES COMPUESTO ******************************************************************************************
					if (Info.TipoInteres.equalsIgnoreCase("COMPUESTO") ){
						if (Info.Base.equals(0L)){ 
							ResultInteres.TasaPorPlazo = new BigDecimal(0);
							ResultInteres.TasaPorDiasTranscurridos = new BigDecimal(0);						
						}else{ 
							ResultInteres.TasaPorPlazo = (Info.Tasa.divide( new BigDecimal(100) ) );
							ResultInteres.TasaPorDiasTranscurridos = (Info.Tasa.divide( new BigDecimal(100) ) );						
						};
											
						BigDecimal periodo = new BigDecimal(Info.Plazo).divide(new BigDecimal(Info.Base),20,RoundingMode.HALF_UP);
						BigDecimal periodoTranscurrido = new BigDecimal(ResultInteres.DiasTranscurridos).divide(new BigDecimal(Info.Base),20,RoundingMode.HALF_UP);
											
						Double factor = Math.pow(BigDecimal.ONE.add(ResultInteres.TasaPorPlazo).doubleValue(),periodo.doubleValue());
						Double factorTranscurrido = Math.pow(BigDecimal.ONE.add(ResultInteres.TasaPorDiasTranscurridos).doubleValue(),periodoTranscurrido.doubleValue());
						
						ResultInteres.InteresDiarioMonedaConsulta 		= Info.MontoMonedaConsulta.multiply(new BigDecimal(factor)).subtract( Info.MontoMonedaConsulta.multiply(new BigDecimal(factorTranscurrido)) );
						ResultInteres.InteresAcumuladoMonedaConsulta 	= Info.MontoMonedaConsulta.multiply(new BigDecimal(factorTranscurrido)).subtract(Info.MontoMonedaConsulta);
						ResultInteres.ValorPresenteMonedaConsulta 		= Info.MontoMonedaConsulta.multiply(new BigDecimal(factorTranscurrido));
						ResultInteres.MontoMonedaConsulta  				= Info.MontoMonedaConsulta.multiply(new BigDecimal(factor));
					};
					
					ResultInteres.ResultadoProceso	= "OK";
					ResultInteres.IdMonedaSistema	= MonedaSistema;
					ResultInteres.IdMonedaAdicional	= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;					
					out.ResultadoCalculoInteres.add(ResultInteres);
					
					//Objeto para obtener calculo de paridad
					CalcularParidad.CalculoEntrada montoParidad = new CalcularParidad.CalculoEntrada();
					montoParidad.IdTrabajo 					= -100L;
					montoParidad.IdInterno 					= i;
					montoParidad.Fecha						= Info.FechaCalculo;
					montoParidad.IdMonedaOrigen 			= Info.IdMonedaConsulta;
					montoParidad.MontoOrigen				= ResultInteres.MontoMonedaConsulta;
					montoParidad.IdMonedaDestino 			= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
					montoParidad.IdUso						= Info.IdUso != null ? Info.IdUso : "CARTERA"; 						
					CalcParidad.p_CalculosEntrada.add(montoParidad);
					idsMontoParidad.put(Info.IdTrabajo, i);
					i++;
					CalcularParidad.CalculoEntrada interesDiarioParidad = new CalcularParidad.CalculoEntrada();
					interesDiarioParidad.IdTrabajo 			= -100L;
					interesDiarioParidad.IdInterno 			= i;
					interesDiarioParidad.Fecha				= Info.FechaCalculo;
					interesDiarioParidad.IdMonedaOrigen 	= Info.IdMonedaConsulta;
					interesDiarioParidad.MontoOrigen		= ResultInteres.InteresDiarioMonedaConsulta;
					interesDiarioParidad.IdMonedaDestino 	= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
					interesDiarioParidad.IdUso				= Info.IdUso != null ? Info.IdUso : "CARTERA";						
					CalcParidad.p_CalculosEntrada.add(interesDiarioParidad);
					idsInteresDiarioParidad.put(Info.IdTrabajo, i);
					i++;
					CalcularParidad.CalculoEntrada interesAcumuladoParidad = new CalcularParidad.CalculoEntrada();
					interesAcumuladoParidad.IdTrabajo 		= -100L;
					interesAcumuladoParidad.IdInterno 		= i;
					interesAcumuladoParidad.Fecha			= Info.FechaCalculo;
					interesAcumuladoParidad.IdMonedaOrigen 	= Info.IdMonedaConsulta;
					interesAcumuladoParidad.MontoOrigen		= ResultInteres.InteresAcumuladoMonedaConsulta;
					interesAcumuladoParidad.IdMonedaDestino = (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
					interesAcumuladoParidad.IdUso			= Info.IdUso != null ? Info.IdUso : "CARTERA";						
					CalcParidad.p_CalculosEntrada.add(interesAcumuladoParidad);
					idsInteresAcumuladoParidad.put(Info.IdTrabajo, i);
					i++;
					CalcularParidad.CalculoEntrada valorPresenteParidad = new CalcularParidad.CalculoEntrada();
					valorPresenteParidad.IdTrabajo 			= -100L;
					valorPresenteParidad.IdInterno 			= i;
					valorPresenteParidad.Fecha				= Info.FechaCalculo;
					valorPresenteParidad.IdMonedaOrigen 	= Info.IdMonedaConsulta;
					valorPresenteParidad.MontoOrigen		= ResultInteres.ValorPresenteMonedaConsulta;
					valorPresenteParidad.IdMonedaDestino 	= (MonedaAdicional != null) ? MonedaAdicional : MonedaSistema;
					valorPresenteParidad.IdUso				= Info.IdUso != null ? Info.IdUso : "CARTERA";						
					CalcParidad.p_CalculosEntrada.add(valorPresenteParidad);
					idsValorPresenteParidad.put(Info.IdTrabajo, i);
					i++;
				} else {
					ResultInteres.ResultadoProceso = "ERROR";
				};				
			};// for
			
			
			//------- CALCULAR PARIDAD o TIPO CAMBIO y ACTULIZAR RESULTADO CALCULO INTERES -------
			if (CalcParidad.p_CalculosEntrada != null){
				CalcParidad.p_IdEmpresa 		= in.IdEmpresa; 
				CalcParidad.p_IdTrabajo 		= -100L;
				
				CalcularParidad.CalcularParidadSalida ResultParidad = CP.logica(con, CalcParidad);
				HashMap<Long,Object> RCP = Utils.List2Hash(ResultParidad.p_out_CalculosSalida, "IdInterno");
				for (ObjResultado Ri: out.ResultadoCalculoInteres){
					CalcularParidad.CalculoSalida resultadoCalculoParidadMonto = (CalcularParidad.CalculoSalida) RCP.get(idsMontoParidad.get(Ri.IdTrabajo));
					Ri.MontoMonedaSistema	= resultadoCalculoParidadMonto.MontoLocal;
					Ri.MontoMonedaAdicional	= resultadoCalculoParidadMonto.MontoDestino;
					
					CalcularParidad.CalculoSalida resultadoCalculoParidadInteresDiario = (CalcularParidad.CalculoSalida) RCP.get(idsInteresDiarioParidad.get(Ri.IdTrabajo));
					Ri.InteresDiarioMonedaSistema	= resultadoCalculoParidadInteresDiario.MontoLocal;
					Ri.InteresDiarioMonedaAdicional	= resultadoCalculoParidadInteresDiario.MontoDestino;
					
					CalcularParidad.CalculoSalida resultadoCalculoParidadInteresAculumado = (CalcularParidad.CalculoSalida) RCP.get(idsInteresAcumuladoParidad.get(Ri.IdTrabajo));
					Ri.InteresAcumuladoMonedaSistema	= resultadoCalculoParidadInteresAculumado.MontoLocal;
					Ri.InteresAcumuladoMonedaAdicional	= resultadoCalculoParidadInteresAculumado.MontoDestino;
					
					CalcularParidad.CalculoSalida resultadoCalculoParidadValorPresente = (CalcularParidad.CalculoSalida) RCP.get(idsValorPresenteParidad.get(Ri.IdTrabajo));
					Ri.ValorPresenteMonedaSistema	= resultadoCalculoParidadValorPresente.MontoLocal;
					Ri.ValorPresenteMonedaAdicional	= resultadoCalculoParidadValorPresente.MontoDestino;
				};
			};
		}
		catch(Exception er) {
			log.write(er);
			er.printStackTrace();				
		}	
		return out;
	};
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CalcularInteresesEntrada jin=gson.fromJson(in.toString() ,CalcularInteresesEntrada.class);
		CalcularInteresesSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};	
};