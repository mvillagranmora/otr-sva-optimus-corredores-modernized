package cl.bcs.plataforma.frm;

import cl.bcs.utils.Fecha;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class CampoFormularioUtils {

	static final Logger logger = LogManager.getLogger(CampoFormularioUtils.class);

	public static Boolean noEsValorValido(String valor, String valorDefecto, String codigoCampo) {
		logger.trace("INICIO::CampoFormularioUtils.noEsValorValido");
		long inicio = System.currentTimeMillis();

		if (valorDefecto == null) {
			logger.warn("El campo de codigo " + codigoCampo + " NO tiene valor por defecto asignado");
		}

		Boolean noEsValorValido = valor == null || valor.isEmpty() || valor.equals(valorDefecto);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.noEsValorValido", fin - inicio);
		return noEsValorValido;
	}

	public static Boolean noEsValorValido(Long valor, String valorDefecto, String codigoCampo) {
		logger.trace("INICIO::CampoFormularioUtils.noEsValorValido");
		long inicio = System.currentTimeMillis();

		Long valorDefectoConTipoDato = null;

		try {
			valorDefectoConTipoDato = Long.valueOf(valorDefecto);
		} catch (NumberFormatException e) {
			logger.warn("El campo de codigo " + codigoCampo + " NO tiene valor por defecto valido, debe ser un numero");
		}

		Boolean noEsValorValido = valor == null || valor.equals(valorDefectoConTipoDato);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.noEsValorValido", fin - inicio);
		return noEsValorValido;
	}

	public static Boolean noEsValorValido(java.sql.Date valor, String valorDefecto, String codigoCampo) {
		logger.trace("INICIO::CampoFormularioUtils.noEsValorValido");
		long inicio = System.currentTimeMillis();

		java.sql.Date valorDefectoConTipoDato;

		try {
			valorDefectoConTipoDato = Fecha.toDate(valorDefecto);
		} catch (Exception e) {
			logger.warn("El campo de codigo " + codigoCampo + " NO tiene valor por defecto valido, debe ser un string de formato yyyy-MM-dd");
			valorDefectoConTipoDato = Fecha.toDate("0001-01-01");
		}

		Boolean noEsValorValido = valor == null || valor.compareTo(valorDefectoConTipoDato) == 0;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.noEsValorValido", fin - inicio);
		return noEsValorValido;
	}

	public static Boolean noEsValorValido(java.sql.Timestamp valor, String valorDefecto, String codigoCampo) {
		logger.trace("INICIO::CampoFormularioUtils.noEsValorValido");
		long inicio = System.currentTimeMillis();

		java.sql.Timestamp valorDefectoConTipoDato;

		try {
			valorDefectoConTipoDato = Fecha.toTimeStamp(valorDefecto);
		} catch (Exception e) {
			logger.warn("El campo de codigo " + codigoCampo + " NO tiene valor por defecto valido, debe ser un string de formato yyyy-MM-dd HH:mm:ss");
			valorDefectoConTipoDato = Fecha.toTimeStamp("0001-01-01 00:00:00");
		}

		Boolean noEsValorValido = valor == null || valor.compareTo(valorDefectoConTipoDato) == 0;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.noEsValorValido", fin - inicio);
		return noEsValorValido;
	}
	
	public static Boolean esCampoString(String tipoDato, Field campo) {
		logger.trace("INICIO::CampoFormularioUtils.esCampoString");
		long inicio = System.currentTimeMillis();
		Boolean esCampoString = "TEXT".equals(tipoDato) && campo.getType().isAssignableFrom(String.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.esCampoString", fin - inicio);
		return esCampoString;
	}
	
	public static Boolean esCampoLong(String tipoDato, Field campo) {
		logger.trace("INICIO::CampoFormularioUtils.esCampoLong");
		long inicio = System.currentTimeMillis();
		Boolean esCampoLong = "NUMBER".equals(tipoDato) && campo.getType().isAssignableFrom(Long.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.esCampoLong", fin - inicio);
		return esCampoLong;
	}
	
	public static Boolean esCampoDate(String tipoDato, Field campo) {
		logger.trace("INICIO::CampoFormularioUtils.esCampoDate");
		long inicio = System.currentTimeMillis();
		Boolean esCampoDate = "DATE".equals(tipoDato) && campo.getType().isAssignableFrom(java.sql.Date.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.esCampoDate", fin - inicio);
		return esCampoDate;
	}
	
	public static Boolean esCampoTimestamp(String tipoDato, Field campo) {
		logger.trace("INICIO::CampoFormularioUtils.esCampoTimestamp");
		long inicio = System.currentTimeMillis();
		Boolean esCampoTimestamp = "SECONDDATE".equals(tipoDato) && campo.getType().isAssignableFrom(java.sql.Timestamp.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CampoFormularioUtils.esCampoTimestamp", fin - inicio);
		return esCampoTimestamp;
	}
	

}
