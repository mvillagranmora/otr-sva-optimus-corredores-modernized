package cl.bcs.contabilidad;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.contabilidad.CentralizaMultiSeleccion.CentralizacionesVigentes;
import cl.bcs.contabilidad.pojo.TabDatosMovimiento;
import cl.bcs.contabilidad.pojo.TabEventos;
import cl.bcs.contabilidad.pojo.TabMovimientos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaSpotUtilidadPerdida implements BcsCentralizacion{
	
	private final Log log = new Log("CON.CentralizaSpotUtilidadPerdida");
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectMovimientos.sql");
	private final String SQL_SELECT_EVENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectEventos.sql");
	private final String SQL_SELECT_DATOS_MOVIMIENTOS = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql");
	private final String SQL_CREACION_TABLA_TEMPORAL_BLOQUEO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_CreateTablaBloqueo.sql");
	private final String SQL_INSERT_TABLA_TEMPORAL = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_InsertTablaBloqueo.sql");
	private final String SQL_ELIMINACION_TABLA_TEMPORAL = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_DeleteTablaBloqueo.sql");
	private final String SQL_CALL_SP_CREACION_BLOQUEO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_Call_Create.sql");
	private final String SQL_CALL_SP_ELIMINACION_BLOQUEO = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_Call_Delete.sql");
	private final String SQL_SELECT_TABLA_BLOQUEO =  loadResourceToString("cl/bcs/contabilidad/sql/CentralizaSpotUtilidadPerdida_ConsultaTablaBloqueo.sql");
	private final String SQL_SELECT_CENTRALIZACIONES_NOMBRE = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectTipoCentralizacion.sql");
	
	public static class TabDatosMovimientoPaso{
		public Long   IdMovimiento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String Portafolio;
		public String Moneda;
//		public String EsRelacionado;
//		public String EsCuentaPropia;
		public String Producto;
		public String ClaseInstrumento;
		public String TipoInstrumento;
		public String Nemo;
		public String Boveda;
		public String IdCartera;
		public String Emisor;
		public String TipoReajuste;
		public String CodigoIFRS;
		public String Agente;
		public String NombreAgente;
		public String SucursalAgente;
		public String NombreSucursalAgente;
		public String Canal;
		public String Relacion;
		public String Bolsa;
		public String CentroCostoAgente;
		public String TipoComprobante;
		public String CompraVenta;
		public String TipoPersona;
		public String FechaOperacion;
		public String ClienteBancario;
		public String OperacionNoFacturable;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BloqueoCentralizacion{
		public String IdBloqueoCentralizacion;
	}
	public static class BloqueoCentralizacionObj{
		public String IdBloqueoCentralizacion;
		public Long IdTipoCentralizacion;
		public String FechaCentralizacion;
	}
	
	@Override
	public SalidaCentralizacion centralizar(Connection con, EntradaCentralizacion entrada, EntradaCentralizacionInfoAdicional infoAdicional) throws Exception {
		log.write("INICIO::CentralizaSpotUtilidadPerdida");
		SQL sql = new SQL();
		SalidaCentralizacion out = new SalidaCentralizacion();
		HashMap<String,Object> paramsQuery = new HashMap<String,Object>();
		String idBloqueoCentralizacion;
		List<BloqueoCentralizacionObj> centralizacionBloqueada= new ArrayList<BloqueoCentralizacionObj>();
		if(!"".equalsIgnoreCase(entrada.p_FechaContable)&&!"".equalsIgnoreCase(entrada.p_IdTipoCentralizacion.toString())) {
			idBloqueoCentralizacion = entrada.p_IdTipoCentralizacion.toString()+"_"+entrada.p_FechaContable;
			paramsQuery.put("IdBloqueoCentralizacion",idBloqueoCentralizacion);
			BloqueoCentralizacionObj id = new BloqueoCentralizacionObj();
			id.IdBloqueoCentralizacion = idBloqueoCentralizacion;
			id.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
			id.FechaCentralizacion = entrada.p_FechaContable;
			centralizacionBloqueada.add(id);
		}
		try {
			List<BloqueoCentralizacion> solicitudesBloqueadasBD = sql.query(con, SQL_SELECT_TABLA_BLOQUEO, paramsQuery, BloqueoCentralizacion.class);
			if(!solicitudesBloqueadasBD.isEmpty()) {
				/*error*/
				HashMap<String,Object> params = new HashMap<String,Object>();
				HashMap<String,String> params2 = new HashMap<String,String>();
				params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
				List<CentralizacionesVigentes> centralizacionVigentes = sql.query(con, SQL_SELECT_CENTRALIZACIONES_NOMBRE, params, CentralizacionesVigentes.class);
				params2.put("Centralizacion", centralizacionVigentes.stream().findFirst().get().Nombre);
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_CENTRALIZACION_CONTABILIZADA_ANULAR",params2,con,entrada.p_IdEmpresa));
				return out;
			} 
			else{
				log.write("INICIO::SQL_CREACION_TABLA_TEMPORAL_BLOQUEO");
				SQL.exec(con, SQL_CREACION_TABLA_TEMPORAL_BLOQUEO);
				sql.batch(con, SQL_INSERT_TABLA_TEMPORAL, centralizacionBloqueada, BloqueoCentralizacionObj.class);
				HashMap<String,Object> paramsSp = new HashMap<String,Object>();
				BloqueoCentralizacionObj aux = new BloqueoCentralizacionObj();
				aux.IdBloqueoCentralizacion = entrada.p_IdTipoCentralizacion.toString()+"_"+entrada.p_FechaContable;
				aux.FechaCentralizacion = entrada.p_FechaContable;
				aux.IdTipoCentralizacion = entrada.p_IdTipoCentralizacion;
				paramsSp.put("p_BloqueoCentralizacion", aux);
				//logger.debug("INICIO::SSQL_CALL_SP_CREACION_BLOQUEO");
				sql.call(con, SQL_CALL_SP_CREACION_BLOQUEO, paramsSp, new HashMap<String,Class>());
				log.write("FIN::SQL_CREACION_TABLA_TEMPORAL_BLOQUEO"); 
			}

			String idMonedaSistema = infoAdicional.p_IdMonedaSistema;
			
			//MOVIMIENTOS
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
			params.put("MonedaSistema", idMonedaSistema);
			out.P_TABMOVIMIENTOS = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, TabMovimientos.class);
			params.clear();
			
			//EVENTOS
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdTipoCentralizacion", entrada.p_IdTipoCentralizacion);
			params.put("MonedaSistema", idMonedaSistema);
			out.P_TABEVENTOS = sql.query(con, SQL_SELECT_EVENTOS, params, TabEventos.class);
			params.clear();
			
			//MOVIMIENTO DATOS
			params.put("FechaContable", entrada.p_FechaContable);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("MonedaSistema", idMonedaSistema);
//			params.put("FechaContable", fechaSistema);
			List<TabDatosMovimientoPaso> tabDatosMovimientoPaso = sql.query(con, SQL_SELECT_DATOS_MOVIMIENTOS, params, TabDatosMovimientoPaso.class);
			params.clear();
			
			for(TabDatosMovimientoPaso mov : tabDatosMovimientoPaso){
		
//				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdMovimiento", mov.IdMovimiento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "DocumentoIdentidad", mov.DocumentoIdentidad, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreCliente", mov.NombreCliente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Portafolio", mov.Portafolio, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Moneda", mov.Moneda, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Producto", mov.Producto, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClaseInstrumento", mov.ClaseInstrumento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoInstrumento", mov.TipoInstrumento, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Nemo", mov.Nemo, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Boveda", mov.Boveda, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "IdCartera", mov.Boveda, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Emisor", mov.Emisor, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoReajuste", mov.TipoReajuste, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CodigoIFRS", mov.CodigoIFRS, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Agente", mov.Agente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreAgente", mov.NombreAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "SucursalAgente", mov.SucursalAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "NombreSucursalAgente", mov.NombreSucursalAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Canal", mov.Canal, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Relacion", mov.Relacion, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "Bolsa", mov.Bolsa, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CentroCostoAgente", mov.CentroCostoAgente, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoComprobante", mov.TipoComprobante, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "CompraVenta", mov.CompraVenta, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "TipoPersona", mov.TipoPersona, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "FechaOperacion", mov.FechaOperacion, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "ClienteBancario", mov.ClienteBancario, BigDecimal.ZERO));
				out.P_TABDATOSMOVIMIENTO.add(new TabDatosMovimiento(entrada.p_IdTipoCentralizacion, mov.IdMovimiento, "OperacionNoFacturable", mov.OperacionNoFacturable, BigDecimal.ZERO));
			}
			
			out.P_OUT_RESULTADO = llenarResultado(out.P_TABMOVIMIENTOS);
			
			log.write("FIN::CentralizaSpotUtilidadPerdida");
		}catch(Exception e) {
			log.write("error en centralizacion spot utilidad-perdida",e);
		}finally {
			if( out.P_OUT_ERRORESCONTADOR<=0) {
				HashMap <String, Object> params = new HashMap<String, Object>();
				params.put("p_BloqueoCentralizacion", centralizacionBloqueada);
				sql.call(con, SQL_CALL_SP_ELIMINACION_BLOQUEO, params, new HashMap<String, Class>());
				SQL.exec(con, SQL_ELIMINACION_TABLA_TEMPORAL);
			}
		}
		return out;
	}	
}
