package cl.bcs.ordenes.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearTransaccionesMarketData;
import cl.bcs.ordenes.CrearOrdenes.ObjCOBROS;
import cl.bcs.ordenes.CrearOrdenes.ObjGARANTIAS;
import cl.bcs.ordenes.CrearOrdenes.ObjRESULTADO;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.CrearTransaccionesMarketData_Salida;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionIFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRFMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionRVMarketData_Entrada;
import cl.bcs.ordenes.CrearTransaccionesMarketData.TransaccionSIMMarketData_Entrada;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarOrderIdHT implements JavaCall {

	public static String SQL_SELECT_TBRVTR = "SELECT TR.FECHA ,RTRIM(LTRIM(TR.FOLIO)) AS FOLIO, BO.ID_ORDEN_COM ,BO.ID_ORDEN_VEN FROM TBRVTR TR LEFT JOIN TBRVTRBO BO ON TR.FECHA = BO.FECHA AND TR.FOLIO = BO.FOLIO WHERE TR.FECHA BETWEEN :fechaDesde AND :fechaHasta AND (TR.COD_CORR_COM = :corredor OR TR.COD_CORR_VEN = :corredor)";
	
	public static String SQL_UPDATE_TRANSACCION = "UPDATE \"BSCL.Orden.BD::ORD.Transaccion\" SET \"OrderIdNegociacion\" = CASE WHEN \"IdTipoOperacion\" LIKE 'COM%' THEN :ID_ORDEN_COM  WHEN \"IdTipoOperacion\" LIKE 'VEN%' THEN :ID_ORDEN_VEN ELSE '' END WHERE \"FechaSistemaCreacion\" = :FECHA AND \"Folio\" = :FOLIO";
	
	public static class TransaccionHTV2 {
		public String FOLIO;
		public String ID_ORDEN_COM;
		public String ID_ORDEN_VEN;
		public String FECHA;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class Entrada_ActualizarOrderIdHT extends BcsPojo {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_FechaDesde;
		public String						p_FechaHasta;
		
	}
	
	public static class Salida_ActualizarOrderIdHT extends BcsPojo {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
	}
	
	public Salida_ActualizarOrderIdHT logica(Connection conn, Entrada_ActualizarOrderIdHT in) throws Exception {
		Salida_ActualizarOrderIdHT salida = new Salida_ActualizarOrderIdHT();
		Properties prop = new Properties();
    	InputStream input = null;
    	try {
    		input = new FileInputStream("ruebas.properties");
    	}
    	catch (FileNotFoundException e) {
    		throw new InterruptedException("Error no se encuentra el archivo de configuracion");
    	}
        prop.load(input);
        
        String DBdriver,DBuser,DBpass,DBurl,DBprop;
        
        DBdriver=prop.getProperty("DBdriver");
        DBuser=prop.getProperty("DBuser");
        DBpass=prop.getProperty("DBpass");
        DBurl=prop.getProperty("DBurl");
        DBprop=prop.getProperty("DBprop");
        input.close();
		SQL _sql = new SQL();
		
		BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
           
    	} 
    	catch(Exception e) {
    		throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }
    	
    	ConsultarEmpresa cons = new ConsultarEmpresa();
    	InformacionEmpresa empresa = cons.logica(conn, in.p_IdEmpresa);
    	int codigoCorredora = Integer.parseInt(empresa.Codigo);
    	
    	//SELECT TBPLTBRV
    	HashMap<String, Object> params = new HashMap<String, Object>();
    	params.put("fechaDesde", in.p_FechaDesde);
    	params.put("fechaHasta", in.p_FechaHasta);
    	params.put("corredor", codigoCorredora);
    	
    	List<TransaccionHTV2> transaccionesSebra = _sql.query(dsTarget.getConnection(), SQL_SELECT_TBRVTR, params , TransaccionHTV2.class);
    	
      	_sql.batch(conn, SQL_UPDATE_TRANSACCION, transaccionesSebra, TransaccionHTV2.class);
    	
    	//UPDATES
    	salida.P_OUT_ERRORESCONTADOR = 0L;
    	
    	return salida;
	}
	
	public static void main(String [] args) throws Exception {
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("scotia","connections.properties")) {
			ActualizarOrderIdHT o = new ActualizarOrderIdHT();
			Entrada_ActualizarOrderIdHT entrada = new Entrada_ActualizarOrderIdHT();
			entrada.p_FechaDesde = "2025-09-01";
			entrada.p_FechaHasta= "2025-09-01";
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			Salida_ActualizarOrderIdHT salida = o.logica(con, entrada);
			System.out.println(salida);
			con.commit();
		}
		
	}
	
	
	
	

	
}
