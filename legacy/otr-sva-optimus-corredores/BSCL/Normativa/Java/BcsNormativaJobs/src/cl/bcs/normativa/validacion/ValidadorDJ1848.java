package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1848.
 *
 * 
 *
 */
public class ValidadorDJ1848 extends Validador {

	private static final List<String> MODALIDAD_ACTUACION = Arrays.asList("1","2");
	private static final List<String> TIPO_INSTRUMENTO_CONTRATO = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14");
	private static final List<String> TIPO_OPERACION_SITUACION_INVERSIONISTA = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11");
	private static final List<String> MONEDA = Arrays.asList("143","161","134","4","153","51","96","146","113","135","139","36"
			,"6","162","13","163","97","136","154","127","142","147","156","82","23","164","165","102","149","138","166","24","1","999"
			,"129","167","150","132","26","128","5","152","155","137","151","169","144","921","72","48","145","900","981","982","980","141",
			"2","998");
	private static final List<String> TIPO_OPERACION_SITUACION_INVERSIONISTA_H1393 = Arrays.asList("6","7","8","9","10","11");
	private static final List<String> TIPO_OPERACION_SITUACION_INVERSIONISTA_H1396 = Arrays.asList("1","2","3","4","5");
	private static final List<String> TIPO_INVERSIONISTA_ART_106_LIR = Arrays.asList("1","2","3","4","5","6","7","8","9");
	private static final List<String> SI_NO = Arrays.asList("SI","NO");
	private static final List<String> LISTA_C33 = Arrays.asList("1","2","3","4","5","6","7","8","9","10");
	private static final List<String> LISTA_C33_H1410 = Arrays.asList("1","2","6");
	private static final List<String> PAISES = Arrays.asList("140","127","150","113","161","141","129","149","130","118","143","144","107",
			"155","124","163","139","145","102","108","104","103","147","137","114","106","125","120","115","133","128","119","134","121",
			"159","131","111","148","142","146","101","156","105","138","112","154","162","166","158","123","122","135","109","126","136",
			"117","116","151","152","153","165","240","224","207","204","236","221","220","226","997","202","211","209","231","218","213",
			"225","232","215","217","208","214","205","216","212","210","222","219","206","241","234","233","235","203","223","201","251",
			"252","253","254","255","256","257","258","230","282","281","261","262","263","264","271","272","308","302","541","313","321",
			"318","344","315","336","334","333","341","335","317","328","309","307","306","331","301","551","552","303","316","311","329",
			"327","337","326","320","304","324","312","332","310","314","319","557","558","560","325","322","342","345","330","406","401",
			"164","418","416","417","402","405","420","412","404","403","419","415","431","350","432","433","434","441","442","443","444",
			"445","446","447","448","449","450","461","462","463","464","471","472","473","518","563","525","540","509","542","514","543",
			"527","305","524","547","507","545","548","517","549","512","505","550","520","530","506","516","504","553","534","554","532",
			"555","523","556","535","594","513","515","528","501","581","582","510","584","544","519","562","536","595","511","508","522",
			"559","591","592","593","600");

	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1848(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1230.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1230(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
    	if(!c2.isEmpty()) return MODALIDAD_ACTUACION.contains(c23);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1231.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1231(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	if(!c2.isEmpty()) return TIPO_INSTRUMENTO_CONTRATO.contains(c3);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1232.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1232(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	if(!c2.isEmpty()) return TIPO_OPERACION_SITUACION_INVERSIONISTA.contains(c4);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1233.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1233(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final Date c5 = (Date) registro.getPorCodigoSII("C5");
    	if(!c2.isEmpty()){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	String anio = String.valueOf(Integer.parseInt(version)-1);
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
    			return (dateInicio.compareTo(c5) < 0 || dateInicio.compareTo(c5) == 0) && (dateFin.compareTo(c5) > 0 || dateFin.compareTo(c5) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	};
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1234.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1234(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
    	if(!c2.isEmpty() && "11".equalsIgnoreCase(c4) && "999".equalsIgnoreCase(c7)) return c6 > 0 && c8 > 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1235.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1235(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	if(!c2.isEmpty()) return MONEDA.contains(c7);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1382.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1382(final Registro registro) {
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	if(c12 > 0) return !c10.isEmpty() && !c11.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1393.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1393(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
    	final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
    	
    	if(TIPO_OPERACION_SITUACION_INVERSIONISTA_H1393.contains(c4)) 
    		return (c9.isEmpty() || "0".equalsIgnoreCase(c9)) && (c10.isEmpty() || "0".equalsIgnoreCase(c10)) && (c11.isEmpty() || "0".equalsIgnoreCase(c11))
    				&& (c24.isEmpty() || "0".equalsIgnoreCase(c24)) && (c12.isEmpty() || "0".equalsIgnoreCase(c12)) && (c13.isEmpty() || "0".equalsIgnoreCase(c13))
    				&& (c33.isEmpty() || "0".equalsIgnoreCase(c33)) && (c34.isEmpty() || "0".equalsIgnoreCase(c34)) && (c35.isEmpty() || "0".equalsIgnoreCase(c35));
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1394.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1394(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	
    	if("3".equalsIgnoreCase(c4) || "4".equalsIgnoreCase(c4)) 
    		return "11".equalsIgnoreCase(c3);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1395.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1395(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	
    	if("6".equalsIgnoreCase(c4) || "11".equalsIgnoreCase(c4)) 
    		return "14".equalsIgnoreCase(c3);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1396.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1396(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	
    	if("14".equalsIgnoreCase(c3)) 
    		return !TIPO_OPERACION_SITUACION_INVERSIONISTA_H1396.contains(c4);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1397.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1397(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	
    	if("7".equalsIgnoreCase(c4) || "8".equalsIgnoreCase(c4)) 
    		return "10".equalsIgnoreCase(c3);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1398.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1398(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
    	
    	if("11".equalsIgnoreCase(c4)) 
    		return !c2.isEmpty() && !c23.isEmpty() && !c5.isEmpty() && !c26.isEmpty() && !c27.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1399.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1399(final Registro registro) {
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	
    	if("999".equalsIgnoreCase(c7)) 
    		return !c6.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1400.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1400(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
    	final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
    	
    	if("6".equalsIgnoreCase(c4)) 
    		return c12 > 0 && (c8.isEmpty() || "0".equalsIgnoreCase(c8)) && (c7.isEmpty() || "0".equalsIgnoreCase(c7)) && (c6.isEmpty() || "0".equalsIgnoreCase(c6))
    				&& (c9.isEmpty() || "0".equalsIgnoreCase(c9)) && (c10.isEmpty() || "0".equalsIgnoreCase(c10)) && (c11.isEmpty() || "0".equalsIgnoreCase(c11)) && (c24.isEmpty() || "0".equalsIgnoreCase(c24))
    				&& (c13.isEmpty() || "0".equalsIgnoreCase(c13)) && (c33.isEmpty() || "0".equalsIgnoreCase(c33)) && (c34.isEmpty() || "0".equalsIgnoreCase(c34)) && (c35.isEmpty() || "0".equalsIgnoreCase(c35));
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1401.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1401(final Registro registro) {
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
    	if(TIPO_INVERSIONISTA_ART_106_LIR.contains(c26))
    		return !c27.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1402.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1402(final Registro registro) {
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
    	if(c26.isEmpty())
    		return (c27.isEmpty() || "0".equalsIgnoreCase(c27));
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1403.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1403(final Registro registro) {
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final Date c27 = (Date) registro.getPorCodigoSII("C27");
    	if(!c26.isEmpty()){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	String anio = String.valueOf(Integer.parseInt(version)-1);
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
    			return (dateInicio.compareTo(c27) < 0 || dateInicio.compareTo(c27) == 0) && (dateFin.compareTo(c27) > 0 || dateFin.compareTo(c27) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return true;
    }
            
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1406.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1406(final Registro registro) {
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	if(c35 > 0) return LISTA_C33.contains(c33);
    	
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1407.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1407(final Registro registro) {
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
    	if("10".equalsIgnoreCase(c33)) return !c34.isEmpty();
    	
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1408.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1408(final Registro registro) {
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
    	if("5".equalsIgnoreCase(c33)) return c24 > 0 && c35 > 0;
    	
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1410.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1410(final Registro registro) {
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C27"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	if(LISTA_C33_H1410.contains(c33)) 
    		return (c9.isEmpty() || "0".equalsIgnoreCase(c9)) && (c10.isEmpty() || "0".equalsIgnoreCase(c10)) && (c11.isEmpty() || "0".equalsIgnoreCase(c11))
    				&& (c24.isEmpty() || "0".equalsIgnoreCase(c24)) && (c12.isEmpty() || "0".equalsIgnoreCase(c12)) && (c13.isEmpty() || "0".equalsIgnoreCase(c13));
    	
    	return true;
    }
    
}