package cl.bcs.plataforma.bat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ReprocesarPrograma implements JavaCall {	

	//QUERIES
	private final String SQL_CONSULTAPROGRAMA = "SELECT \"IdAgenda\",\"idEstado\",\"IdTipoPrograma\",\"IdGrupoParametro\" FROM \"BSCL.Plataforma.BD::BAT.Programa\" where \"IdPrograma\" = :IdPrograma";
	
	private final String SQL_SECUENCIAPROGRAMA = "SELECT \"BSCL.Plataforma.BD.Secuencias::Programa\".NEXTVAL as \"IdSecuencia\" from dummy";
	
	private final String SQL_SECUENCIAGRUPOPARAMETRO = "SELECT \"BSCL.Plataforma.BD.Secuencias::ParametrosPrograma\".NEXTVAL as \"IdSecuencia\" from dummy";
	
	private final String SQL_INSERTPROGRAMA = "INSERT INTO \"BSCL.Plataforma.BD::BAT.Programa\""
			+ "( \"IdPrograma\", \"IdAgenda\", \"IdTipoPrograma\", \"Intento\", \"IdUsuario\", \"idEstado\", \"Resultado\", \"fechaCreacion\", \"fechaEjecucion\", \"fechaSistema\", \"Nodo\", \"IdProgramaReprocesado\",\"Bloqueo\",\"IdGrupoParametro\") "
			+ "SELECT :IdProgramaNuevo, \"IdAgenda\", \"IdTipoPrograma\", 0, :IdUsuario, 'PEN', 'Esperando ejecución', :FechaHoraUTC, :FechaEjecucion, :FechaSistema, \"Nodo\", \"IdPrograma\",'N',:IdGrupoParametro "
			+ "FROM \"BSCL.Plataforma.BD::BAT.Programa\" WHERE \"IdPrograma\" = :IdPrograma";
	
	private final String SQL_INSERTPARAMETROSPROGRAMA = "INSERT INTO \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" "
			+ "(\"IdSecuencia\",\"IdGrupoParametro\",\"IdPrograma\",\"Orden\",\"NombreParametro\",\"Valor\") "
			+ "select \"BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia\".nextval, :IdGrupoParametro, :IdProgramaNuevo,1,\"NombreParametro\",\"Valor\" "
			+ "from \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" where \"IdPrograma\" = :IdPrograma;";
	/*******************
	 * clases in/out
	 *******************/
	public static class ConsultaPrograma{
		public Long 	IdAgenda;
		public String 	idEstado;
		public String 	IdTipoPrograma;
		public Long		IdGrupoParametro;
	}
	
	public static class InsertPrograma{
		public Long IdProgramaNuevo;
		public Long IdUsuario;
		public String FechaHoraUTC;
		public String FechaEjecucion;
		public String FechaSistema;
		public Long IdPrograma;
		public Long	IdGrupoParametro;
	}
	
	public static class InsertParametrosPrograma{
		public Long IdGrupoParametro;
		public Long IdProgramaNuevo;
		public Long IdPrograma;
	}
	
	public static class Secuencia{
		public Long IdSecuencia;
	}
	
	public static class ReprocesarProgramaEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public Long 			p_IdPrograma;
		public Long				p_Segundos; //seg extra para ejecucion inmediata 
	}
	
	public static class ReprocesarProgramaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDPROGRAMANUEVO;
		public Long P_OUT_IDGRUPOPARAMETRONUEVO;
		public String P_OUT_MENSAJE;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ReprocesarProgramaEntrada in=gson.fromJson(jin.toString() ,ReprocesarProgramaEntrada.class);
		ReprocesarProgramaSalida out=logica(con,in);
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
			ReprocesarProgramaEntrada entrada = new ReprocesarProgramaEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 37L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdPrograma = 1091564L;
			entrada.p_Segundos = 60L;
 			ReprocesarPrograma demo=new ReprocesarPrograma();	
	        ReprocesarProgramaSalida salida = demo.logica(con, entrada);
	        con.commit();
	    	con.close();
	    	System.out.println(salida.P_OUT_MENSAJE+"|Id:"+salida.P_OUT_IDPROGRAMANUEVO);	
	    	System.out.println("fin");
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ReprocesarProgramaSalida logica(Connection con, ReprocesarProgramaEntrada entrada) throws Exception {
		ReprocesarProgramaSalida out = new ReprocesarProgramaSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_IDPROGRAMANUEVO = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		//Obtencion datos del programa:
		HashMap<String,Object> parmsConsultaPrograma =new HashMap<String,Object>();
		parmsConsultaPrograma.put("IdPrograma", entrada.p_IdPrograma);	
		List<ConsultaPrograma> listConsultaPrograma = sql.query(con, SQL_CONSULTAPROGRAMA,parmsConsultaPrograma,ConsultaPrograma.class);
		String idTipoPrograma = listConsultaPrograma.get(0).IdTipoPrograma;
		//validaciones:
		if(listConsultaPrograma.size()==0){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "No existe programa";
			return out;
		}
		if(listConsultaPrograma.get(0).IdAgenda.equals(0)){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Existe programa pero no es agenda";
			return out;
		}
		if(!listConsultaPrograma.get(0).idEstado.equals("ERR")){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Programa no presenta error";
			return out;
		}
		//Obtencion Secuencia IdPrograma
		HashMap<String,Object> parmsvacio =new HashMap<String,Object>();
		List<Secuencia> listaSecPrograma = sql.query(con, SQL_SECUENCIAPROGRAMA,parmsvacio,Secuencia.class);
		if(listaSecPrograma.size()==0){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al obtener secuencia de programa";
			return out;
		}
		out.P_OUT_IDPROGRAMANUEVO = listaSecPrograma.get(0).IdSecuencia;		
		if(listConsultaPrograma.get(0).IdGrupoParametro != null && listConsultaPrograma.get(0).IdGrupoParametro.intValue()>0){
			//Obtencion Secuencia IdPrograma
			List<Secuencia> listaSecGrupoParametro = sql.query(con, SQL_SECUENCIAGRUPOPARAMETRO,parmsvacio,Secuencia.class);
			if(listaSecGrupoParametro.size()==0){
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJE = "Error al obtener secuencia grupo parámetro";
				return out;
			}
			out.P_OUT_IDGRUPOPARAMETRONUEVO = listaSecGrupoParametro.get(0).IdSecuencia;	
		}
		
		//generación de fecha
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dfFechaHora.setTimeZone(TimeZone.getTimeZone("GMT"));
		Long segundos = (entrada.p_Segundos==null)?0:entrada.p_Segundos;
		Date fechaCalculada = new Date( System.currentTimeMillis() + (1000L*segundos));
		String fechaCalculadaStr = dfFechaHora.format(fechaCalculada);	
		System.out.println("FechaCalculada:"+fechaCalculadaStr);
		if(out.P_OUT_IDGRUPOPARAMETRONUEVO!=null){
			//Insert Parámetros Programa
			List<InsertParametrosPrograma> paramprog = new ArrayList<InsertParametrosPrograma>();
			InsertParametrosPrograma iteminsparam = new InsertParametrosPrograma();
			iteminsparam.IdGrupoParametro = out.P_OUT_IDGRUPOPARAMETRONUEVO;
			iteminsparam.IdPrograma = entrada.p_IdPrograma;
			iteminsparam.IdProgramaNuevo = out.P_OUT_IDPROGRAMANUEVO;
			paramprog.add(iteminsparam);
			sql.batch(con,SQL_INSERTPARAMETROSPROGRAMA, paramprog, InsertParametrosPrograma.class);
		}
		//Insert Programa
		List<InsertPrograma> prog = new ArrayList<InsertPrograma>();
		InsertPrograma itemins = new InsertPrograma();
		itemins.IdProgramaNuevo = out.P_OUT_IDPROGRAMANUEVO;
		itemins.IdUsuario = entrada.p_IdUsuario;
		itemins.FechaHoraUTC = fechaCalculadaStr;
		itemins.FechaEjecucion = fechaCalculadaStr;
		itemins.FechaSistema = fechaSistema;
		itemins.IdPrograma = entrada.p_IdPrograma;
		itemins.IdGrupoParametro = out.P_OUT_IDGRUPOPARAMETRONUEVO;
		prog.add(itemins);
		sql.batch(con,SQL_INSERTPROGRAMA, prog, InsertPrograma.class);
		//LOG:
		AgregarLog logBD = new AgregarLog();
		AgregarLog.InAgregarLog entradaLogBD = new AgregarLog.InAgregarLog();
		entradaLogBD.p_IdTrabajo = new Long(1313);
		AgregarLog.InAgregarLog.Log inItemLogBD = new AgregarLog.InAgregarLog.Log();
		inItemLogBD.IdTrabajo = entradaLogBD.p_IdTrabajo;
		inItemLogBD.IdCabeceraLog = new Long(1);
		inItemLogBD.IdTipoLog="INFORMATIVO";
		inItemLogBD.IdModulo="BAT";
		inItemLogBD.IdUsuario=entrada.p_IdUsuario;
		inItemLogBD.IdEmpresa=entrada.p_IdEmpresa;
		inItemLogBD.FechaSistemaLog=fechaSistema;
		inItemLogBD.IdUsuarioAtendido=entrada.p_IdUsuario;
		inItemLogBD.IdCliente=new Long(0);
		inItemLogBD.EstadoCliente="";
		inItemLogBD.NombreCliente="";
		inItemLogBD.IdTipoDocumentoIdentidad="";
		inItemLogBD.IdDocumentoIdentidad="";
		inItemLogBD.IdPortafolio=new Long(0);
		inItemLogBD.EstadoPortafolio="";
		inItemLogBD.IdTipoRegistro="EJEPRO";
		inItemLogBD.IdRegistro=entrada.p_IdPrograma.toString();
		inItemLogBD.EstadoRegistro="PEN";
		inItemLogBD.IdTipoOperacion="";
		inItemLogBD.Folio=new Long(0);
		inItemLogBD.Cantidad=new BigDecimal(0);
		inItemLogBD.PrecioTasa=new BigDecimal(0);
		inItemLogBD.Monto=new BigDecimal(0);
		inItemLogBD.IdMonedaOrigen="";
		inItemLogBD.IdMonedaLiquidacion="";
		inItemLogBD.TipoCambio="";
		inItemLogBD.Nemo="";
		inItemLogBD.IdProceso=entrada.p_IdPrograma;
		inItemLogBD.EstadoProceso="PEN";
		inItemLogBD.NombreProceso="REPROCESARPROGRAMA - Tipo Programa: "+idTipoPrograma+", IdPrograma Origen: "+entrada.p_IdPrograma;
		AgregarLog.InAgregarLog.DetalleLog inDetLogBD = new AgregarLog.InAgregarLog.DetalleLog();
		inDetLogBD.IdTrabajo = entradaLogBD.p_IdTrabajo;
		inDetLogBD.IdCabeceraLog = new Long(1);
		inDetLogBD.SubRegistro="";
		inDetLogBD.CampoRegistro="";
		inDetLogBD.ValorAntiguo="";
		inDetLogBD.ValorNuevo="";
		entradaLogBD.p_Log.add(inItemLogBD);
		entradaLogBD.p_LogDetalle.add(inDetLogBD);
		AgregarLog.OutAgregarLog salidaLogBD = logBD.logica(con, entradaLogBD);
		if(salidaLogBD.P_OUT_ERRORESCONTADOR>0){
			out.P_OUT_MENSAJE = "Error al agregar log";
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + salidaLogBD.P_OUT_ERRORESCONTADOR;
		}
		return out;
	}
}
