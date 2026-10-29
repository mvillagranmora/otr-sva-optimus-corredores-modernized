package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenes.InformacionOperacion;
import cl.bcs.ordenes.ConsultarCobrosOrdenes.LineaMatrizOperacional;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.CrearOrdenesPostreras.CrearOrdenesPostreras_Salida;
import cl.bcs.ordenes.CrearOrdenesPostreras.OrdenPostrera_Entrada;
import cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida;
import cl.bcs.ordenes.pojo.Asignacion;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaAsignacionesPostreras implements JavaCall {
	
	private ConstantesOrdenes _c = new ConstantesOrdenes();
	
	private final String SQL_SELECT_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CargaAsignacionesPostreras_SelectPortafolios.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIOS = loadResourceToString("cl/bcs/ordenes/sql/CargaAsignacionesPostreras_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/CargaAsignacionesPostreras_SelectTransacciones.sql");
	
	public CargaAsignacionesPostreras_Salida logica(Connection conn,CargaAsignacionesPostreras_Entrada entrada) throws Exception {
		CargaAsignacionesPostreras_Salida salida = new CargaAsignacionesPostreras_Salida();
		SQL _sql = new SQL();
		
		String idBovedaDefecto = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "ORDENES", "IdBovedaDefecto");
		
		List<Asignacion_Proceso> asignacionesProceso = new ArrayList<>();
		for (Asignacion_Entrada asig : entrada.p_Asignaciones) {
			Asignacion_Proceso asigPro = new Asignacion_Proceso();
			Utils.copyPojo(asig, asigPro);
			asignacionesProceso.add(asigPro);
			
		}
		
		{
			List<String> documentosIdentidad= new ArrayList<>();
			for (Asignacion_Proceso asig : asignacionesProceso) {
				if (!documentosIdentidad.contains(asig.DocumentoIdentidad))
					documentosIdentidad.add(asig.DocumentoIdentidad);
			}
			HashMap<String,Object> params = new HashMap<>();
			params.put("idEmpresa", entrada.p_IdEmpresa);
			params.put("documentosIdentidad", documentosIdentidad);
			List<Portafolio_Proceso> _portafolios = _sql.query(conn, SQL_SELECT_PORTAFOLIOS, params, Portafolio_Proceso.class);
			for (Asignacion_Proceso asig : asignacionesProceso) {
				for (Portafolio_Proceso port : _portafolios) {
					if (asig.CodigoPortafolio.equalsIgnoreCase(port.CodigoPortafolio)
							&& asig.DocumentoIdentidad.equalsIgnoreCase(port.DocumentoIdentidad)
							&& asig.TipoDocumentoIdentidad.equalsIgnoreCase(port.TipoDocumentoIdentidad)) {
						asig.portafolio = port;
					}
				}
			}
		}
		
		HashMap<Long,IndicadorPortafolio_Proceso> indicadores = new HashMap<>();
		{
			List<Long> ports= new ArrayList<>();
			for (Asignacion_Proceso asig : asignacionesProceso) {
				if (asig.portafolio != null)
					ports.add(asig.portafolio.IdPortafolio);
			}
			
			if (ports.size() > 0) {
				HashMap<String,Object> params = new HashMap<>();
				params.put("idsPortafolio", ports);
				List<IndicadorPortafolio_Proceso> _indicadores = _sql.query(conn, SQL_SELECT_INDICADORES_PORTAFOLIOS, params, IndicadorPortafolio_Proceso.class);
				for (IndicadorPortafolio_Proceso ind : _indicadores) {
					indicadores.put(ind.IdPortafolio, ind);
				}
			}

		}
		
		{
			List<String> foliosTransaccion = new ArrayList<>();
			for (Asignacion_Proceso asig : asignacionesProceso) {
				if (!foliosTransaccion.contains(asig.FolioTransaccion))
					foliosTransaccion.add(asig.FolioTransaccion);
			}
			HashMap<String,Object> params = new HashMap<>();
			params.put("foliosTransaccion", foliosTransaccion);
			List<Transaccion_Proceso> _transacciones = _sql.query(conn, SQL_SELECT_TRANSACCIONES, params, Transaccion_Proceso.class);
			for (Asignacion_Proceso asig : asignacionesProceso) {
				for (Transaccion_Proceso tran : _transacciones) {
					if (tran.Folio.equalsIgnoreCase(asig.FolioTransaccion) && Fecha.toStringDate(tran.FechaSistemaCreacion).equals(asig.FechaTransaccion) && tran.Nemotecnico.equalsIgnoreCase(asig.Nemotecnico)) {
						if ((tran.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA) && asig.CompraVenta.equalsIgnoreCase("C")) || 
								(tran.AccionOrden.equalsIgnoreCase(_c.ACCION_VENTA) && asig.CompraVenta.equalsIgnoreCase("V"))) {
							asig.transaccion = tran;
						}
					}
						
				}
				
				
			}
		}
		
		for (Asignacion_Proceso asig : asignacionesProceso) {
			if (asig.transaccion == null) {
				String codigo = "NO_SE_ENCONTRO_TRANSACCION";
				String descripcion = Diccionario.getMensaje(conn,entrada.p_IdEmpresa, codigo).add("Folio", asig.FolioTransaccion).add("Fecha", asig.FechaTransaccion).msg();
				MensajeSalida e = new MensajeSalida(asig.IdInterno, MensajeSalida.MENSAJE_ERROR, codigo, descripcion);
				salida.P_OUT_MENSAJES.add(e);
			}
			
			if (asig.Cantidad.compareTo(BigDecimal.ZERO) != 1) {
				String codigo = "CANTIDAD_MENOR_CERO";
				String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).add("IdInterno", asig.IdInterno).msg();
				MensajeSalida e = new MensajeSalida(asig.IdInterno, MensajeSalida.MENSAJE_ERROR, codigo, descripcion);
				salida.P_OUT_MENSAJES.add(e);
			}
			
			if (asig.portafolio == null) {
				String codigo = "NO_SE_ENCONTRO_PORTAFOLIO";
				String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).add("CodigoPortafolio", asig.CodigoPortafolio).add("DocumentoIdentidad", asig.DocumentoIdentidad).msg();
				MensajeSalida e = new MensajeSalida(asig.IdInterno, MensajeSalida.MENSAJE_ERROR, codigo, descripcion);
				salida.P_OUT_MENSAJES.add(e);
			}
		}
		
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
			return salida;
		
		//TODO ASIGNAR
		List<OrdenPostrera_Entrada> ordenes = new ArrayList<>();
		for (Asignacion_Proceso asig : asignacionesProceso) {
			OrdenPostrera_Entrada orden = new OrdenPostrera_Entrada();
			if (asig.transaccion.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && asig.transaccion.EsFungible.equalsIgnoreCase(_c.SI)) {
				orden.Cantidad = BigDecimal.ONE;
				orden.ValorRescate = asig.Cantidad;
			}
			else {
				orden.Cantidad = asig.Cantidad;
				orden.ValorRescate = BigDecimal.ZERO;
			}
			orden.EsFacturaInmediata = _c.NO;
			orden.FechaRecepcion = FechaDB.ObtenerFechaHoraSistemaUTC(conn, entrada.p_IdEmpresa);
			orden.FolioCartera = 0L;
			orden.Glosa = "";
			orden.IdBoveda = idBovedaDefecto;
			if (asig.transaccion.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV) && indicadores.get(asig.portafolio.IdPortafolio).OperaCustodiaRV.equalsIgnoreCase(_c.SI)) {
				orden.IdFormaCustodia = "CUS";
			}
			else if (asig.transaccion.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF) && indicadores.get(asig.portafolio.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI)) {
				orden.IdFormaCustodia = "CUS";
			}
			else if (asig.transaccion.IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF) && indicadores.get(asig.portafolio.IdPortafolio).OperaCustodiaRFIF.equalsIgnoreCase(_c.SI)) {
				orden.IdFormaCustodia = "CUS";
			}
			else {
				orden.IdFormaCustodia = "NOM";
			}
			orden.IdInterno = asig.IdInterno;
			orden.IdMonedaLiquidacionOrden = "CLP";
			orden.IdPortafolio = asig.portafolio.IdPortafolio;
			if (indicadores.get(asig.portafolio.IdPortafolio).OperaCuentaInversion.equalsIgnoreCase(_c.SI)) {
				orden.PagoCobroCuentaInversion = "S";
			}
			else {
				orden.PagoCobroCuentaInversion = "N";
			}
			orden.IdPreferenciaPagoPortafolio = null;
			orden.IdTransaccion = asig.transaccion.IdTransaccion;
			orden.PlazoVentaCorta = 0L;
			
			orden.TasaVentaCorta = BigDecimal.ZERO;
			orden.TipoCambio = null;
			ordenes.add(orden);	
		}
		
		CrearOrdenesPostreras serviceCrearOrdenesPostreras = new CrearOrdenesPostreras();
		CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada entradaCrearOrdenesPostreras = new CrearOrdenesPostreras.CrearOrdenesPostreras_Entrada();
		entradaCrearOrdenesPostreras.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearOrdenesPostreras.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearOrdenesPostreras.p_IdModulo = entrada.p_IdModulo;
		entradaCrearOrdenesPostreras.p_simularOperacion = _c.NO;
		entradaCrearOrdenesPostreras.p_UsaReglasNegocio = _c.SI;
		entradaCrearOrdenesPostreras.p_Ordenes = ordenes;
		CrearOrdenesPostreras_Salida salidaCrearOrdenesPostreras = serviceCrearOrdenesPostreras.logica(conn, entradaCrearOrdenesPostreras);
		for (MensajeSalida msg : salidaCrearOrdenesPostreras.P_OUT_MENSAJES) {
			if (MensajeSalida.getNumeroErrores(salidaCrearOrdenesPostreras.P_OUT_MENSAJES) > 0 && msg.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
				salida.P_OUT_MENSAJES.add(msg);
			}				
		}
		if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) == 0) {
			for (cl.bcs.ordenes.CrearOrdenesPostreras.Resultado_Salida res : salidaCrearOrdenesPostreras.P_OUT_RESULTADO) {
				Resultado_Salida resx = new Resultado_Salida();
				Utils.copyPojo(res, resx);
				salida.P_OUT_RESULTADO.add(resx);
			}
			String codigo = "ASIGNACIONES_CREADAS_CORRECTAMENTE";
			String desc = Diccionario.getMensaje(conn, entrada.p_IdEmpresa,codigo).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));
		}
		
		return salida;
	}
	
	public static class Transaccion_Proceso {
		public String EsFungible;
		public Long IdTransaccion;
		public String AccionOrden;
		public String Folio;
		public String Nemotecnico;
		public java.sql.Date FechaSistemaCreacion;
		public String IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPortafolio_Proceso {
		public Long IdPortafolio;
		public String OperaCustodiaRV;
		public String OperaCustodiaRFIF;
		public String OperaCuentaInversion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio_Proceso {
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public Long IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado_Salida {
		public Long IdInterno;
		public Long IdAsignacion;
		public Long IdOrden;
	}
	
	public static class CargaAsignacionesPostreras_Salida {

		public List<Resultado_Salida> P_OUT_RESULTADO;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CargaAsignacionesPostreras_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_RESULTADO = new ArrayList<>();
		}
		
	}
	
	public static class Asignacion_Entrada {
		public Long IdInterno;
		public String FolioTransaccion;
		public String FechaTransaccion;
		public String Nemotecnico;
		public String CompraVenta;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public BigDecimal Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Asignacion_Proceso {
		public Long IdInterno;
		public String FolioTransaccion;
		public String FechaTransaccion;
		public String Nemotecnico;
		public String CompraVenta;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public BigDecimal Cantidad;
		
		public Transaccion_Proceso transaccion;
		public Portafolio_Proceso portafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CargaAsignacionesPostreras_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Asignacion_Entrada> p_Asignaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CargaAsignacionesPostreras_Entrada() {
			p_Asignaciones = new ArrayList<>();
		}
	}
		
	public static void main(String[]args) throws Exception{
		try (Connection con = PlataformaUtils.getMBConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"ORD\",\"p_Asignaciones\":[{\"FolioTransaccion\":\"501\",\"FechaTransaccion\":\"2017-08-31\",\"Nemotecnico\":\"CMPC\",\"CompraVenta\":\"C\",\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"15345455-0\",\"CodigoPortafolio\":\"1\",\"Cantidad\":1,\"MENSAJEVALIDACION\":\"OK\",\"IdInterno\":0}]}";
			CargaAsignacionesPostreras_Entrada entrada = gson.fromJson(json, CargaAsignacionesPostreras_Entrada.class);
			
			/*CargaAsignacionesPostreras_Entrada entrada = new CargaAsignacionesPostreras_Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "ORD";
			entrada.p_IdUsuario = 4L;
			Asignacion_Entrada e = new Asignacion_Entrada();
			e.Cantidad = new BigDecimal(100);
			e.CodigoPortafolio = "1";
			e.CompraVenta = "C";
			e.DocumentoIdentidad = "15731790-3";
			e.FechaTransaccion = "2017-06-05";
			e.FolioTransaccion = "1405";
			e.NemotecnicoTransaccion = "CAP";
			e.IdInterno = 0L;
			e.TipoDocumentoIdentidad = "R";
			entrada.p_Asignaciones.add(e);*/
			CargaAsignacionesPostreras simulacion = new CargaAsignacionesPostreras();
			CargaAsignacionesPostreras_Salida salida = simulacion.logica(con, entrada);
			System.out.println(salida.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CargaAsignacionesPostreras_Entrada in = gson.fromJson(jin.toString(), CargaAsignacionesPostreras_Entrada.class);
		CargaAsignacionesPostreras_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}
	