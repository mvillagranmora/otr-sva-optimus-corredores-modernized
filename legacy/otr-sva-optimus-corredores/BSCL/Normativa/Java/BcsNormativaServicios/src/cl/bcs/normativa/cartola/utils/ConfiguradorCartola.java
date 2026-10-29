package cl.bcs.normativa.cartola.utils;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.normativa.cartola.utils.GraficoMaster.GraficoDef;
import cl.bcs.custodia.utils.IngresoCustodia.ObjTablaCartera;
import cl.bcs.normativa.cartola.utils.JasperUtils;
import cl.bcs.normativa.cartola.utils.RecursosMaster.RecursosDef;
import cl.bcs.normativa.cartola.utils.ReporteMaster;
import cl.bcs.normativa.cartola.utils.ReporteMaster.ReporteDef;


public class ConfiguradorCartola implements JavaCall{
	
	private final String SQL_InsertRecurso = loadResourceToString("cl/bcs/normativa/cartola/sql/ConfiguradorCartola_Insert_Recurso.sql");
	private final String SQL_ObtenerRecursoImagen = loadResourceToString("cl/bcs/normativa/cartola/sql/ConfiguradorCartola_SelectRecurso.sql");
	
	
	public static class ConfiguradorCartolaSalida {		
		public Long 					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		
		public ConfiguradorCartolaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<>();			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ObjRecurso {
	    public Long	 	IdPlantilla;
	    public String	Editable;
	    public String	Descripcion;
	    public String	Grupo;
		public String 	Nombre;
		public String	Tipo;
		public String	Valor;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public static class ObjImagenSQL{
		public String NombreImagen;
		public byte[] Binario;
	}
	
	public static class ObjParametros {
		public String	Grupo;
		public String 	Nombre;
	    public String	Editable;
	    public String	Tipo;
	    public String	Valor;
	    public String	Descripcion;
	};
	
	public static class ConfiguradorCartolaEntrada{
		
		public String Ambiente;
		public String RutaResources;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
		
	public ConfiguradorCartolaSalida logica(Connection con , ConfiguradorCartolaEntrada in) throws Exception {
		ConfiguradorCartolaSalida out = new ConfiguradorCartolaSalida();
		SQL sql = new SQL();
		
		try {

			final String currentPath = in.RutaResources != null ? in.RutaResources : "C:\\SAPBOBSCOM\\SVN_PRE\\Implementacion\\BSCL"; // Para debug cambia la ruta al ambiente local de eclipse 
			final String SP_PREFIX = "cl.bcs.normativa.cartola.";
			
			Long IdEmpresa = 1L;
			String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String MonedaSistema		= ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			String MonedaAdicional		= ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
			String tFechaHoraSistema	= FechaDB.ObtenerFechaHoraSistemaUTC(con, IdEmpresa);
			HashMap<String, Object> Recurso = new HashMap<String, Object>();
			
			/* * 
			 * 1- genera nueva plantilla  . OK
			 * 2- carga jasper  - OK
			 * 3- carga procedimientos - OK
			 * 4- carga graficos
			 * 5- carga parametros (problemas con la carga csv - se estan cargando a mano)
			 * 6- carga config
			 * 7- carga imagenes - OK (Mejorar pq esta en duro)
			 * 8- otros - OK
			 * */
			
			
			// 1
			Long IdPlantilla = GrabarPlantilla (IdEmpresa, "NOR", 1L, tFechaHoraSistema, con);
			System.out.println("Plantilla Generada: " +IdPlantilla);
			
			// 2 y 3
			ReporteMaster reporteMaster = new ReporteMaster(localizacionSistema);
			GraficoMaster graficoMaster = new GraficoMaster(localizacionSistema);
			for (ReporteDef RM : reporteMaster.getReporteMaster()){
				///JASPER
				final String path = "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/rpt/" + RM.nombreJasper + ".jrxml";				
				File fFile=new File( currentPath+path);
				
				if (!fFile.exists()){
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "JASPER", "Archivo no encontrado:"+ RM.nombreJasper ));
					continue;
				};				
				System.out.println("Nombre:"+RM.nombreJasper+" Reporte:"+path);
				
				try (final InputStream is =  new FileInputStream(fFile)) {		
		            final String valor = JasperUtils.compileAndSerializeReport(is);
		        
		            HashMap<String, Object> ObjJasper = new HashMap<String, Object>();
		            ObjJasper.put("IdPlantilla", IdPlantilla);
		            ObjJasper.put("Editable", "NO");		            
		            ObjJasper.put("Descripcion","Binario Jasper del reporte "+RM.nombreJasper);
		            ObjJasper.put("Grupo","SISTEMA");
		            ObjJasper.put("Nombre",RM.nombreBinarioJasper);
		            ObjJasper.put("Tipo","jasper");
		            ObjJasper.put("Valor",valor);
		            Recurso.put(RM.nombreJasper, ObjJasper);		           
		        }catch(Exception er) {
		        	out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "JASPER", "No se puede cargar:"+path));
		        	er.printStackTrace();
					continue;
		        };
		        
		        		        	
				///SERVICIO
		        HashMap<String, Object> ObjServicio = new HashMap<String, Object>();
		        ObjServicio.put("Editable", "NO");
		        ObjServicio.put("Descripcion","Procedimiento asociado al reporte "+RM.nombreJasper);
		        ObjServicio.put("Grupo","SISTEMA");
		        ObjServicio.put("Nombre",RM.nombreProcedimiento);
		        ObjServicio.put("Tipo","java");
		        ObjServicio.put("Valor",SP_PREFIX + RM.nombreProcedimiento);
		        Recurso.put(RM.nombreProcedimiento, ObjServicio);
			};
			for (GraficoDef GM : graficoMaster.getGraficoMaster()){
				///SERVICIO				
				HashMap<String, Object> ObjServicio = new HashMap<String, Object>();
		        ObjServicio.put("Editable", "NO");
		        ObjServicio.put("Descripcion","Procedimiento asociado al grafico "+GM.nombreGrafico);
		        ObjServicio.put("Grupo","SISTEMA");
		        ObjServicio.put("Nombre",GM.nombreProcedimiento);
		        ObjServicio.put("Tipo","java");
		        ObjServicio.put("Valor",SP_PREFIX + GM.nombreProcedimiento);
		        Recurso.put(GM.nombreProcedimiento, ObjServicio);
			};
			
			
			// 5
			// Carga de parametros segun ambiente 
			String pathParam ="";
			
			
			//IMPORTANTE: Al definir nuevos casos, siempre usar minuscula!
			switch  (in.Ambiente.toLowerCase()) {
			
			//MBI
			case "mbi" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaMBI.csv"; 
				break;
			//Sura
			case "sura" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaSura.csv"; 
				break;
			//BancoEstado
			case "bancoestado" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaBancoEstado.csv"; 
				break;
			//Merrill
			case "merrill" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaMerrill.csv"; 
				break;			
			//Fynsa
			case "fynsa" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaFynsa.csv"; 
				break;
			//DEMO
			case "demo" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaDemo.csv"; 
				break;
			//LABO
			case "labo01" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaLabo01.csv"; 
				break;
			case "labo02" : 
				pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaLabo02.csv"; 
				break;
			//Scotia
			case "scotia":
			    pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaScotia.csv";
				break;
			//Bice
			case "bice":
			    pathParam= "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/csv/parametrosCartolaBice.csv";
				break;
				
			default :
				throw new IllegalArgumentException("Ambiente no reconocido: " + in.Ambiente);
			
			}
			
			//;
			File fFileParam=new File( currentPath+pathParam);
			
			if (!fFileParam.exists()){
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "PARAMETROS", "Archivo no encontrado: " + pathParam));
			};
			try (final BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(fFileParam),"UTF-8"))){
				try (final CSVReader reader = new CSVReader(is, ';')) {
		            reader.readNext(); 
		            String[] line = reader.readNext();
		            while (line != null) {            	
		            	// Debe mantenerse el orden los campos.
		            	HashMap<String, Object> ObjParametro = new HashMap<String, Object>();
				        ObjParametro.put("Grupo",line[0]);
				        ObjParametro.put("Nombre",line[1]);
				        ObjParametro.put("Editable", line[2]);
				        ObjParametro.put("Tipo",line[3]);
				        ObjParametro.put("Valor",line[4]);
				        ObjParametro.put("Descripcion",line[5]);
				        Recurso.put(line[1], ObjParametro);
		                line = reader.readNext();
		            }
		        }
	        };
					
	        
			// 7  --Crear enum con imagenes para recorrer
	        final String pathIMG = "/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/img/";
	        RecursosMaster recursosMaster = new RecursosMaster();
	        List <RecursosDef> recursosM= recursosMaster.getRecursosMaster();
	        List <String> nombreRecurso = new ArrayList<>();
	        HashMap <String, Object> param = new HashMap <>();
	        HashMap<String, byte[]> recursosSQL = new HashMap<>();

	        
	        for (RecursosDef recDef : recursosM) {
	        	if (recDef.Nombre != null) {
	        		nombreRecurso.add(recDef.Key);
	        	}
	        }
	        
	        if (!nombreRecurso.isEmpty()) {
	        	param.put("Recurso", nombreRecurso);
	        }
	        
	        List <ObjImagenSQL> recursos = sql.query(con, SQL_ObtenerRecursoImagen, param, ObjImagenSQL.class);
	        
	        for (ObjImagenSQL rec : recursos) {
	        	recursosSQL.put(rec.NombreImagen, rec.Binario);
	        }
	        
	        
	        for (RecursosDef R_M : recursosMaster.getRecursosMaster() ){
	        	if (R_M.Tipo.equalsIgnoreCase("IMAGEN")){			
					if(!recursosSQL.containsKey(R_M.Key)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "IMAGEN", "Recurso no encontrado: " + R_M.Key));
						continue;
					};			
					try {
						final byte[] bytes = recursosSQL.get(R_M.Key);
						final String valor = Base64.encodeBase64String(bytes);	            		
	
			            HashMap<String, Object> ObjImagen = new HashMap<String, Object>();
			            ObjImagen.put("IdPlantilla", IdPlantilla);
			            ObjImagen.put("Editable", R_M.Editable);		            
			            ObjImagen.put("Descripcion",R_M.Descripcion);
			            ObjImagen.put("Grupo",R_M.Grupo);
			            ObjImagen.put("Nombre",R_M.Nombre);
			            ObjImagen.put("Tipo",R_M.Tipo);
			            ObjImagen.put("Valor",valor);
			            Recurso.put(R_M.Nombre, ObjImagen);		            
			        }catch(Exception er) {
			        	out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "IMAGEN", "No se puede cargar: "+R_M.NombreArchivo));
			        	er.printStackTrace();
			        };
	        	};
			};
	        
			
	        // 8
	        HashMap<String, Object> ObjCodigoMonedaSistema = new HashMap<String, Object>();
	        ObjCodigoMonedaSistema.put("IdPlantilla", IdPlantilla);
	        ObjCodigoMonedaSistema.put("Grupo","GENERAL");
	        ObjCodigoMonedaSistema.put("Tipo","TEXTO");
	        ObjCodigoMonedaSistema.put("Editable", "NO");
	        ObjCodigoMonedaSistema.put("Nombre","CodigoMonedaLocal");
	        ObjCodigoMonedaSistema.put("Descripcion","Código moneda local (Desde tabla moneda)");
	        ObjCodigoMonedaSistema.put("Valor",MonedaSistema);
            Recurso.put("CodigoMonedaLocal", ObjCodigoMonedaSistema);
            
            HashMap<String, Object> ObjNombreMonedaSistema = new HashMap<String, Object>();
            String NombreMonedaSistema = (MonedaSistema.equals("PEN") ? "Soles" : "Pesos");
            ObjNombreMonedaSistema.put("IdPlantilla", IdPlantilla);
            ObjNombreMonedaSistema.put("Grupo","GENERAL");
            ObjNombreMonedaSistema.put("Tipo","TEXTO");
            ObjNombreMonedaSistema.put("Editable", "NO");
            ObjNombreMonedaSistema.put("Nombre","MonedaLocal");
            ObjNombreMonedaSistema.put("Descripcion","Nombre moneda local (Desde tabla moneda)");
            ObjNombreMonedaSistema.put("Valor",NombreMonedaSistema);
            Recurso.put("MonedaLocal", ObjNombreMonedaSistema);
	        
            HashMap<String, Object> ObjCodigoMonedaAdicional = new HashMap<String, Object>();
            ObjCodigoMonedaAdicional.put("IdPlantilla", IdPlantilla);
            ObjCodigoMonedaAdicional.put("Grupo","GENERAL");
            ObjCodigoMonedaAdicional.put("Tipo","TEXTO");
            ObjCodigoMonedaAdicional.put("Editable", "NO");
            ObjCodigoMonedaAdicional.put("Nombre","CodigoMonedaAdicional");
	        ObjCodigoMonedaAdicional.put("Descripcion","Código moneda adicional (Desde tabla moneda)");	        
	        ObjCodigoMonedaAdicional.put("Valor",MonedaAdicional);
            Recurso.put("CodigoMonedaAdicional", ObjCodigoMonedaAdicional);
	        
            HashMap<String, Object> ObjNombreMonedaAdicional = new HashMap<String, Object>();
            String NombreMonedaAdicional = (MonedaAdicional.equals("USD") ? "Dólares Americanos" : "");
            ObjNombreMonedaAdicional.put("IdPlantilla", IdPlantilla);
            ObjNombreMonedaAdicional.put("Grupo","GENERAL");
            ObjNombreMonedaAdicional.put("Tipo","TEXTO");
            ObjNombreMonedaAdicional.put("Editable", "NO");
            ObjNombreMonedaAdicional.put("Nombre","MonedaAdicional");
            ObjNombreMonedaAdicional.put("Descripcion","Nombre moneda adicional (Desde tabla moneda)");	        
            ObjNombreMonedaAdicional.put("Valor",NombreMonedaAdicional);
            Recurso.put("MonedaAdicional", ObjNombreMonedaAdicional);
            
            //Grabar Recursos
			GrabarRecursos (IdEmpresa, "NOR", 0L, IdPlantilla, Recurso, con);
		}
		finally {
			System.out.println("END");
		}
		return out;
	};
	
	public Long GrabarPlantilla (Long IdEmpresa, String IdModulo, Long IdUsuario, String FechaSistema, Connection con) throws SQLException{
		SQL sql = new SQL();
		Long IdPlantillaGenerada = 0L;

		try {			
			HashMap<String,Object> parmsInSP =new HashMap<String,Object>();
			parmsInSP.put("IdEmpresa", IdEmpresa);
			parmsInSP.put("IdUsuario", IdUsuario);
			parmsInSP.put("IdModulo", IdModulo);
			parmsInSP.put("IdPlantilla", 0L);
			parmsInSP.put("Nombre", "CARTOLA_MASTER");
			parmsInSP.put("Vigente", "SI");
			parmsInSP.put("VigenteDesde", FechaSistema);
			
			HashMap<String,Class> parmsOutSP =new HashMap<String,Class>();
			parmsOutSP.put("p_out_id", Long.class);
			parmsOutSP.put("p_out_AdvertenciasContador", Long.class);
			parmsOutSP.put("p_out_ErroresContador", Long.class);
			
			
			String queryCall = "CALL \"BSCL.Normativa.BD.Servicios::CAR.GrabarPlantilla\"( "
	        		+ "p_IdEmpresa=>:IdEmpresa,"
	        		+ "p_IdModulo=>:IdModulo,"
	        		+ "p_IdUsuario=>:IdUsuario,"
	        		+ "p_IdPlantilla=>:IdPlantilla,"
	        		+ "p_Nombre=>:Nombre,"
					+ "p_Vigente=>:Vigente,"
					+ "p_VigenteDesde=>:VigenteDesde,"
					+ "p_out_id=>@p_out_id,"
					+ "p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,"
					+ "p_out_ErroresContador=>@p_out_ErroresContador)";
					
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsInSP, parmsOutSP);
			IdPlantillaGenerada = (Long)rpt.get("p_out_id");
			
		} catch(Exception er) {
			System.out.println(er);
			con.rollback();
		};
		
		return IdPlantillaGenerada;
	};
	
	
	public String GrabarRecursos (Long IdEmpresa, String IdModulo, Long IdUsuario, Long IdPlantilla, HashMap<String, Object> Recurso, Connection con) throws SQLException{
		SQL sql = new SQL();
		
		try {
			List<ObjRecurso> recursos = new ArrayList<ObjRecurso>();
			for (String  key : Recurso.keySet()){	
				HashMap<String, String> RCS = (HashMap<String, String>) Recurso.get(key);
				
				ObjRecurso p = new ObjRecurso();
				for (String  k : RCS.keySet()){					
					if (k.equalsIgnoreCase("Editable"))
						p.Editable		= RCS.get(k);
					if (k.equalsIgnoreCase("Descripcion"))
						p.Descripcion	= RCS.get(k);
					if (k.equalsIgnoreCase("Grupo"))
						p.Grupo			= RCS.get(k);
					if (k.equalsIgnoreCase("Nombre"))
						p.Nombre		= RCS.get(k);
					if (k.equalsIgnoreCase("Tipo"))
						p.Tipo			= RCS.get(k);
					if (k.equalsIgnoreCase("Valor"))
						p.Valor			= RCS.get(k);
				};
				p.IdEstado	= "ACT";
				p.IdPlantilla = IdPlantilla;
				recursos.add(p);				
			};
			
			sql.batch(con, SQL_InsertRecurso, recursos, ObjRecurso.class);			
		} finally {
			
		};
		
		return "ok";
	};
	
	/*public String GrabarRecursos (Long IdEmpresa, String IdModulo, Long IdUsuario, Long IdPlantilla, HashMap<String, Object> Recurso, Connection con) throws SQLException{
		SQL sql = new SQL();
		
		try {
			List<ObjRecurso> recursos = new ArrayList<ObjRecurso>();
			for (String  key : Recurso.keySet()){	
				HashMap<String, String> RCS = (HashMap<String, String>) Recurso.get(key);
				
				ObjRecurso p = new ObjRecurso();
				for (String  k : RCS.keySet()){					
					if (k.equalsIgnoreCase("Editable"))
						p.Editable		= RCS.get(k);
					if (k.equalsIgnoreCase("Descripcion"))
						p.Descripcion	= RCS.get(k);
					if (k.equalsIgnoreCase("Grupo"))
						p.Grupo			= RCS.get(k);
					if (k.equalsIgnoreCase("Nombre"))
						p.Nombre		= RCS.get(k);
					if (k.equalsIgnoreCase("Tipo"))
						p.Tipo			= RCS.get(k);
					if (k.equalsIgnoreCase("Valor"))
						p.Valor			= RCS.get(k);
				};
				p.IdEstado	= "ACT";
				p.IdPlantilla = IdPlantilla;
				recursos.add(p);				
			};
			
			
			HashMap<String,Object> parmsInSP =new HashMap<String,Object>();
			parmsInSP.put("IdEmpresa", IdEmpresa);
			parmsInSP.put("IdUsuario", IdUsuario);
			parmsInSP.put("IdModulo", IdModulo);
			parmsInSP.put("p_Recursos", new ArrayList<ObjRecurso>()); // recursos);
			
			SQL.exec(con,
        			"CREATE LOCAL TEMPORARY TABLE #CallServicio_Recursos (\"IdRecurso\" BIGINT,\"IdPlantilla\" BIGINT,\"Editable\" NVARCHAR(2),"
        			+ "\"Descripcion\" NVARCHAR(100),\"Grupo\" NVARCHAR(100),\"Nombre\" NVARCHAR(100),\"Tipo\" NVARCHAR(100),\"Valor\" nclob,\"IdEstado\" NVARCHAR(3))");
			sql.batch(con,
					"INSERT INTO #CallServicio_Recursos (\"IdRecurso\",\"IdPlantilla\",\"Editable\",\"Descripcion\",\"Grupo\",\"Nombre\",\"Tipo\",\"Valor\",\"IdEstado\") "
					+ "VALUES (0,:IdPlantilla,:Editable,:Descripcion,:Grupo,:Nombre,:Tipo,:Valor,:IdEstado)", recursos, ObjRecurso.class);
			List<ObjRecurso> pew = sql.query(con, "SELECT * FROM #CallServicio_Recursos", new HashMap<String, Object>(), ObjRecurso.class);
						
			String queryCall = "CALL \"BSCL.Normativa.BD.Servicios::CAR.GrabarRecurso\"( "
	        		+ "p_IdEmpresa=>:IdEmpresa,"
	        		+ "p_IdModulo=>:IdModulo,"
	        		+ "p_IdUsuario=>:IdUsuario,"
	        		+ "p_Recursos=>#CallServicio_Recursos)";
			
			
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsInSP, new HashMap<>());
		} finally {
			SQL.exec(con, "DROP TABLE #CallServicio_Recursos");
		};
		
		return "ok";
	};*/
		
	public static BufferedImage getBufferedImage(
            final String valor)
                    throws IOException {
        final byte[] bytes = Base64.decodeBase64(valor);
        try (final InputStream in = new ByteArrayInputStream(bytes)) {
            // ADVERTENCIA: Esto no soporta JPG.
            return ImageIO.read(in);
        }
    };
    
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConfiguradorCartolaEntrada in = gson.fromJson(jin.toString(), ConfiguradorCartolaEntrada.class);
		ConfiguradorCartolaSalida out=logica(conn,in);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	public static void main(String[] args) {
	    Connection con = null;
	    String resources = null;
	    String ambiente = "";

	    try {
	        if (args != null && args.length > 0 ) {
	        	if (args.length == 7 ) {
	     
	        		// Ejecución desde Jenkins
	        		String ip = args[0];
	        		String puerto = args[1];
	        		String schema = args[2];
	        		String usuario = args[3];
	        		String password = args[4];
	        		
	        		ambiente = args[5];
	        		resources = args[6];
	        		
	        		System.out.println("Ejecutando ConfiguradorCartola en ambiente: " + ambiente);
	        		con = PlataformaUtils.getHanaConnection(ip, puerto, schema, usuario, password);
	        	} else {
	        		System.out.println("Faltan argumentos para ejecucion, se cancela.");
	        		return; 
	        		
	        	}
	        } else {
	            // Ejecución local (debug)
	            ambiente = "Fynsa";
	            System.out.println("Ejecutando en modo debug (ambiente por defecto: " + ambiente + ")");
	            con = PlataformaUtils.getPropertiesConnection("fynsapre", "connections.properties");
	        }

	        ejecutarConfigurador(con, ambiente, resources);

	    } catch (Exception e) {
	        System.err.println("Error al ejecutar ConfiguradorCartola: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        if (con != null) {
	            try {
	                con.close();
	                System.out.println("Conexión cerrada correctamente.");
	            } catch (Exception ex) {
	                System.err.println("Error al cerrar la conexión: " + ex.getMessage());
	            }
	        }
	    }
	    
	}

	/**
	 * Ejecuta el proceso principal de ConfiguradorCartola con la conexión y ambiente indicados.
	 */
	private static void ejecutarConfigurador(Connection con, String ambiente, String resources) {
	    try {
	        ConfiguradorCartola kk = new ConfiguradorCartola();
	        ConfiguradorCartolaEntrada in = new ConfiguradorCartolaEntrada();
	        in.Ambiente = ambiente;
	        in.RutaResources = resources;

	        ConfiguradorCartolaSalida salida = kk.logica(con, in);
	        
	        con.commit();

	        System.out.println("Proceso finalizado correctamente para ambiente: " + ambiente);

	    } catch (Exception e) {
	        try {
	            con.rollback();
	            System.err.println("Transacción revertida por error: " + e.getMessage());
	        } catch (Exception rollbackEx) {
	            System.err.println("Error durante rollback: " + rollbackEx.getMessage());
	        }
	        e.printStackTrace();
	    }
	}

}
