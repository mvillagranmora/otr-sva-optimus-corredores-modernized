package cl.bcs.normativa.validacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import cl.bcs.normativa.data.Registro;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosSalidaServicio;

/**
 * Implementacion del validador de REPORTE 2.2 .
 *
 * 
 *
 */
public class ValidadorREPO2P2 extends Validador {
	
	private static final List<String> MONEDA_ISO = Arrays.asList("AFN","MGA","XUA","THB","PAB","ETB","VEF","BOB","GHS","KES","SOS","TZS","UGX","SVC","CRC","NIO",
			"DKK","ISK","NOK","SEK","CZK","GMD","MKD","DZD","BHD","IQD","JOD","KWD","LYD","RSD","TND","AED",
			"MAD","STD","BSD","BZD","BMD","CAD","BBD","BND","HKD","SBD","KYD","NAD","SGD","SRD","TTD","XCD",
			"USD","USN","FJD","GYD","JMD","LRD","NZD","AUD","ZWL","VND","AMD","CVE","EUR","AWG","ANG","HUF",
			"BIF","XAF","XOF","XPF","KMF","CDF","GNF","RWF","CHF","CHW","DJF","HTG","UAH","PYG","PGK","LAK",
			"HRK","MWK","ZMW","AOA","MMK","GEL","ALL","HNL","SLL","MDL","RON","BGN","SHP","EGP","GBP","GIP",
			"LBP","FKP","SYP","SDG","SSP","SZL","TRY","LSL","AZN","TMT","BAM","MZN","BOV","NGN","ERN","BTN",
			"TWD","ILS","PEN","TOP","MOP","ARS","CLP","COP","CUC","CUP","DOP","PHP","MXN","UYU","UYI","BWP",
			"GTQ","ZAR","BRL","CNY","IRR","OMR","YER","KHR","MYR","QAR","SAR","BYR","RUB","MVR","MUR","SCR",
			"LKR","INR","IDR","NPR","PKR","XDR","KGS","UZS","TJS","XSU","BDT","WST","KZT","MNT","MRO","CLF",
			"MXV","COU","VUV","CHE","KRW","KPW","JPY","PLN");
	
	private final String rut_corredora;
	private static long empresa_id;
	private static Connection conexion;
	
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validacion.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorREPO2P2(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        rut_corredora = contexto.getEmpresa().getRut();
        empresa_id = contexto.getEmpresa().getId();
        conexion = contexto.getConnection();
    }
    
    /**
     * Implementacion de la regla de validacion.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
        
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
            return esUltimoDiaHabil(f);
        } catch (DateTimeParseException e) {
            return false;
        } catch (Exception e) {
			return false;
		}
    }
        
    public static boolean esUltimoDiaHabil(LocalDate fecha) throws Exception {
        LocalDate ultimoDiaMes = fecha.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate dia = ultimoDiaMes;
        while (!esDiaHabil(dia)) {
            dia = dia.minusDays(1);
        }

        return fecha.equals(dia);
	}
    
    public static boolean esDiaHabil(LocalDate fecha) throws Exception {
        ObtenerFeriado obtenerFeriadoService = new ObtenerFeriado();
        ParametrosEntradaServicio inObtenerFeriado = new ParametrosEntradaServicio();
        inObtenerFeriado.fechaConsulta = fecha.toString();
        inObtenerFeriado.IdEmpresa = empresa_id;

        ParametrosSalidaServicio out = obtenerFeriadoService.logica(conexion, inObtenerFeriado);
        return out.habil;
    }
    
    public boolean validarR001(final Registro registro) {
    	final String tipoRegistro = String.valueOf(registro.getPorNombre("TipoRegistro"));
    	return !tipoRegistro.isEmpty();
    }
    
    public boolean validarR002(final Registro registro) {
    	final String rutEntidadMCF = String.valueOf(registro.getPorNombre("RUTEntidadMCF"));
    	return !rutEntidadMCF.isEmpty() && validarRut(rutEntidadMCF) && rutEntidadMCF.equalsIgnoreCase(rut_corredora);
    }    
    
    public boolean validarR003(final Registro registro) {
    	final String fecha = String.valueOf(registro.getPorNombre("Fecha")).trim();
        return validarFecha(fecha);
    }
    
    public boolean validarR004(final Registro registro) {
    	final Number saldoCuentaCambioConversion = (Number) registro.getPorNombre("SaldoCuentaCambioConversion");
    	return saldoCuentaCambioConversion != null; 
    }
    
    public boolean validarR005(final Registro registro) {
    	final Number saldoMonedaOriginal = (Number) registro.getPorNombre("SaldoMonedaOriginal");
    	return saldoMonedaOriginal != null; 
    }
    
    public boolean validarR006(final Registro registro) {
    	final String monedaSaldo = String.valueOf(registro.getPorNombre("MonedaSaldo"));
    	return MONEDA_ISO.contains(monedaSaldo);
    }
    
    public boolean validarR007(final Registro registro) {
    	Object paridad = registro.getPorNombre("Paridad");
        if (paridad == null) return false;
    	BigDecimal monto = new BigDecimal(paridad.toString());
        return monto.compareTo(BigDecimal.ZERO) > 0;
    }
    
    
}