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
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.wbs.ModificarClienteWS.SocioNegocio;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.CuentaBancaria;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarCuentaBancaria implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarCuentaBancaria.class);
	private String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarCuentaBancaria_SelectCuentaBancaria.sql");
	private String SQL_SELECT_NOMBRE_BANCO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarBanco_SelectNombreBancoPorIdBanco.sql");
	
	public static class EntradaModificarCuentaBancaria extends EntradaAdministrarWS {
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaModificarCuentaBancaria(){
			this.p_CuentasBancarias = new ArrayList<CuentaBancaria>();
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarCuentaBancaria jin = gson.fromJson(in.toString(), EntradaModificarCuentaBancaria.class);
		
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::ModificarCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<CuentaBancaria> datosCuentasBancarias = obtenerDatosCuentasBancarias(conn,entrada);
		
		for(CuentaBancaria cuentaBancaria : datosCuentasBancarias) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = cuentaBancaria.IdCuentaBancaria;
		    entidadModificada.TipoEntidad = "CUENTA BANCARIA";
		    cuentaBancaria.EsBorrador = "N";
		    
			if (cuentaBancaria.TipoEnrolamiento == null) {
				entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
			}
		    
		    List<CuentaBancaria> mergeCambios = new ArrayList<>();
		    CuentaBancaria nuevosCampos = entrada.p_CuentasBancarias.parallelStream()
		    		.filter(cuenta -> cuenta.IdCuentaBancaria.equals(cuentaBancaria.IdCuentaBancaria))
		    		.findFirst().orElse(new CuentaBancaria());
		    
		    WebServiceUtils.merge(cuentaBancaria, nuevosCampos);
		    mergeCambios.add(cuentaBancaria);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, cuentaBancaria.Tipo, cuentaBancaria.TipoEnrolamiento, "CUENTA_BANCARIA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, CuentaBancaria.class, formulario, out);
	
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    out = validarIdBancoEntrada(conn, nuevosCampos, out, entrada.p_IdEmpresa);

			if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    		    
		    List<CuentaBancaria> lista =  new ArrayList<>();
		    lista.add(cuentaBancaria);
		    
		    ModificarPersonaCuentaBancariaMasivo modificarPersonaCuentaBancariaMasivo = new ModificarPersonaCuentaBancariaMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(lista);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaCuentaBancariaMasivo.logica(conn, entradaEjecutarCambiosPersona);
		    
		    if(!salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
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
		logger.info("FIN::ModificarCuentaBancaria", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaAdministrarWS validarIdBancoEntrada(Connection conn, CuentaBancaria nuevosCampos, SalidaAdministrarWS out, Long p_IdEmpresa) throws Exception {
		logger.trace("INICIO::ModificarCuentaBancaria.validarIdBancoEntrada");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
	    params.put("IdBanco", nuevosCampos.IdBanco);
	    SQL sql = new SQL();
		List<String> validCuentaBancaria = sql.query(conn, SQL_SELECT_NOMBRE_BANCO, params, String.class);
		
		if(validCuentaBancaria.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR++;
			String msge = params.get("IdBanco").toString(); 
			HashMap<String, String> valorIdBanco = new HashMap<String, String>(2);
			valorIdBanco.put("IdBanco", msge);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "BANCO_NO_EXISTE", valorIdBanco, conn, p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarCuentaBancaria.validarIdBancoEntrada", fin - inicio);
		return out;
	}
	
	private List<RegistroCambioMasivo> crearRegistroCambio(List<CuentaBancaria> cuentasBancarias) {
		logger.trace("INICIO::ModificarCuentaBancaria.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		for(CuentaBancaria cuenta : cuentasBancarias) {
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "EsPac", cuenta.EsPac));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdBanco", cuenta.IdBanco));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdPlaza", cuenta.IdPlaza));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdMoneda", cuenta.IdMoneda));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "CodigoAba", cuenta.CodigoAba));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "CodigoIban", cuenta.CodigoIban));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdentificadorCuenta", cuenta.IdentificadorCuenta));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdPersonaRelacionada", cuenta.IdPersonaRelacionada));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdTipoCuentaBancaria", cuenta.IdTipoCuentaBancaria));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "NumeroCuentaBancaria", cuenta.NumeroCuentaBancaria));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "CodigoCuentaInterbancario", cuenta.CodigoCuentaInterbancario));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdTipoIdentificadorCuenta", cuenta.IdTipoIdentificadorCuenta));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "OtroCodigoBancoExtranjero", cuenta.OtroCodigoBancoExtranjero));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "OtroCodigoCuentaExtranjero", cuenta.OtroCodigoCuentaExtranjero));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(cuenta.IdCuentaBancaria, "IdEstado", cuenta.IdEstado));
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarCuentaBancaria.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<CuentaBancaria> obtenerDatosCuentasBancarias(Connection conn, EntradaModificarCuentaBancaria entrada) throws SQLException {
		logger.debug("INICIO::ModificarCuentaBancaria.obtenerDatosCuentasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsCuentas = entrada.p_CuentasBancarias.stream()
				  .map(cuenta -> cuenta.IdCuentaBancaria)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdCuentaBancaria", idsCuentas);
		
		SQL sql = new SQL();
		List<CuentaBancaria> datosCuentasBancarias = sql.query(conn, SQL_SELECT_CUENTA_BANCARIA, params, CuentaBancaria.class);
		
		System.out.println(datosCuentasBancarias);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarCuentaBancaria.obtenerDatosCuentasBancarias", fin - inicio);
		return datosCuentasBancarias;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_CuentasBancarias\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdCuentaBancaria\": 701548,\r\n"
					+ "      \"NumeroCuentaBancaria\": 4444333322221111\"\",\r\n"
					+ "      \"IdBanco\": 0,\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    }\r\n"
					+ "  ,\r\n"
					+ "    {\r\n"
					+ "      \"IdCuentaBancaria\": 701547,\r\n"
					+ "      \"NumeroCuentaBancaria\": 6666777788889999\"\",\r\n"
					+ "      \"IdBanco\": 5,\r\n"
					+ "      \"IdEstado\": \"ACT\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarCuentaBancaria entradaModificarCuentaBancaria = gson.fromJson(jsonEntrada, EntradaModificarCuentaBancaria.class);
			ModificarCuentaBancaria modificarCuentaBancaria = new ModificarCuentaBancaria();				
			try {
				SalidaWBS salida= modificarCuentaBancaria.logica(con, entradaModificarCuentaBancaria);
				//con.commit();
				con.rollback();
				con.close();
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