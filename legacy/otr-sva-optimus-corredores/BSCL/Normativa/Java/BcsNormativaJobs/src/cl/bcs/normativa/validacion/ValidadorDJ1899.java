package cl.bcs.normativa.validacion;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

import cl.bcs.normativa.data.Registro;
import cl.bcs.plataforma.jobs.ActualizaInstrumentosJob;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;


import cl.bcs.utils.Log;

/**
 * Implementaci&oacute;n del validador de la DJ1899.
 *
 * 
 *
 */
public class ValidadorDJ1899 extends Validador {
	private static final List<String> TIPO_ACTIVO_PENSIONADO = Arrays.asList("1","2");
	private final String version;
	private static Connection cnn;
	private static Connection db_con = null;
	private static DataSource ds;
    private static Long idEmpresa;
    private static Double p16;
    private static Double p31;   
    private static final Log LOG = new Log("MER.ACTUALIZAR_PARAMETROS_FINANCIEROS");
     
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1899(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        cnn = contexto.getConnection();
        idEmpresa = contexto.getEmpresa().getId(); 
        
    }
    
  /*  public boolean validarH0(final Registro registro) {
    	// La columna ActivoPensionado (C3) debe indicar valores segun tabla TIPO_ACTIVO_PENSIONADO 
    	final String C3 = (String) registro.getPorCodigoSII("C3");
    	return C3 != null && TIPO_ACTIVO_PENSIONADO.contains(C3);
    }
    
    public boolean validarH1(final Registro registro) {
    	// Debe haber ejecutado el complemento de actualizacion de montos a moneda dj (uf, $, usd)
    	final String C99 = (String) registro.getPorCodigoSII("C99");
    	return C99 != null && "S".equals(C99);
    }
    
    public boolean validarH2(final Registro registro) {
    	// Debe haber ejecutado el complemento de actualizacion montos segun ipc
    	final String C100 = (String) registro.getPorCodigoSII("C100");
    	return C100 != null && "S".equals(C100);
    }
    
    public boolean validarH3(final Registro registro) {
    	
    	return true;
    }*/
    
//https://jira.bcs.cl/browse/CELBUHOPRO-116
    
    public Double getParametroSistema(String parametro) throws Exception {
    	String parametroStr = ConfiguracionSistema.getParametroConfiguracionString(cnn, idEmpresa, "NORMATIVA", parametro);
    	Double parametroDouble = Double.parseDouble(parametroStr);
        return parametroDouble;    
    }
    
    
     public boolean validarH264(final Registro registro) throws Exception {
       
        p16 = getParametroSistema("P16").doubleValue();
        p31 = getParametroSistema("P31").doubleValue();    
       
        final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
        final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();  
       
        LOG.write("c16: " + c16);
        LOG.write("c17: " + c17);
        LOG.write("p16: " + p16);
       
        int c16p16 = (int) Math.floor(c16 * p16);
       
        if ((c16 > 0) &&  (c17 > 0)) {
       
            if (c17 + p31 >= c16p16) {
                return true;
            } else {    
                return false;
            }
           
        } else {
            return true;
        }  
 
    }  
    
    public boolean validarH923(final Registro registro) throws Exception {
        
      p16 = getParametroSistema("P16").doubleValue();

    	final Double C9 = ((Number) registro.getPorCodigoSII("C9")).doubleValue();
    	final Double C10 = ((Number) registro.getPorCodigoSII("C10")).doubleValue();    	
    	return( C10 >= C9 * p16);    	 

    }     
 
    
   public boolean validarH924(final Registro registro) throws Exception {
	    /* final Double C16 = ((Number) (registro.getPorCodigoSII("C16") != null ? registro.getPorCodigoSII("C16"): "0")).doubleValue(); */
	    /* final Double C3 = ((Number) registro.getPorCodigoSII("C3")).doubleValue(); 
	     * final Double C3 = ((Number) registro.getPorCodigoSII("C3")).doubleValue(); */
		final Double C16 = ((Number) registro.getPorCodigoSII("C16")).doubleValue();
		final String C3 = String.valueOf(registro.getPorCodigoSII("C3"));

	    

		if(C16 > 0 ){
			return C3.equals("1") || C3.equals("2");
		} else{
			return true;
		}
		
    }
   
	public boolean validarH925(final Registro registro) throws Exception {
		
		LOG.write("inicia la H925");
		p16 = getParametroSistema("P16").doubleValue();
		
    	final String C4 = (String) registro.getPorCodigoSII("C4");
    	

    	LOG.write("C4"+C4);
    	
		final Double C7 = ((Number) registro.getPorCodigoSII("C7")).doubleValue();
		

		LOG.write("C7"+C7);
    	
		if(C7 > 0){
			if(C4.isEmpty()){
				
				LOG.write("Ingreso a la condicion C4");
				
				return false;
			}
			LOG.write("ingreso a la condicion C7");
		}

		LOG.write("Fin de validacion H925");
		return true;
    }
/*
	public boolean validarH932(final Registro registro) throws SQLException,Exception{
		
		p16 = getParametroSistema("P16").doubleValue();

		LOG.write("Inicio la validacion H932");
		
		String consultaC23 = "SELECT SUM(\"RetiroEmpleadorActualizado\") AS TOTAL FROM \"BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos\" WHERE \"ActivoPensionado\" = 1";
		LOG.write("ConsultaC23_"+consultaC23);
		
		String consultaC24 = "SELECT SUM(\"MontoRetencionActualizado\") AS TOTAL FROM \"BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos\" WHERE \"ActivoPensionado\" = 1";
		LOG.write("ConsultaC24_"+consultaC24);
		
		Statement sentencia = null;
		
		//db_con = null;
		
		
		try {
			
			LOG.write("Ingreso al try");
			
			//db_con = ds.getConnection();
			
			LOG.write(cnn.isValid(500));
			
			sentencia = cnn.createStatement();
			
			double C23 = 0;
			//sumatoria retiro empleador
			
			double C24 = 0;
			//sumatoria monto retencion 15%
			
			ResultSet resultadoC23 = null;
			ResultSet resultadoC24 = null;
			
			
			resultadoC23 = sentencia.executeQuery(consultaC23);
			
			resultadoC24 = sentencia.executeQuery(consultaC24);
			
			
			LOG.write("resultadoC23_= "+resultadoC23.first());
			
			if(resultadoC23.first()) {
				
				C23 = resultadoC23.getDouble(1);
				
				LOG.write("resultado_C23_= "+C23);
				
			}
			
			LOG.write("resultadoC24_= "+resultadoC24.first());
			
			if(resultadoC24.first()) {
				
				C24 = resultadoC24.getDouble(1);

				LOG.write("resultadoC24_= "+C24);
				
			}

			if(!(C24 >= C23*p16)){

				
				LOG.write("ingresa a la condicion");
				return false;
			}
			
			LOG.write("Fin de la H932");

			return true;
			
		} catch (SQLException ex) {
			cnn.rollback();
            ex.printStackTrace();
            
        }
		
		return false;
	}

	public boolean validarH941(final Registro registro) throws SQLException,Exception{
		
		p16 = getParametroSistema("P16").doubleValue();
		
		LOG.write("inicio H941");
		
		double C30 = 0;
		double C31 = 0;
		double C32 = 0;
		
		ResultSet resultadoC30 = null;
		ResultSet resultadoC31 = null;
		ResultSet resultadoC32 = null;
		
		//db_con = null;
		Statement sentencia = null;
		
		String consultaC30 = "SELECT SUM(\"MontoRetiroPorAhorrosIncisoPrimero\") AS TOTAL FROM \"BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos\" WHERE \"ActivoPensionado\" = 1";
		
		String consultaC31 = "SELECT SUM(\"MontoRetiroPorAhorrosIncisoPrimero\") AS TOTAL FROM \"BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos\" WHERE \"ActivoPensionado\" = 2";
		
		String consultaC32 = "SELECT SUM(\"RetencionImpuestoSobreRetiros\") AS TOTAL FROM \"BSCL.Normativa.BD.Vistas::TRI.DJ1899.Datos\";";

		try {
			
			LOG.write("entrada al try");
			
			//db_con = ds.getConnection();
			LOG.write(cnn.isValid(500));
			
			sentencia = cnn.createStatement();
			
			LOG.write(sentencia);
			
			resultadoC30 = sentencia.executeQuery(consultaC30);
			LOG.write(resultadoC30.getString(1));
			
			resultadoC31 = sentencia.executeQuery(consultaC31);
			LOG.write(resultadoC31.getString(1));
			
			resultadoC32 = sentencia.executeQuery(consultaC32);
			LOG.write(resultadoC32.getString(1));
			
			//LOG.write("resultadoC30_"+resultadoC30.isFirst());
			
			if(resultadoC30.isFirst()) {
				
				C30 = resultadoC30.getDouble("TOTAL");

				
				LOG.write("resultado C30_"+C30);
				
			}else LOG.write("ResultadoC30 es vacio");
			
			LOG.write("resultadoC31_"+resultadoC31.isFirst());
			if(resultadoC31.isFirst()) {
				
				C31 = resultadoC31.getDouble("TOTAL");
				LOG.write("resultado C31_"+C31);
				
			}else LOG.write("ResultadoC31 es vacio");
			
			LOG.write("resultadoC32_"+resultadoC32.isFirst());
			if(resultadoC32.isFirst()) {
				
				C32 = resultadoC32.getDouble("TOTAL");
				LOG.write("resultado C32_"+C32);
				
			}else LOG.write("ResultadoC32 es vacio");

			if(!(C32 >= (p16 *C30) + (p16 * C31))){

				
				LOG.write("Entro a la condicion");


				return true;
			}

			LOG.write("Fin de H941");
			return false;
			
		} catch (SQLException ex) {
			cnn.rollback();
            ex.printStackTrace();
        }
		
		return false;
	}
    */
}