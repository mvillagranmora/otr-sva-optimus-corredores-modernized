package cl.bcs.socionegocio.wbs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
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
import cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.RegistroCambio;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DeclaracionOrigenFondo;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDeclaracionOrigenFondos implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDeclaracionOrigenFondos.class);
	
	private String SQL_SELECT_DECLARACION_ORIGEN_FONDO = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDeclaracionOrigenFondos_SelectDeclaracionOrigenFondos.sql");
	
	public static class EntradaModificarDeclaracionOrigenFondos extends EntradaAdministrarWS {
		public List<DeclaracionOrigenFondo> p_DeclaracionOrigenFondos;
		
		public EntradaModificarDeclaracionOrigenFondos(){
			this.p_DeclaracionOrigenFondos = new ArrayList<DeclaracionOrigenFondo>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaModificarDeclaracionOrigenFondos.class);
		SalidaWBS out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection con, EntradaModificarDeclaracionOrigenFondos entrada) throws Exception {
		logger.info("INICIO::ModificarDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<DeclaracionOrigenFondo> declaracionesOrigenFondos = obtenerDeclaracionesOrigenFondos(con, entrada);
		
		for(DeclaracionOrigenFondo declaracionOrigenFondo : declaracionesOrigenFondos) {
			SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = declaracionOrigenFondo.IdDeclaracionOrigenFondos;
		    entidadModificada.TipoEntidad = "DECLARACION ORIGEN FONDOS";
		    declaracionOrigenFondo.EsBorrador = "N";
		    
		    if(declaracionOrigenFondo.TipoEnrolamiento == null) {
		    	entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DeclaracionOrigenFondo> mergeCambios = new ArrayList<>();
		    DeclaracionOrigenFondo nuevosCampos = entrada.p_DeclaracionOrigenFondos.parallelStream()
		    		.filter(dof -> dof.IdDeclaracionOrigenFondos.equals(declaracionOrigenFondo.IdDeclaracionOrigenFondos))
		    		.findFirst().orElse(new DeclaracionOrigenFondo());
		    WebServiceUtils.merge(declaracionOrigenFondo, nuevosCampos);
		    mergeCambios.add(declaracionOrigenFondo);

		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(con, declaracionOrigenFondo.Tipo, declaracionOrigenFondo.TipoEnrolamiento, "DECLARACION_ORIGEN_FONDOS");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;

		    out = entrada.validarCamposObligatoriosEntrada(con, mergeCambios, DeclaracionOrigenFondo.class, formulario, out);
			  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    List<DeclaracionOrigenFondo> lista =  new ArrayList<>();
		    lista.add(declaracionOrigenFondo);
		    
		    cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos modificarDeclaracionOrigenFondos = new cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos();
		    cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.EntradaModificarDeclaracionOrigenFondos entradaModificarDeclaracionOrigenFondos = new cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.EntradaModificarDeclaracionOrigenFondos();
		    entradaModificarDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaModificarDeclaracionOrigenFondos.p_IdModulo = entrada.p_IdModulo;
		    entradaModificarDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
		    entradaModificarDeclaracionOrigenFondos.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaModificarDeclaracionOrigenFondos.p_IdDeclaracionOrigenFondos = declaracionOrigenFondo.IdDeclaracionOrigenFondos;
		    entradaModificarDeclaracionOrigenFondos.p_RegistroCambio = crearRegistroCambio(lista);
		    
		    cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.SalidaModificarDeclaracionOrigenFondos salidaModificarDeclaracionOrigenFondos = modificarDeclaracionOrigenFondos.logica(con, entradaModificarDeclaracionOrigenFondos);
		    
		    if(!salidaModificarDeclaracionOrigenFondos.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaModificarDeclaracionOrigenFondos.P_OUT_MENSAJES);
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
		logger.info("FIN::ModificarDeclaracionOrigenFondos", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroCambio> crearRegistroCambio(List<DeclaracionOrigenFondo> declaracionOrigenFondos) {
		logger.trace("INICIO::ModificarDeclaracionOrigenFondos.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		List<cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos> modificaDeclaracion = declaracionOrigenFondos.parallelStream().map(dof ->{
		    cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos declaracionModificada =  new cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos();
		    declaracionModificada.IdDeclaracionOrigenFondos = dof.IdDeclaracionOrigenFondos;
		    declaracionModificada.IdCliente = dof.IdCliente;
		    declaracionModificada.IdTipoOperacionOrigenFondos = dof.IdTipoOperacionOrigenFondos;
		    declaracionModificada.FechaDeclaracion = new java.sql.Timestamp(dof.FechaDeclaracion.getTime());
		    declaracionModificada.Comentario = dof.Comentario;
		    declaracionModificada.IdEstado = dof.IdEstado;
		    declaracionModificada.OperacionInscripcion = dof.OperacionInscripcion;
		    declaracionModificada.DetalleOrigenFondo = dof.DetalleOrigenFondo.stream().map(ddof -> {
		    	cl.bcs.socionegocio.cli.pojo.DetalleOrigenFondo detalle = new cl.bcs.socionegocio.cli.pojo.DetalleOrigenFondo();
		    	detalle.IdDeclaracionOrigenFondos = dof.IdDeclaracionOrigenFondos;
		    	detalle.IdTipoOrigenFondo = ddof.IdTipoOrigenFondo;
		    	detalle.Valor = BigDecimal.valueOf(ddof.Valor);
		    	detalle.Otros = ddof.Otros;
		    	return detalle;
		    }).collect(Collectors.toList());
		    return declaracionModificada;
		}).collect(Collectors.toList());
		
		List<RegistroCambio> registrosCambio = new ArrayList<>();
		for(cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos declaracion : modificaDeclaracion) {
			registrosCambio.add(crearRegistroCambio("IdTipoOperacionOrigenFondos", declaracion.IdTipoOperacionOrigenFondos));
			registrosCambio.add(crearRegistroCambio("FechaDeclaracion", declaracion.FechaDeclaracion));
			registrosCambio.add(crearRegistroCambio("IdEstado", declaracion.IdEstado));
			registrosCambio.add(crearRegistroCambio("Comentario", declaracion.Comentario));
			for(cl.bcs.socionegocio.cli.pojo.DetalleOrigenFondo detalle : declaracion.DetalleOrigenFondo) {
				registrosCambio.add(crearRegistroCambio(detalle.IdTipoOrigenFondo, detalle.Valor));
				registrosCambio.add(crearRegistroCambio("Otros", detalle.Otros));
			}
		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeclaracionOrigenFondos.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}
	
    public static RegistroCambio crearRegistroCambio(String codigoCampo, BigDecimal valor) {
		logger.trace("INICIO::ModificarDeclaracionOrigenFondos.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeclaracionOrigenFondos.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }
	
    public static RegistroCambio crearRegistroCambio(String codigoCampo, Long valor) {
		logger.trace("INICIO::ModificarDeclaracionOrigenFondos.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : String.valueOf(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeclaracionOrigenFondos.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }
    
    public static RegistroCambio crearRegistroCambio(String codigoCampo, String valor) {
		logger.trace("INICIO::ModificarDeclaracionOrigenFondos.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeclaracionOrigenFondos.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }
    
    public static RegistroCambio crearRegistroCambio(String codigoCampo, Timestamp valor) {
		logger.trace("INICIO::ModificarDeclaracionOrigenFondos.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		RegistroCambio registroCambio = new RegistroCambio();
		registroCambio.CampoModificado = codigoCampo;
		registroCambio.ValorNuevo = valor == null ? null : Fecha.toStringDatetime(valor);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDeclaracionOrigenFondos.crearRegistroCambio", fin - inicio);
		return registroCambio;
    }

	private List<DeclaracionOrigenFondo> obtenerDeclaracionesOrigenFondos(Connection con, EntradaModificarDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDeclaracionOrigenFondos = entrada.p_DeclaracionOrigenFondos.stream()
				  .map(dof -> dof.IdDeclaracionOrigenFondos)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDeclaracionOrigenFondos", idsDeclaracionOrigenFondos);
		
		SQL sql = new SQL();
		List<DeclaracionOrigenFondo> declaracionesOrigenFondos = sql.query(con, SQL_SELECT_DECLARACION_ORIGEN_FONDO, params, DeclaracionOrigenFondo.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos", fin - inicio);
		return declaracionesOrigenFondos;
	}


}
