package cl.bcs.socionegocio.bck;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizaRequisitoClienteBlockChain implements JavaCall {

	private final String SQL_SELECT_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN 	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaRequisitoClienteBlockChain_SelectClienteIndicadorRequisitoBlockChain.sql");
	private final String SQL_SELECT_INDICADOR_REQUISITO_BLOCKCHAIN 			=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaRequisitoClienteBlockChain_SelectIndicadorRequisitoBlockChain.sql");
	private final String SQL_INSERT_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN 	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaRequisitoClienteBlockChain_InsertClienteIndicadorRequisitoBlockChain.sql");
	private final String SQL_UPDATE_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN 	=	loadResourceToString("cl/bcs/socionegocio/bck/sql/ActualizaRequisitoClienteBlockChain_UpdateClienteIndicadorRequisitoBlockChain.sql");
	
	public static class ActualizaRequisitoClienteBlockChain_Entrada extends BcsPojo {
		public Long 			p_IdEmpresa;
		public Long				p_IdUsuario;
		public List<Cliente>	p_Clientes;
		public String			p_IdTipoDocumento;
		
		public ActualizaRequisitoClienteBlockChain_Entrada() {
			this.p_Clientes = new ArrayList<Cliente>();
		}
	}
	
	public static class ActualizaRequisitoClienteBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR = 0L;
		public ActualizaRequisitoClienteBlockChain_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Cliente extends BcsPojo {
		public Long	IdCliente;
		
		public Cliente(Long idCliente) {
			this.IdCliente = idCliente;
		}
		
		public Cliente() { }
	}
	
	public static class ClienteIndicador extends Cliente {
		public String 	ValorIndicador;
		public Long		IdTipoIndicador;
	}
	
	public static class Indicador extends BcsPojo {
		public Long		IdIndicador;
		public Long		IdEntidad;
		public String	IdTipoEntidad;
		public Long		IdTipoIndicador;
		public String	ModeloCliente;
		public Long		IdEmpresa;
		public String	IdEstadoAutorizacion;
		public String	FechaHoraSistema;
		public String 	ValorIndicador;
		
		public Indicador() {
			this.IdIndicador = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		ActualizaRequisitoClienteBlockChain_Entrada in = gson.fromJson(jin.toString(), ActualizaRequisitoClienteBlockChain_Entrada.class);
		ActualizaRequisitoClienteBlockChain_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ActualizaRequisitoClienteBlockChain_Salida logica(Connection con, ActualizaRequisitoClienteBlockChain_Entrada entrada) throws Exception {
		
		ActualizaRequisitoClienteBlockChain_Salida salida 				= new ActualizaRequisitoClienteBlockChain_Salida();
		HashMap<String,Object> parametroEntrada 						= new HashMap<String,Object>();
		SQL sql 														= new SQL();
		List<Indicador> indicadoresClienteBlockChainInsertar 			= new ArrayList<>();
		List<Indicador> indicadoresClienteBlockChainActualizar 			= new ArrayList<>();
		
		try {
			
			if ( entrada.p_Clientes.size() == 0 ) {
				return salida;
			}
			
			parametroEntrada.put("p_IdEmpresa"		, entrada.p_IdEmpresa);
			parametroEntrada.put("p_IdTipoDocumento", entrada.p_IdTipoDocumento);
			parametroEntrada.put("p_IdCliente"		, entrada.p_Clientes.parallelStream().map(cliente -> cliente.IdCliente).collect(Collectors.toList()));
			List<ClienteIndicador> clienteIndicadorBlockChain = sql.query(con, SQL_SELECT_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN, parametroEntrada, ClienteIndicador.class);
			
			parametroEntrada.remove("p_IdCliente");	
			parametroEntrada.put("p_IdEmpresa"		, entrada.p_IdEmpresa);
			parametroEntrada.put("p_IdTipoDocumento", entrada.p_IdTipoDocumento);
			List<Indicador> indicadorBlockChain = sql.query(con, SQL_SELECT_INDICADOR_REQUISITO_BLOCKCHAIN, parametroEntrada, Indicador.class);
			String fechaHoraSistema 			= ObtenerFechaYHoraSistema(con, entrada.p_IdEmpresa);
			
			for ( Cliente cliente : entrada.p_Clientes ) {
				ConsultarEstadoContratoBlockchain consultarEstadoContratoBlockchain = new ConsultarEstadoContratoBlockchain();
				ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Entrada consultarEstadoContratoBlockchainEntrada = new ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Entrada();
				consultarEstadoContratoBlockchainEntrada.p_IdTipoDocumento 	= entrada.p_IdTipoDocumento;
				consultarEstadoContratoBlockchainEntrada.p_IdCliente 		= cliente.IdCliente.toString();
				ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Salida consultarEstadoContratoBlockchainSalida = consultarEstadoContratoBlockchain.logica(con, consultarEstadoContratoBlockchainEntrada);
				
				if ( consultarEstadoContratoBlockchainSalida.p_Out_ContadorError == 0 ) {
					
					for ( Indicador indicador : indicadorBlockChain ) {
						indicador.IdEntidad 		= cliente.IdCliente;
						indicador.IdEmpresa 		= entrada.p_IdEmpresa;
						indicador.ValorIndicador 	= consultarEstadoContratoBlockchainSalida.p_Out_Status.equals( ConstantesBCK.ESTADO_APROBADO_CONTRATO_BLOCKCHAIN) ? "S" : "N";
						indicador.FechaHoraSistema	= fechaHoraSistema;
						
						if ( clienteIndicadorBlockChain.stream()
								.anyMatch( cliente_indicador -> cliente_indicador.IdCliente.equals( cliente.IdCliente ) 
										&& cliente_indicador.IdTipoIndicador.equals( indicador.IdTipoIndicador ) ) ) {
							indicadoresClienteBlockChainActualizar.add( indicador );
						}
						else {
							indicadoresClienteBlockChainInsertar.add( indicador );
						}
					}
				}
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_ACTUALIZA_REQUISITO_CLIENTE_DOCUMENTO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			}
			
			if ( indicadoresClienteBlockChainInsertar.size() > 0 ) {
				sql.batch(con, SQL_INSERT_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN, indicadoresClienteBlockChainInsertar, Indicador.class);
			}
			
			if ( indicadoresClienteBlockChainActualizar.size() > 0 ) {
				sql.batch(con, SQL_UPDATE_CLIENTE_INDICADOR_REQUISITO_BLOCKCHAIN, indicadoresClienteBlockChainActualizar, Indicador.class);
			}
			
		}
		catch (Exception ex) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR", ex.getMessage()));
		}
		
		return salida;
	}
	
	public String ObtenerFechaYHoraSistema(Connection con, Long idEmpresa) throws SQLException {
		SQL sql 						= new SQL();
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		String fechaHoraSistema			= "";
		
		parmsIN.put("p_IdEmpresa"				, idEmpresa);
		parmsOUT.put("p_out_FechaHoraSistema"	, String.class);
		
		String queryCall 			= "call \"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema\" (:p_IdEmpresa, @p_out_FechaHoraSistema)";
		HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);
		
		fechaHoraSistema 				=  (String) rpt.get("p_out_FechaHoraSistema");
		
		return fechaHoraSistema;
	}
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
			
		try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();						
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":54,\"p_Clientes\":[{\"IdCliente\":1423804},{\"IdCliente\":1423878}],\"p_IdTipoDocumento\":\"CONTRATOPRESTAMOANEXO43\"}";
				ActualizaRequisitoClienteBlockChain_Entrada Objin = gson.fromJson(json, ActualizaRequisitoClienteBlockChain_Entrada.class);
				ActualizaRequisitoClienteBlockChain demo = new ActualizaRequisitoClienteBlockChain();
	 			
				System.out.println("ENTRADA:" + Objin.toString());
				
	 			try {
	 				ActualizaRequisitoClienteBlockChain_Salida ObjOut = demo.logica(con, Objin);
	 				System.out.println("SALIDA:" + ObjOut);
	 				con.commit();
	 				//con.rollback();
	 			} catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
		   }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
