package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1874.
 *
 * 
 *
 */
public class ValidadorDJ1874 extends Validador {

	private static final List<String> TIPO_DECLARANTE = Arrays.asList("1","2","3");
	private static final List<String> TIPO_CONTRIBUYENTE = Arrays.asList("1","2");
	private static final List<String> MONEDA_UNIDAD_REAJUSTE = Arrays.asList("1","2","3","4","5","9");
	

	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1874(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1372.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1372(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	return !c3.isEmpty() && !c4.isEmpty() && !c5.isEmpty() && !c6.isEmpty() && !c7.isEmpty() && !c9.isEmpty() && !c10.isEmpty() && !c10.equals("0");
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1375.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1375(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	return TIPO_CONTRIBUYENTE.contains(c5);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1377.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1377(final Registro registro) {
    	final Date c7 = (Date) registro.getPorCodigoSII("C7");
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	if(c14 > 0){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	String anio = String.valueOf(Integer.parseInt(version));
                String anio_2 = String.valueOf(Integer.parseInt(version)-1);
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
                final Date dateUnico = formatter.parse(anio_2 + "-12-31");
                return (dateInicio.compareTo(c7) < 0 || dateInicio.compareTo(c7) == 0 || dateUnico.compareTo(c7) == 0) && (dateFin.compareTo(c7) > 0 || dateFin.compareTo(c7) == 0 || dateUnico.compareTo(c7) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1378.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1378(final Registro registro) {
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	return MONEDA_UNIDAD_REAJUSTE.contains(c9);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1715.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1715(final Registro registro) {
    	final Date c7 = (Date) registro.getPorCodigoSII("C7");
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
    	final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();
    	/* try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
            if(date.compareTo(c7) != 0) return c14 > 0 || c15 > 0 || c16 > 0 || c17 > 0; */
            /* if(date.compareTo(c7) != 0) return c14 > 0 || c15 > 0 || c16 > 0 || c17 > 0; */
			/* if(date.compareTo(c7) == 0) return c14 > 0 && c15 > 0 && c16 > 0 && c17 > 0; */
		/* }catch(Exception e){
			return false;
		}
    	return true; */

        if(c7 == null) {
    		return false;
    	}
    	return c14 > 0 || c15 > 0 || c16 > 0 || c17 > 0;
    }
    
}