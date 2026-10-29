package cl.bcs.notificacion.validate.sql;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.notificacion.template.EvalTemplate;
import cl.bcs.notificacion.template.KeyValue;
import cl.bcs.utils.Utils;
 
  
public class ConnectionDB {
	
	public int validateToken(Connection con, String p_token) throws SQLException
	{
	    
		String query = "{CALL \"BSCL.Plataforma.BD.Servicios::EVT.ValidarTokenNotificacion\"(?,?)}";
		try{
			Logger.getLogger(ConnectionDB.class.getName()).log(Level.INFO, query);
			try(CallableStatement cstm = con.prepareCall(query);){
				cstm.setString(1, p_token);
				cstm.registerOutParameter(2, Types.BIGINT);
				cstm.execute();
				return cstm.getInt(2);
 			}			 
		}catch(Exception e){
 			Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, e.getMessage());
 			throw e;
 		}
 	}
	
	public String validateTokenAndId(Connection con, String p_token, String p_rut)
	{
		EvalTemplate eval = new EvalTemplate();
		String context = "";
		List<KeyValue> tagsAgrupa = new ArrayList<>();
		List<KeyValue> tagsNoAgrupa = new ArrayList<>();

		Map<String, String> tagPredeterminados = new HashMap<String, String>(); 
		String query = "{CALL \"BSCL.Plataforma.BD.Servicios::EVT.ValidarConfirmarNotificacion\"(?,?,?,?,?,?,?)}";
		try{
			
			Logger.getLogger(ConnectionDB.class.getName()).log(Level.INFO, query);
			try(CallableStatement cstm = con.prepareCall(query);){
				cstm.setString(1, p_token);
				cstm.setString(2, p_rut);
				cstm.registerOutParameter(3, Types.CLOB);
				cstm.registerOutParameter(4, Types.BIGINT);
				cstm.registerOutParameter(5, Types.NVARCHAR);
				boolean f = cstm.execute();
				final StringBuilder template = new StringBuilder();
				final Reader reader = cstm.getClob(3).getCharacterStream();
				final BufferedReader br = new BufferedReader(reader);
				int b ;
				while(-1!= (b=br.read())){
					template.append((char)b);
				}
				br.close();
				int i = 0;
				while(f){
					
					try(ResultSet rs = cstm.getResultSet()){
						if(i==0){
								while(rs.next()){
									if(rs.getString("Agrupa").equals("S")){
										tagsAgrupa.add(new KeyValue(rs.getString("CodigoTipoEvento"), rs.getString("Llave"), rs.getString("Valor"), 0));
										
									}else{
										 
										tagsNoAgrupa.add(new KeyValue(rs.getString("CodigoTipoEvento"), rs.getString("Llave"), rs.getString("Valor"), 0));
 									}
								}
						}else{
								while(rs.next()){
									ResultSetMetaData rsmd = rs.getMetaData();
  									if(rsmd.getColumnName(1).equals("Llave")&&rsmd.getColumnName(2).equals("Valor")){
										tagPredeterminados.put(rs.getString("Llave"), rs.getString("Valor"));
									}
  								
							}				
						}
					}
					i++;
					f = cstm.getMoreResults();
					
					
				}
				
				if(tagsAgrupa.size()>0){
					context = eval.Agrupa(tagsAgrupa, tagPredeterminados);
				}
				if(tagsNoAgrupa.size()>0){
					context  = eval.NoAgrupa(tagsNoAgrupa, tagPredeterminados);
				}
				String ConfirmacionMacro = getConfirmationMacro(con);
				ConfirmacionMacro = ConfirmacionMacro.replace("::URL::", "#");
				String templ = eval.eval(context, ConfirmacionMacro.concat(template.toString()));
				
				
   				if(cstm.getInt(4)==-1){
					templ = null;
				}
  				return templ;
 			}			 
		}catch(Exception e){
 			Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, e.getMessage());
			return null;
		}
 	}
	
	public String getConfirmationMacro(Connection con){
		
		final String sql ="select \"ValorDefault\" from \"BSCL.Plataforma.BD::EVT.TagGenerales\" where \"Llave\" = 'Confirmar'" ;
 		String tag ="";
 
		try{
			
			try(PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()){
				
				while(rs.next()){
					tag = rs.getString("ValorDefault");
				}
			}catch (Exception e) {
 				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
			}
		}catch(Exception e){
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage());
		}
		
 		return tag;
	}
 	
	public int acceptOrReject(Connection con, String p_token, String p_comentario, String p_accion)
	{
		
		String accion = "";
		if(p_accion.equals("1")){
			accion = "ACP";
		}else if(p_accion.equals("2")){
			accion = "RZD";
		}else{
			return -1;
		}
		
		String query = "UPDATE \""+Utils.getSchema()+"\".\"BSCL.Plataforma.BD::EVT.Notificacion\" SET \"Observacion\" = ?, \"Confirmado\"= 'S',\"AccionConfirmada\" = ?, \"FechaConfirmado\"= CURRENT_UTCTIMESTAMP WHERE \"TokenConfirmacion\" = ?";
		try{
			Logger.getLogger(ConnectionDB.class.getName()).log(Level.INFO, query);
			try(PreparedStatement cstm = con.prepareStatement(query);){
				
 
				cstm.setString(1, p_comentario);
				cstm.setString(2, accion);
				cstm.setString(3, p_token);
				cstm.executeUpdate();
				con.commit();
				return 1;
 			}finally{
 				
 				if(con!=null){
 					con.rollback();
 					con.close();
 				}
 				
 			}			 
		}catch(Exception e){
 			Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, e.getMessage());
			return -1;
		} 
 	}
	public String getUrlHana(Connection con, long idEmpresa) throws SQLException
	{
	 
		String query = "Select \"Valor\" FROM \""+Utils.getSchema()+"\".\"BSCL.Plataforma.BD::PLA.Configuracion\" where \"CodigoParametro\" = 'PLA.UrlHana' AND \"IdEmpresa\"=?";
		try{
 			Logger.getLogger(ConnectionDB.class.getName()).log(Level.INFO, query);
			try(PreparedStatement cstm = con.prepareStatement(query);){
				cstm.setLong(1, idEmpresa);
				if(cstm.execute()){
					ResultSet rs = cstm.getResultSet();
					while(rs.next()){
						return rs.getString(1);
					}
				}
  				
  			}			 
		}catch(Exception e){
 			Logger.getLogger(ConnectionDB.class.getName()).log(Level.SEVERE, e.getMessage());
			return null;
		}
		return null;
 	}
	

}