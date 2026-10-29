package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pau.AgregarControlProceso.ControlProcesoEntrada;
import cl.bcs.plataforma.pau.AgregarControlProceso.ControlProcesoSalida;


public class ConfirmarRechazarCuadraturaFacturacion implements JavaCall, BcsAutorizaciones {

	private Logger logger = LogManager.getLogger(ConfirmarRechazarCuadraturaFacturacion.class);
	
	private final String SQL_SELECT_USUARIO = "SELECT \"CodigoUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = :IdUsuario";
    private final String SQL_SELECT_DETALLE_EVENTO = loadResourceToString("cl/bcs/plataforma/pau/sql/ConfirmarRechazarCuadraturaFacturacion_SelectDetalleEvento.sql");

	public static class Usuario extends BcsPojo {
		public String CodigoUsuario;
		
		public Usuario() {
			this.CodigoUsuario = "";
		}
	}
	
    public static class DetalleEvento extends BcsPojo {
        public Long IdEventoDetalle;
        public Long IdEvento;
        public String NombreCampo;
        public String Valor;
    }
    
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
    	logger.info("INICIO:AUTORIZACION");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		

        List<Long> idsEventos = entrada.p_Lista.stream().map(e -> e.IdEvento).distinct().collect(Collectors.toList());
        
        
        try {
        	
            List<DetalleEvento> detalles = obtenerDetallesEventos(conn, idsEventos);
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			for(DetalleEvento detalle : detalles) {
	    		ControlProcesoEntrada controlProcesoEntrada = new ControlProcesoEntrada();
	    		controlProcesoEntrada.IdEmpresa = entrada.p_IdEmpresa;
	    		controlProcesoEntrada.CodigoModulo = "FAC";
	    		controlProcesoEntrada.CodigoProceso = "CUADRATURA_FACTURACION";
	    		controlProcesoEntrada.FechaSistema = fechaSistema;
	    		controlProcesoEntrada.CodigoUsuario = this.obtenerUsuario(conn, entrada.p_IdUsuario).CodigoUsuario;
	    		controlProcesoEntrada.ResultadoProceso = detalle.Valor;
	    		controlProcesoEntrada.p_UsaReglasNegocio = "N";
	    		
	        	AgregarControlProceso agregarControlProceso = new AgregarControlProceso();
	    		ControlProcesoSalida controlProcesoSalida = agregarControlProceso.logica(conn, controlProcesoEntrada);
	    		
	    		salida.P_OUT_ERRORESCONTADOR += controlProcesoSalida.P_OUT_ERRORESCONTADOR;
	    		salida.P_OUT_MENSAJES.addAll(controlProcesoSalida.P_OUT_MENSAJES);
			}        	
        	
        } catch (Exception ignored) {
            salida.P_OUT_ERRORESCONTADOR += 1L;
        }
        	
		logger.info("TERMINO:AUTORIZACION");
		return salida;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
    	logger.info("INICIO:RECHAZO");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		
		agregarLog(conn, entrada, "CUADRATURA_FACTURACION", "FAC");

		logger.info("TERMINO:RECHAZO");
		return salida;
	}
	
	private Usuario obtenerUsuario(Connection con, Long idUsuario) throws Exception {
		logger.trace("INICIO::ConfirmarRechazarCuadraturaFacturacion.obtenerUsuario");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdUsuario", idUsuario);
		
		SQL sql = new SQL();
		List<Usuario> usuarios = sql.query(con, SQL_SELECT_USUARIO, parametros, Usuario.class);
		
		Usuario usuario = usuarios.stream().findFirst().orElse(new Usuario());
		
		logger.trace("FIN::ConfirmarRechazarCuadraturaFacturacion.obtenerUsuario");
		return usuario;
	}
	
    private List<DetalleEvento> obtenerDetallesEventos(Connection conn, List<Long> idsEventos) throws SQLException {
        logger.debug("INICIO::ConfirmarRechazarCuadraturaFacturacion.obtenerDetallesEventos");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdsEventos", idsEventos);

        SQL sql = new SQL();
        List<DetalleEvento> detalles = sql.query(conn, SQL_SELECT_DETALLE_EVENTO, params, DetalleEvento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ConfirmarRechazarCuadraturaFacturacion.obtenerDetallesEventos", fin - inicio);

        return detalles;
    }
    
	public void agregarLog(Connection con, EntradaAutorizacion in, String codigoProceso, String codigoModulo) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			for(Evento evento : in.p_Lista) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				entradaAgregarLog.p_IdEmpresa = in.p_IdEmpresa;
				entradaAgregarLog.p_IdModulo= codigoModulo;
				entradaAgregarLog.p_IdUsuario = 0L;
				log.IdCabeceraLog = in.p_IdUsuario;
				log.IdModulo = codigoModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = 0L;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "RECHAZO CUADRATURA";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "ING";
				log.IdPersona = 0L;
				log.IdTipoLog = "INFORMATIVO";
				log.NombreProceso = "";
				
				entradaAgregarLog.p_Log.add(log);
			}

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	
    public static void main(String[] args) throws Throwable {
        System.out.println("== INICIO TEST ==");

		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
            EntradaAutorizacion entradaAutorizacion = new EntradaAutorizacion();
            entradaAutorizacion.p_IdEmpresa = 1L;
            entradaAutorizacion.p_IdModulo = "AUT";
            entradaAutorizacion.p_IdUsuario = 97L;
            entradaAutorizacion.p_UsaReglasNegocio = "N";

            Evento evento1 = new Evento();
            evento1.IdEvento = 225495L;

            Evento evento2 = new Evento();
            evento2.IdEvento = 225498L;
            entradaAutorizacion.p_Lista = Arrays.asList(evento1, evento2);
            System.out.println("ENTRADA");
            System.out.println(entradaAutorizacion);

            ConfirmarRechazarCuadraturaFacturacion confirmarRechazarCuadraturaFacturacion = new ConfirmarRechazarCuadraturaFacturacion();
            SalidaAutorizacion salidaAutorizacion = confirmarRechazarCuadraturaFacturacion.rechazarEvento(con, entradaAutorizacion);
            System.out.println(salidaAutorizacion);

            con.rollback();

        } catch (ConfigurationException e) {
			e.printStackTrace();
		}

        System.out.println("== FIN TEST ==");
    }
}