package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosAgenda;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.Secuencia;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AdelantarPauta implements JavaCall {	

	//QUERIES
	private final String SQL_CONSULTAPROGRAMA = "SELECT \"IdAgenda\",\"idEstado\" FROM \"BSCL.Plataforma.BD::BAT.Programa\" where \"IdPrograma\" = :IdPrograma";
	private final String SQL_UPDATEPROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"fechaEjecucion\"=:FechaEjecucion where \"IdPrograma\" = :IdPrograma";
	private final String SQL_SECUENCIAGRUPOPARAMETRO = "SELECT distinct \"IdGrupoParametro\" from \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" where \"IdPrograma\" = :IdPrograma";
	private final String SQL_INSERTPARAMETROSPROGRAMA = "INSERT INTO \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" "
			+ "(\"IdSecuencia\",\"IdGrupoParametro\",\"IdPrograma\",\"Orden\",\"NombreParametro\",\"Valor\") "
			+ "select \"BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia\".nextval, :IdGrupoParametro, :IdPrograma,1,:NombreParametro,:Valor "
			+ "from dummy;";
	/*******************
	 * clases in/out
	 *******************/
	public static class ConsultaPrograma{
		public Long 	IdAgenda;
		public String 	idEstado;
	}
	
	public static class SecuenciaGrupoParametro{
		public Long 	IdGrupoParametro;
	}
	
	public static class UpdatePrograma{
		public String FechaEjecucion;
		public Long IdPrograma;
	}
	
	public static class AdelantarPautaEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public Long 			p_IdPrograma;
		public Long				p_Segundos; //--seg extra para ejecucion inmediata 
		public List<Parametro>	p_Parametros;
		public static class Parametro{
			public String	NombreParametro;
			public String	Valor;
		}
		public AdelantarPautaEntrada(){
			p_Parametros = new ArrayList<Parametro>();
		}
	}
	
	public static class AdelantarPautaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}	
	
	public static class ParametrosPrograma {
		public Long					IdSecuencia; 
		public Long					IdGrupoParametro; 
		public Long					IdPrograma; 
		public Integer				Orden; 
		public String				NombreParametro; 
		public String				Valor; 
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		AdelantarPautaEntrada in=gson.fromJson(jin.toString() ,AdelantarPautaEntrada.class);
		AdelantarPautaSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public AdelantarPautaSalida logica(Connection con, AdelantarPautaEntrada entrada) throws Exception {
		AdelantarPautaSalida out = new AdelantarPautaSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		//Obtencion datos del programa:
		HashMap<String,Object> parmsConsultaPrograma =new HashMap<String,Object>();
		parmsConsultaPrograma.put("IdPrograma", entrada.p_IdPrograma);	
		List<ConsultaPrograma> listConsultaPrograma = sql.query(con, SQL_CONSULTAPROGRAMA,parmsConsultaPrograma,ConsultaPrograma.class);
		
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
		if(!listConsultaPrograma.get(0).idEstado.equals("PEN")){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Programa ya procesado";
			return out;
		}
		
		//Se recepcionan parametros de entrada del programa ya agendado
		HashMap<String,Object> parmsSecuenciaGrupoParametro = new HashMap<String,Object>();
		parmsSecuenciaGrupoParametro.put("IdPrograma", entrada.p_IdPrograma);
		List<SecuenciaGrupoParametro> listaSecuenciaGrupoParametro = sql.query(con, SQL_SECUENCIAGRUPOPARAMETRO,parmsSecuenciaGrupoParametro,SecuenciaGrupoParametro.class);
		List<ParametrosPrograma> paramprog = new ArrayList<ParametrosPrograma>();
		if(entrada.p_Parametros.size()>0){
			for(AdelantarPautaEntrada.Parametro item : entrada.p_Parametros){
				ParametrosPrograma aux = new ParametrosPrograma();
				aux.IdGrupoParametro = listaSecuenciaGrupoParametro.get(0).IdGrupoParametro; 
				aux.IdPrograma = entrada.p_IdPrograma;  
				aux.NombreParametro = item.NombreParametro; 
				aux.Valor = item.Valor; 
				paramprog.add(aux);
			}
			//Insert Parámetros Programa
			sql.batch(con,SQL_INSERTPARAMETROSPROGRAMA, paramprog, ParametrosPrograma.class);
		}
		
		//Cambiar fecha
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Long segundos = (entrada.p_Segundos==null)?0:entrada.p_Segundos;
		Date fechaCalculada = new Date( System.currentTimeMillis() + (1000L*segundos));
		String fechaCalculadaStr = dfFechaHora.format(fechaCalculada);	

		List<UpdatePrograma> prog = new ArrayList<UpdatePrograma>();
		UpdatePrograma itemup = new UpdatePrograma();
		itemup.FechaEjecucion = fechaCalculadaStr;
		itemup.IdPrograma = entrada.p_IdPrograma;
		prog.add(itemup);
		sql.batch(con,SQL_UPDATEPROGRAMA, prog, UpdatePrograma.class);
		
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
		inItemLogBD.NombreProceso="EJECUTARINMEDIATO";
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
			AdelantarPautaEntrada entrada = new AdelantarPautaEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 37L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdPrograma = 1176481L;
			entrada.p_Segundos = 30L;
			AdelantarPautaEntrada.Parametro param = new AdelantarPautaEntrada.Parametro();
			param.NombreParametro = "EsManual";
			param.Valor = "S";
			entrada.p_Parametros.add(param);
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        AdelantarPauta demo=new AdelantarPauta();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
