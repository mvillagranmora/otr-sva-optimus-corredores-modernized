package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

public class NotificarCambioEjecutivoCliente {

	private static final Logger logger = LogManager.getLogger(NotificarCambioEjecutivoCliente.class);

	private static final String SQL_INSERT_ENTRADA       = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/eje/sql/NotificarCambioEjecutivoCliente_Insert_Entrada.sql");
	private static final String SQL_SELECT_USUARIO       = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/eje/sql/NotificarCambioEjecutivoCliente_Select_Usuario.sql");
	private static final String SQL_SELECT_DATOS_CLIENTE = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/eje/sql/NotificarCambioEjecutivoCliente_Select_DatosCliente.sql");

	public static class Entrada {
		public Long IdSolicitud;
		public Long IdCliente;
		public Long IdPersonaNuevoEjecutivo;
	}

	public static class EntradaTabla {
		public Long IdSolicitud;
		public Long IdCliente;
		public Long IdPersonaNuevoEjecutivo;
	}

	public static class DatosUsuario {
		public Long IdPersona;
		public Long IdDireccionElectronica;
	}

	public static class DatosCliente {
		public Long   IdSecuencia;
		public String CLIENTE;
		public String DOCUMENTOIDENTIDAD;
		public String TIPODOCUMENTOIDENTIDAD;
		public String TELEFONO;
		public String CORREO;
		public String AGENTE;
		public String CORREDORA;
		public String ATENCIONCLIENTE;
		public Long   IdPersonaDestinatario;
		public Long   IdDireccionElectronicaDestinatario;
		public Long   IdPersonaRemitente;
		public Long   IdDireccionElectronicaRemitente;
		public Long   IdCliente;
		public Long   IdPersonaNuevoEjecutivo;
	}

	public void ejecutar(Connection conn, Long idEmpresa, Long idUsuario, String idModulo,
						 Entrada entrada) throws Exception {

		logger.debug("INICIO::NotificarCambioEjecutivoCliente");
		SQL sql = new SQL();

		// 1. Insertar en tabla temporal de entrada
		List<EntradaTabla> listaInsert = new ArrayList<>();
		EntradaTabla item = new EntradaTabla();
		item.IdSolicitud           = entrada.IdSolicitud;
		item.IdCliente             = entrada.IdCliente;
		item.IdPersonaNuevoEjecutivo = entrada.IdPersonaNuevoEjecutivo;
		listaInsert.add(item);

		sql.batch(conn, SQL_INSERT_ENTRADA, listaInsert, EntradaTabla.class);

		// 2. Obtener datos del usuario (remitente)
		HashMap<String, Object> paramsUsuario = new HashMap<>();
		paramsUsuario.put("IdUsuario", idUsuario);
		List<DatosUsuario> listaUsuario = sql.query(conn, SQL_SELECT_USUARIO, paramsUsuario, DatosUsuario.class);
		if (listaUsuario.isEmpty()) {
			throw new Exception("No se encontró persona asociada al usuario " + idUsuario);
		}
		DatosUsuario usuario = listaUsuario.get(0);

		// 3. Obtener datos de clientes a notificar
		HashMap<String, Object> paramsCliente = new HashMap<>();
		paramsCliente.put("IdPersonaRemitente",            usuario.IdPersona);
		paramsCliente.put("IdDireccionElectronicaRemitente", usuario.IdDireccionElectronica);
		paramsCliente.put("IdEmpresa",                     idEmpresa);
		List<DatosCliente> listaClientes = sql.query(conn, SQL_SELECT_DATOS_CLIENTE, paramsCliente, DatosCliente.class);

		if (listaClientes.isEmpty()) {
			logger.debug("Sin clientes a notificar");
			return;
		}

		// 4. Construir y enviar eventos
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
		notifIn.P_IDEMPRESA       = idEmpresa;
		notifIn.P_IDUSUARIO       = idUsuario;
		notifIn.P_IDMODULO        = idModulo;
		notifIn.P_EVENTOS         = new ArrayList<>();
		notifIn.P_VALORES         = new ArrayList<>();
		notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
		notifIn.P_IDDIRECCIONES   = new ArrayList<>();

		for (DatosCliente dc : listaClientes) {

			AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
			evt.CodigoTipoEvento      = "CAMBIO_EJECUTIVO_AGENTE";
			evt.IdSecuencia           = dc.IdSecuencia;
			evt.FechaCreacionSistema  = new java.sql.Timestamp(System.currentTimeMillis()).toString();
			evt.IdPersonaRemitente    = dc.IdPersonaRemitente;
			evt.IdPersonaDestinatario = dc.IdPersonaDestinatario;
			evt.IdPortafolio          = 0L;
			notifIn.P_EVENTOS.add(evt);

			// Destinatario (TO)
			AgregarEvento.ObjIDDIRECCIONES dirTo = new AgregarEvento.ObjIDDIRECCIONES();
			dirTo.IdSecuencia       = dc.IdSecuencia;
			dirTo.IdDireccion       = dc.IdDireccionElectronicaDestinatario;
			dirTo.TipoDestinatario  = "TO";
			notifIn.P_IDDIRECCIONES.add(dirTo);

			// Remitente en copia (CC)
			AgregarEvento.ObjIDDIRECCIONES dirCc = new AgregarEvento.ObjIDDIRECCIONES();
			dirCc.IdSecuencia       = dc.IdSecuencia;
			dirCc.IdDireccion       = dc.IdDireccionElectronicaRemitente;
			dirCc.TipoDestinatario  = "CC";
			notifIn.P_IDDIRECCIONES.add(dirCc);

			// Valores del template
			notifIn.addValor(dc.IdSecuencia, "CLIENTE",              dc.CLIENTE)
				   .addValor(dc.IdSecuencia, "DOCUMENTOIDENTIDAD",    dc.DOCUMENTOIDENTIDAD)
				   .addValor(dc.IdSecuencia, "TIPODOCUMENTOIDENTIDAD", dc.TIPODOCUMENTOIDENTIDAD)
				   .addValor(dc.IdSecuencia, "TELEFONO",              dc.TELEFONO)
				   .addValor(dc.IdSecuencia, "CORREO",                dc.CORREO)
				   .addValor(dc.IdSecuencia, "AGENTE",                dc.AGENTE)
				   .addValor(dc.IdSecuencia, "CORREDORA",             dc.CORREDORA)
				   .addValor(dc.IdSecuencia, "ATENCIONCLIENTE",       dc.ATENCIONCLIENTE);
		}

		AgregarEvento notif = new AgregarEvento();
		AgregarEvento.ServiceSalida notifOut = notif.logica(conn, notifIn);
		logger.debug("ID EVENTO::", notifOut.P_OUT_IDEVENTO);

		logger.debug("FIN::NotificarCambioEjecutivoCliente");
	}
}
