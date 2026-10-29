package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarFechaMovimiento implements JavaCall{
	private final Log log = new Log("MES.ConfirmarModificarFechaMovimiento");
	
	private final String SQL_UPDATE_MODIFICAR_FECHA_MOVIMIENTO = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarModificarFechaMovimiento_UpdateConfirmarModificarFechaMovimiento.sql");
	private final String SQL_UPDATE_MODIFICAR_FECHA_COMPROBANTE_PAGO = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarModificarFechaMovimiento_UpdateModificarFechaComprobantePago.sql");
	//private final String SQL_SELECT_MODIFICAR_MOVIMIENTO = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarModificarFechaMovimiento_SelectModificarMovimiento.sql");
	
	public static class ConfirmarModificarFechaMovimiento_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaOperacionNueva;
		public List<Movimiento_Entrada> P_MOVIMIENTO;
		
		public ConfirmarModificarFechaMovimiento_Entrada (){
			P_MOVIMIENTO	= new ArrayList<Movimiento_Entrada>();
		 }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoSpot_Proceso {
		
		public Long   IdOperacionSpot;
		public String IdTipoOperacion;
		public Long   IdCliente;
		public Long   IdInstrumento;
		public String Nemotecnico;
		public BigDecimal MontoMonedaPrincipal;
		public String IdMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento_Entrada {		
		public Long   IdOperacion;
		public String FechaOperacionTimeStamp = "0001-01-01 00:00:00";
		public String FechaOperacionNueva;
		public Long   IdUsuarioModifica;
		public String FechaModifica;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
		
	
	public static class ConfirmarModificarFechaMovimiento_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long                     P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida>     P_OUT_ERRORESDETALLE;
		
		public ConfirmarModificarFechaMovimiento_Salida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ConfirmarModificarFechaMovimiento_Entrada in=gson.fromJson(jin.toString(), ConfirmarModificarFechaMovimiento_Entrada.class);
		ConfirmarModificarFechaMovimiento_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConfirmarModificarFechaMovimiento_Salida logica(Connection conn, ConfirmarModificarFechaMovimiento_Entrada in) throws Exception {
		log.write("INICIO");
		ConfirmarModificarFechaMovimiento_Salida out = new ConfirmarModificarFechaMovimiento_Salida();
		SQL sql = new SQL();
		//HashMap<String,String> mapMsg = new HashMap<>();
		//Long Id = 0L;
		//Long advCont = 0L;
		//Long idNumOpe = 0L;
				
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Movimiento_Entrada> updateOperacion = new ArrayList<Movimiento_Entrada>();
		
		//Para fecha con hora 03:00
		int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(conn, in.p_IdEmpresa);
        Fecha.setLocalOffSet(segundosZonaHoraria);
		
		for(Movimiento_Entrada opEn : in.P_MOVIMIENTO){
			Movimiento_Entrada op = new Movimiento_Entrada();
			op.IdOperacion = opEn.IdOperacion;			
			op.FechaOperacionTimeStamp = Fecha.toStringDatetime(Fecha.fechaSistemaHoraLocal_fechaHoraUTC(in.p_FechaOperacionNueva,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa)));
			op.FechaOperacionNueva = in.p_FechaOperacionNueva;
			op.IdUsuarioModifica = in.p_IdUsuario;
			op.FechaModifica  = Fecha.toStringDatetime(Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(conn,in.p_IdEmpresa)));
			
			updateOperacion.add(op);
		}
		
		if (updateOperacion.size() > 0) {			
			sql.batch(conn, SQL_UPDATE_MODIFICAR_FECHA_MOVIMIENTO, updateOperacion, Movimiento_Entrada.class);
			sql.batch(conn, SQL_UPDATE_MODIFICAR_FECHA_COMPROBANTE_PAGO, updateOperacion, Movimiento_Entrada.class);
		}
		
		log.write("FIN");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1020,\"p_IdModulo\":\"MES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaOperacionNueva\":\"2020-10-21\",\"P_MOVIMIENTO\":[{\"IdOperacion\":\"128204\"}]}";
		
		ConfirmarModificarFechaMovimiento_Entrada Objin = gson.fromJson(json, ConfirmarModificarFechaMovimiento_Entrada.class);
		
		ConfirmarModificarFechaMovimiento demo=new ConfirmarModificarFechaMovimiento();				
		try {
			ConfirmarModificarFechaMovimiento_Salida ObjOut=demo.logica(con, Objin);
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 
			//con.commit();
    	}
		System.out.println("== END ==");
	}
	
}
