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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarFechaMovimiento implements JavaCall{
	private final Log log = new Log("MES.ModificarFechaMovimiento");
	
	private final String SQL_SELECT_MODIFICAR_MOVIMIENTO = loadResourceToString("cl/bcs/mesa/MES/sql/ModificarFechaMovimiento_SelectModificarMovimiento.sql");
	
	public static class ModificarFechaMovimiento_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaOperacionNueva;
		public List<Movimiento_Entrada> P_MOVIMIENTO;
		
		public ModificarFechaMovimiento_Entrada (){
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
	
	public static class ModificarFechaMovimiento_Salida{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long                     P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida>     P_OUT_ERRORESDETALLE;
		
		public ModificarFechaMovimiento_Salida(){
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
		ModificarFechaMovimiento_Entrada in=gson.fromJson(jin.toString(), ModificarFechaMovimiento_Entrada.class);
		ModificarFechaMovimiento_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ModificarFechaMovimiento_Salida logica(Connection conn, ModificarFechaMovimiento_Entrada in) throws Exception {
		log.write("INICIO");
		ModificarFechaMovimiento_Salida out = new ModificarFechaMovimiento_Salida();
		SQL sql = new SQL();
		HashMap<String,String> mapMsg = new HashMap<>();
		Long Id = 0L;
		Long advCont = 0L;
		Long idNumOpe = 0L;
		
		List<Long> idsOperacion = new ArrayList<Long>();
        idsOperacion.addAll(in.P_MOVIMIENTO.parallelStream().map(mov->mov.IdOperacion).collect(Collectors.toList()));
				
		if (idsOperacion.size() > 0) {			
			mapMsg.put("NumeroOperacion", String.valueOf(idNumOpe));
			out.P_OUT_MENSAJES.add(new MensajeSalida(idNumOpe,MensajeSalida.MENSAJE_AUTORIZACION,"MODIFICAR_FECHA_MOVIMIENTO",mapMsg,conn,in.p_IdEmpresa));
			Id++;
			advCont++;
		
		
		/***** AUTORIZACIONES *****/		
		List<MovimientoSpot_Proceso> movimientoSpot = null;
		HashMap<String,Object> params = new HashMap<String,Object>();
				
        params.put("idsOperacion", idsOperacion);		
		movimientoSpot = sql.query(conn, SQL_SELECT_MODIFICAR_MOVIMIENTO, params, MovimientoSpot_Proceso.class);
		if (movimientoSpot.size() <= 0) {
			String codigo = "NO_EXISTEN_MOVIMIENTOS_ASOCIADOS_A_AUTORIZACION";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		CrearEventoAutorizacionMasivo eventoAutorizacion = new CrearEventoAutorizacionMasivo();
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaEventoAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for(MovimientoSpot_Proceso mvtoSpot : movimientoSpot) {
			entradaEventoAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
			entradaEventoAutorizacion.P_IDMODULO = in.p_IdModulo;
			entradaEventoAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
			entradaEventoAutorizacion.P_SIMULAROPERACION = "N";
			entradaEventoAutorizacion.P_USAREGLASNEGOCIO = "N";
			
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = "MOFEOP";
			evento.IdModuloSolicita = in.p_IdModulo;
			evento.IdUsuarioSolicita = in.p_IdUsuario;
			evento.Fecha = in.p_FechaOperacionNueva;
			evento.IdTipoOperacion = mvtoSpot.IdTipoOperacion;
			
			if(mvtoSpot.Nemotecnico.equals("DISTRI") || mvtoSpot.Nemotecnico.equals("ARBDIS")){
				evento.IdConcepto = "LISDCP";
			}
			else if(mvtoSpot.Nemotecnico.equals("INTERB") || mvtoSpot.Nemotecnico.equals("ARBINT")){
				evento.IdConcepto = "LISICP";
			}					
			
			evento.NumeroOperacionConcepto = mvtoSpot.IdOperacionSpot.toString();
			evento.FechaOperacionConcepto = "";
			evento.IdCliente = mvtoSpot.IdCliente;
			evento.IdFuncionario = 0L;
			evento.IdProveedor = 0L;
			evento.IdTipoProducto = "";
			evento.IdClasificacionTipoInstrumento = "SP";
			evento.IdTipoInstrumento = "SPT";
			evento.IdInstrumento = mvtoSpot.IdInstrumento;
			evento.Nemo = mvtoSpot.Nemotecnico;
			evento.Cantidad = 0L;
			evento.Monto = MonedaFormato.formatear(conn, mvtoSpot.MontoMonedaPrincipal, "MONTO", mvtoSpot.IdMonedaPrincipal).valor;
			evento.Precio = mvtoSpot.Nemotecnico.equals("DISTRI") || mvtoSpot.Nemotecnico.equals("INTERB") ? mvtoSpot.TipoCambioCierre : mvtoSpot.ParidadCierre;
			evento.Tasa = BigDecimal.ZERO;
			evento.IdMoneda = mvtoSpot.IdMonedaPrincipal;
			evento.Glosa = "SPOT";
			
			entradaEventoAutorizacion.P_TABLAEVENTOS.add(evento);
		}
				
		CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = eventoAutorizacion.logica(conn, entradaEventoAutorizacion);
		for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error: salidaAut.P_OUT_ERRORESDETALLE) {
		    out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,error.Codigo,mapMsg,conn,in.p_IdEmpresa));
		    out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			Id++;
		}
       }
		
		log.write("FIN");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) { 
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1020,\"p_IdModulo\":\"MES\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaOperacionNueva\":\"2020-10-26\",\"P_MOVIMIENTO\":[{\"IdOperacion\":128253}]}";
		
		ModificarFechaMovimiento_Entrada Objin = gson.fromJson(json, ModificarFechaMovimiento_Entrada.class);
		
		ModificarFechaMovimiento  demo=new ModificarFechaMovimiento();				
		try {
			ModificarFechaMovimiento_Salida ObjOut=demo.logica(con, Objin);
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
