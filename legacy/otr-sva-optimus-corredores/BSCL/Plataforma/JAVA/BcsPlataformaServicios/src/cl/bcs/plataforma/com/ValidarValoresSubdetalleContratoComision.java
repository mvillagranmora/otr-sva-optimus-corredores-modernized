package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;

public class ValidarValoresSubdetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ValidarValoresSubdetalleContratoComision.class);

	
	public static class EntradaValidarValoresSubdetalleContratoComision extends BcsPojo {
		public Long								p_IdEmpresa;
		public String							p_IdCobro;
		public String 							p_EsPorRangos;
	    public List<SubdetalleContratoComision> p_SubdetallesContratoComision;
	
	    public EntradaValidarValoresSubdetalleContratoComision() {
	        this.p_SubdetallesContratoComision = new ArrayList<SubdetalleContratoComision>();
	    }
	}

	public static class SalidaValidarValoresSubdetalleContratoComision extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarValoresSubdetalleContratoComision() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarValoresSubdetalleContratoComision logica(Connection con, EntradaValidarValoresSubdetalleContratoComision in) throws Exception {
	    logger.info("INICIO::SalidaValidarValoresSubdetalleContratoComision");
	    Long inicio = System.currentTimeMillis();
	    
	    SalidaValidarValoresSubdetalleContratoComision resultado = new SalidaValidarValoresSubdetalleContratoComision();
	    
	    resultado = validarValoresMayorACero(con, in);
	    
	    if(resultado.P_OUT_ERRORESCONTADOR > 0) return resultado; 
	    
	    
	    resultado = validarMontoMaximoDuplicado(con, in);
	    if(resultado.P_OUT_ERRORESCONTADOR > 0) return resultado; 
	
	    Long fin = System.currentTimeMillis();
	    logger.info("FIN::SalidaValidarValoresSubdetalleContratoComision", fin - inicio);
	
	    return resultado;
	}
	
	private SalidaValidarValoresSubdetalleContratoComision validarMontoMaximoDuplicado(Connection con, EntradaValidarValoresSubdetalleContratoComision in) throws Exception {
		logger.trace("INICIO::SalidaValidarValoresSubdetalleContratoComision.validarMontoMaximoDuplicado");
	    Long inicio = System.currentTimeMillis();
	    
	    SalidaValidarValoresSubdetalleContratoComision resultado = new SalidaValidarValoresSubdetalleContratoComision();
	    if(in.p_EsPorRangos.equals("S")) {
	    	if(in.p_SubdetallesContratoComision.size() > 0) {
				Set<BigDecimal> setMontoMaximoUnicos = new HashSet<BigDecimal>();
				List<SubdetalleContratoComision> subdetalleFiltrado = in.p_SubdetallesContratoComision.stream()
																		.filter( d -> setMontoMaximoUnicos.add(d.MontoMaximo))
																		.collect(Collectors.toList());
				
				if(subdetalleFiltrado.size() < in.p_SubdetallesContratoComision.size()) {
					resultado.P_OUT_ERRORESCONTADOR++;
					resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MONTO_MAXIMO_DUPLICADO", new HashMap<String,String>(), con, in.p_IdEmpresa));
				}
			}
	    }
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::SalidaValidarValoresSubdetalleContratoComision.validarMontoMaximoDuplicado", fin - inicio);
		return resultado;
																		
	}
	
	private SalidaValidarValoresSubdetalleContratoComision validarValoresMayorACero(Connection con, EntradaValidarValoresSubdetalleContratoComision in) throws Exception {
		
		logger.trace("INICIO::SalidaValidarValoresSubdetalleContratoComision.validarValoresMayorACero");
	    Long inicio = System.currentTimeMillis();
	    
	    SalidaValidarValoresSubdetalleContratoComision resultado = new SalidaValidarValoresSubdetalleContratoComision();
		if(in.p_SubdetallesContratoComision.size() > 0) {
			List<SubdetalleContratoComision> subdetalleFiltrado = new ArrayList<SubdetalleContratoComision>();
			
			for(SubdetalleContratoComision subdetalle: in.p_SubdetallesContratoComision) {
				
	
				if(!esMontoValido(in.p_IdCobro,subdetalle.Monto)) {
					subdetalleFiltrado.add(subdetalle);
					break;
				}
				
				if(!esPorcentajeValido(in.p_IdCobro, subdetalle.Porcentaje)) {
					subdetalleFiltrado.add(subdetalle);
					break;
				}
					
				if(!esMontoMaximoValido(in.p_EsPorRangos, subdetalle.MontoMaximo)) {
					subdetalleFiltrado.add(subdetalle);
					break;
				}
				
					
			}
			
			if(subdetalleFiltrado.size() > 0) {
				resultado.P_OUT_ERRORESCONTADOR++;
				resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_VALORES_SUBDETALLE", new HashMap<String,String>(), con, in.p_IdEmpresa));

			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::SalidaValidarValoresSubdetalleContratoComision.validarValoresMayorACero", fin - inicio);
		return resultado;
																		
	}
	
	private Boolean esCobroFijo(String IdCobro) {
		if(IdCobro != null) return IdCobro.equals("COFI") || IdCobro.equals("DEFI");
		
		return false;
		
	}
	
	private Boolean esCobroVariable(String IdCobro) {
		if(IdCobro != null) return IdCobro.equals("COVA") || IdCobro.equals("DEVA");
		
		return false;
	}
	
	private Boolean esMontoValido(String IdCobro, BigDecimal monto) {
		logger.info("INICIO::ValidarValoresSubdetalleContratoComision.esMontoValido");
	    Long inicio = System.currentTimeMillis();
	    
	    if(!esCobroFijo(IdCobro)) return true;
		if( monto != null ) {
			if(monto.compareTo(BigDecimal.ZERO) <= 0) return false;
			
			return true;
		}
		
		Long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarValoresSubdetalleContratoComision.esMontoValido", fin - inicio);
		return false;
	
	}
	
	private Boolean esMontoMaximoValido(String esPorRangos,BigDecimal montoMaximo) {
		logger.info("INICIO::ValidarValoresSubdetalleContratoComision.esMontoMaximoValido");
	    Long inicio = System.currentTimeMillis();
	    
	    if(esPorRangos.equals("N")) return true;
	    
		if( montoMaximo != null ) {
			if(montoMaximo.compareTo(BigDecimal.ZERO) <= 0) return false;
			return true;
		}
		Long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarValoresSubdetalleContratoComision.esMontoMaximoValido", fin - inicio);
		return false;
	}
	
	private Boolean esPorcentajeValido(String idCobro, BigDecimal porcentaje) {
		logger.info("INICIO::ValidarValoresSubdetalleContratoComision.esPorcentajeValido");
	    Long inicio = System.currentTimeMillis();
	    
	    if(!esCobroVariable(idCobro)) return true;
	    
		if( porcentaje != null ) {
			if(porcentaje.compareTo(BigDecimal.ZERO) <= 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
				return false;
			}
			return true;
		}
		Long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarValoresSubdetalleContratoComision.esPorcentajeValido", fin - inicio);
		return false;
	}

}
