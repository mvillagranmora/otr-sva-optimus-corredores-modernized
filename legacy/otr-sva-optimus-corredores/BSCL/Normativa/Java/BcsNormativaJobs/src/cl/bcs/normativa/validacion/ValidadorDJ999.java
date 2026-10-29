package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cl.bcs.utils.SQL;
import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ999.
 *
 * 
 *
 */
public class ValidadorDJ999 extends Validador {
	private static final Logger LOGGER = Logger.getLogger(ValidadorDJ999.class.getName());
	private final String SELECT_CODIGO_COMERCIO = "SELECT \"IdPlanillaBancoCentral\" FROM \"BSCL.Mesa.BD::MES.PlanillaBancoCentral\";";
	private final String SELECT_CODIGO_ADUANA = "SELECT \"Codigo\" FROM \"BSCL.Normativa.BD::DOC.Diccionario\" WHERE \"Tipo\" = 'DJ999_CodigoAduana';";
	private final String SELECT_CODIGO_MONEDA = "SELECT \"Codigo\" FROM \"BSCL.Normativa.BD::DOC.Diccionario\" WHERE \"Tipo\" = 'DJ999_CodigoMoneda';";
	private final String SELECT_SECTOR_ECONOMICO = "SELECT \"Codigo\" FROM \"BSCL.Normativa.BD::DOC.Diccionario\" WHERE \"Tipo\" = 'DJ999_SectorEconomico';";
	
    private static final List<String> EVENTO_REPORTE = Arrays.asList("N", "M", "E");
    private static final List<Long> CODIGO_INSTITUCION = Arrays.asList(85L, 101L, 111L, 113L, 117L, 118L, 121L, 125L);
    private static final List<Long> TIPO_DOCUMENTO = Arrays.asList(1L, 2L);
    private static final List<Long> AFECTO_A_DERIVADO = Arrays.asList(0L, 1L);
    private static final List<String> TIPO_AUTORIZACION = Arrays.asList("CE", "AC", "IN","AE","EE","RP");
    
    private static final List<String> INFORMA_REGISTRO_20 = Arrays.asList("11200","11201","11210","11211","11215","11216","11220","11221","11225","11226","11230","11231",
    		"11240","11241","11242","11245","11246","11250","11251","11260","11261","11270","11271","11280","11281","11290","21200","21210","21215","21220","21225","21230",
    		"21240","21245","21250","21260","21270","21280","21290","21295","11400","11403","11405","11410","11415","11420","11450","11460","11465","11470","11475","11480",
    		"11485","11490","11495","21400","21401","21402","21403","21405","21406","21410","21411","21415","21420","21421","21450","21451","21460","21461","21465","21466",
    		"21470","21471","21475","21476","21480","21481","21485","21486","21490","21491","21498","21499");
    
    private static final List<String> INFORMA_REGISTRO_20_ARB =  Arrays.asList("11200","11201","11210","11211","11215","11216","11220","11221","11225","11226","11230","11231",
    		"11240","11241","11242","11245","11246","11250","11251","11260","11261","11270","11271","11280","11281","11290","21200","21210","21215","21220","21225","21230",
    		"21240","21245","21250","21260","21270","21280","21290","21295","11400","11403","11405","11410","11415","11420","11450","11460","11465","11470","11475","11480",
    		"11485","11490","11495","21400","21401","21402","21403","21405","21406","21410","21411","21415","21420","21421","21450","21451","21460","21461","21465","21466",
    		"21470","21471","21475","21476","21480","21481","21485","21486","21490","21491","21498","21499","10000","10002","10005","10007","10008","10020","10024","10025",
    		"10028","10040","10050","10051","10052","10053","10055","10057","10060","10070","10075","10080","10081","10090","10096","20000","20002","20005","20007","20008",
    		"20020","20023","20024","20025","20026","20028","20040","20050","20051","20052","20053","20055","20057","20060","20070","20075","20077","20080","20081","20090",
    		"20096");
    
    private static final List<String> INFORMA_REGISTRO_60 = Arrays.asList("10080","20080","10090");
    
    private static final List<String> CODIGO_COMERCIO_EE = Arrays.asList("21100","21101","21485","21486");
    
	private static Connection cnn;
	
    public static class IdPlanillaBancoCentral {
		public String IdPlanillaBancoCentral;
	}
    
    public static class Diccionario {
		public String Codigo;
	}
  
    
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ999(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        //version = contexto.getEsquema().getVersion();
        cnn = contexto.getConnection();
    }
      
    //Evento de reporte no corresponde a uno válido
    public boolean validarV001(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	
    	return c2 != null && EVENTO_REPORTE.contains(c2);
    }
    
    //No es un código institución válido
    public boolean validarV002(final Registro registro) {
    	final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
    	
    	return c3 != 0 && CODIGO_INSTITUCION.contains(c3);
    }
    
    //No es un folio planilla válido
    public boolean validarV003(final Registro registro) {
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	
    	return c4 > 0;
    }
    
    //No es un tipo de documento válido
    public boolean validarV004(final Registro registro) {
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	
    	return c6 != 0 && TIPO_DOCUMENTO.contains(c6);
    }
    
    
    //No es un código de comercio válido
    public boolean validarV005(final Registro registro) {
    	SQL sql = new SQL();

    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    
    	List<IdPlanillaBancoCentral> planillasBancoCentral = new ArrayList<IdPlanillaBancoCentral>();
		
    	try {
			planillasBancoCentral = sql.query(cnn, SELECT_CODIGO_COMERCIO, new HashMap<>(), IdPlanillaBancoCentral.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    	List<String> listaCodigosComercio = planillasBancoCentral.parallelStream().map(s -> s.IdPlanillaBancoCentral).collect(Collectors.toList());
    	
    	LOGGER.info("Codigo de comercio: " + c7);
    	
    	
    	if(!listaCodigosComercio.isEmpty()) {
    		LOGGER.info("Entro a validación");
    		return listaCodigosComercio.contains(c7);
    	}
    	
    	return false;
    }
    
    //No es un país válido
    public boolean validarV006(final Registro registro) {
    	SQL sql = new SQL();

    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    
    	List<Diccionario> codigosAduana = new ArrayList<Diccionario>();
		
    	try {
    		codigosAduana = sql.query(cnn, SELECT_CODIGO_ADUANA, new HashMap<>(), Diccionario.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    	List<String> listaCodigosAduana = codigosAduana.parallelStream().map(s -> s.Codigo).collect(Collectors.toList());
    	
    	LOGGER.info("Codigo de aduana: " + c8);
    	
    	
    	if(!listaCodigosAduana.isEmpty()) {
    		LOGGER.info("Entro a validación");
    		return listaCodigosAduana.contains(c8);
    	}
    	
    	return false;
    }
    
    //No es una moneda válida
    public boolean validarV007(final Registro registro) {
    	SQL sql = new SQL();

    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    
    	List<Diccionario> codigosMoneda = new ArrayList<Diccionario>();
		
    	try {
    		codigosMoneda = sql.query(cnn, SELECT_CODIGO_MONEDA, new HashMap<>(), Diccionario.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    	List<String> listaCodigosMoneda = codigosMoneda.parallelStream().map(s -> s.Codigo).collect(Collectors.toList());
    	
    	LOGGER.info("Codigo de moneda: " + c9);
    	
    	
    	if(!listaCodigosMoneda.isEmpty()) {
    		LOGGER.info("Entro a validación");
    		return listaCodigosMoneda.contains(c9);
    	}
    	
    	return false;
    }
    
    //Monto operación no válido
    public boolean validarV008(final Registro registro) {
    	final Double c10 = ((Number) registro.getPorCodigoSII("C10")).doubleValue();
    	
    	return c10 != null && c10 > 0;
    }
    
    //Monto dolar no válido
    public boolean validarV009(final Registro registro) {
    	final Double c11 = ((Number) registro.getPorCodigoSII("C11")).doubleValue();
    	
    	return c11 != null && c11 > 0;
    }
    
    //Tipo cambio no válido
    public boolean validarV010(final Registro registro) {
    	final Double c12 = ((Number) registro.getPorCodigoSII("C12")).doubleValue();
    	
    	return c12 != null && c12 > 0;
    }
    
    //Monto equivalente en CLP no válido
    public boolean validarV011(final Registro registro) {
    	final Double c13 = ((Number) registro.getPorCodigoSII("C13")).doubleValue();
    	
    	return c13 != null && c13 > 0;
    }
    
    //Afecto a derivado no válido
    public boolean validarV012(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	
    	return AFECTO_A_DERIVADO.contains(c14);
    }

    //No es un documento identidad válido
    public boolean validarV013(final Registro registro) {
    	final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));
  
    	return c15 != null && validarRut(c15);
    }
    
    //Nombre interesado no válido
    public boolean validarV014(final Registro registro) {
    	final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
    	
    	return c16.trim() != "";
    }
    
    //Tipo de Autorización no válido
    public boolean validarV015(final Registro registro) {
    	final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
    	
        if (c17.trim() != "") {
        	return TIPO_AUTORIZACION.contains(c17);
        }
        
    	return true;
    }
    
    //Se requiere campo Sector Economico Beneficiario
    public boolean validarV016(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
    	    	
    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return c20 > 0;
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return c20 > 0;
    	}
    	
    	return true;
    }
    
    //Se requiere campo Número de Inscripción
    public boolean validarV017(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();

    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return c21 > 0;
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return c21 > 0;
    	}
    	
    	return true;
    }
    
    //Se requiere campo Fecha Desembolso
    public boolean validarV018(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));
    	
    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return !c22.equals("0001-01-01");
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return !c22.equals("0001-01-01");
    	}
    	
    	return true;
    }
    
    //Se requiere campo Sector Economico Inversionista
    public boolean validarV019(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
    	    	
    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return c23 > 0;
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return c23 > 0;
    	}
    	
    	return true;
    }
    
    //Se requiere campo Nombre Financista Extranjero
    public boolean validarV020(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	
    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return !c24.trim().equals("");
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return !c24.trim().equals("");
    	}
    	
    	return true;
    }
    
    //Se requiere campo Fecha Vencimiento Capital
    public boolean validarV021(final Registro registro) {
    	final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	
    	if(INFORMA_REGISTRO_20.contains(c7) && c30 == 0) {
    		return !c25.equals("0001-01-01");
    	}
    	
    	if(INFORMA_REGISTRO_20_ARB.contains(c7) && c30 == 1) {
    		return !c25.equals("0001-01-01");
    	}
    	
    	return true;
    }
    
    //Se requiere campo NroContDerivado
    public boolean validarV022(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
    	
    	if(c14 == 1) {
    		return c26 > 0;
    	}
    	
    	return true;
    }
    
   //Se requiere campo FechaSuscDerivado
    public boolean validarV023(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));	
    	
    	if(c14 == 1) {
    		return !c27.equals("0001-01-01");
    	}
    	
    	return true;
    }
    
    //Se requiere campo FechaVencDerivado
    public boolean validarV024(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final String c28 = String.valueOf(registro.getPorCodigoSII("C28"));	
    	
    	if(c14 == 1) {
    		return !c28.equals("0001-01-01");
    	}
    	
    	return true;
    }
    
    //Se requiere campo Observaciones
    public boolean validarV025(final Registro registro) {
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	
    	if(INFORMA_REGISTRO_60.contains(c7) ) {
    		return !c29.trim().equals("");
    	}
    	
    	return true;
    }
    
    //Para los codigos de comercio 21100, 21101, 21485 y 21486, Tipo Autorización debe tener valor EE
    public boolean validarV026(final Registro registro) {
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
    	
    	if(CODIGO_COMERCIO_EE.contains(c7) ) {
    		return c17.trim().equals("EE");
    	}
    	
    	return true;
    }
    
    //No es un sector económico beneficiario valido
    public boolean validarV027(final Registro registro) {
    	SQL sql = new SQL();

    	final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
    	
    	if(c20.equals("0")) {
    		return true;
    	}
    	
    	List<Diccionario> codigosSectorEconomico = new ArrayList<Diccionario>();
		
    	try {
    		codigosSectorEconomico = sql.query(cnn, SELECT_SECTOR_ECONOMICO, new HashMap<>(), Diccionario.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    	List<String> listaCodigosSectorEconomico = codigosSectorEconomico.parallelStream().map(s -> s.Codigo).collect(Collectors.toList());
    		   	
    	if(!listaCodigosSectorEconomico.isEmpty()) {
    		return listaCodigosSectorEconomico.contains(c20);
    	}
    	
    	return true;
    }
    
    //No es un sector económico inversionista valido
    public boolean validarV028(final Registro registro) {
    	SQL sql = new SQL();

    	final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
    	
    	if(c23.equals("0")) {
    		return true;
    	}
    	
    	List<Diccionario> codigosSectorEconomico = new ArrayList<Diccionario>();
		
    	try {
    		codigosSectorEconomico = sql.query(cnn, SELECT_SECTOR_ECONOMICO, new HashMap<>(), Diccionario.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
   
    	List<String> listaCodigosSectorEconomico = codigosSectorEconomico.parallelStream().map(s -> s.Codigo).collect(Collectors.toList());
    		   	
    	if(!listaCodigosSectorEconomico.isEmpty()) {
    		return listaCodigosSectorEconomico.contains(c23);
    	}
    	
    	return true;
    }
    
    private boolean validarRut(String registro){
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
}