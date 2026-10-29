
package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.text.SimpleDateFormat;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;

public class ModificarEmprestito implements JavaCall
{	
	//-------------------------------------------------
	// LOG
	//-------------------------------------------------
	private final Log log = new Log("CUS.ModificarEmprestito");
	
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	
	private final String SQL_SELECT_IDCARTERA				= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_SelectIdCartera.sql");      	//para validar cartara
	private final String SQL_SELECT_IDSUBYACENTE			= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_SelectIdSubyacente.sql");   	//para validar subyacente
	private final String SQL_SELECT_SECUENCIA_IDCARTERA		= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_SelectSecuenciaIdCartera.sql"); //obtiene nueva secuencia de cartera
	private final String SQL_SELECT_SECUENCIA_IDSUBYACENTE	= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_SelectSecuenciaIdSubyacente.sql"); // Obtiene nueva secuencia del subyacente
	private final String SQL_INSERT_SELECT_CARTERA			= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_InsertSelectCartera.sql");      // crea nueva cartera a partir de la original
	private final String SQL_INSERT_SELECT_SUBYACENTE		= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_InsertSelectSubyacente.sql");
	private final String SQL_UPDATE_ESTADO_CARTERA			= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_UpdateEstadoCartera.sql");    	// cambia estado cartera
	private final String SQL_UPDATE_ESTADO_SUBYACENTE		= loadResourceToString("cl/bcs/custodia/sql/ModificarEmprestito_UpdateEstadoSubyacente.sql");   // cambia estado subyacente
	

	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ModificarEmprestito_Entrada
	{
		public Long       		p_IdEmpresa;
		public Long       		p_IdUsuario;
		public String     		p_IdModulo;
		public Long				p_IdCartera;
		public Long				p_IdSubyacente;
		public BigDecimal		p_MontoPremio		= new BigDecimal(0);
		public BigDecimal		p_TasaPremio		= new BigDecimal(0);
		public BigDecimal		p_MontoAPlazo		= new BigDecimal(0);
		public BigDecimal		p_InteresDiario		= new BigDecimal(0);
		public BigDecimal		p_InteresAcumulado	= new BigDecimal(0);
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------	
	public static class ModificarEmprestito_Salida
	{	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_NEWIDCARTERA;
		public Long							P_OUT_NEWIDSUBYACENTE;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_ERRCODE;
		public String						P_OUT_ERREXT;
		
		public ModificarEmprestito_Salida()
		{
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class Cartera
	{
		public Long		IdCartera;
		public String	IdEstado;
	}
	
	public static class Subyacente
	{
		public Long		IdSubyacente;
		public String	IdEstado;
	}
	
	public static class NewIdCartera
	{
		public Long NewIdCartera;
	}
	
	public static class NewIdSubyacente
	{
		public Long NewIdSubyacente;
	}
	
	public static class ParametroInsertCartera
	{
		public long			p_out_NewIdCartera;
		public long			p_IdUsuario;
		public BigDecimal	p_TasaPremio;
		public BigDecimal	p_MontoPremio;
		public BigDecimal	p_MontoAPlazo;
		public long			p_IdCartera;
		
		public BigDecimal	p_InteresDiario;
		public BigDecimal	p_InteresAcumulado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ParametroInsertSubyacente
	{
		public long			p_out_NewIdSubyacente;
		public BigDecimal	p_TasaPremio;
		public long			p_IdSubyacente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception
	{
		Gson gson = new Gson();
		ModificarEmprestito_Entrada in = gson.fromJson(jin.toString(), ModificarEmprestito_Entrada.class);
		ModificarEmprestito_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	

	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ModificarEmprestito_Salida logica(Connection con, ModificarEmprestito_Entrada entrada) throws Exception
	{
		SQL sql = new SQL();
		ModificarEmprestito_Salida salida = new ModificarEmprestito_Salida();
		
		log.write("== INICIO EJECUCION MODIFICAR EMPRESTITOS ==");
		
		
		//---------------------------------------------------------------------------------------------
		// VALIDA SI CUMPLE REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) 
		{
			log.write("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		
	
		
		//---------------------------------------------------------------------------------------------
		// VALIDAR CARTERA Y SUBYACENTE
		HashMap<String,Object> ParamIdCartera = new HashMap<String,Object>();
		ParamIdCartera.put("p_IdCartera", entrada.p_IdCartera);
		
		HashMap<String,Object> ParamIdSubyacente = new HashMap<String,Object>();
		ParamIdSubyacente.put("p_IdSubyacente", entrada.p_IdSubyacente);
		
		List<Cartera> Cartera = sql.query(con,SQL_SELECT_IDCARTERA, ParamIdCartera,Cartera.class);
		List<Subyacente> Subyacente = sql.query(con,SQL_SELECT_IDSUBYACENTE, ParamIdSubyacente,Subyacente.class);
		
		if (Cartera.size() == 0 && Subyacente.size() == 0)
		{
			log.write("CARTERA Y SUBYACENTE NO ESTA VIGENTE");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CARTERA_Y_SUBYACENTE_NO_ESTA_VIGENTE", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		else
		{
			//---------------------------------------------------------------------------------------------
			// OBTIENE NEW ID CARTERA
			List<NewIdCartera> NewIdCartera = sql.query(con,SQL_SELECT_SECUENCIA_IDCARTERA, new HashMap<>(),NewIdCartera.class);
			
			if (NewIdCartera.size() == 0)
			{
				log.write("NO SE HA GENERADO ID CARTERA");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_HA_GENERADO_ID_CARTERA", null, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			else
			{
				salida.P_OUT_NEWIDCARTERA = NewIdCartera.get(0).NewIdCartera;
				log.write("DATOS ID CARTERA > " + NewIdCartera.get(0).NewIdCartera);
			}
			
			//---------------------------------------------------------------------------------------------
			// INGRESAR DATOS CARTERA
			List<ParametroInsertCartera> InsertCartera = new ArrayList<ParametroInsertCartera>();
			ParametroInsertCartera ParamCartera = new ParametroInsertCartera();
			ParamCartera.p_out_NewIdCartera = NewIdCartera.get(0).NewIdCartera;
			ParamCartera.p_IdUsuario = entrada.p_IdUsuario;
			ParamCartera.p_TasaPremio = entrada.p_TasaPremio;
			ParamCartera.p_MontoPremio = entrada.p_MontoPremio;
			ParamCartera.p_MontoAPlazo = entrada.p_MontoAPlazo;
			ParamCartera.p_IdCartera = entrada.p_IdCartera;
			ParamCartera.p_InteresDiario = entrada.p_InteresDiario;
			ParamCartera.p_InteresAcumulado = entrada.p_InteresAcumulado;
			
			InsertCartera.add(ParamCartera);
			sql.batch(con, SQL_INSERT_SELECT_CARTERA, InsertCartera, ParametroInsertCartera.class);
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE NEW ID SUBYACENTE
			List<NewIdSubyacente> NewIdSubyacente = sql.query(con,SQL_SELECT_SECUENCIA_IDSUBYACENTE, new HashMap<>(),NewIdSubyacente.class);
			
			if (NewIdSubyacente.size() == 0)
			{
				log.write("NO SE HA GENERADO ID SUBYACENTE");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_HA_GENERADO_ID_SUBYACENTE", null, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			else
			{
				salida.P_OUT_NEWIDSUBYACENTE = NewIdSubyacente.get(0).NewIdSubyacente;
				log.write("DATOS ID SUBYACENTE > " + NewIdSubyacente.get(0).NewIdSubyacente);
			}
			
			//---------------------------------------------------------------------------------------------
			// INGRESAR DATOS SUBYACENTE
			List<ParametroInsertSubyacente> InsertSubyacente = new ArrayList<ParametroInsertSubyacente>();
			ParametroInsertSubyacente ParamSubyacente = new ParametroInsertSubyacente();
			ParamSubyacente.p_out_NewIdSubyacente = NewIdSubyacente.get(0).NewIdSubyacente;
			ParamSubyacente.p_TasaPremio = entrada.p_TasaPremio;
			ParamSubyacente.p_IdSubyacente = entrada.p_IdSubyacente;
			InsertSubyacente.add(ParamSubyacente);
			sql.batch(con, SQL_INSERT_SELECT_SUBYACENTE, InsertSubyacente, ParametroInsertSubyacente.class);
			
			
			
			
			//---------------------------------------------------------------------------------------------
			// UPDATE ESTADO CARTERA
			sql.batch(con, SQL_UPDATE_ESTADO_CARTERA, InsertCartera, ParametroInsertCartera.class);
			
			
			//---------------------------------------------------------------------------------------------
			// UPDATE ESTADO SUBYACENTE
			sql.batch(con, SQL_UPDATE_ESTADO_SUBYACENTE, InsertSubyacente, ParametroInsertSubyacente.class);
			
			
			//---------------------------------------------------------------------------------------------
			// CALL AGREGAR LOG
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date = format.parse(Utils.getFechaHoraSistema(con, "BSCL", entrada.p_IdEmpresa));
			String fechaHoraSistema = format.format(date);
			
			AgregarLog agregarLogService = new AgregarLog();
			AgregarLog.InAgregarLog.Log agregarLog = new AgregarLog.InAgregarLog.Log();
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			
			entradaAgregarLog.p_IdTrabajo = 1L;
			agregarLog.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			agregarLog.IdTipoLog = "MODIFICACION";
			agregarLog.IdModulo = "CUS";
			agregarLog.IdUsuario = entrada.p_IdUsuario;
			agregarLog.IdEmpresa = entrada.p_IdEmpresa;
			agregarLog.FechaSistemaLog = fechaHoraSistema;
			agregarLog.IdUsuarioAtendido = entrada.p_IdUsuario;
			agregarLog.IdTipoRegistro = "MODIFICAREMPRESTITO";
			agregarLog.IdRegistro = NewIdCartera.get(0).NewIdCartera.toString();
			agregarLog.NombreProceso = "PLUGIN_CUS.jar@cl.bcs.custodia.prt.ModificarEmprestito";
			agregarLog.EstadoProceso = "";
			agregarLog.Cantidad = new BigDecimal(0);
			agregarLog.Nemo = "";					
			entradaAgregarLog.p_Log.add(agregarLog);	
			agregarLogService.logica(con, entradaAgregarLog);
			
			
			
		}
		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());
		log.write("== TERMINO MODIFICAR EMPRESTITO ==");
		
		return salida;
	}
	

	 
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args) throws Exception
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getQAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"CUS\",\"p_IdCartera\":1526361,\"p_IdSubyacente\":2017,\"p_MontoPremio\":24592.259999999776,\"p_TasaPremio\":3.6,\"p_MontoAPlazo\":2952242.26,\"p_InteresDiario\":3513.1800000000003,\"p_InteresAcumulado\":0}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CUS\",\"p_IdCartera\":6717,\"p_IdSubyacente\":1010,\"p_MontoPremio\":1869.6000000000058,\"p_TasaPremio\":3.6,\"p_MontoAPlazo\":157669.6,\"p_InteresDiario\":186.96000000000004,\"p_InteresAcumulado\":0}";
			
		
			ModificarEmprestito_Entrada Objin = gson.fromJson(json, ModificarEmprestito_Entrada.class);
			ModificarEmprestito demo = new ModificarEmprestito();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try
 			{
 				ModificarEmprestito_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 			}
 			catch(Exception er)
 			{
				er.printStackTrace();
				con.rollback();
			}
	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
		
}

