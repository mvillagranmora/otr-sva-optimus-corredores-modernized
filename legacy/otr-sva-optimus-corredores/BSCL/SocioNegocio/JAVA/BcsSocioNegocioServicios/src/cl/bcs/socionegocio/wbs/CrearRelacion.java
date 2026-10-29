package cl.bcs.socionegocio.wbs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.socionegocio.per.CrearPersona;

import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.EntradaCrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.SalidaCrearRelacionPersonaMasivo;

import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.Relacion;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;

import cl.bcs.socionegocio.wbs.pojo.TipoPersona;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearRelacion implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearRelacion.class);
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ConsultarPersona_SelectPersona.sql");
	private String SQL_SELECT_BUSCAR_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/CrearRelacion_SelectBuscarPersona.sql");
	
	public static class EntradaCrearRelaciones extends EntradaAdministrarWS {
		public List<Relacion> p_Relaciones;
		
		public EntradaCrearRelaciones() {
			super();
			this.p_Relaciones = new ArrayList<>();
		}
	}
	public static class IdPersona extends BcsPojo{
	    public Long IdPersona;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearRelaciones jin = gson.fromJson(in.toString(), EntradaCrearRelaciones.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaCrearRelaciones entrada) throws Exception {
		logger.info("INICIO::CrearRelacion");
		long inicioServicio = System.currentTimeMillis();
		SalidaWBS salida =  new SalidaWBS();
		
		entrada.p_Relaciones = asignarTipoPersona(conn,entrada);
		

		for (Relacion relacion : entrada.p_Relaciones) {
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadCreada = new EntidadManipulada();
		    entidadCreada.IdEntidad = relacion.IdRelacion;
		    entidadCreada.TipoEntidad = "RELACION";
		    relacion.EsBorrador = "N";
		    
		    if(relacion.TipoEnrolamiento == null || relacion.TipoEnrolamiento.isEmpty()) {
			entidadCreada = WebServiceUtils.noEsCliente(entidadCreada);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = relacion.CodigoFormulario;
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    Boolean existePersona = false;
		    if (relacion.IdPersonaRelacionada!=0 && relacion.IdPersonaRelacionada!= null ) existePersona = true; 
		    if(relacion.IdPersonaRelacionada != null && relacion.IdPersonaRelacionada == 0) {
				SalidaServicioCrearPersona salidaCrearPersona = crearPersonaRelacionada(conn,relacion,entrada);
				Optional<ErrorDetalle> personaExiste = salidaCrearPersona.P_OUT_ERRORESDETALLE.parallelStream().filter(mens -> "PERSONA_EXISTE".equals(mens.Codigo)).findAny();
				if(personaExiste.isPresent()) {
				    relacion.IdPersonaRelacionada = buscarIdPersonaExiste(conn,relacion);
				    existePersona = true;		    
				}
				else if(salidaCrearPersona.P_OUT_ADVERTENCIASCONTADOR > 0 || salidaCrearPersona.P_OUT_ERRORESCONTADOR > 0) {
				    entidadCreada = agregarMensajes(salidaCrearPersona,entidadCreada);
				    salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
				    continue;
				}
		    }
		    
		    List<Relacion> listaDeUno = new ArrayList<>();
		    listaDeUno.add(relacion);
		   
		    CrearRelacionPersonaMasivo crearRelacionMasivo = new CrearRelacionPersonaMasivo();
		    EntradaCrearRelacionPersonaMasivo entradaCrearRelacionMasivo = new EntradaCrearRelacionPersonaMasivo();
		    entradaCrearRelacionMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaCrearRelacionMasivo.p_IdModulo = entrada.p_IdModulo;
		    entradaCrearRelacionMasivo.p_IdUsuario = entrada.p_IdUsuario;
		    entradaCrearRelacionMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaCrearRelacionMasivo.p_RelacionesPersona = obtenerRelaciones(listaDeUno);
		    // SI EXISTE PERSONA SE CAMBIA EL VALOR DE LA VARIABLE PARA QUE EN CASO DE REQUERIR UN CAMPO OBLIGATORIO REALICE UNA EXCEPCIÓN
		    if (existePersona) {
		    	entradaCrearRelacionMasivo.p_RelacionesPersona.get(0).EstaInsertada = "N";		    
		    }
		    		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, entradaCrearRelacionMasivo.p_RelacionesPersona, CrearRelacionPersonaMasivo.RelacionPersona.class, formulario, out);
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
			entidadCreada.Mensajes.addAll(out.P_OUT_MENSAJES);
			salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadCreada);
			continue;
		    }
		    
		    SalidaCrearRelacionPersonaMasivo salidaCrearReacionMasivo = new SalidaCrearRelacionPersonaMasivo();
		    salidaCrearReacionMasivo = crearRelacionMasivo.logica(conn, entradaCrearRelacionMasivo);
		    if(!salidaCrearReacionMasivo.P_OUT_MENSAJES.isEmpty()) {
			entidadCreada.Mensajes.addAll(salidaCrearReacionMasivo.P_OUT_MENSAJES);
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
		logger.info("FIN::CrearRelacion", finServicio - inicioServicio);
		return salida;
	}


	private Long buscarIdPersonaExiste(Connection conn, Relacion relacion) throws SQLException {

	    HashMap<String, Object> params = new HashMap<String, Object>(2);
	    params.put("DocumentoIdentidad", relacion.DocumentoIdentidad);

	    SQL sql = new SQL();
	    List<IdPersona> idPersonaRelacionada = sql.query(conn, SQL_SELECT_BUSCAR_PERSONA, params, IdPersona.class);
	    
	    return idPersonaRelacionada.isEmpty()? 0L: idPersonaRelacionada.get(0).IdPersona;
	}

	private EntidadManipulada agregarMensajes(SalidaServicioCrearPersona salidaCrearPersona, EntidadManipulada entidadCreada) {
	    AtomicLong id = new AtomicLong();
	    List<MensajeSalida> advertencias = salidaCrearPersona.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(adv->{
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.codigo= adv.Codigo;
		mensaje.descripcion = adv.Descripcion;
		mensaje.tipo =PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA;
		mensaje.id = id.getAndIncrement();
		return mensaje;
	    }).collect(Collectors.toList());
	    List<MensajeSalida> errores = salidaCrearPersona.P_OUT_ERRORESDETALLE.parallelStream().map(err->{
		MensajeSalida mensaje = new MensajeSalida();
		mensaje.codigo= err.Codigo;
		mensaje.descripcion = err.Descripcion;
		mensaje.tipo =PlataformaUtils.MensajeSalida.MENSAJE_ERROR;
		mensaje.id = id.getAndIncrement();
		return mensaje;
	    }).collect(Collectors.toList());
	    entidadCreada.Mensajes.addAll(advertencias);
	    entidadCreada.Mensajes.addAll(errores);
	    return entidadCreada;
	}

	private List<TipoPersona> obtenerTiposPersona(Connection con, EntradaCrearRelaciones entrada) throws SQLException {
		logger.debug("INICIO::CrearRelacion.obtenerTiposPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_Relaciones.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<TipoPersona> tiposPersona = sql.query(con, SQL_SELECT_PERSONA, params, TipoPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacion.obtenerTiposPersona", fin - inicio);
		return tiposPersona;
	}
	
	private List<cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.RelacionPersona> obtenerRelaciones(List<Relacion> relaciones) {
	    List<cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.RelacionPersona> relacionesCreacion = relaciones.parallelStream().map(relacion -> {
		cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.RelacionPersona relacionCreacion = new cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.RelacionPersona();
		relacionCreacion.IdPersonaDestino = relacion.IdPersonaRelacionada;
		relacionCreacion.IdPersonaOrigen = relacion.IdPersona;
		relacionCreacion.IdTipoRelacion = relacion.IdTipoRelacion;
		relacionCreacion.IdRelacionPersona = 0L;
		relacionCreacion.FechaInicioRelacion = "";
		relacionCreacion.FechaFinRelacion = "";
		relacionCreacion.PorcentajeParticipacion = new BigDecimal(0);
		relacionCreacion.RetiraDocumentos = "";
		relacionCreacion.FinalidadRelacion = "";
		relacionCreacion.CondicionesRelacion = "";
		relacionCreacion.IdGradoVinculacionParental = 0L;
		relacionCreacion.Ciudad = "";
		relacionCreacion.Calle = "";
		relacionCreacion.Numero = "";
		relacionCreacion.Telefono = "";
		relacionCreacion.Email = "";
		relacionCreacion.Complemento = "";
		relacionCreacion.IdEstado= relacion.IdEstado;
		relacionCreacion.NombresPersonaRelacion = relacion.Nombre;
		relacionCreacion.ApellidoMaternoPersonaRelacion = relacion.ApellidoMaterno;
		relacionCreacion.ApellidoPaternoPersonaRelacion = relacion.ApellidoPaterno;
		relacionCreacion.DocumentoIdentidadRelacion = relacion.DocumentoIdentidad;
		relacionCreacion.TipoDocumentoIdentidadRelacion = relacion.TipoDocumentoIdentidad;
		
		return relacionCreacion;
	    }).collect(Collectors.toList());

	    return relacionesCreacion;
	}
	
	private List<Relacion> asignarTipoPersona(Connection con, EntradaCrearRelaciones entrada) throws SQLException{
		logger.debug("INICIO::CrearRelacion.asignarTipoPersona");
		long inicio = System.currentTimeMillis();
		
		List<TipoPersona> tipoPersonas = obtenerTiposPersona(con, entrada);
		
		entrada.p_Relaciones.parallelStream().forEach(relacion -> {
			TipoPersona tipo = tipoPersonas.parallelStream().filter(tipoPersona -> tipoPersona.IdPersona.equals(relacion.IdPersona)).findFirst().orElse(new TipoPersona());
			relacion.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, tipo.Tipo, tipo.TipoEnrolamiento, "RELACIONES");
			relacion.TipoEnrolamiento = tipo.TipoEnrolamiento;
			relacion.IdCliente = tipo.IdCliente;
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearRelacion.asignarTipoPersona", fin - inicio);
		return entrada.p_Relaciones;
	}

	private SalidaServicioCrearPersona crearPersonaRelacionada(Connection conn, Relacion relacion, EntradaAdministrarWS entrada) throws Exception {

	    SalidaServicioCrearPersona salidaServicioCrearPersona = new SalidaServicioCrearPersona();
	    CrearPersona crearPersona = new CrearPersona();

	    List<cl.bcs.socionegocio.pojo.Enrolamiento> enrolamiento = new ArrayList<>();
	    cl.bcs.socionegocio.pojo.Enrolamiento personaCrear = new cl.bcs.socionegocio.pojo.Enrolamiento();
	    personaCrear.IdTipoDocumentoIdentidad = relacion.TipoDocumentoIdentidad;
	    personaCrear.DocumentoIdentidad = relacion.DocumentoIdentidad;
	    personaCrear.Nombres = relacion.Nombre;
	    personaCrear.ApellidoPaterno = relacion.ApellidoPaterno;
	    personaCrear.ApellidoMaterno = relacion.ApellidoMaterno;
	    personaCrear.IdTipoEntidad = "PERSONA";
	    personaCrear.IdPaisNacimiento = 0L;
	    personaCrear.Tipo = "N";
	    personaCrear.IdEmpresa = entrada.p_IdEmpresa;
	    personaCrear.IdTipoDocumentoIdentidadAsociado = "";
	    enrolamiento.add(personaCrear);

	    EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona();
	    entradaServicioCrearPersona.p_IdEmpresa = entrada.p_IdEmpresa;
	    entradaServicioCrearPersona.p_IdUsuario = entrada.p_IdUsuario;
	    entradaServicioCrearPersona.p_IdModulo = entrada.p_IdModulo;
	    entradaServicioCrearPersona.p_ConsultarEnrolamiento = enrolamiento;

	    salidaServicioCrearPersona = crearPersona.logica(conn, entradaServicioCrearPersona);
	    if (salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR == 0) {
		relacion.IdPersonaRelacionada = salidaServicioCrearPersona.P_OUT_REFID;
	    }

	    return salidaServicioCrearPersona;
	}
	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
				+ "  \"p_Relaciones\": [\r\n"
				+ "    {\r\n"
				+ "      \"IdPersonaRelacionada\": 0,\r\n"
				+ "      \"IdTipoRelacion\": \"BENEMP\",\r\n"
				+ "      \"TipoDocumentoIdentidad\": \"R\",\r\n"
				+ "      \"DocumentoIdentidad\": \"23564923-3\",\r\n"
				+ "      \"Nombre\": \"PROBANDO CREACION\",\r\n"
				+ "      \"ApellidoPaterno\": \"PERSONA\",\r\n"
				+ "      \"ApellidoMaterno\": \"RELACIONES\",\r\n"
				+ "      \"IdPersona\": \"1750824\"\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"IdPersonaRelacionada\": 1790797,\r\n"
				+ "      \"IdTipoRelacion\": \"PARSOC\",\r\n"
				+ "      \"IdPersona\": \"1750824\"\r\n"
				+ "    }\r\n"
				+ "  ],\r\n"
				+ "  \"p_IdEmpresa\": 1,\r\n"
				+ "  \"p_IdUsuario\": 0\r\n"
				+ "}";
			
			String jsonReq = "{\r\n"
					+ "  \"p_Relaciones\": [\r\n"
					+ "    {\r\n"
//					+ "      \"IdPersonaRelacionada\": 0,\r\n"
//					+ "      \"IdTipoRelacion\": \"BENEMP\",\r\n"
//					+ "      \"TipoDocumentoIdentidad\": \"R\",\r\n"
//					+ "      \"DocumentoIdentidad\": \"19676210-8\",\r\n"
//					+ "      \"Nombre\": \"PROBANDO CREACION\",\r\n"
//					+ "      \"ApellidoPaterno\": \"PERSONA\",\r\n"
//					+ "      \"ApellidoMaterno\": \"RELACIONES\",\r\n"
					+ "      \"IdPersona\": \"1732307\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9\r\n"
					+ "}";
			
			EntradaCrearRelaciones entradaCrearTarjetaBancaria = gson.fromJson(jsonReq, EntradaCrearRelaciones.class);
			CrearRelacion crearTarjetaBancaria = new CrearRelacion();		
			System.out.println("ENTRADA:");
			System.out.println(entradaCrearTarjetaBancaria.toString());
			try {
			    SalidaWBS salida= crearTarjetaBancaria.logica(con, entradaCrearTarjetaBancaria);
//				con.commit();
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
