package cl.bcs.plataforma.ccl;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ccl.BcsConciliacion.DetalleConciliacion;
import cl.bcs.plataforma.ccl.BcsConciliacion.SalidaConciliacion;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ElConciliador implements JavaCall {

	public static class ElConciliadorEntrada extends BcsPojo {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
	}
	
	public static class ElConciliadorSalida extends BcsPojo {

		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
		public String P_OUT_SALIDA = "";
		public List<SalidaConciliacion> P_OUT_DETALLE = new ArrayList<>();
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ElConciliadorEntrada in = gson.fromJson(jin.toString(), ElConciliadorEntrada.class);
		ElConciliadorSalida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;

	}

	public final List<Class> conciliaciones = Arrays.asList(new Class[]{
			ConciliacionFacturacionCuentaInversion.class,
			ConcilicacionFFMMCustodia.class,
			ConciliacionFFMMCuentaInversion.class,
			ConciliacionFFMMMovimientoSpot.class
	});
	
	private String generarReporte(SalidaConciliacion s) throws JSONException, JsonProcessingException {
		StringBuilder b = new StringBuilder();
		b.append("CONCILIACION:\t" + s.nombreConciliacion + "\n");
		b.append("ESTADO:\t" + s.estadoConciliacion + "\n");
		
		for (DetalleConciliacion res : s.detalleConciliacion) {
			b.append("\tDETALLE:\t\t\t" + res.nombreDetalle + "\n");
			b.append("\tESTADO DETALLE:\t\t" + res.estadoDetalle + "\n");
			b.append("\tMENSAJE DETALLE:\t" + res.mensajeDetalle + "\n");
			b.append("\n");
			//b.append(Utils.toCSVString(res.detalleMovimientos));
			
			for (Object mov : res.detalleMovimientos) {
				b.append("\t\t" + mov.toString() + "\n");	
			}
			b.append("\n");
		}
	
		b.append("\n");
		
		
		
		return b.toString();
	}
	
	private ElConciliadorSalida logica(Connection conn,ElConciliadorEntrada in) throws Exception {
		
		ElConciliadorSalida salida = new ElConciliadorSalida();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		
		for (Class clazz : conciliaciones) {
			BcsConciliacion conciliacion = (BcsConciliacion) clazz.newInstance(); 
			SalidaConciliacion salidaConciliacion = conciliacion.conciliar(conn);
			salida.P_OUT_DETALLE.add(salidaConciliacion);
			salida.P_OUT_SALIDA+=generarReporte(salidaConciliacion);
		}
		
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getSURAConnection()) {
			ElConciliador c = new ElConciliador();
			ElConciliadorEntrada in = new ElConciliadorEntrada();
			in.p_IdEmpresa = 1L;
			in.p_IdModulo = "PLA";
			in.p_IdUsuario = 0L;
			ElConciliadorSalida asdf = c.logica(conn,in);
			System.out.println(asdf.P_OUT_SALIDA);
	    }
		 	
	}
}
