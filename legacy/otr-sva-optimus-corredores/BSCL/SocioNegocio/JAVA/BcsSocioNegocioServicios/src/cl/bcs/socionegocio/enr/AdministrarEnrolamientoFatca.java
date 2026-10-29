package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.ModificarPersonaFatca;
import cl.bcs.socionegocio.per.ModificarPersonaFatca.EntradaModificarPersonaFatca;
import cl.bcs.socionegocio.per.ModificarPersonaFatca.SalidaModificarPersonaFatca;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;

public class AdministrarEnrolamientoFatca {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoFatca.class);
	
	public List<DatosClienteDatosGenerales> modificarFatcas(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoFatca.modificarFatcas");
		long inicio = System.currentTimeMillis();
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			EntradaModificarPersonaFatca entradaModificarPersonaFatca = new EntradaModificarPersonaFatca();
			entradaModificarPersonaFatca.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificarPersonaFatca.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificarPersonaFatca.p_IdModulo = entrada.p_IdModulo;
			entradaModificarPersonaFatca.p_UsaReglasNegocio = "N";
			entradaModificarPersonaFatca.p_IdPersona = borradorCliente.IdPersona;
			entradaModificarPersonaFatca.p_RegistroCambio = obtenerRegistrosModificarFatcaCambio(borradorCliente.Fatca);

			
			entradaModificarPersonaFatca.p_RegistroCambio = entradaModificarPersonaFatca
					.p_RegistroCambio
					.parallelStream()
					.filter(registroCambio->registroCambio.ValorNuevo != null)
					.collect(Collectors.toList());
			
			ModificarPersonaFatca modificarPersonaFatca = new ModificarPersonaFatca();
			SalidaModificarPersonaFatca salidaModificarPersonaFatca = modificarPersonaFatca.logica(con, entradaModificarPersonaFatca);
			borradorCliente.MensajesSalida.addAll(salidaModificarPersonaFatca.P_OUT_MENSAJES);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoFatca.modificarFatcas", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> crearPaisResidenciaFatca(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoFatca.crearPaisResidenciaFatca");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearFatcaPaisResidencia entradaCrearFatcaPaisResidencia = new EntradaCrearFatcaPaisResidencia();
		entradaCrearFatcaPaisResidencia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearFatcaPaisResidencia.p_IdUsuario = entrada.p_IdUsuario;
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			for(cl.bcs.socionegocio.enr.pojo.FatcaPaisResidencia borradorFatcaPaisResidencia : borradorCliente.FatcaPaisesResidencia) {
				FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
				fatcaPaisResidencia.IdPersona = borradorCliente.IdPersona;
				fatcaPaisResidencia.IdPaisResidencia = borradorFatcaPaisResidencia.IdPaisResidencia;
				fatcaPaisResidencia.TIN = borradorFatcaPaisResidencia.TIN;
				fatcaPaisResidencia.IdCausaNoTin = borradorFatcaPaisResidencia.IdCausaNoTin;
				fatcaPaisResidencia.Observacion = borradorFatcaPaisResidencia.Observacion;
				fatcaPaisResidencia.PaisResidenciaPrincipal = borradorFatcaPaisResidencia.PaisResidenciaPrincipal;
				entradaCrearFatcaPaisResidencia.p_FatcaPaisResidencia.add(fatcaPaisResidencia);
			}
		}
		
		if(entradaCrearFatcaPaisResidencia.p_FatcaPaisResidencia.size() > 0) {
			CrearFatcaPaisResidencia crearFatcaPaisResidencia = new CrearFatcaPaisResidencia();
			crearFatcaPaisResidencia.logica(con, entradaCrearFatcaPaisResidencia);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoFatca.crearPaisResidenciaFatca", fin - inicio);
		return borradoresCliente;
	}
	
	private List<RegistroCambio> obtenerRegistrosModificarFatcaCambio(Fatca fatca){
		logger.trace("INICIO::AdministrarEnrolamientoFatca.obtenerRegistrosModificarFatcaCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambio> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdClasificacionFatca", fatca.IdClasificacionFatca));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("IdClasificacionEmpresaFatca", fatca.IdClasificacionEmpresaFatca));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("NumeroGIIN", fatca.NumeroGIIN));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("FechaFirmaFormulario", fatca.FechaFirmaFormulario));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("AutorizacionInformarOrganismo", fatca.AutorizacionInformarOrganismo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionResidente", fatca.ExcepcionResidente));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionNacimiento", fatca.ExcepcionNacimiento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionDomicilio", fatca.ExcepcionDomicilio));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionTelefono", fatca.ExcepcionTelefono));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionOrdenPermanente", fatca.ExcepcionOrdenPermanente));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ExcepcionFirma", fatca.ExcepcionFirma));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("ParticipacionSociedadesUSA", fatca.ParticipacionSociedadesUSA));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio("OrdenPermanente", fatca.OrdenPermanente));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoFatca.obtenerRegistrosModificarFatcaCambio", fin - inicio);
		return registrosCambio;
	}

}
