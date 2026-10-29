package cl.bcs.tesoreria.generarArchivoCargos;

import cl.bcs.tesoreria.ConsultarFormatoArchivo.SalidaFormatoArchivo;
import cl.bcs.tesoreria.ConsultarFormatoArchivo.FormatoArchivoDetalle;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FormatoArchivo {
	private SalidaFormatoArchivo formatoArchivo;	
	
	
	public FormatoArchivo(SalidaFormatoArchivo formatoArchivo) {
		this.formatoArchivo = formatoArchivo;
	}

	private FormatoArchivoDetalle getFormatoArchivoDetalle(int typeRegister, String fieldName) {		
		if(this.formatoArchivo.Detalles.isEmpty())
			return null;
		for(FormatoArchivoDetalle _detalle: this.formatoArchivo.Detalles)			
			if(_detalle.TipoRegistro == typeRegister && _detalle.Campo.equals(fieldName))
				return _detalle;	
		return null;		
	}
	
	private static String stringFormat(String value, Integer length) {
		if (value.length() > length)
			value = value.substring(0, length);		
		return String.format("%1$-"+(length)+"s", value);				
	}
	
	private static String dateFormat(String value, Integer length, String formatDetail) throws Exception {
		
		if(formatDetail == null || formatDetail.equals(""))
			return String.format("%1$-"+length+"s", value);
		SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formateador = new SimpleDateFormat(formatDetail);
		Date date = parseador.parse(value);
		return String.format("%1$-"+length+"s", formateador.format(date));		
	}
	
	private static String numberFormat(String value, Integer length) {		
		return String.format("%1$"+length+"s", value).replace(' ', '0');		
	}
	
	private static String decimalFormat(String value, Integer length, Integer decimals, String fieldName) {
		if (value.indexOf(".") == -1)  // Si valor no contiene "."  Ej. "OE-80" 
			value = "0";		
		Double d = Double.parseDouble(value);		
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols symbol = new DecimalFormatSymbols();
		symbol.setDecimalSeparator('.');		
		df.setGroupingUsed(false);		
		df.setMinimumIntegerDigits(length - decimals);
		df.setMinimumFractionDigits(decimals);
		df.setMaximumIntegerDigits(length - decimals);
		df.setMaximumFractionDigits(decimals);		
		df.setDecimalFormatSymbols(symbol);
		df.setRoundingMode(RoundingMode.DOWN);
		if(fieldName.equals("pac-monto-cargo"))
			return df.format(d).replace(".", "");
		return df.format(d);
	}
	
	private static String initializeValue(String value, String defaultValue) {
		if(value == null)
			value = "";
		if(value.length() < 1 && defaultValue.compareTo("") != 0)
			return defaultValue.trim();
		return value.trim();
	}
	
	public String formatear(Integer tipoRegistro, String nombreCampo, String valor, Boolean rellenarCampo) throws Exception {
		FormatoArchivoDetalle formatoArchivoDetalle = this.getFormatoArchivoDetalle(tipoRegistro, nombreCampo);		
		if(formatoArchivoDetalle == null)
			throw new Exception("NO EXISTE FORMATO DETALLE PARA EL ARCHIVO A CREAR");
		Integer largo = formatoArchivoDetalle.Largo;
		String tipoDato = formatoArchivoDetalle.TipoDato;		
		String valorFormateado = initializeValue(valor, formatoArchivoDetalle.ValorDefecto);		
		if (rellenarCampo == false)
			valorFormateado = valor;
		if(tipoDato.equals("CHAR") && rellenarCampo)
			valorFormateado = stringFormat(valor, largo);		
		if(tipoDato.equals("DATE"))		
			valorFormateado = dateFormat(valor, largo, formatoArchivoDetalle.Formato);		
		if(tipoDato.equals("NUM") && rellenarCampo)
			valorFormateado = numberFormat(valor, largo);		
		if(tipoDato.equals("DEC") && rellenarCampo)
			valorFormateado = decimalFormat(valor, largo, formatoArchivoDetalle.Decimales, nombreCampo);		
		return valorFormateado;
	}
	
	public static void main(String[] args) throws InterruptedException, Throwable{		
		System.out.println("=== BEGIN TEST ===");		
		System.out.println(stringFormat("000", 5).equals("000  "));
		System.out.println(dateFormat("2019-08-05", 8, "yyyyMMdd").equals("20190805"));		
		System.out.println(decimalFormat("00.387", 11, 2, "").equals("000000000.38"));
		System.out.println(numberFormat("1234", 6).equals("001234"));
		System.out.println("=== END TEST ===");		
	}

}
