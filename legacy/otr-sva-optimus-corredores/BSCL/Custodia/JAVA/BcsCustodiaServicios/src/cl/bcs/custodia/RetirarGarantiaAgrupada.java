package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RetirarGarantiaAgrupada implements JavaCall {
		
	private static final Logger logger = LogManager.getLogger(RetirarGarantiaAgrupada.class);

	
	private final String SQL_TABLA_TEMPORAL_REGISTROS_DES = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantiaAgrupada_TablaRegistrosAgrupados.sql");
	private final String SQL_TABLA_TEMPORAL_INSERT_REGISTROS = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantiaAgrupada_INSERT_TablaRegistrosAgrupados.sql");
	private final String SQL_DELETE_TABLA_TEMPORAL = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantiaAgrupada_DELETE_TablaRegistrosAgrupados.sql");
	
	private final String SQL_SELECT_REGISTROS = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantiaAgrupada_SELECT_RegistrosGarantias.sql");
	
	

	public static class EntradaGarantiasAgrupadas extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_EsSimulacion;
		public Long p_IdEmpleado;
		public List<GarantiaRegistro> p_Garantias;
		

		public EntradaGarantiasAgrupadas() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "CUS";
			this.p_UsaReglasNegocio = "N";
			this.p_EsSimulacion = "S";
			this.p_IdEmpleado = 0L;
			this.p_Garantias = new ArrayList<>();
		}
	}
	
	public static class Registros extends BcsPojo{
		public Long IdGarantia;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaInstrumento;
		public BigDecimal MontoGarantiaMonedaInstrumento;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGPrecioMercadoActual; //Precio Retiro
		public BigDecimal INGTasaMercadoActual; // Tasa Retiro
		public BigDecimal MontoGarantia; // Monto Total Garantia si viene 0 MontoGarantiaMonedaInstrumento
		public BigDecimal CorteMinimo;
		
		public Long IdCliente;
		public Long IdPortafolio;
		public Date INGFechaLiquidacion;	
		public Long IdContraparteGarantia;
		public Long IdInstrumento;
		public String IdOrigenGarantia;
		public String IdConcepto;
		public String ConCertificadoAutomatico;
		public String EsFungible;
		public String Nemotecnico;
		

	}
	
	public static class CalculoGarantia extends BcsPojo{
		public BigDecimal CantidadARetirar;
		public BigDecimal MontoCalculado;
		public BigDecimal MontoARetirar;
		
		public CalculoGarantia() {
			this.CantidadARetirar = BigDecimal.ZERO;
			this.MontoCalculado = BigDecimal.ZERO;
			this.MontoARetirar = BigDecimal.ZERO;
		}
	}
	
	
	public static class GarantiaRegistro extends BcsPojo {
			public Long p_Cliente;
			public Long p_Portafolio;
			public Date p_FechaDeLiquidacion;
			public String p_IdClasificacionIntrumento;
			public Long p_IdContraparteGarantia;
			public String p_IdMonedaInstrumento;
			public String p_IdConcepto;
			public Long p_IdInstrumento;
			public String p_IdOrigenGarantia;
			public String p_Nemotecnico;
			public BigDecimal CantidadAgrupada;
			public BigDecimal CantidadARetirar;
			public BigDecimal MontoARetirar;
			
	}
	
	public static class GarantiaAgrupada extends BcsPojo {
		public GarantiaRegistro GarantiaAgrupada;
		public List <Registros> Registros;
	}
	
	public static class GarantiaARetirar extends BcsPojo {
		public Long IdGarantia;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal MontoMonedaInstrumento;
		public BigDecimal PrecioRetiro;
		public BigDecimal TasaRetiro;
		public BigDecimal MontoARetirar;
		public String IdConcepto;
		public String ConCertificadoAutomatico;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaInstrumento;
		public String EsFungible;
		public String IdOrigenGarantia;
	}
	
	public static class TablaRetiro extends BcsPojo {
		List <GarantiaARetirar> p_tablaRetiro ;
		
		public TablaRetiro() {
	        this.p_tablaRetiro = new ArrayList<GarantiaARetirar>();
	    }
	}
	


	public static class SalidaGarantiasAgrupadas extends BcsPojo {
		public List<CalculoGarantia> P_OUT_TABLA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public SalidaGarantiasAgrupadas() {
			this.P_OUT_TABLA = new ArrayList<CalculoGarantia>();
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;

		}
	}

	public SalidaGarantiasAgrupadas logica(Connection conn, EntradaGarantiasAgrupadas in) throws SQLException, Exception {
		logger.info("INICIO::RetirarGarantiaAgrupada");
		long inicio = System.currentTimeMillis();

		SQL sql=new SQL();
		SalidaGarantiasAgrupadas Salida = new SalidaGarantiasAgrupadas();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		String monedaSistema =  ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa,"PLATAFORMA","PLA.MonedaSistema");
		HashMap<Long, GarantiaARetirar> garantiasARetirar = new HashMap<Long, GarantiaARetirar>();

		HashMap<String, Object> parametros = new HashMap<String, Object>(4);
		
		
		List <GarantiaRegistro> insertRegistros = new ArrayList<GarantiaRegistro>();
		
		if(in.p_Garantias.size() > 0) {
			
			SQL.exec(conn,SQL_TABLA_TEMPORAL_REGISTROS_DES);
			
					// OBTENCION DE REGISTROS
					
					for ( GarantiaRegistro registros : in.p_Garantias) {
						
						GarantiaRegistro garantiaAgrupada = new GarantiaRegistro();
						
						garantiaAgrupada.p_Cliente = registros.p_Cliente;
						garantiaAgrupada.p_Portafolio = registros.p_Portafolio;
						garantiaAgrupada.p_FechaDeLiquidacion = registros.p_FechaDeLiquidacion;
						garantiaAgrupada.p_IdContraparteGarantia = registros.p_IdContraparteGarantia;
						garantiaAgrupada.p_IdMonedaInstrumento = registros.p_IdMonedaInstrumento;
						garantiaAgrupada.p_IdInstrumento = registros.p_IdInstrumento;
						garantiaAgrupada.p_IdClasificacionIntrumento = registros.p_IdClasificacionIntrumento;
						garantiaAgrupada.p_IdOrigenGarantia = registros.p_IdOrigenGarantia;
						garantiaAgrupada.p_IdConcepto = registros.p_IdConcepto;
						garantiaAgrupada.p_Nemotecnico = ("CTA".equals(registros.p_IdOrigenGarantia)) ? "" : registros.p_Nemotecnico;
						garantiaAgrupada.CantidadAgrupada = registros.CantidadAgrupada;
						garantiaAgrupada.CantidadARetirar = registros.CantidadARetirar;
						garantiaAgrupada.MontoARetirar = registros.MontoARetirar;
						
						insertRegistros.add(garantiaAgrupada);
				
					}
					
					sql.batch(conn, SQL_TABLA_TEMPORAL_INSERT_REGISTROS, insertRegistros, GarantiaRegistro.class);
					
					 
					
					List <Registros> registros_des = sql.query(conn, SQL_SELECT_REGISTROS, parametros, Registros.class); 
					
					logger.info("Registros desagrupados.", registros_des);
					
					
					
					List <GarantiaAgrupada> garantiasAgrupadas = new ArrayList<>();
					
					for (GarantiaRegistro garantia : in.p_Garantias) {
						
						GarantiaAgrupada registro = new GarantiaAgrupada();
						
						if ("CTA".equals(garantia.p_IdOrigenGarantia)) {
						    garantia.p_Nemotecnico = "";
						}
			    
					    List<Registros> aux = registros_des.stream()
					        .filter(g -> garantia.p_Cliente.equals(g.IdCliente) &&
					                     garantia.p_Portafolio.equals(g.IdPortafolio) && 
					        			 garantia.p_IdContraparteGarantia.equals(g.IdContraparteGarantia) &&
					        			 garantia.p_IdMonedaInstrumento.equals(g.IdMonedaInstrumento) &&
					        			 garantia.p_IdOrigenGarantia.equals(g.IdOrigenGarantia) &&
					        			 garantia.p_IdConcepto.equals(g.IdConcepto) &&
					        			 garantia.p_IdInstrumento.equals(g.IdInstrumento) &&
					        			 garantia.p_Nemotecnico.equals(g.Nemotecnico)
					        		)
					        
					        .sorted(Comparator.comparingLong(g -> g.IdGarantia))
					        .collect(Collectors.toList()); 

					    registro.GarantiaAgrupada = garantia;
					    registro.Registros = aux;
					    
					    garantiasAgrupadas.add(registro);
					    
					}										
					logger.info("Registros agrupados: ", garantiasAgrupadas);
					
					// COMENSAMOS CON EL PROCESO DE RETIRO DE GARANTIAS
							

					for (GarantiaAgrupada GTAagrup : garantiasAgrupadas) { // SE RECORRE CADA REGISTRO AGRUPADO
						
						
						
						
						
						if("CUS".equals(GTAagrup.GarantiaAgrupada.p_IdOrigenGarantia)){
							BigDecimal totalCantidadGarantiaAgrupada = GTAagrup.GarantiaAgrupada.CantidadAgrupada;
							BigDecimal restante = GTAagrup.GarantiaAgrupada.CantidadARetirar;
							BigDecimal CantidadARetirarCalculada = BigDecimal.ZERO;
							
							if (GTAagrup.GarantiaAgrupada.CantidadAgrupada.compareTo(GTAagrup.GarantiaAgrupada.CantidadARetirar) >= 0) {
							
							
							if ("RV".equals(GTAagrup.GarantiaAgrupada.p_IdClasificacionIntrumento) && GTAagrup.GarantiaAgrupada.CantidadARetirar.compareTo(BigDecimal.ZERO) == 0) {
								
								BigDecimal montoRestante = GTAagrup.GarantiaAgrupada.MontoARetirar;
								
								for (Registros reg : GTAagrup.Registros) {
									
									if(montoRestante.compareTo(BigDecimal.ZERO) > 0) {
										
										if(reg.MontoGarantia.compareTo(montoRestante) >= 0) {	
											CantidadARetirarCalculada = CantidadARetirarCalculada.add((montoRestante.multiply(reg.INGCantidad)).divide(reg.MontoGarantia, 0, RoundingMode.DOWN));
											break;
										}
										else {
											CantidadARetirarCalculada = CantidadARetirarCalculada.add(reg.INGCantidad);
											montoRestante = montoRestante.subtract(reg.MontoGarantia);
										}
									}
									
								}
								restante = CantidadARetirarCalculada;
								
							}
							
							for (Registros reg : GTAagrup.Registros) { // SE RECORREN SUS REGISTROS DESAGRUPADOS ORDENADOS
								
								GarantiaARetirar registroARetirar = new GarantiaARetirar();
								
								Boolean corteMinimoInvalido = validarCorteMinimo(GTAagrup.GarantiaAgrupada, reg);
								
								if ("IF".equals(reg.IdClasificacionTipoInstrumento) || "RF".equals(reg.IdClasificacionTipoInstrumento)) {
									if (!corteMinimoInvalido) {
										Salida.P_OUT_ERRORESCONTADOR++;
										Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CORTE_MINIMO_INVALIDO", new HashMap<>(), conn, in.p_IdEmpresa));
										return Salida;
									}
								}
								
//					
								if(restante.compareTo(BigDecimal.ZERO) > 0) { // MIENTRAS HAYA RESTANTE HARA CALCULO
									if (totalCantidadGarantiaAgrupada.compareTo(restante) >= 0  && reg.INGCantidad.compareTo(BigDecimal.ZERO) != 0) {
										logger.info("INFO::RetirarGarantiaAgrupada Cantidad a retirar agrupada restante: " + restante);
									
										if("IF".equals(GTAagrup.GarantiaAgrupada.p_IdClasificacionIntrumento)) {
											
											if(reg.MontoGarantiaMonedaInstrumento.compareTo(BigDecimal.ZERO) > 0) {
												
												if(restante.compareTo(reg.INGCantidad) <= 0) { // RETIRO PARCIAL
													BigDecimal montoARetirarReg = (reg.MontoGarantiaMonedaInstrumento.divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP)).multiply(restante);													
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = restante;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													
													
													restante = restante.subtract(restante).abs();
												}
												else // RETIRO TOTAL DE LA GARANTIA
												{ 
													BigDecimal montoARetirarReg = (reg.MontoGarantiaMonedaInstrumento.divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP)).multiply(reg.INGCantidad);
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = ("N".equals(reg.EsFungible)) ? restante.divide(reg.CorteMinimo, 10, RoundingMode.HALF_UP) : restante;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(reg.INGCantidad).abs();
												}
											}
											
											else 
											{
												if(restante.compareTo(reg.INGValorRescate) <= 0) { // RETIRO PARCIAL
													BigDecimal montoARetirarReg = ("N".equals(reg.EsFungible)) ? (restante.multiply(reg.MontoGarantia)).divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP) : restante; 
													 
													if("N".equals(reg.EsFungible)) {
														montoARetirarReg = (restante.multiply(reg.MontoGarantia)).divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP);
													}
													
													else {
														if("CLP".equals(reg.IdMonedaInstrumento)) {
															montoARetirarReg = (restante.multiply(reg.MontoGarantia)).divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP);
														}
														else {
															montoARetirarReg = restante;
														}
													}
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = ("N".equals(reg.EsFungible)) ? restante.divide(reg.CorteMinimo, 10, RoundingMode.HALF_UP) : restante;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = restante;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(restante).abs();
												}
												else // RETIRO TOTAL DE LA GARANTIA
												{ 
													BigDecimal montoARetirarReg = BigDecimal.ZERO;
													
													if("N".equals(reg.EsFungible)) {
														montoARetirarReg = reg.MontoGarantia;
													}
													
													else {
														if("CLP".equals(reg.IdMonedaInstrumento)) {
															montoARetirarReg = (reg.INGValorRescate.multiply(reg.MontoGarantia)).divide(reg.INGValorRescate, 10, RoundingMode.HALF_UP);
														}
														else {
															montoARetirarReg = reg.INGValorRescate;
														}
													}
											
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = ("N".equals(reg.EsFungible)) ? reg.INGValorRescate.divide(reg.CorteMinimo, 10, RoundingMode.HALF_UP) : reg.INGValorRescate;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(reg.INGValorRescate).abs();
												}
											}
										}
										
										else { // RV
											if(reg.MontoGarantiaMonedaInstrumento.compareTo(BigDecimal.ZERO) > 0) {
												
												if(restante.compareTo(reg.INGCantidad) <= 0) { // RETIRO PARCIAL
													BigDecimal montoARetirarReg = (reg.MontoGarantiaMonedaInstrumento.divide(reg.INGCantidad, 10, RoundingMode.HALF_UP)).multiply(restante);	
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = restante;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(restante).abs();
												}
												else // RETIRO TOTAL DE LA GARANTIA
												{ 
													BigDecimal montoARetirarReg = (reg.MontoGarantiaMonedaInstrumento.divide(reg.INGCantidad, 10, RoundingMode.HALF_UP)).multiply(reg.INGCantidad);
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = reg.INGCantidad;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(reg.INGCantidad).abs();
												}
											}
											
											else // RF
											{
												if(restante.compareTo(reg.INGCantidad) <= 0) { // RETIRO PARCIAL
													BigDecimal montoARetirarReg = (reg.INGPrecioMercadoActual.multiply(restante));
																										
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = restante;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(restante).abs();
												}
												else // RETIRO TOTAL DE LA GARANTIA
												{
													BigDecimal montoARetirarReg = (reg.INGPrecioMercadoActual.multiply(reg.INGCantidad));
													
													registroARetirar.IdGarantia = reg.IdGarantia;
													registroARetirar.Cantidad = reg.INGCantidad;
													registroARetirar.MontoMonedaInstrumento = montoARetirarReg;
													registroARetirar.IdConcepto = reg.IdConcepto; 
													registroARetirar.ConCertificadoAutomatico = reg.ConCertificadoAutomatico;
													registroARetirar.IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
													registroARetirar.PrecioRetiro = reg.INGPrecioMercadoActual;
													registroARetirar.TasaRetiro = reg.INGTasaMercadoActual;
													registroARetirar.ValorRescate = reg.INGValorRescate;
													registroARetirar.IdMonedaInstrumento = reg.IdMonedaInstrumento;
													registroARetirar.MontoARetirar = montoARetirarReg;
													registroARetirar.EsFungible = reg.EsFungible;
													registroARetirar.IdOrigenGarantia = reg.IdOrigenGarantia;
													garantiasARetirar.put(reg.IdGarantia, registroARetirar);
													
													
													restante = restante.subtract(reg.INGCantidad).abs();
												}
											}
										}
					
									}
								}
								
								
							}
							
						}
							else {
								
								Salida.P_OUT_ERRORESCONTADOR++;
								Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_EXCEDE_MONTO", new HashMap<>(), conn, in.p_IdEmpresa));
								
								
							}
						}
						
						if ("CTA".equals(GTAagrup.GarantiaAgrupada.p_IdOrigenGarantia) ) { // CUENTA INVERSION
							BigDecimal restanteCTA= GTAagrup.GarantiaAgrupada.MontoARetirar;
							
							
							if(GTAagrup.GarantiaAgrupada.CantidadAgrupada.compareTo(GTAagrup.GarantiaAgrupada.MontoARetirar) >= 0) {
							
							if (restanteCTA.compareTo(BigDecimal.ZERO) > 0  && restanteCTA.compareTo(GTAagrup.GarantiaAgrupada.CantidadAgrupada) <= 0) {
								
								for (Registros regCTA : GTAagrup.Registros) {
									
									if (restanteCTA.compareTo(BigDecimal.ZERO) > 0) {
										
										GarantiaARetirar registroARetirar = new GarantiaARetirar();
										
										if (restanteCTA.compareTo(regCTA.INGCantidad) <= 0) { /// RETIRO PARCIAL
											
											
											registroARetirar.IdGarantia = regCTA.IdGarantia;
											registroARetirar.Cantidad = restanteCTA;
											registroARetirar.MontoMonedaInstrumento = restanteCTA;
											registroARetirar.IdConcepto = regCTA.IdConcepto; 
											registroARetirar.ConCertificadoAutomatico = regCTA.ConCertificadoAutomatico;
											registroARetirar.IdClasificacionTipoInstrumento = regCTA.IdClasificacionTipoInstrumento;
											registroARetirar.PrecioRetiro = regCTA.INGPrecioMercadoActual;
											registroARetirar.TasaRetiro = regCTA.INGTasaMercadoActual;
											registroARetirar.ValorRescate = regCTA.INGValorRescate;
											registroARetirar.IdMonedaInstrumento = regCTA.IdMonedaInstrumento;
											registroARetirar.MontoARetirar = restanteCTA;
											registroARetirar.EsFungible = regCTA.EsFungible;
											registroARetirar.IdOrigenGarantia = regCTA.IdOrigenGarantia;
											garantiasARetirar.put(regCTA.IdGarantia, registroARetirar);
											
											
											restanteCTA = restanteCTA.subtract(restanteCTA).abs();
											
											
										}
										else { // RETIRO TOTAL DE LA GARANTIA
											
											
											registroARetirar.IdGarantia = regCTA.IdGarantia;
											registroARetirar.Cantidad = regCTA.INGCantidad;
											registroARetirar.MontoMonedaInstrumento = regCTA.INGCantidad;
											registroARetirar.IdConcepto = regCTA.IdConcepto; 
											registroARetirar.ConCertificadoAutomatico = regCTA.ConCertificadoAutomatico;
											registroARetirar.IdClasificacionTipoInstrumento = regCTA.IdClasificacionTipoInstrumento;
											registroARetirar.PrecioRetiro = regCTA.INGPrecioMercadoActual;
											registroARetirar.TasaRetiro = regCTA.INGTasaMercadoActual;
											registroARetirar.ValorRescate = regCTA.INGValorRescate;
											registroARetirar.IdMonedaInstrumento = regCTA.IdMonedaInstrumento;
											registroARetirar.MontoARetirar = regCTA.INGCantidad;
											registroARetirar.EsFungible = regCTA.EsFungible;
											registroARetirar.IdOrigenGarantia = regCTA.IdOrigenGarantia;
											garantiasARetirar.put(regCTA.IdGarantia, registroARetirar);
											
											
											restanteCTA = restanteCTA.subtract(regCTA.INGCantidad).abs();
										}
									}
								}
								
								
								
							}
						}
							else {
								
								Salida.P_OUT_ERRORESCONTADOR++;
								Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_EXCEDE_MONTO", new HashMap<>(), conn, in.p_IdEmpresa));
								
								
							}
						
						}	
					
					}
					/// SE COMIENZA A EL CALCULO DE PARIDAD DE LAS GARANTIAS A RETIRAR
					logger.info("Registros a Retirar" + garantiasARetirar);
					
					if(!garantiasARetirar.isEmpty()) {
					
					CalcularParidad calcularParidadOut = new CalcularParidad();
					CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
					
					calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
					calcularParidadEntrada.p_IdTrabajo = 0L;
					
					calcularParidadEntrada.p_CalculosEntrada = new ArrayList<>();
					
					garantiasARetirar.forEach((idgarantia, var) -> {
							CalcularParidad.CalculoEntrada datosIntParidad = new CalcularParidad.CalculoEntrada();
							
							datosIntParidad.IdTrabajo = 0L;
							datosIntParidad.IdInterno = var.IdGarantia;
							datosIntParidad.Fecha = fechaSistema;
							datosIntParidad.MontoOrigen = var.MontoMonedaInstrumento;
							datosIntParidad.IdMonedaOrigen = var.IdMonedaInstrumento;
							datosIntParidad.IdMonedaDestino = monedaSistema;
							datosIntParidad.IdUso = "CUS";
					
							calcularParidadEntrada.p_CalculosEntrada.add(datosIntParidad);
						}
					);
					
					CalcularParidadSalida respuestaParidadOut = calcularParidadOut.logica(conn, calcularParidadEntrada);
					logger.info("INFO::RetirarGarantiaAgrupada Monto a Retirar:" + respuestaParidadOut);
					
					// 
					
					for (CalculoSalida paridad : respuestaParidadOut.p_out_CalculosSalida) {
						
						
						
						if(garantiasARetirar.containsKey(paridad.IdInterno)) {
							;
							
							garantiasARetirar.get(paridad.IdInterno).MontoARetirar = paridad.MontoOrigen;
							if (garantiasARetirar.get(paridad.IdInterno).IdMonedaInstrumento.equals(paridad.IdMonedaOrigen)) {
								garantiasARetirar.get(paridad.IdInterno).MontoMonedaInstrumento = paridad.MontoLocal;
							}
							else {
								garantiasARetirar.get(paridad.IdInterno).MontoMonedaInstrumento = paridad.MontoDestino;
							}
							
							
							
						}
						
					}
					
					}
					
					if("S".equals(in.p_EsSimulacion)) {
						
						logger.info("PostParidad:" + garantiasARetirar);
						
						if(!garantiasARetirar.isEmpty()) {
						
						for (GarantiaAgrupada agrupado : garantiasAgrupadas) {
							CalculoGarantia SalidaSimulado = new CalculoGarantia();
							
							 for (Registros reg : agrupado.Registros) {								 
								 GarantiaARetirar garantia = garantiasARetirar.get(reg.IdGarantia);
								 
								 if(garantia != null) {
									 
									 if("IF".equals(garantia.IdClasificacionTipoInstrumento)) {
										 SalidaSimulado.CantidadARetirar = ("N".equals(garantia.EsFungible)) ? SalidaSimulado.CantidadARetirar.add(garantia.ValorRescate): SalidaSimulado.CantidadARetirar.add(garantia.Cantidad);
											
									 }
									 else {
										 SalidaSimulado.CantidadARetirar = SalidaSimulado.CantidadARetirar.add(garantia.Cantidad);
									 }
									 
											 
									 SalidaSimulado.MontoCalculado = SalidaSimulado.MontoCalculado.add(garantia.MontoMonedaInstrumento);
									 SalidaSimulado.MontoARetirar = SalidaSimulado.MontoARetirar.add(garantia.MontoARetirar);
								 }
 
							 }
							 
							 Salida.P_OUT_TABLA.add(SalidaSimulado);
							 
							 
							}

						}
					}
					
					if("N".equals(in.p_EsSimulacion)) {
						/// RETIRO DE GARANTIAS
						
						logger.info("N°Registros a Retirar " + garantiasARetirar.size());
						
						if(!garantiasARetirar.isEmpty()) {
						
						RetirarGarantiaJava retiroGarantias =  new RetirarGarantiaJava();
						
						
						List <ObjGarantiasARetirar> p_tablaRetiroCUS= new ArrayList<>();
						List <ObjGarantiasARetirar> p_tablaRetiroCTA= new ArrayList<>();
						
						garantiasARetirar.forEach((idgarantia, var) -> {
							ObjGarantiasARetirar garantia = new ObjGarantiasARetirar();
							
							if("IF".equals(var.IdClasificacionTipoInstrumento)) {
								
								garantia.IdGarantia = var.IdGarantia;
								garantia.Cantidad =  var.Cantidad;
								garantia.ValorRescate = var.ValorRescate;
								garantia.MontoMonedaInstrumento = BigDecimal.ZERO;
								garantia.PrecioRetiro = BigDecimal.ZERO;
								garantia.TasaRetiro = BigDecimal.ZERO;
								garantia.IdConcepto = var.IdConcepto;
								garantia.ConCertificadoAutomatico = var.ConCertificadoAutomatico;
								garantia.IdClasificacionTipoInstrumento = var.IdClasificacionTipoInstrumento;
								
								if("CUS".equals(var.IdOrigenGarantia)) {
									p_tablaRetiroCUS.add(garantia);
								}
								
								
								
							}
							
							else {
								garantia.IdGarantia = var.IdGarantia;
								garantia.Cantidad = var.Cantidad;
								garantia.ValorRescate = (!"CTA".equals(var.IdOrigenGarantia)) ? var.Cantidad : null;
								garantia.MontoMonedaInstrumento = var.MontoARetirar;
								garantia.PrecioRetiro = var.PrecioRetiro;
								garantia.TasaRetiro = var.TasaRetiro;
								garantia.IdConcepto = var.IdConcepto;
								garantia.ConCertificadoAutomatico = var.ConCertificadoAutomatico;
								garantia.IdClasificacionTipoInstrumento = var.IdClasificacionTipoInstrumento;
								
								if("CTA".equals(var.IdOrigenGarantia)) {
									p_tablaRetiroCTA.add(garantia);
								}
								else {
									p_tablaRetiroCUS.add(garantia);
								}
								
								
							}
							
							
						}
					);
						
						if(!p_tablaRetiroCTA.isEmpty()) {
							
							RetirarGarantiaEntrada entradaRetiroGarantiasCTA = new RetirarGarantiaEntrada();
							
							entradaRetiroGarantiasCTA.p_IdEmpresa = in.p_IdEmpresa;
							entradaRetiroGarantiasCTA.p_IdUsuario = in.p_IdUsuario;
							entradaRetiroGarantiasCTA.p_IdEmpleado = in.p_IdEmpleado;
							entradaRetiroGarantiasCTA.p_IdModulo = in.p_IdModulo;
							entradaRetiroGarantiasCTA.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
							entradaRetiroGarantiasCTA.p_IdTipoOperacion = "RETGTA";
							entradaRetiroGarantiasCTA.p_FechaMovimiento = fechaSistema;
							entradaRetiroGarantiasCTA.p_SimulaOperacion = in.p_EsSimulacion;
							entradaRetiroGarantiasCTA.p_tablaRetiro = p_tablaRetiroCTA;
							
							RetirarGarantiaSalida salidaGTA = retiroGarantias.logica(conn, entradaRetiroGarantiasCTA);
							logger.info("SalidaGTA CUS:" + salidaGTA);
							
							Salida.P_OUT_MENSAJES.addAll(salidaGTA.P_OUT_MENSAJES);
							Salida.P_OUT_ERRORESCONTADOR += salidaGTA.P_OUT_ERRORESCONTADOR;
							
						}
						
						if(!p_tablaRetiroCUS.isEmpty()) {
							
							RetirarGarantiaEntrada entradaRetiroGarantiasCUS = new RetirarGarantiaEntrada();
							
							entradaRetiroGarantiasCUS.p_IdEmpresa = in.p_IdEmpresa;
							entradaRetiroGarantiasCUS.p_IdUsuario = in.p_IdUsuario;
							entradaRetiroGarantiasCUS.p_IdEmpleado = in.p_IdEmpleado;
							entradaRetiroGarantiasCUS.p_IdModulo = in.p_IdModulo;
							entradaRetiroGarantiasCUS.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
							entradaRetiroGarantiasCUS.p_IdTipoOperacion = "RETGTA";
							entradaRetiroGarantiasCUS.p_FechaMovimiento = fechaSistema;
							entradaRetiroGarantiasCUS.p_SimulaOperacion = in.p_EsSimulacion;
							entradaRetiroGarantiasCUS.p_tablaRetiro = p_tablaRetiroCUS;
							
							RetirarGarantiaSalida salidaGTA = retiroGarantias.logica(conn, entradaRetiroGarantiasCUS);
							logger.info("SalidaGTA CUS:" + salidaGTA);
							
							Salida.P_OUT_MENSAJES.addAll(salidaGTA.P_OUT_MENSAJES);
							Salida.P_OUT_ERRORESCONTADOR += salidaGTA.P_OUT_ERRORESCONTADOR;
							
						}

						
					}
				
					}
			
			
			
				
		}
		
		
		
		Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
		
		SQL.exec(conn,SQL_DELETE_TABLA_TEMPORAL);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::RetirarGarantiaAgrupada.", fin - inicio);
		
		return Salida;
	}
	
	
	public boolean validarCorteMinimo(GarantiaRegistro in ,Registros reg) {
		
		Boolean validador = false;
		
		if ("IF".equals(reg.IdClasificacionTipoInstrumento) || "RF".equals(reg.IdClasificacionTipoInstrumento)) {
			BigDecimal cantidadR = in.CantidadARetirar;
			BigDecimal corteMinimo = reg.CorteMinimo;
			BigDecimal nominal = cantidadR.remainder(corteMinimo);
			
			System.out.println("este es el nominal: " + nominal);
			
			if(nominal.compareTo(BigDecimal.ZERO) == 0) {
				validador = true;
				return validador;
			}
			
			else {
				validador = false;
				return validador;
			}			
		}
		return validador;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaGarantiasAgrupadas jin=gson.fromJson(in.toString() , EntradaGarantiasAgrupadas.class);
		SalidaGarantiasAgrupadas out = logica(con,jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) { /// 3050; p:7757
			Gson gson = Utils.getGsonBuilder();			
			String json3 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EsSimulacion\":\"S\",\"p_Garantias\":[{\"p_Cliente\":3050,\"p_Portafolio\":7757,\"p_FechaDeLiquidacion\":\"2025-02-03\",\"p_IdClasificacionIntrumento\":\"RV\",\"p_IdContraparteGarantia\":6,\"p_IdMonedaInstrumento\":\"CLP\",\"p_IdConcepto\":\"IGRV06\",\"p_IdInstrumento\":252930,\"p_IdOrigenGarantia\":\"CUS\",\"CantidadAgrupada\":990000,\"CantidadARetirar\":200000,\"MontoARetirar\":0},{\"p_Cliente\":4843,\"p_Portafolio\":1285,\"p_FechaDeLiquidacion\":\"2025-04-02\",\"p_IdClasificacionIntrumento\":\"RF\",\"p_IdContraparteGarantia\":84,\"p_IdMonedaInstrumento\":\"CLP\",\"p_IdConcepto\":\"IGRV85\",\"p_IdInstrumento\":70051,\"p_IdOrigenGarantia\":\"CUS\",\"CantidadAgrupada\":3500,\"CantidadARetirar\":2000,\"MontoARetirar\":0}]}\r\n";
			String json4LTM = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EsSimulacion\":\"N\",\"p_Garantias\":[{\"p_Cliente\":3050,\"p_Portafolio\":7757,\"p_FechaDeLiquidacion\":\"2025-03-21\",\"p_IdClasificacionIntrumento\":\"RV\",\"p_IdContraparteGarantia\":84,\"p_IdMonedaInstrumento\":\"CLP\",\"p_IdConcepto\":\"IGRV85\",\"p_IdInstrumento\":252930,\"p_IdOrigenGarantia\":\"CUS\",\"CantidadAgrupada\":40000,\"CantidadARetirar\":20000,\"MontoARetirar\":538769.25000000}]}\r\n";
			String jsonCTA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EsSimulacion\":\"S\",\"p_Garantias\":[{\"p_Cliente\":3050,\"p_Portafolio\":7757,\"p_FechaDeLiquidacion\":\"2025-04-01\",\"p_IdClasificacionIntrumento\":\"\",\"p_IdContraparteGarantia\":84,\"p_IdMonedaInstrumento\":\"USD\",\"p_IdConcepto\":\"IGRV41\",\"p_IdInstrumento\":0,\"p_IdOrigenGarantia\":\"CTA\",\"CantidadAgrupada\":700.21,\"CantidadARetirar\":0,\"MontoARetirar\":300}]}\r\n";
			String json5IBM = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EsSimulacion\":\"N\",\"p_Garantias\":[{\"p_Cliente\":3050,\"p_Portafolio\":7757,\"p_FechaDeLiquidacion\":\"2025-03-21\",\"p_IdClasificacionIntrumento\":\"RV\",\"p_IdContraparteGarantia\":84,\"p_IdMonedaInstrumento\":\"USD\",\"p_IdConcepto\":\"IGRV85\",\"p_IdInstrumento\":1135,\"p_IdOrigenGarantia\":\"CUS\",\"CantidadAgrupada\":325.00,\"CantidadARetirar\":300,\"MontoARetirar\":0}]}\r\n";
			EntradaGarantiasAgrupadas Objin = gson.fromJson(json3, EntradaGarantiasAgrupadas.class);
			RetirarGarantiaAgrupada demo = new RetirarGarantiaAgrupada();
			try {
				SalidaGarantiasAgrupadas salida = demo.logica(con, Objin);
				System.out.println(salida.toString());
				//con.commit();
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}