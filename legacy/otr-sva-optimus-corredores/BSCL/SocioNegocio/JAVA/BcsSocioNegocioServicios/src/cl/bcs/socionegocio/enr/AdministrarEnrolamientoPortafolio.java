package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.CrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.EntradaCrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.SalidaCrearPortafolio;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.Portafolio;

public class AdministrarEnrolamientoPortafolio {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoPortafolio.class);
	
	public List<DatosClienteDatosGenerales> crearPortafolio(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoPortafolio.crearPortafolio");
		long inicio = System.currentTimeMillis();

		List<SocioNegocioUtils.ConfiguracionPortafolioDefecto> configuracionPortafolios = SocioNegocioUtils.obtenerConfiguracionPortafolioDefecto(con);
		for (SocioNegocioUtils.ConfiguracionPortafolioDefecto portafolioDefecto : configuracionPortafolios) {
			for (DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				EntradaCrearPortafolio entradaCrearPortafolio = new EntradaCrearPortafolio();
				entradaCrearPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearPortafolio.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearPortafolio.p_IdModulo = entrada.p_IdModulo;
				entradaCrearPortafolio.p_UsaReglasNegocio = "N";
				entradaCrearPortafolio.p_IdCliente = borradorCliente.Cliente.IdCliente;
				entradaCrearPortafolio.p_IdPerfilComercialBase = borradorCliente.Enrolamiento.POR_IdPerfilComercial;
				entradaCrearPortafolio.p_IdPersonaBrokerExtranjero = 0L;
				entradaCrearPortafolio.p_Estado = "ACT";
				entradaCrearPortafolio.p_AreaCliente = borradorCliente.Enrolamiento.CLI_AreaCliente;
				entradaCrearPortafolio.p_CodigoPortafolio = portafolioDefecto.CodigoPortafolio;
				entradaCrearPortafolio.p_PropositoPortafolio = portafolioDefecto.Proposito;
				entradaCrearPortafolio.p_DesdeEnrolamiento = "S";
				entradaCrearPortafolio.p_IdEjecutivoComercial= borradorCliente.Enrolamiento.POR_IdEmpleadoEjecutivoComercial;

				CrearPortafolio crearPortafolio = new CrearPortafolio();
				SalidaCrearPortafolio salidaCrearPortafolio = crearPortafolio.logica(con, entradaCrearPortafolio);
				Portafolio portafolio = new Portafolio();
				portafolio.IdPortafolio = salidaCrearPortafolio.p_out_RefId;
				borradorCliente.Portafolio.add(portafolio);			
				List<MensajeSalida> mensajesAdvertencia = obtenerMensajesSalidaAdvertencia(salidaCrearPortafolio.P_OUT_ADVERTENCIASDETALLE, borradorCliente.IdInterno);
				List<MensajeSalida> mensajesError = obtenerMensajesSalidaError(salidaCrearPortafolio.P_OUT_ERRORESDETALLE, borradorCliente.IdInterno);
				borradorCliente.MensajesSalida.addAll(mensajesAdvertencia);
				borradorCliente.MensajesSalida.addAll(mensajesError);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPortafolio.crearPortafolio", fin - inicio);
		return borradoresCliente;
	}
	
	private List<MensajeSalida> obtenerMensajesSalidaAdvertencia(List<cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle> advertencias, Long idInterno){
		logger.trace("INICIO::AdministrarEnrolamientoPortafolio.obtenerMensajeSalidaAdvertencia");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = advertencias
				.parallelStream()
				.map(advertencia->{
					MensajeSalida mensaje = new MensajeSalida();
					mensaje.id = idInterno;
					mensaje.tipo = "S".equals(advertencia.RequiereAutorizacion) ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ADVERTENCIA;
					mensaje.codigo = advertencia.Codigo;
					mensaje.descripcion = advertencia.Descripcion;
					return mensaje;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPortafolio.obtenerMensajeSalidaAdvertencia", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> obtenerMensajesSalidaError(List<cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle> errores, Long idInterno){
		logger.trace("INICIO::AdministrarEnrolamientoPortafolio.obtenerMensajesSalidaError");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = errores
				.parallelStream()
				.map(error->{
					MensajeSalida mensaje = new MensajeSalida();
					mensaje.id = idInterno;
					mensaje.tipo = MensajeSalida.MENSAJE_ERROR;
					mensaje.codigo = error.Codigo;
					mensaje.descripcion = error.Descripcion;
					return mensaje;
				})
				.collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPortafolio.obtenerMensajesSalidaError", fin - inicio);
		return mensajes;
	}

}
