package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.NombreFantasia;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo.EntradaCrearPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo.SalidaCrearPersonaNombreFantasiaMasivo;

public class AdministrarEnrolamientoNombreFantasia {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoNombreFantasia.class);
	
	public List<DatosClienteDatosGenerales> crearNombreFantasia(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente)  throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoNombreFantasia.crearNombreFantasia");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearPersonaNombreFantasiaMasivo entradaCrearPersonaNombreFantasiaMasivo = new EntradaCrearPersonaNombreFantasiaMasivo();
		entradaCrearPersonaNombreFantasiaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearPersonaNombreFantasiaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaCrearPersonaNombreFantasiaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearPersonaNombreFantasiaMasivo.p_UsaReglasNegocio = "N";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			for(NombreFantasia nombreFantasia : borradorCliente.NombresFantasia) {
				cl.bcs.socionegocio.per.pojo.NombreFantasia entradaNombreFantasia = new cl.bcs.socionegocio.per.pojo.NombreFantasia();
				entradaNombreFantasia.IdPersona = borradorCliente.IdPersona;
				entradaNombreFantasia.DescripcionNombreFantasia = nombreFantasia.NombreFantasia;
				entradaNombreFantasia.IdEstado = "ACT";	
				entradaCrearPersonaNombreFantasiaMasivo.p_NombresFantasia.add(entradaNombreFantasia);
			}
		}
		
		if(entradaCrearPersonaNombreFantasiaMasivo.p_NombresFantasia.size() > 0) {
			CrearPersonaNombreFantasiaMasivo crearPersonaNombreFantasiaMasivo = new CrearPersonaNombreFantasiaMasivo();
			SalidaCrearPersonaNombreFantasiaMasivo salidaCrearPersonaNombreFantasiaMasivo = crearPersonaNombreFantasiaMasivo.logica(con, entradaCrearPersonaNombreFantasiaMasivo);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaCrearPersonaNombreFantasiaMasivo.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoNombreFantasia.crearNombreFantasia", fin - inicio);
		return borradoresCliente;
	}

}
