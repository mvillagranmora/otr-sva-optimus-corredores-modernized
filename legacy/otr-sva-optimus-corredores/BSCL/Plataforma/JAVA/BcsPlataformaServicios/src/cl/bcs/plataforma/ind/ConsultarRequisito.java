package cl.bcs.plataforma.ind;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.bck.ActualizaRequisitoClienteBlockChain;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarRequisito implements JavaCall 
{
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_GRUPO_INDICADORES				= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectGrupoIndicadores.sql");
	private final String SQL_SELECT_INDICADORES						= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectIndicadores.sql");
	private final String SQL_SELECT_IDCLIENTE						= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectIdCliente.sql");
	private final String SQL_SELECT_IDTIPOCLIENTE					= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectIdTipoCliente.sql");
	private final String SQL_SELECT_INDICADORES_TIPOCLIENTE			= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectIndicadoresTipoCliente.sql");
	private final String SQL_SELECT_COMUN							= loadResourceToString("cl/bcs/plataforma/ind/sql/ConsultarRequisito_SelectComun.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN			= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ConsultarRequisito_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdEntidad				= 0L;
		public String				p_IdTipoEntidad			= "";
		public String				p_CodigoTipoIndicador	= "";
		public String				p_GrupoIndicador		= "";
		public String				p_ModeloCliente			= "";
		public Long					p_IdIndicador			= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ConsultarRequisito_Salida
	{
		public Long						P_OUT_INDICADORESCONTADOR;
		public List<Indicadores>		P_OUT_INDICADORES;
		public Long						P_OUT_GRUPOINDICADORESCONTADOR;
		public List<GrupoIndicadores>	P_OUT_GRUPOINDICADORES;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Indicadores
	{
		public Long		IdIndicador;
		public Long		IdTipoIndicador;
		public String	NombreTipoIndicador;
		public String	ClaseDato;
		public String	CodigoTipoIndicador;
		public String	RelacionDocumento;
		public String	ValorIndicador;
		public String	IdEstadoAutorizacion;
		public String	GrupoIndicador;
		public String	IdUso;
		public Long		CantidadDecimales;
		public Long		LongitudMinima;
		public Long		LongitudMaxima;
		public String	IdTipoEntidad;
		public Long		IdEntidad;
		public String	ModeloCliente;
		public String	IdTipoPersona;
		public String	IdTipoDocumento;
		public Long		IdClase;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class GrupoIndicadores
	{
		public String	GrupoIndicador;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectIdCliente
	{
        public Long		IdCliente;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectIdTipoCliente
	{
        public String	IdTipoCliente;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoDocumento {
		Long IdClase = 0L;
	}	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ConsultarRequisito_Salida logica(Connection conn, ConsultarRequisito_Entrada in) throws Exception
	{
		SQL sql 									= new SQL();
		ConsultarRequisito_Salida salida 			= new ConsultarRequisito_Salida();
		HashMap<String,Object> paramDatosCliente 	= new HashMap<String,Object>(); 
		
		String	vIdUso				= "REQ"; //Se buscan solo indicadores para la Logica de Negocio
		Long	vIdEntidadBase		= 0L;
		String	vIdTipoCliente		= "";
		String	vIdIndicadorCero	= "N";
		boolean vResultado			= tablaTemporal(conn, "CREATE", true, new ArrayList<Indicadores>());
		
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("p_IdEntidad", in.p_IdEntidad);
		parametroEntrada.put("p_ModeloCliente", in.p_ModeloCliente);
		parametroEntrada.put("p_IdIndicador", in.p_IdIndicador);
		parametroEntrada.put("p_IdTipoEntidad", in.p_IdTipoEntidad);
		parametroEntrada.put("p_IdUso", vIdUso);
		parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametroEntrada.put("p_CodigoTipoIndicador", in.p_CodigoTipoIndicador);
		parametroEntrada.put("p_GrupoIndicador", in.p_GrupoIndicador);
		List<GrupoIndicadores> grupoIndicadores = sql.query(conn, SQL_SELECT_GRUPO_INDICADORES, parametroEntrada, GrupoIndicadores.class);
		
		salida.P_OUT_GRUPOINDICADORES = grupoIndicadores;
		salida.P_OUT_GRUPOINDICADORESCONTADOR = new Long(grupoIndicadores.size());
		
		List<Indicadores> indicadores = sql.query(conn, SQL_SELECT_INDICADORES, parametroEntrada, Indicadores.class);
		
		/*** LOGICA BLOCKCHAIN ***/
		if ( in.p_IdTipoEntidad.equals("CLIENTE") ) {
			for ( Indicadores indicador : indicadores ) {
				if ( indicador.IdClase == 2 || indicador.IdClase == 3 ) {
					paramDatosCliente.put("p_IdTipoDocumento", indicador.IdTipoDocumento);
					List<TipoDocumento> arrTipoDocumento = sql.query(conn, SQL_SELECT_ESCONTRATOBLOCKCHAIN, paramDatosCliente, TipoDocumento.class);
					paramDatosCliente.clear();
					if ( arrTipoDocumento.size() == 0 ) {
						continue;
					}
					ActualizaRequisitoClienteBlockChain actualizaRequisitoClienteBlockChain 													= new ActualizaRequisitoClienteBlockChain();
			    	ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Entrada actualizaRequisitoClienteBlockChainEntrada 	= new ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Entrada();
			    	actualizaRequisitoClienteBlockChainEntrada.p_IdEmpresa 			= in.p_IdEmpresa;
			    	actualizaRequisitoClienteBlockChainEntrada.p_IdTipoDocumento 	= indicador.IdTipoDocumento;
			    	actualizaRequisitoClienteBlockChainEntrada.p_Clientes.add( new ActualizaRequisitoClienteBlockChain.Cliente(in.p_IdEntidad) );
			    	ActualizaRequisitoClienteBlockChain.ActualizaRequisitoClienteBlockChain_Salida actualizaRequisitoClienteBlockChainSalida = actualizaRequisitoClienteBlockChain.logica(conn, actualizaRequisitoClienteBlockChainEntrada);
				}
			}
			indicadores.clear();
			indicadores = sql.query(conn, SQL_SELECT_INDICADORES, parametroEntrada, Indicadores.class);
		}
		/*** FIN LOGICA BLOCKCHAIN ***/
		
		tablaTemporal(conn, "INSERT", true, indicadores);
		
		if(in.p_IdEntidad > 0)
		{
			vIdEntidadBase = in.p_IdEntidad;
			
			if(in.p_IdTipoEntidad.equals("PORTAFOLIO") || in.p_IdTipoEntidad.equals("CLIENTE"))
			{
				HashMap<String,Object> parametroEntradaSelect = new HashMap<String,Object>();
				
				if(in.p_IdTipoEntidad.equals("PORTAFOLIO"))
				{
					parametroEntradaSelect.put("p_IdEntidad", in.p_IdEntidad);
					List<SelectIdCliente> selectIdCliente = sql.query(conn, SQL_SELECT_IDCLIENTE, parametroEntradaSelect, SelectIdCliente.class);
					
					vIdEntidadBase = selectIdCliente.get(0).IdCliente;
				}
				
				parametroEntradaSelect.put("p_IdEntidadBase", vIdEntidadBase);
				List<SelectIdTipoCliente> selectIdTipoCliente = sql.query(conn, SQL_SELECT_IDTIPOCLIENTE, parametroEntradaSelect, SelectIdTipoCliente.class);
				
				vIdTipoCliente = selectIdTipoCliente.get(0).IdTipoCliente;
			}
			
			if(in.p_ModeloCliente.equals(""))
			{
				vIdIndicadorCero = "S";
			}
		}
		
		parametroEntrada.put("p_IdTipoCliente", vIdTipoCliente);
		parametroEntrada.put("p_IdEntidad", in.p_IdEntidad);
		List<Indicadores> indicadoresTipoCliente = sql.query(conn, SQL_SELECT_INDICADORES_TIPOCLIENTE, parametroEntrada, Indicadores.class);
		
		tablaTemporal(conn, "INSERT", false, indicadoresTipoCliente);
		
		List<Indicadores> indicadoresComun = sql.query(conn, SQL_SELECT_COMUN, new HashMap<String,Object>(), Indicadores.class);
		
		salida.P_OUT_INDICADORES = indicadoresComun;
		salida.P_OUT_INDICADORESCONTADOR = new Long(indicadoresComun.size());
		
		if(!vResultado)
		{
			tablaTemporal(conn, "DROP", true, new ArrayList<Indicadores>());
		}
		
		return salida;
	}
	
	public boolean tablaTemporal(Connection conn, String tipo, boolean opcion, List<Indicadores> listaIndicadores)
	{
		SQL sql = new SQL();
		
		if(tipo.equals("CREATE"))
		{
			try
			{
				SQL.exec(conn, "CREATE LOCAL TEMPORARY TABLE #ConsultarRequisito_Indicadores (\"IdIndicador\" BIGINT, \"IdTipoIndicador\" BIGINT, \"NombreTipoIndicador\" NVARCHAR(255), \"ClaseDato\" NVARCHAR(40), \"CodigoTipoIndicador\" NVARCHAR(10), \"RelacionDocumento\" NVARCHAR(1), \"ValorIndicador\" NVARCHAR(255), \"IdEstadoAutorizacion\" NVARCHAR(3), \"GrupoIndicador\" NVARCHAR(40), \"IdUso\" NVARCHAR(3), \"CantidadDecimales\" BIGINT, \"LongitudMinima\" BIGINT, \"LongitudMaxima\" BIGINT, \"IdTipoEntidad\" NVARCHAR(40), \"IdEntidad\" BIGINT, \"ModeloCliente\" NVARCHAR(40), \"IdTipoPersona\" NVARCHAR(2), \"IdTipoDocumento\" NVARCHAR(40), \"IdClase\" BIGINT)");
				SQL.exec(conn, "CREATE LOCAL TEMPORARY TABLE #ConsultarRequisito_IndicadoresTipoCliente (\"IdIndicador\" BIGINT, \"IdTipoIndicador\" BIGINT, \"NombreTipoIndicador\" NVARCHAR(255), \"ClaseDato\" NVARCHAR(40), \"CodigoTipoIndicador\" NVARCHAR(10), \"RelacionDocumento\" NVARCHAR(1), \"ValorIndicador\" NVARCHAR(255), \"IdEstadoAutorizacion\" NVARCHAR(3), \"GrupoIndicador\" NVARCHAR(40), \"IdUso\" NVARCHAR(3), \"CantidadDecimales\" BIGINT, \"LongitudMinima\" BIGINT, \"LongitudMaxima\" BIGINT, \"IdTipoEntidad\" NVARCHAR(40), \"IdEntidad\" BIGINT, \"ModeloCliente\" NVARCHAR(40), \"IdTipoPersona\" NVARCHAR(2), \"IdTipoDocumento\" NVARCHAR(40), \"IdClase\" BIGINT)");
			}
			catch (Exception error1)
			{
				try
				{
					SQL.exec(conn, "DROP TABLE #ConsultarRequisito_Indicadores");
					SQL.exec(conn, "DROP TABLE #ConsultarRequisito_IndicadoresTipoCliente");
				}
				catch (Exception error2)
				{
					return true;
				}
			}
		}
		else if(tipo.equals("INSERT"))
		{
			if(!listaIndicadores.isEmpty())
			{
				if(opcion)
				{
					try
					{
						sql.batch(conn, "INSERT INTO #ConsultarRequisito_Indicadores (\"IdIndicador\", \"IdTipoIndicador\", \"NombreTipoIndicador\", \"ClaseDato\", \"CodigoTipoIndicador\", \"RelacionDocumento\", \"ValorIndicador\", \"IdEstadoAutorizacion\", \"GrupoIndicador\", \"IdUso\", \"CantidadDecimales\", \"LongitudMinima\", \"LongitudMaxima\", \"IdTipoEntidad\", \"IdEntidad\", \"ModeloCliente\", \"IdTipoPersona\", \"IdTipoDocumento\", \"IdClase\") VALUES (:IdIndicador, :IdTipoIndicador, :NombreTipoIndicador, :ClaseDato, :CodigoTipoIndicador, :RelacionDocumento, :ValorIndicador, :IdEstadoAutorizacion, :GrupoIndicador, :IdUso, :CantidadDecimales, :LongitudMinima, :LongitudMaxima, :IdTipoEntidad, :IdEntidad, :ModeloCliente, :IdTipoPersona, :IdTipoDocumento, :IdClase)", listaIndicadores, Indicadores.class);
					}
					catch (Exception error1)
					{
						try
						{
							sql.batch(conn, "DELETE FROM #ConsultarRequisito_Indicadores", listaIndicadores, Indicadores.class);
						}
						catch (Exception error2)
						{
							return true;
						}
					}
				}
				else
				{
					try
					{
						sql.batch(conn, "INSERT INTO #ConsultarRequisito_IndicadoresTipoCliente (\"IdIndicador\", \"IdTipoIndicador\", \"NombreTipoIndicador\", \"ClaseDato\", \"CodigoTipoIndicador\", \"RelacionDocumento\", \"ValorIndicador\", \"IdEstadoAutorizacion\", \"GrupoIndicador\", \"IdUso\", \"CantidadDecimales\", \"LongitudMinima\", \"LongitudMaxima\", \"IdTipoEntidad\", \"IdEntidad\", \"ModeloCliente\", \"IdTipoPersona\", \"IdTipoDocumento\", \"IdClase\") VALUES (:IdIndicador, :IdTipoIndicador, :NombreTipoIndicador, :ClaseDato, :CodigoTipoIndicador, :RelacionDocumento, :ValorIndicador, :IdEstadoAutorizacion, :GrupoIndicador, :IdUso, :CantidadDecimales, :LongitudMinima, :LongitudMaxima, :IdTipoEntidad, :IdEntidad, :ModeloCliente, :IdTipoPersona, :IdTipoDocumento, :IdClase)", listaIndicadores, Indicadores.class);
					}
					catch (Exception error1)
					{
						try
						{
							sql.batch(conn, "DELETE FROM #ConsultarRequisito_IndicadoresTipoCliente", listaIndicadores, Indicadores.class);
						}
						catch (Exception error2)
						{
							return true;
						}
					}
				}
			}
		}
		else if(tipo.equals("DROP"))
		{
			try
			{
				SQL.exec(conn, "DROP TABLE #ConsultarRequisito_Indicadores");
				SQL.exec(conn, "DROP TABLE #ConsultarRequisito_IndicadoresTipoCliente");
			}
			catch (Exception error1)
			{
				return true;
			}
		}

		return false;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ConsultarRequisito_Entrada in = gson.fromJson(jin.toString(), ConsultarRequisito_Entrada.class);
		ConsultarRequisito_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdEntidad\":11111,\"p_IdTipoEntidad\":\"CLIENTE\",\"p_ModeloCliente\":\"\"}";
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdEntidad\":11391,\"p_IdTipoEntidad\":\"CLIENTE\",\"p_ModeloCliente\":\"\"}";
			ConsultarRequisito_Entrada Objin = gson.fromJson(jsonCL, ConsultarRequisito_Entrada.class);

			ConsultarRequisito demo = new ConsultarRequisito();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ConsultarRequisito_Salida ObjOut = demo.logica(con, Objin);
				//con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}