package cl.bcs.plataforma.demo;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ServiceDemo implements JavaCall {
	
	/*
	 * Nuestro gran amigo el log
	 */
	private final Log log=new Log("PLA.ServiceDemo");
	
	/*
	 * 
	 * Queries que van a ser utilizadas por el servicio. 
	 * Deben usar : para optimizar con los PreparedStatement
	 * 
	 */	
	private final String SQL_DUMMY 		= loadResourceToString("cl/bcs/plataforma/demo/sql/dummy.sql");
	
	private final String SQL_DUMMY_SP 	= loadResourceToString("cl/bcs/plataforma/demo/sql/dummySP.sql");
	
	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT
	 * Recordar que solo son permitidos tipos: 
	 *      - long      como long
	 *      - int       como int
	 *      - decimal   como BigDecimal
	 *      - char		como string
	 *      - varchar	como string
	 *      - date      como string YYYY-MM-DD
	 *      - datetime  como string YYYY-MM-DD HH:MI:SS
	 */	
	public static class ServiceDemoEntrada {
		public long 			p_IdEmpresa;
		public long 			p_IdUsuario;
		public BigDecimal		p_Decimal;
		public String 			p_IdModulo;
		public List<Coleccion>  p_Coleccion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ServiceDemoSalida {
		public long 			P_OUT_ERRORESCONTADOR;
		public long 			p_IdUsuario;
		public BigDecimal		p_Decimal;		
		public String 			p_IdModulo;
		public List<Coleccion>  p_Coleccion;
	}	
	
	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */
	public static class Coleccion {
		public long 				A; 
		public long 				B;
		public String 				C;
		public java.sql.Timestamp	D;
		public java.sql.Date		E;
		public BigDecimal			F;
		
	}	
	
	/*
	 * 
	 * Objeto a ser utilizado para convertir y procesar las llamadas de la capa 
	 * de  transporte.
	 * 
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		ServiceDemoEntrada in=gson.fromJson(jin.toString() ,ServiceDemoEntrada.class);
		ServiceDemoSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	

	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://161.238.34.246:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SOPORTE";
        String pass   = "Bolsa123";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        
        //Example
		try {
			Connection con = ds.getConnection();
			
			
			String query = "call \"BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada\"(?,?,?,?,?,?)";
			int date = -1;
				CallableStatement st = con.prepareCall(query);
				st.setLong(1, 1);
				st.setLong(2, 1);
				st.setString(3, "ORD");
				st.setLong(4, 10533);
				st.registerOutParameter(5, Types.BIGINT); 
				if(st.execute())
				{
					try(ResultSet rs =st.getResultSet()){
						while (rs.next()) {
							date = Integer.parseInt(rs.getString(1));
	 					}
					}
				}
			java.util.Date fecha = new java.util.Date(date);
			System.out.println(fecha);
			
			//---------------------------------
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse("{ 	\"p_IdEmpresa\":1, 	\"p_IdUsuario\":7, \"p_Decimal\":1.1234567890,	\"p_IdModulo\":\"CTA\", 	\"p_Coleccion\":[{\"A\":null,\"B\":27499,\"C\":\"CLP\"}] } ").getAsJsonObject();
			ServiceDemo demo=new ServiceDemo();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	        	        			
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/*
	 * Este servicio optimiza las llamadas internas para java
	 * dado que no hay necesidad de usar JSON
	 * 
	 * Estrategias a la hora de implementar:
	 * 
	 * - Obtener datos masivamente de la base datos
	 * - procesar logica de forma unitaria
	 * - Enviar datos masivamente a la base datos con batch
	 * 
	 * 	 
	 */
	public ServiceDemoSalida logica(Connection con, ServiceDemoEntrada entrada) throws Exception {
		
		int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
		Fecha.setLocalOffSet(SegundosZonaHoraria);
		
		SQL sql=new SQL();
		ServiceDemoSalida out=new ServiceDemoSalida();
		out.P_OUT_ERRORESCONTADOR=0;
		out.p_Coleccion=new ArrayList<Coleccion>();		
		
		log.write("INICIO - SRV");
		
		log.write("Entrada: "+entrada) ;
		
		log.write("Fecha Local: "+Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraSistemaTs(con, 1)));
		
		log.write("Fecha Local: "+Fecha.toStringDatetime(Fecha.toLocalTimeStamp(FechaDB.ObtenerFechaHoraSistemaTs(con, 1))));
		
		log.write("Llamada fecha sin cache: "+FechaDB.ObtenerFechaHoraSistemaUTC(con, 1));
		
		log.write("Llamada fecha con cache: "+FechaDB.ObtenerFechaHoraSistemaUTC(con, 1));

		
		//2) Prueba de llamada a query con parametros nombrados y una lista
		/*
		HashMap<String,Object> parms=new HashMap<String,Object>();
		parms.put("B", 2);	
		List<String> items=new ArrayList<String>();
		items.add("xx");
		items.add("cc");
		items.add("ff");
		items.add("SOY TEXTO");
		items.add("");		
		parms.put("LISTA_C", items);				
		List<Coleccion>  cols=sql.query(con, SQL_DUMMY,parms,Coleccion.class);
		out.p_Coleccion.addAll(cols);
		*/
		
		
		
		//3) Prueba de llamada a batch con parametros List<objeto>
		/*List<Coleccion> params=new ArrayList<Coleccion>();
		Coleccion x=new Coleccion();
		x.A=1;
		x.B=123456;
		x.C="PECO";		
		params.add(x);			
		sql.batch(con,"SELECT :A A,:B B,:C C from dummy ", params,Coleccion.class);*/
		
		
		//4) Prueba de llamada a SP con parametros escalares      List<objeto>
		HashMap<String,Object> parmsIN=new HashMap<String,Object>();
		parmsIN.put("p_inNum", 2);	
		parmsIN.put("p_inChar", "HOLA MUNDO XX");
		parmsIN.put("p_inDec", 12.12342);
		log.write(parmsIN);
		log.writeAsJson(parmsIN);
		
		
		HashMap<String,Class> parmsOUT=new HashMap<String,Class>();
		parmsOUT.put("p_out_tab", Coleccion.class);
		parmsOUT.put("p_out_var", String.class);
		parmsOUT.put("p_out_Num", Long.class);
		parmsOUT.put("p_out_Dec", BigDecimal.class);
		
		
		long timeini=System.currentTimeMillis();
		HashMap<String,Object> respuesta=sql.call(con, SQL_DUMMY_SP, parmsIN, parmsOUT);
                long timeend2=System.currentTimeMillis()-timeini;
		out.p_Decimal = (BigDecimal)respuesta.get("p_out_Dec");
		out.p_IdModulo = (String)respuesta.get("p_out_var");
		out.p_IdUsuario = (Long)respuesta.get("p_out_Num");
		out.p_Coleccion.addAll((List<Coleccion>)respuesta.get("p_out_tab"));
		log.write(out);
		log.writeAsJson(out);
		
		long timeend3=System.currentTimeMillis()-timeini;
		System.out.println("EXEC:"+sql.getTimeExecuteStatament()+" PREP:"+sql.getTimePrepareStatament()+" TOTAL:"+sql.getTimeTotalStatament());
		System.out.println("FINAL2:"+timeend2+"  FINAL3:"+timeend3);
		
		log.write("Termino del servicio");
		
		return out;
		/*
		List<Coleccion> col=new ArrayList<Coleccion>();		
		IndexedCollection<Coleccion> elems = new ConcurrentIndexedCollection<>();
		elems.addAll(col);
		
		QueryRunner qSQL = new QueryRunner();
		List<Coleccion>  cols=qSQL.query(con, SQL_DUMMY,new BeanListHandler<Coleccion>(Coleccion.class),"HOLA","PECO");
		
		//HashMap<String,Object> params

		cols=qSQL.query(con, SQL_DUMMY,new BeanListHandler<Coleccion>(Coleccion.class),new int[]{1,2,3,4,5},"hola");


		 
		
		ServiceDemoSalida out=new ServiceDemoSalida();
		out.P_OUT_ERRORESCONTADOR=0;
		out.p_Coleccion=new ArrayList<Coleccion>();		
		
		///////////////////////////////
		//Inicio de logica del servicio
		try (PreparedStatement st=con.prepareStatement(SQL_DUMMY)){
			st.setString(1, "Hola Mundo");
			try (ResultSet rs=st.executeQuery()) {
				while (rs.next()) {
					Coleccion el=new Coleccion();
					el.A=1;
					el.B=2;
					el.C=rs.getString("Valor");
					out.p_Coleccion.add(el);
				}
			}
		}
		
		///////////////////////////////		
		//Procesamiento de logica
		
		
		///////////////////////////////		
		//Envio de datos a la base datos
		
		
		return out;*/
		
	}




}
