package cl.bcs.plataforma.acc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.utils.Utils;

public class ExportarRoles implements JavaCall {

	
	private final Log log = new Log("Exportar RolesPerfil Completos");
	private final String SQL_SELECT_ROL_PERFILES_ACCESOS = loadResourceToString("cl/bcs/plataforma/acc/sql/ConsultaRolesPerfil_Select.sql");	
		
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	public static class EntradaRolesPerfil {
		public Long    p_IdEmpresa;
		public Long    p_IdUsuario;
		public String  p_IdModulo;
		public String  p_Ruta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaRolesPerfil{
		public Long   P_Out_errores;
		public String P_RutaArchivo;
		public String P_RutaNombreArchivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjRolesPerfilAccesos{
		public Long IdEmpresa; 
		public String NombreRol;
		public Long IdRol;
		public String EstadoRol;
		public String NombrePerfil;
		public Long IdPerfil;
		public String NombreAcceso;
		public String TipoAcceso;
		public String Modulo;
		public String PrimerNivel;
		public String SegundoNivel;
		public String TercerNivel;
		public Long IdRol1;
		public Long Padre1;
		public Long Padre2;
		public Long Padre3;
		public Long Padre4;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
		
	
	
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaRolesPerfil in=gson.fromJson(jin.toString() ,EntradaRolesPerfil.class);
		SalidaRolesPerfil out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	public SalidaRolesPerfil logica(Connection con, EntradaRolesPerfil entrada) throws Exception {
		
		SalidaRolesPerfil salida = new SalidaRolesPerfil();
		salida.P_Out_errores = new Long(0);
		SQL sql=new SQL();
		
		HashMap<String,Object> consultaRPA = new HashMap<String,Object>();
		consultaRPA.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<ObjRolesPerfilAccesos> listaRPA = sql.query(con,SQL_SELECT_ROL_PERFILES_ACCESOS,consultaRPA,ObjRolesPerfilAccesos.class);
		
	    salida.P_RutaNombreArchivo = "ACC/ReporteRolesPerfil" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
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
			
			String [] FilaEscribeRolesPerfils= new String[]{
					"Nombre Rol",
					"Estado Rol",
					"Nombre Perfil",
					"Modulo Menu",
					"Menu",
					"Pestaña",
					"Sub Pestaña",
					"Boton"
					};
			
			EscribeCSV.writeNext(FilaEscribeRolesPerfils);
			log.write("Numero acceso en Roles");
			int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
			Fecha.setLocalOffSet(SegundosZonaHoraria);
			
			ObjRolesPerfilAccesos accesoAnterior = new ObjRolesPerfilAccesos();
			
			for(ObjRolesPerfilAccesos acceso : listaRPA){
				boolean mismoRol = acceso.NombreRol.equalsIgnoreCase(accesoAnterior.NombreRol);
				boolean mismoPerfil = acceso.NombrePerfil.equalsIgnoreCase(accesoAnterior.NombrePerfil);
				boolean mismoModulo = acceso.Modulo.equalsIgnoreCase(accesoAnterior.Modulo);
				boolean mismoAcceso = acceso.NombreAcceso.equalsIgnoreCase(accesoAnterior.NombreAcceso);
				boolean mismoPrimerNivel = acceso.PrimerNivel.equalsIgnoreCase(accesoAnterior.PrimerNivel);
				boolean mismoSegundoNivel = acceso.SegundoNivel.equalsIgnoreCase(accesoAnterior.SegundoNivel);
				accesoAnterior = acceso;
				String[] cabecera = new String[]{
						mismoRol ? "" : acceso.NombreRol,
						mismoRol ? "" : acceso.EstadoRol,
						mismoPerfil ? "" : acceso.NombrePerfil,
						mismoModulo ? "" : acceso.Modulo,
						mismoAcceso ? "" : acceso.NombreAcceso
				};
				if(acceso.PrimerNivel.isEmpty()){
					EscribeCSV.writeNext(new String[]{
						cabecera[0],
						cabecera[1],
						cabecera[2],
						cabecera[3],
						cabecera[4],
						"",
						"",
						""
					});
					continue;
				}
				if(!acceso.PrimerNivel.isEmpty() && acceso.SegundoNivel.isEmpty()){
					EscribeCSV.writeNext(new String[]{
						cabecera[0],
						cabecera[1],
						cabecera[2],
						cabecera[3],
						cabecera[4],
						"",
						"",
						acceso.PrimerNivel
					});
					continue;
				}
				if(!acceso.PrimerNivel.isEmpty() && !acceso.SegundoNivel.isEmpty() && acceso.TercerNivel.isEmpty()){
					EscribeCSV.writeNext(new String[]{
						cabecera[0],
						cabecera[1],
						cabecera[2],
						cabecera[3],
						cabecera[4],
						mismoPrimerNivel ? "" : acceso.PrimerNivel,
						"",
						acceso.SegundoNivel
					});
					continue;
				}
				if(!acceso.PrimerNivel.isEmpty() && !acceso.SegundoNivel.isEmpty() && !acceso.TercerNivel.isEmpty()){
					EscribeCSV.writeNext(new String[]{
						cabecera[0],
						cabecera[1],
						cabecera[2],
						cabecera[3],
						cabecera[4],
						mismoPrimerNivel ? "" : acceso.PrimerNivel,
						mismoSegundoNivel ? "" : acceso.SegundoNivel,
						acceso.TercerNivel
					});
					continue;
				}
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
		try (Connection con =  PlataformaUtils.getMBPEConnection()){
			System.out.println("== COMIENZA==");

			try{		
				EntradaRolesPerfil entrada = new EntradaRolesPerfil();
				entrada.p_IdEmpresa = new Long(1);
				entrada.p_IdUsuario = new Long(21);
				entrada.p_IdModulo = "PLA";
				entrada.p_Ruta     = "C:/reporte/";				
				
	 			JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        ExportarRoles demo=new ExportarRoles();		
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
