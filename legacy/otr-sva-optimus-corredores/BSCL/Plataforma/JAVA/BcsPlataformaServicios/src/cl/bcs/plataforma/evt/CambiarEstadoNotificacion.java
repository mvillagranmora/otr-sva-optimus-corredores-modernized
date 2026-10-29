package cl.bcs.plataforma.evt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoNotificacion implements JavaCall{
	
	private String SQL_SELECT_ORIGEN_EVENTO = loadResourceToString("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_SelectOrigenEvento.sql");
	private String SQL_UPDATE_ORIGEN_EVENTO = loadResourceToString("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_UpdateOrigenEvento.sql");
	private String SQL_UPDATE_NOTIFICACIONES = loadResourceToString("cl/bcs/plataforma/evt/sql/CambiarEstadoNotificacion_UpdateNotificacion.sql");
	
	public class EstadosCambiarEstadoNotificacion{
		public static final String VIG ="VIG";
		public static final String RZD ="RZD";
		public static final String APD ="APD";
	}
	
	private Logger log = LogManager.getLogger(CambiarEstadoNotificacion.class);
	
	public class Notificacion{
		public Long IdNotificacion;
		public String Enviar;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OrigenEvento{
		public Long IdOrigenEvento;
		public Long IdNotificacion;
		public String IdEstado;
		public String Enviar;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public class EntradaCambiarEstadoNotificacion{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<OrigenEvento> p_OrigenesEvento;
		public String p_Estado;
		
		public EntradaCambiarEstadoNotificacion() {
			p_OrigenesEvento = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaCambiarEstadoNotificacion{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaCambiarEstadoNotificacion() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCambiarEstadoNotificacion logica (Connection conn, EntradaCambiarEstadoNotificacion entrada) {
		log.info("INICIO");
		SalidaCambiarEstadoNotificacion salida = new SalidaCambiarEstadoNotificacion();
		SQL sql = new SQL();
		
		try {
			//obtener origen evento de las notificaciones
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdsOrigenEvento", entrada.p_OrigenesEvento.stream().map(ord -> ord.IdOrigenEvento).distinct().collect(Collectors.toList()));
			List<OrigenEvento> origenesEvento = sql.query(conn, SQL_SELECT_ORIGEN_EVENTO, params, OrigenEvento.class);
			params.clear();
			
			//cambiar el estado de la notificacion y origen evento segun lo enviado desde la ui. 
			entrada.p_OrigenesEvento.forEach(org ->{
				org.IdEstado = ("A".equals(entrada.p_Estado)) ? EstadosCambiarEstadoNotificacion.APD : EstadosCambiarEstadoNotificacion.RZD;
			});
			
			origenesEvento.forEach(not -> {
				not.Enviar = ("A".equals(entrada.p_Estado) ) ? "S" : "R"; 
			}); 
			
			sql.batch(conn, SQL_UPDATE_ORIGEN_EVENTO, entrada.p_OrigenesEvento, OrigenEvento.class);
			sql.batch(conn, SQL_UPDATE_NOTIFICACIONES, origenesEvento, OrigenEvento.class);
			
			Map<Long, List<OrigenEvento>> origenesAgrupados = origenesEvento.stream().collect(Collectors.groupingBy(org -> org.IdOrigenEvento));
			
			entrada.p_OrigenesEvento.forEach(org -> {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_CAMBIO_ESTADO_NOTIFICACION", String.format("OrigenEvento: %d fue %s", org.IdOrigenEvento, (EstadosCambiarEstadoNotificacion.APD.equals(org.IdEstado) ? "Aprobado" : "Rechazado") )));						
			});
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log.info("TERMINO");
		return salida;
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaCambiarEstadoNotificacion in= gson.fromJson(jin.toString() ,EntradaCambiarEstadoNotificacion.class);
		SalidaCambiarEstadoNotificacion out=logica(conn, in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa")) {
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1038,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"S\",\"p_UsaReglasNegocio\":\"S\",\"p_CobrosModificados\":[],\"p_CobrosFijos\":[] ,\"p_Ordenes\":[{\"BaseTasa\":0,\"Cantidad\":35000,\"EsFacturaInmediata\":\"N\",\"EsFungible\":\"N\",\"EsOD\":\"N\",\"EsOrdenEspecial\":\"N\",\"EspecificaBolsa\":\"S\",\"EsPorMonto\":\"N\",\"EsVentaCorta\":\"N\",\"FechaEmision\":\"0001-01-01\",\"FechaOperacion\":\"0001-01-01\",\"FechaRecepcion\":\"0001-01-01 00:00:00\",\"FechaVigencia\":\"2020-07-10\",\"Glosa\":\"\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"IdCondicionDeLiquidacion\":\"T2\",\"IdCondicionDeLiquidacionPlazo\":\"\",\"IdCorredorCorresponsal\":null,\"IdCuentaCustodioPortafolio\":null,\"IdEmisor\":\"HITES\",\"IdFormaCustodia\":\"CUS\",\"IdInstrumento\":1100,\"IdInstrumentoSubyacente\":0,\"IdInterno\":0,\"IdLibroOrdenes\":null,\"IdMecanismoCaptacion\":\"OM\",\"IdMonedaLiquidacionCobros\":\"CLP\",\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdPersonaAutorizadoDarOrdenes\":null,\"IdPorcentajeVisibilidad\":1,\"IdPortafolio\":243512,\"IdTipoCanalDistribucion\":\"RUR\",\"IdTipoCanalDistribucionReal\":\"RUR\",\"IdTipoCanalIngreso\":\"CLI\",\"IdTipoLibroOrden\":\"NORMAL\",\"IdTipoOperacion\":\"VENACN\",\"IdTipoPrecio\":\"L\",\"IdTipoReajuste\":\"\",\"IdTipoVigencia\":\"SH\",\"MontoNeto\":0,\"MontoPlazo\":0,\"PagoCobroCuentaInversion\":\"S\",\"PlazoIF\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PrecioLimite\":130,\"PrecioMercado\":0,\"TasaRFIF\":0,\"TasaSimultanea\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"TipoInteres\":\"\",\"ValorRescate\":0}]}";					
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"PLA\",\"p_Estado\":\"R\",\"p_OrigenesEvento\":[{\"IdOrigenEvento\":115},{\"IdOrigenEvento\":116}]}";
			
			
			EntradaCambiarEstadoNotificacion Objin = gson.fromJson(json, EntradaCambiarEstadoNotificacion.class);
			CambiarEstadoNotificacion demo=new CambiarEstadoNotificacion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");

			SalidaCambiarEstadoNotificacion ObjOut=demo.logica(conn, Objin);
			conn.commit();
			System.out.println(ObjOut.toString());

    	} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
