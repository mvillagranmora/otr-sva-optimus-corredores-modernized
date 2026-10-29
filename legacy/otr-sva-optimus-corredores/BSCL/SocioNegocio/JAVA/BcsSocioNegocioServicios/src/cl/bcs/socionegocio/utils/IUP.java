package cl.bcs.socionegocio.utils;

import org.apache.commons.lang.StringUtils;

public class IUP {
	
	public static final int LARGO_IUP = 14;
	
	public enum ConfiguracionDocumentoIdentidad {
	    DNI("02", 8),
		RUC("03", 11),
		PAS("20", 11),
		CDI("00", 8),
		FIC_N("20", 8),
		FIC_M("22", 8),
		FIC_J("22", 8),
		PAI("22", 11),
		RUT("20", 8),
		CEX("20", 8),
		CE("20", 8),
		ERROR("EE", 0);

	    private final String codigoDocumento;
	    private final int largo;

	    ConfiguracionDocumentoIdentidad(String codigoDocumento, int largo) {
	        this.codigoDocumento = codigoDocumento;
	        this.largo = largo;
	    }

	    public String getCodigoDocumento() {
	        return codigoDocumento;
	    }

	    public int getLargo() {
	        return largo;
	    }
	}
	
	public static ConfiguracionDocumentoIdentidad getConfiguracionDocumento(String tipoDocumentoIdentidad, String tipoPersona) {
		ConfiguracionDocumentoIdentidad conf = null;
		switch (tipoDocumentoIdentidad.toUpperCase()) {
		case "DNI":
			conf = ConfiguracionDocumentoIdentidad.DNI;
			break;
		case "RUC":
			conf = ConfiguracionDocumentoIdentidad.RUC;
			break;
		case "PAS":
			conf = ConfiguracionDocumentoIdentidad.PAS;
			break;
		case "CDI":
			conf = ConfiguracionDocumentoIdentidad.CDI;
			break;
		case "FIC":
			if(tipoPersona.equalsIgnoreCase("J")) {
				conf =  ConfiguracionDocumentoIdentidad.FIC_J ;
			}else if(tipoPersona.equalsIgnoreCase("M")) {
				conf =  ConfiguracionDocumentoIdentidad.FIC_M;
			}else if(tipoPersona.equalsIgnoreCase("N")){
				conf =  ConfiguracionDocumentoIdentidad.FIC_N;
			}
			break;
		case "PAI":
			conf = ConfiguracionDocumentoIdentidad.PAI;
			break;
		case "RUT":
		case "CEX":
		case "CE":
			conf = ConfiguracionDocumentoIdentidad.RUT;
			break;
		default:
			conf = ConfiguracionDocumentoIdentidad.ERROR;
		}
		return conf;
	}
	
		
	public static String obtenerDocumentoIdentidadAsociado(String documentoIdentidad, String tipoDocumentoIdentidad, String tipoPersona) {
		ConfiguracionDocumentoIdentidad conf = getConfiguracionDocumento(tipoDocumentoIdentidad, tipoPersona);
		return documentoIdentidad.substring(2, conf.getLargo()+2);
	}
	
	/**
	 * Genera el codigo IUP con las caracteristicas indicadas
	 * @param documentoIdentidad
	 * @param tipoDocumentoIdentidad
	 * @param tipoPersona
	 * @return
	 */
	public static String generar(String documentoIdentidad, String tipoDocumentoIdentidad, String tipoPersona){
		ConfiguracionDocumentoIdentidad conf = getConfiguracionDocumento(tipoDocumentoIdentidad, tipoPersona);
		return StringUtils.rightPad(conf.getCodigoDocumento() + StringUtils.leftPad(documentoIdentidad, conf.getLargo(), "0"),LARGO_IUP,"0");
	}
}
