package cl.bcs.tesoreria.cloper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import cl.bcs.tesoreria.cloper.CloperWsBSC.ParametrosConexionCloperWS;

import com.google.gson.Gson;

public class Cloper {
	
	public Long 				bolsa;
	public String				camara;
	public BigDecimal   		cantidad;	    
	public Long					cod_corr_com;
	public Long					cod_corr_ven;
	public Long					cod_liq_com;
	public Long					cod_liq_ven;
	public Long					cod_ope_com;
	public Long					cod_ope_ven;
	public Long					dias_ven;
    public String				emisor;
	public String				estado;
	public String				estado_liq_bil;	    
	public String				fec_liq;
	public String				fecha;
	public String				fecha_pago;
	public String       		folio;
    public String				hora;
	public String				madurez;
	public String				mercado;
	public String				moneda;
	public BigDecimal   		monto;
	public BigDecimal   		monto_div;
    public String				nemo;
	public String				tipo_liq;		
	public String				tipo_liquida;
	public String				tipo_opera;
	
	// DEFINICION DE CONSTANTES
	private static final Logger logger = LogManager.getLogger(Cloper.class);
	              
	/**
	 * Constructor vacio
	 */
	public Cloper()	{}
	
    /**
    * 
    * @param entradaCloper: Parámetros de entrada para la consulta del WS cloper, parametrosConexion: Parametros de conexion del WS cloper
    * @return: Lista con el resultado de la consulta al WS de cloper 
    * @throws Exception
    */
	public List<Cloper> obtenerDataCloper(String jsonBody, ParametrosConexionCloperWS parametrosConexion) throws Exception{
		List<Cloper> listaCloper = null;
		logger.info("INICIO::Cloper.obtenerDataCloper");
		try {	
			
			logger.trace("PARAMETROS DE CONSULTA: " + jsonBody);
			CloperWsBSC cloperWsBSC = new CloperWsBSC();
			JSONArray jsonRespuesta = cloperWsBSC.consultarCloper(jsonBody, parametrosConexion);
	
			if (jsonRespuesta != null &&  jsonRespuesta.length() > 0){	
				logger.trace("WS CLOPER ENCONTRO INFORMACION");
				
				String jsonR = jsonRespuesta.toString();
				
				Gson gson = new Gson();
				Cloper[] arrCloper = gson.fromJson(jsonR, Cloper[].class);
				
				listaCloper = Arrays.asList(arrCloper);
				listaCloper = listaCloper.stream().filter(cloper -> !"NO EXISTE".equalsIgnoreCase(cloper.fecha)).collect(Collectors.toList());
			}else{
				logger.trace("WS CLOPER NO ENCONTRO INFORMACION");
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		logger.info("FIN::Cloper.obtenerDataCloper");
		return listaCloper;
	}

} // fin class

