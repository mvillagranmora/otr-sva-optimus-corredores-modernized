package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.socionegocio.per.ModificarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.CuentaBancaria;

public class ModificarCuentaBancaria implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarCuentaBancaria.class);
	
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<CuentaBancaria> entrada) throws Exception{
		
		logger.info("INICIO::ModificarCuentaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		SalidaCrearPersonaCuentaBancaria salidaCrearCuentaBancaria = new SalidaCrearPersonaCuentaBancaria();
		
		Map<String, List<CuentaBancaria>> cuentasAgrupadasPorAccion = cuentasAgrupadasPorAccion(entrada.p_datos);
		
		salidaCrearCuentaBancaria = crearCuentaBancaria(conn, entrada, cuentasAgrupadasPorAccion.get("AGREGAR"));
		if(!salidaCrearCuentaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaCrearCuentaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaCrearCuentaBancaria.P_OUT_MENSAJES;
			return out;
		}
		
		SalidaEjecutarCambiosPersona salidaModificarCuentaBancaria = new SalidaEjecutarCambiosPersona();
		salidaModificarCuentaBancaria = modificarCuentaBancaria(conn, entrada, cuentasAgrupadasPorAccion.get("MODIFICAR"));
		if(!salidaModificarCuentaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarCuentaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarCuentaBancaria.P_OUT_MENSAJES;
			return out;
		}
		
		salidaModificarCuentaBancaria = eliminarCuentaBancaria(conn, entrada, cuentasAgrupadasPorAccion.get("ELIMINAR"));
		if(!salidaModificarCuentaBancaria.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarCuentaBancaria.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarCuentaBancaria.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria", finServicio - inicioServicio);
		
		return out;	
	}
	
	private SalidaCrearPersonaCuentaBancaria crearCuentaBancaria(Connection conn, EntradaModificarSocioNegocio<CuentaBancaria> in, List<CuentaBancaria> cuentasBancarias) throws Exception {

		logger.info("INICIO::ModificarCuentaBancaria.crearCuentaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearPersonaCuentaBancaria salida = new SalidaCrearPersonaCuentaBancaria();
		if(cuentasBancarias == null) return salida;
		
		List<CrearPersonaCuentaBancaria.InCuentaBancaria> cuentasBancariasPorCrear = new ArrayList<CrearPersonaCuentaBancaria.InCuentaBancaria>();
		
		cuentasBancarias.stream()
		.forEach(c -> {
			CrearPersonaCuentaBancaria.InCuentaBancaria cuenta = new CrearPersonaCuentaBancaria.InCuentaBancaria();
			cuenta.IdInterno 					= 0L;
			cuenta.IdBanco						= c.IdBanco;
			cuenta.IdMoneda						= c.IdMoneda;
			cuenta.NumeroCuentaBancaria			= c.NumeroCuentaBancaria;
			cuenta.IdPlaza						= c.CodigoPlaza;
			cuenta.IdTipoCuentaBancaria			= c.CodigoTipoCuentaBancaria;
			cuenta.IdPersona 					= in.p_IdPersona;
			cuenta.IdEstado						= "ACT";
			cuenta.IdTipoIdentificadorCuenta 	= "";
			cuenta.IdentificadorCuenta 			= "";
			cuenta.IdPersonaRelacionada 		= 0L;
			cuenta.CodigoCuentaInterbancario 	= "";
			cuenta.CodigoAba 					= "";
			cuenta.OtroCodigoBancoExtranjero 	= "";
			cuenta.CodigoIban 					= "";
			cuenta.OtroCodigoCuentaExtranjero 	= "";
			cuenta.IdCliente					= in.p_IdCliente;
			cuentasBancariasPorCrear.add(cuenta);	    
	
		});		
		
		if(!cuentasBancariasPorCrear.isEmpty()) {
			CrearPersonaCuentaBancaria crearCuenta = new CrearPersonaCuentaBancaria();
			EntradaCrearPersonaCuentaBancaria entradaCuenta = new EntradaCrearPersonaCuentaBancaria();
			entradaCuenta.p_IdEmpresa 				= in.p_IdEmpresa;
			entradaCuenta.p_IdModulo 				= "SOC";
			entradaCuenta.p_UsaReglasNegocio 		= "N";
			entradaCuenta.p_IdUsuario 				= in.p_IdUsuario;
			entradaCuenta.p_InCuentaBancaria		= cuentasBancariasPorCrear;
			salida = crearCuenta.logica(conn, entradaCuenta);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria.crearCuentaBancaria", finServicio - inicioServicio);
		
		return salida;
		
	}
	
	private SalidaEjecutarCambiosPersona modificarCuentaBancaria(Connection conn, EntradaModificarSocioNegocio<CuentaBancaria> in, List<CuentaBancaria> cuentasBancarias) throws Exception {
		
		logger.info("INICIO::ModificarCuentaBancaria.modificarCuentaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(cuentasBancarias == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosCuentasBancariasPorModificar(cuentasBancarias);
		
		
		if(!registroCambio.isEmpty()) {
			ModificarPersonaCuentaBancariaMasivo modificarCuentaBancaria = new ModificarPersonaCuentaBancariaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return modificarCuentaBancaria.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria.modificarCuentaBancaria", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private SalidaEjecutarCambiosPersona eliminarCuentaBancaria(Connection conn, EntradaModificarSocioNegocio<CuentaBancaria> in, List<CuentaBancaria> cuentasBancarias) throws Exception {
		
		logger.info("INICIO::ModificarCuentaBancaria.eliminarCuentaBancaria");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		if(cuentasBancarias == null) return salida;
		
		List<RegistroCambioMasivo> registroCambio = obtenerCambiosCuentasBancariasPorEliminar(cuentasBancarias);
		
		if(!registroCambio.isEmpty()) {
			EliminarPersonaCuentaBancariaMasivo eliminarCuentaBancaria = new EliminarPersonaCuentaBancariaMasivo();
			EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdModulo = "SOC";
			entrada.p_RegistroCambio = registroCambio;
			return eliminarCuentaBancaria.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria.eliminarCuentaBancaria", finServicio - inicioServicio);
		
		return salida;		
	}
	
	private List<RegistroCambioMasivo> obtenerCambiosCuentasBancariasPorModificar(List<CuentaBancaria> cuentasBancarias) {
		
		logger.info("INICIO::ModificarCuentaBancaria.obtenerCambiosCuentasBancariasPorModificar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(CuentaBancaria cuenta: cuentasBancarias) {
			
			if(cuenta.IdBanco > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdBanco";
				registroCambio.ValorNuevo		= String.valueOf(cuenta.IdBanco);
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!cuenta.IdMoneda.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdMoneda";
				registroCambio.ValorNuevo		= cuenta.IdMoneda;
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!cuenta.CodigoTipoCuentaBancaria.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdTipoCuentaBancaria";
				registroCambio.ValorNuevo		= cuenta.CodigoTipoCuentaBancaria;
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(!cuenta.NumeroCuentaBancaria.isEmpty()){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "NumeroCuentaBancaria";
				registroCambio.ValorNuevo		= cuenta.NumeroCuentaBancaria;
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
			if(cuenta.CodigoPlaza > 0L){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "IdPlaza";
				registroCambio.ValorNuevo		= String.valueOf(cuenta.CodigoPlaza);
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria.obtenerCambiosCuentasBancariasPorModificar", finServicio - inicioServicio);
		return listRegistroCambio;
	}
	
	
	private List<RegistroCambioMasivo> obtenerCambiosCuentasBancariasPorEliminar(List<CuentaBancaria> cuentasBancarias) {
		
		logger.info("INICIO::ModificarCuentaBancaria.obtenerCambiosCuentasBancariasPorEliminar");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> listRegistroCambio = new ArrayList<RegistroCambioMasivo>();
		
		for(CuentaBancaria cuenta: cuentasBancarias) {
			
			if(cuenta.IdCuentaBancaria > 0){
				RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
				registroCambio.CampoModificado	= "Borrado";
				registroCambio.ValorNuevo		= "S";
				registroCambio.IdEntidad		= cuenta.IdCuentaBancaria;
				listRegistroCambio.add(registroCambio);
			}
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarCuentaBancaria.obtenerCambiosCuentasBancariasPorEliminar", finServicio - inicioServicio);
		return listRegistroCambio;
	}
	
	private Map<String, List<CuentaBancaria>> cuentasAgrupadasPorAccion(List<CuentaBancaria> cuentasBancarias) {
		return cuentasBancarias.stream().collect(Collectors.groupingBy(cta -> cta.Accion));
	}

}
