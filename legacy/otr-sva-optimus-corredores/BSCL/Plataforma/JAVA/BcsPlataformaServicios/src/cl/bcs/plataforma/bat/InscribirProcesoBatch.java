package cl.bcs.plataforma.bat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class InscribirProcesoBatch implements JavaCall{
	private final Logger log = LogManager.getLogger(InscribirProcesoBatch.class);
	
	private final String SQL_SELECT_TIPO_PROGRAMA = loadResourceToString("cl/bcs/plataforma/bat/sql/ConsultarTipoPrograma_Select.sql");
	private final String SQL_INSERTPROGRAMA = loadResourceToString("cl/bcs/plataforma/bat/sql/InsertarProcesoBatch_Insert.sql");
	private final String SQL_INSERTPARAMETROSPROGRAMA = loadResourceToString("cl/bcs/plataforma/bat/sql/InsertarProcesoBatchParametrosPrograma_Insert.sql");
	private final String SQL_SELECT_PARAMETROS_AGENDA = "select pa.\"IdAgenda\",pa.\"Orden\",pa.\"NombreParametro\",pa.\"TipoDato\",pa.\"Valor\" "
			+ "from \"BSCL.Plataforma.BD::BAT.ParametrosAgenda\" pa join \"BSCL.Plataforma.BD::BAT.Agenda\" a on pa.\"IdAgenda\"=a.\"IdAgenda\" where a.\"IdTipoPrograma\" IN (:IdTipoProgramas)";
	/*******************
	 * clases in/out
	 *******************/
	public static class Parametro{
		public Long   	IdInterno;
		public String	NombreParametro;
		public String	Valor;
		public String	ValorBlob;
		
		public Parametro(Long IdInterno, String NombreParametro, String Valor, String ValorBlob){
			this.IdInterno = IdInterno;
			this.NombreParametro = NombreParametro;
			this.Valor = Valor;
			this.ValorBlob = ValorBlob;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametrosAgenda {
		public Long					IdAgenda; 
		public Integer				Orden; 
		public String				NombreParametro; 
		public String				TipoDato; 
		public String				Valor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTipoPrograma{
		public Long   IdInterno;
		public String IdTipoPrograma;
		public Long	  SegundosDesfase;
		public String Comentario = "";
		public InTipoPrograma() {
			
		}
		public InTipoPrograma(Long idInterno, String idTipoPrograma, Long segundosDesfase, String comentario) {
			super();
			IdInterno = idInterno;
			IdTipoPrograma = idTipoPrograma;
			SegundosDesfase = segundosDesfase;
			Comentario = comentario;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutPrograma{
		public Long   	 IdInterno;
		public Long	  	 IdPrograma;
		public Timestamp FechaEjecucion;
		
		public OutPrograma(Long IdInterno, Long IdPrograma, Timestamp FechaEjecucion){
			this.IdInterno = IdInterno;
			this.IdPrograma = IdPrograma;
			this.FechaEjecucion = FechaEjecucion;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroPrograma{
		public Long IdGrupoParametro;
		public Long IdPrograma;
		public String NombreParametro;
		public String Valor;
		public String ValorBlob;
		public InputStream BlobStream;
		
		public ParametroPrograma(Long IdGrupoParametro,Long IdPrograma, String NombreParametro,String Valor,String ValorBlob){
			this.IdGrupoParametro = IdGrupoParametro;
			this.IdPrograma = IdPrograma;
			this.NombreParametro = NombreParametro;
			this.Valor = Valor;
			this.ValorBlob = ValorBlob;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Programa{
		public Long IdPrograma;
		public Long IdGrupoParametro;
		public String IdTipoPrograma;
		public Long	IdUsuario;
		public String FechaEjecucion;
		public String FechaSistema;
		public String Nodo;
		public String Comentario;
		
		public Programa(Long IdPrograma,Long IdGrupoParametro,String IdTipoPrograma,Long IdUsuario,String FechaEjecucion,String FechaSistema, String Nodo, String Comentario){
			this.IdPrograma = IdPrograma;
			this.IdGrupoParametro = IdGrupoParametro;
			this.IdTipoPrograma = IdTipoPrograma;
			this.IdUsuario = IdUsuario;
			this.FechaEjecucion = FechaEjecucion;
			this.FechaSistema = FechaSistema;
			this.Nodo = Nodo;
			this.Comentario = Comentario;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoPrograma{
		public String IdTipoPrograma;
		public String Descripcion;
		public Integer MaximoIntentos;
		public String Modulo;
		public String NombreClasePrograma;
		public String TipoClasePrograma;
		public String Nodo;
		public String EsPauta;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Secuencia{
		public Long IdSecuencia;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InscribirProcesoBatchEntrada {
	    public Long     		  p_IdEmpresa;
	    public Long				  p_IdUsuario;
	    public String			  p_IdModulo;		  
	    public List<InTipoPrograma> p_TipoPrograma;
	    public List<Parametro> 	  p_Parametro;
	    @Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InscribirProcesoBatchSalida{
		public Long			  		P_OUT_ERRORESCONTADOR;
		public List<OutPrograma> 	P_OUT_PROGRAMA;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		InscribirProcesoBatchEntrada in=gson.fromJson(jin.toString() ,InscribirProcesoBatchEntrada.class);
		InscribirProcesoBatchSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	@Trace
	public InscribirProcesoBatchSalida logica(Connection con, InscribirProcesoBatchEntrada entrada) throws Exception {
		log.info("INICIO:InscribirProcesoBatch");
		InscribirProcesoBatchSalida salida = new InscribirProcesoBatchSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		salida.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		salida.P_OUT_PROGRAMA =  new ArrayList<OutPrograma>();

		if(entrada.p_TipoPrograma.size() == 0){
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(new Long(0),MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_PROGRAMAS", new HashMap<String,String>(), con,entrada.p_IdEmpresa));
			log.debug("SALIDA ERROR NO SE INGRESARON PROGRAMAS:");
			return salida;
		}
		
		SQL sql=new SQL();
		
		List<ParametroPrograma> parametrosPrograma = new ArrayList<ParametroPrograma>();
		List<Programa> programas = new ArrayList<Programa>();
		
		//obtener todos los tipos de programa
		log.debug("INICIO:CONSULTAR CONF TIPOS PROGRAMA");
		List<String> tiposPrograma = entrada.p_TipoPrograma.stream().map(tipoPrograma -> tipoPrograma.IdTipoPrograma).distinct().collect(Collectors.toList());
		HashMap<String,Object> paramsTipoPrograma = new HashMap<String,Object>();
		paramsTipoPrograma.put("IdTipoProgramas", tiposPrograma);
		HashMap<String, Object> confTiposProgramas = Utils.List2Hash(sql.query(con, SQL_SELECT_TIPO_PROGRAMA,paramsTipoPrograma,TipoPrograma.class),"IdTipoPrograma");
		log.debug("TERMINO:CONSULTAR CONF TIPOS PROGRAMA");
		
		log.debug("INICIO: OBTENER FECHAS");
		Timestamp fechaPrograma = FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa);
		Timestamp fechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(con, entrada.p_IdEmpresa);
		log.debug("TERMINO: OBTENER FECHAS");
		
		log.debug("INICIO: OBTENER SECUENCIAS");
		LinkedList<Long> secuenciaGrupoParametro = new LinkedList<>(Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma", new Long(entrada.p_TipoPrograma.size())));
		LinkedList<Long> secuenciaPrograma = new LinkedList<>(Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Programa", Long.valueOf(entrada.p_TipoPrograma.size())));
		if(secuenciaGrupoParametro.isEmpty() || secuenciaPrograma.isEmpty()){
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_SECUENCIAS_PROGRAMA", new HashMap<String,String>(), con,entrada.p_IdEmpresa));
			log.debug("SALIDA ERROR SECUENCIAS:");
			return salida;
		}
		log.debug("TERMINO: OBTENER SECUENCIAS");

		log.debug("INICIO: OBTENER PARAMETROS AGENDA");
		HashMap<String,Object> parmsAgenda = new HashMap<String,Object>();
		parmsAgenda.put("IdTipoProgramas", tiposPrograma);
		List<ParametrosAgenda> listaParametrosAgenda = sql.query(con, SQL_SELECT_PARAMETROS_AGENDA,parmsAgenda,ParametrosAgenda.class);
		log.debug("TERMINO: OBTENER PARAMETROS AGENDA");
		
		log.debug("INICIO: CONSTRUIR PROGRAMAS");
		for(InTipoPrograma tipoPrograma : entrada.p_TipoPrograma){
			TipoPrograma confTipoPrograma = (TipoPrograma) confTiposProgramas.get(tipoPrograma.IdTipoPrograma);

			if(Objects.isNull(confTipoPrograma)){
				salida.P_OUT_ERRORESCONTADOR++;
				String procesobatchfallidomsg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "NO_EXISTE_TIPO_PROGRAMA").msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(new Long(0),MensajeSalida.MENSAJE_ERROR,procesobatchfallidomsg, new HashMap<String,String>(), con,entrada.p_IdEmpresa));
				log.debug("SALIDA ERROR NO EXISTE TIPO PROGRAMA:");
				return salida;
			}
			String nodo = confTipoPrograma.Nodo;
			Timestamp fechaEjecucion = Fecha.addSeconds(fechaPrograma, tipoPrograma.SegundosDesfase.intValue());
			String fechaEjecucionStr = Fecha.toFormat(fechaEjecucion, "yyyy-MM-dd HH:mm:ss");
			String fechaSistemaStr = Fecha.toFormat(fechaSistema, "yyyy-MM-dd");
			
			List<Parametro> parametrosTipoTarea = new ArrayList<Parametro>();
			
			for(Parametro parametro : entrada.p_Parametro){
				if(parametro.IdInterno.equals(tipoPrograma.IdInterno)){
					parametrosTipoTarea.add(parametro);
				}
			}
			
			boolean existeIdEmpresa = false;
			
			for(Parametro parametro : parametrosTipoTarea){
				if(parametro.NombreParametro.equalsIgnoreCase("IdEmpresa")){
					existeIdEmpresa = true;
					break;
				}
			}
			
			if (!existeIdEmpresa) parametrosTipoTarea.add(new Parametro(tipoPrograma.IdInterno,"IdEmpresa",entrada.p_IdEmpresa.toString(),null));
			
			/*
			HashMap<String,Object> parametros = Utils.List2Hash(parametrosTipoTarea,"NombreParametro");
			
			if(parametros.get("IdEmpresa") == null){
				parametrosTipoTarea.add(new Parametro(tipoPrograma.IdInterno,"IdEmpresa",entrada.p_IdEmpresa.toString(),null));
			}
			*/
			Long idSecuenciaPrograma = secuenciaPrograma.pop();
			Long idSecuenciaGrupoParametro = secuenciaGrupoParametro.pop();
			
			for(Parametro parametroPrograma : parametrosTipoTarea){
				parametrosPrograma.add(new ParametroPrograma(idSecuenciaGrupoParametro, idSecuenciaPrograma, parametroPrograma.NombreParametro, parametroPrograma.Valor != null ? parametroPrograma.Valor : "", parametroPrograma.ValorBlob));
			}
			
			boolean existe = false;
			for (ParametrosAgenda parametroAgenda : listaParametrosAgenda){
				for(Parametro parametroPrograma : parametrosTipoTarea){
					if (parametroAgenda.NombreParametro.equals(parametroPrograma.NombreParametro)){
						existe = true;
						break;
					}
				}
				if(!existe){
					parametrosPrograma.add(new ParametroPrograma(idSecuenciaGrupoParametro, idSecuenciaPrograma, parametroAgenda.NombreParametro, parametroAgenda.Valor != null ? parametroAgenda.Valor : "", null));
				}
				existe=false;
			}
			
			programas.add(new Programa(idSecuenciaPrograma,idSecuenciaGrupoParametro,tipoPrograma.IdTipoPrograma,entrada.p_IdUsuario,fechaEjecucionStr,fechaSistemaStr,nodo, tipoPrograma.Comentario));
			
			salida.P_OUT_PROGRAMA.add(new OutPrograma(tipoPrograma.IdInterno,idSecuenciaPrograma,fechaEjecucion));
			
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
			
			parametrosMensaje.put("FechaEjecucion", Fecha.toStringDate(fechaEjecucion));
			parametrosMensaje.put("IdPrograma", idSecuenciaPrograma.toString());
			
			String procesobatchexitosomsg = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "PROCESO_BATCH_EXITOSO").msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(tipoPrograma.IdInterno,MensajeSalida.MENSAJE_OK,procesobatchexitosomsg, parametrosMensaje, con,entrada.p_IdEmpresa));
			
		}
		log.debug("TERMINO: CONSTRUIR PROGRAMAS");
		
		for (ParametroPrograma param : parametrosPrograma) {
			if (param.ValorBlob != null) {
				param.BlobStream = new ByteArrayInputStream(param.ValorBlob.getBytes());
			}
		}
		log.debug("INICIO: INSERT PROGRAMAS");
		sql.batch(con, SQL_INSERTPROGRAMA, programas, Programa.class);
		sql.batch(con, SQL_INSERTPARAMETROSPROGRAMA, parametrosPrograma, ParametroPrograma.class);
		log.debug("TERMINO: INSERT PROGRAMAS");
		
		log.info("SALIDA:InscribirProcesoBatch");
		return salida;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		try (Connection con = PlataformaUtils.getDESAPEConnection()){
			Gson gson = Utils.getGsonBuilder();
			//String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"PLA\",\"p_TipoPrograma\":[{\"IdInterno\":0,\"IdTipoPrograma\":\"EXPORTAR_GRILLA_CSV\",\"SegundosDesfase\":0,\"Comentario\":\"Archivo Exportado: Gestión Socio Negocio_03-10-2018\"}],\"p_Parametro\":[{\"IdInterno\":0,\"NombreParametro\":\"t\",\"Valor\":\"1355\"},{\"IdInterno\":0,\"NombreParametro\":\"r\",\"Valor\":\"\"},{\"IdInterno\":0,\"NombreParametro\":\"callParameters\",\"Valor\":\"{}\"},{\"IdInterno\":0,\"NombreParametro\":\"odataParameters\",\"Valor\":\"{\\\"$inlinecount\\\":\\\"allpages\\\",\\\"$format\\\":\\\"json\\\",\\\"$orderby\\\":\\\"NombreTipoDocumentoIdentidadDefecto,DocumentoIdentidadDefecto,NombreCompleto\\\",\\\"$filter\\\":\\\"IdEmpresa eq 1\\\"}\"},{\"IdInterno\":0,\"NombreParametro\":\"token\",\"Valor\":\"MTpjaGVybmFuZGV6OjIwMTgtMTAtMDMgMTQ6NDY6MzEuNjAxMDAwMA==\"},{\"IdInterno\":0,\"NombreParametro\":\"columns\",\"Valor\":\"[{\\\"field\\\":\\\"FechaIngreso\\\",\\\"title\\\":\\\"Fecha Ingreso\\\",\\\"format\\\":\\\"{0:d}\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"date\\\"},{\\\"field\\\":\\\"NombreTipoDocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Tipo Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"TipoPersona\\\",\\\"title\\\":\\\"Tipo Persona\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombreCompleto\\\",\\\"title\\\":\\\"Nombre\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombrePaisResidencia\\\",\\\"title\\\":\\\"País Residencia\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsCliente\\\",\\\"title\\\":\\\"Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsEmpleado\\\",\\\"title\\\":\\\"Empleado\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsProveedor\\\",\\\"title\\\":\\\"Proveedor\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsContraparte\\\",\\\"title\\\":\\\"Contraparte\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsClienteFinal\\\",\\\"title\\\":\\\"Cliente Final\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DescripcionSegmento\\\",\\\"title\\\":\\\"Segmento Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"}]\"},{\"IdInterno\":0,\"NombreParametro\":\"fileName\",\"Valor\":\"Gestión Socio Negocio_03-10-2018\"}]}";
			String test ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"PLA\",\"p_TipoPrograma\":[{\"IdInterno\":0,\"IdTipoPrograma\":\"EXPORTAR_GRILLA_CSV\",\"SegundosDesfase\":0,\"Comentario\":\"Archivo Exportado: Gestión Socio Negocio_03-10-2018\"}],\"p_Parametro\":[{\"IdInterno\":0,\"NombreParametro\":\"t\",\"Valor\":\"1355\"},{\"IdInterno\":0,\"NombreParametro\":\"r\",\"Valor\":\"\"},{\"IdInterno\":0,\"NombreParametro\":\"callParameters\",\"Valor\":\"{}\"},{\"IdInterno\":0,\"NombreParametro\":\"odataParameters\",\"Valor\":\"{\\\"$inlinecount\\\":\\\"allpages\\\",\\\"$format\\\":\\\"json\\\",\\\"$orderby\\\":\\\"NombreTipoDocumentoIdentidadDefecto,DocumentoIdentidadDefecto,NombreCompleto\\\",\\\"$filter\\\":\\\"IdEmpresa eq 1\\\"}\"},{\"IdInterno\":0,\"NombreParametro\":\"token\",\"Valor\":\"MTpjaGVybmFuZGV6OjIwMTgtMTAtMDMgMTQ6NDY6MzEuNjAxMDAwMA==\"},{\"IdInterno\":0,\"NombreParametro\":\"columns\",\"Valor\":\"[{\\\"field\\\":\\\"FechaIngreso\\\",\\\"title\\\":\\\"Fecha Ingreso\\\",\\\"format\\\":\\\"{0:d}\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"date\\\"},{\\\"field\\\":\\\"NombreTipoDocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Tipo Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DocumentoIdentidadDefecto\\\",\\\"title\\\":\\\"Documento Identidad\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"TipoPersona\\\",\\\"title\\\":\\\"Tipo Persona\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombreCompleto\\\",\\\"title\\\":\\\"Nombre\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"NombrePaisResidencia\\\",\\\"title\\\":\\\"País Residencia\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsCliente\\\",\\\"title\\\":\\\"Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsEmpleado\\\",\\\"title\\\":\\\"Empleado\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsProveedor\\\",\\\"title\\\":\\\"Proveedor\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsContraparte\\\",\\\"title\\\":\\\"Contraparte\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"EsClienteFinal\\\",\\\"title\\\":\\\"Cliente Final\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"},{\\\"field\\\":\\\"DescripcionSegmento\\\",\\\"title\\\":\\\"Segmento Cliente\\\",\\\"align\\\":\\\"left\\\",\\\"type\\\":\\\"string\\\"}]\"},{\"IdInterno\":0,\"NombreParametro\":\"fileName\",\"Valor\":\"Gestión Socio Negocio_03-10-2018\"}]}";
			InscribirProcesoBatchEntrada c = gson.fromJson(test, InscribirProcesoBatchEntrada.class);
		  
			InscribirProcesoBatch ipb = new InscribirProcesoBatch();
		    InscribirProcesoBatchSalida a = ipb.logica(con,c);
		    System.out.println(a.toString());
		    con.rollback();
		    //con.commit();
		} 
	}
	
	
	
}
