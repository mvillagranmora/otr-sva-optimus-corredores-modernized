package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearOrdenes.OrdenProceso;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.LibroOrdenes;
import cl.bcs.ordenes.pojo.TransaccionIFBEC;
import cl.bcs.ordenes.pojo.TransaccionRFBEC;
import cl.bcs.ordenes.pojo.TransaccionRVBEC;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearTransaccionesBolsaElectronica implements JavaCall {
	
	private final Log log = new Log("ORD.CrearTransaccionesBolsaElectronica");
	private final String SQL_INSERT_TRANSACCIONES_WRAPPER = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesBolsaElectronica_InsertTransaccionesWrapper.sql");
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String SQL_SELECT_CORREDORAS = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesBolsaElectronica_SelectCorredoras.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CrearTransaccionesBolsaElectronica_SelectTransacciones.sql");
	private final String SQL_OBTENER_LIBRO_POR_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/Globales_ObtenerLibroPorDefecto.sql");
	
	public static class Corredora {
		public Long IdCorredora;
		public String CodigoCorredora;
		public String CodigoCorredoraFix;
		public String IdBolsa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoTransaccion {
		public Long IdInterno;
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdCondicionDeLiquidacionContado;
		public Long DiasLiquidacionContado;
		public String IdTipoOperacion;
		public String IdTipoReajuste;
		public BigDecimal CorteMinimo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Transaccion_Proceso extends cl.bcs.ordenes.CrearTransacciones.Transaccion_Entrada {
		public String Nemotecnico;
		public String AccionOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionIFBEC_Entrada extends TransaccionIFBEC {
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionRFBEC_Entrada extends TransaccionRFBEC {
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionRVBEC_Entrada extends TransaccionRVBEC {
		public Long IdInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		return null;
	}
		
	public CrearTransaccionesBolsaElectronica_Salida logica(Connection conn,CrearTransaccionesBolsaElectronica_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		CrearTransaccionesBolsaElectronica_Salida salida = new CrearTransaccionesBolsaElectronica_Salida();
		
		String fechaCarga = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		ConsultarEmpresa serviceConsultarEmpresa = new ConsultarEmpresa();
		InformacionEmpresa empresa = serviceConsultarEmpresa.logica(conn, entrada.p_IdEmpresa);
		
		HashMap<String,Corredora> corredoras = new HashMap<String,Corredora>();
		{
			HashMap<String,Object> params1 = new HashMap<>();
			params1.put("idBolsa", "BCS"); //se usa codigo BCS porque en la tabla de ruebas ya llega homologada
			List<Corredora> _corredoras = sql.query(conn, SQL_SELECT_CORREDORAS, params1, Corredora.class);
			corredoras = new HashMap<>();
			for (Corredora corr : _corredoras) {
				corredoras.put(corr.CodigoCorredora, corr);
			}
		}
		
		LibroOrdenes libroDefecto = null;
		{
			HashMap<String,Object> params2= new HashMap<>();
			params2.put("idEmpresa", entrada.p_IdEmpresa);
			List<LibroOrdenes> _libroDefecto = sql.query(conn, SQL_OBTENER_LIBRO_POR_DEFECTO, params2, LibroOrdenes.class);
			if (_libroDefecto.size() == 0) {
				String codigo = "LIBRO_DEFECTO_NO_CONFIGURADO";
				String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, codigo).msg();
				MensajeSalida msj = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc);
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
			}
			libroDefecto = _libroDefecto.get(0);
		}
		
		
		//TRANSACCIONES IF
		List<Transaccion_Proceso> transacciones = new ArrayList<Transaccion_Proceso>();
		{
			for (TransaccionIFBEC_Entrada tx : entrada.transaccionesIF) {
				
				Transaccion_Proceso txe = new Transaccion_Proceso();
				
				Corredora corredoraCompra = corredoras.get(tx.COD_CORR_COM.toString());
				Corredora corredoraVenta = corredoras.get(tx.COD_CORR_VEN.toString());
				
				if (corredoraCompra == null) {
					String codigo = "Corredor " + tx.COD_CORR_COM + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
					salida.P_OUT_MENSAJES.add(msj);
					log.write(msj);
					continue;
				}
				if (corredoraVenta == null) {
					String codigo ="Corredor " + tx.COD_CORR_VEN + " no cargado";
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
					salida.P_OUT_MENSAJES.add(msj);
					log.write(msj);
					continue;
				}
				
				if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
					txe.CodigoOperadorCorredor = tx.COD_OPE_COM.toString();
					txe.CodigoOperadorContraparte = tx.COD_OPE_VEN.toString();
					txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_VEN.toString()).IdCorredora;
					txe.AccionOrden = _c.ACCION_COMPRA;
				}
				else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
					txe.CodigoOperadorCorredor = tx.COD_OPE_VEN.toString();
					txe.CodigoOperadorContraparte = tx.COD_OPE_COM.toString();
					txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_COM.toString()).IdCorredora;
					txe.AccionOrden = _c.ACCION_VENTA;
				}
				else {
					String codigo = "Transaccion otros corredores Folio:" + tx.FOLIO + " Fecha:" + tx.FECHA  + " C:" + tx.COD_CORR_COM + " V:" +tx.COD_CORR_VEN;
					MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
					salida.P_OUT_MENSAJES.add(msj);
					log.write(msj);
					continue;
				}
				
				if (tx.NEMO.equalsIgnoreCase("PRBC") || tx.NEMO.equalsIgnoreCase("PDBC")) {
					txe.Cantidad = BigDecimal.ZERO;  //mas abajo se calcula
				}
				else {
					txe.Cantidad = BigDecimal.ONE;
				}
				
				txe.EsBilateral  = "N";
				txe.EsCuentaPropia = "N";
				txe.EsFueraRueda = "N";
				if (tx.NEMO.equalsIgnoreCase("PDBC") || tx.NEMO.equalsIgnoreCase("PDBC")) {
					txe.EsFungible ="N";
				}
				else {
					txe.EsFungible = "S";	
				}
				
				txe.IndicadorOrdenDirecta = "";
				txe.EsVentaCorta = "N";
				
				//para cargar palos de otras fechas
				if (fechaCarga.length() == 0 || fechaCarga.equalsIgnoreCase("0001-01-01")) {
					txe.FechaCreacion = tx.FECHA;
				}
				else {
					txe.FechaCreacion = fechaCarga;
				}
				
				txe.FechaEmision = tx.FEC_EMI;
				//txe.FechaLiquidacionContado;	
				txe.FechaVencimiento = tx.FEC_VCTO;	
				txe.FolioTransaccion = tx.FOLIO.toString();
				txe.FolioTransaccionPlazo = "";
				String hora = tx.HORA.substring(11,19);
				txe.HoraCreacion = hora;
				txe.IdBolsa = "BEC";
				if (tx.COND_COD_002.equalsIgnoreCase("H")) {
					txe.IdCondicionDeLiquidacionContado = "T0";	
				}
				else if (tx.COND_COD_002.equalsIgnoreCase("M")) {
					txe.IdCondicionDeLiquidacionContado = "T1";	
				}
				else if (tx.COND_COD_002.equalsIgnoreCase("T")) {
					txe.IdCondicionDeLiquidacionContado = "TP";	
				}
				else if (tx.COND_COD_002.equalsIgnoreCase("")) {
					txe.IdCondicionDeLiquidacionContado = "T2";	
				}
				else if (tx.COND_COD_002.equalsIgnoreCase("3")) {
					txe.IdCondicionDeLiquidacionContado = "T3";	
				}
				else {
					txe.IdCondicionDeLiquidacionContado = "T2";
				}
				
				txe.IdCorredoraCorresponsal = 0L;
				txe.IdEmpleado = 0L;
				txe.IdEmisor = tx.EMISOR;
				//txe.IdInstrumento = infoTransaccion.IdInstrumento;
				txe.IdInstrumentoSubyacente = 0L;
				txe.IdInterno = tx.IdInterno;
				txe.IdLibroOrdenes = libroDefecto.IdLibroOrdenes;
				txe.IdMonedaLiquidacionCorresponsal = "";
				txe.IdSistemaBursatil = "BEC";
				//txe.IdTipoOperacion;
				if (tx.TIPO_REAJ.equalsIgnoreCase("NR")) {
					txe.IdTipoReajuste = "CLP";
				}
				else {
					txe.IdTipoReajuste = tx.TIPO_REAJ;	
				}
				
				txe.IndicadorDCV =  "S";
				txe.MarcaNegociacion = "";
				txe.MejorPrecioCompra = BigDecimal.ZERO;
				txe.MejorPrecioVenta = BigDecimal.ZERO;
				txe.Monto = tx.MONTO;
				txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
				txe.MontoPlazo = BigDecimal.ZERO;
				txe.Nemotecnico = tx.NEMO.trim();
				txe.IdMoneda = "CLP";
				txe.Plazo = tx.PLAZO.longValue();
				txe.Precio = BigDecimal.ZERO;
				txe.PrecioPlazo = BigDecimal.ZERO;
				txe.Tasa = tx.TASA;
				txe.TipoCambioCorresponsal = BigDecimal.ZERO;
				txe.ValorRescate = tx.VAL_RESC;
				txe.FlujoOrigen = "INGRESOORDEN";
				
				transacciones.add(txe);
				
			}
		}
		
		for (TransaccionRFBEC_Entrada tx : entrada.transaccionesRF) {
			
			Transaccion_Proceso txe = new Transaccion_Proceso();
			
			Corredora corredoraCompra = corredoras.get(tx.COD_CORR_COM.toString());
			Corredora corredoraVenta = corredoras.get(tx.COD_CORR_VEN.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tx.COD_CORR_COM + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tx.COD_CORR_VEN + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				txe.CodigoOperadorCorredor = tx.COD_OPE_COM.toString();
				txe.CodigoOperadorContraparte = tx.COD_OPE_VEN.toString();
				txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_VEN.toString()).IdCorredora;
				txe.AccionOrden = _c.ACCION_COMPRA;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
				txe.CodigoOperadorCorredor = tx.COD_OPE_VEN.toString();
				txe.CodigoOperadorContraparte = tx.COD_OPE_COM.toString();
				txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_COM.toString()).IdCorredora;
				txe.AccionOrden = _c.ACCION_VENTA;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tx.FOLIO + " Fecha:" + tx.FECHA  + " C:" + tx.COD_CORR_COM + " V:" +tx.COD_CORR_VEN;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			
			
			//txe.AccionOrden;
			txe.Cantidad = tx.CANTIDAD;
			//txe.CodigoOperadorContraparte;
			//txe.CodigoOperadorCorredor;
			txe.EsBilateral  = "N";
			txe.EsCuentaPropia = "N";
			txe.EsFueraRueda = "N";
			txe.EsFungible = "N";
			txe.IndicadorOrdenDirecta = "";
			txe.EsVentaCorta = "N";

			//para cargar palos de otras fechas
			if (fechaCarga.length() == 0 || fechaCarga.equalsIgnoreCase("0001-01-01")) {
				txe.FechaCreacion = tx.FECHA;
			}
			else {
				txe.FechaCreacion = fechaCarga;
			}
			
			txe.FechaEmision = "0001-01-01";
			//txe.FechaLiquidacionContado;	
			txe.FechaVencimiento = "0001-01-01";	
			txe.FolioTransaccion = tx.FOLIO.toString();
			txe.FolioTransaccionPlazo = "";
			String hora = tx.HORA.substring(11,19);
			txe.HoraCreacion = hora;
			txe.IdBolsa = "BEC";
			
			if (tx.COND_COD_002.equalsIgnoreCase("H")) {
				txe.IdCondicionDeLiquidacionContado = "T0";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("M")) {
				txe.IdCondicionDeLiquidacionContado = "T1";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("T")) {
				txe.IdCondicionDeLiquidacionContado = "TP";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("")) {
				txe.IdCondicionDeLiquidacionContado = "T2";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("3")) {
				txe.IdCondicionDeLiquidacionContado = "T3";	
			}
			else {
				txe.IdCondicionDeLiquidacionContado = "T2";
			}
			
			//txe.IdCorredoraContraparte;
			
			txe.IdCorredoraCorresponsal = 0L;
			txe.IdEmpleado = 0L;
			txe.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			txe.IdInstrumentoSubyacente = 0L;
			txe.IdInterno = tx.IdInterno;
			txe.IdLibroOrdenes = libroDefecto.IdLibroOrdenes;
			txe.IdMonedaLiquidacionCorresponsal = "";
			txe.IdSistemaBursatil = "BEC";
			
			//txe.IdTipoOperacion;
			txe.IdTipoReajuste = "";
			txe.IndicadorDCV = "S";
			txe.MarcaNegociacion = "";
			txe.MejorPrecioCompra = BigDecimal.ZERO;
			txe.MejorPrecioVenta = BigDecimal.ZERO;
			txe.Monto = tx.MONTO;
			txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			txe.MontoPlazo = BigDecimal.ZERO;
			txe.Nemotecnico = tx.NEMO;
			txe.NemotecnicoEspecificoBolson = tx.NEMO;
			txe.Plazo = 0L;
			txe.Precio = tx.PRECIO;
			txe.PrecioPlazo = BigDecimal.ZERO;
			txe.Tasa = tx.TIR;
			txe.TipoCambioCorresponsal = BigDecimal.ZERO;
			txe.ValorRescate = BigDecimal.ZERO;
			txe.FlujoOrigen = "INGRESOORDEN";
			
			
			transacciones.add(txe);
			
			
		}
		
		for (TransaccionRVBEC_Entrada tx : entrada.transaccionesRV) {
			
			Transaccion_Proceso txe = new Transaccion_Proceso();
			
			Corredora corredoraCompra = corredoras.get(tx.COD_CORR_COM.toString());
			Corredora corredoraVenta = corredoras.get(tx.COD_CORR_VEN.toString());
			
			if (corredoraCompra == null) {
				String codigo = "Corredor " + tx.COD_CORR_COM + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			if (corredoraVenta == null) {
				String codigo ="Corredor " + tx.COD_CORR_VEN + " no cargado";
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			
			if (corredoraCompra.IdCorredora.equals(empresa.IdCorredora)) {
				txe.CodigoOperadorCorredor = tx.COD_OPE_COM.toString();
				txe.CodigoOperadorContraparte = tx.COD_OPE_VEN.toString();
				txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_VEN.toString()).IdCorredora;
				txe.AccionOrden = _c.ACCION_COMPRA;
			}
			else if (corredoraVenta.IdCorredora.equals(empresa.IdCorredora)) {
				txe.CodigoOperadorCorredor = tx.COD_OPE_VEN.toString();
				txe.CodigoOperadorContraparte = tx.COD_OPE_COM.toString();
				txe.IdCorredoraContraparte = corredoras.get(tx.COD_CORR_COM.toString()).IdCorredora;
				txe.AccionOrden = _c.ACCION_VENTA;
			}
			else {
				String codigo = "Transaccion otros corredores Folio:" + tx.FOLIO + " Fecha:" + tx.FECHA  + " C:" + tx.COD_CORR_COM + " V:" +tx.COD_CORR_VEN;
				MensajeSalida msj = new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,"");
				salida.P_OUT_MENSAJES.add(msj);
				log.write(msj);
				continue;
			}
			
			//txe.AccionOrden;
			txe.Cantidad = tx.CANTIDAD;
			//txe.CodigoOperadorContraparte;
			//txe.CodigoOperadorCorredor;
			txe.EsBilateral  = "N";
			txe.EsCuentaPropia = "N";
			txe.EsFueraRueda = "N";
			txe.EsFungible = "N";
			txe.IndicadorOrdenDirecta = "";
			if (tx.COND_COD_004.equalsIgnoreCase("V")) {
				txe.EsVentaCorta = _c.SI;
			}
			else {
				txe.EsVentaCorta = _c.NO;	
			}
			
			

			//para cargar palos de otras fechas
			if (fechaCarga.length() == 0 || fechaCarga.equalsIgnoreCase("0001-01-01")) {
				txe.FechaCreacion = tx.FECHA;
			}
			else {
				txe.FechaCreacion = fechaCarga;
			}
			
			txe.FechaEmision = "0001-01-01";
			//txe.FechaLiquidacionContado;
			txe.FechaVencimiento = "0001-01-01";	
			txe.FolioTransaccion = tx.FOLIO.toString();
			txe.FolioTransaccionPlazo = "";
			String hora = tx.HORA.substring(11,19);
			txe.HoraCreacion = hora;
			txe.IdBolsa = "BEC";
			
			if (tx.COND_COD_002.equalsIgnoreCase("H")) {
				txe.IdCondicionDeLiquidacionContado = "T0";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("M")) {
				txe.IdCondicionDeLiquidacionContado = "T1";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("T")) {
				txe.IdCondicionDeLiquidacionContado = "TP";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("")) {
				txe.IdCondicionDeLiquidacionContado = "T2";	
			}
			else if (tx.COND_COD_002.equalsIgnoreCase("3")) {
				txe.IdCondicionDeLiquidacionContado = "T3";	
			}
			else {
				txe.IdCondicionDeLiquidacionContado = "T2";
			}
			
			//txe.IdCorredoraContraparte;
			
			txe.IdCorredoraCorresponsal = 0L;
			txe.IdEmpleado = 0L;
			txe.IdEmisor = "";
			//txe.IdInstrumento = infoTransaccion.IdInstrumento;
			txe.IdInstrumentoSubyacente = 0L;
			txe.IdInterno = tx.IdInterno;
			txe.IdLibroOrdenes = libroDefecto.IdLibroOrdenes;
			txe.IdMonedaLiquidacionCorresponsal = "";
			txe.IdSistemaBursatil = "BEC";
			
			//txe.IdTipoOperacion;
			txe.IdTipoReajuste = "";
			if (tx.IND_DCV.equalsIgnoreCase("D")) {
				txe.IndicadorDCV = _c.SI;
			}
			else {
				txe.IndicadorDCV = _c.NO; 
			}
			txe.MarcaNegociacion = "";
			txe.MejorPrecioCompra = BigDecimal.ZERO;
			txe.MejorPrecioVenta = BigDecimal.ZERO;
			txe.Monto = tx.MONTO;
			txe.MontoLiquidacionCorresponsal = BigDecimal.ZERO;
			txe.MontoPlazo = BigDecimal.ZERO;
			txe.Nemotecnico = tx.NEMO.trim();
			txe.NemotecnicoEspecificoBolson = tx.NEMO;
			txe.Plazo = 0L;
			txe.Precio = tx.PRECIO;
			txe.PrecioPlazo = BigDecimal.ZERO;
			txe.Tasa = BigDecimal.ZERO;
			txe.TipoCambioCorresponsal = BigDecimal.ZERO;
			txe.ValorRescate = BigDecimal.ZERO;
			txe.FlujoOrigen = "INGRESOORDEN";
			
			transacciones.add(txe);
			
		}


		if (transacciones.size() == 0) {
			return salida;
		}
		
		
		
		sql.batch(conn, SQL_INSERT_TRANSACCIONES_WRAPPER, transacciones, Transaccion_Proceso.class);
		
		//QUERY TIPO OPERACION, DIAS LIQUIDACION
		List<InfoTransaccion> _infoTransacciones = sql.query(conn, SQL_SELECT_TRANSACCIONES, new HashMap<>(), InfoTransaccion.class);
		HashMap<Long,Object> infoTransacciones = Utils.List2Hash(_infoTransacciones, "IdInterno");
		
		ArrayList<Transaccion_Proceso> _txs = new ArrayList<>();
		for (Transaccion_Proceso tx : transacciones) {
			
			InfoTransaccion info = (InfoTransaccion) infoTransacciones.get(tx.IdInterno);
			if (info == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(tx.IdInterno,MensajeSalida.MENSAJE_ERROR,"Error al obtener instrumento","Error al obtener instrumento"));
				continue;
			}
			tx.IdTipoOperacion = info.IdTipoOperacion;
			tx.IdInstrumento = info.IdInstrumento;
			tx.GastosCorresponsal = BigDecimal.ZERO;
			tx.Intereses = BigDecimal.ZERO;
			tx.Reajustes = BigDecimal.ZERO;
			
			if (tx.Nemotecnico.equalsIgnoreCase("PDBC") || tx.Nemotecnico.equalsIgnoreCase("PRBC")) {
				tx.Cantidad = tx.ValorRescate.divide(info.CorteMinimo);
			}
			_txs.add(tx);
		}
		
		transacciones = _txs;
		
		//FECHAS LIQUIDACION
		{
			ObtenerDiaSiguiente obtenerDiaSiguienteService = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = entrada.p_IdEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = "S";
			inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
			
			for (Transaccion_Proceso tx : transacciones) {
				
				InfoTransaccion info = (InfoTransaccion) infoTransacciones.get(tx.IdInterno);
				
				FechasIn f = new FechasIn();
				f.fechaInicial = tx.FechaCreacion;
				if (info.DiasLiquidacionContado == null) {
					info.DiasLiquidacionContado = 0L;
				}
				f.numeroDias = info.DiasLiquidacionContado;
				inObtenerDiaSiguiente.p_fechas.add(f);
			}
			
			OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguienteService.logica(conn, inObtenerDiaSiguiente);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				for (Transaccion_Proceso tx : transacciones) {
					
					InfoTransaccion info = (InfoTransaccion) infoTransacciones.get(tx.IdInterno);
					
					if (tx.FechaCreacion.equalsIgnoreCase(fecha.fechaInicial) && info.DiasLiquidacionContado.equals(fecha.numeroDiasInicial)){
						tx.FechaLiquidacionContado = fecha.fechaCalculada;
					}
				}
			}
		}
		
		CrearTransacciones serviceCrearTransacciones = new CrearTransacciones();
		
		CrearTransacciones_Entrada entradaCrearTransacciones = new CrearTransacciones_Entrada();
		entradaCrearTransacciones.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearTransacciones.p_IdModulo = _c.ID_MODULO_ORDENES;
		entradaCrearTransacciones.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearTransacciones.p_simularOperacion = _c.NO;
		entradaCrearTransacciones.p_UsaReglasNegocio = _c.SI;
		for (Transaccion_Proceso tx : transacciones) {
			entradaCrearTransacciones.p_Transacciones.add(tx);
		}
		CrearTransacciones_Salida salidaCrearTransacciones = serviceCrearTransacciones.logica(conn, entradaCrearTransacciones );
		
		for (cl.bcs.ordenes.CrearTransacciones.Resultado_Salida resp : salidaCrearTransacciones.P_OUT_RESULTADO) {
			Resultado_Salida resp2 = new Resultado_Salida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_RESULTADO.add(resp2);
		}
		
		for (MensajeSalida resp : salidaCrearTransacciones.P_OUT_MENSAJES) {
			MensajeSalida resp2 = new MensajeSalida();
			Utils.copyPojo(resp, resp2);
			salida.P_OUT_MENSAJES.add(resp2);
		}
		
		return salida;
	}
	
	public static class CrearTransaccionesBolsaElectronica_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<TransaccionIFBEC_Entrada> transaccionesIF;
		public List<TransaccionRVBEC_Entrada> transaccionesRV;
		public List<TransaccionRFBEC_Entrada> transaccionesRF;
		
		public CrearTransaccionesBolsaElectronica_Entrada() {
			this.transaccionesIF = new ArrayList<>();
			this.transaccionesRV = new ArrayList<>();
			this.transaccionesRF = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdTransaccion;
		public String IdTipoOperacion;
		public String EsAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CrearTransaccionesBolsaElectronica_Salida {
		
		public List<Resultado_Salida> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public CrearTransaccionesBolsaElectronica_Salida() {
			this.P_OUT_RESULTADO = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getDESAConnection()){
			CrearTransaccionesBolsaElectronica srv = new CrearTransaccionesBolsaElectronica();
			CrearTransaccionesBolsaElectronica_Entrada in = new CrearTransaccionesBolsaElectronica_Entrada();
			in.p_IdEmpresa = 1L;
			in.p_IdUsuario = 0L;
			CrearTransaccionesBolsaElectronica_Salida out = srv.logica(con, in);
			
			
	

		}	
	}
}
	