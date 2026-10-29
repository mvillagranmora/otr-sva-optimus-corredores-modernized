package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.SalidaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.pojo.DetalleOrigenFondo;

public class AdministrarEnrolamientoDeclaracionOrigenFondos {

	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoDeclaracionOrigenFondos.class);

	public List<DatosClienteDatosGenerales> crearDeclaracionOrigenFondos(Connection con,
			EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoDeclaracionOrigenFondos.crearDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();

		for (DatosClienteDatosGenerales borrador : borradoresCliente) {

			EntradaCrearDeclaracionOrigenFondos entradaCrearDeclaracionOrigenFondos = new EntradaCrearDeclaracionOrigenFondos();

			for (DeclaracionOrigenFondos declaracionOrigenFondo : borrador.DeclaracionOrigenFondos) {
				for (DetalleOrigenFondo origenFondo : declaracionOrigenFondo.DetalleOrigenFondo) {
					CrearDeclaracionOrigenFondos.Detalle detalleOrigenFondo = new CrearDeclaracionOrigenFondos.Detalle();
					detalleOrigenFondo.IdTipoOrigenFondo = origenFondo.IdTipoOrigenFondo;
					detalleOrigenFondo.Valor = origenFondo.Valor;
					detalleOrigenFondo.Otros = origenFondo.Otros;
					entradaCrearDeclaracionOrigenFondos.p_Detalle.add(detalleOrigenFondo);
				}

				entradaCrearDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaCrearDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
				entradaCrearDeclaracionOrigenFondos.p_IdModulo = "SOC";
				entradaCrearDeclaracionOrigenFondos.p_IdCliente = borrador.Cliente.IdCliente;
				entradaCrearDeclaracionOrigenFondos.p_IdTipoOperacionOrigenFondos = declaracionOrigenFondo.IdTipoOperacionOrigenFondos;
				entradaCrearDeclaracionOrigenFondos.p_FechaDeclaracion = String.valueOf(declaracionOrigenFondo.FechaDeclaracion);
				entradaCrearDeclaracionOrigenFondos.p_Comentario = declaracionOrigenFondo.Comentario;
				entradaCrearDeclaracionOrigenFondos.p_IdEstado = "ACT";
				entradaCrearDeclaracionOrigenFondos.p_OperacionInscripcion = declaracionOrigenFondo.OperacionInscripcion;

				CrearDeclaracionOrigenFondos crearDeclaracionOrigenFondos = new CrearDeclaracionOrigenFondos();
				SalidaCrearDeclaracionOrigenFondos salidaCrearDeclaracionOrigenFondos = crearDeclaracionOrigenFondos.logica(con, entradaCrearDeclaracionOrigenFondos);

				borrador.MensajesSalida.addAll(salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoDeclaracionOrigenFondos.crearDeclaracionOrigenFondos", fin - inicio);
		return borradoresCliente;
	}

}
