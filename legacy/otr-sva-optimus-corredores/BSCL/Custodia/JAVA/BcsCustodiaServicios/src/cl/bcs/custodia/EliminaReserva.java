package cl.bcs.custodia;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class EliminaReserva implements JavaCall{

	private static final Logger logger = LogManager.getLogger(IngresoReserva.class);
	
	private final String SQL_DELETE_RESERVA = loadResourceToString("cl/bcs/custodia/sql/EliminaReserva_DeleteReserva.sql");
	private final String SQL_SELECT_RESERVA = loadResourceToString("cl/bcs/custodia/sql/EliminaReserva_SelectReserva.sql");
	
	public static class ReservaObj_IN{
		public Long IdReserva;//				BIGINT
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class EliminaReserva_IN {
		public Long p_IdEmpresa;// 				BIGINT,
		public Long p_IdUsuario;// 				BIGINT,
		public String p_IdModulo;//				NVARCHAR(3),	-- CUS / MES
		public Long p_IdPais;// 				BIGINT,
		public List<ReservaObj_IN> p_TblCarteraReserva;
		
		public EliminaReserva_IN() {
			this.p_TblCarteraReserva = new ArrayList<ReservaObj_IN>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ReservaObj_OUT{
		public Long IdReserva;//			BIGINT
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class EliminaReserva_OUT {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ReservaObj_OUT>		P_OUT_RESERVA;
		
		public EliminaReserva_OUT() {
			this.P_OUT_RESERVA 		= new ArrayList<ReservaObj_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Reserva {
		public Long IdReserva;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	}
	
	
		
	
	
	public EliminaReserva_OUT logica (Connection conn, EliminaReserva_IN in) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CUS.IngresoReserva - " + init);
		
		SQL _sql = new SQL();
		Long Id = 0L;
		EliminaReserva_OUT out = new EliminaReserva_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		HashMap<String, Object> params = new HashMap<>();
			
		List<Reserva> datosReservaDelete = new ArrayList<>();
		params.put("IdReserva", in.p_TblCarteraReserva);
		for (ReservaObj_IN resIn: in.p_TblCarteraReserva){
			params.put("IdReserva", resIn.IdReserva);
		}
		List<Reserva> datosReservaId = _sql.query(conn, SQL_SELECT_RESERVA, params, Reserva.class);
		HashMap<Long, Object> datosReservaIdHash = Utils.List2Hash(datosReservaId, "IdReserva");
		//::Validaciones
		for (ReservaObj_IN resIn: in.p_TblCarteraReserva){
			Reserva tblReserva = (Reserva)datosReservaIdHash.get(resIn.IdReserva);
			if (resIn.IdReserva.equals(0L)){ //son nominales
				break;
			}				
			if (!resIn.IdReserva.equals(0L) && tblReserva == null){
				//no existe la reserva
				mapMsg.put("IdReserva", resIn.IdReserva.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_RESERVA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				continue;
			}
			
			Reserva rs = new Reserva();
			rs = copyPojoEntradaReserva(resIn);
			rs.IdReserva = resIn.IdReserva;
			datosReservaDelete.add(rs);
		}
		
		for (Reserva rs: datosReservaDelete){
				mapMsg.put("IdReserva", rs.IdReserva.toString());
				ReservaObj_OUT re = new ReservaObj_OUT();
				re.IdReserva = rs.IdReserva;
				out.P_OUT_RESERVA.add(re);
		}
			
	
		_sql.batch(conn, SQL_DELETE_RESERVA, datosReservaDelete, Reserva.class);
			
		logger.info("Termino del servicioCUS.IngresoReservaa, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init)/1000 + " seg");
		
		return out;
	};
	
	
	private Reserva copyPojoEntradaReserva(ReservaObj_IN entrada) {
		Reserva salida = new Reserva();
		salida.IdReserva = entrada.IdReserva;
		return salida;
	}
	
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EliminaReserva_IN jin = gson.fromJson(in.toString(), EliminaReserva_IN.class);
		EliminaReserva_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 1645,\r\n"
					+ "    \"p_IdModulo\": \"MES\",\r\n"
					+ "    \"p_IdPais\": 44,\r\n"
					+ "    \"p_TblCarteraReserva\": [\r\n"
					+ "        {\r\n"
					+ "            \"IdReserva\": 8037\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			EliminaReserva_IN objIn = gson.fromJson(json,EliminaReserva_IN.class);
			EliminaReserva demo = new EliminaReserva();
			
			EliminaReserva_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
