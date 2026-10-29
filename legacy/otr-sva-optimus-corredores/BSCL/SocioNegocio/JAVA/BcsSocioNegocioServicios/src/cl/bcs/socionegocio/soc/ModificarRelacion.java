package cl.bcs.socionegocio.soc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarRelacionPersonaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.EntradaCrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.rel.CrearRelacionPersonaMasivo.SalidaCrearRelacionPersonaMasivo;
import cl.bcs.socionegocio.soc.pojo.Relacionados;

public class ModificarRelacion implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarRelacion.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<Relacionados> entrada) throws Exception{
		
		logger.info("INICIO::ModificarRelacion");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearRelacionPersonaMasivo salidaCrearRelacion = new SalidaCrearRelacionPersonaMasivo();
		
		Map<String, List<Relacionados>> relacionesAgrupadasPorAccion = relacionesAgrupadasPorAccion(entrada.p_datos);
		
		salidaCrearRelacion = crearRelacion(conn, entrada, relacionesAgrupadasPorAccion.get("AGREGAR"));
		if(salidaCrearRelacion.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaCrearRelacion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES = salidaCrearRelacion.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarRelacion = new SalidaEjecutarCambiosPersona();
		salidaModificarRelacion = modificarRelacion(conn, entrada, relacionesAgrupadasPorAccion.get("MODIFICAR"));
		if(!salidaModificarRelacion.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarRelacion.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarRelacion.P_OUT_MENSAJES;
			return out;
		}
	
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacion", finServicio - inicioServicio);
		
		return out;	
	}
	
	private SalidaCrearRelacionPersonaMasivo crearRelacion(Connection conn, EntradaModificarSocioNegocio<Relacionados> in, List<Relacionados> relaciones) throws Exception {
		
		logger.info("INICIO::ModificarRelacion.crearRelacion");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearRelacionPersonaMasivo salida = new SalidaCrearRelacionPersonaMasivo();
		if(relaciones == null) return salida;
		
		List<CrearRelacionPersonaMasivo.RelacionPersona> relacionesPorCrear = new ArrayList<CrearRelacionPersonaMasivo.RelacionPersona>();
		
		relaciones.stream()
		.forEach(r -> {
			CrearRelacionPersonaMasivo.RelacionPersona relacion = new CrearRelacionPersonaMasivo.RelacionPersona();
			relacion.IdInterno 					= 0L;
			relacion.IdRelacionPersona			= 0L;
			relacion.IdTipoRelacion				= r.IdTipoRelacion;
			relacion.IdPersonaOrigen			= in.p_IdPersona;
			relacion.IdPersonaDestino			= r.IdEntidadRelacionada;
			relacion.FinalidadRelacion 			= "";
			relacion.CondicionesRelacion 		= "";
			relacion.IdGradoVinculacionParental	= 0L;
			relacion.FechaInicioRelacion 		= "0001-01-01 00:00:00";
			relacion.FechaFinRelacion 			= "0001-01-01 00:00:00";
			relacion.PorcentajeParticipacion 	= BigDecimal.ZERO;
			relacion.RetiraDocumentos 			= "N";
			relacionesPorCrear.add(relacion);	    
		});		
		
		if(!relacionesPorCrear.isEmpty()) {
			CrearRelacionPersonaMasivo crearRelacion = new CrearRelacionPersonaMasivo();
			EntradaCrearRelacionPersonaMasivo entradaRelacion = new EntradaCrearRelacionPersonaMasivo();
			entradaRelacion.p_IdEmpresa 			= in.p_IdEmpresa;
			entradaRelacion.p_IdModulo 				= "SOC";
			entradaRelacion.p_UsaReglasNegocio 		= "N";
			entradaRelacion.p_IdUsuario 			= in.p_IdUsuario;
			entradaRelacion.p_RelacionesPersona		= relacionesPorCrear;
			salida = crearRelacion.logica(conn, entradaRelacion);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacion.crearRelacion", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarRelacion(Connection conn, EntradaModificarSocioNegocio<Relacionados> in, List<Relacionados> relaciones) throws Exception {
		
		logger.info("INICIO::ModificarRelacion.modificarRelacion");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(relaciones == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosRelaciones(relaciones);
		
		if(!registroCambio.isEmpty()) {
			ModificarRelacionPersonaMasivo modificarRelacion = new ModificarRelacionPersonaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarRelacion.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacion.modificarRelacion", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosRelaciones(List<Relacionados> relaciones) {
		
		logger.info("INICIO::ModificarRelacion.obtenerCambiosRelaciones");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(Relacionados relacion: relaciones) {
			
			if(relacion.IdEntidadRelacionada > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdPersonaDestino";
				registroCambio.ValorNuevo		= String.valueOf(relacion.IdEntidadRelacionada);
				registroCambio.IdEntidad		= relacion.IdRelacionado;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!relacion.IdTipoRelacion.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdTipoRelacion";
				registroCambio.ValorNuevo		= relacion.IdTipoRelacion;
				registroCambio.IdEntidad		= relacion.IdRelacionado;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacion.obtenerCambiosRelaciones", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}

	private Map<String, List<Relacionados>> relacionesAgrupadasPorAccion(List<Relacionados> relacionados) {
		return relacionados.stream().collect(Collectors.groupingBy(rel -> rel.Accion));
	}

}
