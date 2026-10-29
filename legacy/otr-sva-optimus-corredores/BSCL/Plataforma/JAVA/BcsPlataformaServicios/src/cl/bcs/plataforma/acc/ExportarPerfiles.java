package cl.bcs.plataforma.acc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class ExportarPerfiles implements JavaCall  {
	
	private final Log log = new Log("Exportar Perfiles Completos");
	private final String SQL_SELECT_PERFILES = loadResourceToString("cl/bcs/plataforma/acc/sql/ConsultaPerfilesAcceso_Select.sql");
	
	
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	public static class EntradaPerfilesAccesos {
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public String  p_Ruta;
	}
	
	public static class SalidaPerfilesAccesos{
		public Long   P_Out_errores;
		public String P_RutaArchivo;
		public String P_RutaNombreArchivo;
	}
	
	public static class PerfilAcceso{
		public Long 	IdPerfil;
		public String   NombrePerfil;
		public String 	IdEstado;
		public String	EstadoPerfil;
		public Long     IdAcceso;
		public String   NombreAcceso;
		public String   TipoAcceso;
		public int      IdMenu;
		public String   NombreMenu;
	}
	
	
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaPerfilesAccesos in=gson.fromJson(jin.toString() ,EntradaPerfilesAccesos.class);
		SalidaPerfilesAccesos out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	public SalidaPerfilesAccesos logica(Connection con, EntradaPerfilesAccesos entrada) throws Exception {
		
		SalidaPerfilesAccesos salida = new SalidaPerfilesAccesos();
		salida.P_Out_errores = new Long(0);
		SQL sql=new SQL();
		
		HashMap<String,Object> consultaPerfil = new HashMap<String,Object>();
		consultaPerfil.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<PerfilAcceso> listaPerfilAcceso = sql.query(con,SQL_SELECT_PERFILES,consultaPerfil,PerfilAcceso.class);
		
	    salida.P_RutaNombreArchivo = "ACC/ReportePerfiles" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
		salida.P_RutaArchivo = entrada.p_Ruta + salida.P_RutaNombreArchivo;
		
		File folder = new File(entrada.p_Ruta+"/ACC");
		if(!folder.exists()){
			folder.mkdirs();
		}		
		
		FileOutputStream os = new FileOutputStream(salida.P_RutaArchivo);
		os.write(0xef);
		os.write(0xbb);
		os.write(0xbf);
		CSVWriter EscribeCSV=new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');
	
		try{
			String [] FilaEscribePerfilAccesos= new String[]{
					"Nombre Perfil",
					"Estado",
					"Modulo Menu",
					"Accesos"
					};
			
			EscribeCSV.writeNext(FilaEscribePerfilAccesos);
			log.write("Numero acceso en perfiles",listaPerfilAcceso.size());
			int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
			Fecha.setLocalOffSet(SegundosZonaHoraria);
			
			
			FilaEscribePerfilAccesos= new String[]{
					listaPerfilAcceso.get(0).NombrePerfil,
					listaPerfilAcceso.get(0).EstadoPerfil,
					listaPerfilAcceso.get(0).NombreMenu,
					listaPerfilAcceso.get(0).NombreAcceso};
					
			
			EscribeCSV.writeNext(FilaEscribePerfilAccesos);
			
			for(int i=1;i<listaPerfilAcceso.size();i++){
					String NombrePerfil="";
					String EstadoPerfil="";
					String NombreModulo="";
					
					
					if(!(listaPerfilAcceso.get(i).NombrePerfil.equals(listaPerfilAcceso.get(i-1).NombrePerfil))){						
						
						NombrePerfil=listaPerfilAcceso.get(i).NombrePerfil;	
						EstadoPerfil=listaPerfilAcceso.get(i).EstadoPerfil;
						NombreModulo=listaPerfilAcceso.get(i).NombreMenu;
					}else{
						if(listaPerfilAcceso.get(i).IdMenu != listaPerfilAcceso.get(i-1).IdMenu){
							NombreModulo=listaPerfilAcceso.get(i).NombreMenu;
						}
					}
					FilaEscribePerfilAccesos= new String[]{NombrePerfil,EstadoPerfil,NombreModulo,listaPerfilAcceso.get(i).NombreAcceso};
					EscribeCSV.writeNext(FilaEscribePerfilAccesos);
					
			}
			
		}catch (Exception e){
			e.printStackTrace();
			salida.P_Out_errores = new Long(1);
		}
		finally {
			try {
				EscribeCSV.close();
			}catch (Exception e){
				e.printStackTrace();
				salida.P_Out_errores = new Long(1);
			}
		}
		
		
		log.writeAsJson("SALIDA:",salida);
		return salida;

	}
	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) {       
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			System.out.println("== COMIENZA==");

			try{		
				EntradaPerfilesAccesos entrada = new EntradaPerfilesAccesos();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "PLA";
				entrada.p_Ruta     = "C:/reporte/";				
				
	 			JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        ExportarPerfiles demo=new ExportarPerfiles();		
		    	System.out.println(demo.call(con, dataJsonIn).toString());	
		    	con.commit();
		    	
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
