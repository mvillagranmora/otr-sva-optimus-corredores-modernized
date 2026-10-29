package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.EnrolamientoUtils;
import cl.bcs.socionegocio.enr.pojo.Ciudad;
import cl.bcs.socionegocio.wbs.pojo.DivisionPais;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DireccionFisica;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDireccionFisica implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearDireccionFisica.class);
	
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");

	public static class EntradaCrearDireccionFisica extends EntradaAdministrarWS {
		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaCrearDireccionFisica() {
			super();
			this.p_DireccionesFisicas = new ArrayList<>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearDireccionFisica jin = gson.fromJson(in.toString(), EntradaCrearDireccionFisica.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearDireccionFisica entrada) throws Exception {
		logger.info("INICIO::CrearDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_DireccionesFisicas = asignarTipoPersona(conn,entrada);
		prepararDireccionesDivisionesPais(conn, entrada);
		prepararDireccionesCiudades(conn,entrada);
		
		for (DireccionFisica direccion : entrada.p_DireccionesFisicas) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = direccion.IdDireccionFisica;
		    entidadCreada.TipoEntidad = "DIRECCION FISICA";
		    direccion.EsBorrador = "N";
		    
		    if(direccion.TipoEnrolamiento == null ) {
				entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = direccion.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    List<DireccionFisica> listaDeUno = new ArrayList<>();
		    listaDeUno.add(direccion);
		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, listaDeUno, DireccionFisica.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				continue;
		    }
		        
		    
		    CrearDireccionFisicaMasivo crearDireccionFisicaMasivo = new CrearDireccionFisicaMasivo();
		    EntradaCrearDireccionFisicaMasivo entradaCrearDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		    entradaCrearDireccionFisicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearDireccionFisicaMasivo.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearDireccionFisicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearDireccionFisicaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearDireccionFisicaMasivo.p_DireccionesFisica = obtenerDirecciones(listaDeUno);
		    
		    SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = new SalidaCrearDireccionFisicaMasivo();
		    salidaCrearDireccionFisicaMasivo = crearDireccionFisicaMasivo.logica(conn, entradaCrearDireccionFisicaMasivo);
		    if(!salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadCreada.Mensajes.addAll(salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES);
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
		
		if(salida.P_OUT_ERRORESCONTADOR > 0 ) {
		    conn.rollback();
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDireccionFisica", finServicio - inicioServicio);
		return salida;
	}



	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::CrearDireccionFisica.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_DireccionesFisicas.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisica.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}

	private List<cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.DireccionFisica> obtenerDirecciones(List<DireccionFisica> direcciones) {
	    List<cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.DireccionFisica> direccionesCreacion = direcciones.parallelStream().map(direccion -> {
		cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.DireccionFisica direccionCreacion = new cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.DireccionFisica();
		direccionCreacion.IdInterno = direccion.IdPersona;
		direccionCreacion.IdDireccionFisica = direccion.IdDireccionFisica;
		direccionCreacion.Calle = direccion.Calle;
		direccionCreacion.Numero = direccion.Numero;
		direccionCreacion.Departamento = direccion.Departamento;
		direccionCreacion.Sector = direccion.Sector;
		direccionCreacion.CodigoPostal = direccion.CodigoPostal;
		direccionCreacion.IdCiudad = direccion.IdCiudad;
		direccionCreacion.IdDivisionPais = direccion.IdComuna;
		direccionCreacion.CodigoProposito = direccion.CodigoProposito;
		direccionCreacion.Comentario = direccion.Comentario;
		direccionCreacion.IdEstado = direccion.IdEstado;
		direccionCreacion.IdPersona = direccion.IdPersona;
		direccionCreacion.CodigoCompuesto = direccion.CodigoCompuesto;
		direccionCreacion.Predeterminado = direccion.Predeterminado;
		return direccionCreacion;
	    }).collect(Collectors.toList());

	    return direccionesCreacion;
	}

	private void prepararDireccionesCiudades(Connection con, EntradaCrearDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::ModificarDireccionFisica.prepararDireccionesCiudades");
		long inicio = System.currentTimeMillis();

		List<Long> ciudadIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdCiudad != null).map(u -> u.IdCiudad).collect(Collectors.toList());
		Map<Long, Ciudad> ciudadPorIdCiudad = EnrolamientoUtils.obtenerCiudadPorIdCiudad(con, ciudadIds);
		
		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {

			Ciudad ciudad = ciudadPorIdCiudad.get(direccionFisica.IdCiudad);
			if (ciudad != null) {
				direccionFisica.IdPais = ciudad.IdPais;
				direccionFisica.UsaDivisionPais = ciudad.UsaDivisionPais;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionFisica.prepararDireccionesCiudades {}", fin - inicio);
	}
	
	private void prepararDireccionesDivisionesPais(Connection con, EntradaCrearDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::ModificarDireccionFisica.prepararDireccionesDivisionesPais");
		long inicio = System.currentTimeMillis();

		List<Long> divisionPaisIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdComuna != null).map(u -> u.IdComuna).collect(Collectors.toList());
		Map<Long, DivisionPais> divisionPaisPorIdDivisionPais = WebServiceUtils.obtenerDivisionPaisPorIdDivisionPais(con, divisionPaisIds);
		
		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {

			DivisionPais divisionPais = divisionPaisPorIdDivisionPais.get(direccionFisica.IdComuna);
			if (divisionPais != null) {
				direccionFisica.CodigoDivisionPais = divisionPais.CodigoDivisionPais;
				direccionFisica.CodigoCompuesto = divisionPais.CodigoDivisionPais;
				direccionFisica.IdCiudad = divisionPais.IdCiudad;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDireccionFisica.prepararDireccionesDivisionesPais {}", fin - inicio);
	}
	
	private List<DireccionFisica> asignarTipoPersona(Connection con, EntradaCrearDireccionFisica entrada) throws SQLException{
		logger.debug("INICIO::CrearDireccionFisica.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_DireccionesFisicas.parallelStream().forEach(tarjeta -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(tarjeta.IdPersona)).findFirst().orElse(new TipoPersona());
			tarjeta.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "DIRECCION_FISICA");
			tarjeta.TipoEnrolamiento = tipo.TipoEnrolamiento;
			tarjeta.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisica.asignarTipoPersona", fin - inicio);
		return entrada.p_DireccionesFisicas;
	}

	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_DireccionesFisicas\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdDireccionFisica\": 0,\r\n"
				+ "      \"Calle\": \"\",\r\n"
				+ "      \"CodigoPostal\": \"\",\r\n"
				+ "      \"CodigoProposito\": \"\",\r\n"
				+ "      \"CodigoDivisionPais\": \"\",\r\n"
				+ "      \"Comentario\": \"\",\r\n"
				+ "      \"Departamento\": \"\",\r\n"
				+ "      \"IdCiudad\": 150,\r\n"
				+ "      \"Numero\": \"\",\r\n"
				+ "      \"Sector\": \"\",\r\n"
				+ "      \"IdDivisionPais\": 0,\r\n"
				+ "      \"Predeterminado\": \"\",\r\n"
				+ "      \"IdEstado\": \"\",\r\n"
				+ "      \"IdPersona\": 1732307\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 9\r\n"
				+ "}";
			
			
			EntradaCrearDireccionFisica entradaCrearDireccionFisica = gson.fromJson(jsonEntrada, EntradaCrearDireccionFisica.class);
			CrearDireccionFisica CrearDireccionFisica = new CrearDireccionFisica();		
			System.out.println("ENTRADA:");
			System.out.println(entradaCrearDireccionFisica.toString());
			try {
			    SalidaWBS salida= CrearDireccionFisica.logica(con, entradaCrearDireccionFisica);
				//con.commit();
				con.rollback();
				con.close();
				System.out.println("SALIDA:");
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
