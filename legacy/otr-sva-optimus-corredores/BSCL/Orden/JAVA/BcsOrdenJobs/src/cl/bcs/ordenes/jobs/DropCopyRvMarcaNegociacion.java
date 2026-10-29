package cl.bcs.ordenes.jobs;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.Transaccion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class DropCopyRvMarcaNegociacion extends BcsJob implements JavaCall {
	
	AtomicBoolean running=new AtomicBoolean(false);
	private final Log log = new Log("ORD.DROPCOPYRV_MARCANEGOCIACION");
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/DropCopyRvMarcaNegociacion_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_ACTUALIZA_MARCA_NEGOCIACION= loadResourceToString("cl/bcs/ordenes/jobs/sql/DropCopyRvMarcaNegociacion_ActualizaMarcaNegociacion.sql");
	private final String SQL_CONSULTAR_TRANSACCION = loadResourceToString("cl/bcs/ordenes/jobs/sql/DropCopyRvMarcaNegociacion_ConsultarTransaccion.sql");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		go(conn);
		return true;
	}
	
	public static class RegistroDropCopy {
		public String IdMensaje1;
		public String IdMensaje2;
		public String ORDERID;
		public String EXECID;
		public String SIDE;
		public String ORDSTATUS;
		public String EXECTYPE;
		public String TRADEDATE;
		public String TRADEID;
		public String CLIENTID;
		public String TRANSACTTIME;
		public String ENTERINGFIRM;
		public String EXECUTINGFIRM;
		public String ENTERINGTRADER;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public void go(Connection conn) throws Exception {
		SQL sql = new SQL();
		running.set(true);
		log.write("buscando mensajes");
		List<RegistroDropCopy> registros = sql.query(conn, this.SQL_OBTENER_MENSAJES_SIN_PROCESAR, new HashMap<String,Object>(),RegistroDropCopy.class);
		
		for(RegistroDropCopy r: registros){
			
			List<ControlMercado> cmupdate = new ArrayList<ControlMercado>();
			List<Transaccion> txupdate = new ArrayList<Transaccion>();
			
			if (r.ORDSTATUS == null) {
				ControlMercado cm = new ControlMercado();
				cm.IdMensaje1 = r.IdMensaje1;
				cm.IdMensaje2 = r.IdMensaje2;
				cm.Procesado = "S";
				cm.Log = r.IdMensaje1 + " " + r.IdMensaje2 + " IGNORADO, NO SE ENCONTRO MENSAJE";
				log.write(cm.Log);
				cmupdate.add(cm);
			}
			else if (!r.ORDSTATUS.equalsIgnoreCase("2") && !r.ORDSTATUS.equalsIgnoreCase("1")) {
				ControlMercado cm = new ControlMercado();
				cm.IdMensaje1 = r.IdMensaje1;
				cm.IdMensaje2 = r.IdMensaje2;
				cm.Procesado = "S";
				cm.Log = r.IdMensaje1 + " " + r.IdMensaje2 + " IGNORADO, NO ES CALCE";
				log.write(cm.Log);
				cmupdate.add(cm);
			}
			else if ((r.ORDSTATUS.equalsIgnoreCase("2") || (r.ORDSTATUS.equalsIgnoreCase("1"))) && r.CLIENTID == null) {
				ControlMercado cm = new ControlMercado();
				cm.IdMensaje1 = r.IdMensaje1;
				cm.IdMensaje2 = r.IdMensaje2;
				cm.Procesado = "S";
				cm.Log = r.IdMensaje1 + " " + r.IdMensaje2 + " IGNORADO, NO TIENE CLIENTID";
				log.write(cm.Log);
				cmupdate.add(cm);
			}
			else if ((r.ORDSTATUS.equalsIgnoreCase("2") || (r.ORDSTATUS.equalsIgnoreCase("1"))) && r.CLIENTID != null && (r.SIDE.equalsIgnoreCase("1") || r.SIDE.equalsIgnoreCase("2")) ) {
				
				HashMap<String, Object> params = new HashMap<String,Object>();
				params.put("Folio", r.TRADEID);
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
				
				Date d1 = sdf1.parse(r.TRADEDATE);
				String FechaSistemaCreacion = sdf2.format(d1);
				params.put("FechaSistemaCreacion", FechaSistemaCreacion);
				if (r.SIDE.equalsIgnoreCase("1")) {
					params.put("AccionOrden", "COMPRA");
				}
				else if (r.SIDE.equalsIgnoreCase("2")) {
					params.put("AccionOrden", "VENTA");
				}
				else {
					//1 O 2 PARA LOS MENSAJES, cualquier otro valor es error
				}
				List<Transaccion> txs = sql.query(conn, this.SQL_CONSULTAR_TRANSACCION, params , Transaccion.class);
				
				if (txs.size() == 1) {
					ControlMercado cm = new ControlMercado();
					cm.IdMensaje1 = r.IdMensaje1;
					cm.IdMensaje2 = r.IdMensaje2;
					cm.Procesado = "S";
					cm.Log = r.IdMensaje1 + " " + r.IdMensaje2 + " TRANSACCION MODIFICADA FOLIO:" + r.TRADEID + " FECHA:" + r.TRADEDATE + " ID:" + txs.get(0).IdTransaccion;
					log.write(cm.Log);
					cmupdate.add(cm);
					
					Transaccion tx = new Transaccion();
					tx.IdTransaccion = txs.get(0).IdTransaccion;
					tx.MarcaNegociacion = r.CLIENTID;
					txupdate.add(tx);
					
				}else {
					log.write(r.IdMensaje1 + " " + r.IdMensaje2 + " TRANSACCION NO HA LLEGADO " + r.TRADEID + " " + r.TRADEDATE);
				}
				
				
			} else {
				ControlMercado cm = new ControlMercado();
				cm.IdMensaje1 = r.IdMensaje1;
				cm.IdMensaje2 = r.IdMensaje2;
				cm.Procesado = "S";
				cm.Log = r.IdMensaje1 + " " + r.IdMensaje2 + " REGISTRO IGNORADO";
				log.write(cm.Log);
				cmupdate.add(cm);
			}
			
			if (txupdate.size() > 0) sql.batch(conn, this.SQL_ACTUALIZA_MARCA_NEGOCIACION, txupdate, Transaccion.class);
			if (cmupdate.size() > 0) sql.batch(conn, this.SQL_ACTUALIZA_CONTROL_MERCADO, cmupdate, ControlMercado.class);
			conn.commit();
			
		}
		
		
		
		running.set(false);
		return;
	}
	
	@Override
    public void onError(Throwable t) {
        log.write(" ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args){
	
		
		while(true) {
		DropCopyRvMarcaNegociacion p = new DropCopyRvMarcaNegociacion();
		try {
			p.go(PlataformaUtils.getMBConnection());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	}




	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
