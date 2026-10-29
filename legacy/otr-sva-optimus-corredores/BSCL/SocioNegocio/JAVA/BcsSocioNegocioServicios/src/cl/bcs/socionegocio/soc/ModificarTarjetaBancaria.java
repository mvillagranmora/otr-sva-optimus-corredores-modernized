package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.EntradaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.SalidaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.TarjetasBancaria;

public class ModificarTarjetaBancaria implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarTarjetaBancaria.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<TarjetasBancaria> entrada) throws Exception{
		
		logger.info("INICIO::ModificarTarjetaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearPersonaTarjetaBancariaMasivo salidaTarjetaBancaria = new SalidaCrearPersonaTarjetaBancariaMasivo();
		
		Map<String, List<TarjetasBancaria>> tarjetasAgrupadasPorAccion = tarjetasAgrupadasPorAccion(entrada.p_datos);
		
		salidaTarjetaBancaria = crearTarjetaBancaria(conn, entrada, tarjetasAgrupadasPorAccion.get("AGREGAR"));
		if(!salidaTarjetaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaTarjetaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaTarjetaBancaria.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarTarjetaBancaria = new SalidaEjecutarCambiosPersona();
		salidaModificarTarjetaBancaria = modificarTarjetaBancaria(conn, entrada, tarjetasAgrupadasPorAccion.get("MODIFICAR"));
		if(!salidaModificarTarjetaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarTarjetaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarTarjetaBancaria.P_OUT_MENSAJES;
		}
		
		salidaModificarTarjetaBancaria = eliminarTarjetaBancaria(conn, entrada, tarjetasAgrupadasPorAccion.get("ELIMINAR"));
		if(!salidaModificarTarjetaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarTarjetaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarTarjetaBancaria.P_OUT_MENSAJES;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria", finServicio - inicioServicio);
		
		return out;	
	}
	
	private SalidaCrearPersonaTarjetaBancariaMasivo crearTarjetaBancaria(Connection conn, EntradaModificarSocioNegocio<TarjetasBancaria> in, List<TarjetasBancaria> tarjetasBancarias) throws Exception {
		
		logger.info("INICIO::ModificarTarjetaBancaria.crearTarjetaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearPersonaTarjetaBancariaMasivo salida = new SalidaCrearPersonaTarjetaBancariaMasivo();
		if(tarjetasBancarias == null) return salida; 
		
		List<CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria> tarjetasBancariasPorCrear = new ArrayList<CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria>();
		
		tarjetasBancarias.stream().filter(t -> t.Accion.equals("AGREGAR"))
		.forEach(t -> {
			CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria tarjeta = new CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria();
			tarjeta.IdInterno 				= 0L;
			tarjeta.IdTarjetaBancaria 		= 0L;
			tarjeta.IdBanco					= t.IdBanco;
			tarjeta.IdTipoTarjetaBancaria	= t.IdTipo;
			tarjeta.NumeroTarjetaBancaria	= t.NumeroTarjeta;
			tarjeta.CodigoVerificacion		= t.CodigoVerificacion;
			tarjeta.FechaVencimiento		= t.FechaVencimiento;
			tarjeta.IdPersona 				= in.p_IdPersona;
			tarjeta.IdUsuarioCreador 		= in.p_IdUsuario;
			tarjetasBancariasPorCrear.add(tarjeta);
	
		});		
		
		if(!tarjetasBancariasPorCrear.isEmpty()) {
			CrearPersonaTarjetaBancariaMasivo crearTarjeta = new CrearPersonaTarjetaBancariaMasivo();
			EntradaCrearPersonaTarjetaBancariaMasivo entradaTarjeta = new EntradaCrearPersonaTarjetaBancariaMasivo();
			entradaTarjeta.p_IdEmpresa 				= in.p_IdEmpresa;
			entradaTarjeta.p_IdModulo 				= "SOC";
			entradaTarjeta.p_UsaReglasNegocio 		= "N";
			entradaTarjeta.p_IdUsuario 				= in.p_IdUsuario;
			entradaTarjeta.p_TarjetasBancarias 		= tarjetasBancariasPorCrear;
			salida = crearTarjeta.logica(conn, entradaTarjeta);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria.crearTarjetaBancaria", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarTarjetaBancaria(Connection conn, EntradaModificarSocioNegocio<TarjetasBancaria> in, List<TarjetasBancaria> tarjetasBancarias) throws Exception {
		
		logger.info("INICIO::ModificarTarjetaBancaria.modificarTarjetaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(tarjetasBancarias == null) return salida; 
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosTarjetasBancariasPorModificar(tarjetasBancarias);
		
		
		if(!registroCambio.isEmpty()) {
			ModificarPersonaTarjetaBancariaMasivo modificarTarjetaBancaria = new ModificarPersonaTarjetaBancariaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarTarjetaBancaria.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria.modificarTarjetaBancaria", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarTarjetaBancaria(Connection conn, EntradaModificarSocioNegocio<TarjetasBancaria> in, List<TarjetasBancaria> tarjetasBancarias) throws Exception {
		
		logger.info("INICIO::ModificarTarjetaBancaria.eliminarTarjetaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(tarjetasBancarias == null) return salida; 
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosTarjetasBancariasPorEliminar(tarjetasBancarias);
		
		if(!registroCambio.isEmpty()) {
			EliminarPersonaTarjetaBancariaMasivo eliminarTarjetaBancaria = new EliminarPersonaTarjetaBancariaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return eliminarTarjetaBancaria.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria.eliminarTarjetaBancaria", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosTarjetasBancariasPorModificar(List<TarjetasBancaria> tarjetasBancarias) {
		
		logger.info("INICIO::ModificarTarjetaBancaria.obtenerCambiosTarjetasBancariasPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(TarjetasBancaria tarjeta: tarjetasBancarias) {
			
			if(tarjeta.IdBanco > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdBanco";
				registroCambio.ValorNuevo		= String.valueOf(tarjeta.IdBanco);
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!tarjeta.IdTipo.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdTipoTarjetaBancaria";
				registroCambio.ValorNuevo		= tarjeta.IdTipo;
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!tarjeta.NumeroTarjeta.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "NumeroTarjetaBancaria";
				registroCambio.ValorNuevo		= tarjeta.NumeroTarjeta;
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(tarjeta.CodigoVerificacion > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "CodigoVerificacion";
				registroCambio.ValorNuevo		= String.valueOf(tarjeta.CodigoVerificacion);
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!tarjeta.FechaVencimiento.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "FechaVencimiento";
				registroCambio.ValorNuevo		= tarjeta.FechaVencimiento;
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria.obtenerCambiosTarjetasBancariasPorModificar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosTarjetasBancariasPorEliminar(List<TarjetasBancaria> tarjetasBancarias) {
		
		logger.info("INICIO::ModificarTarjetaBancaria.obtenerCambiosTarjetasBancariasPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(TarjetasBancaria tarjeta: tarjetasBancarias) {
			
			if(tarjeta.IdTarjetaBancaria > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= tarjeta.IdTarjetaBancaria;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarTarjetaBancaria.obtenerCambiosTarjetasBancariasPorEliminar", finServicio - inicioServicio);
		
		return listRegistroCambio;
	}
	
	private Map<String, List<TarjetasBancaria>> tarjetasAgrupadasPorAccion(List<TarjetasBancaria> tarjetasBancarias) {
		return tarjetasBancarias.stream().collect(Collectors.groupingBy(tar -> tar.Accion));
	}

}
