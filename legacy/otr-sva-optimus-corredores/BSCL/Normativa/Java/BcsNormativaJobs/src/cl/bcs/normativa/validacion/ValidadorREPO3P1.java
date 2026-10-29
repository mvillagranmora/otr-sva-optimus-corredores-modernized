package cl.bcs.normativa.validacion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;

import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.data.Registro;

/**
 * Implementacion del validador de REPORTE 3.1 .
 *
 * 
 *
 */
public class ValidadorREPO3P1 extends Validador {

	private static final List<String> TIPO_OPERACION = Arrays.asList("1", "2");	
	private static final List<String> EVENTO_REPORTE = Arrays.asList("1", "2","3");
	private final String rut_corredora;
	private static final List<String> PLATAFORMA_NEGOCIACION = Arrays.asList("XBCL","BOVA","XSGO","OTC");
    private static String hora_hasta;
	
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validacion.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorREPO3P1(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        rut_corredora = contexto.getEmpresa().getRut();
        hora_hasta = ConfiguracionDAO.buscarPorEmpresaMasCodigo(contexto.getConnection(), contexto.getSchema(), contexto.getEmpresa().getId(), "BCC_REPO3P1_HORAHASTA");
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
    	final String regex = "^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
    	String horaLimite = hora_hasta;
    	if (hora == null || !hora.matches(regex)) {
            return false;
        }
    	if (horaLimite == null || horaLimite.isEmpty() || !horaLimite.matches(regex)) {
            horaLimite = "16:00:00";
        }        
        try {
        	
        	LocalTime t = LocalTime.parse(hora);
            // Límite permitido: 16:00:00 solo para reporte 3.1
            LocalTime limite = LocalTime.parse(horaLimite);

            if (t.isAfter(limite)) {
                return false;
            }
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
    	final String rutEntidadMCF = String.valueOf(registro.getPorNombre("RUTReportante"));
    	return !rutEntidadMCF.isEmpty() && validarRut(rutEntidadMCF) && rutEntidadMCF.equalsIgnoreCase(rut_corredora);
    }    
    
    public boolean validarR003(final Registro registro) {
    	final String fecha = String.valueOf(registro.getPorNombre("FechaOperacion")).trim();
        return validarFecha(fecha);
    }
    
    public boolean validarR004(final Registro registro) {
    	final String hora = String.valueOf(registro.getPorNombre("HoraOperacion")).trim();
        return validarHora(hora);
    }
    
    public boolean validarR005(final Registro registro) {
    	final String tipoOperacion = String.valueOf(registro.getPorNombre("TipoOperacion"));
    	return TIPO_OPERACION.contains(tipoOperacion);
    }
    
    public boolean validarR006(final Registro registro) {
    	final String eventoReporte = String.valueOf(registro.getPorNombre("EventoReporte"));
    	return EVENTO_REPORTE.contains(eventoReporte);
    }
    
    public boolean validarR007(final Registro registro) {
    	final String identificador = String.valueOf(registro.getPorNombre("Identificador"));
    	return !identificador.isEmpty();
    }
    
    public boolean validarR008(final Registro registro) {
    	final String lEIContraparte = String.valueOf(registro.getPorNombre("LEIContraparte"));
    	final String rutContraparte = String.valueOf(registro.getPorNombre("RUTContraparte"));    	
    	if(lEIContraparte.isEmpty()) {
    		return !rutContraparte.isEmpty() && validarRut(rutContraparte);
    	}
    	if(!rutContraparte.isEmpty()) {
    		return validarRut(rutContraparte);
    	}
    	return true;
    }
    
    public boolean validarR009(final Registro registro) {
    	final String lEIContraparte = String.valueOf(registro.getPorNombre("LEIContraparte"));
    	final String rutContraparte = String.valueOf(registro.getPorNombre("RUTContraparte"));
    	if(rutContraparte.isEmpty()) {
    		return !lEIContraparte.isEmpty();
    	}
    	return true;
    }
    
    public boolean validarR010(final Registro registro) {
    	final String nombreContraparte = String.valueOf(registro.getPorNombre("NombreContraparte"));
    	return !nombreContraparte.isEmpty();
    }
    public boolean validarR016(final Registro registro) {
    	final String plataformaNegociacion = String.valueOf(registro.getPorNombre("PlataformaNegociacion"));
    	return PLATAFORMA_NEGOCIACION.contains(plataformaNegociacion);
    }
    
    public boolean validarR011(final Registro registro) {
    	final Number montoMonedaComprada = (Number) registro.getPorNombre("MontoMonedaComprada");
    	final long monto = (montoMonedaComprada == null ? 0L : montoMonedaComprada.longValue());
    	return !(monto <= 0L);  
    }
    
    public boolean validarR012(final Registro registro) {
    	final Number montoMonedaVendida = (Number) registro.getPorNombre("MontoMonedaVendida");
    	final long monto = (montoMonedaVendida == null ? 0L : montoMonedaVendida.longValue());
    	return !(monto <= 0L);  
    }
    
    public boolean validarR013(final Registro registro) {   	
    	Object tipoCambio = registro.getPorNombre("TipoCambio");
    	if (tipoCambio == null) return false;
    	BigDecimal monto = new BigDecimal(tipoCambio.toString());
        return monto.compareTo(BigDecimal.ZERO) > 0;
    }
    
}