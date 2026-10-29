package cl.bcs.normativa.validacion;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cl.bcs.normativa.data.Registro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.Fecha;


/**
 * Implementaci&oacute;n del validador de la DJ1889.
 *
 * 
 *
 */
public class ValidadorDJ1889 extends Validador {
	private static Connection cnn;
    private static Long idEmpresa;
    private static Double p23;
    private static Double p214;    
	private static final List<String> TIPO_AHORRO = Arrays.asList("1","2");	
	private final String version;
	
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1889(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        cnn = contexto.getConnection();
        idEmpresa = contexto.getEmpresa().getId();    
        
    }
    
    
   public Double getParametroSistema(String parametro) throws Exception {
    	String parametroStr = ConfiguracionSistema.getParametroConfiguracionString(cnn, idEmpresa, "NORMATIVA", parametro);
    	Double parametroDouble = Double.parseDouble(parametroStr);
        return parametroDouble;    
    }
    
    public boolean validarH0(final Registro registro) {
    	// Debe haber ejecutado el complemento de actualizacion montos segun ipc
    	final String ComplementoEjecutado = registro.get("ComplementoIPC").toString();
    	
    	return ComplementoEjecutado != null && "S".equals(ComplementoEjecutado);
    }

    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH003(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	
    	if(!c3.isEmpty()){
    		try{
    			return (c3 != version);
    		}catch(Exception e){
    			return false;
    		}
    	};
    	return true;
    }
    
    
    public boolean validarH111(final Registro registro) throws Exception {
    	
    	p23 = getParametroSistema("P23").doubleValue();
    	p214 = getParametroSistema("P214").doubleValue();
    	
        final Double RentabilidadPositiva = ((Number) registro.getPorCodigoSII("C5")).doubleValue();
        final Double RentabilidadActualizadaPositiva = ((Number) registro.getPorCodigoSII("C7")).doubleValue();
		return(RentabilidadActualizadaPositiva <= Math.round((RentabilidadPositiva * p23) + p214));
    }    

    public boolean validarH112(final Registro registro)  throws Exception {
    	p23 = getParametroSistema("P23").doubleValue();
    	p214 = getParametroSistema("P214").doubleValue();

        final Double RentabilidadNegativa = ((Number) registro.getPorCodigoSII("C6")).doubleValue();
        final Double RentabilidadActualizadaNegativa = ((Number) registro.getPorCodigoSII("C8")).doubleValue();
		return(RentabilidadActualizadaNegativa <= Math.round((RentabilidadNegativa * p23) + p214));
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1286.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1286(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	if(c3.isEmpty()){
    		return !c3.isEmpty();
    	}else{
    		return TIPO_AHORRO.contains(c3);
    	}
    }
   
    
    /**
     * Implementacion de la regla de validacion H017.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH017(final Registro registro) {
    	//final Double C39 = ((Number) registro.getPorCodigoSII("C39")).doubleValue();
    	final Double AhorrosTrabajadorActualizado = ((Number) registro.getPorCodigoSII("C4")).doubleValue();
    	//final Double AhorrosTrabajadorActualizado = ((Number) registro.get("AhorrosTrabajadorActualizado")).doubleValue();
        final Double AhorrosDepositoActualizado = ((Number) registro.getPorCodigoSII("C5")).doubleValue();
        //final Double AhorrosDepositoActualizado = ((Number) registro.get("AhorrosDepositoActualizado")).doubleValue();
        final Double AhorrosCotizacionesActualizado = ((Number) registro.getPorCodigoSII("C6")).doubleValue();
        //final Double AhorrosCotizacionesActualizado = ((Number) registro.get("AhorrosCotizacionesActualizado")).doubleValue();
        final Double RetirosTrabajadorActualizado = ((Number) registro.getPorCodigoSII("C7")).doubleValue();
        //final Double RetirosTrabajadorActualizado = ((Number) registro.get("RetirosTrabajadorActualizado")).doubleValue();
        final Double RetirosDepositoActualizado = ((Number) registro.getPorCodigoSII("C8")).doubleValue();
        //final Double RetirosDepositoActualizado = ((Number) registro.get("RetirosDepositoActualizado")).doubleValue();
        final Double RetirosCotizacionesActualizado = ((Number) registro.getPorCodigoSII("C9")).doubleValue();
        //final Double RetirosCotizacionesActualizado = ((Number) registro.get("RetirosCotizacionesActualizado")).doubleValue();
        
        return (AhorrosTrabajadorActualizado != null) || (AhorrosTrabajadorActualizado > 0) ||
        		(AhorrosDepositoActualizado != null) || (AhorrosDepositoActualizado > 0) ||
        		(AhorrosCotizacionesActualizado != null) || (AhorrosCotizacionesActualizado > 0) ||
        		(RetirosTrabajadorActualizado != null) || (RetirosTrabajadorActualizado > 0) ||
        		(RetirosDepositoActualizado != null) || (RetirosDepositoActualizado > 0) ||
        		(RetirosCotizacionesActualizado != null) || (RetirosCotizacionesActualizado > 0);
    }
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH022(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	
    	return c2 != "55555555-5" && c2 != "66666666-6";

    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1403.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH0000(final Registro registro) {
    	final String sc98 = String.valueOf(registro.getPorCodigoSII("C98"));
    	final Date dc98 = (Date) registro.getPorCodigoSII("C98");
    	if(!sc98.isEmpty()){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	String anio = String.valueOf(Integer.parseInt(version)-1);
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
    			return (dateInicio.compareTo(dc98) < 0 || dateInicio.compareTo(dc98) == 0) && (dateFin.compareTo(dc98) > 0 || dateFin.compareTo(dc98) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return true;
    }

}