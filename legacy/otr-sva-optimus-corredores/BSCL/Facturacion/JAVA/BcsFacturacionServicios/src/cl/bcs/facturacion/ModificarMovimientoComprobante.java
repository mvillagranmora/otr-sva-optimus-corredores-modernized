package cl.bcs.facturacion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.facturacion.pojo.MovimientoComprobante;
import cl.bcs.facturacion.pojo.MovimientoComprobanteCobro;
import cl.bcs.facturacion.pojo.MovimientoComprobanteDetalle;
import cl.bcs.facturacion.pojo.MovimientoComprobanteFormaDePago;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ModificarMovimientoComprobante implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ModificarMovimientoComprobante.class);
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobante_SelectMovimientos.sql");
	private final String SQL_SELECT_MOVIMIENTOS_COBROS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobante_SelectMovimientosCobros.sql");
	private final String SQL_SELECT_MOVIMIENTOS_FORMAS_DE_PAGO = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobante_SelectMovimientosFormasDePago.sql");
	
	private final String SQL_UPDATE_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/ModificarMovimientoComprobante_UpdateMovimientos.sql");

	private final String SQL_SELECT_ORDENES_MODIFICADAS = loadResourceToString("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
	private final String SQL_SELECT_MODALIDAD_PAGO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql");
	
	private final String SQL_BLOQUEA_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/CrearComprobante_BloqueaMovimientos.sql");
		
	
	private static final String NO_APLICA_FUSION= "NO_APLICA_FUSION";
	private static final String FUSION_PRECIO_PROMEDIO= "FAC_FUSIONYAGRUPACIONPORPRECIOPROMEDIO";
	private static final String FUSION_SEPARA_PRECIO= "FAC_FUSIONYSEPARAPORPRECIO";
	
	
	public List<String> camposComprobante = new ArrayList<>(); 
	
	List<MovimientoComprobante_Proceso> movimientos = new ArrayList<MovimientoComprobante_Proceso>();
	List<MovimientoComprobante_Proceso> movimientosError = new ArrayList<MovimientoComprobante_Proceso>();
	
	//ENTRADAS
	public static class ModificarMovimientoComprobante_Entrada extends BcsPojo {
		public Long								p_IdEmpresa;		
		public Long								p_IdUsuario;   
		public String							p_IdModulo;   
		public String							p_simularOperacion;				// S/N se usa para simular operacion (algunos casos)
		public String							p_UsaReglasNegocio;				// S/N se usa en caso de servicios anidados.
		public String							p_EsIngresoAutomatico = "N";	// S/N si viene de otro modulo es S para no validar requisitos basicos nuevamente
		public Long 							p_IdPortafolioAConfigurar;
		public List<MovimientoComprobante_Entrada>				P_MOVIMIENTOFACTURAR;
		public List<MovimientoComprobanteCobro_Entrada>			P_MOVIMIENTOFACTURARCOBRO;
		public List<MovimientoComprobanteFormaDePago_Entrada>	P_MOVIMIENTOFACTURARFORMAPAGO;
		
	    public ModificarMovimientoComprobante_Entrada (){
	    	P_MOVIMIENTOFACTURAR	= new ArrayList<MovimientoComprobante_Entrada>();
	    	P_MOVIMIENTOFACTURARCOBRO = new ArrayList<MovimientoComprobanteCobro_Entrada>();
	    	P_MOVIMIENTOFACTURARFORMAPAGO = new ArrayList<MovimientoComprobanteFormaDePago_Entrada>();
		}
	    
	};
	
	public static class MovimientoComprobante_Entrada extends BcsPojo{
		public Long					IdMovimientoComprobante;
	};
	
	//SALIDAS
	public static class ResultadoProceso_Salida {
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ModificarMovimientoComprobante_Salida extends BcsPojo{
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;
		
		
		public ModificarMovimientoComprobante_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};
	public static class MovimientoComprobanteFormaDePago_Entrada extends MovimientoComprobanteFormaDePago{
		/*                  
		 *  "MontoMovimientoComprobante" DECIMAL(26, 8) CS_FIXED DEFAULT 0
		 * */
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobante_Proceso {
		public Long IdMovimientoComprobante;
		public String FechaComprobante;
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;

		/* VALIDACIONES */
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class MovimientoComprobanteCobro_Entrada extends BcsPojo{
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

	public static class MovimientoComprobanteDetalle_Proceso extends MovimientoComprobanteDetalle {
		public MovimientoComprobante_Proceso movimientoComprobanteProceso;
		
		public String Accion;
		public String ActualizarTesoreria;
		public String ActualizarCustodia;
		public String ActualizarCuentaInversion;
		public String CodigoBroker;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public BigDecimal CorteMinimo;
		public BigDecimal DiasPlazo;
		public String DireccionFacturacion;
		public String DocumentoIdentidad;
		public String EsClienteFinal;
		public String EsComprobIndividual;
		public String EsAPlazo;
		public String EsFungible;
		public java.sql.Timestamp FechaComprobante;
		public Date FechaComprobanteReferencia;
		public java.sql.Date FechaLiquidacion;
		public Timestamp FechaVencimiento;
		public Long FolioCartera; //para vender
		public Long FolioComprobanteReferencia;
		public String Glosa;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCliente;
		public Long IdComprobante;
		public Long IdComprobanteReferencia;
		public String IdCondicionDeLiquidacion;
		public Long IdCuentaCustodioPortafolio;
		public String IdEmisor;
		public String IdEmisorSubyacente;
		public Long IdEmpresa;
		public Long IdOrden;
		public Long IdPersona;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public Long IdPersonaBroker;
		public Long IdPortafolio;
		public String IdTipoCanalIngreso;
		public String IdTipoCliente;
		public String IdTipoComprobante;
		public String IdTipoComprobanteReferencia;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public BigDecimal Intereses;
		public String LiquidaBancoCustodio;
		public String MercadoSMV;
		public String ModalidadSMV;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreLargoCliente;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreTipoCliente;
		public BigDecimal ReajustesVAC;
		public String TipoDocumentoIdentidad;
		public String TipoComprobanteFacturacion;
		
		public Long IdOperacion;

		public BigDecimal MontoAPlazo;
		public BigDecimal PrecioAPlazo;
		public String CodigoPortafolio;


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoComprobanteCobro_Proceso extends MovimientoComprobanteCobro {
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CodigoPortafolio
	{
		public String CodigoPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoPortafolioUpdate extends BcsPojo
	{
		//public List<Long> IdMovimientoComprobante;
		public Long IdMovimientoComprobante;
		public Long IdPortafolio;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ModificarMovimientoComprobante_Entrada jin=gson.fromJson(in.toString() ,ModificarMovimientoComprobante_Entrada.class);
		ModificarMovimientoComprobante_Salida out=logica(conn,jin);		
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
	public ModificarMovimientoComprobante_Salida logica(Connection conn, ModificarMovimientoComprobante_Entrada in) throws Exception {
		logger.info("Inicio ModificarMovimientoComprobante");
		ModificarMovimientoComprobante_Salida out = new ModificarMovimientoComprobante_Salida();
		SQL _sql = new SQL();
		
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		//BigDecimal procentajeRetencionAPVB = new BigDecimal(100);
		
		if (in.P_MOVIMIENTOFACTURAR.size() == 0) {
			String codigo = "NO_VIENEN_MOVIMIENTOS";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}

		ArrayList<Long> idsMovimientoComprobante = new ArrayList<Long>();
		for (MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
			if (!idsMovimientoComprobante.contains(mov.IdMovimientoComprobante)) {
				idsMovimientoComprobante.add(mov.IdMovimientoComprobante);
			}
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("idsMovimientoComprobante", idsMovimientoComprobante);
		
		try {
			_sql.query(conn, SQL_BLOQUEA_MOVIMIENTOS, params, MovimientoComprobante.class);
		}catch(SQLException sqle) {
			if(sqle.getErrorCode()==131) {
				//error especifico porque el movimiento que se esta tratando de obtener ya esta siendo procesado.
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
				return out;
			}else {
				throw sqle;
			}
		}
		
		List<MovimientoPortafolioUpdate> movsUpdate = new ArrayList<MovimientoPortafolioUpdate>();
		
		for(MovimientoComprobante_Entrada mov : in.P_MOVIMIENTOFACTURAR) {
			MovimientoPortafolioUpdate mvto = new MovimientoPortafolioUpdate();
			mvto.IdMovimientoComprobante = mov.IdMovimientoComprobante;
			mvto.IdPortafolio = in.p_IdPortafolioAConfigurar;
			movsUpdate.add(mvto);
		}
		
		try {
			_sql.batch(conn, SQL_UPDATE_MOVIMIENTOS, movsUpdate, MovimientoPortafolioUpdate.class);
		}catch(SQLException sqle) {
			if(sqle.getErrorCode()==131) {
				//error especifico porque el movimiento que se esta tratando de obtener ya esta siendo procesado.
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
				return out;
			}else {
				throw sqle;
			}
		}
		
		return out;
			
	};
	
	private void ProcesaRequisito(Connection conn,Long idEmpresa,boolean esError,String codMsg,String codRequisito,MovimientoComprobante_Proceso mov,HashMap<String, Object> requisitos,List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (esError) {
			String msg = Diccionario.getMensaje(conn, idEmpresa,codMsg)
					.add("IdMovimientoComprobante", mov.IdMovimientoComprobante)
					.msg();
			
			if (codRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mov.errores++;
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				mov.errores++;						
			}else if (codRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				mov.autorizaciones++;						
			}
			else if (codRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoComprobante,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				mov.advertencias++;
			}
			else {
				mov.ok++;
			}
		}
		else {
			mov.ok++;
		}
	}
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();					

				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"FAC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_EsIngresoAutomatico\":\"N\",\"p_IdPortafolioAConfigurar\":1451774,\"P_MOVIMIENTOFACTURAR\":[{\"IdMovimientoComprobante\":141477},{\"IdMovimientoComprobante\":141476}]}";
				
				ModificarMovimientoComprobante_Entrada Objin = gson.fromJson(json, ModificarMovimientoComprobante_Entrada.class);			
				ModificarMovimientoComprobante modificarMvtoComp = new ModificarMovimientoComprobante();
				ModificarMovimientoComprobante_Salida Objout = modificarMvtoComp.logica(con, Objin);				
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