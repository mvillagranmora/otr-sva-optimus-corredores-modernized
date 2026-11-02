// GENERATED DOUBLE of the legacy cl.bcs.fondomutuo.NotificarEstadoOrdenFondoMutuo by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class NotificarEstadoOrdenFondoMutuo implements JavaCall{
	
	public static class EntradaNotificarEstadoOrdenFondoMutuo{
		
		public Long p_IdEmpresa;		
		public Long p_IdUsuario;		
		public String p_IdModulo;
		public List<Long> p_Solicitudes;
		
		public EntradaNotificarEstadoOrdenFondoMutuo(){
			this.p_Solicitudes = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudNotificar{
		
		public Long FOLIO;
		public Long IdCliente;
		public Long IDCLIENTEPERSONA;
		public String NEMO;
		public String RUT;
		public String CLIENTE;
		public Long IdPortafolioCliente;
		public String CUENTA;
		public Long IdEmpleadoDigitador;
		public String AGENTE;
		public Long IDEMPLEADOPERSONA;
		public String CORREO;
		public String TELEFONO;
		public String FECHA;
		public String HORA;
		public Timestamp FECHA_EVENTO;
		public String PRODUCTO;
		public String TIPO_OPERACION;
		public String CANTIDAD;
		public String PRECIO;
		public String MONTO;
		public String MONEDA;
		public String ESTADO;
		public String CORREDORA;
		public String ATENCIONCLIENTE;
		public String CONDICIONLIQUIDACION;
		public String TIPOPRECIOORDEN;
		public String FECHAVIGENCIA;
		public Long TASA;
		public Long PLAZOVENCIMIENTO;
		public String MONTOPRIMA;
		public String VENTACORTA;
		public String COMISION;
		public String IMPUESTO;
		public String GASTO;
		public String DERECHO;
		public Long IdDireccionEvento;
		public String TipoDestinatarioEvento;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaNotificarEstadoOrdenFondoMutuo{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaNotificarEstadoOrdenFondoMutuo(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenFondoMutuo", "call", new Object[]{conn, in}, JsonObject.class); }
	
	public void logicaAsync(Connection con,EntradaNotificarEstadoOrdenFondoMutuo entrada) throws Exception { cl.bcs.oracle.T5Notif.parametros(con, entrada.p_IdEmpresa); cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenFondoMutuo", "logicaAsync", new Object[]{con, entrada}, Void.class); }
	
	public SalidaNotificarEstadoOrdenFondoMutuo logica(Connection con, EntradaNotificarEstadoOrdenFondoMutuo entrada) throws Exception{ return cl.bcs.oracle.Doubles.answer("NotificarEstadoOrdenFondoMutuo", "logica", new Object[]{con, entrada}, SalidaNotificarEstadoOrdenFondoMutuo.class); }
	
}
