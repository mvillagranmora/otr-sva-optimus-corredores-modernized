package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearComprobantePagoOperacion implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearComprobantePagoOperacion.class);
	
	private final String SQL_INSERT_COMPROBANTE_PAGO = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_InsertComprobantePago.sql");
	//private final String SQL_DELETE_COMPROBANTE_PAGO = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_DeleteComprobantePago.sql");
	private final String SQL_DELETE_COMPROBANTE_PAGO_OPERACION = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_DeleteComprobantePagoOperacion.sql");
	private final String SQL_UPDATE_COMPROBANTE_PAGO = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_UpdateComprobantePago.sql");
	private final String SQL_SELECT_COMPROBANTE_PAGO = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectComprobantePago.sql");
	//private final String SQL_SELECT_COMPROBANTE_PAGO_NOT_IN = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectComprobantePagoNotIn.sql");
	private final String SQL_SELECT_COMPROBANTE_PAGO_SALIDA = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectComprobantePagoSalida.sql");
	private final String SQL_UPDATE_COMPROBANTE_MONTO = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_UpdateMontoComprobante.sql");
	private final String SQL_SELECT_DATOS_CUENTA_CLIENTE= loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectDatosCuentas.sql");
	private final String SQL_SELECT_DATOS_CUENTA_CORREDOR = loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectDatosCuentasCorredor.sql");
	private final String SQL_SELECT_MEDIOS_PAGO =loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectMediosDePago.sql");
	private final String SQL_SELECT_DATOS_OPERACION =loadResourceToString("cl/bcs/mesa/sql/CrearComprobantePagoOperacion_SelectDatosOperacion.sql");
	

	
	
	public static class ComprobantePagoOperacionObj_IN extends BcsPojo{
		public Long IdInternoPagoOperacion;//						BIGINT
		public Long IdComprobantePagoOperacion;//" 					BIGINT
		public Long IdEmpresa;//"									BIGINT
		public Long IdOperacion;//"									BIGINT
		public Long IdIngresoEgreso;//"								BIGINT
		public Long IdAbono;//"										BIGINT
		public Long IdCargo;//"										BIGINT
		public Long IdIngresoEgresoAnticipo;//"                     BIGINT
		public Long IdAbonoAnticipo;//"                             BIGINT
		public Long IdCargoAnticipo;//"                             BIGINT
		public String IdTipoProducto;//" 								NVARCHAR(4)
		public String RecibePagaPortafolio;//"						NVARCHAR(1)
		public String IdFormaDePago;//" 								NVARCHAR(40)
		public String IdModalidadPago;//" 							NVARCHAR(6)
		public String IdMoneda;//" 									NVARCHAR(3)
		//public String IdMonedaLiquidacion;//"							NVARCHAR(3) /**se elimina por multimoneda: ahora se guarda en moneda de liquidacoion***/
		public BigDecimal Monto;//" 										DECIMAL(26,8)
		//public BigDecimal MontoLiquidacion;//"							DECIMAL(26,8) /**se elimina por multimoneda: ahora se guarda en moneda de liquidacoion***/
		public String FechaIngresoComprobantePagoOperacion;//" 		DATE
		public String FechaLiquidacionComprobantePagoOperacion;//"	DATE
		public String IdEstado;//" 									NVARCHAR(3)
		public String AfectarTesoreria;//"                            NVARCHAR(1)
		public Long IdCuentaBanco;
		public String IdTipoOperacion = "";
	}
	
	public static class ComprobantePagoOperacionObjDel_IN extends BcsPojo{
		public Long IdEmpresa;//"									BIGINT
		public Long IdOperacion;//"									BIGINT
				
	}
	
	public static class ComprobantePagoOperacionObj_OUT extends BcsPojo{
		public Long IdInternoPagoOperacion;//"			BIGINT
		public Long IdOperacion;//"						BIGINT
		public Long IdComprobantePagoOperacion;//" 		BIGINT
		public String IdFormaDePago;//" 					NVARCHAR(40)
			
	}
	
	public static class FormaDePagoObj_OUT extends BcsPojo {


		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public String IdTipoProducto;
		public String IdFormaDePago;
		public String RecibePaga;
		public String RecibePagaDesc;
		public String IdModalidadPago;
		public String IdMonedaComprobantePago;	
		public BigDecimal Monto;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public Long IdAbono;
		public Long IdCargo;
		public String NombreFormaDePago;
		public String NombreMedioDePago;
		public String IdMoneda;
		public String UsaCuentaBancaria;
		public String UsaTarjetaBancaria;
		//public String IdMonedaLiquidacion;
		//public BigDecimal MontoLiquidacion;
		public String AfectarTesoreria;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		public Long IdCuentaBancaria;		//	NVARCHAR(40)
		public Long IdBanco;
		public String NombreBancoCuentaBancaria;
		public String NumeroCuentaBancaria;

	}
	
	public static class CrearComprobantePagoOperacion_IN extends BcsPojo{	
		public Long p_IdEmpresa;//			BIGINT,			--ID Empresa destino
		public Long p_IdUsuario;//			BIGINT,			--ID del usuario que solicita el servicio
		public String p_IdModulo;//			NVARCHAR(3),  	--ID del Modulo que lo invoca
		public String p_UsaReglasNegocio;//	NVARCHAR(1)
		public String p_FormaPagoNueva;
		public String p_ActualizaFormaPago;
		public List<ComprobantePagoOperacionObj_IN> p_ComprobantePagoOperacion;
		
		public CrearComprobantePagoOperacion_IN() {
			this.p_ComprobantePagoOperacion = new ArrayList<ComprobantePagoOperacionObj_IN>();
			this.p_FormaPagoNueva = "N";
			this.p_ActualizaFormaPago = "N";
		}
	}
	
	public static class CrearComprobantePagoOperacion_OUT extends BcsPojo{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ComprobantePagoOperacionObj_OUT>	P_OUT_COMPRABANTEPAGO;
		public List<FormaDePagoObj_OUT>	P_OUT_FORMADEPAGO;
		
		public CrearComprobantePagoOperacion_OUT() {
			this.P_OUT_COMPRABANTEPAGO 		= new ArrayList<ComprobantePagoOperacionObj_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_FORMADEPAGO 		= new ArrayList<FormaDePagoObj_OUT>();

		}
		
	}
	
	public static class ComprobantePagoOperacion extends BcsPojo {
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public String IdTipoProducto;
		public String IdFormaDePago;
		public String RecibePaga;
		public String IdModalidadPago;
		public String IdMoneda;	
		public BigDecimal Monto;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public Long IdAbono;
		public Long IdCargo;
		//public String IdMonedaLiquidacion;
		//public BigDecimal MontoLiquidacion;
		public String AfectarTesoreria;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		public Long IdCuentaBanco;
				
	}

	
	public static class DatoCuenta extends BcsPojo{
		
		public Long IdCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String NombreBanco;
		
	}
	
	public static class DatosPreferenciasDePago extends BcsPojo{
		
		public String IdPreferenciaPago;
		public String NombreFormaDePago;
		public String NombreMedioDePago;
		
	}
	
	public static class DatosOperacion extends BcsPojo{
		
		public Long IdOperacion;
		public String IdTipoOperacion;
	}
	
	
	public CrearComprobantePagoOperacion_OUT logica (Connection conn, CrearComprobantePagoOperacion_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CrearComprobantePagoOperacion");
		
		SQL _sql = new SQL();
		CrearComprobantePagoOperacion_OUT out = new CrearComprobantePagoOperacion_OUT();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		
			Long errTot = 0L;
			List<ComprobantePagoOperacion> v_pagoInsert = new ArrayList<>();
			List<ComprobantePagoOperacion> v_pagoUpdate = new ArrayList<>();	
			List<Object> idPago = new ArrayList<>();
			HashMap<String, Object> v_pagoSelect = new HashMap<>();
			HashMap<String, Object> v_pagoSelectNotIn = new HashMap<>();
			HashMap<String, Object> paramsFormasDePago = new HashMap<String,Object>(2);
			
			//codigo
			for (ComprobantePagoOperacionObj_IN comp : in.p_ComprobantePagoOperacion){
				//Utils.copyPojo(comp, inTbl);
				ComprobantePagoOperacion inTbl = copyPojoComprobantePagoOperacion(comp);
				
				v_pagoSelectNotIn.put("IdOperacion", inTbl.IdOperacion);
				
				if (comp.IdComprobantePagoOperacion.equals(0L) &&  comp.Monto.compareTo(BigDecimal.ZERO) >= 0){//nuevo registro
					inTbl.RecibePaga = comp.RecibePagaPortafolio;
					inTbl.FechaIngresoComprobantePagoOperacion = Fecha.toDate(comp.FechaIngresoComprobantePagoOperacion);
					inTbl.FechaLiquidacionComprobantePagoOperacion = Fecha.toDate(comp.FechaLiquidacionComprobantePagoOperacion);
					v_pagoInsert.add(inTbl);
					//v_pagoDelete.add(inTbl);
				}
				if ("S".equals(in.p_ActualizaFormaPago)){//editar registro  //IdComprobantePago
					inTbl.IdComprobantePagoOperacion=comp.IdComprobantePagoOperacion;
					inTbl.Monto=comp.Monto;
					v_pagoUpdate.add(inTbl);
					idPago.add(comp.IdComprobantePagoOperacion);
				}
				
			}
			
			//:: Inicio validaciones
			//validar que el comprobante exista cuando es update o delete
			List<ComprobantePagoOperacion> v_pagoSelectOut = new ArrayList<>();
			if (idPago.size() > 0){
				v_pagoSelect.put("IdComprobantePagoOperacion", idPago);
				logger.debug("INICIO::SQL_SELECT_COMPROBANTE_PAGO");
				v_pagoSelectOut = _sql.query(conn, SQL_SELECT_COMPROBANTE_PAGO, v_pagoSelect, ComprobantePagoOperacion.class);
				logger.debug("FIN::SQL_SELECT_COMPROBANTE_PAGO");
			}
			if (v_pagoSelectOut.size() != idPago.size()){
				//mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"COMPROBANTE_PAGO_OPERACION_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			} 
			//:: Fin validaciones
			
			errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
			
			List<Long> secuenciaCompPago = Utils.getSecuencia(conn, "BSCL.Mesa.BD.Secuencias::IdComprobantePagoOperacion", new Long(v_pagoInsert.size()));//idOperaciones
			for (int i=0 ; i < v_pagoInsert.size(); i++){
				v_pagoInsert.get(i).IdComprobantePagoOperacion = secuenciaCompPago.get(i);
			}
		
			if(in.p_FormaPagoNueva.equals("S")) {
				_sql.batch(conn, SQL_INSERT_COMPROBANTE_PAGO, v_pagoInsert, ComprobantePagoOperacion.class);

			}else if("S".equals(in.p_ActualizaFormaPago)) {
				_sql.batch(conn, SQL_UPDATE_COMPROBANTE_MONTO, v_pagoUpdate, ComprobantePagoOperacion.class);
				
			}else {			
				_sql.batch(conn, SQL_INSERT_COMPROBANTE_PAGO, v_pagoInsert, ComprobantePagoOperacion.class);
				_sql.batch(conn, SQL_UPDATE_COMPROBANTE_PAGO, v_pagoUpdate, ComprobantePagoOperacion.class);
			}		
		
					
			//Salida forma pago operacion
			for (ComprobantePagoOperacion comp : v_pagoInsert){
				ComprobantePagoOperacionObj_OUT salComp = copyPojoComprobantePagoOperacionObj_OUT(comp);
				paramsFormasDePago.put("IdOperacion", comp.IdOperacion);
				paramsFormasDePago.put("RecibePaga", comp.RecibePaga);
				
				out.P_OUT_COMPRABANTEPAGO.add(salComp);
				mapMsg.put("IdComprobantePagoOperacion", salComp.IdComprobantePagoOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"COMPROBANTE_PAGO_CREADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			
			if(v_pagoInsert.size()>0) {
				
				Map<String,DatosPreferenciasDePago> mapDatosPreferencias = this.obtenerDatosPreferenciaDePago(conn,v_pagoInsert);				
				Map<Long,DatoCuenta> mapDatosCuentas = this.obtenerDatosCuentas(conn,in);				
				List<FormaDePagoObj_OUT> formaDePagoOut = copyPojoFormaDePagoObj_OUT(v_pagoInsert,mapDatosPreferencias,mapDatosCuentas);
				
				out.P_OUT_FORMADEPAGO = formaDePagoOut;
			}
			
			for (ComprobantePagoOperacion comp : v_pagoUpdate){
				ComprobantePagoOperacionObj_OUT salComp = copyPojoComprobantePagoOperacionObj_OUT(comp);
				
				out.P_OUT_COMPRABANTEPAGO.add(salComp);
				mapMsg.put("IdComprobantePagoOperacion", salComp.IdComprobantePagoOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"COMPROBANTE_PAGO_ACTUALIZADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
				
		
		logger.info("Termino del servicio CrearComprobantePagoOperacion, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	};
	
	private Map<String, DatosPreferenciasDePago> obtenerDatosPreferenciaDePago(Connection conn, List<ComprobantePagoOperacion> v_pagoInsert) throws Exception {
		logger.info("INICIO::CrearComprobantePagoOperacion.obtenerDatosPreferenciaDePago");
		long inicioServicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<String,Object>(1); 
		
		List<String> IdPreferenciaPago =v_pagoInsert.stream()
				.map(preferencia -> preferencia.IdFormaDePago)
				.collect(Collectors.toList());
		
		param.put("IdFormaDePago", IdPreferenciaPago);
		
		Map<String,DatosPreferenciasDePago> hashDatoPreferencias = sql.query(conn, SQL_SELECT_MEDIOS_PAGO,param,DatosPreferenciasDePago.class)
				.stream().collect(Collectors.toMap(preferencia ->preferencia.IdPreferenciaPago, preferencia -> preferencia));

		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearComprobantePagoOperacion.obtenerDatosPreferenciaDePago {}", finServicio - inicioServicio);
		return hashDatoPreferencias;
	}

	private Map<Long,DatoCuenta> obtenerDatosCuentas (Connection conn, CrearComprobantePagoOperacion_IN in ) throws Exception{
		logger.info("INICIO::CrearComprobantePagoOperacion.obtenerDatosCuentas");
		long inicioServicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> param = new HashMap<String,Object>(1);
		HashMap<String, Object> paramCor = new HashMap<String,Object>(1);
		HashMap<String, Object> paramCli = new HashMap<String,Object>(1);
		
		List<Long> IdCuentaBancariaCorredor = new ArrayList<Long>();
		List<Long> IdCuentaBancariaCliente = new ArrayList<Long>();
		
		Map<Long,DatoCuenta> hashCuentas = new HashMap<Long,DatoCuenta>();
		
		if("MSP".equals(in.p_IdModulo)) {
			in.p_ComprobantePagoOperacion.stream()
			.forEach(formaPago -> {					
				if(("VENSPT".equals(formaPago.IdTipoOperacion) && "P".equals(formaPago.RecibePagaPortafolio)) || ("COMSPT".equals(formaPago.IdTipoOperacion) && "R".equals(formaPago.RecibePagaPortafolio))) {
					IdCuentaBancariaCorredor.add(formaPago.IdCuentaBanco);
				}else {
					IdCuentaBancariaCliente.add(formaPago.IdCuentaBanco);
				}
			});
		}
		else if("MRF".equals(in.p_IdModulo))
		{
		
			List<Long> listaIdOperaciones = in.p_ComprobantePagoOperacion.stream().map(ope -> ope.IdOperacion).collect(Collectors.toList());
			param.put("IdOperacion", listaIdOperaciones);
			Map<Long,DatosOperacion> hashOperaciones = sql.query(conn, SQL_SELECT_DATOS_OPERACION,param,DatosOperacion.class)		
										.stream().collect(Collectors.toMap(info -> info.IdOperacion, info -> info));
			
			in.p_ComprobantePagoOperacion.stream()
					.forEach(formaPago -> {					
						String IdOperacion = hashOperaciones.get(formaPago.IdOperacion).IdTipoOperacion;
						if(("VENPAC".equals(IdOperacion) && "P".equals(formaPago.RecibePagaPortafolio)) || ("COMPAC".equals(IdOperacion) && "R".equals(formaPago.RecibePagaPortafolio))
								|| "VENTER".equals(IdOperacion)) {
							IdCuentaBancariaCorredor.add(formaPago.IdCuentaBanco);
						}else {
							IdCuentaBancariaCliente.add(formaPago.IdCuentaBanco);
						}
					});
				
		}
		else
		{
			in.p_ComprobantePagoOperacion.stream()
			.forEach(formaPago -> {					
				IdCuentaBancariaCliente.add(formaPago.IdCuentaBanco);
			});
		}
		
		if(IdCuentaBancariaCorredor.size()>0) {
			paramCor.put("IdCuentaBancaria", IdCuentaBancariaCorredor);
			List<DatoCuenta> listCuentasCorredora = sql.query(conn, SQL_SELECT_DATOS_CUENTA_CORREDOR,paramCor,DatoCuenta.class);
			listCuentasCorredora.forEach(dato -> {
				hashCuentas.put(dato.IdCuentaBancaria,dato);
			});
		}
		if(IdCuentaBancariaCliente.size()>0) {
			paramCli.put("IdCuentaBancaria", IdCuentaBancariaCliente);
			List<DatoCuenta> listCuentasCliente = sql.query(conn, SQL_SELECT_DATOS_CUENTA_CLIENTE,paramCli,DatoCuenta.class);
			listCuentasCliente.forEach(dato -> {
				hashCuentas.put(dato.IdCuentaBancaria,dato);
			});
		}				
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearComprobantePagoOperacion.obtenerDatosCuentas {}", finServicio - inicioServicio);
		return hashCuentas;				
	}
	
	
	public CrearComprobantePagoOperacion_OUT logicaDeleteAllOperacion (Connection conn, ArrayList<ComprobantePagoOperacionObjDel_IN> in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CrearComprobantePagoOperacion.logicaDeleteAllOperacion");
		
		SQL _sql = new SQL();
		CrearComprobantePagoOperacion_OUT out = new CrearComprobantePagoOperacion_OUT();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		//try {
			List<ComprobantePagoOperacion> v_pagoDelete = new ArrayList<>();
			
			//codigo
			for (ComprobantePagoOperacionObjDel_IN comp : in){
				//Utils.copyPojo(comp, c);
				ComprobantePagoOperacion c = copyPojoComprobantePagoOperacion(comp);
				
				v_pagoDelete.add(c);
			}
			//:: Inicio validaciones
			_sql.batch(conn, SQL_DELETE_COMPROBANTE_PAGO_OPERACION, v_pagoDelete, ComprobantePagoOperacion.class);
			
			//Salida forma pago operacion
			for (ComprobantePagoOperacion comp : v_pagoDelete){
				//Utils.copyPojo(comp, salComp);
				ComprobantePagoOperacionObj_OUT salComp = copyPojoComprobantePagoOperacionObj_OUT(comp);
				
				out.P_OUT_COMPRABANTEPAGO.add(salComp);
				mapMsg.put("IdOperacion", salComp.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"COMPROBANTE_PAGO_ELIMINADO",mapMsg,conn,comp.IdEmpresa));
				Id++;
			}
		/*}catch (Exception e){
			e.printStackTrace();
		}*/
		logger.info("Termino del servicio CrearComprobantePagoOperacion.logicaDeleteAllOperacion, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	};
	
	private ComprobantePagoOperacion copyPojoComprobantePagoOperacion(ComprobantePagoOperacionObj_IN comp) {
		ComprobantePagoOperacion inTbl = new ComprobantePagoOperacion();
		inTbl.IdInternoPagoOperacion = comp.IdInternoPagoOperacion;				   
		inTbl.IdComprobantePagoOperacion = comp.IdComprobantePagoOperacion;
		inTbl.IdEmpresa = comp.IdEmpresa;
		inTbl.IdOperacion = comp.IdOperacion;
		inTbl.IdIngresoEgreso = comp.IdIngresoEgreso;
		inTbl.IdTipoProducto = comp.IdTipoProducto;
		inTbl.IdFormaDePago = comp.IdFormaDePago;
		//inTbl.RecibePaga = comp.RecibePaga;
		inTbl.IdModalidadPago = comp.IdModalidadPago;
		inTbl.IdMoneda = comp.IdMoneda;	
		inTbl.Monto = comp.Monto;
		//inTbl.FechaIngresoComprobantePagoOperacion = comp.FechaIngresoComprobantePagoOperacion;
		//inTbl.FechaLiquidacionComprobantePagoOperacion = comp.FechaLiquidacionComprobantePagoOperacion;
		inTbl.IdEstado = comp.IdEstado;
		inTbl.IdAbono = comp.IdAbono;
		inTbl.IdCargo = comp.IdCargo;
		inTbl.AfectarTesoreria = comp.AfectarTesoreria;
		inTbl.IdIngresoEgresoAnticipo = comp.IdIngresoEgresoAnticipo;
		inTbl.IdAbonoAnticipo = comp.IdAbonoAnticipo;
		inTbl.IdCargoAnticipo = comp.IdCargoAnticipo;
		inTbl.IdCuentaBanco = comp.IdCuentaBanco;
		return inTbl;
	}
	
	private ComprobantePagoOperacionObj_OUT copyPojoComprobantePagoOperacionObj_OUT(ComprobantePagoOperacion comp) {
		ComprobantePagoOperacionObj_OUT salComp = new ComprobantePagoOperacionObj_OUT();
		salComp.IdInternoPagoOperacion = comp.IdInternoPagoOperacion;
		salComp.IdOperacion = comp.IdOperacion;
		salComp.IdComprobantePagoOperacion = comp.IdComprobantePagoOperacion;
		salComp.IdFormaDePago = comp.IdFormaDePago;
		return salComp;
	}
	
	private List<FormaDePagoObj_OUT> copyPojoFormaDePagoObj_OUT(List<ComprobantePagoOperacion> comp,Map<String,DatosPreferenciasDePago> mapDatosPreferencias ,Map<Long,DatoCuenta> mapDatosCuentas) {
		
		List<FormaDePagoObj_OUT> formPagList = new ArrayList<FormaDePagoObj_OUT>();

		for(ComprobantePagoOperacion comPag: comp) {
			FormaDePagoObj_OUT formPag = new FormaDePagoObj_OUT();			
			DatoCuenta        datoCuenta = mapDatosCuentas.get(comPag.IdCuentaBanco);
			DatosPreferenciasDePago datoPreferencia = mapDatosPreferencias.get(comPag.IdFormaDePago);			
					
			formPag.IdInternoPagoOperacion = comPag.IdInternoPagoOperacion;
			formPag.AfectarTesoreria = comPag.AfectarTesoreria;
			formPag.FechaIngresoComprobantePagoOperacion = comPag.FechaIngresoComprobantePagoOperacion;
			formPag.FechaLiquidacionComprobantePagoOperacion =comPag.FechaLiquidacionComprobantePagoOperacion;
			formPag.IdAbono=comPag.IdAbono;
			formPag.IdAbonoAnticipo=comPag.IdAbonoAnticipo;
			formPag.IdCargo = comPag.IdCargo;
			formPag.IdCargoAnticipo = comPag.IdCargoAnticipo;
			formPag.IdComprobantePagoOperacion = comPag.IdComprobantePagoOperacion;
			formPag.IdEstado = comPag.IdEstado;
			formPag.IdIngresoEgreso = comPag.IdIngresoEgresoAnticipo;
			formPag.IdModalidadPago = comPag.IdModalidadPago;
			formPag.IdMoneda = comPag.IdMoneda;
			formPag.IdOperacion = comPag.IdOperacion;
			formPag.Monto = comPag.Monto;
			formPag.IdFormaDePago = comPag.IdFormaDePago;
			formPag.RecibePaga =comPag.RecibePaga;			
			formPag.NombreBancoCuentaBancaria= datoCuenta!= null ?datoCuenta.NombreBanco:"";
			formPag.NumeroCuentaBancaria = datoCuenta!= null ? datoCuenta.NumeroCuentaBancaria:"";
			formPag.IdCuentaBancaria = datoCuenta!= null ? datoCuenta.IdCuentaBancaria:0L;			
			formPag.NombreFormaDePago =datoPreferencia!= null ? datoPreferencia.NombreFormaDePago:"";
			formPag.NombreMedioDePago =datoPreferencia!= null ? datoPreferencia.NombreMedioDePago:"";			
			
			
			formPagList.add(formPag);
		}
		

		return formPagList;
	}
	private ComprobantePagoOperacion copyPojoComprobantePagoOperacion(ComprobantePagoOperacionObjDel_IN comp) {
		ComprobantePagoOperacion inTbl = new ComprobantePagoOperacion();
		inTbl.IdEmpresa = comp.IdEmpresa;
		inTbl.IdOperacion = comp.IdOperacion;
		return inTbl;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearComprobantePagoOperacion_IN jin = gson.fromJson(in.toString(), CrearComprobantePagoOperacion_IN.class);
		CrearComprobantePagoOperacion_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			String jsonDetalle ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":null,\"p_FormaPagoNueva\":\"S\",\"p_ActualizaFormaPago\":\"N\",\"p_ComprobantePagoOperacion\":[{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdEmpresa\":1,\"IdOperacion\":4795,\"IdIngresoEgreso\":0,\"IdAbono\":0,\"IdCargo\":0,\"IdIngresoEgresoAnticipo\":0,\"IdAbonoAnticipo\":0,\"IdCargoAnticipo\":0,\"IdTipoProducto\":\"MERF\",\"RecibePagaPortafolio\":\"R\",\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"CLP\",\"Monto\":45741632,\"FechaIngresoComprobantePagoOperacion\":\"2022-07-20\",\"FechaLiquidacionComprobantePagoOperacion\":\"2022-07-20\",\"IdEstado\":\"ACT\",\"AfectarTesoreria\":\"S\",\"IdCuentaBanco\":356},{\"IdInternoPagoOperacion\":1,\"IdComprobantePagoOperacion\":0,\"IdEmpresa\":1,\"IdOperacion\":4795,\"IdIngresoEgreso\":0,\"IdAbono\":0,\"IdCargo\":0,\"IdIngresoEgresoAnticipo\":0,\"IdAbonoAnticipo\":0,\"IdCargoAnticipo\":0,\"IdTipoProducto\":\"MERF\",\"RecibePagaPortafolio\":\"P\",\"IdFormaDePago\":\"COMPAC_EFE_CLP\",\"IdModalidadPago\":\"MODCAJ\",\"IdMoneda\":\"CLP\",\"Monto\":35389007,\"FechaIngresoComprobantePagoOperacion\":\"2022-07-20\",\"FechaLiquidacionComprobantePagoOperacion\":\"2022-07-20\",\"IdEstado\":\"ACT\",\"AfectarTesoreria\":\"S\",\"IdCuentaBanco\":0}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":164,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"N\",\"p_FormaPagoNueva\":\"S\",\"p_ComprobantePagoOperacion\":[{\"AfectarTesoreria\":\"S\",\"FechaIngresoComprobantePagoOperacion\":\"2022-07-20\",\"FechaLiquidacionComprobantePagoOperacion\":\"2022-07-27\",\"IdAbono\":0,\"IdAbonoAnticipo\":0,\"IdCargo\":0,\"IdCargoAnticipo\":0,\"IdComprobantePagoOperacion\":0,\"IdCuentaBanco\":356,\"IdEmpresa\":1,\"IdEstado\":\"PEN\",\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdIngresoEgreso\":0,\"IdIngresoEgresoAnticipo\":0,\"IdInternoPagoOperacion\":2,\"IdModalidadPago\":\"MODDEP\",\"IdMoneda\":\"CLP\",\"IdOperacion\":4795,\"IdTipoProducto\":\"MERF\",\"Monto\":35389007,\"RecibePagaPortafolio\":\"R\"}]}";
			CrearComprobantePagoOperacion_IN objIn = gson.fromJson(jsonDetalle,CrearComprobantePagoOperacion_IN.class);
			//mientras pruebo hare el rollback
			CrearComprobantePagoOperacion demo = new CrearComprobantePagoOperacion();
			
			CrearComprobantePagoOperacion_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			//conn.rollback();
			conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	};
}
