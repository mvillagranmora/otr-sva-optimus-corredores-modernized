package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class ConsultarPautaEjecutar implements JavaCall {	
	private final Log log = new Log("ConsultarPautaEjecutar");
	//QUERIES
	private final String SQL_PautaActual = "select \"IdTipoPrograma\",\"Descripcion\",\"MaximoIntentos\",\"Modulo\",\"NombreClasePrograma\",\"TipoClasePrograma\",\"EsPauta\",\"Ejecucion\",\"RequiereTipoPrograma\",\"SiguienteTipoPrograma\",\"IdPrograma\",\"IdGrupoParametro\",\"IdAgenda\",\"Intento\",\"IdUsuario\",\"IdEstadoPrograma\",\"NombreEstadoPrograma\",\"ResultadoPrograma\",\"FechaCreacionPrograma\",\"FechaEjecucionPrograma\",\"FechaFinPrograma\",\"FechaSistemaPrograma\",\"IdProgramaReprocesado\",\"IdControlPauta\",\"FechaSistema\",\"FechaCreacion\",\"IdEstado\",\"NombreEstado\",\"Mensaje\""
			+ "from \"BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta\" where \"IdPrograma\"=:IdPrograma";
	
	private final String SQL_PautaAnterior = "select top 1  p.\"FechaEjecucionPrograma\",a.\"UltimaFechaDeEjecucion\", " 
			+ "p.\"IdTipoPrograma\",p.\"Descripcion\",p.\"MaximoIntentos\",p.\"Modulo\",p.\"NombreClasePrograma\", "
			+ "p.\"TipoClasePrograma\",p.\"EsPauta\",p.\"Ejecucion\",p.\"RequiereTipoPrograma\",p.\"SiguienteTipoPrograma\",p.\"IdPrograma\", "
			+ "p.\"IdGrupoParametro\",p.\"IdAgenda\",p.\"Intento\",p.\"IdUsuario\",p.\"IdEstadoPrograma\",p.\"NombreEstadoPrograma\",p.\"ResultadoPrograma\", "
			+ "p.\"FechaCreacionPrograma\",p.\"FechaEjecucionPrograma\",p.\"FechaFinPrograma\",p.\"FechaSistemaPrograma\",p.\"IdProgramaReprocesado\", "
			+ "p.\"IdControlPauta\",p.\"FechaSistema\",p.\"FechaCreacion\",p.\"IdEstado\",p.\"NombreEstado\",p.\"Mensaje\" "
			+ "from \"BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta\" p "
			+ "join \"BSCL.Plataforma.BD::BAT.Agenda\" a on p.\"IdTipoPrograma\" = a.\"IdTipoPrograma\"	"
			+ "where p.\"IdTipoPrograma\"= :IdTipoPrograma and CURRENT_UTCTIMESTAMP > coalesce(a.\"UltimaFechaDeEjecucion\",'1900-01-01') "
			+ "and p.\"FechaEjecucionPrograma\"<=a.\"UltimaFechaDeEjecucion\" "
			+ "and p.\"IdEstadoPrograma\" = 'TER' " //agregado 16-10-2017 solo debe buscar procesos anteriores que hayan terminado OK
			+ "and p.\"FechaEjecucionPrograma\"!='0001-01-01 00:00:00.000' order by p.\"FechaEjecucionPrograma\" desc; ";
	
	private final String SQL_PautaSiguiente = "select top 1  p.\"FechaEjecucionPrograma\",a.\"UltimaFechaDeEjecucion\", "
			+ "p.\"IdTipoPrograma\",p.\"Descripcion\",p.\"MaximoIntentos\",p.\"Modulo\",p.\"NombreClasePrograma\", "
			+ "p.\"TipoClasePrograma\",p.\"EsPauta\",p.\"Ejecucion\",p.\"RequiereTipoPrograma\",p.\"SiguienteTipoPrograma\",p.\"IdPrograma\", "
			+ "p.\"IdGrupoParametro\",p.\"IdAgenda\",p.\"Intento\",p.\"IdUsuario\",p.\"IdEstadoPrograma\",p.\"NombreEstadoPrograma\",p.\"ResultadoPrograma\", "
			+ "p.\"FechaCreacionPrograma\",p.\"FechaEjecucionPrograma\",p.\"FechaFinPrograma\",p.\"FechaSistemaPrograma\",p.\"IdProgramaReprocesado\", "
			+ "p.\"IdControlPauta\",p.\"FechaSistema\",p.\"FechaCreacion\",p.\"IdEstado\",p.\"NombreEstado\",p.\"Mensaje\" "
			+ "from \"BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta\" p "
			+ "join \"BSCL.Plataforma.BD::BAT.Agenda\" a on p.\"IdTipoPrograma\" = a.\"IdTipoPrograma\" "
			+ "where p.\"IdTipoPrograma\" = :IdTipoPrograma "
			+ "and p.\"FechaEjecucionPrograma\">=a.\"UltimaFechaDeEjecucion\" "
			+ "and p.\"FechaEjecucionPrograma\"!='0001-01-01 00:00:00.000' "
			+ "and p.\"IdEstadoPrograma\"='PEN' " //agregado 2017-06-19
			+ "order by p.\"FechaEjecucionPrograma\" asc; ";
	
	private final String SQL_ModuloEmpresa = "select \"IdCREModuloEmpresa\",\"IdEmpresa\",\"IdModulo\",\"NombreModuloEmpresa\",\"TipoApertura\",\"HoraApertura\",\"TipoCierre\",\"HoraCierre\",\"IdEstado\",\"FechaProceso\",\"TipoEstado\" "
			+ "from \"BSCL.Plataforma.BD::CRE.ModuloEmpresa\" where \"IdModulo\" = 'SIS' and \"IdEmpresa\"=:IdEmpresa";

	/*******************
	 * clases in/out
	 *******************/

	public static class Pauta {
		public String		IdTipoPrograma; 
		public String		Descripcion; 
		public Integer		MaximoIntentos; 
		public String		Modulo; 
		public String		NombreClasePrograma; 
		public String		TipoClasePrograma; 
		public String		EsPauta; 
		public String		Ejecucion; 
		public String		RequiereTipoPrograma; 
		public String		SiguienteTipoPrograma; 
		public Long			IdPrograma; 
		public Long			IdGrupoParametro; 
		public Long			IdAgenda; 
		public Integer		Intento; 
		public Integer		IdUsuario; 
		public String		IdEstadoPrograma; 
		public String		NombreEstadoPrograma; 
		public String		ResultadoPrograma; 
		public java.sql.Timestamp	FechaCreacionPrograma; 
		public java.sql.Timestamp	FechaEjecucionPrograma; 
		public java.sql.Timestamp	FechaFinPrograma; 
		public java.sql.Date	FechaSistemaPrograma; 
		public Long				IdProgramaReprocesado; 
		public Long				IdControlPauta; 
		public java.sql.Date	FechaSistema; 
		public java.sql.Timestamp	FechaCreacion; 
		public String			IdEstado; 
		public String			NombreEstado; 
		public String			Mensaje; 
	};
	
	public static class ModuloEmpresa {
		public Long			IdCREModuloEmpresa; 
		public Long			IdEmpresa; 
		public String		IdModulo; 
		public String		NombreModuloEmpresa; 
		public String		TipoApertura; 
		public String		HoraApertura; 
		public String		TipoCierre; 
		public String		HoraCierre; 
		public String		IdEstado; 
		public String		FechaProceso; 
		public String		TipoEstado; 
	}
	
	public static class ConsultarPautaEjecutarEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdProgramaActual;
	}
	
	public static class ConsultarPautaEjecutarSalida {
		public Long 			P_OUT_ERRORESCONTADOR;
		public Long 			P_OUT_IDPROGRAMAANTERIOR;
		public String 			P_OUT_IDTIPOPROGRAMAANTERIOR;
		public Long 			P_OUT_IDPROGRAMASIGUIENTE;
		public String 			P_OUT_IDTIPOPROGRAMASIGUIENTE;
		public Pauta			P_OUT_PAUTAACTUAL;
		public Pauta			P_OUT_PAUTAANTERIOR;
		public Pauta			P_OUT_PAUTASIGUIENTE;
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ConsultarPautaEjecutarEntrada in=gson.fromJson(jin.toString() ,ConsultarPautaEjecutarEntrada.class);
		ConsultarPautaEjecutarSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getMBConnection()) {
			ConsultarPautaEjecutarEntrada entrada = new ConsultarPautaEjecutarEntrada();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_IdProgramaActual = new Long(216074);
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ConsultarPautaEjecutar demo=new ConsultarPautaEjecutar();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ConsultarPautaEjecutarSalida logica(Connection con, ConsultarPautaEjecutarEntrada entrada) throws Exception {
		ConsultarPautaEjecutarSalida salida = new ConsultarPautaEjecutarSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		SQL sql=new SQL();
		SimpleDateFormat dfFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String fechaJava = dfFechaHora.format(new Date());		
		// Consultar Pauta Actual
		HashMap<String,Object> parmsPautaActual = new HashMap<String,Object>();
		parmsPautaActual.put("IdPrograma", entrada.p_IdProgramaActual);	
		List<Pauta> resPautaActual = sql.query(con, SQL_PautaActual,parmsPautaActual,Pauta.class);
		if(resPautaActual.size()>0){
			salida.P_OUT_PAUTAACTUAL = resPautaActual.get(0);
			//Consultar Pauta Anterior
			if(salida.P_OUT_PAUTAACTUAL.RequiereTipoPrograma.length()>0){
				if(salida.P_OUT_PAUTAACTUAL.RequiereTipoPrograma.equalsIgnoreCase("CORE_CIERRE_SISTEMA")){
					HashMap<String,Object> parmsConsultaModulo = new HashMap<String,Object>();
					parmsConsultaModulo.put("IdEmpresa", entrada.p_IdEmpresa);	
					List<ModuloEmpresa> resModuloEmpresa = sql.query(con, SQL_ModuloEmpresa,parmsConsultaModulo,ModuloEmpresa.class);
					if(resModuloEmpresa.size()>0){
						salida.P_OUT_IDTIPOPROGRAMAANTERIOR = salida.P_OUT_PAUTAACTUAL.RequiereTipoPrograma;
						salida.P_OUT_IDPROGRAMAANTERIOR = new Long(0);
						salida.P_OUT_PAUTAANTERIOR = new Pauta();
						salida.P_OUT_PAUTAANTERIOR.IdEstado = "ACT";
						salida.P_OUT_PAUTAANTERIOR.IdEstadoPrograma = (resModuloEmpresa.get(0).TipoEstado.equals("C"))?"TER":"PEN";
					}else{
						log.write("Error: No existe el módulo sistema");
						salida.P_OUT_ERRORESCONTADOR++;
					}
					
				}else{
					HashMap<String,Object> parmsPautaAnterior = new HashMap<String,Object>();
					parmsPautaAnterior.put("IdTipoPrograma", salida.P_OUT_PAUTAACTUAL.RequiereTipoPrograma);
					List<Pauta> resPautaAnterior = sql.query(con, SQL_PautaAnterior,parmsPautaAnterior,Pauta.class);
					salida.P_OUT_IDTIPOPROGRAMAANTERIOR = salida.P_OUT_PAUTAACTUAL.RequiereTipoPrograma;
					if(resPautaAnterior.size()>0){
						salida.P_OUT_PAUTAANTERIOR = resPautaAnterior.get(0);
						salida.P_OUT_IDPROGRAMAANTERIOR = salida.P_OUT_PAUTAANTERIOR.IdPrograma;
					}
				}
				
			}
			//Consultar Pauta Siguiente
			if(salida.P_OUT_PAUTAACTUAL.SiguienteTipoPrograma.length()>0){
				HashMap<String,Object> parmsPautaSiguiente = new HashMap<String,Object>();
				parmsPautaSiguiente.put("IdTipoPrograma", salida.P_OUT_PAUTAACTUAL.SiguienteTipoPrograma);
				//parmsPautaSiguiente.put("FechaEjecucionPrograma", fechaJava);
				List<Pauta> resPautaSiguiente = sql.query(con, SQL_PautaSiguiente,parmsPautaSiguiente,Pauta.class);
				salida.P_OUT_IDTIPOPROGRAMASIGUIENTE = salida.P_OUT_PAUTAACTUAL.SiguienteTipoPrograma;
				if(resPautaSiguiente.size()>0){
					salida.P_OUT_PAUTASIGUIENTE = resPautaSiguiente.get(0);
					salida.P_OUT_IDPROGRAMASIGUIENTE = salida.P_OUT_PAUTASIGUIENTE.IdPrograma;
				}
			}
		}
		else{
			salida.P_OUT_ERRORESCONTADOR++;
		}
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
	}
}
