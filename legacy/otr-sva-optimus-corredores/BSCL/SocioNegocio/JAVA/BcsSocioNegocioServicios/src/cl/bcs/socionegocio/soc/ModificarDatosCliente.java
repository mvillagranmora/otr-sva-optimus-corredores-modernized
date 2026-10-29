package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.RegistroCambio;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.SalidaModificarClienteDatosGenerales;
import cl.bcs.socionegocio.soc.pojo.DatosCliente;

public class ModificarDatosCliente implements EjecutarCambiosSocioNegocio{
	
	static final Logger logger = LogManager.getLogger(ModificarDatosCliente.class);
	
	public SalidaModificarSocioNegocio logica(Connection conn, EntradaModificarSocioNegocio<DatosCliente> entrada) throws Exception {
		
		logger.info("INICIO::ModificarDatosCliente");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarSocioNegocio out = new SalidaModificarSocioNegocio();
		
		SalidaModificarClienteDatosGenerales salidaModificarCliente = new SalidaModificarClienteDatosGenerales();

		salidaModificarCliente = modificarDatosCliente(conn, entrada);
		
		if(salidaModificarCliente.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarCliente.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarCliente.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosCliente", finServicio - inicioServicio);
		
		return out;	
	}
	

	private SalidaModificarClienteDatosGenerales modificarDatosCliente(Connection conn, EntradaModificarSocioNegocio<DatosCliente> in) throws Exception {
		
		logger.info("INICIO::ModificarDatosCliente.modificarDatosCliente");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambio> registroCambio = obtenerCambiosDatosCliente(in.p_datos);
		SalidaModificarClienteDatosGenerales salida = new SalidaModificarClienteDatosGenerales();
		
		if(!registroCambio.isEmpty()) {
			ModificarClienteDatosGenerales modificarCliente = new ModificarClienteDatosGenerales();
			EntradaModificarClienteDatosGenerales entrada = new EntradaModificarClienteDatosGenerales();
			entrada.p_IdEmpresa 		= in.p_IdEmpresa;
			entrada.p_IdUsuario 		= in.p_IdUsuario;
			entrada.p_UsaReglasNegocio 	= "N";
			entrada.p_IdModulo 			= "SOC";
			entrada.p_IdCliente 		= in.p_IdCliente;
			entrada.p_IdCanal 			= "WBS";
			entrada.p_RegistroCambio 	= registroCambio;
			salida = modificarCliente.logica(conn, entrada);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosCliente.modificarDatosCliente", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private List<RegistroCambio> obtenerCambiosDatosCliente(List<DatosCliente> datosCliente) {
		
		logger.info("INICIO::ModificarDatosCliente.obtenerCambiosDatosCliente");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambio> registroCambioCliente = new ArrayList<RegistroCambio>();
		
		for(DatosCliente cliente: datosCliente) {
			
			if(!cliente.IdTipoCliente.isEmpty()){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "IdTipoCliente";
				cambioCliente.ValorNuevo		= cliente.IdTipoCliente.trim();
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(!cliente.IdCategoriaInversionista.isEmpty()){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "IdCategoriaInversionista";
				cambioCliente.ValorNuevo		= cliente.IdCategoriaInversionista.trim();
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(cliente.SegmentoCliente > 0L){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "IdSegmentoCliente";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.SegmentoCliente);
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(cliente.CodigoEjecutivo > 0L){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "IdPersonaEjecutivo";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.CodigoEjecutivo);
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(!cliente.CodigoEjecutivoExterno.isEmpty()){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "CodigoEjecutivoExterno";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.CodigoEjecutivoExterno);
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(cliente.Sucursal > 0L){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "IdSucursalPreferencia";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.Sucursal);
				registroCambioCliente.add(cambioCliente);	
			}
			
			if(!cliente.ClienteFinal.isEmpty()){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "EsClienteFinal";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.ClienteFinal);
				registroCambioCliente.add(cambioCliente);	
			}
			
			/*if(!cliente.AreaCliente.isEmpty()){
				RegistroCambio cambioCliente = new RegistroCambio();
				cambioCliente.CampoModificado	= "Are";
				cambioCliente.ValorNuevo		= String.valueOf(cliente.ClienteFinal);
				registroCambioCliente.add(cambioCliente);	
			}*/
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosCliente.obtenerCambiosDatosCliente", finServicio - inicioServicio);
		
		return registroCambioCliente;
	}

}
