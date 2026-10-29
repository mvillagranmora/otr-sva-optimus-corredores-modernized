package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.RegistroCambio;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.SalidaModificarClienteDatosGenerales;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDatosGeneralesCliente implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDatosGeneralesCliente.class);
	
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDatosGeneralesCliente_SelectClienteDatosGenerales.sql");
	
	public static class EntradaModificarDatosGeneralesCliente extends EntradaAdministrarWS {
		public List<DatosClienteDatosGenerales> p_ClienteDatosGenerales;
		
		public EntradaModificarDatosGeneralesCliente(){
			this.p_ClienteDatosGenerales = new ArrayList<DatosClienteDatosGenerales>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDatosGeneralesCliente jin = gson.fromJson(in.toString(), EntradaModificarDatosGeneralesCliente.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarDatosGeneralesCliente entrada) throws Exception {
		logger.info("INICIO::ModificarDatosGeneralesCliente");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<DatosClienteDatosGenerales> clientesDatosGenerales = obtenerDatosClienteDatosGenerales(conn,entrada);

		for(DatosClienteDatosGenerales datosCliente : clientesDatosGenerales) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = datosCliente.IdCliente;
		    entidadModificada.TipoEntidad = "DATOS GENERALES CLIENTE";
		    datosCliente.EsBorrador = "N";
		    datosCliente.CLI_IdEmpleadoEjecutivo = datosCliente.IdEmpleadoEjecutivo;
		    datosCliente.CLI_IdSegmentoCliente = datosCliente.IdSegmentoCliente;
		    
		    if(datosCliente.TipoEnrolamiento == null) {
				entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }

		    List<DatosClienteDatosGenerales> mergeCambios = new ArrayList<>();
		    DatosClienteDatosGenerales nuevosCampos = entrada.p_ClienteDatosGenerales.parallelStream()
			    	.filter(cli -> cli.IdPersona.equals(datosCliente.IdPersona))
			    	.findFirst().orElse(new DatosClienteDatosGenerales());
		    WebServiceUtils.merge(datosCliente, nuevosCampos);
		    mergeCambios.add(datosCliente);
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, datosCliente.Tipo, datosCliente.TipoEnrolamiento, "DATOS_GENERALES_CLIENTE");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, DatosClienteDatosGenerales.class, formulario, out);
		  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DatosClienteDatosGenerales> listaDeUno =  new ArrayList<>();
		    listaDeUno.add(datosCliente);
		    
		    ModificarClienteDatosGenerales modificarClienteDatosGenerales = new ModificarClienteDatosGenerales();
		    EntradaModificarClienteDatosGenerales entradaModificarClienteDatosGenerales = new EntradaModificarClienteDatosGenerales();
		    entradaModificarClienteDatosGenerales.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaModificarClienteDatosGenerales.p_IdModulo = entrada.p_IdModulo;
		    entradaModificarClienteDatosGenerales.p_IdUsuario = entrada.p_IdUsuario;
		    entradaModificarClienteDatosGenerales.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaModificarClienteDatosGenerales.p_IdCliente = datosCliente.IdCliente;
		    entradaModificarClienteDatosGenerales.p_IdCanal = "WBS";
		    entradaModificarClienteDatosGenerales.p_RegistroCambio = crearRegistroCambio(conn,listaDeUno);
		    
		    SalidaModificarClienteDatosGenerales salidaModificarClienteDatosGenerales = modificarClienteDatosGenerales.logica(conn, entradaModificarClienteDatosGenerales);

		    if( !salidaModificarClienteDatosGenerales.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaModificarClienteDatosGenerales.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    if(entidadModificada.Mensajes.isEmpty()) {
			entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		}
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosGeneralesCliente", finServicio - inicioServicio);
		return salida;
		
	}

	private List<RegistroCambio> crearRegistroCambio(Connection conn,List<DatosClienteDatosGenerales> datosGenerales) {
		logger.trace("INICIO::ModificarDatosGeneralesCliente.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambio> registrosCambio = new ArrayList<>();
		for(DatosClienteDatosGenerales cliente : datosGenerales) {
			registrosCambio.add(crearRegistroCambio("IdCategoriaInversionista", cliente.IdCategoriaInversionista));
			registrosCambio.add(crearRegistroCambio("CodigoEjecutivoExterno", cliente.CodigoEjecutivoExterno));
			registrosCambio.add(crearRegistroCambio("IdPerfilComercial", cliente.IdPerfilComercial));
			registrosCambio.add(crearRegistroCambio("IdTipoCliente", cliente.IdTipoCliente));
			registrosCambio.add(crearRegistroCambio("IdSegmentoCliente", cliente.IdSegmentoCliente));
			registrosCambio.add(crearRegistroCambio("IdSucursalPreferencia", cliente.IdSucursalPreferencia));
			registrosCambio.add(crearRegistroCambio("IdPersonaEjecutivo", cliente.IdEmpleadoEjecutivo));
			registrosCambio.add(crearRegistroCambio("TipoEnrolamiento", cliente.TipoEnrolamiento));
			registrosCambio.add(crearRegistroCambio("EsClienteFinal", cliente.ClienteFinal));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDatosGeneralesCliente.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	public static RegistroCambio crearRegistroCambio(String codigoCampo, String valor){
		logger.trace("INICIO::ModificarDatosGeneralesCliente.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDatosGeneralesCliente.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	public static RegistroCambio crearRegistroCambio(String codigoCampo, Long valor){
		logger.trace("INICIO::ModificarDatosGeneralesCliente.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDatosGeneralesCliente.crearRegistroCambio", fin - inicio);
		return registroCambio;
	}
	
	private List<DatosClienteDatosGenerales> obtenerDatosClienteDatosGenerales(Connection con, EntradaModificarDatosGeneralesCliente entrada) throws Exception {
		logger.debug("INICIO::ModificarDatosGeneralesCliente.obtenerDatosClienteDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_ClienteDatosGenerales.stream()
				  .map(cliente -> cliente.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<DatosClienteDatosGenerales> datosPersonaDatosGenerales = sql.query(con, SQL_SELECT_CLIENTE, params, DatosClienteDatosGenerales.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDatosGeneralesCliente.obtenerDatosClienteDatosGenerales", fin - inicio);
		return datosPersonaDatosGenerales;
	}

	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_ClienteDatosGenerales\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdPersona\": 1790863,\r\n"
				+ "      \"IdCategoriaInversionista\": \"CALIFICADO\",\r\n"
				+ "      \"IdPersonaEjecutivo\": 1774650,\r\n"
				+ "      \"IdSegmentoCliente\": 3,\r\n"
				+ "      \"Sucursal\": 3,\r\n"
				+ "      \"IdTipoCliente\": \"FAMILYOFFICE\",\r\n"
				+ "      \"CodigoEjecutivoExterno\": \"UN EJECUTIVO\"\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 4\r\n"
				+ "}";
			String jsonReq = "{\r\n"
					+ "  \"p_ClienteDatosGenerales\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1732307\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			System.out.println("ENTRADA=");
			System.out.println(jsonReq);
			EntradaModificarDatosGeneralesCliente entradaModificarDatosGeneralesCliente = gson.fromJson(jsonReq, EntradaModificarDatosGeneralesCliente.class);
			ModificarDatosGeneralesCliente modificarDatosGeneralesCliente = new ModificarDatosGeneralesCliente();				
			try {
				SalidaWBS salida= modificarDatosGeneralesCliente.logica(con, entradaModificarDatosGeneralesCliente);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA=");
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
	
}
