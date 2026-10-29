package cl.bcs.tesoreria.ban;

/**
 * 
 * Objetoivo : Consulta libera banco entrega una especie de cartola con todos los movimientos(com) que afectaron el saldo banco y calcula un saldo simulado
 * Autor     : BCS
 * Fecha     : 13/02/2019
  
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.Map;


public class ConsultarLibretaBanco implements JavaCall {
	
	// Log
	private static final Logger logger = LogManager.getLogger(ConsultarLibretaBanco.class);
	
	// Queries
	private final String SQL_SELECT_CalculaSaldoSimulado  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarLibretaBanco_CalculaSaldoSimulado.sql");
	private final String SQL_SELECT_CalculaSaldoSimuladoCSV  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarLibretaBanco_CalculaSaldoSimuladoCSV.sql");
	private final String SQL_SELECT_ConceptosSpotFwd  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarLibretaBanco_ConceptosSpotFwd.sql");
	private final String SQL_SELECT_OperacionesSpot  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarLibretaBanco_OperacionesSpot.sql");
	private final String SQL_SELECT_DolarObservado  = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarLibretaBanco_DolarObservado.sql");
    
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultarLibretaBanco_Entrada 
	{	
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_UsaReglasNegocio;
		public Long			p_IdCuentaBancariaCorredora;
		public String		p_IdMoneda;
		public Long			p_IdBancoCorredora;
		public String		p_FechaConsulta;
		public BigDecimal	p_SaldoInicialSimulado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultarLibretaBanco_Salida extends BcsPojo {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;		
		public List<MovimientosLibretaBanco_Salida>	P_OUT_MOVIMIENTOSLIBRETABANCO;
		public List<SaldoSimulado> P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS;
		
		
		public ConsultarLibretaBanco_Salida()
		{	
			this.P_OUT_MENSAJES 			 	= new ArrayList<MensajeSalida>();
			this.P_OUT_MOVIMIENTOSLIBRETABANCO	= new ArrayList<MovimientosLibretaBanco_Salida>();
			this.P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS	= new ArrayList<SaldoSimulado>();
		}

	}
	
	
	
	public static class MovimientosLibretaBanco_Salida extends BcsPojo
	{	
		
		public Long  			IdComprobante;
		public Long    			IdComprobanteFormaDePago;
		public String  			NombreSocio;
		public String  			DocumentoIdentidadSocio;
		public String  			IdConcepto;
		public String  			NombreConcepto;
		public String  			NombreMedioDePago;
		public String  			IdMoneda;
		public BigDecimal 		MontoIngreso;
		public BigDecimal 		MontoEgreso;
		public BigDecimal 		SaldoDisponible;
		public BigDecimal 		SaldoContable;
		public BigDecimal 		SaldoSimulado;  // Se calcula de acuerdo al SaldoInicialSimulado
		
		public BigDecimal 		SaldoFinalSimulado;
		public java.sql.Date  	FechaAfectaSaldo;
		public java.sql.Date  	FechaLiquidacion;		
		public String  			IdEstado;
		public String  			NombreEstado;		
		public Long    			NumeroDocumentoCorredora;
		public String 			TipoSocioNegocio;
		public Long				IdPortafolio;
		public String			CodigoPortafolio;
		public String			Alias;
		public String			Comentario;
		
		public BigDecimal 		TcCierre=BigDecimal.ZERO;
		public BigDecimal 		MontoEquivalente=BigDecimal.ZERO;

	}
	


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------
	
	
	public static class SaldoSimulado extends BcsPojo
	{
		public Long 			IdComprobante;		
		public Long 			IdComprobanteFormaDePago;
		public String 			NombreSocio;				
		public String 			DocumentoIdentidadSocio;
		public String 			IdConcepto;
		public String 			NombreConcepto;		
		public String 			NombreMedioDePago;
		public String 			IdMoneda;			
		public BigDecimal 		MontoIngreso;
		public BigDecimal 		MontoEgreso;
		public BigDecimal 		SaldoDisponible;
		public BigDecimal 		SaldoContable;
		public BigDecimal 		SaldoSimulado;
		public java.sql.Date	FechaAfectaSaldo;
		public java.sql.Date	FechaLiquidacion;
		public String 			IdEstado;   //-- ??
		public String 			NombreEstado; // ????
		public Long 			NumeroDocumentoCorredora;  // ????
		public String 			TipoSocioNegocio;
		public Long				IdPortafolio;
		public String			CodigoPortafolio;
		public String			Alias;
		public String			Comentario;
		public BigDecimal 		TcCierre=BigDecimal.ZERO;
		public BigDecimal 		MontoEquivalente=BigDecimal.ZERO;
		
	}	
	
	public static class ConceptosSpotFwd extends BcsPojo
	{
		public String IdConcepto;		
		public String Origen;
	}	
	
	public static class ComprobantesSpotFwd extends BcsPojo
	{
		public Long  IdComprobante;
		public Long IdOperacionSpot;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal MontoMonedaSecundaria;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public String Nemotecnico;
		public BigDecimal DolarObservado;
	}
	
	public static class DolarObservadoLibreta extends BcsPojo
	{
		public String  IdParametroFinanciero;
		public Date Fecha;
		public BigDecimal Valor;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		ConsultarLibretaBanco_Entrada in = gson.fromJson(jin.toString(), ConsultarLibretaBanco_Entrada.class);
		ConsultarLibretaBanco_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ConsultarLibretaBanco_Salida logica(Connection con, ConsultarLibretaBanco_Entrada in) throws Exception {
		SQL _sql = new SQL();
		ConsultarLibretaBanco_Salida out = new ConsultarLibretaBanco_Salida();
		
		logger.info("== INICIO CONSULTA LIBRETA BANCO ==");		
		//System.out.println("PARAMETROS DE ENTRADA > " + in.toString());

		HashMap<String,Object> params = new HashMap<>();

		//---------------------------------------------------------------------------------------------
		//VALIDA REQUISITOS BASICOS
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			logger.debug("NO CUMPLE REQUISITOS BASICOS");
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		
		
		//---------------------------------------------------------------------------------------------
		// CALCULA SALDO SIMULADO
		logger.debug("CALCULANDO SALDO SIMULADO");
		
		params.clear();
		params.put("IdBancoCorredora", in.p_IdBancoCorredora);
		params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("FechaConsultaDesde", in.p_FechaConsulta + " 00:00:00");
		params.put("FechaConsultaHasta", in.p_FechaConsulta + " 23:59:59");
		List<SaldoSimulado> lstSaldoSimulado = new ArrayList<>();
		
		//guardo valor dolar observado al dia de la consulta
		logger.debug("INICIO::SQL_SELECT_ConceptosSpotFwd");
		List<ConceptosSpotFwd> conceptosSpotFwd = _sql.query(con, SQL_SELECT_ConceptosSpotFwd,new HashMap<>(), ConceptosSpotFwd.class);
		logger.debug("FIN::SQL_SELECT_ConceptosSpotFwd");
		
		Set<String> idsConceptoSpot = conceptosSpotFwd.stream()
			.filter(b -> "MSP".equalsIgnoreCase(b.Origen))
		    .map(b -> b.IdConcepto)
		    .collect(Collectors.toSet());
		Set<String> idsConceptoFwd = conceptosSpotFwd.stream()
				.filter(b -> "MFW".equalsIgnoreCase(b.Origen))
			    .map(b -> b.IdConcepto)
			    .collect(Collectors.toSet());
		HashMap<String,Object> paramsDO = new HashMap<String, Object>(1);
		paramsDO.put("Fecha", in.p_FechaConsulta);
		
		logger.debug("INICIO::SQL_SELECT_DolarObservado");
		List<DolarObservadoLibreta> dolarObservado = _sql.query(con, SQL_SELECT_DolarObservado,paramsDO, DolarObservadoLibreta.class);
		BigDecimal valorDO = dolarObservado.stream()
		    .filter(x -> "DO".equals(x.IdParametroFinanciero))
		    .map(x -> x.Valor)
		    .findFirst()
		    .orElse(BigDecimal.ZERO);
		
		BigDecimal ValorDolarObservado = MonedaFormato.formatear(con,valorDO, MonedaFormato.MONTO,"USD","","").valor;
		logger.debug("FIN::SQL_SELECT_DolarObservado");
		
		if(in.p_IdCuentaBancariaCorredora == -41) {
			List<SaldoSimulado> lstSaldoSimulado2 = _sql.query(con, SQL_SELECT_CalculaSaldoSimuladoCSV, params, SaldoSimulado.class);
			for (SaldoSimulado Saldo : lstSaldoSimulado2) {
				Saldo.SaldoSimulado = Saldo.SaldoSimulado.add(in.p_SaldoInicialSimulado) ;
			}
			
			List<Long> idsComprobantesFacturaConSpot= lstSaldoSimulado2.stream()
				    .filter(a -> idsConceptoSpot.contains(a.IdConcepto))
				    .map(a -> a.IdComprobante)
				    .collect(Collectors.toList());
			List<Long> idsComprobantesFacturaConFwd= lstSaldoSimulado2.stream()
					    .filter(a -> idsConceptoFwd.contains(a.IdConcepto))
					    .map(a -> a.IdComprobante)
					    .collect(Collectors.toList());
			if(idsComprobantesFacturaConSpot.size()>0 ) {
				HashMap<String,Object> paramSpot = new HashMap<String, Object>(1);
				paramSpot.put("idsComprobante", idsComprobantesFacturaConSpot);
				
				logger.debug("INICIO::SQL_SELECT_OperacionesSpot");
				List<ComprobantesSpotFwd> idsSpot = _sql.query(con, SQL_SELECT_OperacionesSpot, paramSpot, ComprobantesSpotFwd.class);
				logger.debug("FIN::SQL_SELECT_OperacionesSpot");

				Map<Long, List<ComprobantesSpotFwd>> idsSpotGrupos = idsSpot.stream()
				    .collect(Collectors.groupingBy(c -> c.IdComprobante));
				
				
				Map<Long,ComprobantesSpotFwd> mapOperacionesSpot=idsSpotGrupos.entrySet().stream()
												.filter(e -> e.getValue().size()==1)
												.collect(Collectors.toMap( e->e.getKey(),e->e.getValue().get(0)));
				
				lstSaldoSimulado2.forEach(u -> {
					try {
						ComprobantesSpotFwd dataSpot=mapOperacionesSpot.get(u.IdComprobante);						
						if(dataSpot == null) {
							u.TcCierre = BigDecimal.ZERO;
					        u.MontoEquivalente = BigDecimal.ZERO;
					        return;
						}
						
						BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso: u.MontoIngreso;

						if("CLP".equalsIgnoreCase(u.IdMoneda)) {
							u.TcCierre=BigDecimal.ONE;
							u.MontoEquivalente=montoBase;
							return;
						}
						
					
						
						boolean esArbitraje="ARBINT".equalsIgnoreCase(dataSpot.Nemotecnico) || "ARBDIS".equalsIgnoreCase(dataSpot.Nemotecnico);
						if(!esArbitraje) {
							u.TcCierre=dataSpot.TipoCambioCierre;
							u.MontoEquivalente=MonedaFormato.formatear(con,montoBase.multiply(dataSpot.TipoCambioCierre).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","SPT","").valor;
									
							return;
						}
						
						BigDecimal Tc=BigDecimal.ZERO;
						if(esArbitraje) {
							if("USD".equalsIgnoreCase(u.IdMoneda)) {
								Tc=MonedaFormato.formatear(con,dataSpot.DolarObservado,MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
							}else {
								Tc=MonedaFormato.formatear(con,dataSpot.DolarObservado.multiply(dataSpot.ParidadCierre),MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
							}
							
							u.TcCierre=Tc;
							u.MontoEquivalente=MonedaFormato.formatear(con,(montoBase.multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","SPT","").valor;
									
						}
						
						
					}catch(Exception e){
						logger.error(e);
						//System.out.println("Error en comprobante: " + u.IdComprobante);
						e.printStackTrace();

					}
				});
				
			}
			
			if(idsComprobantesFacturaConFwd.size()>0 ) {

				Set<Long> idsFwd = new HashSet<>(idsComprobantesFacturaConFwd);

				lstSaldoSimulado2.forEach(mov -> {
				    if (idsFwd.contains(mov.IdComprobante)&& "CLP".equalsIgnoreCase(mov.IdMoneda)) {
				    	if("CLP".equalsIgnoreCase(mov.IdMoneda)) {
				    		mov.TcCierre=BigDecimal.ONE;
					    	mov.MontoEquivalente= mov.MontoEgreso.compareTo(BigDecimal.ZERO)!=0 ? mov.MontoEgreso :mov.MontoIngreso;
				    	}else {
				    		mov.TcCierre=BigDecimal.ZERO;
					    	mov.MontoEquivalente=BigDecimal.ZERO;	
				    	}			    
				    }			    
				});		
			}
			
			lstSaldoSimulado2.forEach(u -> {
				try {
					if("CLP".equalsIgnoreCase(u.IdMoneda)) {
						u.TcCierre=BigDecimal.ONE;
						u.MontoEquivalente= u.MontoEgreso.compareTo(BigDecimal.ZERO)!=0 ? u.MontoEgreso :u.MontoIngreso; 
					}else {
						if(u.TcCierre.equals(BigDecimal.ZERO) && u.MontoEquivalente.equals(BigDecimal.ZERO)) {	
							boolean esSpot = idsComprobantesFacturaConSpot.contains(u.IdComprobante);
							boolean esFwd = idsComprobantesFacturaConFwd.contains(u.IdComprobante);
							BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso: u.MontoIngreso;
							if(!esSpot && !esFwd) {	
								if("USD".equalsIgnoreCase(u.IdMoneda)) {
									u.TcCierre=ValorDolarObservado;
									u.MontoEquivalente=MonedaFormato.formatear(con,((montoBase).multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","","").valor;
								}else{
									BigDecimal valorPorMoneda = dolarObservado.stream()
										    .filter(x -> u.IdMoneda.equals(x.IdParametroFinanciero))
										    .map(x -> x.Valor)
										    .findFirst()
										    .orElse(BigDecimal.ZERO);							
									u.TcCierre=MonedaFormato.formatear(con,ValorDolarObservado.multiply(valorPorMoneda),MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
									u.MontoEquivalente=MonedaFormato.formatear(con,((montoBase).multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","","").valor;
								}
							}
						}
						
					}
				}catch(Exception e){
					logger.error(e);
					//System.out.println("Error en comprobante: " + u.IdComprobante);
					e.printStackTrace();

				}
			});
			
			out.P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS = lstSaldoSimulado2;
		}else {
			params.put("IdCuentaBancariaCorredora", in.p_IdCuentaBancariaCorredora);
			params.put("IdMoneda", in.p_IdMoneda);
			lstSaldoSimulado = _sql.query(con, SQL_SELECT_CalculaSaldoSimulado, params, SaldoSimulado.class);
			for (SaldoSimulado Saldo : lstSaldoSimulado) {
				Saldo.SaldoSimulado = Saldo.SaldoSimulado.add(in.p_SaldoInicialSimulado) ;
			}
			//System.out.println("SALDOS DISPONIBLES > " + lstSaldoSimulado);

		}

		
		

		
		//---------------------------------------------------------------------------------------------
		// OBTIENE EL SALDO FINAL SIMULADO
		
		int largo = lstSaldoSimulado.size();
		BigDecimal SaldoFinalSimulado = largo > 0 ? lstSaldoSimulado.get(largo-1).SaldoSimulado : BigDecimal.ZERO;
		
		
		//---------------------------------------------------------------------------------------------
		// PREPARA LA SALIDA DEL SERVICIO
		
		List<MovimientosLibretaBanco_Salida> salidaMovimientosLibretaBanco = new ArrayList<MovimientosLibretaBanco_Salida>();
		
		MovimientosLibretaBanco_Salida objMovimientoLibretaBanco;     
		
		for (SaldoSimulado saldoSim : lstSaldoSimulado)
		{	
			objMovimientoLibretaBanco = new MovimientosLibretaBanco_Salida();
			objMovimientoLibretaBanco.SaldoFinalSimulado = SaldoFinalSimulado;
			
			Utils.copyPojo(saldoSim, objMovimientoLibretaBanco);	        
			salidaMovimientosLibretaBanco.add(objMovimientoLibretaBanco);
		}
		
		//Guardo ids comprobante de pago que sean de un SPOT o FWD

		
		List<Long> idsComprobantesFacturaConSpot= salidaMovimientosLibretaBanco.stream()
		    .filter(a -> idsConceptoSpot.contains(a.IdConcepto))
		    .map(a -> a.IdComprobante)
		    .collect(Collectors.toList());
		List<Long> idsComprobantesFacturaConFwd= salidaMovimientosLibretaBanco.stream()
			    .filter(a -> idsConceptoFwd.contains(a.IdConcepto))
			    .map(a -> a.IdComprobante)
			    .collect(Collectors.toList());
		
		if(idsComprobantesFacturaConSpot.size()>0 ) {
			HashMap<String,Object> paramSpot = new HashMap<String, Object>(1);
			paramSpot.put("idsComprobante", idsComprobantesFacturaConSpot);
			
			logger.debug("INICIO::SQL_SELECT_OperacionesSpot");
			List<ComprobantesSpotFwd> idsSpot = _sql.query(con, SQL_SELECT_OperacionesSpot, paramSpot, ComprobantesSpotFwd.class);
			logger.debug("FIN::SQL_SELECT_OperacionesSpot");

			Map<Long, List<ComprobantesSpotFwd>> idsSpotGrupos = idsSpot.stream()
				    .collect(Collectors.groupingBy(c -> c.IdComprobante));
				
				
			Map<Long,ComprobantesSpotFwd> mapOperacionesSpot=idsSpotGrupos.entrySet().stream()
												.filter(e -> e.getValue().size()==1)
												.collect(Collectors.toMap( e->e.getKey(),e->e.getValue().get(0)));
				
			salidaMovimientosLibretaBanco.forEach(u -> {
				try {
					ComprobantesSpotFwd dataSpot=mapOperacionesSpot.get(u.IdComprobante);						
					if(dataSpot == null) {
						u.TcCierre = BigDecimal.ZERO;
					    u.MontoEquivalente = BigDecimal.ZERO;
					     return;
					}
						
					BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso: u.MontoIngreso;

					if("CLP".equalsIgnoreCase(u.IdMoneda)) {
						u.TcCierre=BigDecimal.ONE;
						u.MontoEquivalente=montoBase;
						return;
					}
						
					boolean esArbitraje="ARBINT".equalsIgnoreCase(dataSpot.Nemotecnico) || "ARBDIS".equalsIgnoreCase(dataSpot.Nemotecnico);
					if(!esArbitraje) {
						u.TcCierre=dataSpot.TipoCambioCierre;
						u.MontoEquivalente=MonedaFormato.formatear(con,montoBase.multiply(dataSpot.TipoCambioCierre).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","SPT","").valor;
						return;
					}
						
					BigDecimal Tc=BigDecimal.ZERO;
					if(esArbitraje) {
						if("USD".equalsIgnoreCase(u.IdMoneda)) {
							Tc=MonedaFormato.formatear(con,dataSpot.DolarObservado,MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
						}else {
							Tc=MonedaFormato.formatear(con,dataSpot.DolarObservado.multiply(dataSpot.ParidadCierre),MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
						}
							
						u.TcCierre=Tc;
						u.MontoEquivalente=MonedaFormato.formatear(con,(montoBase.multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","SPT","").valor;
									
					}
							
					}catch(Exception e){
						logger.error(e);
						//System.out.println("Error en comprobante: " + u.IdComprobante);
						e.printStackTrace();

					}
			});
			
		}
		if(idsComprobantesFacturaConFwd.size()>0 ) {
			Set<Long> idsFwd = new HashSet<>(idsComprobantesFacturaConFwd);

			salidaMovimientosLibretaBanco.forEach(mov -> {
			    if (idsFwd.contains(mov.IdComprobante)&& "CLP".equalsIgnoreCase(mov.IdMoneda)) {
			    	if("CLP".equalsIgnoreCase(mov.IdMoneda)) {
			    		mov.TcCierre=BigDecimal.ONE;
				    	mov.MontoEquivalente= mov.MontoEgreso.compareTo(BigDecimal.ZERO)!=0 ? mov.MontoEgreso :mov.MontoIngreso;
			    	}else {
			    		mov.TcCierre=BigDecimal.ZERO;
				    	mov.MontoEquivalente=BigDecimal.ZERO;	
			    	}			    
			    }			    
			});		
		}
		
		
		
		//para CLP TcCierre y montoEquivalente
		salidaMovimientosLibretaBanco.forEach(u -> {
			try {
				if("CLP".equalsIgnoreCase(u.IdMoneda)) {
					u.TcCierre=BigDecimal.ONE;
					u.MontoEquivalente= u.MontoEgreso.compareTo(BigDecimal.ZERO)!=0 ? u.MontoEgreso :u.MontoIngreso; 
				}else {
					if(u.TcCierre.equals(BigDecimal.ZERO) && u.MontoEquivalente.equals(BigDecimal.ZERO)) {	
						boolean esSpot = idsComprobantesFacturaConSpot.contains(u.IdComprobante);
						boolean esFwd = idsComprobantesFacturaConFwd.contains(u.IdComprobante);
						BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso: u.MontoIngreso;
						if(!esSpot && !esFwd) {								
							if("USD".equalsIgnoreCase(u.IdMoneda)) {
								u.TcCierre=ValorDolarObservado;
								u.MontoEquivalente=MonedaFormato.formatear(con,((montoBase).multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","","").valor;
							}else{
								BigDecimal valorPorMoneda = dolarObservado.stream()
									    .filter(x -> u.IdMoneda.equals(x.IdParametroFinanciero))
									    .map(x -> x.Valor)
									    .findFirst()
									    .orElse(BigDecimal.ZERO);							
								u.TcCierre=MonedaFormato.formatear(con,ValorDolarObservado.multiply(valorPorMoneda),MonedaFormato.TIPOCAMBIO,u.IdMoneda,"SPT","").valor;
								u.MontoEquivalente=MonedaFormato.formatear(con,((montoBase).multiply(u.TcCierre)).setScale(2, RoundingMode.HALF_UP),MonedaFormato.MONTO,"CLP","","").valor;
							}
							
						}
					}
					
				}
			}catch(Exception e){
				logger.error(e);
				//System.out.println("Error en comprobante: " + u.IdComprobante);
				e.printStackTrace();

			}
		});
		out.P_OUT_MOVIMIENTOSLIBRETABANCO = salidaMovimientosLibretaBanco;
		
		
		

		//System.out.println("PARAMETROS DE SALIDA > " + out);
		
		logger.info("== TERMINO CONSULTA LIBRETA BANCO ==");
		return out;
		
	}

	
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) 
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 9495,\r\n"
					+ "    \"p_IdModulo\": \"TES\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_IdCuentaBancariaCorredora\": 1084,\r\n"
					+ "    \"p_IdBancoCorredora\": 18,\r\n"
					+ "    \"p_IdMoneda\": \"CLP\",\r\n"
					+ "    \"p_FechaConsulta\": \"2026-05-22\",\r\n"
					+ "    \"p_SaldoInicialSimulado\": 0\r\n"
					+ "}";
			ConsultarLibretaBanco_Entrada Objin = gson.fromJson(json, ConsultarLibretaBanco_Entrada.class);
			ConsultarLibretaBanco demo = new ConsultarLibretaBanco();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				ConsultarLibretaBanco_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				System.out.println(ObjOut.toString());
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			
 			
 			con.close();
 			
	    }
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}

