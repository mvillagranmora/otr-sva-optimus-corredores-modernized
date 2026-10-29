package cl.bcs.normativa.declaraciones.DJ1922;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarDetalles implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ActualizarDetalles.class);
	private static final Integer CodigoCorredor = 11;
	
	public static class ObjDetalleParidad {
		public Long IdInterno;
		public Long ID;
		public Long Numero;
		public String TipoParidad;
		public String Fecha;
		public String IdMonedaOrigen;
		public BigDecimal MontoOrigen;
		public String IdMonedaDestino;
	    public BigDecimal MontoDestino;
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class ActualizarDetallesEntrada {
		public Long	p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public Long p_Version;
		 public String toString() {
				return Utils.printJson(this, true);
			}
		public Long p_Afecto100;
	}
	

		
	public static class ActualizarDetallesSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
		private static FileLoader loader = new FileLoader();	
		private static String SQL_GET_IDCORREDOR = "SELECT \"CodigoCorredora\" FROM \"BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa\" WHERE \"IdBolsa\" = 'BCS' AND \"IdEmpresa\" = ?;";
		private static String SQL_UPDATE_DETALLE = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ActualizarDetalle.sql");
		private static String SQL_UPDATE_DETALLE_COR = loader.loadResourceToString("cl/bcs/normativa/declaraciones/DJ1922/sql/ActualizarDetalleCorredor.sql");
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ActualizarDetallesEntrada in=gson.fromJson(jin.toString() ,ActualizarDetallesEntrada.class);
		ActualizarDetallesSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public Integer getCorredor(Connection con, long idEmpresa) throws SQLException {
		
		int idCorredor = -1;
		
	    try (PreparedStatement preparedStatement = con.prepareStatement(SQL_GET_IDCORREDOR)) {
	        preparedStatement.setLong(1, idEmpresa);

	        try (ResultSet resultSet = preparedStatement.executeQuery()) {
	            if (resultSet.next()) { 
	                idCorredor = resultSet.getInt("CodigoCorredora");
	                System.out.println("ID del corredor: " + idCorredor);
	            } else {
	                System.out.println("No se encontraron resultados.");
	            }
	        }
	    }

	    return idCorredor;
		
	}
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ActualizarDetallesSalida logica(Connection con, ActualizarDetallesEntrada entrada) throws Exception {
		logger.info("Inicio Modificar Montos DJ1922: " + entrada.toString());
		ActualizarDetallesSalida out = new ActualizarDetallesSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
        try {
	        SQL sql = new SQL();
	    //actualizacion de registros    
	        List<ActualizarDetallesEntrada> lstParametros = new ArrayList<>();
			
	        ActualizarDetallesEntrada param = new ActualizarDetallesEntrada();
				param.p_IdEmpresa = entrada.p_IdEmpresa;
				param.p_IdUsuario = entrada.p_IdUsuario;
				param.p_Version = entrada.p_Version;
				param.p_Afecto100 = entrada.p_Afecto100;
				
				lstParametros.add(param);		
				
				Integer idCorredor = getCorredor(con, entrada.p_IdEmpresa);
                
		        sql.batch(con, SQL_UPDATE_DETALLE, lstParametros, ActualizarDetallesEntrada.class);
		        System.out.println("Se ejecuta SQL_UPDATE_DETALLE");

                if (idCorredor != CodigoCorredor) { // BCS
		            sql.batch(con, SQL_UPDATE_DETALLE_COR, lstParametros, ActualizarDetallesEntrada.class);
                	System.out.println("Se ejecuta SQL_UPDATE_DETALLE_COR");
                }

		       out.P_OUT_MENSAJE = "Montos Actualizados Correctamente";
		       logger.info("Fin logica");
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
		 
	}
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				ActualizarDetallesEntrada entrada = new ActualizarDetallesEntrada();
  				entrada.p_IdEmpresa	= 1L;
  				entrada.p_IdUsuario	= 6L;
  				entrada.p_IdModulo	= "NOR";
  				entrada.p_Version	= 2022L;
  				entrada.p_Afecto100 = 0L;
  				ActualizarDetallesSalida Salida = new ActualizarDetallesSalida();
  				ActualizarDetalles demo = new ActualizarDetalles();
  				Salida = demo.logica(con,entrada);
  				System.out.println(Salida.P_OUT_MENSAJE);
  				con.rollback();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
}
