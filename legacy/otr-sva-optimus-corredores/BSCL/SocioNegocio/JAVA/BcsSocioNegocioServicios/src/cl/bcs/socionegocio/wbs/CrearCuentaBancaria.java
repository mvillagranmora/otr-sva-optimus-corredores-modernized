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
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.InCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.CuentaBancaria;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearCuentaBancaria implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearCuentaBancaria.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");
	private String SQL_SELECT_NOMBRE_BANCO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarBanco_SelectNombreBancoPorIdBanco.sql");
	
	public static class EntradaCrearCuentaBancaria extends EntradaAdministrarWS {
		public List<CuentaBancaria> p_CuentasBancarias;
		
		public EntradaCrearCuentaBancaria() {
			super();
			this.p_CuentasBancarias = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearCuentaBancaria jin = gson.fromJson(in.toString(), EntradaCrearCuentaBancaria.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_CuentasBancarias = asignarTipoPersona(conn,entrada);
		
		for (CuentaBancaria cuenta : entrada.p_CuentasBancarias) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = cuenta.IdCuentaBancaria;
		    entidadCreada.TipoEntidad = "CUENTA BANCARIA";
		    cuenta.EsBorrador = "N";
		    
		    if(cuenta.TipoEnrolamiento == null) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = cuenta.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<CuentaBancaria> listaDeUno = new ArrayList<>();
		    listaDeUno.add(cuenta);
		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, CuentaBancaria.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    out = validarIdBancoEntrada(conn, cuenta, out, entrada.p_IdEmpresa);

			if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadCreada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    CrearPersonaCuentaBancaria crearPersonaCuentaBancaria = new CrearPersonaCuentaBancaria();
		    EntradaCrearPersonaCuentaBancaria entradaCrearPersonaCuentaBancaria= new EntradaCrearPersonaCuentaBancaria();
		    entradaCrearPersonaCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearPersonaCuentaBancaria.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearPersonaCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearPersonaCuentaBancaria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearPersonaCuentaBancaria.p_InCuentaBancaria = obtenerTarjetas(listaDeUno);
		    
		    SalidaCrearPersonaCuentaBancaria salidaCrearPersonaCuentaBancaria = new SalidaCrearPersonaCuentaBancaria();
		    salidaCrearPersonaCuentaBancaria = crearPersonaCuentaBancaria.logica(conn, entradaCrearPersonaCuentaBancaria);
		    if(!salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES.isEmpty()) {
				entidadCreada.Mensajes.addAll(salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    if(entidadCreada.Mensajes.isEmpty()) {
				entidadCreada = WebServiceUtils.agregarMensajeOk(entidadCreada, "CREADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
		    }
		}
		
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
			(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
			(sum1, sum2) -> sum1 + sum2);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearCuentaBancaria", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaAdministrarWS validarIdBancoEntrada(Connection conn, CuentaBancaria cuenta, SalidaAdministrarWS out, Long p_IdEmpresa) throws Exception {
		logger.debug("INICIO::CrearCuentaBancaria.validarIdBancoEntrada");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
	    params.put("IdBanco", cuenta.IdBanco);
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
		logger.debug("FIN::CrearCuentaBancaria.validarIdBancoEntrada", fin - inicio);
		return out;	
	}



	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearCuentaBancaria entrada) throws SQLException {
		logger.debug("INICIO::CrearCuentaBancaria.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_CuentasBancarias.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCuentaBancaria.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<InCuentaBancaria> obtenerTarjetas( List<CuentaBancaria> cuentas) {
	    List<InCuentaBancaria> cuentasCreacion =  cuentas.parallelStream().map(cuenta -> {
	    	InCuentaBancaria cuentaBancaria = new InCuentaBancaria();
			cuentaBancaria.IdInterno = cuenta.IdInterno;
			cuentaBancaria.IdBanco = cuenta.IdBanco;
			cuentaBancaria.IdTipoCuentaBancaria = cuenta.IdTipoCuentaBancaria;
			cuentaBancaria.IdMoneda = cuenta.IdMoneda;
			cuentaBancaria.NumeroCuentaBancaria = cuenta.NumeroCuentaBancaria;
			cuentaBancaria.IdPlaza = cuenta.IdPlaza;
			cuentaBancaria.IdTipoIdentificadorCuenta = cuenta.IdTipoIdentificadorCuenta;
			cuentaBancaria.IdentificadorCuenta = cuenta.IdentificadorCuenta;
			cuentaBancaria.IdPersona = cuenta.IdPersona;
			cuentaBancaria.IdEstado = cuenta.IdEstado;
			cuentaBancaria.IdPersonaRelacionada = cuenta.IdPersonaRelacionada;
			cuentaBancaria.CodigoCuentaInterbancario = cuenta.CodigoCuentaInterbancario;
			cuentaBancaria.CodigoAba = cuenta.CodigoAba;
			cuentaBancaria.OtroCodigoBancoExtranjero = cuenta.OtroCodigoBancoExtranjero;
			cuentaBancaria.CodigoIban = cuenta.CodigoIban;
			cuentaBancaria.OtroCodigoCuentaExtranjero = cuenta.OtroCodigoCuentaExtranjero;
			cuentaBancaria.EsPac = cuenta.EsPac;
			cuentaBancaria.CrearPreferenciaPago = cuenta.CrearPreferenciaPago;
			return cuentaBancaria;
		}).collect(Collectors.toList());
	    
	    return cuentasCreacion;
	}
	
	private List<CuentaBancaria> asignarTipoPersona(Connection con, EntradaCrearCuentaBancaria entrada) throws SQLException{
		logger.debug("INICIO::CrearCuentaBancaria.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_CuentasBancarias.parallelStream().forEach(cuenta -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(cuenta.IdPersona)).findFirst().orElse(new TipoPersona());
			cuenta.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "CUENTA_BANCARIA");
			cuenta.TipoEnrolamiento = tipo.TipoEnrolamiento;
			cuenta.IdCliente = tipo.IdCliente;
			cuenta.Tipo = tipo.Tipo;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearCuentaBancaria.asignarTipoPersona", fin - inicio);
		return entrada.p_CuentasBancarias;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "    \"p_CuentasBancarias\": [\r\n"
					+ "        {\r\n"//SIN NUMERO_CUENTA
					+ "            \"IdInterno\": 0,\r\n"
					+ "            \"IdBanco\": 17,\r\n"
					+ "            \"IdTipoCuentaBancaria\": \"CUCO\",\r\n" 
					+ "            \"NumeroCuentaBancaria\": \"32165478\",\r\n" 
					+ "            \"IdMoneda\": \"CLP\",\r\n"
					+ "            \"IdPersona\": 1819658,\r\n"
					+ "            \"IdEstado\": \"ACT\",\r\n"
					+ "            \"IdPlaza\": 590,\r\n"
					+ "            \"CrearPreferenciaPago\": \"N\",\r\n"
					+ "            \"EsPac\": \"S\",\r\n"
					+ "            \"IdTipoIdentificadorCuenta\": \"\",\r\n"
					+ "            \"IdentificadorCuenta\": \"0\",\r\n"
					+ "            \"IdPersonaRelacionada\": 1732307\r\n"
					+ "        },\r\n"
					+"           {\r\n"//SIN NUMERO_CUENTA
						+ "            \"IdInterno\": 0,\r\n"
						+ "            \"IdBanco\": 43,\r\n"
						+ "            \"IdTipoCuentaBancaria\": \"CUCO\",\r\n" 
						+ "            \"NumeroCuentaBancaria\": \"121312134\",\r\n" 
						+ "            \"IdMoneda\": \"CLP\",\r\n"
						+ "            \"IdPersona\": 1819658,\r\n"
						+ "            \"IdEstado\": \"ACT\",\r\n"
						+ "            \"IdPlaza\": 590,\r\n"
						+ "            \"CrearPreferenciaPago\": \"N\",\r\n"
						+ "            \"EsPac\": \"S\",\r\n"
						+ "            \"IdTipoIdentificadorCuenta\": \"\",\r\n"
						+ "            \"IdentificadorCuenta\": \"0\",\r\n"
						+ "            \"IdPersonaRelacionada\": 1732307\r\n"
						+ "       }\r\n"
					+ "    ],\r\n"
					+ "		\"p_IdEmpresa\": 1,\r\n"
					+ "		\"p_IdUsuario\": 9\r\n"
					+ "}";
			EntradaCrearCuentaBancaria entradaCrearCuentaBancaria = gson.fromJson(jsonEntrada, EntradaCrearCuentaBancaria.class);
			CrearCuentaBancaria crearCuentaBancaria = new CrearCuentaBancaria();				
			try {
			    SalidaWBS salida= crearCuentaBancaria.logica(con, entradaCrearCuentaBancaria);
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
