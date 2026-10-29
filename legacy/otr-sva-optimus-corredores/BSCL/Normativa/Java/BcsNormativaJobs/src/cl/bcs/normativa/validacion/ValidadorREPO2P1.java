package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import cl.bcs.normativa.data.Registro;

/**
 * Implementacion del validador de REPORTE 2.1 .
 *
 * 
 *
 */
public class ValidadorREPO2P1 extends Validador {

	private static final List<String> TIPO_OPERACION = Arrays.asList("01", "02", "03","04");	
	private static final List<String> AFECTA_DERIVADOS = Arrays.asList("S", "N");	
	private static final List<String> MONEDA_ISO = Arrays.asList(
			"AFN","MGA","XUA","THB","PAB","ETB","VEF","BOB","GHS","KES","SOS","TZS","UGX","SVC","CRC","NIO",
			"DKK","ISK","NOK","SEK","CZK","GMD","MKD","DZD","BHD","IQD","JOD","KWD","LYD","RSD","TND","AED",
			"MAD","STD","BSD","BZD","BMD","CAD","BBD","BND","HKD","SBD","KYD","NAD","SGD","SRD","TTD","XCD",
			"USD","USN","FJD","GYD","JMD","LRD","NZD","AUD","ZWL","VND","AMD","CVE","EUR","AWG","ANG","HUF",
			"BIF","XAF","XOF","XPF","KMF","CDF","GNF","RWF","CHF","CHW","DJF","HTG","UAH","PYG","PGK","LAK",
			"HRK","MWK","ZMW","AOA","MMK","GEL","ALL","HNL","SLL","MDL","RON","BGN","SHP","EGP","GBP","GIP",
			"LBP","FKP","SYP","SDG","SSP","SZL","TRY","LSL","AZN","TMT","BAM","MZN","BOV","NGN","ERN","BTN",
			"TWD","ILS","PEN","TOP","MOP","ARS","CLP","COP","CUC","CUP","DOP","PHP","MXN","UYU","UYI","BWP",
			"GTQ","ZAR","BRL","CNY","IRR","OMR","YER","KHR","MYR","QAR","SAR","BYR","RUB","MVR","MUR","SCR",
			"LKR","INR","IDR","NPR","PKR","XDR","KGS","UZS","TJS","XSU","BDT","WST","KZT","MNT","MRO","CLF",
			"MXV","COU","VUV","CHE","KRW","KPW","JPY","PLN");	//168 codigos
	
	private final String rut_corredora;
	private final String nombre_corredora;
	private static final List<String> PAIS = Arrays.asList(
			"ZWE","ZMB","ZAF","YEM","WSM","WLF","VUT","VNM","VIR","VGB","VEN","VCT","VAT","UZB","USA","URY","UMI","UKR",
			"UGA","TZA","TWN","TUV","TUR","TUN","TTO","TON","TLS","TKM","TKL","TJK","THA","TGO","TCD","TCA","SYR","SYC",
			"SXM","SWZ","SWE","SVN","SVK","SUR","STP","SSD","SRB","SPM","SOM","SMR","SLV","SLE","SLB","SJM","SHN","SGS",
			"SGP","SEN","SDN","SAU","RWA","RUS","ROU","REU","QAT","PYF","PSE","PRY","PRT","PRK","PRI","POL","PNG","PLW",
			"PHL","PER","PCN","PAN","PAK","OMN","NZL","NRU","NPL","NOR","NLD","NIU","NIC","NGA","NFK","NER","NCL","NAM",
			"MYT","MYS","MWI","MUS","MTQ","MSR","MRT","MOZ","MNP","MNG","MNE","MMR","MLT","MLI","MKD","MHL","MEX","MDV",
			"MDG","MDA","MCO","MAR","MAF","MAC","LVA","LUX","LTU","LSO","LKA","LIE","LCA","LBY","LBR","LBN","LAO","KWT",
			"KOR","KNA","KIR","KHM","KGZ","KEN","KAZ","JPN","JOR","JEY","JAM","ITA","ISR","ISL","IRQ","IRN","IRL","IOT",
			"IND","IMN","IDN","HUN","HTI","HRV","HND","HMD","HKG","GUY","GUM","GUF","GTM","GRL","GRD","GRC","GNQ","GNB",
			"GMB","GLP","GIN","GIB","GHA","GGY","GEO","GBR","GAB","FSM","FRO","FRA","FLK","FJI","FIN","ETH","EST","ESP",
			"ESH","ERI","EGY","ECU","DZA","DOM","DNK","DMA","DJI","DEU","CZE","CYP","CYM","CXR","CUW","CUB","CRI","CPV",
			"COM","COL","COK","COG","COD","CMR","CIV","CHN","CHL","CHE","CCK","CAN","CAF","BWA","BVT","BTN","BRN","BRB",
			"BRA","BOL","BMU","BLZ","BLR","BLM","BIH","BHS","BHR","BGR","BGD","BFA","BES","BEN","BEL","BDI","AZE","AUT",
			"AUS","ATG","ATF","ATA","ASM","ARM","ARG","ARE","AND","ALB","ALA","AIA","AGO","AFG","ABW"); //249 codigos.

	private static final List<String> PLATAFORMA_NEGOCIACION = Arrays.asList("XBCL","BOVA","XSGO","OTC");
	private static final List<String> EMPRESA_RELACIONADA = Arrays.asList("S", "N");
	
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validacion.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorREPO2P1(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        rut_corredora = contexto.getEmpresa().getRut();
        nombre_corredora = contexto.getEmpresa().getRazonSocial();
    }
    
    /**
     * Implementacion de la regla de validacion.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public static boolean validarFecha(String fecha) {
    	final String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        if (fecha == null || !fecha.matches(regex)) {
            return false;
        }
        try {
            LocalDate f = LocalDate.parse(fecha);

            if (f.isBefore(LocalDate.of(1990, 1, 1))) {
                return false;
            }
            return true; // Fecha valida
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public static boolean validarHora(String hora) {
    	final String regex = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";
        if (hora == null || !hora.matches(regex)) {
            return false;
        }
        try {
            LocalTime.parse(hora);
            return true; // Hora valida
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    private static boolean validarRut(String registro){
    	final String[] rut = registro.split("-");
    	if(rut.length != 2) return false;
    	List<String> numeros = Arrays.asList(rut[0].split(""));
    	String verificador = rut[1];
    	Collections.reverse(numeros);
    	int a = 2;
    	int resultado = 0;
    	for(String numero : numeros){
    		if(numero.isEmpty()) continue;
    		int num = Integer.parseInt(numero);
    		resultado += num * a;
    		if(a == 7){
    			a = 1;
    		}
    		a++;
    	}
    	int resto = resultado % 11;
    	int digito = 11 - resto;
    	String resultadoVerificador = String.valueOf(digito);
    	if(digito == 11) resultadoVerificador = "0";
    	if(digito == 10) resultadoVerificador = "k";
    	return resultadoVerificador.equalsIgnoreCase(verificador);
    }
    
    public boolean validarR001(final Registro registro) {
    	final String tipoRegistro = String.valueOf(registro.getPorNombre("TipoRegistro"));
    	return !tipoRegistro.isEmpty();
    }
    
    public boolean validarR002(final Registro registro) {
    	final String eventoReporte = String.valueOf(registro.getPorNombre("EventoReporte"));
    	return !eventoReporte.isEmpty() && (eventoReporte.length() == 1);
    }
    
    public boolean validarR003(final Registro registro) {
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	return TIPO_OPERACION.contains(tipoOperacion);
    }    
    
    public boolean validarR004(final Registro registro) {
    	final String fechaNegociacion = String.valueOf(registro.getPorNombre("FechaNegociacion"));
    	final String horaNegociacion = String.valueOf(registro.getPorNombre("HoraNegociacion"));
        String fecha = fechaNegociacion.trim(); 
        String hora = horaNegociacion.trim();
        return validarFecha(fecha) && validarHora(hora);
    }
    
    public boolean validarR005(final Registro registro) {
    	final String fechaValutaRecibida = String.valueOf(registro.getPorNombre("FechaValutaMonedaRecibida"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
        String fecha = fechaValutaRecibida.trim();
        if("03".equalsIgnoreCase(tipoOperacion)) {
        	return validarFecha(fecha);
        }
        if(!fecha.isEmpty() && !"0001-01-01".equals(fecha)) {
        	return validarFecha(fecha);
        }
        return true;
    }
    
    public boolean validarR006(final Registro registro) {
    	final String fechaValutaEntregada = String.valueOf(registro.getPorNombre("FechaValutaMonedaEntregada"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
        String fecha = fechaValutaEntregada.trim();
        if("04".equalsIgnoreCase(tipoOperacion)) {
        	return validarFecha(fecha);
        }
        if(!fecha.isEmpty() && !"0001-01-01".equals(fecha)) {
        	return validarFecha(fecha);
        }
        return true;
    }    
    
    public boolean validarR007(final Registro registro) {
    	final String identificador = String.valueOf(registro.getPorNombre("Identificador"));
    	return !identificador.isEmpty();
    }
    
    public boolean validarR008(final Registro registro) {
    	final String rutContraparte1 = String.valueOf(registro.getPorNombre("RUTContraparte1"));
    	return !rutContraparte1.isEmpty() && validarRut(rutContraparte1) && rutContraparte1.equalsIgnoreCase(rut_corredora);
    }
    public boolean validarR009(final Registro registro) {
    	final String lEIContraparte1 = String.valueOf(registro.getPorNombre("LEIContraparte1"));
    	final String rutContraparte1 = String.valueOf(registro.getPorNombre("RUTContraparte1"));
    	if(rutContraparte1.isEmpty()) {
    		return !lEIContraparte1.isEmpty();
    	}
    	return true;
    }
    public boolean validarR010(final Registro registro) {
    	final String nombreContraparte1 = String.valueOf(registro.getPorNombre("NombreContraparte1"));
    	return !nombreContraparte1.isEmpty() && nombreContraparte1.trim().equalsIgnoreCase(nombre_corredora);
    }
    
    public boolean validarR011(final Registro registro) {
    	final String rutContraparte2 = String.valueOf(registro.getPorNombre("RUTContraparte2"));
		final String lEIContraparte2 = String.valueOf(registro.getPorNombre("LEIContraparte2"));
		if(lEIContraparte2.isEmpty()) {
    		return !rutContraparte2.isEmpty() && validarRut(rutContraparte2);
    	}
    	if(!rutContraparte2.isEmpty()) {
    		return validarRut(rutContraparte2);
    	}
    	return true;
    }
    public boolean validarR012(final Registro registro) {
    	final String lEIContraparte2 = String.valueOf(registro.getPorNombre("LEIContraparte2"));
    	final String rutContraparte2 = String.valueOf(registro.getPorNombre("RUTContraparte2"));
    	if(rutContraparte2.isEmpty()) {
    		return !lEIContraparte2.isEmpty();
    	}
    	return true;
    }
    
    public boolean validarR013(final Registro registro) {
    	final String nombreContraparte2 = String.valueOf(registro.getPorNombre("NombreContraparte2"));
    	return !nombreContraparte2.isEmpty();
    }
    
    public boolean validarR014(final Registro registro) {
    	final String paisContraparte2 = String.valueOf(registro.getPorNombre("PaisContraparte2"));
    	return PAIS.contains(paisContraparte2);
    }  
    
    public boolean validarR015(final Registro registro) {
    	final String rUTIntermediario = String.valueOf(registro.getPorNombre("RUTIntermediario"));
    	if(!rUTIntermediario.isEmpty()) {
    		return validarRut(rUTIntermediario);
    	}
    	return true;
    }
    
    public boolean validarR016(final Registro registro) {
    	final String plataformaNegociacion = String.valueOf(registro.getPorNombre("PlataformaNegociacion"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("03".equals(tipoOperacion)) && !("04".equals(tipoOperacion))) {
    		return PLATAFORMA_NEGOCIACION.contains(plataformaNegociacion);
    	}
    	if(!plataformaNegociacion.isEmpty()) {
    		return PLATAFORMA_NEGOCIACION.contains(plataformaNegociacion);
    	}
    	return true;
    }
    
    public boolean validarR017(final Registro registro) {
    	final String codigoOperacionCambio = String.valueOf(registro.getPorNombre("CodigoOperacionCambio"));
    	return !codigoOperacionCambio.isEmpty() || codigoOperacionCambio.length() == 6;
    }
    
    public boolean validarR018(final Registro registro) {
    	final String paisMonedaExtranjera = String.valueOf(registro.getPorNombre("PaisMonedaExtranjera"));
    	return PAIS.contains(paisMonedaExtranjera);
    } 
    
    public boolean validarR019(final Registro registro) {
    	final String monedaCompradaRecibida = String.valueOf(registro.getPorNombre("MonedaCompradaRecibida"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("04".equals(tipoOperacion)) || !monedaCompradaRecibida.isEmpty()) {
    		return MONEDA_ISO.contains(monedaCompradaRecibida);
    	}
    	return true;
    }
    
    public boolean validarR020(final Registro registro) {
    	final Number montoMonedaCompradaRecibida = (Number) registro.getPorNombre("MontoMonedaCompradaRecibida");
    	final long monto = (montoMonedaCompradaRecibida == null ? 0L : montoMonedaCompradaRecibida.longValue());
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("04".equals(tipoOperacion))) {
    		return !(monto <= 0L);
    	}
    	return true;   
    }
    
    public boolean validarR021(final Registro registro) {
    	final String monedaVendidaEntregada = String.valueOf(registro.getPorNombre("MonedaVendidaEntregada"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("03".equals(tipoOperacion)) || !monedaVendidaEntregada.isEmpty()) {
    		return MONEDA_ISO.contains(monedaVendidaEntregada);
    	}
    	return true;
    }
    
    public boolean validarR022(final Registro registro) {
    	final Number montoMonedaVendidaEntregada = (Number) registro.getPorNombre("MontoMonedaVendidaEntregada");
    	final long monto = (montoMonedaVendidaEntregada == null ? 0L : montoMonedaVendidaEntregada.longValue());
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("03".equals(tipoOperacion))) {
    		return !(monto <= 0L);
    	}
    	return true;    	
    }
    
    public boolean validarR023(final Registro registro) {
    	final String convencionTipoCambio = String.valueOf(registro.getPorNombre("ConvencionTipoCambio"));
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	if(!("03".equals(tipoOperacion)) && !("04".equals(tipoOperacion))) {
    		return !convencionTipoCambio.isEmpty();
    	}
    	return true;
    	
    }
    
    public boolean validarR024(final Registro registro) {
    	final Number tipoCambio = (Number) registro.getPorNombre("TipoCambio");
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	final long monto = (tipoCambio == null ? 0L : tipoCambio.longValue());
    	if(!("03".equals(tipoOperacion))) {
    		return !(monto <= 0L);
    	}
    	return true;   
    }
    
    public boolean validarR025(final Registro registro) {
    	final String afectaDerivados = String.valueOf(registro.getPorNombre("AfectaDerivados"));
    	return AFECTA_DERIVADOS.contains(afectaDerivados);
    }
    
    public boolean validarR026(final Registro registro) {
    	final String identificadorContratoDerivados = String.valueOf(registro.getPorNombre("IdentificadorContratoDerivados"));
    	final String afectaDerivados = String.valueOf(registro.getPorNombre("AfectaDerivados"));
    	if("S".equalsIgnoreCase(afectaDerivados)) {
    		return !identificadorContratoDerivados.isEmpty();
    	}
    	return true;
    }
    
    public boolean validarR027(final Registro registro) {
    	final String fechaSuscripcionContrato = String.valueOf(registro.getPorNombre("FechaSuscripcionContrato"));
    	final String horaSuscripcionContrato = String.valueOf(registro.getPorNombre("HoraSuscripcionContrato"));
    	final String afectaDerivados = String.valueOf(registro.getPorNombre("AfectaDerivados"));
    	if("S".equalsIgnoreCase(afectaDerivados)) {
    		return validarFecha(fechaSuscripcionContrato.trim()) && validarHora(horaSuscripcionContrato.trim());
    	}
    	return  true;
    }
    public boolean validarR028(final Registro registro) {
    	final String operacionEmpresaRelacionada = String.valueOf(registro.getPorNombre("OperacionEmpresaRelacionada"));
    	return EMPRESA_RELACIONADA.contains(operacionEmpresaRelacionada);
    }

	public boolean validarR029(final Registro registro) {		
		final List<String> codigos = Arrays.asList("200401", "200402", "200501", "200502");
		final String codigoOperacionCambio = String.valueOf(registro.getPorNombre("CodigoOperacionCambio")); 								//campo 17	
		final String numeroInscripcionObligacionExterior = String.valueOf(registro.getPorNombre("NumeroInscripcionObligacionExterior")); 	//campo 29
		final String codigoOperacionIdentificador = codigoOperacionCambio.substring(0,3);
		final String nombreFinancistaAporante = String.valueOf(registro.getPorNombre("NombreFinancistaAporante"));							//campo 30
		final String lEIFinancistaAporante = String.valueOf(registro.getPorNombre("LEIFinancistaAporante"));								//campo 31
		if("200".equals(codigoOperacionIdentificador)) {
			if(nombreFinancistaAporante.trim().isEmpty() || lEIFinancistaAporante.trim().isEmpty()){
				return false;
			}else{
				if(codigos.contains(codigoOperacionCambio)){
					return !numeroInscripcionObligacionExterior.trim().isEmpty();
				}

			}
    	}
    	return true;
    }

	public boolean validarR030(final Registro registro) {		
		final String codigoOperacionCambio = String.valueOf(registro.getPorNombre("CodigoOperacionCambio"));								//campo 17
		final String codigoOperacionIdentificador = codigoOperacionCambio.substring(0,3);
		final String nombreReceptorInversion = String.valueOf(registro.getPorNombre("NombreReceptorInversion"));							//campo 32
		final String lEIReceptorInversion = String.valueOf(registro.getPorNombre("LEIReceptorInversion"));									//campo 33
		if("100".equals(codigoOperacionIdentificador)) {
    		return !nombreReceptorInversion.trim().isEmpty() && !lEIReceptorInversion.trim().isEmpty();
    	}
    	return true;
    }
    
}