package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPuntasSpot implements JavaCall {
	private final String SQL_CONSULTAR_PUNTAS = "SELECT 1 FROM \"BSCL.Mesa.BD::SPT.Puntas\"\r\n"
			+ " WHERE \"IdMonedaPunta\" = :IdMoneda\r\n"
			+ " AND \"Estado\" = 'VIG';";
	private final String SQL_INSERT_AVISO_WEBSOCKET = "INSERT INTO \"BSCL.Plataforma.BD::PLA.MensajeWebsocket\"\r\n"
			+ " (\"IdEmpresa\",\"FechaRegistro\",\"Evento\",\"DATA\",\"EsProcesado\")\r\n"
			+ " VALUES(:idEmpresa,CURRENT_UTCTIMESTAMP,:evento,:data, :esProcesado);";
	
	private final String SQL_INSERT_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_InsertPuntas.sql");
	private final String SQL_UPDATE_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_UpdatePuntas.sql");
	private final String SQL_UPDATE_PUNTASXTRAMO = loadResourceToString("cl/bcs/mesa/SPT/sql/CrearPuntasSpot_UpdatePuntasPorTramo.sql");
	
	public static class PuntasEntrada {
		public Long p_IdEmpresa;
		public Timestamp p_fecha;
		public String p_IdMoneda;
		public List<Puntas> p_DatosPuntas;
		public List<PuntasTramo> p_DatosPuntasTramo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntasSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long	P_OUT_ADVERTENCIASCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Puntas {
		public Long IdPuntas;
		public String IdMonedaPunta;  						
		public String IdUsuario;
		public BigDecimal AperturaPrecio;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String CheckPrecioPorTramo;
		public String Estado;
		public Long IdEmpresa;
		public Timestamp Fecha;
		public String Origen = "MANUAL";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntasTramo {
	    public long IdPuntasPorTramo;
	    public long IdPuntas;
	    public BigDecimal MontoPorTramo;
	    public BigDecimal PorcentajeTramo;
	    public BigDecimal MontoCentavos;
	    public BigDecimal ValorCompra;
	    public BigDecimal ValorVenta;
	    public String Estado;
	    public String IdUsuario;
	    public Timestamp FechaModificacion;
	    public String Canal;
	    public Long IdEmpresa;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajeSocketIns {
		public String evento;
		public String data;
		public String esProcesado;
		public Long idEmpresa;
		
		public MensajeSocketIns(Long idEmpresa, String evento, String data, String esProcesado) {
			this.idEmpresa = idEmpresa;
			this.evento = evento;
			this.data = data;
			this.esProcesado = esProcesado;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public PuntasSalida logica(Connection conn, PuntasEntrada in) throws Exception {
		PuntasSalida out = new PuntasSalida();
		try {
			SQL _sql = new SQL();
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdMoneda", in.p_IdMoneda);
			
			Long totalPuntas = _sql.countQuery(conn, SQL_CONSULTAR_PUNTAS, params);
			
			for (Puntas p : in.p_DatosPuntas) {
				p.IdEmpresa = in.p_IdEmpresa;
				p.Fecha = in.p_fecha;
				p.Estado = "VIG";
				
				if (!totalPuntas.equals(0L)) {
					in.p_DatosPuntasTramo = in.p_DatosPuntasTramo.stream()
						.filter((PuntasTramo ptt) -> {
							return p.IdPuntas.equals(ptt.IdPuntas);
						})
						.map((PuntasTramo ptt) -> {
							if (p.IdPuntas.equals(ptt.IdPuntas)) {
								ptt.ValorCompra = p.PuntaCompra.subtract(ptt.MontoCentavos);
								ptt.ValorVenta = p.PuntaVenta.add(ptt.MontoCentavos);
								ptt.FechaModificacion = in.p_fecha;
								ptt.IdEmpresa = in.p_IdEmpresa;
							}
							return ptt;
						}).collect(Collectors.toList());
				}
			}
			
			if (totalPuntas.equals(0L)) {
				_sql.batch(conn, SQL_INSERT_PUNTAS, in.p_DatosPuntas, Puntas.class);
			} else {
				_sql.batch(conn, SQL_UPDATE_PUNTAS, in.p_DatosPuntas, Puntas.class);
				_sql.batch(conn, SQL_UPDATE_PUNTASXTRAMO, in.p_DatosPuntasTramo, PuntasTramo.class);
			}
			
			List<MensajeSocketIns> msgWSocket =  new ArrayList<>();
			msgWSocket.add(new MensajeSocketIns(in.p_IdEmpresa, "ACTPTA", "", "N"));
			msgWSocket.add(new MensajeSocketIns(in.p_IdEmpresa, "AVISO", "Puntas Actualizadas", "N"));
			
			_sql.batch(conn, SQL_INSERT_AVISO_WEBSOCKET, msgWSocket, MensajeSocketIns.class);
			
			out.P_OUT_ERRORESCONTADOR = 0L;
			out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			throw e;
		}
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		PuntasEntrada in = gson.fromJson(jin.toString(), PuntasEntrada.class);
		PuntasSalida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_fecha\":\"2023-04-25 16:31:42\",\"p_IdMoneda\":\"PEN\",\"p_DatosPuntas\":[{\"IdPuntas\":172,\"AperturaPrecio\":1,\"IdUsuario\":9,\"PuntaCompra\":1.99999998,\"PuntaVenta\":2.00999998,\"IdMonedaPunta\":\"PEN\",\"CheckPrecioPorTramo\":\"NO\",\"Estado\":\"VIG\"}],\"p_DatosPuntasTramo\":[{\"IdPuntasPorTramo\":2914,\"IdPuntas\":172,\"MontoPorTramo\":700,\"PorcentajeTramo\":0,\"MontoCentavos\":0,\"ValorCompra\":0,\"ValorVenta\":0,\"Estado\":\"VIG\",\"IdUsuario\":9,\"FechaModificacion\":\"2023-04-25\",\"Canal\":\"Punta\"},{\"IdPuntasPorTramo\":2915,\"IdPuntas\":172,\"MontoPorTramo\":500,\"PorcentajeTramo\":0,\"MontoCentavos\":500,\"ValorCompra\":0,\"ValorVenta\":0,\"Estado\":\"VIG\",\"IdUsuario\":9,\"FechaModificacion\":\"2023-04-25\",\"Canal\":\"Punta\"}]}";
			PuntasEntrada Objin = gson.fromJson(jsonDesa, PuntasEntrada.class);
			
			CrearPuntasSpot demo = new CrearPuntasSpot();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				PuntasSalida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}
}
