package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Entrada;
import cl.bcs.ordenes.RefacturarMovimiento.RefacturarMovimiento_Salida;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarComprobantesRefacturacion implements JavaCall {
	
	private final String SQL_SELECT_COMPROBANTES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarComprobantesRefacturacion_SelectComprobantes.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		ConsultarComprobantesRefacturacion_Entrada in=gson.fromJson(jin.toString() ,ConsultarComprobantesRefacturacion_Entrada.class);
		ConsultarComprobantesRefacturacion_Salida out=logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		for (MensajeSalida msje : out.P_OUT_MENSAJES) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarComprobantesRefacturacion_Salida logica(Connection conn,ConsultarComprobantesRefacturacion_Entrada entrada) throws Exception {
		SQL _sql = new SQL();
		ConsultarComprobantesRefacturacion_Salida salida = new ConsultarComprobantesRefacturacion_Salida();
		
		ArrayList<Long> _comprobantes = new ArrayList<>();
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("fechaComprobante", entrada.p_FechaConsulta);
		List<String> tiposComprobanteRefacturable = new ArrayList<>();
		tiposComprobanteRefacturable.addAll(Arrays.asList("FAE","POL"));
		params.put("idsTipoComprobante", tiposComprobanteRefacturable);
		List<MovimientoComprobante_Proceso> movimientos = _sql.query(conn, SQL_SELECT_COMPROBANTES, params, MovimientoComprobante_Proceso.class);
		
		
		for (MovimientoComprobante_Proceso mvto : movimientos) {
			
			if (!_comprobantes.contains(mvto.IdComprobante)) {
				
				Comprobante_Proceso cmp = new Comprobante_Proceso();
				Utils.copyPojo(mvto, cmp);
				salida.P_OUT_COMPROBANTESFACTURACION.add(cmp);
				_comprobantes.add(mvto.IdComprobante);
				
			}
			
			salida.P_OUT_MOVIMIENTOS.add(mvto);
			
		}
		
		
		return salida ;
	}
	
	public static class Comprobante_Proceso {
		public Long IdComprobante;
		public String IdEstadoComprobante;
		public String IdTipoComprobante;
		public Long IdComprobanteReferencia;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdCondicionDeLiquidacion;
		public java.sql.Date FechaLiquidacion;
		public String NombreEstadoComprobante;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String CodigoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante_Proceso {
		public Long IdComprobante;
		public String IdEstadoComprobante;
		public String IdTipoComprobante;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdCondicionDeLiquidacion;
		public java.sql.Date FechaLiquidacion;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String CodigoPortafolio;
		public java.sql.Timestamp FechaTransaccion;
		public String FolioTransaccion;
		public Long IdTransaccion;
		public String Instrumento;
		public String InstrumentoSubyacente;
		public String IdTipoInstrumento;
		public BigDecimal Cantidad;
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;
	}
	
	//ENTRADAS
	public static class ConsultarComprobantesRefacturacion_Entrada {
		public Long 	p_IdEmpresa;
		public Long		p_IdUsuario;
		public String 	p_IdModulo;
		public String	p_FechaConsulta;
		
		public ConsultarComprobantesRefacturacion_Entrada() {
		
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	//SALIDAS
	public static class ConsultarComprobantesRefacturacion_Salida {

		public Long 								P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 					P_OUT_MENSAJES;
		public List<Comprobante_Proceso>			P_OUT_COMPROBANTESFACTURACION;		//COMPROBANTES REFACTURABLES
		
		public List<MovimientoComprobante_Proceso> 	P_OUT_MOVIMIENTOS;
		
		
		public ConsultarComprobantesRefacturacion_Salida() {
			this.P_OUT_COMPROBANTESFACTURACION = new ArrayList<>();
			this.P_OUT_MOVIMIENTOS = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static void main(String[]args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"ORD\",\"p_Origen\":\"COMP\",\"p_Entidades\":[{\"IdEntidad\":751258}]}";
			ConsultarComprobantesRefacturacion_Entrada Objin = gson.fromJson(json, ConsultarComprobantesRefacturacion_Entrada.class);
			
			ConsultarComprobantesRefacturacion demo=new ConsultarComprobantesRefacturacion();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			ConsultarComprobantesRefacturacion_Salida ObjOut=demo.logica(con, Objin);
 			System.out.println(ObjOut.toString());
		}
		 	
		System.out.println("== END ==");
		
		
		
		
	}	
	
}
	