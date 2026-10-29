package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.pojo.ReferenciaTipoDocumento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class ModificarMovimientoComprobanteSP implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ModificarMovimientoComprobanteSP.class);
	
	private final String SQL_INSERT_MOVIMIENTO = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_InsertMovimientoTablaTemporal.sql");
	private final String SQL_INSERT_FORMADEPAGO = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_InsertFormaDePagoTablaTemporal.sql");
	private final String SQL_CALL_MODIFICARMOVIMIENTO = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_CallProcedimiento.sql");
	private final String SQL_SELECT_INFORMACIONOUT = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_SelectOUTInformacion.sql");
	private final String SQL_DELETE_MOVIMIENTO_COMPROBANTE_REFERENCIA = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_DeleteMovimientoComprobanteReferencia.sql");
	private final String SQL_INSERT_MOVIMIENTO_COMPROBANTE_REFERENCIA = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteReferencia.sql");
	private final String SQL_SELECT_DATOS_CLIENTE_MODIFICAR_FORMA_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectDatosClienteModificarFormaDePago.sql");
	private final String SQL_SELECT_DATOS_CLIENTE_MODIFICAR_GENERALES = loadResourceToString("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectDatosClienteModificarGenerales.sql");
	
	//ENTRADAS
	public static class ModificarMovimientoComprobanteSP_Entrada extends BcsPojo {
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;   
		public String							p_IdModulo;   
		public String							p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
		public String							p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
		public String							p_EsIngresoAutomatico = "N";	// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
		public List<MovimientoComprobanteSP_Entrada>				P_MOVIMIENTOFACTURARGRAL;
		public List<MovimientoComprobanteCobroSP_Entrada>			P_MOVIMIENTOFACTURARCOBROS;
		public List<MovimientoComprobanteFormaDePagoSP_Entrada>	P_MOVIMIENTOFACTURARFDPAGO;
		
	    public ModificarMovimientoComprobanteSP_Entrada (){
	    	P_MOVIMIENTOFACTURARGRAL	= new ArrayList<MovimientoComprobanteSP_Entrada>();
	    	P_MOVIMIENTOFACTURARCOBROS 	= new ArrayList<MovimientoComprobanteCobroSP_Entrada>();
	    	P_MOVIMIENTOFACTURARFDPAGO 	= new ArrayList<MovimientoComprobanteFormaDePagoSP_Entrada>();
		}
	    
	};
	
	public static class MovimientoComprobanteSP_Entrada extends BcsPojo{
		public Long			IdMovimientoComprobante;
		public Long 		IdPortafolio;
		public String		IdFormaCustodia;
		public String		IdBoveda;
		public String		IdTipoMovimiento;
		public String		EsComprobIndividual;
		public String		Glosa;
		public String		Referencia;
		public String		TipoReferencia;
		public BigDecimal	MontoMovimientoComprobante;
	};
	
	public static class MovimientoComprobanteFormaDePagoSP_Entrada{
		public Long			IdMovimientoComprobanteFormaDePago;
		public Long			IdMovimientoComprobante;
		public String		IdTipoMovimiento;
		public String		IdMedioDePago;
		public String		NombreMedioDePago;
		public String		IdModalidadDePago;
		public String		NombreModalidadDePago;
		public BigDecimal	MontoLiquidacionFormaDePago;
		public String		IdMoneda;
		public Long			IdBanco;
		public String		CodigoBanco;
		public String		NombreBanco;
		public String		IdEstado;
		public Long			IdCuentaBancaria;
		public String		NombreCuentaBancaria;
		public String		NumeroCuentaBancaria;
		public Long			IdTarjetaBancaria;
		public String		FechaVctoTarjetaBancaria;
		public String		IdTipoCuentaBancaria;
		public String		NombreTipoCuentaBancaria;
		public String		NumeroSerie;
		public Long			NumeroDocumento;
		public String		FechaDocumentoPago;
		public String		IdTipoTarjetaBancaria;
		public String		NombreTipoTarjetaBancaria;
		public String		NumeroTarjetaBancaria;
		public String		AfectarTesoreria;
		public String		IdTipoMovimientoSpot;
		public BigDecimal	MontoMovimientoComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobroSP_Entrada extends BcsPojo{
		public Long			IdMovimientoComprobanteCobro;
		public Long			IdMovimientoComprobante;
        public String		IdCobro;
        public String		IdEstado;
        public BigDecimal	Porcentaje;
        public String		IdMonedaCobro;
        public BigDecimal	MontoCobro;
        public String		IdMonedaMovimientoComprobanteCobro;
        public BigDecimal	MontoMovimientoComprobanteCobro;
        public String		IdMonedaLiquidacionCobro;
        public BigDecimal	MontoLiquidacionCobro;
        public BigDecimal	MontoMovimientoComprobante;
    }
	
	public static class DatosLogClienteModificarFormaDePago extends BcsPojo{
		public Long IdMovimientoComprobante;
		public Long IdMovimientoComprobanteFormaDePago;
		public String NumeroCuentaBancariaAntigua;
		public String IdEstadoAntiguo;
		public Long IdCliente;
		public String NombreCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
	}
	
	public static class DatosLogClienteModificarGenerales extends BcsPojo{
		public Long IdMovimientoComprobante;
		public String EsComprobIndividualAntiguo;
		public String GlosaAntigua;
		public String IdFormaCustodiaAntigua;
		public Long IdPortafolioAntiguo;
		public Long IdCliente;
		public String NombreCliente;
		public String DocumentoIdentidad;
	}
	
	//SALIDAS
	public static class ResultadoProcesoSP_Salida {
		public Long p_out_ErroresContador;
		public Long p_out_InformacionContador;
		public Long p_out_AdvertenciasContador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarMovimientoComprobanteSP_Salida extends BcsPojo{
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProcesoSP_Salida>		P_OUT_RESULTADOPROCESO;
		
		
		public ModificarMovimientoComprobanteSP_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};
	
	public static class InformacionOUT{
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ModificarMovimientoComprobanteSP_Entrada jin=gson.fromJson(in.toString() ,ModificarMovimientoComprobanteSP_Entrada.class);
		ModificarMovimientoComprobanteSP_Salida out=logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		List<MensajeSalida> l = new ArrayList<>();
		
		//PARCIALIADES AUN NO
		if (out.P_OUT_ERRORESCONTADOR != null && out.P_OUT_ERRORESCONTADOR > 0) {
			for (MensajeSalida msj : out.P_OUT_MENSAJES) {
				if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) {
					l.add(msj);	
				}
			}
			out.P_OUT_MENSAJES = l;
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	@Trace
	public ModificarMovimientoComprobanteSP_Salida logica(Connection conn, ModificarMovimientoComprobanteSP_Entrada in) throws Exception {
		logger.info("Inicio ModificarMovimientoComprobante");
		ModificarMovimientoComprobanteSP_Salida out = new ModificarMovimientoComprobanteSP_Salida();
		SQL _sql = new SQL();
		
		List<DatosLogClienteModificarFormaDePago> listaDatosClienteLog1 = new ArrayList();
		HashMap<Long,Object> hashClienteLog1 = new HashMap();
		
		List<DatosLogClienteModificarGenerales> listaDatosClienteLog2 = new ArrayList();
		HashMap<Long,Object> hashClienteLog2 = new HashMap();
		
		if (in.P_MOVIMIENTOFACTURARGRAL.size() == 0 && in.P_MOVIMIENTOFACTURARFDPAGO.size() == 0 && in.P_MOVIMIENTOFACTURARCOBROS.size() == 0) {
			String codigo = "NO_VIENEN_MOVIMIENTOS";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		if (in.P_MOVIMIENTOFACTURARGRAL.size() != 0) {
			List<MovimientoComprobanteSP_Entrada> movimientos = new ArrayList<MovimientoComprobanteSP_Entrada>();
			movimientos.addAll(in.P_MOVIMIENTOFACTURARGRAL);
			_sql.batch(conn, SQL_INSERT_MOVIMIENTO, movimientos, MovimientoComprobanteSP_Entrada.class);
			
			List<Long> listaIdsComprobante = in.P_MOVIMIENTOFACTURARGRAL.stream().map(p_movimiento2 ->{
				return p_movimiento2.IdMovimientoComprobante;
			}).distinct().collect(Collectors.toList());
			
			if (!listaIdsComprobante.isEmpty()) {
					HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
					parmsIn2.put("IdMovimientoComprobante",listaIdsComprobante);

					listaDatosClienteLog2 = _sql.query(conn, SQL_SELECT_DATOS_CLIENTE_MODIFICAR_GENERALES, parmsIn2, DatosLogClienteModificarGenerales.class);
						
					hashClienteLog2 = Utils.List2Hash(listaDatosClienteLog2, "IdMovimientoComprobante");		
			}		
		}
		
		if (in.P_MOVIMIENTOFACTURARFDPAGO.size() != 0) {
			List<MovimientoComprobanteFormaDePagoSP_Entrada> movimientos = new ArrayList<MovimientoComprobanteFormaDePagoSP_Entrada>();
			movimientos.addAll(in.P_MOVIMIENTOFACTURARFDPAGO);
			_sql.batch(conn, SQL_INSERT_FORMADEPAGO, movimientos, MovimientoComprobanteFormaDePagoSP_Entrada.class);
			
			List<Long> listaIdsComprobanteFormaDePago = in.P_MOVIMIENTOFACTURARFDPAGO.stream().map(p_movimiento1 ->{
				return p_movimiento1.IdMovimientoComprobanteFormaDePago;
			}).distinct().collect(Collectors.toList());
			
			if (!listaIdsComprobanteFormaDePago.isEmpty()) {
					HashMap<String,Object> parmsIn1 = new HashMap<String,Object>();
					parmsIn1.put("IdMovimientoComprobanteFormaDePago",listaIdsComprobanteFormaDePago);

					listaDatosClienteLog1 = _sql.query(conn, SQL_SELECT_DATOS_CLIENTE_MODIFICAR_FORMA_DE_PAGO, parmsIn1, DatosLogClienteModificarFormaDePago.class);
						
					hashClienteLog1 = Utils.List2Hash(listaDatosClienteLog1, "IdMovimientoComprobanteFormaDePago");		
			}
		}
		
		if (in.P_MOVIMIENTOFACTURARCOBROS.size() != 0) {
			List<MovimientoComprobanteCobroSP_Entrada> movimientos = new ArrayList<MovimientoComprobanteCobroSP_Entrada>();
			movimientos.addAll(in.P_MOVIMIENTOFACTURARCOBROS);
			_sql.batch(conn, SQL_INSERT_MOVIMIENTO, movimientos, MovimientoComprobanteCobroSP_Entrada.class);
		}
		
		//SQL_CALL_MODIFICARMOVIMIENTO
		HashMap<String,Object> paramsInstrumentoIn = new HashMap<String,Object>();
		HashMap<String,Class> paramsInstrumentoOut = new HashMap<String,Class>();
				
		paramsInstrumentoIn.put("p_IdEmpresa", in.p_IdEmpresa);
		paramsInstrumentoIn.put("p_IdUsuario", in.p_IdUsuario);
		paramsInstrumentoIn.put("p_IdModulo", in.p_IdModulo);
		paramsInstrumentoIn.put("p_simularOperacion", in.p_simularOperacion);
		paramsInstrumentoIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
		paramsInstrumentoIn.put("p_EsIngresoAutomatico", in.p_EsIngresoAutomatico);
		paramsInstrumentoOut.put("p_out_InformacionContador", Long.class);
		paramsInstrumentoOut.put("p_out_AdvertenciasContador", Long.class);
		paramsInstrumentoOut.put("p_out_ErroresContador", Long.class);
		
		HashMap<String,Object> rptMovimiento = _sql.call(conn, SQL_CALL_MODIFICARMOVIMIENTO, paramsInstrumentoIn, paramsInstrumentoOut);
		
		// Modificar referencia
		List<ReferenciaTipoDocumento> listaReferenciaParaEliminar = new ArrayList<>();
		List<ReferenciaTipoDocumento> listaReferenciaParaInsertar = new ArrayList<>();
		if(in.P_MOVIMIENTOFACTURARGRAL.size() > 0) {
			for(int i=0; i<in.P_MOVIMIENTOFACTURARGRAL.size(); i++) {
				String inReferencia = in.P_MOVIMIENTOFACTURARGRAL.get(i).Referencia != null? in.P_MOVIMIENTOFACTURARGRAL.get(i).Referencia.trim(): "";
				String inTipoReferencia = in.P_MOVIMIENTOFACTURARGRAL.get(i).TipoReferencia != null? in.P_MOVIMIENTOFACTURARGRAL.get(i).TipoReferencia.trim(): "";
				String inIdMovimientoComprobante = in.P_MOVIMIENTOFACTURARGRAL.get(i).IdMovimientoComprobante.toString();
				ReferenciaTipoDocumento referenciaTipoDocumento = new ReferenciaTipoDocumento();
				referenciaTipoDocumento.Referencia = inReferencia;
				referenciaTipoDocumento.TipoReferencia = inTipoReferencia;
				referenciaTipoDocumento.IdMovimientoComprobante = inIdMovimientoComprobante;
				referenciaTipoDocumento.IdComprobante = "";
				listaReferenciaParaEliminar.add(referenciaTipoDocumento);
				if (inReferencia.length() > 0 && inTipoReferencia.length() > 0) {					
					listaReferenciaParaInsertar.add(referenciaTipoDocumento);
				}
			}
		}
		
		if (listaReferenciaParaEliminar.size()>0) {
			_sql.batch(conn, SQL_DELETE_MOVIMIENTO_COMPROBANTE_REFERENCIA, listaReferenciaParaEliminar, ReferenciaTipoDocumento.class);	
		}
		
		if (listaReferenciaParaInsertar.size()>0) {
			_sql.batch(conn, SQL_INSERT_MOVIMIENTO_COMPROBANTE_REFERENCIA, listaReferenciaParaInsertar, ReferenciaTipoDocumento.class);	
		}
		
		HashMap<String, Object> params = new HashMap<>();
		List<InformacionOUT> listInformacionSalida = _sql.query(conn, SQL_SELECT_INFORMACIONOUT, params, InformacionOUT.class);
		
		for(InformacionOUT informacionSalida : listInformacionSalida) {
			MensajeSalida msj = new MensajeSalida();
			msj.codigo = informacionSalida.Codigo;
			msj.descripcion = informacionSalida.Descripcion;
			msj.id = 0L;
			msj.tipo = MensajeSalida.MENSAJE_INFORMACION;
			out.P_OUT_MENSAJES.add(msj);
		}
		
		out.P_OUT_ERRORESCONTADOR = (Long) rptMovimiento.get("p_out_ErroresContador");
		
		ResultadoProcesoSP_Salida resultadoModificacion = new ResultadoProcesoSP_Salida();
		resultadoModificacion.p_out_AdvertenciasContador = (Long) rptMovimiento.get("p_out_AdvertenciasContador");
		resultadoModificacion.p_out_InformacionContador = (Long) rptMovimiento.get("p_out_InformacionContador");
		resultadoModificacion.p_out_ErroresContador = (Long) rptMovimiento.get("p_out_ErroresContador");
		out.P_OUT_RESULTADOPROCESO.add(resultadoModificacion);
		
		//agregar log
		if(out.P_OUT_ERRORESCONTADOR == 0) {
			
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.FechaSistema");
			
			if (in.P_MOVIMIENTOFACTURARFDPAGO != null && in.P_MOVIMIENTOFACTURARFDPAGO.size() != 0) {
				
				for (MovimientoComprobanteFormaDePagoSP_Entrada datosLog1: in.P_MOVIMIENTOFACTURARFDPAGO) {
	            	
					DatosLogClienteModificarFormaDePago DatosClienteLog1 = new DatosLogClienteModificarFormaDePago();
					if(hashClienteLog1.size() > 0) {
						DatosClienteLog1 = (DatosLogClienteModificarFormaDePago) hashClienteLog1.get(datosLog1.IdMovimientoComprobanteFormaDePago); 
					}
										
					AgregarLog.InAgregarLog.Log log1 = new AgregarLog.InAgregarLog.Log();
					log1.IdCabeceraLog = datosLog1.IdMovimientoComprobante != null ? datosLog1.IdMovimientoComprobante : 0L;
					log1.IdTipoLog = "MODIFICACION";
					log1.IdModulo = "FAC";
					log1.IdUsuario = in.p_IdUsuario;
					log1.IdEmpresa = in.p_IdEmpresa;
					log1.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
					log1.IdUsuarioAtendido = in.p_IdUsuario;
					log1.IdCliente = DatosClienteLog1.IdCliente != null ? DatosClienteLog1.IdCliente : 0L;
					log1.EstadoCliente = "";
					log1.NombreCliente = DatosClienteLog1.NombreCliente != null ? DatosClienteLog1.NombreCliente : "";
					log1.IdTipoDocumentoIdentidad = "";
					log1.IdDocumentoIdentidad = DatosClienteLog1.DocumentoIdentidad != null ? DatosClienteLog1.DocumentoIdentidad : "";
					log1.IdPortafolio = DatosClienteLog1.IdPortafolio != null ? DatosClienteLog1.IdPortafolio : 0L;
					log1.EstadoPortafolio = "";
					log1.IdTipoRegistro = "MODIFICAR_MOVIMIENTO_COMPROBANTE";
					log1.IdRegistro = datosLog1.IdMovimientoComprobante != null ? datosLog1.IdMovimientoComprobante.toString() : "";
					log1.EstadoRegistro = "";
					log1.IdTipoOperacion = "";
					log1.Folio = 0L;
					log1.Cantidad = BigDecimal.ZERO;
					log1.PrecioTasa = BigDecimal.ZERO;
					log1.Monto = BigDecimal.ZERO;
					log1.IdMonedaOrigen = "";
					log1.IdMonedaLiquidacion = "";
					log1.TipoCambio = "";
					log1.Nemo = "";
					log1.IdProceso = 0L;
					log1.EstadoProceso = "";
					log1.NombreProceso = "ModificarMovimientoComprobanteSP";						
					entradaAgregarLog.p_Log.add(log1);
					
					//detalle log 
					if (datosLog1.NumeroCuentaBancaria != null && !datosLog1.NumeroCuentaBancaria.equals(DatosClienteLog1.NumeroCuentaBancariaAntigua)) {
						AgregarLog.InAgregarLog.DetalleLog detLog1 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog1.IdCabeceraLog = datosLog1.IdMovimientoComprobante != null ? datosLog1.IdMovimientoComprobante : 0L;
						detLog1.IdTrabajo = 0L;
						detLog1.SubRegistro = datosLog1.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog1.IdMovimientoComprobante.toString() : "";
						detLog1.CampoRegistro = "NumeroCuentaBancaria : ";
						detLog1.ValorAntiguo = DatosClienteLog1.NumeroCuentaBancariaAntigua != null ? DatosClienteLog1.NumeroCuentaBancariaAntigua : "";
						detLog1.ValorNuevo = datosLog1.NumeroCuentaBancaria;
						entradaAgregarLog.p_LogDetalle.add(detLog1);
					}
					
					if (datosLog1.IdEstado != null && !datosLog1.IdEstado.equals(DatosClienteLog1.IdEstadoAntiguo)) {
						AgregarLog.InAgregarLog.DetalleLog detLog2 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog2.IdCabeceraLog = datosLog1.IdMovimientoComprobante != null ? datosLog1.IdMovimientoComprobante : 0L;
						detLog2.IdTrabajo = 0L;
						detLog2.SubRegistro = datosLog1.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog1.IdMovimientoComprobante.toString() : "";
						detLog2.CampoRegistro = "IdEstado : ";
						detLog2.ValorAntiguo = DatosClienteLog1.IdEstadoAntiguo != null ? DatosClienteLog1.IdEstadoAntiguo : "";
						detLog2.ValorNuevo = datosLog1.IdEstado;
						entradaAgregarLog.p_LogDetalle.add(detLog2);
					}
				} 
			}
			
			if (in.P_MOVIMIENTOFACTURARGRAL != null && in.P_MOVIMIENTOFACTURARGRAL.size() != 0) {
				
				for (MovimientoComprobanteSP_Entrada datosLog2: in.P_MOVIMIENTOFACTURARGRAL) {
					
					DatosLogClienteModificarGenerales DatosClienteLog2 = new DatosLogClienteModificarGenerales();
					if(hashClienteLog2.size() > 0) {
						DatosClienteLog2 = (DatosLogClienteModificarGenerales) hashClienteLog2.get(datosLog2.IdMovimientoComprobante); 
					}
	            	
					AgregarLog.InAgregarLog.Log log2 = new AgregarLog.InAgregarLog.Log();
					log2.IdCabeceraLog = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante : 0L;
					log2.IdTipoLog = "MODIFICACION";
					log2.IdModulo = "FAC";
					log2.IdUsuario = in.p_IdUsuario;
					log2.IdEmpresa = in.p_IdEmpresa;
					log2.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
					log2.IdUsuarioAtendido = in.p_IdUsuario;
					log2.IdCliente = DatosClienteLog2.IdCliente != null ? DatosClienteLog2.IdCliente : 0L;
					log2.EstadoCliente = "";
					log2.NombreCliente = DatosClienteLog2.NombreCliente != null ? DatosClienteLog2.NombreCliente : "";
					log2.IdTipoDocumentoIdentidad = "";
					log2.IdDocumentoIdentidad = DatosClienteLog2.DocumentoIdentidad != null ? DatosClienteLog2.DocumentoIdentidad : "";
					if (datosLog2.IdPortafolio != null && !datosLog2.IdPortafolio.equals(DatosClienteLog2.IdPortafolioAntiguo)) {
						log2.IdPortafolio = datosLog2.IdPortafolio;
					}else {
						log2.IdPortafolio = DatosClienteLog2.IdPortafolioAntiguo != null ? DatosClienteLog2.IdPortafolioAntiguo : 0L;
					}
					log2.EstadoPortafolio = "";
					log2.IdTipoRegistro = "MODIFICAR_MOVIMIENTO_COMPROBANTE";
					log2.IdRegistro = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante.toString() : "";
					log2.EstadoRegistro = "";
					log2.IdTipoOperacion = "";
					log2.Folio = 0L;
					log2.Cantidad = BigDecimal.ZERO;
					log2.PrecioTasa = BigDecimal.ZERO;
					log2.Monto = BigDecimal.ZERO;
					log2.IdMonedaOrigen = "";
					log2.IdMonedaLiquidacion = "";
					log2.TipoCambio = "";
					log2.Nemo = "";
					log2.IdProceso = 0L;
					log2.EstadoProceso = "";
					log2.NombreProceso = "ModificarMovimientoComprobanteSP";						
					entradaAgregarLog.p_Log.add(log2);
					
					//detalle log 
					if (datosLog2.IdFormaCustodia != null && !datosLog2.IdFormaCustodia.equals(DatosClienteLog2.IdFormaCustodiaAntigua)) {
						AgregarLog.InAgregarLog.DetalleLog detLog3 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog3.IdCabeceraLog = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante : 0L;
						detLog3.IdTrabajo = 0L;
						detLog3.SubRegistro = datosLog2.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog2.IdMovimientoComprobante.toString() : "";
						detLog3.CampoRegistro = "IdFormaCustodia : ";
						detLog3.ValorAntiguo = DatosClienteLog2.IdFormaCustodiaAntigua != null ? DatosClienteLog2.IdFormaCustodiaAntigua : "";
						detLog3.ValorNuevo = datosLog2.IdFormaCustodia;
						entradaAgregarLog.p_LogDetalle.add(detLog3);
					}
					
					if (datosLog2.IdPortafolio != null && !datosLog2.IdPortafolio.equals(DatosClienteLog2.IdPortafolioAntiguo)) {
						AgregarLog.InAgregarLog.DetalleLog detLog4 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog4.IdCabeceraLog = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante : 0L;
						detLog4.IdTrabajo = 0L;
						detLog4.SubRegistro = datosLog2.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog2.IdMovimientoComprobante.toString() : "";
						detLog4.CampoRegistro = "IdPortafolio : ";
						detLog4.ValorAntiguo = DatosClienteLog2.IdPortafolioAntiguo != null ? DatosClienteLog2.IdPortafolioAntiguo.toString() : "";
						detLog4.ValorNuevo = datosLog2.IdPortafolio.toString();
						entradaAgregarLog.p_LogDetalle.add(detLog4);
					}
					
					if (datosLog2.Glosa != null && !datosLog2.Glosa.equals(DatosClienteLog2.GlosaAntigua)) {
						AgregarLog.InAgregarLog.DetalleLog detLog5 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog5.IdCabeceraLog = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante : 0L;
						detLog5.IdTrabajo = 0L;
						detLog5.SubRegistro = datosLog2.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog2.IdMovimientoComprobante.toString() : "";
						detLog5.CampoRegistro = "Comentario : ";
						detLog5.ValorAntiguo = DatosClienteLog2.GlosaAntigua != null ? DatosClienteLog2.GlosaAntigua : "";
						detLog5.ValorNuevo = datosLog2.Glosa;
						entradaAgregarLog.p_LogDetalle.add(detLog5);
					}
					if (datosLog2.EsComprobIndividual != null && !datosLog2.EsComprobIndividual.equals(DatosClienteLog2.EsComprobIndividualAntiguo)) {
						AgregarLog.InAgregarLog.DetalleLog detLog6 = new AgregarLog.InAgregarLog.DetalleLog();
						detLog6.IdCabeceraLog = datosLog2.IdMovimientoComprobante != null ? datosLog2.IdMovimientoComprobante : 0L;
						detLog6.IdTrabajo = 0L;
						detLog6.SubRegistro = datosLog2.IdMovimientoComprobante != null ? "IdComprobante: " + datosLog2.IdMovimientoComprobante.toString() : "";
						detLog6.CampoRegistro = "Comprobante Individual : ";
						detLog6.ValorAntiguo = DatosClienteLog2.EsComprobIndividualAntiguo != null ? DatosClienteLog2.EsComprobIndividualAntiguo : "";
						detLog6.ValorNuevo = datosLog2.EsComprobIndividual;
						entradaAgregarLog.p_LogDetalle.add(detLog6);
					}
				}
			}
				
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);	
		}

		return out;
			
	};
	
	public static void main(String[] args) throws Exception {		
		try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();					

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"P_MOVIMIENTOFACTURARFDPAGO\":[{\"IdMovimientoComprobanteFormaDePago\":0,\"IdMovimientoComprobante\":\"142348\",\"IdTipoMovimiento\":\"CMACNA\",\"IdMedioDePago\":\"CIN\",\"NombreMedioDePago\":\"CUENTA INVERSION\",\"IdModalidadDePago\":\"MODCAJ\",\"NombreModalidadDePago\":\"POR CAJA\",\"MontoLiquidacionFormaDePago\":\"0\",\"IdMoneda\":\"CLP\",\"IdBanco\":null,\"CodigoBanco\":\"\",\"NombreBanco\":null,\"NumeroCuentaBancaria\":null,\"IdTarjetaBancaria\":0,\"FechaVctoTarjetaBancaria\":\"\",\"IdTipoCuentaBancaria\":null,\"NombreTipoCuentaBancaria\":null,\"NumeroSerie\":null,\"NumeroDocumento\":null,\"FechaDocumentoPago\":null,\"IdTipoTarjetaBancaria\":null,\"NumeroTarjetaBancaria\":\"\",\"IdTipoMovimientoSpot\":null,\"AfectarTesoreria\":null,\"MontoMovimientoComprobante\":144975}]}";
				
				ModificarMovimientoComprobanteSP_Entrada Objin = gson.fromJson(json, ModificarMovimientoComprobanteSP_Entrada.class);			
				ModificarMovimientoComprobanteSP modificarMvtoComp = new ModificarMovimientoComprobanteSP();
				ModificarMovimientoComprobanteSP_Salida Objout = modificarMvtoComp.logica(con, Objin);				
				System.out.println(Objout.toString());

				//con.rollback();
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};
};