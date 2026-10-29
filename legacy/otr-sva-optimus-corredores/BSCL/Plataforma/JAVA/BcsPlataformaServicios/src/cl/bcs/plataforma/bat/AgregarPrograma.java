package cl.bcs.plataforma.bat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AgregarPrograma implements JavaCall {	
	private final Log log = new Log("AgregarPrograma");
	//QUERIES
	private final String SQL_SELECT_TIPO_PROGRAMA 		= "select \"IdTipoPrograma\",\"Descripcion\",\"MaximoIntentos\",\"Modulo\",\"NombreClasePrograma\",\"TipoClasePrograma\",\"Nodo\",\"EsPauta\"from \"BSCL.Plataforma.BD::BAT.TipoPrograma\" where \"IdTipoPrograma\"=:IdTipoPrograma";
	private final String SQL_SELECT_PARAMETROS_AGENDA	= "select pa.\"IdAgenda\",pa.\"Orden\",pa.\"NombreParametro\",pa.\"TipoDato\",pa.\"Valor\" "
															+ "from \"BSCL.Plataforma.BD::BAT.ParametrosAgenda\" pa join \"BSCL.Plataforma.BD::BAT.Agenda\" a on pa.\"IdAgenda\"=a.\"IdAgenda\" where a.\"IdTipoPrograma\"=:IdTipoPrograma";
	private final String SQL_SECUENCIAPROGRAMA 			= "SELECT \"BSCL.Plataforma.BD.Secuencias::Programa\".NEXTVAL as \"IdSecuencia\" from dummy";
	private final String SQL_SECUENCIAGRUPOPARAMETRO 	= "SELECT \"BSCL.Plataforma.BD.Secuencias::ParametrosPrograma\".NEXTVAL as \"IdSecuencia\" from dummy";
	private final String SQL_INSERTPROGRAMA 			= "insert into \"BSCL.Plataforma.BD::BAT.Programa\"(\"IdPrograma\",\"IdGrupoParametro\",\"IdAgenda\",\"IdTipoPrograma\","
															+ "\"Intento\",\"IdUsuario\",\"idEstado\",\"Resultado\",\"fechaCreacion\",\"fechaEjecucion\",\"fechaFin\",\"fechaSistema\",\"Bloqueo\",\"Nodo\",\"ZonaHoraria\","
															+ "\"IdProgramaReprocesado\", \"Comentario\")values (:IdPrograma,:IdGrupoParametro,:IdAgenda,:IdTipoPrograma,:Intento,:IdUsuario,:idEstado,:Resultado,:fechaCreacionStr,"
															+ ":fechaEjecucionStr,:fechaFinStr,:fechaSistemaStr,:Bloqueo,:Nodo,:ZonaHoraria,:IdProgramaReprocesado, :Comentario)";
	private final String SQL_INSERTPARAMETROSPROGRAMA = "INSERT INTO \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" "
															+ "(\"IdSecuencia\",\"IdGrupoParametro\",\"IdPrograma\",\"Orden\",\"NombreParametro\",\"Valor\",\"ValorBlob\") "
															+ "select \"BSCL.Plataforma.BD.Secuencias::ParametrosProgramaSecuencia\".nextval, :IdGrupoParametro, :IdPrograma,1,:NombreParametro,:Valor,:ValorBlob "
															+ "from dummy;";
	/*******************
	 * clases in/out
	 *******************/
	public static class TipoPrograma {
		public String				IdTipoPrograma; 
		public String				Descripcion; 
		public Integer				MaximoIntentos; 
		public String				Modulo; 
		public String				NombreClasePrograma; 
		public String				TipoClasePrograma; 
		public String				Nodo; 
		public String				EsPauta; 
	}	
	public static class ParametrosAgenda {
		public Long					IdAgenda; 
		public Integer				Orden; 
		public String				NombreParametro; 
		public String				TipoDato; 
		public String				Valor; 
	}
	public static class Programa {
		public Long					IdPrograma; 
		public Long					IdGrupoParametro; 
		public Long					IdAgenda; 
		public String				IdTipoPrograma; 
		public Integer				Intento; 
		public String				IdUsuario; 
		public String				idEstado; 
		public String				Resultado; 
		public java.sql.Timestamp	fechaCreacion; 
		public String				fechaCreacionStr; 
		public java.sql.Timestamp	fechaEjecucion; 
		public String				fechaEjecucionStr; 
		public java.sql.Timestamp	fechaFin; 
		public String				fechaFinStr; 
		public java.sql.Date		fechaSistema;
		public String				fechaSistemaStr;
		public String				Bloqueo; 
		public String				Nodo; 
		public Long					ZonaHoraria; 
		public Long					IdProgramaReprocesado;
		public String				Comentario;
	}
	public static class ParametrosPrograma {
		public Long					IdSecuencia; 
		public Long					IdGrupoParametro; 
		public Long					IdPrograma; 
		public Integer				Orden; 
		public String				NombreParametro; 
		public String				Valor;
		public InputStream			ValorBlob;
	}
	public static class Secuencia{
		public Long IdSecuencia;
	}
	
	public static class InAgregarPrograma {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public String			p_IdTipoPrograma;
		public Long				p_Segundos;
		public String			p_FechaSistema;
		public List<Parametro>	p_Parametros;
		public String			p_Comentario;
		public static class Parametro{
			public String	NombreParametro;
			public String	Valor;
			public String	ValorBlob;
		}
		public InAgregarPrograma(){
			this.p_Parametros = new ArrayList<Parametro>();
			this.p_Comentario = "";
		}
	}
	
	public static class OutAgregarPrograma {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDPROGRAMA;
		public Long P_OUT_IDGRUPOPARAMETRO;
		public String P_OUT_MENSAJE;
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();		
		InAgregarPrograma in=gson.fromJson(jin.toString() ,InAgregarPrograma.class);
		OutAgregarPrograma out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/**********************
	 * lógica del programa
	 **********************/
	@Trace
	public OutAgregarPrograma logica(Connection con, InAgregarPrograma entrada) throws Exception {
		
		OutAgregarPrograma out = new OutAgregarPrograma();
		
		out.P_OUT_ERRORESCONTADOR 	= new Long(0);
		out.P_OUT_IDPROGRAMA 		= new Long(0);
		out.P_OUT_MENSAJE 			= "";
		
		SQL sql = new SQL();
		
		System.out.println(entrada);
		
		HashMap<String,Object> parmsvacio = new HashMap<String,Object>();
		
		String fechaSistema=entrada.p_FechaSistema;
		
		if(fechaSistema == null||fechaSistema.equals("")){
			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
		}else{
			log.write("Programa se crea con fecha de sistema recibida:"+fechaSistema);
		}
		
		System.out.println("flag1 " + fechaSistema);
		
		//consulta tipo programa
		HashMap<String,Object> parmsTipoPrograma = new HashMap<String,Object>();
		parmsTipoPrograma.put("IdTipoPrograma", entrada.p_IdTipoPrograma);
		
		List<TipoPrograma> listaTipoPrograma = sql.query(con, SQL_SELECT_TIPO_PROGRAMA,parmsTipoPrograma,TipoPrograma.class);
		
		if(listaTipoPrograma.size()==0){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE="No existe tipo programa";
			return out;
		}
		
		log.write("obteniendo secuencia IdPrograma");
		List<Secuencia> listaSecPrograma = sql.query(con, SQL_SECUENCIAPROGRAMA,parmsvacio,Secuencia.class);
		
		if(listaSecPrograma.size()==0){
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJE = "Error al obtener secuencia de programa";
			return out;
		}
		
		out.P_OUT_IDPROGRAMA = listaSecPrograma.get(0).IdSecuencia; 
		
		//consulta parametros agenda
		HashMap<String,Object> parmsAgenda = new HashMap<String,Object>();
		parmsAgenda.put("IdTipoPrograma", entrada.p_IdTipoPrograma);
		
		List<ParametrosAgenda> 		listaParametrosAgenda 	= sql.query(con, SQL_SELECT_PARAMETROS_AGENDA,parmsAgenda,ParametrosAgenda.class);
		List<ParametrosPrograma> 	paramprog 				= new ArrayList<ParametrosPrograma>();
		
		if(listaParametrosAgenda.size()>0) log.write("Hay parámetros agenda, se deben inyectar a parámetros programa");
		
		if(entrada.p_Parametros.size()>0) log.write("Hay parámetros de entrada, se deben inyectar a parámetros programa");
		
		if((entrada.p_Parametros.size()+listaParametrosAgenda.size())>0){
			List<Secuencia> listaSecGrupoParametro = sql.query(con, SQL_SECUENCIAGRUPOPARAMETRO,parmsvacio,Secuencia.class);
			
			if(listaSecGrupoParametro.size()==0){
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJE = "Error al obtener secuencia grupo parámetro";
				return out;
			}
			
			out.P_OUT_IDGRUPOPARAMETRO = listaSecGrupoParametro.get(0).IdSecuencia;
			
			for(ParametrosAgenda item : listaParametrosAgenda){
				ParametrosPrograma aux  = new ParametrosPrograma();
				aux.IdGrupoParametro 	= out.P_OUT_IDGRUPOPARAMETRO; 
				aux.IdPrograma 			= out.P_OUT_IDPROGRAMA;  
				aux.NombreParametro 	= item.NombreParametro; 
				aux.Valor 				= item.Valor; 
				
				paramprog.add(aux);
				log.write("Parametro agenda obtenido: NombreParametro=" + item.NombreParametro + ",Valor=" + item.Valor);
			}
			for(InAgregarPrograma.Parametro item : entrada.p_Parametros){
				ParametrosPrograma aux 	= new ParametrosPrograma();
				aux.IdGrupoParametro 	= out.P_OUT_IDGRUPOPARAMETRO; 
				aux.IdPrograma 			= out.P_OUT_IDPROGRAMA;  
				aux.NombreParametro 	= item.NombreParametro; 
				aux.Valor 				= item.Valor;
				if(item.ValorBlob != null) {
					aux.ValorBlob = new ByteArrayInputStream(item.ValorBlob.getBytes());
				}
				
				paramprog.add(aux);
				log.write("Parametro entrada obtenido: NombreParametro=" + item.NombreParametro + ",Valor=" + item.Valor);
			}
			
			//Insert Parámetros Programa
			sql.batch(con,SQL_INSERTPARAMETROSPROGRAMA, paramprog, ParametrosPrograma.class);
		}
		//generación de fecha
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dfFechaHora.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		Long segundos = (entrada.p_Segundos==null)?0:entrada.p_Segundos;
		
		Date fechaCalculada = new Date( System.currentTimeMillis() + (1000L*segundos));
		String fechaCalculadaStr = dfFechaHora.format(fechaCalculada);	
		
		System.out.println("FechaCalculada:"+fechaCalculadaStr);
		log.write("Parámetros agregados, se procede a insertar programa");
		
		//Insert Programa
		List<Programa> prog = new ArrayList<Programa>();
		
		Programa itemins = new Programa();
		itemins.IdPrograma 			= out.P_OUT_IDPROGRAMA; 
		itemins.IdGrupoParametro 	= out.P_OUT_IDGRUPOPARAMETRO; 
		//obtener id agenda de otro lado.
		itemins.IdAgenda 			= (listaParametrosAgenda.size()>0)?listaParametrosAgenda.get(0).IdAgenda:0L; 
		itemins.IdTipoPrograma 		= entrada.p_IdTipoPrograma; 
		itemins.Intento				= 0; 
		itemins.IdUsuario 			= entrada.p_IdUsuario+"";
		itemins.idEstado 			= "PEN"; 
		itemins.Resultado 			= "Esperando ejecución (Programa adelantado)";  
		itemins.fechaCreacionStr 	= fechaCalculadaStr; 
		itemins.fechaEjecucionStr 	= fechaCalculadaStr; 
		itemins.fechaFinStr 		= null; 
		itemins.fechaSistemaStr 	= fechaSistema; 
		itemins.Bloqueo 			= "N"; 
		itemins.Nodo 				= listaTipoPrograma.get(0).Nodo; 
		itemins.ZonaHoraria			= null; 
		itemins.IdProgramaReprocesado = 0L; 
		itemins.Comentario 			= entrada.p_Comentario;

		System.out.println(itemins);
		
		prog.add(itemins);
		sql.batch(con,SQL_INSERTPROGRAMA, prog, Programa.class);
		
		log.write("Programa insertado, agregando log");
		//LOG:
		AgregarLog logBD 						= new AgregarLog();
		AgregarLog.InAgregarLog entradaLogBD 	= new AgregarLog.InAgregarLog();
		entradaLogBD.p_IdTrabajo 				= new Long(1313);
		
		AgregarLog.InAgregarLog.Log inItemLogBD = new AgregarLog.InAgregarLog.Log();
		inItemLogBD.IdTrabajo 					= entradaLogBD.p_IdTrabajo;
		inItemLogBD.IdCabeceraLog 				= new Long(1);
		inItemLogBD.IdTipoLog					= "INFORMATIVO";
		inItemLogBD.IdModulo					= "BAT";
		inItemLogBD.IdUsuario					= entrada.p_IdUsuario;
		inItemLogBD.IdEmpresa					= entrada.p_IdEmpresa;
		inItemLogBD.FechaSistemaLog				= fechaSistema;
		inItemLogBD.IdUsuarioAtendido			= entrada.p_IdUsuario;
		inItemLogBD.IdCliente					= new Long(0);
		inItemLogBD.EstadoCliente				= "";
		inItemLogBD.NombreCliente				= "";
		inItemLogBD.IdTipoDocumentoIdentidad	= "";
		inItemLogBD.IdDocumentoIdentidad		= "";
		inItemLogBD.IdPortafolio				= new Long(0);
		inItemLogBD.EstadoPortafolio			= "";
		inItemLogBD.IdTipoRegistro				= "EJEPRO";
		inItemLogBD.IdRegistro					= out.P_OUT_IDPROGRAMA.toString();
		inItemLogBD.EstadoRegistro				= "PEN";
		inItemLogBD.IdTipoOperacion				= "";
		inItemLogBD.Folio						= new Long(0);
		inItemLogBD.Cantidad					= new BigDecimal(0);
		inItemLogBD.PrecioTasa					= new BigDecimal(0);
		inItemLogBD.Monto						= new BigDecimal(0);
		inItemLogBD.IdMonedaOrigen				= "";
		inItemLogBD.IdMonedaLiquidacion			= "";
		inItemLogBD.TipoCambio					= "";
		inItemLogBD.Nemo						= "";
		inItemLogBD.IdProceso					= out.P_OUT_IDPROGRAMA;
		inItemLogBD.EstadoProceso				= "PEN";
		inItemLogBD.NombreProceso				= "AGREGARPROGRAMA - Tipo Programa: "+entrada.p_IdTipoPrograma;
		
		AgregarLog.InAgregarLog.DetalleLog inDetLogBD = new AgregarLog.InAgregarLog.DetalleLog();
		inDetLogBD.IdTrabajo 					= entradaLogBD.p_IdTrabajo;
		inDetLogBD.IdCabeceraLog 				= new Long(1);
		inDetLogBD.SubRegistro					= "";
		inDetLogBD.CampoRegistro				= "";
		inDetLogBD.ValorAntiguo					= "";
		inDetLogBD.ValorNuevo					= "";
		
		entradaLogBD.p_Log.add(inItemLogBD);
		entradaLogBD.p_LogDetalle.add(inDetLogBD);
		
		AgregarLog.OutAgregarLog salidaLogBD = logBD.logica(con, entradaLogBD);
		
		if(salidaLogBD.P_OUT_ERRORESCONTADOR>0){
			out.P_OUT_MENSAJE 			= "Error al agregar log";
			out.P_OUT_ERRORESCONTADOR	= out.P_OUT_ERRORESCONTADOR + salidaLogBD.P_OUT_ERRORESCONTADOR;
		}
		
		log.writeAsJson("Ejecución finalizada. Datos de salida:",out);
		
		return out;
	}
	
	public static void main(String[] args) {
		try {
			Connection con =  PlataformaUtils.getDESAConnection();
			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"PLA\",\"p_TipoPrograma\":[{\"IdInterno\":0,\"IdTipoPrograma\":\"EXPORTAR_GRILLA_CSV\",\"SegundosDesfase\":0}],\"p_Comentario\":\"Archivo Exportado: Gestión Socio Negocio_03-10-2018\",\"p_Parametro\":[{\"IdInterno\":0,\"NombreParametro\":\"t\",\"Valor\":\"1355\"},{\"IdInterno\":0,\"NombreParametro\":\"r\",\"Valor\":\"\"},{\"IdInterno\":0,\"NombreParametro\":\"callParameters\",\"Valor\":\"{}\"},{\"IdInterno\":0,\"NombreParametro\":\"odataParameters\",\"Valor\":\"{\\\"$inlinecount\\\":\\\"allpages\\\",\\\"$format\\\":\\\"json\\\",\\\"$orderby\\\":\\\"NombreTipoDocumentoIdentidadDefecto,DocumentoIdentidadDefecto,NombreCompleto\\\",\\\"$filter\\\":\\\"IdEmpresa eq 1\\\"}\"},{\"IdInterno\":0,\"NombreParametro\":\"token\",\"Valor\":\"MTpjaGVybmFuZGV6OjIwMTgtMTAtMDMgMTQ6NDY6MzEuNjAxMDAwMA==\"},{\"IdInterno\":0,\"NombreParametro\":\"columns\",\"Valor\":\"[{\\\"field\\\":\\\"FechaIngreso\\\",\\\"title\\\":\\\"Fecha Ingreso\\\",\\\"format\\\":\\\"{0:d}\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"date\\\"},{\\\"field\\\":\\\"NombreTipoDocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Tipo Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"TipoPersona\\\",\\\"title\\\":\\\"Tipo Persona\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombreCompleto\\\",\\\"title\\\":\\\"Nombre\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombrePaisResidencia\\\",\\\"title\\\":\\\"País Residencia\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsCliente\\\",\\\"title\\\":\\\"Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsEmpleado\\\",\\\"title\\\":\\\"Empleado\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsProveedor\\\",\\\"title\\\":\\\"Proveedor\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsContraparte\\\",\\\"title\\\":\\\"Contraparte\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsClienteFinal\\\",\\\"title\\\":\\\"Cliente Final\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DescripcionSegmento\\\",\\\"title\\\":\\\"Segmento Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"}]\"},{\"IdInterno\":0,\"NombreParametro\":\"fileName\",\"Valor\":\"Gestión Socio Negocio_03-10-2018\"}]}";
			InAgregarPrograma objin = gson.fromJson(json, InAgregarPrograma.class);
	
			AgregarPrograma demo = new AgregarPrograma();
	        OutAgregarPrograma salida = demo.logica(con, objin);
	        con.rollback();
	    	con.close();
	    	System.out.println("Mensaje="+salida.P_OUT_MENSAJE+"|Id="+salida.P_OUT_IDPROGRAMA);	
	    	System.out.println("fin");
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
