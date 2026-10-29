package cl.bcs.socionegocio.blo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambioEstadoMasivoClientesPortafolios implements JavaCall {	
	private final Log log = new Log("SOC_CAMBIOESTADO_CLI_PORT");
	
	//QUERIES
	private final String SQL_SELECT_DATOSCONSULTA="select "
			+ "per.\"IdPersona\",per.\"Tipo\" as \"TipoPersona\",di.\"IdTipoDocumentoIdentidad\",di.\"DocumentoIdentidad\","
			+ "cli.\"IdCliente\",cli.\"IdEstado\" as \"IdEstadoCliente\","
			+ "por.\"IdPortafolio\",por.\"CodigoPortafolio\",por.\"IdEstado\" as \"IdEstadoPortafolio\","
			+ "cli.\"IdTipoBloqueo\" as \"IdTipoBloqueoCliente\", cli.\"IdTipoDesbloqueo\" as \"IdTipoDesbloqueoCliente\","
			+ "cli.\"ComentarioBloqueo\" as \"ComentarioBloqueoCliente\",cli.\"ComentarioDesbloqueo\" as \"ComentarioDesbloqueoCliente\","
			+ "por.\"IdTipoBloqueo\" as \"IdTipoBloqueoPortafolio\", por.\"IdTipoDesbloqueo\" as \"IdTipoDesbloqueoPortafolio\","
			+ "por.\"ComentarioBloqueo\" as \"ComentarioBloqueoPortafolio\",por.\"ComentarioDesbloqueo\" as \"ComentarioDesbloqueoPortafolio\" "
			+ "from \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" di "
			+ "join \"BSCL.SocioNegocio.BD::PER.Persona\" per on per.\"IdPersona\"=di.\"IdPersona\" "
			+ "join \"BSCL.SocioNegocio.BD::CLI.Cliente\" cli on cli.\"IdPersona\"=per.\"IdPersona\" "
			+ "join \"BSCL.Portafolio.BD::POR.Portafolio\" por on por.\"IdCliente\"=cli.\"IdCliente\" "
			+ "where cli.\"IdEmpresa\"=:IdEmpresa and di.\"DocumentoIdentidad\" in (:ListaDocumentoIdentidad);";
	
	private final String SQL_UPDATE_CLIENTE_BLO = "update \"BSCL.SocioNegocio.BD::CLI.Cliente\" set "
			+ "\"IdEstado\"='BLO', \"IdTipoBloqueo\"=:IdTipoCambioEstado, \"ComentarioBloqueo\"='BLOQUEO MASIVO' ,"
			+ "\"FechaBloqueo\" = CURRENT_UTCTIMESTAMP, \"FechaModificacion\" = CURRENT_UTCTIMESTAMP "
			+ "where \"IdCliente\"=:IdEntidad";
	private final String SQL_UPDATE_CLIENTE_ACT = "update \"BSCL.SocioNegocio.BD::CLI.Cliente\" set "
			+ "\"IdEstado\"='ACT', \"IdTipoDesbloqueo\"=:IdTipoCambioEstado, \"ComentarioDesbloqueo\"='ACTIVACIÓN MASIVO' ,"
			+ "\"FechaDesbloqueo\" = CURRENT_UTCTIMESTAMP, \"FechaModificacion\" = CURRENT_UTCTIMESTAMP "
			+ "where \"IdCliente\"=:IdEntidad";
	private final String SQL_UPDATE_PORTAFOLIO_BLO = "update \"BSCL.Portafolio.BD::POR.Portafolio\" set "
			+ "\"IdEstado\"='BLO', \"IdTipoBloqueo\"=:IdTipoCambioEstado, \"ComentarioBloqueo\"='BLOQUEO MASIVO' ,"
			+ "\"FechaBloqueo\" = CURRENT_UTCTIMESTAMP, \"FechaModificacion\" = CURRENT_UTCTIMESTAMP "
			+ "where \"IdPortafolio\"=:IdEntidad";
	private final String SQL_UPDATE_PORTAFOLIO_ACT = "update \"BSCL.Portafolio.BD::POR.Portafolio\" set "
			+ "\"IdEstado\"='ACT', \"IdTipoDesbloqueo\"=:IdTipoCambioEstado, \"ComentarioDesbloqueo\"='ACTIVACIÓN MASIVO' ,"
			+ "\"FechaDesbloqueo\" = CURRENT_UTCTIMESTAMP, \"FechaModificacion\" = CURRENT_UTCTIMESTAMP "
			+ "where \"IdPortafolio\"=:IdEntidad";
	
	/*******************
	 * pojos
	 *******************/
	public static class DatosConsulta{
		public Long 	IdPersona;
		public String	TipoPersona;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;
		public Long 	IdCliente;
		public String	IdEstadoCliente;
		public Long 	IdPortafolio;
		public String	CodigoPortafolio;
		public String 	IdEstadoPortafolio;
		public String 	IdTipoBloqueoCliente;
		public String 	IdTipoDesbloqueoCliente;
		public String 	ComentarioBloqueoCliente;
		public String 	ComentarioDesbloqueoCliente;
		public String 	IdTipoBloqueoPortafolio;
		public String 	IdTipoDesbloqueoPortafolio;
		public String 	ComentarioBloqueoPortafolio;
		public String 	ComentarioDesbloqueoPortafolio;
	}
	
	public static class DatosActualizar{
		public Long 	IdEntidad;
		public String	IdTipoCambioEstado;
	}
	
	public static class DocumentoIdentidad{
		public String DocumentoIdentidad;
		public DocumentoIdentidad(){}
		public DocumentoIdentidad(String DocumentoIdentidad){
			this.DocumentoIdentidad=DocumentoIdentidad;
		}
	}
	
	/*public static class Id{
		public Long	IdRegistro;
	}*/
	
	/*******************
	 * clases in/out
	 *******************/
	public static class InCambioEstadoMasivoClientesPortafolios {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_SimularOperacion;
		public String 		p_Accion; //ACT,BLO
		public List<Registro> p_Registros;
		public static class Registro{
			public String	TipoPersona;
			public String	TipoDocumentoIdentidad;
			public String	DocumentoIdentidad;
			public String	CodigoPortafolio;
			public String	TipoCambioEstadoCliente;
			public String	TipoCambioEstadoPortafolio;
		}
		public InCambioEstadoMasivoClientesPortafolios(){
			p_Registros = new ArrayList<Registro>();
		}
	}
	
	public static class OutCambioEstadoMasivoClientesPortafolios{
		public Long 			P_OUT_ERRORESCONTADOR;
		public String			P_OUT_DESCRIPCION;
		public List<Registro> 	P_OUT_OPERACIONES_REALIZADAS;
		public static class Registro{
			public String	Entidad; 		//CLIENTE,PORTAFOLIO
			public String 	Id;				//Cliente:Documento Identidad, Portafolio
			public String	IdEstado;		//ACT,BLO
			public String	Resultado; 		//EXITO,ERROR
			public String	Descripcion; 	//Texto Descriptivo
		}
		public OutCambioEstadoMasivoClientesPortafolios(){
			P_OUT_OPERACIONES_REALIZADAS = new ArrayList<Registro>();
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InCambioEstadoMasivoClientesPortafolios in=gson.fromJson(jin.toString() ,InCambioEstadoMasivoClientesPortafolios.class);
		OutCambioEstadoMasivoClientesPortafolios out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		//Settings by file
        String dbURL  = "jdbc:sap://bcshanadev:30215/?";
        String dbprop = "currentSchema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "JGARCES";
        String pass   = "Bolsa199";		
        //Datasource
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        //prueba de la clase
		try {
			Connection con = ds.getConnection();
			InCambioEstadoMasivoClientesPortafolios entrada = new InCambioEstadoMasivoClientesPortafolios();
			entrada.p_IdEmpresa=1L;
			entrada.p_IdUsuario=37L;
			entrada.p_IdModulo="SOC";
			entrada.p_SimularOperacion="N";
			entrada.p_Accion="BLO";//BLO=>BMUERT,BFRAUD,BTEMPO||ACT=>
			InCambioEstadoMasivoClientesPortafolios.Registro item1 = new InCambioEstadoMasivoClientesPortafolios.Registro();
			item1.TipoPersona="N";
			item1.TipoDocumentoIdentidad="R";
			item1.DocumentoIdentidad="16929326-0";
			item1.CodigoPortafolio="1";
			item1.TipoCambioEstadoCliente="BNOUSO";
			item1.TipoCambioEstadoPortafolio="BNOUSO";
			entrada.p_Registros.add(item1);
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambioEstadoMasivoClientesPortafolios demo=new CambioEstadoMasivoClientesPortafolios();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e){e.printStackTrace();}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCambioEstadoMasivoClientesPortafolios logica(Connection con, InCambioEstadoMasivoClientesPortafolios entrada) throws Exception {
		boolean debug = true;
		log.write("[INI]PROCESAMIENTO CAMBIO ESTADO CLIENTE/PORTAFOLIO, DEBUG="+debug);
		OutCambioEstadoMasivoClientesPortafolios salida = new OutCambioEstadoMasivoClientesPortafolios();
		salida.P_OUT_ERRORESCONTADOR = 1L;
		SQL sql=new SQL();
		String fechaSistema = Utils.getFechaSistema(con,"BSCL",entrada.p_IdEmpresa);
		List<String> itemsCodigoEmpleado = new ArrayList<String>();
		List<String> itemsDocumentoIdentidad = new ArrayList<String>();
		List<Long> itemsIdCliente = new ArrayList<Long>();
		if(entrada.p_Registros.size()==0){
			log.write("[FIN]NO HAY DATOS PARA PROCESAR");
			return salida;
		}
		if(!(entrada.p_Accion.equals("ACT")||entrada.p_Accion.equals("BLO"))){
			log.write("[FIN]ACCION INGRESADA NO ES VALIDA");
			return salida;
		}
		salida.P_OUT_ERRORESCONTADOR++;
		List<String> listdi = new ArrayList<String>(); 
		log.write("obtiene set datos a partir de documento identidad");
		for(InCambioEstadoMasivoClientesPortafolios.Registro item:entrada.p_Registros){
			listdi.add(item.DocumentoIdentidad);
		}
		HashMap<String,Object> parmsconsulta =new HashMap<String,Object>();
		parmsconsulta.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsconsulta.put("ListaDocumentoIdentidad", listdi);
		List<DatosConsulta> datosConsulta = sql.query(con, SQL_SELECT_DATOSCONSULTA,parmsconsulta,DatosConsulta.class);
		log.write("prepara set de datos de salida y datos para el update");
		List<DatosActualizar> dacliACT=new ArrayList<DatosActualizar>();
		List<DatosActualizar> dacliBLO=new ArrayList<DatosActualizar>();
		List<DatosActualizar> daporACT=new ArrayList<DatosActualizar>();
		List<DatosActualizar> daporBLO=new ArrayList<DatosActualizar>();
		//asigna ids para procesamiento.
		
		for(InCambioEstadoMasivoClientesPortafolios.Registro itemin : entrada.p_Registros){
			log.write("***** siguiente línea *****");
			//registro de salida
			OutCambioEstadoMasivoClientesPortafolios.Registro itemoutCli=new OutCambioEstadoMasivoClientesPortafolios.Registro();
			itemoutCli.Entidad="CLIENTE";
			itemoutCli.Id=itemin.DocumentoIdentidad;
			itemoutCli.IdEstado=entrada.p_Accion;
			itemoutCli.Resultado="";
			itemoutCli.Descripcion="";
			//determina cambio estado a nivel cliente
			boolean existecli=false;
			DatosActualizar itemupdtcli=new DatosActualizar();
			for(DatosConsulta dc:datosConsulta){
				if(itemin.TipoPersona.equals(dc.TipoPersona)&&itemin.TipoDocumentoIdentidad.equals(dc.IdTipoDocumentoIdentidad)&&itemin.DocumentoIdentidad.equals(dc.DocumentoIdentidad)){
					itemupdtcli.IdTipoCambioEstado=itemin.TipoCambioEstadoCliente;
					itemupdtcli.IdEntidad=dc.IdCliente;
					existecli=true;
					break;
				}
			}
			if(!existecli){
				itemoutCli.Resultado="ERROR";
				itemoutCli.Descripcion="No se ha encontrado el cliente";
				salida.P_OUT_OPERACIONES_REALIZADAS.add(itemoutCli);
				continue;
			}
			if(itemin.TipoCambioEstadoCliente!=null&&!itemin.TipoCambioEstadoCliente.equals("")){
				if(entrada.p_Accion.equals("ACT")) dacliACT.add(itemupdtcli);
				else dacliBLO.add(itemupdtcli);
				salida.P_OUT_OPERACIONES_REALIZADAS.add(itemoutCli);
			}else log.write("Para el cliente:"+itemin.DocumentoIdentidad+" no se ha definido tipo de cambio de estado");
			//determina cambio estado a nivel portafolio
			OutCambioEstadoMasivoClientesPortafolios.Registro itemoutPor=new OutCambioEstadoMasivoClientesPortafolios.Registro();
			itemoutPor.Entidad="PORTAFOLIO";
			itemoutPor.Id=itemin.DocumentoIdentidad+",Cod:"+itemin.CodigoPortafolio;
			itemoutPor.IdEstado=entrada.p_Accion;
			itemoutPor.Resultado="";
			itemoutPor.Descripcion="";
			boolean existepor=false;
			DatosActualizar itemupdtpor =new DatosActualizar();
			for(DatosConsulta dc:datosConsulta){
				if(itemin.TipoPersona.equals(dc.TipoPersona)&&itemin.TipoDocumentoIdentidad.equals(dc.IdTipoDocumentoIdentidad)&&itemin.DocumentoIdentidad.equals(dc.DocumentoIdentidad)
				&&itemin.CodigoPortafolio.equals(dc.CodigoPortafolio)){
					itemupdtpor.IdTipoCambioEstado=itemin.TipoCambioEstadoPortafolio;
					itemupdtpor.IdEntidad=dc.IdPortafolio;
					existepor=true;
					break;
				}
			}
			if(!existepor){
				itemoutPor.Resultado="ERROR";
				itemoutPor.Descripcion="No se ha encontrado el portafolio";
				salida.P_OUT_OPERACIONES_REALIZADAS.add(itemoutPor);
				continue;
			}
			if(itemin.TipoCambioEstadoPortafolio!=null&&!itemin.TipoCambioEstadoPortafolio.equals("")){
				if(entrada.p_Accion.equals("ACT")) daporACT.add(itemupdtpor);
				else daporBLO.add(itemupdtpor);
				salida.P_OUT_OPERACIONES_REALIZADAS.add(itemoutPor);
			}else {
				log.write("Para el portafolio:"+itemin.CodigoPortafolio+"[Documento identidad:"+itemin.DocumentoIdentidad+"] no se ha definido tipo de cambio de estado");
			}
		}//for
		if(dacliACT.size()>0){
			sql.batch(con,SQL_UPDATE_CLIENTE_ACT, dacliACT,DatosActualizar.class);
		}
		if(dacliBLO.size()>0){
			sql.batch(con,SQL_UPDATE_CLIENTE_BLO, dacliBLO,DatosActualizar.class);
		}
		if(daporACT.size()>0){
			sql.batch(con,SQL_UPDATE_PORTAFOLIO_ACT, daporACT,DatosActualizar.class);
		}
		if(daporBLO.size()>0){
			sql.batch(con,SQL_UPDATE_PORTAFOLIO_BLO, daporBLO,DatosActualizar.class);
		}
		if(dacliACT.size()==0&&dacliBLO.size()==0&&daporACT.size()==0&&daporBLO.size()==0){
			log.write("no se generó ningún registro para actualizar");
		}else{
			salida.P_OUT_ERRORESCONTADOR = 0L;
			for(OutCambioEstadoMasivoClientesPortafolios.Registro vm:salida.P_OUT_OPERACIONES_REALIZADAS){
				if(vm.Resultado.equals("")){
					vm.Resultado="EXITO";
					vm.Descripcion="Cambio de estado realizado";
				}
			}
		}
		log.writeAsJson("ENTRADA:",entrada);
		log.writeAsJson("SALIDA:",salida);
		log.write("****************** FIN ****************");
		return salida;
	}
}
