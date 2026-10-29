package cl.bcs.plataforma.pla.pojo;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;

public class ValorAntiguo extends BcsPojo {
	public String Valor;
	public String TipoDato;
	
	public static <T> ValorAntiguo obtenerValorAntiguo(T objeto, Class<T> tipo, String campoModificado) throws Exception {

		ValorAntiguo valorAntiguo = new ValorAntiguo();
		
		Field campo = objeto.getClass().getField(campoModificado);
		
		if(campo.getType().equals(String.class)) {
			valorAntiguo.Valor = (String) campo.get(objeto);
			valorAntiguo.TipoDato = "NVARCHAR";
			return valorAntiguo;
		}
		if(campo.getType().equals(Long.class)) {
			valorAntiguo.Valor = String.valueOf((Long) campo.get(objeto));
			valorAntiguo.TipoDato = "BIGINT";
			return valorAntiguo;
		}
		if(campo.getType().equals(Integer.class)) {
			valorAntiguo.Valor = String.valueOf((Integer) campo.get(objeto));
			valorAntiguo.TipoDato = "INT";
			return valorAntiguo;
		}
		if(campo.getType().equals(BigDecimal.class)) {
			valorAntiguo.Valor = String.valueOf((BigDecimal) campo.get(objeto));
			valorAntiguo.TipoDato = "DECIMAL";
			return valorAntiguo;
		}
		if(campo.getType().equals(java.sql.Timestamp.class)) {
			valorAntiguo.Valor = Fecha.toStringDate((java.sql.Timestamp) campo.get(objeto));
			valorAntiguo.TipoDato = "SECONDDATE";
			return valorAntiguo;
		}
		if(campo.getType().equals(java.sql.Date.class)) {
			valorAntiguo.Valor = Fecha.toStringDate((java.sql.Date) campo.get(objeto));
			valorAntiguo.TipoDato = "DATE";
			return valorAntiguo;
		}
		
		return valorAntiguo;
	}
	
	public static <T> T asignarValorNuevo(T objeto, String campoModificado, String valorNuevo) throws Exception {
		Field campo = objeto.getClass().getField(campoModificado);
		if(campo.getType().equals(String.class)) {
			campo.set(objeto, valorNuevo);
		}
		if(campo.getType().equals(Long.class)) {
			campo.set(objeto, Long.parseLong(valorNuevo));
		}
		if(campo.getType().equals(Integer.class)) {
			campo.set(objeto, Integer.parseInt(valorNuevo));
		}
		if(campo.getType().equals(java.sql.Timestamp.class)) {
			campo.set(objeto, Fecha.toDateTime(valorNuevo));
		}
		if(campo.getType().equals(java.sql.Date.class)) {
			campo.set(objeto, Fecha.toDate(valorNuevo));
		}
		if(campo.getType().equals(BigDecimal.class)) {
			campo.set(objeto, new BigDecimal(valorNuevo));
		}
		return objeto;
	}
}