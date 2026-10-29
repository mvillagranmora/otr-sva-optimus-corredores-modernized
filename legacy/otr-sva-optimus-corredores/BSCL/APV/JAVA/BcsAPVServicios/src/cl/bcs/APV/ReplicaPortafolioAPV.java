package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.socionegocio.por.pojo.CuentaCustodio;
import cl.bcs.socionegocio.por.pojo.PreferenciaPagoPortafolio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ReplicaPortafolioAPV implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ReplicaPortafolioAPV.class);
	
	private final String SQL_SELECT_INFORMACION_PORTAFOLIO_APV = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectInformacionPortafolioAPV.sql");
	private final String SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectPortafoliosAPVAsociados.sql");
	private final String SQL_SELECT_INDICADOR_PORTAFOLIO_ORIGEN = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectIndicadorPortafolioOrigen.sql");
	private final String SQL_DELETE_INDICADOR_REPLICA = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_DeleteIndicadorReplica.sql");
	private final String SQL_INSERT_INDICADOR_REPLICA = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_InsertIndicadorReplica.sql");
	private final String SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO_ORIGEN = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectCuentaCustodioPortafolioOrigen.sql");
	private final String SQL_INSERT_CUENTA_CUSTODIO_REPLICA = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_InsertCuentaCustodioReplica.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_ORIGEN = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectPreferenciaPagoPortafolioOrigen.sql");
	private final String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_InsertPreferenciaPagoPortafolioOrigen.sql");
	private final String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_UpdatePreferenciaPagoPortafolioOrigenNoDefecto.sql");
	private final String SQL_SELECT_AUTORIZADO_ORIGEN = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_SelectAutorizadoOrigen.sql");
	private final String SQL_INSERT_AUTORIZADO_REPLICA = loadResourceToString("cl/bcs/APV/sql/ReplicaPortafolioAPV_InsertAutorizadoReplica.sql");
	
	public static class EntradaReplicaPortafolioAPV extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_Portafolio;
		public Long p_IdEntidad;
		public String p_TipoEntidad;
		public String p_TipoOperacion;
	}
	
	public static class SalidaReplicaPortafolioAPV extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaReplicaPortafolioAPV() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class PortafolioAPV extends BcsPojo{
		public Long IdPortafolio;
		public Long IdCliente;
		public java.sql.Timestamp FechaIngreso;
		public String IdPerfilLimitesExtra;
		public Integer IdProductoAgrupador;
	}
	
	public static class Indicador extends BcsPojo{
		public Long IdIndicador;
		public Long IdEntidad;
		public String IdTipoEntidad;
		public Long IdTipoIndicador;
		public String ModeloCliente;
		public Long IdEmpresa;
		public String ValorIndicador;
		public java.sql.Timestamp FechaIngreso;
		public java.sql.Timestamp FechaCreacion;
		public java.sql.Timestamp FechaModificacion;
		public String IdEstadoAutorizacion;
	}
	
	public static class Autorizado extends BcsPojo{
		public Long IdAutorizado;
		public Long IdPortafolio;
		public Long IdPersona;
		public String IdTipoAutorizado;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public java.sql.Timestamp FechaIngreso;
		public java.sql.Timestamp FechaCreacion;
		public java.sql.Timestamp FechaModificacion;
		public Long IdUsuarioCreador;
		public String NumeroRegistroPublico;
		public String Asiento;
		public String FichaPartida;
		public String TipoFirma;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaReplicaPortafolioAPV jin = gson.fromJson(in.toString(), EntradaReplicaPortafolioAPV.class);
		SalidaReplicaPortafolioAPV out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaReplicaPortafolioAPV logica(Connection con, EntradaReplicaPortafolioAPV entrada) throws Exception {
		logger.info("INICIO::ReplicaPortafolioAPV");
		long inicioServicio = System.currentTimeMillis();
		SalidaReplicaPortafolioAPV out = new SalidaReplicaPortafolioAPV();
		PortafolioAPV portafolioAPV = obtenerInformacionPortafolioAPV(con, entrada.p_Portafolio);
		
		if(portafolioAPV == null) {
			return out;
		}
		
		List<PortafolioAPV> portafoliosAPV = obtenerPortafoliosAPVAsociados(con, portafolioAPV);
		if(portafoliosAPV.isEmpty()) {
			//Se usa metodo sin ir a buscar la traduccion a base de datos, dado que es una migracion de un SQL (no se pueden cambiar el codigo del mensaje) y no se puede cambiar la traduccion.
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"","No se encontraron otros portafolios relacionados."));
		}
		
		Long numeroEntidadesReplicadas = 0L;
		numeroEntidadesReplicadas += replicarAutorizados(con, portafoliosAPV, entrada);
		numeroEntidadesReplicadas += replicarIndicadores(con, portafoliosAPV, entrada);
		numeroEntidadesReplicadas += replicarCuentasCustodio(con, portafoliosAPV, entrada);
		numeroEntidadesReplicadas += replicarPreferenciasPagoPortafolio(con, portafoliosAPV, entrada);
		if(numeroEntidadesReplicadas == 0) {
			//Se usa metodo sin ir a buscar la traduccion a base de datos, dado que es una migracion de un SQL (no se pueden cambiar el codigo del mensaje) y no se puede cambiar la traduccion.
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"","El TipoEntidad y el TipoOperacion no coinciden con las opciones disponibles."));
		}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ReplicaPortafolioAPV", finServicio - inicioServicio);
		return out;
	}
	
	private PortafolioAPV obtenerInformacionPortafolioAPV(Connection con, Long idPortafolio) throws Exception {
		logger.debug("INICIO::ReplicaPortafolioAPV.obtenerInformacionPortafolioAPV");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdPortafolio", idPortafolio);
		SQL sql = new SQL();
		List<PortafolioAPV> portafoliosAPVEntrada = sql.query(con, SQL_SELECT_INFORMACION_PORTAFOLIO_APV, parametros, PortafolioAPV.class);
		PortafolioAPV portafolioAPV = portafoliosAPVEntrada.parallelStream().findFirst().orElse(null);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.obtenerInformacionPortafolioAPV", fin - inicio);
		return portafolioAPV;
	}
	
	private List<PortafolioAPV> obtenerPortafoliosAPVAsociados(Connection con, PortafolioAPV portafolioAPV) throws Exception {
		logger.debug("INICIO::ReplicaPortafolioAPV.obtenerPortafoliosAPVAsociados");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdCliente", portafolioAPV.IdCliente);
		parametros.put("IdPortafolio", portafolioAPV.IdPortafolio);
		parametros.put("IdProductoAgrupador", portafolioAPV.IdProductoAgrupador);
		SQL sql = new SQL();
		List<PortafolioAPV> portafolios = sql.query(con, SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS, parametros, PortafolioAPV.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.obtenerPortafoliosAPVAsociados", fin - inicio);
		return portafolios;
	}
	
	private Long replicarIndicadores(Connection con, List<PortafolioAPV> portafoliosAPV, EntradaReplicaPortafolioAPV entrada) throws Exception {
		logger.debug("INICIO::ReplicaPortafolioAPV.replicarIndicadores");
		long inicio = System.currentTimeMillis();
		if(esReplicaIndicadores(entrada)) {
			HashMap<String,Object> parametros = new HashMap<String,Object>();
			parametros.put("IdSolicitudCambio", entrada.p_IdEntidad);
			parametros.put("IdPortafolio", entrada.p_Portafolio);
			SQL sql = new SQL();
			List<Indicador> indicadores = sql.query(con, SQL_SELECT_INDICADOR_PORTAFOLIO_ORIGEN, parametros, Indicador.class);
			List<Indicador> indicadoresReplica = new ArrayList<Indicador>();
			for(PortafolioAPV portafolioAPV : portafoliosAPV) {
				for(Indicador indicador : indicadores) {
					Indicador indicadorReplica = new Indicador();
					indicadorReplica.IdEntidad = portafolioAPV.IdPortafolio;
					indicadorReplica.IdTipoEntidad = indicador.IdTipoEntidad;
					indicadorReplica.IdTipoIndicador = indicador.IdTipoIndicador;
					indicadorReplica.ModeloCliente = indicador.ModeloCliente;
					indicadorReplica.IdEmpresa = indicador.IdEmpresa;
					indicadorReplica.ValorIndicador = indicador.ValorIndicador;
					indicadorReplica.FechaIngreso = indicador.FechaIngreso;
					indicadorReplica.FechaCreacion = indicador.FechaCreacion;
					indicadorReplica.FechaModificacion = indicador.FechaModificacion;
					indicadorReplica.IdEstadoAutorizacion = indicador.IdEstadoAutorizacion;
					indicadoresReplica.add(indicadorReplica);
				}
			}
			sql.batch(con, SQL_DELETE_INDICADOR_REPLICA, indicadoresReplica, Indicador.class);
			sql.batch(con, SQL_INSERT_INDICADOR_REPLICA, indicadoresReplica, Indicador.class);
			return 1L;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.replicarIndicadores", fin - inicio);
		return 0L;
	}
	
	private Boolean esReplicaIndicadores(EntradaReplicaPortafolioAPV entrada) {
		logger.trace("INICIO::ReplicaPortafolioAPV.replicarIndicadores");
		long inicio = System.currentTimeMillis();
		Boolean esReplicaIndicadores = "INDICADORES".equals(entrada.p_TipoEntidad) && "U".equals(entrada.p_TipoOperacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ReplicaPortafolioAPV.replicarIndicadores", fin - inicio);
		return esReplicaIndicadores;
	}

	private Long replicarCuentasCustodio(Connection con, List<PortafolioAPV> portafoliosAPV, EntradaReplicaPortafolioAPV entrada) throws SQLException {
		logger.debug("INICIO::ReplicaPortafolioAPV.replicarCuentasCustodio");
		long inicio = System.currentTimeMillis();

		if (esReplicaCuentaCustodioCrear(entrada)) {
			HashMap<String, Object> parametros = new HashMap<>();
			parametros.put("IdCuentaCustodioPortafolio", entrada.p_IdEntidad);

			SQL sql = new SQL();
			List<CuentaCustodio> cuentasCustodio = sql.query(con, SQL_SELECT_CUENTA_CUSTODIO_PORTAFOLIO_ORIGEN, parametros, CuentaCustodio.class);
			List<CuentaCustodio> cuentasCustodioReplicas = new ArrayList<>();

			for (PortafolioAPV portafolioAPV : portafoliosAPV) {
				for (CuentaCustodio cuentaCustodio : cuentasCustodio) {
					CuentaCustodio cuentaCustodioReplica = new CuentaCustodio();
					cuentaCustodioReplica.IdPortafolio = portafolioAPV.IdPortafolio;
					cuentaCustodioReplica.IdBoveda = cuentaCustodio.IdBoveda;
					cuentaCustodioReplica.Codigo = cuentaCustodio.Codigo;
					cuentaCustodioReplica.IdTipoCuentaCustodia = cuentaCustodio.IdTipoCuentaCustodia;
					cuentaCustodioReplica.IdTipoCustodia = cuentaCustodio.IdTipoCustodia;
					cuentaCustodioReplica.IdModeloCustodia = cuentaCustodio.IdModeloCustodia;
					cuentaCustodioReplica.IdPersonaBancoCustodio = cuentaCustodio.IdPersonaBancoCustodio;
					cuentaCustodioReplica.IdPersonaBancoLiquidador = cuentaCustodio.IdPersonaBancoLiquidador;
					cuentaCustodioReplica.IdEstado = cuentaCustodio.IdEstado;
					cuentaCustodioReplica.LiquidaPrepagos = cuentaCustodio.LiquidaPrepagos;
					cuentaCustodioReplica.LiquidaVencimientos = cuentaCustodio.LiquidaVencimientos;
					cuentaCustodioReplica.LiquidaCupones = cuentaCustodio.LiquidaCupones;
					cuentaCustodioReplica.LiquidaDividendos = cuentaCustodio.LiquidaDividendos;
					cuentaCustodioReplica.FechaIngreso = cuentaCustodio.FechaIngreso;
					cuentaCustodioReplica.FechaCreacion = cuentaCustodio.FechaCreacion;
					cuentaCustodioReplica.FechaModificacion = cuentaCustodio.FechaModificacion;
					cuentaCustodioReplica.IdUsuarioCreador = cuentaCustodio.IdUsuarioCreador;
					cuentaCustodioReplica.IdEstadoAutorizacion = cuentaCustodio.IdEstadoAutorizacion;

					cuentasCustodioReplicas.add(cuentaCustodioReplica);
				}
			}

			sql.batch(con, SQL_INSERT_CUENTA_CUSTODIO_REPLICA, cuentasCustodioReplicas, CuentaCustodio.class);

			return 1L;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.replicarCuentasCustodio", fin - inicio);

		return 0L;
	}

	private Boolean esReplicaCuentaCustodioCrear(EntradaReplicaPortafolioAPV entrada) {
		logger.trace("INICIO::ReplicaPortafolioAPV.esReplicaCuentaCustodioCrear");
		long inicio = System.currentTimeMillis();

		Boolean esReplicaCuentaCustodioCrear = "CUENTACUSTODIO".equals(entrada.p_TipoEntidad) && "I".equals(entrada.p_TipoOperacion);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ReplicaPortafolioAPV.esReplicaCuentaCustodioCrear", fin - inicio);

		return esReplicaCuentaCustodioCrear;
	}
	
	private Long replicarPreferenciasPagoPortafolio(Connection con, List<PortafolioAPV> portafoliosAPV,  EntradaReplicaPortafolioAPV entrada) throws Exception{
		logger.debug("INICIO::ReplicaPortafolioAPV.replicarPreferenciasPagoPortafolio");
		long inicio = System.currentTimeMillis();
		if(esReplicaPreferenciaPagoPortafolio(entrada)) {
			HashMap<String, Object> parametros = new HashMap<>();
			parametros.put("IdEmpresa", entrada.p_IdEmpresa);
			parametros.put("IdPreferenciaPagoPortafolio", entrada.p_IdEntidad);
			
			SQL sql = new SQL();
			List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = sql.query(con, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO_ORIGEN, parametros, PreferenciaPagoPortafolio.class);
			List<PreferenciaPagoPortafolio> preferenciasPagoPortafolioReplicas = new ArrayList<>();
			List<PreferenciaPagoPortafolio> preferenciasPagoPortafolioReplicasPorDefecto = new ArrayList<>();
			
			for (PortafolioAPV portafolioAPV : portafoliosAPV) {
				for(PreferenciaPagoPortafolio preferenciaPagoPortafolio : preferenciasPagoPortafolio) {
					PreferenciaPagoPortafolio preferenciaPagoPortafolioReplica = new PreferenciaPagoPortafolio();
					
					preferenciaPagoPortafolioReplica.IdPreferenciaPagoPortafolio = preferenciaPagoPortafolio.IdPreferenciaPagoPortafolio;
					preferenciaPagoPortafolioReplica.IdPortafolio = portafolioAPV.IdPortafolio;
					preferenciaPagoPortafolioReplica.IdPreferenciaPago = preferenciaPagoPortafolio.IdPreferenciaPago;
					preferenciaPagoPortafolioReplica.IdPersonaRelacionada = preferenciaPagoPortafolio.IdPersonaRelacionada;
					preferenciaPagoPortafolioReplica.FechaCreacion = preferenciaPagoPortafolio.FechaCreacion;
					preferenciaPagoPortafolioReplica.FechaModificacion = preferenciaPagoPortafolio.FechaModificacion;
					preferenciaPagoPortafolioReplica.IdUsuarioCreador = preferenciaPagoPortafolio.IdUsuarioCreador;
					preferenciaPagoPortafolioReplica.IdEstado = preferenciaPagoPortafolio.IdEstado;
					preferenciaPagoPortafolioReplica.IdEmpresa = preferenciaPagoPortafolio.IdEmpresa;
					preferenciaPagoPortafolioReplica.IdTarjetaBancaria = preferenciaPagoPortafolio.IdTarjetaBancaria;
					preferenciaPagoPortafolioReplica.FechaIngreso = preferenciaPagoPortafolio.FechaIngreso;
					preferenciaPagoPortafolioReplica.IdEstadoAutorizacion = preferenciaPagoPortafolio.IdEstadoAutorizacion;
					preferenciaPagoPortafolioReplica.PorDefecto = preferenciaPagoPortafolio.PorDefecto;
					preferenciaPagoPortafolioReplica.IdCarga = preferenciaPagoPortafolio.IdCarga;
					
					if(preferenciaPagoPortafolio.PorDefecto == "S") {
						preferenciasPagoPortafolioReplicasPorDefecto.add(preferenciaPagoPortafolioReplica);
					}		
					preferenciasPagoPortafolioReplicas.add(preferenciaPagoPortafolioReplica);
				}
				
				sql.batch(con, SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA, preferenciasPagoPortafolioReplicas, PreferenciaPagoPortafolio.class);
				sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA_NO_DEFECTO, preferenciasPagoPortafolioReplicasPorDefecto, PreferenciaPagoPortafolio.class);	
			}			
			return 1L;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.replicarPreferenciasPagoPortafolio", fin - inicio);
		return 0L;
	}
	
	private Boolean esReplicaPreferenciaPagoPortafolio(EntradaReplicaPortafolioAPV entrada) {
		logger.trace("INICIO::ReplicaPortafolioAPV.esReplicaPreferenciaPagoPortafolio");
		long inicio = System.currentTimeMillis();
		Boolean esReplicaPreferenciaPagoPortafolio = "PREFERENCIASDEPAGO".equals(entrada.p_TipoEntidad) && ("I".equals(entrada.p_TipoOperacion) || "U".equals(entrada.p_TipoOperacion));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.esReplicaPreferenciaPagoPortafolio", fin - inicio);
		return esReplicaPreferenciaPagoPortafolio;
	}
	
	private Long replicarAutorizados(Connection con, List<PortafolioAPV> portafoliosAPV, EntradaReplicaPortafolioAPV entrada) throws Exception {
		logger.debug("INICIO::ReplicaPortafolioAPV.replicarAutorizados");
		long inicio = System.currentTimeMillis();
		
		if (esReplicaAutorizacion(entrada)) {
			HashMap<String, Object> parametros = new HashMap<>();
			parametros.put("IdAutorizado", entrada.p_IdEntidad);
			
			SQL sql = new SQL();
			List<Autorizado> autorizados = sql.query(con, SQL_SELECT_AUTORIZADO_ORIGEN, parametros, Autorizado.class);
			
			if (!autorizados.isEmpty()) {
				Autorizado autorizadoPrincipal = autorizados.get(0);
				List<Autorizado> autorizadosReplica = new ArrayList<>();
				
				// Obtener secuencias para los nuevos autorizados
				List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Portafolio.BD.Secuencias::Autorizado", Long.valueOf(portafoliosAPV.size()));
				
				int index = 0;
				for (PortafolioAPV portafolioAPV : portafoliosAPV) {
					// No replicar en el portafolio origen
					if (!portafolioAPV.IdPortafolio.equals(autorizadoPrincipal.IdPortafolio)) {
						Autorizado autorizadoReplica = new Autorizado();
						autorizadoReplica.IdAutorizado = secuencias.get(index);
						autorizadoReplica.IdPortafolio = portafolioAPV.IdPortafolio;
						autorizadoReplica.IdPersona = autorizadoPrincipal.IdPersona;
						autorizadoReplica.IdTipoAutorizado = autorizadoPrincipal.IdTipoAutorizado;
						autorizadoReplica.IdEstado = autorizadoPrincipal.IdEstado;
						autorizadoReplica.IdEstadoAutorizacion = autorizadoPrincipal.IdEstadoAutorizacion;
						autorizadoReplica.FechaIngreso = autorizadoPrincipal.FechaIngreso;
						autorizadoReplica.FechaCreacion = autorizadoPrincipal.FechaCreacion;
						autorizadoReplica.FechaModificacion = autorizadoPrincipal.FechaModificacion;
						autorizadoReplica.IdUsuarioCreador = autorizadoPrincipal.IdUsuarioCreador;
						autorizadoReplica.NumeroRegistroPublico = autorizadoPrincipal.NumeroRegistroPublico;
						autorizadoReplica.Asiento = autorizadoPrincipal.Asiento;
						autorizadoReplica.FichaPartida = autorizadoPrincipal.FichaPartida;
						autorizadoReplica.TipoFirma = autorizadoPrincipal.TipoFirma;
						
						autorizadosReplica.add(autorizadoReplica);
						index++;
					}
				}
				
				if (!autorizadosReplica.isEmpty()) {
					sql.batch(con, SQL_INSERT_AUTORIZADO_REPLICA, autorizadosReplica, Autorizado.class);
				}
				
				long fin = System.currentTimeMillis();
				logger.debug("FIN::ReplicaPortafolioAPV.replicarAutorizados {}", fin - inicio);
				return 1L;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ReplicaPortafolioAPV.replicarAutorizados {}", fin - inicio);
		return 0L;
	}
	
	private Boolean esReplicaAutorizacion(EntradaReplicaPortafolioAPV entrada) {
		logger.trace("INICIO::ReplicaPortafolioAPV.esReplicaAutorizacion");
		long inicio = System.currentTimeMillis();
		Boolean esReplicaAutorizacion = "AUTORIZACION".equals(entrada.p_TipoEntidad) && "I".equals(entrada.p_TipoOperacion);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ReplicaPortafolioAPV.esReplicaAutorizacion {}", fin - inicio);
		return esReplicaAutorizacion;
	}
}
